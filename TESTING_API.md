# Guide de test de l'API GestionCompteOM

## 🚀 Statuts de déploiement

### ✅ Déploiement local
- **URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Status**: Accessible directement

### ⏳ Déploiement Render
- **URL attendue**: `https://gestion-compte-om.onrender.com`
- **Swagger UI**: `https://gestion-compte-om.onrender.com/swagger-ui.html`
- **Status**: En attente de déploiement

---

## 🧪 Test local (Recommandé d'abord)

### 1. Démarrer l'application localement

```bash
cd /home/abdoulaye/Musique/Java/Gesion-CompteOM
mvn spring-boot:run
```

Attendre le message: `Started GesionCompteOmApplication`

### 2. Accéder à Swagger UI

Ouvrez dans votre navigateur:
```
http://localhost:8080/swagger-ui.html
```

### 3. Tester l'endpoint REGISTER

**Endpoint**: `POST /api/utilisateurs/register`

**Body JSON**:
```json
{
  "numeroTelephone": "+221774730039",
  "codeVerification": "1234"
}
```

**Résultat attendu**: 
- ✅ Code 200 avec le code SMS envoyé
- Ou ❌ Code 400 si le numéro existe déjà

### 4. Tester l'endpoint VERIFY

**Endpoint**: `POST /api/utilisateurs/verify`

**Body JSON** (utilisez le code de l'étape 3):
```json
{
  "numeroTelephone": "+221774730039",
  "codeVerification": "1234"
}
```

**Résultat attendu**:
- ✅ Code 200 avec JWT token
- 🔑 Le token commence par `eyJ...`

### 5. Utiliser le JWT pour les endpoints protégés

**Endpoint**: `GET /api/comptes/solde`

**Header requis**:
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

**Exemple complet avec curl**:
```bash
curl -X GET "http://localhost:8080/api/comptes/solde" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlMzQyYzA3Zi1lYmI1LTQ5YTktOWM0Yi1kNDI4M2NhNmYwYmYiLCJpc192ZXJpZmllZCI6dHJ1ZSwiaWF0IjoxNzYyODcxMDIyLCJleHAiOjE3NjM0NzU4MjJ9.fe2VUxJVDcJQ5GsL11xpNQlBCTSR7qPmEY2KZtCn4uI"
```

**Résultat attendu**:
```json
{
  "solde": 0
}
```

---

## 🌐 Test sur Render

### ⚠️ IMPORTANT: Configuration requise

Avant de tester sur Render, vous DEVEZ:

1. ✅ **Vérifier le déploiement** (voir ci-dessous)
2. ✅ **Configurer les 7 variables d'environnement** dans Render:
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
   - `JWT_SECRET`
   - `TWILIO_ACCOUNT_SID`
   - `TWILIO_AUTH_TOKEN`
   - `TWILIO_FROM`

3. ✅ **Vérifier les logs** pour voir: `Started GesionCompteOmApplication`

### Vérifier le statut du déploiement

1. Allez sur https://dashboard.render.com
2. Sélectionnez votre service `gestion-compte-om`
3. Vérifiez l'onglet **"Logs"**
4. Cherchez:
   - ✅ `Started GesionCompteOmApplication`
   - ❌ `ERROR` ou `Exception`
5. Vérifiez l'onglet **"Environment"**:
   - ✅ Les 7 variables sont présentes
   - ❌ Pas de variables vides

### Si le statut est "Live" (🟢)

Votre app est accessible à: `https://gestion-compte-om.onrender.com`

**Testez avec curl**:
```bash
curl -X GET "https://gestion-compte-om.onrender.com/actuator/health"
```

**Résultat attendu**: Status 200 avec `{"status":"UP"}`

### Tester les endpoints sur Render

Même process que local, mais remplacez:
- `http://localhost:8080` par `https://gestion-compte-om.onrender.com`

**Exemple**:
```bash
# Register
curl -X 'POST' \
  'https://gestion-compte-om.onrender.com/api/utilisateurs/register' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "numeroTelephone": "+221774730039",
  "codeVerification": "1234"
}'

# Verify
curl -X 'POST' \
  'https://gestion-compte-om.onrender.com/api/utilisateurs/verify' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "numeroTelephone": "+221774730039",
  "codeVerification": "1234"
}'
```

---

## 🐛 Dépannage

### ❌ "Failed to fetch" dans Swagger UI

**Causes possibles**:
1. L'app n'est pas démarrée
2. Le port 8080 n'est pas accessible
3. Problème CORS

**Solutions**:
```bash
# Vérifier que l'app tourne
lsof -i :8080

# Vérifier les logs Render
# Aller sur Render Dashboard → Logs tab

# Tester directement avec curl (pas de CORS)
curl http://localhost:8080/actuator/health
```

### ❌ "Utilisateur non authentifié" (401)

**Cause**: Token JWT manquant ou invalide

**Solution**:
1. Vérifiez que vous avez un token valide (from `/verify`)
2. Vérifiez que le header est correct: `Authorization: Bearer <TOKEN>`
3. Vérifiez que le token n'a pas expiré (7 jours)

### ❌ "Accès refusé" (403)

**Cause**: Token valide mais endpoint ne l'accepte pas

**Solution**:
1. Vérifiez que le endpoint est protégé (doit avoir le bearer token)
2. Vérifiez dans SecurityConfig que l'endpoint est en `.authenticated()`

### ❌ Erreur de base de données

**Message**: `Failed to determine suitable jdbc url`

**Solution**:
1. Vérifiez `SPRING_DATASOURCE_URL` est correct dans Render
2. Testez la connexion Neon:
   ```bash
   psql "jdbc:postgresql://..." -U neondb_owner -W
   ```
3. Vérifiez la base de données existe

### ❌ Erreur Twilio

**Message**: `Twilio not initialized`

**Solution**:
1. Vérifiez `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM` dans Render
2. Testez localement d'abord avec `.env`
3. Vérifiez votre compte Twilio a des crédits

---

## 📊 Endpoints disponibles

### Publics (pas d'authentification)

```
POST   /api/utilisateurs/register
POST   /api/utilisateurs/verify
GET    /swagger-ui.html
GET    /actuator/health
```

### Protégés (JWT requis)

```
GET    /api/comptes/solde
POST   /api/comptes/depot
POST   /api/comptes/retrait
GET    /api/comptes/qr
POST   /api/comptes/transfert
POST   /api/comptes/payer
```

---

## ✅ Checklist de déploiement

- [ ] Code poussé sur GitHub (main branch)
- [ ] Render détecte les changements
- [ ] Build Docker réussit
- [ ] Container démarre (logs: "Started GesionCompteOmApplication")
- [ ] 7 variables d'environnement configurées dans Render
- [ ] Health check: `/actuator/health` retourne 200
- [ ] Endpoint public marche: `/api/utilisateurs/register`
- [ ] JWT marche: `/api/utilisateurs/verify` retourne un token
- [ ] Endpoint protégé marche avec JWT: `/api/comptes/solde`
- [ ] Swagger UI accessible: `/swagger-ui.html`

---

## 🔗 Ressources

- **Render Docs**: https://render.com/docs
- **Spring Boot**: https://spring.io/projects/spring-boot
- **JWT**: https://jwt.io
- **Neon PostgreSQL**: https://neon.tech/docs
- **Twilio**: https://www.twilio.com/docs
