# EcoLabel-MS

Plateforme microservices pour le calcul du score environnemental des produits. L'architecture est organisée autour de sept services Spring Boot interconnectés via Eureka (découverte), Kafka (événements) et PostgreSQL (stockage). Le déploiement local est orchestré avec Docker Compose et l'observabilité fournie par Prometheus/Grafana.

## Microservices

| Service | Port | Rôle |
| --- | --- | --- |
| `eureka` | 8761 | Registre Eureka (service discovery & load balancer). |
| `authservice` | 8080 | Gestion des comptes et JWT pour sécuriser les API. |
| `parserproduitservice` | 8081 | Ingestion et normalisation textuelle des fiches produits. Publie `product.parsed`. |
| `nlpingredientservice` | 8082 | Extraction & standardisation des ingrédients, publie `ingredients.normalized`. |
| `lcaliteservice` | 8083 | Calcul ACV simplifié et publication `lca.completed`. |
| `scoringservice` | 8084 | Agrégation des indicateurs et émission du score final via `score.published`. |
| `widgetapi` | 8085 | API publique/GraphQL Ready fournissant le score et l'explication aux widgets. |

Kafka est utilisé comme bus d’événements pour assurer le chaînage Parser → NLP → LCA → Scoring → Widget. Les topics sont automatiquement créés au démarrage.

### Flux fonctionnel (haut niveau)

1. **Ingestion produit** (`parserproduitservice`) : reçoit la fiche produit, persiste les métadonnées et publie `product.parsed` sur Kafka.
2. **NLP ingrédients** (`nlpingredientservice`) : consomme `product.parsed`, extrait les ingrédients normalisés et publie `ingredients.normalized`.
3. **ACV simplifiée** (`lcaliteservice`) : consomme `ingredients.normalized`, calcule les indicateurs ACV (CO₂, eau, énergie) et publie `lca.completed`.
4. **Scoring** (`scoringservice`) : consomme `lca.completed`, calcule un score numérique (0–100) et une lettre `A–E`, persiste le résultat et publie `score.published`.
5. **Exposition publique** (`widgetapi`) : consomme `score.published` et expose le score final via `GET /public/product/{id}`.

## Lancement rapide

```bash
docker compose up --build
```

Services disponibles :

- Eureka Dashboard : `http://localhost:8761`
- Kafka UI : `http://localhost:9000`
- Prometheus : `http://localhost:9090`
- Grafana : `http://localhost:3000` (login/par défaut `admin` / `admin`)

## Principaux endpoints

- **Auth**
  - `POST /auth/register` : création de compte + retour d’un JWT.
  - `POST /auth/login` : authentification + JWT.
  - `GET /auth/me` : infos du compte courant.

- **ParserProduit**
  - `POST /product/parse` : ingestion et parsing d’un produit.
  - `GET /product/{id}` : métadonnées produit parsées.

- **NLPIngrédients**
  - `POST /nlp/extract` : extraction manuelle sur un texte.
  - `GET /nlp/product/{productId}` : ingrédients normalisés pour un produit.

- **LCALite**
  - `POST /lca/calc` : calcul direct ACV à partir d’ingrédients.
  - `GET /lca/product/{productId}` : dernier résultat ACV d’un produit.

- **Scoring**
  - `POST /score/compute` : calcul direct du score depuis des indicateurs ACV.
  - `GET /score/product/{productId}` : score stocké pour un produit.

- **WidgetAPI (publique)**
  - `GET /public/product/{productId}` : score final A–E + explications.

Tous les services exposent également `/actuator/health` et `/actuator/info`.

## Tests manuels

1. **Créer un compte / obtenir un token**

```bash
curl -X POST http://localhost:8080/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"analyste","email":"analyste@example.com","password":"EcoLabel!1"}'
```

2. **Envoyer une fiche produit**

```bash
curl -X POST http://localhost:8081/product/parse \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"gtin":"3017620425035","name":"Pâte à tartiner","brand":"EcoDelice","originCountry":"FR","packaging":"verre","rawText":"sucre, huile de palme, noisettes 13%, cacao maigre"}'
```

Les événements issus de Kafka propagent ensuite les données jusqu'à `widgetapi`. Vérifiez le score public :

```bash
curl http://localhost:8085/public/product/{productId}
```

> Remplacez `{productId}` par l’UUID retourné par `POST /product/parse`.

## Observabilité

Chaque service expose `/actuator/health` et `/actuator/prometheus`. Prometheus scrute ces endpoints (voir `monitoring/prometheus.yml`) et Grafana dispose d'un datasource à ajouter manuellement (`http://prometheus:9090`) après connexion.

## Services optionnels / production

- **Prometheus / Grafana** : peuvent être commentés dans `docker-compose.yml` si non nécessaires.
- **Kafka UI** : outil pratique pour la démo, optionnel en production.
- **Sécurité** : la clé `JWT_SECRET` est fournie pour le développement. En production, utilisez un secret long stocké dans un gestionnaire de secrets (Vault, AWS Secrets Manager, etc.).
- **Scalabilité** : chaque microservice peut être répliqué derrière Eureka + un API Gateway (Zuul, Spring Cloud Gateway, Traefik…).

## Points d'extension

- Intégration OCR (Tesseract) et modèles NLP Python via gRPC ou REST.
- Enrichissement ACV avec les référentiels FAO/Ademe.
- Ajout du microservice `Provenance` (DVC + MLflow) pour tracer la lignée des données.
- Sécurisation inter-services par OAuth2 client credentials / mTLS.

