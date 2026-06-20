export type Role = "ADMIN" | "TEACHER" | "STUDENT";
export type CourseType = "REQUIRED" | "ELECTIVE";
export type EnrollmentStatus = "PENDING" | "REGISTERED" | "CANCELED" | "PASSED" | "FAILED";
export type EnrollmentType = "ORDINARY" | "RETAKE" | "IMPROVE";
export type RetakeRegistrationType = "RETAKE" | "IMPROVE";
export type EnrollmentRequestStatus = "PENDING" | "PROCESSING" | "SUCCESS" | "FAILED";
export type NotificationType = "SCHOOL" | "CHAT";

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  role: Role;
}

export interface UserProfile {
  username: string;
  email: string;
  role: Role;
  fullName: string;
  code?: string;
  majorOrDegree?: string | null;
  avatarUrl?: string | null;
  // Personal info
  gender?: string | null;
  dateOfBirth?: string | null;
  age?: number | null;
  nationalId?: string | null;
  placeOfBirth?: string | null;
  hometown?: string | null;
  permanentAddress?: string | null;
  currentAddress?: string | null;
  phone?: string | null;
  emergencyContact?: string | null;
  // Academic info (Student)
  cohort?: string | null;
  className?: string | null;
  academicYear?: string | null;
  advisor?: string | null;
  status?: string | null;
  trainingType?: string | null;
  // Professional info (Teacher)
  department?: string | null;
}

export interface MajorResponse {
  id: number;
  majorCode: string;
  name: string;
  description?: string | null;
  studentCount?: number | null;
  courseCount?: number | null;
  departmentId?: number | null;
  departmentName?: string | null;
}

export interface AdminMajorRequest {
  majorCode: string;
  name: string;
  description?: string;
  departmentId?: number | null;
}

export interface RoomResponse {
  id: number;
  name: string;
  capacity: number;
  type?: "LECTURE" | "LAB" | "AUDITORIUM" | string | null;
  status?: "AVAILABLE" | "MAINTENANCE" | string | null;
}

export interface AdminRoomRequest {
  name: string;
  capacity: number;
  type?: "LECTURE" | "LAB" | "AUDITORIUM";
  status?: "AVAILABLE" | "MAINTENANCE";
}

export interface PeriodResponse {
  id: number;
  periodNumber: number;
  startTime: string;
  endTime: string;
}

export interface PeriodRequest {
  periodNumber: number;
  startTime: string;
  endTime: string;
}

export type AdminPeriodRequest = PeriodRequest;

export interface AdminSemesterResponse {
  id: number;
  name: string;
  startDate?: string | null;
  endDate?: string | null;
  registrationOpen?: boolean;
  locked?: boolean;
  examPublished?: boolean;
  retakeOpen?: boolean;
  retakeLocked?: boolean;
  ended?: boolean;
  activeRegistrationRoundId?: number | null;
  activeRegistrationRoundName?: string | null;
  activeRegistrationRoundNumber?: number | null;
}

export interface AdminUserResponse {
  id: number;
  username: string;
  passwordHash?: string;
  email: string;
  role: Role;
  active: boolean;
  fullName?: string | null;
  profileId?: number | null;
  createdAt?: string | null;
  lastLoginAt?: string | null;
}

export interface AdminUserUpdateRequest {
  username: string;
  email: string;
  fullName: string;
}

export interface AdminStudentResponse {
  id: number;
  studentCode: string;
  username: string;
  fullName: string;
  email: string;
  dob?: string | null;
  address?: string | null;
  academicYear?: number | string | null;
  cohort?: string | null;
  majorId?: number | null;
  majorCode?: string | null;
  majorName?: string | null;
  status?: string | null;
  gpa?: number | null;
  cpa?: number | null;
  totalCredits?: number | null;
  // extended profile fields
  gender?: string | null;
  phone?: string | null;
  nationalId?: string | null;
  placeOfBirth?: string | null;
  hometown?: string | null;
  permanentAddress?: string | null;
  currentAddress?: string | null;
  emergencyContact?: string | null;
  className?: string | null;
  homeroomId?: number | null;
  advisorId?: number | null;
  advisorName?: string | null;
  advisorCode?: string | null;
  trainingType?: string | null;
}

