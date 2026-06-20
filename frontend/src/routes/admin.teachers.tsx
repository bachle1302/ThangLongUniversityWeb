import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useMemo, useState, type ReactNode } from "react";
import { DataTable } from "@/components/data-table/DataTable";
import { PageHeader } from "@/components/ui/page-header";
import { adminApi } from "@/lib/api/admin";
import type { AdminTeacherResponse } from "@/lib/api/types";
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

export const Route = createFileRoute("/admin/teachers")({ component: TeachersPage });

// ── Status mapping ─────────────────────────────────────────────────────────
const STATUS_LABELS: Record<string, { label: string; color: string }> = {
  DANG_GIANG_DAY: {
    label: "Đang giảng dạy",
    color: "bg-success/15 text-success border-success/30",
  },
  NGHI_PHEP: {
    label: "Nghỉ phép",
    color: "bg-warning/20 text-warning-foreground border-warning/40",
  },
  DA_NGHI_VIEC: {
    label: "Đã nghỉ việc",
    color: "bg-destructive/15 text-destructive border-destructive/30",
  },
};

const TEACHER_STATUSES = [
  { value: "DANG_GIANG_DAY", label: "Đang giảng dạy" },
  { value: "NGHI_PHEP", label: "Nghỉ phép" },
  { value: "DA_NGHI_VIEC", label: "Đã nghỉ việc" },
];

