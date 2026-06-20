import { Link } from "@tanstack/react-router";
import { ChevronLeft, ChevronRight, Menu, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { HERO_IMAGES } from "../data/landing.data";

const schoolLogo = "/images/LogoThangLongUniversity.png";

// ─── Navbar ──────────────────────────────────────────────────────────────────

export function PublicNavbar() {
  const [isScrolled, setIsScrolled] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => setIsScrolled(window.scrollY > 20);
    window.addEventListener("scroll", handleScroll, { passive: true });
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <nav
      className={`fixed left-0 top-0 z-50 w-full transition-all duration-300 ${
        isScrolled ? "bg-white/95 py-3 shadow-md backdrop-blur-md" : "bg-transparent py-5"
      }`}
    >
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8">
        {/* Logo */}
        <Link to="/" className="flex items-center">
          <img
            src={schoolLogo}
            alt="Logo Đại học Thăng Long"
            className="h-16 w-auto object-contain sm:h-[72px]"
            onError={(e) => {
              (e.currentTarget as HTMLImageElement).style.display = "none";
            }}
          />
          <div
            className={`hidden h-16 w-16 items-center justify-center rounded-xl bg-[#C8102E] font-black text-xl text-white shadow-lg shadow-[#C8102E]/30 fallback-logo sm:h-[72px] sm:w-[72px]`}
          >
            TLU
          </div>
        </Link>

        {/* Desktop nav links */}
        <div
          className={`hidden items-center gap-8 text-sm font-medium transition-colors md:flex ${
            isScrolled ? "text-gray-600" : "text-white/90"
          }`}
        >
          <a href="#news-announcements" className="hover:text-[#C8102E] transition-colors">
            Tin tức
          </a>
          <a href="#support" className="hover:text-[#C8102E] transition-colors">
            Hỗ trợ
          </a>
        </div>

        {/* CTA button */}
        <div className="hidden md:block">
          <Link
            to="/login"
            className="inline-flex items-center gap-2 rounded-full bg-[#C8102E] px-6 py-2.5 text-sm font-semibold text-white shadow-md transition-all duration-300 hover:-translate-y-0.5 hover:bg-[#a00c24] hover:shadow-lg"
          >
            Đăng nhập hệ thống
            <ChevronRight size={16} />
          </Link>
        </div>

        {/* Mobile hamburger */}
        <button
          type="button"
          className={`md:hidden ${isScrolled ? "text-gray-800" : "text-white"}`}
          aria-label="Mở menu"
          onClick={() => setIsOpen((v) => !v)}
        >
          {isOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Mobile menu */}
      {isOpen && (
        <div className="absolute left-0 top-full w-full border-t border-gray-100 bg-white px-4 py-4 shadow-xl md:hidden">
          <div className="flex flex-col gap-3">
            <a
              href="#news-announcements"
              className="rounded-lg p-3 font-medium text-gray-700 hover:bg-gray-50"
              onClick={() => setIsOpen(false)}
            >
              Tin tức
            </a>
            <a
              href="#support"
              className="rounded-lg p-3 font-medium text-gray-700 hover:bg-gray-50"
              onClick={() => setIsOpen(false)}
            >
              Hỗ trợ
            </a>
            <Link
              to="/login"
              className="mt-2 rounded-xl bg-[#C8102E] px-6 py-3 text-center font-semibold text-white"
              onClick={() => setIsOpen(false)}
            >
              Đăng nhập hệ thống
            </Link>
          </div>
        </div>
      )}
    </nav>
  );
}

// ─── Hero Carousel ────────────────────────────────────────────────────────────

export function HeroCarousel() {
  const [current, setCurrent] = useState(0);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const startTimer = () => {
    if (timerRef.current) clearInterval(timerRef.current);
    timerRef.current = setInterval(() => {
      setCurrent((prev) => (prev + 1) % HERO_IMAGES.length);
    }, 5000);
  };

  useEffect(() => {
    startTimer();
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  const goTo = (index: number) => {
    setCurrent(index);
    startTimer();
  };
  const prev = () => goTo((current - 1 + HERO_IMAGES.length) % HERO_IMAGES.length);
  const next = () => goTo((current + 1) % HERO_IMAGES.length);

  return (
    <section className="group relative h-screen w-full overflow-hidden bg-black">
      {/* Images */}
      {HERO_IMAGES.map((img, index) => (
        <div
          key={img.src}
          className={`absolute inset-0 transition-opacity duration-1000 ease-in-out ${
            index === current ? "z-10 opacity-100" : "z-0 opacity-0"
          }`}
        >
          <img
            src={img.src}
            alt={img.alt}
            className={`h-full w-full object-cover object-center transition-transform duration-[10000ms] ease-linear ${
              index === current ? "scale-105" : "scale-100"
            }`}
          />
        </div>
      ))}

      {/* Gradient overlay — makes navbar text readable */}
      <div className="absolute inset-0 z-20 bg-gradient-to-b from-black/60 via-black/10 to-transparent pointer-events-none" />

      {/* Prev / Next buttons */}
      <button
        type="button"
        onClick={prev}
        aria-label="Ảnh trước"
        className="absolute left-4 top-1/2 z-30 -translate-y-1/2 rounded-full bg-black/30 p-3 text-white opacity-0 transition-all duration-300 hover:bg-[#C8102E] group-hover:opacity-100"
      >
        <ChevronLeft size={28} />
      </button>
      <button
        type="button"
        onClick={next}
        aria-label="Ảnh tiếp theo"
        className="absolute right-4 top-1/2 z-30 -translate-y-1/2 rounded-full bg-black/30 p-3 text-white opacity-0 transition-all duration-300 hover:bg-[#C8102E] group-hover:opacity-100"
      >
        <ChevronRight size={28} />
      </button>

      {/* Dot indicators */}
      <div className="absolute bottom-8 left-1/2 z-30 flex -translate-x-1/2 items-center gap-3">
        {HERO_IMAGES.map((_, index) => (
          <button
            key={index}
            type="button"
            aria-label={`Chuyển sang ảnh ${index + 1}`}
            onClick={() => goTo(index)}
            className={`h-2.5 rounded-full transition-all duration-300 ${
              index === current ? "w-8 bg-[#C8102E]" : "w-2.5 bg-white/60 hover:bg-white"
            }`}
          />
        ))}
      </div>
    </section>
  );
}
