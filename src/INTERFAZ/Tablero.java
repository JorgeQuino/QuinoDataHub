package INTERFAZ;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.Statement;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.sql.DatabaseMetaData;

public class Tablero {
	
    private DefaultListModel<String> listaModelo; // Declarar listaModelo como una variable miembro
    private Conexion conexion;
    private JList<String> listaBasesDatos;
    private JList<String> listaTablas;



    private static class CellRenderer extends JCheckBox implements ListCellRenderer<JCheckBox> {
        @Override
        public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
            setComponentOrientation(list.getComponentOrientation());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            setSelected(isSelected);
            setText(value.getText());
            return this;
        }
    }
    private String getTableIconHTML() {
        return "<html><div style='text-align: center;'>" +
                "<span style='font-size: 14pt;'>&#8226;</span>" +
                "</div></html>";
    }
    private static void mostrarColumnasDeTabla(String selectedDatabase, String selectedTable) {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Reemplaza con el driver correspondiente
            String url = "jdbc:mysql://localhost:3306/" + selectedDatabase;
            String usuario = "root";
            String contraseña = "";
            conn = DriverManager.getConnection(url, usuario, contraseña);

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, selectedTable, null);

            DefaultListModel<JCheckBox> columnListModel = new DefaultListModel<>();

            JCheckBox selectAllCheckBox = new JCheckBox("*");
            columnListModel.addElement(selectAllCheckBox);

            selectAllCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        // Si "*" se selecciona, seleccionar todos los demás checkboxes
                        for (int i = 0; i < columnListModel.getSize(); i++) {
                            JCheckBox checkBox = columnListModel.getElementAt(i);
                            checkBox.setSelected(true);
                        }
                    } else {
                        // Si "*" se deselecciona, deseleccionar todos los demás checkboxes
                        for (int i = 0; i < columnListModel.getSize(); i++) {
                            JCheckBox checkBox = columnListModel.getElementAt(i);
                            checkBox.setSelected(false);
                        }
                    }
                }
            });

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                JCheckBox checkBox = new JCheckBox(columnName);
                columnListModel.addElement(checkBox);
            }

            JList<JCheckBox> columnList = new JList<>(columnListModel);
            columnList.setCellRenderer(new CellRenderer());
            columnList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            JFrame columnFrame = new JFrame("Columnas de " + selectedTable);
            columnFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            columnFrame.add(new JScrollPane(columnList));

            JButton seleccionarButton = new JButton("Seleccionar Columnas");
            seleccionarButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<JCheckBox> selectedColumns = columnList.getSelectedValuesList();
                    boolean selectAll = selectAllCheckBox.isSelected();

                    JFrame dataFrame = new JFrame("Datos de Columnas Seleccionadas");
                    dataFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    DefaultTableModel tableModel = new DefaultTableModel();
                    JTable dataTable = new JTable(tableModel);
                    JScrollPane scrollPane = new JScrollPane(dataTable);
                    JTextField queryTextField = new JTextField();
                    queryTextField.setEditable(false);
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(scrollPane, BorderLayout.CENTER);
                    panel.add(queryTextField, BorderLayout.SOUTH);
                    dataFrame.add(panel);

                    try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                        if (selectAll) {
                            // Si se selecciona "*", agregar todas las columnas al modelo de la tabla
                            ResultSet allColumns = metaData.getColumns(null, null, selectedTable, null);
                            while (allColumns.next()) {
                                String columnName = allColumns.getString("COLUMN_NAME");
                                tableModel.addColumn(columnName);
                            }
                        } else {
                            // Agregar las columnas seleccionadas al modelo de la tabla
                            for (JCheckBox checkBox : selectedColumns) {
                                String columnName = checkBox.getText();
                                tableModel.addColumn(columnName);
                            }
                        }

                        StringBuilder queryBuilder = new StringBuilder("SELECT ");
                        if (selectAll) {
                            queryBuilder.append("*");
                        } else {
                            for (int i = 0; i < selectedColumns.size(); i++) {
                                queryBuilder.append(selectedColumns.get(i).getText());
                                if (i < selectedColumns.size() - 1) {
                                    queryBuilder.append(", ");
                                }
                            }
                        }
                        queryBuilder.append(" FROM ").append(selectedTable);

                        String query = queryBuilder.toString();
                        queryTextField.setText("Consulta realizada: " + query);

                        try (ResultSet resultSet = conn.createStatement().executeQuery(query)) {
                            while (resultSet.next()) {
                                Object[] rowData = new Object[selectedColumns.size()];
                                for (int i = 0; i < selectedColumns.size(); i++) {
                                    if (selectAll) {
                                        rowData[i] = resultSet.getObject(i + 1);
                                    } else {
                                        rowData[i] = resultSet.getObject(selectedColumns.get(i).getText());
                                    }
                                }
                                tableModel.addRow(rowData);
                            }
                            dataFrame.pack();
                            dataFrame.setVisible(true);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            columnFrame.add(seleccionarButton, BorderLayout.SOUTH);

            columnFrame.pack();
            columnFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    
    public Tablero() {
        conexion = new Conexion();
        listaModelo = new DefaultListModel<>(); // Inicializar listaModelo en el constructor
        listaBasesDatos = new JList<>(listaModelo);
        listaTablas = new JList<>();

    }
    
    

    

    private static JPanel panelIzquierdo; // Panel izquierdo
    private static JButton btnMostrarPanel; // Botón para mostrar/ocultar el panel


 
  
    
    public static void main(String[] args) {
    	
    
    	 
    	
    	
    	
        Tablero tablero = new Tablero();


    	
        Conexion conexion = new Conexion();

        DefaultListModel<String> listaModelo = new DefaultListModel<>();
        JList<String> listaBasesDatos = new JList<>(listaModelo);
        JScrollPane scrollPane = new JScrollPane(listaBasesDatos);

        SwingUtilities.invokeLater(new Runnable() {
        	
            public void run() {
            	
          
            	
            
                // Crear una ventana
                JFrame frame = new JFrame("Tablero con Panel Desplegable");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                

                // Obtener el tamaño de la pantalla
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                // Crear un panel principal que ocupará la ventana
                JPanel panelPrincipal = new JPanel(new BorderLayout());
                frame.getContentPane().add(panelPrincipal);

                // Crear un panel que irá en el centro (puedes personalizarlo)
                JPanel panelCentro = new JPanel();
                panelCentro.setBackground(Color.WHITE); // Color de fondo del panel central

                // Crear un título en el panel desplegable con mitad azul y mitad negro
                String titleText = "<html><br><font color='blue'>Quino</font><font color='black'>DataHub</font></html>";
                JLabel titleLabel = new JLabel(titleText);
                titleLabel.setFont(new Font("Oswald", Font.BOLD, 20));
                titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

             // Crear un panel desplegable en el lado izquierdo (personalizado)
                panelIzquierdo = new JPanel(new BorderLayout());
                panelIzquierdo.setBackground(new Color(55, 71, 79)); // Fondo con un tono de gris azulado
                panelIzquierdo.setPreferredSize(new Dimension(screenSize.width / 7, screenSize.height));
                panelIzquierdo.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 51, 102))); // Borde derecho
                panelIzquierdo.setOpaque(true); // Asegura que el fondo se ve correctamente

                // Etiqueta del título
                String titleText1 = "<html><br><font color='#2196F3'>Quino</font><font color='black'>DataHub</font></html>";
                JLabel titleLabel1 = new JLabel(titleText1);
                titleLabel1.setFont(new Font("Oswald", Font.BOLD, 24)); // Cambia el tamaño de fuente y estilo
                titleLabel1.setHorizontalAlignment(SwingConstants.CENTER);
                titleLabel1.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0)); // Espaciado en la parte superior e inferior

                // Agrega la etiqueta del título al panel izquierdo
                panelIzquierdo.add(titleLabel1, BorderLayout.NORTH);

                // Crear un panel derecho para mostrar las tablas
                JPanel panelDerecho = new JPanel(new BorderLayout());
                frame.getContentPane().add(panelDerecho, BorderLayout.CENTER);
                
                

                // Crear una formulaeio
                JFrame frame1 = new JFrame("Formulario con Estilo");
                frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame1.setSize(400, 300);

                JPanel panelPrincipal1 = new JPanel(new BorderLayout());
                frame1.add(panelPrincipal1);

                
                
                
                
                JPanel formulario = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);

                formulario.setBackground(Color.WHITE);
                
                JLabel etiqueta = new JLabel("CONECTARSE");
                etiqueta.setBounds(1000, 280, 200, 50); // (x, y, ancho, alto)

                
               
                // Agregar el formulario al panel principal
                panelPrincipal1.add(formulario, BorderLayout.CENTER);

                
          
                

     

                // Personalizar la etiqueta
                etiqueta.setForeground(Color.BLUE); // Cambiar el color del texto a azul
                Font fuente = new Font("Arial", Font.BOLD, 18); // Crear una fuente personalizada
                etiqueta.setFont(fuente); // Establecer la fuente
                etiqueta.setHorizontalAlignment(SwingConstants.CENTER); // Centrar el texto

                // Agregar la etiqueta al panel
                panelPrincipal1.add(etiqueta);

                // Estilo de fuentes y colores
                Font labelFont = new Font("Arial", Font.BOLD, 14);
                Font fieldFont = new Font("Arial", Font.PLAIN, 14);
                Color labelColor = new Color(30, 144, 255); // Azul

             // Crear las etiquetas con iconos más grandes usando HTML y CSS
                String usuarioIcon = "<html><span style='color: #1E90FF; font-size: 24px;'>&#128100;</span> Usuario:";
                JLabel lblUsuario = new JLabel(usuarioIcon);
                lblUsuario.setFont(labelFont);

                String contrasenaIcon = "<html><span style='color: #1E90FF; font-size: 24px;'>&#128274;</span> Contraseña:";
                JLabel lblContrasena = new JLabel(contrasenaIcon);
                lblContrasena.setFont(labelFont);

                String hostIcon = "<html><span style='color: #1E90FF; font-size: 24px;'>&#127760;</span> Host:";
                JLabel lblHost = new JLabel(hostIcon);
                lblHost.setFont(labelFont);

                String puertoIcon = "<html><span style='color: #1E90FF; font-size: 24px;'>&#128279;</span> Puerto:";
                JLabel lblPuerto = new JLabel(puertoIcon);
                lblPuerto.setFont(labelFont);


                JTextField txtUsuario = new JTextField(30);
                txtUsuario.setFont(fieldFont);
                txtUsuario.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));

             // Crear campo de contraseña
                JPasswordField txtContrasena = new JPasswordField(30);
                txtContrasena.setFont(fieldFont);
                txtContrasena.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));

                // Crear botón con icono de "ojo" usando HTML y CSS
                String eyeIcon = "<html><span style='font-size: 16px;'>&#x1F441;</span></html>";
                JButton showPasswordButton = new JButton(eyeIcon);
                showPasswordButton.setBorderPainted(false);
                showPasswordButton.setContentAreaFilled(false);

                // Agregar un ActionListener para el botón que mostrará/ocultará la contraseña
                showPasswordButton.addActionListener(new ActionListener() {
                    boolean passwordVisible = false;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        passwordVisible = !passwordVisible;
                        if (passwordVisible) {
                            txtContrasena.setEchoChar((char) 0); // Mostrar caracteres en lugar de puntos
                        } else {
                            txtContrasena.setEchoChar('\u2022'); // Volver a ocultar caracteres
                        }
                    }
                    
                    
                    
                });

                JTextField txtHost = new JTextField(30);
                txtHost.setFont(fieldFont);
                txtHost.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));

                JTextField txtPuerto = new JTextField(30);
                txtPuerto.setFont(fieldFont);
                txtPuerto.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));

                // Repite el proceso para los otros campos

                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.WEST; // Alinea las etiquetas a la izquierda
                formulario.add(lblUsuario, gbc);

                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.anchor = GridBagConstraints.WEST; // Alinea los campos de texto a la izquierda
                formulario.add(txtUsuario, gbc);

                gbc.gridx = 0;
                gbc.gridy = 2;
                formulario.add(lblContrasena, gbc);
                gbc.gridx = 0;
                gbc.gridy = 3;
                formulario.add(txtContrasena, gbc);

                gbc.gridx = 0;
                gbc.gridy = 4;
                formulario.add(lblHost, gbc);
                gbc.gridx = 0;
                gbc.gridy = 5;
                formulario.add(txtHost, gbc);

                gbc.gridx = 0;
                gbc.gridy = 6;
                formulario.add(lblPuerto, gbc);
                gbc.gridx = 0;
                gbc.gridy = 7;
                formulario.add(txtPuerto, gbc);
                
           
             // Crear botón "Test" con icono y estilo personalizado
                JButton btnTest = new JButton("<html><font color='white'>Test</font></html>");
                btnTest.setBackground(new Color(0, 102, 204)); // Fondo azul oscuro
                btnTest.setForeground(Color.WHITE); // Color del texto en blanco
                btnTest.setBorderPainted(false); // No mostrar el borde del botón
                btnTest.setFocusPainted(false); // No mostrar el foco del botón
                btnTest.setFont(new Font("Arial", Font.BOLD, 14)); // Establecer la fuente y el estilo del texto

                // Crear botón "Conectar" con icono y estilo personalizado
                JButton btnConectar = new JButton("<html><font color='white'>Conectar</font></html>");
                btnConectar.setBackground(new Color(0, 153, 51)); // Fondo verde oscuro
                btnConectar.setForeground(Color.WHITE); // Color del texto en blanco
                btnConectar.setBorderPainted(false); // No mostrar el borde del botón
                btnConectar.setFocusPainted(false); // No mostrar el foco del botón
                btnConectar.setFont(new Font("Arial", Font.BOLD, 14)); // Establecer la fuente y el estilo del texto


              

                JPanel panelBotones = new JPanel();
                panelBotones.add(btnTest);
                panelBotones.add(btnConectar);

                gbc.gridx = 0;
                gbc.gridy = 8;
                gbc.gridwidth = 1;
                gbc.anchor = GridBagConstraints.CENTER; // Alinea los botones al centro
                formulario.add(panelBotones, gbc);
                
                
                

                panelPrincipal1.add(formulario, BorderLayout.CENTER);
                frame1.setVisible(true);
            
                
                
                
                //////////////////////////////////////////////////////
                ///test
                btnTest.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Connection connection = conexion.obtenerConexion();
                            if (connection != null) {
                                conexion.pruebaConexion(connection);
                                JOptionPane.showMessageDialog(null, "Conexión a MySQL exitosa");
                            } else {
                                JOptionPane.showMessageDialog(null, "No se pudo obtener la conexión a MySQL");
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error al realizar la prueba de conexión");
                            ex.printStackTrace();
                        }
                    }
                });

                
                
                
                
          
                
                
                
     
          
          
          
      
                
                btnConectar.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String usuario = txtUsuario.getText();
                        String contraseña = txtContrasena.getText();
                        String host = txtHost.getText();
                        String puerto = txtPuerto.getText();

                        Conexion conexion = new Conexion();
                        try {
                            // Establece la conexión
                            conexion.conexion(host, puerto, usuario, contraseña);
                            etiqueta.setVisible(false);
                            formulario.setVisible(false);

                            List<String> nombresBasesDatos = conexion.obtenerNombresBasesDatos();

                            // Ordenar los nombres de las bases de datos alfabéticamente
                            Collections.sort(nombresBasesDatos);

                            // Limpiar la lista antes de agregar nuevos elementos
                            listaModelo.clear();

                            // Agregar nombres de bases de datos a la lista
                            for (String nombreBD : nombresBasesDatos) {
                                listaModelo.addElement(nombreBD);
                                
                                // Supongamos que tienes un icono llamado "databaseIcon" para las bases de datos
                                Icon icono = new ImageIcon("databaseIcon.png");
                            }

                            // Después de agregar los elementos a la listaModelo, crea el scrollPane
                            JScrollPane scrollPane = new JScrollPane(listaBasesDatos);
                            // Cambia el fondo del scrollPane a color gris
                            scrollPane.getViewport().setBackground(Color.LIGHT_GRAY);
                            // Agrégalo al panel izquierdo
                            panelIzquierdo.add(scrollPane, BorderLayout.CENTER);

                            // Después de crear la etiqueta en el panel izquierdo
                            
                            JButton btnCrearNuevaBD = new JButton("Crear nueva base de datos");
                            btnCrearNuevaBD.setFont(new Font("Arial", Font.BOLD, 14));
                            btnCrearNuevaBD.setBackground(new Color(0, 102, 204));
                            btnCrearNuevaBD.setForeground(Color.WHITE);

                         // Agregar un ActionListener para el botón "Crear nueva base de datos"
                            btnCrearNuevaBD.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    conexion.obtenerConexion();
                                    FormularioCrearBD formulario = new FormularioCrearBD(conexion);
                                    formulario.setVisible(true);
                                    
                                    
                                }
                            });

                            
                            
                            

                            // Agrega el botón de "Crear nueva base de datos" al panel izquierdo
                            panelIzquierdo.add(btnCrearNuevaBD, BorderLayout.SOUTH);

                            // Actualizar el panel desplegable
                            panelIzquierdo.revalidate();
                            panelIzquierdo.repaint();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(null, "Error al conectar a MySQL");
                            ex.printStackTrace();
                        }
                    }
                    
                  
                    
                });
                
     
     
                
                
           
            //    listaBasesDatos = new JList<>(listaModelo);

                // Establecer la apariencia de los elementos del JList con HTML y CSS
                listaBasesDatos.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                        // Establecer la apariencia del texto con HTML y CSS
                        String labelText = "<html><font color='#2196F3'>" + value + "</font></html>"; // Color azul
                        label.setText(labelText);
                        label.setFont(new Font("Arial", Font.BOLD, 16)); // Tamaño de fuente y estilo
                        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Espaciado en el elemento

                        return label;
                        
                    }
                });

                // Establecer la apariencia del JList
                listaBasesDatos.setBackground(new Color(55, 71, 79)); // Fondo del JList con el mismo color que el panel izquierdo
                listaBasesDatos.setForeground(Color.WHITE); // Texto en blanco

             // Establecer la apariencia de los elementos del JList con un icono de base de datos personalizado
                listaBasesDatos.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        String labelText = "<html><span style='font-size: 16px;'>&#128187;</span> " + value + "</html>"; // Icono de base de datos


                        label.setText(labelText);
                        label.setFont(new Font("Arial", Font.BOLD, 16)); // Tamaño de fuente y estilo
                        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Espaciado en el elemento

                        return label;
                    }
                });

                
                listaBasesDatos.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (!e.getValueIsAdjusting()) {
                            int selectedIndex = listaBasesDatos.getSelectedIndex();
                            if (selectedIndex >= 0) {
                                String selectedDatabase = listaModelo.getElementAt(selectedIndex);

                                JFrame frame = new JFrame("Tablas en " + selectedDatabase);
                                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                             
                             // Crear la etiqueta con formato HTML
                                JLabel nombreDatabaseLabel = new JLabel("<html><div style='text-align: center;'>" +
                                        "<span style='color: #2980b9; font-size: 22pt; font-family: \"Segoe UI\", Tahoma, Geneva, Verdana, sans-serif;'>" +
                                        "Base de datos seleccionada: </span><span style='color: #e74c3c;'>" + selectedDatabase + "</span></div></html>");

                                // Aplicar un borde redondeado
                                nombreDatabaseLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
                                nombreDatabaseLabel.setOpaque(true);
                                nombreDatabaseLabel.setBackground(new Color(250, 250, 250));
                                nombreDatabaseLabel.setForeground(new Color(44, 62, 80));
                                nombreDatabaseLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

                                
                                Connection conn = null;
                                try {
                                    Class.forName("com.mysql.jdbc.Driver"); // Reemplaza con el driver correspondiente
                                    String url = "jdbc:mysql://localhost:3306/" + selectedDatabase;
                                    String usuario = "root";
                                    String contraseña = "";
                                    conn = DriverManager.getConnection(url, usuario, contraseña);

                                   
                                    DatabaseMetaData metaData = conn.getMetaData();
                                    ResultSet tables = metaData.getTables(selectedDatabase, null, "%", new String[] { "TABLE" });

                                    DefaultListModel<String> tablaListModel = new DefaultListModel<>();

                                    while (tables.next()) {
                                                                         
                                                       tablaListModel.addElement("<html><span style='color:blue; font-size: 18px; font-family: Arial, sans-serif; font-weight: bold;'>&#xf0ce;</span> </html>" );
                                                       tablaListModel.addElement(tables.getString("TABLE_NAME"));

                                                                       }

                                    JList<String> tablaList = new JList<>(tablaListModel);
                                    JScrollPane scrollPane = new JScrollPane(tablaList);

                                    JPanel panel = new JPanel();
                                    panel.setLayout(new BorderLayout());

                                    panel.add(nombreDatabaseLabel, BorderLayout.NORTH);
                                    panel.add(scrollPane, BorderLayout.CENTER);
                                    
                                    JButton crearTablaButton = new JButton("Crear Nueva Tabla");
                                    // Personalizar el botón
                                    crearTablaButton.setForeground(Color.WHITE);
                                    crearTablaButton.setBackground(new Color(46, 204, 113));
                                    crearTablaButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
                                    crearTablaButton.setFocusPainted(false);
                                    crearTablaButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
                                    
                                    
                                    
                                    crearTablaButton.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                        	 CrearTablas CT = new CrearTablas(selectedDatabase); 
                                        	 CT.crearTablas();
                                        }
                                    });

                                    frame.add(panel);
                                    frame.pack();
                                    frame.setVisible(true);
                                    
                                    panel.add(crearTablaButton, BorderLayout.SOUTH);

                                    
                                    
                                  
                                    
                                    tablaList.addListSelectionListener(new ListSelectionListener() {
                                        @Override
                                        public void valueChanged(ListSelectionEvent e) {
                                            if (!e.getValueIsAdjusting()) {
                                                String selectedTable = tablaList.getSelectedValue();
                                                if (selectedTable != null) {
                                                    mostrarColumnasDeTabla(selectedDatabase, selectedTable);
                                                }
                                            }
                                        }
                                    });

                                    

                                 // Dentro del ListSelectionListener de tablaList
                                    tablaList.addListSelectionListener(new ListSelectionListener() {
                                        @Override
                                        public void valueChanged(ListSelectionEvent e) {
                                            if (!e.getValueIsAdjusting()) {
                                                int selectedTableIndex = tablaList.getSelectedIndex();
                                                if (selectedTableIndex >= 0) {
                                                    String selectedTable = tablaListModel.getElementAt(selectedTableIndex);

                                                    try {
                                                        Connection conn = null;
                                                        Class.forName("com.mysql.jdbc.Driver"); // Reemplaza con el driver correspondiente
                                                        String url = "jdbc:mysql://localhost:3306/" + selectedDatabase;
                                                        String usuario = "root";
                                                        String contraseña = "";
                                                        conn = DriverManager.getConnection(url, usuario, contraseña);

                                                        String query = "SELECT * FROM " + selectedTable;
                                                        java.sql.Statement statement = conn.createStatement();
                                                        ResultSet resultSet = statement.executeQuery(query);

                                                        JPanel detailsPanel = new JPanel(new BorderLayout());

                                                        JLabel databaseLabel = new JLabel("<html><div style='font-size: 18px; color: #007BFF; " +
                                                                "font-weight: bold; text-decoration: underline;'>Base de Datos: " +
                                                                selectedDatabase + "</div></html>");

                                                        JLabel tableNameLabel = new JLabel("<html><div style='font-size: 18px; color: #28A745; " +
                                                                "font-weight: bold; text-decoration: underline;'>Nombre de la tabla: " +
                                                                selectedTable + "</div></html>");
                                                        
                                                        databaseLabel.setFont(new Font("Arial", Font.BOLD, 14));
                                                        tableNameLabel.setFont(new Font("Arial", Font.BOLD, 14));

                                                        detailsPanel.add(databaseLabel, BorderLayout.NORTH);
                                                        detailsPanel.add(tableNameLabel, BorderLayout.CENTER);

                                                        DefaultTableModel tableModel = new DefaultTableModel();
                                                        
                                                        ResultSetMetaData metaData = resultSet.getMetaData();
                                                        
                                                        
                                                        
                                                        int columnCount = metaData.getColumnCount();

                                                        for (int i = 1; i <= columnCount; i++) {
                                                            tableModel.addColumn(metaData.getColumnName(i));
                                                        }

                                                        while (resultSet.next()) {
                                                            Object[] rowData = new Object[columnCount];
                                                            for (int i = 1; i <= columnCount; i++) {
                                                                rowData[i - 1] = resultSet.getObject(i);
                                                            }
                                                            tableModel.addRow(rowData);
                                                        }

                                                        JTable table = new JTable(tableModel);
                                                        table.setCellSelectionEnabled(true); // Hacer celdas editables

                                                        table.getModel().addTableModelListener(new TableModelListener() {
                                                            @Override
                                                            public void tableChanged(TableModelEvent e) {
                                                                if (e.getType() == TableModelEvent.UPDATE) {
                                                                    int row = e.getFirstRow();
                                                                    int column = e.getColumn();
                                                                    
                                                                    if (row >= 0 && column >= 0) {
                                                                        Object newValue = table.getValueAt(row, column);
                                                                        String columnName = table.getColumnName(column);

                                                                        // Resto del código para la actualización en la base de datos
                                                                        // ...

                                                                        System.out.println("Valor nuevo: " + newValue);
                                                                        System.out.println("Columna: " + columnName);
                                                                        
                                                                        try {
                                                                            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + selectedDatabase, "root", "");
                                                                            String primaryKeyColumn ="id";  // Reemplaza "ID" por el nombre correcto de tu columna clave primaria
                                                                            String updateQuery = "UPDATE " + selectedTable + " SET " + columnName + " = ? WHERE " + primaryKeyColumn + " = ?";
                                                                            PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);
                                                                            preparedStatement.setObject(1, newValue);
                                                                            int primaryKeyColumnIndex = 0;  // Reemplaza 0 por el índice correcto de tu columna clave primaria
                                                                            preparedStatement.setObject(2, table.getValueAt(row, primaryKeyColumnIndex));
                                                                            int rowsUpdated = preparedStatement.executeUpdate();
                                                                            conn.close();

                                                                            System.out.println("Filas actualizadas: " + rowsUpdated);
                                                                            JOptionPane.showMessageDialog(null, "Datos actualizados con éxito.");
                                                                        } catch (SQLException ex) {
                                                                            ex.printStackTrace();
                                                                            JOptionPane.showMessageDialog(null, "Error al actualizar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });




                                                        detailsPanel.add(new JScrollPane(table), BorderLayout.SOUTH);

                                                        // Eliminación de datos al seleccionar filas y presionar "Suprimir"
                                                     // ...

                                                        JLabel consultaLabel = new JLabel("Consulta SQL: ");
                                                        consultaLabel.setFont(new Font("Arial", Font.BOLD, 14));
                                                        detailsPanel.add(consultaLabel, BorderLayout.NORTH);

                                                        // Eliminación de datos al seleccionar filas y presionar "Suprimir"
                                                        table.addKeyListener(new KeyAdapter() {
                                                            @Override
                                                            public void keyReleased(KeyEvent e) {
                                                                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                                                                    int[] selectedRows = table.getSelectedRows();
                                                                    if (selectedRows.length > 0) {
                                                                        for (int i = selectedRows.length - 1; i >= 0; i--) {
                                                                            int selectedRow = selectedRows[i];
                                                                            int modelRow = table.convertRowIndexToModel(selectedRow);
                                                                            // Captura el contenido de la fila antes de eliminarla
                                                                            Object[] rowData = new Object[tableModel.getColumnCount()];
                                                                            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                                                                                rowData[j] = tableModel.getValueAt(modelRow, j);
                                                                            }
                                                                            // Elimina la fila del modelo de la tabla
                                                                            tableModel.removeRow(modelRow);
                                                                            // Muestra la consulta en algún componente de la interfaz
                                                                            StringBuilder queryBuilder = new StringBuilder("DELETE FROM " + selectedTable + " WHERE ");
                                                                            for (int k = 0; k < tableModel.getColumnCount(); k++) {
                                                                                queryBuilder.append(tableModel.getColumnName(k))
                                                                                            .append(" = '")
                                                                                            .append(rowData[k])
                                                                                            .append("'");
                                                                                if (k < tableModel.getColumnCount() - 1) {
                                                                                    queryBuilder.append(" AND ");
                                                                                }
                                                                            }
                                                                            String deleteQuery = queryBuilder.toString();
                                                                            consultaLabel.setText("Consulta SQL: " + deleteQuery);
                                                                            JOptionPane.showMessageDialog(null, "Se eliminará el siguiente registro:\n" + deleteQuery);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });

                                                        // ...

                                                        // Método para mostrar la consulta en una etiqueta en la interfaz
                                                        table.getModel().addTableModelListener(new TableModelListener() {
                                                            @Override
                                                            public void tableChanged(TableModelEvent e) {
                                                                if (e.getType() == TableModelEvent.UPDATE) {
                                                                    int row = e.getFirstRow();
                                                                    int column = e.getColumn();

                                                                    if (row >= 0 && column >= 0) {
                                                                        Object newValue = table.getValueAt(row, column);
                                                                        String columnName = table.getColumnName(column);

                                                                        // Resto del código para la actualización en la base de datos
                                                                        // ...

                                                                        System.out.println("Valor nuevo: " + newValue);
                                                                        System.out.println("Columna: " + columnName);

                                                                        try {
                                                                            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + selectedDatabase, "root", "");
                                                                            String primaryKeyColumn = "id";  // Reemplaza "ID" por el nombre correcto de tu columna clave primaria
                                                                            String updateQuery = "UPDATE " + selectedTable + " SET " + columnName + " = ? WHERE " + primaryKeyColumn + " = ?";
                                                                            PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);
                                                                            preparedStatement.setObject(1, newValue);
                                                                            int primaryKeyColumnIndex = 0;  // Reemplaza 0 por el índice correcto de tu columna clave primaria
                                                                            preparedStatement.setObject(2, table.getValueAt(row, primaryKeyColumnIndex));
                                                                            int rowsUpdated = preparedStatement.executeUpdate();
                                                                            conn.close();

                                                                            System.out.println("Filas actualizadas: " + rowsUpdated);
                                                                            JOptionPane.showMessageDialog(null, "Datos actualizados con éxito.\nConsulta SQL: " + updateQuery);
                                                                            consultaLabel.setText("Consulta SQL: " + updateQuery);
                                                                        } catch (SQLException ex) {
                                                                            ex.printStackTrace();
                                                                            JOptionPane.showMessageDialog(null, "Error al actualizar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });

                                                        // ...


                                                        // Crear un botón para insertar datos
                                                        JButton insertButton = new JButton("Insertar Datos");
                                                        detailsPanel.add(insertButton, BorderLayout.EAST);

                                                        insertButton.addActionListener(new ActionListener() {
                                                            @Override
                                                            public void actionPerformed(ActionEvent e) {
                                                                JPanel dataEntryPanel = new JPanel(new GridLayout(columnCount, 2));
                                                                JTextField[] dataFields = new JTextField[columnCount];

                                                                for (int i = 1; i <= columnCount; i++) {
                                                                    JLabel label = null;
                                                                    try {
                                                                        label = new JLabel(metaData.getColumnName(i) + ":");
                                                                    } catch (SQLException e1) {
                                                                        e1.printStackTrace();
                                                                    }
                                                                    dataEntryPanel.add(label);

                                                                    JTextField textField = new JTextField(15);
                                                                    dataFields[i - 1] = textField;
                                                                    dataEntryPanel.add(textField);
                                                                }

                                                                int result = JOptionPane.showConfirmDialog(null, dataEntryPanel, "Ingresar Datos", JOptionPane.OK_CANCEL_OPTION);

                                                                if (result == JOptionPane.OK_OPTION) {
                                                                    try {
                                                                        String insertQuery = "INSERT INTO " + selectedTable + " (";
                                                                        String values = " VALUES (";
                                                                        for (int i = 0; i < columnCount; i++) {
                                                                            insertQuery += metaData.getColumnName(i + 1);
                                                                            values += "'" + dataFields[i].getText() + "'";
                                                                            if (i < columnCount - 1) {
                                                                                insertQuery += ", ";
                                                                                values += ", ";
                                                                            }
                                                                        }
                                                                        insertQuery += ")" + values + ")";

                                                                        Connection insertConn = DriverManager.getConnection(url, usuario, contraseña);
                                                                        java.sql.Statement insertStatement = insertConn.createStatement();
                                                                        insertStatement.executeUpdate(insertQuery);

                                                                        JOptionPane.showMessageDialog(null, "Datos insertados con éxito.");
                                                                    } catch (SQLException ex) {
                                                                        ex.printStackTrace();
                                                                        JOptionPane.showMessageDialog(null, "Error al insertar datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                                                    }
                                                                }
                                                            }
                                                        });

                                                        JOptionPane.showMessageDialog(null, detailsPanel, "Datos de la tabla", JOptionPane.INFORMATION_MESSAGE);
                                                    } catch (Exception ex) {
                                                        ex.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    });


                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                } finally {
                                    
                                }
                            }
                        }
                    }
                });
                
                
                
               
                
                
                
                

                // Agregar el panel izquierdo oculto por defecto
                panelPrincipal1.add(panelIzquierdo, BorderLayout.WEST);

                // Crear un ActionListener al botón "Mostrar Panel Izquierdo"
                btnMostrarPanel = new JButton("Mostrar Panel Izquierdo");

                // Agregar el botón al panel central
                panelCentro.add(btnMostrarPanel);

                // Ajustar el tamaño de la ventana al tamaño de la pantalla
                frame1.setExtendedState(JFrame.MAXIMIZED_BOTH);

                // Hacer visible la ventana
                frame1.setVisible(true);
        

                
            }
        });
        
        
        
        
        

    }
}
