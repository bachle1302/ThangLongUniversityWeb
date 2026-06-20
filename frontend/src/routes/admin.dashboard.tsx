import { useQuery } from "@tanstack/react-query";
import { createFileRoute, Link } from "@tanstack/react-router";
import type { LucideIcon } from "lucide-react";
import {
  AlertTriangle,
  ArrowUpRight,
  BookOpen,
  Building2,
  CalendarDays,
  CheckCircle2,
  GraduationCap,
  Layers,
  School,
  Users,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { PageHeader } from "@/components/ui/page-header";
import { Progress } from "@/components/ui/progress";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from "@/components/ui/skeleton";
import { adminApi } from "@/lib/api/admin";
import type { ClassSectionResponse } from "@/lib/api/types";
import { useSemesterRealtime } from "@/hooks/useSemesterRealtime";

type DashboardSearch = {
  semesterId?: number;
};

export const Route = createFileRoute("/admin/dashboard")({
  validateSearch: (search: Record<string, unknown>): DashboardSearch => {
    const semesterId = Number(search.semesterId);
    return Number.isFinite(semesterId) && semesterId > 0 ? { semesterId } : {};
  },
  component: AdminDashboard,
});

function percentValue(value?: number | null) {
  return Math.min(100, Math.max(0, Math.round(value ?? 0)));
}

function formatDate(value?: string | null) {
  if (!value) return "-";
  return new Date(value).toLocaleDateString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}

function occupancy(section: ClassSectionResponse) {
  const current = section.currentSlots ?? 0;
  const max = section.maxSlots ?? section.roomCapacity ?? 0;
  const rate = max > 0 ? Math.min(100, Math.round((current / max) * 100)) : 0;
  return { current, max, rate };
}

function AdminDashboard() {
  const { semesterId } = Route.useSearch();
  const navigate = Route.useNavigate();
  const semestersQuery = useQuery({
    queryKey: ["admin", "semesters"],
    queryFn: adminApi.listSemesters,
    staleTime: 60 * 1000,
  });
  const dashboardQuery = useQuery({
    queryKey: ["admin", "dashboard", semesterId ?? "current"],
    queryFn: () => adminApi.getDashboard(semesterId),
    staleTime: 60 * 1000,
  });
  useSemesterRealtime(dashboardQuery.data?.currentSemester?.id);

  if (dashboardQuery.isPending) {
    return <DashboardSkeleton />;
  }

  const dashboard = dashboardQuery.data;
  const currentSemester = dashboard?.currentSemester;
  const attentionClasses = dashboard?.attentionClasses ?? [];
  const recentClasses = dashboard?.recentClasses ?? [];

  return (
    <div className="space-y-5">
      <PageHeader
        title={currentSemester?.name ?? "Dashboard học kỳ"}
        description="Tổng hợp riêng dữ liệu lớp học phần và đăng ký của học kỳ đang chọn"
        actions={
          <Select
            value={String(semesterId ?? currentSemester?.id ?? "")}
            onValueChange={(value) => {
              void navigate({
                search: { semesterId: Number(value) },
                replace: true,
              });
            }}
          >
            <SelectTrigger className="w-full sm:w-72">
              <SelectValue placeholder="Chọn học kỳ" />
            </SelectTrigger>
            <SelectContent>
              {(semestersQuery.data ?? []).map((semester) => (
                <SelectItem key={semester.id} value={String(semester.id)}>
                  {semester.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        }
      />

      {dashboardQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          {dashboardQuery.error instanceof Error
            ? dashboardQuery.error.message
            : "Không tải được dashboard"}
        </div>
      )}

      <section className="overflow-hidden rounded-xl border bg-card shadow-sm">
        <div className="grid gap-0 lg:grid-cols-[1.4fr_0.9fr]">
          <div className="border-b p-5 lg:border-b-0 lg:border-r">
            <div className="flex flex-wrap items-center gap-2">
              <Badge variant="secondary" className="gap-1.5">
                <CalendarDays className="h-3.5 w-3.5" />
                {currentSemester?.name ?? "Chưa có học kỳ hiện tại"}
              </Badge>
              <Badge variant={currentSemester?.registrationOpen ? "default" : "outline"}>
                Đăng ký {currentSemester?.registrationOpen ? "đang mở" : "đã đóng"}
              </Badge>
              <Badge variant={currentSemester?.examPublished ? "default" : "outline"}>
                Lịch thi {currentSemester?.examPublished ? "đã công bố" : "chưa công bố"}
              </Badge>
            </div>
            <div className="mt-5 grid gap-4 sm:grid-cols-3">
              <SystemMetric
                label="Lớp học phần"
                value={dashboard?.classSectionCount ?? 0}
                helper="Thuộc học kỳ này"
              />
              <SystemMetric
                label="Chờ chốt"
                value={dashboard?.pendingEnrollmentCount ?? 0}
                helper="Lượt đăng ký PENDING"
              />
              <SystemMetric
                label="Đã chốt"
                value={dashboard?.registeredEnrollmentCount ?? 0}
                helper="Lượt đăng ký REGISTERED"
              />
            </div>
          </div>
          <div className="p-5">
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="text-sm font-semibold">Sức chứa lớp học phần</h2>
                <p className="text-xs text-muted-foreground">
                  {dashboard?.totalRegisteredSlots ?? 0} đăng ký trên{" "}
                  {dashboard?.totalCapacity ?? 0} chỗ ngồi
                </p>
              </div>
              <School className="h-5 w-5 text-muted-foreground" />
            </div>
            <div className="mt-5">
              <div className="mb-2 flex items-end justify-between">
                <span className="text-3xl font-semibold tabular-nums">
                  {percentValue(dashboard?.averageOccupancy)}%
                </span>
                <span className="text-xs text-muted-foreground">Mức lấp đầy trung bình</span>
              </div>
              <Progress value={percentValue(dashboard?.averageOccupancy)} className="h-2.5" />
              <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
                <InfoLine label="Bắt đầu" value={formatDate(currentSemester?.startDate)} />
                <InfoLine label="Kết thúc" value={formatDate(currentSemester?.endDate)} />
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="grid gap-4 md:grid-cols-3">
        <SemesterStatusCard
          label="Lớp đang mở"
          value={dashboard?.openClassCount ?? 0}
          helper={`${dashboard?.classSectionCount ?? 0} lớp trong học kỳ`}
          icon={Layers}
        />
        <SemesterStatusCard
          label="Đã phân công giảng viên"
          value={`${percentValue(dashboard?.assignedTeacherRate)}%`}
          helper={`${dashboard?.assignedClassCount ?? 0}/${dashboard?.classSectionCount ?? 0} lớp`}
          icon={Users}
        />
        <SemesterStatusCard
          label="Đã xếp lịch học"
          value={`${percentValue(dashboard?.scheduledClassRate)}%`}
          helper={`${dashboard?.scheduledClassCount ?? 0}/${dashboard?.classSectionCount ?? 0} lớp`}
          icon={CalendarDays}
        />
      </div>

      <div className="grid gap-4 xl:grid-cols-[1.1fr_0.9fr]">
        <section className="rounded-xl border bg-card p-5 shadow-sm">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold">Lớp học phần cần chú ý</h2>
              <p className="text-xs text-muted-foreground">
                Ưu tiên lớp thiếu giảng viên, chưa xếp lịch hoặc gần đầy
              </p>
            </div>
            <AlertTriangle className="h-5 w-5 text-warning-foreground" />
          </div>

          {attentionClasses.length === 0 ? (
            <EmptyState text="Không có lớp nào cần chú ý ngay." />
          ) : (
            <div className="mt-4 divide-y">
              {attentionClasses.map((section) => {
                const load = occupancy(section);
                return (
                  <div key={section.id} className="py-3">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <div className="font-medium">{section.classCode}</div>
                        <div className="mt-0.5 text-xs text-muted-foreground">
                          {section.courseName}
                        </div>
                      </div>
                      <Badge variant={load.rate >= 90 ? "destructive" : "outline"}>
                        {load.current}/{load.max || "-"}
                      </Badge>
                    </div>
                    <div className="mt-3 flex flex-wrap gap-2 text-xs">
                      {!section.teacherId && <Badge variant="outline">Chưa có giảng viên</Badge>}
                      {!section.schedules?.length && <Badge variant="outline">Chưa xếp lịch</Badge>}
                      {load.rate >= 90 && <Badge variant="outline">Gần đầy lớp</Badge>}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>

        <section className="rounded-xl border bg-card p-5 shadow-sm">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold">Lớp học phần mới cập nhật</h2>
              <p className="text-xs text-muted-foreground">Chỉ hiển thị lớp thuộc học kỳ này</p>
            </div>
            <Layers className="h-5 w-5 text-muted-foreground" />
          </div>

          {recentClasses.length === 0 ? (
            <EmptyState text="Chưa có lớp học phần nào." />
          ) : (
            <div className="mt-4 divide-y">
              {recentClasses.map((section) => {
                const load = occupancy(section);
                return (
                  <div
                    key={section.id}
                    className="flex items-center justify-between gap-4 py-3 text-sm"
                  >
                    <div className="min-w-0">
                      <div className="truncate font-medium">{section.classCode}</div>
                      <div className="truncate text-xs text-muted-foreground">
                        {section.courseName} - {section.teacherName ?? "Chưa có GV"}
                      </div>
                    </div>
                    <div className="w-28 shrink-0">
                      <div className="mb-1 text-right text-xs tabular-nums text-muted-foreground">
                        {load.current}/{load.max || "-"}
                      </div>
                      <Progress value={load.rate} />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>
      </div>

      <div>
        <section className="rounded-xl border bg-card p-5 shadow-sm">
          <div className="flex items-center justify-between gap-3">
            <h2 className="text-sm font-semibold">Thao tác nhanh</h2>
            <CheckCircle2 className="h-5 w-5 text-muted-foreground" />
          </div>
          <div className="mt-4 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            <QuickAction to="/admin/semesters" icon={CalendarDays} label="Quản lý học kỳ" />
            <QuickAction to="/admin/class-sections" icon={Layers} label="Mở lớp học phần" />
            <QuickAction to="/admin/students" icon={GraduationCap} label="Danh sách sinh viên" />
            <QuickAction to="/admin/teachers" icon={Users} label="Phân công giảng viên" />
            <QuickAction to="/admin/courses" icon={BookOpen} label="Danh mục học phần" />
            <QuickAction to="/admin/departments" icon={Building2} label="Khoa / Bộ môn" />
          </div>
        </section>
      </div>
    </div>
  );
}

function SystemMetric({
  label,
  value,
  helper,
}: {
  label: string;
  value: string | number;
  helper: string;
}) {
  return (
    <div className="rounded-lg border bg-background p-4">
      <div className="text-xs font-medium uppercase text-muted-foreground">{label}</div>
      <div className="mt-2 text-2xl font-semibold tabular-nums">{value}</div>
      <div className="mt-1 text-xs text-muted-foreground">{helper}</div>
    </div>
  );
}

function SemesterStatusCard({
  label,
  value,
  helper,
  icon: Icon,
}: {
  label: string;
  value: string | number;
  helper: string;
  icon: LucideIcon;
}) {
  return (
    <div className="flex items-center justify-between gap-4 rounded-xl border bg-card p-5 shadow-sm">
      <div>
        <div className="text-sm text-muted-foreground">{label}</div>
        <div className="mt-1 text-3xl font-semibold tabular-nums">{value}</div>
        <div className="mt-1 text-xs text-muted-foreground">{helper}</div>
      </div>
      <div className="rounded-xl bg-primary/10 p-3 text-primary">
        <Icon className="h-5 w-5" />
      </div>
    </div>
  );
}

function InfoLine({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg bg-muted/50 p-3">
      <div className="text-xs text-muted-foreground">{label}</div>
      <div className="mt-1 font-medium">{value}</div>
    </div>
  );
}

function QuickAction({ to, icon: Icon, label }: { to: string; icon: LucideIcon; label: string }) {
  return (
    <Button asChild variant="outline" className="h-12 justify-between gap-3 px-3">
      <Link to={to}>
        <span className="flex min-w-0 items-center gap-2">
          <Icon className="h-4 w-4 shrink-0" />
          <span className="truncate">{label}</span>
        </span>
        <ArrowUpRight className="h-4 w-4 shrink-0 text-muted-foreground" />
      </Link>
    </Button>
  );
}

function EmptyState({ text }: { text: string }) {
  return (
    <p className="mt-4 rounded-lg border border-dashed p-6 text-center text-sm text-muted-foreground">
      {text}
    </p>
  );
}

function DashboardSkeleton() {
  return (
    <div className="space-y-5">
      <PageHeader title="Dashboard học kỳ" description="Đang tải dữ liệu học kỳ hiện tại" />
      <Skeleton className="h-56 rounded-xl" />
      <div className="grid gap-4 md:grid-cols-3">
        {[0, 1, 2].map((item) => (
          <Skeleton key={item} className="h-28 rounded-xl" />
        ))}
      </div>
      <div className="grid gap-4 xl:grid-cols-2">
        <Skeleton className="h-72 rounded-xl" />
        <Skeleton className="h-72 rounded-xl" />
      </div>
    </div>
  );
}
