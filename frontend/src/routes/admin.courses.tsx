import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { DataTable } from "@/components/data-table/DataTable";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { PageHeader } from "@/components/ui/page-header";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { adminApi } from "@/lib/api/admin";
import type { CourseResponse, CourseType } from "@/lib/api/types";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/courses")({ component: CoursesPage });

type CourseForm = {
  code: string;
  name: string;
  credits: string;
  majorId: string;
  courseType: CourseType;
  description: string;
  prerequisiteCourseIds: string[];
};

type CourseRow = Omit<CourseResponse, "id" | "majorId" | "prerequisiteCourseIds"> & {
  id: number | string;
  majorId?: number | string | null;
  prerequisiteCourseIds?: Array<number | string>;
};

const emptyForm: CourseForm = {
  code: "",
  name: "",
  credits: "",
  majorId: "",
  courseType: "REQUIRED",
  description: "",
  prerequisiteCourseIds: [],
};

const all = "__all";
const none = "__none";

function CoursesPage() {
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editItem, setEditItem] = useState<CourseRow | null>(null);
  const [toDelete, setToDelete] = useState<CourseRow | null>(null);
  const [creditsFilter, setCreditsFilter] = useState(all);
  const [majorFilter, setMajorFilter] = useState(all);
  const [typeFilter, setTypeFilter] = useState(all);

  const query = useQuery({
    queryKey: ["admin", "courses"],
    queryFn: adminApi.listCourses,
    retry: false,
  });

  const majorsQuery = useQuery({
    queryKey: ["admin", "majors"],
    queryFn: adminApi.listMajors,
  });

  const rows = useMemo<CourseRow[]>(() => {
    return query.data ?? [];
  }, [query.data]);

  const majors = majorsQuery.data ?? [];
  const creditOptions = useMemo(
    () => Array.from(new Set(rows.map((course) => course.credits))).sort((a, b) => a - b),
    [rows],
  );

  const filteredRows = useMemo(() => {
    return rows.filter((course) => {
      const matchCredits = creditsFilter === all || String(course.credits) === creditsFilter;
      const matchMajor = majorFilter === all || String(course.majorId ?? "") === majorFilter;
      const matchType = typeFilter === all || course.courseType === typeFilter;
      return matchCredits && matchMajor && matchType;
    });
  }, [creditsFilter, majorFilter, rows, typeFilter]);

  const createMutation = useMutation({
    mutationFn: (form: CourseForm) => adminApi.createCourse(toCourseRequest(form)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "courses"] });
      setCreateOpen(false);
      toast.success("Đã tạo môn học");
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Tạo môn học thất bại"),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, form }: { id: number; form: CourseForm }) =>
      adminApi.updateCourse(id, toCourseRequest(form)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "courses"] });
      setEditItem(null);
      toast.success("Đã cập nhật môn học");
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Cập nhật môn học thất bại"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteCourse(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "courses"] });
      toast.success("Đã xóa môn học");
      setToDelete(null);
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Xóa môn học thất bại"),
  });

  return (
    <div>
      <PageHeader title="Môn học" description={`${filteredRows.length} / ${rows.length} môn`} />

      <DataTable
        data={filteredRows}
        rowKey={(course) => String(course.id)}
        pageSize={10}
        searchPlaceholder="Tìm theo mã, tên môn, ngành..."
        searchSlot={
          <>
            <Select value={creditsFilter} onValueChange={setCreditsFilter}>
              <SelectTrigger className="h-10 w-full sm:w-32">
                <SelectValue placeholder="Tín chỉ" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={all}>Tất cả TC</SelectItem>
                {creditOptions.map((credits) => (
                  <SelectItem key={credits} value={String(credits)}>
                    {credits} tín chỉ
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            <Select value={majorFilter} onValueChange={setMajorFilter}>
              <SelectTrigger className="h-10 w-full sm:w-44">
                <SelectValue placeholder="Ngành" />
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

            <Select value={typeFilter} onValueChange={setTypeFilter}>
              <SelectTrigger className="h-10 w-full sm:w-40">
                <SelectValue placeholder="Tiên quyết" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={all}>Tất cả tiên quyết</SelectItem>
                <SelectItem value="REQUIRED">Bắt buộc</SelectItem>
                <SelectItem value="ELECTIVE">Tự do</SelectItem>
              </SelectContent>
            </Select>
          </>
        }
        toolbar={
          <Button className="gap-2" onClick={() => setCreateOpen(true)} disabled={query.isError}>
            <Plus className="h-4 w-4" />
            Thêm môn học
          </Button>
        }
        columns={[
          {
            key: "code",
            header: "Mã môn",
            render: (course) => <span className="font-mono text-xs">{course.code}</span>,
          },
          {
            key: "name",
            header: "Tên môn",
            render: (course) => (
              <div className="min-w-56">
                <div className="font-medium">{course.name}</div>
              </div>
            ),
          },
          {
            key: "credits",
            header: "Tín chỉ",
            render: (course) => <span className="tabular-nums">{course.credits}</span>,
          },
          {
            key: "majorName",
            header: "Ngành",
            render: (course) => <span className="text-sm">{course.majorName ?? "-"}</span>,
          },
          {
            key: "description",
            header: "Mô tả",
            render: (course) => (
              <span className="line-clamp-2 max-w-64 text-xs text-muted-foreground">
                {course.description || "-"}
              </span>
            ),
          },
          {
            key: "courseType",
            header: "Tiên quyết",
            accessor: (course) => course.courseTypeLabel ?? course.courseType ?? "",
            render: (course) => (
              <span className="text-xs text-muted-foreground">
                {course.courseTypeLabel ?? course.courseType ?? "-"}
              </span>
            ),
          },
          {
            key: "prerequisiteNames",
            header: "Môn học trước",
            accessor: (course) => course.prerequisiteNames?.join(", ") ?? "",
            render: (course) => (
              <span className="text-xs text-muted-foreground">
                {course.prerequisiteNames?.length ? course.prerequisiteNames.join(", ") : "-"}
              </span>
            ),
          },
          {
            key: "actions",
            header: "",
            className: "w-24 text-right",
            searchable: false,
            render: (course) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  onClick={() => setEditItem(course)}
                >
                  <Pencil className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8 text-destructive"
                  onClick={() => setToDelete(course)}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ),
          },
        ]}
      />

      <CourseFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        title="Thêm môn học"
        initial={emptyForm}
        courses={rows}
        submitting={createMutation.isPending}
        onSubmit={(form) => createMutation.mutate(form)}
      />

      <CourseFormDialog
        open={!!editItem}
        onOpenChange={(value) => !value && setEditItem(null)}
        title={`Sửa môn ${editItem?.code ?? ""}`}
        initial={editItem ? toForm(editItem) : emptyForm}
        courses={rows}
        editingId={editItem?.id}
        submitting={updateMutation.isPending}
        onSubmit={(form) => {
          if (editItem && typeof editItem.id === "number") {
            updateMutation.mutate({ id: editItem.id, form });
          }
        }}
      />

      <ConfirmDialog
        open={!!toDelete}
        onOpenChange={(value) => !value && setToDelete(null)}
        title="Xóa môn học?"
        description={toDelete?.name}
        destructive
        confirmText="Xóa"
        onConfirm={() => {
          if (toDelete && typeof toDelete.id === "number") deleteMutation.mutate(toDelete.id);
        }}
      />
    </div>
  );
}

function CourseFormDialog({
  open,
  onOpenChange,
  title,
  initial,
  courses,
  editingId,
  submitting,
  onSubmit,
}: {
  open: boolean;
  onOpenChange: (value: boolean) => void;
  title: string;
  initial: CourseForm;
  courses: CourseRow[];
  editingId?: number | string;
  submitting: boolean;
  onSubmit: (form: CourseForm) => void;
}) {
  const [form, setForm] = useState<CourseForm>(initial);
  const [prerequisiteSearch, setPrerequisiteSearch] = useState("");

  const majorsQuery = useQuery({
    queryKey: ["admin", "majors"],
    queryFn: adminApi.listMajors,
    enabled: open,
  });

  useEffect(() => {
    if (open) {
      setForm(initial);
      setPrerequisiteSearch("");
    }
  }, [initial, open]);

  const set = (key: keyof CourseForm, value: string) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const togglePrerequisite = (id: string) => {
    setForm((prev) => ({
      ...prev,
      prerequisiteCourseIds: prev.prerequisiteCourseIds.includes(id)
        ? prev.prerequisiteCourseIds.filter((item) => item !== id)
        : [...prev.prerequisiteCourseIds, id],
    }));
  };

  const prerequisiteOptions = courses
    .filter((course) => course.id !== editingId)
    .filter((course) => {
      const term = prerequisiteSearch.trim().toLowerCase();
      return (
        !term ||
        course.code.toLowerCase().includes(term) ||
        course.name.toLowerCase().includes(term)
      );
    })
    .slice(0, 80);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <div className="grid gap-3 py-2 sm:grid-cols-2">
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Mã môn</Label>
            <Input
              className="h-8 text-xs"
              value={form.code}
              placeholder="VD: ENG1102"
              onChange={(event) => set("code", event.target.value)}
            />
          </div>

          <div className="flex flex-col gap-1">
            <Label className="text-xs">Số tín chỉ</Label>
            <Input
              className="h-8 text-xs"
              type="number"
              min={1}
              value={form.credits}
              placeholder="VD: 3"
              onChange={(event) => set("credits", event.target.value)}
            />
          </div>

          <div className="flex flex-col gap-1 sm:col-span-2">
            <Label className="text-xs">Tên môn</Label>
            <Input
              className="h-8 text-xs"
              value={form.name}
              placeholder="VD: Tiếng Anh giao tiếp"
              onChange={(event) => set("name", event.target.value)}
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

          <div className="flex flex-col gap-1">
            <Label className="text-xs">Tiên quyết</Label>
            <Select
              value={form.courseType}
              onValueChange={(value) => set("courseType", value as CourseType)}
            >
              <SelectTrigger className="h-8 text-xs">
                <SelectValue placeholder="Chọn loại môn" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="REQUIRED">Bắt buộc</SelectItem>
                <SelectItem value="ELECTIVE">Tự do</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="flex flex-col gap-1 sm:col-span-2">
            <Label className="text-xs">Mô tả</Label>
            <Textarea
              className="min-h-20 text-xs"
              value={form.description}
              placeholder="Mô tả ngắn về môn học"
              onChange={(event) => set("description", event.target.value)}
            />
          </div>

          <div className="flex flex-col gap-2 sm:col-span-2">
            <Label className="text-xs">Môn học trước</Label>
            <Input
              className="h-8 text-xs"
              value={prerequisiteSearch}
              placeholder="Tìm môn học trước..."
              onChange={(event) => setPrerequisiteSearch(event.target.value)}
            />
            <div className="max-h-40 overflow-y-auto rounded-md border">
              {prerequisiteOptions.length === 0 ? (
                <p className="p-3 text-center text-xs text-muted-foreground">
                  Không có môn học phù hợp
                </p>
              ) : (
                <div className="divide-y">
                  {prerequisiteOptions.map((course) => (
                    <label
                      key={course.id}
                      className="flex cursor-pointer items-center gap-3 px-3 py-2 hover:bg-muted/50"
                    >
                      <input
                        type="checkbox"
                        className="rounded"
                        checked={form.prerequisiteCourseIds.includes(String(course.id))}
                        onChange={() => togglePrerequisite(String(course.id))}
                      />
                      <span className="text-xs">
                        {course.code} - {course.name}
                      </span>
                    </label>
                  ))}
                </div>
              )}
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
            disabled={
              submitting || !form.code.trim() || !form.name.trim() || !form.credits || !form.majorId
            }
            onClick={() => onSubmit(form)}
          >
            {submitting ? "Đang lưu..." : "Lưu"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function toForm(course: CourseRow): CourseForm {
  return {
    code: course.code,
    name: course.name,
    credits: String(course.credits),
    majorId: course.majorId != null ? String(course.majorId) : "",
    courseType: course.courseType ?? "REQUIRED",
    description: course.description ?? "",
    prerequisiteCourseIds: (course.prerequisiteCourseIds ?? []).map(String),
  };
}

function toCourseRequest(form: CourseForm) {
  return {
    code: form.code.trim(),
    name: form.name.trim(),
    credits: Number(form.credits),
    description: form.description.trim() || undefined,
    courseType: form.courseType,
    majorId: Number(form.majorId),
    prerequisiteCourseIds: form.prerequisiteCourseIds.map(Number),
  };
}
