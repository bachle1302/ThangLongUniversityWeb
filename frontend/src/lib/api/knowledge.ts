import { apiRequest } from "./client";

export interface KnowledgeDocumentItem {
  id: number;
  title: string;
  sourceUrl: string | null;
  sourceType: string;
  priority: number;
  isActive: boolean;
  fetchedAt: string;
  chunkCount: number;
  searchableChunkCount: number;
}

export interface KnowledgeChunkItem {
  id: number;
  chunkIndex: number;
  content: string;
  createdAt: string;
}

export interface IngestTextPayload {
  title: string;
  content: string;
  sourceUrl?: string;
  sourceType: string;
  priority: number;
}

export interface IngestUrlPayload {
  url: string;
  title: string;
  sourceType: string;
  priority: number;
}

export function listKnowledgeDocuments() {
  return apiRequest<KnowledgeDocumentItem[]>("/api/admin/knowledge/documents");
}

export function getDocumentChunks(id: number) {
  return apiRequest<KnowledgeChunkItem[]>(`/api/admin/knowledge/documents/${id}/chunks`);
}

export function ingestText(payload: IngestTextPayload) {
  return apiRequest<{ documentId: number }>("/api/admin/knowledge/ingest", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function ingestUrl(payload: IngestUrlPayload) {
  return apiRequest<{ documentId: number }>("/api/admin/knowledge/ingest-url", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function deleteKnowledgeDocument(id: number) {
  return apiRequest<void>(`/api/admin/knowledge/documents/${id}`, {
    method: "DELETE",
  });
}

export function reindexDocument(id: number) {
  return apiRequest<void>(`/api/admin/knowledge/documents/${id}/reindex`, {
    method: "POST",
  });
}

export function reindexAll() {
  return apiRequest<void>("/api/admin/knowledge/reindex-all", {
    method: "POST",
  });
}
