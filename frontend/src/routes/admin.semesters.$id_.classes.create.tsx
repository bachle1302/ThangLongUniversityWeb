import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute, Link, useNavigate } from "@tanstack/react-router";
import {
  AlertTriangle,
  ArrowLeft,
  CheckCircle2,
  Info,
  Loader2,
  Plus,
  Wand2,
  XCircle,
} from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
import { adminApi } from "@/lib/api/admin";
import type {
  AdminClassSectionRequest,
  BulkClassSectionCourseSummaryResponse,
  ClassSectionValidationIssueResponse,
  ClassSectionValidationResponse,
} from "@/lib/api/types";
import { buildOptionSets, mapApiClassSection } from "@/features/admin-class-sections/classSectionMappers";
import type {
  ClassSectionFormValues,
  ClassSectionScheduleFormValue,
  CourseOption,
} from "@/features/admin-class-sections/types";
import { validateClassSectionPlanErrors } from "@/features/admin-class-sections/validation";

export const Route = createFileRoute("/admin/semesters/$id_/classes/create")({
  component: CreateCourseClassPage,
});

type Mode = "single" | "bulk";
type BulkStep = "select" | "edit";
type RowState = ClassSectionFormValues & {
  key: string;
  validation?: ClassSectionValidationResponse;
  validating?: boolean;
};

type BulkCourseConfig = {
  count: number;
  maxSlots: number;
  sessionsPerWeek: number;
  periodsPerSession: number;
};
type BulkSeed = {
  courseIds: number[];
  configs: Record<number, BulkCourseConfig>;
};

const defaultBulkCourseConfig: BulkCourseConfig = {
  count: 4,
  maxSlots: 35,
  sessionsPerWeek: 1,
  periodsPerSession: 2,
};

const dayOptions = [
  { value: 2, label: "Thứ 2" },
  { value: 3, label: "Thứ 3" },
  { value: 4, label: "Thứ 4" },
  { value: 5, label: "Thứ 5" },
  { value: 6, label: "Thứ 6" },
  { value: 7, label: "Thứ 7" },
  { value: 8, label: "Chủ nhật" },
];

