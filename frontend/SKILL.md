# Frontend Architecture Skill — ThangLongUniversityWeb

> **For AI agents:** Read this before touching any code. Do not guess. Do not invent patterns.

---

## 1. Project Overview

University management portal (Thang Long University) với 3 role: ADMIN, STUDENT, TEACHER.

| Layer     | Technology                                      |
| --------- | ----------------------------------------------- |
| Framework | TanStack Start (Vite-based SSR) + React 19      |
| Routing   | TanStack Router v1.168 — file-based             |
| State     | TanStack Query v5.83 — server state ONLY        |
| Forms     | react-hook-form + Zod                           |
| UI        | shadcn/ui + TailwindCSS v4                      |
| HTTP      | Native Fetch API via `apiRequest` wrapper       |
| Auth      | JWT (accessToken + refreshToken) — localStorage |
| Icons     | lucide-react                                    |
| Toast     | sonner                                          |
| Charts    | recharts                                        |

---

## 2. Folder Architecture

```txt
frontend/
├── src/
│   ├── routes/              # File-based route pages (TanStack Router)
│   ├── features/            # Feature-scoped logic extracted from routes
│   │   ├── admin-class-sections/   # Components, types, mappers, validation
│   │   ├── chat/                   # ChatModule.tsx
│   │   ├── chatbot/                # ChatbotPanel, ChatbotWidget
│   │   ├── landing/                # ThangLongLanding.tsx
│   │   ├── semester-hub/           # Tabs for semester detail page
│   │   └── teacher/                # TeacherGradeTable, teacherMappers, useTeacherSemester
│   ├── components/
│   │   ├── layout/          # AppLayout.tsx, ProtectedOutlet.tsx
│   │   ├── ui/              # shadcn/ui + custom: page-header, status-badge, confirm-dialog
│   │   ├── data-table/      # DataTable.tsx
│   │   ├── forms/           # EntityFormDialog.tsx
│   │   └── marketing/       # Public-facing UI components
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts    # ⚡ CORE — apiRequest, getStoredAuth, setStoredAuth
│   │   │   ├── types.ts     # ALL DTO interfaces (783 lines) — source of truth
│   │   │   ├── auth.ts      # login, getMe, logout
│   │   │   ├── admin.ts     # Admin endpoints
│   │   │   ├── student.ts   # Student endpoints
│   │   │   ├── teacher.ts   # Teacher endpoints
│   │   │   ├── chat.ts      # Chat REST endpoints
│   │   │   ├── chatbot.ts   # Chatbot endpoints
│   │   │   ├── articles.ts  # Public articles
│   │   │   ├── notifications.ts
│   │   │   └── knowledge.ts
│   │   ├── auth.tsx         # AuthContext + useAuth() hook
│   │   ├── semester.ts      # pickCurrentSemester() util
│   │   ├── utils.ts         # cn() helper
│   │   ├── error-capture.ts
│   │   └── landing-content.tsx
│   ├── hooks/
│   │   └── use-mobile.tsx   # Responsive breakpoint hook
│   ├── data/
│   │   └── mock.ts          # ⚠️ Legacy mock — do NOT add new mocks here
│   ├── routeTree.gen.ts     # 🚫 AUTO-GENERATED — never edit manually
│   ├── router.tsx           # QueryClient + Router setup
│   ├── styles.css           # Tailwind directives
│   └── __root.tsx           # Root component
├── docs/                    # PROJECT.md, FRONTEND_RULES.md, API_GUIDE.md, etc.
├── package.json
└── vite.config.ts
```

---

## 3. Feature Structure Convention

Features that are too large for a single route file are extracted into `src/features/{name}/`.

**Real example — `admin-class-sections`:**

```txt
features/admin-class-sections/
  AdminClassSectionsContent.tsx  # Main content component
  ClassSectionFormDialog.tsx      # Form modal
  ClassSectionSelectField.tsx     # Reusable select field
  ClassSectionStudentsDialog.tsx  # Students in class dialog
  ClassSectionsByMajor.tsx        # Filtered view
  ClassSectionsTable.tsx          # Table display
  classSectionMappers.ts          # API → UI type mappers
  types.ts                        # Feature-local types (extends lib/api/types)
  validation.ts                   # Business validation logic
```

**Real example — `teacher`:**

```txt
features/teacher/
  TeacherGradeTable.tsx    # Grade entry table
  teacherMappers.ts        # API → UI mappers
  useTeacherSemester.ts    # Custom hook (useQuery + local state)
```

