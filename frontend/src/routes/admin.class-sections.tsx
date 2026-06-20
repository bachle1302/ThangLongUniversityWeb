import { createFileRoute, redirect } from "@tanstack/react-router";
import { AdminClassSectionsContent } from "@/features/admin-class-sections/AdminClassSectionsContent";

export const Route = createFileRoute("/admin/class-sections")({
  beforeLoad: () => {
    throw redirect({ to: "/admin/semesters" });
  },
  component: AdminClassSectionsPage,
});

function AdminClassSectionsPage() {
  return <AdminClassSectionsContent />;
}
