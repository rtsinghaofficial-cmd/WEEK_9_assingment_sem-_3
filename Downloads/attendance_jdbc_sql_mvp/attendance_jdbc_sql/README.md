# AttendEase — JDBC + MySQL Attendance MVP

A Java console application using **JDBC** to connect to a **MySQL SQL database**.

## Features
- Add students with unique register numbers
- List registered students
- Mark or update Present/Absent attendance for a specific date
- View an individual student's attendance history
- Calculate attendance percentage using SQL aggregation
- View all students' attendance for a chosen date
- Delete a student and their attendance history
- PreparedStatement used for user inputs
- SQL schema and optional sample student included

## Requirements
- JDK 17+
- MySQL Server 8+
- Maven 3.8+

## 1. Create the database
Open MySQL Workbench or the MySQL command line and execute `sql/schema.sql`.
This creates `attendance_db`, `students`, and `attendance`, plus one optional sample student.

Command-line alternative (run from the project folder):
```bash
mysql -u root -p < sql/schema.sql
```

## 2. Configure database credentials

The default connection settings are:
- URL: `jdbc:mysql://localhost:3306/attendance_db?useSSL=false&serverTimezone=UTC`
- User: `root`
- Password: empty

Recommended: set environment variables rather than editing source code.

PowerShell:
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/attendance_db?useSSL=false&serverTimezone=UTC"
$env:DB_USER="root"
$env:DB_PASSWORD="your_mysql_password"
```

If your local root account has no password, set `$env:DB_PASSWORD=""`.

## 3. Run the app

From the project directory:
```bash
mvn clean package
mvn exec:java -Dexec.mainClass="com.attendease.AttendanceApp"
```

If your Maven setup does not have the exec plugin configured, use:
```bash
mvn dependency:copy-dependencies
java -cp "target/classes;target/dependency/*" com.attendease.AttendanceApp
```
On macOS/Linux, replace the classpath semicolon with a colon:
```bash
java -cp "target/classes:target/dependency/*" com.attendease.AttendanceApp
```

## Attendance percentage formula
`percentage = (present records / total marked records) * 100`

Only explicitly marked Present or Absent dates count. A date with no attendance entry is treated as **Not marked**, not automatically Absent.

## Database design
- `students`: one row per student; `student_id` is unique.
- `attendance`: one row per student per date; unique constraint prevents duplicate daily records. Marking attendance again updates the existing status.
- Foreign key with cascade delete keeps the database consistent when a student is deleted.

## Project structure
```text
attendance_jdbc_sql/
├── pom.xml
├── README.md
├── sql/
│   └── schema.sql
└── src/main/java/com/attendease/
    ├── Database.java
    └── AttendanceApp.java
```

## Troubleshooting
- **Communications link failure**: make sure MySQL Server is running.
- **Access denied**: check `DB_USER` and `DB_PASSWORD`.
- **Unknown database**: execute `sql/schema.sql`.
- **mvn is not recognized**: install Maven or import `pom.xml` into IntelliJ IDEA / Eclipse as a Maven project.
