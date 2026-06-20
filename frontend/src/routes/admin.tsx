import { createFileRoute } from "@tanstack/react-router";
import { ProtectedOutlet } from "@/components/layout/ProtectedOutlet";

export const Route = createFileRoute("/admin")({
  component: () => <ProtectedOutlet role="ADMIN" />,
});
