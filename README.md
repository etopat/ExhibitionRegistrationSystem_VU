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
| **Programming Language** | Java (JDK 24) |
| **IDE** | Apache NetBeans 27 |
| **Database** | Microsoft Access (.accdb) |
| **Connectivity Driver** | UCanAccess JDBC |
| **GUI Toolkit** | Java Swing |
| **Build Tool** | Maven |
| **File Handling** | Java I/O & NIO (Files.copy) |

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

	Ensure that the slashes / are forward slashes in Java file paths.

### 🔹 JDBC Connection URL
In Java, connection URLs for UCanAccess take this format:

String url = "jdbc:ucanaccess://<absolute_path_to_database>";

For this project:
String url = "jdbc:ucanaccess://C:/Users/Dell/Documents/NetBeansProjects/ExhibitionRegistration/src/main/java/vu/ExhibitionRegn/VUE_Exhibition.accdb";


### 3️⃣ Adding Libraries in NetBeans

### Option 1: Manual Dependency Setup (Classic Method)

If you’re not using Maven, follow these steps:

Required Libraries:

ucanaccess.jar

jackcess.jar

commons-lang.jar

commons-logging.jar

hsqldb.jar

	All of these JARs must be in your project’s classpath for the connection to work.

#### Steps to Add in Apache NetBeans (JDK 24)

Right-click your Project > Properties

Go to Libraries > Compile Tab

Click Add JAR/Folder

Select all the above .jar files

Press OK and Clean & Build your project

	If correctly added, NetBeans will list them under Libraries in your project tree.

You can verify successful linkage by checking:

Database connected successfully!

in the NetBeans console when running.

### Option 2: Maven Dependency Management (Recommended)

If your project uses Maven, you don’t need to manually download or add .jar files. Maven automatically downloads all libraries and their transitive dependencies from the central repository.

#### Step 1: Convert Project to Maven

In NetBeans:

Right-click project → Convert to Maven Project

#### Step 2: Add UCanAccess to pom.xml

Insert the following <dependencies> section:

<dependencies>
    <!-- UCanAccess Core Driver -->
	<dependency>
		<groupId>net.sf.ucanaccess</groupId>
		<artifactId>ucanaccess</artifactId>
		<version>5.0.1</version>
	</dependency>
    
    <!-- Additional optional dependencies -->
    <dependency>
        <groupId>commons-lang</groupId>
        <artifactId>commons-lang</artifactId>
        <version>2.6</version>
    </dependency>

    <dependency>
        <groupId>commons-logging</groupId>
        <artifactId>commons-logging</artifactId>
        <version>1.2</version>
    </dependency>

    <dependency>
        <groupId>hsqldb</groupId>
        <artifactId>hsqldb</artifactId>
        <version>2.5.1</version>
    </dependency>
</dependencies>

#### Step 3: Build the Project

After saving, NetBeans automatically downloads the libraries.

Go to:

Project > Dependencies

	You’ll now see all JARs resolved automatically.

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

![Registration Form](Screenshots/Registration-Form.png)

![Participants Table](Screenshots/Data-table-tab.png)

![Successful Registration Popup](Screenshots/Successful-Registration.png)

![No Record Found Popup](Screenshots/no-record-found.png)

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

