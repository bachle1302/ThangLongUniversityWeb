import { useQuery } from "@tanstack/react-query";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { adminApi } from "@/lib/api/admin";

export function SemesterSelect({
  value,
  onChange,
  className,
}: {
  value: string;
  onChange: (v: string) => void;
  className?: string;
}) {
  const query = useQuery({
    queryKey: ["admin", "semesters"],
    queryFn: adminApi.listSemesters,
  });

  return (
    <Select value={value} onValueChange={onChange} disabled={query.isLoading || query.isError}>
      <SelectTrigger className={className ?? "w-[260px]"}>
        <SelectValue placeholder={query.isError ? "Không tải được học kỳ" : "Chọn học kỳ"} />
      </SelectTrigger>
      <SelectContent>
        {(query.data ?? []).map((semester) => (
          <SelectItem key={semester.id} value={String(semester.id)}>
            {semester.name}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}
