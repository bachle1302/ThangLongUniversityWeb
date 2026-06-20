import { useQuery } from "@tanstack/react-query";
import { createFileRoute, Link } from "@tanstack/react-router";
import { CalendarClock, GraduationCap, Layers, NotebookPen, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { PageHeader, StatCard } from "@/components/ui/page-header";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/ui/status-badge";
import { teacherApi } from "@/lib/api/teacher";

export const Route = createFileRoute("/teacher/dashboard")({ component: TeacherDashboardPage });

const dayLabels: Record<number, string> = {
  2: "Thu 2",
  3: "Thu 3",
  4: "Thu 4",
  5: "Thu 5",
  6: "Thu 6",
  7: "Thu 7",
  8: "CN",
};

function TeacherDashboardPage() {
  const dashboardQuery = useQuery({
    queryKey: ["teacher", "dashboard"],
    queryFn: teacherApi.getDashboard,
    staleTime: 60 * 1000,
  });

  if (dashboardQuery.isPending) {
    return <DashboardSkeleton />;
  }

  const dashboard = dashboardQuery.data;
  const profile = dashboard?.profile;
  const classes = dashboard?.classes ?? [];
  const currentSemester = dashboard?.currentSemester;
  const jsToday = new Date().getDay();
  const today = jsToday === 0 ? 8 : jsToday + 1;
  const todaySchedule = classes.flatMap((cs) =>
    (cs.schedules ?? []).filter((s) => s.dayOfWeek === today).map((s) => ({ cs, s })),
  );
  const displayName = profile?.fullName ?? profile?.username ?? "Giang vien";

  return (
    <div className="space-y-5">
      <PageHeader
        title={`Xin chào, ${displayName.split(" ").slice(-1)[0]}!`}
        description={
          dashboardQuery.isError
            ? "Chưa tải được dữ liệu bảng điều khiển từ backend"
            : profile?.code
              ? `Mã GV ${profile.code}`
              : currentSemester?.name
        }
        actions={
          <Button asChild variant="outline" className="gap-2">
            <Link to="/teacher/grades">
              <NotebookPen className="h-4 w-4" />
              Nhập điểm
            </Link>
          </Button>
        }
      />

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard
          label="Lớp đang dạy"
          value={dashboard?.classCount ?? classes.length}
          icon={Layers}
          tone="primary"
        />
        <StatCard
          label="Tổng sinh viên"
          value={dashboard?.totalStudents ?? 0}
          icon={GraduationCap}
          tone="info"
        />
        <StatCard
          label="Lớp chưa khóa điểm"
          value={dashboard?.ungradedClassCount ?? 0}
          icon={NotebookPen}
          tone={(dashboard?.ungradedClassCount ?? 0) > 0 ? "warning" : "success"}
        />
        <StatCard
          label="Lịch hôm nay"
          value={dashboard?.todayScheduleCount ?? todaySchedule.length}
          icon={CalendarClock}
          tone="success"
        />
      </div>

      <div className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
        <section className="rounded-xl border bg-card p-5 shadow-sm">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold">Lịch dạy hôm nay - {dayLabels[today]}</h2>
              <p className="text-xs text-muted-foreground">
                Học kỳ: {currentSemester?.name ?? "-"}
              </p>
            </div>
            <CalendarClock className="h-5 w-5 text-muted-foreground" />
          </div>

          {todaySchedule.length === 0 ? (
            <p className="mt-5 rounded-lg border border-dashed p-6 text-center text-sm text-muted-foreground">
              Hôm nay bạn không có lịch dạy.
            </p>
          ) : (
            <ul className="mt-3 divide-y">
              {todaySchedule.slice(0, 5).map((x, i) => (
                <li
                  key={`${x.cs.id}-${i}`}
                  className="flex items-center justify-between py-3 text-sm"
                >
                  <div>
                    <div className="font-medium">{x.cs.courseName}</div>
                    <div className="font-mono text-xs text-muted-foreground">
                      {x.cs.classCode} - Phòng {x.s.roomName ?? x.cs.room ?? "-"}
                    </div>
                  </div>
                  <span className="tabular-nums text-muted-foreground">
                    Tiết {x.s.startPeriod}-{x.s.endPeriod}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="rounded-xl border bg-card p-5 shadow-sm">
          <div className="flex items-center justify-between gap-3">
            <h2 className="text-sm font-semibold">Việc cần làm</h2>
            <Users className="h-5 w-5 text-muted-foreground" />
          </div>

          {classes.length === 0 ? (
            <p className="mt-4 text-sm text-muted-foreground">Chưa có lớp nào trong học kỳ này.</p>
          ) : (
            <div className="mt-4 space-y-3">
              {classes.slice(0, 5).map((c) => (
                <div key={c.id} className="rounded-lg border bg-background p-3">
                  <div className="flex items-center justify-between gap-2">
                    <div className="font-medium">{c.classCode}</div>
                    <StatusBadge value={c.gradeLocked ? "LOCKED" : "OPEN"} />
                  </div>
                  <div className="mt-1 text-xs text-muted-foreground">
                    {c.courseName} - {c.currentSlots ?? "-"} SV
                  </div>
                  <div className="mt-3 flex justify-end">
                    <Button asChild size="sm" variant="outline">
                      <Link
                        to="/teacher/classes/$classSectionId/students"
                        params={{ classSectionId: String(c.id) }}
                      >
                        Xem lớp
                      </Link>
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

function DashboardSkeleton() {
  return (
    <div className="space-y-5">
      <PageHeader title="Bang dieu khien giang vien" description="Dang tai du lieu tong quan" />
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {[0, 1, 2, 3].map((item) => (
          <Skeleton key={item} className="h-24 rounded-xl" />
        ))}
      </div>
      <div className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
        <Skeleton className="h-64 rounded-xl" />
        <Skeleton className="h-64 rounded-xl" />
      </div>
    </div>
  );
}
