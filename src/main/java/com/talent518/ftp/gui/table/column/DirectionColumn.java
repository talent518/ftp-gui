package com.talent518.ftp.gui.table.column;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import com.talent518.ftp.gui.table.ProgressTable;

public class DirectionColumn extends AbstractCellEditor implements TableCellRenderer {
	private static final long serialVersionUID = 1269871617755922105L;
	
	private static final Icon leftIcon = new ImageIcon(ProgressTable.class.getResource("/icons/left.png"));
	private static final Icon rightIcon = new ImageIcon(ProgressTable.class.getResource("/icons/right.png"));

	private final Color transparent = new Color(0, 0, 0, 0);
	private final JPanel panel;
	private JLabel direction;
	private boolean directVal;

	public DirectionColumn() {
		super();

		direction = new JLabel();
		direction.setPreferredSize(new Dimension(30, 30));
		direction.setHorizontalAlignment(SwingConstants.CENTER);
		direction.setVerticalAlignment(SwingConstants.CENTER);

		panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder());
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(direction, BorderLayout.CENTER);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		directVal = (boolean) value;
		panel.setBackground(isSelected ? table.getSelectionBackground() : transparent);
		direction.setIcon(directVal ? rightIcon : leftIcon);
		return panel;
	}

	@Override
	public Object getCellEditorValue() {
		return directVal;
	}
}
