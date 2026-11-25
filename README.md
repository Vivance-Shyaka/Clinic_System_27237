# Clinic System

## Student Information

- **Name:** Vivance SHYAKA
- **Student ID:** 27237
- **Course:** INSY 8322 - Web Technology and Internet

## Project Overview

This project is a clinic management system with a Spring Boot backend and a React frontend.

The backend provides REST API endpoints for managing appointments, doctors, patients, offices, prescriptions, and specializations, as well as dashboard data. The current frontend displays an office management page where users can add, search, and delete offices through the backend API.

## Project Structure

- `Backend/` - Spring Boot application using Java, Spring Data JPA, and PostgreSQL.
- `Frontend/` - React application built with Vite and Axios for API requests.

## Running Locally

You need Java 24, Node.js with npm, and a PostgreSQL database.

1. Configure the backend database connection in your local `Backend/src/main/resources/application.properties` file. This file is intentionally excluded from Git because it may contain local credentials.
2. Start the backend:

   ```powershell
   cd Backend
   .\mvnw.cmd spring-boot:run
   ```

3. In a separate terminal, start the frontend:

   ```powershell
   cd Frontend
   npm install
   npm run dev
   ```

4. Open the local URL printed by Vite. The frontend currently sends office API requests to `http://localhost:8084`.
