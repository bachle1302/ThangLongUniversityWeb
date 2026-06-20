import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useMemo, useState, type ReactNode } from "react";
import { DataTable } from "@/components/data-table/DataTable";
import { PageHeader } from "@/components/ui/page-header";
import { adminApi } from "@/lib/api/admin";
import type { AdminStudentResponse } from "@/lib/api/types";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { Check, Eye, Pencil, X } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/students")({ component: StudentsPage });

// ── Status mapping ─────────────────────────────────────────────────────────
const STATUS_LABELS: Record<string, { label: string; color: string }> = {
  "Dang hoc": { label: "Đang học", color: "bg-success/15 text-success border-success/30" },
  "Đang học": { label: "Đang học", color: "bg-success/15 text-success border-success/30" },
  "Tot nghiep": { label: "Tốt nghiệp", color: "bg-info/15 text-info border-info/30" },
  "Tốt nghiệp": { label: "Tốt nghiệp", color: "bg-info/15 text-info border-info/30" },
  "Dinh chi": {
    label: "Đình chỉ",
    color: "bg-destructive/15 text-destructive border-destructive/30",
  },
  "Đình chỉ": {
    label: "Đình chỉ",
    color: "bg-destructive/15 text-destructive border-destructive/30",
  },
  "Bao luu": { label: "Bảo lưu", color: "bg-warning/20 text-warning-foreground border-warning/40" },
  "Bảo lưu": { label: "Bảo lưu", color: "bg-warning/20 text-warning-foreground border-warning/40" },
  ACTIVE: { label: "Đang học", color: "bg-success/15 text-success border-success/30" },
  SUSPENDED: {
    label: "Đình chỉ",
    color: "bg-destructive/15 text-destructive border-destructive/30",
  },
  GRADUATED: { label: "Tốt nghiệp", color: "bg-info/15 text-info border-info/30" },
};

