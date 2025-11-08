Déploiement sur Render (Docker)

Ce dépôt contient un `Dockerfile` et un `docker-compose.yml` pour un déploiement local, et un `render.yaml` pour déployer sur Render en utilisant Docker.

Étapes rapides pour déployer sur Render (Docker)

1. Connecte-toi à Render et crée un nouveau service de type "Web Service" en choisissant "Docker".
2. Indique le `Dockerfile` à la racine (chemin: `Dockerfile`).
3. Dans Settings → Environment, ajoute les variables d'environnement suivantes (Render te fournira automatiquement les credentiels de la base gérée si tu crées une DB via Render):
   - SPRING_DATASOURCE_URL (ex: jdbc:postgresql://{DB_HOST}:{DB_PORT}/{DB_NAME})
   - SPRING_DATASOURCE_USERNAME
   - SPRING_DATASOURCE_PASSWORD

Configuration `render.yaml` fournie dans le repo :
- Déclare un service Docker nommé `gestion-compte-om` et une base Postgres managée.

Conseil local (test avant déploiement) :
1. Construire localement le jar :

```bash
./gradlew clean bootJar
```

2. Lancer en local avec docker-compose :

```bash
docker-compose up --build
```

Si tu veux, je peux :
- Mettre à jour le `Dockerfile` pour copier explicitement `build/libs/*.jar` et éviter les erreurs lors du build sur Render.
- Ajouter un script `entrypoint.sh` pour attendre la DB (wait-for-it)

Dis-moi si tu veux que j'applique l'une de ces améliorations automatiquement ; je peux aussi adapter le `Dockerfile` pour être plus robuste sur la plateforme Render.

