package com.talent518.ftp.gui.dialog;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.gui.MainFrame;
import com.talent518.ftp.gui.form.ButtonForm;
import com.talent518.ftp.gui.form.FormField;
import com.talent518.ftp.validator.RequiredValidator;

public class NameDialog extends JDialog {
	private static final long serialVersionUID = -8347197651827484321L;

	final ResourceBundle language = Settings.language();
	final JPanel panel = new JPanel();
	final FormField nameField;
	final ButtonForm btn = new ButtonForm();
	final JButton confirm = new JButton(language.getString("name.confirm"));
	final JButton cancel = new JButton(language.getString("name.cancel"));

	public NameDialog(MainFrame frame) {
		this(frame, false);
	}

	public NameDialog(MainFrame frame, boolean model) {
		super(frame, model);

		setSize(400, 50 * 2 + 10 * 3);
		setLocationRelativeTo(null);
		setResizable(false);
		setIconImage(MainFrame.icon.getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridLayout(2, 1, 10, 10));
		setContentPane(panel);

		nameField = new FormField("favorite.name", "", RequiredValidator.class);

		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NameDialog.this.dispose();
			}
		});

		btn.add(confirm);
		btn.add(cancel);

		panel.add(nameField);
		panel.add(btn);
	}

	public void show(String title, Listener l) {
		show(title, "", l);
	}

	public void show(String title, String value, Listener l) {
		setTitle(title.replaceAll("\\s+\\([A-Z0-9]\\)$", ""));
		nameField.getField().setText(value);
		nameField.getField().addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					confirm(l);
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				confirm(l);
			}
		});
		setVisible(true);
	}

	private void confirm(Listener l) {
		if (!nameField.validator()) {
			return;
		}

		l.name(nameField.getValue());

		dispose();
	}

	public interface Listener {
		public void name(String name);
	}
}