**Pattern rules:**

- Feature `types.ts` imports from `@/lib/api/types` — never duplicates base types
- `mappers.ts` converts `APIResponse → UIRow` — no logic in components
- `validation.ts` contains pure validation functions — no API calls
- Custom hooks in features use `useQuery`/`useMutation` internally

---

## 4. Routing Convention

```txt
Pattern: {role}.{feature}.tsx
Dynamic: {role}.{feature}.$paramId.tsx
Nested layout: {role}.tsx (parent), {role}.{feature}.tsx (child)
Index: {role}.{feature}.index.tsx
```

**Real route files:**

```
admin.tsx                    → /admin (layout: ProtectedOutlet ADMIN)
admin.dashboard.tsx          → /admin/dashboard
admin.students.tsx           → /admin/students
admin.semesters.tsx          → /admin/semesters (layout only)
admin.semesters.index.tsx    → /admin/semesters (list)
admin.semesters.$id.tsx      → /admin/semesters/:id
teacher.classes.$classSectionId.students.tsx → /teacher/classes/:id/students
articles.tsx                 → /articles (layout)
articles.index.tsx           → /articles (list)
articles.$slug.tsx           → /articles/:slug
```

**Route file structure:**

```typescript
// routes/admin.students.tsx
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/admin/students")({
  component: AdminStudentsPage,
});

function AdminStudentsPage() {
  // inline or import from features/
}
```

**Protected route (role layout):**

```typescript
// routes/admin.tsx
export const Route = createFileRoute('/admin')({
  component: () => <ProtectedOutlet role="ADMIN" />
})
```

**Navigation:**

```typescript
// Link-based
<Link to="/admin/students">Students</Link>

// Programmatic
const navigate = useNavigate()
navigate({ to: '/admin/dashboard' })

// ❌ NEVER
window.location.href = '/admin/students'
<a href="/admin/students">...</a>
```

---

## 5. API Layer Convention

### Architecture

```
Component → API module (src/lib/api/{domain}.ts)
                    ↓
              apiRequest() in client.ts
                    ↓
               fetch() + JWT header
```

### API Client (`src/lib/api/client.ts`)

**Exports used by API modules (NOT by components directly):**

- `apiRequest<T>(path, init?)` — main HTTP wrapper
- `downloadApiFile(path, filename)` — file download with auth
- `jsonBody(value)` — JSON.stringify helper

**Exports used by auth system only:**

- `getStoredAuth()` / `setStoredAuth()` — localStorage with key `tlu-auth`
- `apiUrl(path)` — constructs full URL

**Auto-handled:**

- `Authorization: Bearer {token}` header
- 401 → auto-refresh via `POST /api/auth/refresh`
- Error parsing from backend `{ message, status }` format

### API Module Pattern

```typescript
// src/lib/api/student.ts
import { apiRequest, jsonBody } from "./client";
import type { StudentGradesSummaryResponse } from "./types";

export const studentApi = {
  listSemesters: () => apiRequest<StudentSemesterResponse[]>("/api/student/semesters"),

  getGrades: (semesterId?: number) =>
    apiRequest<StudentGradesSummaryResponse>(
      `/api/student/grades${semesterId ? `?semesterId=${semesterId}` : ""}`,
    ),

  enrollClass: (classSectionId: number) =>
    apiRequest<EnrollmentRequestResponse>(`/api/student/enroll/${classSectionId}`, {
      method: "POST",
    }),

  createVNPayUrl: (semesterId: number) =>
    apiRequest<string>(`/api/student/tuition/${semesterId}/vnpay-url`, {
      method: "POST",
      body: jsonBody({}),
    }),
};
```

### Existing API modules

| File               | Domain                                                                                                                                                       |
| ------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `auth.ts`          | login, getMe, logout                                                                                                                                         |
| `admin.ts`         | users, students, teachers, majors, rooms, periods, courses, semesters, class-sections, enrollments, exam-schedules, departments, homerooms, academic-results |
| `student.ts`       | semesters, grades, schedule, exams, enrollment, retakes, tuition, curriculum                                                                                 |
| `teacher.ts`       | semesters, classes, grades, attendance                                                                                                                       |
| `chat.ts`          | chat rooms, messages (WebSocket + REST)                                                                                                                      |
| `chatbot.ts`       | AI chatbot                                                                                                                                                   |
| `articles.ts`      | public news/articles                                                                                                                                         |
| `notifications.ts` | student notifications                                                                                                                                        |
| `knowledge.ts`     | admin knowledge base                                                                                                                                         |

