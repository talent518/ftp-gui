package com.talent518.ftp.gui.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.Timer;

import com.talent518.ftp.util.ImageUtils;

public abstract class GlassPane extends JComponent implements ActionListener, MouseListener {
	private static final long serialVersionUID = -2778776848711363176L;
	private static ImageIcon icon = new ImageIcon(GlassPane.class.getResource("/icons/loading.png"));
	private static BufferedImage[] frames = new BufferedImage[24];
	static {
		for (int i = 0; i < frames.length; i++)
			frames[i] = ImageUtils.rotate(icon, i * 360.0 / frames.length);
	}

	private final Timer timer = new Timer(3000 / frames.length, this);
	private final Color bgColor = new Color(0x66000000, true);
	private int iframe = 0;

	public GlassPane() {
		super();

		setVisible(false);
	}

	public void start() {
		timer.start();
		addMouseListener(this);
		setVisible(true);
	}

	public void restart() {
		timer.restart();
		removeMouseListener(this);
		addMouseListener(this);
		setVisible(true);
	}

	public void stop() {
		timer.stop();
		removeMouseListener(this);
		setVisible(false);
	}

	public boolean isRunning() {
		return timer.isRunning();
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (!isShowing())
			return;

		int w = getWidth(), h = getHeight();

		if ((w <= 0) || (h <= 0)) {
			return;
		}

		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);
		g.drawImage(frames[iframe], (w - icon.getIconWidth()) / 2, (h - icon.getIconHeight()) / 2, null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (iframe + 1 < frames.length) {
			iframe++;
		} else {
			iframe = 0;
		}

		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			stop();
			closeEvent();
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

	public abstract void closeEvent();
}