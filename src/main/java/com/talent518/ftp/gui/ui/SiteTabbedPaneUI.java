package com.talent518.ftp.gui.ui;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class SiteTabbedPaneUI extends BasicTabbedPaneUI {
	@Override
	protected void installDefaults() {
		super.installDefaults();
		lightHighlight = darkShadow;
		tabInsets = new Insets(5, 10, 5, 10);
		tabAreaInsets = new Insets(0, 10, 0, 0);
		selectedTabPadInsets = new Insets(0, 0, 0, 0);
		contentBorderInsets = new Insets(6, 5, 5, 10);
	}

	@Override
	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		super.paintTabBorder(g, tabPlacement, tabIndex, x + 5, y, w - 5, h, isSelected);
	}

	@Override
	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		super.paintTabBackground(g, tabPlacement, tabIndex, x + 5, y, w - 5, h, isSelected);
	}

	@Override
	protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
		Rectangle tabRect = rects[tabIndex];
		if (tabPane.hasFocus() && isSelected) {
			int x = tabRect.x + 3, y = tabRect.y + 3, w = tabRect.width - 6, h = tabRect.height - 5;

			g.setColor(focus);
			BasicGraphicsUtils.drawDashedRect(g, x + 5, y, w - 5, h);
		}
	}

	@Override
	protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
	}

	@Override
	protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
	}

	@Override
	protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
	}

	@Override
	protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
		Rectangle r = rects[selectedIndex];

		g.setColor(lightHighlight);
		g.drawLine(x, y, x + r.x + 5, y);
		g.drawLine(x + r.x + r.width - 1, y, x + w + 1, y);
	}
}
