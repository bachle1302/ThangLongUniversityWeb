import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Outlet, createRootRouteWithContext, HeadContent, Scripts } from "@tanstack/react-router";
import { AuthProvider } from "@/lib/auth";
import { Toaster } from "@/components/ui/sonner";
import type { ReactNode } from "react";
import appCss from "../styles.css?url";

const siteUrl = "https://thanglonguniversity.online/";
const siteTitle = "Đại học Thăng Long - Cổng thông tin";
const siteDescription = "Hệ thống quản lý và cổng thông tin trực tuyến của Đại học Thăng Long.";
const socialImage = `${siteUrl}images/LogoThangLongUniversity.png`;

export const Route = createRootRouteWithContext<{ queryClient: QueryClient }>()({
  head: () => ({
    title: siteTitle,
    meta: [
      { charSet: "utf-8" },
      { name: "viewport", content: "width=device-width, initial-scale=1" },
      { name: "description", content: siteDescription },
      { property: "og:title", content: siteTitle },
      { name: "twitter:title", content: siteTitle },
      { property: "og:description", content: siteDescription },
      { name: "twitter:description", content: siteDescription },
      { property: "og:image", content: socialImage },
      { property: "og:image:alt", content: "Logo Đại học Thăng Long" },
      { property: "og:site_name", content: "Đại học Thăng Long" },
      { property: "og:url", content: siteUrl },
      { name: "twitter:image", content: socialImage },
      { name: "twitter:card", content: "summary_large_image" },
      { name: "theme-color", content: "#C8102E" },
      { property: "og:type", content: "website" },
    ],
    links: [
      { rel: "icon", href: "/favicon.ico", sizes: "48x48" },
      { rel: "icon", type: "image/png", sizes: "48x48", href: "/favicon-48.png" },
      { rel: "icon", type: "image/png", sizes: "192x192", href: "/favicon-192.png" },
      { rel: "icon", type: "image/png", sizes: "512x512", href: "/favicon-512.png" },
      { rel: "apple-touch-icon", sizes: "192x192", href: "/favicon-192.png" },
      { rel: "manifest", href: "/site.webmanifest" },
      { rel: "preconnect", href: "https://fonts.googleapis.com" },
      { rel: "preconnect", href: "https://fonts.gstatic.com", crossOrigin: "anonymous" },
      { rel: "dns-prefetch", href: "https://images.unsplash.com" },
      { rel: "stylesheet", href: appCss },
    ],
  }),
  shellComponent: RootShell,
  component: RootComponent,
  notFoundComponent: () => (
    <div className="grid min-h-screen place-items-center bg-background p-6 text-center">
      <div>
        <h1 className="text-6xl font-bold text-primary">404</h1>
        <p className="mt-2 text-muted-foreground">Trang không tồn tại.</p>
        <a
          href="/"
          className="mt-4 inline-block rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground"
        >
          Về trang chủ
        </a>
      </div>
    </div>
  ),
});

function RootShell({ children }: { children: ReactNode }) {
  return (
    <html lang="vi">
      <head>
        <HeadContent />
      </head>
      <body>
        {children}
        <Scripts />
      </body>
    </html>
  );
}

function RootComponent() {
  const { queryClient } = Route.useRouteContext();
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <PublicShell />
        <Toaster position="top-right" richColors />
      </AuthProvider>
    </QueryClientProvider>
  );
}

function PublicShell() {
  return (
    <>
      <Outlet />
    </>
  );
}
