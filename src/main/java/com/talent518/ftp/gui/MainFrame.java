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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

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
import javax.swing.JPopupMenu;
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
import com.talent518.ftp.dao.Site.Favorite;
import com.talent518.ftp.gui.dialog.FavoriteDialog;
import com.talent518.ftp.gui.table.FileTable;
import com.talent518.ftp.gui.table.FileTable.Row;
import com.talent518.ftp.gui.table.ProgressTable;
import com.talent518.ftp.protocol.IProtocol;
import com.talent518.ftp.util.FileUtils;

@SuppressWarnings("restriction")
public class MainFrame extends JFrame implements ComponentListener, WindowListener, FileTable.Listener, ProgressTable.Listener {
	private static final long serialVersionUID = 1723682780360129927L;
	private static final double DIVIDER = 0.5;

	private static final Logger log = Logger.getLogger(MainFrame.class);
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	public static final ImageIcon icon = new ImageIcon(MainFrame.class.getResource("/icons/app.png"));
	private static JFrame load = new JFrame();
	private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 2, 1, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

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
	private final Transfer transfer = new Transfer();

	JPanel content = new JPanel(true);

	JMenuBar menuBar = new JMenuBar();
	JToolBar toolBar = new JToolBar();
	MenuItem favoriteMenu;
	PopupMenu localMenu = new PopupMenu();
	PopupMenu remoteMenu = new PopupMenu();
	ProgressMenu progressMenu = new ProgressMenu();

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
		initPopupMenu();

		addComponentListener(this);
		addWindowListener(this);

		remoteTable.setListener(this);
		localTable.setListener(this);
		progressTable.setListener(this);

