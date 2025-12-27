import io
import json
import logging
import os
import threading
import time
import uuid
from datetime import datetime, timezone
from typing import Dict, List, Optional

from fastapi import Depends, FastAPI, HTTPException, Response
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sqlalchemy import JSON, Column, DateTime, Float, String, create_engine, func, text
from sqlalchemy.exc import OperationalError
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import Session, sessionmaker

from confluent_kafka import Consumer, KafkaError, Producer
from minio import Minio
from minio.error import S3Error
from prometheus_client import CONTENT_TYPE_LATEST, generate_latest

# -----------------------------------------------------------------------------
# Config
# -----------------------------------------------------------------------------

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
log = logging.getLogger("lcalite")

DATABASE_URL = os.getenv(
    "DATABASE_URL",
    f"postgresql://{os.getenv('DB_USERNAME', 'eco_user')}:{os.getenv('DB_PASSWORD', 'eco_pass')}"
    f"@{os.getenv('DB_HOST', 'postgres')}:{os.getenv('DB_PORT', '5432')}/{os.getenv('DB_NAME', 'eco_label')}",
)

KAFKA_BOOTSTRAP_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092")
KAFKA_GROUP_ID = os.getenv("KAFKA_GROUP_ID", "lcalite-service")
INGREDIENTS_TOPIC = os.getenv("INGREDIENTS_TOPIC", "ingredients.normalized")
DLQ_TOPIC = os.getenv("DLQ_TOPIC", "ingredients.normalized.dlq")
LCA_COMPLETED_TOPIC = os.getenv("LCA_COMPLETED_TOPIC", "lca.completed")

MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "http://minio:9000")
MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", "minioadmin")
MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY", "minioadmin")
MINIO_BUCKET = os.getenv("MINIO_BUCKET", "ecolabel-acv")
MINIO_SECURE = os.getenv("MINIO_SECURE", "false").lower() == "true"

APP_PORT = int(os.getenv("PORT", "8083"))
ARTIFACT_PREFIX = "reports"

# -----------------------------------------------------------------------------
# DB setup
# -----------------------------------------------------------------------------

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


class LcaIngredientFactor(Base):
    __tablename__ = "lca_ingredient_factors"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    category = Column(String, nullable=False)
    name = Column(String, nullable=True)
    co2_kg_per_kg = Column(Float, nullable=False)
    water_l_per_kg = Column(Float, nullable=False)
    energy_mj_per_kg = Column(Float, nullable=False)
    version = Column(String, nullable=False, default="v1")
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)


class LcaPackagingFactor(Base):
    __tablename__ = "lca_packaging_factors"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    material = Column(String, nullable=False)
    co2_kg_per_kg = Column(Float, nullable=False)
    water_l_per_kg = Column(Float, nullable=False)
    energy_mj_per_kg = Column(Float, nullable=False)
    version = Column(String, nullable=False, default="v1")
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)


class LcaTransportFactor(Base):
    __tablename__ = "lca_transport_factors"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    mode = Column(String, nullable=False)
    co2_kg_per_tkm = Column(Float, nullable=False)
    water_l_per_tkm = Column(Float, nullable=False, default=0)
    energy_mj_per_tkm = Column(Float, nullable=False, default=0)
    version = Column(String, nullable=False, default="v1")
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)


class LcaResult(Base):
    __tablename__ = "lca_result"
    id = Column(String, primary_key=True, default=lambda: str(uuid.uuid4()))
    product_id = Column(String, index=True, nullable=False)
    co2_kg = Column(Float, nullable=False)
    water_l = Column(Float, nullable=False)
    energy_mj = Column(Float, nullable=False)
    breakdown = Column(JSON, nullable=False)
    factors_versions = Column(JSON, nullable=False)
    artifact_uri = Column(String, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)


class ProcessedEvent(Base):
    __tablename__ = "lca_processed_events"
    event_id = Column(String, primary_key=True)
    processed_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)


