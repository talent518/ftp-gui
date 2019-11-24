package com.talent518.ftp.gui.table.column;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class ButtonColumn extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener {
	private static final long serialVersionUID = 1439785361725510521L;

	public static void setTableColumn(JTable table, int column) {
		ButtonColumn buttonColumn = new ButtonColumn().setTable(table).setColumn(column);
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(column).setCellRenderer(buttonColumn);
		columnModel.getColumn(column).setCellEditor(buttonColumn);
	}

	// 按钮的两种状态
	private JButton rb, eb;
	private int row;
	private JTable table;
	private int column;

	public ButtonColumn() {
		super();
		System.out.println(getClass().getName());
		rb = new JButton("开始R");
		rb.addActionListener(this);
		eb = new JButton("开始E");
		eb.setFocusPainted(false);
		eb.addActionListener(this);
	}

	public JTable getTable() {
		return table;
	}

	public ButtonColumn setTable(JTable table) {
		this.table = table;
		return this;
	}

	public int getColumn() {
		return column;
	}

	public ButtonColumn setColumn(int column) {
		this.column = column;
		return this;
	}

	@Override
	public Object getCellEditorValue() {
		return null;
	}

	// 监听器方法
	@Override
	public void actionPerformed(ActionEvent arg0) {
		int v = Integer.parseInt(table.getValueAt(row, 3).toString());
		System.out.println("row :" + row + " bar value :" + table.getValueAt(row, 3));
		// 更新进度条 列的值
		table.setValueAt(v + 5, row, 3);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		System.out.println(value);
		this.row = row;
		return rb;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		System.out.println(value);
		this.row = row;
		return eb;
	}
}