function CreateCourseClassPage() {
  const { id } = Route.useParams();
  const semesterId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [mode, setMode] = useState<Mode>("single");
  const [bulkStep, setBulkStep] = useState<BulkStep>("select");
  const [single, setSingle] = useState<ClassSectionFormValues | null>(null);
  const [bulkSeed, setBulkSeed] = useState<BulkSeed>({
    courseIds: [],
    configs: {},
  });
  const [bulkRows, setBulkRows] = useState<RowState[]>([]);

  const semestersQuery = useQuery({
    queryKey: ["admin", "semesters"],
    queryFn: adminApi.listSemesters,
    staleTime: 300_000,
  });
  const classSectionsQuery = useQuery({
    queryKey: ["admin", "class-sections", "semester", semesterId],
    queryFn: () => adminApi.listClassSectionsBySemester(semesterId),
  });
  const coursesQuery = useQuery({ queryKey: ["admin", "courses"], queryFn: adminApi.listCourses });
  const teachersQuery = useQuery({ queryKey: ["admin", "teachers"], queryFn: adminApi.listTeachers });
  const roomsQuery = useQuery({ queryKey: ["admin", "rooms"], queryFn: adminApi.listRooms });
  const periodsQuery = useQuery({ queryKey: ["admin", "periods"], queryFn: adminApi.listPeriods });

  const semester = semestersQuery.data?.find((item) => item.id === semesterId);
  const rows = (classSectionsQuery.data ?? []).map((section) => mapApiClassSection(section));
  const options = buildOptionSets(
    {
      courses: coursesQuery.data,
      semesters: (semestersQuery.data ?? []).filter((item) => item.id === semesterId),
      teachers: teachersQuery.data,
      rooms: roomsQuery.data,
      periods: periodsQuery.data,
      classSections: classSectionsQuery.data,
    },
    rows,
  );

  const initialValues = useMemo<ClassSectionFormValues>(() => {
    const course = options.courses[0];
    const teachers = getCompatibleTeachers(course?.id ?? 0, options);
    return {
      classCode: course ? nextClassCode(course.code, rows) : "",
      courseId: course?.id ?? 0,
      semesterId,
      teacherId: teachers[0]?.id ?? 0,
      roomId: options.rooms[0]?.id ?? 0,
      dayOfWeek: 2,
      startPeriodId: options.periods[0]?.id ?? 0,
      endPeriodId: options.periods[1]?.id ?? options.periods[0]?.id ?? 0,
      maxSlots: 40,
      status: "OPEN",
      schedules: [createDefaultSchedule(options, 0)],
    };
  }, [options, rows, semesterId]);

  useEffect(() => {
    if (!single && options.courses.length && options.teachers.length && options.rooms.length && options.periods.length) {
      const course = options.courses[0];
      const teachers = getCompatibleTeachers(course.id, options);
      setSingle({ ...initialValues, classCode: nextClassCode(course.code, rows), teacherId: teachers[0]?.id ?? 0 });
      setBulkSeed((current) => ({
        courseIds: [course.id],
        configs: {
          ...current.configs,
          [course.id]: current.configs[course.id] ?? defaultBulkCourseConfig,
        },
      }));
    }
  }, [initialValues, options, rows, single]);

  useEffect(() => {
    if (!single) return;
    const teachers = getCompatibleTeachers(single.courseId, options);
    if (teachers.length === 0 && single.teacherId !== 0) {
      setSingle((current) => current ? { ...current, teacherId: 0 } : current);
      return;
    }
    if (teachers.length > 0 && !teachers.some((teacher) => teacher.id === single.teacherId)) {
      setSingle((current) => current ? { ...current, teacherId: teachers[0].id } : current);
    }
  }, [options, single]);

  const invalidate = () => {
    queryClient.invalidateQueries({ queryKey: ["admin", "class-sections", "semester", semesterId] });
    queryClient.invalidateQueries({ queryKey: ["admin", "exam-schedules", semesterId] });
    queryClient.invalidateQueries({ queryKey: ["admin", "semester-summary", semesterId] });
    queryClient.invalidateQueries({ queryKey: ["admin", "semesters"] });
  };

  const singleRequest = single ? toRequest(single, semesterId) : null;
  const clientValidationMessages =
    single && options.periods.length && options.rooms.length
      ? validateClassSectionPlanErrors({ values: single, rows, periods: options.periods, rooms: options.rooms })
      : [];
  const singleReady = single ? isReady(single) : false;
  const singleValidationQuery = useQuery({
    queryKey: ["admin", "class-section-validate", singleRequest],
    queryFn: () => adminApi.validateClassSection(singleRequest as AdminClassSectionRequest),
    enabled: mode === "single" && singleReady && clientValidationMessages.length === 0 && !!singleRequest,
    staleTime: 2_000,
    retry: false,
  });

  const createSingleMutation = useMutation({
    mutationFn: (request: AdminClassSectionRequest) => adminApi.createClassSection(request),
    onSuccess: () => {
      invalidate();
      toast.success("Đã tạo lớp học phần");
      void navigate({ to: "/admin/semesters/$id", params: { id } });
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Không tạo được lớp học phần"),
  });

  const createBulkMutation = useMutation({
    mutationFn: (items: RowState[]) =>
      adminApi.createBulkClassSections(items.map((item) => toRequest(item, semesterId))),
    onSuccess: (created) => {
      invalidate();
      toast.success(`Đã tạo ${created.length} lớp học phần`);
      void navigate({ to: "/admin/semesters/$id", params: { id } });
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Không tạo được các lớp học phần"),
  });

  if (semestersQuery.isLoading || !single) {
    return (
      <div className="space-y-4 p-6">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  const singleErrors: ClassSectionValidationIssueResponse[] = clientValidationMessages.length
    ? clientValidationMessages.map((message, index) => ({ code: `CLIENT_VALIDATION_${index}`, message }))
    : singleValidationQuery.data?.errors ?? [];
  const backendWarnings = singleValidationQuery.data?.warnings ?? [];
  const singleInfos = [
    ...(singleValidationQuery.data?.infos ?? []),
    ...backendWarnings.filter((item) => item.code === "COURSE_ALREADY_HAS_CLASSES"),
  ];
  const singleWarnings = backendWarnings.filter((item) => item.code !== "COURSE_ALREADY_HAS_CLASSES");
  const singleBlocked = singleErrors.length > 0 || singleValidationQuery.isError;
  const singleSchedules = single.schedules?.length
    ? single.schedules
    : [createDefaultSchedule(options, 0)];
  const selectedCourse = options.courses.find((course) => course.id === single.courseId);
  const compatibleTeachers = getCompatibleTeachers(single.courseId, options);

  const updateSingleSchedule = (key: string | undefined, patch: Partial<ClassSectionScheduleFormValue>) => {
    setSingle({
      ...single,
      schedules: singleSchedules.map((schedule) =>
        schedule.key === key ? { ...schedule, ...patch } : schedule,
      ),
    });
  };

  const addSingleSchedule = () => {
    setSingle({
      ...single,
      schedules: [...singleSchedules, createDefaultSchedule(options, singleSchedules.length)],
    });
  };

  const removeSingleSchedule = (key: string | undefined) => {
    if (singleSchedules.length <= 1) return;
    setSingle({
      ...single,
      schedules: singleSchedules.filter((schedule) => schedule.key !== key),
    });
  };

  return (
    <div className="space-y-5 p-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <Link
            to="/admin/semesters/$id"
            params={{ id }}
            className="mb-2 inline-flex items-center text-sm text-muted-foreground hover:text-foreground"
          >
            <ArrowLeft className="mr-1 h-3.5 w-3.5" />
            Quay lại lớp học phần
          </Link>
          <h1 className="text-2xl font-bold">Tạo lớp học phần</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            {semester ? `${semester.name} | ${formatDate(semester.startDate)} - ${formatDate(semester.endDate)}` : `Học kỳ #${semesterId}`}
          </p>
        </div>
        <div className="rounded-md border bg-background p-1 shadow-sm">
          <Button size="sm" variant={mode === "single" ? "default" : "ghost"} onClick={() => setMode("single")}>
            Tạo 1 lớp
          </Button>
          <Button size="sm" variant={mode === "bulk" ? "default" : "ghost"} onClick={() => setMode("bulk")}>
            Tạo nhiều lớp
          </Button>
        </div>
      </div>

      {mode === "single" ? (
        <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
          <div className="space-y-4">
            <SectionCard title="Thông tin lớp">
              <FieldSelect label="Môn học" value={single.courseId} onChange={(courseId) => {
                const course = options.courses.find((item) => item.id === courseId);
                const teachers = getCompatibleTeachers(courseId, options);
                setSingle({
                  ...single,
                  courseId,
                  teacherId: teachers[0]?.id ?? 0,
                  classCode: nextClassCode(course?.code ?? "", rows),
                });
              }}>
                {options.courses.map((course) => (
                  <SelectItem key={course.id} value={String(course.id)}>
                    {course.code} - {course.name}
                  </SelectItem>
                ))}
              </FieldSelect>
              <FieldInput label="Mã lớp học phần" value={single.classCode} readOnly onChange={() => undefined} />
              <FieldSelect label="Học kỳ" value={semesterId} disabled onChange={() => undefined}>
                <SelectItem value={String(semesterId)}>{semester?.name ?? `Học kỳ #${semesterId}`}</SelectItem>
              </FieldSelect>
            </SectionCard>

            <SectionCard title="Phân công giảng dạy">
              <FieldSelect label="Giảng viên" value={single.teacherId} disabled={compatibleTeachers.length === 0} onChange={(teacherId) => setSingle({ ...single, teacherId })}>
                {compatibleTeachers.map((teacher) => (
                  <SelectItem key={teacher.id} value={String(teacher.id)}>
                    {teacher.departmentName ? `${teacher.name} - ${teacher.departmentName}` : teacher.name}
                  </SelectItem>
                ))}
              </FieldSelect>
              <FieldInput label="Sĩ số tối đa" type="number" value={single.maxSlots} onChange={(maxSlots) => setSingle({ ...single, maxSlots: Number(maxSlots) })} />
            </SectionCard>

            <SectionCard title="Lịch học">
              <div className="md:col-span-2 space-y-3">
                {singleSchedules.map((schedule, index) => (
                  <div key={schedule.key} className="grid gap-3 rounded-md border bg-background p-3 md:grid-cols-[1fr_1fr_1fr_1fr_auto]">
                    <FieldSelect label={`Buổi ${index + 1}`} value={schedule.dayOfWeek} onChange={(dayOfWeek) => updateSingleSchedule(schedule.key, { dayOfWeek })}>
                      {dayOptions.map((day) => <SelectItem key={day.value} value={String(day.value)}>{day.label}</SelectItem>)}
                    </FieldSelect>
                    <FieldSelect label="Phòng" value={schedule.roomId} onChange={(roomId) => updateSingleSchedule(schedule.key, { roomId })}>
                      {options.rooms.map((room) => (
                        <SelectItem key={room.id} value={String(room.id)}>{room.name} ({room.capacity})</SelectItem>
                      ))}
                    </FieldSelect>
                    <FieldSelect label="Tiết bắt đầu" value={schedule.startPeriodId} onChange={(startPeriodId) => updateSingleSchedule(schedule.key, { startPeriodId })}>
                      {options.periods.map((period) => <SelectItem key={period.id} value={String(period.id)}>{period.label}</SelectItem>)}
                    </FieldSelect>
                    <FieldSelect label="Tiết kết thúc" value={schedule.endPeriodId} onChange={(endPeriodId) => updateSingleSchedule(schedule.key, { endPeriodId })}>
                      {options.periods.map((period) => <SelectItem key={period.id} value={String(period.id)}>{period.label}</SelectItem>)}
                    </FieldSelect>
                    <div className="flex items-end">
                      <Button
                        type="button"
                        variant="outline"
                        className="w-full"
                        disabled={singleSchedules.length <= 1}
                        onClick={() => removeSingleSchedule(schedule.key)}
                      >
                        Xóa
                      </Button>
                    </div>
                  </div>
                ))}
                <Button type="button" variant="outline" className="gap-2" onClick={addSingleSchedule}>
                  <Plus className="h-4 w-4" />
                  Thêm buổi học
                </Button>
              </div>
            </SectionCard>
          </div>

          <aside className="space-y-4">
            <ValidationPanel
              loading={singleValidationQuery.isFetching}
              ready={singleReady}
              errors={singleValidationQuery.isError ? [{ code: "VALIDATION_API_ERROR", message: singleValidationQuery.error instanceof Error ? singleValidationQuery.error.message : "Không kiểm tra được lớp học phần" }] : singleErrors}
              infos={singleInfos}
              warnings={singleWarnings}
            />
            <PreviewPanel values={single} options={options} />
            <div className="flex justify-end gap-2">
              <Link to="/admin/semesters/$id" params={{ id }}>
                <Button type="button" variant="outline">Hủy</Button>
              </Link>
              <Button
                disabled={!singleReady || singleBlocked || createSingleMutation.isPending}
                onClick={() => createSingleMutation.mutate(toRequest(single, semesterId))}
              >
                {createSingleMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Tạo lớp
              </Button>
            </div>
          </aside>
        </div>
      ) : (
        <BulkCreatePanel
          seed={bulkSeed}
          setSeed={setBulkSeed}
          step={bulkStep}
          setStep={setBulkStep}
          rows={bulkRows}
          setRows={setBulkRows}
          options={options}
          semesterId={semesterId}
          existingRows={rows}
          onCreate={() => createBulkMutation.mutate(bulkRows)}
          creating={createBulkMutation.isPending}
        />
      )}
    </div>
  );
}

function BulkCreatePanel({
  seed,
  setSeed,
  step,
  setStep,
  rows,
  setRows,
  options,
  semesterId,
  existingRows,
  onCreate,
  creating,
}: {
  seed: BulkSeed;
  setSeed: (value: BulkSeed) => void;
  step: BulkStep;
  setStep: (value: BulkStep) => void;
  rows: RowState[];
  setRows: (rows: RowState[]) => void;
  options: ReturnType<typeof buildOptionSets>;
  semesterId: number;
  existingRows: ReturnType<typeof mapApiClassSection>[];
  onCreate: () => void;
  creating: boolean;
}) {
  const [summaries, setSummaries] = useState<BulkClassSectionCourseSummaryResponse[]>([]);
  const [validatingBatch, setValidatingBatch] = useState(false);
  const rowsRef = useRef(rows);
  rowsRef.current = rows;
  const validRows = rows.filter((row) => isRowValid(row));
  const invalidRows = rows.filter((row) => row.validation?.errors.length);
  const selectedCourses = options.courses.filter((course) => seed.courseIds.includes(course.id));
  const groupedRows = selectedCourses
    .map((course) => ({ course, rows: rows.filter((row) => row.courseId === course.id) }))
    .filter((group) => group.rows.length > 0);

  const proposalMutation = useMutation({
    mutationFn: () =>
      adminApi.proposeBulkClassSections({
        semesterId,
        courses: selectedCourses.map((course) => {
          const config = seed.configs[course.id] ?? defaultBulkCourseConfig;
          return {
            courseId: course.id,
            classCount: countSafe(config.count),
            maxSlots: Math.max(1, config.maxSlots),
            sessionsPerWeek: Math.max(1, Math.min(config.sessionsPerWeek, 6)),
            periodsPerSession: Math.max(1, Math.min(config.periodsPerSession, 14)),
          };
        }),
      }),
    onSuccess: (response) => {
      const generated = response.items.map<RowState>((item, index) => {
        const firstSchedule = item.schedules[0];
        return {
          key: `proposal-${item.courseId}-${item.classCode}-${index}`,
          classCode: item.classCode,
          courseId: item.courseId,
          semesterId: item.semesterId,
          teacherId: item.teacherId ?? 0,
          roomId: firstSchedule?.roomId ?? 0,
          dayOfWeek: firstSchedule?.dayOfWeek ?? 2,
          startPeriodId: firstSchedule?.startPeriodId ?? 0,
          endPeriodId: firstSchedule?.endPeriodId ?? 0,
          maxSlots: item.maxSlots,
          status: "OPEN",
          schedules: item.schedules.map((schedule, scheduleIndex) => ({
            key: `proposal-${index}-${scheduleIndex}`,
            roomId: schedule.roomId,
            dayOfWeek: schedule.dayOfWeek,
            startPeriodId: schedule.startPeriodId,
            endPeriodId: schedule.endPeriodId,
          })),
          validation: { valid: true, errors: [], warnings: [], infos: [] },
        };
      });
      setRows(generated);
      setSummaries(response.summaries);
      setStep("edit");
    },
    onError: (error) => {
      toast.error(error instanceof Error ? error.message : "Không thể tạo đề xuất lớp học phần");
    },
  });

  const validationKey = useMemo(
    () => JSON.stringify(rows.map((row) => toRequest(row, semesterId))),
    [rows, semesterId],
  );

  useEffect(() => {
    const currentRows = rowsRef.current;
    if (step !== "edit" || currentRows.length === 0) return;
    const hasLocalError = currentRows.some((row) =>
      !isReady(row) || row.validation?.errors.some((error) => error.code.startsWith("CLIENT_")),
    );
    if (hasLocalError) return;
    let active = true;
    const timer = window.setTimeout(() => {
      setValidatingBatch(true);
      const rowsToValidate = rowsRef.current;
      void adminApi.validateBulkClassSections(rowsToValidate.map((row) => toRequest(row, semesterId)))
        .then((response) => {
          if (!active) return;
          setRows(rowsRef.current.map((row, index) => ({
            ...row,
            validation: response.items[index]?.validation ?? row.validation,
          })));
        })
        .catch((error: unknown) => {
          if (active) {
            toast.error(error instanceof Error ? error.message : "Không thể kiểm tra đề xuất");
          }
        })
        .finally(() => {
          if (active) setValidatingBatch(false);
        });
    }, 500);
    return () => {
      active = false;
      window.clearTimeout(timer);
    };
  }, [semesterId, setRows, step, validationKey]);

  const updateRow = (key: string, patch: Partial<RowState>) => {
    const nextRows = rows.map((row) => (row.key === key ? { ...row, ...patch } : row));
    setRows(markRowsWithLocalValidation(nextRows, existingRows, options.periods, options.rooms));
  };

  const updateRowSchedule = (
    rowKey: string,
    scheduleKey: string | undefined,
    patch: Partial<ClassSectionScheduleFormValue>,
  ) => {
    const nextRows = rows.map((row) => {
      if (row.key !== rowKey) return row;
      return {
        ...row,
        schedules: getSchedules(row).map((schedule) =>
          schedule.key === scheduleKey ? { ...schedule, ...patch } : schedule,
        ),
      };
    });
    setRows(markRowsWithLocalValidation(nextRows, existingRows, options.periods, options.rooms));
  };

  const addRowSchedule = (rowKey: string) => {
    const nextRows = rows.map((row) => {
      if (row.key !== rowKey) return row;
      const schedules = getSchedules(row);
      return { ...row, schedules: [...schedules, createDefaultSchedule(options, schedules.length)] };
    });
    setRows(markRowsWithLocalValidation(nextRows, existingRows, options.periods, options.rooms));
  };

  const removeRowSchedule = (rowKey: string, scheduleKey: string | undefined) => {
    const nextRows = rows.map((row) => {
      if (row.key !== rowKey) return row;
      const schedules = getSchedules(row);
      if (schedules.length <= 1) return row;
      return { ...row, schedules: schedules.filter((schedule) => schedule.key !== scheduleKey) };
    });
    setRows(markRowsWithLocalValidation(nextRows, existingRows, options.periods, options.rooms));
  };

  return (
    <Card>
      <CardHeader className="border-b">
        <CardTitle className="flex items-center gap-2 text-base">
          <Plus className="h-4 w-4" />
          Tạo nhiều lớp học phần
        </CardTitle>
        <p className="text-sm text-muted-foreground">
          Dùng khi một môn cần tạo nhiều lớp. Có thể chỉnh giảng viên, phòng và lịch học cho từng dòng trước khi tạo.
        </p>
      </CardHeader>
      <CardContent className="space-y-4 pt-4">
        <div className="flex gap-2 rounded-md border bg-muted/30 p-1">
          <Button size="sm" variant={step === "select" ? "default" : "ghost"} onClick={() => setStep("select")}>
            1. Chọn môn
          </Button>
          <Button size="sm" variant={step === "edit" ? "default" : "ghost"} onClick={() => setStep("edit")} disabled={rows.length === 0}>
            2. Chỉnh đề xuất
          </Button>
        </div>
        <div className={step === "select" ? "space-y-4" : "hidden"}>
          <CourseMultiSelect
            courses={options.courses}
            selectedIds={seed.courseIds}
            onChange={(courseIds) => {
              const configs = { ...seed.configs };
              courseIds.forEach((courseId) => {
                configs[courseId] = configs[courseId] ?? { ...defaultBulkCourseConfig };
              });
              setSeed({ courseIds, configs });
            }}
          />
          <div className="space-y-3 lg:col-span-4">
            {selectedCourses.map((course) => {
              const config = seed.configs[course.id] ?? defaultBulkCourseConfig;
              const updateConfig = (patch: Partial<BulkCourseConfig>) => {
                setSeed({
                  ...seed,
                  configs: {
                    ...seed.configs,
                    [course.id]: { ...config, ...patch },
                  },
                });
              };
              return (
                <div key={course.id} className="grid gap-3 rounded-md border bg-background p-3 lg:grid-cols-[minmax(220px,1fr)_140px_140px_150px_150px]">
                  <div>
                    <div className="font-medium">{course.code} - {course.name}</div>
                    <div className="text-xs text-muted-foreground">{course.departmentName ?? "Chưa rõ khoa/bộ môn"}</div>
                  </div>
                  <FieldInput label="Số lớp" type="number" value={config.count} onChange={(value) => updateConfig({ count: Number(value) })} />
                  <FieldInput label="Sĩ số/lớp" type="number" value={config.maxSlots} onChange={(value) => updateConfig({ maxSlots: Number(value) })} />
                  <FieldInput label="Buổi/tuần" type="number" value={config.sessionsPerWeek} onChange={(value) => updateConfig({ sessionsPerWeek: Number(value) })} />
                  <FieldInput label="Tiết/buổi" type="number" value={config.periodsPerSession} onChange={(value) => updateConfig({ periodsPerSession: Number(value) })} />
                </div>
              );
            })}
          </div>
          <div className="flex items-end">
            <Button
              className="w-full gap-2"
              onClick={() => proposalMutation.mutate()}
              disabled={seed.courseIds.length === 0 || proposalMutation.isPending}
            >
              <Wand2 className="h-4 w-4" />
              Tạo đề xuất
            </Button>
          </div>
        </div>

        <div className={step === "edit" ? "space-y-4" : "hidden"}>
          {summaries.some((summary) => summary.missingCount > 0) ? (
            <div className="space-y-1 rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              {summaries.filter((summary) => summary.missingCount > 0).map((summary) => (
                <div key={summary.courseId}>
                  {summary.courseCode}: đề xuất được {summary.proposedCount}/{summary.requestedCount} lớp,
                  còn thiếu {summary.missingCount}. {summary.message}
                </div>
              ))}
            </div>
          ) : null}
          {groupedRows.map(({ course, rows: courseRows }) => (
            <div key={course.id} className="rounded-md border bg-background">
              <div className="flex flex-wrap items-center justify-between gap-2 border-b p-3">
                <div>
                  <div className="font-medium">{course.code} - {course.name}</div>
                  <div className="text-xs text-muted-foreground">{courseRows.length} lớp đề xuất</div>
                </div>
                <Badge variant="outline">{course.departmentName ?? "Chưa rõ khoa/bộ môn"}</Badge>
              </div>
              <div className="space-y-3 p-3">
                {courseRows.map((row) => (
                  <div key={row.key} className="rounded-md border p-3">
                    <div className="grid gap-3 lg:grid-cols-[140px_minmax(180px,1fr)_120px_auto]">
                      <FieldInput label="Mã lớp" value={row.classCode} readOnly onChange={() => undefined} />
                      <FieldSelect label="Giảng viên" value={row.teacherId} onChange={(teacherId) => updateRow(row.key, { teacherId })}>
                        {getCompatibleTeachers(row.courseId, options).map((teacher) => (
                          <SelectItem key={teacher.id} value={String(teacher.id)}>{teacher.name}</SelectItem>
                        ))}
                      </FieldSelect>
                      <FieldInput label="Sĩ số" type="number" value={row.maxSlots} onChange={(maxSlots) => updateRow(row.key, { maxSlots: Number(maxSlots) })} />
                      <div className="flex items-end"><ValidationBadge row={row} /></div>
                    </div>
                    <div className="mt-3 space-y-2">
                      {getSchedules(row).map((schedule, index) => (
                        <div key={schedule.key} className="grid gap-2 rounded-md bg-muted/30 p-2 md:grid-cols-[110px_minmax(140px,1fr)_120px_120px_auto]">
                          <MiniSelect value={schedule.dayOfWeek} onChange={(dayOfWeek) => updateRowSchedule(row.key, schedule.key, { dayOfWeek })}>
                            {dayOptions.map((day) => <SelectItem key={day.value} value={String(day.value)}>{day.label}</SelectItem>)}
                          </MiniSelect>
                          <MiniSelect value={schedule.roomId} onChange={(roomId) => updateRowSchedule(row.key, schedule.key, { roomId })}>
                            {options.rooms.map((room) => <SelectItem key={room.id} value={String(room.id)}>{room.name} ({room.capacity})</SelectItem>)}
                          </MiniSelect>
                          <MiniSelect value={schedule.startPeriodId} onChange={(startPeriodId) => updateRowSchedule(row.key, schedule.key, { startPeriodId })}>
                            {options.periods.map((period) => <SelectItem key={period.id} value={String(period.id)}>{period.periodNumber}</SelectItem>)}
                          </MiniSelect>
                          <MiniSelect value={schedule.endPeriodId} onChange={(endPeriodId) => updateRowSchedule(row.key, schedule.key, { endPeriodId })}>
                            {options.periods.map((period) => <SelectItem key={period.id} value={String(period.id)}>{period.periodNumber}</SelectItem>)}
                          </MiniSelect>
                          <Button variant="outline" size="sm" disabled={getSchedules(row).length <= 1} onClick={() => removeRowSchedule(row.key, schedule.key)}>
                            Xóa buổi {index + 1}
                          </Button>
                        </div>
                      ))}
                      <Button variant="outline" size="sm" className="gap-2" onClick={() => addRowSchedule(row.key)}>
                        <Plus className="h-4 w-4" />
                        Thêm buổi học
                      </Button>
                    </div>
                    {row.validation?.errors.length ? (
                      <div className="mt-3 space-y-1 text-sm text-destructive">
                        {row.validation.errors.map((error) => <div key={`${row.key}-${error.message}`}>{error.message}</div>)}
                      </div>
                    ) : null}
                  </div>
                ))}
              </div>
            </div>
          ))}
          {rows.length === 0 ? (
            <div className="rounded-md border p-8 text-center text-sm text-muted-foreground">
              Chọn môn ở bước 1 rồi bấm Tạo đề xuất để bắt đầu.
            </div>
          ) : null}
        </div>

        <div className="hidden">
          <table className="w-full min-w-[1100px] text-sm">
            <thead className="bg-muted/50">
              <tr>
                <th className="p-3 text-left font-medium">Mã lớp</th>
                <th className="p-3 text-left font-medium">Giảng viên</th>
                <th className="p-3 text-left font-medium">Phòng</th>
                <th className="p-3 text-left font-medium">Thứ</th>
                <th className="p-3 text-left font-medium">Tiết bắt đầu</th>
                <th className="p-3 text-left font-medium">Tiết kết thúc</th>
                <th className="p-3 text-left font-medium">Sĩ số</th>
                <th className="p-3 text-left font-medium">Kiểm tra</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.key} className="border-t">
                  <td className="p-2"><Input className="h-9 w-36 bg-muted text-muted-foreground" value={row.classCode} readOnly /></td>
                  <td className="p-2"><MiniSelect value={row.teacherId} onChange={(teacherId) => updateRow(row.key, { teacherId })}>{getCompatibleTeachers(row.courseId, options).map((teacher) => <SelectItem key={teacher.id} value={String(teacher.id)}>{teacher.name}</SelectItem>)}</MiniSelect></td>
                  <td className="p-2"><MiniSelect value={row.roomId} onChange={(roomId) => updateRow(row.key, { roomId })}>{options.rooms.map((room) => <SelectItem key={room.id} value={String(room.id)}>{room.name}</SelectItem>)}</MiniSelect></td>
                  <td className="p-2"><MiniSelect value={row.dayOfWeek} onChange={(dayOfWeek) => updateRow(row.key, { dayOfWeek })}>{dayOptions.map((day) => <SelectItem key={day.value} value={String(day.value)}>{day.label}</SelectItem>)}</MiniSelect></td>
                  <td className="p-2"><MiniSelect value={row.startPeriodId} onChange={(startPeriodId) => updateRow(row.key, { startPeriodId })}>{options.periods.map((period) => <SelectItem key={period.id} value={String(period.id)}>{period.periodNumber}</SelectItem>)}</MiniSelect></td>
                  <td className="p-2"><MiniSelect value={row.endPeriodId} onChange={(endPeriodId) => updateRow(row.key, { endPeriodId })}>{options.periods.map((period) => <SelectItem key={period.id} value={String(period.id)}>{period.periodNumber}</SelectItem>)}</MiniSelect></td>
                  <td className="p-2"><Input className="h-9 w-24" type="number" value={row.maxSlots} onChange={(event) => updateRow(row.key, { maxSlots: Number(event.target.value) })} /></td>
                  <td className="p-2"><ValidationBadge row={row} /></td>
                </tr>
              ))}
              {rows.length === 0 && (
                <tr>
                  <td colSpan={8} className="p-8 text-center text-muted-foreground">
                    Chọn một hoặc nhiều môn học rồi bấm Tạo đề xuất để bắt đầu.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <div className={step === "edit" ? "rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800" : "hidden"}>
          Toàn bộ đề xuất được kiểm tra lại theo lô. Nếu một dòng lỗi, hệ thống sẽ không tạo bất kỳ lớp nào.
        </div>

        <div className={step === "edit" ? "flex flex-wrap items-center justify-between gap-3" : "hidden"}>
          <div className="text-sm text-muted-foreground">
            {validRows.length}/{rows.length} dòng hợp lệ{invalidRows.length ? `, ${invalidRows.length} dòng đang có lỗi` : ""}
          </div>
          <div className="flex gap-2">
            <Button
              onClick={onCreate}
              disabled={rows.length === 0 || validRows.length !== rows.length || creating || validatingBatch}
            >
              {creating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {validatingBatch ? "Đang kiểm tra..." : "Tạo tất cả lớp"}
            </Button>
          </div>
        </div>
      </CardContent>
      <Dialog open={proposalMutation.isPending}>
        <DialogContent
          className="max-w-md"
          onEscapeKeyDown={(event) => event.preventDefault()}
          onPointerDownOutside={(event) => event.preventDefault()}
        >
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Loader2 className="h-5 w-5 animate-spin text-primary" />
              Đang tạo đề xuất
            </DialogTitle>
            <DialogDescription>
              Hệ thống đang tìm giảng viên, phòng và lịch học phù hợp. Vui lòng chờ trong giây lát.
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      </Dialog>
    </Card>
  );
}

function SectionCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4 md:grid-cols-2">{children}</CardContent>
    </Card>
  );
}

