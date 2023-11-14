package INTERFAZ;

import javax.swing.*;
import javax.swing.border.MatteBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColumnDetailsPanel {

    private int index;
    private JPanel panel;
    private JTextField columnNameField;
    private JComboBox<String> columnTypeField;
    private JTextField lengthField;
    private JCheckBox allowNullCheckBox;
    private JComboBox<String> indexComboBox;
    private JTextField columnLengthField; // Agrega un campo para la longitud de valores


    public ColumnDetailsPanel(int index) {
    	
    	
        this.index = index;

        // Crear el panel principal con un borde y diseño de cuadrícula
        panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel nameLabel = new JLabel("Columna " + (index + 1));
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(nameLabel, constraints);

        // Nombre de la columna
        columnNameField = new JTextField(1); // Ajusta el ancho preferido
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        panel.add(new JLabel("Nombre de la Columna:"), constraints);
        constraints.gridx = 1;
        panel.add(columnNameField, constraints);

        // Tipo de datos
        String[] dataTypes = {"INT", "VARCHAR(255)", "DATE", "DECIMAL"};
        columnTypeField = new JComboBox<>(dataTypes);
        columnTypeField.setPreferredSize(new Dimension(150, 25));
        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(new JLabel("Tipo de Datos:"), constraints);
        constraints.gridx = 1;
        panel.add(columnTypeField, constraints);

        
        
       
    
        
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getColumnName() {
        return columnNameField.getText();
    }

    public String getColumnType() {
        return columnTypeField.getSelectedItem().toString();
    }

    public String getValueLength() {
        return lengthField.getText();
    }

    public boolean isAllowNull() {
        return allowNullCheckBox.isSelected();
    }

    public String getIndex() {
        return (String) indexComboBox.getSelectedItem();
    }

    public int getColumnLength() {
        try {
            return Integer.parseInt(columnLengthField.getText());
        } catch (NumberFormatException e) {
            return 0; // Manejo de error si no es un número válido
        }
    }
    
}