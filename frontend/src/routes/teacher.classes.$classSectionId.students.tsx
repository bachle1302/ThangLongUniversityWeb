import { useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute, Link } from "@tanstack/react-router";
import { ChevronLeft } from "lucide-react";
import { useMemo } from "react";
import { DataTable } from "@/components/data-table/DataTable";
import { Button } from "@/components/ui/button";
import { PageHeader } from "@/components/ui/page-header";
import { getTeacherRosterRows } from "@/features/teacher/teacherMappers";
import { teacherApi } from "@/lib/api/teacher";
import type { ClassSectionResponse } from "@/lib/api/types";

export const Route = createFileRoute("/teacher/classes/$classSectionId/students")({
  component: TeacherClassStudentsPage,
});

function TeacherClassStudentsPage() {
  const { classSectionId } = Route.useParams();
  const queryClient = useQueryClient();
  const cachedClass = useMemo(() => {
    const classQueries = queryClient.getQueriesData<ClassSectionResponse[]>({
      queryKey: ["teacher", "classes"],
    });
    return classQueries
      .flatMap(([, data]) => data ?? [])
      .find((section) => String(section.id) === classSectionId);
  }, [classSectionId, queryClient]);
  const title = cachedClass
    ? `${cachedClass.courseName} - ${cachedClass.classCode}`
    : "Danh sách sinh viên";

  const rosterQuery = useQuery({
    queryKey: ["teacher", "classes", classSectionId, "students"],
    queryFn: () => teacherApi.listClassStudents(classSectionId),
    enabled: Boolean(classSectionId),
    refetchOnMount: "always",
    retry: false,
  });

  const rows = useMemo(() => {
    return getTeacherRosterRows(rosterQuery.isError ? undefined : rosterQuery.data);
  }, [rosterQuery.data, rosterQuery.isError]);

  return (
    <div className="space-y-5">
      <Button asChild variant="ghost" size="sm" className="-ml-2 gap-1">
        <Link to="/teacher/classes">
          <ChevronLeft className="h-4 w-4" />
          Quay lại
        </Link>
      </Button>

      <PageHeader
        title={title}
        description={
          rosterQuery.isError
            ? "Chưa tải được danh sách sinh viên từ backend"
            : "Danh sách sinh viên đã chốt trong lớp học phần"
        }
      />

      {rosterQuery.isLoading && (
        <div className="rounded-lg border bg-card p-6 text-sm text-muted-foreground">
          Đang tải danh sách sinh viên...
        </div>
      )}

      {rosterQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          {rosterQuery.error instanceof Error
            ? rosterQuery.error.message
            : "Không tải được danh sách sinh viên"}
        </div>
      )}

      <DataTable
        data={rows}
        rowKey={(row) => row.enrollmentId}
        pageSize={12}
        searchPlaceholder="Tìm mã sinh viên, họ tên, lớp, số điện thoại, email..."
        emptyMessage="Chưa có sinh viên nào trong lớp này"
        columns={[
          {
            key: "studentCode",
            header: "MSV",
            render: (row) => (
              <span className="font-mono text-xs font-semibold">{row.studentCode}</span>
            ),
          },
          {
            key: "fullName",
            header: "Họ tên",
            render: (row) => <span className="min-w-48 font-medium">{row.fullName}</span>,
          },
          {
            key: "className",
            header: "Lớp SH",
            render: (row) => <span className="text-sm">{row.className ?? "-"}</span>,
          },
          {
            key: "phone",
            header: "SĐT",
            render: (row) => <span className="font-mono text-xs">{row.phone ?? "-"}</span>,
          },
          {
            key: "email",
            header: "Email",
            render: (row) => (
              <span className="text-xs text-muted-foreground">{row.email ?? "-"}</span>
            ),
          },
          {
            key: "advisorName",
            header: "Cố vấn HT",
            render: (row) => <span className="text-sm">{row.advisorName ?? "-"}</span>,
          },
          {
            key: "majorName",
            header: "Ngành",
            render: (row) => <span className="text-sm">{row.majorName ?? "-"}</span>,
          },
          {
            key: "facultyName",
            header: "Khóa học",
            render: (row) => <span className="text-sm">{row.facultyName ?? "-"}</span>,
          },
        ]}
      />
    </div>
  );
}
