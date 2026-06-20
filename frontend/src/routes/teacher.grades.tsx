import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { Lock, LockOpen, Search } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { PageHeader } from "@/components/ui/page-header";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { TeacherGradeTable } from "@/features/teacher/TeacherGradeTable";
import {
  buildTeacherGradeUpdateRequest,
  getTeacherClassRows,
  getTeacherGradeRows,
  type TeacherGradeRow,
} from "@/features/teacher/teacherMappers";
import { useTeacherSemester } from "@/features/teacher/useTeacherSemester";
import { teacherApi } from "@/lib/api/teacher";

export const Route = createFileRoute("/teacher/grades")({ component: TeacherGradesPage });

const ALL_COURSES = "__all__";

function TeacherGradesPage() {
  const queryClient = useQueryClient();
  const { semesterId, setSemesterId, semesterOptions } = useTeacherSemester();
  const [classSectionId, setClassSectionId] = useState<string>("");
  const [draftRows, setDraftRows] = useState<TeacherGradeRow[]>([]);
  const [classSearch, setClassSearch] = useState("");
  const [courseFilter, setCourseFilter] = useState("");
  const [gradeLocked, setGradeLocked] = useState(false);
  const [retakeOnly, setRetakeOnly] = useState(false);

  const classesQuery = useQuery({
    queryKey: ["teacher", "classes", semesterId],
    queryFn: () => teacherApi.listMyClasses(semesterId),
    enabled: Boolean(semesterId),
    retry: false,
  });

  const classRows = useMemo(
    () => getTeacherClassRows(classesQuery.isError ? undefined : classesQuery.data),
    [classesQuery.data, classesQuery.isError],
  );

  const uniqueCourses = useMemo(() => {
    const map = new Map<string, string>();
    classRows.forEach((row) => map.set(row.courseCode, row.courseName));
    return Array.from(map.entries()).map(([code, name]) => ({ code, name }));
  }, [classRows]);

  const filteredClasses = useMemo(() => {
    let rows = classRows;
    if (courseFilter && courseFilter !== ALL_COURSES)
      rows = rows.filter((r) => r.courseCode === courseFilter);
    if (classSearch.trim()) {
      const kw = classSearch.toLowerCase();
      rows = rows.filter(
        (r) => r.classCode.toLowerCase().includes(kw) || r.courseName.toLowerCase().includes(kw),
      );
    }
    return rows;
  }, [classRows, courseFilter, classSearch]);

  const gradesQuery = useQuery({
    queryKey: ["teacher", "grades", classSectionId],
    queryFn: () => teacherApi.getClassGrades(classSectionId),
    enabled: Boolean(classSectionId),
    retry: false,
  });

  const rows = useMemo(
    () => getTeacherGradeRows(gradesQuery.isError ? undefined : gradesQuery.data),
    [gradesQuery.data, gradesQuery.isError],
  );

  useEffect(() => {
    setDraftRows(rows);
  }, [rows]);

  // Sync gradeLocked from class data
  useEffect(() => {
    if (!classSectionId) return;
    const apiClass = classesQuery.data?.find((c) => String(c.id) === classSectionId);
    setGradeLocked(apiClass?.gradeLocked ?? false);
  }, [classSectionId, classesQuery.data]);

  const selectedClass = classRows.find((row) => row.id === classSectionId);

  const updateGradeMutation = useMutation({
    mutationFn: (row: TeacherGradeRow) =>
      teacherApi.updateGrade(
        row.numericEnrollmentId ?? row.enrollmentId,
        buildTeacherGradeUpdateRequest(row),
      ),
    onSuccess: () => {
      toast.success("Đã lưu điểm");
      void queryClient.invalidateQueries({ queryKey: ["teacher", "grades", classSectionId] });
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Lưu điểm thất bại"),
  });

  const lockMutation = useMutation({
    mutationFn: async () => {
      // Save all editable rows first
      const editableRows = draftRows.filter((r) => r.canEdit && r.numericEnrollmentId);
      for (const row of editableRows) {
        await teacherApi.updateGrade(row.numericEnrollmentId!, buildTeacherGradeUpdateRequest(row));
      }
      await teacherApi.lockClassGrades(classSectionId);
    },
    onSuccess: () => {
      toast.success("Đã lưu và khóa điểm toàn bộ lớp.");
      setGradeLocked(true);
      void queryClient.invalidateQueries({ queryKey: ["teacher", "grades", classSectionId] });
      void queryClient.invalidateQueries({ queryKey: ["teacher", "classes", semesterId] });
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Khóa điểm thất bại"),
  });

  const selectClass = (id: string) => {
    setClassSectionId(id);
    setDraftRows([]);
    setGradeLocked(false);
  };

  const clearClass = () => {
    setClassSectionId("");
    setDraftRows([]);
    setGradeLocked(false);
  };

  return (
    <div className="space-y-5">
      <PageHeader
        title="Quản lý điểm"
        description="Nhập điểm thành phần và khóa bảng điểm"
        actions={
          classSectionId ? (
            gradeLocked ? (
              <Badge variant="outline" className="gap-1.5 px-3 py-1.5 text-sm">
                <Lock className="h-3.5 w-3.5" /> Bảng điểm đã khóa
              </Badge>
            ) : (
              <Button
                className="gap-2"
                disabled={lockMutation.isPending || draftRows.length === 0}
                onClick={() => lockMutation.mutate()}
              >
                <LockOpen className="h-4 w-4" />
                {lockMutation.isPending ? "Đang khóa..." : "Khóa điểm"}
              </Button>
            )
          ) : undefined
        }
      />

      <section className="rounded-xl border bg-card p-4 shadow-sm">
        {!classSectionId ? (
          /* ── Class list picker ── */
          <>
            <div className="mb-4 flex flex-col gap-3 sm:flex-row">
              <Select
                value={semesterId}
                onValueChange={setSemesterId}
                disabled={semesterOptions.length === 0}
              >
                <SelectTrigger className="sm:w-52">
                  <SelectValue placeholder="Chọn học kỳ" />
                </SelectTrigger>
                <SelectContent>
                  {semesterOptions.map((s) => (
                    <SelectItem key={s.id} value={s.id}>
                      {s.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <div className="relative flex-1">
                <Search className="pointer-events-none absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  className="pl-8"
                  placeholder="Tìm lớp học phần..."
                  value={classSearch}
                  onChange={(e) => setClassSearch(e.target.value)}
                />
              </div>
              <Select
                value={courseFilter || ALL_COURSES}
                onValueChange={(v) => setCourseFilter(v === ALL_COURSES ? "" : v)}
                disabled={uniqueCourses.length === 0}
              >
                <SelectTrigger className="sm:w-56">
                  <SelectValue placeholder="Tất cả môn học" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={ALL_COURSES}>Tất cả môn học</SelectItem>
                  {uniqueCourses.map((c) => (
                    <SelectItem key={c.code} value={c.code}>
                      {c.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {!semesterId && (
              <div className="py-10 text-center text-sm text-muted-foreground">
                Hãy chọn học kỳ để xem danh sách lớp học phần.
              </div>
            )}
            {classesQuery.isLoading && (
              <div className="py-10 text-center text-sm text-muted-foreground">
                Đang tải danh sách lớp...
              </div>
            )}
            {classesQuery.isError && (
              <div className="py-4 text-sm text-destructive">
                {classesQuery.error instanceof Error
                  ? classesQuery.error.message
                  : "Không tải được danh sách lớp"}
              </div>
            )}
            {semesterId &&
              !classesQuery.isLoading &&
              !classesQuery.isError &&
              filteredClasses.length === 0 && (
                <div className="py-10 text-center text-sm text-muted-foreground">
                  Không tìm thấy lớp học phần phù hợp.
                </div>
              )}

            <div className="grid gap-3 lg:grid-cols-2">
              {filteredClasses.map((row) => {
                const isLocked =
                  classesQuery.data?.find((c) => String(c.id) === row.id)?.gradeLocked ?? false;
                return (
                  <div
                    key={row.id}
                    className="flex items-center justify-between gap-3 rounded-lg border bg-background p-3 transition-colors hover:bg-muted/40"
                  >
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <span className="truncate font-semibold">{row.courseName}</span>
                        <Badge
                          variant={isLocked ? "secondary" : "outline"}
                          className="shrink-0 gap-1 text-xs"
                        >
                          {isLocked ? (
                            <Lock className="h-3 w-3" />
                          ) : (
                            <LockOpen className="h-3 w-3" />
                          )}
                          {isLocked ? "Đã khóa" : "Chưa khóa"}
                        </Badge>
                        {row.virtualRetakeClass && (
                          <Badge className="shrink-0 bg-amber-100 text-amber-800 hover:bg-amber-100 text-xs">
                            Lớp thi lại/nâng
                          </Badge>
                        )}
                      </div>
                      <div className="text-sm text-muted-foreground">{row.classCode}</div>
                      {row.scheduleText && (
                        <div className="mt-0.5 text-xs text-muted-foreground">
                          {row.scheduleText}
                        </div>
                      )}
                    </div>
                    <Button size="sm" onClick={() => selectClass(row.id)}>
                      Quản lý điểm
                    </Button>
                  </div>
                );
              })}
            </div>
          </>
        ) : (
          /* ── Selected class header ── */
          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-2">
                <span className="font-semibold">{selectedClass?.courseName}</span>
                <Badge variant={gradeLocked ? "secondary" : "outline"} className="gap-1 text-xs">
                  {gradeLocked ? <Lock className="h-3 w-3" /> : <LockOpen className="h-3 w-3" />}
                  {gradeLocked ? "Đã khóa" : "Chưa khóa"}
                </Badge>
                {selectedClass?.virtualRetakeClass && (
                  <Badge className="bg-amber-100 text-amber-800 hover:bg-amber-100 text-xs">
                    Lớp thi lại/nâng
                  </Badge>
                )}
              </div>
              <div className="mt-0.5 text-sm text-muted-foreground">
                {selectedClass?.classCode}
                {selectedClass?.scheduleText ? ` · ${selectedClass.scheduleText}` : ""}
              </div>
            </div>
            <Button variant="outline" size="sm" onClick={clearClass} className="shrink-0">
              ← Đổi lớp
            </Button>
          </div>
        )}
      </section>

      {classSectionId && (
        <>
          <div className="flex items-center gap-2">
            <label className="flex items-center gap-2 text-sm text-muted-foreground">
              <input
                type="checkbox"
                checked={retakeOnly}
                onChange={(e) => setRetakeOnly(e.target.checked)}
                className="rounded border"
              />
              Chỉ hiển thị sinh viên thi lại / thi nâng điểm
            </label>
          </div>
          {gradesQuery.isLoading && (
            <div className="rounded-lg border bg-card p-6 text-sm text-muted-foreground">
              Đang tải bảng điểm...
            </div>
          )}
          {gradesQuery.isError && (
            <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
              {gradesQuery.error instanceof Error
                ? gradesQuery.error.message
                : "Không tải được bảng điểm"}
            </div>
          )}
          <TeacherGradeTable
            rows={draftRows}
            disabled={gradeLocked}
            retakeOnly={retakeOnly}
            onChange={(nextRow) =>
              setDraftRows((current) =>
                current.map((item) =>
                  item.enrollmentId === nextRow.enrollmentId ? nextRow : item,
                ),
              )
            }
            onSave={(row) => {
              updateGradeMutation.mutate(row);
            }}
          />
        </>
      )}
    </div>
  );
}