		localTable.setAddr(System.getProperty("user.home"));
	}

	public IProtocol getProtocol() {
		return protocol;
	}

	public String getRemoteAddr() {
		return remoteTable.getAddr();
	}

	public String getLocalAddr() {
		return localTable.getAddr();
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

		favoriteMenu = new MenuItem("site.favorite", KeyEvent.VK_F, MenuItem.KEY_FAVORITE).setKeyStroke("ctrl D");
		favoriteMenu.setEnabled(false);

		Menu mSite = new Menu("menu.site", KeyEvent.VK_S);
		mSite.add(new MenuItem("site.manage", KeyEvent.VK_M, MenuItem.KEY_MANAGE).setKeyStroke("ctrl M"));
		mSite.add(favoriteMenu);
		mSite.addSeparator();
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
		toolBar.setVisible(false);

		content.add(toolBar, BorderLayout.NORTH);
	}

	public void initToolbar(Map<String, Favorite> favorites) {
		toolBar.removeAll();

		for (Favorite f : favorites.values()) {
			toolBar.add(new FavoriteButton(f));
		}

		toolBar.setVisible(toolBar.getComponentCount() > 0);
	}

	private void initSplit() {
		JScrollPane scrollPane = new JScrollPane(logText, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();

		logText.setToolTipText(language.getString("tip.logText"));
		logText.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					println("");
				else if (e.getKeyCode() == KeyEvent.VK_DELETE)
					logText.setText("");
			}

			@Override
			public void keyPressed(KeyEvent e) {
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

	private MenuItem localUploadMenu;
	private MenuItem localQueueMenu;
	private MenuItem progressTransferMenu;
	private MenuItem progressCleanAllMenu;

	private void initPopupMenu() {
		localUploadMenu = new MenuItem("local.upload", KeyEvent.VK_U, MenuItem.KEY_UPLOAD);
		localQueueMenu = new MenuItem("local.queue", KeyEvent.VK_Q, MenuItem.KEY_LQUEUE);
		localMenu.add(localUploadMenu);
		localMenu.add(localQueueMenu);
		localMenu.addSeparator();
		localMenu.add(new MenuItem("local.delete", KeyEvent.VK_D, MenuItem.KEY_LDELETE));
		localMenu.add(new MenuItem("local.mkdir", KeyEvent.VK_M, MenuItem.KEY_LMKDIR));

		remoteMenu.add(new MenuItem("remote.download", KeyEvent.VK_D, MenuItem.KEY_DOWNLOAD));
		remoteMenu.add(new MenuItem("remote.queue", KeyEvent.VK_Q, MenuItem.KEY_RQUEUE));
		remoteMenu.addSeparator();
		remoteMenu.add(new MenuItem("remote.delete", KeyEvent.VK_D, MenuItem.KEY_RDELETE));
		remoteMenu.add(new MenuItem("remote.mkdir", KeyEvent.VK_M, MenuItem.KEY_RMKDIR));

		progressTransferMenu = new MenuItem("progress.transfer", KeyEvent.VK_T, MenuItem.KEY_TRANSFER);
		progressTransferMenu.setEnabled(false);
		progressCleanAllMenu = new MenuItem("progress.cleanAll", KeyEvent.VK_A, MenuItem.KEY_CLEAN_ALL);
		progressMenu.add(progressTransferMenu);
		progressMenu.addSeparator();
		progressMenu.add(progressCleanAllMenu);
		progressMenu.add(new MenuItem("progress.cleanCompleted", KeyEvent.VK_C, MenuItem.KEY_CLEAN_COMPLETED));
		progressMenu.add(new MenuItem("progress.cleanError", KeyEvent.VK_E, MenuItem.KEY_CLEAN_ERROR));
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

	public void println(String str) {
		EventQueue.invokeLater(() -> {
			logText.append(timeFormat.format(new Date()) + ' ' + str + '\n');
		});
	}

	@SuppressWarnings("resource")
	public void println(String format, Object... args) {
		println(new Formatter().format(format, args).toString());
	}

	@Override
	public void enterAddr(boolean local, String addr) {
		if (local) {
			log.debug("Enter local: " + addr + " begin");
			println(language.getString("log.localEntering"), addr);
		} else {
			if (protocol == null)
				return;

			log.debug("Enter remote: " + addr + " begin");
			println(language.getString("log.remoteEntering"), addr);
		}
		String old = local ? localTable.getAddr() : remoteTable.getAddr();
		pool.execute(() -> {
			if (local) {
				List<FileTable.Row> list = new ArrayList<FileTable.Row>();
				File path = new File(addr);
				if (path.isDirectory()) {
					File[] files = path.listFiles();
					if (files != null) {
						for (File f : files) {
							list.add(new FileTable.Row(f));
						}
					}
					if (localTable.getAddr().equals(old)) {
						localTable.setAddrText(addr);
						localTable.setList(list);
						log.debug("Enter local: " + addr + " end");
						println(language.getString("log.localEntered"), addr);
					} else {
						println("-----");
					}
				} else {
					localTable.setAddrText(localTable.getCurrentPath());
					log.debug("Enter local: " + addr + " ERROR: not directory");
					println(language.getString("log.localEntererr"), addr);
				}
			} else {
				List<FileTable.Row> list = new ArrayList<FileTable.Row>();
				if (protocol.ls(addr, list)) {
					if (remoteTable.getAddr().equals(old)) {
						remoteTable.setAddrText(addr);
						remoteTable.setList(list);
						log.debug("Enter remote: " + addr + " end");
						println(language.getString("log.remoteEntered"), addr);
					} else {
						println("-----");
					}
				} else {
					remoteTable.setAddrText(remoteTable.getCurrentPath());
					log.debug("Enter remote: " + addr + " , ERROR: " + protocol.getError());
					println(language.getString("log.remoteEntererr"), addr, protocol.getError());
				}
			}
		});
	}

	@Override
	public void rightClicked(boolean isLocal, int i, Row r, MouseEvent e) {
		if (r == null)
			return;

		if (isLocal) {
			localUploadMenu.setEnabled(protocol != null);
			localQueueMenu.setEnabled(protocol != null);
			localMenu.setRow(r);
			localMenu.show(e.getComponent(), e.getX(), e.getY());
		} else {
			remoteMenu.setRow(r);
			remoteMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	@Override
	public void selectedRow(boolean local, int i, Row r) {
		status.setText("selectedRow: " + language.getString("type." + r.getType()) + " \"" + r.getName() + "\" " + FileUtils.formatSize(r.getSize()));
	}

	@Override
	public void doubleClicked(boolean local, int i, Row r) {
		if (r.isDir()) {
			if (local) {
				if (r.isUp()) {
					localTable.setAddr(localTable.getParentPath());
				} else {
					localTable.setAddr(localTable.getPath(r.getName()));
				}
			} else {
				if (r.isUp()) {
					remoteTable.setAddr(remoteTable.getParentPath());
				} else {
					remoteTable.setAddr(remoteTable.getPath(r.getName()));
				}
			}
		} else {
			status.setText("doubleClicked: " + r.getName());
		}
	}

	@Override
	public void rightClicked(int i, ProgressTable.Row r, MouseEvent e) {
		if (r == null)
			return;

		if (transfer.isRunning()) {
			progressTransferMenu.setEnabled(false);
			progressCleanAllMenu.setEnabled(false);
		} else {
			progressTransferMenu.setEnabled(false);
			for (ProgressTable.Row r2 : progressTable.getList()) {
				if (r2.getStatus() == ProgressTable.Row.STATUS_READY) {
					progressTransferMenu.setEnabled(true);
					break;
				}
			}
			progressCleanAllMenu.setEnabled(progressTable.getList().size() > 0);
		}

		progressMenu.setRow(r);
		progressMenu.show(e.getComponent(), e.getX(), e.getY());
	}

	@Override
	public void selectedRow(int i, ProgressTable.Row r) {
		status.setText("selectedRow: " + language.getString("type." + r.getType()) + " \"" + (r.isDirection() ? r.getLocal() : r.getRemote()) + "\" " + FileUtils.formatSize(r.getSize()));
	}

	@Override
	public void doubleClicked(int i, ProgressTable.Row r) {
		status.setText("doubleClicked: " + language.getString("type." + r.getType()) + " \"" + (r.isDirection() ? r.getLocal() : r.getRemote()) + "\" " + FileUtils.formatSize(r.getSize()));
	}

	public class Transfer {
		private AtomicBoolean running = new AtomicBoolean(false);
		private AtomicBoolean diring = new AtomicBoolean(false);
		private LinkedBlockingQueue<ProgressTable.Row> queue = new LinkedBlockingQueue<ProgressTable.Row>(1000);
		private LinkedList<ProgressTable.Row> link = new LinkedList<ProgressTable.Row>();
		private AtomicInteger nThread = new AtomicInteger(0);
		private LinkedList<ProgressTable.Row> progresses = new LinkedList<ProgressTable.Row>();
		private List<ProgressTable.Row> lock;
		private Timer timer;

		public void start() {
			if (running.get())
				return;

			println(language.getString("log.transferBegin"));
			running.set(true);

			lock = progressTable.getList();
			timer = new Timer("transfer", true);
			timer.schedule(new RefreshTask(), 250, 250);

			// pool.execute(new ProgressQueue(progressTable.getList()));
			synchronized (lock) {
				addAll(progressTable.getList());
			}
		}

		public boolean isRunning() {
			return running.get();
		}

		public void add(ProgressTable.Row row) {
			if (running.get()) {
				// pool.execute(new ProgressQueue(row));
				new ProgressQueue(row).run();
			}
		}

		public void addAll(List<ProgressTable.Row> rows) {
			if (running.get()) {
				// pool.execute(new ProgressQueue(rows));
				new ProgressQueue(rows).run();
			}
		}

		private class ProgressQueue implements Runnable {
			LinkedList<ProgressTable.Row> lr = new LinkedList<ProgressTable.Row>();

			public ProgressQueue(List<ProgressTable.Row> list) {
				lr.addAll(list);
			}

			public ProgressQueue(ProgressTable.Row row) {
				lr.add(row);
			}

			@Override
			public void run() {
				synchronized (link) {
					while (!lr.isEmpty()) {
						link.add(lr.removeFirst());
					}
				}

				if (!diring.getAndSet(true)) {
					new DirectoryThread().start();
				}
			}
		}

		private abstract class TransferThread extends Thread {
			Map<String, IProtocol> sites = new HashMap<String, IProtocol>();
			List<FileTable.Row> files = new ArrayList<FileTable.Row>();
			ProgressTable.Row r, p;
			File f;
			IProtocol protocol;

			// return true skip transfer process
			protected boolean isSkip() {
				if (sites.containsKey(r.getSite())) {
					protocol = sites.get(r.getSite());
				} else if (!settings.getSites().containsKey(r.getSite())) {
					println(language.getString("log.siteNotExists"), r.getSite());
					return true;
				} else {
					protocol = settings.getSites().get(r.getSite()).create();

					println(language.getString("log.connecting"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					if (protocol.login())
						println(language.getString("log.connected"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());

					sites.put(r.getSite(), protocol);
				}

				if (!protocol.isConnected() && protocol.isLogined()) {
					protocol.logout();
					println(language.getString("log.connecting"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					if (protocol.login())
						println(language.getString("log.connected"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
				}

				if (!protocol.isConnected() || !protocol.isLogined()) {
					r.setStatus(ProgressTable.Row.STATUS_ERROR);
					println(language.getString("log.connectOrLoginFailure"), r.getSite(), protocol.getError());
					return true;
				}

				return false;
			}

			@Override
			public final void run() {
				work();

				for (IProtocol p2 : sites.values())
					p2.dispose();
			}

			protected abstract void work();
		}

		private class FileThread extends TransferThread {
			@Override
			public void work() {
				nThread.incrementAndGet();

				while (running.get()) {
					try {
						r = queue.take();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						break;
					}
					if (r.getSite() == null)
						break;

					if (isSkip())
						continue;

					protocol.setProgressListener(new Listener(r));
					synchronized (lock) {
						r.setStatus(ProgressTable.Row.STATUS_RUNNING);
					}

					if (r.isDirection()) {
						println(language.getString("log.uploading"), r.getLocal(), r.getRemote(), r.getSite());
						if (protocol.storeFile(r.getRemote(), r.getLocal())) {
							println(language.getString("log.uploaded"), r.getLocal(), r.getRemote(), r.getSite());
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
							}
						} else {
							println(language.getString("log.uploaderr"), r.getLocal(), r.getRemote(), r.getSite(), protocol.getError());
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_ERROR);
							}
						}
					} else {
						println(language.getString("log.downloading"), r.getLocal(), r.getRemote(), r.getSite());
						if (protocol.retrieveFile(r.getRemote(), r.getLocal())) {
							println(language.getString("log.downloaded"), r.getLocal(), r.getRemote(), r.getSite());
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
							}
						} else {
							println(language.getString("log.downloaderr"), r.getLocal(), r.getRemote(), r.getSite(), protocol.getError());
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_ERROR);
							}
						}
					}
				}

				nThread.decrementAndGet();
			}
		}

		private class DirectoryThread extends TransferThread {
			@Override
			public void work() {
				diring.set(true);

				while (running.get()) {
					synchronized (link) {
						if (link.isEmpty())
							r = null;
						else
							r = link.removeFirst();
					}
					if (r == null || r.getSite() == null)
						break;

					if (r.getStatus() == ProgressTable.Row.STATUS_READY) {
						if ("REG".equals(r.getType())) {
							try {
								queue.put(r);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else if ("DIR".equals(r.getType())) {
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_RUNNING);
							}
							if (r.isDirection()) {
								if (isSkip())
									continue;
								println(language.getString("log.mkdiring"), r.getRemote(), r.getSite());
								if (protocol.mkdir(r.getRemote())) {
									println(language.getString("log.mkdired"), r.getRemote(), r.getSite());
									synchronized (lock) {
										r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
									}
									f = new File(r.getLocal());
									if (f.isDirectory()) {
										File[] ls = f.listFiles();
										if (ls != null) {
											for (File f2 : ls) {
												FileTable.Row r2 = new FileTable.Row(f2);
												p = new ProgressTable.Row();
												p.setSite(r.getSite());
												p.setLocal(r.getLocal() + File.separator + r2.getName());
												p.setDirection(true);
												p.setRemote(r.getRemote() + "/" + r2.getName());
												p.setType(r2.getType());
												p.setSize(r2.getSize());
												p.setStatus(ProgressTable.Row.STATUS_READY);
												synchronized (progresses) {
													progresses.add(p);
												}
												synchronized (link) {
													link.add(p);
												}
											}
										}
									} else {
										println(language.getString("log.localDirList"), r.getLocal());
									}
								} else {
									println(language.getString("log.mkdirerr"), r.getRemote(), r.getSite(), protocol.getError());
									synchronized (lock) {
										r.setStatus(ProgressTable.Row.STATUS_ERROR);
									}
								}
							} else {
								f = new File(r.getLocal());
								if (f.exists()) {
									if (f.isDirectory()) {
										synchronized (lock) {
											r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
										}
									} else {
										synchronized (lock) {
											r.setStatus(ProgressTable.Row.STATUS_ERROR);
										}
										println(language.getString("log.notLocalDir"), r.getLocal());
									}
								} else {
									try {
										f.mkdir();
										synchronized (lock) {
											r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
										}
										println(language.getString("log.lmkdired"), r.getLocal());
									} catch (Exception e) {
										synchronized (lock) {
											r.setStatus(ProgressTable.Row.STATUS_ERROR);
										}
										println(language.getString("log.lmkdirerr"), r.getLocal(), e.getMessage());
									}
								}

								if (isSkip())
									continue;

								println(language.getString("log.dirlisting"), r.getRemote(), r.getSite());
								if (protocol.ls(r.getRemote(), files)) {
									println(language.getString("log.dirlisted"), r.getRemote(), r.getSite());
									for (FileTable.Row r2 : files) {
										p = new ProgressTable.Row();
										p.setSite(r.getSite());
										p.setLocal(r.getLocal() + File.separator + r2.getName());
										p.setDirection(false);
										p.setRemote(r.getRemote() + "/" + r2.getName());
										p.setType(r2.getType());
										p.setSize(r2.getSize());
										p.setStatus(ProgressTable.Row.STATUS_READY);
										synchronized (progresses) {
											progresses.add(p);
										}
										synchronized (link) {
											link.add(p);
										}
									}
								} else {
									println(language.getString("log.dirlisterr"), r.getRemote(), r.getSite(), protocol.getError());
								}
								files.clear();
							}
						} else {
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_ERROR);
							}
							println(language.getString("log.notSupportType"), r.getType(), r.getLocal());
						}
					}
				}

				diring.set(false);
			}
		}

		private class Listener implements IProtocol.ProgressListener {
			final ProgressTable.Row row;

			public Listener(ProgressTable.Row r) {
				row = r;
			}

			@Override
			public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
				row.setWritten(totalBytesTransferred);
			}
		};

		private class RefreshTask extends TimerTask {
			private final int NTHREAD = settings.getNthreads();

			@Override
			public void run() {
				if (!running.get() || (!diring.get() && queue.size() == 0)) {
					running.set(false);
					pool.execute(refreshRunnable);
					println(language.getString("log.transferEnd"));

					timer.cancel();
					timer = null;
				} else {
					for (int i = nThread.get(); i < NTHREAD && i < queue.size(); i++)
						new FileThread().start();

					for (int i = queue.size(); i < NTHREAD; i++)
						try {
							queue.put(new ProgressTable.Row());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					pool.execute(refreshRunnable);
				}
			}

			private Runnable refreshRunnable = new Runnable() {
				@Override
				public void run() {
					final List<ProgressTable.Row> list = new ArrayList<ProgressTable.Row>();
					synchronized (progresses) {
						while (!progresses.isEmpty())
							list.add(progresses.removeFirst());
					}

					synchronized (lock) {
						progressTable.getList().addAll(list);
						progressTable.fireTableDataChanged();
					}
				}
			};
		};
	}

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

	public class PopupMenu extends JPopupMenu {
		private static final long serialVersionUID = -6054322075287475764L;

		private Row row;

		public PopupMenu() {
			super();
		}

		public Row getRow() {
			return row;
		}

		public void setRow(Row r) {
			row = r;
		}

		public JMenuItem add(MenuItem menuItem) {
			return super.add((JMenuItem) menuItem);
		}
	}

	public class ProgressMenu extends JPopupMenu {
		private static final long serialVersionUID = -6054322075287475764L;

		private ProgressTable.Row row;

		public ProgressMenu() {
			super();
		}

		public ProgressTable.Row getRow() {
			return row;
		}

		public void setRow(ProgressTable.Row r) {
			row = r;
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
		public static final int KEY_FAVORITE = 11;
		public static final int KEY_SITE = 12;

		// menu lang
		public static final int KEY_ENGLISH = 20;
		public static final int KEY_CHINESE = 21;

		// menu about
		public static final int KEY_PROTOCOL = 30;
		public static final int KEY_APP = 31;

		// popup local menu
		public static final int KEY_UPLOAD = 40;
		public static final int KEY_LQUEUE = 41;
		public static final int KEY_LDELETE = 42;
		public static final int KEY_LMKDIR = 43;

		// popup remote menu
		public static final int KEY_DOWNLOAD = 50;
		public static final int KEY_RQUEUE = 51;
		public static final int KEY_RDELETE = 52;
		public static final int KEY_RMKDIR = 53;

		// popup progress menu
		public static final int KEY_TRANSFER = 60;
		public static final int KEY_CLEAN_ALL = 61;
		public static final int KEY_CLEAN_COMPLETED = 62;
		public static final int KEY_CLEAN_ERROR = 63;

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
				case KEY_FAVORITE:
					new FavoriteDialog(MainFrame.this, true).setVisible(true);
					break;
				case KEY_SITE:
					pool.execute(() -> {
						boolean exists = protocol != null && resKey.equals(protocol.getSite().getName());
						if (exists) {
							println(language.getString("log.disconnecting"), resKey);
							println(language.getString(protocol.logout() ? "log.disconnected" : "log.disconnecterr"), resKey);
						} else {
							if (protocol != null) {
								println(language.getString("log.disconnecting"), protocol.getSite().getName());
								protocol.logout();
								println(language.getString(protocol.logout() ? "log.disconnected" : "log.disconnecterr"), protocol.getSite().getName());
							}
							protocol = settings.getSites().get(resKey).create();
						}
						println(language.getString("log.connecting"), resKey, protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
						if (protocol.login()) {
							println(language.getString("log.connected"), resKey, protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
							favoriteMenu.setEnabled(true);
							initToolbar(protocol.getSite().getFavorites());
							if (exists) {
								enterAddr(false, getRemoteAddr());
							} else {
								if (protocol.getSite().getLocal() != null) {
									localTable.setAddr(protocol.getSite().getLocal());
								}
								if (protocol.getSite().getRemote() != null) {
									remoteTable.setAddr(protocol.getSite().getRemote());
								} else {
									remoteTable.setAddr(protocol.pwd());
								}
							}
						} else {
							println(language.getString("log.connecterr"), resKey, protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
							protocol.logout();
							protocol = null;
						}
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

				case KEY_UPLOAD:
					addProgress(true, true, localMenu.getRow());
					break;
				case KEY_LQUEUE:
					addProgress(false, true, localMenu.getRow());
					break;
				case KEY_LDELETE:
					break;
				case KEY_LMKDIR:
					break;
				case KEY_DOWNLOAD:
					addProgress(true, false, remoteMenu.getRow());
					break;
				case KEY_RQUEUE:
					addProgress(false, false, remoteMenu.getRow());
					break;
				case KEY_RDELETE:
					break;
				case KEY_RMKDIR:
					break;

				case KEY_TRANSFER:
					transfer.start();
					break;
				case KEY_CLEAN_ALL:
					progressTable.clear(true);
					progressCleanAllMenu.setEnabled(false);
					break;
				case KEY_CLEAN_COMPLETED:
					cleanProgress(ProgressTable.Row.STATUS_COMPLETED);
					break;
				case KEY_CLEAN_ERROR:
					cleanProgress(ProgressTable.Row.STATUS_ERROR);
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

		private void addProgress(boolean isStart, boolean isLocal, Row r) {
			ProgressTable.Row progress = new ProgressTable.Row();
			progress.setSite(protocol.getSite().getName());
			progress.setLocal(localTable.getPath(r.getName()));
			progress.setDirection(isLocal);
			progress.setRemote(remoteTable.getPath(r.getName()));
			progress.setType(r.getType());
			progress.setSize(r.getSize());
			progress.setStatus(ProgressTable.Row.STATUS_READY);

			synchronized (progressTable.getList()) {
				progressTable.getList().add(progress);
				progressTable.fireTableDataChanged();
			}
			transfer.add(progress);

			if (isStart) {
				transfer.start();
			}
		}

		private void cleanProgress(final int status) {
			synchronized (progressTable.getList()) {
				progressTable.getList().removeIf(new Predicate<ProgressTable.Row>() {
					@Override
					public boolean test(ProgressTable.Row t) {
						return t.getStatus() == status;
					}
				});
				progressTable.fireTableDataChanged();
			}
		}
	}

	public class FavoriteButton extends JButton implements MouseListener, KeyListener {
		private static final long serialVersionUID = 747095582318609981L;

		private Favorite favorite;

		public FavoriteButton(Favorite f) {
			super(f.getName());

			favorite = f;

			setToolTipText(String.format(language.getString("tip.favorite"), f.getName()));
			addMouseListener(this);
			addKeyListener(this);
		}

		private void click() {
			remoteTable.setAddr(favorite.getRemote());
			localTable.setAddr(favorite.getLocal());
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.isControlDown()) {
				toolBar.remove(this);
				toolBar.setVisible(toolBar.getComponentCount() > 0);
				protocol.getSite().getFavorites().remove(favorite.getName());
				settings.save();
			} else {
				click();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (!e.isControlDown() && !e.isAltDown() && !e.isShiftDown() && (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)) {
				click();
			}
		}
	}
}
