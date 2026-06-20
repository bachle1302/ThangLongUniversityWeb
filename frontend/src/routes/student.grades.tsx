import { createFileRoute, redirect } from "@tanstack/react-router";

export const Route = createFileRoute("/student/grades")({
  beforeLoad: () => {
    throw redirect({ to: "/student/academic-results" });
  },
  component: () => null,
});
