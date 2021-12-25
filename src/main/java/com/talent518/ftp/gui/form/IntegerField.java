package com.talent518.ftp.gui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToolTip;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.ui.MultiLineToolTip;

public class IntegerField extends JPanel {
	private static final long serialVersionUID = 7338631397387052286L;
	public static final Icon errorIcon = new ImageIcon(IntegerField.class.getResource("/icons/error.png"));
	public static final Icon helpIcon = new ImageIcon(IntegerField.class.getResource("/icons/help.png"));

	final ResourceBundle language = Settings.language();
	private JLabel label;
	private SpinnerNumberModel model;
	private JSpinner field;
	private JLabel icon = new JLabel(helpIcon) {
		private static final long serialVersionUID = 3083135914717990808L;

		@Override
		public JToolTip createToolTip() {
			JToolTip tip = new MultiLineToolTip();
			tip.setComponent(this);
			return tip;
		}
	};
	private String help;

	public IntegerField(String key, int value, int minimum, int maximum, int stepSize) {
		this(key, value, (String) null, minimum, maximum, stepSize);
	}

	public IntegerField(String key, int value, String help, int minimum, int maximum, int stepSize) {
		super();

		try {
			this.help = (help == null ? null : language.getString(help));
		} catch (Exception e) {
			this.help = help;
			e.printStackTrace();
		}

		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout(10, 10));

		label = new JLabel(language.getString(key));
		label.setPreferredSize(new Dimension(60, HEIGHT));
		label.setHorizontalAlignment(SwingConstants.RIGHT);

		model = new SpinnerNumberModel(value, minimum, maximum, stepSize);
		field = new JSpinner(model);

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(10, 10));
		center.add(field, BorderLayout.WEST);

		icon.setPreferredSize(new Dimension(40, HEIGHT));
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		icon.setVerticalAlignment(SwingConstants.CENTER);
		setHelp(null);

		add(label, BorderLayout.WEST);
		add(center, BorderLayout.CENTER);
		add(icon, BorderLayout.EAST);
	}

	public JLabel getLabel() {
		return label;
	}

	public JSpinner getField() {
		return field;
	}

	public int getValue() {
		return model.getNumber().intValue();
	}

	private void setHelp(String help) {
		boolean visible = true;
		if (help != null && help.length() > 0) {
			icon.setToolTipText(help);
			icon.setIcon(errorIcon);
		} else if (this.help != null && this.help.length() > 0) {
			icon.setToolTipText(this.help);
			icon.setIcon(helpIcon);
		} else {
			visible = false;
		}
		icon.setVisible(visible);
	}
}