import { useQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { DataTable } from "@/components/data-table/DataTable";
import { PageHeader } from "@/components/ui/page-header";
import { adminApi } from "@/lib/api/admin";

export const Route = createFileRoute("/admin/academic-results")({ component: AcademicResults });

function AcademicResults() {
  const studentsQuery = useQuery({
    queryKey: ["admin", "students"],
    queryFn: adminApi.listStudents,
  });

  return (
    <div>
      <PageHeader
        title="Kết quả học tập"
        description="Danh sách sinh viên từ dữ liệu thật. Chọn sinh viên ở các màn hình điểm chi tiết để xem kết quả."
      />

      <DataTable
        data={studentsQuery.data ?? []}
        rowKey={(student) => String(student.id)}
        searchPlaceholder="Tìm theo mã sinh viên, họ tên, email..."
        emptyMessage={
          studentsQuery.isError ? "Không tải được dữ liệu sinh viên" : "Chưa có dữ liệu sinh viên"
        }
        columns={[
          {
            key: "studentCode",
            header: "Mã SV",
            render: (student) => <span className="font-mono text-xs">{student.studentCode}</span>,
          },
          {
            key: "fullName",
            header: "Họ tên",
            render: (student) => <span className="font-medium">{student.fullName}</span>,
          },
          {
            key: "email",
            header: "Email",
            render: (student) => (
              <span className="text-sm text-muted-foreground">{student.email}</span>
            ),
          },
          {
            key: "majorName",
            header: "Ngành",
            render: (student) => <span>{student.majorName ?? "-"}</span>,
          },
          {
            key: "gpa",
            header: "GPA",
            render: (student) => <span className="tabular-nums">{student.gpa ?? "-"}</span>,
          },
          {
            key: "cpa",
            header: "CPA",
            render: (student) => <span className="tabular-nums">{student.cpa ?? "-"}</span>,
          },
        ]}
      />
    </div>
  );
}
