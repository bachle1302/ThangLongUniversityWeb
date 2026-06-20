import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/admin/exam-registrations")({
  component: RouteComponent,
});

function RouteComponent() {
  return <div>Hello "/admin/exam-registrations"!</div>;
}
