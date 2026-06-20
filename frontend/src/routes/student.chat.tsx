import { createFileRoute } from "@tanstack/react-router";
import { PageHeader } from "@/components/ui/page-header";
import { ChatModule } from "@/features/chat/ChatModule";
export const Route = createFileRoute("/student/chat")({
  component: () => (
    <div>
      <PageHeader title="Chat" />
      <ChatModule />
    </div>
  ),
});
