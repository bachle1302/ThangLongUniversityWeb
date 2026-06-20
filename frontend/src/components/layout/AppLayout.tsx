import { Link, useNavigate, useRouterState } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Award,
  Bell,
  BookCheck,
  BookMarked,
  BookOpen,
  Brain,
  Building2,
  CalendarCheck,
  CalendarDays,
  ClipboardList,
  Clock,
  DoorOpen,
  GraduationCap,
  Layers,
  LayoutDashboard,
  Library,
  KeyRound,
  LogOut,
  Menu,
  MessageSquare,
  NotebookPen,
  PanelLeftClose,
  PanelLeftOpen,
  Receipt,
  Repeat,
  School,
  User,
  UserCog,
  Users,
} from "lucide-react";
import { ChangePasswordDialog } from "@/components/ChangePasswordDialog";
import { lazy, Suspense, useState, type ReactNode } from "react";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { useAuth } from "@/lib/auth";
import type { Role } from "@/lib/api/types";
import {
  type AppNotification,
  listNotifications,
  markNotificationRead,
} from "@/lib/api/notifications";
import { cn } from "@/lib/utils";

const ChatbotWidget = lazy(() =>
  import("@/features/chatbot/ChatbotWidget").then((module) => ({
    default: module.ChatbotWidget,
  })),
);

type Item = { to: string; label: string; icon: React.ComponentType<{ className?: string }> };
type NavGroup = { heading: string; items: Item[] };

const adminNavGroups: NavGroup[] = [
  {
    heading: "Tổng quan",
    items: [
      { to: "/admin/dashboard", label: "Bảng điều khiển", icon: LayoutDashboard },
    ],
  },
  {
    heading: "Người dùng",
    items: [
      { to: "/admin/users", label: "Tài khoản", icon: Users },
      { to: "/admin/students", label: "Sinh viên", icon: GraduationCap },
      { to: "/admin/teachers", label: "Giảng viên", icon: UserCog },
    ],
  },
  {
    heading: "Danh mục",
    items: [
      { to: "/admin/departments", label: "Khoa / Bộ môn", icon: Building2 },
      { to: "/admin/majors", label: "Ngành học", icon: Library },
      { to: "/admin/homerooms", label: "Lớp hành chính", icon: School },
      { to: "/admin/courses", label: "Học phần", icon: BookOpen },
      { to: "/admin/rooms", label: "Phòng học", icon: DoorOpen },
      { to: "/admin/periods", label: "Tiết học", icon: Clock },
    ],
  },
  {
    heading: "Quản lý lớp",
    items: [{ to: "/admin/semesters", label: "Quản lý học kỳ", icon: CalendarDays }],
  },
  {
    heading: "AI Chatbot",
    items: [{ to: "/admin/knowledge", label: "Cơ sở tri thức", icon: Brain }],
  },
];

const teacherNavGroups: NavGroup[] = [
  {
    heading: "Tổng quan",
    items: [
      { to: "/teacher/dashboard", label: "Bảng điều khiển", icon: LayoutDashboard },
      { to: "/teacher/profile", label: "Hồ sơ cá nhân", icon: User },
    ],
  },
  {
    heading: "Giảng dạy",
    items: [
      { to: "/teacher/timetable", label: "Thời khóa biểu", icon: CalendarDays },
      { to: "/teacher/classes", label: "Lớp học phần", icon: Layers },
      { to: "/teacher/attendance", label: "Điểm danh sinh viên", icon: ClipboardList },
      { to: "/teacher/grades", label: "Quản lý điểm", icon: NotebookPen },
    ],
  },
];

const studentNavGroups: NavGroup[] = [
  {
    heading: "Tổng quan",
    items: [
      { to: "/student/dashboard", label: "Bảng điều khiển", icon: LayoutDashboard },
      { to: "/student/profile", label: "Hồ sơ cá nhân", icon: User },
    ],
  },
  {
    heading: "Tra cứu",
    items: [
      { to: "/student/schedule", label: "Thời khóa biểu", icon: CalendarDays },
      { to: "/student/exams", label: "Lịch thi", icon: CalendarCheck },
      { to: "/student/academic-results", label: "Kết quả học tập", icon: Award },
      { to: "/student/curriculum", label: "Chương trình đào tạo", icon: BookMarked },
    ],
  },
  {
    heading: "Chức năng",
    items: [
      { to: "/student/course-registration", label: "Đăng ký học phần", icon: BookCheck },
      { to: "/student/retake-registration", label: "Đăng ký thi lại", icon: Repeat },
      { to: "/student/tuition", label: "Học phí", icon: Receipt },
    ],
  },
];

const chatByRole: Record<Role, string> = {
  ADMIN: "/admin/chat",
  TEACHER: "/teacher/chat",
  STUDENT: "/student/chat",
};

const schoolLogo = "/images/LogoThangLongUniversity.png";

function notificationTarget(item: AppNotification, role: Role) {
  if (item.type === "CHAT") return chatByRole[role];
  return item.link || (role === "STUDENT" ? "/student/notifications" : "");
}

