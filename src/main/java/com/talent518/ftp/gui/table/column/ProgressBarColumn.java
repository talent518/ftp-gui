package com.talent518.ftp.gui.table.column;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class ProgressBarColumn extends AbstractCellEditor implements TableCellRenderer {
	private static final long serialVersionUID = 1269871617755922105L;

	public static void setTableColumn(JTable table, int column) {
		ProgressBarColumn progressBarColumn = new ProgressBarColumn().setTable(table).setColumn(column);

		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer(progressBarColumn);
	}

	private JProgressBar render;
	private JTable table;
	int column;

	public ProgressBarColumn() {
		super();

		render = new JProgressBar();
		render.setMaximum(100);
		render.setBackground(Color.BLUE);
		render.setForeground(Color.LIGHT_GRAY);
		render.setStringPainted(true);
		render.setBorderPainted(false);
	}

	public JTable getTable() {
		return table;
	}

	public ProgressBarColumn setTable(JTable table) {
		this.table = table;

		return this;
	}

	public int getColumn() {
		return column;
	}

	public ProgressBarColumn setColumn(int column) {
		this.column = column;
		return this;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		render.setValue(Integer.parseInt(value.toString()));
		return render;
	}

	@Override
	public Object getCellEditorValue() {
		return render.getValue();
	}
}