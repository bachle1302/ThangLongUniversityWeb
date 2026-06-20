import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { cn } from "@/lib/utils";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Skeleton } from "@/components/ui/skeleton";
import { CalendarDays, ChevronRight, CircleOff, Lock, Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { adminApi } from "@/lib/api/admin";
import type { SemesterRequest, StudentSemesterResponse } from "@/lib/api/types";

export const Route = createFileRoute("/admin/semesters/")({ component: SemestersPage });

function formatDate(value?: string | null) {
  if (!value) return "—";
  return new Intl.DateTimeFormat("vi-VN").format(new Date(value));
}

type TimeStatus = "active" | "upcoming" | "past";

function getTimeStatus(s: StudentSemesterResponse): TimeStatus {
  const now = new Date();
  const start = s.startDate ? new Date(s.startDate) : null;
  const end = s.endDate ? new Date(s.endDate) : null;
  if (start && end && now >= start && now <= end) return "active";
  if (start && now < start) return "upcoming";
  return "past";
}

function getProgress(startDate?: string | null, endDate?: string | null): number {
  if (!startDate || !endDate) return 0;
  const now = Date.now();
  const start = new Date(startDate).getTime();
  const end = new Date(endDate).getTime();
  if (now <= start) return 0;
  if (now >= end) return 100;
  return Math.round(((now - start) / (end - start)) * 100);
}

function TimeStatusChip({ status }: { status: TimeStatus }) {
  if (status === "active")
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-700 ring-1 ring-emerald-200">
        <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
        Đang diễn ra
      </span>
    );
  if (status === "upcoming")
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700 ring-1 ring-blue-200">
        <span className="h-1.5 w-1.5 rounded-full bg-blue-500" />
        Sắp tới
      </span>
    );
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-500">
      Đã kết thúc
    </span>
  );
}

function RegistrationBadge({ s }: { s: StudentSemesterResponse }) {
  if (s.locked)
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">
        <Lock className="h-3 w-3" /> Đã chốt
      </span>
    );
  if (s.registrationOpen)
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700 ring-1 ring-emerald-200">
        <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" /> Đang mở
      </span>
    );
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-gray-50 px-3 py-1 text-xs font-medium text-gray-500 ring-1 ring-gray-200">
      <CircleOff className="h-3 w-3" /> Đóng
    </span>
  );
}

function ExamBadge({ s }: { s: StudentSemesterResponse }) {
  if (s.examPublished)
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700 ring-1 ring-blue-200">
        <span className="h-1.5 w-1.5 rounded-full bg-blue-500" /> Đã công bố
      </span>
    );
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-gray-50 px-3 py-1 text-xs font-medium text-gray-400">
      Chưa có
    </span>
  );
}

function RetakeBadge({ s }: { s: StudentSemesterResponse }) {
  if (s.retakeLocked)
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-700">
        <Lock className="h-3 w-3" /> Đã chốt
      </span>
    );
  if (s.retakeOpen)
    return (
      <span className="inline-flex items-center gap-1.5 rounded-full bg-amber-50 px-3 py-1 text-xs font-medium text-amber-700 ring-1 ring-amber-200">
        <span className="h-1.5 w-1.5 rounded-full bg-amber-500 animate-pulse" /> Đang mở
      </span>
    );
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full bg-gray-50 px-3 py-1 text-xs font-medium text-gray-400">
      Chưa mở
    </span>
  );
}

type FormState = {
  name: string;
  startDate: string;
  endDate: string;
};

function emptyForm(): FormState {
  return { name: "", startDate: "", endDate: "" };
}

function semesterToForm(s: StudentSemesterResponse): FormState {
  return { name: s.name, startDate: s.startDate ?? "", endDate: s.endDate ?? "" };
}

