package com.talent518.ftp.gui;

import java.awt.BorderLayout;
import java.awt.Desktop;
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
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
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
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.dao.Site;
import com.talent518.ftp.dao.Site.Favorite;
import com.talent518.ftp.dao.Skin;
import com.talent518.ftp.gui.dialog.ConfirmDialog;
import com.talent518.ftp.gui.dialog.FavoriteDialog;
import com.talent518.ftp.gui.dialog.NameDialog;
import com.talent518.ftp.gui.dialog.ResumeDialog;
import com.talent518.ftp.gui.dialog.SettingsDialog;
import com.talent518.ftp.gui.dialog.SitesDialog;
import com.talent518.ftp.gui.filter.FileTypeFilter;
import com.talent518.ftp.gui.table.FileTable;
import com.talent518.ftp.gui.table.FileTable.Row;
import com.talent518.ftp.gui.table.ProgressTable;
import com.talent518.ftp.gui.ui.GlassPane;
import com.talent518.ftp.gui.ui.MainTabbedPaneUI;
import com.talent518.ftp.protocol.IProtocol;
import com.talent518.ftp.util.FileUtils;

public class MainFrame extends JFrame implements ComponentListener, WindowListener, FileTable.Listener, ProgressTable.Listener {
	private static final long serialVersionUID = 1723682780360129927L;
	private static final double DIVIDER_LR = 0.6;
	private static final double DIVIDER_TB = 0.5;

	private static final Logger log = Logger.getLogger(MainFrame.class);
	private static final SimpleDateFormat logFormat = new SimpleDateFormat("yyyyMMddHHmm");
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	public static final ImageIcon icon = new ImageIcon(MainFrame.class.getResource("/icons/app.png"));

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

