# EcoLabel-MS

Plateforme microservices pour le calcul du score environnemental des produits. Architecture event-driven : Kafka orchestre le flux Parser → NLP → LCA → Scoring → Widget. Postgres centralise les données, Eureka pour la découverte, Prometheus/Grafana pour l’observabilité. Docker Compose orchestre l’ensemble.

## Microservices

| Service | Port | Rôle |
| --- | --- | --- |
| `eureka` | 8761 | Registre Eureka (service discovery & load balancer). |
| `authservice` | 8080 | Gestion des comptes et JWT. |
| `ml-service` | 8086 | Service Python ML/IA : OCR (Tesseract), NLP (spaCy + BERT). |
| `parserproduitservice` | 8081 | Ingestion fiche produit (texte/PDF/image → OCR via ML), publie `product.parsed`. |
| `nlpingredientservice` | 8082 | Extraction/normalisation ingrédients, publie `ingredients.normalized`. |
| `lcaliteservice` | 8083 | LCA simplifié (FastAPI/Python), consomme `ingredients.normalized`, publie `lca.completed`. |
| `scoringservice` | 8084 | Agrégation ACV → score A–E, publie `score.published`. |
| `widgetapi` | 8085 | API publique pour exposer le score. |
| `minio` | 9000/9001 | Stockage artefacts LCA (reports JSON). |

## Monitoring
Prometheus scrute les services Spring via `/actuator/prometheus` et `lcaliteservice` via `/metrics`. Grafana (3001) avec datasource Prometheus (`http://prometheus:9090`). Kafka UI disponible sur `http://localhost:9000`.

## LCALite (Python FastAPI)

- Consomme `ingredients.normalized`, calcule CO2 / eau / énergie avec facteurs de démo, persiste dans Postgres et dédoublonne via `lca_processed_events`.
- Publie `lca.completed` (événement enrichi + compatibilité scoring avec champs `co2Kg`, `waterLiters`, `energyMj`).
- Génère un artefact JSON stocké dans MinIO `minio://ecolabel-acv/reports/<productId>/<runId>.json`.
- API REST :
  - `POST /lca/calc` (payload identique à l’événement consommé)
  - `GET /lca/product/{productId}` (dernier résultat)
  - `GET /health`, `GET /metrics`
- Env vars clés : `DATABASE_URL`, `KAFKA_BOOTSTRAP_SERVERS`, `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET`.

## Lancement rapide

```bash
docker compose up --build
```

UI utiles : Eureka `http://localhost:8761`, Kafka UI `http://localhost:9000`, Prometheus `http://localhost:9090`, Grafana `http://localhost:3001`, MinIO API `http://localhost:9002`, MinIO console `http://localhost:9003` (minioadmin/minioadmin).

## Tests manuels

Envoyer une fiche produit (Parser) puis laisser les événements Kafka propager jusqu’au scoring : voir README initial. Exemple direct LCALite :

```bash
curl -X POST http://localhost:8083/lca/calc \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "7b9c7b68-1111-4444-8888-123456789000",
    "ingredients": [{"name":"milk","category":"DAIRY","quantity_g":200}],
    "packaging": {"material":"GLASS","weight_g":180},
    "transport": {"mode":"TRUCK","distance_km":120,"weight_g":500}
  }'

curl http://localhost:8083/lca/product/7b9c7b68-1111-4444-8888-123456789000
```

> Les topics Kafka et facteurs ACV sont pré-semés pour la démo; adaptez les valeurs en production.

## Frontend ACV
- Vite dev origin : `http://localhost:5173`. Assurez-vous que `ecolabel-frontend/.env` contient `VITE_LCA_URL=http://localhost:8083`.
- Démarrage : dans `ecolabel-frontend/` exécuter `npm install` puis `npm run dev`.
- Sur “ACV / LCA”, sélectionnez un produit et calculez; les erreurs renvoyées par le backend sont affichées dans l’UI si le calcul échoue.
