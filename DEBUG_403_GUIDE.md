# 403 Error Debugging Guide for POST /api/usuarios

This guide helps systematically identify and resolve 403 Forbidden errors when calling POST /api/usuarios.

## Debug Configurations Created

### 1. Complete Security Bypass (`debug` profile)
**File:** `DebugSecurityConfig.java`
**Profile:** `debug`

Completely disables all security to isolate authentication/authorization issues.

**Usage:**
```bash
mvn spring-boot:run -Dspring.profiles.active=debug
```

**What it does:**
- Permits ALL endpoints without authentication
- Disables CSRF completely
- Most permissive CORS settings
- Stateless sessions

**Test with:**
```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Test User","email":"test@example.com","tipoUsuario":"ATLETA","senha":"123456"}'
```

**Expected result:** If this works, the issue is security-related.

### 2. CORS Debug Configuration (`cors-debug` profile)
**File:** `CorsDebugConfig.java`
**Profile:** `cors-debug`

Provides detailed CORS logging and handles OPTIONS preflight explicitly.

**Usage:**
```bash
mvn spring-boot:run -Dspring.profiles.active=cors-debug
```

**What it does:**
- Verbose CORS logging
- Explicit OPTIONS handling
- Headers inspection
- Pre-flight request debugging

**Test with browser/frontend:**
```javascript
fetch('http://localhost:8080/api/usuarios', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    nome: 'Test User',
    email: 'test@example.com',
    tipoUsuario: 'ATLETA',
    senha: '123456'
  })
})
```

**Expected result:** Detailed CORS logs in console showing preflight handling.

### 3. Development Profile (`dev` profile)
**File:** `application-dev.properties`
**Profile:** `dev`

Enhanced logging for all security and web components.

**Usage:**
```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

**What it does:**
- DEBUG logging for Spring Security
- CORS filter debugging
- HTTP request/response debugging
- Detailed error messages

### 4. Filter Chain Debug (`filter-debug` profile)
**File:** `FilterOrderDebugConfig.java`
**Profile:** `filter-debug`

Traces the entire filter chain execution to identify blocking filters.

**Usage:**
```bash
mvn spring-boot:run -Dspring.profiles.active=filter-debug
```

**What it does:**
- Logs every request start/end
- Shows all headers
- Identifies 403 responses
- Traces filter execution order

## Systematic Debugging Process

### Step 1: Test with Complete Security Bypass
```bash
mvn spring-boot:run -Dspring.profiles.active=debug
```

**If it works:** Security configuration is the issue.
**If it fails:** Problem is not security-related (controller, validation, etc.)

### Step 2: Test CORS Preflight
```bash
mvn spring-boot:run -Dspring.profiles.active=cors-debug
```

Make the request from a browser. Check console for:
- ✈️ Preflight OPTIONS request handling
- 🌐 Origin and headers logging
- ✅ Successful OPTIONS response

**If OPTIONS fails:** CORS configuration issue.
**If OPTIONS succeeds but POST fails:** Security filter issue.

### Step 3: Analyze Filter Chain
```bash
mvn spring-boot:run -Dspring.profiles.active=filter-debug
```

Look for:
- 🛡️ Security filter processing
- 🚫 403 detection and possible causes
- Filter execution order

### Step 4: Combine Profiles for Detailed Analysis
```bash
mvn spring-boot:run -Dspring.profiles.active=dev,filter-debug
```

This provides:
- Detailed Spring Security logs
- Filter chain tracing
- HTTP debugging

## Common 403 Causes and Solutions

### 1. CORS Preflight Issues
**Symptom:** OPTIONS request fails or returns 403
**Solution:** 
- Check origin is allowed
- Verify Content-Type is permitted
- Ensure OPTIONS method is allowed

### 2. Spring Security Auto-Configuration
**Symptom:** Works with `debug` profile but fails normally
**Solution:**
- Check `@EnableWebSecurity` configuration
- Verify filter chain order
- Review security matcher patterns

### 3. Method Security Annotations
**Symptom:** Controller has `@PreAuthorize` on POST methods
**Check:** `UsuarioController.java` line 32-49 (POST /api/usuarios should be permitAll)

### 4. Filter Order Conflicts
**Symptom:** Custom filters interfere with security chain
**Solution:**
- Review filter registration order
- Check for conflicting CORS configurations
- Verify security filter chain precedence

## Expected Logs Analysis

### Successful Request Log Pattern:
```
🚀 REQUEST START: POST /api/usuarios
🛡️ SECURITY CHECK: POST /api/usuarios
👻 No authenticated user (anonymous)  ← Should be OK for user creation
✅ REQUEST COMPLETED: POST /api/usuarios
📊 Status: 201
```

### Failed Request Log Pattern:
```
🚀 REQUEST START: POST /api/usuarios
🛡️ SECURITY CHECK: POST /api/usuarios
🚫 403 FORBIDDEN detected for: POST /api/usuarios
❌ ERROR RESPONSE HEADERS:
  - Access-Control-Allow-Origin: [missing or wrong]
```

### CORS Preflight Success Pattern:
```
✈️ Handling OPTIONS preflight request for: /api/usuarios
✅ OPTIONS response sent with status 200
🚀 REQUEST START: POST /api/usuarios
✅ REQUEST COMPLETED: POST /api/usuarios
```

## Testing Commands

### Basic cURL Test:
```bash
curl -v -X POST http://localhost:8080/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Test User","email":"test@example.com","tipoUsuario":"ATLETA","senha":"123456"}'
```

### CORS Preflight Test:
```bash
curl -v -X OPTIONS http://localhost:8080/api/usuarios \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type"
```

### Browser Console Test:
```javascript
// Test from browser console (F12)
fetch('http://localhost:8080/api/usuarios', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    nome: 'Browser Test',
    email: 'browser@test.com',
    tipoUsuario: 'ATLETA',
    senha: 'password123'
  })
}).then(response => {
  console.log('Response status:', response.status);
  return response.json();
}).then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

## Profile Combinations

For different scenarios, combine profiles:

1. **Full debugging:** `dev,filter-debug,cors-debug`
2. **Security bypass + logging:** `debug,dev`
3. **CORS + security logging:** `cors-debug,dev`
4. **Filter analysis:** `filter-debug,dev`

## Next Steps After Debugging

Based on the results:

1. **If `debug` profile works:** Fix security configuration
2. **If CORS logs show issues:** Fix CORS settings
3. **If filter chain shows conflicts:** Reorder or remove conflicting filters
4. **If all debug profiles fail:** Check controller logic, validation, or data issues

Remember to **disable these debug configurations** before deploying to production!