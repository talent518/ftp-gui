package com.talent518.ftp.gui.table;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.table.column.DirectionColumn;
import com.talent518.ftp.gui.table.column.ProgressColumn;
import com.talent518.ftp.gui.table.column.SizeColumn;
import com.talent518.ftp.gui.table.column.StatusColumn;
import com.talent518.ftp.gui.table.column.StringColumn;
import com.talent518.ftp.gui.table.column.TypeColumn;
import com.talent518.ftp.util.PinyinUtil;

public class ProgressTable extends JPanel {
	private static final long serialVersionUID = -1789896671155598722L;
	private static final Logger log = Logger.getLogger(ProgressTable.class);

	private final ResourceBundle language = Settings.language();

	private JTable table;
	private Model model;
	private TableRowSorter rowSorter;
	private JScrollPane scrollPane;
	private Listener listener = null;
	private boolean isProgress;

	public ProgressTable(boolean isProgress) {
		this.isProgress = isProgress;
		
		model = new Model();
		table = new JTable(model);
		rowSorter = new TableRowSorter();
		scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		table.setRowSorter(rowSorter);
		table.setRowHeight(30);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.setColumnSelectionAllowed(false);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (listener != null) {
					try {
						listener.selectedRow(isProgress, table.getSelectedRow(), table.getSelectedRow() >= 0 ? getList().get(table.getSelectedRow()) : null);
					} catch (ArrayIndexOutOfBoundsException e2) {
					}
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && listener != null) {
					try {
						listener.doubleClicked(isProgress, table.getSelectedRow(), table.getSelectedRow() >=0 ? getList().get(table.getSelectedRow()) : null);
					} catch (ArrayIndexOutOfBoundsException e2) {
					}
				} else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && listener != null) {
					int i = table.rowAtPoint(e.getPoint());
					table.addRowSelectionInterval(i, i);
					listener.rightClicked(isProgress, i, i >= 0 && i < getList().size() ? getList().get(table.getSelectedRow()) : null, e);
				}
			}
		});

		TableColumn tableColumn;

		// site column(0)
		tableColumn = table.getColumnModel().getColumn(0);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(100);
		tableColumn.setCellRenderer(new StringColumn());

		// local column(1)
		tableColumn = table.getColumnModel().getColumn(1);
		tableColumn.setCellRenderer(new StringColumn());

		// direction column(2)
		tableColumn = table.getColumnModel().getColumn(2);
		tableColumn.setMinWidth(35);
		tableColumn.setMaxWidth(Integer.valueOf(language.getString("direction.size")));
		tableColumn.setCellRenderer(new DirectionColumn());

		// remote column(3)
		tableColumn = table.getColumnModel().getColumn(3);
		tableColumn.setCellRenderer(new StringColumn());

		// type column(4)
		tableColumn = table.getColumnModel().getColumn(4);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(Integer.valueOf(language.getString("type.size")));
		tableColumn.setCellRenderer(new TypeColumn());

		// size column(5)
		tableColumn = table.getColumnModel().getColumn(5);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(70);
		tableColumn.setCellRenderer(new SizeColumn());

		// progress column(6)
		tableColumn = table.getColumnModel().getColumn(6);
		tableColumn.setMinWidth(100);
		tableColumn.setMaxWidth(100);
		tableColumn.setCellRenderer(new ProgressColumn());

		// status column(7)
		tableColumn = table.getColumnModel().getColumn(7);
		tableColumn.setMinWidth(60);
		tableColumn.setMaxWidth(Integer.valueOf(language.getString("status.size")));
		tableColumn.setCellRenderer(new StatusColumn());

		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout(0, 0));
		add(table.getTableHeader(), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		scrollPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && listener != null) {
					listener.rightClicked(isProgress, -1, null, e);
				}
			}
		});
	}

	public JTable getTable() {
		return table;
	}

	public Model getModel() {
		return model;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setListener(Listener l) {
		listener = l;
	}

	public void fireTableDataChanged() {
		EventQueue.invokeLater(() -> {
			model.fireTableDataChanged();
		});
	}
	
	public void load() {
		load(isProgress ? "progress" : "processed");
	}

	public void load(String name) {
		load(new File(Settings.ROOT_PATH + File.separator + name + ".json"));
	}

	public void load(File f) {
		FileReader reader = null;
		try {
			reader = new FileReader(f);
			List<Row> list = Settings.gson().fromJson(reader, new TypeToken<List<Row>>() {
			}.getType());
			if (list != null)
				setList(list);
			log.info("Load " + f.getName() + " success");
		} catch (Throwable t) {
			log.error("Load " + f.getName() + " failure", t);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable t) {
					log.error("Close " + f.getName() + " reader failure", t);
				}
			}
		}
	}

	public void save() {
		save(isProgress ? "progress" : "processed");
	}
	
	public void save(String name) {
		save(new File(Settings.ROOT_PATH + File.separator + name + ".json"));
	}

	public void save(File f) {
		FileWriter writer = null;
		try {
			File path = f.getParentFile();
			if (!path.isDirectory()) {
				path.mkdir();
			}
			writer = new FileWriter(f);
			writer.write(Settings.gson().toJson(getList()));
			writer.flush();
			log.info("Save " + f.getName() + " success");
		} catch (Throwable t) {
			log.error("Save " + f.getName() + " failure", t);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException t) {
					log.error("Close " + f.getName() + " writer failure", t);
				}
			}
		}
	}

	public void clear() {
		synchronized (getList()) {
			getList().clear();
		}
	}

	public void clear(boolean changed) {
		synchronized (getList()) {
			getList().clear();
		}
		if (changed)
			fireTableDataChanged();
	}

	public List<Row> getList() {
		return model.getList();
	}

	public void setList(List<Row> list) {
		EventQueue.invokeLater(() -> {
			synchronized (getList()) {
				getList().clear();
				getList().addAll(list);
			}
			model.fireTableDataChanged();
		});
	}

	private static final Comparator<Row> siteComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			return PinyinUtil.compareTo(o1.getSite(), o2.getSite());
		}
	};
	private static final Comparator<Row> localComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			return PinyinUtil.compareTo(o1.getLocal(), o2.getLocal());
		}
	};
	private static final Comparator<Row> directionComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			return Boolean.compare(o1.isDirection(), o2.isDirection());
		}
	};
	private static final Comparator<Row> remoteComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			return PinyinUtil.compareTo(o1.getRemote(), o2.getRemote());
		}
	};
	private static final Comparator<Row> typeComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			return PinyinUtil.compareTo(o1.getType(), o2.getType());
		}
	};
	private static final Comparator<Row> sizeComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			return Long.compare(o1.getSize(), o2.getSize());
		}
	};
	private static final Comparator<Row> progressComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			return Integer.compare(o1.getProgress(), o2.getProgress());
		}
	};
	private static final Comparator<Row> statusComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			return Integer.compare(o1.getStatus(), o2.getStatus());
		}
	};
	@SuppressWarnings("unchecked")
	private static final Comparator<Row>[] comparators = new Comparator[] { siteComparator, localComparator, directionComparator, remoteComparator, typeComparator, sizeComparator, progressComparator, statusComparator };

	public class TableRowSorter extends RowSorter<Model> implements Comparator<Row> {
		private List<SortKey> sortKeys = Collections.emptyList();
		private SortKey sortKey;
		private Comparator<Row> comparator;
		private int nSort;

		public TableRowSorter() {
			super();

			toggleSortOrder(7);
		}

		@Override
		public Model getModel() {
			return model;
		}

		@Override
		public int compare(Row o1, Row o2) {
			return comparator.compare(o1, o2) * nSort;
		}

		public void sort() {
			sortKey = sortKeys.get(0);
			comparator = comparators[sortKey.getColumn()];
			nSort = sortKey.getSortOrder().equals(SortOrder.DESCENDING) ? -1 : 1;

			synchronized (getList()) {
				getList().sort(this);
				fireSortOrderChanged();
			}
		}

		private SortKey toggle(SortKey key) {
			if (key.getSortOrder() == SortOrder.ASCENDING) {
				return new SortKey(key.getColumn(), SortOrder.DESCENDING);
			}
			return new SortKey(key.getColumn(), SortOrder.ASCENDING);
		}

		@Override
		public void toggleSortOrder(int column) {
			List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
			SortKey sortKey;
			int sortIndex;
			for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
				if (keys.get(sortIndex).getColumn() == column) {
					break;
				}
			}
			if (sortIndex == -1) {
				// Key doesn't exist
				sortKey = new SortKey(column, SortOrder.ASCENDING);
				keys.add(0, sortKey);
			} else if (sortIndex == 0) {
				// It's the primary sorting key, toggle it
				keys.set(0, toggle(keys.get(0)));
			} else {
				// It's not the first, but was sorted on, remove old
				// entry, insert as first with ascending.
				keys.remove(sortIndex);
				keys.add(0, new SortKey(column, SortOrder.ASCENDING));
			}
			if (keys.size() > 1) {
				keys = keys.subList(0, 1);
			}
			setSortKeys(keys);
		}

		@Override
		public int convertRowIndexToModel(int index) {
			return index;
		}

		@Override
		public int convertRowIndexToView(int index) {
			return index;
		}

		@Override
		public void setSortKeys(List<? extends SortKey> keys) {
			List<SortKey> old = sortKeys;
			if (keys != null && keys.size() > 0) {
				int max = model.getColumnCount();
				for (SortKey key : keys) {
					if (key == null || key.getColumn() < 0 || key.getColumn() >= max) {
						throw new IllegalArgumentException("Invalid SortKey");
					}
				}
				sortKeys = Collections.unmodifiableList(new ArrayList<SortKey>(keys));
			} else {
				sortKeys = Collections.emptyList();
			}
			if (!sortKeys.equals(old)) {
				sort();
			}
		}

		@Override
		public List<? extends SortKey> getSortKeys() {
			return sortKeys;
		}

		@Override
		public int getViewRowCount() {
			return model.getRowCount();
		}

		@Override
		public int getModelRowCount() {
			return model.getRowCount();
		}

		@Override
		public void modelStructureChanged() {
		}

		@Override
		public void allRowsChanged() {
			sort();
		}

		@Override
		public void rowsInserted(int firstRow, int endRow) {
		}

		@Override
		public void rowsDeleted(int firstRow, int endRow) {
		}

		@Override
		public void rowsUpdated(int firstRow, int endRow) {
		}

		@Override
		public void rowsUpdated(int firstRow, int endRow, int column) {
		}
	}

	public interface Listener {
		public void rightClicked(boolean isProgress, int i, Row r, MouseEvent e);

		public void selectedRow(boolean isProgress, int i, Row r);

		public void doubleClicked(boolean isProgress, int i, Row r);
	}

	public class Model extends AbstractTableModel {
		private static final long serialVersionUID = -1994280421860518219L;

		private Class<?>[] cellType = { String.class, String.class, Boolean.class, String.class, String.class, Long.class, Integer.class, Integer.class };
		// @formatter:off
		private String title[] = {
			language.getString("progressTable.site"),
			language.getString("progressTable.local"),
			language.getString("progressTable.direction"),
			language.getString("progressTable.remote"),
			language.getString("progressTable.type"),
			language.getString("progressTable.size"),
			language.getString("progressTable.progress"),
			language.getString("progressTable.status")
		};
		// @formatter:on
		private List<Row> list = new ArrayList<Row>();
		private final int N = 0;
		private Row err = new Row();

		public Model() {
			err.setSite("default");
			err.setLocal("/home/abao");
			err.setDirection(Math.random() <= 0.5f);
			err.setType(Math.random() <= 0.5f ? "DIR" : "REG");
			err.setRemote("/home/abao");
			err.setProgress((int) (Math.random() * 101));
			err.setSize((long) (Math.random() * 100000000));
			err.setStatus((int) (Math.random() * 4));

			for (int i = 0; i < N; i++) {
				Row r = new Row();
				r.setSite("default");
				r.setLocal("/home/abao");
				r.setDirection(Math.random() <= 0.5f);
				r.setType(Math.random() <= 0.5f ? "DIR" : "REG");
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
			try {
				return list.get(r).get(c);
			} catch (IndexOutOfBoundsException e) {
				log.error("progress table getValueAt error", e);
				return err.get(c);
			}
		}

		@Override
		public boolean isCellEditable(int r, int c) {
			return false;
		}

		@Override
		public void setValueAt(Object value, int r, int c) {
			try {
				list.get(r).set(c, value);
				fireTableCellUpdated(r, c);
			} catch (IndexOutOfBoundsException e) {
				log.error("progress table setValueAt error", e);
			}
		}
	}

	public static class Row {
		public static final int STATUS_READY = 1;
		public static final int STATUS_RUNNING = 0;
		public static final int STATUS_COMPLETED = 2;
		public static final int STATUS_ERROR = 3;

		private String site;
		private String local;
		private boolean direction;
		private String remote;
		private String type;
		private long size;
		private int progress;
		private int status;

		private long written;

		public Row() {
			super();
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
					o = type;
					break;
				case 5:
					o = size;
					break;
				case 6:
					o = progress;
					break;
				case 7:
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
					type = (String) value;
					break;
				case 5:
					size = (long) value;
					break;
				case 6:
					progress = (int) value;
					break;
				case 7:
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

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public int getProgress() {
			return progress;
		}

		public void setProgress(int progress) {
			this.progress = progress;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
			if (status == STATUS_COMPLETED)
				this.progress = 100;
		}

		public long getWritten() {
			return written;
		}

		public void setWritten(long written) {
			this.written = written;

			if (this.written == this.size || this.size == 0)
				this.progress = 100;
			else
				this.progress = (int) ((double) this.written / (double) this.size * 100.0f);
		}

		public void addWritten(long written) {
			setWritten(this.written + written);
		}

		@Override
		public String toString() {
			return Settings.gson().toJson(this);
		}
	}
}