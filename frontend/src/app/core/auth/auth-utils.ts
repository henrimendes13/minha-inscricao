/**
 * Utilitários para trabalhar com JWT e autenticação
 * 
 * Este arquivo contém funções auxiliares para decodificar, validar e trabalhar
 * com tokens JWT. É especialmente útil para desenvolvedores que precisam
 * entender como verificar se um usuário está logado.
 */

export interface JwtPayload {
  sub?: string;          // Subject (user ID)
  email?: string;        // Email do usuário
  name?: string;         // Nome do usuário
  role?: string;         // Role/papel do usuário
  tipoUsuario?: string;  // Tipo do usuário (ADMIN, ORGANIZADOR, ATLETA)
  exp?: number;          // Data de expiração (Unix timestamp)
  iat?: number;          // Data de emissão (Unix timestamp)
  iss?: string;          // Issuer (quem emitiu o token)
}

/**
 * Decodifica um token JWT sem validar a assinatura
 * IMPORTANTE: Este método apenas decodifica o payload, não valida se o token é válido!
 * 
 * @param token Token JWT a ser decodificado
 * @returns Payload do JWT ou null se inválido
 * 
 * @example
 * ```typescript
 * const token = authService.getToken();
 * const payload = decodeJwtPayload(token);
 * 
 * if (payload) {
 * }
 * ```
 */
export function decodeJwtPayload(token: string | null): JwtPayload | null {
  if (!token) {
    console.debug('[AUTH-UTILS] Token não fornecido para decodificação');
    return null;
  }

  try {
    // JWT tem formato: header.payload.signature
    const parts = token.split('.');

    if (parts.length !== 3) {
      console.warn('[AUTH-UTILS] Token JWT inválido - formato incorreto');
      return null;
    }

    // Decodifica a parte do payload (segunda parte)
    const payload = parts[1];

    // Decodifica de base64url
    const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));

    const parsedPayload: any = JSON.parse(decodedPayload);

    // Mapear campos do backend para estrutura esperada
    // Backend usa: sub (email), tipoUsuario, userId, nome
    // Frontend espera: email, role, name
    const mappedPayload: JwtPayload = {
      sub: parsedPayload.sub,
      email: parsedPayload.sub || parsedPayload.email, // sub é o email no backend
      name: parsedPayload.nome || parsedPayload.name,
      role: parsedPayload.tipoUsuario || parsedPayload.role, // tipoUsuario é a role
      tipoUsuario: parsedPayload.tipoUsuario,
      exp: parsedPayload.exp,
      iat: parsedPayload.iat,
      iss: parsedPayload.iss
    };

    console.debug('[AUTH-UTILS] Token decodificado com sucesso:', {
      email: mappedPayload.email,
      role: mappedPayload.role,
      exp: mappedPayload.exp ? new Date(mappedPayload.exp * 1000) : 'N/A'
    });

    return mappedPayload;

  } catch (error) {
    console.error('[AUTH-UTILS] Erro ao decodificar token JWT:', error);
    return null;
  }
}

/**
 * Verifica se um token JWT está expirado
 * 
 * @param token Token JWT a ser verificado
 * @returns true se expirado, false se ainda válido, null se inválido
 * 
 * @example
 * ```typescript
 * const token = authService.getToken();
 * const isExpired = isTokenExpired(token);
 * 
 * if (isExpired === true) {
 * } else if (isExpired === false) {
 * } else {
 * }
 * ```
 */
export function isTokenExpired(token: string | null): boolean | null {
  const payload = decodeJwtPayload(token);
  
  if (!payload || !payload.exp) {
    console.debug('[AUTH-UTILS] Token inválido ou sem data de expiração');
    return null;
  }

  // exp está em segundos, Date.now() em milissegundos
  const expirationTime = payload.exp * 1000;
  const currentTime = Date.now();
  
  const isExpired = currentTime >= expirationTime;
  
  if (isExpired) {
    const expiredSince = new Date(currentTime - expirationTime);
    console.debug('[AUTH-UTILS] Token expirado há:', expiredSince.getMinutes(), 'minutos');
  } else {
    const expiresIn = new Date(expirationTime - currentTime);
    console.debug('[AUTH-UTILS] Token expira em:', expiresIn.getMinutes(), 'minutos');
  }
  
  return isExpired;
}

