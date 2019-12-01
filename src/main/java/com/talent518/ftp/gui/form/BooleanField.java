package com.talent518.ftp.gui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.ui.MultiLineToolTip;

public class BooleanField extends JPanel {
	private static final long serialVersionUID = 7338631397387052286L;
	public static final Icon errorIcon = new ImageIcon(BooleanField.class.getResource("/icons/error.png"));
	public static final Icon helpIcon = new ImageIcon(BooleanField.class.getResource("/icons/help.png"));

	final ResourceBundle language = Settings.language();
	private JLabel label;
	private JCheckBox field;
	private JLabel icon = new JLabel(helpIcon) {
		private static final long serialVersionUID = 3083135914717990808L;

		@Override
		public JToolTip createToolTip() {
			JToolTip tip = new MultiLineToolTip();
			tip.setComponent(this);
			return tip;
		}
	};

	public BooleanField(String key, boolean val) {
		this(key, val, (String) null);
	}

	public BooleanField(String key, boolean val, String help) {
		super();

		if (help != null)
			try {
				help = language.getString(help);
			} catch (Exception e) {
			}

		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout(10, 10));

		label = new JLabel(language.getString(key));
		label.setPreferredSize(new Dimension(60, Integer.MAX_VALUE));
		label.setHorizontalAlignment(SwingConstants.RIGHT);

		field = new JCheckBox();
		field.setSelected(val);
		
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(10, 10));
		center.add(field, BorderLayout.WEST);

		icon.setPreferredSize(new Dimension(40, Integer.MAX_VALUE));
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		icon.setVerticalAlignment(SwingConstants.CENTER);

		if (help != null && help.length() > 0) {
			icon.setToolTipText(help);
			icon.setIcon(helpIcon);
			icon.setVisible(true);
		} else {
			icon.setVisible(false);
		}

		add(label, BorderLayout.WEST);
		add(center, BorderLayout.CENTER);
		add(icon, BorderLayout.EAST);
	}

	public JLabel getLabel() {
		return label;
	}

	public JCheckBox getField() {
		return field;
	}

	public boolean getValue() {
		return field.isSelected();
	}
}
