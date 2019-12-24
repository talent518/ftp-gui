package com.talent518.ftp.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.MainFrame;
import com.talent518.ftp.gui.form.BooleanField;
import com.talent518.ftp.gui.form.ButtonForm;
import com.talent518.ftp.gui.form.FormField;
import com.talent518.ftp.gui.form.IntegerField;

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
	final ButtonForm btn = new ButtonForm();
	final JButton confirm = new JButton(language.getString("favorite.confirm"));
	final JButton cancel = new JButton(language.getString("favorite.cancel"));

	public SettingsDialog(MainFrame frame) {
		this(frame, false);
	}

	public SettingsDialog(MainFrame frame, boolean model) {
		super(frame, model);

		setTitle(language.getString("file.preferences").replaceAll("\\s+\\([^\\)]+\\)$", ""));
		setSize(300, 40 * 6 + 10 * 7);
		setLocationRelativeTo(null);
		setResizable(false);
		setIconImage(MainFrame.icon.getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridLayout(6, 1, 10, 10));
		setContentPane(panel);

		watchField = new BooleanField("settings.watch", settings.isWatch(), "settings.watch.help");
		nthreadsField = new IntegerField("settings.nthreads", settings.getNthreads(), "settings.nthreads.help", 1, Runtime.getRuntime().availableProcessors() * 4, 1);
		isScrollTopField = new BooleanField("settings.isScrollTop", settings.isScrollTop(), "settings.isScrollTop.help");
		triesField = new IntegerField("settings.tries", settings.getTries(), "settings.tries.help", 0, 100, 1);
		logLinesField = new IntegerField("settings.logLines", settings.getLogLines(), "settings.logLines.help", 10000, 1000000, 1000);

		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settings.setWatch(watchField.getValue());
				settings.setNthreads(nthreadsField.getValue());
				settings.setScrollTop(isScrollTopField.getValue());
				settings.setTries(triesField.getValue());
				settings.setLogLines(logLinesField.getValue());
				settings.save();

				SettingsDialog.this.dispose();
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
		panel.add(btn);

		for (int i = 0; i < panel.getComponentCount(); i++) {
			Component com = panel.getComponent(i);
			if (com instanceof FormField) {
				((FormField) com).getLabel().setPreferredSize(new Dimension(100, Integer.MAX_VALUE));
			} else if (com instanceof IntegerField) {
				((IntegerField) com).getLabel().setPreferredSize(new Dimension(100, Integer.MAX_VALUE));
			} else if (com instanceof BooleanField) {
				((BooleanField) com).getLabel().setPreferredSize(new Dimension(100, Integer.MAX_VALUE));
			}
		}
	}
}
