import type { ClassSectionFormValues, ClassSectionRow, PeriodOption, RoomOption } from "./types";

export function validateClassSectionPlan({
  values,
  rows,
  periods,
  rooms,
  editingId,
}: {
  values: ClassSectionFormValues;
  rows: ClassSectionRow[];
  periods: PeriodOption[];
  rooms: RoomOption[];
  editingId?: string;
}): string | null {
  return validateClassSectionPlanErrors({ values, rows, periods, rooms, editingId })[0] ?? null;
}

export function validateClassSectionPlanErrors({
  values,
  rows,
  periods,
  rooms,
  editingId,
}: {
  values: ClassSectionFormValues;
  rows: ClassSectionRow[];
  periods: PeriodOption[];
  rooms: RoomOption[];
  editingId?: string;
}): string[] {
  const errors: string[] = [];
  const schedules = values.schedules?.length
    ? values.schedules
    : [
        {
          roomId: values.roomId,
          dayOfWeek: values.dayOfWeek,
          startPeriodId: values.startPeriodId,
          endPeriodId: values.endPeriodId,
        },
      ];

  for (const schedule of schedules) {
    const startPeriod = findPeriodNumber(periods, schedule.startPeriodId);
    const endPeriod = findPeriodNumber(periods, schedule.endPeriodId);

    if (startPeriod > endPeriod) {
      errors.push("Tiết bắt đầu phải nhỏ hơn hoặc bằng tiết kết thúc.");
    }

    const scheduleRoom = rooms.find((room) => room.id === schedule.roomId);
    if (scheduleRoom && values.maxSlots > scheduleRoom.capacity) {
      errors.push(`Sĩ số tối đa vượt sức chứa phòng ${scheduleRoom.name} (${scheduleRoom.capacity}).`);
    }

    const conflictingRows = rows.filter(
      (row) =>
        row.id !== editingId &&
        row.semesterId === values.semesterId &&
        row.dayOfWeek === schedule.dayOfWeek &&
        row.status !== "CANCELLED" &&
        isPeriodOverlap(startPeriod, endPeriod, row.startPeriod, row.endPeriod),
    );

    const roomConflict = conflictingRows.find((row) => row.roomId === schedule.roomId);
    if (roomConflict) {
      errors.push(
        `Phòng ${roomConflict.roomName} đã có lớp ${roomConflict.classCode} cùng khung giờ trong học kỳ này.`,
      );
    }

    const teacherConflict = conflictingRows.find((row) => row.teacherId === values.teacherId);
    if (teacherConflict) {
      errors.push(
        `Giảng viên ${teacherConflict.teacherName} đã có lớp ${teacherConflict.classCode} cùng khung giờ trong học kỳ này.`,
      );
    }
  }

  for (let index = 0; index < schedules.length; index++) {
    const current = schedules[index];
    const currentStart = findPeriodNumber(periods, current.startPeriodId);
    const currentEnd = findPeriodNumber(periods, current.endPeriodId);
    for (let nextIndex = index + 1; nextIndex < schedules.length; nextIndex++) {
      const next = schedules[nextIndex];
      const nextStart = findPeriodNumber(periods, next.startPeriodId);
      const nextEnd = findPeriodNumber(periods, next.endPeriodId);
      if (
        current.dayOfWeek === next.dayOfWeek &&
        isPeriodOverlap(currentStart, currentEnd, nextStart, nextEnd)
      ) {
        errors.push("Các buổi học trong cùng lớp bị trùng khung giờ.");
      }
    }
  }

  return [...new Set(errors)];
}

function findPeriodNumber(periods: PeriodOption[], periodId: number) {
  return periods.find((period) => period.id === periodId)?.periodNumber ?? periodId;
}

function isPeriodOverlap(startA: number, endA: number, startB: number, endB: number) {
  return startA <= endB && startB <= endA;
}
