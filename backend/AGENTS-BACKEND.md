# AGENTS-BACKEND.md

**Project:** ThangLongUniversityWeb  
**Framework:** Spring Boot + Java + PostgreSQL + JPA/Hibernate  
**Generated:** May 19, 2026

This document guides AI agents (Cursor, Windsurf, GitHub Copilot, Claude Code) on backend architecture, conventions, and practices for production-grade development.

---

## Table of Contents

1. [Stack Overview](#stack-overview)
2. [Project Structure](#project-structure)
3. [Layered Architecture](#layered-architecture)
4. [REST API Conventions](#rest-api-conventions)
5. [Controller Layer](#controller-layer)
6. [Service Layer](#service-layer)
7. [Repository Layer](#repository-layer)
8. [Entity & JPA Patterns](#entity--jpa-patterns)
9. [DTO Patterns & Mapping](#dto-patterns--mapping)
10. [Authentication & Authorization](#authentication--authorization)
11. [Validation Rules](#validation-rules)
12. [Error Handling](#error-handling)
13. [Database Optimization](#database-optimization)
14. [Swagger/OpenAPI Documentation](#swaggeropenapi-documentation)
15. [Transaction Management](#transaction-management)
16. [Kafka Event Patterns](#kafka-event-patterns)
17. [Redis Caching](#redis-caching)
18. [Query Patterns](#query-patterns)
19. [Feature Creation Workflow](#feature-creation-workflow)
20. [Code Generation Rules](#code-generation-rules)
21. [Common Patterns & Anti-Patterns](#common-patterns--anti-patterns)

---

## Stack Overview

### Core Dependencies
- **Java:** 17+ (LTS)
- **Spring Boot:** 3.x (latest)
- **Spring Data JPA:** 3.x
- **Spring Security:** 6.x (JWT-based)
- **PostgreSQL:** 14+ (primary database)
- **Redis:** 7+ (caching, sessions)
- **Apache Kafka:** 3.x (event streaming)
- **Hibernate:** 6.x (ORM)
- **Jackson:** 2.x (JSON serialization)
- **Jakarta Validation:** 3.x (bean validation)
- **Springdoc OpenAPI:** 2.x (Swagger)
- **Gradle or Maven:** (build tools)
- **Lombok:** (optional, for boilerplate reduction)

### No External Alternative Libraries
⛔ **NEVER use:**
- MyBatis (use Spring Data JPA)
- Custom JWT implementations (use Spring Security + jjwt)
- HttpClient (use RestTemplate or WebClient)
- Manual transaction management (use @Transactional)

---

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tlu/
│   │   │       ├── controller/
│   │   │       │   ├── AdminController.java
│   │   │       │   ├── StudentController.java
│   │   │       │   ├── TeacherController.java
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── ChatController.java
│   │   │       │   └── ... (other controllers)
│   │   │       ├── service/
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── UserService.java
│   │   │       │   ├── StudentService.java
│   │   │       │   ├── TeacherService.java
│   │   │       │   ├── CourseService.java
│   │   │       │   ├── EnrollmentService.java
│   │   │       │   ├── ChatService.java
│   │   │       │   ├── GradeService.java
│   │   │       │   └── ... (other services)
│   │   │       ├── repository/
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── StudentRepository.java
│   │   │       │   ├── CourseRepository.java
│   │   │       │   ├── EnrollmentRepository.java
│   │   │       │   ├── ChatRoomRepository.java
│   │   │       │   ├── ChatMessageRepository.java
│   │   │       │   └── ... (other repositories)
│   │   │       ├── entity/
│   │   │       │   ├── User.java
│   │   │       │   ├── Student.java
│   │   │       │   ├── Teacher.java
│   │   │       │   ├── Course.java
│   │   │       │   ├── Enrollment.java
│   │   │       │   ├── Grade.java
│   │   │       │   ├── ChatRoom.java
│   │   │       │   ├── ChatMessage.java
│   │   │       │   └── ... (other entities)
│   │   │       ├── dto/
│   │   │       │   ├── request/
│   │   │       │   │   ├── LoginRequestDTO.java
│   │   │       │   │   ├── CreateUserRequestDTO.java
│   │   │       │   │   ├── CreateCourseRequestDTO.java
│   │   │       │   │   └── ... (other request DTOs)
│   │   │       │   ├── response/
│   │   │       │   │   ├── AuthResponseDTO.java
│   │   │       │   │   ├── UserResponseDTO.java
│   │   │       │   │   ├── CourseResponseDTO.java
│   │   │       │   │   └── ... (other response DTOs)
│   │   │       │   └── PageDTO.java (pagination wrapper)
│   │   │       ├── mapper/
│   │   │       │   ├── UserMapper.java
│   │   │       │   ├── CourseMapper.java
│   │   │       │   └── ... (other mappers)
│   │   │       ├── security/
│   │   │       │   ├── JwtAuthenticationFilter.java
│   │   │       │   ├── JwtTokenProvider.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── CustomUserDetails.java
│   │   │       ├── config/
│   │   │       │   ├── OpenApiConfig.java
│   │   │       │   ├── WebConfig.java
│   │   │       │   ├── CacheConfig.java
│   │   │       │   ├── DataSourceConfig.java
│   │   │       │   └── KafkaConfig.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   ├── ValidationException.java
│   │   │       │   ├── UnauthorizedException.java
│   │   │       │   └── BusinessException.java
│   │   │       ├── kafka/
│   │   │       │   ├── producer/
│   │   │       │   │   ├── ChatKafkaProducer.java
│   │   │       │   │   └── EnrollmentEventProducer.java
│   │   │       │   └── consumer/
│   │   │       │       ├── ChatKafkaConsumer.java
│   │   │       │       └── EnrollmentEventConsumer.java
│   │   │       └── Application.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── db/
│   │           └── migration/ (Flyway or Liquibase)
│   └── test/
│       └── java/
│           └── com/tlu/
│               ├── controller/
│               ├── service/
│               └── repository/
├── build.gradle.kts (or pom.xml)
├── settings.gradle.kts
└── sql/
    └── schema.sql
```

---

## Layered Architecture

### Architecture Overview

```
Request/Response
        ↓
┌───────────────────────┐
│  Controller Layer     │ (Handles HTTP requests/responses)
└───────────┬───────────┘
            ↓
┌───────────────────────┐
│  Service Layer        │ (Business logic, transactions)
└───────────┬───────────┘
            ↓
┌───────────────────────┐
│  Repository Layer     │ (Database access)
└───────────┬───────────┘
            ↓
┌───────────────────────┐
│  Entity Layer         │ (Database models)
└───────────────────────┘
            ↓
        Database
```

### Layer Responsibilities

**Controller Layer:**
- Receive HTTP requests
- Validate request DTOs
- Delegate to services
- Return responses
- Handle authentication/authorization checks

**Service Layer:**
- Business logic implementation
- Transaction management
- Orchestration between repositories
- Validation of business rules
- Exception handling

**Repository Layer:**
- Database access only
- Query execution
- Transaction support via Spring
- Performance optimization

**Entity Layer:**
- ORM models
- Database schema mapping
- Relationships definition
- No business logic

**DTO Layer:**
- Request/response contracts
- Decoupling from entities
- API versioning support
- Validation rules

### Data Flow Example

```
Frontend Request
    ↓
POST /api/admin/users
    ↓
UserController.createUser(CreateUserRequestDTO)
    ↓
Validates DTO (Jakarta Validation)
    ↓
UserService.createUser(CreateUserRequestDTO)
    ↓
Business logic validation
    ↓
UserRepository.save(User entity)
    ↓
Database INSERT
    ↓
Mapper: Entity → UserResponseDTO
    ↓
ResponseEntity<UserResponseDTO>
    ↓
Frontend Response JSON
```

---

## REST API Conventions

### API Base Path
```
/api/v1/resource
```

### Endpoint Naming Patterns

**Public Endpoints:**
```
POST   /api/auth/login
POST   /api/auth/logout
POST   /api/auth/refresh
GET    /api/users/me
```

**Admin Endpoints:**
```
GET    /api/admin/users
POST   /api/admin/users
GET    /api/admin/users/{id}
PUT    /api/admin/users/{id}
DELETE /api/admin/users/{id}

GET    /api/admin/courses
POST   /api/admin/courses
GET    /api/admin/courses/{id}
PUT    /api/admin/courses/{id}
DELETE /api/admin/courses/{id}
```

**Student Endpoints:**
```
GET    /api/student/semesters
GET    /api/student/classes/semester/{semesterId}
POST   /api/student/enroll/{classSectionId}
DELETE /api/student/enroll/{classSectionId}
GET    /api/student/grades
GET    /api/student/schedule/{semesterId}
GET    /api/student/exams
GET    /api/student/tuition/{semesterId}
POST   /api/student/tuition/{semesterId}/vnpay-url
```

**Teacher Endpoints:**
```
GET    /api/teacher/classes
GET    /api/teacher/classes/{classSectionId}/students
POST   /api/teacher/classes/{classSectionId}/grades
GET    /api/teacher/grades/{gradeId}
PUT    /api/teacher/grades/{gradeId}
```

### HTTP Methods & Status Codes

**GET - Retrieve Resource**
```
200 OK - Success
404 Not Found - Resource doesn't exist
401 Unauthorized - Missing authentication
403 Forbidden - Insufficient permissions
```

**POST - Create Resource**
```
201 Created - Resource created successfully
400 Bad Request - Invalid data
409 Conflict - Resource already exists
401 Unauthorized - Missing authentication
403 Forbidden - Insufficient permissions
```

**PUT - Update Resource**
```
200 OK - Update successful
400 Bad Request - Invalid data
404 Not Found - Resource doesn't exist
401 Unauthorized - Missing authentication
403 Forbidden - Insufficient permissions
```

**DELETE - Delete Resource**
```
204 No Content - Deletion successful
404 Not Found - Resource doesn't exist
401 Unauthorized - Missing authentication
403 Forbidden - Insufficient permissions
```

### Query Parameters

**Pagination:**
```
GET /api/users?page=0&size=20
GET /api/courses?pageNumber=1&pageSize=50
```

**Filtering:**
```
GET /api/users?role=STUDENT&status=ACTIVE
GET /api/courses?major=CS&semester=1
```

**Sorting:**
```
GET /api/users?sort=name,asc
GET /api/courses?sort=credits,desc
```

### Response Structure

**Success Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

**Paginated Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      { "id": 1, "name": "John" },
      { "id": 2, "name": "Jane" }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User with ID 123 not found",
    "timestamp": "2026-05-19T10:30:00Z"
  }
}
```

---

## Controller Layer

### Controller Responsibilities

**DO:**
- Accept HTTP requests
- Validate request DTOs
- Check authentication/authorization
- Delegate to services
- Return proper ResponseEntity
- Map entities to DTOs

**DON'T:**
- Place business logic in controllers
- Access database directly
- Bypass service layer
- Expose entities directly
- Handle transactions explicitly

### Controller Example

```java
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "User management endpoints")
public class UserController {
  
  private final UserService userService;
  private final UserMapper userMapper;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "List all users")
  @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
  public ResponseEntity<Page<UserResponseDTO>> listUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String role
  ) {
    Page<User> users = userService.listUsers(page, size, role);
    Page<UserResponseDTO> dtos = users.map(userMapper::toResponseDTO);
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get user by ID")
  public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
    User user = userService.getUserById(id);
    return ResponseEntity.ok(userMapper.toResponseDTO(user));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create new user")
  @ApiResponse(responseCode = "201", description = "User created successfully")
  public ResponseEntity<UserResponseDTO> createUser(
      @Valid @RequestBody CreateUserRequestDTO requestDTO
  ) {
    User user = userService.createUser(requestDTO);
    UserResponseDTO responseDTO = userMapper.toResponseDTO(user);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(responseDTO);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update user")
  public ResponseEntity<UserResponseDTO> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody UpdateUserRequestDTO requestDTO
  ) {
    User user = userService.updateUser(id, requestDTO);
    return ResponseEntity.ok(userMapper.toResponseDTO(user));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete user")
  @ApiResponse(responseCode = "204", description = "User deleted successfully")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
```

### Annotation Rules

**Always use:**
- `@RestController` - For REST controllers
- `@RequestMapping` - For base path
- `@GetMapping`, `@PostMapping`, etc. - For HTTP methods
- `@PathVariable` - For URL parameters
- `@RequestParam` - For query parameters
- `@Valid` - For DTO validation
- `@PreAuthorize` - For authorization
- `@RequiredArgsConstructor` - For dependency injection (Lombok)

**Always document with:**
- `@Tag` - For endpoint grouping in Swagger
- `@Operation` - For endpoint description
- `@ApiResponse` - For response documentation

---

## Service Layer

### Service Responsibilities

**DO:**
- Implement business logic
- Manage transactions (@Transactional)
- Orchestrate repositories
- Validate business rules
- Throw custom exceptions
- Handle edge cases

**DON'T:**
- Place HTTP logic
- Access HTTP context
- Duplicate business logic
- Ignore transaction boundaries

### Service Example

```java
@Service
@RequiredArgsConstructor
public class UserService {
  
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  /**
   * Retrieve paginated list of users
   */
  public Page<User> listUsers(int page, int size, String role) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    
    if (role != null) {
      return userRepository.findByRole(Role.valueOf(role), pageable);
    }
    return userRepository.findAll(pageable);
  }

  /**
   * Get user by ID or throw exception
   */
  public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
  }

  /**
   * Create new user with validation
   */
  @Transactional
  public User createUser(CreateUserRequestDTO dto) {
    // Validate username uniqueness
    if (userRepository.existsByUsername(dto.getUsername())) {
      throw new BusinessException("Username already exists");
    }

    // Validate email uniqueness
    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new BusinessException("Email already exists");
    }

    // Create and save user
    User user = User.builder()
        .username(dto.getUsername())
        .email(dto.getEmail())
        .fullName(dto.getFullName())
        .password(passwordEncoder.encode(dto.getPassword()))
        .role(Role.valueOf(dto.getRole()))
        .build();

    return userRepository.save(user);
  }

  /**
   * Update user with validation
   */
  @Transactional
  public User updateUser(Long id, UpdateUserRequestDTO dto) {
    User user = getUserById(id);

    // Check email uniqueness if changed
    if (!user.getEmail().equals(dto.getEmail()) && 
        userRepository.existsByEmail(dto.getEmail())) {
      throw new BusinessException("Email already exists");
    }

    // Update fields
    user.setEmail(dto.getEmail());
    user.setFullName(dto.getFullName());
    
    if (dto.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(dto.getPassword()));
    }

    return userRepository.save(user);
  }

  /**
   * Delete user
   */
  @Transactional
  public void deleteUser(Long id) {
    User user = getUserById(id);
    userRepository.delete(user);
  }
}
```

### Transaction Management

**@Transactional Usage:**

```java
// Read-only queries (performance optimization)
@Transactional(readOnly = true)
public User getUserById(Long id) {
  return userRepository.findById(id).orElseThrow(...);
}

// Write operations (default)
@Transactional
public User createUser(CreateUserRequestDTO dto) {
  // Save and flush to database
  return userRepository.save(mapToEntity(dto));
}

// Custom propagation
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void processRefund(Order order) {
  // Separate transaction
}
```

### Exception Throwing

```java
// For missing resources
throw new ResourceNotFoundException("User not found with ID: " + id);

// For business rule violations
throw new BusinessException("Username already exists");

// For invalid requests
throw new ValidationException("Email format is invalid");

// For authorization failures
throw new UnauthorizedException("You don't have permission for this operation");
```

---

## Repository Layer

### Repository Responsibilities

**DO:**
- Use Spring Data JPA methods
- Create custom queries with @Query
- Use pagination and sorting
- Optimize queries
- Use projections

**DON'T:**
- Place business logic
- Use native SQL unless necessary
- Fetch unnecessary data
- Create N+1 query problems

### Repository Example

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  
  // Simple finder methods (auto-generated by Spring Data)
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  
  // Check existence
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  
  // Custom query with parameters
  @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
  Page<User> findByRole(@Param("role") Role role, Pageable pageable);
  
  // Query with LIKE
  @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
  List<User> searchByName(@Param("searchTerm") String searchTerm);
  
  // Count with condition
  @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
  long countByRole(@Param("role") Role role);
  
  // Projection for DTO response
  @Query("SELECT new com.tlu.dto.response.UserNameDTO(u.id, u.fullName) FROM User u")
  List<UserNameDTO> getAllUserNames();
  
  // Delete with condition
  @Modifying
  @Query("DELETE FROM User u WHERE u.createdAt < :date")
  void deleteOlderThan(@Param("date") LocalDateTime date);
}
```

### Custom Query Best Practices

**Simple Named Queries (Spring Data):**
```java
// Spring generates SQL automatically
List<User> findByRoleAndIsActive(Role role, boolean isActive);
Page<Student> findBySemesterId(Long semesterId, Pageable pageable);
```

**Complex Queries (@Query):**
```java
@Query("""
    SELECT new com.tlu.dto.response.StudentGradeDTO(
      s.id, s.fullName, AVG(g.score)
    )
    FROM Student s
    LEFT JOIN Grade g ON s.id = g.student.id
    WHERE s.majors.id = :majorId
    GROUP BY s.id, s.fullName
    HAVING AVG(g.score) > :minGpa
""")
List<StudentGradeDTO> findStudentsByMajorAndMinGpa(
    @Param("majorId") Long majorId,
    @Param("minGpa") Double minGpa
);
```

### Pagination & Sorting

```java
// Page 0, 20 items, sorted by name ascending
Pageable pageable = PageRequest.of(0, 20, Sort.by("name").ascending());
Page<User> users = userRepository.findAll(pageable);

// Multiple sorts
Sort sort = Sort.by("role").ascending().and(Sort.by("name").ascending());
Page<User> users = userRepository.findAll(PageRequest.of(0, 20, sort));

// Access paginated results
List<User> items = users.getContent();
int totalPages = users.getTotalPages();
long totalElements = users.getTotalElements();
```

### Projection for Performance

```java
// DTO Projection (reduces data fetched)
@Query("""
    SELECT new com.tlu.dto.response.UserSummaryDTO(u.id, u.fullName, u.email)
    FROM User u
    WHERE u.id = :id
""")
Optional<UserSummaryDTO> findUserSummary(@Param("id") Long id);

// Only fetch needed columns instead of entire entity
@Query("SELECT u.id, u.fullName FROM User u WHERE u.role = 'STUDENT'")
List<Object[]> findStudentNamesAndIds();
```

---

## Entity & JPA Patterns

### Entity Example

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_email", columnList = "email", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, length = 255)
  private String password;

  @Column(name = "full_name", nullable = false, length = 100)
  private String fullName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Relationships
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ChatRoom> chatRooms = new ArrayList<>();

  @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<ChatMessage> messages = new ArrayList<>();
}
```

### Relationship Patterns

**One-to-Many (Most Common):**
```java
@Entity
public class Course {
  @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Enrollment> enrollments = new ArrayList<>();
}

@Entity
public class Enrollment {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;
}
```

**Many-to-Many:**
```java
@Entity
public class Student {
  @ManyToMany
  @JoinTable(
      name = "student_courses",
      joinColumns = @JoinColumn(name = "student_id"),
      inverseJoinColumns = @JoinColumn(name = "course_id")
  )
  private Set<Course> courses = new HashSet<>();
}

@Entity
public class Course {
  @ManyToMany(mappedBy = "courses")
  private Set<Student> students = new HashSet<>();
}
```

**Fetch Strategy Rules:**

```java
// ✅ Use LAZY for most relationships (avoid N+1 problems)
@OneToMany(fetch = FetchType.LAZY)
private List<Enrollment> enrollments;

// ❌ Avoid EAGER unless absolutely necessary
@OneToMany(fetch = FetchType.EAGER) // Can cause N+1 queries
private List<Enrollment> enrollments;

// Use JOIN FETCH in queries to eagerly load when needed
@Query("SELECT u FROM User u JOIN FETCH u.enrollments WHERE u.id = :id")
Optional<User> findUserWithEnrollments(@Param("id") Long id);
```

### JPA Annotations

**Column-Level:**
- `@Column(nullable = false)` - NOT NULL constraint
- `@Column(unique = true)` - UNIQUE constraint
- `@Column(length = 100)` - VARCHAR length
- `@Transient` - Not mapped to database
- `@CreationTimestamp` - Auto-set on creation
- `@UpdateTimestamp` - Auto-update timestamp

**Entity-Level:**
- `@Entity` - Map to database table
- `@Table(name = "table_name")` - Custom table name
- `@Index` - Database indexes
- `@Inheritance(strategy = InheritanceType.JOINED)` - Table inheritance

**Relationship:**
- `@OneToMany` - One entity to many others
- `@ManyToOne` - Many entities to one
- `@ManyToMany` - Many to many relationship
- `@OneToOne` - One-to-one relationship
- `@JoinColumn` - Foreign key specification
- `@JoinTable` - Join table for many-to-many

---

## DTO Patterns & Mapping

### DTO Structure

**Request DTOs:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDTO {
  
  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50)
  private String username;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters")
  private String password;

  @NotBlank(message = "Full name is required")
  private String fullName;

  @NotNull(message = "Role is required")
  @Pattern(regexp = "ADMIN|STUDENT|TEACHER")
  private String role;
}
```

**Response DTOs:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
  private Long id;
  private String username;
  private String email;
  private String fullName;
  private String role;
  private boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
```

### Mapper Pattern

```java
@Service
public class UserMapper {

  /**
   * Map entity to response DTO
   */
  public UserResponseDTO toResponseDTO(User entity) {
    return UserResponseDTO.builder()
        .id(entity.getId())
        .username(entity.getUsername())
        .email(entity.getEmail())
        .fullName(entity.getFullName())
        .role(entity.getRole().name())
        .isActive(entity.isActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  /**
   * Map request DTO to entity
   */
  public User toEntity(CreateUserRequestDTO dto) {
    return User.builder()
        .username(dto.getUsername())
        .email(dto.getEmail())
        .fullName(dto.getFullName())
        .role(Role.valueOf(dto.getRole()))
        .isActive(true)
        .build();
  }

  /**
   * Map list of entities
   */
  public List<UserResponseDTO> toResponseDTOs(List<User> entities) {
    return entities.stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }

  /**
   * Map page of entities
   */
  public Page<UserResponseDTO> toResponseDTOPage(Page<User> entities) {
    return entities.map(this::toResponseDTO);
  }
}
```

### DTO Validation Rules

**Always validate:**
- Required fields with `@NotNull`, `@NotBlank`
- Email format with `@Email`
- String lengths with `@Size`
- Numeric ranges with `@Min`, `@Max`
- Custom business rules with `@Validated`

**Example with custom validation:**
```java
@Data
public class CreateCourseRequestDTO {
  
  @NotBlank
  private String courseCode;

  @NotBlank
  private String name;

  @Min(value = 1, message = "Credits must be at least 1")
  @Max(value = 4, message = "Credits cannot exceed 4")
  private Integer credits;

  @DecimalMin("0.0")
  @DecimalMax("1000000.0")
  private BigDecimal pricePerCredit;
}
```

---

## Authentication & Authorization

### JWT Token Provider

```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
  
  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.expiration}")
  private long jwtExpirationInMs;

  @Value("${app.jwt.refresh-expiration}")
  private long refreshTokenExpirationInMs;

  public String generateToken(UserDetails userDetails) {
    long now = System.currentTimeMillis();
    long expiryDate = now + jwtExpirationInMs;

    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .claim("roles", userDetails.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()))
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(expiryDate))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }

  public String generateRefreshToken(UserDetails userDetails) {
    long now = System.currentTimeMillis();
    long expiryDate = now + refreshTokenExpirationInMs;

    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(expiryDate))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }

  public String getUsernameFromJWT(String token) {
    Claims claims = Jwts.parser()
        .setSigningKey(jwtSecret)
        .parseClaimsJws(token)
        .getBody();
    return claims.getSubject();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser()
          .setSigningKey(jwtSecret)
          .parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException ex) {
      return false;
    }
  }
}
```

### Security Configuration

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .cors().and()
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/student/**").hasRole("STUDENT")
            .requestMatchers("/api/teacher/**").hasRole("TEACHER")
            .anyRequest().authenticated()
        )
        .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(authenticationProvider);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
```

### Login Endpoint

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              request.getUsername(),
              request.getPassword()
          )
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);

      UserDetails userDetails = (UserDetails) authentication.getPrincipal();
      String accessToken = jwtTokenProvider.generateToken(userDetails);
      String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

      User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

      return ResponseEntity.ok(AuthResponseDTO.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .role(user.getRole().name())
          .build());
    } catch (BadCredentialsException ex) {
      throw new UnauthorizedException("Invalid username or password");
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponseDTO> refreshToken(
      @RequestBody RefreshTokenRequestDTO request
  ) {
    if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    String username = jwtTokenProvider.getUsernameFromJWT(request.getRefreshToken());
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    UserDetails userDetails = new CustomUserDetails(user);
    String accessToken = jwtTokenProvider.generateToken(userDetails);

    return ResponseEntity.ok(AuthResponseDTO.builder()
        .accessToken(accessToken)
        .refreshToken(request.getRefreshToken())
        .role(user.getRole().name())
        .build());
  }
}
```

### Role-Based Access Control

```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Page<UserResponseDTO>> listUsers(...) { }

@PreAuthorize("hasRole('STUDENT')")
public ResponseEntity<UserProfileDTO> getProfile() { }

@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public ResponseEntity<PageResponseDTO> getClassList() { }

// Method-level security
@Secured("ROLE_ADMIN")
public void deleteUser(Long id) { }

// Check ownership
@PreAuthorize("@userService.isOwnerOrAdmin(#userId, principal.username)")
public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long userId) { }
```

---

## Validation Rules

### Built-in Validation Annotations

```java
@Data
public class CreateEnrollmentRequestDTO {
  
  @NotNull(message = "Student ID cannot be null")
  private Long studentId;

  @NotNull(message = "Class section ID cannot be null")
  private Long classSectionId;

  @NotBlank(message = "Enrollment type cannot be blank")
  @Pattern(regexp = "REGULAR|RETAKE|IMPROVE")
  private String enrollmentType;

  @Min(value = 1, message = "Attempt number must be at least 1")
  private Integer attemptNumber;
}
```

### Custom Validation

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
  String message() default "Email must be unique";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
  
  @Autowired
  private UserRepository userRepository;

  @Override
  public boolean isValid(String email, ConstraintValidatorContext context) {
    if (email == null) return true;
    return !userRepository.existsByEmail(email);
  }
}

// Usage in DTO
@Data
public class CreateUserRequestDTO {
  @UniqueEmail
  private String email;
}
```

### Validation in Service

```java
@Service
public class EnrollmentService {
  
  @Transactional
  public Enrollment createEnrollment(CreateEnrollmentRequestDTO dto) {
    // Validate student exists
    Student student = studentRepository.findById(dto.getStudentId())
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    // Validate class section exists
    ClassSection classSection = classSectionRepository.findById(dto.getClassSectionId())
        .orElseThrow(() -> new ResourceNotFoundException("Class section not found"));

    // Business logic validation
    if (classSection.getAvailableSlots() <= 0) {
      throw new BusinessException("Class section is full");
    }

    if (enrollmentRepository.existsByStudentAndClassSection(student, classSection)) {
      throw new BusinessException("Student already enrolled in this class");
    }

    // Create enrollment
    return enrollmentRepository.save(Enrollment.builder()
        .student(student)
        .classSection(classSection)
        .enrollmentType(EnrollmentType.valueOf(dto.getEnrollmentType()))
        .attemptNumber(dto.getAttemptNumber())
        .build());
  }
}
```

---

## Error Handling

### Custom Exceptions

```java
// Base exception
public class ApplicationException extends RuntimeException {
  public ApplicationException(String message) {
    super(message);
  }

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
  }
}

// Specific exceptions
public class ResourceNotFoundException extends ApplicationException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}

public class BusinessException extends ApplicationException {
  public BusinessException(String message) {
    super(message);
  }
}

public class UnauthorizedException extends ApplicationException {
  public UnauthorizedException(String message) {
    super(message);
  }
}

public class ValidationException extends ApplicationException {
  public ValidationException(String message) {
    super(message);
  }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
      ResourceNotFoundException ex,
      HttpServletRequest request
  ) {
    log.warn("Resource not found: {}", ex.getMessage());
    
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .code("RESOURCE_NOT_FOUND")
        .message(ex.getMessage())
        .timestamp(LocalDateTime.now())
        .path(request.getRequestURI())
        .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponseDTO> handleBusinessException(
      BusinessException ex,
      HttpServletRequest request
  ) {
    log.warn("Business rule violation: {}", ex.getMessage());
    
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .code("BUSINESS_RULE_VIOLATION")
        .message(ex.getMessage())
        .timestamp(LocalDateTime.now())
        .path(request.getRequestURI())
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponseDTO> handleValidationException(
      MethodArgumentNotValidException ex,
      HttpServletRequest request
  ) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
    );

    ValidationErrorResponseDTO error = ValidationErrorResponseDTO.builder()
        .code("VALIDATION_ERROR")
        .message("Validation failed")
        .errors(errors)
        .timestamp(LocalDateTime.now())
        .path(request.getRequestURI())
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleGeneralException(
      Exception ex,
      HttpServletRequest request
  ) {
    log.error("Unexpected error", ex);
    
    ErrorResponseDTO error = ErrorResponseDTO.builder()
        .code("INTERNAL_SERVER_ERROR")
        .message("An unexpected error occurred")
        .timestamp(LocalDateTime.now())
        .path(request.getRequestURI())
        .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
```

### Error Response DTOs

```java
@Data
@Builder
public class ErrorResponseDTO {
  private String code;
  private String message;
  private LocalDateTime timestamp;
  private String path;
}

@Data
@Builder
public class ValidationErrorResponseDTO {
  private String code;
  private String message;
  private Map<String, String> errors;
  private LocalDateTime timestamp;
  private String path;
}
```

---

## Database Optimization

### Index Strategy

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username", unique = true),
    @Index(name = "idx_email", columnList = "email", unique = true),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
public class User {
  // ... fields
}
```

### Query Optimization

**Avoid N+1 Queries:**
```java
// ❌ Bad: N+1 problem
List<Student> students = studentRepository.findAll();
for (Student student : students) {
  // This triggers query for EACH student
  List<Enrollment> enrollments = student.getEnrollments();
}

// ✅ Good: Single query with JOIN FETCH
@Query("SELECT s FROM Student s JOIN FETCH s.enrollments WHERE s.majors.id = :majorId")
List<Student> findStudentsWithEnrollmentsByMajor(@Param("majorId") Long majorId);
```

**Use Pagination:**
```java
// ❌ Bad: Load 1 million records
List<User> users = userRepository.findAll();

// ✅ Good: Paginate
Page<User> users = userRepository.findAll(PageRequest.of(0, 20));
```

**Use Projections:**
```java
// ❌ Bad: Fetch entire entity with related data
User user = userRepository.findById(1L).orElseThrow();

// ✅ Good: Fetch only needed fields
@Query("SELECT new com.tlu.dto.UserSummaryDTO(u.id, u.fullName) FROM User u WHERE u.id = :id")
Optional<UserSummaryDTO> findUserSummary(@Param("id") Long id);
```

### Connection Pooling

```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

---

## Swagger/OpenAPI Documentation

### Controller Documentation

```java
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "User management endpoints for administrators")
public class UserController {

  @GetMapping
  @Operation(
      summary = "List all users",
      description = "Retrieve a paginated list of all users with optional filtering",
      operationId = "listUsers"
  )
  @ApiResponse(
      responseCode = "200",
      description = "Users retrieved successfully",
      content = @Content(schema = @Schema(implementation = Page.class))
  )
  @ApiResponse(responseCode = "401", description = "Unauthorized")
  @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
  public ResponseEntity<Page<UserResponseDTO>> listUsers(
      @RequestParam(defaultValue = "0")
      @Parameter(description = "Page number (0-indexed)")
      int page,
      
      @RequestParam(defaultValue = "20")
      @Parameter(description = "Page size")
      int size
  ) {
    // ...
  }

  @PostMapping
  @Operation(summary = "Create new user")
  @ApiResponse(responseCode = "201", description = "User created successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request data")
  @ApiResponse(responseCode = "409", description = "User already exists")
  public ResponseEntity<UserResponseDTO> createUser(
      @Valid @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User creation request",
          required = true
      )
      CreateUserRequestDTO requestDTO
  ) {
    // ...
  }
}
```

### DTO Documentation

```java
@Data
@Schema(description = "User creation request")
public class CreateUserRequestDTO {
  
  @NotBlank
  @Schema(description = "Username", example = "john.doe", minLength = 3, maxLength = 50)
  private String username;

  @NotBlank
  @Email
  @Schema(description = "User email address", example = "john@example.com")
  private String email;

  @NotBlank
  @Size(min = 8)
  @Schema(description = "User password", example = "SecurePass123!")
  private String password;

  @NotBlank
  @Schema(description = "User full name", example = "John Doe")
  private String fullName;

  @NotNull
  @Schema(description = "User role", example = "ADMIN", allowableValues = {"ADMIN", "STUDENT", "TEACHER"})
  private String role;
}
```

### Swagger Configuration

```java
@Configuration
public class OpenApiConfig {
  
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Thang Long University API")
            .version("1.0.0")
            .description("REST API for Thang Long University Management System")
            .contact(new Contact()
                .name("Development Team")
                .email("dev@thang-long-university.edu")
            )
        )
        .addServersItem(new Server()
            .url("http://localhost:8080")
            .description("Development Server")
        )
        .addServersItem(new Server()
            .url("https://api.thang-long-university.edu")
            .description("Production Server")
        )
        .components(new Components()
            .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token")
            )
        );
  }
}
```

---

## Transaction Management

### Transaction Best Practices

```java
@Service
@RequiredArgsConstructor
public class CourseService {
  
  private final courseRepository courseRepository;
  private final enrollmentRepository enrollmentRepository;

  /**
   * Read-only operation - improves performance
   */
  @Transactional(readOnly = true)
  public Page<CourseResponseDTO> listCourses(Pageable pageable) {
    return courseRepository.findAll(pageable)
        .map(this::toResponseDTO);
  }

  /**
   * Write operation with automatic transaction management
   */
  @Transactional
  public CourseResponseDTO createCourse(CreateCourseRequestDTO dto) {
    Course course = Course.builder()
        .courseCode(dto.getCourseCode())
        .name(dto.getName())
        .build();
    
    Course saved = courseRepository.save(course);
    return toResponseDTO(saved);
  }

  /**
   * Multiple database operations in single transaction
   */
  @Transactional
  public void softDeleteCourse(Long courseId) {
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    
    // Mark enrollments as cancelled
    course.getEnrollments().forEach(enrollment -> {
      enrollment.setStatus(EnrollmentStatus.CANCELLED);
      enrollmentRepository.save(enrollment);
    });
    
    // Mark course as inactive
    course.setActive(false);
    courseRepository.save(course);
  }

  /**
   * Separate transaction for specific operation
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logAuditEvent(String action) {
    // Audit log stored in separate transaction
    // Ensures audit logging even if main transaction rolls back
  }
}
```

---

## Kafka Event Patterns

### Kafka Producer

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentEventProducer {
  
  private final KafkaTemplate<String, EnrollmentEventDTO> kafkaTemplate;

  public void publishEnrollmentCreated(Enrollment enrollment) {
    EnrollmentEventDTO event = EnrollmentEventDTO.builder()
        .eventType("ENROLLMENT_CREATED")
        .studentId(enrollment.getStudent().getId())
        .courseId(enrollment.getCourse().getId())
        .timestamp(LocalDateTime.now())
        .build();

    kafkaTemplate.send("enrollment-events", 
        String.valueOf(enrollment.getStudent().getId()), 
        event)
        .addCallback(
            result -> log.info("Sent enrollment event: {}", event),
            ex -> log.error("Failed to send enrollment event", ex)
        );
  }
}
```

### Kafka Consumer

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentEventConsumer {
  
  private final EnrollmentService enrollmentService;

  @KafkaListener(topics = "enrollment-events", groupId = "enrollment-group")
  public void handleEnrollmentEvent(EnrollmentEventDTO event) {
    try {
      switch (event.getEventType()) {
        case "ENROLLMENT_CREATED":
          enrollmentService.handleEnrollmentCreated(event);
          break;
        case "ENROLLMENT_CANCELLED":
          enrollmentService.handleEnrollmentCancelled(event);
          break;
        default:
          log.warn("Unknown event type: {}", event.getEventType());
      }
    } catch (Exception ex) {
      log.error("Error processing enrollment event", ex);
      // Consider implementing retry logic or dead letter queue
    }
  }
}
```

### Kafka Configuration

```java
@Configuration
public class KafkaConfig {
  
  @Bean
  public ProducerFactory<String, EnrollmentEventDTO> producerFactory() {
    return new DefaultKafkaProducerFactory<>(
        Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        )
    );
  }

  @Bean
  public KafkaTemplate<String, EnrollmentEventDTO> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public ConsumerFactory<String, EnrollmentEventDTO> consumerFactory() {
    return new DefaultKafkaConsumerFactory<>(
        Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG, "enrollment-group",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
            JsonDeserializer.VALUE_DEFAULT_TYPE, "com.tlu.dto.EnrollmentEventDTO"
        )
    );
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, EnrollmentEventDTO> kafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, EnrollmentEventDTO> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setCommonErrorHandler(new DefaultErrorHandler());
    factory.setConcurrency(3);
    return factory;
  }
}
```

---

## Redis Caching

### Cache Configuration

```java
@Configuration
@EnableCaching
public class CacheConfig {
  
  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10))
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new StringRedisSerializer()
            )
        )
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer()
            )
        );

    return RedisCacheManager.create(connectionFactory);
  }
}
```

### Caching in Service Layer

```java
@Service
@RequiredArgsConstructor
public class StudentService {
  
  private final studentRepository studentRepository;
  private final cacheManager cacheManager;

  /**
   * Cache result for 10 minutes
   */
  @Cacheable(value = "students", key = "#id", unless = "#result == null")
  @Transactional(readOnly = true)
  public StudentResponseDTO getStudent(Long id) {
    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    return toResponseDTO(student);
  }

  /**
   * Invalidate cache when updating
   */
  @CacheEvict(value = "students", key = "#id")
  @Transactional
  public StudentResponseDTO updateStudent(Long id, UpdateStudentRequestDTO dto) {
    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    // Update logic...
    return toResponseDTO(student);
  }

  /**
   * Invalidate entire cache when deleting
   */
  @CacheEvict(value = "students", allEntries = true)
  @Transactional
  public void deleteStudent(Long id) {
    Student student = studentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    studentRepository.delete(student);
  }

  /**
   * Cache list results
   */
  @Cacheable(value = "students_by_major", key = "#majorId")
  public List<StudentResponseDTO> findByMajor(Long majorId) {
    return studentRepository.findByMajorId(majorId)
        .stream()
        .map(this::toResponseDTO)
        .collect(Collectors.toList());
  }
}
```

---

## Query Patterns

### Simple Query Pattern

```java
// Find by ID
public User getUserById(Long id) {
  return userRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));
}

// Find one with optional
public Optional<User> findByUsername(String username) {
  return userRepository.findByUsername(username);
}

// Count
public long countActiveUsers() {
  return userRepository.countByIsActiveTrue();
}
```

### Pagination Pattern

```java
public Page<UserResponseDTO> listUsers(int page, int size, String role) {
  Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
  
  Page<User> users = role != null
      ? userRepository.findByRole(Role.valueOf(role), pageable)
      : userRepository.findAll(pageable);

  return users.map(this::toResponseDTO);
}
```

### Filtering Pattern

```java
public List<Course> findCoursesByFilters(CourseFilterDTO filter) {
  return courseRepository.findAll((root, query, criteriaBuilder) -> {
    List<Predicate> predicates = new ArrayList<>();

    if (filter.getMajorId() != null) {
      predicates.add(criteriaBuilder.equal(root.get("major").get("id"), filter.getMajorId()));
    }

    if (filter.getMinCredits() != null) {
      predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("credits"), filter.getMinCredits()));
    }

    if (filter.getSearchTerm() != null) {
      Predicate namePredicate = criteriaBuilder.like(
          criteriaBuilder.lower(root.get("name")),
          "%" + filter.getSearchTerm().toLowerCase() + "%"
      );
      predicates.add(namePredicate);
    }

    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
  });
}
```

### Batch Operations Pattern

```java
@Transactional
public void batchUpdateGrades(List<GradeUpdateDTO> updates) {
  List<Grade> grades = updates.stream()
      .map(update -> {
        Grade grade = gradeRepository.findById(update.getGradeId())
            .orElseThrow();
        grade.setFinalScore(update.getFinalScore());
        return grade;
      })
      .collect(Collectors.toList());

  gradeRepository.saveAll(grades);
}
```

---

## Feature Creation Workflow

### Before Generating Code: Analysis Phase

1. **Analyze similar existing services**
   - Look in `src/main/java/com/tlu/service/`
   - Study entity relationships
   - Review repository patterns

2. **Identify reusable patterns**
   - Which exception types apply?
   - Which validation patterns exist?
   - Which security roles are involved?

3. **Review database schema**
   - Check `sql/schema.sql`
   - Understand relationships
   - Plan indexes

### Code Generation Phase

#### Step 1: Create Entity

```java
@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "class_section_id", nullable = false)
  private ClassSection classSection;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;
}
```

#### Step 2: Create DTOs

Request DTO:
```java
@Data
@Schema(description = "Enrollment creation request")
public class CreateEnrollmentRequestDTO {
  @NotNull
  private Long studentId;
  
  @NotNull
  private Long classSectionId;
}
```

Response DTO:
```java
@Data
@Builder
@Schema(description = "Enrollment response")
public class EnrollmentResponseDTO {
  private Long id;
  private Long studentId;
  private String studentName;
  private Long classSectionId;
  private String classCode;
  private LocalDateTime createdAt;
}
```

#### Step 3: Create Repository

```java
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
  boolean existsByStudentAndClassSection(Student student, ClassSection classSection);
  
  @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId")
  List<Enrollment> findByStudentId(@Param("studentId") Long studentId);

  @Query("""
      SELECT e FROM Enrollment e
      WHERE e.classSection.id = :classSectionId
      ORDER BY e.createdAt DESC
  """)
  List<Enrollment> findByClassSectionId(@Param("classSectionId") Long classSectionId);
}
```

#### Step 4: Create Service

```java
@Service
@RequiredArgsConstructor
public class EnrollmentService {
  
  private final enrollmentRepository enrollmentRepository;
  private final studentRepository studentRepository;
  private final classSectionRepository classSectionRepository;

  @Transactional(readOnly = true)
  public Page<EnrollmentResponseDTO> listEnrollments(Long studentId, Pageable pageable) {
    Page<Enrollment> enrollments = enrollmentRepository
        .findByStudentId(studentId);
    return enrollments.map(this::toResponseDTO);
  }

  @Transactional
  public EnrollmentResponseDTO createEnrollment(CreateEnrollmentRequestDTO dto) {
    // Validation...
    Enrollment enrollment = Enrollment.builder()
        .student(student)
        .classSection(classSection)
        .build();
    
    return toResponseDTO(enrollmentRepository.save(enrollment));
  }

  @Transactional
  public void deleteEnrollment(Long id) {
    Enrollment enrollment = enrollmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
    enrollmentRepository.delete(enrollment);
  }
}
```

#### Step 5: Create Controller

```java
@RestController
@RequestMapping("/api/student/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
  
  private final enrollmentService enrollmentService;

  @GetMapping
  public ResponseEntity<Page<EnrollmentResponseDTO>> listEnrollments(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Page<EnrollmentResponseDTO> enrollments = enrollmentService
        .listEnrollments(getCurrentStudentId(), PageRequest.of(page, size));
    return ResponseEntity.ok(enrollments);
  }

  @PostMapping
  public ResponseEntity<EnrollmentResponseDTO> createEnrollment(
      @Valid @RequestBody CreateEnrollmentRequestDTO dto
  ) {
    EnrollmentResponseDTO enrollment = enrollmentService.createEnrollment(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEnrollment(@PathVariable Long id) {
    enrollmentService.deleteEnrollment(id);
    return ResponseEntity.noContent().build();
  }
}
```

---

## Code Generation Rules

### General Principles

**When generating code:**

1. **Explain your work**
   - List files being created
   - Explain architecture decisions
   - Explain reuse of existing patterns

2. **Follow layered architecture**
   - Controller → Service → Repository
   - Keep layers focused
   - Use proper separation

3. **Enforce type safety**
   - Use strong typing
   - Validate inputs
   - Handle null safety

4. **Generate scalable code**
   - Use pagination for lists
   - Use caching strategically
   - Optimize queries
   - Proper indexing

5. **Document thoroughly**
   - Swagger annotations
   - JavaDoc comments
   - Clear method names

### What to Do

✅ **DO:**
- Analyze existing similar modules first
- Reuse entity relationships
- Reuse service patterns
- Reuse validation approach
- Follow established naming conventions
- Use established exception types
- Extend existing repositories
- Implement proper transaction boundaries

### What NOT to Do

❌ **DON'T:**
- Create custom HTTP logic
- Use raw SQL unnecessarily
- Bypass repository layer
- Place business logic in controllers
- Expose entities directly
- Create unnecessary exceptions
- Ignore pagination
- Hardcode configuration values
- Use raw passwords
- Bypass authentication/authorization

---

## Common Patterns & Anti-Patterns

### ✅ Pattern: Layered Request Handling

```java
// Good: Proper separation of concerns
@RestController
@RequestMapping("/api/students")
public class StudentController {
  private final StudentService studentService;

  @PostMapping
  public ResponseEntity<StudentResponseDTO> create(
      @Valid @RequestBody CreateStudentRequestDTO dto
  ) {
    StudentResponseDTO response = studentService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}

@Service
public class StudentService {
  private final StudentRepository repository;

  @Transactional
  public StudentResponseDTO create(CreateStudentRequestDTO dto) {
    // Validate business rules
    if (repository.existsByCode(dto.getCode())) {
      throw new BusinessException("Student code already exists");
    }
    
    // Create entity
    Student student = Student.builder().code(dto.getCode()).build();
    Student saved = repository.save(student);
    
    return toResponseDTO(saved);
  }
}
```

### ❌ Anti-Pattern: Mixed Concerns

```java
// Bad: Business logic in controller
@RestController
public class StudentController {
  @PostMapping
  public ResponseEntity<Student> create(@RequestBody CreateStudentRequestDTO dto) {
    // Validation in controller
    if (dto.getCode() == null) {
      throw new IllegalArgumentException("Code required");
    }
    
    // Database access in controller
    if (studentRepository.existsByCode(dto.getCode())) {
      throw new IllegalArgumentException("Already exists");
    }
    
    // Entity returned directly (no DTO)
    Student student = new Student(dto.getCode());
    return ResponseEntity.ok(studentRepository.save(student));
  }
}
```

### ✅ Pattern: Proper Query Optimization

```java
// Good: Paginated with proper fetching
@Query("""
    SELECT new StudentGradeDTO(s.id, s.fullName, AVG(g.score))
    FROM Student s
    LEFT JOIN Grade g ON s.id = g.student.id
    GROUP BY s.id
""")
Page<StudentGradeDTO> findStudentsWithAverageGrade(Pageable pageable);
```

### ❌ Anti-Pattern: Unoptimized Queries

```java
// Bad: Loads all students and processes in memory
List<Student> students = studentRepository.findAll();
List<StudentGradeDTO> grades = new ArrayList<>();

for (Student s : students) { // N+1 problem!
  Double avg = s.getGrades().stream()
      .mapToDouble(Grade::getScore)
      .average()
      .orElse(0);
  grades.add(new StudentGradeDTO(s.getId(), s.getFullName(), avg));
}
```

### ✅ Pattern: Proper Exception Handling

```java
// Good: Specific exceptions, clear messages
try {
  User user = userService.getUserById(userId);
  return ResponseEntity.ok(userMapper.toResponseDTO(user));
} catch (ResourceNotFoundException ex) {
  return ResponseEntity.notFound().build();
} catch (UnauthorizedException ex) {
  return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
}
```

### ❌ Anti-Pattern: Generic Exception Handling

```java
// Bad: Catches all exceptions, loses specificity
try {
  User user = userRepository.findById(userId).orElseThrow();
  return ResponseEntity.ok(user); // Exposes entity
} catch (Exception ex) {
  return ResponseEntity.badRequest().body("Error"); // Generic
}
```

### ✅ Pattern: Proper Validation

```java
// Good: Multi-level validation
@Transactional
public Enrollment createEnrollment(CreateEnrollmentRequestDTO dto) {
  // Business rule validation
  if (!classSection.hasAvailableSlots()) {
    throw new BusinessException("Class is full");
  }
  
  if (enrollmentRepository.existsByStudentAndClassSection(student, classSection)) {
    throw new BusinessException("Already enrolled");
  }
  
  return enrollmentRepository.save(enrollment);
}
```

### ❌ Anti-Pattern: Missing Validation

```java
// Bad: No validation
@Transactional
public Enrollment createEnrollment(CreateEnrollmentRequestDTO dto) {
  return enrollmentRepository.save(Enrollment.builder()
      .student(studentRepository.findById(dto.getStudentId()).orElseThrow())
      .classSection(classSectionRepository.findById(dto.getClassSectionId()).orElseThrow())
      .build());
}
```

---

## Performance Checklist

- [ ] Use pagination for large result sets
- [ ] Add database indexes for frequently queried columns
- [ ] Use `@Query` with projections to fetch only needed fields
- [ ] Use `JOIN FETCH` to avoid N+1 queries
- [ ] Configure connection pooling properly
- [ ] Use `readOnly = true` for read-only transactions
- [ ] Implement caching for frequently accessed data
- [ ] Use batch operations for bulk updates
- [ ] Profile slow queries with database logs
- [ ] Monitor active database connections

---

## Security Checklist

- [ ] All sensitive endpoints protected with `@PreAuthorize`
- [ ] Passwords hashed with BCryptPasswordEncoder
- [ ] JWT tokens validated on every request
- [ ] DTOs used for all responses (no entity exposure)
- [ ] Input validation on all endpoints
- [ ] SQL injection prevention via JPA parameterized queries
- [ ] CORS configured properly
- [ ] Sensitive data not logged
- [ ] Error messages don't expose internal details
- [ ] API versioning implemented if needed

---

## Summary: AI Agent Checklist

Before generating any backend code:

- [ ] Analyzed similar existing services/controllers
- [ ] Identified reusable patterns
- [ ] Confirmed database schema
- [ ] Planned proper layering (Controller → Service → Repository)
- [ ] Identified which exceptions apply
- [ ] Determined security requirements
- [ ] Planned caching strategy if needed
- [ ] Identified pagination needs

When generating code:

- [ ] Created proper entity with JPA annotations
- [ ] Created request and response DTOs
- [ ] Created repository with optimized queries
- [ ] Created service with business logic
- [ ] Created controller with proper endpoints
- [ ] Added Swagger documentation
- [ ] Implemented proper error handling
- [ ] Added validation (DTO + service layer)
- [ ] Followed security rules (authorization checks)
- [ ] No `@Transactional` on repository methods
- [ ] Proper fetch strategies (LAZY by default)
- [ ] Pagination implemented where applicable
- [ ] Explained what was created
- [ ] Explained what patterns were reused

---

## Questions & Support

**For questions about:**
- Architecture → See Layered Architecture
- REST APIs → See REST API Conventions
- Entities & JPA → See Entity & JPA Patterns
- Repositories → See Repository Layer
- Services → See Service Layer
- Controllers → See Controller Layer
- DTOs → See DTO Patterns & Mapping
- Security → See Authentication & Authorization
- Queries → See Query Patterns
- Performance → See Database Optimization
- Caching → See Redis Caching
- Events → See Kafka Event Patterns
- Errors → See Error Handling
- Validation → See Validation Rules

**Key Files to Reference:**
- `src/main/java/com/tlu/security/JwtTokenProvider.java` - JWT implementation
- `src/main/java/com/tlu/config/SecurityConfig.java` - Spring Security setup
- `src/main/java/com/tlu/exception/GlobalExceptionHandler.java` - Error handling
- `sql/schema.sql` - Database schema
- `application.properties` - Configuration

---

**Document Version:** 1.0  
**Last Updated:** May 19, 2026  
**Maintained by:** Backend Development Team
