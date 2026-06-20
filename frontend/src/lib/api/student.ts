import { apiRequest, jsonBody } from "./client";
import type {
  AcademicResultResponse,
  ClassSectionResponse,
  CourseResponse,
  EnrollmentRequestResponse,
  EnrollmentRequestStatusResponse,
  EnrollmentResponse,
  GradeResponse,
  LearningResultsResponse,
  NotificationResponse,
  RetakeEligibleCourseResponse,
  RetakeRegistrationRequest,
  RetakeRegistrationResponse,
  RetakeRequestResponse,
  StudentExamResponse,
  StudentCourseRegistrationOverviewResponse,
  StudentDashboardResponse,
  StudentGradesSummaryResponse,
  StudentRetakeOverviewResponse,
  StudentSemesterResponse,
  TuitionResponse,
  UserProfile,
} from "./types";

const optionalSemesterQuery = (semesterId?: number | string | null) =>
  semesterId == null || semesterId === ""
    ? ""
    : `?semesterId=${encodeURIComponent(String(semesterId))}`;

export const studentApi = {
  getProfile: () => apiRequest<UserProfile>("/api/student/profile"),

  listSemesters: () => apiRequest<StudentSemesterResponse[]>("/api/student/semesters"),

  getDashboard: (semesterId?: number | string | null) => {
    return apiRequest<StudentDashboardResponse>(
      `/api/student/dashboard${optionalSemesterQuery(semesterId)}`,
    );
  },

  listAvailableClasses: (semesterId: number | string) =>
    apiRequest<ClassSectionResponse[]>(`/api/student/classes/semester/${semesterId}`),

  getCourseRegistrationOverview: (semesterId?: number | string | null) =>
    apiRequest<StudentCourseRegistrationOverviewResponse>(
      `/api/student/course-registration/overview${optionalSemesterQuery(semesterId)}`,
    ),

  enrollClass: (classSectionId: number | string) =>
    apiRequest<EnrollmentRequestResponse>(`/api/student/enroll/${classSectionId}`, {
      method: "POST",
    }),

  cancelClass: (classSectionId: number | string) =>
    apiRequest<string>(`/api/student/enroll/${classSectionId}`, { method: "DELETE" }),

  listSelectedEnrollments: (semesterId: number | string) =>
    apiRequest<EnrollmentResponse[]>(
      `/api/student/enrollments/selected?semesterId=${encodeURIComponent(String(semesterId))}`,
    ),

  getEnrollmentStatus: (requestId: string) =>
    apiRequest<EnrollmentRequestStatusResponse>(
      `/api/student/enrollments/status/${encodeURIComponent(requestId)}`,
    ),

  getSchedule: (semesterId: number | string) =>
    apiRequest<EnrollmentResponse[]>(`/api/student/my-schedule/${semesterId}`),

  getGrades: (semesterId?: number | string | null) => {
    return apiRequest<StudentGradesSummaryResponse>(
      `/api/student/grades${optionalSemesterQuery(semesterId)}`,
    );
  },

  listGradesBySemester: (semesterId: number | string) =>
    apiRequest<GradeResponse[]>(`/api/student/grades/semester/${semesterId}`),

  listAllGrades: () => apiRequest<GradeResponse[]>("/api/student/grades/my-grades"),

  getLearningResults: (semesterId?: number | string | null) => {
    return apiRequest<LearningResultsResponse>(
      `/api/student/learning-results${optionalSemesterQuery(semesterId)}`,
    );
  },

  listAcademicResults: () =>
    apiRequest<AcademicResultResponse[]>("/api/student/academic-results/my-results"),

  getCurriculum: () => apiRequest<CourseResponse[]>("/api/student/curriculum"),

  getMyMajorCurriculum: () => apiRequest<CourseResponse[]>("/api/student/curriculum/my-major"),

  getExams: (semesterId: number | string) =>
    apiRequest<StudentExamResponse[]>(
      `/api/student/exams?semesterId=${encodeURIComponent(String(semesterId))}`,
    ),

  getTuition: (semesterId: number | string) =>
    apiRequest<TuitionResponse>(`/api/student/tuition/${semesterId}`),

  createVNPayUrl: (semesterId: number | string) =>
    apiRequest<string>(`/api/student/tuition/${semesterId}/vnpay-url`, { method: "POST" }),

  listRetakeEligibleCourses: (semesterId?: number | string | null) => {
    return apiRequest<RetakeEligibleCourseResponse[]>(
      `/api/student/retakes/eligible-courses${optionalSemesterQuery(semesterId)}`,
    );
  },

  getRetakeOverview: (semesterId?: number | string | null) =>
    apiRequest<StudentRetakeOverviewResponse>(
      `/api/student/retakes/overview${optionalSemesterQuery(semesterId)}`,
    ),

  registerRetakes: (request: RetakeRegistrationRequest) =>
    apiRequest<RetakeRegistrationResponse>("/api/student/retakes/register", {
      method: "POST",
      body: jsonBody(request),
    }),

  cancelRetake: (examRegistrationId: number | string) =>
    apiRequest<string>(`/api/student/retakes/${examRegistrationId}`, { method: "DELETE" }),

  listRetakeRequests: (semesterId?: number | string | null) => {
    return apiRequest<RetakeRequestResponse[]>(
      `/api/student/retakes/my-requests${optionalSemesterQuery(semesterId)}`,
    );
  },

  listNotifications: () => apiRequest<NotificationResponse[]>("/api/student/notifications"),

  markNotificationAsRead: (notificationId: string) =>
    apiRequest<void>(`/api/student/notifications/${encodeURIComponent(notificationId)}/read`, {
      method: "POST",
    }),

  markAllNotificationsAsRead: () =>
    apiRequest<void>("/api/student/notifications/read-all", {
      method: "POST",
    }),
};
