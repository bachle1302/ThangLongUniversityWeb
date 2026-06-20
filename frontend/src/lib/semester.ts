import type { StudentSemesterResponse } from "@/lib/api/types";

function toDate(value?: string | null) {
  return value ? new Date(`${value}T00:00:00`) : null;
}

export function pickCurrentSemester(semesters: StudentSemesterResponse[], now = new Date()) {
  if (semesters.length === 0) return null;

  const current = semesters.find((semester) => {
    const start = toDate(semester.startDate);
    const end = toDate(semester.endDate);
    return start != null && end != null && now >= start && now <= end;
  });
  if (current) return current;

  const open = semesters.find((semester) => semester.registrationOpen);
  if (open) return open;

  const past = semesters
    .filter((semester) => {
      const start = toDate(semester.startDate);
      return start != null && start <= now;
    })
    .sort((a, b) => {
      const aStart = toDate(a.startDate)?.getTime() ?? 0;
      const bStart = toDate(b.startDate)?.getTime() ?? 0;
      return bStart - aStart;
    })[0];
  if (past) return past;

  return semesters[0];
}
