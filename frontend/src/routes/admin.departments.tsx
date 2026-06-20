import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { PageHeader } from "@/components/ui/page-header";
import { DataTable } from "@/components/data-table/DataTable";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { adminApi } from "@/lib/api/admin";
import type { DepartmentResponse } from "@/lib/api/types";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export const Route = createFileRoute("/admin/departments")({ component: DepartmentsPage });

type DeptForm = { departmentCode: string; name: string; description: string };
const emptyForm: DeptForm = { departmentCode: "", name: "", description: "" };

function DepartmentFormDialog({
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
  initial: DeptForm;
  onSubmit: (form: DeptForm) => void;
  submitting: boolean;
}) {
  const [form, setForm] = useState<DeptForm>(initial);

  // sync when dialog opens with new initial
  useMemo(() => {
    setForm(initial);
  }, [initial, open]);

  const set = (key: keyof DeptForm, v: string) => setForm((prev) => ({ ...prev, [key]: v }));

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>
        <div className="space-y-3 py-2">
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Mã khoa</Label>
            <Input
              className="h-8 text-xs"
              placeholder="VD: CNTT"
              value={form.departmentCode}
              onChange={(e) => set("departmentCode", e.target.value)}
            />
          </div>
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Tên khoa</Label>
            <Input
              className="h-8 text-xs"
              placeholder="VD: Khoa Công nghệ thông tin"
              value={form.name}
              onChange={(e) => set("name", e.target.value)}
            />
          </div>
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Mô tả</Label>
            <Input
              className="h-8 text-xs"
              placeholder="Mô tả ngắn (tuỳ chọn)"
              value={form.description}
              onChange={(e) => set("description", e.target.value)}
            />
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
            disabled={submitting || !form.departmentCode.trim() || !form.name.trim()}
            onClick={() => onSubmit(form)}
          >
            {submitting ? "Đang lưu..." : "Lưu"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function DepartmentsPage() {
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: ["admin", "departments"],
    queryFn: adminApi.listDepartments,
  });

  const [createOpen, setCreateOpen] = useState(false);
  const [editItem, setEditItem] = useState<DepartmentResponse | null>(null);
  const [toDelete, setToDelete] = useState<DepartmentResponse | null>(null);

  const createMutation = useMutation({
    mutationFn: (form: DeptForm) =>
      adminApi.createDepartment({
        departmentCode: form.departmentCode,
        name: form.name,
        description: form.description || undefined,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "departments"] });
      setCreateOpen(false);
      toast.success("Đã tạo khoa mới");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Tạo khoa thất bại"),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, form }: { id: number; form: DeptForm }) =>
      adminApi.updateDepartment(id, {
        departmentCode: form.departmentCode,
        name: form.name,
        description: form.description || undefined,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "departments"] });
      setEditItem(null);
      toast.success("Đã cập nhật khoa");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Cập nhật khoa thất bại"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteDepartment(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "departments"] });
      toast.success("Đã xóa khoa");
    },
    onError: (e) => toast.error(e instanceof Error ? e.message : "Xóa khoa thất bại"),
  });

  const data = query.data ?? [];

  return (
    <div>
      <PageHeader title="Khoa / Bộ môn" description={`${data.length} khoa`} />

      <DataTable
        data={data}
        rowKey={(d) => String(d.id)}
        searchPlaceholder="Tìm theo mã, tên khoa..."
        toolbar={
          <Button className="gap-2" onClick={() => setCreateOpen(true)}>
            <Plus className="h-4 w-4" />
            Thêm khoa
          </Button>
        }
        columns={[
          {
            key: "departmentCode",
            header: "Mã khoa",
            render: (d) => <span className="font-mono text-xs">{d.departmentCode}</span>,
          },
          {
            key: "name",
            header: "Tên khoa",
            render: (d) => <span className="font-medium">{d.name}</span>,
          },
          {
            key: "description",
            header: "Mô tả",
            render: (d) => (
              <span className="text-xs text-muted-foreground">{d.description ?? "—"}</span>
            ),
          },
          {
            key: "teacherCount",
            header: "Giang vien",
            render: (d) => <span className="tabular-nums">{d.teacherCount ?? 0}</span>,
          },
          {
            key: "majorCount",
            header: "Nganh",
            render: (d) => <span className="tabular-nums">{d.majorCount ?? 0}</span>,
          },
          {
            key: "actions",
            header: "",
            className: "w-24 text-right",
            searchable: false,
            render: (d) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  onClick={() => setEditItem(d)}
                >
                  <Pencil className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8 text-destructive"
                  onClick={() => setToDelete(d)}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ),
          },
        ]}
      />

      <DepartmentFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        title="Thêm khoa mới"
        initial={emptyForm}
        onSubmit={(form) => createMutation.mutate(form)}
        submitting={createMutation.isPending}
      />

      <DepartmentFormDialog
        open={!!editItem}
        onOpenChange={(v) => !v && setEditItem(null)}
        title={`Sửa khoa — ${editItem?.name ?? ""}`}
        initial={
          editItem
            ? {
                departmentCode: editItem.departmentCode,
                name: editItem.name,
                description: editItem.description ?? "",
              }
            : emptyForm
        }
        onSubmit={(form) => editItem && updateMutation.mutate({ id: editItem.id, form })}
        submitting={updateMutation.isPending}
      />

      <ConfirmDialog
        open={!!toDelete}
        onOpenChange={(v) => !v && setToDelete(null)}
        title="Xóa khoa?"
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
