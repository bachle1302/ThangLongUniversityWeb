import { createFileRoute, Link } from "@tanstack/react-router";
import { AlertTriangle, ArrowLeft, CheckCircle2, ReceiptText, RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { PageHeader } from "@/components/ui/page-header";

type PaymentSearch = {
  status?: "success" | "failed";
  message?: string;
  transactionNo?: string;
  txnRef?: string;
  responseCode?: string;
  transactionStatus?: string;
  amount?: string;
  bankCode?: string;
};

export const Route = createFileRoute("/student/payment-result")({
  validateSearch: (search: Record<string, unknown>): PaymentSearch => ({
    status: search.status === "success" ? "success" : "failed",
    message: typeof search.message === "string" ? search.message : undefined,
    transactionNo: typeof search.transactionNo === "string" ? search.transactionNo : undefined,
    txnRef: typeof search.txnRef === "string" ? search.txnRef : undefined,
    responseCode: typeof search.responseCode === "string" ? search.responseCode : undefined,
    transactionStatus:
      typeof search.transactionStatus === "string" ? search.transactionStatus : undefined,
    amount: typeof search.amount === "string" ? search.amount : undefined,
    bankCode: typeof search.bankCode === "string" ? search.bankCode : undefined,
  }),
  component: PaymentResultPage,
});

function formatVnpAmount(amount?: string) {
  if (!amount) return undefined;
  const value = Number(amount);
  if (!Number.isFinite(value)) return undefined;
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value / 100);
}

function PaymentResultPage() {
  const search = Route.useSearch();
  const isSuccess = search.status === "success";
  const amount = formatVnpAmount(search.amount);
  const Icon = isSuccess ? CheckCircle2 : AlertTriangle;

  const details = [
    ["Mã giao dịch", search.transactionNo],
    ["Mã đơn hàng", search.txnRef],
    ["Ngân hàng", search.bankCode],
    ["Số tiền", amount],
    ["Mã phản hồi", search.responseCode],
    ["Trạng thái VNPay", search.transactionStatus],
  ].filter(([, value]) => Boolean(value));

  return (
    <div>
      <PageHeader
        title="Kết quả thanh toán"
        description="Thông tin phản hồi từ cổng thanh toán VNPay"
      />

      <div className="mx-auto max-w-2xl">
        <Card className="overflow-hidden shadow-sm">
          <div className={isSuccess ? "h-1.5 bg-green-600" : "h-1.5 bg-destructive"} />
          <div className="p-6 sm:p-8">
            <div className="flex flex-col items-center text-center">
              <div
                className={
                  isSuccess
                    ? "flex h-16 w-16 items-center justify-center rounded-full bg-green-50 text-green-700 dark:bg-green-950/30"
                    : "flex h-16 w-16 items-center justify-center rounded-full bg-red-50 text-destructive dark:bg-red-950/30"
                }
              >
                <Icon className="h-9 w-9" />
              </div>

              <h2 className="mt-5 text-2xl font-semibold tracking-normal">
                {isSuccess ? "Thanh toán thành công" : "Thanh toán chưa hoàn tất"}
              </h2>
              <p className="mt-2 max-w-lg text-sm text-muted-foreground">
                {search.message ??
                  (isSuccess
                    ? "Hóa đơn học phí đã được ghi nhận thanh toán."
                    : "Giao dịch bị hủy, thất bại hoặc không thể xác thực. Vui lòng kiểm tra lại và thử thanh toán lại nếu cần.")}
              </p>
            </div>

            {details.length > 0 && (
              <div className="mt-6 rounded-lg border bg-muted/20">
                <div className="flex items-center gap-2 border-b px-4 py-3 text-sm font-medium">
                  <ReceiptText className="h-4 w-4 text-muted-foreground" />
                  Chi tiết giao dịch
                </div>
                <dl className="divide-y">
                  {details.map(([label, value]) => (
                    <div key={label} className="grid grid-cols-[130px_1fr] gap-3 px-4 py-3 text-sm">
                      <dt className="text-muted-foreground">{label}</dt>
                      <dd className="min-w-0 break-words font-medium tabular-nums">{value}</dd>
                    </div>
                  ))}
                </dl>
              </div>
            )}

            <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:justify-center">
              <Button asChild className="gap-2">
                <Link to="/student/tuition">
                  {isSuccess ? (
                    <ArrowLeft className="h-4 w-4" />
                  ) : (
                    <RotateCcw className="h-4 w-4" />
                  )}
                  {isSuccess ? "Về trang học phí" : "Thử thanh toán lại"}
                </Link>
              </Button>
              <Button asChild variant="outline" className="gap-2">
                <Link to="/student/dashboard">Về dashboard</Link>
              </Button>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
