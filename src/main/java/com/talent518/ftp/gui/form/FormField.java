package com.talent518.ftp.gui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.ui.MultiLineToolTip;
import com.talent518.ftp.validator.Validator;

public class FormField extends JPanel {
	private static final long serialVersionUID = 7338631397387052286L;
	public static final Icon errorIcon = new ImageIcon(FormField.class.getResource("/icons/error.png"));
	public static final Icon helpIcon = new ImageIcon(FormField.class.getResource("/icons/help.png"));

	final ResourceBundle language = Settings.language();
	private JLabel label;
	private JTextField field;
	private JLabel icon = new JLabel(helpIcon) {
		private static final long serialVersionUID = 3083135914717990808L;

		@Override
		public JToolTip createToolTip() {
			JToolTip tip = new MultiLineToolTip();
			tip.setComponent(this);
			return tip;
		}
	};
	private Validator[] validators;
	private String help;

	public FormField(String key, String val) {
		this(key, val, (String) null);
	}

	public FormField(String key, String val, String help) {
		super();

		try {
			this.help = (help == null ? null : language.getString(help));
		} catch(Exception e) {
			this.help = help;
			e.printStackTrace();
		}

		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout(10, 10));

		label = new JLabel(language.getString(key));
		label.setPreferredSize(new Dimension(60, HEIGHT));
		label.setHorizontalAlignment(SwingConstants.RIGHT);

		field = new JTextField(val);

		icon.setPreferredSize(new Dimension(40, HEIGHT));
		icon.setHorizontalAlignment(SwingConstants.CENTER);
		icon.setVerticalAlignment(SwingConstants.CENTER);
		setHelp(null);

		add(label, BorderLayout.WEST);
		add(field, BorderLayout.CENTER);
		add(icon, BorderLayout.EAST);
	}

	@SafeVarargs
	public FormField(String key, String val, Class<? extends Validator>... clazzs) {
		this(key, val, null, clazzs);
	}

	@SafeVarargs
	public FormField(String key, String val, String help, Class<? extends Validator>... clazzs) {
		this(key, val, help);

		validators = new Validator[clazzs.length];
		for (int i = 0; i < clazzs.length; i++) {
			try {
				validators[i] = clazzs[i].getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				validators[i] = null;
				e.printStackTrace();
			}
		}
	}

	public JLabel getLabel() {
		return label;
	}

	public JTextField getField() {
		return field;
	}

	public String getValue() {
		return field.getText().trim();
	}

	public void setHelp(String help) {
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

	public boolean validator() {
		String val = getValue();
		for (Validator v : validators) {
			if (!v.validate(val)) {
				setHelp(v.getMessage());
				return false;
			}
		}
		setHelp(null);
		return true;
	}
}