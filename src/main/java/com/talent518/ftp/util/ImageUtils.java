package com.talent518.ftp.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public class ImageUtils {
	private static final RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	static {
		hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	public static RenderingHints getHints() {
		return hints;
	}

	public static BufferedImage rotate(ImageIcon icon, double degrees) {
		BufferedImage rotate = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = rotate.createGraphics();
		g2d.setRenderingHints(hints);
		g2d.setTransform(AffineTransform.getRotateInstance(Math.PI * degrees / 180.0f, icon.getIconWidth() / 2, icon.getIconHeight() / 2));
		g2d.drawImage(icon.getImage(), 0, 0, null);
		g2d.dispose();
		return rotate;
	}
}
