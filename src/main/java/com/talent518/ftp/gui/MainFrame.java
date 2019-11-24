package com.talent518.ftp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import com.sun.awt.AWTUtilities;
import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.table.FileTable;
import com.talent518.ftp.gui.table.FileTable.Listener;
import com.talent518.ftp.gui.table.FileTable.Row;
import com.talent518.ftp.gui.table.ProgressTable;
import com.talent518.ftp.protocol.IProtocol;
import com.talent518.ftp.util.PinyinUtil;

@SuppressWarnings("restriction")
public class MainFrame extends JFrame implements ComponentListener, WindowListener, Listener {
	private static final long serialVersionUID = 1723682780360129927L;
	private static final double DIVIDER = 0.5;

	private static final Logger log = Logger.getLogger(MainFrame.class);

	private static ImageIcon icon = new ImageIcon(MainFrame.class.getResource("/icons/app.png"));
	private static JFrame load = new JFrame();
	private static ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 2, 1000, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

	public static void main(String[] args) {
		JButton btn = new JButton(Settings.language().getString("loading"), icon);

		btn.setBorder(BorderFactory.createEmptyBorder());
		btn.setForeground(Color.BLUE);
		btn.setBackground(new Color(0, 0, 0, 0));
		btn.setContentAreaFilled(false);
		btn.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setVerticalTextPosition(SwingConstants.CENTER);
		btn.setOpaque(false);// 设置控件是否透明，true为不透明，false为透明
		btn.setContentAreaFilled(false);// 设置图片填满按钮所在的区域
		btn.setMargin(new Insets(0, 0, 0, 0));// 设置按钮边框和标签文字之间的距离
		btn.setFocusPainted(false);// 设置这个按钮是不是获得焦点
		btn.setBorderPainted(false);// 设置是否绘制边框

		load.setIconImage(icon.getImage());
		load.setLayout(new BorderLayout(0, 0));
		load.add(btn, BorderLayout.CENTER);
		load.setSize(icon.getIconWidth(), icon.getIconHeight());
		load.setLocationRelativeTo(null);
		load.setResizable(true);
		load.setUndecorated(true);
		load.setVisible(true);

		AWTUtilities.setWindowOpaque(load, false);

		EventQueue.invokeLater(() -> {
			try {
				// UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
				// UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
				// UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
				// UIManager.setLookAndFeel("javax.swing.plaf.multi.MultiLookAndFeel");
				// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
				// UIManager.setLookAndFeel("javax.swing.plaf.synth.SynthLookAndFeel");
				// UIManager.setLookAndFeel("sun.awt.X11.XAWTLookAndFeel");
				// UIManager.setLookAndFeel(new SubstanceAutumnLookAndFeel());
			} catch (Exception e) {
				e.printStackTrace();
			}
			new MainFrame().setVisible(true);
		});
	}

	private final Settings settings = Settings.instance();
	private final ResourceBundle language = Settings.language();

	JPanel content = new JPanel(true);

	JMenuBar menuBar = new JMenuBar();

	JSplitPane lrSplit = new JSplitPane();
	JSplitPane lvSplit = new JSplitPane();
	JSplitPane rvSplit = new JSplitPane();

	FileTable remoteTable = new FileTable(language.getString("remote"));
	FileTable localTable = new FileTable(language.getString("local"), true);
	ProgressTable progressTable = new ProgressTable();
	JTextArea logText = new JTextArea();
	IProtocol protocol = null;

