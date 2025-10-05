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
    
    // JDBC variables
    private static final String DB_URL = "jdbc:ucanaccess://"
            + "C:\\Users\\Dell\\Documents\\NetBeansProjects\\ExhibitionRegistration\\"
            + "src\\main\\java\\vu\\ExhibitionRegn\\VUE_Exhibition.accdb";
    private Connection conn;
    

    // Image path
    private String selectedImagePath = null;
    
    /**
     * Creates new form ExhibitionRegn
     */
    public ExhibitionRegn() {
        initComponents();
        connectDatabase();
        loadParticipantsData();
    }
    
     // Database connection
    private void connectDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            JOptionPane.showMessageDialog(this, "Database Connected!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Connection Error: " + e.getMessage());
        }
    }
    
    // Register Participant
    // ===== Corrected Register Participant =====
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

        // Check if participant already exists
        String checkSql = "SELECT COUNT(*) FROM Participants WHERE RegistrationID = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setString(1, regID);
        ResultSet rs = checkPs.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            JOptionPane.showMessageDialog(this, 
                "Participant with Registration ID. " + regID + " is already registered!");
            return;
        }

        // Rename image to RegistrationID only (keep extension)
        File srcFile = new File(selectedImagePath);
        String ext = "";
        int i = srcFile.getName().lastIndexOf('.');
        if (i > 0) {
            ext = srcFile.getName().substring(i);
        }

        // Save image to images folder
        File destDir = new File("src/main/java/vu/exhibitionRegn/images");
        if (!destDir.exists()) destDir.mkdirs();
        File destFile = new File(destDir, regID + ext);
        Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Insert new participant
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

    //=============== Update Participant Section ======================
    // Image directory
    private static final String IMAGE_DIR = "src/main/java/vu/ExhibitionRegn/images/";
    
    // Save uploaded image as <regId>.<ext>, return absolute path (or null on failure)
    private String saveImageAsRegId(String regId, String sourcePath) throws IOException {
        if (sourcePath == null || sourcePath.trim().isEmpty()) return null;
        File src = new File(sourcePath);
        if (!src.exists()) return null;

        // ensure dir exists
        File dir = new File(IMAGE_DIR);
        if (!dir.exists()) dir.mkdirs();

        // get extension
        String ext = "";
        String name = src.getName();
        int i = name.lastIndexOf('.');
        if (i > 0) ext = name.substring(i); // includes dot

        File dest = new File(dir, regId + ext);
        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return dest.getAbsolutePath();
    }

    //Logic to handle update of partcipant data.
    private void updateParticipant() {
        String regId = txtRegID.getText().trim();
        if (regId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter Registration ID to update.", 
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Basic field validation 
        if (txtName.getText().trim().isEmpty() || txtDept.getText().trim().isEmpty()
                || txtContact.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields (Name, Department, Contact, "
                    + "Email).", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 1) fetch existing image path from DB (if any)
            String existingImagePath = null;
            String fetchSql = "SELECT IDImagePath FROM Participants WHERE RegistrationID = ?";
            try (PreparedStatement psFetch = conn.prepareStatement(fetchSql)) {
                psFetch.setString(1, regId);
                try (ResultSet rs = psFetch.executeQuery()) {
                    if (rs.next()) existingImagePath = rs.getString("IDImagePath");
                    else {
                        JOptionPane.showMessageDialog(this, "No record found for Registration ID: " + 
                                regId, "Not Found", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // 2) If a new image was uploaded (selectedImagePath != null), save it and delete old image
            String finalImagePath = existingImagePath; // default keep old
            if (selectedImagePath != null && !selectedImagePath.trim().isEmpty()) {
                String saved = saveImageAsRegId(regId, selectedImagePath); // may throw IOException
                if (saved != null && !saved.isEmpty()) {
                    finalImagePath = saved;
                    // delete old file if different
                    if (existingImagePath != null && !existingImagePath.isEmpty() && 
                            !existingImagePath.equals(finalImagePath)) {
                        File old = new File(existingImagePath);
                        if (old.exists()) old.delete();
                    }
                }
            }

            // 3) perform update with correct parameter order
            String updateSql = "UPDATE Participants SET ParticipantName=?, Department=?, "
                    + "DancePartner=?, ContactNumber=?, Email=?, IDImagePath=? WHERE RegistrationID=?";
            try (PreparedStatement pst = conn.prepareStatement(updateSql)) {
                pst.setString(1, txtName.getText().trim());
                pst.setString(2, txtDept.getText().trim());
                pst.setString(3, txtPartner.getText().trim());
                pst.setString(4, txtContact.getText().trim());
                pst.setString(5, txtEmail.getText().trim());
                // set image path (may be null)
                if (finalImagePath == null) pst.setNull(6, Types.VARCHAR);
                else pst.setString(6, finalImagePath);
                pst.setString(7, regId);

                int affected = pst.executeUpdate();
                if (affected > 0) {
                    selectedImagePath = finalImagePath; // keep UI state consistent
                    JOptionPane.showMessageDialog(this, "Record updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Update failed. No record updated.", "Update Error", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this, "DB error during update: " + sqle.getMessage(), "DB Error", 
                    JOptionPane.ERROR_MESSAGE);

        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this, "File error saving image: " + ioe.getMessage(), "File Error", 
                    JOptionPane.ERROR_MESSAGE);
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
        btnDelete = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblParticipants = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        btnPrint = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Exhibition Registration System");
        setMinimumSize(new java.awt.Dimension(695, 395));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTabbedPane1.setForeground(new java.awt.Color(71, 40, 22));
        jTabbedPane1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N

        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(173, 160, 127), 2, true));
        jPanel1.setForeground(new java.awt.Color(59, 40, 30));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lbl1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl1.setText("Registation ID:");
        jPanel1.add(lbl1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 120, -1));

        txtRegID.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
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
        jPanel1.add(txtName, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 90, 280, -1));

        lbl3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl3.setText("Department:");
        jPanel1.add(lbl3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, 120, -1));

        txtDept.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel1.add(txtDept, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 130, 280, -1));

        lbl4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl4.setText("Dance Partner:");
        jPanel1.add(lbl4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 120, -1));

        txtPartner.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel1.add(txtPartner, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 170, 280, -1));

        lbl5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl5.setText("Contact Number:");
        jPanel1.add(lbl5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 210, 120, -1));

        txtContact.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel1.add(txtContact, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 210, 280, -1));

        lbl6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbl6.setText("Email Address:");
        jPanel1.add(lbl6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 250, 120, -1));

        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel1.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 250, 280, -1));

        lblImage.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.add(lblImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 50, 190, 180));

        btnUpload.setBackground(new java.awt.Color(211, 222, 218));
        btnUpload.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnUpload.setForeground(new java.awt.Color(255, 255, 255));
        btnUpload.setText("Upload Image");
        btnUpload.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnUpload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploadActionPerformed(evt);
            }
        });
        jPanel1.add(btnUpload, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 240, 190, 30));

        jPanel2.setBackground(new java.awt.Color(230, 235, 233));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnSearch.setBackground(new java.awt.Color(182, 194, 190));
        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSearch.setForeground(new java.awt.Color(255, 255, 255));
        btnSearch.setText("Search");
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
        btnRegister.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegisterActionPerformed(evt);
            }
        });
        jPanel2.add(btnRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 90, 30));

        btnDelete.setBackground(new java.awt.Color(245, 142, 142));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setText("Delete");
        btnDelete.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel2.add(btnDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 10, 90, 30));

        btnUpdate.setBackground(new java.awt.Color(182, 194, 190));
        btnUpdate.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setText("Update");
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
        btnClear.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, new java.awt.Color(153, 153, 153), new java.awt.Color(204, 204, 204), new java.awt.Color(102, 102, 102), new java.awt.Color(204, 204, 204)));
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        jPanel2.add(btnClear, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 10, 90, 30));

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, 660, 50));

        jLabel1.setFont(new java.awt.Font("Garamond", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(34, 40, 54));
        jLabel1.setText("SALSA Festival Participants Registration Form");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 10, 480, -1));

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

        btnPrint.setText("Print Report");
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });
        jPanel4.add(btnPrint, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, -1, -1));

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 680, 340));

        jTabbedPane1.addTab("Participants Data", jPanel3);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 680, 390));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtRegIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRegIDActionPerformed
        // Form event Listener added by mistake text field Registration ID!
    }//GEN-LAST:event_txtRegIDActionPerformed

    private void btnUploadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploadActionPerformed
        uploadImage();        // Opens file chooser to choose file to upload.
    }//GEN-LAST:event_btnUploadActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchParticipant();        // Searches the Participants recods based on entered Registration Number.
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
        registerParticipant();        // Register New Participant.
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateParticipant();        // Updates participants records.
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        deleteParticipant();        // Deletes Participant Record.
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        clearForm();        // Handles form clearing.
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        System.exit(0);        // Handles Closing the Window.
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed
        try {
        boolean complete = tblParticipants.print(
                JTable.PrintMode.FIT_WIDTH, 
                new java.text.MessageFormat("SALSA Festival Participants Report"), 
                new java.text.MessageFormat("Page {0}")
        );
        if (complete) {
            JOptionPane.showMessageDialog(this, "Printing Complete!");
        } else {
            JOptionPane.showMessageDialog(this, "Printing Cancelled.");
        }
        } catch (java.awt.print.PrinterException pe) {
            JOptionPane.showMessageDialog(this, "Printing Error: " + pe.getMessage());
        }

    }//GEN-LAST:event_btnPrintActionPerformed

    /**
     * @param args the command line arguments
     */
   public static void main(String args[]) {
    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
            new ExhibitionRegn().setVisible(true);
        }
    });
}


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExit;
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
