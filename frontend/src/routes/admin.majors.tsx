import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect, useMemo, useState } from "react";
import { PageHeader } from "@/components/ui/page-header";
import { DataTable } from "@/components/data-table/DataTable";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { adminApi } from "@/lib/api/admin";
import type { MajorResponse } from "@/lib/api/types";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

export const Route = createFileRoute("/admin/majors")({ component: MajorsPage });

type MajorForm = { majorCode: string; name: string; description: string; departmentId: string };
type MajorRow = {
  id: string;
  code: string;
  name: string;
  description?: string | null;
  departmentName?: string | null;
  students: number;
  courses: number;
};
const emptyForm: MajorForm = { majorCode: "", name: "", description: "", departmentId: "" };

function MajorFormDialog({
  open,
  onOpenChange,
  title,
  initial,
  departments,
  onSubmit,
  submitting,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  title: string;
  initial: MajorForm;
  departments: { id: number; name: string }[];
  onSubmit: (form: MajorForm) => void;
  submitting: boolean;
}) {
  const [form, setForm] = useState<MajorForm>(initial);

  useEffect(() => {
    if (open) setForm(initial);
  }, [initial, open]);

  const set = (key: keyof MajorForm, value: string) => {
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
            <Label className="text-xs">Ma nganh</Label>
            <Input
              className="h-8 text-xs"
              placeholder="VD: CNTT"
              value={form.majorCode}
              onChange={(event) => set("majorCode", event.target.value)}
            />
          </div>
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Ten nganh</Label>
            <Input
              className="h-8 text-xs"
              placeholder="VD: Công nghệ thông tin"
              value={form.name}
              onChange={(event) => set("name", event.target.value)}
            />
          </div>
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Mô tả</Label>
            <Input
              className="h-8 text-xs"
              placeholder="Mô tả ngắn"
              value={form.description}
              onChange={(event) => set("description", event.target.value)}
            />
          </div>
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Khoa / Bộ môn</Label>
            <Select
              value={form.departmentId || "__none"}
              onValueChange={(value) => set("departmentId", value === "__none" ? "" : value)}
            >
              <SelectTrigger className="h-8 text-xs">
                <SelectValue placeholder="Chọn khoa..." />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="__none">Chưa chọn</SelectItem>
                {departments.map((department) => (
                  <SelectItem key={department.id} value={String(department.id)}>
                    {department.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
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
            disabled={submitting || !form.majorCode.trim() || !form.name.trim()}
            onClick={() => onSubmit(form)}
          >
            {submitting ? "Đang lưu..." : "Lưu"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function MajorsPage() {
  const queryClient = useQueryClient();
  const [toDelete, setToDelete] = useState<MajorRow | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [editItem, setEditItem] = useState<MajorResponse | null>(null);
  const [departmentFilter, setDepartmentFilter] = useState("all");

  const query = useQuery({
    queryKey: ["admin", "majors"],
    queryFn: adminApi.listMajors,
  });
  const departmentsQuery = useQuery({
    queryKey: ["admin", "departments"],
    queryFn: adminApi.listDepartments,
  });

  const createMutation = useMutation({
    mutationFn: (form: MajorForm) =>
      adminApi.createMajor({
        majorCode: form.majorCode.trim(),
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        departmentId: form.departmentId ? Number(form.departmentId) : null,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "majors"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "departments"] });
      setCreateOpen(false);
      toast.success("Đã tạo ngành");
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Tạo ngành thất bại"),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, form }: { id: number; form: MajorForm }) =>
      adminApi.updateMajor(id, {
        majorCode: form.majorCode.trim(),
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        departmentId: form.departmentId ? Number(form.departmentId) : null,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "majors"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "departments"] });
      setEditItem(null);
      toast.success("Đã cập nhật ngành");
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Cập nhật ngành thất bại"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminApi.deleteMajor(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "majors"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "departments"] });
      toast.success("Đã xóa ngành");
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Xóa ngành thất bại"),
  });

  const data = useMemo<MajorRow[]>(() => {
    return (query.data ?? []).map((m) => ({
      id: String(m.id),
      code: m.majorCode,
      name: m.name,
      description: m.description ?? null,
      departmentName: m.departmentName ?? null,
      students: m.studentCount ?? 0,
      courses: m.courseCount ?? 0,
    }));
  }, [query.data]);

  const departments = departmentsQuery.data ?? [];
  const filteredData = useMemo(() => {
    return data.filter(
      (major) => departmentFilter === "all" || major.departmentName === departmentFilter,
    );
  }, [data, departmentFilter]);

  return (
    <div>
      <PageHeader title="Ngành học" description={`${filteredData.length} / ${data.length} ngành`} />
      <DataTable
        data={filteredData}
        rowKey={(m) => m.id}
        searchPlaceholder="Tìm theo mã, tên ngành..."
        searchSlot={
          <Select value={departmentFilter} onValueChange={setDepartmentFilter}>
            <SelectTrigger className="h-10 w-full sm:w-44">
              <SelectValue placeholder="Tất cả khoa" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Tất cả khoa</SelectItem>
              {departments.map((department) => (
                <SelectItem key={department.id} value={department.name}>
                  {department.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        }
        toolbar={
          <Button className="gap-2" onClick={() => setCreateOpen(true)} disabled={query.isError}>
            <Plus className="h-4 w-4" />
            Thêm ngành
          </Button>
        }
        columns={[
          {
            key: "code",
            header: "Mã ngành",
            render: (m) => <span className="font-mono text-xs">{m.code}</span>,
          },
          {
            key: "name",
            header: "Tên ngành",
            render: (m) => <span className="font-medium">{m.name}</span>,
          },
          {
            key: "departmentName",
            header: "Khoa",
            render: (m) => <span className="text-xs">{m.departmentName || "-"}</span>,
          },
          {
            key: "description",
            header: "Mô tả",
            render: (m) => (
              <span className="text-xs text-muted-foreground">{m.description || "-"}</span>
            ),
          },
          {
            key: "students",
            header: "Sinh viên",
            render: (m) => <span className="tabular-nums">{m.students.toLocaleString()}</span>,
          },
          {
            key: "courses",
            header: "Môn học",
            render: (m) => <span className="tabular-nums">{m.courses}</span>,
          },
          {
            key: "actions",
            header: "",
            className: "w-24 text-right",
            searchable: false,
            render: (m) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  onClick={() =>
                    setEditItem(query.data?.find((item) => String(item.id) === m.id) ?? null)
                  }
                  disabled={query.isError}
                >
                  <Pencil className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8 text-destructive"
                  onClick={() => setToDelete(m)}
                  disabled={query.isError}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ),
          },
        ]}
      />
      <MajorFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        title="Thêm ngành mới"
        initial={emptyForm}
        departments={departments}
        onSubmit={(form) => createMutation.mutate(form)}
        submitting={createMutation.isPending}
      />
      <MajorFormDialog
        open={!!editItem}
        onOpenChange={(v) => !v && setEditItem(null)}
        title={`Sửa ngành ${editItem?.name ?? ""}`}
        initial={
          editItem
            ? {
                majorCode: editItem.majorCode,
                name: editItem.name,
                description: editItem.description ?? "",
                departmentId: editItem.departmentId ? String(editItem.departmentId) : "",
              }
            : emptyForm
        }
        departments={departments}
        onSubmit={(form) => editItem && updateMutation.mutate({ id: editItem.id, form })}
        submitting={updateMutation.isPending}
      />
      <ConfirmDialog
        open={!!toDelete}
        onOpenChange={(v) => !v && setToDelete(null)}
        title="Xóa ngành?"
        description={toDelete?.name}
        destructive
        confirmText="Xóa"
        onConfirm={() => {
          if (toDelete) deleteMutation.mutate(toDelete.id);
          setToDelete(null);
        }}
      />
    </div>
  );
}
