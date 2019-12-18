package com.talent518.ftp.gui.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.Timer;

public class GlassPane extends JComponent implements ActionListener, MouseListener {
	private static final long serialVersionUID = -2778776848711363176L;
	private static ImageIcon icon = new ImageIcon(GlassPane.class.getResource("/icons/loading.png"));

	private final int DELAY = 50;
	private final Timer timer = new Timer(DELAY, this);
	private final Color bgColor = new Color(0x66000000, true);
	private final RenderingHints hints;
	private int angle = 0;
	private final int STEP = 360 / (1000 / DELAY);

	public GlassPane() {
		super();

		hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

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
	public void actionPerformed(ActionEvent e) {
		if (angle < 360) {
			angle += STEP;
		} else {
			angle -= 360;
		}

		repaint();
	}

	public void paintComponent(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;
		int w = getWidth();
		int h = getHeight();
		int x = w / 2;
		int y = h / 2;

		g.setRenderingHints(hints);

		g.setColor(bgColor);
		g.fillRect(0, 0, w, h);

		g.setTransform(AffineTransform.getRotateInstance(Math.PI * angle / 180.0f, x, y));
		g.drawImage(icon.getImage(), x - icon.getIconWidth() / 2, y - icon.getIconHeight() / 2, null);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
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
}