function NavItem({
  item,
  pathname,
  onNavigate,
}: {
  item: Item;
  pathname: string;
  onNavigate?: () => void;
}) {
  const active = pathname === item.to || pathname.startsWith(`${item.to}/`);
  return (
    <Link
      to={item.to}
      onClick={onNavigate}
      className={cn(
        "group flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
        active
          ? "bg-sidebar-primary text-sidebar-primary-foreground shadow-sm"
          : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
      )}
    >
      <item.icon className="h-4 w-4 shrink-0" />
      <span className="truncate">{item.label}</span>
    </Link>
  );
}

function GroupedNavList({
  groups,
  pathname,
  onNavigate,
}: {
  groups: NavGroup[];
  pathname: string;
  onNavigate?: () => void;
}) {
  return (
    <nav className="flex flex-col gap-4 px-3 py-3">
      {groups.map((group) => (
        <div key={group.heading}>
          <div className="mb-1 px-3 text-[10px] font-semibold uppercase tracking-wider text-sidebar-foreground/40">
            {group.heading}
          </div>
          <div className="flex flex-col gap-0.5">
            {group.items.map((item) => (
              <NavItem key={item.to} item={item} pathname={pathname} onNavigate={onNavigate} />
            ))}
          </div>
        </div>
      ))}
    </nav>
  );
}

function SidebarInner({ pathname, onNavigate }: { pathname: string; onNavigate?: () => void }) {
  const { role } = useAuth();
  const navGroups =
    role === "ADMIN" ? adminNavGroups : role === "TEACHER" ? teacherNavGroups : studentNavGroups;

  return (
    <div className="flex h-full flex-col bg-sidebar text-sidebar-foreground">
      <Link
        to="/"
        onClick={onNavigate}
        className="flex items-center gap-3 border-b border-sidebar-border px-5 py-4 transition-colors hover:bg-sidebar-accent/70"
      >
        <span className="grid h-12 w-12 place-items-center rounded-md bg-white/95 p-1 shadow-sm">
          <img
            src={schoolLogo}
            alt="Logo Đại học Thăng Long"
            className="h-full w-full object-contain"
          />
        </span>
        <div className="leading-tight">
          <div className="text-sm font-semibold">Thăng Long</div>
          <div className="text-xs text-sidebar-foreground/60">Cổng thông tin</div>
        </div>
      </Link>
      <div className="sidebar-scroll flex-1 overflow-y-auto">
        {role ? (
          <GroupedNavList groups={navGroups} pathname={pathname} onNavigate={onNavigate} />
        ) : null}
      </div>
      <div className="border-t border-sidebar-border px-4 py-3 text-[11px] text-sidebar-foreground/50">
        © {new Date().getFullYear()} Đại học Thăng Long
      </div>
    </div>
  );
}

function RoleSwitcher() {
  const { role } = useAuth();
  return (
    <Button variant="outline" size="sm" className="gap-2" disabled>
      <span className="hidden sm:inline">Vai trò:</span>
      <span className="font-semibold capitalize">{role?.toLowerCase()}</span>
    </Button>
  );
}

function Breadcrumbs({ pathname }: { pathname: string }) {
  const parts = pathname.split("/").filter(Boolean);
  return (
    <div className="hidden items-center gap-1 text-sm text-muted-foreground md:flex">
      {parts.map((part, index) => (
        <span key={index} className="flex items-center gap-1">
          {index > 0 && <span className="text-muted-foreground/40">/</span>}
          <span className={cn(index === parts.length - 1 && "font-medium text-foreground")}>
            {part.replace(/-/g, " ")}
          </span>
        </span>
      ))}
    </div>
  );
}

