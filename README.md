# EcoLabel-MS

Plateforme microservices pour le calcul du score environnemental des produits. L'architecture est organisée autour de sept services Spring Boot interconnectés via Eureka (découverte), Kafka (événements) et PostgreSQL (stockage). Le déploiement local est orchestré avec Docker Compose et l'observabilité fournie par Prometheus/Grafana.

## Microservices

| Service | Port | Rôle |
| --- | --- | --- |
| `eureka` | 8761 | Registre Eureka (service discovery & load balancer). |
| `authservice` | 8080 | Gestion des comptes et JWT pour sécuriser les API. |
| `ml-service` | 8086 | **Service Python ML/IA** : OCR (Tesseract), NLP (spaCy + BERT) pour extraction intelligente. |
| `parserproduitservice` | 8081 | Ingestion et normalisation textuelle des fiches produits. **Utilise ML pour OCR/PDF**. Publie `product.parsed`. |
| `nlpingredientservice` | 8082 | Extraction & standardisation des ingrédients **via ML (spaCy/BERT)**. Publie `ingredients.normalized`. |
| `lcaliteservice` | 8083 | Calcul ACV simplifié et publication `lca.completed`. |
| `scoringservice` | 8084 | Agrégation des indicateurs et émission du score final via `score.published`. |
| `widgetapi` | 8085 | API publique/GraphQL Ready fournissant le score et l'explication aux widgets. |

Kafka est utilisé comme bus d’événements pour assurer le chaînage Parser → NLP → LCA → Scoring → Widget. Les topics sont automatiquement créés au démarrage.

### Flux fonctionnel (haut niveau)

1. **Ingestion produit** (`parserproduitservice`) : reçoit la fiche produit (texte, image base64, ou PDF base64). Si image/PDF fourni, **appelle le service ML pour OCR** (Tesseract) ou parsing PDF. Enrichit les métadonnées (marque, origine) via **NLP du service ML**. Persiste les métadonnées et publie `product.parsed` sur Kafka.
2. **NLP ingrédients** (`nlpingredientservice`) : consomme `product.parsed`, **appelle le service ML (spaCy + BERT)** pour extraire intelligemment les ingrédients avec catégories et confiance. Fallback sur regex si ML indisponible. Publie `ingredients.normalized`.
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

2. **Envoyer une fiche produit** (avec texte brut)

```bash
curl -X POST http://localhost:8081/product/parse \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{"gtin":"3017620425035","name":"Pâte à tartiner","brand":"EcoDelice","originCountry":"FR","packaging":"verre","rawText":"sucre, huile de palme, noisettes 13%, cacao maigre"}'
```

3. **Envoyer une image pour OCR** (extraction automatique via ML)

```bash
# Encoder une image en base64
IMAGE_B64=$(base64 -w 0 path/to/product_label.jpg)

curl -X POST http://localhost:8081/product/parse \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d "{\"gtin\":\"3017620425035\",\"name\":\"Pâte à tartiner\",\"imageBase64\":\"$IMAGE_B64\"}"
```

Le service ML extraira automatiquement le texte de l'image et enrichira les métadonnées.

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

## Machine Learning / IA

Le projet intègre un **microservice Python** (`ml-service`) utilisant :

- **OCR** : Tesseract pour extraire le texte d'images (étiquettes produits, photos)
- **Parsing PDF** : pdfplumber pour extraire le texte de fiches produits PDF
- **NLP avancé** : 
  - **spaCy** (modèle français/anglais) pour la reconnaissance d'entités nommées (ingrédients, marques, origines)
  - **BERT multilingue** (`dbmdz/bert-base-french-europeana-cased`) pour l'extraction d'ingrédients avec scores de confiance
- **Classification intelligente** : catégorisation automatique des ingrédients (DAIRY, SWEETENER, PACKAGING, etc.)

**Utilisation** :
- `parserproduitservice` appelle `/ocr/image` ou `/ocr/pdf` si une image/PDF est fournie
- `nlpingredientservice` appelle `/nlp/extract-ingredients` pour l'extraction ML, avec fallback sur regex si le service est indisponible

