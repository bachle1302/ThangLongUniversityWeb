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
import { adminApi } from "@/lib/api/admin";
import type { PeriodResponse } from "@/lib/api/types";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";

export const Route = createFileRoute("/admin/periods")({ component: PeriodsPage });

type PeriodForm = {
  periodNumber: string;
  startTime: string;
  endTime: string;
};

type PeriodRow = {
  id: number | string;
  periodNumber: number;
  startTime: string;
  endTime: string;
};

const emptyForm: PeriodForm = {
  periodNumber: "",
  startTime: "",
  endTime: "",
};

function PeriodsPage() {
  const queryClient = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [editItem, setEditItem] = useState<PeriodRow | null>(null);
  const [toDelete, setToDelete] = useState<PeriodRow | null>(null);

  const query = useQuery({
    queryKey: ["admin", "periods"],
    queryFn: adminApi.listPeriods,
    retry: false,
  });

  const data = useMemo<PeriodRow[]>(() => {
    return (query.data ?? []).map(mapApiPeriod).sort((a, b) => a.periodNumber - b.periodNumber);
  }, [query.data]);

  const createMutation = useMutation({
    mutationFn: (form: PeriodForm) => adminApi.createPeriod(toPeriodRequest(form)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "periods"] });
      setCreateOpen(false);
      toast.success("Đã thêm tiết học");
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Thêm tiết học thất bại"),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, form }: { id: number | string; form: PeriodForm }) =>
      adminApi.updatePeriod(id, toPeriodRequest(form)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "periods"] });
      setEditItem(null);
      toast.success("Đã cập nhật tiết học");
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Cập nhật tiết học thất bại"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number | string) => adminApi.deletePeriod(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "periods"] });
      toast.success("Đã xóa tiết học");
      setToDelete(null);
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Xóa tiết học thất bại"),
  });

  return (
    <div>
      <PageHeader title="Tiết học" description={`${data.length} tiết`} />

      <DataTable
        data={data}
        rowKey={(period) => String(period.id)}
        searchPlaceholder="Tìm theo tiết, giờ bắt đầu, giờ kết thúc..."
        toolbar={
          <Button className="gap-2" onClick={() => setCreateOpen(true)}>
            <Plus className="h-4 w-4" />
            Thêm tiết học
          </Button>
        }
        columns={[
          {
            key: "periodNumber",
            header: "Tiết",
            render: (period) => (
              <span className="font-mono font-semibold">Tiết {period.periodNumber}</span>
            ),
          },
          {
            key: "startTime",
            header: "Giờ bắt đầu",
            render: (period) => <span className="tabular-nums">{period.startTime}</span>,
          },
          {
            key: "endTime",
            header: "Giờ kết thúc",
            render: (period) => <span className="tabular-nums">{period.endTime}</span>,
          },
          {
            key: "actions",
            header: "",
            className: "w-24 text-right",
            searchable: false,
            render: (period) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  onClick={() => setEditItem(period)}
                >
                  <Pencil className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8 text-destructive"
                  onClick={() => setToDelete(period)}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </div>
            ),
          },
        ]}
      />

      <PeriodFormDialog
        open={createOpen}
        onOpenChange={setCreateOpen}
        title="Thêm tiết học"
        initial={emptyForm}
        submitting={createMutation.isPending}
        onSubmit={(form) => createMutation.mutate(form)}
      />

      <PeriodFormDialog
        open={!!editItem}
        onOpenChange={(value) => !value && setEditItem(null)}
        title={`Sửa tiết ${editItem?.periodNumber ?? ""}`}
        initial={editItem ? toForm(editItem) : emptyForm}
        submitting={updateMutation.isPending}
        onSubmit={(form) => editItem && updateMutation.mutate({ id: editItem.id, form })}
      />

      <ConfirmDialog
        open={!!toDelete}
        onOpenChange={(value) => !value && setToDelete(null)}
        title="Xóa tiết học?"
        description={toDelete ? `Tiết ${toDelete.periodNumber}` : undefined}
        destructive
        confirmText="Xóa"
        onConfirm={() => {
          if (toDelete) deleteMutation.mutate(toDelete.id);
        }}
      />
    </div>
  );
}

function PeriodFormDialog({
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
  initial: PeriodForm;
  submitting: boolean;
  onSubmit: (form: PeriodForm) => void;
}) {
  const [form, setForm] = useState<PeriodForm>(initial);

  useEffect(() => {
    if (open) setForm(initial);
  }, [initial, open]);

  const periodNumber = Number(form.periodNumber);
  const canSubmit =
    Number.isInteger(periodNumber) &&
    periodNumber >= 1 &&
    periodNumber <= 12 &&
    isTimeValue(form.startTime) &&
    isTimeValue(form.endTime) &&
    form.startTime < form.endTime;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
        </DialogHeader>

        <div className="grid gap-3 py-2">
          <div className="flex flex-col gap-1">
            <Label className="text-xs">Số tiết</Label>
            <Input
              className="h-9 text-sm"
              type="number"
              min={1}
              max={12}
              value={form.periodNumber}
              placeholder="VD: 1"
              onChange={(event) =>
                setForm((prev) => ({ ...prev, periodNumber: event.target.value }))
              }
            />
          </div>

          <div className="grid gap-3 sm:grid-cols-2">
            <div className="flex flex-col gap-1">
              <Label className="text-xs">Giờ bắt đầu</Label>
              <Input
                className="h-9 text-sm"
                type="time"
                value={form.startTime}
                onChange={(event) =>
                  setForm((prev) => ({ ...prev, startTime: event.target.value }))
                }
              />
            </div>

            <div className="flex flex-col gap-1">
              <Label className="text-xs">Giờ kết thúc</Label>
              <Input
                className="h-9 text-sm"
                type="time"
                value={form.endTime}
                onChange={(event) => setForm((prev) => ({ ...prev, endTime: event.target.value }))}
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
            Huy
          </Button>
          <Button size="sm" disabled={submitting || !canSubmit} onClick={() => onSubmit(form)}>
            {submitting ? "Dang luu..." : "Luu"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function mapApiPeriod(period: PeriodResponse): PeriodRow {
  return {
    id: period.id,
    periodNumber: period.periodNumber,
    startTime: normalizeTime(period.startTime),
    endTime: normalizeTime(period.endTime),
  };
}

function toForm(period: PeriodRow): PeriodForm {
  return {
    periodNumber: String(period.periodNumber),
    startTime: normalizeTime(period.startTime),
    endTime: normalizeTime(period.endTime),
  };
}

function toPeriodRequest(form: PeriodForm) {
  return {
    periodNumber: Number(form.periodNumber),
    startTime: form.startTime,
    endTime: form.endTime,
  };
}

function normalizeTime(value: string) {
  return value.length >= 5 ? value.slice(0, 5) : value;
}

function isTimeValue(value: string) {
  return /^\d{2}:\d{2}$/.test(value);
}