def seed_data(session: Session):
    if session.query(LcaIngredientFactor).count() == 0:
        log.info("Seeding ingredient factors (demo defaults)")
        defaults = [
            ("DAIRY", 9.2, 1000, 15),
            ("SWEETENER", 3.5, 600, 5),
            ("GRAIN", 2.1, 400, 4),
            ("MEAT", 27.0, 15000, 50),
            ("VEGETABLE", 0.9, 300, 2),
            ("FRUIT", 1.1, 500, 2.5),
            ("OIL", 5.0, 200, 8),
            ("OTHER", 1.0, 200, 4),
        ]
        for cat, co2, water, energy in defaults:
            session.add(
                LcaIngredientFactor(
                    category=cat,
                    co2_kg_per_kg=co2,
                    water_l_per_kg=water,
                    energy_mj_per_kg=energy,
                    version="v1",
                )
            )

    if session.query(LcaPackagingFactor).count() == 0:
        log.info("Seeding packaging factors (demo defaults)")
        defaults = [
            ("GLASS", 1.8, 150, 6),
            ("PLASTIC", 2.2, 100, 8),
            ("PAPER", 1.2, 80, 3),
            ("ALUMINUM", 10.0, 50, 20),
            ("OTHER", 2.0, 120, 5),
        ]
        for mat, co2, water, energy in defaults:
            session.add(
                LcaPackagingFactor(
                    material=mat,
                    co2_kg_per_kg=co2,
                    water_l_per_kg=water,
                    energy_mj_per_kg=energy,
                    version="v1",
                )
            )

    if session.query(LcaTransportFactor).count() == 0:
        log.info("Seeding transport factors (demo defaults)")
        defaults = [
            ("TRUCK", 0.1, 0.0, 0.05),
            ("SHIP", 0.015, 0.0, 0.01),
            ("AIR", 0.6, 0.0, 0.4),
        ]
        for mode, co2, water, energy in defaults:
            session.add(
                LcaTransportFactor(
                    mode=mode,
                    co2_kg_per_tkm=co2,
                    water_l_per_tkm=water,
                    energy_mj_per_tkm=energy,
                    version="v1",
                )
            )
    session.commit()


# -----------------------------------------------------------------------------
# Schemas
# -----------------------------------------------------------------------------

class IngredientPayload(BaseModel):
    name: str
    category: Optional[str] = Field(default="OTHER")
    quantity_g: Optional[float] = Field(default=0, description="Mass in grams")
    confidence: Optional[float] = None


class PackagingPayload(BaseModel):
    material: Optional[str] = Field(default="OTHER")
    weight_g: Optional[float] = Field(default=0)


class TransportPayload(BaseModel):
    mode: Optional[str] = Field(default="TRUCK")
    distance_km: Optional[float] = Field(default=0)
    weight_g: Optional[float] = Field(default=0, description="Weight in grams")


class LcaRequest(BaseModel):
    productId: uuid.UUID
    eventId: Optional[str] = None
    correlationId: Optional[str] = None
    occurredAt: Optional[datetime] = None
    ingredients: List[IngredientPayload] = []
    packaging: Optional[PackagingPayload] = None
    transport: Optional[TransportPayload] = None


class LcaResponse(BaseModel):
    resultId: str
    productId: str
    co2_kg: float
    water_l: float
    energy_mj: float
    breakdown: Dict[str, Dict[str, float]]
    factors_versions: Dict[str, str]
    artifact_uri: Optional[str] = None
    created_at: datetime
    eventId: Optional[str] = None
    correlationId: Optional[str] = None


# -----------------------------------------------------------------------------
# Utility
# -----------------------------------------------------------------------------

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def ensure_minio_bucket(client: Minio, bucket: str):
    if not client.bucket_exists(bucket):
        client.make_bucket(bucket)
        log.info("Created MinIO bucket %s", bucket)


def artifact_path(product_id: str, run_id: str) -> str:
    return f"{ARTIFACT_PREFIX}/{product_id}/{run_id}.json"


def upload_artifact(client: Minio, bucket: str, path: str, content: Dict):
    payload = json.dumps(content, default=str).encode("utf-8")
    client.put_object(
        bucket_name=bucket,
        object_name=path,
        data=io.BytesIO(payload),
        length=len(payload),
        content_type="application/json",
    )


