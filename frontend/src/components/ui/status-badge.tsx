import { cn } from "@/lib/utils";

type Variant = "success" | "warning" | "info" | "destructive" | "muted" | "primary";

const styles: Record<Variant, string> = {
  success: "bg-success/15 text-success border-success/30",
  warning: "bg-warning/20 text-warning-foreground border-warning/40",
  info: "bg-info/15 text-info border-info/30",
  destructive: "bg-destructive/15 text-destructive border-destructive/30",
  muted: "bg-muted text-muted-foreground border-border",
  primary: "bg-primary/10 text-primary border-primary/30",
};

const map: Record<string, Variant> = {
  ACTIVE: "success",
  OPEN: "success",
  AVAILABLE: "success",
  PAID: "success",
  SUCCESS: "success",
  GRADUATED: "info",
  PENDING: "warning",
  PROCESSING: "info",
  UPCOMING: "info",
  PARTIAL: "warning",
  UNPAID: "warning",
  CLOSED: "muted",
  INACTIVE: "muted",
  CANCELLED: "muted",
  FAILED: "destructive",
  OVERDUE: "destructive",
  SUSPENDED: "destructive",
  FULL: "destructive",
  MAINTENANCE: "destructive",
  ADMIN: "primary",
  TEACHER: "info",
  STUDENT: "success",
  LECTURE: "info",
  LAB: "primary",
  AUDITORIUM: "warning",
  OFFLINE: "muted",
  ONLINE: "info",
  PROJECT: "primary",
  "ĐANG HỌC": "info",
  "ĐANG DẠY": "success",
  "ĐÃ KẾT THÚC": "muted",
  "ĐỦ ĐIỀU KIỆN THI": "success",
  "HỌC LẠI": "warning",
  REPEAT_COURSE: "warning",
  "CẤM THI": "destructive",
  BANNED_FROM_EXAM: "destructive",
  "THI LẠI": "warning",
  RETAKE_EXAM: "warning",
  "QUA MÔN": "success",
  PASSED: "success",
};

export function StatusBadge({ value, variant }: { value: string; variant?: Variant }) {
  const cleanVal = value ? value.toUpperCase() : "";
  const v = variant ?? map[value] ?? map[cleanVal] ?? "muted";
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] font-medium uppercase tracking-wide",
        styles[v],
      )}
    >
      {value}
    </span>
  );
}
