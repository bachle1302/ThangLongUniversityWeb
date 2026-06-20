import { createFileRoute } from "@tanstack/react-router";
import { ProfileView } from "./student.profile";

export const Route = createFileRoute("/admin/profile")({
  component: () => <ProfileView subtitle="Tài khoản quản trị hệ thống" />,
});
