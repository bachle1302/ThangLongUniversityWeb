import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { adminApi } from "@/lib/api/admin";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Download, Search } from "lucide-react";
import { toast } from "sonner";

interface Props {
  semesterId: number;
}

const STATUS_OPTIONS = ["", "PENDING", "REGISTERED", "CANCELED"];
const STATUS_LABEL: Record<string, string> = {
  PENDING: "Chờ xử lý",
  REGISTERED: "Đã xác nhận",
  CANCELED: "Đã hủy",
};

export function EnrollmentsTab({ semesterId }: Props) {
  const [status, setStatus] = useState("");
  const [search, setSearch] = useState("");
  const [page, setPage] = useState(0);

  const query = useQuery({
    queryKey: ["admin", "enrollments", semesterId, status, page],
    queryFn: () =>
      adminApi.listEnrollments({ semesterId, status: status || undefined, page, size: 50 }),
  });

  const data = query.data;
  const items = data?.content ?? [];

  const filteredItems = search
    ? items.filter(
        (e) =>
          e.studentCode?.toLowerCase().includes(search.toLowerCase()) ||
          e.studentName?.toLowerCase().includes(search.toLowerCase()) ||
          e.classCode?.toLowerCase().includes(search.toLowerCase()),
      )
    : items;

  const pendingCount = items.filter((e) => e.status === "PENDING").length;
  const registeredCount = items.filter((e) => e.status === "REGISTERED").length;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <Select
          value={status}
          onValueChange={(v) => {
            setStatus(v === "__all__" ? "" : v);
            setPage(0);
          }}
        >
          <SelectTrigger className="w-40">
            <SelectValue placeholder="Trạng thái" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="__all__">Tất cả</SelectItem>
            {STATUS_OPTIONS.filter(Boolean).map((s) => (
              <SelectItem key={s} value={s}>
                {STATUS_LABEL[s] ?? s}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Tìm kiếm..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-8 h-8 w-48"
            />
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={() =>
              void adminApi
                .exportEnrollments(semesterId)
                .catch((error) =>
                  toast.error(error instanceof Error ? error.message : "Không xuất được Excel"),
                )
            }
          >
            <Download className="h-4 w-4 mr-1" />
            Xuất Excel
          </Button>
        </div>
      </div>

      {/* Summary stats */}
      {!query.isLoading && (
        <div className="flex items-center gap-4 text-sm text-muted-foreground bg-muted/30 rounded-lg px-4 py-2">
          <span>
            Tổng: <span className="font-medium text-foreground">{items.length}</span>
          </span>
          <span className="text-border">|</span>
          <span>
            Chờ xử lý: <span className="font-medium text-amber-600">{pendingCount}</span>
          </span>
          <span className="text-border">|</span>
          <span>
            Đã xác nhận: <span className="font-medium text-green-600">{registeredCount}</span>
          </span>
        </div>
      )}

      {query.isLoading && <Skeleton className="h-64 w-full" />}

      {!query.isLoading && (
        <div className="rounded-lg border overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-muted/50">
              <tr>
                <th className="text-left p-3 font-medium">MSSV</th>
                <th className="text-left p-3 font-medium">Tên sinh viên</th>
                <th className="text-left p-3 font-medium">Lớp HP</th>
                <th className="text-left p-3 font-medium">Môn học</th>
                <th className="text-left p-3 font-medium">Tín chỉ</th>
                <th className="text-left p-3 font-medium">Trạng thái</th>
                <th className="text-left p-3 font-medium">Đợt đăng ký</th>
              </tr>
            </thead>
            <tbody>
              {filteredItems.map((e) => (
                <tr key={e.enrollmentId} className="border-t hover:bg-muted/30">
                  <td className="p-3 font-mono text-xs">{e.studentCode}</td>
                  <td className="p-3">{e.studentName}</td>
                  <td className="p-3 font-mono text-xs">{e.classCode}</td>
                  <td className="p-3">{e.courseName}</td>
                  <td className="p-3 text-center">{e.credits ?? "—"}</td>
                  <td className="p-3">
                    <EnrollmentStatusBadge status={e.status} />
                  </td>
                  <td className="p-3 text-xs text-muted-foreground">
                    <div className="font-medium text-foreground">{e.registrationRoundName || "—"}</div>
                    {e.enrolledAt && (
                      <div className="text-[10px] text-muted-foreground mt-0.5">
                        {new Intl.DateTimeFormat("vi-VN", {
                          dateStyle: "short",
                          timeStyle: "short",
                        }).format(new Date(e.enrolledAt))}
                      </div>
                    )}
                  </td>
                </tr>
              ))}
              {filteredItems.length === 0 && (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-muted-foreground">
                    Không có dữ liệu
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {data && data.totalPages > 1 && (
        <div className="flex justify-center gap-2">
          <Button
            variant="outline"
            size="sm"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            ← Trước
          </Button>
          <span className="text-sm self-center">
            Trang {page + 1}/{data.totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            disabled={page >= data.totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
          >
            Sau →
          </Button>
        </div>
      )}
    </div>
  );
}

function EnrollmentStatusBadge({ status }: { status: string }) {
  if (status === "REGISTERED")
    return <Badge className="bg-green-100 text-green-800 hover:bg-green-100">Đã xác nhận</Badge>;
  if (status === "PENDING")
    return <Badge className="bg-yellow-100 text-yellow-800 hover:bg-yellow-100">Chờ xử lý</Badge>;
  if (status === "CANCELED") return <Badge variant="secondary">Đã hủy</Badge>;
  return <Badge variant="outline">{status}</Badge>;
}