export interface AdminStudentRequest {
  username: string;
  password: string;
  email: string;
  studentCode: string;
  fullName: string;
  dob: string;
  majorId: number;
  academicYear: number;
  address?: string;
}

export interface AdminTeacherResponse {
  id: number;
  username?: string | null;
  email?: string | null;
  teacherCode: string;
  fullName: string;
  dob?: string | null;
  gender?: string | null;
  phone?: string | null;
  nationalId?: string | null;
  placeOfBirth?: string | null;
  hometown?: string | null;
  permanentAddress?: string | null;
  currentAddress?: string | null;
  emergencyContact?: string | null;
  departmentId?: number | null;
  departmentCode?: string | null;
  departmentName?: string | null;
  degree?: string | null;
  address?: string | null;
  status?: "DANG_GIANG_DAY" | "NGHI_PHEP" | "DA_NGHI_VIEC" | null;
}

export interface AdminTeacherRequest {
  username: string;
  password: string;
  email: string;
  teacherCode: string;
  fullName: string;
  dob?: string;
  departmentId?: number;
  degree?: string;
  address?: string;
  phone?: string;
}

export interface DepartmentResponse {
  id: number;
  departmentCode: string;
  name: string;
  description?: string | null;
  teacherCount?: number | null;
  majorCount?: number | null;
}

export interface DepartmentRequest {
  departmentCode: string;
  name: string;
  description?: string;
}

export interface HomeroomResponse {
  id: number;
  className: string;
  advisorId?: number | null;
  advisorCode?: string | null;
  advisorName?: string | null;
  majorId?: number | null;
  majorName?: string | null;
  academicYear?: number | null;
  cohort?: string | null;
  studentCount?: number | null;
  isActive?: boolean;
}

export interface HomeroomRequest {
  className: string;
  advisorId?: number | null;
  majorId?: number | null;
  academicYear?: number | null;
  cohort?: string;
}

export interface HomeroomStudentsRequest {
  studentIds: number[];
}

export interface AdminCourseRequest {
  code: string;
  name: string;
  credits: number;
  description?: string;
  courseType?: "REQUIRED" | "ELECTIVE";
  majorId: number;
  prerequisiteCourseIds?: number[];
}

export interface ClassSectionScheduleResponse {
  id: number;
  dayOfWeek: number;
  startPeriodId: number;
  startPeriod: number;
  endPeriodId: number;
  endPeriod: number;
  lessonCount?: number | null;
  periodRange?: string | null;
  startTime?: string | null;
  endTime?: string | null;
  roomId?: number | null;
  roomName?: string | null;
}

export type AdminClassSectionStatus = "DRAFT" | "OPEN" | "CLOSED" | "CANCELLED";

export interface ClassSectionScheduleRequest {
  dayOfWeek: number;
  startPeriodId: number;
  endPeriodId: number;
  roomId: number;
}

export interface AdminClassSectionRequest {
  classCode: string;
  courseId: number;
  semesterId: number;
  registrationRoundId?: number | null;
  teacherId?: number | null;
  schedules: ClassSectionScheduleRequest[];
  maxSlots: number;
}

export interface ClassSectionValidationIssueResponse {
  code: string;
  message: string;
}

export interface ClassSectionValidationResponse {
  valid: boolean;
  errors: ClassSectionValidationIssueResponse[];
  warnings: ClassSectionValidationIssueResponse[];
  infos?: ClassSectionValidationIssueResponse[];
}

export interface BulkClassSectionCourseRequest {
  courseId: number;
  classCount: number;
  maxSlots: number;
  sessionsPerWeek: number;
  periodsPerSession: number;
}

