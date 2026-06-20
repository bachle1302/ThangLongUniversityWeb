import { apiRequest, downloadApiFile, jsonBody } from "./client";
import type {
  AdminUserUpdateRequest,
  AdminClassSectionOptionsResponse,
  AdminClassSectionRequest,
  AdminClassSectionStudentResponse,
  BulkClassSectionProposalRequest,
  BulkClassSectionProposalResponse,
  BulkClassSectionValidationResponse,
  ClassSectionValidationResponse,
  AdminCourseRequest,
  AdminRoomRequest,
  AdminStudentRequest,
  AdminStudentResponse,
  AdminTeacherRequest,
  AdminTeacherResponse,
  AdminUserResponse,
  AdminEnrollmentResponse,
  AdminDashboardResponse,
  AdminOverrideEnrollmentRequest,
  AdminExamRegistrationResponse,
  AdminExamRegistrationSummary,
  ClassSectionResponse,
  CourseResponse,
  DepartmentRequest,
  DepartmentResponse,
  ExamScheduleRequest,
  ExamScheduleResponse,
  ExamSeatAssignmentResponse,
  ExamSessionRequest,
  ExamSessionResponse,
  ExamConflictResponse,
  ExamCandidateResponse,
  HomeroomRequest,
  HomeroomResponse,
  HomeroomStudentsRequest,
  AdminMajorRequest,
  MajorResponse,
  PageResponse,
  PeriodRequest,
  PeriodResponse,
  RoomResponse,
  SemesterRequest,
  SemesterSummaryResponse,
  RegistrationRoundRequest,
  RegistrationRoundResponse,
  StudentSemesterResponse,
} from "./types";

export interface CreateAdminRequest {
  username: string;
  password: string;
  email: string;
}

