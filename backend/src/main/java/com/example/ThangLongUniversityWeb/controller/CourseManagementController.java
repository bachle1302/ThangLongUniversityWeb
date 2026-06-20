package com.example.ThangLongUniversityWeb.controller;

import com.example.ThangLongUniversityWeb.dto.request.CourseRequest;
import com.example.ThangLongUniversityWeb.dto.response.CourseResponse;
import com.example.ThangLongUniversityWeb.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@Tag(name = "Admin - Quản lý Danh mục Môn học")
@SecurityRequirement(name = "bearerAuth")
public class CourseManagementController {

    private final CourseService courseService;

    @Operation(
            summary = "Lấy danh sách tất cả môn học",
            description = "Trả về danh sách tất cả môn học trong hệ thống"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Không có quyền truy cập",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @Operation(
            summary = "Thêm mới một môn học",
            description = "Tạo môn học mới với thông tin chi tiết"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tạo môn học thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseResponse.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "id": 1,
                                              "code": "CS101",
                                              "name": "Java Core Programming",
                                              "credits": 3,
                                              "description": "Khóa học lập trình Java cơ bản",
                                              "majorName": "Công nghệ thông tin",
                                              "prerequisiteNames": ["CS100"]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Không có quyền truy cập",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Mã môn học đã tồn tại",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<?> createCourse(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin môn học cần tạo",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CourseRequest.class),
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = """
                                            {
                                              "code": "CS101",
                                              "name": "Java Core Programming",
                                              "credits": 3,
                                              "description": "Khóa học lập trình Java cơ bản cho sinh viên năm 1",
                                              "majorId": 1,
                                              "prerequisiteCourseIds": [1, 2]
                                            }
                                            """
                            )
                    )
            )
            @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.createCourse(request));
    }

    @Operation(
            summary = "Cập nhật thông tin môn học",
            description = "Cập nhật thông tin của môn học theo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = CourseResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu đầu vào không hợp lệ",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Không có quyền truy cập",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy môn học",
                    content = @Content
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(
            @Parameter(description = "ID của môn học cần cập nhật", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin cập nhật môn học",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CourseRequest.class))
            )
            @RequestBody CourseRequest request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @Operation(
            summary = "Xóa môn học",
            description = "Xóa môn học theo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Xóa thành công",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    value = "\"Xóa môn học thành công!\""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Không có quyền truy cập",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy môn học",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Không thể xóa môn học đang được sử dụng",
                    content = @Content
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(
            @Parameter(description = "ID của môn học cần xóa", required = true, example = "1")
            @PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok("Xóa môn học thành công!");
    }
}