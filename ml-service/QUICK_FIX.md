# Correction appliquée - Sérialisation JSON

## Problème résolu

Le problème `Object of type float32 is not JSON serializable` a été corrigé.

**Cause :** BERT retourne des valeurs `float32` numpy au lieu de `float` Python natifs.

**Solution :** Conversion explicite des valeurs numpy en types Python natifs avant sérialisation JSON.

## Redémarrer le service

Pour appliquer la correction, redémarrez le service Docker :

```bash
# Arrêter le service
docker compose stop ml-service

# Rebuild avec les corrections
docker compose build ml-service

# Redémarrer
docker compose up ml-service
```

Ou en une seule commande :

```bash
docker compose up --build ml-service
```

## Tester la correction

Une fois le service redémarré :

```bash
# Depuis le dossier ml-service
python test_endpoints.py http://localhost:8086
```

Le test `/nlp/extract-ingredients` devrait maintenant passer.

## Note sur spaCy

Le warning `No spaCy model found` n'est pas critique. Le service fonctionne avec :
- **BERT** pour l'extraction d'ingrédients (fonctionne ✅)
- **Regex fallback** pour les métadonnées (fonctionne ✅)

Pour installer spaCy dans le container, vous pouvez :
1. Modifier le Dockerfile pour forcer le téléchargement
2. Ou l'installer manuellement dans le container en cours d'exécution

Mais ce n'est **pas nécessaire** pour que le service fonctionne correctement.

