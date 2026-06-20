import { HeroCarousel, PublicNavbar } from "./components/PublicNavbar";
import { NewsAnnouncementSection } from "./components/NewsAnnouncementSection";
import { SupportFooter } from "./components/SupportFooter";

export function ThangLongLanding() {
  return (
    <div className="min-h-screen bg-white font-sans selection:bg-[#C8102E] selection:text-white">
      <PublicNavbar />
      <HeroCarousel />
      <NewsAnnouncementSection />
      <SupportFooter />
    </div>
  );
}

export default ThangLongLanding;
