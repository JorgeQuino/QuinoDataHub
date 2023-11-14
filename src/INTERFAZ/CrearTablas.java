package INTERFAZ;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CrearTablas {
    private String selectedDatabase;
    private ArrayList<ColumnDetailsPanel> columnPanels;
    private JTextField tableNameField;
    private JTextField columnCountField;
    private JTextField[] txtColumnNames;
    private int columnCount;

    public CrearTablas(String selectedDatabase) {
        this.selectedDatabase = selectedDatabase;
        columnPanels = new ArrayList<>();
    }

    public void crearTablas() {
    	
    	
        JFrame frame = new JFrame("Crear Tabla en la Base de Datos " + selectedDatabase);

        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font fieldFont = new Font("Arial", Font.PLAIN, 14);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        
        
     // Usar un diseño absoluto para el panel
       //mainPanel.setLayout(null);

     // Añadir un icono de "cancelar" con HTML y CSS
        JLabel cancelButton = new JLabel("<html><div style='font-size: 24px; color: red;'>&#10006;</div></html>");

        // Establecer la posición del icono en la esquina superior izquierda
        cancelButton.setBounds(0, 0, 30, 30); // (x, y, ancho, alto)

        // Agregar un ActionListener para cerrar la ventana principal
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                frame.dispose(); // Cierra la ventana actual
            }
        });

        mainPanel.add(cancelButton, gbc);




        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setUndecorated(true);

        tableNameField = new JTextField(30);
        tableNameField.setFont(fieldFont);
        tableNameField.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));

        columnCountField = new JTextField(5);
        columnCountField.setFont(fieldFont);
        columnCountField.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));

        JButton createTableButton = new JButton("Crear Tabla");
        createTableButton.setFont(labelFont);
        createTableButton.setForeground(Color.WHITE);
        createTableButton.setBackground(new Color(0, 102, 204));
        
        

        createTableButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                columnCount = Integer.parseInt(columnCountField.getText());
                openColumnDetailsForm(columnCount);
            }
        });

        
        // Añadimos el título con formato HTML y CSS
        JLabel titleLabel = new JLabel("<html><div style='text-align: center;'><h1 style='font-family: Arial, sans-serif; color: #0076C0;'>Crear Tabla en la Base de Datos</h1><p style='font-size: 14px; color: #555; margin-top: -10px;'>" + selectedDatabase + "</p></div><br><br></html>");

        mainPanel.add(titleLabel, gbc);
        mainPanel.add(new JLabel("Nombre de la tabla:"), gbc);
        mainPanel.add(tableNameField, gbc);
        mainPanel.add(new JLabel("Número de columnas:"), gbc);
        mainPanel.add(columnCountField, gbc);
        mainPanel.add(createTableButton, gbc);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        
        
        
        
        
    }

    private void openColumnDetailsForm(int columnCount) {
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        Font fieldFont = new Font("Arial", Font.PLAIN, 14);

        JFrame columnFrame = new JFrame("Definir Columnas");
        JPanel columnPanel = new JPanel(new GridBagLayout());

        columnFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        columnPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        txtColumnNames = new JTextField[columnCount];

        for (int i = 0; i < columnCount; i++) {
            ColumnDetailsPanel columnDetails = new ColumnDetailsPanel(i);
            columnPanels.add(columnDetails);

            JLabel lblColumnName = new JLabel("Nombre Columna " + (i + 1));
            lblColumnName.setFont(labelFont);
            lblColumnName.setForeground(null);

            txtColumnNames[i] = new JTextField();
            txtColumnNames[i].setPreferredSize(new Dimension(200, 25));
            txtColumnNames[i].setFont(fieldFont);
            txtColumnNames[i].setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));

            JLabel lblType = new JLabel("Tipo de Datos");
            lblType.setFont(labelFont);
            lblType.setForeground(null);
            JComboBox<String> columnTypeField = new JComboBox<>(new String[]{"INT", "VARCHAR(255)", "DATE", "DECIMAL"});
            columnTypeField.setPreferredSize(new Dimension(200, 25));

            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.WEST;
            columnPanel.add(lblColumnName, gbc);

            gbc.gridx = 1;
            columnPanel.add(txtColumnNames[i], gbc);

            gbc.gridx = 2;
            columnPanel.add(lblType, gbc);

            gbc.gridx = 3;
            columnPanel.add(columnTypeField, gbc);
        }
        
        

        JButton saveColumnsButton = new JButton("CREAR TABLA");
        saveColumnsButton.setFont(labelFont);
        saveColumnsButton.setForeground(Color.WHITE);
        saveColumnsButton.setBackground(new Color(0, 153, 51));

        columnFrame.getContentPane().add(columnPanel, BorderLayout.CENTER);
        columnFrame.getContentPane().add(saveColumnsButton, BorderLayout.SOUTH);
        columnFrame.pack();
        columnFrame.setLocationRelativeTo(null);
        columnFrame.setVisible(true);
        
     


        saveColumnsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String tableName = tableNameField.getText();
                    String url = "jdbc:mysql://localhost:3306/";
                    String user = "root";
                    String password = "";

                    Connection connection = DriverManager.getConnection(url + selectedDatabase, user, password);
                    StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName +  " (");

                    for (int i = 0; i < columnCount; i++) {
                        String columnName = txtColumnNames[i].getText();
                        String columnType = columnPanels.get(i).getColumnType();
                        createTableSQL.append(columnName).append(" ").append(columnType);
                        if (i < columnCount - 1) {
                            createTableSQL.append(", ");
                        }
                    }

                    createTableSQL.append(");");
                    System.out.println("SQL Query: " + createTableSQL.toString());

                    Statement statement = connection.createStatement();
                    statement.executeUpdate(createTableSQL.toString());

                    JOptionPane.showMessageDialog(null, "Tabla creada exitosamente.");



                
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al crear la tabla: " + ex.getMessage());
                }
            }
        });
    }
    
    
    

   
}




