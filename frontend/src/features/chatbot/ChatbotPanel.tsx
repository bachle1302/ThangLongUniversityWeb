import { useRef, useEffect, useState, useCallback } from "react";
import ReactMarkdown from "react-markdown";
import { Bot, Send, Trash2, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { cn } from "@/lib/utils";
import {
  sendChatbotMessage,
  clearChatbotHistory,
  type ChatbotMessageItem,
} from "@/lib/api/chatbot";

interface Props {
  sessionId: string;
  onClose: () => void;
}

interface Message {
  role: "USER" | "ASSISTANT";
  content: string;
  pending?: boolean;
}

export function ChatbotPanel({ sessionId, onClose }: Props) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const send = useCallback(async () => {
    const text = input.trim();
    if (!text || loading) return;

    setInput("");
    setMessages((prev) => [
      ...prev,
      { role: "USER", content: text },
      { role: "ASSISTANT", content: "", pending: true },
    ]);
    setLoading(true);

    try {
      const res = await sendChatbotMessage(text, sessionId);
      setMessages((prev) => [...prev.slice(0, -1), { role: "ASSISTANT", content: res.answer }]);
    } catch {
      setMessages((prev) => [
        ...prev.slice(0, -1),
        {
          role: "ASSISTANT",
          content: "Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.",
        },
      ]);
    } finally {
      setLoading(false);
    }
  }, [input, loading, sessionId]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      void send();
    }
  };

  const handleClear = async () => {
    await clearChatbotHistory(sessionId);
    setMessages([]);
  };

  return (
    <div className="fixed bottom-20 right-6 z-50 flex h-[560px] w-[380px] flex-col rounded-xl border bg-background shadow-2xl">
      {/* Header */}
      <div className="flex items-center gap-3 rounded-t-xl border-b bg-primary px-4 py-3">
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary-foreground/20">
          <Bot className="h-5 w-5 text-primary-foreground" />
        </div>
        <div className="flex-1">
          <p className="text-sm font-semibold text-primary-foreground">Trợ lý Sinh viên TLU</p>
          <p className="text-[11px] text-primary-foreground/70">
            Hỗ trợ học vụ & dịch vụ sinh viên
          </p>
        </div>
        <Button
          variant="ghost"
          size="icon"
          className="h-7 w-7 text-primary-foreground hover:bg-primary-foreground/20"
          onClick={handleClear}
          title="Xóa lịch sử"
        >
          <Trash2 className="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="icon"
          className="h-7 w-7 text-primary-foreground hover:bg-primary-foreground/20"
          onClick={onClose}
          title="Đóng"
        >
          <X className="h-4 w-4" />
        </Button>
      </div>

      {/* Messages */}
      <ScrollArea className="flex-1 px-3 py-3">
        {messages.length === 0 && (
          <div className="flex h-full flex-col items-center justify-center gap-2 py-8 text-center text-muted-foreground">
            <Bot className="h-10 w-10 opacity-30" />
            <p className="text-sm">Xin chào! Mình có thể giúp gì cho bạn?</p>
            <p className="text-xs opacity-70">Hỏi về tuyển sinh, học vụ, học phí, thư viện...</p>
          </div>
        )}
        <div className="flex flex-col gap-3">
          {messages.map((msg, i) => (
            <MessageBubble key={i} message={msg} />
          ))}
        </div>
        <div ref={bottomRef} />
      </ScrollArea>

      {/* Input */}
      <div className="flex items-center gap-2 rounded-b-xl border-t px-3 py-3">
        <Input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Nhập câu hỏi..."
          disabled={loading}
          className="flex-1 text-sm"
        />
        <Button
          size="icon"
          onClick={() => void send()}
          disabled={loading || !input.trim()}
          className="h-9 w-9 shrink-0"
        >
          <Send className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}

function MessageBubble({ message }: { message: Message }) {
  const isUser = message.role === "USER";

  return (
    <div className={cn("flex items-end gap-2", isUser && "flex-row-reverse")}>
      {!isUser && (
        <Avatar className="h-6 w-6 shrink-0">
          <AvatarFallback className="bg-primary text-[10px] text-primary-foreground">
            <Bot className="h-3.5 w-3.5" />
          </AvatarFallback>
        </Avatar>
      )}
      <div
        className={cn(
          "max-w-[85%] rounded-2xl px-3 py-2 text-sm",
          isUser
            ? "rounded-br-sm bg-primary text-primary-foreground"
            : "rounded-bl-sm bg-muted text-foreground",
        )}
      >
        {message.pending ? (
          <TypingIndicator />
        ) : isUser ? (
          <p className="whitespace-pre-wrap">{message.content}</p>
        ) : (
          <div className="prose prose-sm max-w-none dark:prose-invert prose-p:my-1 prose-ul:my-1 prose-li:my-0">
            <ReactMarkdown>{message.content}</ReactMarkdown>
          </div>
        )}
      </div>
    </div>
  );
}

function TypingIndicator() {
  return (
    <div className="flex items-center gap-1 py-0.5">
      {[0, 1, 2].map((i) => (
        <span
          key={i}
          className="h-2 w-2 rounded-full bg-muted-foreground/50 animate-bounce"
          style={{ animationDelay: `${i * 0.15}s` }}
        />
      ))}
    </div>
  );
}