def minio_client() -> Minio:
    endpoint = MINIO_ENDPOINT.replace("http://", "").replace("https://", "")
    return Minio(
        endpoint=endpoint,
        access_key=MINIO_ACCESS_KEY,
        secret_key=MINIO_SECRET_KEY,
        secure=MINIO_SECURE,
    )


# -----------------------------------------------------------------------------
# Calculation logic
# -----------------------------------------------------------------------------

def _get_ingredient_factor(session: Session, category: str) -> LcaIngredientFactor:
    cat = (category or "OTHER").upper()
    factor = (
        session.query(LcaIngredientFactor)
        .filter(LcaIngredientFactor.category == cat)
        .order_by(LcaIngredientFactor.updated_at.desc())
        .first()
    )
    if factor is None:
        factor = (
            session.query(LcaIngredientFactor)
            .filter(LcaIngredientFactor.category == "OTHER")
            .first()
        )
    return factor


def _get_packaging_factor(session: Session, material: Optional[str]) -> LcaPackagingFactor:
    mat = (material or "OTHER").upper()
    factor = (
        session.query(LcaPackagingFactor)
        .filter(LcaPackagingFactor.material == mat)
        .order_by(LcaPackagingFactor.updated_at.desc())
        .first()
    )
    if factor is None:
        factor = (
            session.query(LcaPackagingFactor)
            .filter(LcaPackagingFactor.material == "OTHER")
            .first()
        )
    return factor


def _get_transport_factor(session: Session, mode: Optional[str]) -> LcaTransportFactor:
    m = (mode or "TRUCK").upper()
    factor = (
        session.query(LcaTransportFactor)
        .filter(LcaTransportFactor.mode == m)
        .order_by(LcaTransportFactor.updated_at.desc())
        .first()
    )
    if factor is None:
        factor = (
            session.query(LcaTransportFactor)
            .filter(LcaTransportFactor.mode == "TRUCK")
            .first()
        )
    return factor


def compute_lca(session: Session, payload: LcaRequest) -> Dict:
    ingredients_total = {"co2_kg": 0.0, "water_l": 0.0, "energy_mj": 0.0}
    packaging_total = {"co2_kg": 0.0, "water_l": 0.0, "energy_mj": 0.0}
    transport_total = {"co2_kg": 0.0, "water_l": 0.0, "energy_mj": 0.0}

    ingredient_version = packaging_version = transport_version = "v1"

    for ing in payload.ingredients:
        factor = _get_ingredient_factor(session, ing.category)
        if factor:
            ingredient_version = factor.version
        qty_kg = (ing.quantity_g or 0) / 1000.0
        ingredients_total["co2_kg"] += qty_kg * (factor.co2_kg_per_kg if factor else 0)
        ingredients_total["water_l"] += qty_kg * (factor.water_l_per_kg if factor else 0)
        ingredients_total["energy_mj"] += qty_kg * (factor.energy_mj_per_kg if factor else 0)

    if payload.packaging:
        factor = _get_packaging_factor(session, payload.packaging.material)
        if factor:
            packaging_version = factor.version
        weight_kg = (payload.packaging.weight_g or 0) / 1000.0
        packaging_total["co2_kg"] += weight_kg * (factor.co2_kg_per_kg if factor else 0)
        packaging_total["water_l"] += weight_kg * (factor.water_l_per_kg if factor else 0)
        packaging_total["energy_mj"] += weight_kg * (factor.energy_mj_per_kg if factor else 0)

    if payload.transport:
        factor = _get_transport_factor(session, payload.transport.mode)
        if factor:
            transport_version = factor.version
        weight_kg = (payload.transport.weight_g or 0) / 1000.0
        ton_km = (weight_kg / 1000.0) * (payload.transport.distance_km or 0)
        transport_total["co2_kg"] += ton_km * (factor.co2_kg_per_tkm if factor else 0)
        transport_total["water_l"] += ton_km * (factor.water_l_per_tkm if factor else 0)
        transport_total["energy_mj"] += ton_km * (factor.energy_mj_per_tkm if factor else 0)

    co2 = round(ingredients_total["co2_kg"] + packaging_total["co2_kg"] + transport_total["co2_kg"], 4)
    water = round(ingredients_total["water_l"] + packaging_total["water_l"] + transport_total["water_l"], 4)
    energy = round(ingredients_total["energy_mj"] + packaging_total["energy_mj"] + transport_total["energy_mj"], 4)

    breakdown = {
        "ingredients": {k: round(v, 4) for k, v in ingredients_total.items()},
        "packaging": {k: round(v, 4) for k, v in packaging_total.items()},
        "transport": {k: round(v, 4) for k, v in transport_total.items()},
    }

    versions = {
        "ingredient_factors_version": ingredient_version,
        "packaging_version": packaging_version,
        "transport_version": transport_version,
    }

    return {
        "co2_kg": co2,
        "water_l": water,
        "energy_mj": energy,
        "breakdown": breakdown,
        "versions": versions,
    }


