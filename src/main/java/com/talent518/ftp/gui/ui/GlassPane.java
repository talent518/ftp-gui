package com.talent518.ftp.gui.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.Timer;

import com.talent518.ftp.dao.Settings;
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
	private final String tip = Settings.language().getString("glassPane");
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
	protected void paintComponent(Graphics _g) {
		if (!isShowing())
			return;

		int w = getWidth(), h = getHeight();

		if ((w <= 0) || (h <= 0)) {
			return;
		}

		Graphics2D g = (Graphics2D) _g;
		Font font = new Font("微软雅黑", Font.PLAIN, 26);
		FontMetrics fm = g.getFontMetrics(font);

		g.setRenderingHints(ImageUtils.getHints());
		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);
		g.drawImage(frames[iframe], (w - icon.getIconWidth()) / 2, (h - icon.getIconHeight()) / 2, null);
		g.setFont(font);
		g.setColor(Color.WHITE);

		g.drawString(tip, (w - fm.stringWidth(tip)) / 2, (h + icon.getIconHeight()) / 2 + font.getSize() * 2);
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