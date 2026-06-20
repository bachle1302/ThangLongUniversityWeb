import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import {
  ArrowLeft,
  Check,
  Download,
  Plus,
  Save,
  Search,
  Users,
  AlertTriangle,
  AlertCircle,
  CheckCircle,
  Loader2,
} from "lucide-react";
import { toast } from "sonner";
import { adminApi } from "@/lib/api/admin";
import type {
  ExamSeatAssignmentResponse,
  ExamSessionResponse,
  ExamConflictResponse,
  ExamCandidateResponse,
} from "@/lib/api/types";
import { triggerBrowserDownload } from "@/lib/api/client";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";

interface Props {
  semesterId: number;
}

const EMPTY_ARRAY: never[] = [];

export function ExamSchedulesTab({ semesterId }: Props) {
  const queryClient = useQueryClient();
  const [courseId, setCourseId] = useState("");
  const examType = "NORMAL" as const;
  const [candidateSelection, setCandidateSelection] = useState<"ALL" | "NORMAL_ONLY" | "RETAKE_ONLY">("ALL");
  const [examAt, setExamAt] = useState("");
  const [roomIds, setRoomIds] = useState<number[]>([]);
  const [roomProctors, setRoomProctors] = useState<Record<number, number>>({});
  const [search, setSearch] = useState("");
  const [selectedSession, setSelectedSession] = useState<ExamSessionResponse | null>(null);

  const [isScheduling, setIsScheduling] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);

  // States for student conflicts
  const [conflicts, setConflicts] = useState<ExamConflictResponse[]>([]);
  const [isValidating, setIsValidating] = useState(false);

  // Advanced Exam Scheduling States
  const [allocationMethod, setAllocationMethod] = useState<"SEQUENTIAL" | "BALANCED">("SEQUENTIAL");
  const [candidates, setCandidates] = useState<ExamCandidateResponse[]>([]);
  const [isLoadingCandidates, setIsLoadingCandidates] = useState(false);
  const [selectedRoomPreview, setSelectedRoomPreview] = useState<{
    roomName: string;
    students: ExamCandidateResponse[];
  } | null>(null);
  const [ignoreConflicts, setIgnoreConflicts] = useState(false);

  const sessionsQuery = useQuery({
    queryKey: ["admin", "exam-sessions", semesterId],
    queryFn: () => adminApi.listExamSessions(semesterId),
  });
  const coursesQuery = useQuery({
    queryKey: ["admin", "courses"],
    queryFn: adminApi.listCourses,
    staleTime: 300_000,
  });
  const roomsQuery = useQuery({
    queryKey: ["admin", "rooms"],
    queryFn: adminApi.listRooms,
    staleTime: 3_600_000,
  });
  const classSectionsQuery = useQuery({
    queryKey: ["admin", "class-sections", semesterId],
    queryFn: () => adminApi.listClassSectionsBySemester(semesterId),
  });
  const teachersQuery = useQuery({
    queryKey: ["admin", "teachers"],
    queryFn: adminApi.listTeachers,
    staleTime: 300_000,
  });

  const sessions = sessionsQuery.data ?? EMPTY_ARRAY;
  const courses = coursesQuery.data ?? EMPTY_ARRAY;
  const rooms = roomsQuery.data ?? EMPTY_ARRAY;
  const classSections = classSectionsQuery.data ?? EMPTY_ARRAY;
  const teachers = teachersQuery.data ?? EMPTY_ARRAY;

  const filteredSessions = sessions.filter((session) => {
    const keyword = search.trim().toLowerCase();
    return (
      !keyword ||
      session.courseCode.toLowerCase().includes(keyword) ||
      session.courseName.toLowerCase().includes(keyword) ||
      session.rooms.some((room) => room.roomName.toLowerCase().includes(keyword))
    );
  });

  const totalRoomCapacity = useMemo(
    () =>
      rooms
        .filter((room) => roomIds.includes(room.id))
        .reduce((sum, room) => sum + (room.capacity ?? 0), 0),
    [rooms, roomIds],
  );

  // Candidate count must match the exam-candidate API so the UI reflects the real seat allocation.
  const totalCandidates = useMemo(() => {
    if (!courseId) return 0;
    return candidates.length;
  }, [courseId, candidates]);
  const hasNoCandidates = Boolean(courseId) && !isLoadingCandidates && totalCandidates === 0;

  // Simulate seat allocation with room preview details and method support
  const simulatedAllocation = useMemo(() => {
    if (!courseId || roomIds.length === 0)
      return { allocation: [], remainingCandidates: totalCandidates };

    const selectedRooms = rooms
      .filter((r) => roomIds.includes(r.id))
      .sort((a, b) => a.name.localeCompare(b.name));

    const totalCapacity = selectedRooms.reduce((sum, r) => sum + (r.capacity ?? 0), 0);
    let remainingCandidates = totalCandidates;
    const allocation: Array<{
      roomId: number;
      roomName: string;
      capacity: number;
      assigned: number;
      percentage: number;
      students: ExamCandidateResponse[];
    }> = [];

    const targetCounts = new Array(selectedRooms.length).fill(0);
    if (allocationMethod === "BALANCED") {
      let allocated = 0;
      for (let i = 0; i < selectedRooms.length; i++) {
        const cap = selectedRooms[i].capacity ?? 0;
        targetCounts[i] = Math.floor((totalCandidates * cap) / (totalCapacity || 1));
        allocated += targetCounts[i];
      }
      let remaining = totalCandidates - allocated;
      let i = 0;
      while (remaining > 0) {
        const cap = selectedRooms[i].capacity ?? 0;
        if (targetCounts[i] < cap) {
          targetCounts[i]++;
          remaining--;
        }
        i = (i + 1) % selectedRooms.length;
      }
    } else {
      // SEQUENTIAL
      let allocated = 0;
      for (let i = 0; i < selectedRooms.length; i++) {
        const cap = selectedRooms[i].capacity ?? 0;
        const toAllocate = Math.min(totalCandidates - allocated, cap);
        targetCounts[i] = toAllocate;
        allocated += toAllocate;
      }
    }

    let candidateIndex = 0;
    for (let i = 0; i < selectedRooms.length; i++) {
      const room = selectedRooms[i];
      const capacity = room.capacity ?? 0;
      const assigned = targetCounts[i];
      const assignedStudents = candidates.slice(candidateIndex, candidateIndex + assigned);
      candidateIndex += assigned;

      allocation.push({
        roomId: room.id,
        roomName: room.name,
        capacity,
        assigned,
        percentage: capacity > 0 ? (assigned / capacity) * 100 : 0,
        students: assignedStudents,
      });
      remainingCandidates -= assigned;
    }

    return {
      allocation,
      remainingCandidates,
    };
  }, [courseId, roomIds, rooms, totalCandidates, allocationMethod, candidates]);

  const saveMutation = useMutation({
    mutationFn: () =>
      adminApi.saveExamSession(semesterId, {
        courseId: Number(courseId),
        examType,
        examAt: `${examAt}:00`,
        roomIds,
        proctorIds: roomIds.map((id) => roomProctors[id] ?? null),
        allocationMethod,
        candidateSelection,
      }),
    onSuccess: (response) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "exam-sessions", semesterId] });
      queryClient.invalidateQueries({ queryKey: ["admin", "exam-registrations", semesterId] });
      queryClient.invalidateQueries({ queryKey: ["admin", "semester-summary", semesterId] });
      const parts = ["Đã lưu và chia phòng thi thành công"];
      if (response.assignedRetakeCount != null && response.assignedRetakeCount > 0) {
        parts.push(`Đã gán ${response.assignedRetakeCount} SV thi lại/nâng vào lớp chấm điểm`);
      }
      if (response.virtualClassCode) {
        parts.push(`Lớp ảo: ${response.virtualClassCode}`);
      }
      if (response.assignmentWarnings?.length) {
        response.assignmentWarnings.forEach((w) => toast.warning(w));
      }
      toast.success(parts.join(". "));
      handleCancel();
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Không lưu được lịch thi"),
  });

  const checkConflicts = async (timeValue: string, selection?: "ALL" | "NORMAL_ONLY" | "RETAKE_ONLY") => {
    const activeSelection = selection ?? candidateSelection;
    if (!courseId || !timeValue) {
      setConflicts([]);
      return;
    }
    setIsValidating(true);
    try {
      const res = await adminApi.validateExamConflicts(semesterId, {
        courseId: Number(courseId),
        examType,
        examAt: `${timeValue}:00`,
        roomIds: [],
        candidateSelection: activeSelection,
      });
      setConflicts(res);
    } catch (err) {
      console.error("Lỗi khi kiểm tra trùng lịch thi:", err);
    } finally {
      setIsValidating(false);
    }
  };

  const [isStep1CandidatesOpen, setIsStep1CandidatesOpen] = useState(false);

  const handleCourseChange = (val: string) => {
    setCourseId(val);
    void loadCandidates(val);
  };

  const handleCandidateSelectionChange = (val: "ALL" | "NORMAL_ONLY" | "RETAKE_ONLY") => {
    setCandidateSelection(val);
    if (courseId) {
      void loadCandidates(courseId, val);
    }
    if (examAt) {
      void checkConflicts(examAt, val);
    }
  };

  const loadCandidates = async (cId: string, selection?: "ALL" | "NORMAL_ONLY" | "RETAKE_ONLY") => {
    const activeSelection = selection ?? candidateSelection;
    if (!cId) {
      setCandidates([]);
      return;
    }
    setCandidates([]);
    setIsLoadingCandidates(true);
    try {
      const res = await adminApi.listExamCandidates(semesterId, Number(cId), activeSelection);
      setCandidates(res);
    } catch (err) {
      console.error("Lỗi khi tải danh sách thí sinh:", err);
      toast.error("Không tải được danh sách sinh viên dự thi");
    } finally {
      setIsLoadingCandidates(false);
    }
  };

  const handleCancel = () => {
    setCourseId("");
    setExamAt("");
    setRoomIds([]);
    setRoomProctors({});
    setConflicts([]);
    setCandidates([]);
    setCandidateSelection("ALL");
    setAllocationMethod("SEQUENTIAL");
    setIgnoreConflicts(false);
    setSelectedRoomPreview(null);
    setIsScheduling(false);
    setCurrentStep(1);
  };

  const handleNextStep = () => {
    if (currentStep === 1) {
      setCurrentStep(2);
      if (examAt) {
        void checkConflicts(examAt);
      }
    } else if (currentStep === 2) {
      setCurrentStep(3);
    }
  };

  const hasCapacityShortage = totalRoomCapacity < totalCandidates;

  return (
    <div className="space-y-4">
      {isScheduling ? (
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <Button
              variant="ghost"
              className="gap-1.5 pl-0 hover:bg-transparent hover:text-primary"
              onClick={handleCancel}
            >
              <ArrowLeft className="h-4 w-4" />
              Quay lại danh sách
            </Button>
            <h3 className="text-base font-semibold">Xếp lịch thi học phần</h3>
          </div>

          {/* Stepper Progress Bar */}
          <div className="flex items-center justify-center gap-2 max-w-lg mx-auto py-2">
            <div className="flex items-center gap-2">
              <div
                className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold border transition-all ${
                  currentStep >= 1
                    ? "bg-emerald-500 text-white border-emerald-500 shadow-sm shadow-emerald-500/20"
                    : "bg-muted text-muted-foreground border-border"
                }`}
              >
                {currentStep > 1 ? <Check className="h-4 w-4" /> : "1"}
              </div>
              <span
                className={`text-xs font-semibold ${currentStep >= 1 ? "text-foreground" : "text-muted-foreground"}`}
              >
                Môn & Loại thi
              </span>
            </div>
            <div
              className={`h-[2px] flex-1 max-w-16 transition-colors ${currentStep >= 2 ? "bg-emerald-500" : "bg-border"}`}
            />
            <div className="flex items-center gap-2">
              <div
                className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold border transition-all ${
                  currentStep >= 2
                    ? "bg-emerald-500 text-white border-emerald-500 shadow-sm shadow-emerald-500/20"
                    : "bg-muted text-muted-foreground border-border"
                }`}
              >
                {currentStep > 2 ? <Check className="h-4 w-4" /> : "2"}
              </div>
              <span
                className={`text-xs font-semibold ${currentStep >= 2 ? "text-foreground" : "text-muted-foreground"}`}
              >
                Thời gian & Phòng
              </span>
            </div>
            <div
              className={`h-[2px] flex-1 max-w-16 transition-colors ${currentStep >= 3 ? "bg-emerald-500" : "bg-border"}`}
            />
            <div className="flex items-center gap-2">
              <div
                className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold border transition-all ${
                  currentStep >= 3
                    ? "bg-emerald-500 text-white border-emerald-500 shadow-sm shadow-emerald-500/20"
                    : "bg-muted text-muted-foreground border-border"
                }`}
              >
                3
              </div>
              <span
                className={`text-xs font-semibold ${currentStep >= 3 ? "text-foreground" : "text-muted-foreground"}`}
              >
                Xác nhận
              </span>
            </div>
          </div>

          {/* Form Card for Current Step */}
          <div className="rounded-xl border bg-card text-card-foreground shadow-sm max-w-xl mx-auto overflow-hidden">
            <div className="p-6 space-y-4">
              {currentStep === 1 && (
                <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                      Chọn môn thi
                    </label>
                    <Select value={courseId} onValueChange={handleCourseChange}>
                      <SelectTrigger className="w-full h-10">
                        <SelectValue placeholder="Chọn môn học phần..." />
                      </SelectTrigger>
                      <SelectContent>
                        {courses.map((course) => (
                          <SelectItem key={course.id} value={String(course.id)}>
                            {course.code} - {course.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                      Đối tượng dự thi
                    </label>
                    <Select
                      value={candidateSelection}
                      onValueChange={(val: "ALL" | "NORMAL_ONLY" | "RETAKE_ONLY") =>
                        handleCandidateSelectionChange(val)
                      }
                    >
                      <SelectTrigger className="w-full h-10">
                        <SelectValue placeholder="Chọn đối tượng dự thi..." />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ALL">
                          Tất cả (Học chính thức + Thi lại, nâng điểm)
                        </SelectItem>
                        <SelectItem value="NORMAL_ONLY">
                          Chỉ học sinh học chính thức (Thi lần đầu)
                        </SelectItem>
                        <SelectItem value="RETAKE_ONLY">
                          Chỉ học sinh thi lại, thi nâng điểm
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {candidateSelection === "RETAKE_ONLY" && (
                    <div className="flex items-start gap-1.5 rounded-md border border-blue-200 bg-blue-50 px-3 py-2 text-xs text-blue-800">
                      <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
                      <span>
                        Với ca thi riêng thi lại/nâng: hãy chọn giám thị cho từng phòng. Giám thị phòng đầu tiên sẽ là giảng viên chấm điểm (lớp học phần ảo).
                      </span>
                    </div>
                  )}

                  {courseId && (
                    <div className="rounded-lg border bg-muted/10 p-3.5 text-xs space-y-2 text-muted-foreground">
                      <div className="flex justify-between items-center">
                        <span>Số lớp học phần trong kỳ:</span>
                        <span className="font-semibold text-foreground">
                          {classSections.filter((cs) => String(cs.courseId) === courseId).length}{" "}
                          lớp
                        </span>
                      </div>
                      <div className="flex justify-between items-center">
                        <div className="flex items-center gap-2">
                          <span>Tổng số sinh viên dự thi dự kiến:</span>
                          {isLoadingCandidates && (
                            <Loader2 className="h-3.5 w-3.5 animate-spin text-muted-foreground" />
                          )}
                        </div>
                        <span className="font-bold text-foreground text-sm">
                          {totalCandidates} sinh viên
                        </span>
                      </div>
                      {hasNoCandidates && (
                        <div className="flex items-start gap-1.5 rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-800">
                          <AlertCircle className="mt-0.5 h-4 w-4 shrink-0 text-amber-600" />
                          <span>
                            Không có sinh viên đủ điều kiện dự thi, thi lại hoặc thi nâng điểm cho
                            môn này.
                          </span>
                        </div>
                      )}
                      <div className="flex justify-end pt-2 border-t border-muted mt-1.5">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          className="h-8 gap-1.5 text-xs bg-background hover:bg-muted/30"
                          disabled={isLoadingCandidates || candidates.length === 0}
                          onClick={() => setIsStep1CandidatesOpen(true)}
                        >
                          {isLoadingCandidates ? (
                            <>
                              <Loader2 className="h-3.5 w-3.5 animate-spin text-muted-foreground" />
                              Đang tải danh sách...
                            </>
                          ) : (
                            <>
                              <Users className="h-3.5 w-3.5 text-muted-foreground" />
                              Xem danh sách sinh viên
                            </>
                          )}
                        </Button>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {currentStep === 2 && (
                <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                      Thời gian thi
                    </label>
                    <div className="relative">
                      <Input
                        type="datetime-local"
                        value={examAt}
                        onChange={(event) => {
                          const val = event.target.value;
                          setExamAt(val);
                          setIgnoreConflicts(false);
                          void checkConflicts(val);
                        }}
                        className="w-full h-10"
                      />
                      {isValidating && (
                        <div className="absolute right-3 top-3">
                          <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                      Phương thức phân bổ phòng thi
                    </label>
                    <Select
                      value={allocationMethod}
                      onValueChange={(value) =>
                        setAllocationMethod(value as "SEQUENTIAL" | "BALANCED")
                      }
                    >
                      <SelectTrigger className="w-full h-10">
                        <SelectValue placeholder="Chọn phương thức phân bổ..." />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="SEQUENTIAL">Phân bổ dồn phòng (Sequential)</SelectItem>
                        <SelectItem value="BALANCED">Phân bổ chia đều (Balanced)</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {/* Exam conflicts notification */}
                  {!isValidating && conflicts.length > 0 && (
                    <div className="rounded-lg border border-amber-200 bg-amber-50 p-4 space-y-2 text-sm text-amber-800 animate-in fade-in duration-200">
                      <div className="flex items-start gap-2 font-semibold text-amber-900">
                        <AlertTriangle className="h-4 w-4 text-amber-600 mt-0.5 shrink-0" />
                        <span>Phát hiện {conflicts.length} sinh viên bị trùng giờ thi!</span>
                      </div>
                      <p className="text-xs text-amber-700">
                        Những sinh viên dưới đây đã có lịch thi môn khác vào cùng thời điểm này:
                      </p>
                      <div className="border border-amber-200 rounded-md overflow-hidden bg-background max-h-32 overflow-y-auto mt-2">
                        <table className="w-full text-[11px] text-left text-foreground">
                          <thead className="bg-muted text-muted-foreground">
                            <tr>
                              <th className="p-1.5 font-medium">MSSV</th>
                              <th className="p-1.5 font-medium">Họ tên</th>
                              <th className="p-1.5 font-medium">Môn bị trùng</th>
                            </tr>
                          </thead>
                          <tbody>
                            {conflicts.map((c, idx) => (
                              <tr key={idx} className="border-t border-muted">
                                <td className="p-1.5 font-mono">{c.studentCode}</td>
                                <td className="p-1.5">{c.studentName}</td>
                                <td className="p-1.5 text-muted-foreground truncate max-w-[150px]">
                                  {c.conflictingCourseCode} - {c.conflictingCourseName}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                      <div className="flex items-center gap-2 mt-3 pt-2 border-t border-amber-200/50">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          className={`h-8 text-xs ${
                            ignoreConflicts
                              ? "bg-emerald-50 text-emerald-800 border-emerald-200 hover:bg-emerald-100"
                              : "bg-amber-100 text-amber-900 border-amber-300 hover:bg-amber-200"
                          }`}
                          onClick={() => {
                            setIgnoreConflicts(!ignoreConflicts);
                            if (!ignoreConflicts) {
                              toast.warning("Đã xác nhận bỏ qua xung đột trùng lịch.");
                            }
                          }}
                        >
                          {ignoreConflicts ? "✓ Đã bỏ qua xung đột" : "Bỏ qua & Vẫn xếp"}
                        </Button>
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="text-amber-800 hover:bg-amber-200/40 h-8 text-xs"
                          onClick={() => {
                            setExamAt("");
                            setConflicts([]);
                            setIgnoreConflicts(false);
                          }}
                        >
                          Chọn lại giờ thi
                        </Button>
                      </div>
                    </div>
                  )}

                  {!isValidating && conflicts.length === 0 && examAt && (
                    <div className="flex items-center gap-1.5 text-xs text-emerald-600 bg-emerald-50 border border-emerald-200 rounded-lg p-3">
                      <CheckCircle className="h-4 w-4 shrink-0" />
                      <span>Không có xung đột trùng lịch thi cho sinh viên.</span>
                    </div>
                  )}

                  <div className="space-y-1.5">
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
                      Chọn phòng thi
                    </label>
                    <div className="grid gap-2 sm:grid-cols-2 max-h-60 overflow-y-auto pr-1 border rounded-lg p-3 bg-muted/10">
                      {rooms.map((room) => {
                        const checked = roomIds.includes(room.id);
                        return (
                          <div
                            key={room.id}
                            className={`flex flex-col gap-2 rounded-md border p-2 text-sm transition-colors ${
                              checked ? "border-emerald-500 bg-emerald-500/5" : "hover:bg-muted/30"
                            }`}
                          >
                            <div className="flex items-center justify-between gap-3">
                              <span className="truncate">
                                <span className="font-medium text-foreground">{room.name}</span>
                                <span className="ml-2 text-xs text-muted-foreground">
                                  {room.capacity} chỗ
                                </span>
                              </span>
                              <Checkbox
                                checked={checked}
                                onCheckedChange={(value) => {
                                  setRoomIds((current) =>
                                    value
                                      ? [...current, room.id]
                                      : current.filter((id) => id !== room.id),
                                  );
                                  if (!value) {
                                    setRoomProctors((prev) => {
                                      const next = { ...prev };
                                      delete next[room.id];
                                      return next;
                                    });
                                  }
                                }}
                              />
                            </div>
                            {checked && (
                              <div className="flex flex-col gap-1 border-t pt-1.5 mt-0.5">
                                <span className="text-[10px] font-semibold text-muted-foreground uppercase tracking-wider">
                                  Cán bộ coi thi
                                </span>
                                <Select
                                  value={String(roomProctors[room.id] ?? "")}
                                  onValueChange={(val) =>
                                    setRoomProctors((prev) => ({
                                      ...prev,
                                      [room.id]: Number(val),
                                    }))
                                  }
                                >
                                  <SelectTrigger className="h-8 text-xs bg-background">
                                    <SelectValue placeholder="Chọn cán bộ..." />
                                  </SelectTrigger>
                                  <SelectContent>
                                    {teachers.map((t) => (
                                      <SelectItem key={t.id} value={String(t.id)}>
                                        {t.fullName} ({t.teacherCode})
                                      </SelectItem>
                                    ))}
                                  </SelectContent>
                                </Select>
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>

                    <div className="flex justify-between items-center text-xs mt-2">
                      <span className="text-muted-foreground">
                        Sinh viên dự thi:{" "}
                        <span className="font-semibold text-foreground">{totalCandidates}</span>
                      </span>
                      <span className="text-muted-foreground">
                        Tổng sức chứa đã chọn:{" "}
                        <span
                          className={`font-semibold ${hasCapacityShortage ? "text-amber-600 font-bold" : "text-foreground"}`}
                        >
                          {totalRoomCapacity} / {totalCandidates}
                        </span>
                      </span>
                    </div>

                    {hasCapacityShortage && roomIds.length > 0 && (
                      <div className="flex items-start gap-1.5 text-xs text-amber-700 bg-amber-50 border border-amber-200 rounded-lg p-3 mt-2">
                        <AlertCircle className="h-4 w-4 shrink-0 mt-0.5 text-amber-600" />
                        <span>
                          Sức chứa phòng thi đã chọn ({totalRoomCapacity} chỗ) không đủ cho số sinh
                          viên dự thi ({totalCandidates}). Vui lòng chọn thêm phòng.
                        </span>
                      </div>
                    )}
                  </div>
                </div>
              )}

              {currentStep === 3 && (
                <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-300">
                  <div className="text-sm font-semibold text-emerald-600">
                    Xác nhận thông tin lịch thi
                  </div>

                  <div className="grid gap-3 rounded-lg border bg-muted/20 p-4 text-sm">
                    <div className="grid grid-cols-[100px_1fr] items-start">
                      <span className="text-muted-foreground font-medium">Môn thi:</span>
                      <span className="font-semibold text-foreground">
                        {courses.find((c) => String(c.id) === courseId)?.code} -{" "}
                        {courses.find((c) => String(c.id) === courseId)?.name}
                      </span>
                    </div>
                    <div className="grid grid-cols-[100px_1fr]">
                      <span className="text-muted-foreground font-medium">Loại thi:</span>
                      <span className="font-semibold text-foreground">
                        {examType === "NORMAL"
                          ? "Thi kết thúc"
                          : examType === "RETAKE"
                            ? "Thi lại"
                            : "Nâng điểm"}
                      </span>
                    </div>
                    <div className="grid grid-cols-[100px_1fr]">
                      <span className="text-muted-foreground font-medium">Thời gian:</span>
                      <span className="font-semibold text-foreground">
                        {formatDateTime(examAt)}
                      </span>
                    </div>
                    <div className="grid grid-cols-[100px_1fr] border-t pt-2 mt-2">
                      <span className="text-muted-foreground font-medium">Sinh viên:</span>
                      <span className="font-semibold text-foreground">
                        {totalCandidates} thí sinh
                      </span>
                    </div>
                  </div>

                  {/* Seat Allocation Preview */}
                  <div className="space-y-2.5">
                    <label className="text-xs font-semibold text-muted-foreground uppercase tracking-wider block">
                      Bản xem trước phân bổ phòng thi (
                      {allocationMethod === "BALANCED" ? "Chia đều" : "Dồn phòng"})
                    </label>
                    <div className="border rounded-lg p-4 space-y-4 bg-muted/5 max-h-60 overflow-y-auto">
                      {simulatedAllocation.allocation.map((item, idx) => (
                        <div
                          key={idx}
                          className="space-y-1.5 pb-2 border-b last:border-b-0 border-muted"
                        >
                          <div className="flex justify-between items-center text-xs font-medium">
                            <span className="text-foreground font-semibold">
                              Phòng {item.roomName}
                            </span>
                            <div className="flex items-center gap-2">
                              <span className="text-muted-foreground">
                                {item.assigned} / {item.capacity} thí sinh
                              </span>
                              <Button
                                type="button"
                                variant="link"
                                size="sm"
                                className="h-auto p-0 text-xs text-primary font-normal hover:underline"
                                disabled={item.assigned === 0}
                                onClick={() =>
                                  setSelectedRoomPreview({
                                    roomName: item.roomName,
                                    students: item.students,
                                  })
                                }
                              >
                                Xem chi tiết
                              </Button>
                            </div>
                          </div>
                          <div className="w-full bg-muted rounded-full h-2">
                            <div
                              className="bg-emerald-500 h-2 rounded-full transition-all duration-500"
                              style={{ width: `${item.percentage}%` }}
                            />
                          </div>
                        </div>
                      ))}

                      {simulatedAllocation.remainingCandidates > 0 && (
                        <div className="text-xs text-amber-600 bg-amber-50 border border-amber-200 rounded-lg p-2.5 flex items-center gap-1.5">
                          <AlertTriangle className="h-4 w-4 shrink-0 text-amber-500" />
                          <span>
                            Thiếu chỗ cho {simulatedAllocation.remainingCandidates} thí sinh do
                            phòng thi đã chọn không đủ sức chứa!
                          </span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              )}
            </div>

            <div className="flex items-center justify-between border-t px-6 py-4 bg-muted/20">
              {currentStep > 1 ? (
                <Button variant="outline" size="sm" onClick={() => setCurrentStep((s) => s - 1)}>
                  Quay lại
                </Button>
              ) : (
                <Button variant="outline" size="sm" onClick={handleCancel}>
                  Hủy bỏ
                </Button>
              )}

              {currentStep < 3 ? (
                <Button
                  size="sm"
                  disabled={
                    (currentStep === 1 && (!courseId || isLoadingCandidates || hasNoCandidates)) ||
                    (currentStep === 2 &&
                      (!examAt ||
                        roomIds.length === 0 ||
                        hasCapacityShortage ||
                        (conflicts.length > 0 && !ignoreConflicts)))
                  }
                  onClick={handleNextStep}
                >
                  Tiếp theo
                </Button>
              ) : (
                <Button
                  size="sm"
                  className="bg-emerald-600 hover:bg-emerald-700 text-white"
                  disabled={saveMutation.isPending || simulatedAllocation.remainingCandidates > 0}
                  onClick={() => saveMutation.mutate()}
                >
                  {saveMutation.isPending ? "Đang xử lý..." : "Lưu và chia phòng"}
                </Button>
              )}
            </div>
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="relative w-full sm:w-80">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Tìm môn hoặc phòng thi..."
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                className="h-9 pl-8"
              />
            </div>
            <div className="flex items-center gap-3">
              <span className="text-xs text-muted-foreground hidden md:inline text-right">
                Tổng cộng: {filteredSessions.length}/{sessions.length} lịch thi
              </span>
              <Button
                size="sm"
                className="bg-primary hover:bg-primary/90"
                onClick={() => setIsScheduling(true)}
              >
                <Plus className="mr-1.5 h-4 w-4" />
                Xếp lịch thi
              </Button>
            </div>
          </div>

          {sessionsQuery.isLoading ? (
            <Skeleton className="h-64 w-full" />
          ) : (
            <div className="overflow-hidden rounded-lg border">
              <table className="w-full text-sm">
                <thead className="bg-muted/50">
                  <tr>
                    <th className="p-3 text-left font-medium">Môn thi</th>
                    <th className="p-3 text-left font-medium">Thời gian</th>
                    <th className="p-3 text-left font-medium">Phòng</th>
                    <th className="p-3 text-left font-medium">Sinh viên</th>
                    <th className="p-3" />
                  </tr>
                </thead>
                <tbody>
                  {filteredSessions.map((session) => (
                    <tr key={session.id} className="border-t hover:bg-muted/30">
                      <td className="p-3">
                        <div className="font-medium text-foreground">{session.courseName}</div>
                        <div className="flex items-center gap-1.5 mt-0.5">
                          <span className="text-xs text-muted-foreground">{session.courseCode}</span>
                          {session.candidateSelection && (
                            <Badge
                              variant="outline"
                              className={
                                session.candidateSelection === "NORMAL_ONLY"
                                  ? "border-blue-200 bg-blue-50/50 text-blue-700 text-[10px] py-0 px-1.5"
                                  : session.candidateSelection === "RETAKE_ONLY"
                                  ? "border-amber-200 bg-amber-50/50 text-amber-700 text-[10px] py-0 px-1.5"
                                  : "border-gray-200 bg-gray-50/50 text-gray-700 text-[10px] py-0 px-1.5"
                              }
                            >
                              {session.candidateSelection === "NORMAL_ONLY"
                                ? "Thi lần đầu"
                                : session.candidateSelection === "RETAKE_ONLY"
                                ? "Thi lại/nâng điểm"
                                : "Trộn tất cả"}
                            </Badge>
                          )}
                        </div>
                      </td>
                      <td className="p-3 text-xs text-muted-foreground">
                        {formatDateTime(session.examAt)}
                      </td>
                      <td className="p-3 text-xs">
                        {session.rooms
                          .map((room) => {
                            const proctorInfo = room.proctorName ? ` - CB coi thi: ${room.proctorName}` : "";
                            return `${room.roomName} (${room.assignedCount}/${room.capacity}${proctorInfo})`;
                          })
                          .join(", ")}
                      </td>
                      <td className="p-3 font-medium">{session.studentCount}</td>
                      <td className="p-3 text-right">
                        <Button
                          variant="outline"
                          size="sm"
                          className="gap-1.5"
                          onClick={() => setSelectedSession(session)}
                        >
                          <Users className="h-3.5 w-3.5" />
                          Danh sách
                        </Button>
                      </td>
                    </tr>
                  ))}
                  {filteredSessions.length === 0 && (
                    <tr>
                      <td colSpan={6} className="p-8 text-center text-muted-foreground">
                        Chưa có lịch thi theo môn
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      <ExamSeatsDialog
        session={selectedSession}
        onOpenChange={(open) => {
          if (!open) setSelectedSession(null);
        }}
      />

      <Dialog open={isStep1CandidatesOpen} onOpenChange={setIsStep1CandidatesOpen}>
        <DialogContent className="max-h-[80vh] max-w-2xl overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Danh sách sinh viên dự kiến dự thi</DialogTitle>
            <DialogDescription>
              Môn học: {courses.find((c) => String(c.id) === courseId)?.code} -{" "}
              {courses.find((c) => String(c.id) === courseId)?.name}
            </DialogDescription>
          </DialogHeader>
          <div className="overflow-hidden rounded-lg border mt-2">
            <table className="w-full text-sm">
              <thead className="bg-muted/50">
                <tr>
                  <th className="p-2 text-left font-medium w-12 text-center">STT</th>
                  <th className="p-2 text-left font-medium">MSSV</th>
                  <th className="p-2 text-left font-medium">Họ tên</th>
                  <th className="p-2 text-left font-medium">Lớp</th>
                  <th className="p-2 text-left font-medium">Loại đăng ký</th>
                </tr>
              </thead>
              <tbody>
                {candidates.map((candidate, index) => (
                  <tr key={candidate.studentId} className="border-t hover:bg-muted/30">
                    <td className="p-2 text-center text-muted-foreground text-xs">{index + 1}</td>
                    <td className="p-2 font-mono text-xs">{candidate.studentCode}</td>
                    <td className="p-2">{candidate.studentName}</td>
                    <td className="p-2 font-mono text-xs text-muted-foreground">
                      {candidate.classCode || "—"}
                    </td>
                    <td className="p-2 text-xs">
                      {candidate.sourceType === "RETAKE" ? (
                        <Badge className="bg-red-100 text-red-800 hover:bg-red-100">Thi lại</Badge>
                      ) : candidate.sourceType === "IMPROVE" ? (
                        <Badge className="bg-blue-100 text-blue-800 hover:bg-blue-100">
                          Nâng điểm
                        </Badge>
                      ) : candidate.sourceType === "REPEAT_COURSE" ? (
                        <Badge className="bg-amber-100 text-amber-800 hover:bg-amber-100">
                          Học lại
                        </Badge>
                      ) : (
                        <Badge variant="outline">Thi kết thúc</Badge>
                      )}
                    </td>
                  </tr>
                ))}
                {candidates.length === 0 && (
                  <tr>
                    <td colSpan={5} className="p-8 text-center text-muted-foreground">
                      Không có sinh viên dự kiến thi môn này
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog
        open={!!selectedRoomPreview}
        onOpenChange={(open) => {
          if (!open) setSelectedRoomPreview(null);
        }}
      >
        <DialogContent className="max-h-[80vh] max-w-2xl overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              Danh sách thí sinh dự kiến - Phòng {selectedRoomPreview?.roomName}
            </DialogTitle>
            <DialogDescription>
              Tổng số: {selectedRoomPreview?.students.length} thí sinh (Phân bổ theo mã sinh viên)
            </DialogDescription>
          </DialogHeader>
          <div className="overflow-hidden rounded-lg border mt-2">
            <table className="w-full text-sm">
              <thead className="bg-muted/50">
                <tr>
                  <th className="p-2 text-left font-medium w-12 text-center">STT</th>
                  <th className="p-2 text-left font-medium">MSSV</th>
                  <th className="p-2 text-left font-medium">Họ tên</th>
                  <th className="p-2 text-left font-medium">Lớp</th>
                  <th className="p-2 text-left font-medium">Nguồn</th>
                </tr>
              </thead>
              <tbody>
                {selectedRoomPreview?.students.map((student, index) => (
                  <tr key={student.studentId} className="border-t hover:bg-muted/30">
                    <td className="p-2 text-center text-muted-foreground text-xs">{index + 1}</td>
                    <td className="p-2 font-mono text-xs">{student.studentCode}</td>
                    <td className="p-2">{student.studentName}</td>
                    <td className="p-2 font-mono text-xs text-muted-foreground">
                      {student.classCode || "—"}
                    </td>
                    <td className="p-2 text-xs">
                      {student.sourceType === "RETAKE" ? (
                        <span className="text-red-600 font-medium">Thi lại</span>
                      ) : student.sourceType === "IMPROVE" ? (
                        <span className="text-blue-600 font-medium">Nâng điểm</span>
                      ) : student.sourceType === "REPEAT_COURSE" ? (
                        <span className="text-amber-600 font-medium">Học lại</span>
                      ) : (
                        <span className="text-muted-foreground">Thi kết thúc</span>
                      )}
                    </td>
                  </tr>
                ))}
                {(!selectedRoomPreview || selectedRoomPreview.students.length === 0) && (
                  <tr>
                    <td colSpan={5} className="p-8 text-center text-muted-foreground">
                      Không có thí sinh phân vào phòng này
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function ExamSeatsDialog({
  session,
  onOpenChange,
}: {
  session: ExamSessionResponse | null;
  onOpenChange: (open: boolean) => void;
}) {
  const queryClient = useQueryClient();
  const seatsQuery = useQuery({
    queryKey: ["admin", "exam-session-seats", session?.id],
    queryFn: () => adminApi.listExamSessionSeats(session?.id ?? 0),
    enabled: !!session,
  });
  const rows = seatsQuery.data ?? [];

  const moveSeatMutation = useMutation({
    mutationFn: (args: { seatId: number; targetRoomAssignmentId: number }) =>
      adminApi.moveExamSeatAssignment(args.seatId, args.targetRoomAssignmentId),
    onSuccess: () => {
      void seatsQuery.refetch();
      queryClient.invalidateQueries({ queryKey: ["admin", "exam-sessions", session?.semesterId] });
      toast.success("Chuyển phòng thi thành công");
    },
    onError: (err) => {
      toast.error(err instanceof Error ? err.message : "Không đổi được phòng thi");
    },
  });

  const handleMoveSeat = (seatId: number, targetRoomAssignmentId: number) => {
    moveSeatMutation.mutate({ seatId, targetRoomAssignmentId });
  };

  return (
    <Dialog open={!!session} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[88vh] max-w-4xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Danh sách phòng thi</DialogTitle>
          <DialogDescription>
            {session
              ? `${session.courseCode} - ${session.courseName} - ${formatDateTime(session.examAt)}`
              : "Danh sách sinh viên thi"}
          </DialogDescription>
          <Button
            variant="outline"
            size="sm"
            className="w-fit gap-2"
            disabled={rows.length === 0 || !session}
            onClick={() => session && exportSeats(session, rows)}
          >
            <Download className="h-4 w-4" />
            Xuất CSV
          </Button>
        </DialogHeader>

        {seatsQuery.isLoading ? (
          <Skeleton className="h-64 w-full" />
        ) : (
          <div className="overflow-hidden rounded-lg border">
            <table className="w-full text-sm">
              <thead className="bg-muted/50">
                <tr>
                  <th className="p-3 text-left font-medium">Phòng hiện tại</th>
                  <th className="p-3 text-left font-medium">MSSV</th>
                  <th className="p-3 text-left font-medium">Họ tên</th>
                  <th className="p-3 text-left font-medium">Lớp</th>
                  <th className="p-3 text-left font-medium">Nguồn</th>
                  <th className="p-3 text-left font-medium">Đổi phòng</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.id} className="border-t hover:bg-muted/30">
                    <td className="p-3 font-medium">{row.roomName}</td>
                    <td className="p-3 font-mono text-xs">{row.studentCode}</td>
                    <td className="p-3">{row.studentName}</td>
                    <td className="p-3 font-mono text-xs text-muted-foreground">{row.classCode || "—"}</td>
                    <td className="p-3">
                      <ExamTypeBadge type={row.sourceType} />
                    </td>
                    <td className="p-3">
                      <Select
                        value={String(row.roomAssignmentId || "")}
                        onValueChange={(val) => handleMoveSeat(row.id, Number(val))}
                        disabled={moveSeatMutation.isPending}
                      >
                        <SelectTrigger className="h-8 w-40 text-xs bg-background">
                          <SelectValue placeholder="Chuyển sang..." />
                        </SelectTrigger>
                        <SelectContent>
                          {session?.rooms.map((room) => (
                            <SelectItem
                              key={room.id}
                              value={String(room.id)}
                              disabled={room.id === row.roomAssignmentId}
                            >
                              {room.roomName} ({room.assignedCount}/{room.capacity})
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </td>
                  </tr>
                ))}
                {rows.length === 0 && (
                  <tr>
                    <td colSpan={6} className="p-8 text-center text-muted-foreground">
                      Chưa có sinh viên được xếp phòng
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

function ExamTypeBadge({ type }: { type?: string | null }) {
  if (type === "RETAKE") return <Badge className="bg-red-100 text-red-800">Thi lại</Badge>;
  if (type === "IMPROVE") return <Badge className="bg-blue-100 text-blue-800">Nâng điểm</Badge>;
  if (type === "REPEAT_COURSE")
    return <Badge className="bg-amber-100 text-amber-800">Học lại</Badge>;
  return <Badge variant="outline">Thi kết thúc</Badge>;
}

function formatDateTime(value?: string | null) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function exportSeats(session: ExamSessionResponse, rows: ExamSeatAssignmentResponse[]) {
  const header = ["STT", "Phong", "MSSV", "Ho ten", "Lop", "Nguon"];
  const lines = rows.map((row, index) => [
    index + 1,
    row.roomName,
    row.studentCode,
    row.studentName,
    row.classCode ?? "",
    row.sourceType,
  ]);
  const csv = [header, ...lines].map((line) => line.map(csvCell).join(",")).join("\r\n");
  const blob = new Blob([`\uFEFF${csv}`], { type: "text/csv;charset=utf-8" });
  triggerBrowserDownload(blob, `lich-thi-${session.courseCode}-${session.examType}.csv`);
}

function csvCell(value: unknown) {
  const text = String(value ?? "");
  return `"${text.replace(/"/g, '""')}"`;
}