function CourseMultiSelect({
  courses,
  selectedIds,
  onChange,
}: {
  courses: CourseOption[];
  selectedIds: number[];
  onChange: (ids: number[]) => void;
}) {
  const selectedCourses = courses.filter((course) => selectedIds.includes(course.id));
  return (
    <div className="space-y-1.5 text-sm font-medium">
      <span>Môn học</span>
      <div className="max-h-44 overflow-y-auto rounded-md border bg-background p-2">
        {courses.map((course) => {
          const checked = selectedIds.includes(course.id);
          return (
            <label key={course.id} className="flex cursor-pointer items-center gap-2 rounded px-2 py-1.5 hover:bg-muted">
              <input
                type="checkbox"
                checked={checked}
                onChange={(event) => {
                  if (event.target.checked) onChange([...selectedIds, course.id]);
                  else onChange(selectedIds.filter((id) => id !== course.id));
                }}
              />
              <span className="truncate font-normal">{course.code} - {course.name}</span>
            </label>
          );
        })}
        {courses.length === 0 && <div className="px-2 py-3 text-sm font-normal text-muted-foreground">Chưa có môn học</div>}
      </div>
      <div className="min-h-6 text-xs font-normal text-muted-foreground">
        {selectedCourses.length > 0
          ? `Đã chọn ${selectedCourses.length} môn`
          : "Chọn ít nhất một môn để tạo đề xuất"}
      </div>
    </div>
  );
}

