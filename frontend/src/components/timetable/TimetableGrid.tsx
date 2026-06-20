import type { ReactNode } from "react";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

export type TimetableCell = {
  rowSpan: number;
  isStart: boolean;
};

type Period = {
  index: number;
  start: string;
  end: string;
};

type TimetableGridProps<TCell extends TimetableCell> = {
  cells: Record<string, TCell | null>;
  renderCell: (cell: TCell) => ReactNode;
};

const dayLabels: Record<number, string> = {
  2: "Thứ 2",
  3: "Thứ 3",
  4: "Thứ 4",
  5: "Thứ 5",
  6: "Thứ 6",
  7: "Thứ 7",
  8: "CN",
};

const periods: Period[] = [
  { index: 1, start: "07:00", end: "07:50" },
  { index: 2, start: "08:00", end: "08:50" },
  { index: 3, start: "09:00", end: "09:50" },
  { index: 4, start: "10:00", end: "10:50" },
  { index: 5, start: "13:00", end: "13:50" },
  { index: 6, start: "14:00", end: "14:50" },
  { index: 7, start: "15:00", end: "15:50" },
  { index: 8, start: "16:00", end: "16:50" },
];

const days = [2, 3, 4, 5, 6, 7, 8];

function getTodayDayOfWeek() {
  const day = new Date().getDay();
  return day === 0 ? 8 : day + 1;
}

export function TimetableGrid<TCell extends TimetableCell>({
  cells,
  renderCell,
}: TimetableGridProps<TCell>) {
  const todayDayOfWeek = getTodayDayOfWeek();

  return (
    <div className="overflow-x-auto rounded-xl border bg-card shadow-sm">
      <table className="w-full min-w-[900px] table-fixed border-collapse text-sm">
        <colgroup>
          <col style={{ width: 104 }} />
          {days.map((day) => (
            <col key={day} style={{ width: "calc((100% - 104px) / 7)" }} />
          ))}
        </colgroup>
        <thead>
          <tr className="bg-muted/40">
            <th className="border-b p-2 text-left text-xs uppercase tracking-wide text-muted-foreground">
              Tiết
            </th>
            {days.map((day) => (
              <th
                key={day}
                className={cn(
                  "border-b border-l p-2 text-left text-xs uppercase tracking-wide text-muted-foreground",
                  day === todayDayOfWeek && "bg-primary/10 text-primary",
                )}
              >
                <div className="flex min-h-10 flex-col items-center justify-center gap-1 text-center sm:min-h-0 sm:flex-row sm:justify-center sm:gap-2">
                  <span className="leading-none">{dayLabels[day]}</span>
                  {day === todayDayOfWeek && (
                    <Badge className="h-5 shrink-0 px-2 text-[10px] leading-none">Hôm nay</Badge>
                  )}
                </div>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {periods.map((period) => (
            <tr key={period.index}>
              <td className="border-b p-2 align-top">
                <div className="font-semibold">Tiết {period.index}</div>
                <div className="text-[10px] tabular-nums text-muted-foreground">
                  {period.start}-{period.end}
                </div>
              </td>
              {days.map((day) => {
                const cell = cells[`${day}-${period.index}`];
                if (cell && !cell.isStart) return null;

                return (
                  <td
                    key={day}
                    rowSpan={cell?.rowSpan ?? 1}
                    className={cn(
                      "h-20 border-b border-l p-1.5 align-top",
                      day === todayDayOfWeek && "bg-primary/[0.03]",
                      cell && "bg-primary/5",
                    )}
                  >
                    {cell ? renderCell(cell) : null}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
