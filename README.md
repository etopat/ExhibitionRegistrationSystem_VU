# Exhibition Registration System – Victoria University

A Java-based **desktop registration management system** built for the **Victoria University SALSA Dance Festival**.  
This system automates participant registration, image handling, and table printing while connecting to a **Microsoft Access** database using **UCanAccess JDBC**.

---

## Key Features

✅ **Tabbed Pane Interface** – Separate tabs for registration and participant table.  
✅ **CRUD Support** – Register, Search, Update, Delete participants in Access DB.  
✅ **Image Upload** – Upload and rename images to match participant Registration ID.  
✅ **Real-Time Table View** – Auto-refreshes after any operation.  
✅ **Print Function** – Print participant list directly from JTable.  
✅ **Data Validation** – Prevents duplicates, invalid emails, and empty fields.  

---

## Technologies Used

| Component | Technology |
|------------|-------------|
| **Language** | Java (JDK 24) |
| **IDE** | Apache NetBeans 27 |
| **Database** | Microsoft Access (.accdb) |
| **Driver** | UCanAccess |
| **GUI** | Java Swing |
| **Print API** | JTable Print Method |

---

## Project Directory Structure

ExhibitionRegistration/
│

└── src/

└── main/

└── java/

└── vu/

└── ExhibitionRegn/

├── ExhibitionSystem.java # Main JFrame Form

├── VUE_Exhibition.accdb # Access Database

├── images/ # Uploaded Participant Images

├── DBConnection.java # UCanAccess Database Connection

├── README.md # Project Documentation


---

## Setup & Configuration

### 1️⃣ Prerequisites
Install:
- Java JDK 24+
- Apache NetBeans 27
- Microsoft Access
- UCanAccess Driver (ucanaccess.jar, jackcess.jar, commons-lang.jar, etc.)

### 2️⃣ Database Path Setup
In **DBConnection.java**, set your database connection as:

String url = "jdbc:ucanaccess://C:/Users/Dell/Documents/NetBeansProjects/ExhibitionRegistration/src/main/java/vu/ExhibitionRegn/VUE_Exhibition.accdb";

💡 Ensure that the slashes / are forward slashes in Java file paths.

### 3️⃣ Adding Libraries in NetBeans

Right-click the project → Properties

Go to Libraries

Click Add JAR/Folder

Select all UCanAccess driver files (ucanaccess.jar, jackcess.jar, etc.)

### 4️⃣ Run the Program

Use NetBeans Run Project ▶️ or from terminal:

java -jar ExhibitionRegistrationSystem.jar

GUI Overview
Tab	Description
Registration Form	Capture details, validate inputs, upload ID image
Participants Table	Displays all records with print and refresh options

### Sample Use Case

1️⃣ Open app
2️⃣ Fill in Registration details
3️⃣ Upload ID image (auto-renamed to Reg ID, e.g. REG001.jpg)
4️⃣ Click Register
5️⃣ View all records in Table Tab
6️⃣ Use Print to output table to printer

### Screenshots (Add Later)
Interface	Description

	Registration Form

	Participants Table

	Print Preview

(Store screenshots under /docs folder.)

### Input Validation
Field	Validation
Registration ID	Must be unique (e.g., REG001)
Name	Cannot be empty
Email	Checked with regex pattern
Contact	Numeric only
Image	Required before submission

### Printing Feature
Print the full participant list directly from the table:
boolean printed = tblParticipants.print();
if (printed) {
    JOptionPane.showMessageDialog(this, "Table printed successfully!");
}

### Future Enhancements

Export participant list to PDF

Add search filters & sorting

Add Login (Admin/Clerk roles)

Switch from Access to MySQL

### Author

Developer: Patrick Etomet
🎓 Victoria University – Faculty of Computing
📅 October 2025

### License
MIT License © 2025 Patrick Etomet
Permission is granted for educational and academic use only.

### GitHub Repository

🔗 https://github.com/etopat/ExhibitionRegistrationSystem_VU

💡 Tip: Keep your .accdb file and /images directory inside the same project folder to ensure portability.

