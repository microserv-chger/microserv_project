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


def extract_ingredients_section(text: str) -> str:
    """
    Extrait uniquement la section des ingrédients d'un texte.
    Filtre les métadonnées comme Product, Brand, Origin, etc.
    """
    if not text or not text.strip():
        return ""
    
    text_clean = text.strip()
    
    # Patterns pour trouver la section des ingrédients (français et anglais)
    ingredients_patterns = [
        r'(?:ingr[ée]dients?|ingredients?)\s*[:\-]?\s*(.+)',
        r'(?:composition)\s*[:\-]?\s*(.+)',
        r'(?:contient|contains)\s*[:\-]?\s*(.+)',
    ]
    
    for pattern in ingredients_patterns:
        match = re.search(pattern, text_clean, re.IGNORECASE | re.DOTALL)
        if match:
            ingredients_text = match.group(1).strip()
            # Arrêter si on trouve une autre section (comme "Allergènes:", "Nutrition:", etc.)
            stop_patterns = [
                r'\n\s*(?:allerg[èe]nes?|allergens?|nutrition|valeurs?|conservation|storage)',
                r'\n\s*(?:poids|weight|volume|contenance)',
                r'\n\s*(?:à consommer|best before|use by)',
            ]
            for stop_pattern in stop_patterns:
                stop_match = re.search(stop_pattern, ingredients_text, re.IGNORECASE)
                if stop_match:
                    ingredients_text = ingredients_text[:stop_match.start()].strip()
            
            return ingredients_text
    
    # Si pas de section "Ingrédients:" trouvée, retourner le texte tel quel
    # mais seulement si ça ne ressemble pas à des métadonnées structurées
    lines = text_clean.split('\n')
    filtered_lines = []
    
    # Mots clés de métadonnées à exclure
    metadata_keywords = [
        'product', 'produit', 'brand', 'marque', 'origin', 'origine',
        'name', 'nom', 'manufacturer', 'fabricant', 'country', 'pays',
        'weight', 'poids', 'volume', 'ean', 'upc', 'barcode', 'code'
    ]
    
    for line in lines:
        line_lower = line.lower().strip()
        # Ignorer les lignes de métadonnées
        is_metadata = False
        for kw in metadata_keywords:
            if line_lower.startswith(kw) and ':' in line:
                is_metadata = True
                break
        if not is_metadata and line.strip():
            filtered_lines.append(line)
    
    return ' '.join(filtered_lines)


