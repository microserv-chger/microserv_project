FROM python:3.11-slim

# Installer Tesseract OCR et dépendances système
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-fra \
    tesseract-ocr-eng \
    libtesseract-dev \
    poppler-utils \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copier requirements et installer
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Télécharger les modèles spaCy (essayer français d'abord, puis anglais en fallback)
# Note: spacy download nécessite que spacy soit déjà installé (fait dans l'étape précédente)
RUN python -m spacy download fr_core_news_sm || \
    (python -m spacy download en_core_web_sm || echo "Warning: No spaCy model downloaded") || true

# Copier le code
COPY app.py .

# Vérifier que Tesseract est installé
RUN tesseract --version || echo "Warning: Tesseract check failed"

EXPOSE 8086

# Healthcheck pour Docker
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8086/health || exit 1

CMD ["python", "app.py"]


