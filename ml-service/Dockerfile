FROM python:3.11-slim

# Installer Tesseract OCR et dépendances système
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-fra \
    tesseract-ocr-eng \
    libtesseract-dev \
    poppler-utils \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copier requirements et installer
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Télécharger les modèles spaCy
RUN python -m spacy download fr_core_news_sm || python -m spacy download en_core_web_sm || true

# Copier le code
COPY app.py .

EXPOSE 8086

CMD ["python", "app.py"]


