import { Link } from "@tanstack/react-router";
import { Mail, MapPin, Phone, Shield, Smartphone } from "lucide-react";

const schoolLogo = "/images/LogoThangLongUniversity.png";

export function SupportFooter() {
  return (
    <footer id="support" className="border-t border-gray-100 bg-white pb-10 pt-20">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="mb-16 grid grid-cols-1 gap-12 md:grid-cols-12">
          {/* Brand column */}
          <div className="md:col-span-5">
            <div className="mb-6 flex items-center gap-3">
              <img
                src={schoolLogo}
                alt="Logo Đại học Thăng Long"
                className="h-10 w-10 object-contain"
                onError={(e) => {
                  const el = e.currentTarget as HTMLImageElement;
                  el.style.display = "none";
                  const fallback = el.nextElementSibling as HTMLElement | null;
                  if (fallback) fallback.style.display = "flex";
                }}
              />
              <div
                className="hidden h-10 w-10 items-center justify-center rounded-xl bg-[#C8102E] font-black text-xl text-white"
                style={{ display: "none" }}
              >
                TLU
              </div>
              <span className="text-xl font-bold text-[#00204A]">Đại học Thăng Long</span>
            </div>
            <p className="mb-6 max-w-sm text-sm leading-relaxed text-gray-500">
              Cổng thông tin sinh viên nội bộ. Hệ thống được quản trị và phát triển bởi Trung tâm
              CNTT – Đại học Thăng Long.
            </p>
            <div className="flex gap-3">
              <div className="flex h-10 w-10 cursor-pointer items-center justify-center rounded-full bg-gray-100 text-gray-500 transition-colors hover:bg-[#C8102E] hover:text-white">
                <Smartphone size={18} />
              </div>
              <div className="flex h-10 w-10 cursor-pointer items-center justify-center rounded-full bg-gray-100 text-gray-500 transition-colors hover:bg-[#00204A] hover:text-white">
                <Shield size={18} />
              </div>
            </div>
          </div>

          {/* Links + Contact */}
          <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 md:col-span-7">
            {/* Quick support */}
            <div>
              <h4 className="mb-4 font-bold text-[#00204A]">Hỗ trợ nhanh</h4>
              <ul className="space-y-3 text-sm text-gray-500">
                {[
                  "Quên mật khẩu?",
                  "Cấp lại tài khoản",
                  "Báo lỗi hệ thống",
                  "Câu hỏi thường gặp",
                ].map((item) => (
                  <li key={item} className="cursor-pointer transition-colors hover:text-[#C8102E]">
                    {item}
                  </li>
                ))}
              </ul>
            </div>

            {/* Contact card */}
            <div className="rounded-2xl bg-blue-50 p-6">
              <h4 className="mb-2 font-bold text-blue-900">Phòng Đào tạo</h4>
              <p className="mb-4 text-sm text-blue-700">
                Giải đáp thắc mắc về điểm, đăng ký môn và lịch thi.
              </p>
              <div className="flex items-center gap-2 text-sm text-blue-900">
                <Phone className="h-4 w-4" />
                <span className="font-bold">024 3858 7346 (Phím 1)</span>
              </div>
              <div className="mt-2 flex items-center gap-2 text-sm text-blue-700">
                <MapPin className="h-4 w-4 shrink-0" />
                <span>Nghiêm Xuân Yêm, Đại Kim, Hoàng Mai, Hà Nội</span>
              </div>
              <div className="mt-2 flex items-center gap-2 text-sm text-blue-700">
                <Mail className="h-4 w-4" />
                <span>info@thanglong.edu.vn</span>
              </div>
            </div>
          </div>
        </div>

        {/* Bottom bar */}
        <div className="flex flex-col items-center justify-between gap-4 border-t border-gray-100 pt-8 text-sm text-gray-400 md:flex-row">
          <p>© 2026 Thang Long University. All rights reserved.</p>
          <div className="flex gap-6">
            <Link to="/login" className="hover:text-gray-600">
              Cổng sinh viên
            </Link>
            <a href="#" className="hover:text-gray-600">
              Điều khoản sử dụng
            </a>
            <a href="#" className="hover:text-gray-600">
              Chính sách bảo mật
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
