import { useQuery } from "@tanstack/react-query";
import { AlertCircle, Users } from "lucide-react";
import { useMemo } from "react";
import { DataTable } from "@/components/data-table/DataTable";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Skeleton } from "@/components/ui/skeleton";
import { StatusBadge } from "@/components/ui/status-badge";
import { adminApi } from "@/lib/api/admin";
import { mapApiClassSectionStudent } from "./classSectionMappers";
import type { ClassSectionRow } from "./types";

interface ClassSectionStudentsDialogProps {
  open: boolean;
  section: ClassSectionRow | null;
  onOpenChange: (open: boolean) => void;
}

export function ClassSectionStudentsDialog({
  open,
  section,
  onOpenChange,
}: ClassSectionStudentsDialogProps) {
  const canLoadApi = open && !!section?.numericId;
  const studentsQuery = useQuery({
    queryKey: ["admin", "class-sections", section?.numericId, "students"],
    queryFn: () => adminApi.listClassSectionStudents(section?.numericId ?? 0),
    enabled: canLoadApi,
    retry: false,
  });

  const rows = useMemo(() => {
    if (!section) return [];
    return (studentsQuery.data ?? []).map(mapApiClassSectionStudent);
  }, [section, studentsQuery.data]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[88vh] max-w-5xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Users className="h-5 w-5" />
            Sinh viên đăng ký lớp {section?.classCode ?? ""}
          </DialogTitle>
          <DialogDescription>
            {section
              ? `${section.courseName} - ${section.currentSlots}/${section.maxSlots} sinh viên`
              : "Danh sách sinh viên đăng ký lớp học phần"}
          </DialogDescription>
        </DialogHeader>

        {studentsQuery.isPending && canLoadApi ? (
          <StudentsSkeleton />
        ) : (
          <div className="space-y-3">
            {studentsQuery.isError && (
              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertTitle>Chưa tải được danh sách sinh viên</AlertTitle>
                <AlertDescription>{studentsQuery.error.message}</AlertDescription>
              </Alert>
            )}

            <DataTable
              data={rows}
              rowKey={(student) => student.enrollmentId}
              pageSize={8}
              searchPlaceholder="Tìm theo mã SV, họ tên, email, ngành..."
              emptyMessage="Chưa có sinh viên đăng ký lớp này"
              columns={[
                {
                  key: "studentCode",
                  header: "Mã SV",
                  render: (student) => (
                    <span className="font-mono text-xs font-medium">{student.studentCode}</span>
                  ),
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
                    <span className="text-xs text-muted-foreground">{student.email}</span>
                  ),
                },
                {
                  key: "majorName",
                  header: "Ngành",
                  render: (student) => (
                    <div>
                      <div className="text-sm">{student.majorName}</div>
                      <div className="text-xs text-muted-foreground">{student.cohort}</div>
                    </div>
                  ),
                },
                {
                  key: "enrolledAt",
                  header: "Ngày đăng ký",
                  render: (student) => (
                    <span className="text-xs text-muted-foreground">
                      {formatDateTime(student.enrolledAt)}
                    </span>
                  ),
                },
                {
                  key: "status",
                  header: "Trạng thái",
                  render: (student) => <StatusBadge value={student.status} />,
                },
              ]}
            />
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}

function StudentsSkeleton() {
  return (
    <div className="space-y-3">
      <Skeleton className="h-10 w-full" />
      <Skeleton className="h-64 w-full" />
    </div>
  );
}

function formatDateTime(value?: string | null) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
}
