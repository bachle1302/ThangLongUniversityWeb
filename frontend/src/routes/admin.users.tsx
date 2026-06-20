import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { DataTable } from "@/components/data-table/DataTable";
import { EntityFormDialog } from "@/components/forms/EntityFormDialog";
import { Button } from "@/components/ui/button";
import { ConfirmDialog } from "@/components/ui/confirm-dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { PageHeader, StatCard } from "@/components/ui/page-header";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { StatusBadge } from "@/components/ui/status-badge";
import { Switch } from "@/components/ui/switch";
import { adminApi } from "@/lib/api/admin";
import type { AdminUserResponse, Role } from "@/lib/api/types";
import { GraduationCap, Pencil, Plus, User, UserRound, Users, Trash2, KeyRound, Eye, EyeOff, Loader2 } from "lucide-react";
import { toast } from "sonner";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

export const Route = createFileRoute("/admin/users")({ component: UsersPage });

type AdminUserRow = {
  id: string;
  numericId?: number;
  profileId?: number;
  username: string;
  email: string;
  fullName: string;
  role: Role;
  active: boolean;
  createdAt: string;
  lastLogin: string;
  source: "API";
};

const roleLabels: Record<Role, string> = {
  ADMIN: "Quản trị hệ thống",
  TEACHER: "Giảng viên",
  STUDENT: "Sinh viên",
};

