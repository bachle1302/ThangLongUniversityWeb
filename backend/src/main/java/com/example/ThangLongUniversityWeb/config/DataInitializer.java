package com.example.ThangLongUniversityWeb.config;

import com.example.ThangLongUniversityWeb.entity.Course;
import com.example.ThangLongUniversityWeb.entity.Department;
import com.example.ThangLongUniversityWeb.entity.Major;
import com.example.ThangLongUniversityWeb.entity.Period;
import com.example.ThangLongUniversityWeb.entity.Room;
import com.example.ThangLongUniversityWeb.entity.SystemSettings;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.enums.CourseType;
import com.example.ThangLongUniversityWeb.enums.Role;
import com.example.ThangLongUniversityWeb.repository.CourseRepository;
import com.example.ThangLongUniversityWeb.repository.DepartmentRepository;
import com.example.ThangLongUniversityWeb.repository.MajorRepository;
import com.example.ThangLongUniversityWeb.repository.PeriodRepository;
import com.example.ThangLongUniversityWeb.repository.RoomRepository;
import com.example.ThangLongUniversityWeb.repository.SystemSettingsRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.service.StudentRetakeService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
@Order(10)
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final PeriodRepository periodRepository;
    private final SystemSettingsRepository systemSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public void run(String... args) {
        seedSystemSettings();
        seedAdmin();
        seedStaticCatalog();
        seedRooms();
        seedPeriods();

        System.out.println("DataInitializer đã seed dữ liệu nền và tài khoản admin.");
    }

    private void seedSystemSettings() {
        systemSettingsRepository.findById(StudentRetakeService.KEY_RETAKE_FEE).orElseGet(() ->
                systemSettingsRepository.save(new SystemSettings(
                        StudentRetakeService.KEY_RETAKE_FEE,
                        String.valueOf(StudentRetakeService.DEFAULT_RETAKE_FEE),
                        "Phí thi lại mỗi môn (VND)"
                )));
        systemSettingsRepository.findById(StudentRetakeService.KEY_MAX_RETAKE_ATTEMPTS).orElseGet(() ->
                systemSettingsRepository.save(new SystemSettings(
                        StudentRetakeService.KEY_MAX_RETAKE_ATTEMPTS,
                        String.valueOf(StudentRetakeService.DEFAULT_MAX_RETAKE_ATTEMPTS),
                        "So lan thi lai toi da moi mon"
                )));
        systemSettingsRepository.findById(StudentRetakeService.KEY_MAX_IMPROVE_ATTEMPTS).orElseGet(() ->
                systemSettingsRepository.save(new SystemSettings(
                        StudentRetakeService.KEY_MAX_IMPROVE_ATTEMPTS,
                        String.valueOf(StudentRetakeService.DEFAULT_MAX_IMPROVE_ATTEMPTS),
                        "So lan thi nang diem toi da moi mon"
                )));
    }

    private void seedAdmin() {
        userRepository.findByUsername("admin")
                .orElseGet(() -> saveUser("admin", "admin@tlu.edu.vn", Role.ADMIN));
    }

    private void seedStaticCatalog() {
        Department cntt = upsertDepartment("CNTT", "Khoa Công nghệ thông tin", "Đào tạo công nghệ phần mềm, dữ liệu và trí tuệ nhân tạo");
        Department kinhTe = upsertDepartment("KT", "Khoa Kinh tế", "Đào tạo kinh tế ứng dụng, tài chính và kiểm toán");
        Department qtkd = upsertDepartment("QTKD", "Khoa Quản trị kinh doanh", "Đào tạo quản trị doanh nghiệp, marketing và nhân sự");
        Department ngoaiNgu = upsertDepartment("NN", "Khoa Ngoại ngữ", "Đào tạo ngôn ngữ Anh, Trung, Nhật trong môi trường học thuật");
        Department duLich = upsertDepartment("DL", "Khoa Du lịch - Khách sạn", "Đào tạo quản trị du lịch, khách sạn và lữ hành");
        Department luat = upsertDepartment("LUAT", "Khoa Luật", "Đào tạo pháp luật kinh tế và pháp luật thương mại");
        Department tkdh = upsertDepartment("TKDH", "Khoa Thiết kế đồ họa", "Đào tạo thiết kế sáng tạo, truyền thông đa phương tiện");

        Major majorCntt = upsertMajor("CNTT", "Công nghệ thông tin", "Đào tạo phát triển phần mềm và hệ thống thông tin", cntt);
        Major majorKhmt = upsertMajor("KHMT", "Khoa học máy tính", "Đào tạo giải thuật, AI và tính toán hiệu năng cao", cntt);
        Major majorAttt = upsertMajor("ATTT", "An toàn thông tin", "Đào tạo an ninh mạng và kiểm thử xâm nhập", cntt);
        Major majorKtp = upsertMajor("KTPM", "Kỹ thuật phần mềm", "Đào tạo kiểm thử, DevOps và quản trị dự án phần mềm", cntt);
        Major majorKt = upsertMajor("KT", "Kinh tế", "Đào tạo kinh tế học ứng dụng", kinhTe);
        Major majorTcnh = upsertMajor("TCNH", "Tài chính ngân hàng", "Đào tạo nghiệp vụ tài chính, ngân hàng, đầu tư", kinhTe);
        Major majorKtdn = upsertMajor("KTDN", "Kinh tế đối ngoại", "Đào tạo xuất nhập khẩu và logistics quốc tế", kinhTe);
        Major majorQtkd = upsertMajor("QTKD", "Quản trị kinh doanh", "Đào tạo quản trị chiến lược và điều hành doanh nghiệp", qtkd);
        Major majorMarketing = upsertMajor("MKT", "Marketing", "Đào tạo truyền thông tích hợp và thương hiệu", qtkd);
        Major majorNna = upsertMajor("NNA", "Ngôn ngữ Anh", "Đào tạo biên phiên dịch và tiếng Anh thương mại", ngoaiNgu);
        Major majorNnt = upsertMajor("NNT", "Ngôn ngữ Trung", "Đào tạo ngôn ngữ Trung thương mại", ngoaiNgu);
        Major majorNh = upsertMajor("NHKS", "Quản trị khách sạn", "Đào tạo nghiệp vụ quản trị khách sạn và nhà hàng", duLich);
        Major majorLh = upsertMajor("LH", "Quản trị lữ hành", "Đào tạo điều hành tour và dịch vụ du lịch", duLich);
        Major majorLkt = upsertMajor("LKT", "Luật kinh tế", "Đào tạo pháp lý cho hoạt động sản xuất kinh doanh", luat);
        Major majorTkdh = upsertMajor("TKDH", "Thiết kế đồ họa", "Đào tạo thiết kế thương hiệu, minh họa số và UI/UX", tkdh);

        List<CourseSeed> courses = List.of(
                // CNTT & KHMT — nền tảng kỹ thuật
                new CourseSeed("MATH1101", "Giải tích 1", 3, "Nền tảng toán học cho kỹ thuật", majorCntt, CourseType.REQUIRED),
                new CourseSeed("MATH1102", "Toán rời rạc", 3, "Logic, quan hệ, đồ thị, tổ hợp", majorCntt, CourseType.REQUIRED),
                new CourseSeed("MATH1201", "Giải tích 2", 3, "Tích phân nhiều biến và chuỗi số", majorCntt, CourseType.REQUIRED, List.of("MATH1101")),
                new CourseSeed("MATH2201", "Xác suất thống kê", 3, "Phân phối xác suất và ước lượng thống kê", majorCntt, CourseType.REQUIRED),
                new CourseSeed("INT2204", "Lập trình hướng đối tượng", 3, "Lập trình Java và nguyên lý OOP", majorCntt, CourseType.REQUIRED),
                new CourseSeed("INT2207", "Cơ sở dữ liệu", 3, "Thiết kế và tối ưu hóa cơ sở dữ liệu quan hệ", majorCntt, CourseType.REQUIRED),
                new CourseSeed("INT2208", "Lập trình Web", 3, "Thiết kế và phát triển ứng dụng web hiện đại", majorCntt, CourseType.REQUIRED),
                new CourseSeed("INT3305", "Lập trình Python", 3, "Python cho phân tích dữ liệu và tự động hóa", majorCntt, CourseType.REQUIRED),
                new CourseSeed("INT2214", "Cấu trúc dữ liệu và giải thuật", 3, "Mảng, cây, đồ thị, tối ưu thuật toán", majorKhmt, CourseType.REQUIRED, List.of("INT2204")),
                new CourseSeed("INT2215", "Hệ điều hành", 3, "Tiến trình, bộ nhớ và quản trị tài nguyên", majorKhmt, CourseType.REQUIRED, List.of("INT2214")),
                new CourseSeed("INT3310", "Trí tuệ nhân tạo", 3, "Học máy cơ bản và ứng dụng AI", majorKhmt, CourseType.REQUIRED, List.of("INT2214")),
                new CourseSeed("INT3312", "Xử lý ngôn ngữ tự nhiên", 3, "Phân tích văn bản và mô hình ngôn ngữ", majorKhmt, CourseType.REQUIRED),
                // ATTT & KTPM
                new CourseSeed("INT2213", "Mạng máy tính", 3, "Kiến trúc mạng và giao thức truyền thông", majorAttt, CourseType.REQUIRED, List.of("INT2204")),
                new CourseSeed("SEC2201", "Mật mã học ứng dụng", 3, "Mã hóa, chữ ký số và giao thức bảo mật", majorAttt, CourseType.REQUIRED),
                new CourseSeed("INT3306", "Linux và Shell Script", 3, "Quản trị hệ thống Linux và tự động hóa", majorAttt, CourseType.REQUIRED),
                new CourseSeed("SEC3301", "An ninh mạng", 3, "Phòng chống tấn công và bảo vệ hạ tầng số", majorAttt, CourseType.REQUIRED, List.of("INT2213", "SEC2201")),
                new CourseSeed("INT3301", "Kiểm thử phần mềm", 3, "Thiết kế test case và tự động hóa kiểm thử", majorKtp, CourseType.REQUIRED, List.of("INT2204")),
                new CourseSeed("INT3302", "Điện toán đám mây", 3, "Triển khai hệ thống trên nền tảng cloud", majorKtp, CourseType.REQUIRED, List.of("INT2208")),
                new CourseSeed("INT3303", "Lập trình ứng dụng di động", 3, "Phát triển app Android và iOS", majorKtp, CourseType.REQUIRED, List.of("INT2204", "INT2208")),
                // Kinh tế & Tài chính
                new CourseSeed("ECON1101", "Kinh tế vi mô", 3, "Hành vi doanh nghiệp và thị trường", majorKt, CourseType.REQUIRED),
                new CourseSeed("ECON1201", "Kinh tế vĩ mô", 3, "Lạm phát, tăng trưởng và chính sách công", majorKt, CourseType.REQUIRED, List.of("ECON1101")),
                new CourseSeed("ECON2101", "Kinh tế phát triển", 3, "Tăng trưởng kinh tế và chính sách phát triển", majorKt, CourseType.REQUIRED),
                new CourseSeed("ECON2201", "Kinh tế lượng", 3, "Mô hình hồi quy và phân tích dữ liệu kinh tế", majorKt, CourseType.REQUIRED),
                new CourseSeed("STAT2101", "Thống kê kinh tế", 3, "Thu thập và phân tích số liệu kinh tế", majorKt, CourseType.REQUIRED),
                new CourseSeed("ACC1101", "Nguyên lý kế toán", 3, "Báo cáo tài chính và nghiệp vụ kế toán", majorTcnh, CourseType.REQUIRED),
                new CourseSeed("ACC2101", "Kế toán quản trị", 3, "Phân tích chi phí và lập ngân sách", majorTcnh, CourseType.REQUIRED, List.of("ACC1101")),
                new CourseSeed("FIN2101", "Thị trường tài chính", 3, "Cấu trúc thị trường vốn và chứng khoán", majorTcnh, CourseType.REQUIRED, List.of("ECON1101")),
                new CourseSeed("FIN2201", "Ngân hàng thương mại", 3, "Nghiệp vụ tín dụng và thanh toán ngân hàng", majorTcnh, CourseType.REQUIRED),
                new CourseSeed("FIN2301", "Quản trị rủi ro tài chính", 3, "Đo lường và kiểm soát rủi ro tài chính", majorTcnh, CourseType.REQUIRED),
                new CourseSeed("FIN3201", "Phân tích đầu tư", 3, "Định giá tài sản và danh mục đầu tư", majorTcnh, CourseType.REQUIRED, List.of("FIN2101", "ACC1101")),
                // Quản trị & Marketing
                new CourseSeed("BUS1101", "Quản trị học", 3, "Quản trị tổ chức và lãnh đạo", majorQtkd, CourseType.REQUIRED),
                new CourseSeed("BUS2201", "Hành vi tổ chức", 3, "Động lực làm việc và văn hóa doanh nghiệp", majorQtkd, CourseType.REQUIRED),
                new CourseSeed("BUS3101", "Quản trị chiến lược", 3, "Phân tích môi trường và chiến lược cạnh tranh", majorQtkd, CourseType.REQUIRED, List.of("BUS1101")),
                new CourseSeed("BUS3202", "Quản trị nhân sự", 3, "Tuyển dụng, đào tạo và đánh giá hiệu suất", majorQtkd, CourseType.REQUIRED),
                new CourseSeed("BUS2101", "Ngoại thương", 3, "Xuất nhập khẩu và thương mại quốc tế", majorKtdn, CourseType.REQUIRED),
                new CourseSeed("BUS3201", "Logistics quốc tế", 3, "Chuỗi cung ứng và vận tải xuyên biên giới", majorKtdn, CourseType.REQUIRED),
                new CourseSeed("MKT1101", "Marketing căn bản", 3, "Nghiên cứu thị trường và hành vi khách hàng", majorMarketing, CourseType.REQUIRED),
                new CourseSeed("MKT2201", "Bán hàng và phân phối", 3, "Kỹ năng bán hàng và quản lý kênh phân phối", majorMarketing, CourseType.REQUIRED),
                new CourseSeed("MKT2202", "Truyền thông số", 3, "Nội dung số và quảng cáo đa nền tảng", majorMarketing, CourseType.REQUIRED, List.of("MKT1101")),
                new CourseSeed("MKT3301", "Chiến lược thương hiệu", 3, "Xây dựng và quản trị thương hiệu dài hạn", majorMarketing, CourseType.REQUIRED, List.of("MKT1101", "MKT2202")),
                // Ngoại ngữ
                new CourseSeed("ENG1101", "Tiếng Anh học thuật", 2, "Kỹ năng đọc viết trong môi trường đại học", majorNna, CourseType.REQUIRED),
                new CourseSeed("ENG2201", "Tiếng Anh thương mại", 2, "Giao tiếp và đàm phán bằng tiếng Anh", majorNna, CourseType.REQUIRED),
                new CourseSeed("ENG2101", "Biên dịch Anh - Việt", 3, "Kỹ năng biên dịch tài liệu chuyên ngành", majorNna, CourseType.REQUIRED, List.of("ENG1101")),
                new CourseSeed("ENG3101", "Phiên dịch chuyên ngành", 3, "Kỹ năng phiên dịch đồng thời và liên tục", majorNna, CourseType.REQUIRED, List.of("ENG1101", "ENG2101")),
                new CourseSeed("CHI1101", "Tiếng Trung 1", 3, "Ngữ pháp và từ vựng tiếng Trung cơ bản", majorNnt, CourseType.REQUIRED),
                new CourseSeed("CHI1201", "Tiếng Trung 2", 3, "Giao tiếp tiếng Trung trung cấp", majorNnt, CourseType.REQUIRED),
                new CourseSeed("CHI2101", "Biên dịch Trung - Việt", 3, "Kỹ năng biên dịch tiếng Trung thương mại", majorNnt, CourseType.REQUIRED),
                new CourseSeed("CHI2201", "Văn hóa Trung Hoa", 2, "Tìm hiểu văn hóa và xã hội Trung Quốc", majorNnt, CourseType.REQUIRED),
                // Du lịch & Khách sạn
                new CourseSeed("TOUR1101", "Giới thiệu ngành du lịch", 2, "Tổng quan ngành du lịch và lữ hành", majorLh, CourseType.REQUIRED),
                new CourseSeed("TOUR2101", "Thiết kế chương trình du lịch", 3, "Xây dựng lịch trình tour và quản trị dịch vụ", majorLh, CourseType.REQUIRED),
                new CourseSeed("TOUR2201", "Marketing du lịch", 3, "Quảng bá điểm đến và sản phẩm du lịch", majorLh, CourseType.REQUIRED),
                new CourseSeed("HOTEL1101", "Nghiệp vụ khách sạn", 3, "Vận hành và dịch vụ khách sạn cơ bản", majorNh, CourseType.REQUIRED),
                new CourseSeed("HOTEL2101", "Quản trị lễ tân", 3, "Nghiệp vụ tiền sảnh khách sạn", majorNh, CourseType.REQUIRED),
                new CourseSeed("HOTEL2201", "Dịch vụ ăn uống", 3, "Quản lý nhà hàng và dịch vụ F&B", majorNh, CourseType.REQUIRED),
                new CourseSeed("HOTEL2301", "Quản trị doanh thu", 3, "Định giá phòng và tối ưu doanh thu khách sạn", majorNh, CourseType.REQUIRED),
                // Luật
                new CourseSeed("LAW1101", "Pháp luật đại cương", 2, "Nền tảng pháp luật Việt Nam", majorLkt, CourseType.REQUIRED),
                new CourseSeed("LAW2101", "Luật lao động", 3, "Quan hệ lao động và chính sách nhân sự", majorLkt, CourseType.REQUIRED),
                new CourseSeed("LAW2201", "Luật thương mại", 3, "Quy định pháp lý trong hợp đồng và giao dịch thương mại", majorLkt, CourseType.REQUIRED, List.of("LAW1101")),
                new CourseSeed("LAW2202", "Sở hữu trí tuệ", 3, "Bảo hộ bản quyền, nhãn hiệu và bằng sáng chế", majorLkt, CourseType.REQUIRED),
                new CourseSeed("LAW3301", "Luật doanh nghiệp", 3, "Thành lập, giải thể và quản trị công ty", majorLkt, CourseType.REQUIRED, List.of("LAW1101", "LAW2201")),
                // Thiết kế đồ họa
                new CourseSeed("DES1101", "Nguyên lý thiết kế đồ họa", 3, "Bố cục, màu sắc và typography", majorTkdh, CourseType.REQUIRED),
                new CourseSeed("DES1201", "Minh họa số", 3, "Kỹ thuật vẽ minh họa trên nền tảng số", majorTkdh, CourseType.REQUIRED),
                new CourseSeed("DES2201", "Thiết kế nhận diện thương hiệu", 3, "Logo, bộ nhận diện và guideline thương hiệu", majorTkdh, CourseType.REQUIRED),
                new CourseSeed("DES2202", "Thiết kế giao diện UI/UX", 3, "Nghiên cứu người dùng và thiết kế tương tác", majorTkdh, CourseType.REQUIRED, List.of("DES1101")),
                new CourseSeed("DES3301", "Motion Graphics", 3, "Thiết kế chuyển động và hoạt hình đồ họa", majorTkdh, CourseType.REQUIRED),
                // Đại cương / tự chọn
                new CourseSeed("GEN1101", "Kỹ năng mềm", 2, "Giao tiếp, thuyết trình và làm việc nhóm", null, CourseType.ELECTIVE),
                new CourseSeed("GEN1102", "Khởi nghiệp đổi mới sáng tạo", 2, "Tư duy sản phẩm và mô hình kinh doanh", null, CourseType.ELECTIVE),
                new CourseSeed("GEN1103", "Phương pháp nghiên cứu khoa học", 2, "Thiết kế nghiên cứu và phân tích dữ liệu", null, CourseType.ELECTIVE),
                new CourseSeed("GEN1104", "Giáo dục thể chất", 2, "Rèn luyện thể lực toàn diện", null, CourseType.ELECTIVE),
                new CourseSeed("GEN1105", "Đạo đức học thuật", 2, "Trung thực học thuật và trách nhiệm nghiên cứu", null, CourseType.ELECTIVE)
        );

        seedCoursesWithPrerequisites(courses);
    }

    private void seedRooms() {
        for (int i = 1; i <= 35; i++) {
            String floor = String.valueOf((i - 1) / 10 + 1);
            String roomNo = String.format("A%s%02d", floor, i);
            upsertRoom("Phòng " + roomNo, 70, "LECTURE", "AVAILABLE");
        }
        for (int i = 1; i <= 15; i++) {
            String roomNo = String.format("LAB%03d", 300 + i);
            upsertRoom("Phòng " + roomNo, 45, "LAB", "AVAILABLE");
        }
    }

    private void seedPeriods() {
        upsertPeriod(1, "07:00", "07:50");
        upsertPeriod(2, "08:00", "08:50");
        upsertPeriod(3, "09:00", "09:50");
        upsertPeriod(4, "10:00", "10:50");
        upsertPeriod(5, "13:00", "13:50");
        upsertPeriod(6, "14:00", "14:50");
        upsertPeriod(7, "15:00", "15:50");
        upsertPeriod(8, "16:00", "16:50");
    }

    private User saveUser(String username, String email, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    private Department upsertDepartment(String code, String name, String description) {
        return departmentRepository.findByDepartmentCode(code).map(existing -> {
            existing.setName(name);
            existing.setDescription(description);
            return departmentRepository.save(existing);
        }).orElseGet(() -> {
            Department department = new Department();
            department.setDepartmentCode(code);
            department.setName(name);
            department.setDescription(description);
            return departmentRepository.save(department);
        });
    }

    private Major upsertMajor(String code, String name, String description, Department department) {
        return majorRepository.findByMajorCode(code).map(existing -> {
            existing.setName(name);
            existing.setDescription(description);
            existing.setDepartment(department);
            return majorRepository.save(existing);
        }).orElseGet(() -> {
            Major major = new Major();
            major.setMajorCode(code);
            major.setName(name);
            major.setDescription(description);
            major.setDepartment(department);
            return majorRepository.save(major);
        });
    }

    private void seedCoursesWithPrerequisites(List<CourseSeed> seeds) {
        Map<String, Course> byCode = new HashMap<>();
        for (CourseSeed seed : seeds) {
            Course course = upsertCourse(seed.code(), seed.name(), seed.credits(), seed.description(), seed.major(), seed.type());
            byCode.put(seed.code(), course);
        }
        for (CourseSeed seed : seeds) {
            if (seed.prerequisiteCodes().isEmpty()) {
                continue;
            }
            Course course = byCode.get(seed.code());
            Set<Course> prerequisites = new HashSet<>();
            for (String prereqCode : seed.prerequisiteCodes()) {
                Course prereq = byCode.get(prereqCode);
                if (prereq == null) {
                    throw new IllegalStateException("Không tìm thấy môn tiên quyết: " + prereqCode + " cho " + seed.code());
                }
                prerequisites.add(prereq);
            }
            course.setPrerequisites(prerequisites);
            courseRepository.save(course);
        }
        evictCourseCaches();
    }

    private void evictCourseCaches() {
        if (cacheManager.getCache("courses") != null) {
            cacheManager.getCache("courses").clear();
        }
        if (cacheManager.getCache("adminDashboard") != null) {
            cacheManager.getCache("adminDashboard").clear();
        }
        if (cacheManager.getCache("classSectionOptions") != null) {
            cacheManager.getCache("classSectionOptions").clear();
        }
    }

    private Course upsertCourse(String code, String name, int credits, String description, Major major, CourseType type) {
        return courseRepository.findByCode(code).map(existing -> {
            existing.setName(name);
            existing.setCredits(credits);
            existing.setDescription(description);
            existing.setMajor(major);
            existing.setCourseType(type);
            return courseRepository.save(existing);
        }).orElseGet(() -> {
            Course course = new Course();
            course.setCode(code);
            course.setName(name);
            course.setCredits(credits);
            course.setDescription(description);
            course.setMajor(major);
            course.setCourseType(type);
            return courseRepository.save(course);
        });
    }

    private Room upsertRoom(String name, int capacity, String type, String status) {
        return roomRepository.findByName(name).map(existing -> {
            existing.setCapacity(capacity);
            existing.setType(type);
            existing.setStatus(status);
            return roomRepository.save(existing);
        }).orElseGet(() -> {
            Room room = new Room();
            room.setName(name);
            room.setCapacity(capacity);
            room.setType(type);
            room.setStatus(status);
            return roomRepository.save(room);
        });
    }

    private Period upsertPeriod(int number, String start, String end) {
        return periodRepository.findByPeriodNumber(number).map(existing -> {
            existing.setStartTime(LocalTime.parse(start));
            existing.setEndTime(LocalTime.parse(end));
            return periodRepository.save(existing);
        }).orElseGet(() -> {
            Period period = new Period();
            period.setPeriodNumber(number);
            period.setStartTime(LocalTime.parse(start));
            period.setEndTime(LocalTime.parse(end));
            return periodRepository.save(period);
        });
    }

    private record CourseSeed(
            String code,
            String name,
            int credits,
            String description,
            Major major,
            CourseType type,
            List<String> prerequisiteCodes
    ) {
        CourseSeed(String code, String name, int credits, String description, Major major, CourseType type) {
            this(code, name, credits, description, major, type, List.of());
        }
    }
}
