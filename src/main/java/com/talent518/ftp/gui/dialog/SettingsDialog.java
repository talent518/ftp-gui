package com.talent518.ftp.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.MainFrame;
import com.talent518.ftp.gui.form.BooleanField;
import com.talent518.ftp.gui.form.ButtonForm;
import com.talent518.ftp.gui.form.FormField;
import com.talent518.ftp.gui.form.IntegerField;
import com.talent518.ftp.gui.form.SelectField;

public class SettingsDialog extends JDialog {
	private static final long serialVersionUID = -8347197651827484321L;

	final Settings settings = Settings.instance();
	final ResourceBundle language = Settings.language();
	final JPanel panel = new JPanel();
	final BooleanField watchField;
	final IntegerField nthreadsField;
	final BooleanField isScrollTopField;
	final IntegerField triesField;
	final IntegerField logLinesField;
	final SelectField fontField;
	final ButtonForm btn = new ButtonForm();
	final JButton confirm = new JButton(language.getString("favorite.confirm"));
	final JButton cancel = new JButton(language.getString("favorite.cancel"));

	public SettingsDialog(MainFrame frame) {
		this(frame, false);
	}

	public SettingsDialog(MainFrame frame, boolean model) {
		super(frame, model);

		setTitle(language.getString("file.preferences").replaceAll("\\s+\\([^\\)]+\\)$", ""));
		setSize(500, 40 * 7 + 10 * 8);
		setLocationRelativeTo(null);
		setResizable(false);
		setIconImage(MainFrame.icon.getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridLayout(7, 1, 10, 10));
		setContentPane(panel);

		watchField = new BooleanField("settings.watch", settings.isWatch(), "settings.watch.help");
		nthreadsField = new IntegerField("settings.nthreads", settings.getNthreads(), "settings.nthreads.help", 1, Runtime.getRuntime().availableProcessors() * 4, 1);
		isScrollTopField = new BooleanField("settings.isScrollTop", settings.isScrollTop(), "settings.isScrollTop.help");
		triesField = new IntegerField("settings.tries", settings.getTries(), "settings.tries.help", 0, 100, 1);
		logLinesField = new IntegerField("settings.logLines", settings.getLogLines(), "settings.logLines.help", 10000, 1000000, 1000);
		fontField = new SelectField("settings.font", settings.getFont(), "settings.font.help", GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.SIMPLIFIED_CHINESE));
		fontField.getField().setRenderer(new BasicComboBoxRenderer() {
			private static final long serialVersionUID = 5124430506703490578L;

			@Override
			public Dimension getPreferredSize() {
				Dimension size = super.getPreferredSize();
				size.height = 30;
				return size;
			}

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				BasicComboBoxRenderer c = (BasicComboBoxRenderer) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				c.setFont(new Font((String) value, Font.PLAIN, 12));
				c.setToolTipText((String) value);
				c.setBorder(new EmptyBorder(0, 10, 0, 10));
				return c;
			}
		});

		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String font = settings.getFont();

				settings.setWatch(watchField.getValue());
				settings.setNthreads(nthreadsField.getValue());
				settings.setScrollTop(isScrollTopField.getValue());
				settings.setTries(triesField.getValue());
				settings.setLogLines(logLinesField.getValue());
				settings.setFont(fontField.getValue());
				settings.save();

				SettingsDialog.this.dispose();

				if (!font.equals(settings.getFont()))
					frame.restart();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SettingsDialog.this.dispose();
			}
		});

		btn.add(confirm);
		btn.add(cancel);

		panel.add(watchField);
		panel.add(nthreadsField);
		panel.add(isScrollTopField);
		panel.add(triesField);
		panel.add(logLinesField);
		panel.add(fontField);
		panel.add(btn);

		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component com = panel.getComponent(i);
			if (com instanceof FormField) {
				((FormField) com).getLabel().setPreferredSize(new Dimension(100, Integer.MAX_VALUE));
			} else if (com instanceof IntegerField) {
				((IntegerField) com).getLabel().setPreferredSize(new Dimension(100, Integer.MAX_VALUE));
			} else if (com instanceof BooleanField) {
				((BooleanField) com).getLabel().setPreferredSize(new Dimension(100, Integer.MAX_VALUE));
			} else if (com instanceof SelectField) {
				((SelectField) com).getLabel().setPreferredSize(new Dimension(100, Integer.MAX_VALUE));
			}
		}
	}
}
