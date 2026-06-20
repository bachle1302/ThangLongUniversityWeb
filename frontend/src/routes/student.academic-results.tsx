import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { PageHeader } from "@/components/ui/page-header";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { studentApi } from "@/lib/api/student";
import { pickCurrentSemester } from "@/lib/semester";
import type { StudentSemesterResponse } from "@/lib/api/types";

export const Route = createFileRoute("/student/academic-results")({
  component: AcademicResultsPage,
});

const emptySemesters: StudentSemesterResponse[] = [];

function classify(cpa: number) {
  if (cpa >= 3.6) return { label: "Xuất sắc", color: "text-green-600" };
  if (cpa >= 3.2) return { label: "Giỏi", color: "text-blue-600" };
  if (cpa >= 2.5) return { label: "Khá", color: "text-yellow-600" };
  if (cpa >= 2.0) return { label: "Trung bình", color: "text-orange-500" };
  return { label: "Yếu", color: "text-destructive" };
}

function AcademicResultsPage() {
  const semestersQuery = useQuery({
    queryKey: ["student", "semesters"],
    queryFn: studentApi.listSemesters,
  });
  const semesters = semestersQuery.data ?? emptySemesters;
  const [semesterId, setSemesterId] = useState<number | null>(null);
  const [viewAll, setViewAll] = useState(false);

  useEffect(() => {
    if (!semesterId && semesters.length) setSemesterId(pickCurrentSemester(semesters)?.id ?? null);
  }, [semesterId, semesters]);

  const resultsQuery = useQuery({
    queryKey: ["student", "learning-results", viewAll ? null : semesterId],
    queryFn: () => studentApi.getLearningResults(viewAll ? null : semesterId),
    enabled: viewAll || semesterId != null,
  });

  const data = resultsQuery.data;
  const grades = data?.grades ?? [];
  const summaries = data?.semesterSummaries ?? [];
  const cpa = data?.cumulativeGpa ?? 0;
  const classification = classify(cpa);

  return (
    <div>
      <PageHeader
        title="Kết quả học tập"
        description="Bảng điểm và GPA/CPA tích lũy"
        actions={
          <div className="flex items-center gap-2">
            <button
              onClick={() => setViewAll((v) => !v)}
              className={`h-9 rounded-md border px-3 text-sm transition-colors ${viewAll ? "bg-primary text-primary-foreground" : "bg-background"}`}
            >
              {viewAll ? "Tất cả kỳ" : "Một kỳ"}
            </button>
            {!viewAll && (
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
            )}
          </div>
        }
      />

      <div className="mb-4 grid grid-cols-2 gap-3 md:grid-cols-4">
        <div className="rounded-xl border bg-card p-4">
          <div className="text-xs text-muted-foreground">CPA tích lũy</div>
          <div className="mt-1 text-2xl font-semibold text-primary tabular-nums">
            {cpa.toFixed(2)}
          </div>
        </div>
        {!viewAll && (
          <div className="rounded-xl border bg-card p-4">
            <div className="text-xs text-muted-foreground">GPA học kỳ</div>
            <div className="mt-1 text-2xl font-semibold tabular-nums">
              {(data?.semesterGpa ?? 0).toFixed(2)}
            </div>
          </div>
        )}
        <div className="rounded-xl border bg-card p-4">
          <div className="text-xs text-muted-foreground">Tín chỉ tích lũy</div>
          <div className="mt-1 text-2xl font-semibold tabular-nums">
            {data?.cumulativeCredits ?? 0}
          </div>
        </div>
        <div className="rounded-xl border bg-card p-4">
          <div className="text-xs text-muted-foreground">Xếp loại</div>
          <div className={`mt-1 text-xl font-semibold ${classification.color}`}>
            {classification.label}
          </div>
        </div>
      </div>

      {summaries.length > 0 && (
        <div className="mb-4 rounded-xl border bg-card p-5 shadow-sm">
          <h2 className="mb-3 text-sm font-semibold">GPA theo học kỳ</h2>
          <div className="space-y-2">
            {summaries.map((s) => (
              <div key={s.semesterId}>
                <div className="flex justify-between text-sm">
                  <span className="font-medium">{s.semesterName}</span>
                  <div className="flex gap-4 tabular-nums">
                    <span className="text-muted-foreground">
                      GPA:{" "}
                      <span className="font-semibold text-foreground">
                        {s.semesterGpa != null ? s.semesterGpa.toFixed(2) : "-"}
                      </span>
                    </span>
                    <span className="text-muted-foreground">
                      CPA:{" "}
                      <span className="font-semibold text-primary">
                        {s.cumulativeGpa != null ? s.cumulativeGpa.toFixed(2) : "-"}
                      </span>
                    </span>
                  </div>
                </div>
                <div className="mt-1 h-2.5 overflow-hidden rounded-full bg-muted">
                  <div
                    className="h-full rounded-full bg-primary"
                    style={{ width: `${Math.min(((s.semesterGpa ?? 0) / 4) * 100, 100)}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="rounded-xl border bg-card shadow-sm">
        <div className="border-b px-4 py-3">
          <h2 className="text-sm font-semibold">Bảng điểm chi tiết</h2>
        </div>
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/40">
              <TableHead>Môn học</TableHead>
              <TableHead className="hidden md:table-cell">Học kỳ</TableHead>
              <TableHead className="text-center">TC</TableHead>
              <TableHead className="text-center hidden sm:table-cell">Chuyên cần</TableHead>
              <TableHead className="text-center hidden sm:table-cell">Giữa kỳ</TableHead>
              <TableHead className="text-center hidden sm:table-cell">Cuối kỳ</TableHead>
              <TableHead className="text-center hidden sm:table-cell">Thi lại/nâng</TableHead>
              <TableHead className="text-center">Tổng</TableHead>
              <TableHead className="text-center">Chữ</TableHead>
              <TableHead className="text-center">GPA4</TableHead>
              <TableHead className="text-center">Trạng thái</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {resultsQuery.isLoading ? (
              <TableRow>
                <TableCell colSpan={11} className="py-10 text-center text-sm text-muted-foreground">
                  Đang tải bảng điểm...
                </TableCell>
              </TableRow>
            ) : grades.length === 0 ? (
              <TableRow>
                <TableCell colSpan={11} className="py-10 text-center text-sm text-muted-foreground">
                  Chưa có điểm.
                </TableCell>
              </TableRow>
            ) : (
              grades.map((r) => {
                const isFailed = r.totalScore != null && r.totalScore < 4;
                const isRetakeRow =
                  r.examRegistrationId != null ||
                  r.enrollmentType === "RETAKE" ||
                  r.enrollmentType === "IMPROVE";
                return (
                  <TableRow key={r.examRegistrationId ? `exam-${r.examRegistrationId}` : r.enrollmentId}>
                    <TableCell>
                      <div className="font-medium">{r.courseName}</div>
                      <div className="text-xs text-muted-foreground font-mono">{r.classCode}</div>
                      {r.studySemesterName && r.examRegistrationId != null && (
                        <div className="text-[11px] text-muted-foreground">
                          Học gốc: {r.studySemesterName}
                        </div>
                      )}
                    </TableCell>
                    <TableCell className="hidden md:table-cell text-sm text-muted-foreground">
                      {r.semesterName}
                    </TableCell>
                    <TableCell className="text-center tabular-nums">{r.credits}</TableCell>
                    <TableCell className="text-center tabular-nums hidden sm:table-cell">
                      {r.participationScore != null ? r.participationScore.toFixed(1) : "-"}
                    </TableCell>
                    <TableCell className="text-center tabular-nums hidden sm:table-cell">
                      {r.midtermScore != null ? r.midtermScore.toFixed(1) : "-"}
                    </TableCell>
                    <TableCell className="text-center tabular-nums hidden sm:table-cell">
                      {r.finalScore != null ? r.finalScore.toFixed(1) : "-"}
                    </TableCell>
                    <TableCell className="text-center tabular-nums hidden sm:table-cell">
                      {r.retestScore != null ? (
                        <span className="font-semibold text-amber-700">{r.retestScore.toFixed(1)}</span>
                      ) : isRetakeRow ? (
                        <span className="text-xs text-muted-foreground">Chờ chấm</span>
                      ) : (
                        "-"
                      )}
                    </TableCell>
                    <TableCell className="text-center">
                      <span
                        className={`font-bold tabular-nums ${isFailed ? "text-destructive" : ""}`}
                      >
                        {r.totalScore != null ? r.totalScore.toFixed(1) : "-"}
                      </span>
                    </TableCell>
                    <TableCell className="text-center">
                      <span
                        className={`rounded px-2 py-0.5 text-xs font-bold ${isFailed ? "bg-destructive/10 text-destructive" : "bg-primary/10 text-primary"}`}
                      >
                        {r.letterGrade ?? "-"}
                      </span>
                    </TableCell>
                    <TableCell className="text-center tabular-nums">
                      {r.gradePoint != null ? r.gradePoint.toFixed(2) : "-"}
                    </TableCell>
                    <TableCell className="text-center">
                      {r.enrollmentType === "RETAKE" && (
                        <span className="rounded px-2 py-0.5 text-xs font-semibold bg-amber-100 text-amber-700">
                          Thi lại
                        </span>
                      )}
                      {r.enrollmentType === "IMPROVE" && (
                        <span className="rounded px-2 py-0.5 text-xs font-semibold bg-blue-100 text-blue-700">
                          Thi nâng
                        </span>
                      )}
                      {r.enrollmentType !== "RETAKE" && r.enrollmentType !== "IMPROVE" && (
                        <>
                          {r.courseStatus === "BANNED_FROM_EXAM" && (
                            <span className="rounded px-2 py-0.5 text-xs font-semibold bg-destructive/15 text-destructive">Cấm thi</span>
                          )}
                          {r.courseStatus === "REPEAT_COURSE" && (
                            <span className="rounded px-2 py-0.5 text-xs font-semibold bg-destructive/15 text-destructive">Học lại</span>
                          )}
                          {r.courseStatus === "RETAKE_EXAM" && (
                            <span className="rounded px-2 py-0.5 text-xs font-semibold bg-amber-100 text-amber-700">Thi lại</span>
                          )}
                          {r.courseStatus === "PASSED" && (
                            <span className="rounded px-2 py-0.5 text-xs font-semibold bg-green-100 text-green-700">Đạt</span>
                          )}
                        </>
                      )}
                      {(!r.courseStatus || r.courseStatus === "IN_PROGRESS") && !isRetakeRow && (
                        <span className="text-xs text-muted-foreground">Đang học</span>
                      )}
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>

      {resultsQuery.isError && (
        <div className="mt-4 text-sm text-destructive">
          {resultsQuery.error instanceof Error
            ? resultsQuery.error.message
            : "Không tải được kết quả học tập"}
        </div>
      )}
    </div>
  );
}
