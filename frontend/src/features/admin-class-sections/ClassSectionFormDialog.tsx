import { zodResolver } from "@hookform/resolvers/zod";
import { Loader2, Plus, Trash2 } from "lucide-react";
import type { ReactNode } from "react";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { StatusBadge } from "@/components/ui/status-badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ClassSectionSelectField } from "./ClassSectionSelectField";
import type {
  ClassSectionFormValues,
  ClassSectionOptionSets,
  ClassSectionRow,
  ClassSectionScheduleFormValue,
} from "./types";

const dayOptions = [
  { value: 2, label: "Thứ 2" },
  { value: 3, label: "Thứ 3" },
  { value: 4, label: "Thứ 4" },
  { value: 5, label: "Thứ 5" },
  { value: 6, label: "Thứ 6" },
  { value: 7, label: "Thứ 7" },
  { value: 8, label: "Chủ nhật" },
];

const scheduleSchema = z.object({
  key: z.string().optional(),
  roomId: z.coerce.number().min(1, "Bắt buộc chọn phòng học"),
  dayOfWeek: z.coerce.number().min(2).max(8),
  startPeriodId: z.coerce.number().min(1, "Bắt buộc chọn tiết bắt đầu"),
  endPeriodId: z.coerce.number().min(1, "Bắt buộc chọn tiết kết thúc"),
});

const classSectionSchema = z.object({
  classCode: z.string().trim().min(1, "Mã lớp không được để trống"),
  courseId: z.coerce.number().min(1, "Bắt buộc chọn môn học"),
  semesterId: z.coerce.number().min(1, "Bắt buộc chọn học kỳ"),
  registrationRoundId: z.coerce.number().optional().nullable(),
  teacherId: z.coerce.number().min(1, "Bắt buộc chọn giảng viên"),
  roomId: z.coerce.number().min(1, "Bắt buộc chọn phòng học"),
  dayOfWeek: z.coerce.number().min(2).max(8),
  startPeriodId: z.coerce.number().min(1, "Bắt buộc chọn tiết bắt đầu"),
  endPeriodId: z.coerce.number().min(1, "Bắt buộc chọn tiết kết thúc"),
  maxSlots: z.coerce.number().int().min(1, "Sĩ số tối đa phải lớn hơn 0"),
  status: z.enum(["DRAFT", "OPEN", "CLOSED", "CANCELLED"]),
  schedules: z.array(scheduleSchema).min(1, "Cần có ít nhất một buổi học"),
});

interface ClassSectionFormDialogProps {
  open: boolean;
  editing: ClassSectionRow | null;
  options: ClassSectionOptionSets;
  isPending: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (values: ClassSectionFormValues) => void;
}