function SemestersPage() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const semestersQuery = useQuery({
    queryKey: ["admin", "semesters"],
    queryFn: adminApi.listSemesters,
  });
  const semesters = semestersQuery.data ?? [];

  const [formOpen, setFormOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<StudentSemesterResponse | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm());
  const [deleteTarget, setDeleteTarget] = useState<StudentSemesterResponse | null>(null);

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["admin", "semesters"] });
    queryClient.invalidateQueries({ queryKey: ["student", "semesters"] });
  };

  const createMutation = useMutation({
    mutationFn: (req: SemesterRequest) => adminApi.createSemester(req),
    onSuccess: () => {
      invalidate();
      toast.success("Đã tạo học kỳ mới");
      setFormOpen(false);
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : "Lỗi khi tạo học kỳ"),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, req }: { id: number; req: SemesterRequest }) =>
      adminApi.updateSemester(id, req),
    onSuccess: () => {
      invalidate();
      toast.success("Đã cập nhật học kỳ");
      setFormOpen(false);
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : "Lỗi khi cập nhật học kỳ"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteSemester(id),
    onSuccess: () => {
      invalidate();
      toast.success("Đã xóa học kỳ");
      setDeleteTarget(null);
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : "Lỗi khi xóa học kỳ"),
  });

  function openCreate() {
    setEditTarget(null);
    setForm(emptyForm());
    setFormOpen(true);
  }

  function openEdit(s: StudentSemesterResponse, e: React.MouseEvent) {
    e.stopPropagation();
    setEditTarget(s);
    setForm(semesterToForm(s));
    setFormOpen(true);
  }

  function handleFormSubmit() {
    if (!form.name.trim()) {
      toast.error("Tên học kỳ không được để trống");
      return;
    }
    const req: SemesterRequest = {
      name: form.name,
      startDate: form.startDate || null,
      endDate: form.endDate || null,
      registrationOpen: editTarget?.registrationOpen ?? false,
    };
    if (editTarget) {
      updateMutation.mutate({ id: editTarget.id, req });
    } else {
      createMutation.mutate(req);
    }
  }

  const isMutating = createMutation.isPending || updateMutation.isPending;

  if (semestersQuery.isLoading) {
    return (
      <div className="p-6 space-y-4">
        <Skeleton className="h-10 w-64" />
        {[1, 2, 3].map((i) => (
          <Skeleton key={i} className="h-40 w-full rounded-xl" />
        ))}
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold flex items-center gap-2">
            <CalendarDays className="h-6 w-6" />
            Quản lý Học kỳ
          </h1>
          <p className="text-muted-foreground text-sm mt-1">
            {semesters.length} học kỳ trong hệ thống
          </p>
        </div>
        <Button onClick={openCreate}>
          <Plus className="mr-1 h-4 w-4" />
          Thêm học kỳ
        </Button>
      </div>

      {semestersQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          {semestersQuery.error instanceof Error
            ? semestersQuery.error.message
            : "Không tải được danh sách học kỳ"}
        </div>
      )}

      {/* Semester cards */}
      <div className="space-y-4">
        {semesters.map((s) => {
          const timeStatus = getTimeStatus(s);
          const progress = getProgress(s.startDate, s.endDate);
          return (
            <div
              key={s.id}
              className={cn(
                "rounded-xl border bg-card p-5 shadow-sm hover:shadow-md transition-all cursor-pointer",
                timeStatus === "active" && "border-l-4 border-l-emerald-500",
                timeStatus === "upcoming" && "border-l-4 border-l-blue-500",
                timeStatus === "past" && "opacity-75",
              )}
              onClick={() => navigate({ to: "/admin/semesters/$id", params: { id: String(s.id) } })}
            >
              <div className="flex items-start justify-between gap-4">
                {/* Left */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-3 flex-wrap">
                    <h2 className="text-lg font-semibold">{s.name}</h2>
                    <TimeStatusChip status={timeStatus} />
                  </div>
                  <p className="text-sm text-muted-foreground mt-1">
                    {formatDate(s.startDate)} → {formatDate(s.endDate)}
                  </p>

                  {/* Progress bar for active semesters */}
                  {timeStatus === "active" && (
                    <div className="mt-2 space-y-1">
                      <div className="h-1.5 w-full max-w-xs rounded-full bg-muted overflow-hidden">
                        <div
                          className="h-full rounded-full bg-gradient-to-r from-emerald-500 to-emerald-400 transition-all"
                          style={{ width: `${progress}%` }}
                        />
                      </div>
                      <p className="text-xs text-muted-foreground">{progress}% thời gian đã qua</p>
                    </div>
                  )}

                  {/* Status badges */}
                  <div className="mt-3 flex flex-wrap gap-4">
                    <div className="flex flex-col gap-1">
                      <span className="text-xs text-muted-foreground font-medium">
                        Đăng ký học phần
                      </span>
                      <RegistrationBadge s={s} />
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="text-xs text-muted-foreground font-medium">Lịch thi</span>
                      <ExamBadge s={s} />
                    </div>
                    <div className="flex flex-col gap-1">
                      <span className="text-xs text-muted-foreground font-medium">Thi lại</span>
                      <RetakeBadge s={s} />
                    </div>
                  </div>
                </div>

                {/* Right: actions */}
                <div className="flex items-center gap-2 shrink-0">
                  <Button variant="ghost" size="sm" onClick={(e) => openEdit(s, e)}>
                    <Pencil className="h-3.5 w-3.5 mr-1" />
                    Sửa
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-destructive hover:text-destructive hover:bg-destructive/10"
                    disabled={s.locked}
                    onClick={(e) => {
                      e.stopPropagation();
                      setDeleteTarget(s);
                    }}
                  >
                    <Trash2 className="h-3.5 w-3.5 mr-1" />
                    Xóa
                  </Button>
                  <Button
                    size="sm"
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate({ to: "/admin/semesters/$id", params: { id: String(s.id) } });
                    }}
                  >
                    Quản lý
                    <ChevronRight className="h-3.5 w-3.5 ml-1" />
                  </Button>
                </div>
              </div>
            </div>
          );
        })}

        {semesters.length === 0 && !semestersQuery.isLoading && (
          <div className="text-center py-16 text-muted-foreground">
            <CalendarDays className="h-12 w-12 mx-auto mb-4 opacity-30" />
            <p>Chưa có học kỳ nào. Hãy tạo học kỳ đầu tiên.</p>
          </div>
        )}
      </div>

      {/* Create/Edit Dialog */}
      <Dialog open={formOpen} onOpenChange={setFormOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editTarget ? "Sửa học kỳ" : "Thêm học kỳ"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-1">
              <Label>
                Tên học kỳ <span className="text-destructive">*</span>
              </Label>
              <Input
                placeholder="VD: Học kỳ 1 2024-2025"
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>Ngày bắt đầu</Label>
                <Input
                  type="date"
                  value={form.startDate}
                  onChange={(e) => setForm((f) => ({ ...f, startDate: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <Label>Ngày kết thúc</Label>
                <Input
                  type="date"
                  value={form.endDate}
                  onChange={(e) => setForm((f) => ({ ...f, endDate: e.target.value }))}
                />
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setFormOpen(false)}>
              Hủy
            </Button>
            <Button onClick={handleFormSubmit} disabled={isMutating}>
              {editTarget ? "Lưu thay đổi" : "Tạo học kỳ"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirm */}
      <AlertDialog
        open={!!deleteTarget}
        onOpenChange={(o) => {
          if (!o) setDeleteTarget(null);
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xóa học kỳ?</AlertDialogTitle>
            <AlertDialogDescription>
              Bạn có chắc muốn xóa học kỳ <strong>{deleteTarget?.name}</strong>? Học kỳ chỉ có thể
              xóa khi chưa có lớp học phần.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Hủy</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={() => deleteTarget && deleteMutation.mutate(deleteTarget.id)}
              disabled={deleteMutation.isPending}
            >
              Xóa
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
