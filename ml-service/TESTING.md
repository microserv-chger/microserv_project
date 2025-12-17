# Guide de test du ML Service

Ce guide vous aide à vérifier que le service ML fonctionne correctement avant que vos collègues l'utilisent.

## Prérequis

1. **Python 3.11+** installé
2. **Tesseract OCR** installé (pour les tests OCR)
   - Windows: télécharger depuis https://github.com/UB-Mannheim/tesseract/wiki
   - Linux: `sudo apt-get install tesseract-ocr tesseract-ocr-fra`
   - Mac: `brew install tesseract`

## Installation locale (sans Docker)

```bash
cd ml-service

# Créer un environnement virtuel (recommandé)
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# Installer les dépendances
pip install -r requirements.txt

# Télécharger les modèles spaCy
python -m spacy download fr_core_news_sm
# ou
python -m spacy download en_core_web_sm
```

## Lancer le service localement

```bash
python app.py
```

Le service démarre sur `http://localhost:8086`

## Tests rapides

### 1. Health Check

```bash
curl http://localhost:8086/health
```

**Résultat attendu:**
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

### 2. Test d'extraction d'ingrédients

```bash
curl -X POST http://localhost:8086/nlp/extract-ingredients \
  -H "Content-Type: application/json" \
  -d '{"text": "sucre, huile de palme, noisettes 13%, cacao maigre, lait écrémé"}'
```

**Résultat attendu:** Liste d'ingrédients avec catégories et confiance.

### 3. Test d'extraction de métadonnées

```bash
curl -X POST http://localhost:8086/nlp/extract-metadata \
  -H "Content-Type: application/json" \
  -d '{"text": "Produit de la marque EcoDelice, origine France. Pâte à tartiner bio."}'
```

**Résultat attendu:** Métadonnées avec brand et origin si détectés.

## Tests automatisés

Le script `test_endpoints.py` teste tous les endpoints :

```bash
# Installer requests si nécessaire
pip install requests

# Lancer les tests
python test_endpoints.py
```

## Test avec Docker

### Build et run

```bash
# Depuis la racine du projet
docker compose build ml-service
docker compose up ml-service
```

### Vérifier les logs

```bash
docker compose logs ml-service
```

Vous devriez voir :
- "Loaded French spaCy model" ou "Loaded English spaCy model"
- "Loaded BERT NER model"
- "Running on http://0.0.0.0:8086"

### Test depuis l'extérieur du container

```bash
# Health check
curl http://localhost:8086/health

# Test avec le script
python test_endpoints.py http://localhost:8086
```

## Vérification de l'intégration

Une fois le service lancé, testez depuis les autres services :

### Depuis parserproduitservice

```bash
# Simuler un appel depuis parserproduitservice
curl -X POST http://localhost:8086/nlp/extract-metadata \
  -H "Content-Type: application/json" \
  -d '{"text": "Pâte à tartiner EcoDelice, fabriqué en France"}'
```

### Depuis nlpingredientservice

```bash
# Simuler un appel depuis nlpingredientservice
curl -X POST http://localhost:8086/nlp/extract-ingredients \
  -H "Content-Type: application/json" \
  -d '{"text": "sucre, huile de palme, noisettes 13%, cacao maigre"}'
```

## Problèmes courants

### 1. Tesseract non trouvé

**Erreur:** `TesseractNotFoundError`

**Solution:**
- Vérifier que Tesseract est installé: `tesseract --version`
- Sur Windows, ajouter Tesseract au PATH ou définir `TESSDATA_PREFIX`

### 2. Modèle spaCy non trouvé

**Erreur:** `OSError: Can't find model 'fr_core_news_sm'`

**Solution:**
```bash
python -m spacy download fr_core_news_sm
```

### 3. BERT lent au démarrage

**Normal:** Le modèle BERT peut prendre 30-60 secondes à charger au premier démarrage. C'est normal.

### 4. Port 8086 déjà utilisé

**Solution:** Changer le port dans `app.py` ou arrêter le service qui utilise le port.

## Checklist avant de passer aux collègues

- [ ] Service démarre sans erreur
- [ ] `/health` retourne `"ready": true`
- [ ] `/nlp/extract-ingredients` fonctionne avec un texte de test
- [ ] `/nlp/extract-metadata` fonctionne avec un texte de test
- [ ] `/ocr/image` fonctionne (ou retourne 503 si Tesseract non disponible)
- [ ] `/ocr/pdf` fonctionne (ou retourne 503 si pdfplumber non disponible)
- [ ] Le script `test_endpoints.py` passe tous les tests
- [ ] Le service fonctionne dans Docker
- [ ] Les logs ne montrent pas d'erreurs critiques

## Support

Si vous rencontrez des problèmes :
1. Vérifier les logs: `docker compose logs ml-service`
2. Tester chaque endpoint individuellement
3. Vérifier que toutes les dépendances sont installées
4. Consulter le README.md pour plus de détails