# -----------------------------------------------------------------------------
# Kafka wiring
# -----------------------------------------------------------------------------

producer: Optional[Producer] = None
consumer: Optional[Consumer] = None
minio_client_instance: Optional[Minio] = None


def ensure_minio():
    global minio_client_instance
    if minio_client_instance is not None:
        return
    try:
        client = minio_client()
        ensure_minio_bucket(client, MINIO_BUCKET)
        minio_client_instance = client
        log.info("MinIO client initialised")
    except Exception as exc:  # pragma: no cover
        log.error("MinIO client initialisation failed: %s", exc)


def publish_lca_completed(result: LcaResult, payload: LcaRequest, metrics: Dict):
    if producer is None:
        return
    event_id = payload.eventId or str(uuid.uuid4())
    occurred_at = (payload.occurredAt or datetime.now(timezone.utc)).isoformat()

    message = {
        "eventId": event_id,
        "productId": str(result.product_id),
        "resultId": str(result.id),
        "occurredAt": occurred_at,
        "co2Kg": metrics["co2_kg"],
        "waterLiters": metrics["water_l"],
        "energyMj": metrics["energy_mj"],
        "calculatedAt": result.created_at.isoformat() if result.created_at else occurred_at,
        "lca": {
            "co2_kg": metrics["co2_kg"],
            "water_l": metrics["water_l"],
            "energy_mj": metrics["energy_mj"],
        },
        "breakdown": metrics["breakdown"],
        "versions": metrics["versions"],
        "artifact_uri": result.artifact_uri,
    }
    if payload.correlationId:
        message["correlationId"] = payload.correlationId
    try:
        producer.produce(
            topic=LCA_COMPLETED_TOPIC,
            value=json.dumps(message).encode("utf-8"),
            key=str(payload.productId),
        )
        producer.flush()
        log.info("Published lca.completed for product %s", payload.productId)
    except Exception as exc:  # pragma: no cover - best-effort logging
        log.error("Failed to publish lca.completed: %s", exc)


def send_to_dlq(raw_value: bytes, headers: Dict[str, str] | None = None):
    if producer is None:
        return
    try:
        producer.produce(topic=DLQ_TOPIC, value=raw_value, headers=headers)
        producer.flush()
    except Exception as exc:  # pragma: no cover
        log.error("Failed to publish to DLQ: %s", exc)


def process_message(session: Session, msg):
    payload_dict = json.loads(msg.value())
    try:
        payload = LcaRequest(**payload_dict)
    except Exception as exc:
        log.error("Invalid message payload, sending to DLQ: %s", exc)
        send_to_dlq(msg.value())
        return

    if payload.eventId:
        exists = session.get(ProcessedEvent, payload.eventId)
        if exists:
            log.info("Event %s already processed, skipping", payload.eventId)
            return

    try:
        response = handle_lca(payload, session)
    except Exception as exc:
        log.error(
            "Failed to process LCA event product=%s eventId=%s correlationId=%s error=%s",
            payload.productId,
            payload.eventId,
            payload.correlationId,
            exc,
        )
        raise

    if payload.eventId:
        session.add(ProcessedEvent(event_id=payload.eventId))
        session.commit()

    publish_lca_completed(response["result_model"], payload, response["metrics"])


