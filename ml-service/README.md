# ML Service - OCR & NLP avec IA

Microservice Python pour l'extraction intelligente de texte et d'ingrédients via OCR et NLP.

## Technologies utilisées

- **Tesseract OCR** : extraction de texte depuis des images
- **pdfplumber** : parsing de PDFs
- **spaCy** : NLP pour la reconnaissance d'entités nommées
- **Transformers (BERT)** : modèle multilingue pour l'extraction d'ingrédients

## Installation des modèles spaCy

Avant de lancer le service, téléchargez un modèle spaCy :

```bash
# Modèle français (recommandé)
python -m spacy download fr_core_news_sm

# Ou modèle anglais (fallback)
python -m spacy download en_core_web_sm
```

Le Dockerfile tente automatiquement de télécharger le modèle français, avec fallback sur l'anglais.

## Endpoints

### `GET /health`
Vérifie l'état du service et des modèles ML chargés.

**Réponse:**
```json
{
  "status": "UP",
  "tesseract": true,
  "pdf": true,
  "nlp": true,
  "bert": true,
  "ready": true
}
```

### `POST /ocr/image`
Extraction texte d'une image via OCR (Tesseract).

**Body:**
```json
{
  "image": "base64_encoded_image_string"
}
```

**Réponse:**
```json
{
  "text": "texte extrait...",
  "method": "tesseract_ocr"
}
```

### `POST /ocr/pdf`
Extraction texte d'un PDF.

**Body:**
```json
{
  "pdf": "base64_encoded_pdf_string"
}
```

**Réponse:**
```json
{
  "text": "texte extrait...",
  "method": "pdfplumber"
}
```

### `POST /nlp/extract-ingredients`
Extraction d'ingrédients avec ML (spaCy + BERT).

**Body:**
```json
{
  "text": "sucre, huile de palme, noisettes 13%, cacao maigre"
}
```

**Réponse:**
```json
{
  "ingredients": [
    {
      "name": "sucre",
      "category": "SWEETENER",
      "confidence": 0.85
    },
    {
      "name": "huile de palme",
      "category": "OTHER",
      "confidence": 0.72
    }
  ],
  "organic": false,
  "count": 2,
  "method": "spacy_bert"
}
```

### `POST /nlp/extract-metadata`
Extraction de métadonnées (marque, origine) via NLP.

**Body:**
```json
{
  "text": "Produit de la marque EcoDelice, origine France"
}
```

**Réponse:**
```json
{
  "metadata": {
    "brand": "EcoDelice",
    "origin": "France"
  },
  "method": "spacy_nlp"
}
```

## Tests

### Test manuel avec curl

```bash
# Health check
curl http://localhost:8086/health

# Extraction d'ingrédients
curl -X POST http://localhost:8086/nlp/extract-ingredients \
  -H "Content-Type: application/json" \
  -d '{"text": "sucre, huile de palme, noisettes 13%"}'

# Extraction de métadonnées
curl -X POST http://localhost:8086/nlp/extract-metadata \
  -H "Content-Type: application/json" \
  -d '{"text": "Produit de la marque EcoDelice, origine France"}'
```

### Test automatisé

Un script de test est fourni pour valider tous les endpoints :

```bash
# Installer requests si nécessaire
pip install requests

# Lancer les tests
python test_endpoints.py http://localhost:8086
```

## Utilisation avec Docker

```bash
# Build
docker build -t ml-service ./ml-service

# Run
docker run -p 8086:8086 ml-service

# Ou avec docker-compose (depuis la racine du projet)
docker compose up ml-service
```

## Intégration avec les autres services

Ce service est utilisé par :
- **parserproduitservice** : appelle `/ocr/image`, `/ocr/pdf`, `/nlp/extract-metadata`
- **nlpingredientservice** : appelle `/nlp/extract-ingredients`

L'URL du service est configurée via la variable d'environnement `ML_SERVICE_URL` (par défaut: `http://ml-service:8086`).

## Notes

- Le service utilise un fallback si les modèles ML ne sont pas disponibles
- Les modèles BERT peuvent être lents au premier chargement (30-60 secondes)
- Le service est conçu pour être résilient : si ML échoue, les autres services utilisent un fallback
- Pour la production, considérez l'utilisation de modèles quantifiés ou d'APIs cloud (AWS Textract, Google Cloud Vision)


