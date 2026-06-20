import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { PageHeader } from "@/components/ui/page-header";
import { TimetableGrid, type TimetableCell } from "@/components/timetable/TimetableGrid";
import type { ClassSectionResponse, StudentSemesterResponse } from "@/lib/api/types";
import { teacherApi } from "@/lib/api/teacher";
import {
  useTeacherSemester,
  type TeacherSemesterOption,
} from "@/features/teacher/useTeacherSemester";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export const Route = createFileRoute("/teacher/timetable")({ component: TeacherTimetablePage });

type ScheduleCell = TimetableCell & {
  courseName: string;
  courseCode: string;
  classCode: string;
  roomName: string;
  lessonCount: number;
  periodRange: string;
  startTime: string;
  endTime: string;
};

function formatApiTime(value: string | null | undefined) {
  if (!value) return "";
  const [hour = "", minute = ""] = value.split(":");
  return hour && minute ? `${hour}:${minute}` : value;
}

function formatSemesterDate(value?: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("vi-VN").format(new Date(value));
}

function formatSemesterRange(semester?: StudentSemesterResponse | null) {
  if (!semester) return "Lịch dạy của giảng viên theo học kỳ, phòng học và tiết học";
  return `${semester.name}: ${formatSemesterDate(semester.startDate)} - ${formatSemesterDate(semester.endDate)}`;
}

function buildScheduleCells(classes: ClassSectionResponse[]) {
  const cells: Record<string, ScheduleCell | null> = {};

  classes.forEach((section) => {
    (section.schedules ?? []).forEach((slot) => {
      const startPeriod = slot.startPeriod;
      const endPeriod = slot.endPeriod;
      if (!slot.dayOfWeek || !startPeriod || !endPeriod) return;

      const rowSpan = Math.max(endPeriod - startPeriod + 1, 1);
      for (let period = startPeriod; period <= endPeriod; period += 1) {
        cells[`${slot.dayOfWeek}-${period}`] = {
          courseName: section.courseName,
          courseCode: section.courseCode,
          classCode: section.classCode,
          roomName: slot.roomName ?? section.room ?? "",
          lessonCount: slot.lessonCount ?? rowSpan,
          periodRange: slot.periodRange ?? `${startPeriod}-${endPeriod}`,
          startTime: formatApiTime(slot.startTime),
          endTime: formatApiTime(slot.endTime),
          rowSpan,
          isStart: period === startPeriod,
        };
      }
    });
  });

  return cells;
}

function TeacherTimetablePage() {
  const { semesterId, setSemesterId, semesterOptions, semestersQuery } = useTeacherSemester();

  const classesQuery = useQuery({
    queryKey: ["teacher", "classes", semesterId],
    queryFn: () => teacherApi.listMyClasses(semesterId),
    enabled: Boolean(semesterId),
    retry: false,
  });

  const currentSemester = semestersQuery.data?.find(
    (semester) => String(semester.id) === semesterId,
  );
  const cells = buildScheduleCells(classesQuery.data ?? []);
  const hasSchedule = Object.keys(cells).length > 0;

  return (
    <div className="space-y-5">
      <PageHeader
        title="Thời khóa biểu"
        description={formatSemesterRange(currentSemester)}
        actions={
          <SemesterFilter
            value={semesterId}
            options={semesterOptions}
            disabled={semestersQuery.isLoading || semesterOptions.length === 0}
            onValueChange={setSemesterId}
          />
        }
      />

      <TimetableGrid cells={cells} renderCell={(cell) => <TeacherScheduleCard cell={cell} />} />

      {classesQuery.isLoading && (
        <div className="text-sm text-muted-foreground">Đang tải thời khóa biểu...</div>
      )}
      {!classesQuery.isLoading && !classesQuery.isError && semesterId && !hasSchedule && (
        <div className="text-sm text-muted-foreground">Chưa có lịch dạy trong học kỳ này.</div>
      )}
      {classesQuery.isError && (
        <div className="text-sm text-destructive">
          {classesQuery.error instanceof Error
            ? classesQuery.error.message
            : "Không tải được thời khóa biểu"}
        </div>
      )}
      {semestersQuery.isError && (
        <div className="text-sm text-destructive">
          {semestersQuery.error instanceof Error
            ? semestersQuery.error.message
            : "Không tải được danh sách học kỳ"}
        </div>
      )}
    </div>
  );
}

function TeacherScheduleCard({ cell }: { cell: ScheduleCell }) {
  return (
    <div className="flex h-full min-h-16 w-full max-w-full flex-col overflow-hidden rounded-md border border-primary/30 bg-card p-2 text-xs leading-tight shadow-sm">
      <div className="truncate font-semibold text-primary">{cell.courseName}</div>
      <div className="font-mono text-[10px] text-muted-foreground">
        {cell.classCode} - {cell.courseCode}
      </div>
      <div className="mt-1 text-muted-foreground">Phòng: {cell.roomName}</div>
      <div className="text-[10px] text-muted-foreground">Số tiết: {cell.lessonCount}</div>
      <div className="text-[10px] text-muted-foreground">Tiết: {cell.periodRange}</div>
      {cell.startTime && (
        <div className="text-[10px] text-muted-foreground">
          {cell.startTime}
          {cell.endTime ? ` - ${cell.endTime}` : ""}
        </div>
      )}
    </div>
  );
}

function SemesterFilter({
  value,
  options,
  disabled,
  onValueChange,
}: {
  value: string;
  options: TeacherSemesterOption[];
  disabled?: boolean;
  onValueChange: (value: string) => void;
}) {
  return (
    <Select value={value} onValueChange={onValueChange} disabled={disabled}>
      <SelectTrigger className="w-[280px]">
        <SelectValue placeholder="Chọn học kỳ" />
      </SelectTrigger>
      <SelectContent>
        {options.map((semester) => (
          <SelectItem key={semester.id} value={semester.id}>
            {semester.name}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
