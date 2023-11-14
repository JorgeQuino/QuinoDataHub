package INTERFAZ;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JList;

import java.awt.Component;
public class CellRenderer  extends DefaultListCellRenderer {

	
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) value;
            checkBox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            checkBox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            checkBox.setEnabled(list.isEnabled());
            checkBox.setFont(list.getFont());
            checkBox.setFocusPainted(false);
            checkBox.setBorderPainted(true);
            return checkBox;
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
