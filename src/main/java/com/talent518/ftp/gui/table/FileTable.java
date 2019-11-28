package com.talent518.ftp.gui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.net.ftp.FTPFile;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.util.FileUtils;
import com.talent518.ftp.util.PinyinUtil;

public class FileTable extends JPanel {
	private static final long serialVersionUID = -1789896671155598722L;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final Icon folderIcon = new ImageIcon(FileTable.class.getResource("/icons/folder.png"));
	private static final Icon linkIcon = new ImageIcon(FileTable.class.getResource("/icons/link.png"));
	private static final Icon fileIcon = new ImageIcon(FileTable.class.getResource("/icons/file.png"));
	private static final Map<String, Icon> fileIcons = new HashMap<String, Icon>();
	static {
		// image file
		fileIcons.put("png", new ImageIcon(FileTable.class.getResource("/icons/png.png")));
		Icon jpg = new ImageIcon(FileTable.class.getResource("/icons/jpeg.png"));
		fileIcons.put("jpg", jpg);
		fileIcons.put("jpeg", jpg);
		fileIcons.put("gif", new ImageIcon(FileTable.class.getResource("/icons/gif.png")));
		fileIcons.put("bmp", new ImageIcon(FileTable.class.getResource("/icons/bmp.png")));
		fileIcons.put("gif", new ImageIcon(FileTable.class.getResource("/icons/gif.png")));
		fileIcons.put("ai", new ImageIcon(FileTable.class.getResource("/icons/ai.png")));
		fileIcons.put("psd", new ImageIcon(FileTable.class.getResource("/icons/psd.png")));
		fileIcons.put("svg", new ImageIcon(FileTable.class.getResource("/icons/svg.png")));
		fileIcons.put("cdr", new ImageIcon(FileTable.class.getResource("/icons/cdr.png")));

		// office document
		fileIcons.put("doc", new ImageIcon(FileTable.class.getResource("/icons/doc.png")));
		fileIcons.put("docx", new ImageIcon(FileTable.class.getResource("/icons/docx.png")));
		fileIcons.put("ppt", new ImageIcon(FileTable.class.getResource("/icons/ppt.png")));
		fileIcons.put("pptx", new ImageIcon(FileTable.class.getResource("/icons/pptx.png")));
		fileIcons.put("xls", new ImageIcon(FileTable.class.getResource("/icons/xls.png")));
		fileIcons.put("xlsx", new ImageIcon(FileTable.class.getResource("/icons/xlsx.png")));
		fileIcons.put("pdf", new ImageIcon(FileTable.class.getResource("/icons/pdf.png")));

		// text file
		fileIcons.put("ada", new ImageIcon(FileTable.class.getResource("/icons/ada.png")));
		fileIcons.put("html", new ImageIcon(FileTable.class.getResource("/icons/html.png")));
		fileIcons.put("php", new ImageIcon(FileTable.class.getResource("/icons/php.png")));
		fileIcons.put("txt", new ImageIcon(FileTable.class.getResource("/icons/txt.png")));
		fileIcons.put("xml", new ImageIcon(FileTable.class.getResource("/icons/xml.png")));
		fileIcons.put("java", new ImageIcon(FileTable.class.getResource("/icons/java.png")));
		fileIcons.put("class", new ImageIcon(FileTable.class.getResource("/icons/class.png")));
		fileIcons.put("js", new ImageIcon(FileTable.class.getResource("/icons/js.png")));
		fileIcons.put("css", new ImageIcon(FileTable.class.getResource("/icons/css.png")));
		fileIcons.put("json", new ImageIcon(FileTable.class.getResource("/icons/json.png")));
		fileIcons.put("c", new ImageIcon(FileTable.class.getResource("/icons/c.png")));
		fileIcons.put("cs", new ImageIcon(FileTable.class.getResource("/icons/cs.png")));
		Icon cpp = new ImageIcon(FileTable.class.getResource("/icons/cpp.png"));
		fileIcons.put("cpp", cpp);
		fileIcons.put("cxx", cpp);
		Icon h = new ImageIcon(FileTable.class.getResource("/icons/h.png"));
		fileIcons.put("h", h);
		fileIcons.put("hxx", h);
		fileIcons.put("vbs", new ImageIcon(FileTable.class.getResource("/icons/vbs.png")));
		fileIcons.put("xhtml", new ImageIcon(FileTable.class.getResource("/icons/xhtml.png")));
		fileIcons.put("py", new ImageIcon(FileTable.class.getResource("/icons/py.png")));
		fileIcons.put("rb", new ImageIcon(FileTable.class.getResource("/icons/rb.png")));
		fileIcons.put("sql", new ImageIcon(FileTable.class.getResource("/icons/sql.png")));
		fileIcons.put("vb", new ImageIcon(FileTable.class.getResource("/icons/vb.png")));
		fileIcons.put("go", new ImageIcon(FileTable.class.getResource("/icons/go.png")));
		fileIcons.put("sh", new ImageIcon(FileTable.class.getResource("/icons/sh.png")));

		// video file
		fileIcons.put("avi", new ImageIcon(FileTable.class.getResource("/icons/avi.png")));
		fileIcons.put("mp4", new ImageIcon(FileTable.class.getResource("/icons/mp4.png")));
		fileIcons.put("wmv", new ImageIcon(FileTable.class.getResource("/icons/wmv.png")));
		fileIcons.put("divx", new ImageIcon(FileTable.class.getResource("/icons/divx.png")));
		fileIcons.put("rmvb", new ImageIcon(FileTable.class.getResource("/icons/rmvb.png")));
		fileIcons.put("rm", new ImageIcon(FileTable.class.getResource("/icons/rm.png")));
		fileIcons.put("flv", new ImageIcon(FileTable.class.getResource("/icons/flv.png")));
		fileIcons.put("mov", new ImageIcon(FileTable.class.getResource("/icons/mov.png")));
		fileIcons.put("3gp", new ImageIcon(FileTable.class.getResource("/icons/3gp.png")));
		fileIcons.put("asf", new ImageIcon(FileTable.class.getResource("/icons/asf.png")));

		// audio file
		fileIcons.put("aiff", new ImageIcon(FileTable.class.getResource("/icons/aiff.png")));
		fileIcons.put("wma", new ImageIcon(FileTable.class.getResource("/icons/wma.png")));
		fileIcons.put("cda", new ImageIcon(FileTable.class.getResource("/icons/cda.png")));
		fileIcons.put("mp3", new ImageIcon(FileTable.class.getResource("/icons/mp3.png")));
		fileIcons.put("midi", new ImageIcon(FileTable.class.getResource("/icons/mp3.png")));
		fileIcons.put("ogg", new ImageIcon(FileTable.class.getResource("/icons/ogg.png")));
		fileIcons.put("flac", new ImageIcon(FileTable.class.getResource("/icons/flac.png")));

		// compress
		Icon compress = new ImageIcon(FileTable.class.getResource("/icons/compress.png"));
		fileIcons.put("zip", new ImageIcon(FileTable.class.getResource("/icons/zip.png")));
		fileIcons.put("rar", new ImageIcon(FileTable.class.getResource("/icons/rar.png")));
		fileIcons.put("tar", new ImageIcon(FileTable.class.getResource("/icons/tar.png")));
		fileIcons.put("tgz", new ImageIcon(FileTable.class.getResource("/icons/tgz.png")));
		fileIcons.put("tbz", new ImageIcon(FileTable.class.getResource("/icons/tbz.png")));
		fileIcons.put("gz", new ImageIcon(FileTable.class.getResource("/icons/gz.png")));
		fileIcons.put("bz2", new ImageIcon(FileTable.class.getResource("/icons/bz2.png")));
		fileIcons.put("xz", compress);
		fileIcons.put("7z", new ImageIcon(FileTable.class.getResource("/icons/7z.png")));
		fileIcons.put("jar", new ImageIcon(FileTable.class.getResource("/icons/jar.png")));
	}

