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

- `GET /health` : État des modèles ML chargés
- `POST /ocr/image` : Extraction texte d'une image (body: `{"image": "base64_string"}`)
- `POST /ocr/pdf` : Extraction texte d'un PDF (body: `{"pdf": "base64_string"}`)
- `POST /nlp/extract-ingredients` : Extraction ingrédients avec ML (body: `{"text": "..."}`)
- `POST /nlp/extract-metadata` : Extraction métadonnées (marque, origine) (body: `{"text": "..."}`)

## Notes

- Le service utilise un fallback si les modèles ML ne sont pas disponibles
- Les modèles BERT peuvent être lents au premier chargement
- Pour la production, considérez l'utilisation de modèles quantifiés ou d'APIs cloud (AWS Textract, Google Cloud Vision)