### Do ✅

```typescript
import { studentApi } from "@/lib/api/student";
import { adminApi } from "@/lib/api/admin";
import type { StudentGradesSummaryResponse } from "@/lib/api/types";

const grades = await studentApi.getGrades(semesterId);
```

### Don't ❌

```typescript
// ❌ Direct fetch
const res = await fetch('/api/student/grades').then(r => r.json())

// ❌ Hardcoded base URL
const res = await fetch('http://localhost:8080/api/student/grades')

// ❌ Direct localStorage access in component
const token = localStorage.getItem('tlu-auth')

// ❌ No type on apiRequest
await apiRequest('/api/admin/users') // missing <AdminUserResponse[]>

// ❌ Mock data in new code
const data = mockStudents.filter(...)
```

---

## 6. Schema and Type Convention

**All API response/request types → `src/lib/api/types.ts`**

No Zod schemas for API types. Types are TypeScript interfaces:

```typescript
// src/lib/api/types.ts — add here, nowhere else
export interface NewEntityResponse {
  id: number;
  name: string;
}
```

**Feature-local types** (`features/{name}/types.ts`) extend or re-shape API types for UI:

```typescript
// features/admin-class-sections/types.ts
import type { AdminClassSectionStatus, ClassSectionResponse } from "@/lib/api/types";

export interface ClassSectionRow {
  // UI display shape
  id: string; // converted from number
  classCode: string;
  status: AdminClassSectionStatus;
  source: "API"; // marks data provenance
}
```

**Mappers** (`features/{name}/*Mappers.ts`) convert API → UI types:

```typescript
export function mapApiClassSection(section: ClassSectionResponse): ClassSectionRow {
  return { id: String(section.id), classCode: section.classCode, ... }
}
```

**Zod** — used in validation files for business rules (not API parsing):

```typescript
// features/{name}/validation.ts — pure functions, no Zod schemas needed
export function validateClassSectionPlan(values): string | null { ... }
```

**Rules:**

- ❌ No `any` — TypeScript strict mode enforced (`noImplicitAny`)
- ❌ No non-null assertion `!` without guard
- ❌ No duplicate types — import from `@/lib/api/types`
- ✅ Use `import type` for type-only imports
- ✅ Optional fields: `field?: Type | null`

---

## 7. Auth Convention

### Storage

```typescript
// Key: 'tlu-auth' in localStorage
interface StoredAuth {
  accessToken: string;
  refreshToken: string;
  role: "ADMIN" | "STUDENT" | "TEACHER";
  name?: string | null;
}
```

### Flow

```
Login → POST /api/auth/login → { accessToken, refreshToken, role }
      → getMe() → GET /api/users/me → UserProfile
      → stored in localStorage + AuthContext

Every API call → apiRequest adds Authorization: Bearer header
401 → auto POST /api/auth/refresh → retry original request
Logout → POST /api/auth/logout → clear localStorage
```

### Usage in components

```typescript
// ✅ Always use the hook
import { useAuth } from "@/lib/auth";

const { role, name, profile, login, logout } = useAuth();

// ❌ Never access localStorage directly in components
```

### Protected routes

```typescript
// routes/admin.tsx — role must match EXACTLY
export const Route = createFileRoute('/admin')({
  component: () => <ProtectedOutlet role="ADMIN" />
})
```

`ProtectedOutlet` (`src/components/layout/ProtectedOutlet.tsx`):

- Not logged in → redirect to `/login`
- Wrong role → redirect to their dashboard
- Renders `<AppLayout>` with `<Outlet />`

---

## 8. UI and Component Convention

### Hierarchy

```
Route file → Feature component (from features/) → UI primitives
```

**Size limits:**

| Type              | Max Lines |
| ----------------- | --------- |
| Route page        | 150       |
| Feature component | 300       |
| UI component      | 200       |
| Custom hook       | 150       |

### Reusable components (always check before creating new)