export interface BulkClassSectionProposalRequest {
  semesterId: number;
  courses: BulkClassSectionCourseRequest[];
}

export interface BulkClassSectionCourseSummaryResponse {
  courseId: number;
  courseCode: string;
  courseName: string;
  requestedCount: number;
  proposedCount: number;
  missingCount: number;
  message: string;
}

export interface BulkClassSectionProposalResponse {
  items: AdminClassSectionRequest[];
  summaries: BulkClassSectionCourseSummaryResponse[];
}

export interface BulkClassSectionValidationItemResponse {
  index: number;
  classCode: string;
  validation: ClassSectionValidationResponse;
}

export interface BulkClassSectionValidationResponse {
  valid: boolean;
  items: BulkClassSectionValidationItemResponse[];
}

export interface AdminClassSectionStudentResponse {
  enrollmentId: number;
  studentId: number;
  studentCode: string;
  fullName: string;
  email?: string | null;
  majorId?: number | null;
  majorCode?: string | null;
  majorName?: string | null;
  cohort?: string | null;
  academicYear?: number | string | null;
  enrolledAt?: string | null;
  status?: string | null;
}

export interface ClassSectionResponse {
  id: number;
  classCode: string;
  courseId: number;
  courseCode: string;
  courseName: string;
  majorName?: string | null;
  courseType?: CourseType | null;
  courseTypeLabel?: string | null;
  credits: number;
  semesterId: number;
  semesterName: string;
  registrationRoundId?: number | null;
  registrationRoundName?: string | null;
  registrationRoundNumber?: number | null;
  teacherId?: number | null;
  teacherName?: string | null;
  room?: string | null;
  roomId?: number | null;
  roomCapacity?: number | null;
  schedules: ClassSectionScheduleResponse[];
  maxSlots?: number | null;
  currentSlots?: number | null;
  status?: AdminClassSectionStatus | string | null;
  closed?: boolean;
  isClosed?: boolean;
  gradeLocked?: boolean | null;
  gradeStatus?: "DRAFT" | "SUBMITTED" | "LOCKED" | string | null;
  examAt?: string | null;
  examRoom?: string | null;
  examType?: "NORMAL" | "RETAKE" | "IMPROVE" | null;
  sourceExamSessionId?: number | null;
  virtualRetakeClass?: boolean;
  semesterEnded?: boolean;
}

export interface TeacherGradeRequest {
  enrollmentId: number;
  participationScore?: number | null;
  midTermScore?: number | null;
  finalScore?: number | null;
  retestScore?: number | null;
}

export interface TeacherStudentGradeResponse {
  enrollmentId: number;
  studentCode: string;
  fullName: string;
  phone?: string | null;
  email?: string | null;
  className?: string | null;
  advisorName?: string | null;
  majorName?: string | null;
  facultyName?: string | null;
  midTermScore?: number | null;
  finalScore?: number | null;
  totalScore?: number | null;
  status: "REGISTERED" | "PASSED" | "FAILED" | "CANCELED" | string;
  courseStatus?: CourseStudyStatus | null;
  absenceCount?: number | null;
}

export type TeacherGradeResponse = GradeResponse;

export interface StudentSemesterResponse {
  id: number;
  name: string;
  startDate?: string | null;
  endDate?: string | null;
  registrationOpen: boolean;
  locked: boolean;
  examPublished: boolean;
  retakeOpen: boolean;
  retakeLocked: boolean;
  ended?: boolean;
  activeRegistrationRoundId?: number | null;
  activeRegistrationRoundName?: string | null;
  activeRegistrationRoundNumber?: number | null;
}

export type AttendanceStatus = "PRESENT" | "LATE" | "ABSENT";
export type CourseStudyStatus =
  | "IN_PROGRESS"
  | "PASSED"
  | "BANNED_FROM_EXAM"
  | "REPEAT_COURSE"
  | "RETAKE_EXAM";

