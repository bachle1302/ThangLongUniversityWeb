import { Ban, Pencil, Trash2, Users } from "lucide-react";
import { DataTable } from "@/components/data-table/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { StatusBadge } from "@/components/ui/status-badge";
import { formatClassDay } from "./classSectionMappers";
import type { ClassSectionRow } from "./types";

interface ClassSectionsTableProps {
  rows: ClassSectionRow[];
  title?: string;
  onEdit: (row: ClassSectionRow) => void;
  onDelete: (row: ClassSectionRow) => void;
  onViewStudents: (row: ClassSectionRow) => void;
}

export function ClassSectionsTable({
  rows,
  title,
  onEdit,
  onDelete,
  onViewStudents,
}: ClassSectionsTableProps) {
  return (
    <div className="space-y-3">
      {title && (
        <div className="flex items-center justify-between">
          <h2 className="text-base font-semibold">{title}</h2>
          <Badge variant="outline">{rows.length} lớp</Badge>
        </div>
      )}
      <DataTable
        data={rows}
        rowKey={(section) => section.id}
        pageSize={10}
        searchPlaceholder="Tìm theo mã lớp, môn học, ngành, học kỳ, giảng viên..."
        emptyMessage="Chưa có lớp học phần"
        columns={[
          {
            key: "classCode",
            header: "Mã lớp",
            render: (section) => (
              <div className="space-y-1">
                <span className="font-mono text-xs font-medium">{section.classCode}</span>
                <Badge variant={section.source === "API" ? "secondary" : "outline"}>
                  {section.source}
                </Badge>
              </div>
            ),
          },
          {
            key: "courseName",
            header: "Môn học",
            render: (section) => <span className="font-medium">{section.courseName}</span>,
          },
          {
            key: "majorName",
            header: "Ngành",
            render: (section) => (
              <span className="text-xs text-muted-foreground">{section.majorName}</span>
            ),
          },
          {
            key: "semesterName",
            header: "Học kỳ",
            render: (section) => (
              <span className="text-xs text-muted-foreground">{section.semesterName}</span>
            ),
          },
          {
            key: "teacherName",
            header: "Giảng viên",
            render: (section) => <span className="text-sm">{section.teacherName}</span>,
          },
          {
            key: "roomName",
            header: "Phòng",
            render: (section) => <span className="text-sm">{section.roomName}</span>,
          },
          {
            key: "schedule",
            header: "Lịch học",
            accessor: (section) =>
              `${formatClassDay(section.dayOfWeek)} T${section.startPeriod}-${section.endPeriod}`,
            render: (section) => (
              <div className="text-xs text-muted-foreground">
                <div>{formatClassDay(section.dayOfWeek)}</div>
                <div>
                  Tiết {section.startPeriod}-{section.endPeriod}
                </div>
              </div>
            ),
          },
          {
            key: "size",
            header: "Sĩ số",
            accessor: (section) => `${section.currentSlots}/${section.maxSlots}`,
            render: (section) => (
              <span className="tabular-nums">
                {section.currentSlots}/{section.maxSlots}
              </span>
            ),
          },
          {
            key: "status",
            header: "Trạng thái",
            render: (section) => <StatusBadge value={section.status} />,
          },
          {
            key: "actions",
            header: "",
            className: "w-36 text-right",
            searchable: false,
            render: (section) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  aria-label={`Xem sinh viên ${section.classCode}`}
                  onClick={() => onViewStudents(section)}
                >
                  <Users className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  aria-label={`Sửa ${section.classCode}`}
                  onClick={() => onEdit(section)}
                >
                  <Pencil className="h-4 w-4" />
                </Button>
                {section.status !== "CANCELLED" && (
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8 text-destructive"
                    aria-label={
                      section.currentSlots > 0
                        ? `Hủy ${section.classCode}`
                        : `Xóa ${section.classCode}`
                    }
                    onClick={() => onDelete(section)}
                  >
                    {section.currentSlots > 0 ? (
                      <Ban className="h-4 w-4" />
                    ) : (
                      <Trash2 className="h-4 w-4" />
                    )}
                  </Button>
                )}
              </div>
            ),
          },
        ]}
      />
    </div>
  );
}
