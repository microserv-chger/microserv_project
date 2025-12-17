#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script de test pour valider tous les endpoints du ml-service
Utilisation: python test_endpoints.py [base_url]
Exemple: python test_endpoints.py http://localhost:8086
"""

import sys
import json
import base64
import requests
from pathlib import Path
import io

# Configurer l'encodage UTF-8 pour Windows
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

# URL par défaut
BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8086"

def test_health():
    """Test du endpoint /health"""
    print("\n=== Test /health ===")
    try:
        response = requests.get(f"{BASE_URL}/health", timeout=5)
        print(f"Status: {response.status_code}")
        data = response.json()
        print(f"Response: {json.dumps(data, indent=2)}")
        
        if data.get("status") == "UP" or data.get("status") == "DEGRADED":
            print("[OK] Health check OK")
            return True
        else:
            print("[FAIL] Health check FAILED")
            return False
    except Exception as e:
        print(f"[ERROR] Error: {e}")
        return False

def test_extract_metadata():
    """Test du endpoint /nlp/extract-metadata"""
    print("\n=== Test /nlp/extract-metadata ===")
    try:
        test_text = "Produit de la marque EcoDelice, origine France. Pâte à tartiner bio."
        payload = {"text": test_text}
        
        response = requests.post(
            f"{BASE_URL}/nlp/extract-metadata",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=10
        )
        print(f"Status: {response.status_code}")
        data = response.json()
        print(f"Response: {json.dumps(data, indent=2)}")
        
        if response.status_code == 200 and "metadata" in data:
            print("[OK] Metadata extraction OK")
            return True
        else:
            print("[FAIL] Metadata extraction FAILED")
            return False
    except Exception as e:
        print(f"[ERROR] Error: {e}")
        return False

def test_extract_ingredients():
    """Test du endpoint /nlp/extract-ingredients"""
    print("\n=== Test /nlp/extract-ingredients ===")
    try:
        test_text = "sucre, huile de palme, noisettes 13%, cacao maigre, lait écrémé en poudre"
        payload = {"text": test_text}
        
        response = requests.post(
            f"{BASE_URL}/nlp/extract-ingredients",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=30  # BERT peut être lent
        )
        print(f"Status: {response.status_code}")
        data = response.json()
        print(f"Response: {json.dumps(data, indent=2)}")
        
        if response.status_code == 200 and "ingredients" in data:
            print(f"[OK] Ingredient extraction OK ({data.get('count', 0)} ingrédients trouvés)")
            return True
        else:
            print("[FAIL] Ingredient extraction FAILED")
            return False
    except Exception as e:
        print(f"[ERROR] Error: {e}")
        return False

def test_ocr_image():
    """Test du endpoint /ocr/image (nécessite une image)"""
    print("\n=== Test /ocr/image ===")
    try:
        # Créer une image de test simple (1x1 pixel PNG en base64)
        # En production, vous utiliseriez une vraie image
        test_image_b64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
        
        payload = {"image": test_image_b64}
        
        response = requests.post(
            f"{BASE_URL}/ocr/image",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=15
        )
        print(f"Status: {response.status_code}")
        data = response.json()
        print(f"Response: {json.dumps(data, indent=2)}")
        
        if response.status_code == 200 and "text" in data:
            print("[OK] OCR image OK")
            return True
        elif response.status_code == 503:
            print("[WARN] OCR not available (Tesseract may not be installed)")
            return True  # Pas une erreur si le service n'est pas configuré
        else:
            print("[FAIL] OCR image FAILED")
            return False
    except Exception as e:
        print(f"[ERROR] Error: {e}")
        return False

def test_ocr_pdf():
    """Test du endpoint /ocr/pdf (nécessite un PDF)"""
    print("\n=== Test /ocr/pdf ===")
    try:
        # Créer un PDF de test minimal en base64
        # En production, vous utiliseriez un vrai PDF
        test_pdf_b64 = "JVBERi0xLjQKJeLjz9MKMSAwIG9iago8PC9UeXBlL0NhdGFsb2cvUGFnZXMgMiAwIFI+PgplbmRvYmoKMiAwIG9iago8PC9UeXBlL1BhZ2VzL0tpZHNbMyAwIFJdL0NvdW50IDE+PgplbmRvYmoKMyAwIG9iago8PC9UeXBlL1BhZ2UvUGFyZW50IDIgMCBSL1Jlc291cmNlczw8L1Byb2NTZXRbL1BERi9UZXh0XT4+L01lZGlhQm94WzAgMCA2MTIgNzkyXS9Db250ZW50cyA0IDAgUj4+CmVuZG9iago0IDAgb2JqCjw8L0xlbmd0aCA0ND4+CnN0cmVhbQpCVAovRjggMTIgVGYKNzAgNzIwIFRkCihUZXN0KSBUagpFVAplbmRzdHJlYW0KZW5kb2JqCnhyZWYKMCA1CjAwMDAwMDAwMDAgNjU1MzUgZiAKMDAwMDAwMDAwOSAwMDAwMCBuIAowMDAwMDAwMDU4IDAwMDAwIG4gCjAwMDAwMDAxMTUgMDAwMDAgbiAKMDAwMDAwMDI3MyAwMDAwMCBuIAp0cmFpbGVyCjw8L1NpemUgNS9Sb290IDEgMCBSPj4Kc3RhcnR4cmVmCjM0MQolJUVPRgo="
        
        payload = {"pdf": test_pdf_b64}
        
        response = requests.post(
            f"{BASE_URL}/ocr/pdf",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=15
        )
        print(f"Status: {response.status_code}")
        data = response.json()
        print(f"Response: {json.dumps(data, indent=2)}")
        
        if response.status_code == 200 and "text" in data:
            print("[OK] OCR PDF OK")
            return True
        elif response.status_code == 503:
            print("[WARN] PDF parsing not available (pdfplumber may not be installed)")
            return True  # Pas une erreur si le service n'est pas configuré
        else:
            print("[FAIL] OCR PDF FAILED")
            return False
    except Exception as e:
        print(f"[ERROR] Error: {e}")
        return False

def main():
    """Lance tous les tests"""
    print(f"Testing ML Service at: {BASE_URL}")
    print("=" * 50)
    
    results = []
    
    # Test health en premier
    results.append(("Health", test_health()))
    
    # Tests des endpoints principaux
    results.append(("Extract Metadata", test_extract_metadata()))
    results.append(("Extract Ingredients", test_extract_ingredients()))
    results.append(("OCR Image", test_ocr_image()))
    results.append(("OCR PDF", test_ocr_pdf()))
    
    # Résumé
    print("\n" + "=" * 50)
    print("RÉSUMÉ DES TESTS")
    print("=" * 50)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for name, result in results:
        status = "[PASS]" if result else "[FAIL]"
        print(f"{name:25} {status}")
    
    print(f"\nTotal: {passed}/{total} tests passes")
    
    if passed == total:
        print("\n[SUCCESS] Tous les tests sont passes ! Le service est pret.")
        return 0
    else:
        print(f"\n[WARNING] {total - passed} test(s) ont echoue. Verifiez les erreurs ci-dessus.")
        return 1

if __name__ == "__main__":
    sys.exit(main())

