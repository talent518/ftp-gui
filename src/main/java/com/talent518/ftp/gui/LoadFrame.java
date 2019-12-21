package com.talent518.ftp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.dao.Skin;

public class LoadFrame extends JFrame implements WindowListener {
	private static final long serialVersionUID = 8353063665199041781L;
	private final ResourceBundle language = Settings.language();
	private final Color transparent = new Color(0, 0, 0, 0);
	private final Font font = new Font(language.getString("app.font"), Font.PLAIN, 12);

	public LoadFrame() {
		super();

		JLabel btn = new JLabel(language.getString("loading"), SwingConstants.CENTER) {
			private static final long serialVersionUID = 3108348025330638284L;

			@Override
			protected void paintComponent(Graphics _g) {
				Graphics2D g = (Graphics2D) _g;
				g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g.drawImage(MainFrame.icon.getImage(), 0, 0, null);
				g.setFont(new Font(font.getName(), Font.BOLD, 16));

				@SuppressWarnings("deprecation")
				FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(g.getFont());
				int width = SwingUtilities.computeStringWidth(metrics, getText());

				g.setColor(new Color(0xFF6600));
				g.drawString(getText(), (MainFrame.icon.getIconWidth() - width) / 2, (MainFrame.icon.getIconHeight() + g.getFont().getSize()) / 2);
			}
		};

		btn.setBorder(BorderFactory.createEmptyBorder());
		btn.setBounds(0, 0, MainFrame.icon.getIconWidth(), MainFrame.icon.getIconHeight());
		btn.setOpaque(false);
		btn.setFont(font);

		setTitle(language.getString("app.name"));
		setIconImage(MainFrame.icon.getImage());
		setLayout(new BorderLayout(0, 0));
		add(btn, BorderLayout.CENTER);
		setSize(MainFrame.icon.getIconWidth(), MainFrame.icon.getIconHeight());
		setLocationRelativeTo(null);
		setResizable(false);
		setUndecorated(true);
		setBackground(transparent);

		addWindowListener(this);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				EventQueue.invokeLater(() -> {
					try {
						UIManager.setLookAndFeel(Skin.get(Settings.instance().getSkin()));
						initGlobalFontSetting(font);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					new MainFrame().setVisible(true);
				});
			}
		}, 1000);
	}

	private void initGlobalFontSetting(Font fnt) {
		FontUIResource fontRes = new FontUIResource(fnt);
		for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource)
				UIManager.put(key, fontRes);
		}
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	private static LoadFrame load = null;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			load = new LoadFrame();
			load.setVisible(true);
		});
	}

	public static void close() {
		if (load == null)
			return;

		load.dispose();
		load = null;
	}
}