def consumer_loop():
    if consumer is None:
        log.warning("Kafka consumer not initialised")
        return
    consumer.subscribe([INGREDIENTS_TOPIC])
    log.info("Kafka consumer subscribed to %s", INGREDIENTS_TOPIC)

    while True:
        msg = consumer.poll(1.0)
        if msg is None:
            continue
        if msg.error():
            if msg.error().code() != KafkaError._PARTITION_EOF:
                log.error("Kafka error: %s", msg.error())
            continue
        session = SessionLocal()
        try:
            retries = 0
            while retries < 3:
                try:
                    process_message(session, msg)
                    consumer.commit(message=msg, asynchronous=False)
                    break
                except Exception as exc:
                    session.rollback()
                    retries += 1
                    log.error("Processing failed (%s), retry %s", exc, retries)
                    time.sleep(1 * retries)
            else:
                send_to_dlq(msg.value())
                consumer.commit(message=msg, asynchronous=False)
        finally:
            session.close()


# -----------------------------------------------------------------------------
# Core handler
# -----------------------------------------------------------------------------

def handle_lca(payload: LcaRequest, session: Session) -> Dict:
    metrics = compute_lca(session, payload)
    run_id = str(uuid.uuid4())
    ensure_minio()
    artifact_uri = None
    artifact = {
        "input": payload.model_dump(mode="json"),
        "result": metrics,
        "productId": str(payload.productId),
        "runId": run_id,
        "computed_at": datetime.now(timezone.utc).isoformat(),
    }

    artifact_key = artifact_path(str(payload.productId), run_id)
    if minio_client_instance:
        try:
            from io import BytesIO

            payload_bytes = json.dumps(artifact).encode("utf-8")
            minio_client_instance.put_object(
                bucket_name=MINIO_BUCKET,
                object_name=artifact_key,
                data=BytesIO(payload_bytes),
                length=len(payload_bytes),
                content_type="application/json",
            )
            artifact_uri = f"minio://{MINIO_BUCKET}/{artifact_key}"
        except Exception as exc:  # pragma: no cover - runtime behaviour
            log.warning(
                "MinIO upload failed for product %s run %s: %s",
                payload.productId,
                run_id,
                exc,
            )

    result = (
        session.query(LcaResult)
        .filter(LcaResult.product_id == str(payload.productId))
        .order_by(LcaResult.created_at.desc())
        .first()
    )
    if result is None:
        result = LcaResult(product_id=str(payload.productId))
        session.add(result)

    result.co2_kg = metrics["co2_kg"]
    result.water_l = metrics["water_l"]
    result.energy_mj = metrics["energy_mj"]
    result.breakdown = metrics["breakdown"]
    result.factors_versions = metrics["versions"]
    result.artifact_uri = artifact_uri
    if not result.created_at:
        result.created_at = datetime.now(timezone.utc)
    if hasattr(result, "calculated_at"):
        if result.calculated_at is None:
            result.calculated_at = datetime.now(timezone.utc)

    session.commit()
    session.refresh(result)

    return {"result_model": result, "metrics": metrics, "artifact_uri": artifact_uri}


# -----------------------------------------------------------------------------
# FastAPI
# -----------------------------------------------------------------------------

app = FastAPI(title="LCALite Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_origin_regex=".*",
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
    expose_headers=["*"],
)


@app.options("/lca/calc")
def options_lca():
    return Response(
        status_code=200,
        headers={
            "Access-Control-Allow-Origin": "*",
            "Access-Control-Allow-Methods": "POST, OPTIONS",
            "Access-Control-Allow-Headers": "*",
        },
    )