	private ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 2, 1, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());

	private MenuItem favoriteMenu;
	private MenuItem localUploadMenu;
	private MenuItem localUploadAsMenu;
	private MenuItem localQueueMenu;
	private MenuItem localQueueAsMenu;
	private MenuItem progressTransferMenu;
	private MenuItem progressSuspendMenu;
	private MenuItem progressCleanMenu;
	private MenuItem progressDeleteMenu;

	private IProtocol protocol = null;

	public MainFrame() {
		super();

		setTitle(language.getString("app.name"));
		setSize(1024, 768);
		setMinimumSize(new Dimension(800, 600));
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

		initLogBuffer();
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

	private Menu mSite, mLang, mSkin;

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

		mLang = new Menu("menu.lang", KeyEvent.VK_L);
		mLang.add(new MenuItem("lang.english", KeyEvent.VK_E, MenuItem.KEY_ENGLISH).setKeyStroke("ctrl shift E").enabled(!settings.getLang().equals("en"))); // English Language
		mLang.add(new MenuItem("lang.chinese", KeyEvent.VK_C, MenuItem.KEY_CHINESE).setKeyStroke("ctrl shift C").enabled(!settings.getLang().equals("zh"))); // Chinese Language
		menuBar.add(mLang);

		mSkin = new Menu("menu.skin", KeyEvent.VK_K);
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
		toolBar.setVisible(false);
		toolBar.setFloatable(false);
		content.add(toolBar, BorderLayout.NORTH);
	}

	public void initToolbar(Map<String, Favorite> favorites) {
		toolBar.removeAll();

		for (Favorite f : favorites.values()) {
			toolBar.add(new FavoriteButton(f));
		}

		toolBar.setVisible(toolBar.getComponentCount() > 0);
		toolBar.validate();
		toolBar.repaint();
	}

	private void initSplit() {
		JScrollPane scrollPane = new JScrollPane(logText, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();

		logText.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				scrollBar.setValue(scrollBar.getMaximum());
				tabbedPane.setTitleAt(2, String.format(language.getString("tabbed.logging"), logText.getLineCount() - 1));
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				scrollBar.setValue(scrollBar.getMaximum());
				tabbedPane.setTitleAt(2, String.format(language.getString("tabbed.logging"), logText.getLineCount() - 1));
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				scrollBar.setValue(scrollBar.getMaximum());
				tabbedPane.setTitleAt(2, String.format(language.getString("tabbed.logging"), logText.getLineCount() - 1));
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
		processedTable.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
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
		lrSplit.setBorder(BorderFactory.createEmptyBorder());
		lrSplit.setLeftComponent(remoteTable);
		lrSplit.setRightComponent(localTable);

		tbSplit.setOneTouchExpandable(true);
		tbSplit.setContinuousLayout(true);
		tbSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		tbSplit.setDividerLocation(0.5);
		tbSplit.setBorder(BorderFactory.createEmptyBorder());
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
		localUploadAsMenu = new MenuItem("local.uploadas", KeyEvent.VK_A, MenuItem.KEY_UPLOAD_AS);
		localQueueMenu = new MenuItem("local.queue", KeyEvent.VK_Q, MenuItem.KEY_LQUEUE);
		localQueueAsMenu = new MenuItem("local.queueas", KeyEvent.VK_S, MenuItem.KEY_LQUEUE_AS);
		localMenu.add(localUploadMenu);
		localMenu.add(localUploadAsMenu);
		localMenu.add(localQueueMenu);
		localMenu.add(localQueueAsMenu);
		localMenu.addSeparator();
		localMenu.add(new MenuItem("local.delete", KeyEvent.VK_D, MenuItem.KEY_LDELETE));
		localMenu.add(new MenuItem("local.mkdir", KeyEvent.VK_M, MenuItem.KEY_LMKDIR));
		localMenu.add(new MenuItem("local.rename", KeyEvent.VK_R, MenuItem.KEY_LRENAME));

		remoteMenu.add(new MenuItem("remote.download", KeyEvent.VK_L, MenuItem.KEY_DOWNLOAD));
		remoteMenu.add(new MenuItem("remote.downloadas", KeyEvent.VK_A, MenuItem.KEY_DOWNLOAD_AS));
		remoteMenu.add(new MenuItem("remote.queue", KeyEvent.VK_Q, MenuItem.KEY_RQUEUE));
		remoteMenu.add(new MenuItem("remote.queueas", KeyEvent.VK_S, MenuItem.KEY_RQUEUE_AS));
		remoteMenu.addSeparator();
		remoteMenu.add(new MenuItem("remote.delete", KeyEvent.VK_D, MenuItem.KEY_RDELETE));
		remoteMenu.add(new MenuItem("remote.mkdir", KeyEvent.VK_M, MenuItem.KEY_RMKDIR));
		remoteMenu.add(new MenuItem("remote.rename", KeyEvent.VK_R, MenuItem.KEY_RRENAME));

		progressTransferMenu = new MenuItem("progress.transfer", KeyEvent.VK_T, MenuItem.KEY_TRANSFER);
		progressTransferMenu.setEnabled(false);
		progressSuspendMenu = new MenuItem("progress.suspend", KeyEvent.VK_S, MenuItem.KEY_SUSPEND);
		progressSuspendMenu.setEnabled(false);
		progressCleanMenu = new MenuItem("progress.clean", KeyEvent.VK_C, MenuItem.KEY_CLEAN);
		progressDeleteMenu = new MenuItem("progress.delete", KeyEvent.VK_D, MenuItem.KEY_DELETE);
		progressMenu.add(progressTransferMenu);
		progressMenu.add(progressSuspendMenu);
		progressMenu.addSeparator();
		progressMenu.add(progressCleanMenu);
		progressMenu.add(progressDeleteMenu);

		processedMenu.add(new MenuItem("processed.cleanAll", KeyEvent.VK_A, MenuItem.KEY_CLEAN_ALL));
		processedMenu.addSeparator();
		processedMenu.add(new MenuItem("processed.cleanCompleted", KeyEvent.VK_C, MenuItem.KEY_CLEAN_COMPLETED));
		processedMenu.add(new MenuItem("processed.cleanSkip", KeyEvent.VK_S, MenuItem.KEY_CLEAN_SKIP));
		processedMenu.add(new MenuItem("processed.cleanError", KeyEvent.VK_E, MenuItem.KEY_CLEAN_ERROR));
		processedMenu.addSeparator();
		processedMenu.add(new MenuItem("processed.errorRetransfer", KeyEvent.VK_R, MenuItem.KEY_ERROR_RETRANSFER));

		logMenu.add(new MenuItem("logMenu.save", KeyEvent.VK_S, MenuItem.KEY_LOG_SAVE));
		logMenu.add(new MenuItem("logMenu.saveAs", KeyEvent.VK_A, MenuItem.KEY_LOG_SAVE_AS));
		logMenu.addSeparator();
		logMenu.add(new MenuItem("logMenu.clean", KeyEvent.VK_C, MenuItem.KEY_LOG_CLEAN));
	}

	private void closeWindow() {
		showLoading();

		transfer.unwatch();

		if (transfer.isRunning()) {
			transfer.stop(true);
		} else {
			progressTable.save();
			processedTable.save();
			System.exit(0);
		}
	}

	public void restart() {
		if (transfer.isRunning())
			return;

		logTimer.cancel();
		transfer.stop();
		transfer.unwatch();

		pool.shutdownNow();
		dispose();
		LoadFrame.main(new String[0]);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		lrSplit.setDividerLocation(DIVIDER_LR);
		tbSplit.setDividerLocation(DIVIDER_TB);

		glassPane.setBounds(0, 0, getWidth(), getHeight());
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

	private final StringBuffer logBuffer = new StringBuffer();
	private final Timer logTimer = new Timer("logTimer", true);
	private final TimerTask logTimerTask = new TimerTask() {
		@Override
		public void run() {
			final String log;
			synchronized (logBuffer) {
				log = logBuffer.toString();
				logBuffer.delete(0, log.length());
			}
			EventQueue.invokeLater(() -> {
				logText.append(log);
				int line = logText.getLineCount() - settings.getLogLines() - 2;
				if (line > 0) {
					try {
						logText.replaceRange("", 0, logText.getLineEndOffset(line));
					} catch (BadLocationException e) {
					}
				}
			});
		}
	};

	private void initLogBuffer() {
		logTimer.schedule(logTimerTask, 250, 250);
	}

	public void println(String str) {
		log.debug(str);

		synchronized (logBuffer) {
			logBuffer.append(timeFormat.format(new Date()));
			logBuffer.append(' ');
			logBuffer.append(str);
			logBuffer.append('\n');
		}
	}

	@SuppressWarnings("resource")
	public void println(String format, Object... args) {
		println(new Formatter().format(format, args).toString());
	}

	private final IProtocol.DeleteListener remoteDeleteListener = new IProtocol.DeleteListener() {
		@Override
		public void unlink(String remote) {
			println(language.getString("remote.delete.file.success"), remote);
		}

		@Override
		public void rmdir(String remote) {
			println(language.getString("remote.delete.dir.success"), remote);
		}

		@Override
		public void ls(String remote) {
			println(language.getString("log.remoteListed"), remote);
		}
	};

	private final FileUtils.DeleteListener localDeleteListener = new FileUtils.DeleteListener() {
		@Override
		public void unlink(String remote) {
			println(language.getString("local.delete.file.success"), remote);
		}

		@Override
		public void rmdir(String remote) {
			println(language.getString("local.delete.dir.success"), remote);
		}

		@Override
		public void ls(String remote) {
			println(language.getString("log.localListed"), remote);
		}
	};

	private final GlassPane glassPane = new GlassPane() {
		private static final long serialVersionUID = -4818814688439077751L;

		public void closeEvent() {
			pool.shutdownNow();
			pool = new ThreadPoolExecutor(1, 2, 1, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());
		};
	};

	private void showLoading() {
		EventQueue.invokeLater(() -> {
			if (glassPane.isRunning())
				return;

			glassPane.setBounds(0, 0, getWidth(), getHeight());
			setGlassPane(glassPane);
			glassPane.start();
		});
	}

	private void hideLoading() {
		EventQueue.invokeLater(() -> {
			glassPane.stop();
		});
	}

	private void relogin(IProtocol protocol) {
		if (!protocol.isConnected() && protocol.isLogined()) {
			protocol.logout();
			println(language.getString("log.connecting"), protocol.getSite().getName(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
			if (protocol.login())
				println(language.getString("log.connected"), protocol.getSite().getName(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
			else
				println(language.getString("log.connecterr"), protocol.getSite().getName(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername(), protocol.getError());
		}
	}

	private void enterSite(String site) {
		enterSite(site, null, null);
	}

	private void enterSite(String key, String local, String remote) {
		showLoading();
		pool.execute(() -> {
			Site site = settings.getSites().get(key);
			IProtocol p = site.create();
			p.setDeleteListener(remoteDeleteListener);
			println(language.getString("log.connecting"), site.getName(), site.getHost(), site.getPort(), site.getUsername());
			if (p.login()) {
				if (protocol != null) {
					protocol.dispose();
				}
				protocol = p;
				println(language.getString("log.connected"), site.getName(), site.getHost(), site.getPort(), site.getUsername());
				if (settings.isWatch() && site.isWatch()) {
					transfer.watch(site);
				}
				favoriteMenu.setEnabled(true);
				EventQueue.invokeLater(() -> {
					setTitle(site.getName() + " - " + (site.isSync() ? language.getString("sync") + " - " : "") + language.getString("app.name"));
					initToolbar(site.getFavorites());

					if (local != null) {
						localTable.setAddr(local);
					} else if (site.getLocal() != null && site.getLocal().length() > 0) {
						localTable.setAddr(site.getLocal());
					}
					if (remote != null) {
						remoteTable.setAddr(remote);
					} else if (site.getRemote() != null && site.getRemote().length() > 0) {
						remoteTable.setAddr(site.getRemote());
					} else {
						remoteTable.setAddr(p.pwd());
					}
				});
			} else {
				println(language.getString("log.connecterr"), site.getName(), site.getHost(), site.getPort(), site.getUsername(), p.getError());
				p.logout();
			}

			hideLoading();
		});
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

		showLoading();

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
				relogin(protocol);
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

			hideLoading();
		});
	}

	@Override
	public void rightClicked(boolean isLocal, int index, Row row, MouseEvent e) {
		if (row == null) {
			for (int i = 0; i < localMenu.getComponentCount(); i++) {
				localMenu.getComponent(i).setEnabled(i == 6);
			}
			for (int i = 0; i < remoteMenu.getComponentCount(); i++) {
				remoteMenu.getComponent(i).setEnabled(protocol != null && i == 6);
			}
		} else {
			for (int i = 0; i < localMenu.getComponentCount(); i++) {
				localMenu.getComponent(i).setEnabled(true);
			}
			for (int i = 0; i < remoteMenu.getComponentCount(); i++) {
				remoteMenu.getComponent(i).setEnabled(true);
			}
		}
		if (isLocal) {
			if (row != null) {
				localUploadMenu.setEnabled(protocol != null);
				localUploadAsMenu.setEnabled(protocol != null);
				localQueueMenu.setEnabled(protocol != null);
				localQueueAsMenu.setEnabled(protocol != null);
			}
			localMenu.setRow(row);
			localMenu.show(e.getComponent(), e.getX(), e.getY());
		} else {
			remoteMenu.setRow(row);
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
					if (protocol != null && protocol.getSite().isSync()) {
						String rStr = remoteTable.getParentPath();
						String lStr = localTable.getParentPath();
						if (rStr.startsWith(protocol.getSite().getRemote()) && lStr.startsWith(protocol.getSite().getLocal())) {
							remoteTable.setAddr(rStr);
							localTable.setAddr(lStr);
						} else {
							String s = language.getString("log.sync");
							leftStatus.setText(s);
							println(s);
						}
					} else {
						localTable.setAddr(localTable.getParentPath());
					}
				} else {
					localTable.setAddr(localTable.getPath(r.getName()));
					if (protocol != null && protocol.getSite().isSync()) {
						remoteTable.setAddr(remoteTable.getPath(r.getName()));
					}
				}
			} else {
				if (r.isUp()) {
					if (protocol.getSite().isSync()) {
						String rStr = remoteTable.getParentPath();
						String lStr = localTable.getParentPath();
						if (rStr.startsWith(protocol.getSite().getRemote()) && lStr.startsWith(protocol.getSite().getLocal())) {
							remoteTable.setAddr(rStr);
							localTable.setAddr(lStr);
						} else {
							String s = language.getString("log.sync");
							leftStatus.setText(s);
							println(s);
						}
					} else {
						remoteTable.setAddr(remoteTable.getParentPath());
					}
				} else {
					remoteTable.setAddr(remoteTable.getPath(r.getName()));
					if (protocol.getSite().isSync()) {
						localTable.setAddr(localTable.getPath(r.getName()));
					}
				}
			}
		} else if (local) {
			try {
				Desktop.getDesktop().open(new File(localTable.getPath(r.getName())));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if ("REG".equals(r.getType())) {
			new ResumeDialog(MainFrame.this, true).show(new ResumeDialog.Listener() {
				@Override
				public void resume(boolean resume) {
					showLoading();
					pool.execute(() -> {
						String remoteFile = remoteTable.getPath(r.getName());
						File localFile = new File(Settings.ROOT_PATH + File.separator + "files" + File.separator + r.getName());
						if (!localFile.exists()) {
							File f = localFile.getParentFile();
							if (!f.isDirectory()) {
								f.mkdir();
							}
						}

						relogin(protocol);

						protocol.setResume(resume);
						protocol.setProgressListener(new IProtocol.ProgressListener() {
							long precent = 0;

							@Override
							public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
								long p = 100 * totalBytesTransferred / r.getSize();
								if (p != precent) {
									precent = p;
									leftStatus.setText(localFile.getAbsolutePath() + " - " + FileUtils.formatSize(totalBytesTransferred) + " - " + precent + '%');
								}
							}
						});

						println(language.getString("log.downloading"), localFile.getAbsolutePath(), remoteFile, protocol.getSite().getName());
						if (protocol.retrieveFile(remoteFile, localFile.getAbsolutePath())) {
							println(language.getString("log.downloaded"), localFile.getAbsolutePath(), remoteFile, protocol.getSite().getName());
							EventQueue.invokeLater(() -> {
								ConfirmDialog dialog = new ConfirmDialog(MainFrame.this, true);
								dialog.setSize(600, dialog.getHeight());
								dialog.show(language.getString("upload.title"), localFile.getAbsolutePath() + " => " + remoteFile, language.getString("upload.confirm"), new Runnable() {
									long lastModified = localFile.lastModified();

									public void run() {
										long lastModified2 = localFile.lastModified();

										if (lastModified == lastModified2) {
											return;
										}
										lastModified = lastModified2;

										relogin(protocol);

										println(language.getString("log.uploading"), localFile.getAbsolutePath(), remoteFile, protocol.getSite().getName());
										if (protocol.storeFile(remoteFile, localFile.getAbsolutePath())) {
											println(language.getString("log.uploaded"), localFile.getAbsolutePath(), remoteFile, protocol.getSite().getName());
										} else {
											println(language.getString("log.uploaderr"), localFile.getAbsolutePath(), remoteFile, protocol.getSite().getName(), protocol.getError());
										}
									}
								});
							});
							try {
								Desktop.getDesktop().open(localFile);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							println(language.getString("log.downloaderr"), localFile.getAbsolutePath(), remoteFile, protocol.getSite().getName(), protocol.getError());
						}

						hideLoading();
					});
				}
			});
		} else
			leftStatus.setText(language.getString(local ? "local" : "remote") + " " + language.getString("type." + r.getType()) + " \"" + r.getName() + "\" " + FileUtils.formatSize(r.getSize()));
	}

	@Override
	public void rightClicked(boolean isProgress, int i, ProgressTable.Row r, MouseEvent e) {
		if (isProgress) {
			if (transfer.isRunning()) {
				progressTransferMenu.setEnabled(false);
				progressSuspendMenu.setEnabled(true);
				progressCleanMenu.setEnabled(false);
				progressDeleteMenu.setEnabled(false);
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
				progressDeleteMenu.setEnabled(r != null);
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

			String local, remote;
			if ("DIR".equals(r.getType())) {
				local = r.getLocal();
				remote = r.getRemote();
			} else {
				local = r.getLocal().substring(0, r.getLocal().lastIndexOf(File.separatorChar));
				if (local.isEmpty())
					local = "/";
				remote = r.getRemote().substring(0, r.getRemote().lastIndexOf('/'));
				if (remote.isEmpty())
					remote = "/";
			}

			if (protocol == null || !protocol.getSite().getName().equals(r.getSite())) {
				if (settings.getSites().containsKey(r.getSite())) {
					enterSite(r.getSite(), local, remote);
				}
			} else {
				localTable.setAddr(local);
				remoteTable.setAddr(remote);
			}
		} else {
			leftStatus.setText("");
		}
	}

	public class Transfer {
		private AtomicBoolean running = new AtomicBoolean(false);
		private AtomicBoolean diring = new AtomicBoolean(false);
		private LinkedList<ProgressTable.Row> fileQueue = new LinkedList<ProgressTable.Row>();
		private LinkedList<ProgressTable.Row> dirQueue = new LinkedList<ProgressTable.Row>();
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
		private Set<TransferThread> threads = new HashSet<TransferThread>();
		private long beginTime = System.currentTimeMillis();
		private AtomicBoolean resume = new AtomicBoolean(false);

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

			fileQueue.clear();
			dirQueue.clear();

			lock = progressTable.getList();
			timer = new Timer("transfer", true);

			mLang.setEnabled(false);
			mSkin.setEnabled(false);

			new ResumeDialog(MainFrame.this, true).show(new ResumeDialog.Listener() {
				@Override
				public void resume(boolean b) {
					beginTime = System.currentTimeMillis();
					resume.set(b);
					synchronized (lock) {
						addAll(progressTable.getList());
					}
					timer.schedule(new RefreshTask(), 50, 250);
					timer.schedule(new BytesTask(), 50, 1000);
				}
			});
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
			synchronized (threads) {
				for (Thread t : threads)
					t.interrupt();
			}
		}

		public void add(ProgressTable.Row row) {
			if (isRunning()) {
				pool.execute(new ProgressQueue(row));
				// new ProgressQueue(row).run();
			}
		}

		public void addAll(List<ProgressTable.Row> rows) {
			if (isRunning()) {
				pool.execute(new ProgressQueue(rows));
				// new ProgressQueue(rows).run();
			}
		}

		private final Set<String> watchSet = new HashSet<String>();
		private final Set<Thread> watchThreads = new HashSet<Thread>();

		public void watch(Site s) {
			synchronized (watchSet) {
				if (!watchSet.contains(s.getName())) {
					watchSet.add(s.getName());
					new WatchThread(s).start();
				}
			}
		}

		public void unwatch() {
			synchronized (watchSet) {
				watchSet.clear();
			}
			synchronized (watchThreads) {
				for (Thread t : watchThreads)
					t.interrupt();
			}
		}

		private class ProgressQueue implements Runnable {
			LinkedList<ProgressTable.Row> lr = new LinkedList<ProgressTable.Row>();

			public ProgressQueue(List<ProgressTable.Row> list) {
				list.sort(new Comparator<ProgressTable.Row>() {
					@Override
					public int compare(com.talent518.ftp.gui.table.ProgressTable.Row o1, com.talent518.ftp.gui.table.ProgressTable.Row o2) {
						return Integer.compare(o1.getId(), o1.getId());
					}
				});
				lr.addAll(list);
			}

			public ProgressQueue(ProgressTable.Row row) {
				lr.add(row);
			}

			@Override
			public void run() {
				synchronized (dirQueue) {
					ProgressTable.Row r;
					while (!lr.isEmpty()) {
						r = lr.removeFirst();
						r.setId(0);
						dirQueue.add(r);
					}
				}

				if (isRunning() && diring.compareAndSet(false, true)) {
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
			private long time = System.currentTimeMillis();

			public long getTime() {
				return time;
			}

			protected void makeTime() {
				time = System.currentTimeMillis();
			}

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
					protocol.setResume(resume.get());

					println(language.getString("log.connecting"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					if (protocol.login())
						println(language.getString("log.connected"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
					else
						println(language.getString("log.connecterr"), r.getSite(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername(), protocol.getError());

					sites.put(r.getSite(), protocol);
				}

				relogin(protocol);

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
				synchronized (threads) {
					threads.add(this);
				}
				work();

				for (IProtocol p2 : sites.values())
					p2.dispose();

				synchronized (threads) {
					threads.remove(this);
				}
			}

			protected abstract void work();
		}

		private class FileThread extends TransferThread {
			@Override
			public void work() {
				nThread.incrementAndGet();

				while (running.get()) {
					makeTime();

					synchronized (fileQueue) {
						if (fileQueue.isEmpty())
							break;
						else
							r = fileQueue.removeFirst();
					}

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
							if (r.tries()) {
								if (r.getWritten() == 0 && !isSkip()) {
									println(language.getString("remote.delete.file.being"), r.getRemote());
									if (protocol.unlink(r.getRemote())) {
										println(language.getString("remote.delete.file.success"), r.getRemote());
									} else {
										println(language.getString("remote.delete.file.failure"), r.getRemote(), protocol.getError());
									}
								}
								synchronized (lock) {
									r.setStatus(ProgressTable.Row.STATUS_READY);
								}
								synchronized (fileQueue) {
									fileQueue.add(r);
								}
							} else {
								synchronized (lock) {
									r.setStatus(ProgressTable.Row.STATUS_ERROR);
								}
								nError.incrementAndGet();
							}
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
							if (r.tries()) {
								synchronized (lock) {
									r.setStatus(ProgressTable.Row.STATUS_READY);
								}
								synchronized (fileQueue) {
									fileQueue.add(r);
								}
							} else {
								synchronized (lock) {
									r.setStatus(ProgressTable.Row.STATUS_ERROR);
								}
								nError.incrementAndGet();
							}
						}
					}
					nRunning.decrementAndGet();
				}

				nThread.decrementAndGet();
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
					synchronized (lock) {
						row.setWritten(totalBytesTransferred);
					}
					makeTime();
				}
			}
		}

		private class DirectoryThread extends TransferThread {
			@Override
			public void work() {
				diring.set(true);

				while (running.get()) {
					makeTime();

					synchronized (dirQueue) {
						if (dirQueue.isEmpty())
							break;
						else
							r = dirQueue.removeFirst();
					}
					if (r.getSite() == null || (r.getStatus() != ProgressTable.Row.STATUS_READY && r.getStatus() != ProgressTable.Row.STATUS_RUNNING))
						continue;

					if (r.getId() == 0)
						r.setId(nCount.incrementAndGet());

					if ("REG".equals(r.getType())) {
						synchronized (fileQueue) {
							fileQueue.add(r);
						}
					} else if ("DIR".equals(r.getType())) {
						synchronized (lock) {
							r.setStatus(ProgressTable.Row.STATUS_RUNNING);
						}
						nRunning.incrementAndGet();
						if (r.isDirection()) {
							if (isSkip()) {
								nRunning.decrementAndGet();
								nError.incrementAndGet();
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
											p.setId(nCount.incrementAndGet());
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
											if ("REG".equals(p.getType())) {
												synchronized (fileQueue) {
													fileQueue.add(p);
												}
											} else {
												synchronized (dirQueue) {
													dirQueue.add(p);
												}
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
								if (r.tries()) {
									synchronized (lock) {
										r.setStatus(ProgressTable.Row.STATUS_READY);
									}
									synchronized (dirQueue) {
										dirQueue.add(r);
									}
								} else {
									synchronized (lock) {
										r.setStatus(ProgressTable.Row.STATUS_ERROR);
									}
									nError.incrementAndGet();
								}
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
								nError.incrementAndGet();
								continue;
							}

							println(language.getString("log.dirlisting"), r.getRemote(), r.getSite());
							if (protocol.ls(r.getRemote(), files)) {
								for (FileTable.Row r2 : files) {
									p = new ProgressTable.Row();
									p.setId(nCount.incrementAndGet());
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
									if ("REG".equals(p.getType())) {
										synchronized (fileQueue) {
											fileQueue.add(p);
										}
									} else {
										synchronized (dirQueue) {
											dirQueue.add(p);
										}
									}
								}
								println(language.getString("log.dirlisted"), r.getRemote(), r.getSite());
								synchronized (lock) {
									r.setStatus(ProgressTable.Row.STATUS_COMPLETED);
								}
								nCompleted.incrementAndGet();
							} else {
								println(language.getString("log.dirlisterr"), r.getRemote(), r.getSite(), protocol.getError());
								if (r.tries()) {
									synchronized (lock) {
										r.setStatus(ProgressTable.Row.STATUS_READY);
									}
									synchronized (dirQueue) {
										dirQueue.add(r);
									}
								} else {
									synchronized (lock) {
										r.setStatus(ProgressTable.Row.STATUS_ERROR);
									}
									nError.incrementAndGet();
								}
							}
							files.clear();
						}
						nRunning.decrementAndGet();
					} else {
						println(language.getString("log.notSupportType"), r.getType(), r.getLocal());
						synchronized (lock) {
							r.setStatus(ProgressTable.Row.STATUS_SKIP);
						}
						nSkip.incrementAndGet();
					}
				}

				diring.set(false);
			}
		}

		private class WatchThread extends Thread {
			private final Site site;
			private final LinkedList<File> fList = new LinkedList<File>();
			private final Map<WatchKey, String> watchKeys = new HashMap<WatchKey, String>();
			private final Map<String, WatchKey> watchVals = new HashMap<String, WatchKey>();
			private LinkedBlockingQueue<DeleteNode> watchQueue = new LinkedBlockingQueue<DeleteNode>(1000000);
			private Runnable runQueue = new Runnable() {
				DeleteNode node;
				IProtocol protocol = null;

				@Override
				public void run() {
					while (true) {
						try {
							node = watchQueue.take();
						} catch (InterruptedException e) {
							e.printStackTrace();
							continue;
						}

						if (protocol == null) {
							protocol = site.create();
							protocol.setDeleteListener(remoteDeleteListener);

							println(language.getString("log.connecting"), site.getName(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
							if (protocol.login())
								println(language.getString("log.connected"), site.getName(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername());
							else
								println(language.getString("log.connecterr"), site.getName(), protocol.getSite().getHost(), protocol.getSite().getPort(), protocol.getSite().getUsername(), protocol.getError());
						} else
							relogin(protocol);

						if (!protocol.isConnected() || !protocol.isLogined()) {
							println(language.getString("log.connectOrLoginFailure"), site.getName(), protocol.getError());
							if (node.isDir()) {
								println(language.getString("remote.delete.dir.failure"), node.getRemote(), protocol.getError());
							} else {
								println(language.getString("remote.delete.file.failure"), node.getRemote(), protocol.getError());
							}
							continue;
						}

						if (node.isDelete()) {
							if (node.isDir()) {
								println(language.getString("remote.delete.dir.being"), node.getRemote());
								if (protocol.rmdir(node.getRemote())) {
									println(language.getString("remote.delete.dir.success"), node.getRemote());
								} else {
									println(language.getString("remote.delete.dir.failure"), node.getRemote(), protocol.getError());
								}
							} else {
								println(language.getString("remote.delete.file.being"), node.getRemote());
								if (protocol.unlink(node.getRemote())) {
									println(language.getString("remote.delete.file.success"), node.getRemote());
								} else {
									println(language.getString("remote.delete.file.failure"), node.getRemote(), protocol.getError());
								}
							}
						} else {
							if (node.isDir()) {
								println(language.getString("log.mkdiring"), node.getRemote(), site.getName());
								if (protocol.mkdir(node.getRemote())) {
									println(language.getString("log.mkdired"), node.getRemote(), site.getName());
								} else {
									println(language.getString("log.mkdirerr"), node.getRemote(), site.getName(), protocol.getError());
								}
							} else {
								println(language.getString("log.uploading"), node.getLocal(), node.getRemote(), site.getName());
								if (protocol.storeFile(node.getRemote(), node.getLocal())) {
									println(language.getString("log.uploaded"), node.getLocal(), node.getRemote(), site.getName());
								} else {
									println(language.getString("log.uploaderr"), node.getLocal(), node.getRemote(), site.getName(), protocol.getError());
								}
							}
						}
					}
				}
			};

			public WatchThread(Site s) {
				super();

				site = s;
			}

			@Override
			public void run() {
				WatchService watchService;
				File[] fs;
				File f;
				WatchKey key, key2;

				synchronized (watchThreads) {
					watchThreads.add(this);
					Thread t = new Thread(runQueue);
					watchThreads.add(t);
					t.start();
				}

				try {
					watchService = FileSystems.getDefault().newWatchService();

					fList.add(new File(site.getLocal()));

					while (fList.size() > 0) {
						f = fList.removeFirst();
						println("WATCH: " + f.getAbsolutePath());
						if (f.isDirectory() && f.canRead()) {
							key = Paths.get(f.getAbsolutePath()).register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
							watchKeys.put(key, f.getAbsolutePath());
							watchVals.put(f.getAbsolutePath(), key);
						} else
							continue;
						fs = f.listFiles();
						if (fs == null)
							continue;
						for (File f2 : fs)
							if (f2.isDirectory())
								fList.add(f2);
					}

					while (watchKeys.size() > 0) {
						key = watchService.take();
						String path = watchKeys.get(key);
						List<WatchEvent<?>> watchEvents = key.pollEvents();
						for (WatchEvent<?> event : watchEvents) {
							f = new File(path + File.separator + event.context());
							println(event.kind() + ": " + f.getAbsolutePath());

							if (StandardWatchEventKinds.ENTRY_CREATE == event.kind()) {
								if (f.isDirectory()) {
									println("WATCH: " + f.getAbsolutePath());
									key2 = Paths.get(f.getAbsolutePath()).register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
									watchKeys.put(key2, f.getAbsolutePath());
									watchVals.put(f.getAbsolutePath(), key2);

									watchQueue.put(new DeleteNode(getRemote(f.getAbsolutePath()), true, null));
//								} else {
//									watchQueue.put(new DeleteNode(getRemote(f.getAbsolutePath()), false, f.getAbsolutePath()));
								}
							}
							if (StandardWatchEventKinds.ENTRY_MODIFY == event.kind()) {
								if (!f.isDirectory()) {
									watchQueue.put(new DeleteNode(getRemote(f.getAbsolutePath()), false, f.getAbsolutePath()));
								}
							}
							if (StandardWatchEventKinds.ENTRY_DELETE == event.kind()) {
								if (watchVals.containsKey(f.getAbsolutePath())) {
									println("UNWATCH: " + f.getAbsolutePath());
									key2 = watchVals.get(f.getAbsolutePath());
									key2.cancel();
									watchKeys.remove(key2);
									watchVals.remove(f.getAbsolutePath());

									watchQueue.put(new DeleteNode(getRemote(f.getAbsolutePath()), true));
								} else {
									watchQueue.put(new DeleteNode(getRemote(f.getAbsolutePath()), false));
								}
							}
						}
						key.reset();
					}
				} catch (Exception e) {
					log.error("newWatchService error", e);
					println(e.getMessage());
				}
			}

			private String getRemote(String path) {
				return site.getRemote() + path.substring(site.getRemote().length());
			}

			private class DeleteNode {
				private String remote;
				private boolean isDir;
				private boolean isDelete;
				private String local;

				public DeleteNode(String remote, boolean isDir) {
					super();
					this.remote = remote;
					this.isDir = isDir;
					this.isDelete = true;
				}

				public DeleteNode(String remote, boolean isDir, String local) {
					super();
					this.remote = remote;
					this.isDir = isDir;
					this.local = local;
				}

				public String getRemote() {
					return remote;
				}

				public boolean isDir() {
					return isDir;
				}

				public boolean isDelete() {
					return isDelete;
				}

				public String getLocal() {
					return local;
				}

				@Override
				public String toString() {
					return Settings.gson().toJson(this);
				}
			}
		}

		private class BytesTask extends TimerTask {
			final String format = language.getString("transfer.speed");
			final String resumeStr = language.getString("name.resume");

			@Override
			public void run() {
				long down = nDownBytes.getAndSet(0);
				long up = nUpBytes.getAndSet(0);
				long total = nTotalBytes.getAndSet(0);

				String totalBytes = FileUtils.formatSize(total);
				String upBytes = FileUtils.formatSize(up);
				String downBytes = FileUtils.formatSize(down);

				final String left = (resume.get() ? resumeStr + " - " : "") + String.format(format, totalBytes, upBytes, downBytes, (double) (System.currentTimeMillis() - beginTime) / 1000.0f);

				EventQueue.invokeLater(() -> {
					leftStatus.setText(left);
				});
			}
		}

		private class RefreshTask extends TimerTask {
			private final Predicate<ProgressTable.Row> filter = new Predicate<ProgressTable.Row>() {
				@Override
				public boolean test(ProgressTable.Row t) {
					if (t.getStatus() != ProgressTable.Row.STATUS_READY && t.getStatus() != ProgressTable.Row.STATUS_RUNNING) {
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
					if (running.get()) {
						synchronized (threads) {
							long time = System.currentTimeMillis();
							for (TransferThread t : threads)
								if (time - t.getTime() > 15000)
									t.interrupt();
						}

						synchronized (fileQueue) {
							for (int i = nThread.get(); i < settings.getNthreads() && i < fileQueue.size(); i++)
								new FileThread().start();
						}

						synchronized (dirQueue) {
							if (!dirQueue.isEmpty() && diring.compareAndSet(false, true))
								new DirectoryThread().start();
						}

						if (!diring.get() && nThread.get() == 0) {
							synchronized (fileQueue) {
								synchronized (dirQueue) {
									synchronized (lock) {
										synchronized (progresses) {
											if (fileQueue.isEmpty() && dirQueue.isEmpty() && progressTable.getList().isEmpty() && progresses.isEmpty())
												running.set(false);
										}
									}
								}
							}
						}
					} else {
						synchronized (threads) {
							for (Thread t : threads)
								t.interrupt();
						}
					}
				} else {
					println(language.getString("log.transferEnd"));

					timer.cancel();
					timer = null;

					mLang.setEnabled(true);
					mSkin.setEnabled(true);
				}

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

				EventQueue.invokeLater(() -> {
					rightStatus.setText(String.format(language.getString("status.progress"), nThread.get(), nCount.get(), nSkip.get(), nReady.get(), nRunning.get(), nCompleted.get(), nError.get()));
				});

				if (timer == null && closed.get()) {
					progressTable.save();
					processedTable.save();
					System.exit(0);
				}
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
					restart();
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
		public static final int KEY_UPLOAD_AS = 41;
		public static final int KEY_LQUEUE = 42;
		public static final int KEY_LQUEUE_AS = 43;
		public static final int KEY_LDELETE = 44;
		public static final int KEY_LMKDIR = 45;
		public static final int KEY_LRENAME = 46;

		// popup remote menu
		public static final int KEY_DOWNLOAD = 50;
		public static final int KEY_DOWNLOAD_AS = 51;
		public static final int KEY_RQUEUE = 52;
		public static final int KEY_RQUEUE_AS = 53;
		public static final int KEY_RDELETE = 54;
		public static final int KEY_RMKDIR = 55;
		public static final int KEY_RRENAME = 56;

		// popup progress menu
		public static final int KEY_TRANSFER = 60;
		public static final int KEY_SUSPEND = 61;
		public static final int KEY_CLEAN = 62;
		public static final int KEY_DELETE = 63;

		// popup processed menu
		public static final int KEY_CLEAN_ALL = 70;
		public static final int KEY_CLEAN_COMPLETED = 71;
		public static final int KEY_CLEAN_SKIP = 72;
		public static final int KEY_CLEAN_ERROR = 73;
		public static final int KEY_ERROR_RETRANSFER = 74;

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
				case KEY_OPEN: {
					File f = new File(System.getProperty("user.home"));
					JFileChooser chooser = new JFileChooser(f);
					chooser.setDialogTitle(language.getString("chooser.open.title"));
					chooser.setDialogType(JFileChooser.OPEN_DIALOG);
					chooser.setApproveButtonText(language.getString("chooser.save"));
					chooser.addChoosableFileFilter(new FileTypeFilter(".json", language.getString("chooser.json.type")));
					chooser.setAcceptAllFileFilterUsed(false);
					if (chooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						progressTable.load(chooser.getSelectedFile());
					}
					break;
				}
				case KEY_SAVE: {
					File f = new File(System.getProperty("user.home"));
					JFileChooser chooser = new JFileChooser(f);
					chooser.setDialogTitle(language.getString("chooser.save.title"));
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.setApproveButtonText(language.getString("chooser.save"));
					chooser.addChoosableFileFilter(new FileTypeFilter(".json", language.getString("chooser.json.type")));
					chooser.setAcceptAllFileFilterUsed(false);
					chooser.setSelectedFile(new File(f, "ftp-gui-" + logFormat.format(new Date()) + ".json"));
					if (chooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						progressTable.save(chooser.getSelectedFile());
					}
					break;
				}
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
					enterSite(resKey);
					break;
				case KEY_ENGLISH:
					settings.setLocale(new Locale("en", "US"));
					settings.save();
					restart();
					break;
				case KEY_CHINESE:
					settings.setLocale(new Locale("zh", "CN"));
					settings.save();
					restart();
					break;
				case KEY_PROTOCOL:
					leftStatus.setText(language.getString("about.protocol.help"));
					break;
				case KEY_APP:
					leftStatus.setText(language.getString("about.application.help"));
					break;

				case KEY_UPLOAD:
					addProgress(true, true, localMenu.getRow());
					break;
				case KEY_UPLOAD_AS:
					new NameDialog(MainFrame.this, true).show(resVal, localMenu.getRow().getName(), new NameDialog.Listener() {
						@Override
						public void name(String name) {
							addProgress(true, true, localMenu.getRow(), name);
						}
					});
					break;
				case KEY_LQUEUE:
					addProgress(false, true, localMenu.getRow());
					break;
				case KEY_LQUEUE_AS:
					new NameDialog(MainFrame.this, true).show(resVal, localMenu.getRow().getName(), new NameDialog.Listener() {
						@Override
						public void name(String name) {
							addProgress(false, true, localMenu.getRow(), name);
						}
					});
					break;
				case KEY_LDELETE:
					showLoading();
					pool.execute(() -> {
						final File f = new File(localTable.getAddr() + File.separator + localMenu.getRow().getName());
						if (localMenu.getRow().isDir()) {
							try {
								println(language.getString("remote.delete.dir.being"), f.getAbsolutePath());
								FileUtils.deleteDirectory(f, localDeleteListener);
								EventQueue.invokeLater(() -> {
									localTable.setAddr(localTable.getAddr());
								});
								println(language.getString("remote.delete.dir.success"), f.getAbsolutePath());
							} catch (Exception e2) {
								println(language.getString("remote.delete.dir.failure"), f.getAbsolutePath(), e2.getMessage());
							}
						} else {
							try {
								println(language.getString("remote.delete.file.being"), f.getAbsolutePath());
								f.delete();
								EventQueue.invokeLater(() -> {
									localTable.setAddr(localTable.getAddr());
								});
								println(language.getString("remote.delete.file.success"), f.getAbsolutePath());
							} catch (Exception e2) {
								println(language.getString("remote.delete.file.failure"), f.getAbsolutePath(), e2.getMessage());
							}
						}

						hideLoading();
					});
					break;
				case KEY_LMKDIR:
					new NameDialog(MainFrame.this, true).show(resVal, new NameDialog.Listener() {
						@Override
						public void name(String name) {
							showLoading();
							pool.execute(() -> {
								File f = new File(localTable.getAddr() + File.separator + name);
								try {
									println(language.getString("local.mkdir.being"), f.getAbsolutePath());
									f.mkdir();
									EventQueue.invokeLater(() -> {
										localTable.setAddr(localTable.getAddr());
									});
									println(language.getString("local.mkdir.success"), f.getAbsolutePath());
								} catch (Exception e2) {
									println(language.getString("local.mkdir.failure"), f.getAbsolutePath(), e2.getMessage());
								}

								hideLoading();
							});
						}
					});
					break;
				case KEY_LRENAME:
					new NameDialog(MainFrame.this, true).show(resVal, localMenu.getRow().getName(), new NameDialog.Listener() {
						@Override
						public void name(String name) {
							if (name.equals(localMenu.getRow().getName()))
								return;
							showLoading();
							pool.execute(() -> {
								File from = new File(localTable.getAddr() + File.separator + localMenu.getRow().getName());
								File to = new File(localTable.getAddr() + File.separator + name);
								try {
									println(language.getString("local.rename.being"), from.getAbsolutePath(), to.getAbsolutePath());
									from.renameTo(to);
									EventQueue.invokeLater(() -> {
										localTable.setAddr(localTable.getAddr());
									});
									println(language.getString("local.rename.success"), from.getAbsolutePath(), to.getAbsolutePath());
								} catch (Exception e2) {
									println(language.getString("local.rename.failure"), from.getAbsolutePath(), to.getAbsolutePath(), e2.getMessage());
								}

								hideLoading();
							});
						}
					});
					break;
				case KEY_DOWNLOAD:
					addProgress(true, false, remoteMenu.getRow());
					break;
				case KEY_DOWNLOAD_AS:
					new NameDialog(MainFrame.this, true).show(resVal, remoteMenu.getRow().getName(), new NameDialog.Listener() {
						@Override
						public void name(String name) {
							addProgress(true, false, remoteMenu.getRow(), name);
						}
					});
					break;
				case KEY_RQUEUE:
					addProgress(false, false, remoteMenu.getRow());
					break;
				case KEY_RQUEUE_AS:
					new NameDialog(MainFrame.this, true).show(resVal, remoteMenu.getRow().getName(), new NameDialog.Listener() {
						@Override
						public void name(String name) {
							addProgress(false, false, remoteMenu.getRow(), name);
						}
					});
					break;
				case KEY_RDELETE:
					showLoading();
					pool.execute(() -> {
						final String f = remoteTable.getPath(remoteMenu.getRow().getName());
						relogin(protocol);
						if (remoteMenu.getRow().isDir()) {
							println(language.getString("remote.delete.dir.being"), f);
							if (protocol.rmdir(f)) {
								EventQueue.invokeLater(() -> {
									remoteTable.setAddr(remoteTable.getAddr());
								});
								println(language.getString("remote.delete.dir.success"), f);
							} else {
								println(language.getString("remote.delete.dir.failure"), f, protocol.getError());
							}
						} else {
							println(language.getString("remote.delete.file.being"), f);
							if (protocol.unlink(f)) {
								EventQueue.invokeLater(() -> {
									remoteTable.setAddr(remoteTable.getAddr());
								});
								println(language.getString("remote.delete.file.success"), f);
							} else {
								println(language.getString("remote.delete.file.failure"), f, protocol.getError());
							}
						}

						hideLoading();
					});
					break;
				case KEY_RMKDIR:
					new NameDialog(MainFrame.this, true).show(resVal, new NameDialog.Listener() {
						@Override
						public void name(String name) {
							final String f = remoteTable.getPath(name);
							showLoading();
							pool.execute(() -> {
								relogin(protocol);
								println(language.getString("remote.mkdir.being"), f);
								if (protocol.mkdir(f)) {
									EventQueue.invokeLater(() -> {
										remoteTable.setAddr(remoteTable.getAddr());
									});
									println(language.getString("remote.mkdir.success"), f);
								} else {
									println(language.getString("remote.mkdir.failure"), f, protocol.getError());
								}

								hideLoading();
							});
						}
					});
					break;
				case KEY_RRENAME:
					new NameDialog(MainFrame.this, true).show(resVal, remoteMenu.getRow().getName(), new NameDialog.Listener() {
						@Override
						public void name(String name) {
							final String from = remoteTable.getPath(remoteMenu.getRow().getName());
							final String to = remoteTable.getPath(name);
							if (from.equals(to))
								return;
							showLoading();
							pool.execute(() -> {
								relogin(protocol);
								println(language.getString("remote.rename.being"), from, to);
								if (protocol.rename(from, to)) {
									EventQueue.invokeLater(() -> {
										remoteTable.setAddr(remoteTable.getAddr());
									});
									println(language.getString("remote.rename.success"), from, to);
								} else {
									println(language.getString("remote.rename.failure"), from, to, protocol.getError());
								}

								hideLoading();
							});
						}
					});
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
				case KEY_DELETE:
					synchronized (progressTable.getList()) {
						progressTable.getList().remove(progressMenu.getRow());
					}
					progressTable.fireTableDataChanged();
					break;

				case KEY_CLEAN_ALL:
					processedTable.clear(true);
					break;
				case KEY_CLEAN_COMPLETED:
					cleanProgress(ProgressTable.Row.STATUS_COMPLETED);
					break;
				case KEY_CLEAN_SKIP:
					cleanProgress(ProgressTable.Row.STATUS_SKIP);
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
				case KEY_LOG_SAVE_AS: {
					File f = new File(System.getProperty("user.home"));
					JFileChooser chooser = new JFileChooser(f);
					chooser.setDialogTitle(language.getString("chooser.log.title"));
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.setApproveButtonText(language.getString("chooser.save"));
					chooser.addChoosableFileFilter(new FileTypeFilter(".log", language.getString("chooser.log.type")));
					chooser.setAcceptAllFileFilterUsed(false);
					chooser.setSelectedFile(new File(f, "ftp-gui-" + logFormat.format(new Date()) + ".log"));
					if (chooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						logSave(chooser.getSelectedFile());
					}
					break;
				}
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
			addProgress(isStart, isLocal, r, r.getName());
		}

		private void addProgress(boolean isStart, boolean isLocal, Row r, String as) {
			ProgressTable.Row progress = new ProgressTable.Row();
			progress.setSite(protocol.getSite().getName());
			progress.setLocal(localTable.getPath(isLocal ? r.getName() : as));
			progress.setDirection(isLocal);
			progress.setRemote(remoteTable.getPath(isLocal ? as : r.getName()));
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
			showLoading();
			pool.execute(() -> {
				synchronized (processedTable.getList()) {
					processedTable.getList().removeIf(new Predicate<ProgressTable.Row>() {
						@Override
						public boolean test(ProgressTable.Row t) {
							return t.getStatus() == status;
						}
					});
				}
				processedTable.fireTableDataChanged();
				hideLoading();
			});
		}

		private void errorRetransfer() {
			showLoading();
			pool.execute(() -> {
				final List<ProgressTable.Row> list = new ArrayList<ProgressTable.Row>();
				synchronized (processedTable.getList()) {
					processedTable.getList().removeIf(new Predicate<ProgressTable.Row>() {
						@Override
						public boolean test(ProgressTable.Row t) {
							if (t.getStatus() == ProgressTable.Row.STATUS_ERROR) {
								t.setWritten(0);
								t.setProgress(0);
								t.setStatus(ProgressTable.Row.STATUS_READY);
								t.setTries();
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
					EventQueue.invokeLater(() -> {
						transfer.start();
					});
				} else {
					transfer.addAll(list);
				}
				hideLoading();
			});
		}

		private void logSave(File f) {
			showLoading();
			pool.execute(() -> {
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
				hideLoading();
			});
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
