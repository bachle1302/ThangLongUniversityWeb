import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/lib/auth";
import type { Role } from "@/lib/api/types";
import { Link, createFileRoute, useNavigate } from "@tanstack/react-router";
import { useEffect, useState, type FormEvent } from "react";
import { AlertCircle, BookOpen, CheckCircle2, Loader2, Sparkles, WifiOff } from "lucide-react";
import { toast } from "sonner";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const BACKEND_PROBE_TIMEOUT_MS = 3000;
const schoolLogo = "/images/LogoThangLongUniversity.png";

type BackendStatus = "checking" | "online" | "offline";
type Credentials = {
  username: string;
  password: string;
};

export const Route = createFileRoute("/login")({
  component: LoginPage,
});

function buildApiUrl(path: string) {
  return new URL(path, API_BASE_URL).toString();
}

async function probeBackend() {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), BACKEND_PROBE_TIMEOUT_MS);

  try {
    await fetch(buildApiUrl("/api/auth/login"), {
      method: "OPTIONS",
      signal: controller.signal,
    });
    return true;
  } catch {
    return false;
  } finally {
    window.clearTimeout(timeoutId);
  }
}

function resolveDashboard(role: Role) {
  if (role === "ADMIN") return "/admin/dashboard";
  if (role === "TEACHER") return "/teacher/dashboard";
  return "/student/dashboard";
}

function getLoginErrorMessage(error: unknown) {
  const message = error instanceof Error ? error.message : "Đăng nhập thất bại";
  const normalized = message.trim().toLowerCase();

  if (
    normalized === "bad credentials" ||
    normalized === "invalid username or password" ||
    normalized.includes("bad credentials")
  ) {
    return "Sai tên đăng nhập hoặc mật khẩu.";
  }

  return message || "Đăng nhập thất bại";
}

