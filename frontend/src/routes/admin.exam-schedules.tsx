import { createFileRoute, redirect } from "@tanstack/react-router";
import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { PageHeader } from "@/components/ui/page-header";
import { DataTable } from "@/components/data-table/DataTable";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { Pencil, Calendar } from "lucide-react";
import { toast } from "sonner";
import { adminApi } from "@/lib/api/admin";
import type { ExamScheduleResponse } from "@/lib/api/types";

export const Route = createFileRoute("/admin/exam-schedules")({
  beforeLoad: () => {
    throw redirect({ to: "/admin/semesters" });
  },
  component: ExamSchedulesPage,
});

function formatDateTime(value?: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(value));
}

function ExamSchedulesPage() {
  const queryClient = useQueryClient();

  const semestersQuery = useQuery({
    queryKey: ["admin", "semesters"],
    queryFn: adminApi.listSemesters,
  });
  const semesters = semestersQuery.data ?? [];

  const [semesterId, setSemesterId] = useState<string>("");
  const [editTarget, setEditTarget] = useState<ExamScheduleResponse | null>(null);
  const [examAt, setExamAt] = useState("");
  const [examRoom, setExamRoom] = useState("");

  const schedulesQuery = useQuery({
    queryKey: ["admin", "exam-schedules", semesterId],
    queryFn: () => adminApi.getExamSchedules(Number(semesterId)),
    enabled: !!semesterId,
  });

  const schedules = schedulesQuery.data ?? [];
  const withExam = schedules.filter((s) => s.examAt);
  const withoutExam = schedules.filter((s) => !s.examAt);

  // Conflict detection: same room + overlapping time (simplified: same examAt + same examRoom)
  const conflictKeys = new Set<number>();
  for (let i = 0; i < schedules.length; i++) {
    for (let j = i + 1; j < schedules.length; j++) {
      const a = schedules[i];
      const b = schedules[j];
      if (
        a.examAt &&
        b.examAt &&
        a.examRoom &&
        b.examRoom &&
        a.examAt === b.examAt &&
        a.examRoom === b.examRoom
      ) {
        conflictKeys.add(a.classSectionId);
        conflictKeys.add(b.classSectionId);
      }
    }
  }

  const updateMutation = useMutation({
    mutationFn: ({
      id,
      req,
    }: {
      id: number;
      req: { classSectionId: number; examAt: string | null; examRoom: string | null };
    }) => adminApi.updateExamSchedule(id, req),
    onSuccess: () => {
      toast.success("Đã cập nhật lịch thi");
      queryClient.invalidateQueries({ queryKey: ["admin", "exam-schedules", semesterId] });
      setEditTarget(null);
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : "Lỗi khi cập nhật lịch thi"),
  });

  function openEdit(s: ExamScheduleResponse) {
    setEditTarget(s);
    setExamAt(s.examAt ? s.examAt.replace("T", "T").slice(0, 16) : "");
    setExamRoom(s.examRoom ?? "");
  }

  function handleSubmit() {
    if (!editTarget) return;
    updateMutation.mutate({
      id: editTarget.classSectionId,
      req: {
        classSectionId: editTarget.classSectionId,
        examAt: examAt ? examAt + ":00" : null,
        examRoom: examRoom.trim() || null,
      },
    });
  }

  function handleClearSchedule() {
    if (!editTarget) return;
    updateMutation.mutate({
      id: editTarget.classSectionId,
      req: { classSectionId: editTarget.classSectionId, examAt: null, examRoom: null },
    });
  }

  return (
    <div>
      <PageHeader
        title="Quản lý lịch thi"
        description={
          semesterId
            ? `${withExam.length}/${schedules.length} lớp đã có lịch thi`
            : "Chọn học kỳ để xem"
        }
      />

      <div className="mb-4 flex items-center gap-3">
        <Select value={semesterId} onValueChange={setSemesterId}>
          <SelectTrigger className="w-[240px]">
            <SelectValue placeholder="Chọn học kỳ…" />
          </SelectTrigger>
          <SelectContent>
            {semesters.map((s) => (
              <SelectItem key={s.id} value={String(s.id)}>
                {s.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {semesterId && (
          <div className="flex gap-2 text-sm text-muted-foreground">
            <Badge variant="secondary">{withExam.length} đã có lịch</Badge>
            {withoutExam.length > 0 && (
              <Badge variant="outline">{withoutExam.length} chưa có lịch</Badge>
            )}
            {conflictKeys.size > 0 && (
              <Badge variant="destructive">{conflictKeys.size} lớp xung đột phòng</Badge>
            )}
          </div>
        )}
      </div>

      {schedulesQuery.isError && (
        <div className="mb-4 rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          {schedulesQuery.error instanceof Error
            ? schedulesQuery.error.message
            : "Không tải được lịch thi"}
        </div>
      )}

      {semesterId && (
        <DataTable
          data={schedules}
          rowKey={(s) => String(s.classSectionId)}
          pageSize={20}
          searchPlaceholder="Tìm theo mã lớp, môn học, giảng viên…"
          columns={[
            {
              key: "class",
              header: "Lớp học phần",
              accessor: (s) => s.classCode,
              render: (s) => (
                <div>
                  <div className="font-mono text-sm font-medium">{s.classCode}</div>
                  {conflictKeys.has(s.classSectionId) && (
                    <Badge variant="destructive" className="text-xs mt-0.5">
                      Xung đột phòng
                    </Badge>
                  )}
                </div>
              ),
            },
            {
              key: "course",
              header: "Môn học",
              accessor: (s) => s.courseName,
              render: (s) => (
                <div>
                  <div className="font-medium">{s.courseName}</div>
                  <div className="text-xs text-muted-foreground">
                    {s.courseCode} · {s.credits} TC
                  </div>
                </div>
              ),
            },
            {
              key: "teacher",
              header: "Giảng viên",
              accessor: (s) => s.teacherName,
              render: (s) => <span className="text-sm">{s.teacherName}</span>,
            },
            {
              key: "students",
              header: "Sinh viên",
              accessor: (s) => s.studentCount,
              render: (s) => <span className="text-sm">{s.studentCount}</span>,
            },
            {
              key: "examAt",
              header: "Ngày & giờ thi",
              accessor: (s) => s.examAt ?? "",
              render: (s) =>
                s.examAt ? (
                  <div className="flex items-center gap-1 text-sm">
                    <Calendar className="h-3.5 w-3.5 text-muted-foreground" />
                    {formatDateTime(s.examAt)}
                  </div>
                ) : (
                  <span className="text-xs text-muted-foreground">Chưa có lịch</span>
                ),
            },
            {
              key: "examRoom",
              header: "Phòng thi",
              accessor: (s) => s.examRoom ?? "",
              render: (s) =>
                s.examRoom ? (
                  <span className="font-mono text-sm">{s.examRoom}</span>
                ) : (
                  <span className="text-xs text-muted-foreground">-</span>
                ),
            },
            {
              key: "actions",
              header: "",
              searchable: false,
              render: (s) => (
                <Button variant="ghost" size="sm" onClick={() => openEdit(s)}>
                  <Pencil className="h-3.5 w-3.5 mr-1" />
                  Sửa
                </Button>
              ),
            },
          ]}
        />
      )}

      <Dialog
        open={!!editTarget}
        onOpenChange={(o) => {
          if (!o) setEditTarget(null);
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cập nhật lịch thi — {editTarget?.classCode}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="text-sm text-muted-foreground">
              {editTarget?.courseName} · {editTarget?.credits} TC · GV: {editTarget?.teacherName}
            </div>
            <div className="space-y-1">
              <Label>Ngày & giờ thi</Label>
              <Input
                type="datetime-local"
                value={examAt}
                onChange={(e) => setExamAt(e.target.value)}
              />
            </div>
            <div className="space-y-1">
              <Label>Phòng thi</Label>
              <Input
                placeholder="VD: A101, B201…"
                value={examRoom}
                onChange={(e) => setExamRoom(e.target.value)}
              />
            </div>
          </div>
          <DialogFooter className="gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleClearSchedule}
              disabled={updateMutation.isPending}
            >
              Xóa lịch thi
            </Button>
            <Button variant="outline" onClick={() => setEditTarget(null)}>
              Hủy
            </Button>
            <Button onClick={handleSubmit} disabled={updateMutation.isPending}>
              Lưu
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
