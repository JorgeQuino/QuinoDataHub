package INTERFAZ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;

public class FormularioCrearBD extends JFrame {
    private Conexion conexion;
    private JTextField nombreBDField;
    private JComboBox<String> charsetComboBox;

    public FormularioCrearBD(Conexion conexion) {
        this.conexion = conexion;
        setTitle("Crear Nueva Base de Datos");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Elimina los botones de cerrar, minimizar y maximizar
        setUndecorated(true);

        // Crear un panel principal
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Icono de cerrar con HTML y CSS en la esquina superior derecha
        JLabel closeIcon = new JLabel("<html><span style='font-size: 20px;'>&#10006;</span></html>");
        closeIcon.setForeground(Color.RED);
        closeIcon.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(closeIcon, gbc);

        // Agregar un ActionListener para cerrar la ventana cuando se haga clic en el icono
        closeIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose(); // Cierra la ventana
            }
        });

        JLabel etiqueta = new JLabel("Crear nueva base de datos...");
        etiqueta.setForeground(Color.BLUE);
        Font fuente = new Font("Arial", Font.BOLD, 20);
        etiqueta.setFont(fuente);
        etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(etiqueta, gbc);

        // Etiqueta y campo de entrada para el nombre de la base de datos
        JLabel nombreBDLabel = new JLabel("Nombre de la Base de Datos:");
        nombreBDLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nombreBDField = new JTextField(30);
        nombreBDField.setFont(new Font("Arial", Font.PLAIN, 14));
        nombreBDField.setBorder(new MatteBorder(0, 0, 2, 0, Color.BLACK));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(nombreBDLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nombreBDField, gbc);

        // Etiqueta y JComboBox para el charset
        JLabel charsetLabel = new JLabel("Charset:");
        charsetLabel.setFont(new Font("Arial", Font.BOLD, 14));
        String[] charsets = {"UTF-8", "ISO-8859-1", "UTF-16", "Otros"};
        charsetComboBox = new JComboBox<>(charsets);
        charsetComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        // Crear un borde inferior para el JComboBox
        CompoundBorder charsetBorder = BorderFactory.createCompoundBorder(
                charsetComboBox.getBorder(),
                new MatteBorder(0, 0, 1, 0, Color.BLACK)
        );
        charsetComboBox.setBorder(charsetBorder);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(charsetLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(charsetComboBox, gbc);

        // Botón para crear la base de datos
        JButton crearBDButton = new JButton("Crear Base de Datos");
        crearBDButton.setBackground(new Color(0, 102, 0));
        crearBDButton.setForeground(Color.WHITE);
        crearBDButton.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(crearBDButton, gbc);

        crearBDButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nombreBD = nombreBDField.getText();
                String charset = charsetComboBox.getSelectedItem().toString();

                // En este ejemplo, simplemente mostraremos un mensaje con los datos ingresados.
                try {
                	conexion.obtenerConexion();

                    String mensaje = "Nombre de la Base de Datos: " + nombreBD + "\nCharset: " + charset;
                    JOptionPane.showMessageDialog(null, "Base de datos creada con éxito.");
                    conexion.crearBaseDeDatos(nombreBD);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error al crear la base de datos: " + ex.getMessage());
                }


                // Cierra el formulario después de completar la acción
                dispose();

            }
        });


        // Agregar el panel al marco
        add(panel);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Conexion conexion = new Conexion();
                FormularioCrearBD formulario = new FormularioCrearBD(conexion);
                formulario.setVisible(true);
            }
        });
    }
}