export function ClassSectionFormDialog({
  open,
  editing,
  options,
  isPending,
  onOpenChange,
  onSubmit,
}: ClassSectionFormDialogProps) {
  const form = useForm<ClassSectionFormValues>({
    resolver: zodResolver(classSectionSchema),
    defaultValues: getDefaultValues(editing, options),
  });

  const selectedCourseId = form.watch("courseId");
  const selectedCourse = options.courses.find((course) => course.id === Number(selectedCourseId));
  const teacherOptions = selectedCourse?.departmentId
    ? options.teachers.filter((teacher) => teacher.departmentId === selectedCourse.departmentId)
    : options.teachers;
  const schedules = form.watch("schedules")?.length
    ? form.watch("schedules")
    : [createDefaultSchedule(options, 0)];

  useEffect(() => {
    if (open) form.reset(getDefaultValues(editing, options));
  }, [editing, form, open, options]);

  useEffect(() => {
    if (!open) return;
    if (teacherOptions.length === 0) {
      if (Number(form.getValues("teacherId")) !== 0) {
        form.setValue("teacherId", 0, { shouldValidate: true });
      }
      return;
    }
    const currentTeacherId = Number(form.getValues("teacherId"));
    if (!teacherOptions.some((teacher) => teacher.id === currentTeacherId)) {
      form.setValue("teacherId", teacherOptions[0].id, { shouldValidate: true });
    }
  }, [form, open, teacherOptions]);

  const updateSchedule = (key: string | undefined, patch: Partial<ClassSectionScheduleFormValue>) => {
    form.setValue(
      "schedules",
      schedules.map((schedule) => (schedule.key === key ? { ...schedule, ...patch } : schedule)),
      { shouldDirty: true, shouldValidate: true },
    );
  };

  const addSchedule = () => {
    form.setValue(
      "schedules",
      [...schedules, createDefaultSchedule(options, schedules.length)],
      { shouldDirty: true, shouldValidate: true },
    );
  };

  const removeSchedule = (key: string | undefined) => {
    if (schedules.length <= 1) return;
    form.setValue(
      "schedules",
      schedules.filter((schedule) => schedule.key !== key),
      { shouldDirty: true, shouldValidate: true },
    );
  };

  const submit = (values: ClassSectionFormValues) => {
    const firstSchedule = values.schedules?.[0];
    onSubmit({
      ...values,
      roomId: firstSchedule?.roomId ?? values.roomId,
      dayOfWeek: firstSchedule?.dayOfWeek ?? values.dayOfWeek,
      startPeriodId: firstSchedule?.startPeriodId ?? values.startPeriodId,
      endPeriodId: firstSchedule?.endPeriodId ?? values.endPeriodId,
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-4xl">
        <DialogHeader>
          <DialogTitle>{editing ? "Sửa lớp học phần" : "Mở lớp học phần"}</DialogTitle>
          <DialogDescription>
            Chọn môn học, học kỳ, giảng viên và một hoặc nhiều buổi học trong tuần.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(submit)} className="space-y-4">
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="classCode"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Mã lớp học phần</FormLabel>
                    <FormControl>
                      <Input placeholder="JAVA101-01" readOnly className="bg-muted text-muted-foreground" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              {editing && (
                <div className="space-y-2">
                  <div className="text-sm font-medium">Trạng thái</div>
                  <div className="flex h-10 items-center">
                    <StatusBadge value={editing.status} />
                  </div>
                </div>
              )}
              <ClassSectionSelectField
                control={form.control}
                name="courseId"
                label="Môn học"
                options={options.courses.map((course) => ({
                  value: course.id,
                  label: `${course.code} - ${course.name}`,
                }))}
              />
              <ClassSectionSelectField
                control={form.control}
                name="semesterId"
                label="Học kỳ"
                options={options.semesters.map((semester) => ({
                  value: semester.id,
                  label: semester.name,
                }))}
              />
              <ClassSectionSelectField
                control={form.control}
                name="teacherId"
                label="Giảng viên"
                options={teacherOptions.map((teacher) => ({
                  value: teacher.id,
                  label: teacher.departmentName ? `${teacher.name} - ${teacher.departmentName}` : teacher.name,
                }))}
              />
              <FormField
                control={form.control}
                name="maxSlots"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Sĩ số tối đa</FormLabel>
                    <FormControl>
                      <Input type="number" min={1} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="space-y-3">
              <div className="font-medium">Lịch học</div>
              {schedules.map((schedule, index) => (
                <div
                  key={schedule.key}
                  className="grid gap-3 rounded-md border bg-background p-3 md:grid-cols-[1fr_1fr_1fr_1fr_auto]"
                >
                  <ScheduleSelect
                    label={`Buổi ${index + 1}`}
                    value={schedule.dayOfWeek}
                    onChange={(dayOfWeek) => updateSchedule(schedule.key, { dayOfWeek })}
                  >
                    {dayOptions.map((day) => (
                      <SelectItem key={day.value} value={String(day.value)}>{day.label}</SelectItem>
                    ))}
                  </ScheduleSelect>
                  <ScheduleSelect
                    label="Phòng"
                    value={schedule.roomId}
                    onChange={(roomId) => updateSchedule(schedule.key, { roomId })}
                  >
                    {options.rooms.map((room) => (
                      <SelectItem key={room.id} value={String(room.id)}>{room.name} ({room.capacity})</SelectItem>
                    ))}
                  </ScheduleSelect>
                  <ScheduleSelect
                    label="Tiết bắt đầu"
                    value={schedule.startPeriodId}
                    onChange={(startPeriodId) => updateSchedule(schedule.key, { startPeriodId })}
                  >
                    {options.periods.map((period) => (
                      <SelectItem key={period.id} value={String(period.id)}>{period.label}</SelectItem>
                    ))}
                  </ScheduleSelect>
                  <ScheduleSelect
                    label="Tiết kết thúc"
                    value={schedule.endPeriodId}
                    onChange={(endPeriodId) => updateSchedule(schedule.key, { endPeriodId })}
                  >
                    {options.periods.map((period) => (
                      <SelectItem key={period.id} value={String(period.id)}>{period.label}</SelectItem>
                    ))}
                  </ScheduleSelect>
                  <div className="flex items-end">
                    <Button
                      type="button"
                      variant="outline"
                      size="icon"
                      disabled={schedules.length <= 1}
                      onClick={() => removeSchedule(schedule.key)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              ))}
              <Button type="button" variant="outline" className="gap-2" onClick={addSchedule}>
                <Plus className="h-4 w-4" />
                Thêm buổi học
              </Button>
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Hủy
              </Button>
              <Button type="submit" disabled={isPending}>
                {isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                {editing ? "Lưu thay đổi" : "Mở lớp"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

function ScheduleSelect({
  label,
  value,
  onChange,
  children,
}: {
  label: string;
  value: number;
  onChange: (value: number) => void;
  children: ReactNode;
}) {
  return (
    <label className="space-y-1.5 text-sm font-medium">
      <span>{label}</span>
      <Select value={String(value)} onValueChange={(next) => onChange(Number(next))}>
        <SelectTrigger>
          <SelectValue />
        </SelectTrigger>
        <SelectContent>{children}</SelectContent>
      </Select>
    </label>
  );
}

function getDefaultValues(
  editing: ClassSectionRow | null,
  options: ClassSectionOptionSets,
): ClassSectionFormValues {
  const courseId = editing?.courseId ?? options.courses[0]?.id ?? 0;
  const selectedCourse = options.courses.find((course) => course.id === courseId);
  const teacherOptions = selectedCourse?.departmentId
    ? options.teachers.filter((teacher) => teacher.departmentId === selectedCourse.departmentId)
    : options.teachers;
  const schedules = editing?.schedules?.length
    ? editing.schedules.map((schedule, index) => ({
        ...schedule,
        key: schedule.key ?? `editing-schedule-${index}`,
      }))
    : [createDefaultSchedule(options, 0)];
  const firstSchedule = schedules[0];

  return {
    classCode: editing?.classCode ?? "",
    courseId,
    semesterId: editing?.semesterId ?? options.semesters[0]?.id ?? 0,
    registrationRoundId: editing?.registrationRoundId ?? null,
    teacherId: editing?.teacherId ?? teacherOptions[0]?.id ?? 0,
    roomId: firstSchedule?.roomId ?? options.rooms[0]?.id ?? 0,
    dayOfWeek: firstSchedule?.dayOfWeek ?? 2,
    startPeriodId: firstSchedule?.startPeriodId ?? options.periods[0]?.id ?? 0,
    endPeriodId: firstSchedule?.endPeriodId ?? options.periods[1]?.id ?? options.periods[0]?.id ?? 0,
    maxSlots: editing?.maxSlots ?? 40,
    status: editing?.status ?? "DRAFT",
    schedules,
  };
}

function createDefaultSchedule(
  options: ClassSectionOptionSets,
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
