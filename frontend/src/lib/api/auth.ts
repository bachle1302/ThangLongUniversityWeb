import { apiRequest, getStoredAuth, jsonBody } from "./client";
import type { AuthResponse, UserProfile } from "./types";

export function login(username: string, password: string) {
  return apiRequest<AuthResponse>("/api/auth/login", {
    method: "POST",
    body: jsonBody({ username, password }),
  });
}

export function getMe() {
  return apiRequest<UserProfile>("/api/users/me");
}

export function logout() {
  const auth = getStoredAuth();
  return apiRequest<string>("/api/auth/logout", {
    method: "POST",
    body: jsonBody({ refreshToken: auth?.refreshToken }),
  });
}

export function changePassword(currentPassword: string, newPassword: string, confirmPassword: string) {
  return apiRequest<{ message: string }>("/api/auth/change-password", {
    method: "PUT",
    body: jsonBody({ currentPassword, newPassword, confirmPassword }),
  });
}
