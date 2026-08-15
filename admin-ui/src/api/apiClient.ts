// Thin fetch wrapper mirroring web/app/HttpService.ts's auth model (Basic auth
// header built from stored credentials) but adapted to fetch()/sessionStorage
// and the fact that WebSecurityConfig's httpBasic() filter is what actually
// rejects bad credentials with 401 — permitAll() means routes are otherwise open.

const TOKEN_KEY = 'admin-ui-token';

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}

function getToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY);
}

function setToken(token: string | null): void {
  if (token) {
    sessionStorage.setItem(TOKEN_KEY, token);
  } else {
    sessionStorage.removeItem(TOKEN_KEY);
  }
}

export function isAuthenticated(): boolean {
  return getToken() !== null;
}

function buildHeaders(extra?: HeadersInit): Headers {
  const headers = new Headers(extra);
  const token = getToken();
  if (token) {
    headers.set('Authorization', `Basic ${token}`);
  }
  return headers;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (response.status === 401 || response.status === 403) {
    setToken(null);
    window.location.assign('/login');
    throw new ApiError(response.status, 'Not authenticated');
  }
  if (!response.ok) {
    throw new ApiError(response.status, await response.text());
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  return (text.length > 0 ? JSON.parse(text) : undefined) as T;
}

export function apiGet<T>(uri: string): Promise<T> {
  return fetch(uri, { headers: buildHeaders() }).then(handleResponse<T>);
}

export function apiPost<T>(uri: string, body?: unknown): Promise<T> {
  return fetch(uri, {
    method: 'POST',
    headers: buildHeaders({ 'Content-Type': 'application/json' }),
    body: body !== undefined ? JSON.stringify(body) : undefined,
  }).then(handleResponse<T>);
}

export function apiPut<T>(uri: string, body?: unknown): Promise<T> {
  return fetch(uri, {
    method: 'PUT',
    headers: buildHeaders({ 'Content-Type': 'application/json' }),
    body: body !== undefined ? JSON.stringify(body) : undefined,
  }).then(handleResponse<T>);
}

export function apiDelete<T>(uri: string): Promise<T> {
  return fetch(uri, { method: 'DELETE', headers: buildHeaders() }).then(handleResponse<T>);
}

// Validates credentials against the backend (GET /user/current is unsecured
// but still runs through the httpBasic filter, so a bad password still 401s)
// and stores the resulting token on success.
export async function login(username: string, password: string): Promise<void> {
  const token = btoa(`${username}:${password}`);
  const response = await fetch('/user/current', {
    headers: { Authorization: `Basic ${token}` },
  });
  if (!response.ok) {
    throw new ApiError(response.status, 'Invalid credentials');
  }
  setToken(token);
}

export function logout(): void {
  setToken(null);
}
