package com.talent518.ftp.gui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.talent518.ftp.dao.Settings;

public class FileTable extends JPanel {
	private static final long serialVersionUID = -1789896671155598722L;
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final Icon folderIcon = new ImageIcon(FileTable.class.getClass().getResource("/icons/folder.png"));
	private static final Icon fileIcon = new ImageIcon(FileTable.class.getClass().getResource("/icons/file.png"));
	private static final Map<String, Icon> fileIcons = new HashMap<String, Icon>();
	static {
		// image file
		fileIcons.put("png", new ImageIcon(FileTable.class.getClass().getResource("/icons/png.png")));
		Icon jpg = new ImageIcon(FileTable.class.getClass().getResource("/icons/jpeg.png"));
		fileIcons.put("jpg", jpg);
		fileIcons.put("jpeg", jpg);
		fileIcons.put("gif", new ImageIcon(FileTable.class.getClass().getResource("/icons/gif.png")));
		fileIcons.put("bmp", new ImageIcon(FileTable.class.getClass().getResource("/icons/bmp.png")));
		fileIcons.put("gif", new ImageIcon(FileTable.class.getClass().getResource("/icons/gif.png")));
		fileIcons.put("ai", new ImageIcon(FileTable.class.getClass().getResource("/icons/ai.png")));
		fileIcons.put("psd", new ImageIcon(FileTable.class.getClass().getResource("/icons/psd.png")));
		fileIcons.put("svg", new ImageIcon(FileTable.class.getClass().getResource("/icons/svg.png")));
		fileIcons.put("cdr", new ImageIcon(FileTable.class.getClass().getResource("/icons/cdr.png")));

		// office document
		fileIcons.put("doc", new ImageIcon(FileTable.class.getClass().getResource("/icons/doc.png")));
		fileIcons.put("docx", new ImageIcon(FileTable.class.getClass().getResource("/icons/docx.png")));
		fileIcons.put("ppt", new ImageIcon(FileTable.class.getClass().getResource("/icons/ppt.png")));
		fileIcons.put("pptx", new ImageIcon(FileTable.class.getClass().getResource("/icons/pptx.png")));
		fileIcons.put("xls", new ImageIcon(FileTable.class.getClass().getResource("/icons/xls.png")));
		fileIcons.put("xlsx", new ImageIcon(FileTable.class.getClass().getResource("/icons/xlsx.png")));
		fileIcons.put("pdf", new ImageIcon(FileTable.class.getClass().getResource("/icons/pdf.png")));

		// text file
		fileIcons.put("ada", new ImageIcon(FileTable.class.getClass().getResource("/icons/ada.png")));
		fileIcons.put("html", new ImageIcon(FileTable.class.getClass().getResource("/icons/html.png")));
		fileIcons.put("php", new ImageIcon(FileTable.class.getClass().getResource("/icons/php.png")));
		fileIcons.put("txt", new ImageIcon(FileTable.class.getClass().getResource("/icons/txt.png")));
		fileIcons.put("xml", new ImageIcon(FileTable.class.getClass().getResource("/icons/xml.png")));
		fileIcons.put("java", new ImageIcon(FileTable.class.getClass().getResource("/icons/java.png")));
		fileIcons.put("class", new ImageIcon(FileTable.class.getClass().getResource("/icons/class.png")));
		fileIcons.put("js", new ImageIcon(FileTable.class.getClass().getResource("/icons/js.png")));
		fileIcons.put("css", new ImageIcon(FileTable.class.getClass().getResource("/icons/css.png")));
		fileIcons.put("json", new ImageIcon(FileTable.class.getClass().getResource("/icons/json.png")));
		fileIcons.put("c", new ImageIcon(FileTable.class.getClass().getResource("/icons/c.png")));
		fileIcons.put("cs", new ImageIcon(FileTable.class.getClass().getResource("/icons/cs.png")));
		Icon cpp = new ImageIcon(FileTable.class.getClass().getResource("/icons/cpp.png"));
		fileIcons.put("cpp", cpp);
		fileIcons.put("cxx", cpp);
		Icon h = new ImageIcon(FileTable.class.getClass().getResource("/icons/h.png"));
		fileIcons.put("h", h);
		fileIcons.put("hxx", h);
		fileIcons.put("vbs", new ImageIcon(FileTable.class.getClass().getResource("/icons/vbs.png")));
		fileIcons.put("xhtml", new ImageIcon(FileTable.class.getClass().getResource("/icons/xhtml.png")));
		fileIcons.put("py", new ImageIcon(FileTable.class.getClass().getResource("/icons/py.png")));
		fileIcons.put("rb", new ImageIcon(FileTable.class.getClass().getResource("/icons/rb.png")));
		fileIcons.put("sql", new ImageIcon(FileTable.class.getClass().getResource("/icons/sql.png")));
		fileIcons.put("vb", new ImageIcon(FileTable.class.getClass().getResource("/icons/vb.png")));
		fileIcons.put("go", new ImageIcon(FileTable.class.getClass().getResource("/icons/go.png")));
		fileIcons.put("sh", new ImageIcon(FileTable.class.getClass().getResource("/icons/sh.png")));

		// video file
		fileIcons.put("avi", new ImageIcon(FileTable.class.getClass().getResource("/icons/avi.png")));
		fileIcons.put("mp4", new ImageIcon(FileTable.class.getClass().getResource("/icons/mp4.png")));
		fileIcons.put("wmv", new ImageIcon(FileTable.class.getClass().getResource("/icons/wmv.png")));
		fileIcons.put("divx", new ImageIcon(FileTable.class.getClass().getResource("/icons/divx.png")));
		fileIcons.put("rmvb", new ImageIcon(FileTable.class.getClass().getResource("/icons/rmvb.png")));
		fileIcons.put("rm", new ImageIcon(FileTable.class.getClass().getResource("/icons/rm.png")));
		fileIcons.put("flv", new ImageIcon(FileTable.class.getClass().getResource("/icons/flv.png")));
		fileIcons.put("mov", new ImageIcon(FileTable.class.getClass().getResource("/icons/mov.png")));
		fileIcons.put("3gp", new ImageIcon(FileTable.class.getClass().getResource("/icons/3gp.png")));
		fileIcons.put("asf", new ImageIcon(FileTable.class.getClass().getResource("/icons/asf.png")));

		// audio file
		fileIcons.put("aiff", new ImageIcon(FileTable.class.getClass().getResource("/icons/aiff.png")));
		fileIcons.put("wma", new ImageIcon(FileTable.class.getClass().getResource("/icons/wma.png")));
		fileIcons.put("cda", new ImageIcon(FileTable.class.getClass().getResource("/icons/cda.png")));
		fileIcons.put("mp3", new ImageIcon(FileTable.class.getClass().getResource("/icons/mp3.png")));
		fileIcons.put("midi", new ImageIcon(FileTable.class.getClass().getResource("/icons/mp3.png")));
		fileIcons.put("ogg", new ImageIcon(FileTable.class.getClass().getResource("/icons/ogg.png")));
		fileIcons.put("flac", new ImageIcon(FileTable.class.getClass().getResource("/icons/flac.png")));

		// compress
		Icon compress = new ImageIcon(FileTable.class.getClass().getResource("/icons/compress.png"));
		fileIcons.put("zip", new ImageIcon(FileTable.class.getClass().getResource("/icons/zip.png")));
		fileIcons.put("rar", new ImageIcon(FileTable.class.getClass().getResource("/icons/rar.png")));
		fileIcons.put("tar", new ImageIcon(FileTable.class.getClass().getResource("/icons/tar.png")));
		fileIcons.put("tgz", new ImageIcon(FileTable.class.getClass().getResource("/icons/tgz.png")));
		fileIcons.put("tbz", new ImageIcon(FileTable.class.getClass().getResource("/icons/tbz.png")));
		fileIcons.put("gz", new ImageIcon(FileTable.class.getClass().getResource("/icons/gz.png")));
		fileIcons.put("bz2", new ImageIcon(FileTable.class.getClass().getResource("/icons/bz2.png")));
		fileIcons.put("xz", compress);
		fileIcons.put("7z", new ImageIcon(FileTable.class.getClass().getResource("/icons/7z.png")));
		fileIcons.put("jar", new ImageIcon(FileTable.class.getClass().getResource("/icons/jar.png")));
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(600, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Custom JTable");
		frame.setContentPane(new FileTable("Test:"));
		frame.setVisible(true);
	}

	private final ResourceBundle lauguage = Settings.language();

	private JTable table;
	private Model model;
	private boolean isLocal;
	private JTextField addr = new JTextField();
	private Listener listener = null;

	public FileTable(String label) {
		this(label, false);
	}

	public FileTable(String label, boolean value) {
		super();

		isLocal = value;
		model = new Model();
		table = new JTable(model);

		table.setRowHeight(30);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.setColumnSelectionAllowed(false);
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
		table.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && listener != null) {
					try {
						listener.doubleClicked(isLocal, table.getSelectedRow(), getList().get(table.getSelectedRow()));
					} catch (ArrayIndexOutOfBoundsException e2) {
					}
				}
			}
		});

		TableColumn tableColumn;

		tableColumn = table.getColumnModel().getColumn(0);
		tableColumn.setCellRenderer(new NameColumn());

		tableColumn = table.getColumnModel().getColumn(1);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(40);

		tableColumn = table.getColumnModel().getColumn(2);
		tableColumn.setMinWidth(40);
		tableColumn.setMaxWidth(40);

		tableColumn = table.getColumnModel().getColumn(3);
		tableColumn.setMinWidth(126);
		tableColumn.setMaxWidth(126);

		if (!isLocal) {
			tableColumn = table.getColumnModel().getColumn(4);
			tableColumn.setMinWidth(60);
			tableColumn.setMaxWidth(60);

			tableColumn = table.getColumnModel().getColumn(5);
			tableColumn.setMinWidth(60);
			tableColumn.setMaxWidth(60);

			tableColumn = table.getColumnModel().getColumn(6);
			tableColumn.setMinWidth(60);
			tableColumn.setMaxWidth(60);
		}

		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout(0, 0));

		addr.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && listener != null) {
					listener.enterAddr(isLocal);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		JButton btn = new JButton(lauguage.getString("enter"));
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (listener != null) {
					listener.enterAddr(isLocal);
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
		addr.setText(text);

		if (listener != null) {
			listener.enterAddr(isLocal);
		}
	}

	public void setListener(Listener l) {
		listener = l;
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

	public interface Listener {
		public void enterAddr(boolean local);

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
		public boolean shouldSelectCell(EventObject anEvent) {
			return super.shouldSelectCell(anEvent);
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
			} else {
				int i = r.getName().lastIndexOf('.');
				icon.setIcon(fileIcon);
				if (i > 0) {
					String ext = r.getName().substring(i + 1).toLowerCase();
					if (fileIcons.containsKey(ext))
						icon.setIcon(fileIcons.get(ext));
				}
			}

			name.setText(r.getName());
			return panel;
		}

	}

	public class Model extends AbstractTableModel {
		private static final long serialVersionUID = -1994280421860518219L;

		private Class<?>[] cellType = { String.class, Long.class, String.class, String.class, String.class, String.class, String.class };
		// @formatter:off
		private String title[] = {
			lauguage.getString("fileTable.name"),
			lauguage.getString("fileTable.size"),
			lauguage.getString("fileTable.type"),
			lauguage.getString("fileTable.mtime"),
			lauguage.getString("fileTable.perms"),
			lauguage.getString("fileTable.uid"),
			lauguage.getString("fileTable.gid")
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

	public class Row {
		private String name;
		private long size;
		private String type;
		private String mtime;
		private String perms;
		private String uid;
		private String gid;
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
			type = "Unknow";

			if (f.isDirectory()) {
				type = "DIR";
				isDir = true;
			} else {
				type = "REG";
			}

			mtime = dateFormat.format(f.lastModified());
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
					uid = (String) value;
					break;
				case 6:
					gid = (String) value;
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

		public String getUid() {
			return uid;
		}

		public void setUid(String uid) {
			this.uid = uid;
		}

		public String getGid() {
			return gid;
		}

		public void setGid(String gid) {
			this.gid = gid;
		}

		@Override
		public String toString() {
			return Settings.gson().toJson(this);
		}
	}
}