# University Management System (UMS)

A comprehensive web-based platform for managing university operations — student information, course registration, scheduling, grading, tuition payments, and internal communication.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![Java](https://img.shields.io/badge/Java-17-orange)
![React](https://img.shields.io/badge/React%20%2F%20TanStack-18-blue)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-black)

---

## 📁 Repository Structure

```
universityweb/
├── docker-compose.yml   # Infrastructure (PostgreSQL, Redis, Kafka, PgAdmin, Kafka UI)
├── backend/             # Spring Boot API (Java / Gradle)
└── frontend/            # React + TanStack Start (Bun)
```

---

## 🛠 Tech Stack

### Backend (`backend/`)
| | |
|---|---|
| Framework | Spring Boot 3.x |
| Language | Java 17 |
| Build Tool | Gradle (Kotlin DSL) |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA (Hibernate) |
| Security | Spring Security + JWT |
| Caching | Redis |
| Messaging | Apache Kafka |
| Real-time | WebSocket (STOMP) |

### Frontend (`frontend/`)
| | |
|---|---|
| Framework | TanStack Start (React 18 + Vite) |
| Language | TypeScript |
| Package Manager | Bun |
| UI Components | Radix UI + shadcn/ui |
| Styling | Tailwind CSS |

### Infrastructure
- Docker Compose — PostgreSQL, Redis, Kafka, Zookeeper, PgAdmin, Kafka UI

---

## 🌟 Key Features

**Student Portal** — Profile, course registration (Kafka queue), schedule, grades, GPA, tuition, internal chat

**Lecturer Portal** — Teaching schedule, class management, grading, student chat

**Admin Portal** — User management, curriculum (courses/semesters/classes), system config

---

## ⚙️ Prerequisites

- **Docker Desktop** — for infrastructure services
- **Java JDK 17+** — for backend
- **Bun** — for frontend (`curl -fsSL https://bun.sh/install | bash`)

---

## 🚀 Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

Wait ~30 seconds for all services to initialize.

### 2. Backend

```bash
cd backend
cp .env.example .env   # fill in values
./gradlew bootRun
```

API running at: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Frontend

```bash
cd frontend
cp .env.example .env   # fill in values
bun install
bun run dev
```

App running at: `http://localhost:5173`

---

## 🔌 Service Ports

| Service | Port | Notes |
|---|---|---|
| Frontend | `5173` | React / TanStack app |
| Backend | `8080` | Spring Boot API |
| PostgreSQL | `5432` | Main database |
| Redis | `6379` | Cache & sessions |
| Kafka | `9092` | Message broker |
| Kafka UI | `8090` | Kafka dashboard |
| PgAdmin | `5050` | Database GUI |

---

## 📖 Documentation

See [`backend/docs/`](backend/docs/) for:
- [`PROJECT.md`](backend/docs/PROJECT.md) — Detailed project overview & ERD
- [`SWAGGER_SETUP_GUIDE.md`](backend/docs/SWAGGER_SETUP_GUIDE.md) — API docs setup
- [`CHAT_SYSTEM_README.md`](backend/docs/CHAT_SYSTEM_README.md) — Chat system architecture

---

## 📦 Database Schema

Initial schema: [`backend/sql/schema.sql`](backend/sql/schema.sql)

---

## Environment Variables

Copy `.env.example` to `.env` in each sub-folder and fill in the values. **Never commit `.env` files.**

---

## 💳 Hướng dẫn Test Thanh toán VNPay

Sử dụng thông tin thẻ test dưới đây tại giao diện thanh toán của ứng dụng.

### Chọn ngân hàng:

- Click vào logo ngân hàng **NCB**.

### Điền thông tin thẻ:

- **Số thẻ**: `9704198526191432198`
- **Tên chủ thẻ**: `NGUYEN VAN A`
- **Ngày phát hành**: `07/15`
- **Mã OTP**: `123456`

---

