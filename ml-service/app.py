"""
Microservice Python pour OCR et NLP avec ML/IA
Utilise Tesseract (OCR), spaCy + Transformers (BERT) pour l'extraction intelligente
"""
from flask import Flask, request, jsonify
from flask_cors import CORS
import base64
import io
import logging
from typing import List, Dict, Optional
import re

# OCR
try:
    import pytesseract
    from PIL import Image
    TESSERACT_AVAILABLE = True
except ImportError:
    TESSERACT_AVAILABLE = False
    logging.warning("Tesseract/PIL not available - OCR disabled")

# PDF parsing
try:
    import pdfplumber
    PDF_AVAILABLE = True
except ImportError:
    PDF_AVAILABLE = False
    logging.warning("pdfplumber not available - PDF parsing disabled")

# NLP avec spaCy
try:
    import spacy
    from transformers import pipeline, AutoTokenizer, AutoModelForTokenClassification
    NLP_AVAILABLE = True
except ImportError:
    NLP_AVAILABLE = False
    logging.warning("spaCy/transformers not available - using fallback NLP")

app = Flask(__name__)
CORS(app)
logging.basicConfig(level=logging.INFO)

# Charger les modèles NLP au démarrage
nlp_model = None
ner_pipeline = None

if NLP_AVAILABLE:
    try:
        # Charger spaCy (modèle français si disponible, sinon anglais)
        try:
            nlp_model = spacy.load("fr_core_news_sm")
            logging.info("Loaded French spaCy model")
        except OSError:
            try:
                nlp_model = spacy.load("en_core_web_sm")
                logging.info("Loaded English spaCy model")
            except OSError:
                logging.warning("No spaCy model found - using basic NLP")
        
        # Charger BERT pour NER (Named Entity Recognition)
        try:
            ner_pipeline = pipeline(
                "ner",
                model="dbmdz/bert-base-french-europeana-cased",
                aggregation_strategy="simple"
            )
            logging.info("Loaded BERT NER model")
        except Exception as e:
            logging.warning(f"Could not load BERT model: {e}")
    except Exception as e:
        logging.error(f"Error loading NLP models: {e}")


def extract_text_from_image(image_data: bytes) -> str:
    """Extrait le texte d'une image via OCR (Tesseract)"""
    if not TESSERACT_AVAILABLE:
        raise ValueError("Tesseract not available")
    
    image = Image.open(io.BytesIO(image_data))
    text = pytesseract.image_to_string(image, lang='fra+eng')
    return text.strip()


def extract_text_from_pdf(pdf_data: bytes) -> str:
    """Extrait le texte d'un PDF"""
    if not PDF_AVAILABLE:
        raise ValueError("PDF parsing not available")
    
    text_parts = []
    with pdfplumber.open(io.BytesIO(pdf_data)) as pdf:
        for page in pdf.pages:
            text = page.extract_text()
            if text:
                text_parts.append(text)
    return "\n".join(text_parts)


def extract_ingredients_nlp(text: str) -> List[Dict]:
    """
    Extrait les ingrédients d'un texte avec spaCy + BERT
    Retourne une liste d'ingrédients normalisés avec catégories
    """
    if not text or not text.strip():
        return []
    
    ingredients = []
    
    # Nettoyage basique
    text_clean = re.sub(r'\s+', ' ', text).strip()
    
    # Utiliser spaCy si disponible
    if nlp_model:
        doc = nlp_model(text_clean)
        
        # Extraire les entités nommées (ingrédients potentiels)
        for ent in doc.ents:
            if ent.label_ in ["ORG", "MISC", "PRODUCT"] or len(ent.text) > 3:
                ingredients.append({
                    "name": ent.text.strip(),
                    "category": classify_ingredient_ml(ent.text),
                    "confidence": 0.7
                })
        
        # Extraire les noms communs (NOUN) qui pourraient être des ingrédients
        for token in doc:
            if token.pos_ == "NOUN" and len(token.text) > 3:
                # Éviter les doublons
                if not any(ing["name"].lower() == token.text.lower() for ing in ingredients):
                    ingredients.append({
                        "name": token.text.strip(),
                        "category": classify_ingredient_ml(token.text),
                        "confidence": 0.5
                    })
    
    # Utiliser BERT NER si disponible
    if ner_pipeline:
        try:
            ner_results = ner_pipeline(text_clean[:512])  # Limiter la longueur
            for entity in ner_results:
                if entity.get("score", 0) > 0.5:
                    ingredients.append({
                        "name": entity["word"].strip(),
                        "category": classify_ingredient_ml(entity["word"]),
                        "confidence": entity.get("score", 0.5)
                    })
        except Exception as e:
            logging.warning(f"BERT NER error: {e}")
    
    # Fallback: parsing par virgules/points-virgules si aucun modèle ML
    if not ingredients:
        parts = re.split(r'[,;]\s*', text_clean)
        for part in parts:
            part = part.strip()
            if len(part) > 2 and not part.isdigit():
                # Retirer les pourcentages
                part = re.sub(r'\d+%', '', part).strip()
                if part:
                    ingredients.append({
                        "name": part,
                        "category": classify_ingredient_ml(part),
                        "confidence": 0.4
                    })
    
    # Dédupliquer et nettoyer
    seen = set()
    unique_ingredients = []
    for ing in ingredients:
        name_lower = ing["name"].lower()
        if name_lower not in seen and len(ing["name"]) > 2:
            seen.add(name_lower)
            unique_ingredients.append(ing)
    
    return unique_ingredients


