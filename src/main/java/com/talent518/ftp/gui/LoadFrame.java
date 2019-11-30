package com.talent518.ftp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.dao.Skin;

public class LoadFrame extends JFrame implements WindowListener {
	private static final long serialVersionUID = 8353063665199041781L;
	private final Color transparent = new Color(0, 0, 0, 0);
	private final Font font = new Font("微软雅黑", Font.PLAIN, 13);

	public LoadFrame() {
		super();

		JButton btn = new JButton(Settings.language().getString("loading"), MainFrame.icon);

		btn.setBorder(BorderFactory.createEmptyBorder());
		btn.setForeground(Color.BLUE);
		btn.setBackground(transparent);
		btn.setContentAreaFilled(false);
		btn.setBounds(0, 0, MainFrame.icon.getIconWidth(), MainFrame.icon.getIconHeight());
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setVerticalTextPosition(SwingConstants.CENTER);
		btn.setOpaque(false);// 设置控件是否透明，true为不透明，false为透明
		btn.setContentAreaFilled(false);// 设置图片填满按钮所在的区域
		btn.setMargin(new Insets(0, 0, 0, 0));// 设置按钮边框和标签文字之间的距离
		btn.setFocusPainted(false);// 设置这个按钮是不是获得焦点
		btn.setBorderPainted(false);// 设置是否绘制边框
		btn.setFont(font);

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
