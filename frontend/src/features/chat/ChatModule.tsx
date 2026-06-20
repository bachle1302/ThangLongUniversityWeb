import { useEffect, useMemo, useRef, useState } from "react";
import {
  createGroupRoom,
  createPrivateRoom,
  leaveRoom,
  listFiles,
  listLinks,
  listMessages,
  listRooms,
  markRoomRead,
  searchUsers,
  sendMessage,
  uploadFile,
  type ChatMessage,
  type ChatRoom,
  type ChatUser,
} from "@/lib/api/chat";
import { useAuth } from "@/lib/auth";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { cn } from "@/lib/utils";
import {
  ArrowLeft,
  ArrowUp,
  FileText,
  Link as LinkIcon,
  LogOut,
  MessageCircle,
  PanelLeftClose,
  PanelLeftOpen,
  Paperclip,
  Plus,
  Search,
  Users,
  X,
} from "lucide-react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

function initials(value?: string | null) {
  return (value || "?")
    .split(" ")
    .slice(-2)
    .map((part) => part[0] ?? "")
    .join("")
    .toUpperCase();
}

function personLabel(
  person?: { code?: string | null; fullName?: string | null; username?: string | null } | null,
) {
  if (!person) return "Chat riêng";
  const code = person.code || person.username;
  const fullName = person.fullName || person.username;
  if (code && fullName && code !== fullName) return `${code} - ${fullName}`;
  return fullName || code || "Người dùng";
}

function roomTitle(room: ChatRoom, username?: string | null) {
  if (room.type !== "PRIVATE") return room.name || "Nhóm chat";
  const otherMember = room.members.find((member) => member.username !== username);
  return otherMember ? personLabel(otherMember) : room.name || "Chat riêng";
}

function roomSubtitle(room: ChatRoom) {
  if (room.lastMessagePreview) return room.lastMessagePreview;
  if (room.type === "PRIVATE") return "Nhắn tin riêng";
  return `${room.memberCount} thành viên`;
}

function mediaHref(url?: string | null) {
  if (!url) return "#";
  return url.startsWith("http") ? url : `${API_BASE_URL}${url}`;
}

