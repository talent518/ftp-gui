package com.talent518.ftp.gui.table.column;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.EventObject;
import java.util.ResourceBundle;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.talent518.ftp.dao.Settings;

public class StatusColumn extends AbstractCellEditor implements TableCellRenderer {
	private static final long serialVersionUID = 4160637907563033833L;

	private final ResourceBundle language = Settings.language();
	private final Color transparent = new Color(0, 0, 0, 0);
	// @formatter:off
		private final String[] statusTexts = {
			language.getString("status.running"),
			language.getString("status.ready"),
			language.getString("status.completed"),
			language.getString("status.error")
		};
		// @formatter:on
	private final Color[] colors = { new Color(0x999999), new Color(0x333333), new Color(0x339933), new Color(0xcc3300) };
	private final JPanel panel;
	private final JLabel status;

	public StatusColumn() {
		super();

		status = new JLabel();

		panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(status, BorderLayout.CENTER);
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return super.shouldSelectCell(anEvent);
	}

	@Override
	public Object getCellEditorValue() {
		return status.getText();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		panel.setBackground(isSelected ? table.getSelectionBackground() : transparent);
		status.setForeground(isSelected ? table.getSelectionForeground() : colors[(int) value]);
		status.setText(statusTexts[(int) value]);
		return panel;
	}
}
