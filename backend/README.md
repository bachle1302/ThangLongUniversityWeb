# University Management System (UMS)

A comprehensive web-based platform designed to manage university operations, including student information, course registration, scheduling, grading, tuition payments, and internal communication.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![React](https://img.shields.io/badge/React-18-blue)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-Xi)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-black)

## 📖 Table of Contents
- [Architecture & Database](#-architecture--database)
- [Tech Stack](#-tech-stack)
- [Key Features](#-key-features)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Project Structure](#-project-structure)

---

## 🏗 Architecture & Database

The system is built using a Microservices-ready architecture pattern with **Spring Boot** for the backend and **React** for the frontend. It leverages **Docker** for containerization of infrastructure services.

### Database Design (ERD)
The database is designed to handle complex relationships between Students, Teachers, Courses, and Real-time messaging.

> **[👉 Click here to view and edit the ERD on Mermaid Live Editor](https://mermaid.live/edit#pako:eNq1V11r40YU_SuDYEsCtrEdr-P1y6Iq2liNv2rJoZSAmEgTa3YljdFHumkSaFkoFLqwS-lTH7olT1soLU2fYkofHPo__E96R5ZsWVK22Zb6wZZm7p0799xzz4zPBYOZRGgLxNujeOJh58hF8HnwAJXLZdTvzL_tobEqj9AHSBxrHT66tOCDKjpfvvCPzdwJoiYaHqzH_MCjMBr6xHOxQ3ITU-z7nzHP1C3sW7lZ4mBqr0eJGzrIYzZBR4K411P6JaRq4z25r5WQJotSRx4dCWvzYwam2EXU17ER0NM4-uWRu3yIff8xh2iMZ6DDxJMDCF4r19DpYvY9XYKQDhrv3A9Ck7iBzsEFh978CqmHBXYnoW3rm8iYOCDIZMc5W2yaHvH93LiDnzIPgvQn8zeuhazF7KWRDkXdAGEDm8Shhn5GMLc9sObXODFaIRKD-L8gEhBsWOCRQmT_nogkXLGYmx81ycQjfEXNWtz8aCD_9i2wgS5u_nT5c6VSyaW5we3bV_MvBkhb3LwZoC1REvfkniJtr2muyj1Z1e5Pdb512M3hXht1DmqoXq0_LMNXM51qVGI_wF6g88fMBHHNzHCKyR6ZUIiEA8pcnU2JC7FuX2FOg8XsB3T7-vYFPD-b__E4l7Y0GI9U-b55pOrkzH9320jRqtVaQcHihLX5T25s-RE-xUhiHsmy0PCISQMfrNXF7DUK5j-7yAC-fl2wrEl8w6NTnmc2j66oqroqS5oy6N87HRuUJk0-G9g6RZ1hnFilv5FbtJLBQs8nS45npnziED9IGiAzmVA9MxfvxGPMgT0Mrflv8Jbr1kRAYAUztBMqafWtWnlnG5j9cKtRbm5nkXXwc9232RLb27fIj_CFLwqUwLk6hJ7H1SnxgB64SlHnDhE1bOYTM1MMuT8adLu9e-toIoxFuC1r5BMjInfG4sRmGPKkpg6wO7pvAL-irdPF7IWDJvDzC0bPFrPr9PaXXifUxXbOxQgjfIpdAhakXLTF7DuODahKkLaNTiRo5CDkMI7kfYVLhbxXQpLYl-QufxoCW_nvE1GB93eLUWcx-0ZCw878Jdp6ovT5Gikp0sYKp7z-odLt_ne076KwSQzqYDtGADssBMasIAgAuy9d0OLFzRXn1vzanWxIW-w9xVCplXNMsKxxmlzMmdokIGZi_TxihPXXr_hxVjoDCooDWgKPECN4F6BSR9TQFii4Ku4r_f0UmNKgfwiqLr6PiKSlzoUTFDhk4TwhgrMpNxuOlENRk0tofzQYD0uxbkUv75fRUBxpiqQMxaImM5h7SjwfJz0zPCjla52c1dnJVeynjLpFoZfIyfdjW3YrBZRzzTt1EbwD4Goxlpr8CVzylB7sBTKARvpXnPh4LPZ5j32FtkZyd1n6jjJUt7O32ouLcpldrG-IbdgC3FDR1GMn1F6daxvGq8tTgXHmehV5nGcPMu63PDr8JEByZHOHi0IHk4C4rR3Wt5W7XSzmB_56V6sk412lFZ2bL28cUNfVpjaXvMONlxNT9x2BNuWM-xxT274jlUL7KfEoM9cRUvU43-wabs05nkI23f4xWDkXXkaHOMfp7HOOEGvVJunMkQW4Me-sgC0ZD94XEEAoCROPmkI78EJSEhw45zB_FaLmA3JYINlHQlx2HNqR9FyC2xS7n8K1IvH0WDixhPYJtn14C6e8QeJ_d6tRL-pFiQu00K43Gs1oFaF9LjwX2uXmo1alVtutNnYeterVZr0knMFobadVabZ2q82dRqvZrDV2m5cl4fMocK3SalUf7dZbjVpzZ_dhs9q6_Bv0P3Is)**

---

## 🛠 Tech Stack

### Backend
* **Framework:** Spring Boot 3.x
* **Language:** Java / Kotlin
* **Build Tool:** Gradle (Kotlin DSL)
* **Database:** PostgreSQL 15
* **ORM:** Spring Data JPA (Hibernate)
* **Security:** Spring Security + JWT
* **Caching:** Redis
* **Messaging (Async):** Apache Kafka (for Course Registration & Chat History)
* **Real-time:** WebSocket (STOMP)

### Frontend
* **Framework:** React 18 (Vite)
* **UI Library:** Ant Design
* **State Management:** React Hooks / Context API
* **HTTP Client:** Axios

### Infrastructure
* **Docker & Docker Compose**

---

## 🌟 Key Features

### 👨‍🎓 Student Portal
* View personal profile and academic history.
* **Course Registration:** High-concurrency handling using Kafka Queue.
* View schedules (Timetable) and Exam dates.
* Check Grades and GPA.
* **Tuition:** View bills and payment history.
* **Chat:** Internal messenger (MS Teams style) to chat with classmates and lecturers.

### 👨‍🏫 Lecturer Portal
* View teaching schedules and exam proctoring schedules.
* Manage class lists.
* **Grading:** Input and update student scores.
* Communication with students via internal chat.

### 👨‍💻 Admin Portal
* Manage Users (CRUD).
* Manage Curriculum (Courses, Semesters, Classes).
* System Configuration.

---

## ⚙️ Prerequisites

Before running the project, ensure you have the following installed:
* **Docker Desktop** (Essential for DB, Redis, Kafka)
* **Java JDK 17+**
* **Node.js 18+**

---

## 🚀 Installation & Setup

### 1. Start Infrastructure (Docker)
We use Docker Compose to run PostgreSQL, Redis, Kafka, and Zookeeper.

```bash
docker-compose up -d

```

*Wait a few minutes for all services to initialize.*

### 2. Backend Setup (Spring Boot)

Navigate to the backend directory:

```bash
cd backend
./gradlew bootRun

```

* Server runs at: `http://localhost:8080`
* Swagger API Docs: `http://localhost:8080/swagger-ui.html` (if configured)

### 3. Frontend Setup (React)

Navigate to the frontend directory:

```bash
cd frontend-uni
npm install
npm run dev

```

* App runs at: `http://localhost:5173`

---

## 📂 Project Structure

```bash
university-management-system/
├── docker-compose.yml       # Infrastructure Config (DB, Redis, Kafka)
├── backend/                 # Spring Boot Source Code
│   ├── src/main/java/       # Controller, Service, Repository, Entity
│   ├── src/main/resources/  # application.yml
│   └── build.gradle.kts     # Backend Dependencies
└── frontend-uni/            # React Source Code
    ├── src/                 # Components, Pages, Hooks
    ├── package.json         # Frontend Dependencies
    └── vite.config.js       # Vite Config

```

---

## 🔌 Service Ports

| Service | Port | Description |
| --- | --- | --- |
| **Frontend** | `5173` | React App |
| **Backend** | `8080` | Spring Boot API |
| **PostgreSQL** | `5432` | Main Database |
| **Redis** | `6379` | Cache & Session |
| **Kafka** | `9092` | Message Broker |
| **Kafka UI** | `8090` | Kafka Dashboard (Optional) |
| **PgAdmin** | `5050` | Database GUI (Optional) |

