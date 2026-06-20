import type { SemesterSummaryResponse } from "@/lib/api/types";

export type { SemesterSummaryResponse };

export type TabId = "overview" | "class-sections" | "enrollments" | "exam-schedules";

export const TABS: { id: TabId; label: string }[] = [
  { id: "overview", label: "Tổng quan" },
  { id: "class-sections", label: "Lớp học phần" },
  { id: "enrollments", label: "Đăng ký học" },
  { id: "exam-schedules", label: "Lịch thi" },
];
