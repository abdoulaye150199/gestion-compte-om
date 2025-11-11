# Configuration Render - Guide de déploiement

## 🚀 Déploiement sur Render

Ce guide explique comment déployer l'application sur Render avec les bonnes configurations.

### Prérequis

1. Repository Git poussé sur GitHub
2. Compte Render (gratuit sur render.com)
3. Variables d'environnement prêtes (dans votre `.env` local)

### Étapes de déploiement

#### 1. Connecter votre repository GitHub à Render

1. Allez sur [render.com](https://render.com)
2. Cliquez sur **"New +"** → **"Web Service"**
3. Sélectionnez **"Build and deploy from a Git repository"**
4. Cliquez sur **"Connect"** et authentifiez-vous avec GitHub
5. Sélectionnez le repository `gestion-compte-om`
6. Configurez les paramètres suivants:

   | Paramètre | Valeur |
   |-----------|--------|
   | Name | `gestion-compte-om` |
   | Environment | `Docker` |
   | Region | `Ohio` ou `Frankfurt` |
   | Branch | `main` |

7. Cliquez sur **"Create Web Service"**

#### 2. Configurer les variables d'environnement

Une fois le service créé, allez dans l'onglet **"Environment"** et ajoutez ces 7 variables (voir `.env` local pour les valeurs):

```
SPRING_DATASOURCE_URL       (PostgreSQL Neon URL)
SPRING_DATASOURCE_USERNAME  (Neon username)
SPRING_DATASOURCE_PASSWORD  (Neon password)
JWT_SECRET                  (JWT secret key)
TWILIO_ACCOUNT_SID          (Twilio Account SID)
TWILIO_AUTH_TOKEN           (Twilio Auth Token)
TWILIO_FROM                 (Twilio phone number)
```

**⚠️ Sécurité:** Les valeurs viennent de votre `.env` local. Ne les commitez JAMAIS dans Git.

#### 3. Configuration du build

Le fichier `render.yaml` configure automatiquement:

- **Dockerfile**: Multi-stage build avec Maven
- **Port**: 8080 (exposé automatiquement)
- **Health Check**: Via `/actuator/health`
- **Environnement**: Docker

#### 4. Déclencher le déploiement

Deux options:

**Option A - Déploiement automatique** (recommandé):
- Pushez des commits sur `main` → Render redéploie automatiquement

**Option B - Déploiement manuel**:
- Dashboard Render → Click **"Manual Deploy"**
- Environ 5-10 minutes pour build et startup

#### 5. Vérifier le déploiement

1. Dans Render Dashboard, vérifiez les **logs** pour voir:
   ```
   Started GesionCompteOmApplication
   Listening on port 8080
   ```

2. Une fois déployé, l'URL sera: `https://gestion-compte-om.onrender.com`

3. Testez un endpoint:
   ```bash
   curl https://gestion-compte-om.onrender.com/swagger-ui.html
   ```

### Dépannage

#### Service ne démarre pas

Vérifiez les logs Render pour les erreurs. Problèmes courants:

- ❌ Variables d'environnement manquantes → Ajoutez-les dans le Dashboard
- ❌ Base de données inaccessible → Vérifiez `SPRING_DATASOURCE_URL`
- ❌ Port non écouté → Vérifiez que l'app écoute sur 8080

#### Erreur de build

Si le build échoue:

1. Vérifiez le `pom.xml` contient toutes les dépendances
2. Vérifiez que le `Dockerfile` utilise Maven (`mvnw`)
3. Vérifiez le `.mvn` et `mvnw` sont commitées dans le repo

#### Application redémarre continuellement

Cela indique une erreur au démarrage. Vérifiez:

1. Les logs pour l'erreur exacte
2. Que JWT_SECRET n'est pas vide
3. Que la base de données est accessible

### Structure du déploiement

```
Repository GitHub (main-production)
       ↓
   render.yaml (configuration)
       ↓
   Dockerfile (build avec Maven)
       ↓
   Render Build Server
       ↓
   Docker Image
       ↓
   Container running on Render
```

### Variables d'environnement

Les variables viennent de trois sources:

1. **render.yaml** - Définit quelles variables sont nécessaires
2. **Dockerfile** - Expose le port 8080
3. **Render Dashboard** - Injecte les vraies valeurs

### Après le déploiement

✅ L'application est accessible publiquement
✅ Swagger UI disponible à `/swagger-ui.html`
✅ SMS Twilio opérationnel
✅ Authentification JWT active
✅ Base de données Neon connectée

### Support

- **Render Docs**: https://render.com/docs
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Neon PostgreSQL**: https://neon.tech/docs
- **Twilio**: https://www.twilio.com/docs
