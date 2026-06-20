import { ArrowRight, CalendarDays, Megaphone, Newspaper } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import type { AnnouncementItem, FeaturedNewsItem } from "../data/landing.data";
import {
  FEATURED_NEWS,
  PORTAL_ANNOUNCEMENTS,
  TLU_ANNOUNCEMENTS_URL,
  TLU_NEWS_URL,
} from "../data/landing.data";

// ─── Scroll Reveal helper ─────────────────────────────────────────────────────

function ScrollReveal({
  children,
  className = "",
  delay = 0,
}: {
  children: React.ReactNode;
  className?: string;
  delay?: number;
}) {
  const [visible, setVisible] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry?.isIntersecting) {
          setTimeout(() => setVisible(true), delay);
          observer.unobserve(el);
        }
      },
      { threshold: 0.1 },
    );
    observer.observe(el);
    return () => observer.disconnect();
  }, [delay]);

  return (
    <div
      ref={ref}
      className={`transition-all duration-700 ease-out ${
        visible ? "translate-y-0 opacity-100" : "translate-y-8 opacity-0"
      } ${className}`}
    >
      {children}
    </div>
  );
}

// ─── News Card (large featured) ───────────────────────────────────────────────

function FeaturedNewsCard({ item }: { item: FeaturedNewsItem }) {
  return (
    <a
      href={item.href}
      target="_blank"
      rel="noreferrer"
      className="group block"
      aria-label={`Mở tin tức: ${item.title}`}
    >
      <div className="mb-4 h-[300px] overflow-hidden rounded-2xl shadow-sm">
        <img
          src={item.image}
          alt={item.title}
          className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
        />
      </div>
      <div className="flex items-center gap-2 text-sm text-gray-500 mb-2">
        <CalendarDays className="h-4 w-4" />
        {item.date}
      </div>
      <h3 className="text-xl font-bold leading-tight text-[#00204A] transition-colors group-hover:text-[#C8102E] sm:text-2xl">
        {item.title}
      </h3>
    </a>
  );
}

// ─── News Card (small) ────────────────────────────────────────────────────────

function SmallNewsCard({ item }: { item: FeaturedNewsItem }) {
  return (
    <a
      href={item.href}
      target="_blank"
      rel="noreferrer"
      className="group flex flex-col gap-3"
      aria-label={`Mở tin tức: ${item.title}`}
    >
      <div className="h-[160px] overflow-hidden rounded-xl shadow-sm">
        <img
          src={item.image}
          alt={item.title}
          className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
        />
      </div>
      <div>
        <div className="mb-1.5 flex items-center gap-1.5 text-xs text-gray-500">
          <CalendarDays className="h-3.5 w-3.5" />
          {item.date}
        </div>
        <h4 className="line-clamp-2 text-sm font-semibold leading-snug text-[#00204A] transition-colors group-hover:text-[#C8102E]">
          {item.title}
        </h4>
      </div>
    </a>
  );
}

// ─── Announcement Item ────────────────────────────────────────────────────────

function AnnouncementListItem({ item, isLast }: { item: AnnouncementItem; isLast: boolean }) {
  return (
    <a
      href={item.href}
      target="_blank"
      rel="noreferrer"
      className={`group flex cursor-pointer gap-4 ${!isLast ? "border-b border-gray-50 pb-4" : ""}`}
      aria-label={`Mở thông báo: ${item.title}`}
    >
      {/* Date box */}
      <div className="flex h-14 w-14 shrink-0 flex-col items-center justify-center rounded-xl border border-gray-100 bg-gray-50 text-center transition-colors group-hover:border-rose-100 group-hover:bg-rose-50">
        <span className="text-lg font-black leading-none text-[#00204A]">{item.date}</span>
        <span className="mt-0.5 text-[10px] font-medium uppercase text-gray-500">{item.month}</span>
      </div>

      {/* Content */}
      <div className="flex flex-col justify-center">
        <div className="mb-1 flex flex-wrap items-center gap-1.5">
          <span className="rounded-full bg-gray-100 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-gray-600">
            {item.tag}
          </span>
          {item.isNew && (
            <span className="animate-pulse rounded-full bg-rose-100 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider text-rose-600">
              Mới
            </span>
          )}
        </div>
        <h4 className="line-clamp-2 text-sm font-semibold leading-snug text-[#00204A] transition-colors group-hover:text-[#C8102E]">
          {item.title}
        </h4>
      </div>
    </a>
  );
}

// ─── Main Section ─────────────────────────────────────────────────────────────

export function NewsAnnouncementSection() {
  return (
    <section id="news-announcements" className="bg-[#F8FAFC] py-24">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Section heading */}
        <ScrollReveal className="mb-10 text-center">
          <h2 className="text-3xl font-bold text-[#00204A] md:text-4xl">Tin tức &amp; Thông báo</h2>
          <div className="mx-auto mt-3 h-1 w-16 rounded-full bg-[#C8102E]" />
        </ScrollReveal>

        <div className="grid grid-cols-1 gap-12 lg:grid-cols-12">
          {/* ── Left: Featured News ────────────────────────────────────── */}
          <div className="lg:col-span-7">
            <ScrollReveal className="mb-6 flex items-center gap-2 text-[#00204A]">
              <Newspaper className="h-6 w-6 text-[#C8102E]" />
              <h3 className="text-xl font-bold">Tin tức nổi bật</h3>
            </ScrollReveal>

            <div className="flex flex-col gap-6">
              {/* Large featured card */}
              <ScrollReveal delay={100}>
                <FeaturedNewsCard item={FEATURED_NEWS[0]} />
              </ScrollReveal>

              {/* Small cards grid */}
              <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                {FEATURED_NEWS.slice(1).map((news, idx) => (
                  <ScrollReveal delay={200 + idx * 100} key={news.id}>
                    <SmallNewsCard item={news} />
                  </ScrollReveal>
                ))}
              </div>

              <ScrollReveal delay={400}>
                <a
                  href={TLU_NEWS_URL}
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-2 text-sm font-semibold text-[#C8102E] hover:underline"
                >
                  Xem tất cả tin tức <ArrowRight className="h-4 w-4" />
                </a>
              </ScrollReveal>
            </div>
          </div>

          {/* ── Right: Announcements ───────────────────────────────────── */}
          <div className="lg:col-span-5">
            <ScrollReveal className="mb-6 flex items-center justify-between">
              <div className="flex items-center gap-2 text-[#00204A]">
                <Megaphone className="h-6 w-6 text-rose-500" />
                <h3 className="text-xl font-bold">Thông báo mới</h3>
              </div>
              <a
                href={TLU_ANNOUNCEMENTS_URL}
                target="_blank"
                rel="noreferrer"
                className="flex cursor-pointer items-center gap-1 text-sm font-semibold text-[#C8102E] hover:underline"
              >
                Xem tất cả <ArrowRight className="h-4 w-4" />
              </a>
            </ScrollReveal>

            <ScrollReveal delay={100}>
              <div className="flex flex-col gap-4 rounded-3xl border border-gray-100 bg-white p-6 shadow-sm">
                {PORTAL_ANNOUNCEMENTS.map((item, idx) => (
                  <AnnouncementListItem
                    key={item.id}
                    item={item}
                    isLast={idx === PORTAL_ANNOUNCEMENTS.length - 1}
                  />
                ))}
              </div>
            </ScrollReveal>
          </div>
        </div>
      </div>
    </section>
  );
}
