package com.talent518.ftp.gui.dialog;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

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

public class ResumeDialog extends JDialog {
	private static final long serialVersionUID = -8347197851827484459L;

	final ResourceBundle language = Settings.language();
	final JPanel panel = new JPanel();
	final JLabel label = new JLabel(language.getString("resume.content"));
	final ButtonForm btn = new ButtonForm();
	final JButton override = new JButton(language.getString("name.override"));
	final JButton resume = new JButton(language.getString("name.resume"));

	public ResumeDialog(MainFrame frame) {
		this(frame, false);
	}

	public ResumeDialog(MainFrame frame, boolean model) {
		super(frame, model);

		setTitle(language.getString("resume.title"));
		setSize(400, 50 * 2 + 10 * 3);
		setLocationRelativeTo(null);
		setResizable(false);
		setIconImage(MainFrame.icon.getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		panel.setLayout(new GridLayout(2, 1, 10, 10));
		setContentPane(panel);

		label.setHorizontalAlignment(SwingConstants.CENTER);

		btn.add(override);
		btn.add(resume);

		panel.add(label);
		panel.add(btn);
	}

	public void show(Listener l) {
		override.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				l.resume(false);

				ResumeDialog.this.dispose();
			}
		});
		resume.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				l.resume(true);

				ResumeDialog.this.dispose();
			}
		});
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				l.resume(false);
			}
		});
		setVisible(true);
	}

	public interface Listener {
		public void resume(boolean resume);
	}
}