	public MainFrame() {
		super();

		setTitle(language.getString("app.name"));
		setSize(1024, 768);
		setLocationRelativeTo(null);
		setResizable(true);
		setIconImage(icon.getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		content.setBorder(new EmptyBorder(0, 0, 0, 0));
		content.setLayout(new BorderLayout(0, 0));
		setContentPane(content);

		initMenubar();
		initToolbar();
		initSplit();
		initStatusBar();

		addComponentListener(this);
		addWindowListener(this);

		remoteTable.setListener(this);
		localTable.setListener(this);

		localTable.setAddr(System.getProperty("user.dir"));
	}

	private void initMenubar() {
		Menu mFile = new Menu("menu.file", KeyEvent.VK_F);
		mFile.add(new MenuItem("file.open", KeyEvent.VK_O, MenuItem.KEY_OPEN).setKeyStroke("ctrl O"));
		mFile.add(new MenuItem("file.save", KeyEvent.VK_S, MenuItem.KEY_SAVE).setKeyStroke("ctrl S"));
		mFile.addSeparator();
		mFile.add(new MenuItem("file.preferences", KeyEvent.VK_P, MenuItem.KEY_PREF).setKeyStroke("ctrl P")); // Preferences
		mFile.addSeparator();
		mFile.add(new MenuItem("file.quit", KeyEvent.VK_Q, MenuItem.KEY_QUIT).setKeyStroke("ctrl Q"));
		menuBar.add(mFile);

		Menu mSite = new Menu("menu.site", KeyEvent.VK_S);
		mSite.add(new MenuItem("site.manage", KeyEvent.VK_E, MenuItem.KEY_MANAGE).setKeyStroke("ctrl M"));
		int i = KeyEvent.VK_A;
		for (String site : settings.getSiteNames())
			mSite.add(new MenuItem(site, i, MenuItem.KEY_SITE).setKeyStroke("ctrl alt " + (char) i++));
		menuBar.add(mSite);

		Menu mLang = new Menu("menu.lang", KeyEvent.VK_L);
		mLang.add(new MenuItem("lang.english", KeyEvent.VK_E, MenuItem.KEY_ENGLISH).setKeyStroke("ctrl shift E").enabled(!settings.getLang().equals("en"))); // English Language
		mLang.add(new MenuItem("lang.chinese", KeyEvent.VK_C, MenuItem.KEY_CHINESE).setKeyStroke("ctrl shift C").enabled(!settings.getLang().equals("zh"))); // Chinese Language
		menuBar.add(mLang);

		Menu mAbout = new Menu("menu.about", KeyEvent.VK_A);
		mAbout.add(new MenuItem("about.protocol", KeyEvent.VK_P, MenuItem.KEY_PROTOCOL).setKeyStroke("ctrl shift P")); // About Protocol
		mAbout.add(new MenuItem("about.application", KeyEvent.VK_A, MenuItem.KEY_APP).setKeyStroke("ctrl shift A")); // About Application
		menuBar.add(mAbout);

		setJMenuBar(menuBar);
	}

	private void initToolbar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setBorder(new LineBorder(new Color(0x999999), 1) {
			private static final long serialVersionUID = 8347901766288376013L;

			@Override
			public Insets getBorderInsets(Component c, Insets insets) {
				insets.set(5, 5, 6, 5);
				return insets;
			}

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Color oldColor = g.getColor();
				Graphics2D g2 = (Graphics2D) g;

				g2.setRenderingHints(rh);
				g2.setColor(lineColor);
				g2.drawLine(0, height - 1, width, height - 1);

				g2.setColor(oldColor);
			}
		});
		toolBar.add(new JButton("Tool1"));
		toolBar.add(new JButton("Tool2"));
		toolBar.add(new JButton("Tool3"));
		toolBar.add(new JButton("Tool4"));

		content.add(toolBar, BorderLayout.NORTH);
	}

	private void initSplit() {
		JScrollPane scrollPane = new JScrollPane(logText, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();

		logText.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				logText.append(e.paramString() + "\n");
			}

			@Override
			public void keyReleased(KeyEvent e) {
				logText.append(e.paramString() + "\n");
			}

			@Override
			public void keyPressed(KeyEvent e) {
				logText.append(e.paramString() + "\n");
			}
		});
		logText.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				scrollBar.setValue(scrollBar.getMaximum());
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				scrollBar.setValue(scrollBar.getMaximum());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				scrollBar.setValue(scrollBar.getMaximum());
			}
		});
		logText.setEditable(false);

		lrSplit.setOneTouchExpandable(true);
		lrSplit.setContinuousLayout(true);
		lrSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		lrSplit.setDividerLocation(0.5);
		lrSplit.setBorder(new EmptyBorder(0, 0, 0, 0));

		lvSplit.setOneTouchExpandable(true);
		lvSplit.setContinuousLayout(true);
		lvSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		lvSplit.setDividerLocation(300);
		lvSplit.setBorder(new EmptyBorder(0, 0, 0, 0));
		lvSplit.setTopComponent(remoteTable);
		lvSplit.setBottomComponent(progressTable);

		rvSplit.setOneTouchExpandable(true);
		rvSplit.setContinuousLayout(true);
		rvSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		rvSplit.setDividerLocation(0.5);
		rvSplit.setBorder(new EmptyBorder(0, 0, 0, 0));
		rvSplit.setTopComponent(localTable);
		rvSplit.setBottomComponent(scrollPane);

		lrSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// System.out.println("LR: " + lrSplit.getDividerLocation());
			}
		});

		lvSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// System.out.println("LV: " + lvSplit.getDividerLocation());
				rvSplit.setDividerLocation(lvSplit.getDividerLocation());
			}
		});

		rvSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// System.out.println("RV: " + rvSplit.getDividerLocation());
				lvSplit.setDividerLocation(rvSplit.getDividerLocation());
			}
		});

		lrSplit.setLeftComponent(lvSplit);
		lrSplit.setRightComponent(rvSplit);

		content.add(lrSplit, BorderLayout.CENTER);
	}

	private JLabel status = new JLabel("Status");

	private void initStatusBar() {
		status.setBorder(new LineBorder(new Color(0x999999), 1) {
			private static final long serialVersionUID = 8347901766288376013L;

			@Override
			public Insets getBorderInsets(Component c, Insets insets) {
				insets.set(6, 5, 5, 5);
				return insets;
			}

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Color oldColor = g.getColor();
				Graphics2D g2 = (Graphics2D) g;

				g2.setRenderingHints(rh);
				g2.setColor(lineColor);
				g2.drawLine(0, 0, width, 0);

				g2.setColor(oldColor);
			}
		});