def classify_ingredient_ml(ingredient_text: str) -> str:
    """
    Classifie un ingrédient en catégorie (DAIRY, SWEETENER, etc.)
    Utilise des patterns ML + règles
    """
    text_lower = ingredient_text.lower()
    
    # Patterns pour classification
    dairy_keywords = ["lait", "milk", "fromage", "cheese", "beurre", "butter", "crème", "cream", "yaourt"]
    sweetener_keywords = ["sucre", "sugar", "sirop", "syrup", "miel", "honey", "fructose", "glucose"]
    packaging_keywords = ["plastique", "plastic", "pet", "pe", "pp", "emballage", "packaging"]
    glass_keywords = ["verre", "glass"]
    organic_keywords = ["bio", "organic", "biologique"]
    
    if any(kw in text_lower for kw in dairy_keywords):
        return "DAIRY"
    if any(kw in text_lower for kw in sweetener_keywords):
        return "SWEETENER"
    if any(kw in text_lower for kw in packaging_keywords):
        return "PACKAGING"
    if any(kw in text_lower for kw in glass_keywords):
        return "GLASS"
    if any(kw in text_lower for kw in organic_keywords):
        return "ORGANIC"
    
    return "OTHER"


def detect_organic_label(text: str) -> bool:
    """Détecte si un produit est bio/organic via ML"""
    if not text:
        return False
    
    text_lower = text.lower()
    organic_patterns = [
        r'\b(bio|organic|biologique|ecocert|ab)\b',
        r'certifi[ée]\s+(bio|organic)',
        r'label\s+(bio|organic)'
    ]
    
    for pattern in organic_patterns:
        if re.search(pattern, text_lower, re.IGNORECASE):
            return True
    
    return False


@app.route('/health', methods=['GET'])
def health():
    """Health check"""
    return jsonify({
        "status": "UP",
        "tesseract": TESSERACT_AVAILABLE,
        "pdf": PDF_AVAILABLE,
        "nlp": NLP_AVAILABLE and nlp_model is not None,
        "bert": ner_pipeline is not None
    })


@app.route('/ocr/image', methods=['POST'])
def ocr_image():
    """
    Extrait le texte d'une image via OCR
    Body: { "image": "base64_encoded_image" }
    """
    try:
        data = request.json
        if not data or "image" not in data:
            return jsonify({"error": "Missing 'image' field (base64)"}), 400
        
        image_b64 = data["image"]
        image_data = base64.b64decode(image_b64)
        
        text = extract_text_from_image(image_data)
        
        return jsonify({
            "text": text,
            "method": "tesseract_ocr"
        })
    except Exception as e:
        logging.error(f"OCR error: {e}")
        return jsonify({"error": str(e)}), 500


@app.route('/ocr/pdf', methods=['POST'])
def ocr_pdf():
    """
    Extrait le texte d'un PDF
    Body: { "pdf": "base64_encoded_pdf" }
    """
    try:
        data = request.json
        if not data or "pdf" not in data:
            return jsonify({"error": "Missing 'pdf' field (base64)"}), 400
        
        pdf_b64 = data["pdf"]
        pdf_data = base64.b64decode(pdf_b64)
        
        text = extract_text_from_pdf(pdf_data)
        
        return jsonify({
            "text": text,
            "method": "pdfplumber"
        })
    except Exception as e:
        logging.error(f"PDF parsing error: {e}")
        return jsonify({"error": str(e)}), 500


@app.route('/nlp/extract-ingredients', methods=['POST'])
def extract_ingredients():
    """
    Extrait les ingrédients d'un texte avec ML (spaCy + BERT)
    Body: { "text": "texte brut" }
    """
    try:
        data = request.json
        if not data or "text" not in data:
            return jsonify({"error": "Missing 'text' field"}), 400
        
        text = data["text"]
        ingredients = extract_ingredients_nlp(text)
        
        # Détecter si bio
        is_organic = detect_organic_label(text)
        
        return jsonify({
            "ingredients": ingredients,
            "organic": is_organic,
            "count": len(ingredients),
            "method": "spacy_bert" if NLP_AVAILABLE else "fallback"
        })
    except Exception as e:
        logging.error(f"NLP extraction error: {e}")
        return jsonify({"error": str(e)}), 500


@app.route('/nlp/extract-metadata', methods=['POST'])
def extract_metadata():
    """
    Extrait les métadonnées d'un produit (nom, marque, origine, etc.) via NLP
    Body: { "text": "texte brut" }
    """
    try:
        data = request.json
        if not data or "text" not in data:
            return jsonify({"error": "Missing 'text' field"}), 400
        
        text = data["text"]
        metadata = {}
        
        if nlp_model:
            doc = nlp_model(text)
            
            # Extraire les entités nommées
            for ent in doc.ents:
                if ent.label_ == "ORG":
                    if "brand" not in metadata:
                        metadata["brand"] = ent.text
                elif ent.label_ == "GPE" or ent.label_ == "LOC":
                    if "origin" not in metadata:
                        metadata["origin"] = ent.text
        
        # Patterns regex pour métadonnées
        origin_pattern = r'(origine|origin|pays|country)[\s:]+([A-Z][a-z]+)'
        match = re.search(origin_pattern, text, re.IGNORECASE)
        if match and "origin" not in metadata:
            metadata["origin"] = match.group(2)
        
        return jsonify({
            "metadata": metadata,
            "method": "spacy_nlp" if nlp_model else "regex"
        })
    except Exception as e:
        logging.error(f"Metadata extraction error: {e}")
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8086, debug=True)


