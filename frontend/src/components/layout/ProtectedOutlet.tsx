import { Navigate, Outlet } from "@tanstack/react-router";
import { Loader2 } from "lucide-react";
import type { Role } from "@/lib/api/types";
import { useAuth } from "@/lib/auth";
import { AppLayout } from "./AppLayout";

export function ProtectedOutlet({ role }: { role: Role }) {
  const { role: current, isReady } = useAuth();

  if (!isReady) {
    return (
      <div className="grid min-h-screen place-items-center bg-background">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <p className="text-sm text-muted-foreground">Đang xác thực...</p>
        </div>
      </div>
    );
  }

  if (!current) return <Navigate to="/login" />;
  if (current !== role) {
    const to =
      current === "ADMIN"
        ? "/admin/dashboard"
        : current === "TEACHER"
          ? "/teacher/dashboard"
          : "/student/dashboard";
    return <Navigate to={to} />;
  }
  return (
    <AppLayout>
      <Outlet />
    </AppLayout>
  );
}
