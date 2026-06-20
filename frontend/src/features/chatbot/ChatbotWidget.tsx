import { lazy, Suspense, useMemo, useState } from "react";
import { Bot } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const ChatbotPanel = lazy(() =>
  import("./ChatbotPanel").then((module) => ({ default: module.ChatbotPanel })),
);

function generateSessionId() {
  return crypto.randomUUID();
}

export function ChatbotWidget() {
  const [open, setOpen] = useState(false);
  const sessionId = useMemo(() => generateSessionId(), []);

  return (
    <>
      {open && (
        <Suspense fallback={null}>
          <ChatbotPanel sessionId={sessionId} onClose={() => setOpen(false)} />
        </Suspense>
      )}
      <Button
        size="icon"
        className={cn(
          "fixed bottom-6 right-6 z-50 h-14 w-14 rounded-full shadow-lg transition-all",
          open && "opacity-0 pointer-events-none",
        )}
        onClick={() => setOpen(true)}
        title="Trợ lý Sinh viên TLU"
      >
        <Bot className="h-6 w-6" />
        <span className="absolute right-0 top-0 h-3 w-3 rounded-full bg-green-500 ring-2 ring-background" />
      </Button>
    </>
  );
}
