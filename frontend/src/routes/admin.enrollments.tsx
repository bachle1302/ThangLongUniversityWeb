import { createFileRoute, redirect } from "@tanstack/react-router";
import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { PageHeader } from "@/components/ui/page-header";
import { DataTable } from "@/components/data-table/DataTable";
import { StatusBadge } from "@/components/ui/status-badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
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
import { adminApi } from "@/lib/api/admin";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/enrollments")({
  beforeLoad: () => {
    throw redirect({ to: "/admin/semesters" });
  },
  component: EnrollmentsPage,
});

const ALL = "ALL";

function EnrollmentsPage() {
  const queryClient = useQueryClient();

  const semestersQuery = useQuery({
    queryKey: ["admin", "semesters"],
    queryFn: adminApi.listSemesters,
  });
  const semesters = semestersQuery.data ?? [];

  const [semesterId, setSemesterId] = useState<string>(ALL);
  const [status, setStatus] = useState<string>(ALL);
  const [page, setPage] = useState(0);
  const [overrideOpen, setOverrideOpen] = useState(false);
  const [overrideStudentId, setOverrideStudentId] = useState("");
  const [overrideClassId, setOverrideClassId] = useState("");

  const enrollmentsQuery = useQuery({
    queryKey: ["admin", "enrollments", semesterId, status, page],
    queryFn: () =>
      adminApi.listEnrollments({
        semesterId: semesterId !== ALL ? Number(semesterId) : undefined,
        status: status !== ALL ? status : undefined,
        page,
        size: 20,
      }),
  });

  const data = enrollmentsQuery.data?.content ?? [];
  const totalPages = enrollmentsQuery.data?.totalPages ?? 1;
  const totalElements = enrollmentsQuery.data?.totalElements ?? 0;

  const overrideMutation = useMutation({
    mutationFn: adminApi.overrideEnrollment,
    onSuccess: () => {
      toast.success("Đã đăng ký hộ sinh viên thành công");
      setOverrideOpen(false);
      setOverrideStudentId("");
      setOverrideClassId("");
      queryClient.invalidateQueries({ queryKey: ["admin", "enrollments"] });
    },
    onError: (err) => toast.error(err instanceof Error ? err.message : "Lỗi khi đăng ký hộ"),
  });

  function handleOverrideSubmit() {
    const studentId = Number(overrideStudentId);
    const classSectionId = Number(overrideClassId);
    if (!studentId || !classSectionId) {
      toast.error("Vui lòng nhập đầy đủ ID sinh viên và lớp học phần");
      return;
    }
    overrideMutation.mutate({ studentId, classSectionId });
  }

  return (
    <div>
      <PageHeader
        title="Đăng ký môn học"
        description={`${totalElements} đăng ký`}
        actions={<Button onClick={() => setOverrideOpen(true)}>Override đăng ký</Button>}
      />

      {enrollmentsQuery.isError && (
        <div className="mb-4 rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          {enrollmentsQuery.error instanceof Error
            ? enrollmentsQuery.error.message
            : "Không tải được danh sách đăng ký"}
        </div>
      )}

      <DataTable
        data={data}
        rowKey={(e) => String(e.enrollmentId)}
        pageSize={20}
        searchPlaceholder="Tìm theo sinh viên, môn học, mã lớp…"
        toolbar={
          <div className="flex flex-wrap gap-2">
            <Select
              value={semesterId}
              onValueChange={(v) => {
                setSemesterId(v);
                setPage(0);
              }}
            >
              <SelectTrigger className="w-[200px]">
                <SelectValue placeholder="Tất cả học kỳ" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>Tất cả học kỳ</SelectItem>
                {semesters.map((s) => (
                  <SelectItem key={s.id} value={String(s.id)}>
                    {s.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              value={status}
              onValueChange={(v) => {
                setStatus(v);
                setPage(0);
              }}
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="Tất cả trạng thái" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={ALL}>Tất cả trạng thái</SelectItem>
                <SelectItem value="PENDING">Pending</SelectItem>
                <SelectItem value="REGISTERED">Registered</SelectItem>
                <SelectItem value="CANCELED">Canceled</SelectItem>
                <SelectItem value="PASSED">Passed</SelectItem>
                <SelectItem value="FAILED">Failed</SelectItem>
              </SelectContent>
            </Select>
          </div>
        }
        columns={[
          {
            key: "student",
            header: "Sinh viên",
            accessor: (e) => e.studentName,
            render: (e) => (
              <div>
                <div className="font-medium">{e.studentName}</div>
                <div className="text-xs text-muted-foreground font-mono">{e.studentCode}</div>
              </div>
            ),
          },
          {
            key: "course",
            header: "Lớp học phần",
            accessor: (e) => e.courseName,
            render: (e) => (
              <div>
                <div className="text-sm font-medium">{e.courseName}</div>
                <div className="text-xs text-muted-foreground font-mono">{e.classCode}</div>
                {e.credits != null && (
                  <div className="text-xs text-muted-foreground">{e.credits} tín chỉ</div>
                )}
              </div>
            ),
          },
          {
            key: "sem",
            header: "Học kỳ",
            accessor: (e) => e.semesterName ?? String(e.semesterId),
            render: (e) => (
              <span className="text-xs text-muted-foreground">
                {e.semesterName ?? e.semesterId}
              </span>
            ),
          },
          {
            key: "status",
            header: "Trạng thái",
            render: (e) => <StatusBadge value={e.status} />,
          },
        ]}
      />

      {totalPages > 1 && (
        <div className="mt-4 flex items-center justify-between text-sm text-muted-foreground">
          <span>
            Trang {page + 1} / {totalPages}
          </span>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
            >
              Trước
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Sau
            </Button>
          </div>
        </div>
      )}

      <Dialog open={overrideOpen} onOpenChange={setOverrideOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Override đăng ký (đăng ký hộ sinh viên)</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-2">
            <div className="space-y-1">
              <Label>ID sinh viên</Label>
              <Input
                placeholder="VD: 1"
                value={overrideStudentId}
                onChange={(e) => setOverrideStudentId(e.target.value)}
              />
            </div>
            <div className="space-y-1">
              <Label>ID lớp học phần</Label>
              <Input
                placeholder="VD: 5"
                value={overrideClassId}
                onChange={(e) => setOverrideClassId(e.target.value)}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOverrideOpen(false)}>
              Hủy
            </Button>
            <Button onClick={handleOverrideSubmit} disabled={overrideMutation.isPending}>
              Xác nhận
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