export interface AttendanceRecordRequest {
  enrollmentId: number;
  status: AttendanceStatus;
  note?: string | null;
}

export interface AttendanceRecordResponse {
  id: number;
  enrollmentId: number;
  studentCode: string;
  studentName: string;
  status: AttendanceStatus;
  note?: string | null;
}

export interface AttendanceSessionResponse {
  id: number;
  classSectionId: number;
  sessionNumber: number;
  weekNumber?: number | null;
  meetingIndex?: number | null;
  sessionDate?: string | null;
  locked: boolean;
  records: AttendanceRecordResponse[];
}

export interface EnrollmentRequestResponse {
  requestId?: string | null;
  message: string;
}

export interface EnrollmentRequestStatusResponse {
  requestId: string;
  status: EnrollmentRequestStatus | string;
  message?: string | null;
}

export interface EnrollmentResponse {
  enrollmentId: number;
  classSectionId?: number | null;
  courseCode?: string | null;
  classCode: string;
  courseName: string;
  credits: number;
  room?: string | null;
  schedules?: ClassSectionScheduleResponse[] | null;
  dayOfWeek: number;
  startPeriod: number;
  endPeriod: number;
  teacherName?: string | null;
  teacherCode?: string | null;
  teacherEmail?: string | null;
  midTermScore?: number | null;
  finalScore?: number | null;
  totalScore?: number | null;
  status?: EnrollmentStatus | string | null;
}

export interface StudentExamResponse {
  classCode: string;
  examSourceType?: string | null;
  courseName: string;
  credits: number;
  examAt?: string | null;
  examRoom?: string | null;
}

export interface StudentGradeItemResponse {
  enrollmentId: number;
  semesterId: number;
  semesterName: string;
  classCode: string;
  courseName: string;
  credits: number;
  totalScore?: number | null;
  gradePoint?: number | null;
}

export interface StudentGradesSummaryResponse {
  semesterId?: number | null;
  semesterGpa: number;
  cumulativeGpa: number;
  items: StudentGradeItemResponse[];
}

export interface GradeResponse {
  id: number;
  enrollmentId: number;
  studentId: number;
  studentCode: string;
  studentName: string;
  courseId: number;
  courseCode: string;
  classCode: string;
  courseName: string;
  credits: number;
  semesterId: number;
  semesterName: string;
  participationScore?: number | null;
  midtermScore?: number | null;
  finalScore?: number | null;
  retestScore?: number | null;
  attemptNumber?: number | null;
  enrollmentType?: EnrollmentType | string | null;
  totalScore?: number | null;
  letterGrade?: string | null;
  gpa4?: number | null;
  gradePoint?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  courseStatus?: CourseStudyStatus | string | null;
  absenceCount?: number | null;
  examRegistrationId?: number | null;
  examAttemptNumber?: number | null;
  studySemesterName?: string | null;
}

export interface TuitionItemResponse {
  feeType?: "COURSE" | "RETAKE" | string | null;
  courseCode: string;
  courseName: string;
  credits: number;
  pricePerCredit: number;
  subtotal: number;
}

export interface TuitionResponse {
  semesterName: string;
  totalCredits: number;
  totalAmount: number;
  paidAmount: number;
  pricePerCredit: number;
  paid: boolean;
  items: TuitionItemResponse[];
}

export interface StudentDashboardResponse {
  profile: UserProfile;
  currentSemester?: StudentSemesterResponse | null;
  learningResults?: LearningResultsResponse | null;
  grades?: StudentGradesSummaryResponse | null;
  tuition?: TuitionResponse | null;
  schedule?: EnrollmentResponse[];
  todaySchedule?: EnrollmentResponse[];
  exams?: StudentExamResponse[];
  upcomingExams?: StudentExamResponse[];
  semesterGpa: number;
  cumulativeGpa: number;
  registeredCredits: number;
  earnedCredits: number;
  gradedCourseCount: number;
  activeCourseCount: number;
  upcomingExamCount: number;
  tuitionRemaining: number;
  tuitionStatus: string;
  registrationStatus: string;
}

