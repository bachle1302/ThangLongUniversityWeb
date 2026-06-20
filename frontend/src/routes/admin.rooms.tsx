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
import { StatusBadge } from "@/components/ui/status-badge";
import { adminApi } from "@/lib/api/admin";
import type { RoomResponse } from "@/lib/api/types";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/rooms")({ component: RoomsPage });

type RoomForm = {
  name: string;
  capacity: string;
  type: RoomType;
  status: RoomStatus;
};

type RoomType = "LECTURE" | "LAB" | "AUDITORIUM";
type RoomStatus = "AVAILABLE" | "MAINTENANCE";

type RoomRow = {
  id: number | string;
  name: string;
  capacity: number;
  type: RoomType;
  status: RoomStatus;
};

const emptyForm: RoomForm = {
  name: "",
  capacity: "",
  type: "LECTURE",
  status: "AVAILABLE",
};

const all = "__all";

function RoomsPage() {
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editItem, setEditItem] = useState<RoomRow | null>(null);
  const [toDelete, setToDelete] = useState<RoomRow | null>(null);
  const [typeFilter, setTypeFilter] = useState(all);
  const [statusFilter, setStatusFilter] = useState(all);

  const query = useQuery({
    queryKey: ["admin", "rooms"],
    queryFn: adminApi.listRooms,
    retry: false,
  });

  const data = useMemo<RoomRow[]>(() => {
    return (query.data ?? []).map(mapApiRoom);
  }, [query.data]);

  const filteredData = useMemo(() => {
    return data.filter((room) => {
      const matchType = typeFilter === all || room.type === typeFilter;
      const matchStatus = statusFilter === all || room.status === statusFilter;
      return matchType && matchStatus;
    });
  }, [data, statusFilter, typeFilter]);

  const createMutation = useMutation({
    mutationFn: (form: RoomForm) => adminApi.createRoom(toRoomRequest(form)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "rooms"] });
      setCreateOpen(false);
      toast.success("Đã thêm phòng");
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Thêm phòng thất bại"),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, form }: { id: number | string; form: RoomForm }) =>
      adminApi.updateRoom(id, toRoomRequest(form)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "rooms"] });
      setEditItem(null);
      toast.success("Đã cập nhật phòng");
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Cập nhật phòng thất bại"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number | string) => adminApi.deleteRoom(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "rooms"] });
      toast.success("Đã xóa phòng");
      setToDelete(null);
    },
    onError: (error) => toast.error(error instanceof Error ? error.message : "Xóa phòng thất bại"),
  });

  return (
    <div>
      <PageHeader title="Phòng học" description={`${filteredData.length} / ${data.length} phòng`} />

      <DataTable
        data={filteredData}
        rowKey={(room) => String(room.id)}
        searchPlaceholder="Tìm theo tên phòng, sức chứa, loại..."
        searchSlot={
          <>
            <Select value={typeFilter} onValueChange={setTypeFilter}>
              <SelectTrigger className="h-10 w-full sm:w-40">
                <SelectValue placeholder="Loại phòng" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={all}>Tất cả loại</SelectItem>
                <SelectItem value="LECTURE">Phòng học</SelectItem>
                <SelectItem value="LAB">Phòng lab</SelectItem>
                <SelectItem value="AUDITORIUM">Hội trường</SelectItem>
              </SelectContent>
            </Select>

            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="h-10 w-full sm:w-44">
                <SelectValue placeholder="Trạng thái" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={all}>Tất cả trạng thái</SelectItem>
                <SelectItem value="AVAILABLE">Sẵn sàng</SelectItem>
                <SelectItem value="MAINTENANCE">Bảo trì</SelectItem>
              </SelectContent>
            </Select>
          </>
        }
        toolbar={
          <Button className="gap-2" onClick={() => setCreateOpen(true)}>
            <Plus className="h-4 w-4" />
            Thêm phòng
          </Button>
        }
        columns={[
          {
            key: "name",
            header: "Tên phòng",
            render: (room) => <span className="font-mono font-medium">{room.name}</span>,
          },
          {
            key: "capacity",
            header: "Sức chứa",
            render: (room) => <span className="tabular-nums">{room.capacity}</span>,
          },
          {
            key: "type",
            header: "Loại phòng",
            render: (room) => <StatusBadge value={room.type} />,
          },
          {
            key: "status",
            header: "Trạng thái",
            render: (room) => <StatusBadge value={room.status} />,
          },
          {
            key: "actions",
            header: "",
            className: "w-24 text-right",
            searchable: false,
            render: (room) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  onClick={() => setEditItem(room)}
                >
                  <Pencil className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8 text-destructive"
                  onClick={() => setToDelete(room)}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ),
          },
        ]}
      />

      <RoomFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        title="Thêm phòng"
        initial={emptyForm}
        submitting={createMutation.isPending}
        onSubmit={(form) => createMutation.mutate(form)}
      />

      <RoomFormDialog
        open={!!editItem}
        onOpenChange={(value) => !value && setEditItem(null)}
        title={`Sửa phòng ${editItem?.name ?? ""}`}
        initial={editItem ? toForm(editItem) : emptyForm}
        submitting={updateMutation.isPending}
        onSubmit={(form) => editItem && updateMutation.mutate({ id: editItem.id, form })}
      />

      <ConfirmDialog
        open={!!toDelete}
        onOpenChange={(value) => !value && setToDelete(null)}
        title="Xóa phòng?"
        description={toDelete?.name}
        destructive
        confirmText="Xóa"
        onConfirm={() => {
          if (toDelete) deleteMutation.mutate(toDelete.id);
        }}
      />
    </div>
  );
}

