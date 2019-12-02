package com.talent518.ftp.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.dao.Site.Favorite;
import com.talent518.ftp.dao.Skin;
import com.talent518.ftp.gui.dialog.FavoriteDialog;
import com.talent518.ftp.gui.dialog.SettingsDialog;
import com.talent518.ftp.gui.dialog.SitesDialog;
import com.talent518.ftp.gui.filter.FileTypeFilter;
import com.talent518.ftp.gui.table.FileTable;
import com.talent518.ftp.gui.table.FileTable.Row;
import com.talent518.ftp.gui.table.ProgressTable;
import com.talent518.ftp.gui.ui.MainTabbedPaneUI;
import com.talent518.ftp.protocol.IProtocol;
import com.talent518.ftp.util.FileUtils;

public class MainFrame extends JFrame implements ComponentListener, WindowListener, FileTable.Listener, ProgressTable.Listener {
	private static final long serialVersionUID = 1723682780360129927L;
	private static final double DIVIDER = 0.5;

	private static final Logger log = Logger.getLogger(MainFrame.class);
	private static final SimpleDateFormat logFormat = new SimpleDateFormat("yyyyMMddHHmm");
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	public static final ImageIcon icon = new ImageIcon(MainFrame.class.getResource("/icons/app.png"));
	private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 2, 1, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

	private final Settings settings = Settings.instance();
	private final ResourceBundle language = Settings.language();
	private final Transfer transfer = new Transfer();

	private final JPanel content = new JPanel(true);

	private final JMenuBar menuBar = new JMenuBar();
	private final JToolBar toolBar = new JToolBar();
	private final PopupMenu localMenu = new PopupMenu();
	private final PopupMenu remoteMenu = new PopupMenu();
	private final ProgressMenu progressMenu = new ProgressMenu();
	private final ProgressMenu processedMenu = new ProgressMenu();
	private final LogMenu logMenu = new LogMenu();

	private final JSplitPane lrSplit = new JSplitPane();
	private final JSplitPane tbSplit = new JSplitPane();

