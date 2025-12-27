# LCALite (FastAPI)

Service ACV/LCA simplifié en Python (FastAPI + SQLAlchemy). Consomme `ingredients.normalized`, calcule CO2/eau/énergie, stocke les résultats dans Postgres, publie `lca.completed` et écrit un rapport JSON dans MinIO.

## Endpoints
- `GET /health` – statut (DB/Kafka/MinIO)
- `GET /metrics` – métriques Prometheus
- `POST /lca/calc` – calcul manuel (même payload que l'événement Kafka)
- `GET /lca/product/{productId}` – dernier résultat pour un produit

Payload exemple :
```json
{
  "productId": "7b9c7b68-1111-4444-8888-123456789000",
  "ingredients": [{"name":"milk","category":"DAIRY","quantity_g":200}],
  "packaging": {"material":"GLASS","weight_g":180},
  "transport": {"mode":"TRUCK","distance_km":120,"weight_g":500},
  "eventId": "d46f5f2f-aaaa-bbbb-cccc-123456789000"
}
```

Réponse simplifiée :
```json
{
  "resultId": "...",
  "productId": "...",
  "co2_kg": 1.23,
  "water_l": 12.3,
  "energy_mj": 3.4,
  "breakdown": { "ingredients": {...}, "packaging": {...}, "transport": {...} },
  "artifact_uri": "minio://ecolabel-acv/reports/<product>/<run>.json"
}
```

## Kafka
- Consomme `ingredients.normalized` (groupe `lcalite-service`, commit manuel, DLQ `ingredients.normalized.dlq`).
- Publie `lca.completed` (inclut champs top-level `co2Kg/waterLiters/energyMj` pour compatibilité scoring + breakdown détaillé).

## MinIO
Bucket `ecolabel-acv` créé via `minio-mc` (compose). Artefacts JSON stockés sous `reports/<productId>/<runId>.json`.

## Local

```bash
uvicorn app.main:app --reload --port 8083

# curl rapide
curl http://localhost:8083/health
```