	private final ResourceBundle language = Settings.language();

	private JTable table;
	private Model model;
	private boolean isLocal;
	private JTextField addr;
	private Listener listener = null;
	private TableRowSorter rowSorter = null;
	private String currentPath = null;

	public FileTable(String label) {
		this(label, false);
	}

	public FileTable(String label, boolean value) {
		super();

		isLocal = value;
		model = new Model();
		table = new JTable(model);
		rowSorter = new TableRowSorter();

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
						listener.selectedRow(isLocal, table.getSelectedRow(), getList().get(table.getSelectedRow()));
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
						listener.doubleClicked(isLocal, table.getSelectedRow(), getList().get(table.getSelectedRow()));
					} catch (ArrayIndexOutOfBoundsException e2) {
					}
				} else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 && listener != null) {
					int i = table.rowAtPoint(e.getPoint());
					table.addRowSelectionInterval(i, i);
					listener.rightClicked(isLocal, i, i >= 0 && i < getList().size() ? getList().get(table.getSelectedRow()) : null, e);
				}
			}
		});

		TableColumn tableColumn;

		// name column(0)
		tableColumn = table.getColumnModel().getColumn(0);
		tableColumn.setCellRenderer(new NameColumn());

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
		tableColumn.setMinWidth(126);
		tableColumn.setMaxWidth(126);

		if (!isLocal) {
			// perms column(4)
			tableColumn = table.getColumnModel().getColumn(4);
			tableColumn.setMinWidth(70);
			tableColumn.setMaxWidth(Integer.valueOf(language.getString("perms.size")));

			// uid column(5)
			tableColumn = table.getColumnModel().getColumn(5);
			tableColumn.setMinWidth(50);
			tableColumn.setMaxWidth(50);

			// gid column(6)
			tableColumn = table.getColumnModel().getColumn(6);
			tableColumn.setMinWidth(50);
			tableColumn.setMaxWidth(50);
		}

		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout(0, 0));

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
		panelNav.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
		panelNav.setLayout(new BorderLayout(0, 0));
		panelNav.add(new JLabel(label), BorderLayout.WEST);
		panelNav.add(addr, BorderLayout.CENTER);
		panelNav.add(btn, BorderLayout.EAST);

		JPanel panelTable = new JPanel();
		panelTable.setBorder(BorderFactory.createEmptyBorder());
		panelTable.setLayout(new BorderLayout(0, 0));
		panelTable.add(table.getTableHeader(), BorderLayout.NORTH);
		panelTable.add(new JScrollPane(table), BorderLayout.CENTER);

		add(panelNav, BorderLayout.NORTH);
		add(panelTable, BorderLayout.CENTER);
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

		char separator;
		if (isLocal) {
			separator = File.separatorChar;
			String resolve = currentPath != null ? currentPath : System.getProperty("user.dir");
			path = Paths.get(resolve).resolve(path).toAbsolutePath().toString();
		} else {
			separator = '/';
			if (!path.startsWith("/"))
				path = (currentPath != null ? currentPath : "") + '/' + path;
		}

		path = path.replaceAll(separator + "\\.$", "");
		path = path.replaceAll(separator + "\\." + separator, "" + separator);

		if (path.contains("..")) {
			path = path.replaceAll(separator + "[^" + separator + "]+" + separator + "\\.\\.$", "" + separator);
			path = path.replaceAll(separator + "[^" + separator + "]+" + separator + "\\.\\." + separator, "" + separator);
			path = path.replaceAll(separator + "\\.\\.$", "");
			path = path.replaceAll(separator + "\\.\\." + separator, "" + separator);
		}

		return path;
	}

	public void setListener(Listener l) {
		listener = l;
	}

	public void fireTableDataChanged() {
		model.fireTableDataChanged();
	}

	public void clear() {
		synchronized (getList()) {
			getList().clear();
		}
	}

	public List<Row> getList() {
		return model.getList();
	}

	public void setList(List<Row> list) {
		EventQueue.invokeLater(() -> {
			synchronized (getList()) {
				getList().clear();
				if ((isLocal && getAddr().length() > 1) || (!isLocal && !"/".equals(getAddr()))) {
					getList().add(new Row());
				}
				getList().addAll(list);
				fireTableDataChanged();
			}
		});
	}

	private static final Comparator<Row> nameComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			if (o1.isDir() && !o2.isDir())
				return -1;
			else if (!o1.isDir() && o2.isDir())
				return 1;

			return PinyinUtil.compareTo(o1.getName(), o2.getName());
		}
	};
	private static final Comparator<Row> sizeComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			if (o1.isDir() && !o2.isDir())
				return -1;
			else if (!o1.isDir() && o2.isDir())
				return 1;

			return Long.compare(o1.getSize(), o2.getSize());
		}
	};
	private static final Comparator<Row> typeComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			if (o1.isDir() && !o2.isDir())
				return -1;
			else if (!o1.isDir() && o2.isDir())
				return 1;

			return o1.getType().compareTo(o2.getType());
		}
	};
	private static final Comparator<Row> mtimeComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			if (o1.isDir() && !o2.isDir())
				return -1;
			else if (!o1.isDir() && o2.isDir())
				return 1;

			return o1.getMtime().compareTo(o2.getMtime());
		}
	};
	private static final Comparator<Row> permsComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			if (o1.isDir() && !o2.isDir())
				return -1;
			else if (!o1.isDir() && o2.isDir())
				return 1;

			return o1.getPerms().compareTo(o2.getPerms());
		}
	};
	private static final Comparator<Row> uidComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			if (o1.isDir() && !o2.isDir())
				return -1;
			else if (!o1.isDir() && o2.isDir())
				return 1;

			return Integer.compare(o1.getUid(), o2.getUid());
		}
	};
	private static final Comparator<Row> gidComparator = new Comparator<Row>() {
		@Override
		public int compare(Row o1, Row o2) {
			if (o1.isDir() && !o2.isDir())
				return -1;
			else if (!o1.isDir() && o2.isDir())
				return 1;

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
		public void enterAddr(boolean local, String addr);

		public void rightClicked(boolean isLocal, int i, Row r, MouseEvent e);

		public void selectedRow(boolean local, int i, Row r);

		public void doubleClicked(boolean local, int i, Row r);
	}

	public class NameColumn extends AbstractCellEditor implements TableCellRenderer {
		private static final long serialVersionUID = -4015532600021619257L;
		private final Color transparent = new Color(0, 0, 0, 0);
		private final JPanel panel;
		private final JLabel icon, name;

		public NameColumn() {
			super();

			icon = new JLabel();
			icon.setPreferredSize(new Dimension(30, 30));
			icon.setHorizontalAlignment(SwingConstants.CENTER);
			icon.setVerticalAlignment(SwingConstants.CENTER);
			name = new JLabel();

			panel = new JPanel();
			panel.setBorder(BorderFactory.createEmptyBorder());
			panel.setLayout(new BorderLayout(5, 5));
			panel.add(icon, BorderLayout.WEST);
			panel.add(name, BorderLayout.CENTER);
		}

		@Override
		public Object getCellEditorValue() {
			return name.getText();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Row r = getList().get(row);

			panel.setBackground(isSelected ? getTable().getSelectionBackground() : transparent);

			if (r.isDir()) {
				icon.setIcon(folderIcon);
			} else if ("LNK".equals(r.getType())) {
				icon.setIcon(linkIcon);
			} else {
				int i = r.getName().lastIndexOf('.');
				icon.setIcon(fileIcon);
				if (i > 0) {
					String ext = r.getName().substring(i + 1).toLowerCase();
					if (fileIcons.containsKey(ext))
						icon.setIcon(fileIcons.get(ext));
				}
			}

			name.setForeground(isSelected ? getTable().getSelectionForeground() : getTable().getForeground());
			name.setText(r.getName());
			return panel;
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

	public class TypeColumn extends AbstractCellEditor implements TableCellRenderer {
		private static final long serialVersionUID = 4160637907563033833L;

		private final Color transparent = new Color(0, 0, 0, 0);
		private final JPanel panel;
		private final JLabel type;

		public TypeColumn() {
			super();

			type = new JLabel();

			panel = new JPanel();
			panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
			panel.setLayout(new BorderLayout(0, 0));
			panel.add(type, BorderLayout.CENTER);
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			return super.shouldSelectCell(anEvent);
		}

		@Override
		public Object getCellEditorValue() {
			return type.getText();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			panel.setBackground(isSelected ? getTable().getSelectionBackground() : transparent);
			type.setForeground(isSelected ? getTable().getSelectionForeground() : getTable().getForeground());
			type.setText(language.getString("type." + value));
			return panel;
		}
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
			return isLocal ? 4 : title.length;
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

			if (f.isDirectory()) {
				type = "DIR";
				isDir = true;
			} else {
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
			uid = new Integer(f.getUser()).intValue();
			gid = new Integer(f.getGroup()).intValue();
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