function FieldInput({
  label,
  value,
  onChange,
  type = "text",
  readOnly = false,
}: {
  label: string;
  value: string | number;
  onChange: (value: string) => void;
  type?: string;
  readOnly?: boolean;
}) {
  return (
    <label className="space-y-1.5 text-sm font-medium">
      <span>{label}</span>
      <Input
        type={type}
        value={value}
        readOnly={readOnly}
        className={readOnly ? "bg-muted text-muted-foreground" : undefined}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

function FieldSelect({ label, value, onChange, disabled, children }: { label: string; value: string | number; onChange: (value: number) => void; disabled?: boolean; children: React.ReactNode }) {
  return (
    <label className="space-y-1.5 text-sm font-medium">
      <span>{label}</span>
      <Select value={String(value)} onValueChange={(next) => onChange(Number(next))} disabled={disabled}>
        <SelectTrigger><SelectValue /></SelectTrigger>
        <SelectContent>{children}</SelectContent>
      </Select>
    </label>
  );
}

function MiniSelect({ value, onChange, children }: { value: string | number; onChange: (value: number) => void; children: React.ReactNode }) {
  return (
    <Select value={String(value)} onValueChange={(next) => onChange(Number(next))}>
      <SelectTrigger className="h-9 min-w-28"><SelectValue /></SelectTrigger>
      <SelectContent>{children}</SelectContent>
    </Select>
  );
}

function ValidationPanel({ loading, ready, errors, infos, warnings }: { loading: boolean; ready: boolean; errors: ClassSectionValidationIssueResponse[]; infos: ClassSectionValidationIssueResponse[]; warnings: ClassSectionValidationIssueResponse[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-base">
          {errors.length > 0 ? <XCircle className="h-4 w-4 text-destructive" /> : warnings.length > 0 ? <AlertTriangle className="h-4 w-4 text-amber-600" /> : infos.length > 0 ? <Info className="h-4 w-4 text-sky-700" /> : <CheckCircle2 className="h-4 w-4 text-emerald-600" />}
          Kiểm tra lớp học phần
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3 text-sm">
        {!ready && <p className="text-muted-foreground">Nhập đủ thông tin để hệ thống kiểm tra.</p>}
        {loading && <p className="inline-flex items-center gap-2 text-muted-foreground"><Loader2 className="h-4 w-4 animate-spin" />Đang kiểm tra...</p>}
        {errors.map((item) => <IssueRow key={`${item.code}-${item.message}`} tone="error" item={item} />)}
        {warnings.map((item) => <IssueRow key={`${item.code}-${item.message}`} tone="warning" item={item} />)}
        {infos.map((item) => <IssueRow key={`${item.code}-${item.message}`} tone="info" item={item} />)}
        {ready && !loading && errors.length === 0 && warnings.length === 0 && infos.length === 0 && <IssueRow tone="success" item={{ code: "VALID", message: "Thông tin lớp học phần hợp lệ" }} />}
      </CardContent>
    </Card>
  );
}

function IssueRow({ tone, item }: { tone: "success" | "info" | "warning" | "error"; item: ClassSectionValidationIssueResponse }) {
  const Icon = tone === "success" ? CheckCircle2 : tone === "info" ? Info : tone === "warning" ? AlertTriangle : XCircle;
  const color = tone === "success" ? "text-emerald-700" : tone === "info" ? "text-sky-700" : tone === "warning" ? "text-amber-700" : "text-destructive";
  return <div className={`flex gap-2 ${color}`}><Icon className="mt-0.5 h-4 w-4 shrink-0" /><span>{item.message}</span></div>;
}

function ValidationBadge({ row }: { row: RowState }) {
  if (!row.validation) return <Badge variant="outline">Chưa kiểm tra</Badge>;
  if (row.validation.errors.length) return <Badge variant="destructive">Lỗi</Badge>;
  if (row.validation.warnings.length) return <Badge className="bg-amber-100 text-amber-800 hover:bg-amber-100">Cảnh báo</Badge>;
  return <Badge className="bg-emerald-100 text-emerald-800 hover:bg-emerald-100">Hợp lệ</Badge>;
}

function PreviewPanel({ values, options }: { values: ClassSectionFormValues; options: ReturnType<typeof buildOptionSets> }) {
  const course = options.courses.find((item) => item.id === values.courseId);
  const teacher = options.teachers.find((item) => item.id === values.teacherId);
  const schedules = getSchedules(values);
  return (
    <Card>
      <CardHeader><CardTitle className="text-base">Preview lớp học phần</CardTitle></CardHeader>
      <CardContent className="space-y-2 text-sm">
        <PreviewLine label="Mã lớp" value={values.classCode || "-"} />
        <PreviewLine label="Môn học" value={course ? `${course.code} - ${course.name}` : "-"} />
        <PreviewLine label="Giảng viên" value={teacher?.name ?? "-"} />
        {schedules.map((schedule, index) => {
          const room = options.rooms.find((item) => item.id === schedule.roomId);
          const start = options.periods.find((item) => item.id === schedule.startPeriodId);
          const end = options.periods.find((item) => item.id === schedule.endPeriodId);
          const day = dayOptions.find((item) => item.value === schedule.dayOfWeek)?.label ?? "-";
          return (
            <PreviewLine
              key={schedule.key ?? index}
              label={`Buổi ${index + 1}`}
              value={`${day}, tiết ${start?.periodNumber ?? "?"}-${end?.periodNumber ?? "?"}${room ? ` - ${room.name}` : ""}`}
            />
          );
        })}
        <PreviewLine label="Sĩ số" value={`${values.maxSlots || 0} sinh viên`} />
      </CardContent>
    </Card>
  );
}

function PreviewLine({ label, value }: { label: string; value: string }) {
  return <div className="flex items-start justify-between gap-3"><span className="text-muted-foreground">{label}</span><span className="text-right font-medium">{value}</span></div>;
}

function toRequest(values: ClassSectionFormValues, semesterId: number): AdminClassSectionRequest {
  return {
    classCode: values.classCode.trim(),
    courseId: values.courseId,
    semesterId,
    teacherId: values.teacherId,
    maxSlots: Number(values.maxSlots),
    schedules: getSchedules(values).map((schedule) => ({
      dayOfWeek: schedule.dayOfWeek,
      startPeriodId: schedule.startPeriodId,
      endPeriodId: schedule.endPeriodId,
      roomId: schedule.roomId,
    })),
  };
}

function isReady(values: ClassSectionFormValues) {
  return Boolean(values.classCode.trim())
    && values.courseId > 0
    && values.teacherId > 0
    && values.maxSlots > 0
    && getSchedules(values).every((schedule) =>
      schedule.roomId > 0
      && schedule.dayOfWeek > 0
      && schedule.startPeriodId > 0
      && schedule.endPeriodId > 0,
    );
}

function isRowValid(row: RowState) {
  return isReady(row) && Boolean(row.validation) && row.validation.errors.length === 0;
}

function createDefaultSchedule(
  options: ReturnType<typeof buildOptionSets>,
  index: number,
): ClassSectionScheduleFormValue {
  const start = options.periods[(index * 2) % Math.max(options.periods.length, 1)];
  const end = options.periods[
    Math.min(options.periods.findIndex((item) => item.id === start?.id) + 1, options.periods.length - 1)
  ] ?? start;
  return {
    key: `schedule-${Date.now()}-${index}`,
    roomId: options.rooms[index % Math.max(options.rooms.length, 1)]?.id ?? 0,
    dayOfWeek: dayOptions[index % dayOptions.length].value,
    startPeriodId: start?.id ?? 0,
    endPeriodId: end?.id ?? start?.id ?? 0,
  };
}

function getSchedules(values: ClassSectionFormValues): ClassSectionScheduleFormValue[] {
  if (values.schedules?.length) return values.schedules;
  return [
    {
      key: "legacy-schedule",
      roomId: values.roomId,
      dayOfWeek: values.dayOfWeek,
      startPeriodId: values.startPeriodId,
      endPeriodId: values.endPeriodId,
    },
  ];
}

function markRowsWithLocalValidation(
  rows: RowState[],
  existingRows: ReturnType<typeof mapApiClassSection>[],
  periods: ReturnType<typeof buildOptionSets>["periods"],
  rooms: ReturnType<typeof buildOptionSets>["rooms"],
) {
  const acceptedRows: ReturnType<typeof mapApiClassSection>[] = [...existingRows];
  return rows.map((row) => {
    const duplicateCode = acceptedRows.some(
      (existing) => existing.semesterId === row.semesterId && existing.classCode === row.classCode,
    );
    const localMessages = duplicateCode
      ? "Mã lớp học phần đã tồn tại trong học kỳ này."
      : validateClassSectionPlanErrors({
          values: row,
          rows: acceptedRows,
          periods,
          rooms,
        });
    const messages = Array.isArray(localMessages) ? localMessages : [localMessages].filter(Boolean);
    const validation = messages.length
      ? {
          valid: false,
          errors: messages.map((message, index) => ({ code: `CLIENT_VALIDATION_${index}`, message })),
          warnings: [],
        }
      : { valid: true, errors: [], warnings: [] };

    if (!messages.length) {
      acceptedRows.push(...rowToConflictRows(row, periods, rooms));
    }

    return { ...row, validation };
  });
}

function rowToConflictRows(
  row: RowState,
  periods: ReturnType<typeof buildOptionSets>["periods"],
  rooms: ReturnType<typeof buildOptionSets>["rooms"],
): ReturnType<typeof mapApiClassSection>[] {
  return getSchedules(row).map((schedule, index) => {
    const room = rooms.find((item) => item.id === schedule.roomId);
    return {
      id: `${row.key}-${index}`,
      classCode: row.classCode,
      courseId: row.courseId,
      courseName: row.classCode.split("-")[0],
      majorName: "-",
      semesterId: row.semesterId,
      semesterName: "",
      teacherId: row.teacherId,
      teacherName: "",
      roomId: schedule.roomId,
      roomName: room?.name ?? "",
      dayOfWeek: schedule.dayOfWeek,
      startPeriodId: schedule.startPeriodId,
      startPeriod: findPeriodNumber(periods, schedule.startPeriodId),
      endPeriodId: schedule.endPeriodId,
      endPeriod: findPeriodNumber(periods, schedule.endPeriodId),
      currentSlots: 0,
      maxSlots: row.maxSlots,
      status: "OPEN",
      source: "API",
    };
  });
}

function findPeriodNumber(periods: ReturnType<typeof buildOptionSets>["periods"], periodId: number) {
  return periods.find((period) => period.id === periodId)?.periodNumber ?? periodId;
}

function getCompatibleTeachers(courseId: number, options: ReturnType<typeof buildOptionSets>) {
  const course = options.courses.find((item) => item.id === courseId);
  if (!course?.departmentId) return options.teachers;
  return options.teachers.filter((teacher) => teacher.departmentId === course.departmentId);
}

function nextClassCode(courseCode: string, rows: Array<{ classCode: string }>) {
  if (!courseCode) return "";
  const usedNumbers = rows
    .map((row) => row.classCode.match(new RegExp(`^${escapeRegex(courseCode)}-(\\d+)$`))?.[1])
    .filter(Boolean)
    .map((value) => Number(value));
  const next = Math.max(0, ...usedNumbers) + 1;
  return `${courseCode}-${String(next).padStart(2, "0")}`;
}

function countSafe(value: number) {
  return Math.max(1, Math.min(value || 1, 20));
}

function escapeRegex(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function formatDate(value?: string | null) {
  if (!value) return "?";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat("vi-VN").format(date);
}
