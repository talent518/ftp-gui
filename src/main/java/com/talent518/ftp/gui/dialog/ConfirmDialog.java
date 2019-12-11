package com.talent518.ftp.gui.dialog;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.MainFrame;
import com.talent518.ftp.gui.form.ButtonForm;

public class ConfirmDialog extends JDialog {
	private static final long serialVersionUID = -8347197651827484456L;

	final ResourceBundle language = Settings.language();
	final JPanel panel = new JPanel();
	final JLabel label = new JLabel();
	final ButtonForm btn = new ButtonForm();
	final JButton confirm = new JButton(language.getString("name.confirm"));
	final JButton cancel = new JButton(language.getString("name.cancel"));
	final Timer timer = new Timer(true);

	public ConfirmDialog(MainFrame frame) {
		this(frame, false);
	}

	public ConfirmDialog(MainFrame frame, boolean model) {
		super(frame, model);

		setSize(400, 50 * 2 + 10 * 3);
		setLocationRelativeTo(null);
		setResizable(false);
		setIconImage(MainFrame.icon.getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridLayout(2, 1, 10, 10));
		setContentPane(panel);

		label.setHorizontalAlignment(SwingConstants.CENTER);

		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConfirmDialog.this.dispose();
			}
		});

		btn.add(confirm);
		btn.add(cancel);

		panel.add(label);
		panel.add(btn);
	}

	public void show(String title, String label, String confirm, Runnable runnable) {
		setTitle(title);
		this.label.setText(label);
		if (confirm != null)
			this.confirm.setText(confirm);
		this.confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				runnable.run();
				ConfirmDialog.this.dispose();
			}
		});
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				ConfirmDialog.this.confirm.setEnabled(false);
				runnable.run();
				ConfirmDialog.this.confirm.setEnabled(true);
			}
		}, 250, 250);
		setVisible(true);
	}

	@Override
	public void dispose() {
		timer.cancel();

		super.dispose();
	}
}
