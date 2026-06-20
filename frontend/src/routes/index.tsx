import { createFileRoute } from "@tanstack/react-router";
import ThangLongLanding from "@/features/landing/ThangLongLanding";
import { LandingContentProvider } from "@/lib/landing-content";

const SITE = "https://thanglonguniversity.online";

function LandingWithProvider() {
  return (
    <LandingContentProvider>
      <ThangLongLanding />
    </LandingContentProvider>
  );
}

export const Route = createFileRoute("/")({
  component: LandingWithProvider,
  head: () => ({
    meta: [
      { title: "Đại học Thăng Long - Chất riêng Thăng Long" },
      {
        name: "description",
        content:
          "Landing page Đại học Thăng Long: môi trường học tập cởi mở, cơ sở vật chất hiện đại, đa dạng ngành đào tạo và đời sống sinh viên giàu năng lượng.",
      },
    ],
    links: [
      { rel: "canonical", href: `${SITE}/` },
      {
        rel: "preload",
        as: "image",
        href: "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?auto=format&fit=crop&w=1920&q=85",
      },
    ],
  }),
});