| Component          | Location                                | When to use                     |
| ------------------ | --------------------------------------- | ------------------------------- |
| `AppLayout`        | `components/layout/AppLayout.tsx`       | Every authenticated page        |
| `ProtectedOutlet`  | `components/layout/ProtectedOutlet.tsx` | Role route group                |
| `DataTable`        | `components/data-table/DataTable.tsx`   | Any list with search/pagination |
| `PageHeader`       | `components/ui/page-header.tsx`         | Top of every page               |
| `StatCard`         | `components/ui/page-header.tsx`         | Dashboard metric cards          |
| `StatusBadge`      | `components/ui/status-badge.tsx`        | Status columns                  |
| `EntityFormDialog` | `components/forms/EntityFormDialog.tsx` | Add/Edit CRUD modals            |
| `ConfirmDialog`    | `components/ui/confirm-dialog.tsx`      | Delete confirmations            |
| `shadcn/ui` (44+)  | `components/ui/`                        | All primitive UI                |

### Naming

```
Components: PascalCase.tsx  → DataTable.tsx, AdminStudentsPage
Hooks: useXxx.ts            → useTeacherSemester.ts, useMobile.tsx
API modules: {domain}.ts    → student.ts, admin.ts (no -api suffix)
Mappers: {domain}Mappers.ts → classSectionMappers.ts, teacherMappers.ts
Route files: {role}.{feature}[.$param].tsx
```

### Styling

```typescript
// ✅ TailwindCSS utility classes
<div className="space-y-4 rounded-lg border bg-card p-5 shadow-sm">
  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">

// ✅ Semantic colors
className="bg-card text-foreground text-muted-foreground text-destructive"

// ❌ No inline styles
style={{ padding: '20px' }}

// ❌ No custom CSS (except Tailwind directives in styles.css)

// ❌ No arbitrary Tailwind values for standard spacing
className="p-[20px]"  // use p-5
```

---

## 9. Admin Module Convention

**Admin layout:** `routes/admin.tsx` → `<ProtectedOutlet role="ADMIN" />` → `AppLayout`

**Sidebar nav** is defined inside `AppLayout.tsx` with `adminNavGroups` — role-based.

**Admin modules currently implemented:**

- `admin.users.tsx` — user management
- `admin.students.tsx` — student management
- `admin.teachers.tsx` — teacher management
- `admin.majors.tsx` — majors
- `admin.rooms.tsx` — rooms
- `admin.periods.tsx` — periods
- `admin.courses.tsx` — courses
- `admin.departments.tsx` — departments
- `admin.homerooms.tsx` — homeroom classes
- `admin.semesters.index.tsx` — semester list
- `admin.semesters.$id.tsx` — semester detail (tabs via `features/semester-hub/`)
- `admin.enrollments.tsx` — enrollments
- `admin.exam-schedules.tsx` — exam schedules
- `admin.academic-results.tsx` — academic results
- `admin.knowledge.tsx` — knowledge base
- `admin.class-sections.tsx` → delegates to `features/admin-class-sections/`

**When adding a new admin module:**

1. Copy pattern from `admin.majors.tsx` (simple CRUD) or `admin.students.tsx` (complex)
2. Add API functions to `src/lib/api/admin.ts`
3. Add types to `src/lib/api/types.ts`
4. If complex: create `features/admin-{name}/` with components, types, mappers
5. Add nav item to `AppLayout.tsx` adminNavGroups
6. Route name: `admin.{feature}.tsx`

---

## 10. Data Fetching Convention

**useQuery — read data:**

```typescript
const { data, isPending, isError, error } = useQuery({
  queryKey: ['student', 'grades', semesterId],  // hierarchical, include ALL deps
  queryFn: () => studentApi.getGrades(semesterId),
  enabled: semesterId != null,                   // guard undefined params
  staleTime: 5 * 60 * 1000                       // 5 min for grade data
})

if (isPending) return <Skeleton />         // REQUIRED
if (isError) return <Alert>{error.message}</Alert>  // REQUIRED
if (!data?.length) return <EmptyState />   // REQUIRED
return <DataTable data={data} />
```

**useMutation — write data:**

```typescript
const queryClient = useQueryClient();
const { mutate, isPending } = useMutation({
  mutationFn: (payload: FormData) => adminApi.createStudent(payload),
  onSuccess: () => {
    queryClient.invalidateQueries({ queryKey: ["admin", "students"] }); // REQUIRED
    toast.success("Đã tạo sinh viên");
  },
  onError: (error) => {
    toast.error(error.message); // REQUIRED
  },
});
```

**Query key hierarchy:**

```typescript
["student", "grades"][("student", "grades", semesterId)][("admin", "users", page, pageSize)][ // list // filtered // paginated
  ("teacher", "classes", classId)
]; // specific
```

**Stale times:**

