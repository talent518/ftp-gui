package com.talent518.ftp.gui.form;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class ButtonForm extends JPanel implements ComponentListener {
	private static final long serialVersionUID = -4857565964713660029L;

	public ButtonForm() {
		super();

		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		addComponentListener(this);
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		for (int i = 0; i < getComponentCount(); i++) {
			Component btn = getComponent(i);
			Dimension dim = new Dimension(btn.getWidth(), getHeight());
			btn.setPreferredSize(dim);
			btn.setSize(dim);
		}
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
}
