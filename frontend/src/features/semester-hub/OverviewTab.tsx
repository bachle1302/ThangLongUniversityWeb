import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  BookOpen,
  CalendarCheck,
  CheckCircle2,
  Circle,
  Flag,
  Lock,
  Megaphone,
  Plus,
  Power,
  RotateCcw,
  Users,
  Trash2,
} from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { adminApi } from "@/lib/api/admin";
import { cn } from "@/lib/utils";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { RegistrationRoundResponse, RegistrationRoundRequest, RegistrationTimeSlotRequest } from "@/lib/api/types";

interface Props {
  semesterId: number;
}

type StepState = "done" | "active" | "waiting";

export function OverviewTab({ semesterId }: Props) {
  const queryClient = useQueryClient();
  const [isConfigDialogOpen, setIsConfigDialogOpen] = useState(false);
  const [configRound, setConfigRound] = useState<RegistrationRoundResponse | null>(null);
  const [roundName, setRoundName] = useState("");
  const [timeSlots, setTimeSlots] = useState<RegistrationTimeSlotRequest[]>([]);

  const summaryQuery = useQuery({
    queryKey: ["admin", "semester-summary", semesterId],
    queryFn: () => adminApi.getSemesterSummary(semesterId),
  });

  const courseRoundsQuery = useQuery({
    queryKey: ["admin", "registration-rounds", semesterId, "COURSE"],
    queryFn: () => adminApi.listRegistrationRounds(semesterId, "COURSE"),
  });

  const retakeRoundsQuery = useQuery({
    queryKey: ["admin", "registration-rounds", semesterId, "RETAKE"],
    queryFn: () => adminApi.listRegistrationRounds(semesterId, "RETAKE"),
  });

  const majorsQuery = useQuery({
    queryKey: ["admin", "majors"],
    queryFn: adminApi.listMajors,
  });

  const s = summaryQuery.data;
  const courseRounds = courseRoundsQuery.data ?? [];
  const retakeRounds = retakeRoundsQuery.data ?? [];
  const majors = majorsQuery.data ?? [];

  const cohortOptions = ["K33", "K34", "K35", "K36", "K37"];

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["admin", "semester-summary", semesterId] });
    queryClient.invalidateQueries({ queryKey: ["admin", "registration-rounds", semesterId] });
    queryClient.invalidateQueries({ queryKey: ["admin", "semesters"] });
  };

  const createRoundMutation = useMutation({
    mutationFn: (req: RegistrationRoundRequest) =>
      adminApi.createRegistrationRound(semesterId, req),
    onSuccess: () => {
      invalidate();
      toast.success("Đã tạo đợt đăng ký mới thành công");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Lỗi"),
  });

  const openRoundMutation = useMutation({
    mutationFn: ({ roundId, req }: { roundId: number; req?: RegistrationRoundRequest }) =>
      adminApi.openRegistrationRound(semesterId, roundId, req),
    onSuccess: () => {
      invalidate();
      toast.success("Đã lưu cấu hình và cập nhật đợt đăng ký");
      setIsConfigDialogOpen(false);
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Lỗi"),
  });

  const closeRoundMutation = useMutation({
    mutationFn: (roundId: number) => adminApi.closeRegistrationRound(semesterId, roundId),
    onSuccess: () => {
      invalidate();
      toast.success("Đã đóng đợt đăng ký");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Lỗi"),
  });

  const lockRoundMutation = useMutation({
    mutationFn: (roundId: number) => adminApi.lockRegistrationRound(semesterId, roundId),
    onSuccess: (res) => {
      invalidate();
      toast.success(res.message || "Đã chốt đợt đăng ký");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Lỗi"),
  });

  const lockEnrollmentsMutation = useMutation({
    mutationFn: () => adminApi.lockEnrollments(semesterId),
    onSuccess: (res) => {
      invalidate();
      toast.success(res.message || "Đã chốt tổng đăng ký học phần");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Lỗi"),
  });

  const lockRetakeMutation = useMutation({
    mutationFn: () => adminApi.lockRetakes(semesterId),
    onSuccess: (res) => {
      invalidate();
      toast.success(res.message || "Đã chốt tổng đăng ký thi lại");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Lỗi"),
  });

  const publishExamMutation = useMutation({
    mutationFn: () => adminApi.publishExamSchedules(semesterId),
    onSuccess: () => {
      invalidate();
      toast.success("Đã công bố lịch thi");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Lỗi"),
  });

  const unpublishExamMutation = useMutation({
    mutationFn: () => adminApi.unpublishExamSchedules(semesterId),
    onSuccess: () => {
      invalidate();
      toast.success("Đã hủy công bố lịch thi");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Lỗi"),
  });

  const endSemesterMutation = useMutation({
    mutationFn: () => adminApi.endSemester(semesterId),
    onSuccess: () => {
      invalidate();
      toast.success("Đã kết thúc học kỳ");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Lỗi"),
  });

  if (summaryQuery.isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-32 w-full" />
        <Skeleton className="h-32 w-full" />
      </div>
    );
  }

  if (!s) return null;

  const step1Done = s.locked;
  const step2Done = s.retakeLocked;
  const step3Done = s.examPublished;
  const step4Done = Boolean(s.ended);
  const canUseStep2 = step1Done;
  const canUseStep3 = step1Done && step2Done;
  const canUseStep4 = step1Done && step2Done && step3Done;

  const anyCourseRoundOpen = courseRounds.some((round) => round.registrationOpen);
  const anyRetakeRoundOpen = retakeRounds.some((round) => round.registrationOpen);

  const stepState = (step: number): StepState => {
    if (step === 1) return step1Done ? "done" : "active";
    if (step === 2) return step2Done ? "done" : step1Done ? "active" : "waiting";
    if (step === 3) return step3Done ? "done" : step2Done ? "active" : "waiting";
    return step4Done ? "done" : step3Done ? "active" : "waiting";
  };

  const handleConfigRoundClick = (round: RegistrationRoundResponse) => {
    setConfigRound(round);
    setRoundName(round.name || "");
    if (round.timeSlots && round.timeSlots.length > 0) {
      setTimeSlots(
        round.timeSlots.map((ts) => ({
          startTime: ts.startTime.slice(0, 16),
          endTime: ts.endTime.slice(0, 16),
          allowedMajorIds: ts.allowedMajorIds || [],
          allowedCohorts: ts.allowedCohorts || [],
        })),
      );
    } else {
      setTimeSlots([]);
    }
    setIsConfigDialogOpen(true);
  };

  const handleConfigRoundSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!configRound) return;
    openRoundMutation.mutate({
      roundId: configRound.id,
      req: {
        name: roundName.trim() || undefined,
        timeSlots: timeSlots.map((ts) => ({
          startTime: new Date(ts.startTime).toISOString(),
          endTime: new Date(ts.endTime).toISOString(),
          allowedMajorIds: ts.allowedMajorIds && ts.allowedMajorIds.length > 0 ? ts.allowedMajorIds : undefined,
          allowedCohorts: ts.allowedCohorts && ts.allowedCohorts.length > 0 ? ts.allowedCohorts : undefined,
        })),
      },
    });
  };

  const handleAddSlot = () => {
    const now = new Date();
    const tmr = new Date(now.getTime() + 24 * 60 * 60 * 1000);
    // adjust to local time string
    const formatLocal = (d: Date) => {
      const pad = (n: number) => String(n).padStart(2, '0');
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    };
    setTimeSlots([
      ...timeSlots,
      {
        startTime: formatLocal(now),
        endTime: formatLocal(tmr),
        allowedMajorIds: [],
        allowedCohorts: [],
      },
    ]);
  };

  const handleRemoveSlot = (index: number) => {
    setTimeSlots(timeSlots.filter((_, i) => i !== index));
  };

  const handleSlotChange = (index: number, key: keyof RegistrationTimeSlotRequest, value: any) => {
    const newSlots = [...timeSlots];
    newSlots[index] = { ...newSlots[index], [key]: value };
    setTimeSlots(newSlots);
  };

  const handleSlotMajorToggle = (index: number, majorId: number) => {
    const slot = timeSlots[index];
    const prev = slot.allowedMajorIds || [];
    const newArr = prev.includes(majorId) ? prev.filter((id) => id !== majorId) : [...prev, majorId];
    handleSlotChange(index, "allowedMajorIds", newArr);
  };

  const handleSlotCohortToggle = (index: number, cohort: string) => {
    const slot = timeSlots[index];
    const prev = slot.allowedCohorts || [];
    const newArr = prev.includes(cohort) ? prev.filter((c) => c !== cohort) : [...prev, cohort];
    handleSlotChange(index, "allowedCohorts", newArr);
  };

  const formatDateTimeVN = (iso: string) => {
    if (!iso) return "—";
    const d = new Date(iso);
    return new Intl.DateTimeFormat("vi-VN", { dateStyle: "short", timeStyle: "short" }).format(d);
  };

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <MetricCard icon={<BookOpen className="h-5 w-5" />} value={s.classSectionCount} label="Lớp học phần" />
        <MetricCard icon={<Users className="h-5 w-5" />} value={s.enrollmentCount} label="Lượt đăng ký" />
        <MetricCard icon={<CalendarCheck className="h-5 w-5" />} value={`${s.examScheduledCount}/${s.classSectionCount}`} label="Lớp có lịch thi" />
        <MetricCard icon={<RotateCcw className="h-5 w-5" />} value={s.retakeRegistrations} label="Đăng ký thi lại" />
      </div>

      <div className="space-y-4">
        {/* Bước 1: Đăng ký học phần */}
        <StepCard
          stepNum={1}
          state={stepState(1)}
          title="Đăng ký học phần"
          subtitle={
            step1Done
              ? `Đã chốt: ${s.registeredEnrollments} đăng ký`
              : anyCourseRoundOpen
                ? "Đang mở đợt đăng ký"
                : "Chưa mở đăng ký"
          }
          statusBadge={<LifecycleBadge done={step1Done} open={anyCourseRoundOpen} />}
          note="Khi chốt từng đợt: Tất cả PENDING của đợt đó chuyển thành REGISTERED, tạo Grade, khóa đăng ký đợt đó. Cần bấm Khóa tổng đăng ký để kết thúc Bước 1."
        >
          <div className="space-y-3 pt-3 border-t mt-2">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div>
                <h4 className="text-xs font-semibold">Các đợt đăng ký trong học kỳ</h4>
                <p className="mt-0.5 text-[11px] text-muted-foreground">
                  Mỗi đợt có thể mở, đóng và chốt riêng; cấu hình từng phân luồng thời gian cho Ngành/Khóa học.
                </p>
              </div>
              <div className="flex items-center gap-2">
                {!step1Done && courseRounds.length > 0 && (
                  <Button
                    size="sm"
                    variant="default"
                    className="h-7 text-xs px-2.5 bg-amber-600 hover:bg-amber-700 text-white"
                    disabled={lockEnrollmentsMutation.isPending || anyCourseRoundOpen || step4Done}
                    onClick={() => {
                      if (confirm("Bạn có chắc chắn muốn khóa tổng toàn bộ đăng ký học phần? Hành động này sẽ chuyển sang bước tiếp theo.")) {
                        lockEnrollmentsMutation.mutate();
                      }
                    }}
                  >
                    <Lock className="mr-1 h-3.5 w-3.5" />
                    Khóa tổng đăng ký
                  </Button>
                )}
                <Button
                  size="sm"
                  variant="outline"
                  className="h-7 text-xs px-2.5"
                  disabled={createRoundMutation.isPending || step1Done || step4Done}
                  onClick={() => createRoundMutation.mutate({ roundType: "COURSE", open: false })}
                >
                  <Plus className="mr-1 h-3.5 w-3.5" />
                  Tạo đợt mới
                </Button>
              </div>
            </div>

            <div className="grid gap-2 md:grid-cols-2 mt-2">
              {courseRounds.map((round) => (
                <div key={round.id} className="rounded-md border p-3 bg-background">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <div className="text-sm font-medium">Đợt {round.roundNumber}: {round.name}</div>
                      {round.timeSlots && round.timeSlots.length > 0 ? (
                        <div className="mt-1 space-y-1">
                          {round.timeSlots.map((ts, i) => (
                            <div key={i} className="text-[10px] text-muted-foreground border-l-2 pl-2">
                              <div>{formatDateTimeVN(ts.startTime)} - {formatDateTimeVN(ts.endTime)}</div>
                              {ts.allowedMajorIds && ts.allowedMajorIds.length > 0 && (
                                <div>Ngành: {ts.allowedMajorIds.map(mId => majors.find(m => m.id === mId)?.name || mId).join(", ")}</div>
                              )}
                              {ts.allowedCohorts && ts.allowedCohorts.length > 0 && (
                                <div>Khóa: {ts.allowedCohorts.join(", ")}</div>
                              )}
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="text-[10px] text-muted-foreground mt-0.5">Mở tự do (Không có phân luồng)</div>
                      )}
                      <div className="mt-2 text-xs text-muted-foreground">
                        {round.classSectionCount} lớp, {round.pendingEnrollments} chờ chốt, {round.registeredEnrollments} đã chốt
                      </div>
                    </div>
                    <LifecycleBadge done={round.locked} open={round.registrationOpen} />
                  </div>
                  {!round.locked && !step1Done && !step4Done && (
                    <div className="mt-3 flex gap-2">
                      {round.registrationOpen ? (
                        <>
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-8 text-xs"
                            disabled={closeRoundMutation.isPending}
                            onClick={() => closeRoundMutation.mutate(round.id)}
                          >
                            <Power className="mr-1 h-3.5 w-3.5" />
                            Đóng đăng ký
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-8 text-xs bg-muted/30"
                            disabled={openRoundMutation.isPending}
                            onClick={() => handleConfigRoundClick(round)}
                          >
                            Cấu hình
                          </Button>
                        </>
                      ) : (
                        <Button
                          variant="outline"
                          size="sm"
                          className="h-8 text-xs"
                          disabled={openRoundMutation.isPending || anyCourseRoundOpen}
                          onClick={() => handleConfigRoundClick(round)}
                        >
                          <Power className="mr-1 h-3.5 w-3.5" />
                          Mở đăng ký
                        </Button>
                      )}
                      <Button
                        size="sm"
                        className="h-8 text-xs ml-auto"
                        disabled={lockRoundMutation.isPending}
                        onClick={() => lockRoundMutation.mutate(round.id)}
                      >
                        <Lock className="mr-1 h-3.5 w-3.5" />
                        Chốt đợt
                        {round.pendingEnrollments > 0 ? (
                          <Badge variant="secondary" className="ml-1.5 h-4 px-1 text-xs">
                            {round.pendingEnrollments}
                          </Badge>
                        ) : null}
                      </Button>
                    </div>
                  )}
                </div>
              ))}
              {courseRounds.length === 0 && (
                <div className="rounded-md border border-dashed p-4 text-center text-xs text-muted-foreground col-span-2">
                  Chưa có đợt đăng ký.
                </div>
              )}
            </div>
          </div>
        </StepCard>

        {/* Bước 2: Đăng ký thi lại / Nâng điểm */}
        <StepCard
          stepNum={2}
          state={stepState(2)}
          title="Đăng ký thi lại / Nâng điểm"
          subtitle={
            step2Done
              ? `Đã chốt: ${s.retakeRegistered} đăng ký`
              : anyRetakeRoundOpen
                ? "Đang mở đợt đăng ký thi lại"
                : "Chưa mở đăng ký"
          }
          statusBadge={<LifecycleBadge done={step2Done} open={anyRetakeRoundOpen} />}
          note="Khi chốt tổng thi lại: Khóa toàn bộ đăng ký thi lại để chuyển sang Bước 3: Công bố lịch thi."
        >
          <div className="space-y-3 pt-3 border-t mt-2">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div>
                <h4 className="text-xs font-semibold">Các đợt đăng ký thi lại trong học kỳ</h4>
                <p className="mt-0.5 text-[11px] text-muted-foreground">
                  Mỗi đợt đăng ký thi lại/nâng điểm có thể cấu hình giới hạn phân luồng thời gian riêng biệt.
                </p>
              </div>
              <div className="flex items-center gap-2">
                {!step2Done && retakeRounds.length > 0 && (
                  <Button
                    size="sm"
                    variant="default"
                    className="h-7 text-xs px-2.5 bg-amber-600 hover:bg-amber-700 text-white"
                    disabled={lockRetakeMutation.isPending || anyRetakeRoundOpen || step4Done || !canUseStep2}
                    onClick={() => {
                      if (confirm("Bạn có chắc chắn muốn khóa tổng toàn bộ đăng ký thi lại/nâng điểm? Hành động này sẽ chuyển sang bước tiếp theo.")) {
                        lockRetakeMutation.mutate();
                      }
                    }}
                  >
                    <Lock className="mr-1 h-3.5 w-3.5" />
                    Khóa tổng thi lại
                  </Button>
                )}
                <Button
                  size="sm"
                  variant="outline"
                  className="h-7 text-xs px-2.5"
                  disabled={createRoundMutation.isPending || step2Done || step4Done || !canUseStep2}
                  onClick={() => createRoundMutation.mutate({ roundType: "RETAKE", open: false })}
                >
                  <Plus className="mr-1 h-3.5 w-3.5" />
                  Tạo đợt mới
                </Button>
              </div>
            </div>

            <div className="grid gap-2 md:grid-cols-2 mt-2">
              {retakeRounds.map((round) => (
                <div key={round.id} className="rounded-md border p-3 bg-background">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <div className="text-sm font-medium">Đợt {round.roundNumber}: {round.name}</div>
                      {round.timeSlots && round.timeSlots.length > 0 ? (
                        <div className="mt-1 space-y-1">
                          {round.timeSlots.map((ts, i) => (
                            <div key={i} className="text-[10px] text-muted-foreground border-l-2 pl-2">
                              <div>{formatDateTimeVN(ts.startTime)} - {formatDateTimeVN(ts.endTime)}</div>
                              {ts.allowedMajorIds && ts.allowedMajorIds.length > 0 && (
                                <div>Ngành: {ts.allowedMajorIds.map(mId => majors.find(m => m.id === mId)?.name || mId).join(", ")}</div>
                              )}
                              {ts.allowedCohorts && ts.allowedCohorts.length > 0 && (
                                <div>Khóa: {ts.allowedCohorts.join(", ")}</div>
                              )}
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="text-[10px] text-muted-foreground mt-0.5">Mở tự do (Không có phân luồng)</div>
                      )}
                      <div className="mt-2 text-xs text-muted-foreground">
                        {round.pendingEnrollments} chờ chốt, {round.registeredEnrollments} đã chốt
                      </div>
                    </div>
                    <LifecycleBadge done={round.locked} open={round.registrationOpen} />
                  </div>
                  {!round.locked && !step2Done && !step4Done && canUseStep2 && (
                    <div className="mt-3 flex gap-2">
                      {round.registrationOpen ? (
                        <>
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-8 text-xs"
                            disabled={closeRoundMutation.isPending}
                            onClick={() => closeRoundMutation.mutate(round.id)}
                          >
                            <Power className="mr-1 h-3.5 w-3.5" />
                            Đóng đăng ký
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            className="h-8 text-xs bg-muted/30"
                            disabled={openRoundMutation.isPending}
                            onClick={() => handleConfigRoundClick(round)}
                          >
                            Cấu hình
                          </Button>
                        </>
                      ) : (
                        <Button
                          variant="outline"
                          size="sm"
                          className="h-8 text-xs"
                          disabled={openRoundMutation.isPending || anyRetakeRoundOpen || !canUseStep2}
                          onClick={() => handleConfigRoundClick(round)}
                        >
                          <Power className="mr-1 h-3.5 w-3.5" />
                          Mở đăng ký
                        </Button>
                      )}
                      <Button
                        size="sm"
                        className="h-8 text-xs ml-auto"
                        disabled={lockRoundMutation.isPending || !canUseStep2}
                        onClick={() => lockRoundMutation.mutate(round.id)}
                      >
                        <Lock className="mr-1 h-3.5 w-3.5" />
                        Chốt đợt
                        {round.pendingEnrollments > 0 ? (
                          <Badge variant="secondary" className="ml-1.5 h-4 px-1 text-xs">
                            {round.pendingEnrollments}
                          </Badge>
                        ) : null}
                      </Button>
                    </div>
                  )}
                </div>
              ))}
              {retakeRounds.length === 0 && (
                <div className="rounded-md border border-dashed p-4 text-center text-xs text-muted-foreground col-span-2">
                  Chưa có đợt đăng ký thi lại.
                </div>
              )}
            </div>
          </div>
        </StepCard>

        {/* Bước 3: Công bố lịch thi */}
        <StepCard
          stepNum={3}
          state={stepState(3)}
          title="Công bố lịch thi"
          subtitle={`${s.examScheduledCount}/${s.classSectionCount} lớp đã có lịch thi`}
          statusBadge={step3Done ? <Badge className="bg-emerald-100 text-emerald-800 border-emerald-200">Đã công bố</Badge> : <Badge variant="outline">Chưa công bố</Badge>}
          actions={
            !step4Done ? (
            <div className="flex flex-wrap gap-2">
              {!step3Done ? (
                <Button size="sm" disabled={publishExamMutation.isPending || !canUseStep3} onClick={() => publishExamMutation.mutate()}>
                  <Megaphone className="mr-1 h-3.5 w-3.5" />
                  Công bố lịch thi
                </Button>
              ) : (
                <Button variant="outline" size="sm" disabled={unpublishExamMutation.isPending || step4Done} onClick={() => unpublishExamMutation.mutate()}>
                  Hủy công bố
                </Button>
              )}
            </div>
            ) : null
          }
          note="Khi công bố: Sinh viên sẽ thấy lịch thi trên trang Lịch thi"
        />

        {/* Bước 4: Kết thúc kỳ học */}
        <StepCard
          stepNum={4}
          state={stepState(4)}
          title="Kết thúc kỳ học"
          subtitle={step4Done ? "Học kỳ đã kết thúc" : "Đóng học kỳ thủ công khi đã hoàn tất vận hành"}
          statusBadge={step4Done ? <Badge className="bg-emerald-100 text-emerald-800 border-emerald-200">Đã kết thúc</Badge> : <Badge variant="outline">Chưa kết thúc</Badge>}
          actions={
            !step4Done ? (
              <Button size="sm" disabled={endSemesterMutation.isPending || !canUseStep4} onClick={() => endSemesterMutation.mutate()}>
                <Flag className="mr-1 h-3.5 w-3.5" />
                Kết thúc kỳ học
              </Button>
            ) : null
          }
          note="Khi kết thúc: Đóng đăng ký học phần và đăng ký thi lại, dù chưa tới ngày kết thúc trên lịch."
        />
      </div>

      {/* Dialog cấu hình giới hạn đợt đăng ký (Time Slots) */}
      <Dialog open={isConfigDialogOpen} onOpenChange={setIsConfigDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <form onSubmit={handleConfigRoundSubmit} className="space-y-4">
            <DialogHeader>
              <DialogTitle>
                Cấu hình phân luồng: {configRound?.name}
              </DialogTitle>
              <DialogDescription>
                Thiết lập các khung giờ đăng ký cho từng ngành và khóa học. Nếu không có khung giờ nào, đợt đăng ký sẽ mở cho tất cả mọi người bất kể thời gian.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4">
              <div className="space-y-1">
                <Label htmlFor="roundName">Tên đợt đăng ký</Label>
                <Input
                  id="roundName"
                  placeholder="Ví dụ: Đợt 1, Đợt phụ..."
                  value={roundName}
                  onChange={(e) => setRoundName(e.target.value)}
                />
              </div>

              <div>
                <div className="flex items-center justify-between mb-2">
                  <Label>Danh sách khung giờ (Phân luồng)</Label>
                  <Button type="button" size="sm" variant="outline" className="h-8" onClick={handleAddSlot}>
                    <Plus className="mr-1 h-3.5 w-3.5" /> Thêm khung giờ
                  </Button>
                </div>
                
                {timeSlots.length === 0 && (
                  <div className="text-center p-4 border rounded-md text-muted-foreground text-sm border-dashed">
                    Không có phân luồng. Đợt đăng ký mở tự do.
                  </div>
                )}

                <div className="space-y-3">
                  {timeSlots.map((slot, index) => (
                    <div key={index} className="border rounded-md p-3 relative bg-muted/10">
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="absolute top-2 right-2 h-6 w-6 text-red-500 hover:bg-red-50"
                        onClick={() => handleRemoveSlot(index)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                      
                      <div className="text-xs font-semibold mb-2">Khung giờ {index + 1}</div>
                      
                      <div className="grid grid-cols-2 gap-3 mb-3">
                        <div>
                          <Label className="text-[11px]">Bắt đầu</Label>
                          <Input 
                            type="datetime-local" 
                            className="h-8 text-xs" 
                            value={slot.startTime} 
                            onChange={(e) => handleSlotChange(index, "startTime", e.target.value)} 
                          />
                        </div>
                        <div>
                          <Label className="text-[11px]">Kết thúc</Label>
                          <Input 
                            type="datetime-local" 
                            className="h-8 text-xs" 
                            value={slot.endTime} 
                            onChange={(e) => handleSlotChange(index, "endTime", e.target.value)} 
                          />
                        </div>
                      </div>

                      <div className="space-y-3">
                        <div className="space-y-1">
                          <Label className="text-[11px]">Ngành học áp dụng</Label>
                          <div className="grid grid-cols-2 gap-2 border rounded-md p-2 max-h-32 overflow-y-auto bg-background">
                            {majors.map((m) => (
                              <label key={m.id} className="flex items-center space-x-2 text-xs cursor-pointer hover:text-foreground">
                                <Checkbox
                                  checked={slot.allowedMajorIds?.includes(m.id)}
                                  onCheckedChange={() => handleSlotMajorToggle(index, m.id)}
                                />
                                <span className="truncate">{m.name}</span>
                              </label>
                            ))}
                          </div>
                        </div>

                        <div className="space-y-1">
                          <Label className="text-[11px]">Khóa học áp dụng</Label>
                          <div className="flex flex-wrap gap-2 border rounded-md p-2 bg-background">
                            {cohortOptions.map((cohort) => (
                              <label key={cohort} className="flex items-center space-x-2 text-xs cursor-pointer hover:text-foreground">
                                <Checkbox
                                  checked={slot.allowedCohorts?.includes(cohort)}
                                  onCheckedChange={() => handleSlotCohortToggle(index, cohort)}
                                />
                                <span>{cohort}</span>
                              </label>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <DialogFooter className="pt-2">
              <Button type="button" variant="outline" size="sm" onClick={() => setIsConfigDialogOpen(false)}>
                Hủy
              </Button>
              <Button type="submit" size="sm" disabled={openRoundMutation.isPending}>
                Lưu cấu hình
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function MetricCard({ icon, value, label }: { icon: React.ReactNode; value: number | string; label: string }) {
  return (
    <Card>
      <CardContent className="p-4">
        <div className="flex items-start justify-between gap-3">
          <div>
            <div className="text-3xl font-semibold tabular-nums">{value}</div>
            <p className="mt-1 text-xs text-muted-foreground">{label}</p>
          </div>
          <div className="grid h-10 w-10 place-items-center rounded-md border bg-muted/40 text-muted-foreground">
            {icon}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function StepCard({
  stepNum,
  state,
  title,
  subtitle,
  statusBadge,
  actions,
  note,
  children,
}: {
  stepNum: number;
  state: StepState;
  title: string;
  subtitle: string;
  statusBadge: React.ReactNode;
  actions?: React.ReactNode;
  note: string;
  children?: React.ReactNode;
}) {
  const active = state === "active";
  const done = state === "done";

  return (
    <div className="grid gap-3 md:grid-cols-[40px_minmax(0,1fr)]">
      <div className="flex justify-center pt-1">
        <div
          className={cn(
            "grid h-10 w-10 place-items-center rounded-full border-2 bg-background",
            (active || done) && "border-emerald-500 bg-emerald-50 text-emerald-700",
          )}
        >
          {done ? <CheckCircle2 className="h-5 w-5" /> : active ? <span className="text-sm font-semibold">{stepNum}</span> : <Circle className="h-5 w-5 text-muted-foreground" />}
        </div>
      </div>

      <Card className={cn("overflow-hidden", active && "border-emerald-300 shadow-sm", done && "bg-emerald-50/30")}>
        <CardContent className="space-y-3 p-4">
          <div className="flex flex-wrap items-start justify-between gap-2">
            <div>
              <h3 className="text-sm font-semibold">Bước {stepNum}: {title}</h3>
              <p className="mt-0.5 text-xs text-muted-foreground">{subtitle}</p>
            </div>
            {statusBadge}
          </div>
          {actions ? <div>{actions}</div> : null}
          {children}
          <p className="text-xs italic text-muted-foreground/70">{note}</p>
        </CardContent>
      </Card>
    </div>
  );
}

function LifecycleBadge({ done, open }: { done: boolean; open: boolean }) {
  if (done) return <Badge className="bg-emerald-100 text-emerald-800 border-emerald-200">Đã chốt</Badge>;
  if (open) return <Badge className="bg-emerald-100 text-emerald-800 border-emerald-200">Đang mở</Badge>;
  return <Badge variant="outline">Đóng</Badge>;
}
