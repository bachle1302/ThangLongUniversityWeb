import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { PageHeader } from "@/components/ui/page-header";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { studentApi } from "@/lib/api/student";
import { Search, BookOpen } from "lucide-react";
import type { CourseResponse } from "@/lib/api/types";

export const Route = createFileRoute("/student/curriculum")({ component: CurriculumPage });

const emptyCourses: CourseResponse[] = [];

function CurriculumPage() {
  const [search, setSearch] = useState("");
  const [major, setMajor] = useState("all");
  const [credits, setCredits] = useState("all");
  const [courseType, setCourseType] = useState("all");

  const query = useQuery({
    queryKey: ["student", "curriculum", "my-major"],
    queryFn: studentApi.getMyMajorCurriculum,
  });

  const courses = query.data ?? emptyCourses;
  const majorOptions = useMemo(
    () => Array.from(new Set(courses.map((c) => c.majorName ?? "Môn học chung"))).sort(),
    [courses],
  );
  const creditOptions = useMemo(
    () =>
      Array.from(new Set(courses.map((c) => c.credits).filter((c): c is number => c != null))).sort(
        (a, b) => a - b,
      ),
    [courses],
  );
  const filtered = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    return courses.filter((c) => {
      const matchesSearch =
        !keyword ||
        c.name.toLowerCase().includes(keyword) ||
        c.code.toLowerCase().includes(keyword);
      const matchesMajor = major === "all" || (c.majorName ?? "Môn học chung") === major;
      const matchesCredits = credits === "all" || String(c.credits) === credits;
      const matchesType = courseType === "all" || (c.courseType ?? "REQUIRED") === courseType;
      return matchesSearch && matchesMajor && matchesCredits && matchesType;
    });
  }, [courses, search, major, credits, courseType]);

  const grouped = useMemo(() => {
    const map = new Map<string, typeof filtered>();
    filtered.forEach((c) => {
      const key = c.majorName ?? "Môn học chung";
      const coursesByMajor = map.get(key) ?? [];
      coursesByMajor.push(c);
      map.set(key, coursesByMajor);
    });
    return Array.from(map.entries());
  }, [filtered]);

  return (
    <div>
      <PageHeader title="Chương trình đào tạo" description="Danh sách môn học trong chương trình" />

      <div className="mb-5 flex flex-col items-center gap-3">
        <div className="relative w-full max-w-md">
          <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
          <Input
            className="h-10 pl-8"
            placeholder="Tìm môn học..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="grid w-full max-w-3xl gap-2 sm:grid-cols-3">
          <Select value={major} onValueChange={setMajor}>
            <SelectTrigger>
              <SelectValue placeholder="Ngành" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Tất cả ngành</SelectItem>
              {majorOptions.map((name) => (
                <SelectItem key={name} value={name}>
                  {name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={credits} onValueChange={setCredits}>
            <SelectTrigger>
              <SelectValue placeholder="Tín chỉ" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Tất cả tín chỉ</SelectItem>
              {creditOptions.map((value) => (
                <SelectItem key={value} value={String(value)}>
                  {value} tín chỉ
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={courseType} onValueChange={setCourseType}>
            <SelectTrigger>
              <SelectValue placeholder="Loại môn" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Tất cả loại môn</SelectItem>
              <SelectItem value="REQUIRED">Tín chỉ bắt buộc</SelectItem>
              <SelectItem value="ELECTIVE">Tín chỉ tự chọn</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {query.isLoading ? (
        <div className="rounded-xl border bg-card p-8 text-center text-sm text-muted-foreground">
          Đang tải chương trình đào tạo...
        </div>
      ) : grouped.length === 0 ? (
        <div className="rounded-xl border bg-card p-8 text-center text-sm text-muted-foreground">
          {search ? "Không tìm thấy môn học phù hợp." : "Chưa có môn học trong chương trình."}
        </div>
      ) : (
        <div className="space-y-4">
          {grouped.map(([groupName, groupCourses]) => {
            return (
              <div key={groupName} className="overflow-hidden rounded-xl border bg-card shadow-sm">
                <div className="flex items-center border-b bg-muted/40 px-4 py-3">
                  <div className="flex items-center gap-2">
                    <BookOpen className="h-4 w-4 text-muted-foreground" />
                    <span className="text-sm font-semibold">{groupName}</span>
                  </div>
                </div>
                <Table>
                  <TableHeader>
                    <TableRow className="bg-muted/20">
                      <TableHead>Mã môn</TableHead>
                      <TableHead>Tên môn học</TableHead>
                      <TableHead className="text-center">Tín chỉ</TableHead>
                      <TableHead className="hidden lg:table-cell">Loại môn</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {groupCourses.map((course) => (
                      <TableRow key={course.id}>
                        <TableCell className="font-mono text-xs font-medium">
                          {course.code}
                        </TableCell>
                        <TableCell>
                          <div className="font-medium">{course.name}</div>
                          {course.description && (
                            <div className="mt-0.5 line-clamp-1 text-xs text-muted-foreground">
                              {course.description}
                            </div>
                          )}
                        </TableCell>
                        <TableCell className="text-center">
                          <span className="rounded-full bg-primary/10 px-2.5 py-0.5 text-xs font-semibold text-primary">
                            {course.credits}
                          </span>
                        </TableCell>
                        <TableCell className="hidden lg:table-cell">
                          <span
                            className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                              course.courseType === "ELECTIVE"
                                ? "bg-blue-50 text-blue-700"
                                : "bg-emerald-50 text-emerald-700"
                            }`}
                          >
                            {course.courseTypeLabel ??
                              (course.courseType === "ELECTIVE" ? "Tự do" : "Bắt buộc")}
                          </span>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            );
          })}
        </div>
      )}

      {query.isError && (
        <div className="mt-4 text-sm text-destructive">
          {query.error instanceof Error
            ? query.error.message
            : "Không tải được chương trình đào tạo"}
        </div>
      )}
    </div>
  );
}