export interface AdminDashboardMajorCount {
  majorId?: number | null;
  majorCode?: string | null;
  majorName?: string | null;
  studentCount: number;
}

export interface AdminDashboardResponse {
  currentSemester?: StudentSemesterResponse | null;
  studentCount: number;
  teacherCount: number;
  courseCount: number;
  departmentCount: number;
  roomCount: number;
  roomCapacity: number;
  classSectionCount: number;
  openClassCount: number;
  assignedClassCount: number;
  scheduledClassCount: number;
  totalRegisteredSlots: number;
  pendingEnrollmentCount: number;
  registeredEnrollmentCount: number;
  totalCapacity: number;
  totalCourseCredits: number;
  averageOccupancy: number;
  assignedTeacherRate: number;
  scheduledClassRate: number;
  studentsByMajor: AdminDashboardMajorCount[];
  attentionClasses: ClassSectionResponse[];
  recentClasses: ClassSectionResponse[];
}

export interface TeacherDashboardResponse {
  profile: UserProfile;
  currentSemester?: StudentSemesterResponse | null;
  classes: ClassSectionResponse[];
  todaySchedule: ClassSectionResponse[];
  classCount: number;
  totalStudents: number;
  ungradedClassCount: number;
  todayScheduleCount: number;
}

export interface StudentCourseRegistrationOverviewResponse {
  semesters: StudentSemesterResponse[];
  currentSemester?: StudentSemesterResponse | null;
  availableClasses: ClassSectionResponse[];
  selectedEnrollments: EnrollmentResponse[];
  readonly: boolean;
  registrationStatus: string;
}

export interface StudentRetakeOverviewResponse {
  semesters: StudentSemesterResponse[];
  currentSemester?: StudentSemesterResponse | null;
  eligibleCourses: RetakeEligibleCourseResponse[];
  requests: RetakeRequestResponse[];
  readonly: boolean;
  registrationStatus: string;
}

export interface AdminClassSectionOptionsResponse {
  courses: CourseResponse[];
  semesters: AdminSemesterResponse[];
  teachers: AdminTeacherResponse[];
  rooms: RoomResponse[];
  periods: PeriodResponse[];
}

export interface AcademicResultStudentRef {
  id: number;
  studentCode: string;
  fullName: string;
  dob?: string | null;
  gender?: string | null;
  phone?: string | null;
  nationalId?: string | null;
  placeOfBirth?: string | null;
  hometown?: string | null;
  permanentAddress?: string | null;
  currentAddress?: string | null;
  emergencyContact?: string | null;
  address?: string | null;
  cohort?: string | null;
  className?: string | null;
  advisor?: string | null;
  status?: string | null;
  trainingType?: string | null;
  academicYear?: number | null;
}

export interface AcademicResultSemesterRef {
  id: number;
  name: string;
  startDate?: string | null;
  endDate?: string | null;
  registrationOpen?: boolean;
  locked?: boolean;
}

export interface AcademicResultResponse {
  id: number;
  student?: AcademicResultStudentRef | null;
  semester?: AcademicResultSemesterRef | null;
  semesterGpa?: number | null;
  cumulativeGpa?: number | null;
  totalCredits?: number | null;
  cumulativeCredits?: number | null;
  calculatedAt?: string | null;
}

export interface RetakeEligibleCourseResponse {
  gradeId: number;
  enrollmentId: number;
  courseId: number;
  courseCode: string;
  courseName: string;
  credits: number;
  previousTotalScore: number;
  previousAttemptNumber?: number | null;
  registrationType: RetakeRegistrationType | string;
  retakeFee: number;
}

