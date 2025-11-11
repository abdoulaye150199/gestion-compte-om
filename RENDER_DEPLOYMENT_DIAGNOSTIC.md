# 🔧 Diagnostic de déploiement Render

## 📋 Checklist - Vérifier ceci dans Render Dashboard

### 1. Service Details
```
Aller sur: https://dashboard.render.com
Sélectionner: gestion-compte-om
```

Vérifier:
- [ ] Status est **"Live"** (🟢 vert) ou **"Building"** (🟡 jaune)
- [ ] Region: Ohio ou autre
- [ ] Environment: Docker
- [ ] Branch: main

### 2. Logs - Onglet "Logs"
```
Cliquer sur: Logs tab
```

Chercher ces messages:
- ✅ `Started GesionCompteOmApplication in X.XXX seconds`
- ✅ `Listening on port 8080`
- ✅ `Tomcat initialized with port(s): 8080`

Si vous voyez ❌:
- ❌ `ERROR` ou `Exception`
- ❌ `Application failed to start`
- ❌ `Database connection failed`

### 3. Environment Variables - Onglet "Environment"
```
Cliquer sur: Environment tab
```

Vérifier que ces 7 variables existent ET ont des valeurs:
- [ ] `SPRING_DATASOURCE_URL` = (votre URL Neon)
- [ ] `SPRING_DATASOURCE_USERNAME` = (votre username)
- [ ] `SPRING_DATASOURCE_PASSWORD` = (votre password)
- [ ] `JWT_SECRET` = (votre secret)
- [ ] `TWILIO_ACCOUNT_SID` = (votre SID)
- [ ] `TWILIO_AUTH_TOKEN` = (votre token)
- [ ] `TWILIO_FROM` = (votre numéro Twilio)

**ATTENTION**: Si une variable est VIDE, c'est pour ça que ça ne marche pas!

### 4. Tests simples

Si le service est **Live** (🟢), testez:

#### a) Health Check
```bash
curl https://gestion-compte-om.onrender.com/actuator/health
```

Résultat attendu:
```json
{"status":"UP"}
```

#### b) Register
```bash
curl -X POST "https://gestion-compte-om.onrender.com/api/utilisateurs/register" \
  -H "Content-Type: application/json" \
  -d '{"numeroTelephone":"+221774730039","codeVerification":"1234"}'
```

Résultat attendu:
- ✅ Code 200 avec message
- ❌ Code 400 si numéro existe déjà

#### c) Verify
```bash
curl -X POST "https://gestion-compte-om.onrender.com/api/utilisateurs/verify" \
  -H "Content-Type: application/json" \
  -d '{"numeroTelephone":"+221774730039","codeVerification":"1234"}'
```

Résultat attendu:
- ✅ Code 200 avec JWT token
- ❌ Code 400 si code incorrect

---

## 🚨 Problèmes courants et solutions

### Problème 1: Service en 503 (Service Unavailable)
**Causes**:
- L'app est encore en train de démarrer (attendre 5-10 minutes)
- L'app a crashé au démarrage

**Solutions**:
1. Attendre 5 minutes
2. Vérifier les Logs
3. Si erreur en logs, voir "Problème 2" ou "Problème 3"

### Problème 2: Database Connection Failed
**Erreur dans les logs**:
```
Failed to determine suitable jdbc url
could not connect to Neon
```

**Causes**:
- `SPRING_DATASOURCE_URL` est vide ou incorrect
- `SPRING_DATASOURCE_USERNAME` ou `PASSWORD` manquent
- La base de données Neon n'existe pas

**Solutions**:
1. Allez sur Environment tab dans Render
2. Vérifiez les 3 variables DATABASE:
   ```
   SPRING_DATASOURCE_URL = jdbc:postgresql://ep-xxxx.c-3.us-east-1.aws.neon.tech/ompay?sslmode=require&channel_binding=require
   SPRING_DATASOURCE_USERNAME = neondb_owner
   SPRING_DATASOURCE_PASSWORD = votre_password
   ```
3. Si vides, remplissez-les (copier depuis votre `.env` local)
4. Cliquez **"Manual Deploy"** pour redéployer

### Problème 3: JWT_SECRET manquant
**Erreur dans les logs**:
```
JWT_SECRET is required
PropertySourceException: Could not resolve placeholder
```

**Causes**:
- `JWT_SECRET` est vide dans Environment

**Solutions**:
1. Allez sur Environment tab
2. Trouvez `JWT_SECRET`
3. Remplissez-la (copier depuis `.env` local)
4. Cliquez **"Manual Deploy"**

### Problème 4: Twilio Not Initialized
**Erreur dans les logs**:
```
Failed to init Twilio
TwilioException
```

**Causes**:
- `TWILIO_ACCOUNT_SID` ou `TWILIO_AUTH_TOKEN` vides
- Credentials Twilio invalides

**Solutions**:
1. Allez sur Environment tab
2. Vérifiez les 3 variables TWILIO:
   ```
   TWILIO_ACCOUNT_SID = AC... (commence par AC)
   TWILIO_AUTH_TOKEN = ...
   TWILIO_FROM = +1... (numéro Twilio)
   ```
3. Si vides, remplissez-les
4. Cliquez **"Manual Deploy"**

### Problème 5: Failed to fetch (Swagger UI)
**Erreur dans Swagger**:
```
Failed to fetch
URL scheme must be "http" or "https"
```

**Causes**:
- L'URL Swagger est `https://your-app.onrender.com` (placeholder)
- L'app n'est pas accessible

**Solutions**:
1. **Remplacez l'URL dans Swagger UI**:
   - En haut à gauche, trouvez le champ "Servers"
   - Remplacez `https://your-app.onrender.com` par `https://gestion-compte-om.onrender.com`
   - Cliquez "Change Server"

2. Testez avec curl à la place:
   ```bash
   curl https://gestion-compte-om.onrender.com/actuator/health
   ```

---

## ✅ Action immédiate

1. **Allez sur Render Dashboard**: https://dashboard.render.com
2. **Cliquez sur votre service**: gestion-compte-om
3. **Onglet "Environment"**: Vérifiez les 7 variables
4. **Onglet "Logs"**: Cherchez les erreurs
5. **Si erreurs**: Remplissez les variables manquantes
6. **Cliquez "Manual Deploy"**: Pour relancer
7. **Attendez 5-10 minutes**
8. **Testez avec curl**:
   ```bash
   curl https://gestion-compte-om.onrender.com/actuator/health
   ```

---

## 📞 Besoin d'aide?

Si vous êtes bloqué:
1. **Partagez vos logs** (copier depuis Render Logs)
2. **Vérifiez les 7 variables d'environnement** (elles sont remplies?)
3. **Testez en local d'abord** (`http://localhost:8080`)

