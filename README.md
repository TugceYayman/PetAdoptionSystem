PET ADOPTION SYSTEM - README
================================

📌 PROJECT OVERVIEW
---------------------------------
The Pet Adoption System is a web application that allows users to browse available pets, submit adoption requests, and manage approvals. 
It includes secure authentication using JWT, RESTful API design, and role-based access control (User/Admin).

⚙️ SETUP INSTRUCTIONS
---------------------------------

Clone the Repository:
1. Open your terminal or command prompt.
2. Run the following command to clone the repository:
   git clone https://github.com/TugceYayman/PetAdoptionSystem.git
3. Navigate into the project directory:
   cd PetAdoptionSystem

Database Setup:
1. Open MySQL and create a new schema called `petadoptiondb`:
   CREATE DATABASE petadoptiondb;
2. Once the application is started, the database will be automatically populated with:
   - An **admin account** for management.
   - A set of **preloaded pets** available for adoption.

Running the Application:
1. Open the project in an IDE like IntelliJ IDEA or VS Code.
2. Ensure you have **Java 17+** and **Maven** installed on your machine.
3. Run the backend using:
   mvn spring-boot:run
4. The application will start on:
   http://localhost:8081

Admin Credentials (Preloaded):
---------------------------------
- **Email:** admin@petadoption.com
- **Password:** admin123

Next Steps:
---------------------------------
- Use **Postman** or a web browser to test API endpoints.
- Log in with the admin account to manage pet adoptions.
- Register as a user and explore the adoption process.



