import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { lazy, Suspense, useState, type ReactNode } from "react";
import { adminApi } from "@/lib/api/admin";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useSemesterRealtime } from "@/hooks/useSemesterRealtime";
import {
  ArrowLeft,
  BookOpen,
  CalendarCheck,
  CalendarDays,
  ClipboardList,
  Layers,
  GraduationCap,
} from "lucide-react";
const OverviewTab = lazy(() =>
  import("@/features/semester-hub/OverviewTab").then((module) => ({
    default: module.OverviewTab,
  })),
);
const ClassSectionsTab = lazy(() =>
  import("@/features/semester-hub/ClassSectionsTab").then((module) => ({
    default: module.ClassSectionsTab,
  })),
);
const EnrollmentsTab = lazy(() =>
  import("@/features/semester-hub/EnrollmentsTab").then((module) => ({
    default: module.EnrollmentsTab,
  })),
);
const RetakeRegistrationsTab = lazy(() =>
  import("@/features/semester-hub/RetakeRegistrationsTab").then((module) => ({
    default: module.RetakeRegistrationsTab,
  })),
);
const ExamSchedulesTab = lazy(() =>
  import("@/features/semester-hub/ExamSchedulesTab").then((module) => ({
    default: module.ExamSchedulesTab,
  })),
);

export const Route = createFileRoute("/admin/semesters/$id")({ component: SemesterHubPage });

function SemesterHubPage() {
  const { id } = Route.useParams();
  const semesterId = Number(id);
  const [activeTab, setActiveTab] = useState("overview");
  useSemesterRealtime(semesterId);

  const semestersQuery = useQuery({
    queryKey: ["admin", "semesters"],
    queryFn: adminApi.listSemesters,
    staleTime: 60_000,
  });
  const semester = semestersQuery.data?.find((s) => s.id === semesterId);

  if (semestersQuery.isLoading) {
    return (
      <div className="p-6 space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  if (!semester) {
    return (
      <div className="p-6">
        <p className="text-destructive">Không tìm thấy học kỳ (id={id})</p>
        <Link to="/admin/semesters">
          <Button variant="outline" className="mt-4">
            ← Quay lại
          </Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between gap-4">
        <div>
          <Link
            to="/admin/semesters"
            className="inline-flex items-center text-sm text-muted-foreground hover:text-foreground transition-colors mb-2"
          >
            <ArrowLeft className="h-3.5 w-3.5 mr-1" />
            Quản lý Học kỳ
          </Link>
          <h1 className="text-2xl font-bold flex items-center gap-2">
            <CalendarDays className="h-6 w-6 text-primary" />
            {semester.name}
          </h1>
          <p className="text-sm text-muted-foreground mt-0.5">
            {semester.startDate
              ? new Intl.DateTimeFormat("vi-VN").format(new Date(semester.startDate))
              : "?"}{" "}
            →{" "}
            {semester.endDate
              ? new Intl.DateTimeFormat("vi-VN").format(new Date(semester.endDate))
              : "?"}
          </p>
        </div>
        <Link to="/admin/semesters">
          <Button variant="outline" size="sm">
            <ArrowLeft className="h-4 w-4 mr-1" />
            Quay lại
          </Button>
        </Link>
      </div>

      {/* Hub tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-5 h-11 bg-muted/50 p-1 rounded-lg">
          <TabsTrigger
            value="overview"
            className="rounded-md data-[state=active]:bg-background data-[state=active]:shadow-sm gap-1.5"
          >
            <BookOpen className="h-3.5 w-3.5" /> Tổng quan
          </TabsTrigger>
          <TabsTrigger
            value="class-sections"
            className="rounded-md data-[state=active]:bg-background data-[state=active]:shadow-sm gap-1.5"
          >
            <Layers className="h-3.5 w-3.5" /> Lớp học phần
          </TabsTrigger>
          <TabsTrigger
            value="enrollments"
            className="rounded-md data-[state=active]:bg-background data-[state=active]:shadow-sm gap-1.5"
          >
            <ClipboardList className="h-3.5 w-3.5" /> Đăng ký học
          </TabsTrigger>
          <TabsTrigger
            value="retake-registrations"
            className="rounded-md data-[state=active]:bg-background data-[state=active]:shadow-sm gap-1.5"
          >
            <GraduationCap className="h-3.5 w-3.5" /> Đăng ký thi lại/nâng
          </TabsTrigger>
          <TabsTrigger
            value="exam-schedules"
            className="rounded-md data-[state=active]:bg-background data-[state=active]:shadow-sm gap-1.5"
          >
            <CalendarCheck className="h-3.5 w-3.5" /> Lịch thi
          </TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="mt-6">
          <LazyTabPanel>
            {activeTab === "overview" && <OverviewTab semesterId={semesterId} />}
          </LazyTabPanel>
        </TabsContent>
        <TabsContent value="class-sections" className="mt-6">
          <LazyTabPanel>
            {activeTab === "class-sections" && <ClassSectionsTab semesterId={semesterId} />}
          </LazyTabPanel>
        </TabsContent>
        <TabsContent value="enrollments" className="mt-6">
          <LazyTabPanel>
            {activeTab === "enrollments" && <EnrollmentsTab semesterId={semesterId} />}
          </LazyTabPanel>
        </TabsContent>
        <TabsContent value="retake-registrations" className="mt-6">
          <LazyTabPanel>
            {activeTab === "retake-registrations" && (
              <RetakeRegistrationsTab semesterId={semesterId} />
            )}
          </LazyTabPanel>
        </TabsContent>
        <TabsContent value="exam-schedules" className="mt-6">
          <LazyTabPanel>
            {activeTab === "exam-schedules" && <ExamSchedulesTab semesterId={semesterId} />}
          </LazyTabPanel>
        </TabsContent>
      </Tabs>
    </div>
  );
}

function LazyTabPanel({ children }: { children: ReactNode }) {
  return (
    <Suspense fallback={<Skeleton className="h-80 w-full" />}>
      {children}
    </Suspense>
  );
}
