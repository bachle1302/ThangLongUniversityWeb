import { apiRequest } from "./client";

export type ChatRoomType = "PRIVATE" | "GROUP" | "CLASS_GROUP";
export type MessageType = "TEXT" | "IMAGE" | "FILE";

export interface ChatUser {
  id: number;
  username: string;
  email: string;
  role: "ADMIN" | "TEACHER" | "STUDENT";
  code?: string | null;
  fullName: string;
  subtitle?: string | null;
  avatarUrl?: string | null;
}

export interface ChatMember {
  id: number;
  userId: number;
  username: string;
  code?: string | null;
  fullName: string;
  role: string;
  unreadCount: number;
  isOnline: boolean;
}

export interface ChatRoom {
  id: number;
  name?: string | null;
  description?: string | null;
  type: ChatRoomType;
  avatarUrl?: string | null;
  creatorId: number;
  creatorUsername: string;
  lastMessagePreview?: string | null;
  lastMessageSender?: string | null;
  lastMessageTime?: string | null;
  memberCount: number;
  unreadCount: number;
  members: ChatMember[];
  createdAt: string;
  isActive: boolean;
}

export interface ChatMessage {
  id: number;
  chatRoomId: number;
  senderId: number;
  senderUsername: string;
  senderCode?: string | null;
  senderFullName: string;
  senderAvatarUrl?: string | null;
  content?: string | null;
  type: MessageType;
  status: string;
  mediaUrl?: string | null;
  fileName?: string | null;
  fileSize?: number | null;
  createdAt?: string | null;
  createdAtEpochMs?: number | null;
}

interface Page<T> {
  content: T[];
  totalElements: number;
}

export function listRooms() {
  return apiRequest<Page<ChatRoom>>("/api/chat/rooms?size=100");
}

export function searchUsers(q: string) {
  return apiRequest<ChatUser[]>(`/api/chat/users/search?q=${encodeURIComponent(q)}`);
}

export function createPrivateRoom(otherUserId: number) {
  return apiRequest<ChatRoom>(`/api/chat/rooms/private?otherUserId=${otherUserId}`, {
    method: "POST",
  });
}

export function createGroupRoom(name: string, memberIds: number[]) {
  return apiRequest<ChatRoom>("/api/chat/rooms", {
    method: "POST",
    body: JSON.stringify({ name, type: "GROUP", memberIds }),
  });
}

export function leaveRoom(roomId: number) {
  return apiRequest<void>(`/api/chat/rooms/${roomId}/members/me`, { method: "DELETE" });
}

export function listMessages(roomId: number) {
  return apiRequest<Page<ChatMessage>>(`/api/chat/rooms/${roomId}/messages?size=100`);
}

export function sendMessage(roomId: number, content: string) {
  return apiRequest<ChatMessage>(`/api/chat/rooms/${roomId}/messages`, {
    method: "POST",
    body: JSON.stringify({ content, type: "TEXT" }),
  });
}

export function markRoomRead(roomId: number) {
  return apiRequest<void>(`/api/chat/rooms/${roomId}/read`, { method: "POST" });
}

export function listFiles(roomId: number) {
  return apiRequest<Page<ChatMessage>>(`/api/chat/rooms/${roomId}/files?size=100`);
}

export function listLinks(roomId: number) {
  return apiRequest<Page<ChatMessage>>(`/api/chat/rooms/${roomId}/links?size=100`);
}

export function uploadFile(roomId: number, file: File) {
  const form = new FormData();
  form.append("file", file);
  return apiRequest<ChatMessage>(`/api/chat/rooms/${roomId}/files`, { method: "POST", body: form });
}
