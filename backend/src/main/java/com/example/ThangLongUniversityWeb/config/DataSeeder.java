package com.example.ThangLongUniversityWeb.config;

import com.example.ThangLongUniversityWeb.entity.AttendanceRecord;
import com.example.ThangLongUniversityWeb.entity.AttendanceSession;
import com.example.ThangLongUniversityWeb.entity.ClassSection;
import com.example.ThangLongUniversityWeb.entity.ClassSectionSchedule;
import com.example.ThangLongUniversityWeb.entity.Course;
import com.example.ThangLongUniversityWeb.entity.Department;
import com.example.ThangLongUniversityWeb.entity.Enrollment;
import com.example.ThangLongUniversityWeb.entity.ExamRegistration;
import com.example.ThangLongUniversityWeb.entity.Grade;
import com.example.ThangLongUniversityWeb.entity.Homeroom;
import com.example.ThangLongUniversityWeb.entity.Major;
import com.example.ThangLongUniversityWeb.entity.Period;
import com.example.ThangLongUniversityWeb.entity.Room;
import com.example.ThangLongUniversityWeb.entity.Semester;
import com.example.ThangLongUniversityWeb.entity.Student;
import com.example.ThangLongUniversityWeb.entity.Teacher;
import com.example.ThangLongUniversityWeb.entity.TuitionBill;
import com.example.ThangLongUniversityWeb.entity.User;
import com.example.ThangLongUniversityWeb.enums.AttendanceStatus;
import com.example.ThangLongUniversityWeb.enums.ClassSectionStatus;
import com.example.ThangLongUniversityWeb.enums.CourseStudyStatus;
import com.example.ThangLongUniversityWeb.enums.CourseType;
import com.example.ThangLongUniversityWeb.enums.EnrollmentStatus;
import com.example.ThangLongUniversityWeb.enums.EnrollmentType;
import com.example.ThangLongUniversityWeb.enums.Role;
import com.example.ThangLongUniversityWeb.enums.TeacherStatus;
import com.example.ThangLongUniversityWeb.repository.AttendanceRecordRepository;
import com.example.ThangLongUniversityWeb.repository.AttendanceSessionRepository;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.CourseRepository;
import com.example.ThangLongUniversityWeb.repository.DepartmentRepository;
import com.example.ThangLongUniversityWeb.repository.EnrollmentRepository;
import com.example.ThangLongUniversityWeb.repository.ExamRegistrationRepository;
import com.example.ThangLongUniversityWeb.repository.GradeRepository;
import com.example.ThangLongUniversityWeb.repository.HomeroomRepository;
import com.example.ThangLongUniversityWeb.repository.MajorRepository;
import com.example.ThangLongUniversityWeb.repository.PeriodRepository;
import com.example.ThangLongUniversityWeb.repository.RoomRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.StudentRepository;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import com.example.ThangLongUniversityWeb.repository.ExamSessionRepository;
import com.example.ThangLongUniversityWeb.repository.TuitionBillRepository;
import com.example.ThangLongUniversityWeb.repository.UserRepository;
import com.example.ThangLongUniversityWeb.dto.request.ExamSessionRequest;
import com.example.ThangLongUniversityWeb.service.AcademicResultService;
import com.example.ThangLongUniversityWeb.service.CourseOutcomeService;
import com.example.ThangLongUniversityWeb.service.ExamSessionService;
import com.example.ThangLongUniversityWeb.service.RegistrationRoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile({"dev", "local"})
@RequiredArgsConstructor
@Order(20)
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "password123";
    private static final int TARGET_TEACHERS = 10;
    private static final int TARGET_STUDENTS = 50;
    private static final int TARGET_SEMESTERS = 2;
    private static final int SESSIONS_PER_SECTION = 8;
    private static final Random RANDOM = new Random(20260617L);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final PeriodRepository periodRepository;
    private final SemesterRepository semesterRepository;
    private final TeacherRepository teacherRepository;
    private final HomeroomRepository homeroomRepository;
    private final StudentRepository studentRepository;
    private final ClassSectionRepository classSectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TuitionBillRepository tuitionBillRepository;
    private final ExamRegistrationRepository examRegistrationRepository;
    private final ExamSessionRepository examSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationRoundService registrationRoundService;
    private final CourseOutcomeService courseOutcomeService;
    private final ExamSessionService examSessionService;
    private final AcademicResultService academicResultService;

    @Override
    public void run(String... args) {
        if (isSeedComplete()) {
            System.out.println("DataSeeder: dữ liệu demo đã đủ, bỏ qua seed lớn.");
            return;
        }

        if (isPartialSeedNeedingRetest()) {
            System.out.println("DataSeeder: bổ sung điểm thi lại cho dữ liệu demo...");
            Semester hk2 = findLatestEndedSemester();
            if (hk2 != null) {
                seedRetestScores(hk2);
                recalculateAcademicResults(studentRepository.findAll(), findEndedSemesters());
            }
            System.out.println("DataSeeder: đã bổ sung điểm thi lại.");
            return;
        }

        System.out.println("DataSeeder: bắt đầu seed dữ liệu demo (" + TARGET_STUDENTS + " sinh viên, " + TARGET_TEACHERS + " giảng viên)...");

        List<Department> departments = ensureDepartments();
        List<Major> majors = ensureMajors(departments);
        List<Course> courses = ensureCourses(majors);
        List<Room> rooms = roomRepository.findAll();
        List<Period> periods = periodRepository.findAll();

        List<Semester> semesters = ensureCompletedSemesters();
        semesters.forEach(this::ensureRounds);

        List<Teacher> teachers = ensureTeachers(departments);
        List<Homeroom> homerooms = ensureHomerooms(majors, teachers);
        List<Student> students = ensureStudents(homerooms);

        seedAcademicData(semesters, courses, teachers, students, rooms, periods);

        Semester hk2 = semesters.get(1);
        seedExamSessionsForSemester(hk2, courses, rooms, teachers);
        seedRetestScores(hk2);

        recalculateAcademicResults(students, semesters);

        System.out.println("DataSeeder hoàn tất: "
                + studentRepository.count() + " sinh viên, "
                + teacherRepository.count() + " giảng viên, "
                + semesters.size() + " học kỳ đã kết thúc, "
                + examSessionRepository.count() + " ca thi.");
    }

    private boolean isSeedComplete() {
        long endedSemesters = semesterRepository.findAll().stream().filter(Semester::isEnded).count();
        return studentRepository.count() >= TARGET_STUDENTS
                && teacherRepository.count() >= TARGET_TEACHERS
                && roomRepository.count() >= 50
                && endedSemesters >= TARGET_SEMESTERS
                && examSessionRepository.count() > 0
                && gradeRepository.countByRetestScoreIsNotNull() > 0
                && attendanceSessionRepository.count() > 0;
    }

    private boolean isPartialSeedNeedingRetest() {
        return studentRepository.count() >= TARGET_STUDENTS
                && examSessionRepository.count() > 0
                && examRegistrationRepository.count() > 0
                && gradeRepository.countByRetestScoreIsNotNull() == 0;
    }

    private Semester findLatestEndedSemester() {
        return semesterRepository.findAll().stream()
                .filter(Semester::isEnded)
                .max((a, b) -> Long.compare(a.getId(), b.getId()))
                .orElse(null);
    }

    private List<Semester> findEndedSemesters() {
        return semesterRepository.findAll().stream()
                .filter(Semester::isEnded)
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
    }

    private List<Department> ensureDepartments() {
        List<DepartmentSeed> seeds = List.of(
                new DepartmentSeed("CNTT", "Khoa Công nghệ thông tin", "Đào tạo kỹ sư công nghệ và hệ thống số"),
                new DepartmentSeed("KT", "Khoa Kinh tế", "Đào tạo kinh tế ứng dụng và phân tích dữ liệu kinh tế"),
                new DepartmentSeed("QTKD", "Khoa Quản trị kinh doanh", "Đào tạo quản trị chiến lược và vận hành doanh nghiệp"),
                new DepartmentSeed("NN", "Khoa Ngoại ngữ", "Đào tạo biên phiên dịch và ngôn ngữ ứng dụng"),
                new DepartmentSeed("DL", "Khoa Du lịch - Khách sạn", "Đào tạo quản trị dịch vụ du lịch hiện đại"),
                new DepartmentSeed("LUAT", "Khoa Luật", "Đào tạo pháp luật kinh tế và thực hành pháp lý"),
                new DepartmentSeed("TKDH", "Khoa Thiết kế đồ họa", "Đào tạo thiết kế sáng tạo và truyền thông số"),
                new DepartmentSeed("XD", "Khoa Xây dựng", "Đào tạo kỹ thuật công trình và quản lý xây dựng")
        );

        List<Department> result = new ArrayList<>();
        for (DepartmentSeed seed : seeds) {
            result.add(upsertDepartment(seed.code(), seed.name(), seed.description()));
        }
        return result;
    }

    private List<Major> ensureMajors(List<Department> departments) {
        Map<String, Department> byCode = new HashMap<>();
        for (Department department : departments) {
            byCode.put(department.getDepartmentCode(), department);
        }

        List<MajorSeed> seeds = List.of(
                new MajorSeed("CNTT", "Công nghệ thông tin", "Đào tạo phát triển phần mềm và hệ thống thông tin", "CNTT"),
                new MajorSeed("KHMT", "Khoa học máy tính", "Đào tạo thuật toán, dữ liệu và trí tuệ nhân tạo", "CNTT"),
                new MajorSeed("ATTT", "An toàn thông tin", "Đào tạo bảo mật hạ tầng và an ninh mạng", "CNTT"),
                new MajorSeed("KTPM", "Kỹ thuật phần mềm", "Đào tạo kiểm thử và DevOps", "CNTT"),
                new MajorSeed("KT", "Kinh tế", "Đào tạo kinh tế ứng dụng", "KT"),
                new MajorSeed("TCNH", "Tài chính ngân hàng", "Đào tạo tài chính và ngân hàng số", "KT"),
                new MajorSeed("KTDN", "Kinh tế đối ngoại", "Đào tạo ngoại thương và logistics", "KT"),
                new MajorSeed("QTKD", "Quản trị kinh doanh", "Đào tạo quản trị doanh nghiệp", "QTKD"),
                new MajorSeed("MKT", "Marketing", "Đào tạo truyền thông và thương hiệu", "QTKD"),
                new MajorSeed("NNA", "Ngôn ngữ Anh", "Đào tạo tiếng Anh học thuật và thương mại", "NN"),
                new MajorSeed("NNT", "Ngôn ngữ Trung", "Đào tạo tiếng Trung ứng dụng", "NN"),
                new MajorSeed("NHKS", "Quản trị khách sạn", "Đào tạo nghiệp vụ khách sạn nhà hàng", "DL"),
                new MajorSeed("LH", "Quản trị lữ hành", "Đào tạo điều hành tour và dịch vụ lữ hành", "DL"),
                new MajorSeed("LKT", "Luật kinh tế", "Đào tạo pháp lý cho doanh nghiệp", "LUAT"),
                new MajorSeed("TKDH", "Thiết kế đồ họa", "Đào tạo đồ họa, UI/UX, truyền thông", "TKDH"),
                new MajorSeed("XD", "Kỹ thuật xây dựng", "Đào tạo kỹ thuật thi công và quản lý công trình", "XD")
        );

        List<Major> result = new ArrayList<>();
        for (MajorSeed seed : seeds) {
            result.add(upsertMajor(seed.code(), seed.name(), seed.description(), byCode.get(seed.departmentCode())));
        }
        return result;
    }

    private List<Course> ensureCourses(List<Major> majors) {
        Map<String, Major> byCode = new HashMap<>();
        for (Major major : majors) {
            byCode.put(major.getMajorCode(), major);
        }

        List<CourseSeed> seeds = List.of(
                new CourseSeed("MATH1101", "Giải tích 1", 3, "Nền tảng giải tích cho các ngành kỹ thuật", "CNTT", CourseType.REQUIRED),
                new CourseSeed("MATH1102", "Toán rời rạc", 3, "Logic và cấu trúc toán học rời rạc", "CNTT", CourseType.REQUIRED),
                new CourseSeed("INT2204", "Lập trình hướng đối tượng", 3, "Lập trình Java và thiết kế OOP", "CNTT", CourseType.REQUIRED),
                new CourseSeed("INT2207", "Cơ sở dữ liệu", 3, "Thiết kế cơ sở dữ liệu quan hệ", "CNTT", CourseType.REQUIRED),
                new CourseSeed("INT2208", "Lập trình Web", 3, "Phát triển ứng dụng web thực tế", "CNTT", CourseType.REQUIRED),
                new CourseSeed("INT2214", "Cấu trúc dữ liệu và giải thuật", 3, "Giải thuật nền tảng", "KHMT", CourseType.REQUIRED),
                new CourseSeed("INT2215", "Hệ điều hành", 3, "Quản lý tiến trình và bộ nhớ", "KHMT", CourseType.REQUIRED),
                new CourseSeed("INT3301", "Kiểm thử phần mềm", 3, "Kiểm thử tự động và đảm bảo chất lượng", "KTPM", CourseType.REQUIRED),
                new CourseSeed("INT3302", "Điện toán đám mây", 3, "Triển khai hạ tầng cloud", "KTPM", CourseType.REQUIRED),
                new CourseSeed("SEC2201", "Mật mã học ứng dụng", 3, "Ứng dụng mật mã trong hệ thống số", "ATTT", CourseType.REQUIRED),
                new CourseSeed("ECON1101", "Kinh tế vi mô", 3, "Hành vi thị trường và doanh nghiệp", "KT", CourseType.REQUIRED),
                new CourseSeed("ECON1201", "Kinh tế vĩ mô", 3, "Tổng quan nền kinh tế quốc gia", "KT", CourseType.REQUIRED),
                new CourseSeed("ACC1101", "Nguyên lý kế toán", 3, "Cơ sở kế toán tài chính", "TCNH", CourseType.REQUIRED),
                new CourseSeed("FIN2101", "Thị trường tài chính", 3, "Thị trường vốn và đầu tư", "TCNH", CourseType.REQUIRED),
                new CourseSeed("BUS1101", "Quản trị học", 3, "Lý thuyết và kỹ năng quản trị", "QTKD", CourseType.REQUIRED),
                new CourseSeed("MKT1101", "Marketing căn bản", 3, "Nghiên cứu thị trường và khách hàng", "MKT", CourseType.REQUIRED),
                new CourseSeed("MKT2202", "Truyền thông số", 3, "Quảng cáo và nội dung số", "MKT", CourseType.REQUIRED),
                new CourseSeed("ENG1101", "Tiếng Anh học thuật", 2, "Đọc viết học thuật tiếng Anh", "NNA", CourseType.REQUIRED),
                new CourseSeed("ENG2101", "Biên dịch Anh - Việt", 3, "Biên phiên dịch chuyên ngành", "NNA", CourseType.REQUIRED),
                new CourseSeed("CHI2101", "Biên dịch Trung - Việt", 3, "Biên phiên dịch tiếng Trung", "NNT", CourseType.REQUIRED),
                new CourseSeed("HOTEL2101", "Quản trị lễ tân", 3, "Nghiệp vụ lễ tân khách sạn", "NHKS", CourseType.REQUIRED),
                new CourseSeed("TOUR2101", "Thiết kế chương trình du lịch", 3, "Điều hành tour và sản phẩm du lịch", "LH", CourseType.REQUIRED),
                new CourseSeed("LAW1101", "Pháp luật đại cương", 2, "Nền tảng pháp luật", "LKT", CourseType.REQUIRED),
                new CourseSeed("LAW2201", "Luật thương mại", 3, "Pháp luật hợp đồng thương mại", "LKT", CourseType.REQUIRED),
                new CourseSeed("DES1101", "Nguyên lý thiết kế đồ họa", 3, "Bố cục và màu sắc cơ bản", "TKDH", CourseType.REQUIRED),
                new CourseSeed("DES2202", "Thiết kế giao diện UI/UX", 3, "Thiết kế trải nghiệm người dùng", "TKDH", CourseType.REQUIRED),
                new CourseSeed("CIV2101", "Kết cấu công trình", 3, "Nguyên lý thiết kế kết cấu", "XD", CourseType.REQUIRED),
                new CourseSeed("GEN1101", "Kỹ năng mềm", 2, "Giao tiếp và làm việc nhóm", null, CourseType.ELECTIVE),
                new CourseSeed("GEN1102", "Khởi nghiệp đổi mới sáng tạo", 2, "Tư duy sản phẩm và khởi nghiệp", null, CourseType.ELECTIVE),
                new CourseSeed("GEN1103", "Phương pháp nghiên cứu khoa học", 2, "Phương pháp nghiên cứu học thuật", null, CourseType.ELECTIVE),
                new CourseSeed("GEN1104", "Giáo dục thể chất", 2, "Rèn luyện thể lực", null, CourseType.ELECTIVE)
        );

        List<Course> result = new ArrayList<>();
        for (CourseSeed seed : seeds) {
            Major major = seed.majorCode() == null ? null : byCode.get(seed.majorCode());
            result.add(upsertCourse(seed.code(), seed.name(), seed.credits(), seed.description(), major, seed.type()));
        }
        return result;
    }

    private List<Semester> ensureCompletedSemesters() {
        return new ArrayList<>(List.of(
                upsertCompletedSemester("Học kỳ 1 năm học 2024-2025", LocalDate.of(2024, 9, 5), LocalDate.of(2025, 1, 20)),
                upsertCompletedSemester("Học kỳ 2 năm học 2024-2025", LocalDate.of(2025, 2, 10), LocalDate.of(2025, 6, 20))
        ));
    }

    private void ensureRounds(Semester semester) {
        registrationRoundService.ensureDefaultRound(semester.getId(), "COURSE");
        registrationRoundService.ensureDefaultRound(semester.getId(), "RETAKE");
    }

    private List<Teacher> ensureTeachers(List<Department> departments) {
        Map<String, Department> byCode = new HashMap<>();
        for (Department department : departments) {
            byCode.put(department.getDepartmentCode(), department);
        }
        String[] deptCodes = byCode.keySet().toArray(new String[0]);

        String[] ho = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Vũ", "Đỗ", "Bùi", "Đặng", "Phan"};
        String[] dem = {"Văn", "Hồng", "Minh", "Thị", "Thu", "Quang", "Thanh", "Bảo", "Khánh", "Gia"};
        String[] ten = {"An", "Anh", "Bình", "Châu", "Dũng", "Giang", "Hà", "Hải", "Hùng", "Khôi", "Linh", "Long", "Mai", "Nam", "Ngân", "Phong", "Quân", "Trang", "Tuấn", "Vy"};

        List<Teacher> teachers = new ArrayList<>();
        for (int i = 1; i <= TARGET_TEACHERS; i++) {
            String code = String.format("GV%03d", i);
            String username = String.format("gv%03d", i);
            String email = username + "@tlu.edu.vn";
            String fullName = ho[i % ho.length] + " " + dem[(i * 3) % dem.length] + " " + ten[(i * 7) % ten.length];
            String deptCode = deptCodes[i % deptCodes.length];
            teachers.add(upsertTeacher(username, email, code, fullName, byCode.get(deptCode), i));
        }
        return teachers;
    }

    private List<Homeroom> ensureHomerooms(List<Major> majors, List<Teacher> teachers) {
        Map<Long, Teacher> advisorByMajor = new HashMap<>();
        for (Major major : majors) {
            Teacher advisor = teachers.stream()
                    .filter(t -> t.getDepartment() != null && major.getDepartment() != null
                            && t.getDepartment().getId().equals(major.getDepartment().getId()))
                    .findFirst()
                    .orElse(teachers.get(0));
            advisorByMajor.put(major.getId(), advisor);
        }

        List<Homeroom> result = new ArrayList<>();
        for (Major major : majors) {
            int cohortYear = 2024;
            String cohort = "K24";
            String className = major.getMajorCode() + "-" + cohort + "A";
            result.add(upsertHomeroom(className, advisorByMajor.get(major.getId()), major, cohortYear, cohort));
        }
        return result;
    }

    private List<Student> ensureStudents(List<Homeroom> homerooms) {
        String[] ho = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Vũ", "Đỗ", "Bùi", "Đặng", "Phan", "Đoàn", "Lý"};
        String[] dem = {"Văn", "Thị", "Minh", "Gia", "Quốc", "Thanh", "Thu", "Bảo", "Hoàng", "Khánh"};
        String[] ten = {"An", "Anh", "Bình", "Châu", "Duy", "Giang", "Hà", "Hiếu", "Hùng", "Khôi", "Linh", "Long", "Mai", "Nam", "Ngọc", "Phúc", "Quân", "Trang", "Tuấn", "Vy"};
        String[] provinces = {"Hà Nội", "Nam Định", "Nghệ An", "Thanh Hóa", "Thái Bình", "Hải Dương", "Hải Phòng", "Bắc Ninh"};

        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= TARGET_STUDENTS; i++) {
            Homeroom homeroom = homerooms.get((i - 1) % homerooms.size());
            String username = String.format("sv%04d", i);
            String email = username + "@tlu.edu.vn";
            String code = String.format("SV%04d", i);
            String fullName = ho[i % ho.length] + " " + dem[(i * 2) % dem.length] + " " + ten[(i * 5) % ten.length];
            String province = provinces[i % provinces.length];
            students.add(upsertStudent(username, email, code, fullName, homeroom, province, i));
        }
        return students;
    }

    private void seedAcademicData(
            List<Semester> semesters,
            List<Course> courses,
            List<Teacher> teachers,
            List<Student> students,
            List<Room> rooms,
            List<Period> periods
    ) {
        int sectionCounter = 1;
        List<Period> sortedPeriods = periods.stream()
                .sorted((a, b) -> Integer.compare(a.getPeriodNumber(), b.getPeriodNumber()))
                .toList();

        List<List<ClassSection>> sectionsBySemester = new ArrayList<>();

        for (int semesterIndex = 0; semesterIndex < semesters.size(); semesterIndex++) {
            Semester semester = semesters.get(semesterIndex);
            List<ClassSection> semesterSections = new ArrayList<>();

            for (int i = 0; i < courses.size(); i++) {
                Course course = courses.get(i);
                for (int group = 1; group <= 2; group++) {
                    Teacher teacher = teachers.get((i + group + semesterIndex) % teachers.size());
                    Room room = rooms.get((i * 2 + group + semesterIndex) % rooms.size());
                    int dayOfWeek = 2 + ((i + group + semesterIndex) % 5);
                    Period start = sortedPeriods.get((i + group) % (sortedPeriods.size() - 2));
                    Period end = sortedPeriods.get(Math.min(start.getPeriodNumber() + 1, sortedPeriods.size()) - 1);
                    String classCode = course.getCode() + "-" + String.format("%03d", sectionCounter++);

                    ClassSection section = upsertClassSection(classCode, course, semester, teacher, room, dayOfWeek, start, end, 70);
                    semesterSections.add(section);
                }
            }

            for (int i = 0; i < students.size(); i++) {
                Student student = students.get(i);
                List<ClassSection> candidate = semesterSections.stream()
                        .filter(section -> section.getCourse().getMajor() == null
                                || (student.getMajor() != null && section.getCourse().getMajor().getId().equals(student.getMajor().getId())))
                        .toList();
                if (candidate.isEmpty()) {
                    continue;
                }

                int selectedStart = (i + semesterIndex * 7) % candidate.size();
                int toTake = 4 + (i % 2);
                int registered = 0;
                Set<Long> enrolledCourseIds = new HashSet<>();
                for (int c = 0; c < candidate.size() && registered < toTake; c++) {
                    ClassSection section = candidate.get((selectedStart + c) % candidate.size());
                    if (enrolledCourseIds.contains(section.getCourse().getId())) {
                        continue;
                    }
                    Enrollment enrollment = upsertEnrollment(student, section, EnrollmentStatus.REGISTERED);
                    if (enrollment != null) {
                        enrolledCourseIds.add(section.getCourse().getId());
                        registered++;
                    }
                }

                upsertTuition(student, semester, new BigDecimal("14500000"), new BigDecimal("14500000"), true);
            }

            sectionsBySemester.add(semesterSections);

            if (semesterIndex > 0) {
                seedRepeatCourseEnrollments(
                        sectionsBySemester.get(semesterIndex - 1),
                        semesterSections,
                        semesterIndex
                );
            }

            for (ClassSection section : semesterSections) {
                seedAttendanceAndGradesForSection(section, semesterIndex);
            }
        }
    }

    private void seedRepeatCourseEnrollments(List<ClassSection> currentSections, List<ClassSection> nextSections, int nextSemesterIndex) {
        for (ClassSection section : currentSections) {
            List<Enrollment> enrollments = enrollmentRepository.findByClassSectionId(section.getId()).stream()
                    .filter(e -> e.getStatus() == EnrollmentStatus.REGISTERED
                            && e.getCourseStatus() == CourseStudyStatus.REPEAT_COURSE)
                    .toList();
            if (enrollments.isEmpty()) {
                continue;
            }

            ClassSection repeatSection = nextSections.stream()
                    .filter(s -> s.getCourse().getId().equals(section.getCourse().getId()))
                    .findFirst()
                    .orElse(null);
            if (repeatSection == null) {
                continue;
            }

            for (Enrollment failed : enrollments) {
                Enrollment repeatEnrollment = upsertEnrollment(failed.getStudent(), repeatSection, EnrollmentStatus.REGISTERED);
                if (repeatEnrollment == null) {
                    continue;
                }
                Grade repeatGrade = upsertRepeatGrade(repeatEnrollment, nextSemesterIndex);
                courseOutcomeService.recalculate(repeatEnrollment);
                if (repeatGrade != null && repeatEnrollment.getCourseStatus() == CourseStudyStatus.RETAKE_EXAM) {
                    seedRetakeIfNeeded(repeatEnrollment, repeatGrade, nextSemesterIndex);
                }
            }
        }
    }

    private void seedAttendanceAndGradesForSection(ClassSection section, int semesterIndex) {
        List<Enrollment> enrollments = enrollmentRepository.findByClassSectionId(section.getId()).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.REGISTERED)
                .toList();
        if (enrollments.isEmpty()) {
            return;
        }

        for (int sessionNo = 1; sessionNo <= SESSIONS_PER_SECTION; sessionNo++) {
            AttendanceSession attendanceSession = upsertAttendanceSession(section, sessionNo, semesterIndex);
            for (Enrollment enrollment : enrollments) {
                AttendanceStatus status = attendanceStatusByPattern(
                        parseStudentNum(enrollment.getStudent().getStudentCode()), sessionNo, semesterIndex);
                upsertAttendanceRecord(attendanceSession, enrollment, status);
            }
        }

        for (Enrollment enrollment : enrollments) {
            Grade grade = upsertGrade(enrollment, semesterIndex);
            if (grade != null) {
                courseOutcomeService.recalculate(enrollment);
                seedRetakeIfNeeded(enrollment, grade, semesterIndex);
            }
        }
    }

    private void seedRetakeIfNeeded(Enrollment enrollment, Grade grade, int semesterIndex) {
        if (semesterIndex != 0 || grade.getTotalScore() == null) {
            return;
        }
        if (enrollment.getCourseStatus() == null) {
            return;
        }
        if (enrollment.getCourseStatus() == CourseStudyStatus.REPEAT_COURSE
                || enrollment.getCourseStatus() == CourseStudyStatus.BANNED_FROM_EXAM) {
            return;
        }
        if (grade.getTotalScore() >= 8.0f && enrollment.getCourseStatus() != CourseStudyStatus.RETAKE_EXAM) {
            return;
        }

        int studentNum = parseStudentNum(enrollment.getStudent().getStudentCode());
        if (studentNum % 5 == 0) {
            return;
        }

        List<Semester> orderedSemesters = semesterRepository.findAll().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .toList();
        if (semesterIndex + 1 >= orderedSemesters.size()) {
            return;
        }
        Semester nextSemester = orderedSemesters.get(semesterIndex + 1);
        boolean isRetake = enrollment.getCourseStatus() == CourseStudyStatus.RETAKE_EXAM || grade.getTotalScore() < 4.0f;
        upsertExamRegistration(enrollment.getStudent(), nextSemester, enrollment.getClassSection().getCourse(), grade, isRetake);
    }

    private void seedExamSessionsForSemester(Semester semester, List<Course> courses, List<Room> rooms, List<Teacher> teachers) {
        Set<Long> coursesWithEnrollments = enrollmentRepository
                .findByClassSectionSemesterIdAndStatus(semester.getId(), EnrollmentStatus.REGISTERED)
                .stream()
                .map(e -> e.getClassSection().getCourse().getId())
                .collect(Collectors.toSet());

        Set<Long> coursesWithRetakeRegs = examRegistrationRepository.findBySemesterId(semester.getId())
                .stream()
                .filter(r -> r.getStatus() == EnrollmentStatus.REGISTERED)
                .map(r -> r.getCourse().getId())
                .collect(Collectors.toSet());

        List<Room> examRooms = rooms.stream()
                .filter(r -> r.getCapacity() != null && r.getCapacity() >= 30)
                .toList();
        if (examRooms.isEmpty()) {
            examRooms = rooms;
        }

        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            if (coursesWithEnrollments.contains(course.getId())) {
                createExamSession(semester, course, i, examRooms, teachers,
                        "NORMAL_ONLY", semester.getEndDate().minusDays(7), 8);
            }
            if (coursesWithRetakeRegs.contains(course.getId())) {
                createExamSession(semester, course, i, examRooms, teachers,
                        "RETAKE_ONLY", semester.getEndDate().minusDays(3), 14);
            }
        }
    }

    private void createExamSession(
            Semester semester,
            Course course,
            int courseIndex,
            List<Room> rooms,
            List<Teacher> teachers,
            String candidateSelection,
            LocalDate examDate,
            int baseHour
    ) {
        Room room = rooms.get(courseIndex % rooms.size());
        Teacher proctor = teachers.get(courseIndex % teachers.size());
        int minuteOffset = (courseIndex % 4) * 15;
        LocalDateTime examAt = LocalDateTime.of(examDate, LocalTime.of(baseHour, minuteOffset));

        ExamSessionRequest request = new ExamSessionRequest();
        request.setCourseId(course.getId());
        request.setExamAt(examAt);
        request.setRoomIds(List.of(room.getId()));
        request.setProctorIds(List.of(proctor.getId()));
        request.setAllocationMethod("SEQUENTIAL");
        request.setCandidateSelection(candidateSelection);

        try {
            examSessionService.saveSession(semester.getId(), request);
        } catch (RuntimeException ex) {
            System.out.println("DataSeeder: bỏ qua ca thi " + course.getCode()
                    + " (" + candidateSelection + "): " + ex.getMessage());
        }
    }

    private void seedRetestScores(Semester semester) {
        List<ExamRegistration> registrations = examRegistrationRepository.findBySemesterId(semester.getId())
                .stream()
                .filter(r -> r.getStatus() == EnrollmentStatus.REGISTERED)
                .toList();

        for (int i = 0; i < registrations.size(); i++) {
            ExamRegistration registration = registrations.get(i);
            if (registration.getOriginalGrade() == null) {
                continue;
            }

            Grade originalGrade = gradeRepository.findById(registration.getOriginalGrade().getId()).orElse(null);
            if (originalGrade == null || originalGrade.getEnrollment() == null) {
                continue;
            }

            if (i % 5 == 4) {
                continue;
            }

            float retestScore;
            if (registration.getRegistrationType() == EnrollmentType.RETAKE) {
                retestScore = 5.5f + (RANDOM.nextFloat() * 1.5f);
                originalGrade.prepareGradeCalculation(EnrollmentType.RETAKE);
            } else {
                retestScore = 7.0f + (RANDOM.nextFloat() * 1.5f);
                originalGrade.prepareGradeCalculation(EnrollmentType.IMPROVE);
            }
            originalGrade.setRetestScore(retestScore);
            gradeRepository.save(originalGrade);

            Enrollment originalEnrollment = enrollmentRepository.findById(originalGrade.getEnrollment().getId())
                    .orElse(null);
            if (originalEnrollment != null) {
                courseOutcomeService.recalculate(originalEnrollment);
            }
        }
    }

    private int parseStudentNum(String studentCode) {
        if (studentCode == null || studentCode.length() < 3) {
            return 0;
        }
        try {
            return Integer.parseInt(studentCode.replaceAll("\\D", ""));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void recalculateAcademicResults(List<Student> students, List<Semester> semesters) {
        for (Student student : students) {
            for (Semester semester : semesters) {
                try {
                    academicResultService.calculateSemesterGPA(student.getId(), semester.getId());
                } catch (Exception ignored) {
                }
            }
            try {
                academicResultService.calculateCumulativeGPA(student.getId());
            } catch (Exception ignored) {
            }
        }
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

    private Semester upsertCompletedSemester(String name, LocalDate startDate, LocalDate endDate) {
        return semesterRepository.findByName(name).map(existing -> {
            existing.setStartDate(startDate);
            existing.setEndDate(endDate);
            existing.setRegistrationOpen(false);
            existing.setLocked(true);
            existing.setEnded(true);
            existing.setExamPublished(true);
            existing.setRetakeOpen(false);
            existing.setRetakeLocked(true);
            existing.setMaxCreditsPerSemester(24);
            return semesterRepository.save(existing);
        }).orElseGet(() -> {
            Semester semester = new Semester();
            semester.setName(name);
            semester.setStartDate(startDate);
            semester.setEndDate(endDate);
            semester.setRegistrationOpen(false);
            semester.setLocked(true);
            semester.setEnded(true);
            semester.setExamPublished(true);
            semester.setRetakeOpen(false);
            semester.setRetakeLocked(true);
            semester.setMaxCreditsPerSemester(24);
            return semesterRepository.save(semester);
        });
    }

    private Teacher upsertTeacher(String username, String email, String code, String fullName, Department department, int index) {
        return teacherRepository.findByTeacherCode(code).map(existing -> {
            applyTeacher(existing, fullName, department, index);
            return teacherRepository.save(existing);
        }).orElseGet(() -> {
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> saveUser(username, email, Role.TEACHER));
            Teacher teacher = new Teacher();
            teacher.setUser(user);
            teacher.setTeacherCode(code);
            applyTeacher(teacher, fullName, department, index);
            return teacherRepository.save(teacher);
        });
    }

    private void applyTeacher(Teacher teacher, String fullName, Department department, int index) {
        teacher.setFullName(fullName);
        teacher.setDepartment(department);
        teacher.setDob(LocalDate.of(1980 + (index % 12), 1 + (index % 12), 1 + (index % 20)));
        teacher.setGender(index % 2 == 0 ? "Nam" : "Nữ");
        teacher.setPhone(String.format("09%08d", 10000000 + index));
        teacher.setNationalId(String.format("0%011d", 10000000000L + index));
        teacher.setPlaceOfBirth("Hà Nội");
        teacher.setHometown("Hà Nội");
        teacher.setPermanentAddress("Số " + (index + 10) + " đường Nguyễn Trãi, Hà Nội");
        teacher.setCurrentAddress("Số " + (index + 10) + " đường Nguyễn Trãi, Hà Nội");
        teacher.setEmergencyContact("Người thân - 09" + String.format("%08d", 20000000 + index));
        teacher.setAddress(teacher.getCurrentAddress());
        teacher.setDegree(index % 5 == 0 ? "Tiến sĩ" : "Thạc sĩ");
        teacher.setStatus(TeacherStatus.DANG_GIANG_DAY);
    }

    private Homeroom upsertHomeroom(String className, Teacher advisor, Major major, int academicYear, String cohort) {
        return homeroomRepository.findByClassName(className).map(existing -> {
            existing.setAdvisor(advisor);
            existing.setMajor(major);
            existing.setAcademicYear(academicYear);
            existing.setCohort(cohort);
            existing.setIsActive(true);
            return homeroomRepository.save(existing);
        }).orElseGet(() -> {
            Homeroom homeroom = new Homeroom();
            homeroom.setClassName(className);
            homeroom.setAdvisor(advisor);
            homeroom.setMajor(major);
            homeroom.setAcademicYear(academicYear);
            homeroom.setCohort(cohort);
            homeroom.setIsActive(true);
            return homeroomRepository.save(homeroom);
        });
    }

    private Student upsertStudent(String username, String email, String code, String fullName, Homeroom homeroom, String province, int index) {
        return studentRepository.findByStudentCode(code).map(existing -> {
            applyStudent(existing, fullName, homeroom, province, index);
            return studentRepository.save(existing);
        }).orElseGet(() -> {
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> saveUser(username, email, Role.STUDENT));
            Student student = new Student();
            student.setUser(user);
            student.setStudentCode(code);
            applyStudent(student, fullName, homeroom, province, index);
            return studentRepository.save(student);
        });
    }

    private void applyStudent(Student student, String fullName, Homeroom homeroom, String province, int index) {
        student.setFullName(fullName);
        student.setDob(LocalDate.of(2002 + (index % 5), 1 + (index % 12), 1 + (index % 26)));
        student.setGender(index % 2 == 0 ? "Nam" : "Nữ");
        student.setPhone(String.format("03%08d", 10000000 + index));
        student.setNationalId(String.format("0%011d", 20000000000L + index));
        student.setPlaceOfBirth(province);
        student.setHometown(province);
        student.setPermanentAddress("Thôn " + ((index % 12) + 1) + ", " + province);
        student.setCurrentAddress("Ký túc xá Thăng Long, phòng " + ((index % 500) + 1));
        student.setEmergencyContact("Phụ huynh - 09" + String.format("%08d", 30000000 + index));
        student.setAddress(student.getCurrentAddress());
        student.setHomeroom(homeroom);
        student.setMajor(homeroom.getMajor());
        student.setAcademicYear(homeroom.getAcademicYear());
        student.setCohort(homeroom.getCohort());
        student.setStatus("Đang học");
        student.setTrainingType("Đại học chính quy");
    }

    private ClassSection upsertClassSection(
            String code,
            Course course,
            Semester semester,
            Teacher teacher,
            Room room,
            int dayOfWeek,
            Period startPeriod,
            Period endPeriod,
            int maxSlots
    ) {
        return classSectionRepository.findBySemesterIdAndClassCode(semester.getId(), code).map(existing -> {
            existing.setCourse(course);
            existing.setSemester(semester);
            existing.setTeacher(teacher);
            existing.setRoom(room);
            existing.setDayOfWeek(dayOfWeek);
            existing.setStartPeriod(startPeriod);
            existing.setEndPeriod(endPeriod);
            existing.setMaxSlots(maxSlots);
            if (existing.getCurrentSlots() == null) {
                existing.setCurrentSlots(0);
            }
            existing.setStatus(ClassSectionStatus.CLOSED);
            existing.setGradeLocked(true);
            existing.setExamRoom(room.getName());
            existing.setExamAt(LocalDateTime.of(semester.getEndDate().minusDays(7), LocalTime.of(8, 0)));
            if (existing.getSchedules().isEmpty()) {
                ClassSectionSchedule schedule = new ClassSectionSchedule();
                schedule.setClassSection(existing);
                schedule.setDayOfWeek(dayOfWeek);
                schedule.setStartPeriod(startPeriod);
                schedule.setEndPeriod(endPeriod);
                schedule.setRoom(room);
                existing.getSchedules().add(schedule);
            }
            return classSectionRepository.save(existing);
        }).orElseGet(() -> {
            ClassSection section = new ClassSection();
            section.setClassCode(code);
            section.setCourse(course);
            section.setSemester(semester);
            section.setTeacher(teacher);
            section.setRoom(room);
            section.setDayOfWeek(dayOfWeek);
            section.setStartPeriod(startPeriod);
            section.setEndPeriod(endPeriod);
            section.setMaxSlots(maxSlots);
            section.setCurrentSlots(0);
            section.setStatus(ClassSectionStatus.CLOSED);
            section.setGradeLocked(true);
            section.setExamRoom(room.getName());
            section.setExamAt(LocalDateTime.of(semester.getEndDate().minusDays(7), LocalTime.of(8, 0)));

            ClassSectionSchedule schedule = new ClassSectionSchedule();
            schedule.setClassSection(section);
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setStartPeriod(startPeriod);
            schedule.setEndPeriod(endPeriod);
            schedule.setRoom(room);
            section.getSchedules().add(schedule);
            return classSectionRepository.save(section);
        });
    }

    private Enrollment upsertEnrollment(Student student, ClassSection section, EnrollmentStatus status) {
        if (section.getCurrentSlots() != null && section.getMaxSlots() != null && section.getCurrentSlots() >= section.getMaxSlots()) {
            return null;
        }
        return enrollmentRepository.findByStudentIdAndClassSectionId(student.getId(), section.getId()).map(existing -> {
            existing.setStatus(status);
            return enrollmentRepository.save(existing);
        }).orElseGet(() -> {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setClassSection(section);
            enrollment.setStatus(status);
            enrollment.setCourseStatus(CourseStudyStatus.IN_PROGRESS);
            section.setCurrentSlots((section.getCurrentSlots() == null ? 0 : section.getCurrentSlots()) + 1);
            classSectionRepository.save(section);
            return enrollmentRepository.save(enrollment);
        });
    }

    private AttendanceSession upsertAttendanceSession(ClassSection section, int sessionNo, int semesterIndex) {
        return attendanceSessionRepository.findByClassSectionIdAndSessionNumber(section.getId(), sessionNo).orElseGet(() -> {
            AttendanceSession session = new AttendanceSession();
            session.setClassSection(section);
            session.setSessionNumber(sessionNo);
            session.setWeekNumber((sessionNo + 1) / 2);
            session.setMeetingIndex(sessionNo % 2 == 0 ? 2 : 1);
            session.setSessionDate(section.getSemester().getStartDate().plusDays(sessionNo + semesterIndex * 2L));
            session.setLocked(true);
            return attendanceSessionRepository.save(session);
        });
    }

    private AttendanceRecord upsertAttendanceRecord(AttendanceSession session, Enrollment enrollment, AttendanceStatus status) {
        return attendanceRecordRepository.findByAttendanceSessionId(session.getId()).stream()
                .filter(record -> record.getEnrollment().getId().equals(enrollment.getId()))
                .findFirst()
                .map(existing -> {
                    existing.setStatus(status);
                    existing.setNote(status == AttendanceStatus.ABSENT ? "Vắng có lý do" : null);
                    return attendanceRecordRepository.save(existing);
                }).orElseGet(() -> {
                    AttendanceRecord record = new AttendanceRecord();
                    record.setAttendanceSession(session);
                    record.setEnrollment(enrollment);
                    record.setStatus(status);
                    record.setNote(status == AttendanceStatus.ABSENT ? "Vắng có lý do" : null);
                    return attendanceRecordRepository.save(record);
                });
    }

    private Grade upsertRepeatGrade(Enrollment enrollment, int semesterIndex) {
        return gradeRepository.findByEnrollmentId(enrollment.getId()).map(existing -> {
            existing.setParticipationScore(7.5f);
            existing.setMidtermScore(7.0f);
            existing.setFinalScore(6.8f);
            existing.setEnrollmentType(EnrollmentType.ORDINARY);
            existing.setAttemptNumber(2);
            return gradeRepository.save(existing);
        }).orElseGet(() -> {
            Grade grade = new Grade();
            grade.setEnrollment(enrollment);
            grade.setParticipationScore(7.5f);
            grade.setMidtermScore(7.0f);
            grade.setFinalScore(6.8f);
            grade.setAttemptNumber(2);
            grade.setEnrollmentType(EnrollmentType.ORDINARY);
            return gradeRepository.save(grade);
        });
    }

    private Grade upsertGrade(Enrollment enrollment, int semesterIndex) {
        float participation = 6.5f + (RANDOM.nextFloat() * 3.0f);
        float midterm = 5.5f + (RANDOM.nextFloat() * 4.0f);
        float finalScore = 5.0f + (RANDOM.nextFloat() * 4.5f);

        int studentNum = parseStudentNum(enrollment.getStudent().getStudentCode());
        long marker = studentNum + semesterIndex;
        if (marker % 19 == 0) {
            participation = 2.5f;
            midterm = 2.8f;
            finalScore = 3.2f;
        } else if (marker % 17 == 0) {
            participation = 3.5f;
            midterm = 3.6f;
            finalScore = 6.2f;
        } else if (marker % 13 == 0) {
            participation = 8.2f;
            midterm = 7.8f;
            finalScore = 6.1f;
        }

        final float p = participation;
        final float m = midterm;
        final float f = finalScore;
        Grade grade = gradeRepository.findByEnrollmentId(enrollment.getId()).map(existing -> {
            existing.setParticipationScore(p);
            existing.setMidtermScore(m);
            existing.setFinalScore(f);
            existing.setEnrollmentType(EnrollmentType.ORDINARY);
            if (existing.getAttemptNumber() == null) {
                existing.setAttemptNumber(1);
            }
            return gradeRepository.save(existing);
        }).orElseGet(() -> {
            Grade g = new Grade();
            g.setEnrollment(enrollment);
            g.setParticipationScore(p);
            g.setMidtermScore(m);
            g.setFinalScore(f);
            g.setAttemptNumber(1);
            g.setEnrollmentType(EnrollmentType.ORDINARY);
            return gradeRepository.save(g);
        });
        enrollment.setGrade(grade);
        return grade;
    }

    private AttendanceStatus attendanceStatusByPattern(int studentNum, int sessionNo, int semesterIndex) {
        long marker = studentNum + sessionNo + semesterIndex * 3L;
        if (studentNum % 47 == 0 && sessionNo <= 5) {
            return AttendanceStatus.ABSENT;
        }
        if (marker % 11 == 0) return AttendanceStatus.ABSENT;
        if (marker % 7 == 0) return AttendanceStatus.LATE;
        return AttendanceStatus.PRESENT;
    }

    private TuitionBill upsertTuition(Student student, Semester semester, BigDecimal total, BigDecimal paid, boolean completed) {
        return tuitionBillRepository.findByStudentIdAndSemesterId(student.getId(), semester.getId()).map(existing -> {
            existing.setTotalAmount(total);
            existing.setPaidAmount(paid);
            existing.setCompleted(completed);
            return tuitionBillRepository.save(existing);
        }).orElseGet(() -> {
            TuitionBill bill = new TuitionBill();
            bill.setStudent(student);
            bill.setSemester(semester);
            bill.setTotalAmount(total);
            bill.setPaidAmount(paid);
            bill.setCompleted(completed);
            return tuitionBillRepository.save(bill);
        });
    }

    private ExamRegistration upsertExamRegistration(Student student, Semester semester, Course course, Grade originalGrade, boolean isRetake) {
        return examRegistrationRepository.findByStudentIdAndCourseIdAndSemesterId(student.getId(), course.getId(), semester.getId())
                .map(existing -> {
                    existing.setOriginalGrade(originalGrade);
                    existing.setStatus(EnrollmentStatus.REGISTERED);
                    existing.setRegistrationType(isRetake ? EnrollmentType.RETAKE : EnrollmentType.IMPROVE);
                    existing.setFeeCharged(200_000L);
                    existing.setAttemptNumber((originalGrade.getAttemptNumber() == null ? 1 : originalGrade.getAttemptNumber()) + 1);
                    return examRegistrationRepository.save(existing);
                }).orElseGet(() -> {
                    ExamRegistration registration = new ExamRegistration();
                    registration.setStudent(student);
                    registration.setSemester(semester);
                    registration.setCourse(course);
                    registration.setOriginalGrade(originalGrade);
                    registration.setStatus(EnrollmentStatus.REGISTERED);
                    registration.setRegistrationType(isRetake ? EnrollmentType.RETAKE : EnrollmentType.IMPROVE);
                    registration.setFeeCharged(200_000L);
                    registration.setAttemptNumber((originalGrade.getAttemptNumber() == null ? 1 : originalGrade.getAttemptNumber()) + 1);
                    return examRegistrationRepository.save(registration);
                });
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

    private record DepartmentSeed(String code, String name, String description) {}
    private record MajorSeed(String code, String name, String description, String departmentCode) {}
    private record CourseSeed(String code, String name, int credits, String description, String majorCode, CourseType type) {}
}