**Endpoints ML** :
- `POST /ocr/image` : extraction texte d'une image (base64)
- `POST /ocr/pdf` : extraction texte d'un PDF (base64)
- `POST /nlp/extract-ingredients` : extraction ingrédients avec ML
- `POST /nlp/extract-metadata` : extraction métadonnées (marque, origine)
- `GET /health` : état des modèles ML chargés

## Points d'extension

- Enrichissement ACV avec les référentiels FAO/Ademe.
- Ajout du microservice `Provenance` (DVC + MLflow) pour tracer la lignée des données.
- Sécurisation inter-services par OAuth2 client credentials / mTLS.
- Fine-tuning des modèles BERT sur un dataset d'ingrédients français.




## 🧩 Local Jenkins CI/CD Setup

This project uses **Jenkins (local installation)** to automate deployment using **Docker Compose**.
If you want to run the CI/CD pipeline on your own machine, follow the steps below.

---

## 1️⃣ Prerequisites

Make sure the following tools are installed **and available in your system PATH**:

### Required software

* **Git**

  ```bash
  git --version
  ```

* **Docker Desktop** (Docker Compose must be enabled)

  ```bash
  docker --version
  docker compose version
  ```

* **Java JDK 11 or later** (required by Jenkins)

  ```bash
  java -version
  ```

* **Jenkins (LTS recommended)**
  👉 [https://www.jenkins.io/download/](https://www.jenkins.io/download/)

---

## 2️⃣ Jenkins Initial Configuration

After installing Jenkins:

1. Start Jenkins

   * URL: `http://localhost:8080`

2. Unlock Jenkins using the password located at:

   ```text
   <JENKINS_HOME>/secrets/initialAdminPassword
   ```

3. Install **Recommended Plugins**

4. Create an **admin user**

---

## 3️⃣ Required Jenkins Plugins

Ensure the following plugins are installed:

* Pipeline
* Git
* Docker Pipeline
* Blue Ocean (optional, for better UI)

Check via:

```
Manage Jenkins → Plugins
```

---

## 4️⃣ Create the Jenkins Pipeline Job

1. Click **New Item**
2. Choose **Pipeline**
3. Enter a name (example: `microserv_project`)

### Pipeline configuration

* **Definition**: `Pipeline script from SCM`
* **SCM**: `Git`
* **Repository URL**:

  ```text
  https://github.com/microserv-chger/microserv_project.git
  ```
* **Branch**:

  ```text
  */main
  ```
* **Script Path**:

  ```text
  Jenkinsfile
  ```

Click **Save**.

---

## 5️⃣ Jenkinsfile Behavior (Important)

The provided `Jenkinsfile` is **cross-platform**:

* Uses `bat` on **Windows**
* Uses `sh` on **Linux / macOS**

👉 No modification is required.

---

## 6️⃣ Environment Requirements

The Jenkins agent **must**:

* Run on the same machine as Docker
* Have permission to execute Docker commands
* Have required ports available (e.g. `8080`, `8761`)

---

## 7️⃣ Running the Pipeline

To deploy the application:

1. Open the Jenkins job
2. Click **Build Now**
3. Monitor execution via:

   * **Pipeline Overview**
   * **Console Output**

On success, Jenkins will:

* Stop previous containers
* Deploy the latest version using Docker Compose
* Skip image rebuilds (`--no-build`)

---

## 8️⃣ Verifying Deployment

After a successful build, verify containers are running:

```bash
docker compose ps
```

Expected:

* All services show status **Up**

Optional health check:

```bash
curl http://localhost:8080/actuator/health
```

---

## 9️⃣ Common Issues

### ❌ `docker` command not found

* Ensure Docker Desktop is running
* Ensure Docker is in system PATH

### ❌ Pipeline fails on `sh`

* Ensure you are using the latest `Jenkinsfile` from the `main` branch

---

## 🔟 Notes

* This setup is intended for **local development and testing**
* For production usage, Jenkins should run on a dedicated server or container

---

✅ This guide ensures that any team member can reproduce the local CI/CD setup reliably.

