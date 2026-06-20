import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { PageHeader } from "@/components/ui/page-header";
import { TimetableGrid, type TimetableCell } from "@/components/timetable/TimetableGrid";
import { studentApi } from "@/lib/api/student";
import { pickCurrentSemester } from "@/lib/semester";
import type { EnrollmentResponse, StudentSemesterResponse } from "@/lib/api/types";

export const Route = createFileRoute("/student/schedule")({ component: SchedulePage });

const emptySemesters: StudentSemesterResponse[] = [];

type ScheduleCell = TimetableCell & {
  name: string;
  room: string;
  code: string;
  lessonCount: number;
  periodRange: string;
  startTime: string;
  endTime: string;
  teacherName: string;
  teacherCode: string;
  teacherEmail: string;
};

function formatApiTime(value: string | null | undefined) {
  if (!value) return "";
  const [hour = "", minute = ""] = value.split(":");
  return hour && minute ? `${hour}g${minute}` : value;
}

function formatSemesterDate(value?: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("vi-VN").format(new Date(value));
}

function formatSemesterRange(semester?: StudentSemesterResponse | null) {
  if (!semester) return "Lịch học theo học kỳ";
  return `${semester.name}: ${formatSemesterDate(semester.startDate)} - ${formatSemesterDate(semester.endDate)}`;
}

function getScheduleSlots(item: EnrollmentResponse) {
  if (item.schedules?.length) {
    return item.schedules.map((slot) => ({
      dayOfWeek: slot.dayOfWeek,
      startPeriod: slot.startPeriod,
      endPeriod: slot.endPeriod,
      room: slot.roomName ?? item.room ?? "",
      lessonCount: slot.lessonCount ?? Math.max(slot.endPeriod - slot.startPeriod + 1, 1),
      periodRange: slot.periodRange ?? `${slot.startPeriod}-${slot.endPeriod}`,
      startTime: formatApiTime(slot.startTime),
      endTime: formatApiTime(slot.endTime),
    }));
  }

  return [
    {
      dayOfWeek: item.dayOfWeek,
      startPeriod: item.startPeriod,
      endPeriod: item.endPeriod,
      room: item.room ?? "",
      lessonCount: Math.max(item.endPeriod - item.startPeriod + 1, 1),
      periodRange: `${item.startPeriod}-${item.endPeriod}`,
      startTime: "",
      endTime: "",
    },
  ];
}

function SchedulePage() {
  const semestersQuery = useQuery({
    queryKey: ["student", "semesters"],
    queryFn: studentApi.listSemesters,
  });
  const semesters = semestersQuery.data ?? emptySemesters;
  const [semesterId, setSemesterId] = useState<number | null>(null);

  useEffect(() => {
    if (!semesterId && semesters.length) setSemesterId(pickCurrentSemester(semesters)?.id ?? null);
  }, [semesterId, semesters]);

  const scheduleQuery = useQuery({
    queryKey: ["student", "schedule", semesterId],
    queryFn: () => studentApi.getSchedule(semesterId as number),
    enabled: semesterId != null,
  });

  const currentSemester = semesters.find((semester) => semester.id === semesterId);
  const cells: Record<string, ScheduleCell | null> = {};
  (scheduleQuery.data ?? []).forEach((item) => {
    getScheduleSlots(item).forEach((slot) => {
      const rowSpan = Math.max(slot.endPeriod - slot.startPeriod + 1, 1);
      for (let p = slot.startPeriod; p <= slot.endPeriod; p += 1) {
        cells[`${slot.dayOfWeek}-${p}`] = {
          name: item.courseName,
          room: slot.room,
          code: item.classCode,
          lessonCount: slot.lessonCount,
          periodRange: slot.periodRange,
          startTime: slot.startTime,
          endTime: slot.endTime,
          teacherName: item.teacherName ?? "",
          teacherCode: item.teacherCode ?? "",
          teacherEmail: item.teacherEmail ?? "",
          rowSpan,
          isStart: p === slot.startPeriod,
        };
      }
    });
  });

  return (
    <div>
      <PageHeader
        title="Thời khóa biểu"
        description={formatSemesterRange(currentSemester)}
        actions={
          <select
            className="h-9 rounded-md border bg-background px-3 text-sm"
            value={semesterId ?? ""}
            onChange={(e) => setSemesterId(Number(e.target.value))}
          >
            {semesters.map((semester) => (
              <option key={semester.id} value={semester.id}>
                {semester.name}
              </option>
            ))}
          </select>
        }
      />

      <TimetableGrid cells={cells} renderCell={(cell) => <StudentScheduleCard cell={cell} />} />

      {scheduleQuery.isError && (
        <div className="mt-4 text-sm text-destructive">
          {scheduleQuery.error instanceof Error
            ? scheduleQuery.error.message
            : "Không tải được thời khóa biểu"}
        </div>
      )}
    </div>
  );
}

function StudentScheduleCard({ cell }: { cell: ScheduleCell }) {
  return (
    <div className="flex h-full min-h-16 w-full max-w-full flex-col overflow-hidden rounded-md border border-primary/30 bg-card p-2 text-xs leading-tight">
      <div className="truncate font-semibold text-primary">{cell.name}</div>
      <div className="font-mono text-[10px] text-muted-foreground">{cell.code}</div>
      <div className="text-muted-foreground">Phòng: {cell.room}</div>
      <div className="mt-1 text-[10px] text-muted-foreground">Số tiết: {cell.lessonCount}</div>
      <div className="text-[10px] text-muted-foreground">Tiết: {cell.periodRange}</div>
      {cell.startTime && (
        <div className="text-[10px] text-muted-foreground">
          Bắt đầu: {cell.startTime}
          {cell.endTime ? ` - ${cell.endTime}` : ""}
        </div>
      )}
      {cell.teacherName && (
        <div className="text-[10px] text-info">
          GV: {cell.teacherName}
          {cell.teacherCode ? ` (${cell.teacherCode})` : ""}
        </div>
      )}
      {cell.teacherEmail && (
        <div className="break-all text-[10px] text-info">Email: {cell.teacherEmail}</div>
      )}
    </div>
  );
}
