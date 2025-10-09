/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vu.ExhibitionRegn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import javax.swing.filechooser.FileNameExtensionFilter;
//import com.itextpdf.text.*;
//import com.itextpdf.text.pdf.*;


/**
 *
 * @author Dell
 */
public class ExhibitionRegn extends javax.swing.JFrame {
    
     // =================== GLOBAL VARIABLES =====================
    private static final String DB_URL = "jdbc:ucanaccess://"
            + "C:\\Users\\Dell\\Documents\\NetBeansProjects\\ExhibitionRegistration\\"
            + "src\\main\\java\\vu\\ExhibitionRegn\\VUE_Exhibition.accdb";
    private Connection conn;
    private String loggedUser;
    private String role;

    private String selectedImagePath = null;
    private static final String IMAGE_DIR = "src/main/java/vu/ExhibitionRegn/images/";

    // =================== CONSTRUCTOR ==========================
    public ExhibitionRegn(String username, String userRole) {
        this.loggedUser = username;
        this.role = userRole;

        initComponents();
        connectDatabase();
        loadParticipantsData();

        setTitle("Exhibition Registration System - Logged in as: " + username + " (" + userRole + ")");
        
    }
    
     // =================== DATABASE CONNECTION =====================
    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Connection Error: " + e.getMessage());
        }
    }
    
    // =================== REGISTER PARTICIPANT =====================
    private void registerParticipant() {
        try {
            if (txtRegID.getText().isEmpty() || txtName.getText().isEmpty() ||
                txtDept.getText().isEmpty() || txtPartner.getText().isEmpty() ||
                txtContact.getText().isEmpty() || txtEmail.getText().isEmpty() ||
                selectedImagePath == null) {
                JOptionPane.showMessageDialog(this, "All fields including image are required!");
                return;
            }

            String regID = txtRegID.getText().trim();
            String checkSql = "SELECT COUNT(*) FROM Participants WHERE RegistrationID = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, regID);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Participant with Registration ID. " + regID + " already exists!");
                return;
            }

            File srcFile = new File(selectedImagePath);
            String ext = "";
            int i = srcFile.getName().lastIndexOf('.');
            if (i > 0) ext = srcFile.getName().substring(i);

            File destDir = new File(IMAGE_DIR);
            if (!destDir.exists()) destDir.mkdirs();

            File destFile = new File(destDir, regID + ext);
            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String sql = "INSERT INTO Participants (RegistrationID, ParticipantName, Department, DancePartner, ContactNumber, Email, IDImagePath) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, regID);
            ps.setString(2, txtName.getText());
            ps.setString(3, txtDept.getText());
            ps.setString(4, txtPartner.getText());
            ps.setString(5, txtContact.getText());
            ps.setString(6, txtEmail.getText());
            ps.setString(7, destFile.getPath());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Participant Registered Successfully!");
            clearForm();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Registration Error: " + e.getMessage());
        }
        loadParticipantsData();
    }


    // Search Participant
    private void searchParticipant() {
        try {
            String sql = "SELECT * FROM Participants WHERE RegistrationID=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtRegID.getText());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtName.setText(rs.getString("ParticipantName"));
                txtDept.setText(rs.getString("Department"));
                txtPartner.setText(rs.getString("DancePartner"));
                txtContact.setText(rs.getString("ContactNumber"));
                txtEmail.setText(rs.getString("Email"));
                String imgPath = rs.getString("IDImagePath");
                    selectedImagePath = imgPath;
                    lblImage.setIcon(new ImageIcon(selectedImagePath));
                    ImageIcon icon = new ImageIcon(selectedImagePath);
                    Image img = icon.getImage();
                    Image scaled = img.getScaledInstance(lblImage.getWidth(), 
                            lblImage.getHeight(), Image.SCALE_SMOOTH);
                    lblImage.setIcon(new ImageIcon(scaled));
            } else {
                JOptionPane.showMessageDialog(this, "No Record Found!");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search Error: " + e.getMessage());
        }
    }

    // =================== UPDATE PARTICIPANT =====================
    private String saveImageAsRegId(String regId, String sourcePath) throws IOException {
        if (sourcePath == null || sourcePath.trim().isEmpty()) return null;
        File src = new File(sourcePath);
        if (!src.exists()) return null;

        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) dir.mkdirs();

        String ext = "";
        int i = src.getName().lastIndexOf('.');
        if (i > 0) ext = src.getName().substring(i);

        File dest = new File(dir, regId + ext);
        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return dest.getAbsolutePath();
    }

    private void updateParticipant() {
    try {
        String regID = txtRegID.getText().trim();
        if (regID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Registration ID to update!");
            return;
        }

        // Check if participant exists
        String checkSql = "SELECT * FROM Participants WHERE RegistrationID = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setString(1, regID);
        ResultSet rs = checkPs.executeQuery();

        if (!rs.next()) {
            JOptionPane.showMessageDialog(this, "No participant found with Reg No: " + regID);
            return;
        }

        // Handle image update (if a new one is selected)
        String relativeImagePath = rs.getString("IDImagePath"); // keep old one if not changed
        if (selectedImagePath != null) {
            File srcFile = new File(selectedImagePath);
            String ext = "";

            int i = srcFile.getName().lastIndexOf('.');
            if (i > 0) ext = srcFile.getName().substring(i);

            // Define relative folder path
            String relativeFolder = "src\\main\\java\\vu\\exhibitionRegn\\images";
            File destDir = new File(relativeFolder);
            if (!destDir.exists()) destDir.mkdirs();

            // Copy and rename image file to match regID
            File destFile = new File(destDir, regID + ext);
            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Store only the relative path in the database
            relativeImagePath = relativeFolder + "\\" + regID + ext;
        }

        // Update query
        String updateSql = "UPDATE Participants SET ParticipantName=?, Department=?, DancePartner=?, "
                + "ContactNumber=?, Email=?, IDImagePath=? WHERE RegistrationID=?";
        PreparedStatement ps = conn.prepareStatement(updateSql);

        ps.setString(1, txtName.getText());
        ps.setString(2, txtDept.getText());
        ps.setString(3, txtPartner.getText());
        ps.setString(4, txtContact.getText());
        ps.setString(5, txtEmail.getText());
        ps.setString(6, relativeImagePath);
        ps.setString(7, regID);

        int rows = ps.executeUpdate();

        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "Participant details updated successfully!");
            clearForm();
            loadParticipantsData();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed. Please try again.");
        }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update Error: " + e.getMessage());
        }
        loadParticipantsData();
    }
    // ------------------ End of Update section ------------------

    // Delete Participant from Participants  and Associated Image from Images folder
    private void deleteParticipant() {
    try {
        // First fetch the image path for this registration ID
        String fetchSql = "SELECT IDImagePath FROM Participants WHERE RegistrationID=?";
        PreparedStatement psFetch = conn.prepareStatement(fetchSql);
        psFetch.setString(1, txtRegID.getText());
        ResultSet rs = psFetch.executeQuery();
        
        String imgPath = null;
        if (rs.next()) {
            imgPath = rs.getString("IDImagePath");
        }
        
        // Now delete record
        String sql = "DELETE FROM Participants WHERE RegistrationID=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, txtRegID.getText());
        int rows = ps.executeUpdate();
        
        if (rows > 0) {
            // Also delete image file if it exists
            if (imgPath != null) {
                File imgFile = new File(imgPath);
                if (imgFile.exists()) {
                    imgFile.delete();
                }
            }
            JOptionPane.showMessageDialog(this, "Record and Image Deleted!");
            clearForm();
        } else {
            JOptionPane.showMessageDialog(this, "No record found to delete.");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Delete Error: " + e.getMessage());
    }
    loadParticipantsData();

}

    // Clear Form
    private void clearForm() {
        txtRegID.setText("");
        txtName.setText("");
        txtDept.setText("");
        txtPartner.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        lblImage.setIcon(null);
        selectedImagePath = null;
    }

    // Upload Image
    private void uploadImage() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif");
        chooser.setFileFilter(filter);
        int option = chooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = chooser.getSelectedFile().getAbsolutePath();
            lblImage.setIcon(new ImageIcon(selectedImagePath));
            ImageIcon icon = new ImageIcon(selectedImagePath);
            Image img = icon.getImage();
            Image scaled = img.getScaledInstance(lblImage.getWidth(), lblImage.getHeight(), Image.SCALE_SMOOTH);
            lblImage.setIcon(new ImageIcon(scaled));
        }
    }
    
    // Load participants into JTable
    private void loadParticipantsData() {
        try {
            String sql = "SELECT RegistrationID, ParticipantName, Department, DancePartner, ContactNumber, Email FROM Participants";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            // Get metadata for column headers
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // Build table model
            javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel();
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(meta.getColumnName(i));
            }

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }
            tblParticipants.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading participants: " + e.getMessage());
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        lbl1 = new javax.swing.JLabel();
        txtRegID = new javax.swing.JTextField();
        lbl2 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lbl3 = new javax.swing.JLabel();
        txtDept = new javax.swing.JTextField();
        lbl4 = new javax.swing.JLabel();
        txtPartner = new javax.swing.JTextField();
        lbl5 = new javax.swing.JLabel();
        txtContact = new javax.swing.JTextField();
        lbl6 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblImage = new javax.swing.JLabel();
        btnUpload = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btnSearch = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnLogout2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblParticipants = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        btnLogout = new javax.swing.JButton();
        btnPrint = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Exhibition Registration System");
        setMinimumSize(new java.awt.Dimension(695, 395));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTabbedPane1.setForeground(new java.awt.Color(71, 40, 22));
        jTabbedPane1.setToolTipText("");
        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(173, 160, 127), 2, true));
        jPanel1.setForeground(new java.awt.Color(59, 40, 30));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbl1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl1.setText("Registation ID:");
        jPanel1.add(lbl1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 120, -1));

        txtRegID.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtRegID.setToolTipText("Enter Participant's ID.");
        txtRegID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRegIDActionPerformed(evt);
            }
        });
        jPanel1.add(txtRegID, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 50, 280, -1));

        lbl2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl2.setText("Participant's Name:");
        jPanel1.add(lbl2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 90, 120, -1));

        txtName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtName.setToolTipText("Enter Particpant's Name.");
        jPanel1.add(txtName, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 90, 280, -1));

        lbl3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl3.setText("Department:");
        jPanel1.add(lbl3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, 120, -1));

        txtDept.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtDept.setToolTipText("Enter Department.");
        jPanel1.add(txtDept, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 130, 280, -1));

        lbl4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl4.setText("Dance Partner:");
        jPanel1.add(lbl4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 120, -1));

        txtPartner.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtPartner.setToolTipText("Enter Dance Partner.");
        jPanel1.add(txtPartner, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 170, 280, -1));

        lbl5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl5.setText("Contact Number:");
        jPanel1.add(lbl5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 210, 120, -1));

        txtContact.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtContact.setToolTipText("Enter Contact Number.");
        jPanel1.add(txtContact, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 210, 280, -1));

        lbl6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl6.setText("Email Address:");
        jPanel1.add(lbl6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 250, 120, -1));

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtEmail.setToolTipText("Enter Email Address.");
        jPanel1.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 250, 280, -1));

        lblImage.setLabelFor(btnUpload);
        lblImage.setToolTipText("Participant's Photo");
        lblImage.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.add(lblImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 50, 170, 190));

        btnUpload.setBackground(new java.awt.Color(211, 222, 218));
        btnUpload.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnUpload.setForeground(new java.awt.Color(255, 255, 255));
        btnUpload.setText("Upload Image");
        btnUpload.setToolTipText("Browse to upload Image");
        btnUpload.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploadActionPerformed(evt);
            }
        });
        jPanel1.add(btnUpload, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 250, 190, 30));

        jPanel2.setBackground(new java.awt.Color(230, 235, 233));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnSearch.setBackground(new java.awt.Color(182, 194, 190));
        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSearch.setForeground(new java.awt.Color(255, 255, 255));
        btnSearch.setText("Search");
        btnSearch.setToolTipText("Search Participant.");
        btnSearch.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        jPanel2.add(btnSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 10, 90, 30));

        btnRegister.setBackground(new java.awt.Color(182, 194, 190));
        btnRegister.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnRegister.setForeground(new java.awt.Color(255, 255, 255));
        btnRegister.setText("Register");
        btnRegister.setToolTipText("Register Participant.");
        btnRegister.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        jPanel2.add(btnRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 90, 30));

        btnUpdate.setBackground(new java.awt.Color(182, 194, 190));
        btnUpdate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setText("Update");
        btnUpdate.setToolTipText("Update Record!");
        btnUpdate.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });
        jPanel2.add(btnUpdate, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 10, 90, 30));

        btnExit.setBackground(new java.awt.Color(199, 115, 115));
        btnExit.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnExit.setForeground(new java.awt.Color(255, 255, 255));
        btnExit.setText("Exit");
        btnExit.setToolTipText("Exit form.");
        btnExit.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        jPanel2.add(btnExit, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 10, 90, 30));

        btnClear.setBackground(new java.awt.Color(218, 227, 224));
        btnClear.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnClear.setForeground(new java.awt.Color(255, 255, 255));
        btnClear.setText("Clear");
        btnClear.setToolTipText("Clear Fields!");
        btnClear.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        jPanel2.add(btnClear, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 10, 90, 30));

        btnDelete.setBackground(new java.awt.Color(245, 142, 142));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("Delete");
        btnDelete.setToolTipText("Delete Record!");
        btnDelete.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 10, 90, 30));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 660, 50));

        jLabel1.setFont(new java.awt.Font("Garamond", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(34, 40, 54));
        jLabel1.setText("SALSA Festival Participants Registration Form");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 10, 480, -1));

        btnLogout2.setBackground(new java.awt.Color(214, 104, 83));
        btnLogout2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogout2.setForeground(new java.awt.Color(255, 204, 204));
        btnLogout2.setText("Logout");
        btnLogout2.setToolTipText("Logout!");
        btnLogout2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogout2ActionPerformed(evt);
            }
        });
        jPanel1.add(btnLogout2, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 10, 80, -1));

        jTabbedPane1.addTab("Register Participant", jPanel1);

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        tblParticipants.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Regn ID", "Participant Name", "Department", "Dance Partner", "Contact", "Email"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblParticipants);

        jPanel4.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 40, 680, 300));

        jLabel2.setFont(new java.awt.Font("Yu Gothic UI Semibold", 1, 18)); // NOI18N
        jLabel2.setText("SALSA FESTIVAL REGISTERED PARTICIPANTS DATA");
        jPanel4.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 10, -1, -1));

        btnLogout.setBackground(new java.awt.Color(214, 104, 83));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(255, 204, 204));
        btnLogout.setText("Logout");
        btnLogout.setToolTipText("Logout!");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        jPanel4.add(btnLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(593, 10, 80, -1));

        btnPrint.setBackground(javax.swing.UIManager.getDefaults().getColor("Actions.Blue"));
        btnPrint.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnPrint.setForeground(new java.awt.Color(255, 255, 255));
        btnPrint.setText("Print Report");
        btnPrint.setToolTipText("Print Participants' Data.");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });
        jPanel4.add(btnPrint, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 10, -1, -1));

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 680, 340));

        jTabbedPane1.addTab("Participants Data", jPanel3);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 680, 390));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int choice = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to logout?",
        "Logout Confirmation",
        JOptionPane.YES_NO_OPTION
    );
    if (choice == JOptionPane.YES_OPTION) {
        dispose();
        new LoginForm().setVisible(true);
        }
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();        // Handles form clearing.
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        System.exit(0);        // Handles Closing the Window.
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateParticipant();        // Updates participants records.
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        deleteParticipant();        // Deletes Participant Record.
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        registerParticipant();        // Register New Participant.
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchParticipant();        // Searches the Participants recods based on entered Registration Number.
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadActionPerformed
        uploadImage();        // Opens file chooser to choose file to upload.
    }//GEN-LAST:event_btnUploadActionPerformed

    private void txtRegIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRegIDActionPerformed
        // Form event Listener added by mistake text field Registration ID!
    }//GEN-LAST:event_txtRegIDActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        try {
        boolean complete = tblParticipants.print();
        if (complete) {
            JOptionPane.showMessageDialog(this, "Printing Complete!");
        } else {
            JOptionPane.showMessageDialog(this, "Printing Canceled!");
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Print Error: " + e.getMessage());
    } // TODO add your handling code here:
    }//GEN-LAST:event_btnPrintActionPerformed

    private void btnLogout2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogout2ActionPerformed
        int choice = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to logout?",
        "Logout Confirmation",
        JOptionPane.YES_NO_OPTION
    );
    if (choice == JOptionPane.YES_OPTION) {
        dispose();
        new LoginForm().setVisible(true);        // TODO add your handling code here:
        }
    }//GEN-LAST:event_btnLogout2ActionPerformed

    /**
     * @param args the command line arguments
     */
   // =================== MAIN METHOD =====================
    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
    
    // =================== LOGIN FORM INNER CLASS =====================
    static class LoginForm extends JFrame {
        JTextField txtUser;
        JPasswordField txtPass;
        JButton btnLogin, btnExit;

        public LoginForm() {
        setTitle("Login - Exhibition Registration System");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Use GridBagLayout for better alignment
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30); // top, left, bottom, right padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Username:"), gbc);

        // Username Field
        txtUser = new JTextField(15);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(txtUser, gbc);

        // Password Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Password:"), gbc);

        // Password Field
        txtPass = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.LINE_START;
        add(txtPass, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));

        btnLogin = new JButton("Login");
        btnExit = new JButton("Exit");
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnExit);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);

        // Add actions
        btnExit.addActionListener(e -> System.exit(0));
        btnLogin.addActionListener(e -> authenticate());

        setVisible(true);
    }

        private void authenticate() {
            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter username and password!");
                return;
            }

            try (Connection conn = DriverManager.getConnection(ExhibitionRegn.DB_URL)) {
                String sql = "SELECT * FROM Users WHERE Username=? AND Password=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, username);
                ps.setString(2, password);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String role = rs.getString("Role");
                    JOptionPane.showMessageDialog(this, "Welcome " + username + " (" + role + ")");
                    dispose();
                    new ExhibitionRegn(username, role).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Credentials!");
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
            }
        }
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnLogout2;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton btnRegister;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JButton btnUpload;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lbl1;
    private javax.swing.JLabel lbl2;
    private javax.swing.JLabel lbl3;
    private javax.swing.JLabel lbl4;
    private javax.swing.JLabel lbl5;
    private javax.swing.JLabel lbl6;
    private javax.swing.JLabel lblImage;
    private javax.swing.JTable tblParticipants;
    private javax.swing.JTextField txtContact;
    private javax.swing.JTextField txtDept;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPartner;
    private javax.swing.JTextField txtRegID;
    // End of variables declaration//GEN-END:variables
}
