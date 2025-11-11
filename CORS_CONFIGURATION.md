# 🔗 Configuration CORS et URL API

## ✅ CORS est maintenant configuré!

L'API accepte maintenant les requêtes depuis:
- ✅ `http://localhost:3000` (développement local React)
- ✅ `http://localhost:8080` (développement local)
- ✅ `https://gestion-compte-om-1.onrender.com` (production)

---

## 🚨 ERREUR CORS - Comment corriger

Si vous recevez:
```
Access to fetch at 'http://localhost:8080' from origin 'https://gestion-compte-om-1.onrender.com' 
has been blocked by CORS policy
```

### Cause
Votre **frontend** sur Render essaie d'accéder à `http://localhost:8080` au lieu de l'API Render.

### ✅ Solution

**Dans votre code frontend (React, Angular, Vue, etc.):**

❌ **Ne PAS faire:**
```javascript
fetch('http://localhost:8080/api/utilisateurs/register', ...)
```

✅ **Faire à la place:**
```javascript
// En production (Render)
const API_URL = 'https://gestion-compte-om-1.onrender.com';
fetch(`${API_URL}/api/utilisateurs/register`, ...)

// Ou utiliser une variable d'environnement
const API_URL = process.env.REACT_APP_API_URL || 'https://gestion-compte-om-1.onrender.com';
```

---

## 🛠️ Configuration par environnement

### React/Next.js

**`.env.local` (développement local)**
```
REACT_APP_API_URL=http://localhost:8080
```

**`.env.production` (production)**
```
REACT_APP_API_URL=https://gestion-compte-om-1.onrender.com
```

**Dans votre code**
```javascript
const API_URL = process.env.REACT_APP_API_URL;

// Register
fetch(`${API_URL}/api/utilisateurs/register`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ numeroTelephone: '+221...', codeVerification: '1234' })
})
```

### Angular

**`environment.ts` (développement)**
```typescript
export const environment = {
  apiUrl: 'http://localhost:8080'
};
```

**`environment.prod.ts` (production)**
```typescript
export const environment = {
  apiUrl: 'https://gestion-compte-om-1.onrender.com'
};
```

**Service**
```typescript
import { environment } from '../environments/environment';

@Injectable()
export class AuthService {
  private apiUrl = environment.apiUrl;

  register(phone: string, code: string) {
    return this.http.post(`${this.apiUrl}/api/utilisateurs/register`, {
      numeroTelephone: phone,
      codeVerification: code
    });
  }
}
```

### Vue.js

**`src/config.js`**
```javascript
const API_URL = process.env.NODE_ENV === 'production'
  ? 'https://gestion-compte-om-1.onrender.com'
  : 'http://localhost:8080';

export default API_URL;
```

**Dans les composants**
```javascript
import API_URL from '@/config.js';

methods: {
  register() {
    fetch(`${API_URL}/api/utilisateurs/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ... })
    })
  }
}
```

---

## 🔒 Ajouter le JWT Bearer Token

Après l'authentification, vous obtenez un JWT token. Voici comment l'utiliser:

### Stocker le token
```javascript
// Après verify()
const response = await fetch(`${API_URL}/api/utilisateurs/verify`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ numeroTelephone: '...', codeVerification: '...' })
});

const data = await response.json();
localStorage.setItem('token', data.token);  // 💾 Sauvegarder le token
```

### Utiliser le token pour les endpoints protégés
```javascript
// Récupérer le token
const token = localStorage.getItem('token');

// Appeler un endpoint protégé
fetch(`${API_URL}/api/comptes/solde`, {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`  // ← Important!
  }
})
```

### Créer un helper HTTP
```javascript
class ApiClient {
  static async request(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
      ...options,
      headers
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`);
    }

    return response.json();
  }

  static get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  }

  static post(endpoint, body) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body)
    });
  }
}

// Utilisation
ApiClient.get('/api/comptes/solde')
  .then(data => console.log(data))
  .catch(err => console.error(err));
```

---

## 📋 Checklist

- [ ] CORS configuré dans l'API Spring Boot ✅
- [ ] Frontend utilise `https://gestion-compte-om-1.onrender.com` en production
- [ ] Frontend utilise `http://localhost:8080` en développement local
- [ ] JWT token sauvegardé dans localStorage après verify
- [ ] JWT token inclus dans le header `Authorization: Bearer <TOKEN>`
- [ ] Les endpoints protégés reçoivent le token

---

## 🧪 Test rapide avec curl

```bash
# CORS test - register (public, pas besoin de CORS spécial)
curl -X POST "https://gestion-compte-om-1.onrender.com/api/utilisateurs/register" \
  -H "Origin: https://gestion-compte-om-1.onrender.com" \
  -H "Content-Type: application/json" \
  -d '{"numeroTelephone":"+221774730039","codeVerification":"1234"}'

# Vérifier que Access-Control-Allow-Origin est présent
curl -i -X OPTIONS "https://gestion-compte-om-1.onrender.com/api/utilisateurs/register" \
  -H "Origin: https://gestion-compte-om-1.onrender.com" \
  -H "Access-Control-Request-Method: POST"
```

---

## ✅ Problème résolu!

Maintenant votre frontend peut appeler l'API depuis Render sans erreurs CORS! 🎉

