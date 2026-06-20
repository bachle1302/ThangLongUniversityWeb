import type {
  AdminClassSectionStatus,
  AdminClassSectionStudentResponse,
  ClassSectionResponse,
} from "@/lib/api/types";
import type {
  ClassSectionFormValues,
  ClassSectionOptionSets,
  ClassSectionRow,
  ClassSectionStudentRow,
  CourseOption,
  PeriodOption,
  ReferenceApiData,
  RoomOption,
  SemesterOption,
  TeacherOption,
} from "./types";

const dayLabels: Record<number, string> = {
  2: "Thứ 2",
  3: "Thứ 3",
  4: "Thứ 4",
  5: "Thứ 5",
  6: "Thứ 6",
  7: "Thứ 7",
  8: "Chủ nhật",
};

export function formatClassDay(dayOfWeek: number) {
  return dayLabels[dayOfWeek] ?? `Thứ ${dayOfWeek}`;
}

export function mapApiClassSection(section: ClassSectionResponse): ClassSectionRow {
  const schedules = section.schedules ?? [];
  const firstSchedule = schedules[0];

  return {
    id: String(section.id),
    numericId: section.id,
    classCode: section.classCode,
    courseId: section.courseId,
    courseName: `${section.courseCode} - ${section.courseName}`,
    majorName: section.majorName ?? "-",
    semesterId: section.semesterId,
    semesterName: section.semesterName,
    registrationRoundId: section.registrationRoundId,
    registrationRoundName: section.registrationRoundName,
    registrationRoundNumber: section.registrationRoundNumber,
    teacherId: section.teacherId ?? 0,
    teacherName: section.teacherName ?? "Chưa phân công",
    roomId: section.roomId ?? firstSchedule?.roomId ?? 0,
    roomName: section.room ?? firstSchedule?.roomName ?? "-",
    dayOfWeek: firstSchedule?.dayOfWeek ?? 2,
    startPeriodId: firstSchedule?.startPeriodId ?? 0,
    startPeriod: firstSchedule?.startPeriod ?? 0,
    endPeriodId: firstSchedule?.endPeriodId ?? 0,
    endPeriod: firstSchedule?.endPeriod ?? 0,
    schedules: schedules.map((schedule) => ({
      key: String(schedule.id),
      roomId: schedule.roomId ?? 0,
      dayOfWeek: schedule.dayOfWeek,
      startPeriodId: schedule.startPeriodId,
      endPeriodId: schedule.endPeriodId,
    })),
    currentSlots: section.currentSlots ?? 0,
    maxSlots: section.maxSlots ?? 0,
    status: normalizeClassSectionStatus(section.status, section.closed ?? section.isClosed),
    source: "API",
  };
}

function normalizeClassSectionStatus(
  status: ClassSectionResponse["status"],
  closed?: boolean,
): AdminClassSectionStatus {
  if (status === "DRAFT" || status === "OPEN" || status === "CLOSED" || status === "CANCELLED") {
    return status;
  }
  return closed ? "CLOSED" : "OPEN";
}

export function buildOptionSets(
  data: ReferenceApiData,
  rows: ClassSectionRow[],
): ClassSectionOptionSets {
  return {
    courses: mergeCourseOptions(mapApiCourses(data.courses), rows),
    semesters: mergeSemesterOptions(mapApiSemesters(data.semesters), rows),
    teachers: mergeTeacherOptions(mapApiTeachers(data.teachers), rows),
    rooms: mergeRoomOptions(mapApiRooms(data.rooms), rows),
    periods: mapApiPeriods(data.periods),
  };
}

export function toClassSectionRequest(values: ClassSectionFormValues) {
  const schedules = values.schedules?.length
    ? values.schedules
    : [
        {
          dayOfWeek: values.dayOfWeek,
          startPeriodId: values.startPeriodId,
          endPeriodId: values.endPeriodId,
          roomId: values.roomId,
        },
      ];
  return {
    classCode: values.classCode.trim(),
    courseId: values.courseId,
    semesterId: values.semesterId,
    teacherId: values.teacherId,
    registrationRoundId: values.registrationRoundId ?? null,
    maxSlots: values.maxSlots,
    schedules: schedules.map((schedule) => ({
      dayOfWeek: schedule.dayOfWeek,
      startPeriodId: schedule.startPeriodId,
      endPeriodId: schedule.endPeriodId,
      roomId: schedule.roomId,
    })),
  };
}

export function mapApiClassSectionStudent(
  student: AdminClassSectionStudentResponse,
): ClassSectionStudentRow {
  return {
    enrollmentId: String(student.enrollmentId),
    studentId: String(student.studentId),
    studentCode: student.studentCode,
    fullName: student.fullName,
    email: student.email ?? "-",
    majorName: student.majorName ?? "-",
    cohort: student.cohort ?? (student.academicYear ? `K${student.academicYear}` : "-"),
    enrolledAt: student.enrolledAt ?? null,
    status: student.status ?? "-",
    source: "API",
  };
}

function mapApiCourses(items?: ReferenceApiData["courses"]): CourseOption[] {
  return (items ?? []).map((course) => ({
    id: course.id,
    code: course.code,
    name: course.name,
    departmentId: course.departmentId,
    departmentName: course.departmentName,
  }));
}

function mapApiSemesters(items?: ReferenceApiData["semesters"]): SemesterOption[] {
  return (items ?? []).map((semester) => ({ id: semester.id, name: semester.name }));
}

function mapApiTeachers(items?: ReferenceApiData["teachers"]): TeacherOption[] {
  return (items ?? []).map((teacher) => ({
    id: teacher.id,
    name: teacher.fullName,
    departmentId: teacher.departmentId,
    departmentName: teacher.departmentName,
  }));
}

function mapApiRooms(items?: ReferenceApiData["rooms"]): RoomOption[] {
  return (items ?? []).map((room) => ({ id: room.id, name: room.name, capacity: room.capacity }));
}

function mapApiPeriods(items?: ReferenceApiData["periods"]): PeriodOption[] {
  return (items ?? []).map((period) => ({
    id: period.id,
    periodNumber: period.periodNumber,
    label: `Tiết ${period.periodNumber} (${period.startTime}-${period.endTime})`,
  }));
}

function mergeCourseOptions(options: CourseOption[], rows: ClassSectionRow[]) {
  const fromRows = rows.map((row) => ({
    id: row.courseId,
    code: row.courseName.split(" - ")[0],
    name: row.courseName,
  }));
  return uniqueById([...options, ...fromRows]).filter((course) => course.id > 0);
}

function mergeSemesterOptions(options: SemesterOption[], rows: ClassSectionRow[]) {
  return uniqueById([
    ...options,
    ...rows.map((row) => ({ id: row.semesterId, name: row.semesterName })),
  ]).filter((semester) => semester.id > 0);
}

function mergeTeacherOptions(options: TeacherOption[], rows: ClassSectionRow[]) {
  return uniqueById([
    ...options,
    ...rows.map((row) => ({ id: row.teacherId, name: row.teacherName })),
  ]).filter((teacher) => teacher.id > 0);
}

function mergeRoomOptions(options: RoomOption[], rows: ClassSectionRow[]) {
  return uniqueById([
    ...options,
    ...rows.map((row) => ({ id: row.roomId, name: row.roomName, capacity: row.maxSlots })),
  ]).filter((room) => room.id > 0);
}

function uniqueById<T extends { id: number }>(items: T[]) {
  const map = new Map<number, T>();
  for (const item of items) {
    if (!map.has(item.id)) {
      map.set(item.id, item);
    }
  }
  return Array.from(map.values());
}
