import { apiRequest } from "./client";
import type { Role } from "./types";

export type NotificationType = "SCHOOL" | "CHAT";

export interface StudentNotification {
  id: string;
  type: NotificationType;
  title: string;
  body?: string | null;
  link?: string | null;
  read: boolean;
  createdAt: string;
}

export type AppNotification = StudentNotification;
export type NotificationRole = Extract<Role, "STUDENT" | "TEACHER">;

function notificationBasePath(role: NotificationRole) {
  return role === "TEACHER" ? "/api/teacher/notifications" : "/api/student/notifications";
}

export function listNotifications(role: NotificationRole = "STUDENT") {
  return apiRequest<AppNotification[]>(notificationBasePath(role));
}

export function listStudentNotifications() {
  return apiRequest<StudentNotification[]>("/api/student/notifications");
}

export function markNotificationRead(id: string, role: NotificationRole = "STUDENT") {
  return apiRequest<void>(`${notificationBasePath(role)}/${encodeURIComponent(id)}/read`, {
    method: "POST",
  });
}

export function markStudentNotificationRead(id: string) {
  return apiRequest<void>(`/api/student/notifications/${encodeURIComponent(id)}/read`, {
    method: "POST",
  });
}

export function markAllNotificationsRead(role: NotificationRole = "STUDENT") {
  return apiRequest<void>(`${notificationBasePath(role)}/read-all`, { method: "POST" });
}

export function markAllStudentNotificationsRead() {
  return apiRequest<void>("/api/student/notifications/read-all", { method: "POST" });
}