function formatSize(size?: number | null) {
  if (!size) return "";
  if (size < 1024 * 1024) return `${Math.max(1, Math.round(size / 1024))} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

function formatTime(message: Pick<ChatMessage, "createdAt" | "createdAtEpochMs">) {
  const value =
    message.createdAtEpochMs ?? (message.createdAt ? new Date(message.createdAt).getTime() : null);
  if (!value) return "";
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
  }).format(new Date(value));
}

function formatDateTime(value?: string | number | null) {
  if (!value) return "";
  const date = typeof value === "number" ? new Date(value) : new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return new Intl.DateTimeFormat("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
  }).format(date);
}

const urlPattern = /(https?:\/\/[^\s]+)/gi;

function extractFirstUrl(value?: string | null) {
  if (!value) return null;
  return value.match(urlPattern)?.[0] ?? null;
}

function MessageContent({ content }: { content?: string | null }) {
  if (!content) return null;
  const parts = content.split(urlPattern);
  return (
    <>
      {parts.map((part, index) => {
        const isUrl = /^https?:\/\//i.test(part);
        return isUrl ? (
          <a
            key={`${part}-${index}`}
            href={part}
            target="_blank"
            rel="noreferrer"
            className="break-all underline underline-offset-2"
            onClick={(event) => event.stopPropagation()}
          >
            {part}
          </a>
        ) : (
          <span key={`${part}-${index}`}>{part}</span>
        );
      })}
    </>
  );
}

function UserSuggestionList({
  users,
  onSelect,
  emptyText,
}: {
  users: ChatUser[];
  onSelect: (user: ChatUser) => void;
  emptyText: string;
}) {
  if (users.length === 0) {
    return <div className="px-3 py-4 text-sm text-muted-foreground">{emptyText}</div>;
  }

  return (
    <div className="max-h-72 overflow-y-auto py-1">
      {users.map((user) => (
        <button
          key={user.id}
          onClick={() => onSelect(user)}
          className="flex w-full items-center gap-3 px-3 py-2.5 text-left transition-colors hover:bg-muted"
        >
          <Avatar className="h-9 w-9">
            <AvatarFallback className="text-xs">{initials(user.fullName)}</AvatarFallback>
          </Avatar>
          <span className="min-w-0 flex-1">
            <span className="block truncate text-sm font-medium">{personLabel(user)}</span>
            <span className="block truncate text-xs text-muted-foreground">
              {user.subtitle || user.email}
            </span>
          </span>
        </button>
      ))}
    </div>
  );
}

export function ChatModule() {
  const { profile } = useAuth();
  const [rooms, setRooms] = useState<ChatRoom[]>([]);
  const [activeId, setActiveId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [files, setFiles] = useState<ChatMessage[]>([]);
  const [links, setLinks] = useState<ChatMessage[]>([]);
  const [personQuery, setPersonQuery] = useState("");
  const [personResults, setPersonResults] = useState<ChatUser[]>([]);
  const [groupQuery, setGroupQuery] = useState("");
  const [groupResults, setGroupResults] = useState<ChatUser[]>([]);
  const [selectedMembers, setSelectedMembers] = useState<ChatUser[]>([]);
  const [groupName, setGroupName] = useState("");
  const [groupOpen, setGroupOpen] = useState(false);
  const [draft, setDraft] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loadingRooms, setLoadingRooms] = useState(true);
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);

  const activeRoom = rooms.find((room) => room.id === activeId) ?? null;
  const sortedMessages = useMemo(
    () => [...messages].sort((a, b) => (a.createdAtEpochMs ?? 0) - (b.createdAtEpochMs ?? 0)),
    [messages],
  );

  const reloadRooms = async (preferredId?: number) => {
    const page = await listRooms();
    const readRoomId = preferredId ?? activeId;
    setRooms(
      page.content.map((room) => (room.id === readRoomId ? { ...room, unreadCount: 0 } : room)),
    );
    setActiveId((current) => {
      if (preferredId) return preferredId;
      return page.content.some((room) => room.id === current) ? current : null;
    });
  };

  const selectRoom = (roomId: number) => {
    setActiveId(roomId);
    setRooms((prev) =>
      prev.map((room) => (room.id === roomId ? { ...room, unreadCount: 0 } : room)),
    );
    if (window.innerWidth < 1024) setSidebarOpen(false);
    markRoomRead(roomId)
      .then(() => reloadRooms(roomId))
      .catch(() => undefined);
  };

  useEffect(() => {
    reloadRooms()
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoadingRooms(false));
  }, []);

  useEffect(() => {
    if (!activeId) {
      setMessages([]);
      setFiles([]);
      setLinks([]);
      return;
    }

    Promise.all([listMessages(activeId), listFiles(activeId), listLinks(activeId)])
      .then(([messagePage, filePage, linkPage]) => {
        setMessages(messagePage.content);
        setFiles(filePage.content);
        setLinks(linkPage.content);
        setRooms((prev) =>
          prev.map((room) => (room.id === activeId ? { ...room, unreadCount: 0 } : room)),
        );
      })
      .catch((e: Error) => setError(e.message));
  }, [activeId]);

  useEffect(() => {
    const handle = window.setTimeout(() => {
      const query = personQuery.trim();
      if (query.length < 2) {
        setPersonResults([]);
        return;
      }
      searchUsers(query)
        .then(setPersonResults)
        .catch((e: Error) => setError(e.message));
    }, 220);
    return () => window.clearTimeout(handle);
  }, [personQuery]);

  useEffect(() => {
    const handle = window.setTimeout(() => {
      const query = groupQuery.trim();
      if (query.length < 2) {
        setGroupResults([]);
        return;
      }
      searchUsers(query)
        .then(setGroupResults)
        .catch((e: Error) => setError(e.message));
    }, 220);
    return () => window.clearTimeout(handle);
  }, [groupQuery]);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
  }, [activeId, sortedMessages.length]);

  const startPrivateChat = async (user: ChatUser) => {
    const room = await createPrivateRoom(user.id);
    setPersonQuery("");
    setPersonResults([]);
    await reloadRooms(room.id);
  };

  const createGroup = async () => {
    if (selectedMembers.length < 2) {
      setError("Cần chọn ít nhất 2 thành viên để tạo nhóm.");
      return;
    }

    const room = await createGroupRoom(
      groupName.trim() || "Nhóm chat mới",
      selectedMembers.map((user) => user.id),
    );
    setGroupOpen(false);
    setGroupName("");
    setGroupQuery("");
    setGroupResults([]);
    setSelectedMembers([]);
    await reloadRooms(room.id);
  };

  const submitMessage = async () => {
    if (!activeId || !draft.trim()) return;
    const text = draft.trim();
    const created = await sendMessage(activeId, text);
    setMessages((prev) => [...prev, created]);
    if (extractFirstUrl(text)) {
      const linkPage = await listLinks(activeId);
      setLinks(linkPage.content);
    }
    setDraft("");
    await reloadRooms(activeId);
  };

  const submitFile = async (file?: File) => {
    if (!file || !activeId) return;
    const created = await uploadFile(activeId, file);
    setMessages((prev) => [...prev, created]);
    setFiles((prev) => [created, ...prev]);
    await reloadRooms(activeId);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const outGroup = async () => {
    if (!activeRoom || activeRoom.type === "PRIVATE") return;
    await leaveRoom(activeRoom.id);
    await reloadRooms();
  };

  return (
    <div
      className={cn(
        "grid h-[calc(100vh-8rem)] min-h-[640px] overflow-hidden rounded-lg border bg-background shadow-sm",
        sidebarOpen ? "lg:grid-cols-[320px_minmax(0,1fr)]" : "lg:grid-cols-[minmax(0,1fr)]",
      )}
    >
      <aside
        className={cn("min-h-0 flex-col border-r bg-muted/20", sidebarOpen ? "flex" : "hidden")}
      >
        <div className="border-b bg-background p-4">
          <div className="mb-3 flex items-center justify-between">
            <div>
              <div className="text-base font-semibold">Tin nhắn</div>
              <div className="text-xs text-muted-foreground">
                Tìm sinh viên, giảng viên để bắt đầu chat
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Button
                size="icon"
                variant="outline"
                onClick={() => setSidebarOpen(false)}
                title="Ẩn danh sách"
              >
                <PanelLeftClose className="h-4 w-4" />
              </Button>
              <Button size="icon" onClick={() => setGroupOpen(true)} title="Tạo nhóm">
                <Plus className="h-4 w-4" />
              </Button>
            </div>
          </div>

          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={personQuery}
              onChange={(event) => setPersonQuery(event.target.value)}
              placeholder="Nhập mã sinh viên hoặc tên"
              className="h-10 bg-muted/40 pl-9"
            />
            {personQuery.trim().length >= 2 && (
              <div className="absolute left-0 right-0 top-12 z-30 overflow-hidden rounded-lg border bg-popover shadow-lg">
                <UserSuggestionList
                  users={personResults}
                  onSelect={startPrivateChat}
                  emptyText="Không tìm thấy người phù hợp."
                />
              </div>
            )}
          </div>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto">
          {loadingRooms ? (
            <div className="p-4 text-sm text-muted-foreground">Đang tải danh sách chat...</div>
          ) : rooms.length === 0 ? (
            <div className="p-6 text-sm text-muted-foreground">
              Chưa có cuộc trò chuyện nào. Hãy tìm người để nhắn tin.
            </div>
          ) : (
            <div className="divide-y">
              {rooms.map((room) => {
                const title = roomTitle(room, profile?.username);
                const active = room.id === activeId;
                return (
                  <button
                    key={room.id}
                    onClick={() => selectRoom(room.id)}
                    className={cn(
                      "flex w-full items-center gap-3 px-4 py-3 text-left transition-colors hover:bg-muted",
                      active && "bg-background",
                    )}
                  >
                    <Avatar className="h-11 w-11">
                      <AvatarFallback
                        className={cn(
                          "text-xs",
                          room.type !== "PRIVATE" && "bg-primary/10 text-primary",
                        )}
                      >
                        {initials(title)}
                      </AvatarFallback>
                    </Avatar>
                    <span className="min-w-0 flex-1">
                      <span className="flex items-center gap-2">
                        <span className="truncate text-sm font-semibold">{title}</span>
                        {room.type !== "PRIVATE" && (
                          <Users className="h-3.5 w-3.5 text-muted-foreground" />
                        )}
                      </span>
                      <span className="mt-0.5 block truncate text-xs text-muted-foreground">
                        {roomSubtitle(room)}
                      </span>
                      {room.lastMessageTime && (
                        <span className="mt-0.5 block text-[10px] text-muted-foreground/80">
                          {formatDateTime(room.lastMessageTime)}
                        </span>
                      )}
                    </span>
                    {room.unreadCount > 0 && (
                      <span className="grid h-5 min-w-5 place-items-center rounded-full bg-primary px-1.5 text-[11px] font-semibold text-primary-foreground">
                        {room.unreadCount > 9 ? "9+" : room.unreadCount}
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </div>
      </aside>

      <section className={cn("flex min-h-0 min-w-0 flex-col", sidebarOpen && "hidden lg:flex")}>
        {activeRoom ? (
          <>
            <header className="flex h-16 items-center justify-between border-b bg-background px-5">
              <div className="flex min-w-0 items-center gap-3">
                {!sidebarOpen && (
                  <Button
                    size="icon"
                    variant="ghost"
                    onClick={() => setSidebarOpen(true)}
                    title="Hiện danh sách"
                  >
                    <ArrowLeft className="h-4 w-4 lg:hidden" />
                    <PanelLeftOpen className="hidden h-4 w-4 lg:block" />
                  </Button>
                )}
                <Avatar className="h-10 w-10">
                  <AvatarFallback>
                    {initials(roomTitle(activeRoom, profile?.username))}
                  </AvatarFallback>
                </Avatar>
                <div className="min-w-0">
                  <div className="truncate text-sm font-semibold">
                    {roomTitle(activeRoom, profile?.username)}
                  </div>
                  <div className="text-xs text-muted-foreground">
                    {activeRoom.type === "PRIVATE"
                      ? "Tin nhắn riêng"
                      : `${activeRoom.memberCount} thành viên`}
                  </div>
                </div>
              </div>
              {activeRoom.type !== "PRIVATE" && (
                <Button variant="outline" size="sm" onClick={outGroup}>
                  <LogOut className="mr-2 h-4 w-4" /> Rời nhóm
                </Button>
              )}
            </header>

            <div
              className={cn(
                "grid min-h-0 flex-1 grid-cols-1",
                "xl:grid-cols-[minmax(0,1fr)_220px]",
              )}
            >
              <div className="flex min-h-0 flex-col">
                <div
                  ref={scrollRef}
                  className="min-h-0 flex-1 overflow-y-auto bg-muted/10 px-6 py-5"
                >
                  {sortedMessages.length === 0 ? (
                    <div className="grid h-full place-items-center text-center text-sm text-muted-foreground">
                      <div>
                        <MessageCircle className="mx-auto mb-3 h-10 w-10 text-muted-foreground/40" />
                        Chưa có tin nhắn. Hãy bắt đầu cuộc trò chuyện.
                      </div>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {sortedMessages.map((message) => {
                        const mine = message.senderUsername === profile?.username;
                        return (
                          <div key={message.id} className={cn("flex gap-2", mine && "justify-end")}>
                            {!mine && (
                              <Avatar className="mt-5 h-8 w-8">
                                <AvatarFallback className="text-xs">
                                  {initials(message.senderFullName)}
                                </AvatarFallback>
                              </Avatar>
                            )}
                            <div className={cn("max-w-[min(760px,82%)]", mine && "text-right")}>
                              <div className="mb-1 text-[11px] text-muted-foreground">
                                {mine
                                  ? "Bạn"
                                  : personLabel({
                                      code: message.senderCode,
                                      fullName: message.senderFullName,
                                      username: message.senderUsername,
                                    })}
                              </div>
                              <div
                                className={cn(
                                  "rounded-lg border bg-background px-3 py-2 text-sm shadow-sm",
                                  mine && "border-primary bg-primary text-primary-foreground",
                                )}
                              >
                                {message.type === "FILE" ? (
                                  <a
                                    href={mediaHref(message.mediaUrl)}
                                    target="_blank"
                                    rel="noreferrer"
                                    className="inline-flex items-center gap-2 underline-offset-2 hover:underline"
                                  >
                                    <FileText className="h-4 w-4" />
                                    <span>{message.fileName || message.content}</span>
                                    <span className="opacity-70">
                                      {formatSize(message.fileSize)}
                                    </span>
                                  </a>
                                ) : (
                                  <MessageContent content={message.content} />
                                )}
                              </div>
                              <div
                                className={cn(
                                  "mt-1 text-[10px] text-muted-foreground",
                                  mine && "text-right",
                                )}
                              >
                                {formatTime(message)}
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>

                <div className="border-t bg-background p-4">
                  <div className="flex items-center gap-2 rounded-lg border bg-muted/20 p-2">
                    <input
                      ref={fileInputRef}
                      type="file"
                      className="hidden"
                      onChange={(event) => submitFile(event.target.files?.[0])}
                    />
                    <Button
                      size="icon"
                      variant="ghost"
                      onClick={() => fileInputRef.current?.click()}
                      title="Gửi file"
                    >
                      <Paperclip className="h-4 w-4" />
                    </Button>
                    <Input
                      value={draft}
                      onChange={(event) => setDraft(event.target.value)}
                      onKeyDown={(event) => {
                        if (event.key === "Enter" && !event.shiftKey) {
                          event.preventDefault();
                          submitMessage();
                        }
                      }}
                      placeholder="Nhập tin nhắn..."
                      className="border-0 bg-transparent shadow-none focus-visible:ring-0"
                    />
                    <Button
                      size="icon"
                      disabled={!draft.trim()}
                      onClick={submitMessage}
                      title="Gửi"
                    >
                      <ArrowUp className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </div>

              <aside className="hidden min-h-0 border-l bg-background xl:block">
                <Tabs
                  defaultValue={activeRoom.type === "PRIVATE" ? "files" : "members"}
                  className="flex h-full min-h-0 flex-col"
                >
                  <TabsList
                    className={cn(
                      "grid h-11 w-full shrink-0 rounded-none border-b bg-muted/30",
                      activeRoom.type === "PRIVATE" ? "grid-cols-2" : "grid-cols-3",
                    )}
                  >
                    {activeRoom.type !== "PRIVATE" && (
                      <TabsTrigger value="members">
                        <Users className="h-4 w-4" />
                      </TabsTrigger>
                    )}
                    <TabsTrigger value="files">
                      <FileText className="h-4 w-4" />
                    </TabsTrigger>
                    <TabsTrigger value="links">
                      <LinkIcon className="h-4 w-4" />
                    </TabsTrigger>
                  </TabsList>
                  {activeRoom.type !== "PRIVATE" && (
                    <TabsContent value="members" className="m-0 min-h-0 flex-1 overflow-y-auto p-3">
                      <div className="mb-2 text-xs font-semibold uppercase text-muted-foreground">
                        Thành viên
                      </div>
                      {activeRoom.members.map((member) => (
                        <div
                          key={member.userId}
                          className="flex items-center gap-3 rounded-md px-2 py-2"
                        >
                          <Avatar className="h-8 w-8">
                            <AvatarFallback className="text-xs">
                              {initials(member.fullName)}
                            </AvatarFallback>
                          </Avatar>
                          <div className="min-w-0">
                            <div className="truncate text-sm font-medium">
                              {personLabel(member)}
                            </div>
                            <div className="text-xs text-muted-foreground">{member.role}</div>
                          </div>
                        </div>
                      ))}
                    </TabsContent>
                  )}
                  <TabsContent value="files" className="m-0 min-h-0 flex-1 overflow-y-auto p-3">
                    <div className="mb-2 text-xs font-semibold uppercase text-muted-foreground">
                      File đã gửi
                    </div>
                    <div className="space-y-2">
                      {files.length === 0 ? (
                        <div className="text-sm text-muted-foreground">Chưa có file.</div>
                      ) : (
                        files.map((file) => (
                          <a
                            key={file.id}
                            href={mediaHref(file.mediaUrl)}
                            target="_blank"
                            rel="noreferrer"
                            className="block rounded-md border p-2 text-sm hover:bg-muted"
                          >
                            <span className="flex min-w-0 gap-2">
                              <FileText className="mt-0.5 h-4 w-4 shrink-0" />
                              <span className="min-w-0 truncate">
                                {file.fileName || file.content}
                              </span>
                            </span>
                            <span className="mt-1 block pl-6 text-[10px] text-muted-foreground">
                              {formatSize(file.fileSize)} {formatTime(file)}
                            </span>
                          </a>
                        ))
                      )}
                    </div>
                  </TabsContent>
                  <TabsContent value="links" className="m-0 min-h-0 flex-1 overflow-y-auto p-3">
                    <div className="mb-2 text-xs font-semibold uppercase text-muted-foreground">
                      Link đã gửi
                    </div>
                    <div className="space-y-2">
                      {links.length === 0 ? (
                        <div className="text-sm text-muted-foreground">Chưa có link.</div>
                      ) : (
                        links.map((link) => (
                          <a
                            key={link.id}
                            href={extractFirstUrl(link.content) || "#"}
                            target="_blank"
                            rel="noreferrer"
                            className="block truncate rounded-md border p-2 text-sm hover:bg-muted"
                          >
                            <span className="block truncate">
                              {extractFirstUrl(link.content) || link.content}
                            </span>
                            <span className="mt-1 block text-[10px] text-muted-foreground">
                              {formatTime(link)}
                            </span>
                          </a>
                        ))
                      )}
                    </div>
                  </TabsContent>
                </Tabs>
              </aside>
            </div>
          </>
        ) : (
          <div className="grid flex-1 place-items-center bg-muted/10 p-8 text-center text-sm text-muted-foreground">
            <div>
              {!sidebarOpen && (
                <Button className="mb-4" variant="outline" onClick={() => setSidebarOpen(true)}>
                  <PanelLeftOpen className="mr-2 h-4 w-4" /> Hiện danh sách chat
                </Button>
              )}
              <MessageCircle className="mx-auto mb-3 h-12 w-12 text-muted-foreground/40" />
              {error || "Tìm sinh viên, giảng viên để bắt đầu chat."}
            </div>
          </div>
        )}
      </section>

      <Dialog open={groupOpen} onOpenChange={setGroupOpen}>
        <DialogContent className="sm:max-w-xl">
          <DialogHeader>
            <DialogTitle>Tạo nhóm chat</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <Input
              value={groupName}
              onChange={(event) => setGroupName(event.target.value)}
              placeholder="Tên nhóm"
            />
            <div className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                value={groupQuery}
                onChange={(event) => setGroupQuery(event.target.value)}
                placeholder="Nhập mã sinh viên, tên sinh viên hoặc giảng viên"
                className="pl-9"
              />
            </div>
            <div className="flex min-h-9 flex-wrap gap-2">
              {selectedMembers.map((user) => (
                <Button
                  key={user.id}
                  type="button"
                  size="sm"
                  variant="secondary"
                  onClick={() =>
                    setSelectedMembers((prev) => prev.filter((item) => item.id !== user.id))
                  }
                >
                  {personLabel(user)}
                  <X className="ml-1 h-3 w-3" />
                </Button>
              ))}
            </div>
            <div className="overflow-hidden rounded-lg border">
              <UserSuggestionList
                users={groupResults.filter(
                  (user) => !selectedMembers.some((item) => item.id === user.id),
                )}
                onSelect={(user) => setSelectedMembers((prev) => [...prev, user])}
                emptyText={
                  groupQuery.trim().length < 2
                    ? "Nhập ít nhất 2 ký tự để tìm."
                    : "Không tìm thấy người phù hợp."
                }
              />
            </div>
          </div>
          <DialogFooter>
            <Button onClick={createGroup} disabled={selectedMembers.length < 2}>
              Tạo nhóm
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