function UsersPage() {
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const [roleFilter, setRoleFilter] = useState<"ALL" | Role>("ALL");
  const [editingUser, setEditingUser] = useState<AdminUserRow | null>(null);
  const [toDelete, setToDelete] = useState<AdminUserRow | null>(null);
  const [resetPasswordUser, setResetPasswordUser] = useState<AdminUserRow | null>(null);
  const [resetPasswordVal, setResetPasswordVal] = useState("");
  const [showResetPassword, setShowResetPassword] = useState(false);
  const [form, setForm] = useState({
    username: "",
    email: "",
    fullName: "",
    role: "ADMIN" as Role,
    password: "",
    teacherCode: "",
    studentCode: "",
    majorId: "",
    academicYear: String(new Date().getFullYear()),
    dob: "",
  });
  const [editForm, setEditForm] = useState({
    username: "",
    email: "",
    fullName: "",
  });

  const query = useQuery({
    queryKey: ["admin", "users"],
    queryFn: adminApi.listUsers,
  });

  const majorsQuery = useQuery({
    queryKey: ["admin", "majors"],
    queryFn: adminApi.listMajors,
  });

  const rows = useMemo(() => {
    return (query.data ?? []).map(mapApiUser);
  }, [query.data]);

  const filteredRows = useMemo(() => {
    if (roleFilter === "ALL") return rows;
    return rows.filter((row) => row.role === roleFilter);
  }, [roleFilter, rows]);

  const stats = useMemo(
    () => ({
      total: rows.length,
      admins: rows.filter((row) => row.role === "ADMIN").length,
      teachers: rows.filter((row) => row.role === "TEACHER").length,
      students: rows.filter((row) => row.role === "STUDENT").length,
    }),
    [rows],
  );

  const createMutation = useMutation({
    mutationFn: async () => {
      if (form.role === "ADMIN") {
        return adminApi.createAdmin({
          username: form.username,
          email: form.email,
          password: form.password,
        });
      }

      if (form.role === "TEACHER") {
        if (!form.teacherCode.trim() || !form.fullName.trim()) {
          throw new Error("Vui lòng nhập mã giảng viên và họ tên");
        }
        return adminApi.createTeacher({
          username: form.username,
          email: form.email,
          password: form.password,
          teacherCode: form.teacherCode.trim(),
          fullName: form.fullName.trim(),
        });
      }

      if (!form.studentCode.trim() || !form.fullName.trim()) {
        throw new Error("Vui lòng nhập mã sinh viên và họ tên");
      }
      if (!form.majorId) {
        throw new Error("Vui lòng chọn ngành cho sinh viên");
      }

      return adminApi.createStudent({
        username: form.username,
        email: form.email,
        password: form.password,
        studentCode: form.studentCode.trim(),
        fullName: form.fullName.trim(),
        dob: form.dob || "2000-01-01",
        majorId: Number(form.majorId),
        academicYear: Number(form.academicYear) || new Date().getFullYear(),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      toast.success("Đã tạo tài khoản");
      setForm({
        username: "",
        email: "",
        fullName: "",
        role: "ADMIN",
        password: "",
        teacherCode: "",
        studentCode: "",
        majorId: "",
        academicYear: String(new Date().getFullYear()),
        dob: "",
      });
      setOpen(false);
    },
    onError: (error) => toast.error(error.message),
  });

  const toggleMutation = useMutation({
    mutationFn: adminApi.toggleUserStatus,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      toast.success("Đã cập nhật trạng thái tài khoản");
    },
    onError: (error) => toast.error(error.message),
  });

  const updateMutation = useMutation({
    mutationFn: ({
      id,
      payload,
    }: {
      id: number;
      payload: { username: string; email: string; fullName: string };
    }) => adminApi.updateUser(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      toast.success("Đã cập nhật tài khoản");
      setEditingUser(null);
    },
    onError: (error) => toast.error(error.message),
  });

  const deleteMutation = useMutation({
    mutationFn: (user: AdminUserRow) => {
      if (user.role === "ADMIN") {
        if (!user.numericId) throw new Error("Không tìm thấy id tài khoản ADMIN");
        return adminApi.deleteAdminUser(user.numericId);
      }
      if (user.role === "STUDENT") {
        if (!user.profileId) throw new Error("Không tìm thấy student id để xóa");
        return adminApi.deleteStudent(user.profileId);
      }
      if (!user.profileId) throw new Error("Không tìm thấy teacher id để xóa");
      return adminApi.deleteTeacher(user.profileId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      toast.success("Đã xóa tài khoản");
      setToDelete(null);
    },
    onError: (error) =>
      toast.error(error instanceof Error ? error.message : "Không xóa được tài khoản"),
  });

  const resetPasswordMutation = useMutation({
    mutationFn: async () => {
      if (!resetPasswordUser || !resetPasswordUser.numericId) return;
      if (resetPasswordVal.length < 6) {
        throw new Error("Mật khẩu mới phải từ 6 ký tự trở lên");
      }
      return adminApi.resetUserPassword(resetPasswordUser.numericId, resetPasswordVal);
    },
    onSuccess: () => {
      toast.success(`Đã reset mật khẩu cho tài khoản ${resetPasswordUser?.username} thành công!`);
      setResetPasswordUser(null);
      setResetPasswordVal("");
    },
    onError: (error: any) => {
      toast.error(error.message || "Không thể reset mật khẩu");
    },
  });

  const submit = async () => {
    await createMutation.mutateAsync();
  };

  const submitEdit = async () => {
    if (!editingUser?.numericId) {
      toast.error("Không tìm thấy tài khoản cần cập nhật");
      return;
    }
    await updateMutation.mutateAsync({
      id: editingUser.numericId,
      payload: {
        username: editForm.username,
        email: editForm.email,
        fullName: editForm.fullName,
      },
    });
  };

  const openEditDialog = (user: AdminUserRow) => {
    setEditingUser(user);
    setEditForm({
      username: user.username,
      email: user.email,
      fullName: user.fullName,
    });
  };

  return (
    <div>
      <PageHeader title="Quản lý tài khoản" />

      <div className="mb-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="Tài khoản trong hệ thống"
          value={stats.total}
          icon={Users}
          tone="primary"
        />
        <StatCard label="TK admin" value={stats.admins} icon={User} tone="warning" />
        <StatCard
          label="Tài khoản giảng viên"
          value={stats.teachers}
          icon={GraduationCap}
          tone="info"
        />
        <StatCard
          label="Tài khoản sinh viên"
          value={stats.students}
          icon={UserRound}
          tone="success"
        />
      </div>

      <DataTable
        data={filteredRows}
        rowKey={(user) => user.id}
        searchPlaceholder="Tìm theo username, họ tên, email, role..."
        searchSlot={
          <Select value={roleFilter} onValueChange={(value) => setRoleFilter(toRoleFilter(value))}>
            <SelectTrigger className="w-[170px]">
              <SelectValue placeholder="Lọc vai trò" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">Tất cả vai trò</SelectItem>
              <SelectItem value="ADMIN">Admin</SelectItem>
              <SelectItem value="TEACHER">Teacher</SelectItem>
              <SelectItem value="STUDENT">Student</SelectItem>
            </SelectContent>
          </Select>
        }
        toolbar={
          <Button onClick={() => setOpen(true)} className="gap-2">
            <Plus className="h-4 w-4" />
            Thêm tài khoản
          </Button>
        }
        columns={[
          {
            key: "username",
            header: "Username",
            render: (user) => <span className="font-mono text-xs">{user.username}</span>,
          },
          {
            key: "fullName",
            header: "Họ tên",
            render: (user) => (
              <div className="min-w-44 space-y-1">
                <div className="font-medium">{user.fullName}</div>
              </div>
            ),
          },
          {
            key: "email",
            header: "Email",
            render: (user) => <span className="text-sm text-muted-foreground">{user.email}</span>,
          },
          {
            key: "role",
            header: "Vai trò",
            accessor: (user) => `${user.role} ${roleLabels[user.role]}`,
            render: (user) => (
              <div className="space-y-1">
                <StatusBadge value={user.role} />
                <div className="text-xs text-muted-foreground">{roleLabels[user.role]}</div>
              </div>
            ),
          },
          {
            key: "active",
            header: "Trạng thái",
            render: (user) => (
              <div className="flex items-center gap-2">
                <Switch
                  checked={user.active}
                  disabled={!user.numericId || toggleMutation.isPending}
                  onCheckedChange={() => user.numericId && toggleMutation.mutate(user.numericId)}
                />
                <span className="text-xs text-muted-foreground">
                  {user.active ? "Active" : "Inactive"}
                </span>
              </div>
            ),
          },
          {
            key: "createdAt",
            header: "Tạo lúc",
            render: (user) => (
              <div className="text-xs text-muted-foreground">
                <div>{user.createdAt}</div>
              </div>
            ),
          },
          {
            key: "lastLogin",
            header: "Đăng nhập gần nhất",
            render: (user) => (
              <span className="text-xs text-muted-foreground">{user.lastLogin}</span>
            ),
          },
          {
            key: "actions",
            header: "",
            className: "w-32 text-right",
            searchable: false,
            render: (user) => (
              <div className="flex justify-end gap-1">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  disabled={!user.numericId}
                  onClick={() => setResetPasswordUser(user)}
                  title="Reset mật khẩu"
                >
                  <KeyRound className="h-4 w-4 text-primary" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-8 w-8"
                  disabled={!user.numericId}
                  onClick={() => openEditDialog(user)}
                >
                  <Pencil className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  className="h-8 gap-1 px-3 text-destructive"
                  disabled={!user.numericId || deleteMutation.isPending}
                  onClick={() => setToDelete(user)}
                >
                  <Trash2 className="h-4 w-4" />
                  Xóa
                </Button>
              </div>
            ),
          },
        ]}
      />

      <EntityFormDialog
        open={open}
        onOpenChange={setOpen}
        title="Thêm tài khoản"
        description="Hỗ trợ tạo tài khoản ADMIN, TEACHER, STUDENT từ một form"
        onSubmit={submit}
      >
        <div className="grid gap-3 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label>Username</Label>
            <Input
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              required
            />
          </div>
          <div className="space-y-1.5">
            <Label>Email</Label>
            <Input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
            />
          </div>
          <div className="space-y-1.5">
            <Label>Mật khẩu</Label>
            <Input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              required
            />
          </div>
          <div className="space-y-1.5">
            <Label>Vai trò</Label>
            <Select
              value={form.role}
              onValueChange={(value) =>
                setForm({
                  ...form,
                  role: toRole(value),
                  teacherCode: "",
                  studentCode: "",
                  majorId: "",
                })
              }
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ADMIN">Quản trị viên</SelectItem>
                <SelectItem value="TEACHER">Giảng viên</SelectItem>
                <SelectItem value="STUDENT">Sinh viên</SelectItem>
              </SelectContent>
            </Select>
          </div>
          {form.role !== "ADMIN" && (
            <div className="space-y-1.5 sm:col-span-2">
              <Label>Họ tên hiển thị</Label>
              <Input
                value={form.fullName}
                onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                required
              />
            </div>
          )}
          {form.role === "TEACHER" && (
            <div className="space-y-1.5 sm:col-span-2">
              <Label>Mã giảng viên</Label>
              <Input
                value={form.teacherCode}
                onChange={(e) => setForm({ ...form, teacherCode: e.target.value })}
                required
              />
            </div>
          )}
          {form.role === "STUDENT" && (
            <>
              <div className="space-y-1.5">
                <Label>Mã sinh viên</Label>
                <Input
                  value={form.studentCode}
                  onChange={(e) => setForm({ ...form, studentCode: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-1.5">
                <Label>Ngày sinh</Label>
                <Input
                  type="date"
                  value={form.dob}
                  onChange={(e) => setForm({ ...form, dob: e.target.value })}
                />
              </div>
              <div className="space-y-1.5">
                <Label>Niên khóa</Label>
                <Input
                  type="number"
                  value={form.academicYear}
                  onChange={(e) => setForm({ ...form, academicYear: e.target.value })}
                  required
                />
              </div>
              <div className="space-y-1.5 sm:col-span-2">
                <Label>Ngành</Label>
                <Select
                  value={form.majorId}
                  onValueChange={(value) => setForm({ ...form, majorId: value })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Chọn ngành" />
                  </SelectTrigger>
                  <SelectContent>
                    {(majorsQuery.data ?? []).map((major) => (
                      <SelectItem key={major.id} value={String(major.id)}>
                        {major.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </>
          )}
        </div>
      </EntityFormDialog>

      <EntityFormDialog
        open={!!editingUser}
        onOpenChange={(value) => !value && setEditingUser(null)}
        title="Sửa tài khoản"
        description="Cập nhật username, email và họ tên hiển thị"
        onSubmit={submitEdit}
        submitText="Luu thay doi"
      >
        <div className="grid gap-3 sm:grid-cols-2">
          <div className="space-y-1.5">
            <Label>Username</Label>
            <Input
              value={editForm.username}
              onChange={(e) => setEditForm({ ...editForm, username: e.target.value })}
              required
            />
          </div>
          <div className="space-y-1.5">
            <Label>Email</Label>
            <Input
              type="email"
              value={editForm.email}
              onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
              required
            />
          </div>
          <div className="space-y-1.5 sm:col-span-2">
            <Label>Họ tên hiển thị</Label>
            <Input
              value={editForm.fullName}
              onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
              required
            />
          </div>
        </div>
      </EntityFormDialog>

      <ConfirmDialog
        open={!!toDelete}
        onOpenChange={(value) => !value && setToDelete(null)}
        title="Xóa tài khoản?"
        description={`Hành động này không thể hoàn tác. Tài khoản: ${toDelete?.username}. Nếu tài khoản đã phát sinh dữ liệu liên kết (học phí, điểm, đăng ký môn), hệ thống sẽ từ chối xóa và trả về lý do cụ thể.`}
        destructive
        confirmText="Xóa"
        onConfirm={() => {
          if (toDelete) deleteMutation.mutate(toDelete);
        }}
      />

      <Dialog
        open={!!resetPasswordUser}
        onOpenChange={(value) => {
          if (!value) {
            setResetPasswordUser(null);
            setResetPasswordVal("");
            setShowResetPassword(false);
          }
        }}
      >
        <DialogContent className="sm:max-w-[425px]">
          <form
            onSubmit={(e) => {
              e.preventDefault();
              resetPasswordMutation.mutate();
            }}
          >
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2 text-lg font-semibold">
                <KeyRound className="h-5 w-5 text-primary" />
                Reset mật khẩu
              </DialogTitle>
              <DialogDescription>
                Nhập mật khẩu mới cho tài khoản{" "}
                <span className="font-semibold">{resetPasswordUser?.username}</span>.
              </DialogDescription>
            </DialogHeader>

            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label htmlFor="adminResetPassword">Mật khẩu mới</Label>
                <div className="relative">
                  <Input
                    id="adminResetPassword"
                    type={showResetPassword ? "text" : "password"}
                    value={resetPasswordVal}
                    onChange={(e) => setResetPasswordVal(e.target.value)}
                    placeholder="Mật khẩu mới (tối thiểu 6 ký tự)"
                    disabled={resetPasswordMutation.isPending}
                    required
                    className="pr-10"
                  />
                  <button
                    type="button"
                    onClick={() => setShowResetPassword(!showResetPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                    disabled={resetPasswordMutation.isPending}
                  >
                    {showResetPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
              </div>
            </div>

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                disabled={resetPasswordMutation.isPending}
                onClick={() => {
                  setResetPasswordUser(null);
                  setResetPasswordVal("");
                  setShowResetPassword(false);
                }}
              >
                Hủy
              </Button>
              <Button type="submit" disabled={resetPasswordMutation.isPending}>
                {resetPasswordMutation.isPending && (
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                )}
                Xác nhận Reset
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function mapApiUser(user: AdminUserResponse): AdminUserRow {
  return {
    id: String(user.id),
    numericId: user.id,
    profileId: user.profileId ?? undefined,
    username: user.username,
    email: user.email,
    fullName: user.fullName?.trim() || `${roleLabels[user.role]} ${user.username}`,
    role: user.role,
    active: user.active,
    createdAt: formatDateTime(user.createdAt),
    lastLogin: formatDateTime(user.lastLoginAt, "Chưa đăng nhập"),
    source: "API",
  };
}

function toRole(value: string): Role {
  return value === "TEACHER" || value === "STUDENT" ? value : "ADMIN";
}

function toRoleFilter(value: string): "ALL" | Role {
  return value === "ALL" || value === "TEACHER" || value === "STUDENT" || value === "ADMIN"
    ? value
    : "ALL";
}

function formatDateTime(value?: string | null, fallback = "-") {
  if (!value) return fallback;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return fallback;
  return new Intl.DateTimeFormat("vi-VN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}