| Data type                       | staleTime             |
| ------------------------------- | --------------------- |
| Static (rooms, periods, majors) | `60 * 60 * 1000` (1h) |
| User/grade data                 | `5 * 60 * 1000` (5m)  |
| Dashboard                       | `60 * 1000` (1m)      |

**Custom hooks in features:**

```typescript
// features/teacher/useTeacherSemester.ts
export function useTeacherSemester() {
  const semestersQuery = useQuery({
    queryKey: ["teacher", "semesters"],
    queryFn: teacherApi.listSemesters,
  });
  // local UI state derived from query
  return { semesterId, setSemesterId, semesterOptions, semestersQuery };
}
```

---

## 11. Files That Should NOT Be Edited Manually

| File                                        | Reason                                           |
| ------------------------------------------- | ------------------------------------------------ |
| `src/routeTree.gen.ts`                      | Auto-generated by TanStack Router on dev/build   |
| `package-lock.json`                         | Lock file — use npm commands                     |
| `vite.config.ts`                            | Build config — only change if explicitly asked   |
| `tsconfig.json`                             | TypeScript config — do not relax strict settings |
| `src/styles.css`                            | Only Tailwind directives — no component styles   |
| `src/lib/api/client.ts`                     | Core HTTP/auth logic — only touch for auth bugs  |
| `src/components/layout/AppLayout.tsx`       | Core layout — only add nav items, nothing else   |
| `src/components/layout/ProtectedOutlet.tsx` | Auth guard — do not change unless fixing auth    |

---

## 12. Rules for Future AI Agents

```txt
✅ DO:
- Read feature similar to yours before writing any code
- Add API types to src/lib/api/types.ts
- Add API functions to correct src/lib/api/{domain}.ts
- Use existing components: DataTable, PageHeader, StatCard, EntityFormDialog, ConfirmDialog
- Wrap all data in useQuery / useMutation
- Handle isPending, isError, empty states in every query
- Invalidate queries after mutations
- Use import type for type-only imports
- Add route file as {role}.{feature}.tsx in src/routes/
- Use Link and useNavigate — never window.location
- Show toast on success and error
- Run npm run lint && npm run build after changes

❌ DON'T:
- Use fetch() or axios directly — always apiRequest()
- Access localStorage directly in components
- Use any type — TypeScript strict mode
- Add mock data to src/data/mock.ts
- Hardcode API base URLs
- Duplicate types that already exist in lib/api/types.ts
- Create components > 300 lines without splitting
- Edit routeTree.gen.ts manually
- Add new folders outside src/routes/, src/features/, src/components/, src/lib/, src/hooks/
- Use Redux, Zustand, or Context for server state
- Skip error/loading/empty state handling
- Use window.location for navigation
- Modify AppLayout or ProtectedOutlet unless the task explicitly requires it
- Hardcode field names that backend hasn't confirmed
- Sửa nhiều module không liên quan đến task
```

---

## 13. Task Execution Checklist

```txt
[ ] Đọc feature tương tự trong codebase trước
[ ] Kiểm tra COMPONENT_MAP.md — dùng component có sẵn
[ ] Kiểm tra API_GUIDE.md — endpoint đã có chưa
[ ] Thêm type mới vào src/lib/api/types.ts
[ ] Thêm API function vào đúng src/lib/api/{domain}.ts
[ ] Đặt route file đúng: src/routes/{role}.{feature}.tsx
[ ] Feature phức tạp → tách vào src/features/{name}/
[ ] Dùng useQuery + useMutation + invalidateQueries
[ ] Handle isPending / isError / empty state
[ ] Toast success + toast error trên mutation
[ ] Không dùng any
[ ] Không mock data
[ ] Không hardcode field backend chưa có
[ ] Không sửa ngoài phạm vi task
[ ] npm run lint — không có lỗi ESLint
[ ] npm run build — không có lỗi TypeScript
```

---

## 14. Examples from This Project

### ✅ Add a new API function (correct)

```typescript
// src/lib/api/admin.ts — extend existing object
export const adminApi = {
  // existing...
  listDepartments: () => apiRequest<DepartmentResponse[]>("/api/admin/departments"),

  createDepartment: (req: DepartmentRequest) =>
    apiRequest<DepartmentResponse>("/api/admin/departments", {
      method: "POST",
      body: jsonBody(req),
    }),
};
```

### ✅ Add a new admin page (correct pattern)

