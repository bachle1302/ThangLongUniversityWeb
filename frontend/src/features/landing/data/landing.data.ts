/**
 * landing.data.ts
 * Static data for the public landing page.
 * Replace with real API calls when backend provides public endpoints.
 */

export const HERO_IMAGES = [
  {
    src: "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?auto=format&fit=crop&w=1920&q=85",
    alt: "Không gian đại học Thăng Long",
  },
  {
    src: "https://images.unsplash.com/photo-1562774053-701939374585?auto=format&fit=crop&w=1920&q=85",
    alt: "Khuôn viên trường đại học hiện đại",
  },
  {
    src: "https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=1920&q=85",
    alt: "Sinh viên học tập và làm việc nhóm",
  },
  {
    src: "https://images.unsplash.com/photo-1517486808906-6ca8b3f04846?auto=format&fit=crop&w=1920&q=85",
    alt: "Cộng đồng sinh viên năng động",
  },
] as const;

export const TLU_NEWS_URL = "https://thanglong.edu.vn/tin-tuc";
export const TLU_ANNOUNCEMENTS_URL = "https://thanglong.edu.vn/thong-bao";

export interface FeaturedNewsItem {
  id: number;
  title: string;
  date: string;
  image: string;
  slug: string;
  href: string;
  featured?: boolean;
}

export const FEATURED_NEWS: FeaturedNewsItem[] = [
  {
    id: 1,
    title: "PHÓ THỦ TƯỚNG HỒ QUỐC DŨNG DỰ LỄ KHAI TRƯƠNG AI LAB CỦA TRƯỜNG ĐẠI HỌC THĂNG LONG",
    date: "15/05/2026",
    image:
      "https://images.unsplash.com/photo-1523580494863-6f3031224c94?auto=format&fit=crop&w=900&q=80",
    slug: "pho-thu-tuong-ho-quoc-dung-du-le-khai-truong-ai-lab",
    href: "https://thanglong.edu.vn/pho-thu-tuong-ho-quoc-dung-du-le-khai-truong-ai-lab-cua-truong-dai-hoc-thang-long-21849.html",
    featured: true,
  },
  {
    id: 2,
    title: "SINH VIÊN TRUYỀN THÔNG ĐA PHƯƠNG TIỆN “ĐỘT NHẬP” TÒA SOẠN VNEXPRESS",
    date: "09/05/2026",
    image:
      "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=600&q=80",
    slug: "sinh-vien-truyen-thong-da-phuong-tien-dot-nhap-toa-soan-vnexpress",
    href: "https://thanglong.edu.vn/sinh-vien-truyen-thong-da-phuong-tien-dot-nhap-toa-soan-vnexpress-hau-truong-mot-tin-tuc-trieu-view-co-gi-21855.html",
  },
  {
    id: 3,
    title: "KHOA KHOA HỌC SỨC KHỎE “BẮT NHỊP” XU HƯỚNG ĐÀO TẠO Y TẾ TRONG KỶ NGUYÊN MỚI",
    date: "09/05/2026",
    image:
      "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?auto=format&fit=crop&w=600&q=80",
    slug: "khoa-khoa-hoc-suc-khoe-bat-nhip-xu-huong-dao-tao-y-te",
    href: "https://thanglong.edu.vn/khoa-khoa-hoc-suc-khoe-bat-nhip-xu-huong-dao-tao-y-te-trong-ky-nguyen-moi-21850.html",
  },
];

export interface AnnouncementItem {
  id: number;
  title: string;
  date: string;
  month: string;
  tag: string;
  href: string;
  isNew?: boolean;
  slug: string;
}

export const PORTAL_ANNOUNCEMENTS: AnnouncementItem[] = [
  {
    id: 1,
    title: "Luận án Tiến sĩ của Nghiên cứu sinh Nguyễn Lâm Tùng (Ngành Toán ứng dụng)",
    date: "29",
    month: "Th05",
    tag: "Thông báo",
    href: "https://thanglong.edu.vn/luan-an-tien-si-cua-nghien-cuu-sinh-nguyen-lam-tung-nganh-toan-ung-dung-21873.html",
    isNew: true,
    slug: "luan-an-tien-si-nguyen-lam-tung",
  },
  {
    id: 2,
    title:
      "THÔNG BÁO Hướng dẫn làm final assignment môn Giảng dạy tiếng Anh chuyên ngành và Lý luận Giảng dạy Tiếng Anh",
    date: "28",
    month: "Th05",
    tag: "Đào tạo",
    href: "https://thanglong.edu.vn/thong-bao-huong-dan-lam-final-assignment-mon-giang-day-tieng-anh-chuyen-nganh-va-ly-luan-giang-day-tieng-anh-danh-cho-sinh-vien-ngon-ngu-anh-hoc-ki-2-nam-2025-2026-21871.html",
    isNew: true,
    slug: "huong-dan-final-assignment-ngon-ngu-anh",
  },
  {
    id: 3,
    title: "KHÁM PHÁ 25 CHƯƠNG TRÌNH ĐÀO TẠO ĐÓN ĐẦU XU HƯỚNG",
    date: "28",
    month: "Th05",
    tag: "Tuyển sinh",
    href: "https://thanglong.edu.vn/kham-pha-25-chuong-trinh-dao-tao-don-dau-xu-huong-21870.html",
    slug: "kham-pha-25-chuong-trinh-dao-tao",
  },
  {
    id: 4,
    title: "THÔNG BÁO hướng dẫn bài tập lớn cuối kì môn Giảng dạy tiếng Anh cho trẻ em",
    date: "27",
    month: "Th05",
    tag: "Đào tạo",
    href: "https://thanglong.edu.vn/thong-bao-huong-dan-bai-tap-lon-cuoi-ki-mon-giang-day-tieng-anh-cho-tre-em-danh-cho-sinh-vien-ngon-ngu-anh-hoc-ki-2-nam-2025-2026-21869.html",
    slug: "huong-dan-bai-tap-lon-giang-day-tieng-anh",
  },
  {
    id: 5,
    title: "THÔNG BÁO Đăng ký học kỳ phụ - Năm học 2025 - 2026",
    date: "27",
    month: "Th05",
    tag: "Đào tạo",
    href: "https://thanglong.edu.vn/thong-bao-dang-ky-hoc-ky-phu-nam-hoc-2025-2026-21868.html",
    slug: "dang-ky-hoc-ky-phu-2025-2026",
  },
];
