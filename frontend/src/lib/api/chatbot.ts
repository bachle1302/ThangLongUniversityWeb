import { apiRequest } from "./client";

export interface ChatbotMessageItem {
  id: number;
  role: "USER" | "ASSISTANT";
  content: string;
  createdAt: string;
}

export interface ChatbotResponse {
  answer: string;
  sessionId: string;
  timestamp: string;
}

export function sendChatbotMessage(message: string, sessionId?: string) {
  return apiRequest<ChatbotResponse>("/api/chatbot/send", {
    method: "POST",
    body: JSON.stringify({ message, sessionId }),
  });
}

export function getChatbotHistory(sessionId?: string) {
  const params = sessionId ? `?sessionId=${encodeURIComponent(sessionId)}` : "";
  return apiRequest<ChatbotMessageItem[]>(`/api/chatbot/history${params}`);
}

export function clearChatbotHistory(sessionId?: string) {
  const params = sessionId ? `?sessionId=${encodeURIComponent(sessionId)}` : "";
  return apiRequest<void>(`/api/chatbot/history${params}`, { method: "DELETE" });
}
