package com.talent518.ftp.gui.table;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.table.column.ProgressBarColumn;

public class ProgressTable extends JPanel {
	private static final long serialVersionUID = -1789896671155598722L;

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(600, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Custom JTable");
		frame.setContentPane(new ProgressTable());
		frame.setVisible(true);
	}

	private final ResourceBundle lauguage = Settings.language();
	
	private JTable table;
	private Model model;

	public ProgressTable() {
		model = new Model();
		table = new JTable(model);

		table.setRowHeight(30);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.setColumnSelectionAllowed(false);

		ProgressBarColumn.setTableColumn(table, 3);

		TableColumn tableColumn;

		tableColumn = table.getColumnModel().getColumn(1);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(40);

		tableColumn = table.getColumnModel().getColumn(3);
		tableColumn.setMinWidth(100);
		tableColumn.setMaxWidth(100);

		tableColumn = table.getColumnModel().getColumn(4);
		tableColumn.setMinWidth(100);
		tableColumn.setMaxWidth(100);

		tableColumn = table.getColumnModel().getColumn(5);
		tableColumn.setMinWidth(60);
		tableColumn.setMaxWidth(60);

		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout(0, 0));
		add(table.getTableHeader(), BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	public JTable getTable() {
		return table;
	}

	public Model getModel() {
		return model;
	}

	public void clear() {
		model.getList().clear();
	}

	public List<Row> getList() {
		return model.getList();
	}

	public void fireTableDataChanged() {
		model.fireTableDataChanged();
	}

	public class Model extends AbstractTableModel {
		private static final long serialVersionUID = -1994280421860518219L;

		private Class<?>[] cellType = { String.class, Boolean.class, String.class, Integer.class, Long.class, Integer.class };
		// @formatter:off
		private String title[] = {
			lauguage.getString("progressTable.local"),
			lauguage.getString("progressTable.direction"),
			lauguage.getString("progressTable.remote"),
			lauguage.getString("progressTable.progress"),
			lauguage.getString("progressTable.size"),
			lauguage.getString("progressTable.status")
		};
		// @formatter:on
		private List<Row> list = new ArrayList<Row>();

		public Model() {
		}

		public List<Row> getList() {
			return list;
		}

		@Override
		public Class<?> getColumnClass(int i) {
			return cellType[i];
		}

		@Override
		public String getColumnName(int i) {
			return title[i];
		}

		@Override
		public int getColumnCount() {
			return title.length;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int r, int c) {
			return list.get(r).get(c);
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return false;
		}

		@Override
		public void setValueAt(Object value, int r, int c) {
			list.get(r).set(c, value);
			fireTableCellUpdated(r, c);
		}
	}

	public class Row {
		private String local;
		private boolean direction;
		private String remote;
		private int progress;
		private long size;
		private int status;

		public Object get(int c) {
			Object o = null;
			switch (c) {
				case 0:
					o = local;
					break;
				case 1:
					o = direction;
					break;
				case 2:
					o = remote;
					break;
				case 3:
					o = progress;
					break;
				case 4:
					o = size;
					break;
				case 5:
					o = status;
					break;
			}
			return o;
		}

		public void set(int c, Object value) {
			switch (c) {
				case 0:
					local = (String) value;
					break;
				case 1:
					direction = (boolean) value;
					break;
				case 2:
					remote = (String) value;
					break;
				case 3:
					progress = (int) value;
					break;
				case 4:
					size = (long) value;
					break;
				case 5:
					status = (int) value;
					break;
			}
		}

		public String getLocal() {
			return local;
		}

		public void setLocal(String local) {
			this.local = local;
		}

		public boolean isDirection() {
			return direction;
		}

		public void setDirection(boolean direction) {
			this.direction = direction;
		}

		public String getRemote() {
			return remote;
		}

		public void setRemote(String remote) {
			this.remote = remote;
		}

		public int getProgress() {
			return progress;
		}

		public void setProgress(int progress) {
			this.progress = progress;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		@Override
		public String toString() {
			return Settings.gson().toJson(this);
		}
	}
}