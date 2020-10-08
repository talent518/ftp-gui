package com.talent518.ftp.gui.table;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.table.column.NameColumn;
import com.talent518.ftp.gui.table.column.SizeColumn;
import com.talent518.ftp.gui.table.column.StringColumn;
import com.talent518.ftp.gui.table.column.TypeColumn;
import com.talent518.ftp.util.PinyinUtil;

public class FileTable extends JPanel {
	private static final long serialVersionUID = -1789896671155598722L;
	private static final Logger log = Logger.getLogger(FileTable.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private final ResourceBundle language = Settings.language();

	private JTable table;
	private Model model;
	private boolean isLocal;
	private JTextField addr;
	private Listener listener = null;
	private TableRowSorter rowSorter = null;
	private String currentPath = null;
	private JScrollPane scrollPane;

	public FileTable(String label) {
		this(label, false);
	}

	public FileTable(String label, boolean value) {
		super();

		isLocal = value;
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
						listener.selectedRow(isLocal, table.getSelectedRow(), table.getSelectedRow() >= 0 ? model.getList().get(table.getSelectedRow()) : null);
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
						listener.doubleClicked(isLocal, table.getSelectedRow(), model.getList().get(table.getSelectedRow()));
					} catch (ArrayIndexOutOfBoundsException e2) {
					}
				} else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && listener != null) {
					int i = table.rowAtPoint(e.getPoint());
					table.addRowSelectionInterval(i, i);
					listener.rightClicked(isLocal, i, i >= 0 && i < model.getList().size() ? model.getList().get(table.getSelectedRow()) : null, e);
				}
			}
		});

		TableColumn tableColumn;

		// name column(0)
		tableColumn = table.getColumnModel().getColumn(0);
		tableColumn.setCellRenderer(new NameColumn(2));

		// size column(1)
		tableColumn = table.getColumnModel().getColumn(1);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(60);
		tableColumn.setCellRenderer(new SizeColumn());

		// type column(2)
		tableColumn = table.getColumnModel().getColumn(2);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(Integer.valueOf(language.getString("type.size")));
		tableColumn.setCellRenderer(new TypeColumn());

		// mtime column(3)
		tableColumn = table.getColumnModel().getColumn(3);
		tableColumn.setMinWidth(100);
		tableColumn.setMaxWidth(145);
		tableColumn.setCellRenderer(new StringColumn());

		if (!isLocal) {
			// perms column(4)
			tableColumn = table.getColumnModel().getColumn(4);
			tableColumn.setMinWidth(50);
			tableColumn.setMaxWidth(82);
			tableColumn.setCellRenderer(new StringColumn());

			// uid column(5)
			tableColumn = table.getColumnModel().getColumn(5);
			tableColumn.setMinWidth(50);
			tableColumn.setMaxWidth(50);
			tableColumn.setCellRenderer(new StringColumn());

			// gid column(6)
			tableColumn = table.getColumnModel().getColumn(6);
			tableColumn.setMinWidth(50);
			tableColumn.setMaxWidth(50);
			tableColumn.setCellRenderer(new StringColumn());
		}

		addr = new JTextField();
		addr.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && listener != null) {
					listener.enterAddr(isLocal, getRealPath(addr.getText()));
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		JButton btn = new JButton(language.getString("enter"));
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (listener != null) {
					listener.enterAddr(isLocal, getRealPath(addr.getText()));
				}
			}
		});

		JPanel panelNav = new JPanel();
		panelNav.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		panelNav.setLayout(new BorderLayout(5, 5));
		panelNav.add(new JLabel(label), BorderLayout.WEST);
		panelNav.add(addr, BorderLayout.CENTER);
		panelNav.add(btn, BorderLayout.EAST);

		JPanel panelTable = new JPanel();
		panelTable.setBorder(BorderFactory.createEmptyBorder());
		panelTable.setLayout(new BorderLayout(5, 5));
		panelTable.add(table.getTableHeader(), BorderLayout.NORTH);
		panelTable.add(scrollPane, BorderLayout.CENTER);

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));
		add(panelNav, BorderLayout.NORTH);
		add(panelTable, BorderLayout.CENTER);
		scrollPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && listener != null) {
					listener.rightClicked(isLocal, -1, null, e);
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

	public String getAddr() {
		return addr.getText();
	}

	public void setAddr(String text) {
		if (listener != null) {
			listener.enterAddr(isLocal, getRealPath(text));
		} else {
			addr.setText(getRealPath(text));
		}
	}

	public void setAddrText(String text) {
		currentPath = text;
		addr.setText(text);
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public String getParentPath() {
		if (isLocal) {
			return new File(addr.getText()).getParent();
		} else {
			int i = addr.getText().lastIndexOf('/');
			if (i > 0)
				return addr.getText().substring(0, i);
			else
				return "/";
		}
	}

	public String getPath(String name) {
		if (isLocal) {
			return addr.getText().replaceAll("[\\/]+$", "") + File.separator + name;
		} else {
			return addr.getText().replaceAll("[/]+$", "") + '/' + name;
		}
	}

	private String getRealPath(String path) {
		if (path == null || path.length() == 0 || path.equals(".")) {
			return isLocal ? System.getProperty("user.dir") : addr.getText();
		}

		char separatorChar;
		if (isLocal) {
			separatorChar = File.separatorChar;
			String resolve = currentPath != null ? currentPath : System.getProperty("user.dir");
			path = Paths.get(resolve).resolve(path).toAbsolutePath().toString();
		} else {
			separatorChar = '/';
			if (!path.startsWith("/"))
				path = (currentPath != null ? currentPath : "") + '/' + path;
		}

		String separator = (separatorChar == '\\' ? "\\\\" : "" + separatorChar);

		path = path.replaceAll(separator + "\\.$", "");
		path = path.replaceAll(separator + "\\." + separator, "" + separatorChar);

		if (path.contains("..")) {
			path = path.replaceAll(separator + "[^" + separator + "]+" + separator + "\\.\\.$", "" + separatorChar);
			path = path.replaceAll(separator + "[^" + separator + "]+" + separator + "\\.\\." + separator, "" + separatorChar);
			path = path.replaceAll(separator + "\\.\\.$", "");
			path = path.replaceAll(separator + "\\.\\." + separator, "" + separatorChar);
		}

		return path;
	}

	public void setListener(Listener l) {
		listener = l;
	}

	public void setList(List<Row> list) {
		synchronized (model.getList()) {
			model.getList().clear();
			if ((isLocal && getAddr().length() > 1) || (!isLocal && !"/".equals(getAddr()))) {
				model.getList().add(new Row());
			}
			model.getList().addAll(list);
			EventQueue.invokeLater(() -> {
				model.fireTableDataChanged();
				if (Settings.instance().isScrollTop()) {
					scrollPane.getVerticalScrollBar().setValue(0);
				}
			});
		}
	}

	private static int firstCompare(Row o1, Row o2) {
		boolean d1 = "DIR".equals(o1.getType());
		boolean d2 = "DIR".equals(o2.getType());

		if (d1 && !d2)
			return -1;
		else if (!d1 && d2)
			return 1;

		boolean l1 = "LNK".equals(o1.getType());
		boolean l2 = "LNK".equals(o2.getType());

		if (l1 && !l2)
			return -1;
		else if (!l1 && l2)
			return 1;

		if (o1.isDir() && !o2.isDir())
			return -1;
		else if (!o1.isDir() && o2.isDir())
			return 1;
		else
			return 0;
	}

	private static final Comparator<Row> nameComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			int c = firstCompare(o1, o2);
			if (c != 0)
				return c;

			return PinyinUtil.compareTo(o1.getName(), o2.getName());
		}
	};
	private static final Comparator<Row> sizeComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			int c = firstCompare(o1, o2);
			if (c != 0)
				return c;

			return Long.compare(o1.getSize(), o2.getSize());
		}
	};
	private static final Comparator<Row> typeComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			return o1.getType().compareTo(o2.getType());
		}
	};
	private static final Comparator<Row> mtimeComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			int c = firstCompare(o1, o2);
			if (c != 0)
				return c;

			return o1.getMtime().compareTo(o2.getMtime());
		}
	};
	private static final Comparator<Row> permsComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			int c = firstCompare(o1, o2);
			if (c != 0)
				return c;

			return o1.getPerms().compareTo(o2.getPerms());
		}
	};
	private static final Comparator<Row> uidComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			int c = firstCompare(o1, o2);
			if (c != 0)
				return c;

			return Integer.compare(o1.getUid(), o2.getUid());
		}
	};
	private static final Comparator<Row> gidComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			int c = firstCompare(o1, o2);
			if (c != 0)
				return c;

			return Integer.compare(o1.getGid(), o2.getGid());
		}
	};
	@SuppressWarnings("unchecked")
	private static final Comparator<Row>[] comparators = new Comparator[] { nameComparator, sizeComparator, typeComparator, mtimeComparator, permsComparator, uidComparator, gidComparator };

	public class TableRowSorter extends RowSorter<Model> implements Comparator<Row> {
		private List<SortKey> sortKeys = Collections.emptyList();
		private SortKey sortKey;
		private Comparator<Row> comparator;
		private int nSort;

		public TableRowSorter() {
			super();

			toggleSortOrder(0);
		}

		@Override
		public Model getModel() {
			return model;
		}

		@Override
		public int compare(Row o1, Row o2) {
			if (o1.isUp())
				return -1;
			if (o2.isUp())
				return 1;
			return comparator.compare(o1, o2) * nSort;
		}

		public void sort() {
			sortKey = sortKeys.get(0);
			comparator = comparators[sortKey.getColumn()];
			nSort = sortKey.getSortOrder().equals(SortOrder.DESCENDING) ? -1 : 1;

			synchronized (model.getList()) {
				model.getList().sort(this);
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
		public void enterAddr(boolean local, String addr);

		public void rightClicked(boolean isLocal, int i, Row r, MouseEvent e);

		public void selectedRow(boolean local, int i, Row r);

		public void doubleClicked(boolean local, int i, Row r);
	}

	public class Model extends AbstractTableModel {
		private static final long serialVersionUID = -1994280421860518219L;

		private Class<?>[] cellType = { String.class, Long.class, String.class, String.class, String.class, Integer.class, Integer.class };
		// @formatter:off
		private String title[] = {
			language.getString("fileTable.name"),
			language.getString("fileTable.size"),
			language.getString("fileTable.type"),
			language.getString("fileTable.mtime"),
			language.getString("fileTable.perms"),
			language.getString("fileTable.uid"),
			language.getString("fileTable.gid")
		};
		// @formatter:on
		private List<Row> list = new ArrayList<Row>();
		private final Row err = new Row();

		public Model() {
			err.setName("index out");
			err.setType("DIR");
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
			return isLocal ? 4 : title.length;
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
				log.error("file table getValueAt error", e);
				return err.get(c);
			}
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

	public static class Row {
		private String name;
		private long size;
		private String type;
		private String mtime = "";
		private String perms = "----------";
		private int uid;
		private int gid;
		private boolean isUp;
		private boolean isDir;

		public Row() {
			name = "..";
			type = "DIR";
			isUp = true;
			isDir = true;
		}

		public Row(File f) {
			name = f.getName();
			size = f.length();

			if (Files.isSymbolicLink(f.toPath())) {
				type = "LNK";
				isDir = f.isDirectory();
			} else if (f.isDirectory()) {
				type = "DIR";
				isDir = true;
			} else if (f.isFile()) {
				type = "REG";
			}

			mtime = dateFormat.format(f.lastModified());
		}

		public Row(FTPFile f) {
			name = f.getName();
			size = f.getSize();

			type = "Unknown";
			if (f.isDirectory()) {
				type = "DIR";
				isDir = true;
			} else if (f.isFile()) {
				type = "REG";
			} else if (f.isSymbolicLink()) {
				type = "LNK";
			}

			mtime = dateFormat.format(f.getTimestamp().getTimeInMillis());

			StringBuilder sb = new StringBuilder();
			sb.append(formatType(f.getType()));
			sb.append(permissionToString(f, FTPFile.USER_ACCESS));
			sb.append(permissionToString(f, FTPFile.GROUP_ACCESS));
			sb.append(permissionToString(f, FTPFile.WORLD_ACCESS));
			perms = sb.toString();
			try {
				uid = Integer.parseInt(f.getUser());
				gid = Integer.parseInt(f.getGroup());
			} catch(NumberFormatException e) {
				uid = -1;
				gid = -1;
			}
		}

		public Row(LsEntry entry) {
			SftpATTRS attrs = entry.getAttrs();

			type = "Unknown";
			if (attrs.isDir()) {
				type = "DIR";
				isDir = true;
			} else if (attrs.isReg()) {
				type = "REG";
			} else if (attrs.isLink()) {
				type = "LNK";
			} else if (attrs.isBlk()) {
				type = "BLK";
			} else if (attrs.isChr()) {
				type = "CHR";
			} else if (attrs.isFifo()) {
				type = "FIFO";
			} else if (attrs.isSock()) {
				type = "SOCK";
			}

			name = entry.getFilename();
			perms = attrs.getPermissionsString();
			mtime = dateFormat.format(new Date((long) (attrs.getMTime()) * 1000));
			size = attrs.getSize();
			uid = attrs.getUId();
			gid = attrs.getGId();
		}

		private char formatType(int _type) {
			switch (_type) {
				case FTPFile.FILE_TYPE:
					return '-';
				case FTPFile.DIRECTORY_TYPE:
					return 'd';
				case FTPFile.SYMBOLIC_LINK_TYPE:
					return 'l';
				default:
					return '?';
			}
		}

		private String permissionToString(FTPFile f, int access) {
			StringBuilder sb = new StringBuilder();
			if (f.hasPermission(access, FTPFile.READ_PERMISSION)) {
				sb.append('r');
			} else {
				sb.append('-');
			}
			if (f.hasPermission(access, FTPFile.WRITE_PERMISSION)) {
				sb.append('w');
			} else {
				sb.append('-');
			}
			if (f.hasPermission(access, FTPFile.EXECUTE_PERMISSION)) {
				sb.append('x');
			} else {
				sb.append('-');
			}
			return sb.toString();
		}

		public Object get(int c) {
			Object o = null;
			switch (c) {
				case 0:
					o = name;
					break;
				case 1:
					o = size;
					break;
				case 2:
					if (isDir && "LNK".equals(type))
						o = "DL";
					else
						o = type;
					break;
				case 3:
					o = mtime;
					break;
				case 4:
					o = perms;
					break;
				case 5:
					o = uid;
					break;
				case 6:
					o = gid;
					break;
			}
			return o;
		}

		public void set(int c, Object value) {
			switch (c) {
				case 0:
					name = (String) value;
					break;
				case 1:
					size = (long) value;
					break;
				case 2:
					type = (String) value;
					break;
				case 3:
					mtime = (String) value;
					break;
				case 4:
					perms = (String) value;
					break;
				case 5:
					uid = (int) value;
					break;
				case 6:
					gid = (int) value;
					break;
			}
		}

		public boolean isDir() {
			return isDir;
		}

		public boolean isUp() {
			return isUp;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public long getSize() {
			return size;
		}

		public void setSize(long size) {
			this.size = size;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getMtime() {
			return mtime;
		}

		public void setMtime(String mtime) {
			this.mtime = mtime;
		}

		public String getPerms() {
			return perms;
		}

		public void setPerms(String perms) {
			this.perms = perms;
		}

		public int getUid() {
			return uid;
		}

		public void setUid(int uid) {
			this.uid = uid;
		}

		public int getGid() {
			return gid;
		}

		public void setGid(int gid) {
			this.gid = gid;
		}

		@Override
		public String toString() {
			return Settings.gson().toJson(this);
		}
	}
}