function RoomFormDialog({
  open,
  onOpenChange,
  title,
  initial,
  submitting,
  onSubmit,
}: {
  open: boolean;
  onOpenChange: (value: boolean) => void;
  title: string;
  initial: RoomForm;
  submitting: boolean;
  onSubmit: (form: RoomForm) => void;
}) {
  const [form, setForm] = useState<RoomForm>(initial);

  useEffect(() => {
    if (open) setForm(initial);
  }, [initial, open]);

  const capacity = Number(form.capacity);
  const canSubmit = form.name.trim() && Number.isInteger(capacity) && capacity > 0;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <div className="grid gap-3 py-2">
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Tên phòng</Label>
            <Input
              className="h-9 text-sm"
              value={form.name}
              placeholder="VD: A301"
              onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
            />
          </div>

          <div className="flex flex-col gap-1">
            <Label className="text-xs">Sức chứa</Label>
            <Input
              className="h-9 text-sm"
              type="number"
              min={1}
              value={form.capacity}
              placeholder="VD: 60"
              onChange={(event) => setForm((prev) => ({ ...prev, capacity: event.target.value }))}
            />
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="flex flex-col gap-1">
              <Label className="text-xs">Loại phòng</Label>
              <Select
                value={form.type}
                onValueChange={(value) => setForm((prev) => ({ ...prev, type: value as RoomType }))}
              >
                <SelectTrigger className="h-9 text-sm">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="LECTURE">Phòng học</SelectItem>
                  <SelectItem value="LAB">Phòng lab</SelectItem>
                  <SelectItem value="AUDITORIUM">Hội trường</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1">
              <Label className="text-xs">Trạng thái</Label>
              <Select
                value={form.status}
                onValueChange={(value) =>
                  setForm((prev) => ({ ...prev, status: value as RoomStatus }))
                }
              >
                <SelectTrigger className="h-9 text-sm">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="AVAILABLE">Sẵn sàng</SelectItem>
                  <SelectItem value="MAINTENANCE">Bảo trì</SelectItem>
                </SelectContent>
              </Select>
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
          <Button size="sm" disabled={submitting || !canSubmit} onClick={() => onSubmit(form)}>
            {submitting ? "Đang lưu..." : "Lưu"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function mapApiRoom(room: RoomResponse): RoomRow {
  return {
    id: room.id,
    name: room.name,
    capacity: room.capacity,
    type: normalizeRoomType(room.type),
    status: normalizeRoomStatus(room.status),
  };
}

function toForm(room: RoomRow): RoomForm {
  return {
    name: room.name,
    capacity: String(room.capacity),
    type: room.type,
    status: room.status,
  };
}

function toRoomRequest(form: RoomForm) {
  return {
    name: form.name.trim(),
    capacity: Number(form.capacity),
    type: form.type,
    status: form.status,
  };
}

function normalizeRoomType(value: RoomResponse["type"]): RoomType {
  return value === "LAB" || value === "AUDITORIUM" || value === "LECTURE" ? value : "LECTURE";
}

function normalizeRoomStatus(value: RoomResponse["status"]): RoomStatus {
  return value === "MAINTENANCE" || value === "AVAILABLE" ? value : "AVAILABLE";
}