const HERO_STATS = [
  { label: "Năm thành lập", value: "1988" },
  { label: "Ngành đào tạo", value: "30+" },
  { label: "Sinh viên", value: "10 000+" },
] as const;

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [backendStatus, setBackendStatus] = useState<BackendStatus>("checking");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [cooldownSeconds, setCooldownSeconds] = useState(0);

  useEffect(() => {
    if (cooldownSeconds <= 0) return;
    const interval = setInterval(() => {
      setCooldownSeconds((prev) => prev - 1);
    }, 1000);
    return () => clearInterval(interval);
  }, [cooldownSeconds]);

  useEffect(() => {
    let alive = true;

    probeBackend().then((online) => {
      if (!alive) return;
      setBackendStatus(online ? "online" : "offline");
    });

    return () => {
      alive = false;
    };
  }, []);

  const submitLogin = async (credentials: Credentials) => {
    if (cooldownSeconds > 0) return;
    setIsSubmitting(true);
    setErrorMessage(null);

    try {
      const role = await login(credentials.username, credentials.password);
      toast.success(`Đăng nhập thành công với vai trò ${role.toLowerCase()}`);
      navigate({ to: resolveDashboard(role) });
    } catch (error: any) {
      if (error && error.status === 429) {
        const retry = error.retryAfter || 10;
        setCooldownSeconds(retry);
        const limitMessage = `Bạn đã gửi yêu cầu quá nhanh. Vui lòng thử lại sau ${retry} giây.`;
        setErrorMessage(limitMessage);
        toast.error(limitMessage);
      } else {
        const message = getLoginErrorMessage(error);
        setErrorMessage(message);
        toast.error(message);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    void submitLogin({ username, password });
  };

  const statusIcon =
    backendStatus === "checking" ? (
      <Loader2 className="h-3 w-3 animate-spin text-muted-foreground" />
    ) : backendStatus === "online" ? (
      <CheckCircle2 className="h-3 w-3 text-green-500" />
    ) : (
      <WifiOff className="h-3 w-3 text-destructive" />
    );

  const statusText =
    backendStatus === "checking"
      ? `Đang kiểm tra kết nối...`
      : backendStatus === "online"
        ? `Hệ thống sẵn sàng`
        : `Không kết nối được hệ thống`;

  const statusTextClassName =
    backendStatus === "offline" ? "text-destructive" : "text-muted-foreground";

  return (
    <div className="relative min-h-screen lg:grid lg:grid-cols-[1fr_480px] xl:grid-cols-[1fr_520px]">
      <div className="relative hidden lg:block">
        <img
          src="/images/DHTL.jpg"
          alt="Toàn cảnh khuôn viên Đại học Thăng Long"
          className="absolute inset-0 h-full w-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/40 to-black/20" />

        <div className="relative flex h-full flex-col justify-between p-10">
          <Link to="/" className="inline-flex items-center" aria-label="Về trang chính">
            <div className="grid h-24 w-40 place-items-center rounded-2xl p-3 shadow-xl transition-transform hover:scale-[1.02]">
              <img
                src={schoolLogo}
                alt="Logo Đại học Thăng Long"
                className="h-full w-full object-contain"
              />
            </div>
          </Link>

          <div>
            <div className="mb-6 flex items-center gap-2">
              <Sparkles className="h-4 w-4 text-amber-400" />
              <span className="text-xs font-medium uppercase tracking-widest text-amber-400">
                Trường đại học tư thục đầu tiên tại Việt Nam
              </span>
            </div>

            <h1 className="max-w-lg text-4xl font-bold leading-tight text-white xl:text-5xl">
              Cổng Quản lý
              <br />
              <span className="bg-gradient-to-r from-amber-300 to-amber-500 bg-clip-text text-transparent">
                Đào tạo Trực tuyến
              </span>
            </h1>

            <p className="mt-4 max-w-md text-base leading-relaxed text-white/70">
              Hệ thống quản lý đào tạo toàn diện dành cho sinh viên, giảng viên và cán bộ quản lý
              Trường Đại học Thăng Long.
            </p>

            {/* Stats row */}
            <div className="mt-8 flex gap-6">
              {HERO_STATS.map((stat) => (
                <div
                  key={stat.label}
                  className="border-l border-white/20 pl-4 first:border-l-0 first:pl-0"
                >
                  <div className="text-2xl font-bold text-white">{stat.value}</div>
                  <div className="mt-0.5 text-xs text-white/60">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="flex min-h-screen flex-col bg-[#f6f8fc] lg:bg-background">
        <div className="px-6 pt-8 text-center lg:hidden">
          <Link
            to="/"
            className="mx-auto grid h-32 w-48 place-items-center transition-transform hover:scale-[1.02]"
            aria-label="Về trang chính"
          >
            <img
              src={schoolLogo}
              alt="Logo Đại học Thăng Long"
              className="h-full w-full object-contain"
            />
          </Link>
          <div className="mx-auto mt-5 max-w-xs">
            <div className="inline-flex items-center gap-2 rounded-full bg-[#C8102E]/10 px-3 py-1 text-xs font-semibold uppercase tracking-widest text-[#C8102E]">
              <BookOpen className="h-3.5 w-3.5" />
              Cổng đào tạo
            </div>
            <h1 className="mt-4 text-2xl font-bold leading-tight text-[#00204A]">
              Trường Đại học Thăng Long
            </h1>
            <p className="mt-2 text-sm leading-6 text-slate-600">
              Không gian truy cập dành cho sinh viên, giảng viên và cán bộ quản lý.
            </p>
          </div>
        </div>

        <div className="flex flex-1 items-start justify-center px-4 py-7 sm:px-6 lg:items-center lg:px-10 lg:py-8">
          <div className="w-full max-w-sm rounded-lg border border-slate-200 bg-white p-5 shadow-sm sm:max-w-md sm:p-7 lg:border-0 lg:bg-transparent lg:p-0 lg:shadow-none">
            <div className="mb-1 hidden items-center gap-2 lg:flex">
              <BookOpen className="h-5 w-5 text-primary" />
              <span className="text-sm font-medium text-muted-foreground">
                Hệ thống Quản lý Đào tạo
              </span>
            </div>

            <h2 className="text-2xl font-bold tracking-tight text-[#00204A] lg:text-foreground">
              Đăng nhập
            </h2>
            <p className="mt-1.5 text-sm leading-6 text-muted-foreground">
              Sử dụng tài khoản được cấp để truy cập hệ thống.
            </p>

            <div className="mt-4 inline-flex items-center gap-1.5 rounded-full border bg-muted/40 px-3 py-1.5 text-xs">
              {statusIcon}
              <span className={statusTextClassName}>{statusText}</span>
            </div>

            <form className="mt-6 space-y-5" onSubmit={onSubmit}>
              <div className="space-y-2">
                <Label htmlFor="username">Tên đăng nhập</Label>
                <Input
                  id="username"
                  value={username}
                  onChange={(event) => setUsername(event.target.value)}
                  placeholder="Nhập tên đăng nhập"
                  autoComplete="username"
                  className="h-12 lg:h-11"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">Mật khẩu</Label>
                <Input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  autoComplete="current-password"
                  className="h-12 lg:h-11"
                />
              </div>

              {errorMessage && (
                <div className="flex items-start gap-2 rounded-md border border-destructive/40 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                  <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
                  <span>{errorMessage}</span>
                </div>
              )}

              <Button
                type="submit"
                className="h-12 w-full text-sm font-semibold lg:h-11"
                disabled={isSubmitting || cooldownSeconds > 0}
              >
                {isSubmitting ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : null}
                {cooldownSeconds > 0 ? `Thử lại sau ${cooldownSeconds}s` : "Đăng nhập"}
              </Button>
            </form>

            <p className="mt-8 text-center text-xs text-muted-foreground">
              © {new Date().getFullYear()} Trường Đại học Thăng Long. Bảo lưu mọi quyền.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
