import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { PageHeader, StatCard } from "@/components/ui/page-header";
import { Skeleton } from "@/components/ui/skeleton";
import { Award, BookOpen, CalendarCheck, GraduationCap, Layers, Receipt } from "lucide-react";
import { studentApi } from "@/lib/api/student";
import type { EnrollmentResponse } from "@/lib/api/types";

export const Route = createFileRoute("/student/dashboard")({ component: StudentDashboardPage });

const dayLabels: Record<number, string> = {
  2: "Thứ 2",
  3: "Thứ 3",
  4: "Thứ 4",
  5: "Thứ 5",
  6: "Thứ 6",
  7: "Thứ 7",
  8: "CN",
};

function formatVND(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function formatExamDate(examAt: string | null | undefined) {
  if (!examAt) return "-";
  const d = new Date(examAt);
  return d.toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function getTodaySlots(items: EnrollmentResponse[], apiDayOfWeek: number) {
  return items.flatMap((item) => {
    const schedules = item.schedules?.length
      ? item.schedules
      : [
          {
            dayOfWeek: item.dayOfWeek,
            startPeriod: item.startPeriod,
            endPeriod: item.endPeriod,
            roomName: item.room,
          },
        ];

    return schedules
      .filter((schedule) => schedule.dayOfWeek === apiDayOfWeek)
      .map((schedule) => ({
        ...item,
        room: schedule.roomName ?? item.room,
        startPeriod: schedule.startPeriod,
        endPeriod: schedule.endPeriod,
      }));
  });
}

function StudentDashboardPage() {
  const dashboardQuery = useQuery({
    queryKey: ["student", "dashboard"],
    queryFn: () => studentApi.getDashboard(),
    staleTime: 2 * 60 * 1000,
  });

  if (dashboardQuery.isPending) {
    return <DashboardSkeleton />;
  }

  if (dashboardQuery.isError) {
    return (
      <div>
        <PageHeader title="Bảng điều khiển sinh viên" description="Tổng quan học tập và học phí" />
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          {dashboardQuery.error instanceof Error
            ? dashboardQuery.error.message
            : "Không tải được bảng điều khiển"}
        </div>
      </div>
    );
  }

  const dashboard = dashboardQuery.data;
  const profile = dashboard.profile;
  const currentSemester = dashboard?.currentSemester;

  const jsToday = new Date().getDay();
  const apiToday = jsToday === 0 ? 8 : jsToday + 1;
  const todaySchedule = getTodaySlots(dashboard?.todaySchedule ?? [], apiToday).slice(0, 4);
  const credits = dashboard?.registeredCredits ?? 0;
  const tuitionRemaining = dashboard?.tuitionRemaining ?? 0;
  const displayName = profile.fullName;

  const courseCount = dashboard?.activeCourseCount ?? 0;
  const upcomingExams = dashboard?.upcomingExams ?? [];
  const nextExamsPanel = upcomingExams.slice(0, 4);

  return (
    <div>
      <PageHeader
        title={`Xin chào, ${displayName.split(" ").slice(-1)[0]}!`}
        description={profile?.code ? `Mã SV ${profile.code}` : currentSemester?.name}
      />
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-3 xl:grid-cols-6">
        <StatCard
          label="GPA học kỳ"
          value={(dashboard?.semesterGpa ?? 0).toFixed(2)}
          icon={Award}
          tone="primary"
        />
        <StatCard
          label="CPA tích lũy"
          value={(dashboard?.cumulativeGpa ?? 0).toFixed(2)}
          icon={GraduationCap}
          tone="info"
        />
        <StatCard label="Tín chỉ" value={credits} icon={BookOpen} tone="success" />
        <StatCard label="Môn đang học" value={courseCount} icon={Layers} tone="info" />
        <StatCard
          label="Lịch thi sắp tới"
          value={dashboard?.upcomingExamCount ?? upcomingExams.length}
          icon={CalendarCheck}
          tone={upcomingExams.length > 0 ? "warning" : "success"}
        />
        <StatCard
          label="Học phí còn nợ"
          value={formatVND(tuitionRemaining)}
          icon={Receipt}
          tone={tuitionRemaining > 0 ? "warning" : "success"}
        />
      </div>
      <div className="mt-6 grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div className="rounded-xl border bg-card p-5 shadow-sm">
          <h2 className="text-sm font-semibold">Lịch học hôm nay - {dayLabels[apiToday]}</h2>
          {todaySchedule.length === 0 ? (
            <p className="mt-4 text-sm text-muted-foreground">Bạn không có lịch hôm nay.</p>
          ) : (
            <ul className="mt-3 divide-y">
              {todaySchedule.map((item) => (
                <li
                  key={item.enrollmentId}
                  className="flex items-center justify-between py-3 text-sm"
                >
                  <div>
                    <div className="font-medium">{item.courseName}</div>
                    <div className="font-mono text-xs text-muted-foreground">
                      {item.classCode} - Phòng {item.room ?? "-"}
                    </div>
                  </div>
                  <span className="tabular-nums text-muted-foreground">
                    Tiết {item.startPeriod}-{item.endPeriod}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
        <div className="rounded-xl border bg-card p-5 shadow-sm">
          <h2 className="text-sm font-semibold">Lịch thi sắp tới</h2>
          {nextExamsPanel.length === 0 ? (
            <p className="mt-4 text-sm text-muted-foreground">Không có lịch thi sắp tới.</p>
          ) : (
            <ul className="mt-3 divide-y">
              {nextExamsPanel.map((exam, i) => (
                <li key={i} className="flex items-center justify-between py-3 text-sm">
                  <div>
                    <div className="font-medium">{exam.courseName}</div>
                    <div className="font-mono text-xs text-muted-foreground">
                      Phòng {exam.examRoom ?? "-"}
                    </div>
                  </div>
                  <span className="tabular-nums text-muted-foreground text-right">
                    {formatExamDate(exam.examAt)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
      <div className="mt-4 rounded-xl border bg-card p-5 shadow-sm">
        <h2 className="text-sm font-semibold">Tổng quan học kỳ</h2>
        <div className="mt-3 grid grid-cols-2 gap-3 text-sm sm:grid-cols-4">
          <div className="flex flex-col">
            <span className="text-muted-foreground">Học kỳ</span>
            <span className="font-medium">{currentSemester?.name ?? "-"}</span>
          </div>
          <div className="flex flex-col">
            <span className="text-muted-foreground">Số môn đang học</span>
            <span className="font-medium tabular-nums">{courseCount}</span>
          </div>
          <div className="flex flex-col">
            <span className="text-muted-foreground">Trạng thái học phí</span>
            <span className="font-medium">{dashboard?.tuitionStatus ?? "-"}</span>
          </div>
          <div className="flex flex-col">
            <span className="text-muted-foreground">Đăng ký học phần</span>
            <span className="font-medium">{dashboard?.registrationStatus ?? "-"}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function DashboardSkeleton() {
  return (
    <div>
      <PageHeader title="Bảng điều khiển sinh viên" description="Đang tải dữ liệu tổng quan" />
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-3 xl:grid-cols-6">
        {[0, 1, 2, 3, 4, 5].map((item) => (
          <Skeleton key={item} className="h-24 rounded-xl" />
        ))}
      </div>
      <div className="mt-6 grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Skeleton className="h-48 rounded-xl" />
        <Skeleton className="h-48 rounded-xl" />
      </div>
      <Skeleton className="mt-4 h-32 rounded-xl" />
    </div>
  );
}
