import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AlertCircle, Plus } from "lucide-react";
import { lazy, Suspense, useMemo, useState } from "react";
import { toast } from "sonner";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { PageHeader } from "@/components/ui/page-header";
import { Skeleton } from "@/components/ui/skeleton";
import { adminApi } from "@/lib/api/admin";
import { buildOptionSets, mapApiClassSection, toClassSectionRequest } from "./classSectionMappers";
import { ClassSectionFormDialog } from "./ClassSectionFormDialog";
import { ClassSectionsByMajor } from "./ClassSectionsByMajor";
import type { ClassSectionFormValues, ClassSectionRow } from "./types";
import { validateClassSectionPlan } from "./validation";

const classSectionsKey = ["admin", "class-sections"] as const;
const ClassSectionStudentsDialog = lazy(() =>
  import("./ClassSectionStudentsDialog").then((module) => ({
    default: module.ClassSectionStudentsDialog,
  })),
);

export function AdminClassSectionsContent() {
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<ClassSectionRow | null>(null);
  const [toDelete, setToDelete] = useState<ClassSectionRow | null>(null);
  const [studentsSection, setStudentsSection] = useState<ClassSectionRow | null>(null);

  const classSectionsQuery = useQuery({
    queryKey: classSectionsKey,
    queryFn: adminApi.listClassSections,
    staleTime: 60 * 1000,
  });
  const optionsQuery = useQuery({
    queryKey: ["admin", "class-section-options"],
    queryFn: adminApi.getClassSectionOptions,
    enabled: open || editing != null,
    retry: false,
    staleTime: 5 * 60 * 1000,
  });

  const rows = useMemo(() => {
    return (classSectionsQuery.data ?? []).map((section) => mapApiClassSection(section));
  }, [classSectionsQuery.data]);

  const rowsWithMajors = rows;
  const optionData = optionsQuery.data;

  const options = useMemo(
    () =>
      buildOptionSets(
        {
          courses: optionData?.courses,
          semesters: optionData?.semesters,
          teachers: optionData?.teachers,
          rooms: optionData?.rooms,
          periods: optionData?.periods,
          classSections: classSectionsQuery.data,
        },
        rowsWithMajors,
      ),
    [classSectionsQuery.data, optionData, rowsWithMajors],
  );

  const createMutation = useMutation({
    mutationFn: (values: ClassSectionFormValues) =>
      adminApi.createClassSection(toClassSectionRequest(values)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classSectionsKey });
      queryClient.invalidateQueries({ queryKey: ["admin", "dashboard"] });
      toast.success("Đã mở lớp học phần");
      closeForm();
    },
    onError: (error) => toast.error(error.message),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, values }: { id: number; values: ClassSectionFormValues }) =>
      adminApi.updateClassSection(id, toClassSectionRequest(values)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classSectionsKey });
      queryClient.invalidateQueries({ queryKey: ["admin", "dashboard"] });
      toast.success("Đã cập nhật lớp học phần");
      closeForm();
    },
    onError: (error) => toast.error(error.message),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteClassSection(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classSectionsKey });
      queryClient.invalidateQueries({ queryKey: ["admin", "dashboard"] });
      toast.success("Đã xóa lớp học phần");
      setToDelete(null);
    },
    onError: (error) => toast.error(error.message),
  });

  const cancelMutation = useMutation({
    mutationFn: (id: number) => adminApi.cancelClassSection(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: classSectionsKey });
      queryClient.invalidateQueries({ queryKey: ["admin", "dashboard"] });
      toast.success("Đã hủy lớp học phần");
      setToDelete(null);
    },
    onError: (error) => toast.error(error.message),
  });

  const submit = (values: ClassSectionFormValues) => {
    const validationMessage = validateClassSectionPlan({
      values,
      rows: rowsWithMajors,
      periods: options.periods,
      rooms: options.rooms,
      editingId: editing?.id,
    });
    if (validationMessage) {
      toast.error(validationMessage);
      return;
    }
    if (editing?.numericId) updateMutation.mutate({ id: editing.numericId, values });
    else createMutation.mutate(values);
  };

  const confirmDelete = () => {
    if (!toDelete) return;
    if (toDelete.currentSlots > 0) {
      if (toDelete.numericId) cancelMutation.mutate(toDelete.numericId);
      return;
    }
    if (toDelete.numericId) deleteMutation.mutate(toDelete.numericId);
    else setToDelete(null);
  };

  if (classSectionsQuery.isPending) return <ClassSectionsSkeleton />;

  return (
    <div className="space-y-4">
      <PageHeader
        title="Lớp học phần"
        description={`${rowsWithMajors.length} lớp học phần theo ngành`}
        actions={
          <Button
            className="gap-2"
            onClick={() => {
              setEditing(null);
              setOpen(true);
            }}
          >
            <Plus className="h-4 w-4" />
            Mở lớp học phần
          </Button>
        }
      />

      {classSectionsQuery.isError && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Không tải được API lớp học phần</AlertTitle>
          <AlertDescription>{classSectionsQuery.error.message}</AlertDescription>
        </Alert>
      )}
      {optionsQuery.isError && (
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Không tải được một số danh mục</AlertTitle>
          <AlertDescription>
            Vui lòng kiểm tra API Course/Semester trước khi tạo hoặc sửa lớp học phần.
          </AlertDescription>
        </Alert>
      )}

      <ClassSectionsByMajor
        rows={rowsWithMajors}
        onEdit={(row) => {
          setEditing(row);
          setOpen(true);
        }}
        onDelete={setToDelete}
        onViewStudents={setStudentsSection}
      />

      <ClassSectionFormDialog
        open={open}
        editing={editing}
        options={options}
        isPending={createMutation.isPending || updateMutation.isPending || optionsQuery.isPending}
        onOpenChange={(value) => {
          if (!value) closeForm();
          else setOpen(true);
        }}
        onSubmit={submit}
      />

      {studentsSection && (
        <Suspense fallback={null}>
          <ClassSectionStudentsDialog
            open={!!studentsSection}
            section={studentsSection}
            onOpenChange={(value) => {
              if (!value) setStudentsSection(null);
            }}
          />
        </Suspense>
      )}

      <ConfirmDialog
        open={!!toDelete}
        onOpenChange={(value) => !value && setToDelete(null)}
        title={toDelete && toDelete.currentSlots > 0 ? "Hủy lớp học phần?" : "Xóa lớp học phần?"}
        description={
          toDelete && toDelete.currentSlots > 0
            ? `${toDelete.classCode} đã có ${toDelete.currentSlots} sinh viên, hệ thống sẽ hủy lớp và chuyển enrollment active sang CANCELED.`
            : `${toDelete?.classCode ?? ""} sẽ bị xóa khỏi hệ thống.`
        }
        destructive
        confirmText={toDelete && toDelete.currentSlots > 0 ? "Hủy lớp" : "Xóa"}
        onConfirm={confirmDelete}
      />
    </div>
  );

  function closeForm() {
    setOpen(false);
    setEditing(null);
  }
}

function ClassSectionsSkeleton() {
  return (
    <div className="space-y-4">
      <Skeleton className="h-12 w-72" />
      <Skeleton className="h-96 w-full" />
    </div>
  );
}
