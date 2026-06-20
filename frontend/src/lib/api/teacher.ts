import { apiRequest, jsonBody } from "./client";
import type {
  AttendanceRecordRequest,
  AttendanceSessionResponse,
  ClassSectionResponse,
  GradeResponse,
  NotificationResponse,
  StudentSemesterResponse,
  TeacherDashboardResponse,
  TeacherGradeRequest,
  TeacherStudentGradeResponse,
  UserProfile,
} from "./types";

export const teacherApi = {
  getDashboard: () => apiRequest<TeacherDashboardResponse>("/api/teacher/dashboard"),

  getProfile: () => apiRequest<UserProfile>("/api/users/me"),

  listSemesters: () => apiRequest<StudentSemesterResponse[]>("/api/teacher/semesters"),

  listMyClasses: (semesterId: number | string) =>
    apiRequest<ClassSectionResponse[]>(`/api/teacher/my-classes/semester/${semesterId}`),

  listClassStudents: (classSectionId: number | string) =>
    apiRequest<TeacherStudentGradeResponse[]>(`/api/teacher/classes/${classSectionId}/students`),

  getClassGrades: (classSectionId: number | string) =>
    apiRequest<GradeResponse[]>(`/api/teacher/grades/class/${classSectionId}`),

  updateGrade: (enrollmentId: number | string, request: TeacherGradeRequest) =>
    apiRequest<GradeResponse>(`/api/teacher/grades/${enrollmentId}`, {
      method: "PUT",
      body: jsonBody(request),
    }),

  getAttendanceSessions: (classSectionId: number | string) =>
    apiRequest<AttendanceSessionResponse[]>(
      `/api/teacher/classes/${classSectionId}/attendance-sessions`,
    ),

  getAttendanceSession: (classSectionId: number | string, sessionNumber: number) =>
    apiRequest<AttendanceSessionResponse>(
      `/api/teacher/classes/${classSectionId}/attendance-sessions/${sessionNumber}`,
    ),

  saveAttendanceRecords: (
    classSectionId: number | string,
    sessionNumber: number,
    records: AttendanceRecordRequest[],
  ) =>
    apiRequest<AttendanceSessionResponse>(
      `/api/teacher/classes/${classSectionId}/attendance-sessions/${sessionNumber}/records`,
      { method: "PUT", body: jsonBody(records) },
    ),

  lockAttendanceSession: (classSectionId: number | string, sessionNumber: number) =>
    apiRequest<AttendanceSessionResponse>(
      `/api/teacher/classes/${classSectionId}/attendance-sessions/${sessionNumber}/lock`,
      { method: "POST" },
    ),

  lockClassGrades: (classSectionId: number | string) =>
    apiRequest<string>(`/api/teacher/grades/class/${classSectionId}/lock`, { method: "POST" }),

  listNotifications: () => apiRequest<NotificationResponse[]>("/api/teacher/notifications"),

  markNotificationAsRead: (notificationId: string) =>
    apiRequest<void>(`/api/teacher/notifications/${notificationId}/read`, {
      method: "POST",
    }),

  markAllNotificationsAsRead: () =>
    apiRequest<void>("/api/teacher/notifications/read-all", {
      method: "POST",
    }),
};