export interface RetakeRegistrationRequest {
  semesterId?: number | null;
  courseIds: number[];
}

export interface RetakeRegisteredItemResponse {
  courseId: number;
  courseCode: string;
  courseName: string;
  credits: number;
  registrationType: RetakeRegistrationType | string;
  attemptNumber: number;
  feeCharged: number;
  examAt?: string | null;
  examRoom?: string | null;
}

export interface RetakeRegistrationResponse {
  registeredCourses: RetakeRegisteredItemResponse[];
  totalFee: number;
  registeredCount: number;
}

export interface RetakeRequestResponse {
  enrollmentId: number;
  classSectionId: number;
  classCode: string;
  courseId: number;
  courseCode: string;
  courseName: string;
  semesterId: number;
  semesterName: string;
  status?: string | null;
  enrollmentType?: EnrollmentType | string | null;
  attemptNumber?: number | null;
  totalScore?: number | null;
}

export interface SemesterRequest {
  name: string;
  startDate?: string | null;
  endDate?: string | null;
  registrationOpen: boolean;
}

export interface AdminSemesterRequest {
  name: string;
  startDate: string;
  endDate: string;
  registrationOpen?: boolean;
}

export interface SemesterGpaSummary {
  semesterId: number;
  semesterName: string;
  semesterGpa: number;
  cumulativeGpa: number;
  totalCredits: number;
  cumulativeCredits: number;
}

export interface LearningResultsResponse {
  semesterId?: number | null;
  semesterName?: string | null;
  semesterGpa?: number | null;
  cumulativeGpa?: number | null;
  semesterCredits?: number | null;
  cumulativeCredits?: number | null;
  grades: GradeResponse[];
  semesterSummaries: SemesterGpaSummary[];
}

export interface CourseResponse {
  id: number;
  code: string;
  name: string;
  credits: number;
  description?: string | null;
  courseType?: CourseType | null;
  courseTypeLabel?: string | null;
  majorId?: number | null;
  majorName?: string | null;
  departmentId?: number | null;
  departmentName?: string | null;
  prerequisiteCourseIds?: number[];
  prerequisiteNames?: string[];
}

