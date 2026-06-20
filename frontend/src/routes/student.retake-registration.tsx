import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";
import { PageHeader } from "@/components/ui/page-header";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/status-badge";
import { toast } from "sonner";
import { Loader2, Lock, X } from "lucide-react";
import { studentApi } from "@/lib/api/student";
import { useSemesterRealtime } from "@/hooks/useSemesterRealtime";

export const Route = createFileRoute("/student/retake-registration")({ component: RetakePage });

type SemesterOption = { id: number; name: string };

function formatVND(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function RetakePage() {
  const queryClient = useQueryClient();
  const [semesterId, setSemesterId] = useState<number | null>(null);
  const [selectedCourseIds, setSelectedCourseIds] = useState<Set<number>>(new Set());
  useSemesterRealtime(semesterId);

  const overviewQuery = useQuery({
    queryKey: ["student", "retakes", "overview", semesterId],
    queryFn: () => studentApi.getRetakeOverview(semesterId),
  });

  const semesterOptions = useMemo<SemesterOption[]>(
    () => (overviewQuery.data?.semesters ?? []).map((s) => ({ id: s.id, name: s.name })),
    [overviewQuery.data?.semesters],
  );

  useEffect(() => {
    if (semesterId == null && overviewQuery.data?.currentSemester?.id != null) {
      setSemesterId(overviewQuery.data.currentSemester.id);
    }
  }, [overviewQuery.data?.currentSemester?.id, semesterId]);

  const currentSemester = overviewQuery.data?.currentSemester;
  const readonly =
    overviewQuery.data?.readonly ??
    Boolean(currentSemester?.retakeLocked || !currentSemester?.retakeOpen);

  const registerMutation = useMutation({
    mutationFn: (courseIds: number[]) => studentApi.registerRetakes({ semesterId, courseIds }),
    onSuccess: (response) => {
      setSelectedCourseIds(new Set());
      queryClient.invalidateQueries({ queryKey: ["student", "retakes"] });
      queryClient.invalidateQueries({ queryKey: ["student", "dashboard"] });
      toast.success(
        `Đã thêm ${response.registeredCount} môn vào danh sách chờ. Phí sẽ tính sau khi admin chốt.`,
      );
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Đăng ký thất bại"),
  });

  const cancelMutation = useMutation({
    mutationFn: (examRegistrationId: number) => studentApi.cancelRetake(examRegistrationId),
    onSuccess: (message) => {
      queryClient.invalidateQueries({ queryKey: ["student", "retakes"] });
      queryClient.invalidateQueries({ queryKey: ["student", "dashboard"] });
      toast.success(message || "Đã bỏ chọn thi lại / nâng điểm");
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Bỏ chọn thất bại"),
  });

  const rows = overviewQuery.data?.eligibleCourses ?? [];
  const requests = overviewQuery.data?.requests ?? [];
  const requestedCourseIds = new Set(requests.map((r) => r.courseId));
  const pendingRequests = requests.filter((r) => r.status === "PENDING");
  const selectedRows = rows.filter((row) => selectedCourseIds.has(row.courseId));
  const totalFee = selectedRows.reduce((sum, row) => sum + row.retakeFee, 0);

  const toggleCourse = (courseId: number) => {
    setSelectedCourseIds((prev) => {
      const next = new Set(prev);
      if (next.has(courseId)) next.delete(courseId);
      else next.add(courseId);
      return next;
    });
  };

  const submit = () => {
    if (selectedCourseIds.size === 0) {
      toast.error("Chọn ít nhất một môn để đăng ký");
      return;
    }
    registerMutation.mutate(Array.from(selectedCourseIds));
  };

  return (
    <div>
      {currentSemester && !currentSemester.retakeOpen && (
        <div className="m-6 rounded-lg border bg-amber-100 p-4 text-center text-amber-800">
          <p className="font-medium">🔴 Chưa mở đăng ký thi lại</p>
          <p className="text-sm mt-1">
            Nhà trường chưa mở đăng ký thi lại / nâng điểm cho học kỳ này.
          </p>
        </div>
      )}
      <PageHeader
        title="Đăng ký thi lại / nâng điểm"
        description="Môn dưới 4 điểm: thi lại. Từ 4 đến dưới 8: thi nâng điểm."
        actions={
          <>
            <select
              className="h-9 rounded-md border bg-background px-3 text-sm"
              value={semesterId ?? ""}
              onChange={(e) => {
                setSemesterId(Number(e.target.value));
                setSelectedCourseIds(new Set());
              }}
            >
              {semesterOptions.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
            <Button
              onClick={submit}
              disabled={readonly || selectedCourseIds.size === 0 || registerMutation.isPending}
            >
              {registerMutation.isPending ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : null}
              Xác nhận đăng ký ({selectedCourseIds.size})
            </Button>
          </>
        }
      />

      {readonly && (
        <div className="mb-4 flex items-center gap-2 rounded-lg border bg-muted/50 px-4 py-3 text-sm text-muted-foreground">
          <Lock className="h-4 w-4" />
          Kỳ đăng ký đã đóng hoặc đã khóa. Danh sách hiện tại chỉ được xem.
        </div>
      )}

      <div className="mb-6 rounded-xl border bg-card p-4 shadow-sm">
        <div className="flex items-center justify-between gap-3">
          <h2 className="text-sm font-semibold">Danh sách thi lại / nâng điểm đã chọn</h2>
          <span className="rounded-md bg-muted px-2 py-1 text-xs text-muted-foreground">
            {pendingRequests.length} môn
          </span>
        </div>
        <div className="mt-3 divide-y">
          {overviewQuery.isLoading ? (
            <div className="py-4 text-sm text-muted-foreground">Đang tải danh sách...</div>
          ) : pendingRequests.length === 0 ? (
            <div className="py-4 text-sm text-muted-foreground">
              Chưa có môn nào ở trạng thái PENDING.
            </div>
          ) : (
            pendingRequests.map((r) => {
              const canceling =
                cancelMutation.isPending && cancelMutation.variables === r.enrollmentId;
              return (
                <div key={r.enrollmentId} className="flex items-center justify-between gap-3 py-3">
                  <div className="min-w-0">
                    <div className="truncate font-medium">{r.courseName}</div>
                    <div className="text-xs text-muted-foreground">
                      {r.courseCode} - {r.classCode} - {r.enrollmentType}
                    </div>
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    className="gap-2"
                    disabled={readonly || canceling}
                    onClick={() => cancelMutation.mutate(r.enrollmentId)}
                  >
                    {canceling ? (
                      <Loader2 className="h-4 w-4 animate-spin" />
                    ) : (
                      <X className="h-4 w-4" />
                    )}
                    Bỏ chọn
                  </Button>
                </div>
              );
            })
          )}
        </div>
      </div>

      {overviewQuery.isError && (
        <div className="mb-4 rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          {overviewQuery.error instanceof Error
            ? overviewQuery.error.message
            : "Không tải được danh sách môn đủ điều kiện"}
        </div>
      )}

      <div className="rounded-xl border bg-card shadow-sm">
        <div className="border-b px-4 py-3 flex items-center justify-between">
          <h2 className="text-sm font-semibold">Danh sách môn đủ điều kiện</h2>
          <span className="text-xs text-muted-foreground">Phí sẽ được xác nhận khi đăng ký</span>
        </div>
        <div className="divide-y">
          {overviewQuery.isLoading ? (
            <div className="py-10 text-center text-sm text-muted-foreground">
              Đang tải dữ liệu...
            </div>
          ) : rows.length === 0 ? (
            <div className="py-10 text-center text-sm text-muted-foreground">
              Không có môn đủ điều kiện trong học kỳ này.
            </div>
          ) : (
            rows.map((r) => {
              const alreadyRequested = requestedCourseIds.has(r.courseId);
              const isSelected = selectedCourseIds.has(r.courseId);
              return (
                <label
                  key={r.courseId}
                  className={`flex cursor-pointer items-center gap-4 px-4 py-3 transition-colors hover:bg-muted/30 ${alreadyRequested || readonly ? "opacity-60 cursor-not-allowed" : ""}`}
                >
                  <input
                    type="checkbox"
                    className="h-4 w-4"
                    checked={isSelected || alreadyRequested}
                    disabled={alreadyRequested || readonly}
                    onChange={() => !alreadyRequested && !readonly && toggleCourse(r.courseId)}
                  />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-medium">{r.courseName}</span>
                      <span className="font-mono text-xs text-muted-foreground">
                        {r.courseCode}
                      </span>
                      <StatusBadge value={r.registrationType} />
                      {alreadyRequested && (
                        <StatusBadge
                          value={
                            requests.find((req) => req.courseId === r.courseId)?.status ?? "PENDING"
                          }
                        />
                      )}
                    </div>
                    <div className="mt-0.5 flex gap-4 text-xs text-muted-foreground">
                      <span>{r.credits} tín chỉ</span>
                      <span>
                        Điểm cũ:{" "}
                        <span
                          className={
                            r.previousTotalScore < 4
                              ? "font-semibold text-destructive"
                              : "font-semibold"
                          }
                        >
                          {r.previousTotalScore.toFixed(1)}
                        </span>
                      </span>
                      <span>Lần thi: {r.previousAttemptNumber ?? 1}</span>
                    </div>
                  </div>
                  <div className="text-sm font-semibold text-primary tabular-nums">
                    {formatVND(r.retakeFee)}
                  </div>
                </label>
              );
            })
          )}
        </div>
        {selectedCourseIds.size > 0 && (
          <div className="border-t bg-muted/30 px-4 py-3 flex justify-between items-center">
            <span className="text-sm text-muted-foreground">
              {selectedCourseIds.size} môn được chọn
            </span>
            <span className="font-semibold text-primary">{formatVND(totalFee)}</span>
          </div>
        )}
      </div>

      {requests.length > 0 && (
        <div className="mt-6">
          <h3 className="mb-3 text-sm font-semibold text-muted-foreground">Đã đăng ký thi lại</h3>
          <div className="rounded-xl border bg-card shadow-sm divide-y">
            {requests.map((r) => (
              <div key={r.enrollmentId} className="flex items-center justify-between px-4 py-3">
                <div>
                  <div className="font-medium">{r.courseName}</div>
                  <div className="text-xs text-muted-foreground">
                    {r.semesterName} - {r.classCode}
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  {r.enrollmentType && <StatusBadge value={r.enrollmentType} />}
                  {r.status && <StatusBadge value={r.status} />}
                  {r.totalScore != null && (
                    <span className="text-xs text-muted-foreground">
                      Điểm: {r.totalScore.toFixed(1)}
                    </span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
