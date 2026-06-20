import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { PageHeader } from "@/components/ui/page-header";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import {
  deleteKnowledgeDocument,
  getDocumentChunks,
  ingestText,
  ingestUrl,
  listKnowledgeDocuments,
  reindexAll,
  reindexDocument,
  type KnowledgeDocumentItem,
} from "@/lib/api/knowledge";
import { Brain, Eye, Globe, Plus, RefreshCw, Trash2 } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/knowledge")({ component: KnowledgePage });

const SOURCE_TYPES = [
  { value: "ANNOUNCEMENT", label: "Thông báo / Quyết định" },
  { value: "WEBSITE", label: "Trang web chính thức" },
  { value: "HANDBOOK", label: "Sổ tay sinh viên" },
  { value: "FAQ", label: "Câu hỏi thường gặp" },
  { value: "WIKIPEDIA", label: "Wikipedia (bối cảnh nền)" },
];

const PRIORITY_LABELS: Record<number, string> = {
  1: "1 – Cao nhất (thông báo mới)",
  2: "2 – Trang phòng ban",
  3: "3 – Trang ngành",
  4: "4 – Sổ tay sinh viên",
  5: "5 – Thấp nhất (Wikipedia)",
};

function PriorityBadge({ priority }: { priority: number }) {
  const colors: Record<number, string> = {
    1: "bg-destructive/15 text-destructive border-destructive/30",
    2: "bg-warning/20 text-warning-foreground border-warning/40",
    3: "bg-info/15 text-info border-info/30",
    4: "bg-muted text-muted-foreground border-border",
    5: "bg-muted text-muted-foreground border-border",
  };
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] font-medium ${colors[priority] ?? ""}`}
    >
      P{priority}
    </span>
  );
}

function KnowledgePage() {
  const queryClient = useQueryClient();
  const [addMode, setAddMode] = useState<"text" | "url" | null>(null);
  const [toDelete, setToDelete] = useState<KnowledgeDocumentItem | null>(null);
  const [viewDoc, setViewDoc] = useState<KnowledgeDocumentItem | null>(null);

  // Text ingest form
  const [textForm, setTextForm] = useState({
    title: "",
    content: "",
    sourceUrl: "",
    sourceType: "WEBSITE",
    priority: 3,
  });

  // URL ingest form
  const [urlForm, setUrlForm] = useState({
    url: "",
    title: "",
    sourceType: "WEBSITE",
    priority: 3,
  });

  const listQuery = useQuery({
    queryKey: ["admin", "knowledge", "documents"],
    queryFn: listKnowledgeDocuments,
  });

  const chunksQuery = useQuery({
    queryKey: ["admin", "knowledge", "chunks", viewDoc?.id],
    queryFn: () => getDocumentChunks(viewDoc!.id),
    enabled: viewDoc != null,
  });

  const ingestTextMut = useMutation({
    mutationFn: ingestText,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "knowledge"] });
      toast.success("Đã thêm tài liệu vào cơ sở tri thức");
      setAddMode(null);
      setTextForm({ title: "", content: "", sourceUrl: "", sourceType: "WEBSITE", priority: 3 });
    },
    onError: () => toast.error("Không thể thêm tài liệu"),
  });

  const ingestUrlMut = useMutation({
    mutationFn: ingestUrl,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "knowledge"] });
      toast.success("Đã crawl và thêm tài liệu");
      setAddMode(null);
      setUrlForm({ url: "", title: "", sourceType: "WEBSITE", priority: 3 });
    },
    onError: (e: Error) => toast.error(e.message || "Không thể crawl URL"),
  });

  const deleteMut = useMutation({
    mutationFn: (id: number) => deleteKnowledgeDocument(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "knowledge"] });
      toast.success("Đã xóa tài liệu");
      setToDelete(null);
    },
    onError: () => toast.error("Không thể xóa tài liệu"),
  });

  const reindexDocMut = useMutation({
    mutationFn: (id: number) => reindexDocument(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "knowledge"] });
      toast.success("Đã re-index tài liệu");
    },
    onError: () => toast.error("Re-index thất bại"),
  });

  const reindexAllMut = useMutation({
    mutationFn: reindexAll,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "knowledge"] });
      toast.success("Đã re-index toàn bộ tài liệu");
    },
    onError: () => toast.error("Re-index all thất bại"),
  });

  const docs = listQuery.data ?? [];

  return (
    <div className="flex flex-col gap-6 p-6">
      <PageHeader
        title="Quản lý Knowledge Base"
        description="Thêm, xóa và re-index tài liệu cho AI Chatbot (RAG)"
        actions={
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => reindexAllMut.mutate()}
              disabled={reindexAllMut.isPending}
            >
              <RefreshCw className="mr-1.5 h-4 w-4" />
              Re-index All
            </Button>
            <Button size="sm" variant="outline" onClick={() => setAddMode("url")}>
              <Globe className="mr-1.5 h-4 w-4" />
              Crawl URL
            </Button>
            <Button size="sm" onClick={() => setAddMode("text")}>
              <Plus className="mr-1.5 h-4 w-4" />
              Thêm văn bản
            </Button>
          </div>
        }
      />

      {/* Documents table */}
      <div className="rounded-lg border bg-card">
        <div className="p-4 border-b">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Brain className="h-4 w-4" />
            <span>{docs.length} tài liệu đang hoạt động</span>
          </div>
        </div>
        {listQuery.isLoading ? (
          <div className="p-8 text-center text-muted-foreground">Đang tải...</div>
        ) : docs.length === 0 ? (
          <div className="p-8 text-center text-muted-foreground">
            Chưa có tài liệu nào. Thêm mới để chatbot có dữ liệu tham chiếu.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-muted/50">
                  <th className="px-4 py-3 text-left font-medium">Tiêu đề</th>
                  <th className="px-4 py-3 text-left font-medium">Loại nguồn</th>
                  <th className="px-4 py-3 text-left font-medium">Ưu tiên</th>
                  <th className="px-4 py-3 text-right font-medium">Chunks</th>
                  <th className="px-4 py-3 text-left font-medium">Cập nhật</th>
                  <th className="px-4 py-3 text-right font-medium">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {docs.map((doc) => (
                  <tr
                    key={doc.id}
                    className="border-b last:border-0 hover:bg-muted/30 transition-colors"
                  >
                    <td className="px-4 py-3 max-w-xs">
                      <div className="font-medium truncate">{doc.title}</div>
                      {doc.sourceUrl && (
                        <a
                          href={doc.sourceUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-xs text-muted-foreground hover:underline truncate block"
                        >
                          {doc.sourceUrl}
                        </a>
                      )}
                    </td>
                    <td className="px-4 py-3">
                      <span className="text-xs bg-secondary px-2 py-0.5 rounded">
                        {doc.sourceType}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <PriorityBadge priority={doc.priority} />
                    </td>
                    <td className="px-4 py-3 text-right tabular-nums">
                      {doc.chunkCount}
                      {doc.searchableChunkCount != null &&
                        doc.searchableChunkCount < doc.chunkCount && (
                        <span className="block text-[10px] text-warning-foreground">
                          {doc.searchableChunkCount} indexed
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-muted-foreground text-xs">
                      {new Date(doc.fetchedAt).toLocaleDateString("vi-VN")}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-1">
                        <Button
                          size="icon"
                          variant="ghost"
                          className="h-7 w-7"
                          onClick={() => setViewDoc(doc)}
                          title="Xem nội dung"
                        >
                          <Eye className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          size="icon"
                          variant="ghost"
                          className="h-7 w-7"
                          onClick={() => reindexDocMut.mutate(doc.id)}
                          disabled={reindexDocMut.isPending}
                          title="Re-index"
                        >
                          <RefreshCw className="h-3.5 w-3.5" />
                        </Button>
                        <Button
                          size="icon"
                          variant="ghost"
                          className="h-7 w-7 text-destructive hover:text-destructive"
                          onClick={() => setToDelete(doc)}
                          title="Xóa"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add Text Dialog */}
      <Dialog open={addMode === "text"} onOpenChange={(o) => !o && setAddMode(null)}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Thêm tài liệu văn bản</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-2">
            <div className="grid gap-1.5">
              <Label htmlFor="text-title">Tiêu đề *</Label>
              <Input
                id="text-title"
                value={textForm.title}
                onChange={(e) => setTextForm((f) => ({ ...f, title: e.target.value }))}
                placeholder="Ví dụ: Thông tin tuyển sinh 2026"
              />
            </div>
            <div className="grid gap-1.5">
              <Label htmlFor="text-content">Nội dung *</Label>
              <Textarea
                id="text-content"
                rows={10}
                value={textForm.content}
                onChange={(e) => setTextForm((f) => ({ ...f, content: e.target.value }))}
                placeholder="Dán nội dung văn bản tại đây..."
                className="font-mono text-xs"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="grid gap-1.5">
                <Label htmlFor="text-type">Loại nguồn *</Label>
                <Select
                  value={textForm.sourceType}
                  onValueChange={(v) => setTextForm((f) => ({ ...f, sourceType: v }))}
                >
                  <SelectTrigger id="text-type">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {SOURCE_TYPES.map((t) => (
                      <SelectItem key={t.value} value={t.value}>
                        {t.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="grid gap-1.5">
                <Label htmlFor="text-priority">Mức ưu tiên *</Label>
                <Select
                  value={String(textForm.priority)}
                  onValueChange={(v) => setTextForm((f) => ({ ...f, priority: Number(v) }))}
                >
                  <SelectTrigger id="text-priority">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {[1, 2, 3, 4, 5].map((p) => (
                      <SelectItem key={p} value={String(p)}>
                        {PRIORITY_LABELS[p]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div className="grid gap-1.5">
              <Label htmlFor="text-url">URL nguồn (tùy chọn)</Label>
              <Input
                id="text-url"
                value={textForm.sourceUrl}
                onChange={(e) => setTextForm((f) => ({ ...f, sourceUrl: e.target.value }))}
                placeholder="https://thanglong.edu.vn/..."
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setAddMode(null)}>
              Hủy
            </Button>
            <Button
              onClick={() => ingestTextMut.mutate(textForm)}
              disabled={
                ingestTextMut.isPending || !textForm.title.trim() || !textForm.content.trim()
              }
            >
              {ingestTextMut.isPending ? "Đang xử lý..." : "Thêm và nhúng"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Add URL Dialog */}
      <Dialog open={addMode === "url"} onOpenChange={(o) => !o && setAddMode(null)}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle>Crawl từ URL</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-2">
            <div className="grid gap-1.5">
              <Label htmlFor="url-url">URL *</Label>
              <Input
                id="url-url"
                value={urlForm.url}
                onChange={(e) => setUrlForm((f) => ({ ...f, url: e.target.value }))}
                placeholder="https://thanglong.edu.vn/..."
              />
            </div>
            <div className="grid gap-1.5">
              <Label htmlFor="url-title">Tiêu đề *</Label>
              <Input
                id="url-title"
                value={urlForm.title}
                onChange={(e) => setUrlForm((f) => ({ ...f, title: e.target.value }))}
                placeholder="Tên tài liệu hiển thị trong admin"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="grid gap-1.5">
                <Label htmlFor="url-type">Loại nguồn *</Label>
                <Select
                  value={urlForm.sourceType}
                  onValueChange={(v) => setUrlForm((f) => ({ ...f, sourceType: v }))}
                >
                  <SelectTrigger id="url-type">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {SOURCE_TYPES.map((t) => (
                      <SelectItem key={t.value} value={t.value}>
                        {t.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="grid gap-1.5">
                <Label htmlFor="url-priority">Mức ưu tiên *</Label>
                <Select
                  value={String(urlForm.priority)}
                  onValueChange={(v) => setUrlForm((f) => ({ ...f, priority: Number(v) }))}
                >
                  <SelectTrigger id="url-priority">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {[1, 2, 3, 4, 5].map((p) => (
                      <SelectItem key={p} value={String(p)}>
                        {PRIORITY_LABELS[p]}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setAddMode(null)}>
              Hủy
            </Button>
            <Button
              onClick={() => ingestUrlMut.mutate(urlForm)}
              disabled={ingestUrlMut.isPending || !urlForm.url.trim() || !urlForm.title.trim()}
            >
              {ingestUrlMut.isPending ? "Đang crawl..." : "Crawl & Thêm"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* View chunks dialog */}
      <Dialog open={viewDoc != null} onOpenChange={(o) => !o && setViewDoc(null)}>
        <DialogContent className="max-w-3xl max-h-[85vh] flex flex-col">
          <DialogHeader>
            <DialogTitle>{viewDoc?.title}</DialogTitle>
            {viewDoc && (
              <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground pt-1">
                <span className="bg-secondary px-2 py-0.5 rounded">{viewDoc.sourceType}</span>
                <PriorityBadge priority={viewDoc.priority} />
                <span>{viewDoc.chunkCount} chunk</span>
                {viewDoc.sourceUrl && (
                  <a
                    href={viewDoc.sourceUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:underline truncate max-w-md"
                  >
                    {viewDoc.sourceUrl}
                  </a>
                )}
              </div>
            )}
          </DialogHeader>
          <div className="flex-1 overflow-y-auto min-h-0 space-y-3 py-2">
            {chunksQuery.isLoading ? (
              <div className="text-center text-muted-foreground py-8">Đang tải nội dung...</div>
            ) : chunksQuery.isError ? (
              <div className="text-center text-destructive py-8">Không thể tải nội dung chunk</div>
            ) : (chunksQuery.data ?? []).length === 0 ? (
              <div className="text-center text-muted-foreground py-8">Chưa có chunk nào</div>
            ) : (
              (chunksQuery.data ?? []).map((chunk) => (
                <div key={chunk.id} className="rounded-md border bg-muted/30 p-3">
                  <div className="text-[11px] font-medium text-muted-foreground mb-2">
                    Chunk #{chunk.chunkIndex}
                  </div>
                  <pre className="whitespace-pre-wrap break-words text-xs font-mono leading-relaxed">
                    {chunk.content}
                  </pre>
                </div>
              ))
            )}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setViewDoc(null)}>
              Đóng
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete confirm */}
      {toDelete && (
        <ConfirmDialog
          open={!!toDelete}
          title="Xóa tài liệu"
          description={`Xóa "${toDelete.title}" và tất cả ${toDelete.chunkCount} chunk của nó?`}
          onConfirm={() => deleteMut.mutate(toDelete.id)}
          onCancel={() => setToDelete(null)}
          loading={deleteMut.isPending}
        />
      )}
    </div>
  );
}
