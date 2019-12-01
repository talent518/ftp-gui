package com.talent518.ftp.gui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.ui.MultiLineToolTip;

public class SelectField extends JPanel {
	private static final long serialVersionUID = 7338631397387052286L;
	public static final Icon errorIcon = new ImageIcon(SelectField.class.getResource("/icons/error.png"));
	public static final Icon helpIcon = new ImageIcon(SelectField.class.getResource("/icons/help.png"));

	final ResourceBundle language = Settings.language();
	private JLabel label;
	private JComboBox<String> field;
	private String help;
	private JLabel icon = new JLabel(helpIcon) {
		private static final long serialVersionUID = 3083135914717990808L;

		@Override
		public JToolTip createToolTip() {
			JToolTip tip = new MultiLineToolTip();
			tip.setComponent(this);
			return tip;
		}
	};

	public SelectField(String key, String value, String[] values) {
		this(key, value, (String) null, values);
	}

	public SelectField(String key, String value, String help, String[] values) {
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
		label.setPreferredSize(new Dimension(60, Integer.MAX_VALUE));
		label.setHorizontalAlignment(SwingConstants.RIGHT);

		field = new JComboBox<String>();
		for (String item : values)
			field.addItem(item);
		field.setSelectedItem(value);
		field.setEditable(false);

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout(10, 10));
		center.add(field, BorderLayout.WEST);

		icon.setPreferredSize(new Dimension(40, Integer.MAX_VALUE));
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

	public JComboBox<String> getField() {
		return field;
	}

	public int getIndex() {
		return field.getSelectedIndex();
	}

	public String getValue() {
		return (String) field.getSelectedItem();
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