	private final FileTable remoteTable = new FileTable(language.getString("remote"));
	private final FileTable localTable = new FileTable(language.getString("local"), true);

	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM, JTabbedPane.WRAP_TAB_LAYOUT);
	private final ProgressTable progressTable = new ProgressTable(true);
	private final ProgressTable processedTable = new ProgressTable(false);
	private final JTextArea logText = new JTextArea();

	private final JLabel leftStatus = new JLabel();
	private final JLabel rightStatus = new JLabel();

	private MenuItem favoriteMenu;
	private MenuItem localUploadMenu;
	private MenuItem localQueueMenu;
	private MenuItem progressTransferMenu;
	private MenuItem progressSuspendMenu;
	private MenuItem progressCleanMenu;

	private IProtocol protocol = null;

	public MainFrame() {
		super();

		setTitle(language.getString("app.name"));
		setSize(1024, 768);
		setLocationRelativeTo(null);
		setResizable(true);
		setIconImage(icon.getImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		content.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
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
		processedTable.setListener(this);

		localTable.setAddr(System.getProperty("user.home"));
		progressTable.load();
		processedTable.load();
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

	private Menu mSite;

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

		mSite = new Menu("menu.site", KeyEvent.VK_S);
		reInitSite();
		menuBar.add(mSite);

		Menu mLang = new Menu("menu.lang", KeyEvent.VK_L);
		mLang.add(new MenuItem("lang.english", KeyEvent.VK_E, MenuItem.KEY_ENGLISH).setKeyStroke("ctrl shift E").enabled(!settings.getLang().equals("en"))); // English Language
		mLang.add(new MenuItem("lang.chinese", KeyEvent.VK_C, MenuItem.KEY_CHINESE).setKeyStroke("ctrl shift C").enabled(!settings.getLang().equals("zh"))); // Chinese Language
		menuBar.add(mLang);

		Menu mSkin = new Menu("menu.skin", KeyEvent.VK_K);
		for (String key : Skin.keys())
			mSkin.add(new RadioMenuItem(key, key.equals(settings.getSkin()), RadioMenuItem.KEY_SKIN));
		menuBar.add(mSkin);

		Menu mAbout = new Menu("menu.about", KeyEvent.VK_A);
		mAbout.add(new MenuItem("about.protocol", KeyEvent.VK_P, MenuItem.KEY_PROTOCOL).setKeyStroke("ctrl shift P")); // About Protocol
		mAbout.add(new MenuItem("about.application", KeyEvent.VK_A, MenuItem.KEY_APP).setKeyStroke("ctrl shift A")); // About Application
		menuBar.add(mAbout);

		setJMenuBar(menuBar);
	}

	public void reInitSite() {
		mSite.removeAll();
		mSite.add(new MenuItem("site.manage", KeyEvent.VK_M, MenuItem.KEY_MANAGE).setKeyStroke("ctrl M"));
		mSite.add(favoriteMenu);
		mSite.addSeparator();
		int i = KeyEvent.VK_A;
		for (String site : settings.getSiteNames())
			mSite.add(new MenuItem(site, i, MenuItem.KEY_SITE).setKeyStroke("ctrl alt " + (char) i++));
	}

	private void initToolbar() {
		toolBar.setBorder(BorderFactory.createEmptyBorder());
		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		toolBar.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				tbSplit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			}

			@Override
			public void componentResized(ComponentEvent e) {
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				tbSplit.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
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

		logText.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				scrollBar.setValue(scrollBar.getMaximum());
				tabbedPane.setTitleAt(2, String.format(language.getString("tabbed.logging"), logText.getLineCount()));
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				scrollBar.setValue(scrollBar.getMaximum());
				tabbedPane.setTitleAt(2, String.format(language.getString("tabbed.logging"), logText.getLineCount()));
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				scrollBar.setValue(scrollBar.getMaximum());
				tabbedPane.setTitleAt(2, String.format(language.getString("tabbed.logging"), logText.getLineCount()));
			}
		});
		logText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
					logMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		logText.setEditable(false);

		progressTable.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				tabbedPane.setTitleAt(0, String.format(language.getString("tabbed.progress"), progressTable.getList().size()));
			}
		});
		final JScrollBar processedScrollBar = processedTable.getScrollPane().getVerticalScrollBar();
		processedTable.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				processedScrollBar.setValue(processedScrollBar.getMaximum());
				tabbedPane.setTitleAt(1, String.format(language.getString("tabbed.processed"), processedTable.getList().size()));
			}
		});

		tabbedPane.setUI(new MainTabbedPaneUI());
		tabbedPane.addTab(String.format(language.getString("tabbed.progress"), 0), progressTable);
		tabbedPane.addTab(String.format(language.getString("tabbed.processed"), 0), processedTable);
		tabbedPane.addTab(String.format(language.getString("tabbed.logging"), 0), scrollPane);

		lrSplit.setOneTouchExpandable(true);
		lrSplit.setContinuousLayout(true);
		lrSplit.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		lrSplit.setDividerLocation(0.5);
		lrSplit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		lrSplit.setLeftComponent(remoteTable);
		lrSplit.setRightComponent(localTable);

		tbSplit.setOneTouchExpandable(true);
		tbSplit.setContinuousLayout(true);
		tbSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		tbSplit.setDividerLocation(0.5);
		tbSplit.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		tbSplit.setTopComponent(lrSplit);
		tbSplit.setBottomComponent(tabbedPane);

		content.add(tbSplit, BorderLayout.CENTER);
	}

	private void initStatusBar() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout(10, 10));
		panel.add(leftStatus, BorderLayout.CENTER);
		panel.add(rightStatus, BorderLayout.EAST);
		content.add(panel, BorderLayout.SOUTH);
	}

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
		progressSuspendMenu = new MenuItem("progress.suspend", KeyEvent.VK_S, MenuItem.KEY_SUSPEND);
		progressSuspendMenu.setEnabled(false);
		progressCleanMenu = new MenuItem("progress.clean", KeyEvent.VK_C, MenuItem.KEY_CLEAN);
		progressMenu.add(progressTransferMenu);
		progressMenu.add(progressSuspendMenu);
		progressMenu.addSeparator();
		progressMenu.add(progressCleanMenu);

		processedMenu.add(new MenuItem("processed.cleanAll", KeyEvent.VK_A, MenuItem.KEY_CLEAN_ALL));
		processedMenu.addSeparator();
		processedMenu.add(new MenuItem("processed.cleanCompleted", KeyEvent.VK_C, MenuItem.KEY_CLEAN_COMPLETED));
		processedMenu.add(new MenuItem("processed.cleanError", KeyEvent.VK_E, MenuItem.KEY_CLEAN_ERROR));
		processedMenu.addSeparator();
		processedMenu.add(new MenuItem("processed.errorRetransfer", KeyEvent.VK_R, MenuItem.KEY_ERROR_RETRANSFER));

		logMenu.add(new MenuItem("logMenu.save", KeyEvent.VK_S, MenuItem.KEY_LOG_SAVE));
		logMenu.add(new MenuItem("logMenu.saveAs", KeyEvent.VK_A, MenuItem.KEY_LOG_SAVE_AS));
		logMenu.addSeparator();
		logMenu.add(new MenuItem("logMenu.clean", KeyEvent.VK_C, MenuItem.KEY_LOG_CLEAN));
	}

	private void closeWindow() {
		if (transfer.isRunning()) {
			transfer.stop(true);
		} else {
			progressTable.save();
			processedTable.save();
			System.exit(0);
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		lrSplit.setDividerLocation(DIVIDER);
		tbSplit.setDividerLocation(DIVIDER);
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
		LoadFrame.close();
	}

	@Override
	public void windowClosing(WindowEvent e) {
		log.debug(e);
		closeWindow();
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
				if (!protocol.isConnected() && protocol.isLogined()) {
					protocol.logout();
					println(language.getString("log.connecting"), protocol.getSite().getName(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					if (protocol.login())
						println(language.getString("log.connected"), protocol.getSite().getName(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					else
						println(language.getString("log.connecterr"), protocol.getSite().getName(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername(), protocol.getError());
				}
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
		if (r != null) {
			leftStatus.setText(language.getString(local ? "local" : "remote") + " " + language.getString("type." + r.getType()) + " \"" + r.getName() + "\" " + FileUtils.formatSize(r.getSize()));
		} else {
			leftStatus.setText("");
		}
	}

	@Override
	public void doubleClicked(boolean local, int i, Row r) {
		if (r == null)
			return;

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
			leftStatus.setText(language.getString(local ? "local" : "remote") + " " + language.getString("type." + r.getType()) + " \"" + r.getName() + "\" " + FileUtils.formatSize(r.getSize()));
		}
	}

	@Override
	public void rightClicked(boolean isProgress, int i, ProgressTable.Row r, MouseEvent e) {
		if (isProgress) {
			if (transfer.isRunning()) {
				progressTransferMenu.setEnabled(false);
				progressCleanMenu.setEnabled(false);
				progressSuspendMenu.setEnabled(true);
			} else {
				progressTransferMenu.setEnabled(false);
				progressSuspendMenu.setEnabled(false);
				for (ProgressTable.Row r2 : progressTable.getList()) {
					if (r2.getStatus() == ProgressTable.Row.STATUS_READY) {
						progressTransferMenu.setEnabled(true);
						break;
					}
				}
				progressCleanMenu.setEnabled(progressTable.getList().size() > 0);
			}

			progressMenu.setRow(r);
			progressMenu.show(e.getComponent(), e.getX(), e.getY());
		} else {
			processedMenu.setRow(r);
			processedMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	@Override
	public void selectedRow(boolean isProgress, int i, ProgressTable.Row r) {
		if (r != null) {
			leftStatus.setText(r.getSite() + " \"" + r.getLocal() + "\" " + (r.isDirection() ? "=>" : "<=") + " \"" + r.getRemote() + "\" " + language.getString("type." + r.getType()) + " " + FileUtils.formatSize(r.getSize()));
		} else {
			leftStatus.setText("");
		}
	}

	@Override
	public void doubleClicked(boolean isProgress, int i, ProgressTable.Row r) {
		if (r != null) {
			leftStatus.setText(r.getSite() + " \"" + r.getLocal() + "\" " + (r.isDirection() ? "=>" : "<=") + " \"" + r.getRemote() + "\" " + language.getString("type." + r.getType()) + " " + FileUtils.formatSize(r.getSize()));
		} else {
			leftStatus.setText("");
		}
	}

	public class Transfer {
		private AtomicBoolean running = new AtomicBoolean(false);
		private AtomicBoolean diring = new AtomicBoolean(false);
		private LinkedBlockingQueue<ProgressTable.Row> queue = new LinkedBlockingQueue<ProgressTable.Row>(1000000);
		private LinkedList<ProgressTable.Row> link = new LinkedList<ProgressTable.Row>();
		private AtomicInteger nThread = new AtomicInteger(0);
		private AtomicInteger nReady = new AtomicInteger(0);
		private AtomicInteger nRunning = new AtomicInteger(0);
		private AtomicInteger nCompleted = new AtomicInteger(0);
		private AtomicInteger nError = new AtomicInteger(0);
		private AtomicInteger nSkip = new AtomicInteger(0);
		private AtomicInteger nCount = new AtomicInteger(0);
		private AtomicLong nTotalBytes = new AtomicLong(0);
		private AtomicLong nUpBytes = new AtomicLong(0);
		private AtomicLong nDownBytes = new AtomicLong(0);
		private LinkedList<ProgressTable.Row> progresses = new LinkedList<ProgressTable.Row>();
		private List<ProgressTable.Row> lock;
		private Timer timer;
		private AtomicBoolean closed = new AtomicBoolean(false);

		public void start() {
			if (isRunning())
				return;

			println(language.getString("log.transferBegin"));

			running.set(true);
			nReady.set(0);
			nRunning.set(0);
			nCompleted.set(0);
			nError.set(0);
			nSkip.set(0);
			nCount.set(0);
			nTotalBytes.set(0);
			nUpBytes.set(0);
			nDownBytes.set(0);
			rightStatus.setText("");

			queue.clear();
			link.clear();

			lock = progressTable.getList();
			timer = new Timer("transfer", true);
			timer.schedule(new RefreshTask(), 100, 100);
			timer.schedule(new BytesTask(), 1000, 1000);

			synchronized (lock) {
				addAll(progressTable.getList());
			}
		}

		public boolean isRunning() {
			return running.get() || diring.get() || nThread.get() > 0;
		}

		public void stop() {
			stop(false);
		}

		public void stop(boolean b) {
			running.set(false);
			closed.set(b);
		}

		public void add(ProgressTable.Row row) {
			if (isRunning()) {
				// pool.execute(new ProgressQueue(row));
				new ProgressQueue(row).run();
			}
		}

		public void addAll(List<ProgressTable.Row> rows) {
			if (isRunning()) {
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

				if (isRunning() && !diring.getAndSet(true)) {
					running.set(true);
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
					synchronized (lock) {
						r.setStatus(ProgressTable.Row.STATUS_ERROR);
					}
					println(language.getString("log.siteNotExists"), r.getSite());
					return true;
				} else {
					protocol = settings.getSites().get(r.getSite()).create();

					println(language.getString("log.connecting"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					if (protocol.login())
						println(language.getString("log.connected"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					else
						println(language.getString("log.connecterr"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername(), protocol.getError());

					sites.put(r.getSite(), protocol);
				}

				if (!protocol.isConnected() && protocol.isLogined()) {
					protocol.logout();
					println(language.getString("log.connecting"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					if (protocol.login())
						println(language.getString("log.connected"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					else
						println(language.getString("log.connecterr"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername(), protocol.getError());
				}

				if (!protocol.isConnected() || !protocol.isLogined()) {
					synchronized (lock) {
						r.setStatus(ProgressTable.Row.STATUS_ERROR);
					}
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

					if (isSkip()) {
						nError.incrementAndGet();
						continue;
					}

					protocol.setProgressListener(new Listener(r));
					synchronized (lock) {
						r.setStatus(ProgressTable.Row.STATUS_RUNNING);
					}

					nRunning.incrementAndGet();
					if (r.isDirection()) {
						println(language.getString("log.uploading"), r.getLocal(), r.getRemote(), r.getSite());
						if (protocol.storeFile(r.getRemote(), r.getLocal())) {
							println(language.getString("log.uploaded"), r.getLocal(), r.getRemote(), r.getSite());
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
							}
							nCompleted.incrementAndGet();
						} else {
							println(language.getString("log.uploaderr"), r.getLocal(), r.getRemote(), r.getSite(), protocol.getError());
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_ERROR);
							}
							nError.incrementAndGet();
						}
					} else {
						println(language.getString("log.downloading"), r.getLocal(), r.getRemote(), r.getSite());
						if (protocol.retrieveFile(r.getRemote(), r.getLocal())) {
							println(language.getString("log.downloaded"), r.getLocal(), r.getRemote(), r.getSite());
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
							}
							nCompleted.incrementAndGet();
						} else {
							println(language.getString("log.downloaderr"), r.getLocal(), r.getRemote(), r.getSite(), protocol.getError());
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_ERROR);
							}
							nError.incrementAndGet();
						}
					}
					nRunning.decrementAndGet();
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

					nCount.incrementAndGet();

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
							nRunning.incrementAndGet();
							if (r.isDirection()) {
								if (isSkip()) {
									nRunning.decrementAndGet();
									nSkip.incrementAndGet();
									continue;
								}
								println(language.getString("log.mkdiring"), r.getRemote(), r.getSite());
								if (protocol.mkdir(r.getRemote())) {
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

										println(language.getString("log.mkdired"), r.getRemote(), r.getSite());
										synchronized (lock) {
											r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
										}
										nCompleted.incrementAndGet();
									} else {
										println(language.getString("log.localDirList"), r.getLocal());
										synchronized (lock) {
											r.setStatus(ProgressTable.Row.STATUS_ERROR);
										}
										nError.incrementAndGet();
									}
								} else {
									println(language.getString("log.mkdirerr"), r.getRemote(), r.getSite(), protocol.getError());
									synchronized (lock) {
										r.setStatus(ProgressTable.Row.STATUS_ERROR);
									}
									nError.incrementAndGet();
								}
							} else {
								f = new File(r.getLocal());
								if (f.exists()) {
									if (!f.isDirectory()) {
										println(language.getString("log.notLocalDir"), r.getLocal());
										synchronized (lock) {
											r.setStatus(ProgressTable.Row.STATUS_ERROR);
										}
										nError.incrementAndGet();
										nRunning.decrementAndGet();
										continue;
									}
								} else {
									try {
										f.mkdir();
										println(language.getString("log.lmkdired"), r.getLocal());
									} catch (Exception e) {
										println(language.getString("log.lmkdirerr"), r.getLocal(), e.getMessage());
										synchronized (lock) {
											r.setStatus(ProgressTable.Row.STATUS_ERROR);
										}
										nError.incrementAndGet();
										nRunning.decrementAndGet();
										continue;
									}
								}

								if (isSkip()) {
									nRunning.decrementAndGet();
									nSkip.incrementAndGet();
									continue;
								}

								println(language.getString("log.dirlisting"), r.getRemote(), r.getSite());
								if (protocol.ls(r.getRemote(), files)) {
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
									println(language.getString("log.dirlisted"), r.getRemote(), r.getSite());
									synchronized (lock) {
										r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
									}
									nCompleted.incrementAndGet();
								} else {
									println(language.getString("log.dirlisterr"), r.getRemote(), r.getSite(), protocol.getError());
									synchronized (lock) {
										r.setStatus(ProgressTable.Row.STATUS_ERROR);
									}
									nError.incrementAndGet();
								}
								files.clear();
							}
							nRunning.decrementAndGet();
						} else {
							println(language.getString("log.notSupportType"), r.getType(), r.getLocal());
							synchronized (lock) {
								r.setStatus(ProgressTable.Row.STATUS_ERROR);
							}
							nError.incrementAndGet();
						}
					} else {
						nSkip.incrementAndGet();
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
				long delta = totalBytesTransferred - row.getWritten();
				nTotalBytes.getAndAdd(delta);
				if (row.isDirection()) {
					nUpBytes.getAndAdd(delta);
				} else {
					nDownBytes.getAndAdd(delta);
				}
				row.setWritten(totalBytesTransferred);
			}
		}

		private class BytesTask extends TimerTask {
			final String format = language.getString("transfer.speed");

			@Override
			public void run() {
				long down = nDownBytes.getAndSet(0);
				long up = nUpBytes.getAndSet(0);
				long total = nTotalBytes.getAndSet(0);

				String totalBytes = FileUtils.formatSize(total);
				String upBytes = FileUtils.formatSize(up);
				String downBytes = FileUtils.formatSize(down);

				leftStatus.setText(String.format(format, totalBytes, upBytes, downBytes));
			}
		}

		private class RefreshTask extends TimerTask {
			private final int NTHREAD = settings.getNthreads();
			private final Predicate<ProgressTable.Row> filter = new Predicate<ProgressTable.Row>() {
				@Override
				public boolean test(ProgressTable.Row t) {
					if (t.getStatus() == ProgressTable.Row.STATUS_COMPLETED || t.getStatus() == ProgressTable.Row.STATUS_ERROR) {
						processedTable.getList().add(t);
						return true;
					} else {
						return false;
					}
				}
			};

			@Override
			public void run() {
				if (isRunning()) {
					if (!diring.get() && queue.size() == 0) {
						running.set(false);
					}

					if (running.get())
						for (int i = nThread.get(); i < NTHREAD && i < queue.size(); i++)
							new FileThread().start();
					else {
						if (diring.get() && queue.remainingCapacity() == 0)
							try {
								queue.take();
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						for (int i = queue.size(); i < NTHREAD && i < nThread.get(); i++)
							try {
								queue.put(new ProgressTable.Row());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
					}
				} else {
					println(language.getString("log.transferEnd"));

					timer.cancel();
					timer = null;
				}

				EventQueue.invokeLater(() -> {
					final List<ProgressTable.Row> list = new ArrayList<ProgressTable.Row>();
					synchronized (progresses) {
						while (!progresses.isEmpty())
							list.add(progresses.removeFirst());
					}

					synchronized (lock) {
						synchronized (processedTable.getList()) {
							list.removeIf(filter);
							progressTable.getList().removeIf(filter);
						}
						progressTable.getList().addAll(list);
					}
					progressTable.fireTableDataChanged();
					processedTable.fireTableDataChanged();

					if (timer == null && closed.get()) {
						progressTable.save();
						processedTable.save();
						System.exit(0);
					}
				});

				rightStatus.setText(String.format(language.getString("status.progress"), nThread.get(), queue.size(), nCount.get(), nSkip.get(), nReady.get(), nRunning.get(), nCompleted.get(), nError.get()));
			}
		}
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

		public JMenuItem add(RadioMenuItem menuItem) {
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
		private static final long serialVersionUID = -6054322075287475765L;

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

	public class LogMenu extends JPopupMenu {
		private static final long serialVersionUID = -6054322075287475766L;

		public LogMenu() {
			super();
		}

		public JMenuItem add(MenuItem menuItem) {
			return super.add((JMenuItem) menuItem);
		}
	}

	public class RadioMenuItem extends JRadioButtonMenuItem implements Action {
		private static final long serialVersionUID = -1218722648519492109L;

		// menu skin
		public static final int KEY_SKIN = 0;

		String resKey, resVal;
		private int key;

		public RadioMenuItem(String res, boolean selected, int key) {
			super();

			resKey = res;
			resVal = language.getString(res);
			setText(resVal);
			setSelected(selected);

			this.key = key;

			addActionListener(this);
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

		@Override
		public void actionPerformed(ActionEvent e) {
			log.debug("Performed: resKey = " + resKey + ", resVal = " + resVal);

			switch (key) {
				case KEY_SKIN:
					settings.setSkin(resKey);
					settings.save();
					dispose();
					LoadFrame.main(new String[0]);
					break;
			}
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
		public static final int KEY_SUSPEND = 61;
		public static final int KEY_CLEAN = 62;

		// popup processed menu
		public static final int KEY_CLEAN_ALL = 70;
		public static final int KEY_CLEAN_COMPLETED = 71;
		public static final int KEY_CLEAN_ERROR = 72;
		public static final int KEY_ERROR_RETRANSFER = 73;

		// popup log menu
		public static final int KEY_LOG_SAVE = 80;
		public static final int KEY_LOG_SAVE_AS = 81;
		public static final int KEY_LOG_CLEAN = 82;

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
					new SettingsDialog(MainFrame.this, true).setVisible(true);
					break;
				case KEY_QUIT:
					closeWindow();
					break;
				case KEY_MANAGE:
					new SitesDialog(MainFrame.this, true).setVisible(true);
					break;
				case KEY_FAVORITE:
					new FavoriteDialog(MainFrame.this, true).setVisible(true);
					break;
				case KEY_SITE:
					pool.execute(() -> {
						if (protocol != null) {
							protocol.dispose();
						}
						protocol = settings.getSites().get(resKey).create();
						println(language.getString("log.connecting"), resKey, protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
						if (protocol.login()) {
							println(language.getString("log.connected"), resKey, protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
							favoriteMenu.setEnabled(true);
							EventQueue.invokeLater(() -> {
								setTitle(protocol.getSite().getName() + " - " + language.getString("app.name"));
								initToolbar(protocol.getSite().getFavorites());

								if (protocol.getSite().getLocal() != null && protocol.getSite().getLocal().length() > 0) {
									localTable.setAddr(protocol.getSite().getLocal());
								}
								if (protocol.getSite().getRemote() != null && protocol.getSite().getRemote().length() > 0) {
									remoteTable.setAddr(protocol.getSite().getRemote());
								} else {
									remoteTable.setAddr(protocol.pwd());
								}
							});
						} else {
							println(language.getString("log.connecterr"), resKey, protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername(), protocol.getError());
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
				case KEY_SUSPEND:
					transfer.stop();
					break;
				case KEY_CLEAN:
					progressTable.clear(true);
					progressCleanMenu.setEnabled(false);
					break;

				case KEY_CLEAN_ALL:
					processedTable.clear(true);
					break;
				case KEY_CLEAN_COMPLETED:
					cleanProgress(ProgressTable.Row.STATUS_COMPLETED);
					break;
				case KEY_CLEAN_ERROR:
					cleanProgress(ProgressTable.Row.STATUS_ERROR);
					break;
				case KEY_ERROR_RETRANSFER:
					errorRetransfer();
					break;

				case KEY_LOG_SAVE:
					logSave(new File(Settings.LOG_PATH + "ftp-gui.log"));
					break;
				case KEY_LOG_SAVE_AS:
					File f = new File(System.getProperty("user.home"));
					JFileChooser chooser = new JFileChooser(f);
					chooser.setDialogTitle(language.getString("chooser.log.title"));
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.setApproveButtonText(language.getString("chooser.save"));
					chooser.addChoosableFileFilter(new FileTypeFilter(".log", language.getString("chooser.log.type")));
					chooser.setSelectedFile(new File(f, "ftp-gui-" + logFormat.format(new Date()) + ".log"));
					if (chooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						logSave(chooser.getSelectedFile());
					}
					break;
				case KEY_LOG_CLEAN:
					logText.setText("");
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
			}
			progressTable.fireTableDataChanged();

			if (!transfer.isRunning() && isStart) {
				transfer.start();
			} else {
				transfer.add(progress);
			}
		}

		private void cleanProgress(final int status) {
			synchronized (processedTable.getList()) {
				processedTable.getList().removeIf(new Predicate<ProgressTable.Row>() {
					@Override
					public boolean test(ProgressTable.Row t) {
						return t.getStatus() == status;
					}
				});
			}
			processedTable.fireTableDataChanged();
		}

		private void errorRetransfer() {
			final List<ProgressTable.Row> list = new ArrayList<ProgressTable.Row>();
			synchronized (processedTable.getList()) {
				processedTable.getList().removeIf(new Predicate<ProgressTable.Row>() {
					@Override
					public boolean test(ProgressTable.Row t) {
						if (t.getStatus() == ProgressTable.Row.STATUS_ERROR) {
							t.setWritten(0);
							t.setProgress(0);
							t.setStatus(ProgressTable.Row.STATUS_READY);
							list.add(t);
							return true;
						}
						return false;
					}
				});
			}
			synchronized (progressTable.getList()) {
				progressTable.getList().addAll(list);
			}
			progressTable.fireTableDataChanged();
			processedTable.fireTableDataChanged();
			if (!transfer.isRunning()) {
				transfer.start();
			} else {
				transfer.addAll(list);
			}
		}

		private void logSave(File f) {
			FileWriter writer = null;
			try {
				File path = f.getParentFile();
				if (!path.isDirectory()) {
					path.mkdir();
				}
				writer = new FileWriter(f);
				writer.write(logText.getText());
				writer.flush();
				logText.setText("");
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
	}

	public class FavoriteButton extends JButton implements MouseListener, KeyListener {
		private static final long serialVersionUID = 747095582318609981L;

		private Favorite favorite;

		public FavoriteButton(Favorite f) {
			super(f.getName());

			favorite = f;

			final int W = getFont().getSize();
			int w = W * 2;

			for (char c : f.getName().toCharArray()) {
				w += (c > 128 ? W : W / 2);
			}

			setPreferredSize(new Dimension(w, 30));
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
