# 🏨 AirBnb Backend Clone

This is a robust backend clone of the Airbnb platform, built using Java Spring Boot. It includes complete hotel and room management, role-based authentication, dynamic pricing strategies, booking functionality, and RESTful APIs. The system supports both admin and user roles with a strong focus on modularity, scalability, and clean architecture.

## 🚀 Tech Stack

- **Backend**: Java, Spring Boot, Spring MVC
- **Security**: Spring Security, OAuth2, JWT, Session Management
- **Database**: PostgreSQL
- **ORM**: Hibernate
- **API Documentation**: Swagger
- **Dev Tools**: Spring DevTools, CI/PI (Continuous Integration / Project Integration)

---

## 📌 Features

### 🔐 Authentication & Authorization
- Role-based access: `USER` and `ADMIN`
- Implemented with **JWT**, **OAuth2**, and **Session**

### 👤 User Functionality
- User registration and login
- User profile management
- Search hotels by **city**, **check-in/check-out dates**, **guest count**
- View all hotels with **minimum price** listed
- Add guests before booking
- Initiate booking and receive **Booking ID** upon confirmation
- Make **secure payments** for bookings
-  User can cancel the and booking and get refund 

### 🛠️ Admin Functionality
- Create hotels and rooms
- Activate/deactivate hotels
- View all bookings and future inventories
- Dynamic pricing strategies for rooms
- Manual and automatic pricing updates
- Full inventory view up to **1 year in the future**

---

## 🧪 REST API Endpoints

- Built using **RESTful architecture**
- Integrated with **Swagger** for easy testing and documentation

---

## ⚙️ Installation

> Prerequisite: Java 17+, PostgreSQL

```bash
# Clone the repo
git clone https://github.com/your-username/airbnb-backend-clone.git

# Open the project in IntelliJ or VS Code with Java support

# Configure PostgreSQL credentials in application.yml or application.properties

# Run the Spring Boot application
./mvnw spring-boot:run