/**
 * Obtém o tempo restante até a expiração do token em minutos
 * 
 * @param token Token JWT
 * @returns Minutos até expiração, 0 se expirado, null se inválido
 * 
 * @example
 * ```typescript
 * const token = authService.getToken();
 * const minutesLeft = getTokenExpirationMinutes(token);
 * 
 * if (minutesLeft === null) {
 * } else if (minutesLeft <= 0) {
 * } else if (minutesLeft < 5) {
 * }
 * ```
 */
export function getTokenExpirationMinutes(token: string | null): number | null {
  const payload = decodeJwtPayload(token);
  
  if (!payload || !payload.exp) {
    return null;
  }

  const expirationTime = payload.exp * 1000;
  const currentTime = Date.now();
  const timeLeft = expirationTime - currentTime;
  
  return Math.max(0, Math.floor(timeLeft / (1000 * 60)));
}

/**
 * Verifica se o usuário tem uma role específica baseado no token
 * 
 * @param token Token JWT
 * @param requiredRole Role necessária
 * @returns true se tem a role, false caso contrário
 * 
 * @example
 * ```typescript
 * const token = authService.getToken();
 * 
 * if (hasRole(token, 'ADMIN')) {
 * }
 * 
 * if (hasRole(token, 'ORGANIZADOR')) {
 * }
 * ```
 */
export function hasRole(token: string | null, requiredRole: string): boolean {
  const payload = decodeJwtPayload(token);
  
  if (!payload) {
    return false;
  }

  // Verifica tanto 'role' quanto 'tipoUsuario'
  const userRole = payload.role || payload.tipoUsuario;
  
  if (!userRole) {
    console.debug('[AUTH-UTILS] Token não contém informação de role');
    return false;
  }

  // Remove prefixo 'ROLE_' se existir para comparação
  const normalizedUserRole = userRole.replace(/^ROLE_/, '');
  const normalizedRequiredRole = requiredRole.replace(/^ROLE_/, '');
  
  const hasRequiredRole = normalizedUserRole === normalizedRequiredRole;
  
  console.debug('[AUTH-UTILS] Verificação de role:', {
    userRole: normalizedUserRole,
    requiredRole: normalizedRequiredRole,
    hasRole: hasRequiredRole
  });
  
  return hasRequiredRole;
}

/**
 * Obtém informações básicas do usuário a partir do token
 * 
 * @param token Token JWT
 * @returns Objeto com informações do usuário ou null
 * 
 * @example
 * ```typescript
 * const token = authService.getToken();
 * const userInfo = getUserInfoFromToken(token);
 * 
 * if (userInfo) {
 * }
 * ```
 */
export function getUserInfoFromToken(token: string | null): {
  id?: string;
  name?: string;
  email?: string;
  role?: string;
  tipoUsuario?: string;
} | null {
  const payload = decodeJwtPayload(token);
  
  if (!payload) {
    return null;
  }

  return {
    id: payload.sub,
    name: payload.name,
    email: payload.email,
    role: payload.role,
    tipoUsuario: payload.tipoUsuario
  };
}

/**
 * Função utilitária para debug - mostra todas as informações do token
 * Útil durante desenvolvimento para entender o que está no token
 * 
 * @param token Token JWT
 * @returns Objeto com todas as informações para debug
 * 
 * @example
 * ```typescript
 * // Durante desenvolvimento, para entender o token:
 * const token = authService.getToken();
 * ```
 */
export function debugToken(token: string | null): {
  isValid: boolean;
  isExpired: boolean | null;
  payload: JwtPayload | null;
  userInfo: any;
  expirationMinutes: number | null;
  rawToken?: string;
} {
  const payload = decodeJwtPayload(token);
  const isExpired = isTokenExpired(token);
  const userInfo = getUserInfoFromToken(token);
  const expirationMinutes = getTokenExpirationMinutes(token);
  
  const debug = {
    isValid: payload !== null,
    isExpired,
    payload,
    userInfo,
    expirationMinutes,
    ...(token && { rawToken: token })
  };
  
  console.group('[AUTH-UTILS] 🐛 Token Debug Info');
  console.groupEnd();
  
  return debug;
}

/**
 * Verifica se o token está próximo do vencimento (menos de 5 minutos)
 * Útil para implementar renovação automática de token
 * 
 * @param token Token JWT
 * @returns true se está próximo do vencimento
 */
export function isTokenNearExpiration(token: string | null): boolean {
  const minutesLeft = getTokenExpirationMinutes(token);
  return minutesLeft !== null && minutesLeft > 0 && minutesLeft < 5;
}
