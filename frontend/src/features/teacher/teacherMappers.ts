import type {
  ClassSectionResponse,
  GradeResponse,
  TeacherGradeRequest,
  TeacherStudentGradeResponse,
} from "@/lib/api/types";

export interface TeacherClassRow {
  id: string;
  numericId: number;
  classCode: string;
  courseName: string;
  courseCode: string;
  credits: number;
  semesterId: string;
  semesterName: string;
  scheduleText: string;
  roomText: string;
  scheduleRoomItems: string[];
  currentSlots: number | null;
  maxSlots: number | null;
  status: string;
  gradeStatus: string;
  virtualRetakeClass?: boolean;
}

export interface TeacherRosterRow {
  enrollmentId: string;
  numericEnrollmentId: number;
  studentCode: string;
  fullName: string;
  phone: string | null;
  email: string | null;
  cohort: string | null;
  className: string | null;
  advisorName: string | null;
  majorName: string | null;
  facultyName: string | null;
  midtermScore: number | null;
  finalScore: number | null;
  totalScore: number | null;
  status: string;
  courseStatus?: string | null;
  absenceCount?: number | null;
}

export interface TeacherGradeRow {
  enrollmentId: string;
  numericEnrollmentId: number;
  studentCode: string;
  studentName: string;
  classCode: string;
  courseName: string;
  participationScore: number | null;
  midtermScore: number | null;
  finalScore: number | null;
  retestScore: number | null;
  totalScore: number | null;
  letterGrade: string | null;
  gpa4: number | null;
  canEdit: boolean;
  gradeStatus: string;
  courseStatus: string | null;
  absenceCount: number | null;
  enrollmentType?: string | null;
  examAttemptNumber?: number | null;
}

const dayLabels: Record<number, string> = {
  2: "Thứ 2",
  3: "Thứ 3",
  4: "Thứ 4",
  5: "Thứ 5",
  6: "Thứ 6",
  7: "Thứ 7",
  8: "CN",
};

export function getTeacherClassRows(
  apiRows: ClassSectionResponse[] | undefined,
): TeacherClassRow[] {
  return apiRows?.map(mapApiClassSection) ?? [];
}

export function getTeacherRosterRows(
  apiRows: TeacherStudentGradeResponse[] | undefined,
): TeacherRosterRow[] {
  return apiRows?.map(mapApiRosterRow) ?? [];
}

export function getTeacherGradeRows(apiRows: GradeResponse[] | undefined): TeacherGradeRow[] {
  return apiRows?.map(mapApiGradeRow) ?? [];
}

export function isRetakeOrImproveEnrollment(row: TeacherGradeRow): boolean {
  return row.enrollmentType === "RETAKE" || row.enrollmentType === "IMPROVE";
}

export function buildTeacherGradeUpdateRequest(row: TeacherGradeRow): TeacherGradeRequest {
  const enrollmentId = Number(row.numericEnrollmentId ?? row.enrollmentId);
  if (isRetakeOrImproveEnrollment(row)) {
    return { enrollmentId, retestScore: row.retestScore };
  }
  return {
    enrollmentId,
    participationScore: row.participationScore,
    midTermScore: row.midtermScore,
    finalScore: row.finalScore,
    retestScore: row.retestScore,
  };
}

function formatScheduleItem(
  schedule: NonNullable<ClassSectionResponse["schedules"]>[number],
  sectionRoom: string | null | undefined,
) {
  const day = dayLabels[schedule.dayOfWeek] ?? `Thứ ${schedule.dayOfWeek}`;
  const roomName = schedule.roomName ?? sectionRoom;
  const period = `${schedule.startPeriod}-${schedule.endPeriod}`;
  return roomName ? `${day}, tiết ${period} - ${roomName}` : `${day}, tiết ${period}`;
}

function mapApiClassSection(section: ClassSectionResponse): TeacherClassRow {
  const schedules = section.schedules ?? [];
  const isClosed = section.closed ?? section.isClosed ?? false;
  const scheduleRoomItems = schedules.map((schedule) => formatScheduleItem(schedule, section.room));

  return {
    id: String(section.id),
    numericId: section.id,
    classCode: section.classCode,
    courseName: section.courseName,
    courseCode: section.courseCode,
    credits: section.credits,
    semesterId: String(section.semesterId),
    semesterName: section.semesterName,
    scheduleText: schedules
      .map((schedule) => {
        const day = dayLabels[schedule.dayOfWeek] ?? `Thứ ${schedule.dayOfWeek}`;
        return `${day}, tiết ${schedule.startPeriod}-${schedule.endPeriod}`;
      })
      .join("; "),
    roomText: schedules
      .map((schedule) => schedule.roomName)
      .filter(Boolean)
      .join(", "),
    scheduleRoomItems,
    currentSlots: section.currentSlots ?? null,
    maxSlots: section.maxSlots ?? null,
    status:
      section.status === "CANCELLED"
        ? "CANCELLED"
        : section.semesterEnded
          ? "ĐÃ KẾT THÚC"
          : "ĐANG DẠY",
    gradeStatus: section.gradeLocked ? "LOCKED" : "OPEN",
    virtualRetakeClass: section.virtualRetakeClass ?? false,
  };
}

const roundTo2Decimals = (val: number | null | undefined): number | null => {
  if (val == null) return null;
  return Math.round(val * 100) / 100;
};

function mapApiRosterRow(row: TeacherStudentGradeResponse): TeacherRosterRow {
  return {
    enrollmentId: String(row.enrollmentId),
    numericEnrollmentId: row.enrollmentId,
    studentCode: row.studentCode,
    fullName: row.fullName,
    phone: row.phone ?? null,
    email: row.email ?? null,
    cohort: row.facultyName ?? null,
    className: row.className ?? null,
    advisorName: row.advisorName ?? null,
    majorName: row.majorName ?? null,
    facultyName: row.facultyName ?? null,
    midtermScore: roundTo2Decimals(row.midTermScore),
    finalScore: roundTo2Decimals(row.finalScore),
    totalScore: roundTo2Decimals(row.totalScore),
    status: row.status,
    courseStatus: row.courseStatus ?? null,
    absenceCount: row.absenceCount ?? null,
  };
}

function mapApiGradeRow(row: GradeResponse): TeacherGradeRow {
  const courseStatus = row.courseStatus ?? null;
  const banned = courseStatus === "BANNED_FROM_EXAM" || courseStatus === "REPEAT_COURSE";
  return {
    enrollmentId: String(row.enrollmentId),
    numericEnrollmentId: row.enrollmentId,
    studentCode: row.studentCode,
    studentName: row.studentName,
    classCode: row.classCode,
    courseName: row.courseName,
    participationScore: roundTo2Decimals(row.participationScore),
    midtermScore: roundTo2Decimals(row.midtermScore),
    finalScore: roundTo2Decimals(row.finalScore),
    retestScore: roundTo2Decimals(row.retestScore),
    totalScore: roundTo2Decimals(row.totalScore),
    letterGrade: row.letterGrade ?? null,
    gpa4: row.gpa4 ?? row.gradePoint ?? null,
    canEdit: !banned,
    gradeStatus: "OPEN",
    courseStatus,
    absenceCount: row.absenceCount ?? null,
    enrollmentType: row.enrollmentType ?? null,
    examAttemptNumber: row.examAttemptNumber ?? null,
  };
}
