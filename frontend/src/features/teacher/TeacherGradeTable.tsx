import { useRef, useState } from "react";
import { toast } from "sonner";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { TeacherGradeRow } from "./teacherMappers";

const COURSE_STATUS_LABELS: Record<string, { label: string; className: string }> = {
  IN_PROGRESS: { label: "Đang học", className: "bg-muted text-muted-foreground" },
  PASSED: { label: "Đạt", className: "bg-green-100 text-green-700" },
  BANNED_FROM_EXAM: { label: "Cấm thi", className: "bg-destructive/15 text-destructive" },
  REPEAT_COURSE: { label: "Học lại", className: "bg-destructive/15 text-destructive" },
  RETAKE_EXAM: { label: "Thi lại", className: "bg-amber-100 text-amber-700" },
};

interface TeacherGradeTableProps {
  rows: TeacherGradeRow[];
  disabled?: boolean;
  retakeOnly?: boolean;
  onChange: (row: TeacherGradeRow) => void;
  onSave?: (row: TeacherGradeRow) => void;
}

export function TeacherGradeTable({ rows, disabled, retakeOnly, onChange, onSave }: TeacherGradeTableProps) {
  const visibleRows = retakeOnly
    ? rows.filter((row) => row.enrollmentType === "RETAKE" || row.enrollmentType === "IMPROVE")
    : rows;

  return (
    <div className="overflow-x-auto rounded-xl border bg-card shadow-sm">
      <Table>
        <TableHeader>
          <TableRow className="bg-muted/40 hover:bg-muted/40">
            <TableHead>Sinh viên</TableHead>
            <TableHead className="w-28">Chuyên cần</TableHead>
            <TableHead className="w-28">Giữa kỳ</TableHead>
            <TableHead className="w-28">Cuối kỳ</TableHead>
            <TableHead className="w-28">Điểm thi lại</TableHead>
            <TableHead>Tổng</TableHead>
            <TableHead>Chữ</TableHead>
            <TableHead>GPA4</TableHead>
            <TableHead>Trạng thái</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {visibleRows.length === 0 ? (
            <TableRow>
              <TableCell colSpan={9} className="py-12 text-center text-sm text-muted-foreground">
                {retakeOnly ? "Không có sinh viên thi lại/nâng điểm trong lớp này" : "Chưa có sinh viên trong bảng điểm lớp này"}
              </TableCell>
            </TableRow>
          ) : (
            visibleRows.map((row) => (
              <GradeRow
                key={row.enrollmentId}
                row={row}
                disabled={disabled ?? !row.canEdit}
                onChange={onChange}
                onSave={onSave}
              />
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
}

function GradeRow({
  row,
  disabled,
  onChange,
  onSave,
}: {
  row: TeacherGradeRow;
  disabled: boolean;
  onChange: (row: TeacherGradeRow) => void;
  onSave?: (row: TeacherGradeRow) => void;
}) {
  // Track the latest computed row so blur handler can save the correct value
  const pendingRow = useRef<TeacherGradeRow | null>(null);

  const updateScore = (
    key: "participationScore" | "midtermScore" | "finalScore" | "retestScore",
    rawValue: string,
  ) => {
    if (rawValue.trim() === "") {
      const next = { ...row, [key]: null };
      onChange(next);
      pendingRow.current = next;
      return;
    }
    const value = Number(rawValue);
    if (Number.isNaN(value) || value < 0 || value > 10) {
      toast.error("Điểm phải trong khoảng 0-10");
      return;
    }
    const next = { ...row, [key]: value };
    onChange(next);
    pendingRow.current = next;
  };

  const handleBlur = () => {
    if (pendingRow.current) {
      onSave?.(pendingRow.current);
      pendingRow.current = null;
    }
  };

  const isBanned =
    row.courseStatus === "BANNED_FROM_EXAM" || row.courseStatus === "REPEAT_COURSE";

  const isRetake = row.enrollmentType === "RETAKE" || row.enrollmentType === "IMPROVE";

  return (
    <TableRow className={isBanned ? "bg-destructive/5" : undefined}>
      <TableCell>
        <div className="min-w-52">
          <div className="font-medium">{row.studentName}</div>
          <div className="text-xs text-muted-foreground flex items-center gap-1.5">
            <span className="font-mono">{row.studentCode}</span>
            {isRetake && (
              <span className="rounded bg-amber-100 px-1.5 py-0.5 text-[10px] font-bold text-amber-800">
                {row.enrollmentType === "RETAKE" ? "Thi lại" : "Thi nâng điểm"}
              </span>
            )}
            {isRetake && row.examAttemptNumber != null && (
              <span className="text-[10px] text-muted-foreground">Lần {row.examAttemptNumber}</span>
            )}
          </div>
          {isRetake && (
            <div className="mt-1 text-[11px] text-muted-foreground tabular-nums">
              CC {row.participationScore ?? "-"} · GK {row.midtermScore ?? "-"} · CK {row.finalScore ?? "-"}
            </div>
          )}
        </div>
      </TableCell>
      <ScoreInput
        value={row.participationScore}
        disabled={disabled || isRetake}
        onChange={(value) => updateScore("participationScore", value)}
        onBlur={handleBlur}
      />
      <ScoreInput
        value={row.midtermScore}
        disabled={disabled || isRetake}
        onChange={(value) => updateScore("midtermScore", value)}
        onBlur={handleBlur}
      />
      <ScoreInput
        value={row.finalScore}
        disabled={disabled || isRetake}
        onChange={(value) => updateScore("finalScore", value)}
        onBlur={handleBlur}
      />
      <ScoreInput
        value={row.retestScore}
        disabled={disabled || !isRetake}
        onChange={(value) => updateScore("retestScore", value)}
        onBlur={handleBlur}
      />
      <TableCell className="font-semibold tabular-nums">
        {row.totalScore != null ? row.totalScore.toFixed(2) : "-"}
      </TableCell>
      <TableCell>
        <span className="rounded bg-primary/10 px-2 py-0.5 text-xs font-bold text-primary">
          {row.letterGrade ?? "-"}
        </span>
      </TableCell>
      <TableCell className="tabular-nums">{row.gpa4 != null ? row.gpa4.toFixed(1) : "-"}</TableCell>
      <TableCell>
        {row.courseStatus && COURSE_STATUS_LABELS[row.courseStatus] ? (
          <span
            className={`rounded px-2 py-0.5 text-xs font-semibold ${COURSE_STATUS_LABELS[row.courseStatus].className}`}
          >
            {COURSE_STATUS_LABELS[row.courseStatus].label}
          </span>
        ) : (
          <span className="text-xs text-muted-foreground">-</span>
        )}
      </TableCell>
    </TableRow>
  );
}

function ScoreInput({
  value,
  disabled,
  onChange,
  onBlur,
}: {
  value: number | null;
  disabled: boolean;
  onChange: (value: string) => void;
  onBlur?: () => void;
}) {
  const [local, setLocal] = useState(value == null ? "" : String(value));

  // Sync local state when the parent value changes (e.g. after save/reset)
  const prevValue = useRef(value);
  if (prevValue.current !== value) {
    prevValue.current = value;
    const next = value == null ? "" : String(value);
    if (local !== next) setLocal(next);
  }

  return (
    <TableCell>
      <Input
        type="number"
        min={0}
        max={10}
        step={0.1}
        value={local}
        disabled={disabled}
        onChange={(e) => setLocal(e.target.value)}
        onBlur={() => {
          onChange(local);
          onBlur?.();
        }}
        className="h-8 w-20 tabular-nums"
      />
    </TableCell>
  );
}