```typescript
// src/routes/admin.departments.tsx
import { createFileRoute } from '@tanstack/react-router'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { PageHeader } from '@/components/ui/page-header'
import { DataTable } from '@/components/data-table/DataTable'
import { Button } from '@/components/ui/button'
import { EntityFormDialog } from '@/components/forms/EntityFormDialog'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { adminApi } from '@/lib/api/admin'
import type { DepartmentResponse } from '@/lib/api/types'
import { Plus, Trash2 } from 'lucide-react'
import { toast } from 'sonner'

export const Route = createFileRoute('/admin/departments')({
  component: AdminDepartmentsPage
})

function AdminDepartmentsPage() {
  const { data, isPending, isError, error } = useQuery({
    queryKey: ['admin', 'departments'],
    queryFn: adminApi.listDepartments,
    staleTime: 60 * 60 * 1000
  })

  if (isPending) return <div className="space-y-4"><Skeleton /></div>
  if (isError) return <Alert variant="destructive">{error.message}</Alert>
  if (!data?.length) return <div className="py-16 text-center text-muted-foreground">No departments</div>

  return <DepartmentsContent data={data} />
}

function DepartmentsContent({ data }: { data: DepartmentResponse[] }) {
  const [open, setOpen] = useState(false)
  const [toDelete, setToDelete] = useState<DepartmentResponse | null>(null)
  const queryClient = useQueryClient()

  const { mutate: deleteDept } = useMutation({
    mutationFn: (id: number) => adminApi.deleteDepartment(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'departments'] })
      toast.success('Đã xóa khoa')
    },
    onError: (e) => toast.error(e.message)
  })

  return (
    <div>
      <PageHeader title="Khoa" actions={<Button onClick={() => setOpen(true)}><Plus className="h-4 w-4" />Thêm</Button>} />
      <DataTable
        data={data}
        columns={[
          { key: 'departmentCode', header: 'Mã khoa', searchable: true },
          { key: 'name', header: 'Tên khoa', searchable: true },
          { key: 'actions', header: '', render: (d) => (
            <Button variant="ghost" size="icon" onClick={() => setToDelete(d)}>
              <Trash2 className="h-4 w-4" />
            </Button>
          )}
        ]}
        rowKey={(d) => String(d.id)}
      />
      <EntityFormDialog open={open} onOpenChange={setOpen} title="Thêm khoa" onSubmit={() => {}}>
        {/* form fields */}
      </EntityFormDialog>
      <ConfirmDialog
        open={!!toDelete}
        onOpenChange={(v) => !v && setToDelete(null)}
        title="Xóa khoa?"
        description={`Xóa ${toDelete?.name}?`}
        destructive
        confirmText="Xóa"
        onConfirm={() => deleteDept(toDelete!.id)}
      />
    </div>
  )
}
```

### ✅ Custom hook in feature (correct pattern)

```typescript
// features/teacher/useTeacherSemester.ts
import { useQuery } from "@tanstack/react-query";
import { teacherApi } from "@/lib/api/teacher";

export function useTeacherSemester() {
  const semestersQuery = useQuery({
    queryKey: ["teacher", "semesters"],
    queryFn: teacherApi.listSemesters,
    retry: false,
  });
  // derives local state from query
  return { semesterId, setSemesterId, semesterOptions, semestersQuery };
}
```

### ❌ Wrong patterns to avoid

```typescript
// ❌ Direct fetch
const res = await fetch(`http://localhost:8080/api/admin/users`)

// ❌ any type
const handleData = (data: any) => { ... }

// ❌ Skip loading state
const { data } = useQuery({ queryFn: adminApi.listUsers })
return <DataTable data={data} />  // crashes if loading/error

// ❌ No cache invalidation
useMutation({
  mutationFn: adminApi.createUser,
  onSuccess: () => toast.success('Done')  // list stays stale!
})

// ❌ Mock in production code
const students = mockStudents.filter(s => s.majorId === 1)

// ❌ Duplicate type
// Don't define AdminStudent again if AdminStudentResponse already exists in types.ts

// ❌ Edit generated file
// src/routeTree.gen.ts — never touch manually
```

---

## 15. Dev Commands

```bash
npm install          # install dependencies
npm run dev          # start dev server → http://localhost:5173
npm run build        # production build
npm run build:dev    # dev build
npm run lint         # ESLint + TypeScript check
npm run format       # Prettier format

# Backend must run on http://localhost:8080 (or set VITE_API_BASE_URL)
```

**Environment:**

```bash
# .env.local
VITE_API_BASE_URL=http://localhost:8080
```

---

_Last updated: 2026-06-03 based on actual source code analysis._