export interface NotificationResponse {
  id: string;
  type: NotificationType;
  title: string;
  body?: string | null;
  link?: string | null;
  read: boolean;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface AdminEnrollmentResponse {
  enrollmentId: number;
  studentId: number;
  studentCode: string;
  studentName: string;
  classSectionId: number;
  classCode: string;
  semesterId: number;
  semesterName?: string | null;
  courseName: string;
  courseCode?: string | null;
  credits?: number | null;
  enrolledAt?: string | null;
  status: string;
  registrationRoundName?: string | null;
  registrationRoundNumber?: number | null;
}

export interface AdminOverrideEnrollmentRequest {
  studentId: number;
  classSectionId: number;
  note?: string;
}

export interface ExamScheduleRequest {
  classSectionId: number;
  examAt: string | null;
  examRoom: string | null;
  examType?: "NORMAL" | "RETAKE" | "IMPROVE" | null;
}

export interface ExamScheduleResponse {
  classSectionId: number;
  classCode: string;
  courseName: string;
  courseCode: string;
  credits: number;
  teacherName: string;
  examAt: string | null;
  examRoom: string | null;
  examType: "NORMAL" | "RETAKE" | "IMPROVE" | null;
  studentCount: number;
  semesterId: number;
  semesterName: string;
}

export interface RegistrationTimeSlotRequest {
  startTime: string;
  endTime: string;
  allowedMajorIds?: number[];
  allowedCohorts?: string[];
}

export interface RegistrationTimeSlotResponse {
  id: number;
  startTime: string;
  endTime: string;
  allowedMajorIds?: number[];
  allowedCohorts?: string[];
}

export interface RegistrationRoundRequest {
  name?: string;
  open?: boolean;
  roundType?: string;
  timeSlots?: RegistrationTimeSlotRequest[];
}

export interface RegistrationRoundResponse {
  id: number;
  semesterId: number;
  semesterName: string;
  name: string;
  roundNumber: number;
  registrationOpen: boolean;
  locked: boolean;
  classSectionCount: number;
  pendingEnrollments: number;
  registeredEnrollments: number;
  createdAt?: string | null;
  lockedAt?: string | null;
  roundType?: string;
  timeSlots?: RegistrationTimeSlotResponse[];
}

export interface ExamSessionRequest {
  courseId: number;
  examType?: "NORMAL" | "RETAKE" | "IMPROVE" | null;
  examAt: string;
  roomIds: number[];
  proctorIds?: (number | null)[] | null;
  allocationMethod?: "SEQUENTIAL" | "BALANCED" | null;
  candidateSelection?: "ALL" | "NORMAL_ONLY" | "RETAKE_ONLY" | null;
}

export interface ExamRoomAssignmentResponse {
  id: number;
  roomId: number;
  roomName: string;
  capacity: number;
  assignedCount: number;
  proctorId?: number | null;
  proctorCode?: string | null;
  proctorName?: string | null;
}

export interface ExamSessionResponse {
  id: number;
  semesterId: number;
  semesterName: string;
  courseId: number;
  courseCode: string;
  courseName: string;
  credits: number;
  examType: "NORMAL" | "RETAKE" | "IMPROVE";
  examAt: string;
  studentCount: number;
  rooms: ExamRoomAssignmentResponse[];
  candidateSelection?: "ALL" | "NORMAL_ONLY" | "RETAKE_ONLY" | null;
  assignedRetakeCount?: number | null;
  virtualClassCode?: string | null;
  virtualClassSectionId?: number | null;
  assignmentWarnings?: string[] | null;
}

export interface ExamSeatAssignmentResponse {
  id: number;
  studentId: number;
  studentCode: string;
  studentName: string;
  roomId: number;
  roomName: string;
  roomAssignmentId?: number;
  sourceType: string;
  enrollmentId?: number | null;
  examRegistrationId?: number | null;
  classCode?: string | null;
}

export interface SemesterSummaryResponse {
  semesterId: number;
  name: string;
  startDate?: string | null;
  endDate?: string | null;
  classSectionCount: number;
  examScheduledCount: number;
  examNotScheduledCount: number;
  enrollmentCount: number;
  pendingEnrollments: number;
  registeredEnrollments: number;
  retakeRegistrations: number;
  retakePending: number;
  retakeRegistered: number;
  registrationOpen: boolean;
  locked: boolean;
  examPublished: boolean;
  retakeOpen: boolean;
  retakeLocked: boolean;
  ended?: boolean;
  maxCreditsPerSemester: number;
  activeRegistrationRoundId?: number | null;
  activeRegistrationRoundName?: string | null;
  activeRegistrationRoundNumber?: number | null;
  registrationRoundCount?: number | null;
}

export interface AdminExamRegistrationResponse {
  id: number;
  studentId: number;
  studentCode: string;
  studentName: string;
  classSectionId?: number | null;
  classCode?: string | null;
  classAssigned?: boolean;
  courseName: string;
  courseCode: string;
  credits: number;
  semesterId: number;
  semesterName: string;
  status: string;
  registrationType: string;
  feeCharged: number | null;
  attemptNumber: number | null;
  examAt: string | null;
  examRoom: string | null;
  createdAt: string | null;
}

export interface AdminExamRegistrationSummary {
  semesterId: number;
  total: number;
  pending: number;
  registered: number;
  totalFeeCharged: number;
}

export interface ExamConflictResponse {
  studentCode: string;
  studentName: string;
  conflictingCourseCode: string;
  conflictingCourseName: string;
}

export interface ExamCandidateResponse {
  studentId: number;
  studentCode: string;
  studentName: string;
  sourceType: string;
  classCode?: string | null;
}