function TeacherStatusBadge({ status }: { status?: string | null }) {
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
type TeacherRow = {
  id: string;
  numericId: number;
  raw: AdminTeacherResponse;
  code: string;
  username: string;
  fullName: string;
  email: string;
  departmentId: number | null;
  departmentName: string;
  degree: string;
  phone: string;
  dob: string;
  dobYear: string;
  address: string;
  status: string;
};

function mapApiTeacher(t: AdminTeacherResponse): TeacherRow {
  const dobYear = t.dob ? t.dob.split("-")[0] : "";
  return {
    id: String(t.id),
    numericId: t.id,
    raw: t,
    code: t.teacherCode,
    username: t.username ?? "",
    fullName: t.fullName,
    email: t.email ?? "",
    departmentId: t.departmentId ?? null,
    departmentName: t.departmentName ?? "",
    degree: t.degree ?? "",
    phone: t.phone ?? "",
    dob: t.dob ?? "",
    dobYear,
    address: t.address ?? t.currentAddress ?? "",
    status: t.status ?? "",
  };
}

// ── Detail sheet with inline editing ──────────────────────────────────────
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

type EditableField =
  | "fullName"
  | "email"
  | "dob"
  | "gender"
  | "phone"
  | "nationalId"
  | "placeOfBirth"
  | "hometown"
  | "permanentAddress"
  | "currentAddress"
  | "emergencyContact"
  | "departmentId"
  | "degree"
  | "address"
  | "status";

function TeacherDetailSheet({
  teacher,
  open,
  onClose,
}: {
  teacher: TeacherRow | null;
  open: boolean;
  onClose: () => void;
}) {
  const queryClient = useQueryClient();
  const [editingField, setEditingField] = useState<EditableField | null>(null);
  const [draft, setDraft] = useState<Record<string, string>>({});

  const departmentsQuery = useQuery({
    queryKey: ["admin", "departments"],
    queryFn: adminApi.listDepartments,
    enabled: open,
  });

  useEffect(() => {
    if (!teacher) {
      setEditingField(null);
      setDraft({});
      return;
    }
    const t = teacher.raw;
    setDraft({
      fullName: teacher.fullName,
      email: teacher.email,
      dob: teacher.dob,
      gender: t.gender ?? "",
      phone: t.phone ?? "",
      nationalId: t.nationalId ?? "",
      placeOfBirth: t.placeOfBirth ?? "",
      hometown: t.hometown ?? "",
      permanentAddress: t.permanentAddress ?? "",
      currentAddress: t.currentAddress ?? "",
      emergencyContact: t.emergencyContact ?? "",
      departmentId: t.departmentId != null ? String(t.departmentId) : "",
      degree: t.degree ?? "",
      address: t.address ?? t.currentAddress ?? "",
      status: teacher.status,
    });
    setEditingField(null);
  }, [teacher]);

  const mutation = useMutation({
    mutationFn: async ({ payload }: { payload: Record<string, string | number | undefined> }) => {
      if (!teacher) throw new Error("Không tìm thấy giảng viên");
      return adminApi.updateTeacher(teacher.numericId, payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "teachers"] });
      setEditingField(null);
      toast.success("Đã cập nhật giảng viên");
    },
    onError: (e) => toast.error(e.message),
  });

  if (!teacher) return null;

  const departments = departmentsQuery.data ?? [];
  const set = (key: string, value: string) => setDraft((prev) => ({ ...prev, [key]: value }));

  const buildPayload = (field: EditableField) => {
    switch (field) {
      case "fullName":
        return { fullName: draft.fullName || undefined };
      case "email":
        return { email: draft.email || undefined };
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
      case "departmentId":
        return { departmentId: draft.departmentId ? Number(draft.departmentId) : undefined };
      case "degree":
        return { degree: draft.degree || undefined };
      case "address":
        return { address: draft.address || undefined };
      case "status":
        return { status: (draft.status as AdminTeacherResponse["status"]) || undefined };
      default:
        return {};
    }
  };

  const saveField = (field: EditableField) => mutation.mutate({ payload: buildPayload(field) });

  const rowActions = (field: EditableField) => ({
    editing: editingField === field,
    onEdit: () => setEditingField(field),
    onSave: () => saveField(field),
    onCancel: () => setEditingField(null),
    saving: mutation.isPending,
  });

  const deptLabel =
    departments.find((d) => String(d.id) === draft.departmentId)?.name ?? teacher.departmentName;

  return (
    <Sheet open={open} onOpenChange={(v) => !v && onClose()}>
      <SheetContent side="right" className="w-full sm:max-w-lg overflow-y-auto">
        <SheetHeader className="mb-4">
          <SheetTitle className="text-base">{teacher.fullName}</SheetTitle>
          <p className="text-xs text-muted-foreground">
            {teacher.code} · {teacher.email}
          </p>
        </SheetHeader>

        <div className="space-y-4">
          <section>
            <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              Thông tin cơ bản
            </h3>
            <DetailRow
              label="Họ tên"
              value={draft.fullName}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.fullName ?? ""}
                  onChange={(e) => set("fullName", e.target.value)}
                />
              }
              {...rowActions("fullName")}
            />
            <DetailRow
              label="Email"
              value={draft.email}
              editor={
                <Input
                  className="h-8 text-xs"
                  type="email"
                  value={draft.email ?? ""}
                  onChange={(e) => set("email", e.target.value)}
                />
              }
              {...rowActions("email")}
            />
          </section>

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
              label="Địa chỉ (tổng quát)"
              value={draft.address}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.address ?? ""}
                  onChange={(e) => set("address", e.target.value)}
                />
              }
              {...rowActions("address")}
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
              Công tác
            </h3>
            <DetailRow
              label="Khoa / Bộ môn"
              value={deptLabel}
              editor={
                <Select
                  value={draft.departmentId || "__none"}
                  onValueChange={(v) => set("departmentId", v === "__none" ? "" : v)}
                >
                  <SelectTrigger className="h-8 text-xs">
                    <SelectValue placeholder="Chọn khoa..." />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__none">Chưa chọn</SelectItem>
                    {departments.map((d) => (
                      <SelectItem key={d.id} value={String(d.id)}>
                        {d.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              }
              {...rowActions("departmentId")}
            />
            <DetailRow
              label="Học vị"
              value={draft.degree}
              editor={
                <Input
                  className="h-8 text-xs"
                  value={draft.degree ?? ""}
                  onChange={(e) => set("degree", e.target.value)}
                />
              }
              {...rowActions("degree")}
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
                    {TEACHER_STATUSES.map((s) => (
                      <SelectItem key={s.value} value={s.value}>
                        {s.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              }
              {...rowActions("status")}
            />
          </section>
        </div>
      </SheetContent>
    </Sheet>
  );
}

// ── Main page ──────────────────────────────────────────────────────────────
function TeachersPage() {
  const query = useQuery({
    queryKey: ["admin", "teachers"],
    queryFn: adminApi.listTeachers,
  });

  const [statusFilter, setStatusFilter] = useState("all");
  const [departmentFilter, setDepartmentFilter] = useState("all");
  const [dobYearFilter, setDobYearFilter] = useState("all");

  const [detailTeacher, setDetailTeacher] = useState<TeacherRow | null>(null);

  const rows = useMemo<TeacherRow[]>(
    () => (query.data?.length ? query.data.map(mapApiTeacher) : []),
    [query.data],
  );

  const uniqueStatuses = useMemo(() => {
    const seen = new Set<string>();
    for (const r of rows) if (r.status) seen.add(r.status);
    return Array.from(seen);
  }, [rows]);

  const uniqueDepartments = useMemo(() => {
    const seen = new Set<string>();
    for (const r of rows) if (r.departmentName) seen.add(r.departmentName);
    return Array.from(seen).sort();
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
        if (departmentFilter !== "all" && r.departmentName !== departmentFilter) return false;
        if (dobYearFilter !== "all" && r.dobYear !== dobYearFilter) return false;
        return true;
      }),
    [rows, statusFilter, departmentFilter, dobYearFilter],
  );

  const filterRow = (
    <>
      <div className="flex flex-col gap-1">
        <Label className="text-xs text-muted-foreground">Trạng thái</Label>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="h-8 w-36 text-xs">
            <SelectValue placeholder="Tất cả" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {uniqueStatuses.map((s) => (
              <SelectItem key={s} value={s}>
                {STATUS_LABELS[s]?.label ?? s}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div className="flex flex-col gap-1">
        <Label className="text-xs text-muted-foreground">Khoa / Bộ môn</Label>
        <Select value={departmentFilter} onValueChange={setDepartmentFilter}>
          <SelectTrigger className="h-8 w-44 text-xs">
            <SelectValue placeholder="Tất cả" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Tất cả</SelectItem>
            {uniqueDepartments.map((d) => (
              <SelectItem key={d} value={d}>
                {d}
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
    </>
  );

  return (
    <div>
      <PageHeader title="Giảng viên" description={`${filteredRows.length} giảng viên`} />

      <DataTable
        data={filteredRows}
        rowKey={(t) => t.id}
        pageSize={10}
        searchPlaceholder="Tìm theo mã, tên, email, khoa..."
        filterRow={filterRow}
        columns={[
          {
            key: "code",
            header: "Mã GV",
            render: (t) => <span className="font-mono text-xs">{t.code}</span>,
          },
          {
            key: "fullName",
            header: "Họ tên",
            render: (t) => (
              <div className="min-w-40">
                <div className="font-medium text-sm">{t.fullName}</div>
                {t.username && <div className="text-xs text-muted-foreground">@{t.username}</div>}
              </div>
            ),
          },
          {
            key: "email",
            header: "Email",
            render: (t) => <span className="text-xs text-muted-foreground">{t.email || "—"}</span>,
          },
          {
            key: "department",
            header: "Khoa / Bộ môn",
            render: (t) => (
              <div>
                <div className="text-sm">{t.departmentName || "—"}</div>
                {t.degree && <div className="text-xs text-muted-foreground">{t.degree}</div>}
              </div>
            ),
          },
          {
            key: "dob",
            header: "Ngày sinh",
            render: (t) => <span className="text-xs tabular-nums">{t.dob || "—"}</span>,
          },
          {
            key: "phone",
            header: "Điện thoại",
            render: (t) => <span className="text-xs tabular-nums">{t.phone || "—"}</span>,
          },
          {
            key: "status",
            header: "Trạng thái",
            render: (t) => <TeacherStatusBadge status={t.status} />,
          },
          {
            key: "actions",
            header: "",
            className: "w-12 text-right",
            searchable: false,
            render: (t) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7"
                  onClick={() => setDetailTeacher(t)}
                >
                  <Eye className="h-3.5 w-3.5" />
                </Button>
              </div>
            ),
          },
        ]}
      />

      <TeacherDetailSheet
        teacher={detailTeacher}
        open={!!detailTeacher}
        onClose={() => setDetailTeacher(null)}
      />
    </div>
  );
}
