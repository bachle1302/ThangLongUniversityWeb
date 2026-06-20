import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import {
  AlertTriangle,
  Check,
  CheckCircle2,
  Clock,
  Lock,
  LockOpen,
  Search,
  UserMinus,
  Users,
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { PageHeader, StatCard } from "@/components/ui/page-header";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  getTeacherClassRows,
  getTeacherRosterRows,
  type TeacherRosterRow,
} from "@/features/teacher/teacherMappers";
import { useTeacherSemester } from "@/features/teacher/useTeacherSemester";
import type { AttendanceRecordRequest } from "@/lib/api/types";
import { teacherApi } from "@/lib/api/teacher";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/teacher/attendance")({ component: TeacherAttendancePage });

type AttendanceStatus = "PRESENT" | "ABSENT" | "LATE" | "UNMARKED";
type AttendanceBook = Record<string, Record<number, AttendanceStatus>>;

const WEEKS_PER_COURSE = 15;
const ABSENT_LIMIT = 3;

const statusMeta: Record<AttendanceStatus, { label: string; short: string; className: string }> = {
  PRESENT: {
    label: "Có mặt",
    short: "C",
    className: "border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100",
  },
  LATE: {
    label: "Muộn",
    short: "M",
    className: "border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-100",
  },
  ABSENT: {
    label: "Vắng",
    short: "V",
    className: "border-red-200 bg-red-50 text-red-700 hover:bg-red-100",
  },
  UNMARKED: {
    label: "Chưa điểm danh",
    short: "-",
    className: "border-border bg-background text-muted-foreground hover:bg-muted",
  },
};

const checkIsBanned = (row: TeacherRosterRow, studentSessions: Record<number, AttendanceStatus>) => {
  const absentCount = Object.values(studentSessions).filter((s) => s === "ABSENT").length;
  const backendStatus = row.courseStatus;
  const localBanned = absentCount > ABSENT_LIMIT;
  return (
    backendStatus === "BANNED_FROM_EXAM" ||
    backendStatus === "REPEAT_COURSE" ||
    localBanned
  );
};