export function AppLayout({ children }: { children: ReactNode }) {
  const pathname = useRouterState({ select: (state) => state.location.pathname });
  const { name, role, logout } = useAuth();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const [desktopSidebarOpen, setDesktopSidebarOpen] = useState(true);
  const [changePasswordOpen, setChangePasswordOpen] = useState(false);

  const initials = (name ?? "?")
    .split(" ")
    .slice(-2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();

  const notificationsQuery = useQuery({
    queryKey: [role?.toLowerCase(), "notifications"],
    queryFn: () => listNotifications(role === "TEACHER" ? "TEACHER" : "STUDENT"),
    enabled: role === "STUDENT" || role === "TEACHER",
    refetchInterval: role === "STUDENT" || role === "TEACHER" ? 30000 : false,
  });
  const notificationItems = notificationsQuery.data ?? [];
  const unreadCount = notificationItems.filter((item) => !item.read).length;

  const markReadMutation = useMutation({
    mutationFn: (id: string) =>
      markNotificationRead(id, role === "TEACHER" ? "TEACHER" : "STUDENT"),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: [role?.toLowerCase(), "notifications"] }),
  });

  const openNotification = async (item: AppNotification) => {
    if (!role) return;
    if (!item.read) {
      await markReadMutation.mutateAsync(item.id);
    }

    const target = notificationTarget(item, role);
    if (!target) return;
    if (target.startsWith(`/${role.toLowerCase()}/`)) {
      navigate({ to: target as never });
      return;
    }

    window.location.href = target;
  };

  const handleLogout = async () => {
    await logout();
    navigate({ to: "/login" });
  };

  return (
    <div className="flex min-h-screen w-full bg-muted/30">
      <aside
        className={cn(
          "sticky top-0 hidden h-screen w-64 shrink-0 border-r border-sidebar-border lg:block",
          !desktopSidebarOpen && "lg:hidden",
        )}
      >
        <SidebarInner pathname={pathname} />
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        <header className="sticky top-0 z-20 flex h-14 items-center gap-3 border-b bg-background/95 px-4 backdrop-blur md:px-6">
          <Sheet open={open} onOpenChange={setOpen}>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="lg:hidden" aria-label="Mở menu">
                <Menu className="h-5 w-5" />
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-64 p-0">
              <SheetHeader className="sr-only">
                <SheetTitle>Menu điều hướng</SheetTitle>
              </SheetHeader>
              <SidebarInner pathname={pathname} onNavigate={() => setOpen(false)} />
            </SheetContent>
          </Sheet>

          <Button
            variant="ghost"
            size="icon"
            className="hidden h-9 w-9 lg:inline-flex"
            onClick={() => setDesktopSidebarOpen((value) => !value)}
            title={desktopSidebarOpen ? "Ẩn thanh bên" : "Hiện thanh bên"}
          >
            {desktopSidebarOpen ? (
              <PanelLeftClose className="h-5 w-5" />
            ) : (
              <PanelLeftOpen className="h-5 w-5" />
            )}
          </Button>

          <Breadcrumbs pathname={pathname} />

          <div className="ml-auto flex items-center gap-2">
            {role && role !== "ADMIN" && (
              <Link to={chatByRole[role]}>
                <Button variant="ghost" size="icon" className="h-9 w-9" title="Trò chuyện">
                  <MessageSquare className="h-5 w-5" />
                </Button>
              </Link>
            )}

            {(role === "STUDENT" || role === "TEACHER") && (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="relative h-9 w-9"
                    title="Thông báo"
                  >
                    <Bell className="h-5 w-5" />
                    {unreadCount > 0 && (
                      <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-destructive text-[10px] font-bold text-destructive-foreground">
                        {unreadCount > 9 ? "9+" : unreadCount}
                      </span>
                    )}
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-80">
                  <DropdownMenuLabel>Thông báo</DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  {notificationsQuery.isLoading ? (
                    <div className="px-3 py-4 text-sm text-muted-foreground">
                      Đang tải thông báo...
                    </div>
                  ) : notificationItems.length === 0 ? (
                    <div className="px-3 py-4 text-sm text-muted-foreground">
                      Không có thông báo mới.
                    </div>
                  ) : (
                    notificationItems.slice(0, 4).map((item) => (
                      <DropdownMenuItem
                        key={item.id}
                        className="flex cursor-pointer items-start gap-3 py-3"
                        onClick={() => void openNotification(item)}
                      >
                        <span
                          className={cn(
                            "mt-1 h-2 w-2 shrink-0 rounded-full",
                            item.read ? "bg-muted-foreground/30" : "bg-primary",
                          )}
                        />
                        <span className="min-w-0">
                          <span className="block truncate text-sm font-medium">{item.title}</span>
                          <span className="line-clamp-2 text-xs text-muted-foreground">
                            {item.body}
                          </span>
                        </span>
                      </DropdownMenuItem>
                    ))
                  )}
                  {role === "STUDENT" && (
                    <>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem
                        onClick={() => navigate({ to: "/student/notifications" })}
                        className="cursor-pointer justify-center text-sm font-medium"
                      >
                        Xem chi tiết
                      </DropdownMenuItem>
                    </>
                  )}
                </DropdownMenuContent>
              </DropdownMenu>
            )}

            <RoleSwitcher />

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="h-9 gap-2 px-2">
                  <Avatar className="h-7 w-7">
                    <AvatarFallback className="bg-primary text-xs text-primary-foreground">
                      {initials}
                    </AvatarFallback>
                  </Avatar>
                  <div className="hidden text-left leading-tight md:block">
                    <div className="text-xs font-medium">{name}</div>
                    <div className="text-[10px] text-muted-foreground capitalize">
                      {role?.toLowerCase()}
                    </div>
                  </div>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuLabel>{name}</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  onClick={() => setChangePasswordOpen(true)}
                  className="cursor-pointer"
                >
                  <KeyRound className="mr-2 h-4 w-4" />
                  Đổi mật khẩu
                </DropdownMenuItem>
                <DropdownMenuItem onClick={handleLogout} className="cursor-pointer">
                  <LogOut className="mr-2 h-4 w-4" />
                  Đăng xuất
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </header>

        <main className="flex-1 p-4 md:p-6 lg:p-8">{children}</main>
      </div>
      {role !== "ADMIN" && (
        <Suspense fallback={null}>
          <ChatbotWidget />
        </Suspense>
      )}
      <ChangePasswordDialog open={changePasswordOpen} onOpenChange={setChangePasswordOpen} />
    </div>
  );
}
