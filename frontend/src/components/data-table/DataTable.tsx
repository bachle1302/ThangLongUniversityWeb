import { useMemo, useState, type ReactNode } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ChevronLeft, ChevronRight, Search } from "lucide-react";
import { cn } from "@/lib/utils";

export interface Column<T> {
  key: string;
  header: string;
  render?: (row: T) => ReactNode;
  className?: string;
  searchable?: boolean;
  accessor?: (row: T) => string | number;
}

interface Props<T> {
  data: T[];
  columns: Column<T>[];
  pageSize?: number;
  searchPlaceholder?: string;
  searchSlot?: ReactNode;
  filterRow?: ReactNode;
  emptyMessage?: string;
  toolbar?: ReactNode;
  rowKey: (row: T) => string;
  onRowClick?: (row: T) => void;
}

export function DataTable<T>({
  data,
  columns,
  pageSize = 8,
  searchPlaceholder = "Tìm kiếm…",
  searchSlot,
  filterRow,
  emptyMessage = "Không có dữ liệu",
  toolbar,
  rowKey,
  onRowClick,
}: Props<T>) {
  const [q, setQ] = useState("");
  const [page, setPage] = useState(1);

  const filtered = useMemo(() => {
    if (!q.trim()) return data;
    const term = q.toLowerCase();
    return data.filter((row) =>
      columns.some((c) => {
        if (c.searchable === false) return false;
        const v = c.accessor ? c.accessor(row) : (row as any)[c.key];
        return v != null && String(v).toLowerCase().includes(term);
      }),
    );
  }, [q, data, columns]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const safePage = Math.min(page, totalPages);
  const slice = filtered.slice((safePage - 1) * pageSize, safePage * pageSize);

  return (
    <div className="rounded-xl border bg-card shadow-sm">
      <div className="flex flex-col gap-3 border-b p-4 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex w-full flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-end lg:flex-1">
          <div className="relative w-full sm:min-w-xs sm:max-w-xs sm:self-end">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={q}
              onChange={(e) => {
                setQ(e.target.value);
                setPage(1);
              }}
              placeholder={searchPlaceholder}
              className="pl-9"
            />
          </div>
          {searchSlot && (
            <div className="flex w-full flex-col gap-2 sm:w-auto sm:flex-row sm:flex-wrap sm:items-end">
              {searchSlot}
            </div>
          )}
          {filterRow && (
            <div className="flex w-full flex-col gap-3 sm:w-auto sm:flex-row sm:flex-wrap sm:items-end">
              {filterRow}
            </div>
          )}
        </div>
        {toolbar && (
          <div className="flex flex-wrap items-center gap-2 lg:justify-end">{toolbar}</div>
        )}
      </div>

      <div className="overflow-x-auto">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/40 hover:bg-muted/40">
              {columns.map((c) => (
                <TableHead
                  key={c.key}
                  className={cn(
                    "text-xs uppercase tracking-wide text-muted-foreground",
                    c.className,
                  )}
                >
                  {c.header}
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {slice.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  className="py-16 text-center text-sm text-muted-foreground"
                >
                  {emptyMessage}
                </TableCell>
              </TableRow>
            ) : (
              slice.map((row) => (
                <TableRow
                  key={rowKey(row)}
                  className={cn(onRowClick && "cursor-pointer")}
                  onClick={() => onRowClick?.(row)}
                >
                  {columns.map((c) => (
                    <TableCell key={c.key} className={c.className}>
                      {c.render ? c.render(row) : (row as any)[c.key]}
                    </TableCell>
                  ))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <div className="flex items-center justify-between border-t px-4 py-3 text-sm text-muted-foreground">
        <div>
          Hiển thị {slice.length === 0 ? 0 : (safePage - 1) * pageSize + 1}–
          {(safePage - 1) * pageSize + slice.length} / {filtered.length}
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            disabled={safePage === 1}
            onClick={() => setPage((p) => Math.max(1, p - 1))}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <span className="tabular-nums">
            {safePage} / {totalPages}
          </span>
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            disabled={safePage === totalPages}
            onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
