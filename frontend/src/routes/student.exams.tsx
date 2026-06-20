import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { PageHeader } from "@/components/ui/page-header";
import { DataTable } from "@/components/data-table/DataTable";
import { studentApi } from "@/lib/api/student";
import { pickCurrentSemester } from "@/lib/semester";
import type { StudentSemesterResponse } from "@/lib/api/types";

export const Route = createFileRoute("/student/exams")({ component: ExamsPage });

const emptySemesters: StudentSemesterResponse[] = [];

function formatDateTime(value?: string | null) {
  if (!value) return "-";
  return new Intl.DateTimeFormat("vi-VN", { dateStyle: "medium", timeStyle: "short" }).format(
    new Date(value),
  );
}

const EXAM_SOURCE_LABELS: Record<string, string> = {
  NORMAL: "Thi lần đầu",
  RETAKE: "Thi lại",
  IMPROVE: "Thi nâng điểm",
};

function ExamsPage() {
  const semestersQuery = useQuery({
    queryKey: ["student", "semesters"],
    queryFn: studentApi.listSemesters,
  });
  const semesters = semestersQuery.data ?? emptySemesters;
  const [semesterId, setSemesterId] = useState<number | null>(null);

  useEffect(() => {
    if (!semesterId && semesters.length) setSemesterId(pickCurrentSemester(semesters)?.id ?? null);
  }, [semesterId, semesters]);

  const currentSemester = semesters.find((s) => s.id === semesterId);

  const examsQuery = useQuery({
    queryKey: ["student", "exams", semesterId],
    queryFn: () => studentApi.getExams(semesterId as number),
    enabled: semesterId != null && !!currentSemester?.examPublished,
  });

  const exams = examsQuery.data ?? [];

  if (currentSemester && !currentSemester.examPublished) {
    return (
      <div className="p-6">
        <PageHeader title="Lịch thi" description="Lịch thi chưa được công bố" />
        <div className="mt-4 rounded-lg border bg-muted/30 p-8 text-center text-muted-foreground">
          <p className="text-lg">⏳ Lịch thi chưa được công bố</p>
          <p className="text-sm mt-1">Vui lòng quay lại sau khi nhà trường công bố lịch thi.</p>
        </div>
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title="Lịch thi"
        description={`${exams.length} kỳ thi`}
        actions={
          <select
            className="h-9 rounded-md border bg-background px-3 text-sm"
            value={semesterId ?? ""}
            onChange={(e) => setSemesterId(Number(e.target.value))}
          >
            {semesters.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        }
      />
      <DataTable
        data={exams}
        rowKey={(e) => `${e.classCode}-${e.examAt ?? ""}`}
        emptyMessage={examsQuery.isLoading ? "Đang tải dữ liệu..." : "Chưa có lịch thi"}
        columns={[
          {
            key: "course",
            header: "Môn học",
            accessor: (e) => e.courseName,
            render: (e) => <span className="font-medium">{e.courseName}</span>,
          },
          {
            key: "examType",
            header: "Loại thi",
            render: (e) => (
              <span className="text-xs">
                {EXAM_SOURCE_LABELS[e.examSourceType ?? "NORMAL"] ?? e.examSourceType ?? "-"}
              </span>
            ),
          },
          {
            key: "classCode",
            header: "Lớp",
            render: (e) => <span className="font-mono text-xs">{e.classCode}</span>,
          },
          {
            key: "examAt",
            header: "Thời gian",
            render: (e) => <span className="tabular-nums">{formatDateTime(e.examAt)}</span>,
          },
          {
            key: "room",
            header: "Phòng",
            render: (e) => <span className="font-mono">{e.examRoom ?? "-"}</span>,
          },
        ]}
      />
      {examsQuery.isError && (
        <div className="mt-4 text-sm text-destructive">
          {examsQuery.error instanceof Error ? examsQuery.error.message : "Không tải được lịch thi"}
        </div>
      )}
    </div>
  );
}
