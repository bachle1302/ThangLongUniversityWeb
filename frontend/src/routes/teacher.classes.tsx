import { useQuery } from "@tanstack/react-query";
import { createFileRoute, Outlet, useNavigate, useRouterState } from "@tanstack/react-router";
import { useMemo } from "react";
import { DataTable } from "@/components/data-table/DataTable";
import { Button } from "@/components/ui/button";
import { PageHeader } from "@/components/ui/page-header";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StatusBadge } from "@/components/ui/status-badge";
import { getTeacherClassRows } from "@/features/teacher/teacherMappers";
import {
  useTeacherSemester,
  type TeacherSemesterOption,
} from "@/features/teacher/useTeacherSemester";
import { teacherApi } from "@/lib/api/teacher";

export const Route = createFileRoute("/teacher/classes")({ component: TeacherClassesRouteShell });

function TeacherClassesRouteShell() {
  const pathname = useRouterState({ select: (state) => state.location.pathname });
  if (pathname !== "/teacher/classes") return <Outlet />;
  return <TeacherClassesPage />;
}

function TeacherClassesPage() {
  const { semesterId, setSemesterId, semesterOptions } = useTeacherSemester();
  const navigate = useNavigate();

  const classesQuery = useQuery({
    queryKey: ["teacher", "classes", semesterId],
    queryFn: () => teacherApi.listMyClasses(semesterId),
    enabled: Boolean(semesterId),
    retry: false,
  });

  const rows = useMemo(
    () => getTeacherClassRows(classesQuery.isError ? undefined : classesQuery.data),
    [classesQuery.data, classesQuery.isError],
  );

  return (
    <div className="space-y-5">
      <PageHeader
        title="Lớp học phần đang dạy"
        description={
          classesQuery.isError
            ? "Chưa tải được dữ liệu lớp học từ backend"
            : "Danh sách lớp học phần được phân công theo học kỳ"
        }
      />

      {classesQuery.isLoading && (
        <div className="rounded-lg border bg-card p-6 text-sm text-muted-foreground">
          Đang tải danh sách lớp học phần...
        </div>
      )}

      {classesQuery.isError && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
          {classesQuery.error instanceof Error
            ? classesQuery.error.message
            : "Không tải được danh sách lớp học phần"}
        </div>
      )}

      <DataTable
        data={rows}
        rowKey={(row) => row.id}
        toolbar={
          <SemesterFilter
            value={semesterId}
            options={semesterOptions}
            onValueChange={setSemesterId}
          />
        }
        pageSize={10}
        searchPlaceholder="Tìm mã lớp, môn học, phòng..."
        emptyMessage="Chưa có lớp học phần nào trong học kỳ này"
        columns={[
          {
            key: "classCode",
            header: "Mã lớp",
            render: (row) => (
              <div>
                <span className="font-mono text-xs font-semibold">{row.classCode}</span>
              </div>
            ),
          },
          {
            key: "courseName",
            header: "Môn học",
            render: (row) => (
              <div className="min-w-56">
                <div className="font-medium">{row.courseName}</div>
                <div className="text-xs text-muted-foreground">
                  {row.courseCode} - {row.credits} tín chỉ
                </div>
              </div>
            ),
          },
          {
            key: "scheduleRoomItems",
            header: "Lịch học / Phòng",
            accessor: (row) => row.scheduleRoomItems.join(" "),
            render: (row) => (
              <div className="min-w-72 space-y-1">
                {row.scheduleRoomItems.map((item, index) => (
                  <div key={`${row.id}-${index}-${item}`} className="text-xs text-muted-foreground">
                    {item}
                  </div>
                ))}
              </div>
            ),
          },
          {
            key: "size",
            header: "Sĩ số",
            accessor: (row) => `${row.currentSlots}/${row.maxSlots}`,
            render: (row) => (
              <span className="tabular-nums">
                {row.currentSlots ?? "-"}/{row.maxSlots ?? "-"}
              </span>
            ),
          },
          {
            key: "status",
            header: "Lớp",
            render: (row) => <StatusBadge value={row.status} />,
          },
          {
            key: "actions",
            header: "",
            className: "w-28 text-right",
            searchable: false,
            render: (row) => (
              <Button
                variant="outline"
                size="sm"
                onClick={() =>
                  navigate({
                    to: "/teacher/classes/$classSectionId/students",
                    params: { classSectionId: row.id },
                  })
                }
              >
                Xem sinh viên
              </Button>
            ),
          },
        ]}
      />
    </div>
  );
}

function SemesterFilter({
  value,
  options,
  onValueChange,
}: {
  value: string;
  options: TeacherSemesterOption[];
  onValueChange: (value: string) => void;
}) {
  return (
    <Select value={value} onValueChange={onValueChange}>
      <SelectTrigger className="w-[280px]">
        <SelectValue placeholder="Chọn học kỳ" />
      </SelectTrigger>
      <SelectContent>
        {options.map((semester) => (
          <SelectItem key={semester.id} value={semester.id}>
            {semester.name}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
