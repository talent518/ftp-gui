package com.talent518.ftp.gui.dialog;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.dao.Site.Favorite;
import com.talent518.ftp.gui.MainFrame;
import com.talent518.ftp.gui.form.ButtonForm;
import com.talent518.ftp.gui.form.FormField;
import com.talent518.ftp.validator.RequiredValidator;

public class FavoriteDialog extends JDialog {
	private static final long serialVersionUID = -8347197651827484321L;

	final ResourceBundle language = Settings.language();
	final JPanel panel = new JPanel();
	final FormField nameField;
	final FormField remoteField;
	final FormField localField;
	final ButtonForm btn = new ButtonForm();
	final JButton confirm = new JButton(language.getString("favorite.confirm"));
	final JButton cancel = new JButton(language.getString("favorite.cancel"));

	public FavoriteDialog(MainFrame frame) {
		this(frame, false);
	}

	public FavoriteDialog(MainFrame frame, boolean model) {
		super(frame, model);

		setTitle(language.getString("favorite.title"));
		setSize(400, 40 * 4 + 10 * 5);
		setLocationRelativeTo(null);
		setResizable(false);
		setIconImage(MainFrame.icon.getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridLayout(4, 1, 10, 10));
		setContentPane(panel);

		nameField = new FormField("favorite.name", "", RequiredValidator.class);
		remoteField = new FormField("favorite.remote", frame.getRemoteAddr(), RequiredValidator.class);
		localField = new FormField("favorite.local", frame.getLocalAddr(), RequiredValidator.class);

		confirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!nameField.validator() || !remoteField.validator() || !localField.validator()) {
					return;
				}

				Favorite f = new Favorite(nameField.getValue(), localField.getValue(), remoteField.getValue());
				Map<String, Favorite> favorites = frame.getProtocol().getSite().getFavorites();
				favorites.put(f.getName(), f);
				Settings.instance().save();
				frame.initToolbar(favorites);

				FavoriteDialog.this.dispose();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FavoriteDialog.this.dispose();
			}
		});

		btn.add(confirm);
		btn.add(cancel);

		add(nameField);
		add(remoteField);
		add(localField);
		add(btn);
	}
}
