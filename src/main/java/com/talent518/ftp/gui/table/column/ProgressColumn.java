package com.talent518.ftp.gui.table.column;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressColumn extends AbstractCellEditor implements TableCellRenderer {
	private static final long serialVersionUID = 1269871617755922105L;

	private JProgressBar progressBar;

	public ProgressColumn() {
		super();

		progressBar = new JProgressBar();
		progressBar.setMaximum(100);
		progressBar.setStringPainted(true);
		progressBar.setBorderPainted(false);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		progressBar.setValue(Integer.parseInt(value.toString()));
		return progressBar;
	}

	@Override
	public Object getCellEditorValue() {
		return progressBar.getValue();
	}
}