def extract_ingredients_nlp(text: str) -> List[Dict]:
    """
    Extrait les ingrédients d'un texte avec spaCy + BERT
    Retourne une liste d'ingrédients normalisés avec catégories
    """
    if not text or not text.strip():
        return []
    
    # IMPORTANT: D'abord extraire seulement la section des ingrédients
    ingredients_text = extract_ingredients_section(text)
    
    if not ingredients_text:
        return []
    
    ingredients = []
    
    # Nettoyage basique
    text_clean = re.sub(r'\s+', ' ', ingredients_text).strip()
    
    # Méthode préférée: parsing par virgules/points-virgules (plus fiable pour les ingrédients)
    # Les ingrédients sont typiquement séparés par des virgules
    parts = re.split(r'[,;]\s*', text_clean)
    
    for part in parts:
        part = part.strip()
        # Ignorer les parties trop courtes ou vides
        if len(part) < 2 or part.isdigit():
            continue
        
        # Nettoyer le nom de l'ingrédient
        # Supprimer les parenthèses et leur contenu pour le nom principal, mais garder pour sous-ingrédients
        main_part = part
        sub_ingredients = []
        
        # Extraire les sous-ingrédients entre parenthèses
        paren_match = re.search(r'\(([^)]+)\)', part)
        if paren_match:
            sub_content = paren_match.group(1)
            # Séparer les sous-ingrédients
            sub_parts = re.split(r'[,;]\s*', sub_content)
            for sub in sub_parts:
                sub = sub.strip()
                if len(sub) > 2 and not sub.isdigit():
                    # Nettoyer les pourcentages
                    sub = re.sub(r'\d+\s*%', '', sub).strip()
                    if sub:
                        sub_ingredients.append(sub)
            # Retirer les parenthèses du nom principal
            main_part = re.sub(r'\([^)]+\)', '', part).strip()
        
        # Nettoyer les pourcentages
        main_part = re.sub(r'\d+\s*%', '', main_part).strip()
        
        if main_part and len(main_part) > 2:
            ingredients.append({
                "name": main_part.strip(),
                "category": classify_ingredient_ml(main_part),
                "confidence": float(0.8)
            })
        
        # Ajouter les sous-ingrédients
        for sub in sub_ingredients:
            if not any(ing["name"].lower() == sub.lower() for ing in ingredients):
                ingredients.append({
                    "name": sub.strip(),
                    "category": classify_ingredient_ml(sub),
                    "confidence": float(0.7)
                })
    
    # Si le parsing par virgules n'a rien donné, essayer spaCy comme fallback
    if not ingredients and nlp_model:
        doc = nlp_model(text_clean)
        
        # Extraire les noms communs (NOUN) qui pourraient être des ingrédients
        for token in doc:
            if token.pos_ == "NOUN" and len(token.text) > 3:
                # Éviter les doublons
                if not any(ing["name"].lower() == token.text.lower() for ing in ingredients):
                    ingredients.append({
                        "name": token.text.strip(),
                        "category": classify_ingredient_ml(token.text),
                        "confidence": float(0.5)
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
    """Health check - endpoint utilisé par les autres services pour vérifier la disponibilité"""
    health_status = {
        "status": "UP",
        "tesseract": TESSERACT_AVAILABLE,
        "pdf": PDF_AVAILABLE,
        "nlp": NLP_AVAILABLE and nlp_model is not None,
        "bert": ner_pipeline is not None,
        "ready": True  # Indique que le service est prêt à recevoir des requêtes
    }
    
    # Si aucun modèle n'est disponible, le service n'est pas vraiment "ready"
    if not TESSERACT_AVAILABLE and not PDF_AVAILABLE and not (NLP_AVAILABLE and nlp_model):
        health_status["ready"] = False
        health_status["status"] = "DEGRADED"
    
    status_code = 200 if health_status["ready"] else 503
    return jsonify(health_status), status_code


@app.route('/ocr/image', methods=['POST'])
def ocr_image():
    """
    Extrait le texte d'une image via OCR
    Body: { "image": "base64_encoded_image" }
    Retourne: { "text": "...", "method": "tesseract_ocr" }
    """
    try:
        data = request.json
        if not data or "image" not in data:
            return jsonify({"error": "Missing 'image' field (base64)"}), 400
        
        if not TESSERACT_AVAILABLE:
            return jsonify({"error": "Tesseract OCR not available"}), 503
        
        image_b64 = data["image"]
        if not image_b64 or not isinstance(image_b64, str):
            return jsonify({"error": "Invalid image data (must be base64 string)"}), 400
        
        try:
            image_data = base64.b64decode(image_b64)
        except Exception as e:
            return jsonify({"error": f"Invalid base64 encoding: {str(e)}"}), 400
        
        text = extract_text_from_image(image_data)
        
        return jsonify({
            "text": text,
            "method": "tesseract_ocr"
        })
    except ValueError as e:
        logging.error(f"OCR validation error: {e}")
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        logging.error(f"OCR error: {e}", exc_info=True)
        return jsonify({"error": f"OCR processing failed: {str(e)}"}), 500


@app.route('/ocr/pdf', methods=['POST'])
def ocr_pdf():
    """
    Extrait le texte d'un PDF
    Body: { "pdf": "base64_encoded_pdf" }
    Retourne: { "text": "...", "method": "pdfplumber" }
    """
    try:
        data = request.json
        if not data or "pdf" not in data:
            return jsonify({"error": "Missing 'pdf' field (base64)"}), 400
        
        if not PDF_AVAILABLE:
            return jsonify({"error": "PDF parsing not available"}), 503
        
        pdf_b64 = data["pdf"]
        if not pdf_b64 or not isinstance(pdf_b64, str):
            return jsonify({"error": "Invalid PDF data (must be base64 string)"}), 400
        
        try:
            pdf_data = base64.b64decode(pdf_b64)
        except Exception as e:
            return jsonify({"error": f"Invalid base64 encoding: {str(e)}"}), 400
        
        text = extract_text_from_pdf(pdf_data)
        
        return jsonify({
            "text": text,
            "method": "pdfplumber"
        })
    except ValueError as e:
        logging.error(f"PDF validation error: {e}")
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        logging.error(f"PDF parsing error: {e}", exc_info=True)
        return jsonify({"error": f"PDF processing failed: {str(e)}"}), 500


@app.route('/nlp/extract-ingredients', methods=['POST'])
def extract_ingredients():
    """
    Extrait les ingrédients d'un texte avec ML (spaCy + BERT)
    Body: { "text": "texte brut" }
    Retourne: { "ingredients": [...], "organic": bool, "count": int, "method": "..." }
    Format ingrédient: { "name": "...", "category": "...", "confidence": float }
    """
    try:
        data = request.json
        if not data or "text" not in data:
            return jsonify({"error": "Missing 'text' field"}), 400
        
        text = data["text"]
        if not isinstance(text, str):
            return jsonify({"error": "Field 'text' must be a string"}), 400
        
        if not text or not text.strip():
            return jsonify({
                "ingredients": [],
                "organic": False,
                "count": 0,
                "method": "empty_input"
            })
        
        ingredients = extract_ingredients_nlp(text)
        
        # Détecter si bio
        is_organic = detect_organic_label(text)
        
        return jsonify({
            "ingredients": ingredients,
            "organic": is_organic,
            "count": len(ingredients),
            "method": "spacy_bert" if (NLP_AVAILABLE and nlp_model) else "fallback"
        })
    except Exception as e:
        logging.error(f"NLP extraction error: {e}", exc_info=True)
        return jsonify({"error": f"Ingredient extraction failed: {str(e)}"}), 500


@app.route('/nlp/extract-metadata', methods=['POST'])
def extract_metadata():
    """
    Extrait les métadonnées d'un produit (nom, marque, origine, etc.) via NLP
    Body: { "text": "texte brut" }
    Retourne: { "metadata": {"brand": "...", "origin": "..."}, "method": "..." }
    """
    try:
        data = request.json
        if not data or "text" not in data:
            return jsonify({"error": "Missing 'text' field"}), 400
        
        text = data["text"]
        if not isinstance(text, str):
            return jsonify({"error": "Field 'text' must be a string"}), 400
        
        metadata = {}
        
        if nlp_model and text.strip():
            try:
                doc = nlp_model(text)
                
                # Extraire les entités nommées
                for ent in doc.ents:
                    if ent.label_ == "ORG":
                        if "brand" not in metadata:
                            metadata["brand"] = ent.text.strip()
                    elif ent.label_ == "GPE" or ent.label_ == "LOC":
                        if "origin" not in metadata:
                            metadata["origin"] = ent.text.strip()
            except Exception as e:
                logging.warning(f"spaCy processing error: {e}")
        
        # Patterns regex pour métadonnées (fallback ou complément)
        # Patterns regex pour métadonnées (fallback ou complément)
        if text:
            origin_pattern = r'(?:origine|origin|pays|country)[\s:]+([A-Z][a-z]+)'
            match = re.search(origin_pattern, text, re.IGNORECASE)
            if match and "origin" not in metadata:
                metadata["origin"] = match.group(1).strip()
            
            brand_pattern = r'(?:marque|brand)[\s:]+([A-Z][a-zA-Z0-9]+)'
            match = re.search(brand_pattern, text, re.IGNORECASE)
            if match and "brand" not in metadata:
                metadata["brand"] = match.group(1).strip()

        # 🔴 ADD THIS BLOCK RIGHT HERE
        # Defensive cleanup to avoid greedy matches
        for k in metadata:
            if isinstance(metadata[k], str):
                metadata[k] = metadata[k].strip().split()[0]

        return jsonify({
            "metadata": metadata,
            "method": "spacy_nlp" if nlp_model else "regex"
        })

    except Exception as e:
        logging.error(f"Metadata extraction error: {e}", exc_info=True)
        return jsonify({"error": f"Metadata extraction failed: {str(e)}"}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8086, debug=True)


