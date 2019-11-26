package com.talent518.ftp.gui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.util.FileUtils;

public class ProgressTable extends JPanel {
	private static final long serialVersionUID = -1789896671155598722L;
	private static final Icon leftIcon = new ImageIcon(ProgressTable.class.getResource("/icons/left.png"));
	private static final Icon rightIcon = new ImageIcon(ProgressTable.class.getResource("/icons/right.png"));

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(600, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Custom JTable");
		frame.setContentPane(new ProgressTable());
		frame.setVisible(true);
	}

	private final ResourceBundle language = Settings.language();

	private JTable table;
	private Model model;

	public ProgressTable() {
		model = new Model();
		table = new JTable(model);

		table.setRowHeight(30);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.setColumnSelectionAllowed(false);

		TableColumn tableColumn;

		tableColumn = table.getColumnModel().getColumn(0);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(100);

		tableColumn = table.getColumnModel().getColumn(2);
		tableColumn.setMinWidth(35);
		tableColumn.setMaxWidth(Integer.valueOf(language.getString("direction.size")));
		tableColumn.setCellRenderer(new DirectionColumn());

		tableColumn = table.getColumnModel().getColumn(4);
		tableColumn.setMinWidth(100);
		tableColumn.setMaxWidth(100);
		tableColumn.setCellRenderer(new ProgressColumn());

		tableColumn = table.getColumnModel().getColumn(5);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(60);
		tableColumn.setCellRenderer(new SizeColumn());

		tableColumn = table.getColumnModel().getColumn(6);
		tableColumn.setMinWidth(60);
		tableColumn.setMaxWidth(Integer.valueOf(language.getString("status.size")));
		tableColumn.setCellRenderer(new StatusColumn());

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

	public class DirectionColumn extends AbstractCellEditor implements TableCellRenderer {
		private static final long serialVersionUID = 1269871617755922105L;

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
			panel.setBackground(isSelected ? getTable().getSelectionBackground() : transparent);
			direction.setIcon(directVal ? rightIcon : leftIcon);
			return panel;
		}

		@Override
		public Object getCellEditorValue() {
			return directVal;
		}
	}

	public class ProgressColumn extends AbstractCellEditor implements TableCellRenderer {
		private static final long serialVersionUID = 1269871617755922105L;

		private JProgressBar progressBar;

		public ProgressColumn() {
			super();

			progressBar = new JProgressBar();
			progressBar.setMaximum(100);
			progressBar.setBackground(getTable().getSelectionBackground());
			progressBar.setForeground(getTable().getSelectionForeground());
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

	public class SizeColumn extends AbstractCellEditor implements TableCellRenderer {
		private static final long serialVersionUID = 4160637907563033833L;

		private final Color transparent = new Color(0, 0, 0, 0);
		private final JPanel panel;
		private final JLabel size;

		public SizeColumn() {
			super();

			size = new JLabel();

			panel = new JPanel();
			panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			panel.setLayout(new BorderLayout(0, 0));
			panel.add(size, BorderLayout.CENTER);
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			return super.shouldSelectCell(anEvent);
		}

		@Override
		public Object getCellEditorValue() {
			return size.getText();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			panel.setBackground(isSelected ? getTable().getSelectionBackground() : transparent);
			size.setForeground(isSelected ? getTable().getSelectionForeground() : getTable().getForeground());
			size.setText(FileUtils.formatSize((long) value));
			return panel;
		}
	}

	public class StatusColumn extends AbstractCellEditor implements TableCellRenderer {
		private static final long serialVersionUID = 4160637907563033833L;

		private final Color transparent = new Color(0, 0, 0, 0);
		// @formatter:off
		private final String[] statusTexts = {
			language.getString("status.ready"),
			language.getString("status.running"),
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
			panel.setBackground(isSelected ? getTable().getSelectionBackground() : transparent);
			status.setForeground(isSelected ? getTable().getSelectionForeground() : colors[(int) value]);
			status.setText(statusTexts[(int) value]);
			return panel;
		}
	}

	public class Model extends AbstractTableModel {
		private static final long serialVersionUID = -1994280421860518219L;

		private Class<?>[] cellType = { String.class, String.class, Boolean.class, String.class, Integer.class, Long.class, Integer.class };
		// @formatter:off
		private String title[] = {
			language.getString("progressTable.site"),
			language.getString("progressTable.local"),
			language.getString("progressTable.direction"),
			language.getString("progressTable.remote"),
			language.getString("progressTable.progress"),
			language.getString("progressTable.size"),
			language.getString("progressTable.status")
		};
		// @formatter:on
		private List<Row> list = new ArrayList<Row>();
		private final int N = 20;

		public Model() {
			for (int i = 0; i < N; i++) {
				Row r = new Row();
				r.setSite("default");
				r.setLocal("/home/abao");
				r.setDirection(Math.random() <= 0.5f);
				r.setRemote("/home/abao");
				r.setProgress((int) (Math.random() * 101));
				r.setSize((long) (Math.random() * 100000000));
				r.setStatus((int) (Math.random() * 4));
				list.add(r);
			}
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
		private String site;
		private String local;
		private boolean direction;
		private String remote;
		private int progress;
		private long size;
		private int status;

		public Row() {
			super();
		}

		public Row(String site, String local, boolean direction, String remote, int progress, long size, int status) {
			super();
			this.site = site;
			this.local = local;
			this.direction = direction;
			this.remote = remote;
			this.progress = progress;
			this.size = size;
			this.status = status;
		}

		public Object get(int c) {
			Object o = null;
			switch (c) {
				case 0:
					o = site;
					break;
				case 1:
					o = local;
					break;
				case 2:
					o = direction;
					break;
				case 3:
					o = remote;
					break;
				case 4:
					o = progress;
					break;
				case 5:
					o = size;
					break;
				case 6:
					o = status;
					break;
			}
			return o;
		}

		public void set(int c, Object value) {
			switch (c) {
				case 0:
					site = (String) value;
					break;
				case 1:
					local = (String) value;
					break;
				case 2:
					direction = (boolean) value;
					break;
				case 3:
					remote = (String) value;
					break;
				case 4:
					progress = (int) value;
					break;
				case 5:
					size = (long) value;
					break;
				case 6:
					status = (int) value;
					break;
			}
		}

		public String getSite() {
			return site;
		}

		public void setSite(String site) {
			this.site = site;
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