package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeds the knowledge base with TLU data from deep-research-report.md on first startup.
 * Skips documents whose content hasn't changed (same SHA-256 hash).
 *
 * Run order 100 so it runs after all other beans are fully initialized.
 */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class KnowledgeSeedService implements CommandLineRunner {

    private final DocumentIngestionService ingestionService;
    private final KnowledgeDocumentRepository documentRepository;

    @Override
    public void run(String... args) {
        log.info("Checking knowledge base seed status...");
        seedAll();
        log.info("Knowledge base seed complete. Total documents: {}",
                documentRepository.count());
    }

    private void seedAll() {
        seedDepartmentContacts();
        seedAdmissions2026();
        seedAcademicRegulations();
        seedSchoolInfo();
        seedProgramDetails();
    }

    // ── 1. Department contacts (Priority 2 – official department pages) ──────

    private void seedDepartmentContacts() {
        ingestionService.ingestText(
                "Đầu mối liên hệ các phòng ban Trường Đại học Thăng Long",
                """
## Phòng Đào tạo
Nhiệm vụ: Kế hoạch đào tạo, tổ chức giảng dạy, học tập, tốt nghiệp, tuyển sinh.
Liên hệ: 024 9999 1988 nhánh 2 | p.daotao@thanglong.edu.vn | Tầng 1 Nhà A

## Phòng Công tác Chính trị Sinh viên (CTSV)
Nhiệm vụ: Đầu mối tiếp nhận và giải quyết thủ tục hành chính sinh viên, khen thưởng, kỷ luật, tư vấn hỗ trợ sinh viên, hỗ trợ một cửa.
Liên hệ: 024 9999 1988 nhánh 3 | p.ctsv@thanglong.edu.vn | Tầng 1 Nhà A

## Phòng Tài chính - Kế toán (Tài vụ)
Nhiệm vụ: Thu chi, học phí, quy định tài chính liên quan người học.
Liên hệ: 024 9999 1988 nhánh 4 | p.taivu@thanglong.edu.vn | Tầng 1 Nhà A

## Phòng Thông tin tư liệu - Thư viện
Nhiệm vụ: Mượn/gia hạn tài liệu, hỗ trợ tra cứu, phòng học nhóm, phòng học cá nhân.
Liên hệ: 024 3559 2376 | thuvien@thanglong.edu.vn | Tầng 1 tòa nhà Thư viện

## Trung tâm E-learning
Nhiệm vụ: Hệ E-learning, hỗ trợ học trực tuyến, Office 365, các môn đại cương trên hệ thống.
Liên hệ: tt.elearning@thanglong.edu.vn | elearning.helponline@thanglong.edu.vn | Tầng 2 Nhà A

## Phòng Công nghệ Thông tin (CNTT)
Nhiệm vụ: Mạng, thiết bị giảng dạy, tài khoản mail, Office 365, phòng máy, hệ thống thi trắc nghiệm.
Liên hệ: 024 9999 1988 nhánh 10 | p.cntt@thanglong.edu.vn | A701 Nhà A

## Trung tâm Đảm bảo Chất lượng và Khảo thí
Nhiệm vụ: Thi quá trình, kết thúc học phần, phúc khảo, chuẩn đầu ra tiếng Anh.
Liên hệ: 024 9999 1988 nhánh 122 | tt.dbcl@thanglong.edu.vn | A302 Nhà A

## Phòng Hợp tác Quốc tế (HTQT)
Nhiệm vụ: Hợp tác, tuyển sinh, sinh viên đi trao đổi, chương trình liên kết quốc tế.
Liên hệ: 024 9999 1988 nhánh 8 | sic.tlu@thanglong.edu.vn | Tầng 1 Nhà A

## Văn phòng trường
Nhiệm vụ: Lễ tân, phòng trực giáo viên, trạm y tế, bảo vệ, điều phối hành chính chung.
Liên hệ: nhánh 101 lễ tân | 115 y tế | 113 bảo vệ | p.hanhchinh@thanglong.edu.vn | Tầng 1 Nhà A
""",
                "https://thanglong.edu.vn",
                "WEBSITE",
                2
        );
    }

    // ── 2. Admissions 2026 (Priority 1 – newest announcement) ────────────────

    private void seedAdmissions2026() {
        ingestionService.ingestText(
                "Thông tin tuyển sinh Đại học Thăng Long năm 2026",
                """
## Tuyển sinh đại học chính quy 2026 – Tổng quan
Mã trường: DTL
Hotline tuyển sinh: 024 9999 1988
Website: thanglong.edu.vn
Hình thức: 10 lĩnh vực, 25 ngành đào tạo hệ đại học chính quy.

## Các ngành đào tạo và học phí 2026

### Nhóm Công nghệ thông tin & Kỹ thuật
- Công nghệ thông tin (Mã: 7480201): 4 năm | Tổ hợp A00, A01, D01, D07, X06, X26 | Học phí 40,8 triệu/năm
- Trí tuệ nhân tạo (Mã: 7480207): 4 năm | Tổ hợp A00, A01, D01, D07, X06, X26 | Học phí 40,2 triệu/năm

### Nhóm Kinh tế – Quản trị
- Marketing (Mã: 7340115): 4 năm | Tổ hợp A00, A01, D01, D07, X01, X25 | Học phí 39,6 triệu/năm
- Kinh tế quốc tế (Mã: 7310106): 4 năm | Tổ hợp A00, A01, D01, D07, X01, X25 | Học phí 37,8 triệu/năm
- Kế toán (Mã: 7340301): 4 năm | Tổ hợp A00, A01, D01, D07, X01, X25 | Học phí 38,9 triệu/năm
- Quản trị kinh doanh (Mã: 7340101): 4 năm | Học phí ~38-40 triệu/năm
- Tài chính – Ngân hàng (Mã: 7340201): 4 năm | Học phí ~38-40 triệu/năm

### Nhóm Ngoại ngữ
- Ngôn ngữ Anh (Mã: 7220201): 4 năm | Tổ hợp D01, D14, D15 | Học phí 40,9 triệu/năm
- Ngôn ngữ Trung Quốc (Mã: 7220204): 4 năm | Học phí ~40 triệu/năm

### Nhóm Du lịch – Dịch vụ
- Quản trị khách sạn (Mã: 7810201): 4 năm | Tổ hợp A00, A01, A07, D01, D09, D10 | Học phí 40,9 triệu/năm
- Quản trị dịch vụ du lịch – lữ hành (Mã: 7810103): 4 năm | Tổ hợp A00, A01, A07, D01, D09, D10 | Học phí 43,8 triệu/năm

### Nhóm Khoa học sức khỏe
- Điều dưỡng (Mã: 7720301): 4 năm
- Y tế công cộng (Mã: 7720701): 4 năm

### Nhóm Truyền thông – Nghệ thuật
- Truyền thông đa phương tiện (Mã: 7320104): 4 năm
- Thiết kế đồ họa (Mã: 7210403): 4 năm

## Lưu ý quan trọng
- Học phí trên là học phí năm học 2026, có thể điều chỉnh theo từng năm.
- Thông tin điểm trúng tuyển các năm trước: liên hệ Phòng Đào tạo hoặc xem trang tuyển sinh trường.
- Thủ tục nhập học: theo thông báo chính thức của Phòng Đào tạo sau khi công bố điểm.
""",
                "https://thanglong.edu.vn/tuyen-sinh",
                "ANNOUNCEMENT",
                1
        );
    }

    // ── 3. Academic regulations (Priority 4 – student handbook) ──────────────

    private void seedAcademicRegulations() {
        ingestionService.ingestText(
                "Quy chế học vụ – Sổ tay sinh viên Đại học Thăng Long",
                """
## Đăng ký học và tín chỉ
- Học kỳ 1 năm nhất: học theo TKB nhà trường, chỉ đăng ký môn tiếng Anh.
- Từ học kỳ 2 năm nhất trở đi: tự đăng ký học phần.
- Số tín chỉ mỗi kỳ: tối thiểu 12, tối đa 18 tín chỉ.
- Mở đăng ký học: khoảng 2 tuần trước khi học kỳ bắt đầu.
- Toàn khóa: ~140 tín chỉ (~50 học phần).
- Cố vấn học tập: mỗi sinh viên được phân công 1 cố vấn phụ trách tư vấn chọn môn.

## Giờ học trong ngày
- Giờ 1: 07:00–07:50
- Giờ 2: 07:55–08:45
- Giờ 3: 08:50–09:40
- Giờ 4: 09:45–10:35
- Giờ 5: 10:45–11:35
- Giờ 6: 11:40–12:30
- Giờ 7: 12:45–13:35
- Giờ 8: 13:40–14:30
- Giờ 9: 14:35–15:25
- Giờ 10: 15:35–16:25
- Giờ 11: 16:30–17:20
- Giờ 12: 18:00–18:50
- Giờ 13: 18:55–19:45 (tối đa đến 20:50)

## Điều kiện thi cuối kỳ
- Vắng > 30% tổng số tiết → KHÔNG được dự thi, phải học lại.
- Điểm quá trình < 4 → KHÔNG được dự thi, phải học lại.
- Điểm quá trình ≥ 4 và điểm thi < 4 → được thi lại 1 lần; điểm tối đa sau thi lại là 7.

## Học lại và học cải thiện
- Học lại: áp dụng khi trượt môn (điểm tổng kết < 4 và không được thi lại, hoặc đã thi lại vẫn < 4).
- Học cải thiện: áp dụng khi điểm tổng kết từ 4 đến 5,4; điểm cải thiện cao hơn thì thay thế điểm cũ.
- Đăng ký thi lại: tuần 5–7 của học kỳ.

## Cảnh báo học tập
- Sau 1 năm (2 học kỳ): tích lũy < 14 tín chỉ → cảnh báo lần 1.
- Sau 2 năm (4 học kỳ): tích lũy < 36 tín chỉ → cảnh báo lần 2.
- Sau 3 năm (6 học kỳ): tích lũy < 62 tín chỉ → cảnh báo lần 3.
- Hai lần cảnh báo liên tiếp → xem xét buộc thôi học.

## Buộc thôi học
- Cảnh báo học tập 2 lần liên tiếp.
- Không hoàn thành chương trình trong thời hạn tối đa 8 năm.
- Vi phạm kỷ luật nghiêm trọng theo quy định.

## Chuyển ngành, bảo lưu, học hai ngành
- Chuyển ngành: phải đáp ứng điều kiện đầu vào của ngành mới; nộp đơn tại Phòng Đào tạo.
- Bảo lưu kết quả học tập: sinh viên có thể xin bảo lưu trong trường hợp đặc biệt; nộp đơn tại Phòng CTSV.
- Học cùng lúc hai chương trình (song ngành): được phép sau khi hoàn thành đủ điều kiện theo quy định.

## Điều kiện tốt nghiệp
- GPA tích lũy ≥ 5,0 (thang điểm 10).
- Hoàn thành đủ số tín chỉ yêu cầu của chương trình đào tạo (~140 tín chỉ).
- Đạt chuẩn đầu ra ngoại ngữ: B1 tiếng Anh (hoặc tương đương).
- Đạt tiêu chuẩn Giáo dục quốc phòng (GDQP) và Giáo dục thể chất (GDTC).

## Xếp loại tốt nghiệp (thang điểm 10)
- Xuất sắc: GPA ≥ 9,0
- Giỏi: GPA ≥ 8,0
- Khá: GPA ≥ 7,0
- Trung bình khá: GPA ≥ 6,0
- Trung bình: GPA ≥ 5,0

## Học bổng
- Học bổng 15/12: dành cho sinh viên có kết quả học tập xuất sắc, trao vào dịp kỷ niệm ngày thành lập trường.
- Học bổng khuyến khích học tập: trao mỗi học kỳ cho sinh viên có GPA cao nhất lớp/khoa.
- Điều kiện: không bị kỷ luật, không có môn điểm dưới trung bình trong kỳ xét học bổng.
- Chi tiết: liên hệ Phòng CTSV.
""",
                "https://thanglong.edu.vn/so-tay-sinh-vien",
                "HANDBOOK",
                4
        );
    }

    // ── 4. General school info (Priority 2 – official pages) ─────────────────

    private void seedSchoolInfo() {
        ingestionService.ingestText(
                "Giới thiệu Trường Đại học Thăng Long",
                """
## Tổng quan
Trường Đại học Thăng Long (Thang Long University – TLU) là cơ sở giáo dục đại học ngoài công lập, đa ngành, định hướng ứng dụng tại Hà Nội, Việt Nam.

## Lịch sử thành lập
- 15/12/1988: Thành lập Trung tâm Đại học dân lập Thăng Long – mốc lịch sử đầu tiên của hệ thống đại học ngoài công lập tại Việt Nam.
- 1994: Đổi tên thành Trường Đại học Dân lập Thăng Long.
- 2007: Chuyển đổi thành Trường Đại học Tư thục Thăng Long.
- 2008: Chuyển đến cơ sở mới trên đường Nghiêm Xuân Yêm.

## Địa chỉ và liên hệ chung
- Địa chỉ: Đường Nghiêm Xuân Yêm, Đại Kim, Hoàng Mai, Hà Nội.
- Hotline: 024 9999 1988 | 024 3858 7346
- Email: info@thanglong.edu.vn
- Website: thanglong.edu.vn

## Cơ cấu đào tạo
Trường có 8 khoa đào tạo:
- Khoa Công nghệ thông tin (còn gọi Khoa Toán – Tin học)
- Khoa Kinh tế – Quản lý
- Khoa Khoa học sức khỏe
- Khoa Ngoại ngữ
- Khoa Khoa học xã hội và nhân văn
- Khoa Du lịch
- Khoa Truyền thông Đa phương tiện
- Khoa Âm nhạc ứng dụng

Ngoài ra còn có các khoa/bộ môn bổ trợ: Bộ môn Giáo dục quốc phòng, Bộ môn Giáo dục thể chất, Trung tâm Ngoại ngữ.

## Kênh thông tin chính thức
- Website: thanglong.edu.vn
- Facebook: facebook.com/DaiHocThangLong
- Thông báo học vụ: cổng thông tin sinh viên nội bộ
""",
                "https://thanglong.edu.vn/gioi-thieu",
                "WEBSITE",
                2
        );
    }

    // ── 5. Program-level details (Priority 3 – program pages) ────────────────

    private void seedProgramDetails() {
        ingestionService.ingestText(
                "Chi tiết ngành đào tạo Khoa Công nghệ thông tin – Trường Đại học Thăng Long",
                """
## Ngành Công nghệ thông tin
- Mã ngành: 7480201
- Thời gian đào tạo: 4 năm
- Tổ hợp xét tuyển: A00 (Toán–Lý–Hóa), A01 (Toán–Lý–Anh), D01 (Văn–Toán–Anh), D07 (Toán–Hóa–Anh), X06, X26
- Học phí: 40.800.000 đồng/năm học (2026)
- Chuẩn đầu ra tiếng Anh: B1 (CEFR)
- Cơ hội nghề nghiệp: lập trình viên, kỹ sư phần mềm, DevOps, QA/QC, BA, nhà phân tích dữ liệu

## Ngành Trí tuệ nhân tạo
- Mã ngành: 7480207
- Thời gian đào tạo: 4 năm
- Tổ hợp xét tuyển: A00, A01, D01, D07, X06, X26
- Học phí: 40.200.000 đồng/năm học (2026)
- Chuẩn đầu ra tiếng Anh: B1 (CEFR)
- Cơ hội nghề nghiệp: kỹ sư AI/ML, data scientist, NLP engineer, computer vision engineer

## Ngành Marketing
- Mã ngành: 7340115
- Thời gian đào tạo: 4 năm
- Tổ hợp xét tuyển: A00, A01, D01, D07, X01, X25
- Học phí: 39.600.000 đồng/năm học (2026)
- Cơ hội nghề nghiệp: marketing manager, content creator, digital marketer, brand manager

## Ngành Kinh tế quốc tế
- Mã ngành: 7310106
- Thời gian đào tạo: 4 năm
- Tổ hợp xét tuyển: A00, A01, D01, D07, X01, X25
- Học phí: 37.800.000 đồng/năm học (2026)

## Ngành Kế toán
- Mã ngành: 7340301
- Thời gian đào tạo: 4 năm
- Tổ hợp xét tuyển: A00, A01, D01, D07, X01, X25
- Học phí: 38.900.000 đồng/năm học (2026)

## Ngành Ngôn ngữ Anh
- Mã ngành: 7220201
- Thời gian đào tạo: 4 năm
- Tổ hợp xét tuyển: D01 (Văn–Toán–Anh), D14 (Văn–Sử–Anh), D15 (Văn–Địa–Anh)
- Học phí: 40.900.000 đồng/năm học (2026)

## Ngành Quản trị khách sạn
- Mã ngành: 7810201
- Thời gian đào tạo: 4 năm
- Tổ hợp xét tuyển: A00, A01, A07, D01, D09, D10
- Học phí: 40.900.000 đồng/năm học (2026)

## Ngành Quản trị dịch vụ du lịch – lữ hành
- Mã ngành: 7810103
- Thời gian đào tạo: 4 năm
- Tổ hợp xét tuyển: A00, A01, A07, D01, D09, D10
- Học phí: 43.800.000 đồng/năm học (2026)
""",
                "https://thanglong.edu.vn/dao-tao",
                "WEBSITE",
                3
        );
    }
}