function TeacherAttendancePage() {
  const { semesterId, setSemesterId, semesterOptions } = useTeacherSemester();
  const [classSectionId, setClassSectionId] = useState("");
  const [selectedSession, setSelectedSession] = useState(1);
  const [search, setSearch] = useState("");
  const [classSearch, setClassSearch] = useState("");
  const [courseFilter, setCourseFilter] = useState("");
  const [lockedSessions, setLockedSessions] = useState<Set<number>>(new Set());
  const [hasAutoSelected, setHasAutoSelected] = useState(false);
  const [attendance, setAttendance] = useState<AttendanceBook>({});
  const queryClient = useQueryClient();

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

  useEffect(() => {
    if (classSectionId && !classRows.some((row) => row.id === classSectionId)) {
      setClassSectionId("");
      setSelectedSession(1);
      setAttendance({});
    }
  }, [classRows, classSectionId]);

  const uniqueCourses = useMemo(() => {
    const map = new Map<string, string>();
    classRows.forEach((row) => map.set(row.courseCode, row.courseName));
    return Array.from(map.entries()).map(([code, name]) => ({ code, name }));
  }, [classRows]);

  const ALL_COURSES = "__all__";

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

  const selectedClass = classRows.find((row) => row.id === classSectionId);
  const selectedApiClass = classesQuery.data?.find((row) => String(row.id) === classSectionId);
  const sessionsPerWeek = Math.min(Math.max(selectedApiClass?.schedules?.length ?? 1, 1), 2);
  const totalSessions = WEEKS_PER_COURSE * sessionsPerWeek;
  const sessionNumbers = useMemo(
    () => Array.from({ length: totalSessions }, (_, index) => index + 1),
    [totalSessions],
  );

  const rosterQuery = useQuery({
    queryKey: ["teacher", "classes", classSectionId, "students"],
    queryFn: () => teacherApi.listClassStudents(selectedClass?.numericId ?? classSectionId),
    enabled: Boolean(selectedClass?.numericId),
    retry: false,
  });

  const rosterRows = useMemo(
    () => getTeacherRosterRows(rosterQuery.isError ? undefined : rosterQuery.data),
    [rosterQuery.data, rosterQuery.isError],
  );

  // Load saved attendance records for the selected session from backend
  const sessionQuery = useQuery({
    queryKey: ["teacher", "attendance", classSectionId, selectedSession],
    queryFn: () =>
      teacherApi.getAttendanceSession(selectedClass?.numericId ?? classSectionId, selectedSession),
    enabled: Boolean(selectedClass?.numericId) && rosterRows.length > 0,
    retry: false,
  });

  // Load all sessions to determine locked state and auto-select current session
  const allSessionsQuery = useQuery({
    queryKey: ["teacher", "attendance", classSectionId, "all"],
    queryFn: () => teacherApi.getAttendanceSessions(selectedClass?.numericId ?? classSectionId),
    enabled: Boolean(selectedClass?.numericId),
    retry: false,
  });

  // Initialize locked sessions from backend data
  useEffect(() => {
    if (!allSessionsQuery.data) return;
    const locked = new Set(
      allSessionsQuery.data.filter((s) => s.locked).map((s) => s.sessionNumber),
    );
    setLockedSessions(locked);
  }, [allSessionsQuery.data]);

  // Auto-select the current (today's) session: first session not yet locked
  useEffect(() => {
    if (hasAutoSelected || !allSessionsQuery.data || !rosterRows.length) return;
    const lockedNums = new Set(
      allSessionsQuery.data.filter((s) => s.locked).map((s) => s.sessionNumber),
    );
    const firstUnlocked = sessionNumbers.find((n) => !lockedNums.has(n)) ?? sessionNumbers[0] ?? 1;
    setSelectedSession(firstUnlocked);
    setHasAutoSelected(true);
  }, [allSessionsQuery.data, rosterRows.length, hasAutoSelected, sessionNumbers]);

  // Hydrate local state with records returned from backend
  useEffect(() => {
    if (!sessionQuery.data) return;
    const records = sessionQuery.data.records;
    if (records.length === 0) return;
    setAttendance((current) => {
      const next = { ...current };
      records.forEach((record) => {
        const eid = String(record.enrollmentId);
        next[eid] = {
          ...(next[eid] ?? {}),
          [selectedSession]: record.status as AttendanceStatus,
        };
      });
      return next;
    });
  }, [sessionQuery.data, selectedSession]);

  useEffect(() => {
    setAttendance((current) => {
      const next: AttendanceBook = {};
      rosterRows.forEach((row) => {
        next[row.enrollmentId] = {};
        sessionNumbers.forEach((session) => {
          next[row.enrollmentId][session] = current[row.enrollmentId]?.[session] ?? "UNMARKED";
        });
      });
      return next;
    });
  }, [rosterRows, sessionNumbers]);

  useEffect(() => {
    if (selectedSession > totalSessions) setSelectedSession(1);
  }, [selectedSession, totalSessions]);

  const filteredRows = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    if (!keyword) return rosterRows;
    return rosterRows.filter((row) =>
      [row.studentCode, row.fullName].some((value) => value.toLowerCase().includes(keyword)),
    );
  }, [rosterRows, search]);

  const nextAllowedSession = useMemo(
    () => getNextAllowedSession(lockedSessions, totalSessions),
    [lockedSessions, totalSessions],
  );
  const currentCounts = useMemo(
    () => countSession(attendance, rosterRows, selectedSession),
    [attendance, rosterRows, selectedSession],
  );
  const activeStudentCount = useMemo(
    () =>
      rosterRows.filter((row) => !checkIsBanned(row, attendance[row.enrollmentId] ?? {}))
        .length,
    [attendance, rosterRows],
  );
  const currentWeek = Math.ceil(selectedSession / sessionsPerWeek);
  const sessionInWeek = ((selectedSession - 1) % sessionsPerWeek) + 1;
  const isCurrentSessionComplete = rosterRows.length > 0 && currentCounts.UNMARKED === 0;
  const isSessionLocked = lockedSessions.has(selectedSession);

  const lockMutation = useMutation({
    mutationFn: async () => {
      const records: AttendanceRecordRequest[] = rosterRows
        .map((row) => {
          const status = attendance[row.enrollmentId]?.[selectedSession];
          if (!status || status === "UNMARKED") return null;
          return {
            enrollmentId: row.numericEnrollmentId ?? Number(row.enrollmentId),
            status: status as "PRESENT" | "LATE" | "ABSENT",
          };
        })
        .filter((r): r is AttendanceRecordRequest => r !== null);
      await teacherApi.saveAttendanceRecords(
        selectedClass?.numericId ?? classSectionId,
        selectedSession,
        records,
      );
      await teacherApi.lockAttendanceSession(
        selectedClass?.numericId ?? classSectionId,
        selectedSession,
      );
    },
    onSuccess: () => {
      toast.success(`Đã khóa buổi ${selectedSession}.`);
      setLockedSessions((prev) => new Set([...prev, selectedSession]));
      void queryClient.invalidateQueries({
        queryKey: ["teacher", "classes", classSectionId, "students"],
      });
      void queryClient.invalidateQueries({
        queryKey: ["teacher", "attendance", classSectionId, selectedSession],
      });
      void queryClient.invalidateQueries({
        queryKey: ["teacher", "attendance", classSectionId, "all"],
      });
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Khóa thất bại, vui lòng thử lại.");
    },
  });

  const setStudentStatus = (enrollmentId: string, status: AttendanceStatus) => {
    setAttendance((current) => ({
      ...current,
      [enrollmentId]: {
        ...current[enrollmentId],
        [selectedSession]: status,
      },
    }));
  };

  const selectSession = (session: number) => {
    if (session > nextAllowedSession && !lockedSessions.has(session)) {
      toast.error(`Cần hoàn thành buổi ${nextAllowedSession} trước khi điểm danh buổi ${session}`);
      return;
    }
    setSelectedSession(session);
  };

  const lockCurrentSession = () => {
    if (!isCurrentSessionComplete) {
      toast.error("Còn sinh viên chưa được điểm danh trong buổi này");
      return;
    }
    lockMutation.mutate();
  };

  const selectClass = (id: string) => {
    setClassSectionId(id);
    setSelectedSession(1);
    setSearch("");
    setAttendance({});
    setHasAutoSelected(false);
    setLockedSessions(new Set());
  };

  const clearClass = () => {
    setClassSectionId("");
    setSelectedSession(1);
    setSearch("");
    setAttendance({});
    setHasAutoSelected(false);
    setLockedSessions(new Set());
  };

  return (
    <div className="space-y-5">
      <PageHeader
        title="Điểm danh"
        description="Điểm danh theo từng buổi học và khóa các buổi tương lai nếu buổi hiện tại chưa xong"
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
              {filteredClasses.map((row) => (
                <div
                  key={row.id}
                  className="flex items-center justify-between gap-3 rounded-lg border bg-background p-3 transition-colors hover:bg-muted/40"
                >
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-semibold">{row.courseName}</div>
                    <div className="text-sm text-muted-foreground">{row.classCode}</div>
                    {row.scheduleText && (
                      <div className="mt-0.5 text-xs text-muted-foreground">{row.scheduleText}</div>
                    )}
                  </div>
                  <Button size="sm" onClick={() => selectClass(row.id)}>
                    Điểm danh
                  </Button>
                </div>
              ))}
            </div>
          </>
        ) : (
          /* ── Selected class header ── */
          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
            <div className="min-w-0 flex-1">
              <div className="font-semibold">{selectedClass?.courseName}</div>
              <div className="mt-0.5 text-sm text-muted-foreground">
                {selectedClass?.classCode}
                {selectedClass?.scheduleText ? ` · ${selectedClass.scheduleText}` : ""}
              </div>
              <div className="mt-2 flex flex-wrap gap-2">
                <Badge variant="outline">{WEEKS_PER_COURSE} tuần</Badge>
                <Badge variant="outline">{sessionsPerWeek} buổi/tuần</Badge>
                <Badge variant="outline">{totalSessions} buổi học</Badge>
              </div>
            </div>
            <Button variant="outline" size="sm" onClick={clearClass} className="shrink-0">
              ← Đổi lớp
            </Button>
          </div>
        )}
      </section>

      {selectedClass && (
        <>
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard label="Còn lại lớp" value={activeStudentCount} icon={Users} tone="primary" />
            <StatCard
              label="Đi học buổi này"
              value={currentCounts.PRESENT}
              icon={CheckCircle2}
              tone="success"
            />
            <StatCard
              label="Muộn buổi này"
              value={currentCounts.LATE}
              icon={Clock}
              tone="warning"
            />
            <StatCard
              label="Nghỉ buổi này"
              value={currentCounts.ABSENT}
              icon={UserMinus}
              tone="destructive"
            />
          </div>

          <section className="rounded-xl border bg-card p-4 shadow-sm">
            <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <div className="flex items-center gap-2 text-sm font-semibold">
                  Buổi {selectedSession} - Tuần {currentWeek}
                  {sessionsPerWeek > 1 ? `, ca ${sessionInWeek}` : ""}
                  {isSessionLocked && (
                    <Badge variant="secondary" className="gap-1">
                      <Lock className="h-3 w-3" /> Đã khóa
                    </Badge>
                  )}
                </div>
                <div className="text-xs text-muted-foreground">
                  {isSessionLocked
                    ? "Buổi này đã được khóa, không thể chỉnh sửa thêm."
                    : "Điểm danh xong tất cả sinh viên rồi bấm Khóa buổi."}
                </div>
              </div>
              <div>
                {isSessionLocked ? (
                  <Badge variant="outline" className="gap-1.5 px-3 py-1.5 text-sm">
                    <Lock className="h-3.5 w-3.5" /> Buổi đã khóa
                  </Badge>
                ) : (
                  <Button
                    type="button"
                    disabled={
                      !isCurrentSessionComplete || lockMutation.isPending || rosterRows.length === 0
                    }
                    onClick={lockCurrentSession}
                    className="gap-2"
                  >
                    <LockOpen className="h-4 w-4" />
                    {lockMutation.isPending ? "Đang khóa..." : `Khóa buổi ${selectedSession}`}
                  </Button>
                )}
              </div>
            </div>

            <SessionTimeline
              attendance={attendance}
              nextAllowedSession={nextAllowedSession}
              rosterRows={rosterRows}
              selectedSession={selectedSession}
              sessionsPerWeek={sessionsPerWeek}
              lockedSessions={lockedSessions}
              onSelect={selectSession}
            />
          </section>

          <section className="rounded-xl border bg-card shadow-sm">
            <div className="flex flex-col gap-3 border-b p-4 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <h2 className="text-sm font-semibold">
                  Danh sách điểm danh buổi {selectedSession}
                </h2>
                <p className="text-xs text-muted-foreground">
                  Sinh viên nghỉ quá {ABSENT_LIMIT} buổi sẽ chuyển trạng thái môn học thành học lại.
                </p>
              </div>
              <div className="relative">
                <Search className="pointer-events-none absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  className="pl-8 sm:w-80"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Tìm mã SV hoặc họ tên"
                />
              </div>
            </div>

            <div className="overflow-x-auto">
              <Table className="min-w-[900px]">
                <TableHeader>
                  <TableRow className="bg-muted/40 hover:bg-muted/40">
                    <TableHead className="w-32 font-semibold">Mã SV</TableHead>
                    <TableHead className="font-semibold">Họ tên</TableHead>
                    <TableHead className="w-36 text-center font-semibold">Trạng thái môn</TableHead>
                    <TableHead className="w-24 text-center font-semibold">Đã nghỉ</TableHead>
                    <TableHead className="w-[360px] text-center font-semibold">
                      Điểm danh buổi này
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredRows.map((row) => {
                    const studentSessions = attendance[row.enrollmentId] ?? {};
                    const status = studentSessions[selectedSession] ?? "UNMARKED";
                    const absentCount = countAbsences(studentSessions);
                    const backendStatus = row.courseStatus;
                    const isBanned = checkIsBanned(row, studentSessions);

                    const courseStatusLabel: Record<string, string> = {
                      IN_PROGRESS: "Đang học",
                      PASSED: "Qua môn",
                      BANNED_FROM_EXAM: "Cấm thi",
                      REPEAT_COURSE: "Học lại",
                      RETAKE_EXAM: "Thi lại",
                    };
                    const statusLabel = backendStatus
                      ? (courseStatusLabel[backendStatus] ?? backendStatus)
                      : isBanned
                        ? "Cấm thi"
                        : "Đang học";

                    return (
                      <TableRow key={row.enrollmentId}>
                        <TableCell className="font-mono text-xs font-semibold">
                          {row.studentCode}
                        </TableCell>
                        <TableCell>
                          <div className="font-medium">{row.fullName}</div>
                          <div className="text-xs text-muted-foreground">
                            {row.className ?? "-"}
                          </div>
                        </TableCell>
                        <TableCell className="text-center">
                          <Badge
                            variant={isBanned ? "destructive" : "outline"}
                            className={cn(
                              !isBanned &&
                                backendStatus === "PASSED" &&
                                "border-emerald-200 text-emerald-700",
                              !isBanned &&
                                backendStatus === "RETAKE_EXAM" &&
                                "border-amber-200 text-amber-700",
                            )}
                          >
                            {statusLabel}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-center">
                          <span
                            className={cn(
                              "tabular-nums",
                              isBanned && "font-semibold text-destructive",
                            )}
                          >
                            {absentCount}/{ABSENT_LIMIT}
                          </span>
                        </TableCell>
                        <TableCell>
                          <AttendanceControls
                            value={status}
                            onChange={(nextStatus) =>
                              setStudentStatus(row.enrollmentId, nextStatus)
                            }
                            disabled={isSessionLocked || isBanned}
                          />
                        </TableCell>
                      </TableRow>
                    );
                  })}
                  {!rosterQuery.isLoading && filteredRows.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={5} className="h-28 text-center text-muted-foreground">
                        Không có sinh viên phù hợp.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </div>

            {rosterQuery.isLoading && (
              <div className="border-t p-4 text-sm text-muted-foreground">
                Đang tải danh sách sinh viên...
              </div>
            )}
            {rosterQuery.isError && (
              <div className="border-t p-4 text-sm text-destructive">
                {rosterQuery.error instanceof Error
                  ? rosterQuery.error.message
                  : "Không tải được danh sách sinh viên"}
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}

function AttendanceControls({
  value,
  onChange,
  disabled,
}: {
  value: AttendanceStatus;
  onChange: (status: AttendanceStatus) => void;
  disabled?: boolean;
}) {
  return (
    <div className="grid grid-cols-4 gap-2">
      {(["PRESENT", "LATE", "ABSENT"] as const).map((status) => {
        const meta = statusMeta[status];
        const active = value === status;
        return (
          <Button
            key={status}
            type="button"
            variant="outline"
            disabled={disabled}
            className={cn(
              "h-9 justify-center border text-xs font-semibold",
              active ? meta.className : "bg-background text-muted-foreground",
            )}
            onClick={() => onChange(status)}
          >
            {meta.label}
          </Button>
        );
      })}
    </div>
  );
}

function SessionTimeline({
  attendance,
  nextAllowedSession,
  rosterRows,
  selectedSession,
  sessionsPerWeek,
  lockedSessions,
  onSelect,
}: {
  attendance: AttendanceBook;
  nextAllowedSession: number;
  rosterRows: TeacherRosterRow[];
  selectedSession: number;
  sessionsPerWeek: number;
  lockedSessions: Set<number>;
  onSelect: (session: number) => void;
}) {
  return (
    <div className="mt-4 overflow-x-auto pb-1">
      <div
        className="grid min-w-[860px] gap-2"
        style={{ gridTemplateColumns: `repeat(${WEEKS_PER_COURSE}, minmax(0, 1fr))` }}
      >
        {Array.from({ length: WEEKS_PER_COURSE }, (_, weekIndex) => {
          const week = weekIndex + 1;
          return (
            <div key={week} className="rounded-lg border bg-background p-2">
              <div className="mb-2 text-center text-[11px] font-semibold text-muted-foreground">
                Tuần {week}
              </div>
              <div className="grid gap-1">
                {Array.from({ length: sessionsPerWeek }, (_, slotIndex) => {
                  const session = weekIndex * sessionsPerWeek + slotIndex + 1;
                  const state = getSessionState(attendance, rosterRows, session);
                  const isLocked = lockedSessions.has(session);
                  const locked = !isLocked && session > nextAllowedSession;
                  return (
                    <Button
                      key={session}
                      type="button"
                      variant={selectedSession === session ? "default" : "outline"}
                      className={cn(
                        "h-8 px-0 text-[11px]",
                        state === "done" &&
                          selectedSession !== session &&
                          "border-emerald-200 bg-emerald-50 text-emerald-700",
                        isLocked &&
                          selectedSession !== session &&
                          "border-emerald-300 bg-emerald-100 text-emerald-800",
                        state === "partial" &&
                          selectedSession !== session &&
                          "border-amber-200 bg-amber-50 text-amber-700",
                        locked && "opacity-45",
                      )}
                      onClick={() => onSelect(session)}
                    >
                      B{session}
                    </Button>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
      <div className="mt-3 flex flex-wrap gap-3 text-xs text-muted-foreground">
        <span className="inline-flex items-center gap-1">
          <Check className="h-3.5 w-3.5 text-emerald-600" /> Đã xong
        </span>
        <span className="inline-flex items-center gap-1">
          <Clock className="h-3.5 w-3.5 text-amber-600" /> Đang làm
        </span>
        <span className="inline-flex items-center gap-1">
          <AlertTriangle className="h-3.5 w-3.5 text-muted-foreground" /> Chưa mở
        </span>
      </div>
    </div>
  );
}

function countSession(attendance: AttendanceBook, rows: TeacherRosterRow[], session: number) {
  const counts: Record<AttendanceStatus, number> = {
    PRESENT: 0,
    LATE: 0,
    ABSENT: 0,
    UNMARKED: 0,
  };

  rows.forEach((row) => {
    const studentSessions = attendance[row.enrollmentId] ?? {};
    if (checkIsBanned(row, studentSessions)) return;

    const status = studentSessions[session] ?? "UNMARKED";
    counts[status] += 1;
  });

  return counts;
}

function countAbsences(studentSessions: Record<number, AttendanceStatus>) {
  return Object.values(studentSessions).filter((status) => status === "ABSENT").length;
}

function getSessionState(attendance: AttendanceBook, rows: TeacherRosterRow[], session: number) {
  const counts = countSession(attendance, rows, session);
  if (rows.length === 0 || counts.UNMARKED === rows.length) return "empty";
  if (counts.UNMARKED === 0) return "done";
  return "partial";
}

function getNextAllowedSession(
  lockedSessions: Set<number>,
  totalSessions: number,
) {
  for (let i = 1; i <= totalSessions; i++) {
    if (!lockedSessions.has(i)) {
      return i;
    }
  }
  return totalSessions;
}
