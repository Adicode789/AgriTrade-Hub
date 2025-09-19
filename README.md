🌱 AgritradeHub

AgritradeHub is a Spring Boot 3.5.4 based web application designed to facilitate agricultural trade, connecting farmers, buyers, and other stakeholders. It leverages modern web technologies with database integration, payment gateway support, and templating for a smooth user experience.

🚀 Features

Spring Boot Web – RESTful APIs and web layer support

Spring Data JPA – Database persistence with MySQL

Thymeleaf – Server-side HTML rendering

Spring Mail – Email notifications and communication

Razorpay Integration – Secure payment gateway

DevTools – Hot reload for faster development

Testing Support – Spring Boot Test for unit and integration tests

📦 Tech Stack

Java: 21

Spring Boot: 3.5.4

Database: MySQL

Template Engine: Thymeleaf

Build Tool: Maven

Payment Gateway: Razorpay

⚙️ Installation & Setup
Prerequisites

Java 21

Maven 3.9+

MySQL 8+

Git

Steps
# Clone the repository
git clone https://github.com/your-username/AgritradeHub.git
cd AgritradeHub

# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run


The application will start on:
👉 http://localhost:8080

🔑 Configuration

Update your application.properties (or application.yml) with:

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/agritradehub
spring.datasource.username=your-username
spring.datasource.password=your-password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Mail
spring.mail.host=smtp.example.com
spring.mail.username=your-email
spring.mail.password=your-password

# Razorpay
razorpay.key_id=your_key_id
razorpay.key_secret=your_key_secret

🧪 Running Tests
./mvnw test

📜 License

This project is licensed under the MIT License – feel free to use, modify, and distribute.

✨ Happy Coding & Farming! 🌾
