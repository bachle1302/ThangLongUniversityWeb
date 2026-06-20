import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";
import { DataTable } from "@/components/data-table/DataTable";
import { PageHeader } from "@/components/ui/page-header";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Eye, Pencil, Plus, Trash2, UserMinus, UserPlus } from "lucide-react";
import { toast } from "sonner";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { adminApi } from "@/lib/api/admin";
import type {
  AdminStudentResponse,
  AdminTeacherResponse,
  HomeroomResponse,
  MajorResponse,
} from "@/lib/api/types";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Sheet, SheetContent, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export const Route = createFileRoute("/admin/homerooms")({ component: HomeroomsPage });

type HomeroomForm = {
  className: string;
  majorId: string;
  academicYear: string;
  cohort: string;
};

const emptyForm: HomeroomForm = {
  className: "",
  majorId: "",
  academicYear: "",
  cohort: "",
};

const none = "__none";
const all = "__all";

function toForm(h: HomeroomResponse): HomeroomForm {
  return {
    className: h.className,
    majorId: h.majorId != null ? String(h.majorId) : "",
    academicYear: h.academicYear != null ? String(h.academicYear) : "",
    cohort: h.cohort ?? "",
  };
}

function toHomeroomPayload(form: HomeroomForm, advisorId?: number | null) {
  return {
    className: form.className.trim(),
    advisorId: advisorId ?? null,
    majorId: form.majorId ? Number(form.majorId) : null,
    academicYear: form.academicYear ? Number(form.academicYear) : null,
    cohort: form.cohort.trim() || undefined,
  };
}

function academicRange(year?: number | null) {
  return year ? `${year}-${year + 4}` : "-";
}

function teacherMatchesMajor(teacher: AdminTeacherResponse, major?: MajorResponse) {
  if (!major?.departmentId) return true;
  return teacher.departmentId === major.departmentId;
}

