package com.talent518.ftp.gui.table.column;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import com.talent518.ftp.gui.table.FileTable;

public class NameColumn extends AbstractCellEditor implements TableCellRenderer {
	private static final long serialVersionUID = -4015532600021619257L;

	private static final Icon folderIcon = new ImageIcon(FileTable.class.getResource("/icons/folder.png"));
	private static final Icon folderLinkIcon = new ImageIcon(FileTable.class.getResource("/icons/folder_link.png"));
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

	private final Color transparent = new Color(0, 0, 0, 0);
	private final JPanel panel;
	private final JLabel icon, name;
	private final int typeCol;

	public NameColumn(int typeCol) {
		super();

		this.typeCol = typeCol;
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
		String type = (String) table.getValueAt(row, typeCol);

		panel.setBackground(isSelected ? table.getSelectionBackground() : transparent);
		name.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
		name.setText((String) value);

		if ("DIR".equals(type)) {
			icon.setIcon(folderIcon);
		} else if ("DL".equals(type)) {
			icon.setIcon(folderLinkIcon);
		} else if ("LNK".equals(type)) {
			icon.setIcon(linkIcon);
		} else {
			int i = name.getText().lastIndexOf('.');
			icon.setIcon(fileIcon);
			if (i > 0) {
				String ext = name.getText().substring(i + 1).toLowerCase();
				if (fileIcons.containsKey(ext))
					icon.setIcon(fileIcons.get(ext));
			}
		}

		return panel;
	}
}