@app.on_event("startup")
def startup_event():
    global producer, consumer, minio_client_instance
    Base.metadata.create_all(bind=engine)
    with SessionLocal() as session:
        # Ensure columns exist if legacy table was created by a different service
        session.execute(text("ALTER TABLE IF EXISTS lca_result ADD COLUMN IF NOT EXISTS co2_kg double precision"))
        session.execute(text("ALTER TABLE IF EXISTS lca_result ADD COLUMN IF NOT EXISTS water_l double precision"))
        session.execute(text("ALTER TABLE IF EXISTS lca_result ADD COLUMN IF NOT EXISTS energy_mj double precision"))
        session.execute(text("ALTER TABLE IF EXISTS lca_result ADD COLUMN IF NOT EXISTS breakdown jsonb"))
        session.execute(text("ALTER TABLE IF EXISTS lca_result ADD COLUMN IF NOT EXISTS factors_versions jsonb"))
        session.execute(text("ALTER TABLE IF EXISTS lca_result ADD COLUMN IF NOT EXISTS artifact_uri text"))
        session.execute(text("ALTER TABLE IF EXISTS lca_result ADD COLUMN IF NOT EXISTS created_at timestamptz DEFAULT now()"))
        session.execute(text("ALTER TABLE IF EXISTS lca_result ALTER COLUMN calculated_at DROP NOT NULL"))
        session.execute(text("ALTER TABLE IF EXISTS lca_result ALTER COLUMN calculated_at SET DEFAULT now()"))
        session.commit()
        seed_data(session)

    producer = Producer({"bootstrap.servers": KAFKA_BOOTSTRAP_SERVERS})
    consumer_conf = {
        "bootstrap.servers": KAFKA_BOOTSTRAP_SERVERS,
        "group.id": KAFKA_GROUP_ID,
        "enable.auto.commit": False,
        "auto.offset.reset": "earliest",
    }
    consumer = Consumer(consumer_conf)

    ensure_minio()

    threading.Thread(target=consumer_loop, daemon=True).start()


@app.get("/health")
def health(db: Session = Depends(get_db)):
    ensure_minio()
    try:
        db.execute(text("SELECT 1"))
        db_status = "UP"
    except OperationalError:
        db_status = "DOWN"
    return {
        "status": "UP" if db_status == "UP" else "DEGRADED",
        "database": db_status,
        "kafka": "UP" if producer else "DOWN",
        "minio": "UP" if minio_client_instance else "DOWN",
    }


@app.get("/metrics")
def metrics():
    return Response(generate_latest(), media_type=CONTENT_TYPE_LATEST)


@app.post("/lca/calc", response_model=LcaResponse)
def lca_calc(request: LcaRequest, db: Session = Depends(get_db)):
    try:
        result = handle_lca(request, db)
        model: LcaResult = result["result_model"]
        metrics = result["metrics"]
        publish_lca_completed(model, request, metrics)
        payload = request
        return LcaResponse(
            resultId=str(model.id),
            productId=str(model.product_id),
            co2_kg=metrics["co2_kg"],
            water_l=metrics["water_l"],
            energy_mj=metrics["energy_mj"],
            breakdown=metrics["breakdown"],
            factors_versions=metrics["versions"],
            artifact_uri=model.artifact_uri,
            created_at=model.created_at,
            eventId=payload.eventId,
            correlationId=payload.correlationId,
        )
    except HTTPException:
        raise
    except Exception as exc:
        log.error(
            "LCA calc failed product=%s correlationId=%s eventId=%s error=%s",
            request.productId,
            request.correlationId,
            request.eventId,
            exc,
        )
        raise HTTPException(status_code=500, detail="LCA calculation failed. Please check payload or backend logs.")


@app.get("/lca/product/{product_id}", response_model=LcaResponse)
def get_lca(product_id: str, db: Session = Depends(get_db)):
    record = (
        db.query(LcaResult)
        .filter(LcaResult.product_id == str(product_id))
        .order_by(LcaResult.created_at.desc())
        .first()
    )
    if not record:
        raise HTTPException(status_code=404, detail="LCA result not found")
    return LcaResponse(
        resultId=record.id,
        productId=record.product_id,
        co2_kg=record.co2_kg,
        water_l=record.water_l,
        energy_mj=record.energy_mj,
        breakdown=record.breakdown,
        factors_versions=record.factors_versions,
        artifact_uri=record.artifact_uri,
        created_at=record.created_at,
    )


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=APP_PORT)