function StudentStatusBadge({ status }: { status?: string | null }) {
  if (!status) return <span className="text-xs text-muted-foreground">—</span>;
  const info = STATUS_LABELS[status];
  const label = info?.label ?? status;
  const color = info?.color ?? "bg-muted text-muted-foreground border-border";
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2 py-0.5 text-[11px] font-medium tracking-wide ${color}`}
    >
      {label}
    </span>
  );
}

// ── Types ──────────────────────────────────────────────────────────────────
type StudentRow = {
  id: string;
  numericId: number;
  raw: AdminStudentResponse;
  code: string;
  username: string;
  fullName: string;
  email: string;
  majorName: string;
  majorId: number | null;
  cohort: string;
  academicYearLabel: string;
  academicYearNum: number | null;
  dob: string;
  dobYear: string;
  address: string;
  status: string;
  gpa: number | null;
  cpa: number | null;
  totalCredits: number | null;
};

function mapApiStudent(s: AdminStudentResponse): StudentRow {
  const rawYear =
    typeof s.academicYear === "number"
      ? s.academicYear
      : typeof s.academicYear === "string" && s.academicYear.trim()
        ? parseInt(s.academicYear, 10) || null
        : null;
  const academicYearLabel = rawYear ? `${rawYear}–${rawYear + 4}` : "";
  const cohort = s.cohort ?? (rawYear ? `K${rawYear}` : "");
  const dobYear = s.dob ? s.dob.split("-")[0] : "";

  return {
    id: String(s.id),
    numericId: s.id,
    raw: s,
    code: s.studentCode,
    username: s.username,
    fullName: s.fullName,
    email: s.email,
    majorName: s.majorName ?? "",
    majorId: s.majorId ?? null,
    cohort,
    academicYearLabel,
    academicYearNum: rawYear,
    dob: s.dob ?? "",
    dobYear,
    address: s.address ?? "",
    status: s.status ?? "",
    gpa: s.gpa ?? null,
    cpa: s.cpa ?? null,
    totalCredits: s.totalCredits ?? null,
  };
}

// ── Detail sheet ────────────────────────────────────────────────────────────
function DetailRow({
  label,
  value,
  editing,
  editor,
  onEdit,
  onSave,
  onCancel,
  saving,
}: {
  label: string;
  value?: string | null;
  editing?: boolean;
  editor?: ReactNode;
  onEdit?: () => void;
  onSave?: () => void;
  onCancel?: () => void;
  saving?: boolean;
}) {
  return (
    <div className="grid grid-cols-[140px_1fr_auto] items-start gap-2 py-1.5 border-b border-border/40 last:border-0">
      <span className="text-xs text-muted-foreground">{label}</span>
      {editing ? <div>{editor}</div> : <span className="text-xs font-medium">{value || "—"}</span>}
      {onEdit && (
        <div className="flex items-center gap-1">
          {editing ? (
            <>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                className="h-6 w-6"
                onClick={onSave}
                disabled={saving}
              >
                <Check className="h-3.5 w-3.5" />
              </Button>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                className="h-6 w-6"
                onClick={onCancel}
                disabled={saving}
              >
                <X className="h-3.5 w-3.5" />
              </Button>
            </>
          ) : (
            <Button type="button" variant="ghost" size="icon" className="h-6 w-6" onClick={onEdit}>
              <Pencil className="h-3.5 w-3.5" />
            </Button>
          )}
        </div>
      )}
    </div>
  );
}

const STATUSES = ["Đang học", "Đình chỉ", "Tốt nghiệp", "Bảo lưu"];

type EditableField =
  | "dob"
  | "gender"
  | "phone"
  | "nationalId"
  | "placeOfBirth"
  | "hometown"
  | "permanentAddress"
  | "currentAddress"
  | "emergencyContact"
  | "majorId"
  | "academicYear"
  | "cohort"
  | "trainingType"
  | "status";

function StudentDetailSheet({
  student,
  open,
  onClose,
  majors,
}: {
  student: StudentRow | null;
  open: boolean;
  onClose: () => void;
  majors: { id: number; name: string }[];
}) {
  const queryClient = useQueryClient();
  const [editingField, setEditingField] = useState<EditableField | null>(null);
  const [draft, setDraft] = useState<Record<string, string>>({});

  useEffect(() => {
    if (!student) {
      setEditingField(null);
      setDraft({});
      return;
    }
    const s = student.raw;
    setDraft({
      dob: student.dob,
      gender: s.gender ?? "",
      phone: s.phone ?? "",
      nationalId: s.nationalId ?? "",
      placeOfBirth: s.placeOfBirth ?? "",
      hometown: s.hometown ?? "",
      permanentAddress: s.permanentAddress ?? "",
      currentAddress: s.currentAddress ?? "",
      emergencyContact: s.emergencyContact ?? "",
      majorId: student.majorId != null ? String(student.majorId) : "",
      academicYear: student.academicYearNum != null ? String(student.academicYearNum) : "",
      cohort: student.cohort,
      trainingType: s.trainingType ?? "",
      status: student.status,
    });
    setEditingField(null);
  }, [student]);

  const mutation = useMutation({
    mutationFn: async ({ payload }: { payload: Record<string, string | number | undefined> }) => {
      if (!student) throw new Error("Không tìm thấy sinh viên");
      return adminApi.updateStudent(student.numericId, payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "students"] });
      setEditingField(null);
      toast.success("Đã cập nhật sinh viên");
    },
    onError: (e) => toast.error(e.message),
  });

  if (!student) return null;

  const set = (key: string, value: string) => setDraft((prev) => ({ ...prev, [key]: value }));

  const buildPayload = (field: EditableField) => {
    switch (field) {
      case "dob":
        return { dob: draft.dob || undefined };
      case "gender":
        return { gender: draft.gender || undefined };
      case "phone":
        return { phone: draft.phone || undefined };
      case "nationalId":
        return { nationalId: draft.nationalId || undefined };
      case "placeOfBirth":
        return { placeOfBirth: draft.placeOfBirth || undefined };
      case "hometown":
        return { hometown: draft.hometown || undefined };
      case "permanentAddress":
        return { permanentAddress: draft.permanentAddress || undefined };
      case "currentAddress":
        return { currentAddress: draft.currentAddress || undefined };
      case "emergencyContact":
        return { emergencyContact: draft.emergencyContact || undefined };
      case "majorId":
        return { majorId: draft.majorId ? Number(draft.majorId) : undefined };
      case "academicYear":
        return { academicYear: draft.academicYear ? Number(draft.academicYear) : undefined };
      case "cohort":
        return { cohort: draft.cohort || undefined };
      case "trainingType":
        return { trainingType: draft.trainingType || undefined };
      case "status":
        return { status: draft.status || undefined };
      default:
        return {};
    }
  };

  const saveField = (field: EditableField) => {
    mutation.mutate({ payload: buildPayload(field) });
  };

  const rowActions = (field: EditableField) => ({
    editing: editingField === field,
    onEdit: () => setEditingField(field),
    onSave: () => saveField(field),
    onCancel: () => setEditingField(null),
    saving: mutation.isPending,
  });

  return (
    <Sheet open={open} onOpenChange={(v) => !v && onClose()}>
      <SheetContent side="right" className="w-full sm:max-w-lg overflow-y-auto">
        <SheetHeader className="mb-4">
          <SheetTitle className="text-base">{student.fullName}</SheetTitle>
          <p className="text-xs text-muted-foreground">
            {student.code} · {student.email}
          </p>
        </SheetHeader>

        <div className="space-y-4">
          <section>
            <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              Thông tin cá nhân
            </h3>
            <DetailRow
              label="Ngày sinh"
              value={draft.dob}
              editor={
                <Input
                  className="h-8 text-xs"
                  type="date"
                  value={draft.dob ?? ""}
                  onChange={(e) => set("dob", e.target.value)}
                />
              }
              {...rowActions("dob")}
            />
            <DetailRow
              label="Giới tính"
              value={draft.gender}
              editor={
                <Select value={draft.gender ?? ""} onValueChange={(v) => set("gender", v)}>
                  <SelectTrigger className="h-8 text-xs">
                    <SelectValue placeholder="Chọn..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Nam">Nam</SelectItem>
                    <SelectItem value="Nữ">Nữ</SelectItem>
                    <SelectItem value="Khác">Khác</SelectItem>
                  </SelectContent>
                </Select>
              }
              {...rowActions("gender")}
            />
            <DetailRow
              label="Số điện thoại"
              value={draft.phone}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.phone ?? ""}
                  onChange={(e) => set("phone", e.target.value)}
                />
              }
              {...rowActions("phone")}
            />
            <DetailRow
              label="Số CCCD/CMND"
              value={draft.nationalId}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.nationalId ?? ""}
                  onChange={(e) => set("nationalId", e.target.value)}
                />
              }
              {...rowActions("nationalId")}
            />
            <DetailRow
              label="Nơi sinh"
              value={draft.placeOfBirth}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.placeOfBirth ?? ""}
                  onChange={(e) => set("placeOfBirth", e.target.value)}
                />
              }
              {...rowActions("placeOfBirth")}
            />
            <DetailRow
              label="Quê quán"
              value={draft.hometown}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.hometown ?? ""}
                  onChange={(e) => set("hometown", e.target.value)}
                />
              }
              {...rowActions("hometown")}
            />
            <DetailRow
              label="Địa chỉ thường trú"
              value={draft.permanentAddress}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.permanentAddress ?? ""}
                  onChange={(e) => set("permanentAddress", e.target.value)}
                />
              }
              {...rowActions("permanentAddress")}
            />
            <DetailRow
              label="Nơi ở hiện tại"
              value={draft.currentAddress}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.currentAddress ?? ""}
                  onChange={(e) => set("currentAddress", e.target.value)}
                />
              }
              {...rowActions("currentAddress")}
            />
            <DetailRow
              label="Liên hệ khẩn cấp"
              value={draft.emergencyContact}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.emergencyContact ?? ""}
                  onChange={(e) => set("emergencyContact", e.target.value)}
                />
              }
              {...rowActions("emergencyContact")}
            />
          </section>

          <section>
            <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              Học vụ
            </h3>
            <DetailRow
              label="Ngành học"
              value={majors.find((m) => String(m.id) === draft.majorId)?.name ?? student.majorName}
              editor={
                <Select
                  value={draft.majorId || "__none"}
                  onValueChange={(v) => set("majorId", v === "__none" ? "" : v)}
                >
                  <SelectTrigger className="h-8 text-xs">
                    <SelectValue placeholder="Chọn ngành..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__none">Chưa chọn</SelectItem>
                    {majors.map((m) => (
                      <SelectItem key={m.id} value={String(m.id)}>
                        {m.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              }
              {...rowActions("majorId")}
            />
            <DetailRow
              label="Năm học"
              value={
                draft.academicYear ? `${draft.academicYear}–${Number(draft.academicYear) + 4}` : "—"
              }
              editor={
                <Input
                  className="h-8 text-xs"
                  type="number"
                  value={draft.academicYear ?? ""}
                  onChange={(e) => set("academicYear", e.target.value)}
                />
              }
              {...rowActions("academicYear")}
            />
            <DetailRow
              label="Khóa"
              value={draft.cohort}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.cohort ?? ""}
                  onChange={(e) => set("cohort", e.target.value)}
                />
              }
              {...rowActions("cohort")}
            />
            <DetailRow label="Lớp hành chính" value={student.raw.className ?? "Chưa gán"} />
            <DetailRow label="Cố vấn học tập" value={student.raw.advisorName ?? "—"} />
            <DetailRow
              label="Hệ đào tạo"
              value={draft.trainingType}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.trainingType ?? ""}
                  onChange={(e) => set("trainingType", e.target.value)}
                />
              }
              {...rowActions("trainingType")}
            />
            <DetailRow
              label="Trạng thái"
              value={STATUS_LABELS[draft.status]?.label ?? draft.status}
              editor={
                <Select value={draft.status ?? ""} onValueChange={(v) => set("status", v)}>
                  <SelectTrigger className="h-8 text-xs">
                    <SelectValue placeholder="Chọn trạng thái..." />
                  </SelectTrigger>
                  <SelectContent>
                    {STATUSES.map((status) => (
                      <SelectItem key={status} value={status}>
                        {status}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              }
              {...rowActions("status")}
            />
          </section>

          <section>
            <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              Kết quả học tập
            </h3>
            <DetailRow
              label="GPA học kỳ gần nhất"
              value={student.gpa != null ? student.gpa.toFixed(2) : null}
            />
            <DetailRow
              label="CPA tích lũy"
              value={student.cpa != null ? student.cpa.toFixed(2) : null}
            />
            <DetailRow
              label="Tổng tín chỉ tích lũy"
              value={student.totalCredits != null ? String(student.totalCredits) : null}
            />
          </section>
        </div>
      </SheetContent>
    </Sheet>
  );
}

// ── Main page ──────────────────────────────────────────────────────────────
function StudentsPage() {
  const query = useQuery({
    queryKey: ["admin", "students"],
    queryFn: adminApi.listStudents,
  });
  const majorsQuery = useQuery({
    queryKey: ["admin", "majors"],
    queryFn: adminApi.listMajors,
  });

  const [statusFilter, setStatusFilter] = useState("all");
  const [majorFilter, setMajorFilter] = useState("all");
  const [yearFilter, setYearFilter] = useState("all");
  const [dobYearFilter, setDobYearFilter] = useState("all");
  const [gpaMin, setGpaMin] = useState("");
  const [gpaMax, setGpaMax] = useState("");

  const [detailStudent, setDetailStudent] = useState<StudentRow | null>(null);

  const rows = useMemo<StudentRow[]>(
    () => (query.data?.length ? query.data.map(mapApiStudent) : []),
    [query.data],
  );

  const uniqueStatuses = useMemo(() => {
    const seen = new Map<string, string>();
    for (const r of rows)
      if (r.status && !seen.has(r.status))
        seen.set(r.status, STATUS_LABELS[r.status]?.label ?? r.status);
    return Array.from(seen.entries());
  }, [rows]);

  const uniqueMajors = useMemo(() => {
    const seen = new Set<string>();
    for (const r of rows) if (r.majorName) seen.add(r.majorName);
    return Array.from(seen).sort();
  }, [rows]);

  const uniqueYears = useMemo(() => {
    const seen = new Set<number>();
    for (const r of rows) if (r.academicYearNum) seen.add(r.academicYearNum);
    return Array.from(seen).sort((a, b) => b - a);
  }, [rows]);

  const uniqueDobYears = useMemo(() => {
    const seen = new Set<string>();
    for (const r of rows) if (r.dobYear) seen.add(r.dobYear);
    return Array.from(seen).sort().reverse();
  }, [rows]);

  const filteredRows = useMemo(
    () =>
      rows.filter((r) => {
        if (statusFilter !== "all" && r.status !== statusFilter) return false;
        if (majorFilter !== "all" && r.majorName !== majorFilter) return false;
        if (yearFilter !== "all" && String(r.academicYearNum) !== yearFilter) return false;
        if (dobYearFilter !== "all" && r.dobYear !== dobYearFilter) return false;
        const minVal = gpaMin !== "" ? parseFloat(gpaMin) : null;
        const maxVal = gpaMax !== "" ? parseFloat(gpaMax) : null;
        if (minVal !== null && (r.gpa === null || r.gpa < minVal)) return false;
        if (maxVal !== null && (r.gpa === null || r.gpa > maxVal)) return false;
        return true;
      }),
    [rows, statusFilter, majorFilter, yearFilter, dobYearFilter, gpaMin, gpaMax],
  );

  const majors = majorsQuery.data ?? [];

  const filterRow = (
    <>
      <div className="flex flex-col gap-1">
        <Label className="text-xs text-muted-foreground">Trạng thái</Label>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="h-8 w-32 text-xs">
            <SelectValue placeholder="Tất cả" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {uniqueStatuses.map(([raw, label]) => (
              <SelectItem key={raw} value={raw}>
                {label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-1">
        <Label className="text-xs text-muted-foreground">Ngành</Label>
        <Select value={majorFilter} onValueChange={setMajorFilter}>
          <SelectTrigger className="h-8 w-40 text-xs">
            <SelectValue placeholder="Tất cả ngành" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả ngành</SelectItem>
            {uniqueMajors.map((name) => (
              <SelectItem key={name} value={name}>
                {name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-1">
        <Label className="text-xs text-muted-foreground">Năm học</Label>
        <Select value={yearFilter} onValueChange={setYearFilter}>
          <SelectTrigger className="h-8 w-28 text-xs">
            <SelectValue placeholder="Tất cả" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {uniqueYears.map((y) => (
              <SelectItem key={y} value={String(y)}>
                {y}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-1">
        <Label className="text-xs text-muted-foreground">Năm sinh</Label>
        <Select value={dobYearFilter} onValueChange={setDobYearFilter}>
          <SelectTrigger className="h-8 w-24 text-xs">
            <SelectValue placeholder="Tất cả" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {uniqueDobYears.map((y) => (
              <SelectItem key={y} value={y}>
                {y}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-1">
        <Label className="text-xs text-muted-foreground">GPA từ – đến</Label>
        <div className="flex items-center gap-1">
          <Input
            className="h-8 w-20 text-xs"
            placeholder="0.0"
            value={gpaMin}
            onChange={(e) => setGpaMin(e.target.value)}
            type="number"
            min={0}
            max={4}
            step={0.1}
          />
          <span className="text-xs text-muted-foreground">–</span>
          <Input
            className="h-8 w-20 text-xs"
            placeholder="4.0"
            value={gpaMax}
            onChange={(e) => setGpaMax(e.target.value)}
            type="number"
            min={0}
            max={4}
            step={0.1}
          />
        </div>
      </div>
    </>
  );

  return (
    <div>
      <PageHeader title="Sinh viên" description={`${filteredRows.length} sinh viên`} />

      <DataTable
        data={filteredRows}
        rowKey={(s) => s.id}
        pageSize={10}
        searchPlaceholder="Tìm theo mã, tên, email, ngành..."
        filterRow={filterRow}
        columns={[
          {
            key: "code",
            header: "Mã SV",
            render: (s) => <span className="font-mono text-xs">{s.code}</span>,
          },
          {
            key: "fullName",
            header: "Họ tên",
            render: (s) => (
              <div className="min-w-40">
                <div className="font-medium text-sm">{s.fullName}</div>
                <div className="text-xs text-muted-foreground">@{s.username}</div>
              </div>
            ),
          },
          {
            key: "email",
            header: "Email",
            render: (s) => <span className="text-xs text-muted-foreground">{s.email}</span>,
          },
          {
            key: "majorName",
            header: "Ngành",
            render: (s) => <span className="text-sm">{s.majorName || "—"}</span>,
          },
          {
            key: "academicYearLabel",
            header: "Năm học",
            render: (s) => (
              <div>
                <div className="text-xs">{s.academicYearLabel || "—"}</div>
                <div className="text-xs text-muted-foreground">{s.cohort}</div>
              </div>
            ),
          },
          {
            key: "dob",
            header: "Ngày sinh",
            render: (s) => <span className="text-xs tabular-nums">{s.dob || "—"}</span>,
          },
          {
            key: "address",
            header: "Địa chỉ",
            render: (s) => (
              <div className="max-w-48 truncate text-xs text-muted-foreground">
                {s.address || "—"}
              </div>
            ),
          },
          {
            key: "gpa",
            header: "GPA / CPA",
            render: (s) => (
              <div className="text-xs tabular-nums whitespace-nowrap">
                <div>GPA {s.gpa != null ? s.gpa.toFixed(2) : "—"}</div>
                <div className="text-muted-foreground">
                  CPA {s.cpa != null ? s.cpa.toFixed(2) : "—"}
                </div>
                {s.totalCredits != null && (
                  <div className="text-muted-foreground">{s.totalCredits} TC</div>
                )}
              </div>
            ),
          },
          {
            key: "status",
            header: "Trạng thái",
            render: (s) => <StudentStatusBadge status={s.status} />,
          },
          {
            key: "actions",
            header: "",
            className: "w-20 text-right",
            searchable: false,
            render: (s) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7"
                  onClick={() => setDetailStudent(s)}
                >
                  <Eye className="h-3.5 w-3.5" />
                </Button>
              </div>
            ),
          },
        ]}
      />

      <StudentDetailSheet
        student={detailStudent}
        open={!!detailStudent}
        onClose={() => setDetailStudent(null)}
        majors={majors}
      />
    </div>
  );
}