function HomeroomFormDialog({
  open,
  onOpenChange,
  title,
  initial,
  onSubmit,
  submitting,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  title: string;
  initial: HomeroomForm;
  onSubmit: (form: HomeroomForm) => void;
  submitting: boolean;
}) {
  const [form, setForm] = useState<HomeroomForm>(initial);

  useEffect(() => {
    if (open) setForm(initial);
  }, [initial, open]);

  const majorsQuery = useQuery({
    queryKey: ["admin", "majors"],
    queryFn: adminApi.listMajors,
    enabled: open,
  });

  const set = (key: keyof HomeroomForm, value: string) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <div className="space-y-3 py-2">
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Tên lớp</Label>
            <Input
              className="h-8 text-xs"
              placeholder="VD: CNTT-K36A"
              value={form.className}
              onChange={(e) => set("className", e.target.value)}
            />
          </div>

          <div className="flex flex-col gap-1">
            <Label className="text-xs">Ngành</Label>
            <Select
              value={form.majorId || none}
              onValueChange={(value) => set("majorId", value === none ? "" : value)}
            >
              <SelectTrigger className="h-8 text-xs">
                <SelectValue placeholder="Chọn ngành" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={none}>Chưa chọn</SelectItem>
                {(majorsQuery.data ?? []).map((major) => (
                  <SelectItem key={major.id} value={String(major.id)}>
                    {major.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="flex gap-2">
            <div className="flex flex-1 flex-col gap-1">
              <Label className="text-xs">Năm bắt đầu</Label>
              <Input
                className="h-8 text-xs"
                type="number"
                placeholder="VD: 2024"
                value={form.academicYear}
                onChange={(e) => set("academicYear", e.target.value)}
              />
            </div>
            <div className="flex flex-1 flex-col gap-1">
              <Label className="text-xs">Khóa</Label>
              <Input
                className="h-8 text-xs"
                placeholder="VD: K36"
                value={form.cohort}
                onChange={(e) => set("cohort", e.target.value)}
              />
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onOpenChange(false)}
            disabled={submitting}
          >
            Hủy
          </Button>
          <Button
            size="sm"
            disabled={submitting || !form.className.trim() || !form.majorId}
            onClick={() => onSubmit(form)}
          >
            {submitting ? "Đang lưu..." : "Lưu"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function AddStudentsDialog({
  open,
  onOpenChange,
  homeroom,
  existingStudentIds,
  majors,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  homeroom: HomeroomResponse;
  existingStudentIds: Set<number>;
  majors: MajorResponse[];
}) {
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [search, setSearch] = useState("");
  const [advisorId, setAdvisorId] = useState(
    homeroom.advisorId != null ? String(homeroom.advisorId) : "",
  );

  useEffect(() => {
    if (open) {
      setSelected(new Set());
      setSearch("");
      setAdvisorId(homeroom.advisorId != null ? String(homeroom.advisorId) : "");
    }
  }, [homeroom.advisorId, open]);

  const studentsQuery = useQuery({
    queryKey: ["admin", "students"],
    queryFn: adminApi.listStudents,
    enabled: open,
  });

  const teachersQuery = useQuery({
    queryKey: ["admin", "teachers"],
    queryFn: adminApi.listTeachers,
    enabled: open,
  });

  const major = majors.find((item) => item.id === homeroom.majorId);
  const advisorOptions = (teachersQuery.data ?? []).filter((teacher) =>
    teacherMatchesMajor(teacher, major),
  );

  const addMutation = useMutation({
    mutationFn: () =>
      adminApi.addStudentsToHomeroom(homeroom.id, { studentIds: Array.from(selected) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "homerooms", homeroom.id, "students"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "homerooms"] });
      setSelected(new Set());
      toast.success("Đã thêm sinh viên vào lớp");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Thêm sinh viên thất bại"),
  });

  const advisorMutation = useMutation({
    mutationFn: () =>
      adminApi.updateHomeroom(homeroom.id, {
        className: homeroom.className,
        advisorId: advisorId ? Number(advisorId) : null,
        majorId: homeroom.majorId ?? null,
        academicYear: homeroom.academicYear ?? null,
        cohort: homeroom.cohort ?? undefined,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "homerooms"] });
      toast.success("Đã cập nhật cố vấn học tập");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Cập nhật cố vấn thất bại"),
  });

  const available = useMemo(() => {
    const term = search.trim().toLowerCase();
    return (studentsQuery.data ?? []).filter((student) => {
      const sameMajor = homeroom.majorId == null || student.majorId === homeroom.majorId;
      const matchedSearch =
        !term ||
        student.fullName.toLowerCase().includes(term) ||
        student.studentCode.toLowerCase().includes(term) ||
        student.email.toLowerCase().includes(term);
      return !existingStudentIds.has(student.id) && sameMajor && matchedSearch;
    });
  }, [existingStudentIds, homeroom.majorId, search, studentsQuery.data]);

  const visible = available.slice(0, 80);
  const visibleIds = visible.map((student) => student.id);
  const allVisibleSelected = visibleIds.length > 0 && visibleIds.every((id) => selected.has(id));

  const toggle = (id: number) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleVisible = () => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (allVisibleSelected) {
        visibleIds.forEach((id) => next.delete(id));
      } else {
        visibleIds.forEach((id) => next.add(id));
      }
      return next;
    });
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>Thêm sinh viên</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <section className="grid gap-2 sm:grid-cols-[1fr_auto] sm:items-end">
            <div className="flex flex-col gap-1">
              <Label className="text-xs">Cố vấn học tập</Label>
              <Select
                value={advisorId || none}
                onValueChange={(value) => setAdvisorId(value === none ? "" : value)}
              >
                <SelectTrigger className="h-8 text-xs">
                  <SelectValue placeholder="Chọn cố vấn" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={none}>Chưa chọn</SelectItem>
                  {advisorOptions.map((teacher) => (
                    <SelectItem key={teacher.id} value={String(teacher.id)}>
                      {teacher.fullName} ({teacher.teacherCode})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <Button
              size="sm"
              variant="outline"
              onClick={() => advisorMutation.mutate()}
              disabled={advisorMutation.isPending}
            >
              Cập nhật cố vấn
            </Button>
          </section>

          <section className="space-y-3">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
              <Input
                className="h-8 text-xs"
                placeholder="Tìm theo tên, mã SV, email..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
              <Button
                size="sm"
                variant="outline"
                className="shrink-0"
                onClick={toggleVisible}
                disabled={visibleIds.length === 0}
              >
                {allVisibleSelected ? "Bỏ chọn tất cả" : "Chọn tất cả"} ({visibleIds.length})
              </Button>
            </div>

            <div className="max-h-72 overflow-y-auto rounded-md border">
              {studentsQuery.isLoading ? (
                <p className="p-3 text-center text-xs text-muted-foreground">Đang tải...</p>
              ) : visible.length === 0 ? (
                <p className="p-3 text-center text-xs text-muted-foreground">
                  Không có sinh viên cùng ngành để thêm
                </p>
              ) : (
                <div className="divide-y">
                  {visible.map((student) => (
                    <label
                      key={student.id}
                      className="flex cursor-pointer items-center gap-3 px-3 py-2 hover:bg-muted/50"
                    >
                      <input
                        type="checkbox"
                        checked={selected.has(student.id)}
                        onChange={() => toggle(student.id)}
                        className="rounded"
                      />
                      <div className="min-w-0 flex-1">
                        <div className="truncate text-xs font-medium">{student.fullName}</div>
                        <div className="truncate text-xs text-muted-foreground">
                          {student.studentCode} - {student.email} - {student.majorName ?? "-"}
                        </div>
                      </div>
                    </label>
                  ))}
                </div>
              )}
            </div>

            <p className="text-xs text-muted-foreground">
              Đã chọn {selected.size} sinh viên. Danh sách chỉ hiển thị sinh viên cùng ngành với lớp.
            </p>
          </section>
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            size="sm"
            onClick={() => onOpenChange(false)}
            disabled={addMutation.isPending}
          >
            Đóng
          </Button>
          <Button
            size="sm"
            disabled={addMutation.isPending || selected.size === 0}
            onClick={() => addMutation.mutate()}
          >
            {addMutation.isPending ? "Đang thêm..." : `Thêm ${selected.size} SV`}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function HomeroomDetailSheet({
  homeroom,
  open,
  onClose,
  majors,
}: {
  homeroom: HomeroomResponse | null;
  open: boolean;
  onClose: () => void;
  majors: MajorResponse[];
}) {
  const queryClient = useQueryClient();
  const [addOpen, setAddOpen] = useState(false);
  const [toRemove, setToRemove] = useState<AdminStudentResponse | null>(null);

  const studentsQuery = useQuery({
    queryKey: ["admin", "homerooms", homeroom?.id, "students"],
    queryFn: () => adminApi.listHomeroomStudents(homeroom!.id),
    enabled: open && homeroom != null,
  });

  const removeMutation = useMutation({
    mutationFn: (studentId: number) => adminApi.removeStudentFromHomeroom(homeroom!.id, studentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "homerooms", homeroom?.id, "students"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "homerooms"] });
      setToRemove(null);
      toast.success("Đã gỡ sinh viên khỏi lớp");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Gỡ sinh viên thất bại"),
  });

  const students = studentsQuery.data ?? [];
  const existingIds = useMemo(() => new Set(students.map((student) => student.id)), [students]);

  if (!homeroom) return null;

  return (
    <>
      <Sheet open={open} onOpenChange={(value) => !value && onClose()}>
        <SheetContent side="right" className="w-full overflow-y-auto sm:max-w-xl">
          <SheetHeader className="mb-4">
            <SheetTitle className="text-base">{homeroom.className}</SheetTitle>
            <p className="text-xs text-muted-foreground">
              {homeroom.majorName ?? "-"} - Khoa {homeroom.cohort ?? "-"} -{" "}
              {academicRange(homeroom.academicYear)}
            </p>
          </SheetHeader>

          <div className="space-y-4">
            <section className="grid grid-cols-2 gap-2 text-xs">
              <div>
                <span className="text-muted-foreground">Cố vấn: </span>
                <span className="font-medium">{homeroom.advisorName ?? "-"}</span>
              </div>
              <div>
                <span className="text-muted-foreground">Số SV: </span>
                <span className="font-medium">{homeroom.studentCount ?? students.length}</span>
              </div>
              <div>
                <span className="text-muted-foreground">Trạng thái: </span>
                <span
                  className={
                    homeroom.isActive ? "font-medium text-green-600" : "text-muted-foreground"
                  }
                >
                  {homeroom.isActive ? "Đang hoạt động" : "Kết niên khóa"}
                </span>
              </div>
            </section>

            <section>
              <div className="mb-2 flex items-center justify-between">
                <h3 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Danh sách sinh viên ({students.length})
                </h3>
                <Button
                  size="sm"
                  variant="outline"
                  className="h-7 gap-1 text-xs"
                  onClick={() => setAddOpen(true)}
                >
                  <UserPlus className="h-3.5 w-3.5" />
                  Thêm SV / cố vấn
                </Button>
              </div>

              {studentsQuery.isLoading ? (
                <p className="py-4 text-center text-xs text-muted-foreground">Đang tải...</p>
              ) : students.length === 0 ? (
                <p className="py-4 text-center text-xs text-muted-foreground">
                  Chưa có sinh viên trong lớp
                </p>
              ) : (
                <div className="max-h-96 overflow-y-auto rounded-md border">
                  <div className="divide-y">
                    {students.map((student) => (
                      <div key={student.id} className="flex items-center gap-3 px-3 py-2">
                        <div className="min-w-0 flex-1">
                          <div className="truncate text-xs font-medium">{student.fullName}</div>
                          <div className="truncate text-xs text-muted-foreground">
                            {student.studentCode} - {student.email}
                          </div>
                        </div>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-6 w-6 shrink-0 text-destructive"
                          onClick={() => setToRemove(student)}
                        >
                          <UserMinus className="h-3.5 w-3.5" />
                        </Button>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </section>
          </div>
        </SheetContent>
      </Sheet>

      <AddStudentsDialog
        open={addOpen}
        onOpenChange={setAddOpen}
        homeroom={homeroom}
        existingStudentIds={existingIds}
        majors={majors}
      />

      <ConfirmDialog
        open={!!toRemove}
        onOpenChange={(value) => !value && setToRemove(null)}
        title="Gỡ sinh viên khỏi lớp?"
        description={toRemove?.fullName}
        destructive
        confirmText="Gỡ bỏ"
        onConfirm={() => {
          if (toRemove) removeMutation.mutate(toRemove.id);
        }}
      />
    </>
  );
}

function HomeroomsPage() {
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editItem, setEditItem] = useState<HomeroomResponse | null>(null);
  const [toDelete, setToDelete] = useState<HomeroomResponse | null>(null);
  const [detailItem, setDetailItem] = useState<HomeroomResponse | null>(null);
  const [majorFilter, setMajorFilter] = useState(all);
  const [cohortFilter, setCohortFilter] = useState(all);
  const [statusFilter, setStatusFilter] = useState(all);

  const query = useQuery({
    queryKey: ["admin", "homerooms"],
    queryFn: adminApi.listHomerooms,
  });

  const majorsQuery = useQuery({
    queryKey: ["admin", "majors"],
    queryFn: adminApi.listMajors,
  });

  const data = query.data ?? [];
  const majors = majorsQuery.data ?? [];
  const cohorts = useMemo(
    () => Array.from(new Set(data.map((item) => item.cohort).filter(Boolean))).sort() as string[],
    [data],
  );

  const filteredData = useMemo(() => {
    return data.filter((item) => {
      const matchedMajor = majorFilter === all || String(item.majorId ?? "") === majorFilter;
      const matchedCohort = cohortFilter === all || item.cohort === cohortFilter;
      const matchedStatus =
        statusFilter === all ||
        (statusFilter === "active" ? item.isActive !== false : item.isActive === false);
      return matchedMajor && matchedCohort && matchedStatus;
    });
  }, [cohortFilter, data, majorFilter, statusFilter]);

  const createMutation = useMutation({
    mutationFn: (form: HomeroomForm) => adminApi.createHomeroom(toHomeroomPayload(form)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "homerooms"] });
      setCreateOpen(false);
      toast.success("Đã tạo lớp");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Tạo lớp thất bại"),
  });

  const updateMutation = useMutation({
    mutationFn: ({
      id,
      form,
      advisorId,
    }: {
      id: number;
      form: HomeroomForm;
      advisorId?: number | null;
    }) => adminApi.updateHomeroom(id, toHomeroomPayload(form, advisorId)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "homerooms"] });
      setEditItem(null);
      toast.success("Đã cập nhật lớp");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Cập nhật lớp thất bại"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteHomeroom(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "homerooms"] });
      toast.success("Đã xóa lớp");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Xóa lớp thất bại"),
  });

  return (
    <div>
      <PageHeader
        title="Lớp hành chính"
        description={`${filteredData.length} / ${data.length} lớp`}
      />

      <DataTable
        data={filteredData}
        rowKey={(homeroom) => String(homeroom.id)}
        searchPlaceholder="Tìm theo tên lớp, cố vấn, ngành..."
        searchSlot={
          <>
            <Select value={majorFilter} onValueChange={setMajorFilter}>
              <SelectTrigger className="h-10 w-full sm:w-44">
                <SelectValue placeholder="Tất cả ngành" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={all}>Tất cả ngành</SelectItem>
                {majors.map((major) => (
                  <SelectItem key={major.id} value={String(major.id)}>
                    {major.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select value={cohortFilter} onValueChange={setCohortFilter}>
              <SelectTrigger className="h-10 w-full sm:w-36">
                <SelectValue placeholder="Tất cả khóa" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={all}>Tất cả khóa</SelectItem>
                {cohorts.map((cohort) => (
                  <SelectItem key={cohort} value={cohort}>
                    {cohort}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="h-10 w-full sm:w-40">
                <SelectValue placeholder="Trạng thái" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={all}>Tất cả trạng thái</SelectItem>
                <SelectItem value="active">Đang hoạt động</SelectItem>
                <SelectItem value="inactive">ết niên khóa</SelectItem>
              </SelectContent>
            </Select>
          </>
        }
        toolbar={
          <Button className="gap-2" onClick={() => setCreateOpen(true)}>
            <Plus className="h-4 w-4" />
            Them lop
          </Button>
        }
        columns={[
          {
            key: "className",
            header: "Tên lớp",
            render: (homeroom) => <span className="font-medium">{homeroom.className}</span>,
          },
          {
            key: "advisorName",
            header: "Cố vấn học tập",
            render: (homeroom) => <span className="text-sm">{homeroom.advisorName ?? "-"}</span>,
          },
          {
            key: "majorName",
            header: "Ngành",
            render: (homeroom) => (
              <span className="text-xs text-muted-foreground">{homeroom.majorName ?? "-"}</span>
            ),
          },
          {
            key: "cohort",
            header: "Khóa",
            render: (homeroom) => (
              <span className="text-xs tabular-nums">{homeroom.cohort ?? "-"}</span>
            ),
          },
          {
            key: "academicYear",
            header: "Niên khóa",
            accessor: (homeroom) => academicRange(homeroom.academicYear),
            render: (homeroom) => (
              <span className="text-xs tabular-nums">{academicRange(homeroom.academicYear)}</span>
            ),
          },
          {
            key: "studentCount",
            header: "Số SV",
            render: (homeroom) => (
              <span className="text-xs tabular-nums">{homeroom.studentCount ?? "-"}</span>
            ),
          },
          {
            key: "isActive",
            header: "Trạng thái",
            accessor: (homeroom) =>
              homeroom.isActive === false ? "Kết niên khóa" : "Đang hoạt động",
            render: (homeroom) =>
              homeroom.isActive === false ? (
                <span className="text-xs text-muted-foreground">Het nien khoa</span>
              ) : (
                <span className="text-xs text-green-600">Dang hoat dong</span>
              ),
          },
          {
            key: "actions",
            header: "",
            className: "w-28 text-right",
            searchable: false,
            render: (homeroom) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7"
                  onClick={() => setDetailItem(homeroom)}
                >
                  <Eye className="h-3.5 w-3.5" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7"
                  onClick={() => setEditItem(homeroom)}
                >
                  <Pencil className="h-3.5 w-3.5" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 text-destructive"
                  onClick={() => setToDelete(homeroom)}
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </Button>
              </div>
            ),
          },
        ]}
      />

      <HomeroomFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        title="Tạo lớp hành chính mới"
        initial={emptyForm}
        onSubmit={(form) => createMutation.mutate(form)}
        submitting={createMutation.isPending}
      />

      <HomeroomFormDialog
        open={!!editItem}
        onOpenChange={(value) => !value && setEditItem(null)}
        title={`Sửa lớp ${editItem?.className ?? ""}`}
        initial={editItem ? toForm(editItem) : emptyForm}
        onSubmit={(form) =>
          editItem &&
          updateMutation.mutate({ id: editItem.id, form, advisorId: editItem.advisorId })
        }
        submitting={updateMutation.isPending}
      />

      <HomeroomDetailSheet
        homeroom={detailItem}
        open={!!detailItem}
        onClose={() => setDetailItem(null)}
        majors={majors}
      />

      <ConfirmDialog
        open={!!toDelete}
        onOpenChange={(value) => !value && setToDelete(null)}
        title="Xóa lớp hành chính?"
        description={toDelete?.className}
        destructive
        confirmText="Xóa bỏ"
        onConfirm={() => {
          if (toDelete) deleteMutation.mutate(toDelete.id);
          setToDelete(null);
        }}
      />
    </div>
  );
}
