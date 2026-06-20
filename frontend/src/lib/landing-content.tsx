import { createContext, useContext, useEffect, useState, type ReactNode } from "react";

export interface LandingNews {
  id: string;
  title: string;
  date: string;
  excerpt: string;
}
export interface LandingProgram {
  id: string;
  name: string;
  description: string;
  duration: string;
}
export interface LandingStat {
  label: string;
  value: string;
}

export interface LandingContent {
  heroTitle: string;
  heroSubtitle: string;
  heroCtaPrimary: string;
  heroCtaSecondary: string;
  aboutTitle: string;
  aboutBody: string;
  stats: LandingStat[];
  programs: LandingProgram[];
  news: LandingNews[];
  contactAddress: string;
  contactPhone: string;
  contactEmail: string;
  admissionsHotline: string;
  admissionsDeadline: string;
}

export const defaultContent: LandingContent = {
  heroTitle: "Đại học Thăng Long — Kiến tạo tri thức, dẫn dắt tương lai",
  heroSubtitle:
    "Hơn 35 năm tiên phong giáo dục đại học ngoài công lập tại Việt Nam, đào tạo nguồn nhân lực chất lượng cao trong các lĩnh vực Công nghệ, Kinh tế, Y khoa và Ngôn ngữ.",
  heroCtaPrimary: "Tìm hiểu tuyển sinh",
  heroCtaSecondary: "Cổng sinh viên",
  aboutTitle: "Về Trường Đại học Thăng Long",
  aboutBody:
    "Thành lập năm 1988, Đại học Thăng Long là trường đại học ngoài công lập đầu tiên của Việt Nam. Với triết lý lấy người học làm trung tâm, nhà trường không ngừng đổi mới chương trình đào tạo, ứng dụng công nghệ và hợp tác quốc tế nhằm mang lại trải nghiệm học thuật hiện đại, gắn liền thực tiễn cho hơn 12,000 sinh viên mỗi năm.",
  stats: [
    { label: "Sinh viên đang theo học", value: "12,400+" },
    { label: "Giảng viên cơ hữu", value: "480+" },
    { label: "Chương trình đào tạo", value: "32" },
    { label: "Đối tác quốc tế", value: "60+" },
  ],
  programs: [
    {
      id: "p1",
      name: "Công nghệ Thông tin",
      description: "Lập trình, AI, An toàn thông tin, Khoa học dữ liệu.",
      duration: "4 năm",
    },
    {
      id: "p2",
      name: "Kinh tế & Quản trị",
      description: "Quản trị Kinh doanh, Tài chính – Ngân hàng, Marketing.",
      duration: "4 năm",
    },
    {
      id: "p3",
      name: "Ngôn ngữ",
      description: "Ngôn ngữ Anh, Trung, Nhật, Hàn theo chuẩn quốc tế.",
      duration: "4 năm",
    },
    {
      id: "p4",
      name: "Khoa học Sức khỏe",
      description: "Điều dưỡng, Y tế công cộng, Công tác xã hội.",
      duration: "4 năm",
    },
  ],
  news: [
    {
      id: "n1",
      title: "Thông báo tuyển sinh đại học chính quy 2025",
      date: "2025-04-12",
      excerpt: "Nhà trường công bố phương án tuyển sinh năm 2025 với 6 phương thức xét tuyển.",
    },
    {
      id: "n2",
      title: "Hợp tác chiến lược với Đại học Tokyo",
      date: "2025-03-28",
      excerpt: "Mở rộng chương trình trao đổi sinh viên và nghiên cứu chung trong lĩnh vực AI.",
    },
    {
      id: "n3",
      title: "Khai mạc Tuần lễ Khởi nghiệp TLU 2025",
      date: "2025-03-10",
      excerpt: "Hơn 60 dự án sinh viên tranh tài tại sự kiện thường niên lớn nhất nhà trường.",
    },
  ],
  contactAddress: "Nghiêm Xuân Yêm, Đại Kim, Hoàng Mai, Hà Nội",
  contactPhone: "024 3858 7346",
  contactEmail: "info@thanglong.edu.vn",
  admissionsHotline: "1900 1582",
  admissionsDeadline: "30/06/2025",
};

const KEY = "tlu-landing-content";
const Ctx = createContext<{
  content: LandingContent;
  update: (patch: Partial<LandingContent>) => void;
  reset: () => void;
} | null>(null);

export function LandingContentProvider({ children }: { children: ReactNode }) {
  const [content, setContent] = useState<LandingContent>(defaultContent);
  useEffect(() => {
    try {
      const raw = localStorage.getItem(KEY);
      if (raw) setContent({ ...defaultContent, ...JSON.parse(raw) });
    } catch {}
  }, []);
  const update = (patch: Partial<LandingContent>) => {
    setContent((c) => {
      const next = { ...c, ...patch };
      try {
        localStorage.setItem(KEY, JSON.stringify(next));
      } catch {}
      return next;
    });
  };
  const reset = () => {
    try {
      localStorage.removeItem(KEY);
    } catch {}
    setContent(defaultContent);
  };
  return <Ctx.Provider value={{ content, update, reset }}>{children}</Ctx.Provider>;
}

export function useLanding() {
  const v = useContext(Ctx);
  if (!v) throw new Error("LandingContentProvider missing");
  return v;
}
