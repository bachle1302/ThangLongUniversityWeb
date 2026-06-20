import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useEffect, useState } from "react";
import { PageHeader } from "@/components/ui/page-header";
import { StatusBadge } from "@/components/ui/status-badge";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { CreditCard, Loader2, FileText } from "lucide-react";
import { toast } from "sonner";
import { studentApi } from "@/lib/api/student";
import { pickCurrentSemester } from "@/lib/semester";

export const Route = createFileRoute("/student/tuition")({ component: TuitionPage });

function formatVND(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function TuitionPage() {
  const semestersQuery = useQuery({
    queryKey: ["student", "semesters"],
    queryFn: studentApi.listSemesters,
  });
  const semesters = semestersQuery.data ?? [];
  const [semesterId, setSemesterId] = useState<number | null>(null);

  useEffect(() => {
    if (!semesterId && semesters.length) setSemesterId(pickCurrentSemester(semesters)?.id ?? null);
  }, [semesterId, semesters]);

  const tuitionQuery = useQuery({
    queryKey: ["student", "tuition", semesterId],
    queryFn: () => studentApi.getTuition(semesterId as number),
    enabled: semesterId != null,
  });

  const payMutation = useMutation({
    mutationFn: (id: number) => studentApi.createVNPayUrl(id),
    onSuccess: (url) => {
      window.location.href = url;
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Không tạo được link thanh toán"),
  });

  const tuition = tuitionQuery.data;
  const items = tuition?.items ?? [];
  const courseItems = items.filter((item) => (item.feeType ?? "COURSE") === "COURSE");
  const retakeItems = items.filter((item) => item.feeType === "RETAKE");
  const courseTotal = courseItems.reduce((sum, item) => sum + item.subtotal, 0);
  const retakeTotal = retakeItems.reduce((sum, item) => sum + item.subtotal, 0);

  return (
    <div>
      <PageHeader
        title="Học phí"
        description={tuition?.semesterName ?? "Hóa đơn theo học kỳ"}
        actions={
          <select
            className="h-9 rounded-md border bg-background px-3 text-sm"
            value={semesterId ?? ""}
            onChange={(e) => setSemesterId(Number(e.target.value))}
          >
            {semesters.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        }
      />

      {tuitionQuery.isLoading ? (
        <div className="rounded-lg border bg-card p-6 text-sm text-muted-foreground">
          Đang tải dữ liệu...
        </div>
      ) : tuition ? (
        <div className="max-w-2xl space-y-4">
          <div className="rounded-xl border bg-card p-5 shadow-sm">
            <div className="flex items-start justify-between">
              <div>
                <div className="text-xs text-muted-foreground">Học kỳ</div>
                <h3 className="mt-1 font-semibold">{tuition.semesterName}</h3>
                <div className="mt-1 text-xs text-muted-foreground">
                  {tuition.totalCredits} tín chỉ — {formatVND(tuition.pricePerCredit ?? 0)}/tín chỉ
                </div>
              </div>
              <StatusBadge value={tuition.paid ? "PAID" : "UNPAID"} />
            </div>

            <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
              <div className="rounded-lg bg-muted/40 p-3">
                <div className="text-xs text-muted-foreground">Tổng học phí</div>
                <div className="mt-1 text-lg font-bold tabular-nums">
                  {formatVND(tuition.totalAmount)}
                </div>
              </div>
              <div className="rounded-lg bg-muted/40 p-3">
                <div className="text-xs text-muted-foreground">Còn lại</div>
                <div
                  className={`mt-1 text-lg font-bold tabular-nums ${!tuition.paid ? "text-destructive" : "text-green-600"}`}
                >
                  {formatVND(tuition.paid ? 0 : (tuition.totalAmount - (tuition.paidAmount ?? 0)))}
                </div>
              </div>
              <div className="rounded-lg bg-muted/40 p-3">
                <div className="text-xs text-muted-foreground">Học phần</div>
                <div className="mt-1 font-semibold tabular-nums">{formatVND(courseTotal)}</div>
              </div>
              <div className="rounded-lg bg-muted/40 p-3">
                <div className="text-xs text-muted-foreground">Thi lại / nâng điểm</div>
                <div className="mt-1 font-semibold tabular-nums">{formatVND(retakeTotal)}</div>
              </div>
            </div>

            <div className="mt-4 flex gap-3">
              {items.length > 0 && (
                <Dialog>
                  <DialogTrigger asChild>
                    <Button variant="outline" className="flex-1 gap-2">
                      <FileText className="h-4 w-4" />
                      Xem chi tiết học phí
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="max-w-2xl">
                    <DialogHeader>
                      <DialogTitle>Chi tiết học phí — {tuition.semesterName}</DialogTitle>
                    </DialogHeader>
                    <div className="mt-2">
                      <Table>
                        <TableHeader>
                          <TableRow className="bg-muted/40">
                            <TableHead>Môn học</TableHead>
                            <TableHead>Loại phí</TableHead>
                            <TableHead className="text-center">Tín chỉ</TableHead>
                            <TableHead className="text-right">Đơn giá</TableHead>
                            <TableHead className="text-right">Thành tiền</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {items.map((item, idx) => (
                            <TableRow key={idx}>
                              <TableCell>
                                <div className="font-medium">{item.courseName}</div>
                                <div className="font-mono text-xs text-muted-foreground">
                                  {item.courseCode}
                                </div>
                              </TableCell>
                              <TableCell>
                                <StatusBadge
                                  value={
                                    (item.feeType ?? "COURSE") === "RETAKE"
                                      ? "Thi lại/Nâng điểm"
                                      : "Học phần"
                                  }
                                />
                              </TableCell>
                              <TableCell className="text-center tabular-nums">
                                {item.credits}
                              </TableCell>
                              <TableCell className="text-right tabular-nums text-sm">
                                {formatVND(item.pricePerCredit)}
                              </TableCell>
                              <TableCell className="text-right tabular-nums font-medium">
                                {formatVND(item.subtotal)}
                              </TableCell>
                            </TableRow>
                          ))}
                          <TableRow className="bg-muted/20 font-semibold">
                            <TableCell>Tổng cộng</TableCell>
                            <TableCell />
                            <TableCell className="text-center tabular-nums">
                              {tuition.totalCredits}
                            </TableCell>
                            <TableCell />
                            <TableCell className="text-right tabular-nums text-primary">
                              {formatVND(tuition.totalAmount)}
                            </TableCell>
                          </TableRow>
                        </TableBody>
                      </Table>
                    </div>
                  </DialogContent>
                </Dialog>
              )}
              <Button
                className="flex-1 gap-2"
                disabled={tuition.paid || payMutation.isPending || !semesterId}
                onClick={() => semesterId && payMutation.mutate(semesterId)}
              >
                {payMutation.isPending ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <CreditCard className="h-4 w-4" />
                )}
                {tuition.paid ? "Đã thanh toán" : "Thanh toán qua VNPay"}
              </Button>
            </div>
          </div>
        </div>
      ) : (
        <div className="rounded-lg border bg-card p-6 text-sm text-muted-foreground">
          Chưa có hóa đơn.
        </div>
      )}

      {tuitionQuery.isError && (
        <div className="mt-4 text-sm text-destructive">
          {tuitionQuery.error instanceof Error
            ? tuitionQuery.error.message
            : "Không tải được học phí"}
        </div>
      )}
    </div>
  );
}
