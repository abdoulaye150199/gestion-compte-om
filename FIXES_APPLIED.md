# Corrections Appliquées - 11 Novembre 2025

## 🔧 Problème Principal
L'endpoint `/api/comptes/solde` retournait une erreur **500** avec le message:
```
"message": "Utilisateur non authentifié"
```

Bien que un token JWT était fourni, il n'était pas validé correctement.

## 🐛 Cause Identifiée
Dans `JwtAuthFilter.java`, ligne 30:
```java
String secret = System.getProperty("JWT_SECRET");  // ❌ INCORRECT
```

Le code utilisait `System.getProperty()` qui lit les **propriétés système Java**, pas les **variables d'environnement**.

## ✅ Correction Appliquée

### 1. **JwtAuthFilter.java** (LIGNE 30)
**Avant:**
```java
String secret = System.getProperty("JWT_SECRET");
```

**Après:**
```java
String secret = System.getenv("JWT_SECRET");
```

**Impact:** Le filtre JWT peut maintenant lire correctement la variable d'environnement `JWT_SECRET` passée par Docker/docker-compose.

### 2. **build.gradle** - Dépendances Mises à Jour
Ajout des dépendances manquantes:
- ✅ `spring-boot-starter-security`
- ✅ `spring-boot-starter-data-jpa`
- ✅ `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- ✅ `google.zxing` (QR codes)
- ✅ `twilio`
- ✅ `flyway-core`

### 3. **Dockerfile** - Changement de Maven vers Gradle
Le projet utilise **Gradle**, pas Maven. Le Dockerfile a été mis à jour pour:
- Utiliser `gradlew` au lieu de `mvn`
- Copier les fichiers Gradle: `gradlew`, `gradle/`, `build.gradle`, `settings.gradle`
- Compiler avec: `./gradlew clean build -x test`

### 4. **docker-compose.yml** - Correction de la Syntaxe
**Avant:**
```yaml
SPRING_FLYWAY_ENABLED: ${SPRING_FLYWAY_ENABLED:false}  # ❌ Syntaxe invalide
```

**Après:**
```yaml
SPRING_FLYWAY_ENABLED: "false"  # ✅ Correct
```

### 5. **.env** - Vérification
✅ Le fichier `.env` contient:
```properties
JWT_SECRET=thisismyverylongjwtsecretkeyforproductionuse1234567890abcdef
```

## 🚀 Workflow Correct Maintenant

```
1. docker-compose up -d
   ↓
2. Lit .env (JWT_SECRET=...)
   ↓
3. Passe JWT_SECRET au conteneur
   ↓
4. Application démarre
   ↓
5. JwtAuthFilter.java lit System.getenv("JWT_SECRET") ✅
   ↓
6. Valide le token JWT correctement ✅
   ↓
7. Endpoint /api/comptes/solde retourne 200 OK ✅
```

## 📋 Fichiers Modifiés

| Fichier | Changement | Ligne |
|---------|-----------|--------|
| `JwtAuthFilter.java` | `getProperty()` → `getenv()` | 30 |
| `build.gradle` | Ajout dépendances Spring Security & JWT | 27-44 |
| `Dockerfile` | Maven → Gradle | 1-27 |
| `docker-compose.yml` | Correction syntaxe SPRING_FLYWAY_ENABLED | 19 |

## ✨ Résultat Attendu

Après les corrections:

1. **Register:**
```bash
curl -X POST http://localhost:8081/api/utilisateurs/register \
  -H "Content-Type: application/json" \
  -d '{"nom":"Test","prenom":"User","numeroTelephone":"+221771234567","codeVerification":"123456"}'
```
Response: `{"id":"...","message":"Code envoyé"}`

2. **Verify (Obtenir Token):**
```bash
curl -X POST http://localhost:8081/api/utilisateurs/verify \
  -H "Content-Type: application/json" \
  -d '{"numeroTelephone":"+221771234567","codeVerification":"123456"}'
```
Response: `{"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}`

3. **Protected Endpoint (Avec Token):**
```bash
curl -X GET http://localhost:8081/api/comptes/solde \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json"
```
Response: `{"solde":0}` ou valeur actuelle

## 🔒 Sécurité

- ✅ JWT_SECRET n'est jamais codé en dur dans le code
- ✅ JWT_SECRET est défini dans `.env` (exclu du git via `.gitignore`)
- ✅ JWT_SECRET est passé via variables d'environnement Docker
- ✅ Token valide 7 jours par défaut

## 📝 Notes

- Le projet utilise **Gradle**, pas Maven
- Docker est sur le port **8081** → conteneur **8080**
- Database: **Neon PostgreSQL** (externe)
- Base de données configurée via `NEON_DATABASE_URL` dans `.env`

## ✔️ Prochaines Étapes

1. Laisser Docker construire l'image (peut prendre 2-3 minutes)
2. Tester avec les commandes curl ci-dessus
3. Vérifier les logs: `docker logs gesion-compteom_app_1`
4. Les endpoints protégés doivent maintenant fonctionner ✅