//		status.setFont(new Font(status.getFont().getFontName(), Font.PLAIN, 16));
		content.add(status, BorderLayout.SOUTH);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		lrSplit.setDividerLocation(DIVIDER);
		lvSplit.setDividerLocation(DIVIDER);
		rvSplit.setDividerLocation(DIVIDER);
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
		log.debug(e);
		if (load != null) {
			load.dispose();
			load = null;
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		log.debug(e);
	}

	@Override
	public void windowClosed(WindowEvent e) {
		log.debug(e);
	}

	@Override
	public void windowIconified(WindowEvent e) {
		log.debug(e);
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		log.debug(e);
	}

	@Override
	public void windowActivated(WindowEvent e) {
		log.debug(e);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		log.debug(e);
	}

	@Override
	public void enterAddr(boolean local) {
		if (local) {
			log.debug("Enter local: " + localTable.getAddr());

			localTable.getList().clear();
			if (localTable.getAddr().length() > 1)
				localTable.getList().add(new FileTable.Row());
			File path = new File(localTable.getAddr());
			if (path.isDirectory()) {
				File[] files = path.listFiles();
				if (files != null) {
					Arrays.sort(files, fileSortComparator);
					for (File f : files) {
						localTable.getList().add(new FileTable.Row(f));
					}
				}
			}
			localTable.fireTableDataChanged();
		} else {
			log.debug("Enter remote: " + remoteTable.getAddr());

			pool.execute(() -> {
				List<FileTable.Row> files = protocol.ls(remoteTable.getAddr());
				files.sort(rowSortComparator);

				remoteTable.getList().clear();
				if (!"/".equals(remoteTable.getAddr()))
					remoteTable.getList().add(new FileTable.Row());
				remoteTable.getList().addAll(files);
				remoteTable.fireTableDataChanged();
			});
		}
	}

	@Override
	public void selectedRow(boolean local, int i, Row r) {
		status.setText(r.getType() + " " + r.getName() + " " + r.getSize());
	}

	@Override
	public void doubleClicked(boolean local, int i, Row r) {
		if (r.isDir()) {
			if (local) {
				if (r.isUp()) {
					i = localTable.getAddr().lastIndexOf(File.separator);
					if (i > 0)
						localTable.setAddr(localTable.getAddr().substring(0, i));
					else if(File.separator.equals("/"))
						localTable.setAddr(File.separator);
					else
						localTable.setAddr(localTable.getAddr());
				} else {
					localTable.setAddr(localTable.getAddr().replaceAll("[\\/]+$", "") + File.separator + r.getName());
				}
				status.setText("doubleClicked: " + localTable.getAddr());
			} else {
				if (r.isUp()) {
					i = remoteTable.getAddr().lastIndexOf('/');
					if (i > 0)
						remoteTable.setAddr(remoteTable.getAddr().substring(0, i));
					else
						remoteTable.setAddr("/");
				} else {
					remoteTable.setAddr(remoteTable.getAddr().replaceAll("[\\/]+$", "") + '/' + r.getName());
				}
				status.setText("doubleClicked: " + remoteTable.getAddr());
			}
		} else {
			status.setText("doubleClicked: " + r.getName());
		}
	}

	private Comparator<File> fileSortComparator = new Comparator<File>() {
		@Override
		public int compare(File o1, File o2) {
			if (o1.isDirectory() && !o2.isDirectory())
				return -1;
			else if (!o1.isDirectory() && o2.isDirectory())
				return 1;

			return PinyinUtil.compareTo(o1.getName(), o2.getName());
		}
	};

	private Comparator<FileTable.Row> rowSortComparator = new Comparator<FileTable.Row>() {
		@Override
		public int compare(FileTable.Row o1, FileTable.Row o2) {
			if (o1.isDir() && !o2.isDir())
				return -1;
			else if (!o1.isDir() && o2.isDir())
				return 1;

			return PinyinUtil.compareTo(o1.getName(), o2.getName());
		}
	};

	public class Menu extends JMenu {
		private static final long serialVersionUID = 4386512476542676379L;

		String resKey, resVal;

		public Menu(String res, int mnemonic) {
			super();
			resKey = res;
			resVal = language.getString(res);
			setText(resVal);
			setMnemonic(mnemonic);
		}

		public JMenuItem add(MenuItem menuItem) {
			return super.add((JMenuItem) menuItem);
		}
	}

	public class MenuItem extends JMenuItem implements Action {
		private static final long serialVersionUID = 6896925674527585756L;

		// menu file
		public static final int KEY_OPEN = 0;
		public static final int KEY_SAVE = 1;
		public static final int KEY_PREF = 2;
		public static final int KEY_QUIT = 3;

		// menu site
		public static final int KEY_MANAGE = 10;
		public static final int KEY_SITE = 11;

		// menu lang
		public static final int KEY_ENGLISH = 20;
		public static final int KEY_CHINESE = 21;

		// menu about
		public static final int KEY_PROTOCOL = 30;
		public static final int KEY_APP = 31;

		String resKey, resVal;
		private int key;
		KeyStroke keyStroke;

		public MenuItem(String res, int mnemonic, int key) {
			super();

			resKey = res;
			if (key == KEY_SITE) {
				resVal = res;
			} else {
				resVal = language.getString(res);
				setText(resVal);
			}
			setMnemonic(mnemonic);

			this.key = key;

			addActionListener(this);
		}

		public MenuItem setKeyStroke(String stroke) {
			return setKeyStroke(stroke, this);
		}

		public MenuItem setKeyStroke(String stroke, Action action) {
			JPanel panel = MainFrame.this.content;
			keyStroke = KeyStroke.getKeyStroke(stroke);

			panel.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, resKey);
			panel.getActionMap().put(resKey, action);

			String[] exprs = stroke.split("\\s+");
			StringBuilder sb = new StringBuilder();
			for (String s : exprs) {
				if (sb.length() > 0)
					sb.append('+');
				sb.append(s.substring(0, 1).toUpperCase() + s.substring(1));
			}
			String shortcut = sb.toString();
			if (resKey.equals(resVal))
				resVal = resVal + " (" + (char) getMnemonic() + ") - " + shortcut;
			else
				resVal = resVal + " - " + shortcut;
			setText(resVal);

			return this;
		}

		public MenuItem enabled(boolean b) {
			setEnabled(b);
			return this;
		}

		public int getKey() {
			return key;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			log.debug("Performed: resKey = " + resKey + ", resVal = " + resVal + ", mnemonic = " + (char) getMnemonic());

			switch (key) {
				case KEY_OPEN:
					break;
				case KEY_SAVE:
					break;
				case KEY_PREF:
					break;
				case KEY_QUIT:
					MainFrame.this.dispose();
					break;
				case KEY_MANAGE:
					break;
				case KEY_SITE:
					protocol = settings.getSites().get(resKey).create();
					pool.execute(() -> {
						if (protocol.login()) {
							logText.append(String.format(language.getString("log.connected"), resKey, protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername()));
							remoteTable.setAddr(protocol.pwd());
						} else {
							logText.append(String.format(language.getString("log.connecterr"), resKey, protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername()));
						}
						logText.append("\n");
					});
					break;
				case KEY_ENGLISH:
					settings.setLocale(new Locale("en", "US"));
					settings.save();
					dispose();
					new MainFrame().setVisible(true);
					break;
				case KEY_CHINESE:
					settings.setLocale(new Locale("zh", "CN"));
					settings.save();
					dispose();
					new MainFrame().setVisible(true);
					break;
				case KEY_PROTOCOL:
					break;
				case KEY_APP:
					break;
			}
		}

		@Override
		public Object getValue(String key) {
			log.debug("getValue: key = " + key);
			return resVal;
		}

		@Override
		public void putValue(String key, Object value) {
			log.debug("putValue: key = " + key + ", value = " + value);
		}
	}
}
