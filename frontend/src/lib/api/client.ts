import type { AuthResponse } from "./types";

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const STORAGE_KEY = "tlu-auth";
export const AUTH_STORAGE_EVENT = "tlu-auth-storage";

let refreshPromise: Promise<boolean> | null = null;

export interface StoredAuth {
  accessToken: string;
  refreshToken: string;
  role: AuthResponse["role"];
  name?: string | null;
}

export function getStoredAuth(): StoredAuth | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as StoredAuth;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function setStoredAuth(auth: StoredAuth | null) {
  if (typeof window === "undefined") return;
  if (!auth) {
    localStorage.removeItem(STORAGE_KEY);
    notifyAuthChanged();
    return;
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
  notifyAuthChanged();
}

function notifyAuthChanged() {
  window.dispatchEvent(new Event(AUTH_STORAGE_EVENT));
}

export class ApiError extends Error {
  status: number;
  retryAfter?: number;

  constructor(message: string, status: number, retryAfter?: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.retryAfter = retryAfter;
  }
}

async function parseResponse<T>(response: Response): Promise<T> {
  const text = await response.text();
  const body = text ? tryParseJson(text) : null;

  if (!response.ok) {
    const message =
      body && typeof body === "object" && "message" in body
        ? String((body as { message: unknown }).message)
        : response.statusText || "Request failed";

    let retryAfter: number | undefined = undefined;
    const retryAfterHeader = response.headers.get("Retry-After");
    if (retryAfterHeader) {
      const parsed = parseInt(retryAfterHeader, 10);
      if (!isNaN(parsed)) {
        retryAfter = parsed;
      }
    } else if (body && typeof body === "object" && "retryAfter" in body) {
      retryAfter = Number((body as { retryAfter: unknown }).retryAfter);
    }

    throw new ApiError(message, response.status, retryAfter);
  }

  return body as T;
}

function tryParseJson(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function shouldAttemptTokenRefresh(response: Response, path: string, auth: StoredAuth | null) {
  return (
    (response.status === 401 || response.status === 403) &&
    Boolean(auth?.refreshToken) &&
    !path.startsWith("/api/auth/")
  );
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  return apiRequestWithRetry<T>(path, init, true);
}

export function apiUrl(path: string) {
  return `${API_BASE_URL}${path}`;
}

export async function downloadApiFile(path: string, fallbackFilename: string) {
  return downloadApiFileWithRetry(path, fallbackFilename, true);
}

async function downloadApiFileWithRetry(
  path: string,
  fallbackFilename: string,
  allowRefresh: boolean,
) {
  const auth = getStoredAuth();
  const headers = new Headers();
  if (auth?.accessToken) headers.set("Authorization", `Bearer ${auth.accessToken}`);

  const response = await fetch(apiUrl(path), { headers });
  if (allowRefresh && shouldAttemptTokenRefresh(response, path, auth)) {
    const refreshed = await refreshAccessToken(auth);
    if (refreshed) {
      return downloadApiFileWithRetry(path, fallbackFilename, false);
    }
  }

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || response.statusText || "Khong tai duoc file");
  }

  const blob = await response.blob();
  const filename =
    getDownloadFilename(response.headers.get("Content-Disposition")) ?? fallbackFilename;
  triggerBrowserDownload(blob, filename);
}

export function triggerBrowserDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

function getDownloadFilename(contentDisposition: string | null) {
  if (!contentDisposition) return null;
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match?.[1]) return decodeURIComponent(utf8Match[1]);
  const asciiMatch = contentDisposition.match(/filename="?([^"]+)"?/i);
  return asciiMatch?.[1] ?? null;
}

async function apiRequestWithRetry<T>(
  path: string,
  init: RequestInit,
  allowRefresh: boolean,
): Promise<T> {
  const auth = getStoredAuth();
  const headers = new Headers(init.headers);
  if (!headers.has("Content-Type") && init.body && !(init.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }
  if (auth?.accessToken) headers.set("Authorization", `Bearer ${auth.accessToken}`);

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers,
    });
  } catch (error) {
    throw new Error(
      `Khong ket noi duoc backend tai ${API_BASE_URL}. Kiem tra Spring Boot dang chay, VITE_API_BASE_URL va CORS.`,
      { cause: error },
    );
  }

  if (allowRefresh && shouldAttemptTokenRefresh(response, path, auth)) {
    const refreshed = await refreshAccessToken(auth);
    if (refreshed) {
      return apiRequestWithRetry<T>(path, init, false);
    }
  }

  return parseResponse<T>(response);
}

async function refreshAccessToken(auth: StoredAuth): Promise<boolean> {
  if (refreshPromise) return refreshPromise;

  refreshPromise = requestAccessTokenRefresh(auth).finally(() => {
    refreshPromise = null;
  });

  return refreshPromise;
}

async function requestAccessTokenRefresh(auth: StoredAuth): Promise<boolean> {
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken: auth.refreshToken }),
    });
    if (!response.ok) {
      setStoredAuth(null);
      return false;
    }

    const next = (await response.json()) as AuthResponse;
    setStoredAuth({
      accessToken: next.accessToken,
      refreshToken: next.refreshToken,
      role: next.role,
      name: auth.name ?? null,
    });
    return true;
  } catch {
    setStoredAuth(null);
    return false;
  }
}

export function jsonBody(value: unknown) {
  return JSON.stringify(value);
}
