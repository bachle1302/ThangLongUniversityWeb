import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "@tanstack/react-router";
import { useState, type ReactNode } from "react";
import { toast } from "sonner";
import { Ban, Pencil, Plus, Search, SlidersHorizontal, Trash2, Users } from "lucide-react";
import { adminApi } from "@/lib/api/admin";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { Input } from "@/components/ui/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Skeleton } from "@/components/ui/skeleton";
import { ClassSectionFormDialog } from "@/features/admin-class-sections/ClassSectionFormDialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ClassSectionStudentsDialog } from "@/features/admin-class-sections/ClassSectionStudentsDialog";
import {
  buildOptionSets,
  mapApiClassSection,
  toClassSectionRequest,
} from "@/features/admin-class-sections/classSectionMappers";
import type { ClassSectionScheduleResponse } from "@/lib/api/types";
import type {
  ClassSectionFormValues,
  ClassSectionRow,
} from "@/features/admin-class-sections/types";

interface Props {
  semesterId: number;
}

export function ClassSectionsTab({ semesterId }: Props) {
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [courseFilter, setCourseFilter] = useState("all");
  const [teacherFilter, setTeacherFilter] = useState("all");
  const [roomFilter, setRoomFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");
  const [capacityFilter, setCapacityFilter] = useState("all");
  const [examScheduleFilter, setExamScheduleFilter] = useState("all");
  const [formOpen, setFormOpen] = useState(false);
  const [editing, setEditing] = useState<ClassSectionRow | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<ClassSectionRow | null>(null);
  const [studentsSection, setStudentsSection] = useState<ClassSectionRow | null>(null);

  const classSectionsQuery = useQuery({
    queryKey: ["admin", "class-sections", "semester", semesterId],
    queryFn: () => adminApi.listClassSectionsBySemester(semesterId),
  });
  const coursesQuery = useQuery({
    queryKey: ["admin", "courses"],
    queryFn: adminApi.listCourses,
    staleTime: 300_000,
  });
  const semestersQuery = useQuery({
    queryKey: ["admin", "semesters"],
    queryFn: adminApi.listSemesters,
    staleTime: 300_000,
  });
  const teachersQuery = useQuery({
    queryKey: ["admin", "teachers"],
    queryFn: adminApi.listTeachers,
    staleTime: 300_000,
  });
  const roomsQuery = useQuery({
    queryKey: ["admin", "rooms"],
    queryFn: adminApi.listRooms,
    staleTime: 3_600_000,
  });
  const periodsQuery = useQuery({
    queryKey: ["admin", "periods"],
    queryFn: adminApi.listPeriods,
    staleTime: 3_600_000,
  });

  const allSections = classSectionsQuery.data ?? [];
  const rows = allSections.map((section) => mapApiClassSection(section));
  const options = buildOptionSets(
    {
      courses: coursesQuery.data,
      semesters: (semestersQuery.data ?? []).filter((semester) => semester.id === semesterId),
      teachers: teachersQuery.data,
      rooms: roomsQuery.data,
      periods: periodsQuery.data,
      classSections: allSections,
    },
    rows,
  );

  const invalidate = () => {
    queryClient.invalidateQueries({
      queryKey: ["admin", "class-sections", "semester", semesterId],
    });
    queryClient.invalidateQueries({ queryKey: ["admin", "exam-schedules", semesterId] });
    queryClient.invalidateQueries({ queryKey: ["admin", "semester-summary", semesterId] });
    queryClient.invalidateQueries({ queryKey: ["admin", "semesters"] });
  };

  const createMutation = useMutation({
    mutationFn: (values: ClassSectionFormValues) =>
      adminApi.createClassSection(toClassSectionRequest({ ...values, semesterId })),
    onSuccess: () => {
      invalidate();
      toast.success("Đã tạo lớp học phần");
      closeForm();
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Không tạo được lớp học phần"),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, values }: { id: number; values: ClassSectionFormValues }) =>
      adminApi.updateClassSection(id, toClassSectionRequest({ ...values, semesterId })),
    onSuccess: () => {
      invalidate();
      toast.success("Đã cập nhật lớp học phần");
      closeForm();
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Không cập nhật được lớp học phần"),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteClassSection(id),
    onSuccess: () => {
      invalidate();
      toast.success("Đã xóa lớp học phần");
      setDeleteTarget(null);
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Không xóa được lớp học phần"),
  });

  const cancelMutation = useMutation({
    mutationFn: (id: number) => adminApi.cancelClassSection(id),
    onSuccess: () => {
      invalidate();
      toast.success("Đã hủy lớp học phần");
      setDeleteTarget(null);
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Không hủy được lớp học phần"),
  });

  if (classSectionsQuery.isLoading) return <Skeleton className="h-64 w-full" />;
  if (classSectionsQuery.isError) {
    return <div className="text-sm text-destructive">{String(classSectionsQuery.error)}</div>;
  }

  const sections = allSections.filter((section) => {
    const keyword = search.trim().toLowerCase();
    const row = mapApiClassSection(section);
    const currentSlots = section.currentSlots ?? 0;
    const maxSlots = section.maxSlots ?? 0;
    const fillRate = maxSlots > 0 ? currentSlots / maxSlots : 0;

    const matchesKeyword =
      !keyword ||
      section.classCode?.toLowerCase().includes(keyword) ||
      section.courseName?.toLowerCase().includes(keyword) ||
      section.teacherName?.toLowerCase().includes(keyword) ||
      section.room?.toLowerCase().includes(keyword) ||
      section.schedules?.some((schedule) => schedule.roomName?.toLowerCase().includes(keyword));
    const matchesCourse = courseFilter === "all" || String(section.courseId) === courseFilter;
    const matchesTeacher = teacherFilter === "all" || String(section.teacherId ?? 0) === teacherFilter;
    const matchesRoom =
      roomFilter === "all" ||
      section.schedules?.some((schedule) => String(schedule.roomId ?? 0) === roomFilter) ||
      String(section.roomId ?? 0) === roomFilter;
    const matchesStatus = statusFilter === "all" || row.status === statusFilter;
    const matchesCapacity =
      capacityFilter === "all" ||
      (capacityFilter === "empty" && currentSlots === 0) ||
      (capacityFilter === "available" && currentSlots > 0 && currentSlots < maxSlots && fillRate < 0.8) ||
      (capacityFilter === "almost-full" && currentSlots < maxSlots && fillRate >= 0.8) ||
      (capacityFilter === "full" && maxSlots > 0 && currentSlots >= maxSlots);
    const matchesExamSchedule =
      examScheduleFilter === "all" ||
      (examScheduleFilter === "scheduled" && Boolean(section.examAt)) ||
      (examScheduleFilter === "unscheduled" && !section.examAt);

    return (
      matchesKeyword &&
      matchesCourse &&
      matchesTeacher &&
      matchesRoom &&
      matchesStatus &&
      matchesCapacity &&
      matchesExamSchedule
    );
  });
  const activeFilterCount = [
    courseFilter,
    teacherFilter,
    roomFilter,
    statusFilter,
    capacityFilter,
    examScheduleFilter,
  ].filter((value) => value !== "all").length;

  const resetFilters = () => {
    setCourseFilter("all");
    setTeacherFilter("all");
    setRoomFilter("all");
    setStatusFilter("all");
    setCapacityFilter("all");
    setExamScheduleFilter("all");
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-muted-foreground">
          Hiển thị {sections.length}/{allSections.length} lớp học phần
        </p>
        <div className="flex flex-wrap items-center justify-end gap-2">
          <div className="relative w-full sm:w-80">
            <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Tìm mã lớp, môn, giảng viên, phòng"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              className="h-9 pl-8"
            />
          </div>
          <Popover>
            <PopoverTrigger asChild>
              <Button variant="outline" size="sm" className="gap-2">
                <SlidersHorizontal className="h-4 w-4" />
                Bộ lọc
                {activeFilterCount > 0 && (
                  <Badge className="ml-1 h-5 min-w-5 justify-center rounded-full px-1.5 text-[11px]">
                    {activeFilterCount}
                  </Badge>
                )}
              </Button>
            </PopoverTrigger>
            <PopoverContent align="end" className="w-[min(92vw,560px)]">
              <div className="grid gap-3 sm:grid-cols-2">
                <FilterSelect value={courseFilter} onValueChange={setCourseFilter} placeholder="Môn học">
                  {options.courses.map((course) => (
                    <SelectItem key={course.id} value={String(course.id)}>
                      {course.code} - {course.name}
                    </SelectItem>
                  ))}
                </FilterSelect>
                <FilterSelect value={teacherFilter} onValueChange={setTeacherFilter} placeholder="Giảng viên">
                  {options.teachers.map((teacher) => (
                    <SelectItem key={teacher.id} value={String(teacher.id)}>
                      {teacher.name}
                    </SelectItem>
                  ))}
                </FilterSelect>
                <FilterSelect value={roomFilter} onValueChange={setRoomFilter} placeholder="Phòng học">
                  {options.rooms.map((room) => (
                    <SelectItem key={room.id} value={String(room.id)}>
                      {room.name}
                    </SelectItem>
                  ))}
                </FilterSelect>
                <FilterSelect value={statusFilter} onValueChange={setStatusFilter} placeholder="Trạng thái">
                  <SelectItem value="DRAFT">Nháp</SelectItem>
                  <SelectItem value="OPEN">Đang mở</SelectItem>
                  <SelectItem value="CLOSED">Đã đóng</SelectItem>
                  <SelectItem value="CANCELLED">Đã hủy</SelectItem>
                </FilterSelect>
                <FilterSelect value={capacityFilter} onValueChange={setCapacityFilter} placeholder="Tình trạng sĩ số">
                  <SelectItem value="empty">Chưa có sinh viên</SelectItem>
                  <SelectItem value="available">Còn chỗ</SelectItem>
                  <SelectItem value="almost-full">Gần đầy</SelectItem>
                  <SelectItem value="full">Đã đầy</SelectItem>
                </FilterSelect>
                <FilterSelect value={examScheduleFilter} onValueChange={setExamScheduleFilter} placeholder="Lịch thi">
                  <SelectItem value="scheduled">Đã có lịch thi</SelectItem>
                  <SelectItem value="unscheduled">Chưa có lịch thi</SelectItem>
                </FilterSelect>
              </div>
              <div className="mt-3 flex items-center justify-between gap-2 border-t pt-3">
                <span className="text-xs text-muted-foreground">
                  {activeFilterCount > 0 ? `${activeFilterCount} bộ lọc đang bật` : "Chưa bật bộ lọc"}
                </span>
                <Button variant="ghost" size="sm" onClick={resetFilters} disabled={activeFilterCount === 0}>
                  Xóa lọc
                </Button>
              </div>
            </PopoverContent>
          </Popover>
          <Link to="/admin/semesters/$id/classes/create" params={{ id: String(semesterId) }}>
            <Button size="sm">
              <Plus className="mr-1 h-4 w-4" />
              Tạo lớp học phần
            </Button>
          </Link>
        </div>
      </div>

      <div className="overflow-hidden rounded-lg border">
        <table className="w-full text-sm">
          <thead className="bg-muted/50">
            <tr>
              <th className="p-3 text-left font-medium">Mã lớp</th>
              <th className="p-3 text-left font-medium">Môn học</th>
              <th className="p-3 text-left font-medium">Giảng viên</th>
              <th className="p-3 text-left font-medium">Phòng</th>
              <th className="p-3 text-left font-medium">Sĩ số</th>
              <th className="p-3 text-left font-medium">Trạng thái</th>
              <th className="p-3" />
            </tr>
          </thead>
          <tbody>
            {sections.map((section) => {
              const row = mapApiClassSection(section);
              return (
                <tr key={section.id} className="border-t hover:bg-muted/30">
                  <td className="p-3 font-mono text-xs">{section.classCode}</td>
                  <td className="p-3">{section.courseName}</td>
                  <td className="p-3">{section.teacherName ?? "-"}</td>
                  <td className="p-3">
                    <ScheduleSummary schedules={section.schedules} fallbackRoom={section.room} />
                  </td>
                  <td className="p-3">
                    {section.currentSlots ?? 0}/{section.maxSlots ?? "-"}
                  </td>
                  <td className="p-3">
                    <ClassSectionStatusBadge status={row.status} />
                  </td>
                  <td className="p-3">
                    <div className="flex justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => setStudentsSection(row)}
                      >
                        <Users className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={() => {
                          setEditing(row);
                          setFormOpen(true);
                        }}
                      >
                        <Pencil className="h-4 w-4" />
                      </Button>
                      {row.status !== "CANCELLED" && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-8 w-8 text-destructive"
                          onClick={() => setDeleteTarget(row)}
                        >
                          {row.currentSlots > 0 ? (
                            <Ban className="h-4 w-4" />
                          ) : (
                            <Trash2 className="h-4 w-4" />
                          )}
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              );
            })}
            {sections.length === 0 && (
              <tr>
                <td colSpan={7} className="p-8 text-center text-muted-foreground">
                  Chưa có lớp học phần
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <ClassSectionFormDialog
        open={formOpen}
        editing={editing}
        options={options}
        isPending={createMutation.isPending || updateMutation.isPending}
        onOpenChange={(open) => {
          if (!open) closeForm();
          else setFormOpen(true);
        }}
        onSubmit={(values) => {
          if (editing?.numericId) updateMutation.mutate({ id: editing.numericId, values });
          else createMutation.mutate(values);
        }}
      />

      <ClassSectionStudentsDialog
        open={!!studentsSection}
        section={studentsSection}
        onOpenChange={(open) => {
          if (!open) setStudentsSection(null);
        }}
      />

      <ConfirmDialog
        open={!!deleteTarget}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
        title={deleteTarget && deleteTarget.currentSlots > 0 ? "Hủy lớp học phần?" : "Xóa lớp học phần?"}
        description={
          deleteTarget && deleteTarget.currentSlots > 0
            ? `${deleteTarget.classCode} đã có ${deleteTarget.currentSlots} sinh viên, hệ thống sẽ hủy lớp và chuyển enrollment active sang CANCELED.`
            : `${deleteTarget?.classCode ?? ""} chỉ xóa được nếu chưa có tham chiếu bởi đăng ký, điểm, lịch học hoặc bảng liên quan.`
        }
        confirmText={deleteTarget && deleteTarget.currentSlots > 0 ? "Hủy lớp" : "Xóa"}
        destructive
        onConfirm={() => {
          if (!deleteTarget?.numericId) return;
          if (deleteTarget.currentSlots > 0) cancelMutation.mutate(deleteTarget.numericId);
          else deleteMutation.mutate(deleteTarget.numericId);
        }}
      />
    </div>
  );

  function closeForm() {
    setFormOpen(false);
    setEditing(null);
  }
}

function FilterSelect({
  value,
  onValueChange,
  placeholder,
  children,
}: {
  value: string;
  onValueChange: (value: string) => void;
  placeholder: string;
  children: ReactNode;
}) {
  return (
    <Select value={value} onValueChange={onValueChange}>
      <SelectTrigger className="h-9">
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>
      <SelectContent>
        <SelectItem value="all">Tất cả</SelectItem>
        {children}
      </SelectContent>
    </Select>
  );
}

function ScheduleSummary({
  schedules,
  fallbackRoom,
}: {
  schedules?: ClassSectionScheduleResponse[];
  fallbackRoom?: string | null;
}) {
  if (!schedules?.length) {
    return <span>{fallbackRoom ?? "-"}</span>;
  }

  return (
    <div className="space-y-1">
      {schedules.map((schedule, index) => (
        <div key={schedule.id ?? index} className="leading-tight">
          <div>{schedule.roomName ?? fallbackRoom ?? "-"}</div>
          <div className="text-xs text-muted-foreground">
            {formatDay(schedule.dayOfWeek)}, tiết {schedule.startPeriod}-{schedule.endPeriod}
          </div>
        </div>
      ))}
    </div>
  );
}

function ClassSectionStatusBadge({ status }: { status: string }) {
  if (status === "OPEN") return <Badge className="bg-emerald-100 text-emerald-800 hover:bg-emerald-100">Đang mở</Badge>;
  if (status === "CLOSED") return <Badge variant="secondary">Đã đóng</Badge>;
  if (status === "CANCELLED") return <Badge variant="destructive">Đã hủy</Badge>;
  if (status === "DRAFT") return <Badge variant="outline">Nháp</Badge>;
  return <Badge variant="outline">{status}</Badge>;
}

function formatDay(dayOfWeek: number) {
  if (dayOfWeek === 8) return "Chủ nhật";
  return `Thứ ${dayOfWeek}`;
}