export const adminApi = {
  getDashboard: (semesterId?: number | null) =>
    apiRequest<AdminDashboardResponse>(
      `/api/admin/dashboard${semesterId == null ? "" : `?semesterId=${encodeURIComponent(String(semesterId))}`}`,
    ),

  listUsers: () => apiRequest<AdminUserResponse[]>("/api/admin/users"),
  createAdmin: (request: CreateAdminRequest) => {
    const params = new URLSearchParams({
      username: request.username,
      password: request.password,
      email: request.email,
    });
    return apiRequest<string>(`/api/admin/users/admin?${params.toString()}`, { method: "POST" });
  },
  toggleUserStatus: (id: number | string) =>
    apiRequest<string>(`/api/admin/users/${id}/toggle-status`, { method: "PUT" }),
  updateUser: (id: number | string, request: AdminUserUpdateRequest) =>
    apiRequest<AdminUserResponse>(`/api/admin/users/${id}`, {
      method: "PUT",
      body: jsonBody(request),
    }),
  deleteAdminUser: (id: number | string) =>
    apiRequest<string>(`/api/admin/users/admin/${id}`, { method: "DELETE" }),
  resetUserPassword: (id: number | string, newPassword: string) =>
    apiRequest<{ message: string }>(`/api/admin/users/${id}/reset-password`, {
      method: "PUT",
      body: jsonBody({ newPassword }),
    }),

  listStudents: () => apiRequest<AdminStudentResponse[]>("/api/admin/students"),
  searchStudents: (params?: {
    page?: number;
    size?: number;
    keyword?: string;
    majorId?: number | string | null;
    status?: string | null;
  }) => {
    const search = new URLSearchParams();
    if (params?.page != null) search.set("page", String(params.page));
    if (params?.size != null) search.set("size", String(params.size));
    if (params?.keyword) search.set("keyword", params.keyword);
    if (params?.majorId != null && params.majorId !== "") search.set("majorId", String(params.majorId));
    if (params?.status) search.set("status", params.status);
    const qs = search.toString();
    return apiRequest<PageResponse<AdminStudentResponse>>(
      `/api/admin/students/search${qs ? `?${qs}` : ""}`,
    );
  },
  createStudent: (request: AdminStudentRequest) =>
    apiRequest<AdminStudentResponse>("/api/admin/students", {
      method: "POST",
      body: jsonBody(request),
    }),
  updateStudent: (
    id: number | string,
    request: Partial<AdminStudentRequest> & {
      majorId?: number;
      academicYear?: number;
      cohort?: string;
      homeroomId?: number | null;
      status?: string;
      trainingType?: string;
      gender?: string;
      phone?: string;
      nationalId?: string;
      placeOfBirth?: string;
      hometown?: string;
      permanentAddress?: string;
      currentAddress?: string;
      emergencyContact?: string;
      address?: string;
    },
  ) =>
    apiRequest<AdminStudentResponse>(`/api/admin/students/${id}`, {
      method: "PUT",
      body: jsonBody(request),
    }),
  deleteStudent: (id: number | string) =>
    apiRequest<string>(`/api/admin/students/${id}`, { method: "DELETE" }),

  listTeachers: () => apiRequest<AdminTeacherResponse[]>("/api/admin/teachers"),
  searchTeachers: (params?: {
    page?: number;
    size?: number;
    keyword?: string;
    departmentId?: number | string | null;
    status?: string | null;
  }) => {
    const search = new URLSearchParams();
    if (params?.page != null) search.set("page", String(params.page));
    if (params?.size != null) search.set("size", String(params.size));
    if (params?.keyword) search.set("keyword", params.keyword);
    if (params?.departmentId != null && params.departmentId !== "") {
      search.set("departmentId", String(params.departmentId));
    }
    if (params?.status) search.set("status", params.status);
    const qs = search.toString();
    return apiRequest<PageResponse<AdminTeacherResponse>>(
      `/api/admin/teachers/search${qs ? `?${qs}` : ""}`,
    );
  },
  createTeacher: (request: AdminTeacherRequest) =>
    apiRequest<AdminTeacherResponse>("/api/admin/teachers", {
      method: "POST",
      body: jsonBody(request),
    }),
  updateTeacher: (
    id: number | string,
    request: Partial<
      Pick<
        AdminTeacherResponse,
        | "email"
        | "fullName"
        | "dob"
        | "gender"
        | "phone"
        | "nationalId"
        | "placeOfBirth"
        | "hometown"
        | "permanentAddress"
        | "currentAddress"
        | "emergencyContact"
        | "departmentId"
        | "degree"
        | "address"
        | "status"
      >
    >,
  ) =>
    apiRequest<AdminTeacherResponse>(`/api/admin/teachers/${id}`, {
      method: "PUT",
      body: jsonBody(request),
    }),
  deleteTeacher: (id: number | string) =>
    apiRequest<string>(`/api/admin/teachers/${id}`, { method: "DELETE" }),

  listCourses: () => apiRequest<CourseResponse[]>("/api/admin/courses"),
  createCourse: (request: AdminCourseRequest) =>
    apiRequest<CourseResponse>("/api/admin/courses", { method: "POST", body: jsonBody(request) }),
  updateCourse: (id: number | string, request: AdminCourseRequest) =>
    apiRequest<CourseResponse>(`/api/admin/courses/${id}`, {
      method: "PUT",
      body: jsonBody(request),
    }),
  deleteCourse: (id: number | string) =>
    apiRequest<string>(`/api/admin/courses/${id}`, { method: "DELETE" }),

  listSemesters: () => apiRequest<StudentSemesterResponse[]>("/api/admin/semesters"),
  createSemester: (request: SemesterRequest) =>
    apiRequest<StudentSemesterResponse>("/api/admin/semesters", {
      method: "POST",
      body: jsonBody(request),
    }),
  updateSemester: (id: number | string, request: SemesterRequest) =>
    apiRequest<StudentSemesterResponse>(`/api/admin/semesters/${id}`, {
      method: "PUT",
      body: jsonBody(request),
    }),
  deleteSemester: (id: number | string) =>
    apiRequest<string>(`/api/admin/semesters/${id}`, { method: "DELETE" }),
  getSemesterSummary: (id: number | string) =>
    apiRequest<SemesterSummaryResponse>(`/api/admin/semesters/${id}/summary`),
  toggleRegistration: (semesterId: number | string, open: boolean) =>
    apiRequest<StudentSemesterResponse>(`/api/admin/semesters/${semesterId}/toggle-registration`, {
      method: "POST",
      body: jsonBody({ open }),
    }),
  lockEnrollmentSemester: (semesterId: number | string) =>
    apiRequest<string>(`/api/admin/enrollments/lock-semester/${semesterId}`, { method: "POST" }),
  lockRetakeSemester: (semesterId: number | string) =>
    apiRequest<string>(`/api/admin/enrollments/lock-retakes/${semesterId}`, { method: "POST" }),
  lockEnrollments: (semesterId: number | string) =>
    apiRequest<{ message: string }>(`/api/admin/semesters/${semesterId}/lock-enrollments`, {
      method: "POST",
    }),
  publishExamSchedules: (semesterId: number | string) =>
    apiRequest<StudentSemesterResponse>(`/api/admin/semesters/${semesterId}/publish-exams`, {
      method: "POST",
    }),
  unpublishExamSchedules: (semesterId: number | string) =>
    apiRequest<StudentSemesterResponse>(`/api/admin/semesters/${semesterId}/unpublish-exams`, {
      method: "POST",
    }),
  toggleRetakeRegistration: (semesterId: number | string, open: boolean) =>
    apiRequest<StudentSemesterResponse>(`/api/admin/semesters/${semesterId}/toggle-retake`, {
      method: "POST",
      body: jsonBody({ open }),
    }),
  lockRetakes: (semesterId: number | string) =>
    apiRequest<{ message: string }>(`/api/admin/semesters/${semesterId}/lock-retakes`, {
      method: "POST",
    }),
  endSemester: (semesterId: number | string) =>
    apiRequest<StudentSemesterResponse>(`/api/admin/semesters/${semesterId}/end`, {
      method: "POST",
    }),
  listRegistrationRounds: (semesterId: number | string, roundType?: string) =>
    apiRequest<RegistrationRoundResponse[]>(
      `/api/admin/semesters/${semesterId}/registration-rounds${roundType ? `?roundType=${roundType}` : ""}`,
    ),
  createRegistrationRound: (semesterId: number | string, request?: RegistrationRoundRequest) =>
    apiRequest<RegistrationRoundResponse>(`/api/admin/semesters/${semesterId}/registration-rounds`, {
      method: "POST",
      body: jsonBody(request ?? {}),
    }),
  openRegistrationRound: (
    semesterId: number | string,
    roundId: number | string,
    request?: RegistrationRoundRequest,
  ) =>
    apiRequest<RegistrationRoundResponse>(
      `/api/admin/semesters/${semesterId}/registration-rounds/${roundId}/open`,
      {
        method: "POST",
        body: request ? jsonBody(request) : undefined,
      },
    ),
  closeRegistrationRound: (semesterId: number | string, roundId: number | string) =>
    apiRequest<RegistrationRoundResponse>(
      `/api/admin/semesters/${semesterId}/registration-rounds/${roundId}/close`,
      { method: "POST" },
    ),
  lockRegistrationRound: (semesterId: number | string, roundId: number | string) =>
    apiRequest<{ message: string }>(
      `/api/admin/semesters/${semesterId}/registration-rounds/${roundId}/lock`,
      { method: "POST" },
    ),

  listEnrollments: (params?: {
    semesterId?: number;
    classSectionId?: number;
    status?: string;
    page?: number;
    size?: number;
  }) => {
    const qs = new URLSearchParams();
    if (params?.semesterId != null) qs.set("semesterId", String(params.semesterId));
    if (params?.classSectionId != null) qs.set("classSectionId", String(params.classSectionId));
    if (params?.status) qs.set("status", params.status);
    if (params?.page != null) qs.set("page", String(params.page));
    if (params?.size != null) qs.set("size", String(params.size));
    const query = qs.toString();
    return apiRequest<PageResponse<AdminEnrollmentResponse>>(
      `/api/admin/enrollments${query ? `?${query}` : ""}`,
    );
  },
  overrideEnrollment: (request: AdminOverrideEnrollmentRequest) =>
    apiRequest<AdminEnrollmentResponse>("/api/admin/enrollments/override", {
      method: "POST",
      body: jsonBody(request),
    }),

  getExamSchedules: (semesterId: number | string) =>
    apiRequest<ExamScheduleResponse[]>(
      `/api/admin/class-sections/semester/${semesterId}/exam-schedules`,
    ),
  updateExamSchedule: (classSectionId: number | string, request: ExamScheduleRequest) =>
    apiRequest<ExamScheduleResponse>(`/api/admin/class-sections/${classSectionId}/exam-schedule`, {
      method: "PUT",
      body: jsonBody(request),
    }),
  batchUpdateExamSchedules: (semesterId: number | string, requests: ExamScheduleRequest[]) =>
    apiRequest<ExamScheduleResponse[]>(
      `/api/admin/class-sections/semester/${semesterId}/exam-schedules`,
      {
        method: "PUT",
        body: jsonBody(requests),
      },
    ),
  listExamSessions: (semesterId: number | string) =>
    apiRequest<ExamSessionResponse[]>(
      `/api/admin/class-sections/semester/${semesterId}/exam-sessions`,
    ),
  saveExamSession: (semesterId: number | string, request: ExamSessionRequest) =>
    apiRequest<ExamSessionResponse>(
      `/api/admin/class-sections/semester/${semesterId}/exam-sessions`,
      { method: "POST", body: jsonBody(request) },
    ),
  validateExamConflicts: (semesterId: number | string, request: ExamSessionRequest) =>
    apiRequest<ExamConflictResponse[]>(
      `/api/admin/class-sections/semester/${semesterId}/exam-sessions/validate-conflicts`,
      { method: "POST", body: jsonBody(request) },
    ),
  listExamCandidates: (semesterId: number | string, courseId: number, candidateSelection?: string) => {
    const qs = new URLSearchParams({ courseId: String(courseId) });
    if (candidateSelection) qs.set("candidateSelection", candidateSelection);
    return apiRequest<ExamCandidateResponse[]>(
      `/api/admin/class-sections/semester/${semesterId}/exam-sessions/candidates?${qs.toString()}`,
    );
  },
  updateExamRegistrationClassSection: (registrationId: number, classSectionId: number) =>
    apiRequest<{ success: boolean; classCode: string; classSectionId: number }>(
      `/api/admin/exam-registrations/${registrationId}/class-section?classSectionId=${classSectionId}`,
      { method: "PUT" },
    ),
  moveExamSeatAssignment: (seatId: number, targetRoomAssignmentId: number) =>
    apiRequest<{ success: boolean }>(
      `/api/admin/class-sections/exam-sessions/seats/${seatId}/move?targetRoomAssignmentId=${targetRoomAssignmentId}`,
      { method: "PUT" },
    ),
  listExamSessionSeats: (examSessionId: number | string) =>
    apiRequest<ExamSeatAssignmentResponse[]>(
      `/api/admin/class-sections/exam-sessions/${examSessionId}/seats`,
    ),

  listExamRegistrations: (semesterId: number, status?: string) => {
    const qs = new URLSearchParams({ semesterId: String(semesterId) });
    if (status) qs.set("status", status);
    return apiRequest<AdminExamRegistrationResponse[]>(
      `/api/admin/exam-registrations?${qs.toString()}`,
    );
  },
  getExamRegistrationSummary: (semesterId: number) =>
    apiRequest<AdminExamRegistrationSummary>(
      `/api/admin/exam-registrations/semester/${semesterId}/summary`,
    ),

  listClassSectionsBySemester: (semesterId: number | string) =>
    apiRequest<ClassSectionResponse[]>(`/api/admin/class-sections/semester/${semesterId}`),

  // Export Excel
  exportEnrollmentsUrl: (semesterId: number | string) =>
    `/api/admin/export/enrollments/semester/${semesterId}`,
  exportExamSchedulesUrl: (semesterId: number | string) =>
    `/api/admin/export/exam-schedules/semester/${semesterId}`,
  exportRetakesUrl: (semesterId: number | string) =>
    `/api/admin/export/retakes/semester/${semesterId}`,
  exportEnrollments: (semesterId: number | string) =>
    downloadApiFile(
      `/api/admin/export/enrollments/semester/${semesterId}`,
      `enrollments-semester-${semesterId}.xlsx`,
    ),
  exportExamSchedules: (semesterId: number | string) =>
    downloadApiFile(
      `/api/admin/export/exam-schedules/semester/${semesterId}`,
      `exam-schedules-semester-${semesterId}.xlsx`,
    ),
  exportRetakes: (semesterId: number | string) =>
    downloadApiFile(
      `/api/admin/export/retakes/semester/${semesterId}`,
      `retakes-semester-${semesterId}.xlsx`,
    ),

  listClassSections: () => apiRequest<ClassSectionResponse[]>("/api/admin/class-sections"),
  searchClassSections: (params?: {
    page?: number;
    size?: number;
    semesterId?: number | string | null;
    keyword?: string;
    status?: string | null;
  }) => {
    const search = new URLSearchParams();
    if (params?.page != null) search.set("page", String(params.page));
    if (params?.size != null) search.set("size", String(params.size));
    if (params?.semesterId != null && params.semesterId !== "") {
      search.set("semesterId", String(params.semesterId));
    }
    if (params?.keyword) search.set("keyword", params.keyword);
    if (params?.status) search.set("status", params.status);
    const qs = search.toString();
    return apiRequest<PageResponse<ClassSectionResponse>>(
      `/api/admin/class-sections/search${qs ? `?${qs}` : ""}`,
    );
  },
  getClassSectionOptions: () =>
    apiRequest<AdminClassSectionOptionsResponse>("/api/admin/class-sections/options"),
  createClassSection: (request: AdminClassSectionRequest) =>
    apiRequest<ClassSectionResponse>("/api/admin/class-sections", {
      method: "POST",
      body: JSON.stringify(request),
    }),
  validateClassSection: (request: AdminClassSectionRequest, excludeId?: number | string | null) =>
    apiRequest<ClassSectionValidationResponse>(
      `/api/admin/class-sections/validate${excludeId ? `?excludeId=${excludeId}` : ""}`,
      {
        method: "POST",
        body: JSON.stringify(request),
      },
    ),
  proposeBulkClassSections: (request: BulkClassSectionProposalRequest) =>
    apiRequest<BulkClassSectionProposalResponse>("/api/admin/class-sections/bulk/proposals", {
      method: "POST",
      body: JSON.stringify(request),
    }),
  validateBulkClassSections: (items: AdminClassSectionRequest[]) =>
    apiRequest<BulkClassSectionValidationResponse>("/api/admin/class-sections/bulk/validate", {
      method: "POST",
      body: JSON.stringify({ items }),
    }),
  createBulkClassSections: (items: AdminClassSectionRequest[]) =>
    apiRequest<ClassSectionResponse[]>("/api/admin/class-sections/bulk", {
      method: "POST",
      body: JSON.stringify({ items }),
    }),
  updateClassSection: (id: number | string, request: AdminClassSectionRequest) =>
    apiRequest<ClassSectionResponse>(`/api/admin/class-sections/${id}`, {
      method: "PUT",
      body: JSON.stringify(request),
    }),
  deleteClassSection: (id: number | string) =>
    apiRequest<string>(`/api/admin/class-sections/${id}`, { method: "DELETE" }),
  cancelClassSection: (id: number | string) =>
    apiRequest<{ message: string; classSection: ClassSectionResponse }>(
      `/api/admin/class-sections/${id}/cancel`,
      { method: "POST" },
    ),
  listClassSectionStudents: (id: number | string) =>
    apiRequest<AdminClassSectionStudentResponse[]>(`/api/admin/class-sections/${id}/students`),

  listMajors: () => apiRequest<MajorResponse[]>("/api/admin/majors"),
  createMajor: (request: AdminMajorRequest) =>
    apiRequest<MajorResponse>("/api/admin/majors", { method: "POST", body: jsonBody(request) }),
  updateMajor: (id: number | string, request: AdminMajorRequest) =>
    apiRequest<MajorResponse>(`/api/admin/majors/${id}`, {
      method: "PUT",
      body: jsonBody(request),
    }),
  deleteMajor: (id: number | string) =>
    apiRequest<string>(`/api/admin/majors/${id}`, { method: "DELETE" }),

  listRooms: () => apiRequest<RoomResponse[]>("/api/admin/rooms"),
  createRoom: (request: AdminRoomRequest) =>
    apiRequest<RoomResponse>("/api/admin/rooms", { method: "POST", body: jsonBody(request) }),
  updateRoom: (id: number | string, request: AdminRoomRequest) =>
    apiRequest<RoomResponse>(`/api/admin/rooms/${id}`, { method: "PUT", body: jsonBody(request) }),
  deleteRoom: (id: number | string) =>
    apiRequest<string>(`/api/admin/rooms/${id}`, { method: "DELETE" }),

  listPeriods: () => apiRequest<PeriodResponse[]>("/api/admin/periods"),
  createPeriod: (request: PeriodRequest) =>
    apiRequest<PeriodResponse>("/api/admin/periods", { method: "POST", body: jsonBody(request) }),
  updatePeriod: (id: number | string, request: PeriodRequest) =>
    apiRequest<PeriodResponse>(`/api/admin/periods/${id}`, {
      method: "PUT",
      body: jsonBody(request),
    }),
  deletePeriod: (id: number | string) =>
    apiRequest<string>(`/api/admin/periods/${id}`, { method: "DELETE" }),

  listDepartments: () => apiRequest<DepartmentResponse[]>("/api/admin/departments"),
  createDepartment: (req: DepartmentRequest) =>
    apiRequest<DepartmentResponse>("/api/admin/departments", {
      method: "POST",
      body: jsonBody(req),
    }),
  updateDepartment: (id: number | string, req: DepartmentRequest) =>
    apiRequest<DepartmentResponse>(`/api/admin/departments/${id}`, {
      method: "PUT",
      body: jsonBody(req),
    }),
  deleteDepartment: (id: number | string) =>
    apiRequest<string>(`/api/admin/departments/${id}`, { method: "DELETE" }),

  listHomerooms: () => apiRequest<HomeroomResponse[]>("/api/admin/homerooms"),
  createHomeroom: (req: HomeroomRequest) =>
    apiRequest<HomeroomResponse>("/api/admin/homerooms", { method: "POST", body: jsonBody(req) }),
  updateHomeroom: (id: number | string, req: HomeroomRequest) =>
    apiRequest<HomeroomResponse>(`/api/admin/homerooms/${id}`, {
      method: "PUT",
      body: jsonBody(req),
    }),
  deleteHomeroom: (id: number | string) =>
    apiRequest<string>(`/api/admin/homerooms/${id}`, { method: "DELETE" }),
  listHomeroomStudents: (id: number | string) =>
    apiRequest<AdminStudentResponse[]>(`/api/admin/homerooms/${id}/students`),
  addStudentsToHomeroom: (id: number | string, req: HomeroomStudentsRequest) =>
    apiRequest<string>(`/api/admin/homerooms/${id}/students`, {
      method: "POST",
      body: jsonBody(req),
    }),
  removeStudentFromHomeroom: (homeroomId: number | string, studentId: number | string) =>
    apiRequest<string>(`/api/admin/homerooms/${homeroomId}/students/${studentId}`, {
      method: "DELETE",
    }),
};
