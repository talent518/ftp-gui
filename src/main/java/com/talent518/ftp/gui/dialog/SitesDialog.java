package com.talent518.ftp.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.talent518.ftp.dao.Settings;
import com.talent518.ftp.dao.Site;
import com.talent518.ftp.gui.MainFrame;
import com.talent518.ftp.gui.form.BooleanField;
import com.talent518.ftp.gui.form.ButtonForm;
import com.talent518.ftp.gui.form.FormField;
import com.talent518.ftp.gui.form.IntegerField;
import com.talent518.ftp.gui.form.SelectField;
import com.talent518.ftp.gui.ui.SiteTabbedPaneUI;
import com.talent518.ftp.gui.ui.Toast;
import com.talent518.ftp.gui.ui.VerticalFlowLayout;
import com.talent518.ftp.validator.RequiredValidator;

public class SitesDialog extends JDialog {
	private static final long serialVersionUID = -8347197651827484321L;

	private static final ImageIcon upHoverIcon = new ImageIcon(SitesDialog.class.getResource("/icons/up_hover.png"));
	private static final ImageIcon downHoverIcon = new ImageIcon(SitesDialog.class.getResource("/icons/down_hover.png"));
	private static final ImageIcon copyHoverIcon = new ImageIcon(SitesDialog.class.getResource("/icons/copy_hover.png"));
	private static final ImageIcon rmHoverIcon = new ImageIcon(SitesDialog.class.getResource("/icons/remove_hover.png"));
	private static final ImageIcon addHoverIcon = new ImageIcon(SitesDialog.class.getResource("/icons/add_hover.png"));

	private static final ImageIcon upIcon = new ImageIcon(SitesDialog.class.getResource("/icons/up.png"));
	private static final ImageIcon downIcon = new ImageIcon(SitesDialog.class.getResource("/icons/down.png"));
	private static final ImageIcon copyIcon = new ImageIcon(SitesDialog.class.getResource("/icons/copy.png"));
	private static final ImageIcon rmIcon = new ImageIcon(SitesDialog.class.getResource("/icons/remove.png"));
	private static final ImageIcon addIcon = new ImageIcon(SitesDialog.class.getResource("/icons/add.png"));

	final Settings settings = Settings.instance();
	final ResourceBundle language = Settings.language();
	final JPanel panel = new JPanel();

	final Map<String, Site> siteMap = new HashMap<String, Site>();

	final DefaultListModel<String> model = new DefaultListModel<String>();
	final JList<String> siteList = new JList<String>(model);
	final SiteForm siteForm = new SiteForm();

	final MainFrame mainFrame;

	public SitesDialog(MainFrame frame) {
		this(frame, false);
	}

	public SitesDialog(MainFrame frame, boolean modal) {
		super(frame, modal);

		mainFrame = frame;

		setTitle(language.getString("site.manage").replaceAll("\\s+\\([^\\)]+\\)$", ""));
		setSize(800, 600);
		setLocationRelativeTo(null);
		setResizable(false);
		setIconImage(MainFrame.icon.getImage());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
		panel.setLayout(new BorderLayout(0, 0));
		setContentPane(panel);

		JScrollPane left = new JScrollPane(siteList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		left.setPreferredSize(new Dimension(150, HEIGHT));
		left.setBorder(new LineBorder(UIManager.getColor("TabbedPane.darkShadow"), 1));

		JLabel up = new JLabel(upIcon);
		JLabel down = new JLabel(downIcon);
		JLabel copy = new JLabel(copyIcon);
		JLabel rm = new JLabel(rmIcon);
		JLabel add = new JLabel(addIcon);

		up.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int i = siteList.getSelectedIndex();
				if (i > 0) {
					String s = model.get(i - 1);
					model.set(i - 1, model.get(i));
					model.set(i, s);
					siteList.setSelectedIndex(i - 1);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				up.setIcon(upHoverIcon);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				up.setIcon(upIcon);
			}
		});
		down.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int i = siteList.getSelectedIndex();
				if (i + 1 < model.size()) {
					String s = model.get(i + 1);
					model.set(i + 1, model.get(i));
					model.set(i, s);
					siteList.setSelectedIndex(i + 1);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				down.setIcon(downHoverIcon);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				down.setIcon(downIcon);
			}
		});
		copy.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String s = siteList.getSelectedValue();
				String s2;
				int i = 0;
				do {
					s2 = s + "(" + (++i) + ")";
				} while (model.contains(s2));
				Site ss = siteMap.get(s).clone();
				ss.setName(s2);
				siteMap.put(s2, ss);
				model.add(model.indexOf(s) + 1, s2);
				siteList.setSelectedValue(s2, true);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				copy.setIcon(copyHoverIcon);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				copy.setIcon(copyIcon);
			}
		});
		rm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int i = siteList.getSelectedIndex();
				String s = siteList.getSelectedValue();

				siteForm.setSite(null);
				siteMap.remove(s);
				model.removeElement(s);
				siteList.setSelectedIndex(i < model.size() ? i : i - 1);
				siteList.revalidate();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				rm.setIcon(rmHoverIcon);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				rm.setIcon(rmIcon);
			}
		});
		add.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				siteForm.save();

				String s;
				int i = 0;
				do {
					s = String.format(language.getString("site.nameFormat"), ++i);
				} while (model.contains(s));

				Site s2 = new Site(s, "ftp", "", 0);

				model.addElement(s);
				siteMap.put(s, s2);

				siteList.setSelectedValue(s, true);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				add.setIcon(addHoverIcon);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				add.setIcon(addIcon);
			}
		});

		JPanel btn = new JPanel();
		btn.setBorder(BorderFactory.createEmptyBorder());
		btn.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		btn.add(up);
		btn.add(down);
		btn.add(copy);
		btn.add(rm);
		btn.add(add);

		JPanel left2 = new JPanel();
		left2.setBorder(BorderFactory.createEmptyBorder());
		left2.setLayout(new BorderLayout(10, 10));
		left2.add(left, BorderLayout.CENTER);
		left2.add(btn, BorderLayout.SOUTH);

		panel.add(left2, BorderLayout.WEST);
		panel.add(siteForm, BorderLayout.CENTER);

		for (String s : settings.getSiteNames())
			model.addElement(s);
		for (Site s : settings.getSites().values())
			siteMap.put(s.getName(), (Site) s.clone());

		siteList.setFixedCellHeight(30);
		siteList.addListSelectionListener(new ListSelectionListener() {
			int i = -1;

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!siteList.getValueIsAdjusting()) {
					if(i == siteList.getSelectedIndex()) {
						return;
					}

					if(siteForm.save()) {
						siteForm.setSite(siteMap.get(siteList.getSelectedValue()));
						i = siteList.getSelectedIndex();
					} else {
						siteList.setSelectedIndex(i);
						siteForm.setSite(siteMap.get(siteList.getSelectedValue()));
						siteForm.alert(false);
					}
				}
			}
		});
	}

	private class SiteForm extends JPanel implements ActionListener {
		private static final long serialVersionUID = -6139710137425580998L;

		private Site mSite = null;

		final JButton save = new JButton(language.getString("site.save"));
		final JButton close = new JButton(language.getString("site.close"));

		private JTabbedPane tabbed = new JTabbedPane(JTabbedPane.TOP);

		private FormField nameField;
		private SelectField protocolField;
		private FormField hostField;
		private IntegerField portField;
		private FormField userField;
		private FormField passField;
		private FormField remoteField;
		private FormField localField;
		private FormField remarkField;
		private BooleanField syncField;
		private BooleanField watchField;

		private SelectField encodingField;

		private FormField proxyHostField;
		private IntegerField proxyPortField;
		private FormField proxyUserField;
		private FormField proxyPassField;

		private BooleanField isImplicitField;
		private SelectField secretField;
		private SelectField trustmgrField;

		private BooleanField hiddenField;
		private SelectField serverTypeField;
		private BooleanField saveUnparseableField;
		private BooleanField binaryTransferField;
		private BooleanField localActiveField;
		private BooleanField useEpsvWithIPv4Field;
		private BooleanField isMlsdField;

		private SelectField transferModeField;
		private SelectField protField;

		private FormField privateKeyField;
		private FormField passphraseField;

		public SiteForm() {
			super();

			final ButtonForm btn = new ButtonForm();
			save.addActionListener(e -> {
				if(save()) {
					settings.getSiteNames().clear();
					for (int i = 0; i < model.size(); i++)
						settings.getSiteNames().add(model.get(i));

					settings.getSites().clear();
					for (String key : siteMap.keySet())
						settings.getSites().put(key, siteMap.get(key));

					settings.save();
					
					mainFrame.reInitSite();

					alert(true);
				} else {
					alert(false);
				}
			});
			close.addActionListener(e -> {
				SitesDialog.this.dispose();
			});

			btn.setPreferredSize(new Dimension(WIDTH, 30));
			btn.add(save);
			btn.add(close);

			tabbed.setUI(new SiteTabbedPaneUI());

			setBorder(BorderFactory.createEmptyBorder());
			setLayout(new BorderLayout(0, 0));
			add(tabbed, BorderLayout.CENTER);
			add(btn, BorderLayout.SOUTH);

			initBasic();
			initAdvanced();
		}
		
		public void alert(boolean ok) {
			String msg = language.getString(ok ? "site.save.success" : "site.save.failure");
			new Toast(SitesDialog.this, msg, 1000, ok ? Toast.TYPE_SUCCESS : Toast.TYPE_ERROR).start();
		}

		public void setSite(Site site) {
			mSite = site;

			if (site == null) {
				site = new Site("", "ftp", "", 0);
			}

			nameField.getField().setText(site.getName());
			nameField.setHelp(null);
			protocolField.getField().setSelectedItem(site.getProtocol());
			hostField.getField().setText(site.getHost());
			hostField.setHelp(null);
			portField.getField().setValue(site.getPort());
			userField.getField().setText(site.getUsername());
			userField.setHelp(null);
			passField.getField().setText(site.getPassword());
			passField.setHelp(null);
			remoteField.getField().setText(site.getRemote());
			localField.getField().setText(site.getLocal());
			remarkField.getField().setText(site.getRemark());
			syncField.getField().setSelected(site.isSync());
			watchField.getField().setSelected(site.isWatch());

			encodingField.getField().setSelectedItem(site.getEncoding());

			proxyHostField.getField().setText(site.getProxyHost());
			proxyPortField.getField().setValue(site.getProxyPort());
			proxyUserField.getField().setText(site.getProxyUser());
			proxyPassField.getField().setText(site.getProxyPassword());
			
			privateKeyField.getField().setText(site.getPrivateKey());

			isImplicitField.getField().setSelected(site.isImplicit());
			secretField.getField().setSelectedItem(site.getSecret());
			trustmgrField.getField().setSelectedItem(site.getTrustmgr());

			hiddenField.getField().setSelected(site.isHidden());
			serverTypeField.getField().setSelectedItem(site.getServerType());
			saveUnparseableField.getField().setSelected(site.isSaveUnparseable());
			binaryTransferField.getField().setSelected(site.isBinaryTransfer());
			localActiveField.getField().setSelected(site.isLocalActive());
			useEpsvWithIPv4Field.getField().setSelected(site.isUseEpsvWithIPv4());
			isMlsdField.getField().setSelected(site.isMlsd());

			transferModeField.getField().setSelectedItem(site.getTransferMode());
			protField.getField().setSelectedItem(site.getProt());
		}

		public boolean save() {
			if(mSite == null) {
				return true;
			}

			if (!nameField.validator() || !hostField.validator() || !userField.validator() || !passField.validator()) {
				return false;
			}

			String name = mSite.getName();

			mSite.setName(nameField.getValue());
			mSite.setProtocol(protocolField.getValue());
			mSite.setHost(hostField.getValue());
			mSite.setPort(portField.getValue());
			mSite.setUsername(userField.getValue());
			mSite.setPassword(passField.getValue());
			mSite.setRemote(remoteField.getValue());
			mSite.setLocal(localField.getValue());
			mSite.setRemark(remarkField.getValue());
			mSite.setSync(syncField.getValue() && localField.validator() && remoteField.validator());
			mSite.setWatch(watchField.getValue());

			mSite.setEncoding(encodingField.getValue());

			mSite.setProxyHost(proxyHostField.getValue());
			mSite.setProxyPort(proxyPortField.getValue());
			mSite.setProxyUser(proxyUserField.getValue());
			mSite.setProxyPassword(proxyPassField.getValue());
			
			mSite.setPrivateKey(privateKeyField.getValue());

			mSite.setImplicit(isImplicitField.getValue());
			mSite.setSecret(secretField.getValue());
			mSite.setTrustmgr(trustmgrField.getValue());

			mSite.setHidden(hiddenField.getValue());
			mSite.setServerType(serverTypeField.getValue());
			mSite.setSaveUnparseable(saveUnparseableField.getValue());
			mSite.setBinaryTransfer(binaryTransferField.getValue());
			mSite.setLocalActive(localActiveField.getValue());
			mSite.setUseEpsvWithIPv4(useEpsvWithIPv4Field.getValue());
			mSite.setMlsd(isMlsdField.getValue());

			mSite.setTransferMode(transferModeField.getValue());
			mSite.setProt(protField.getValue());
			
			int i = model.indexOf(name);
			if (i >= 0) {
				model.set(i, nameField.getValue());
				siteMap.remove(name);
				siteMap.put(mSite.getName(), mSite);
			} else {
				mSite = null;
			}

			return true;
		}

		private void initBasic() {
			nameField = new FormField("site.name", "", RequiredValidator.class);
			protocolField = new SelectField("site.protocol", "", Site.getProtocols());
			protocolField.getField().addActionListener(this);
			hostField = new FormField("site.host", "", RequiredValidator.class);
			portField = new IntegerField("site.port", 0, 0, 65535, 1);
			userField = new FormField("site.user", "", RequiredValidator.class);
			passField = new FormField("site.pass", "", RequiredValidator.class);
			remoteField = new FormField("site.remote", "", "site.remote.help", RequiredValidator.class);
			localField = new FormField("site.local", "", "site.local.help", RequiredValidator.class);
			remarkField = new FormField("site.remark", "", RequiredValidator.class);
			syncField = new BooleanField("site.sync", false, "site.sync.help");
			watchField = new BooleanField("site.watch", false, "site.watch.help");

			JPanel panel = new JPanel();

			panel.setBorder(BorderFactory.createEmptyBorder());
			panel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 10, 10, true));

			JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.getVerticalScrollBar().setUnitIncrement(17);

			tabbed.add(language.getString("site.basic"), scrollPane);

			panel.add(nameField);
			panel.add(protocolField);
			panel.add(hostField);
			panel.add(portField);
			panel.add(userField);
			panel.add(passField);
			panel.add(remoteField);
			panel.add(localField);
			panel.add(remarkField);
			panel.add(syncField);
			panel.add(watchField);

			for (int i = 0; i < panel.getComponentCount(); i++) {
				Component com = panel.getComponent(i);
				com.setPreferredSize(new Dimension(WIDTH, 40));
				if (com instanceof FormField) {
					((FormField) com).getLabel().setPreferredSize(new Dimension(80, HEIGHT));
				} else if (com instanceof IntegerField) {
					((IntegerField) com).getLabel().setPreferredSize(new Dimension(80, HEIGHT));
				} else if (com instanceof BooleanField) {
					((BooleanField) com).getLabel().setPreferredSize(new Dimension(80, HEIGHT));
				} else if (com instanceof SelectField) {
					((SelectField) com).getLabel().setPreferredSize(new Dimension(80, HEIGHT));
				}
			}
		}

		private void initAdvanced() {
			Set<String> charsetSet = Charset.availableCharsets().keySet();
			String[] charsets = new String[charsetSet.size()];
			charsetSet.toArray(charsets);

			encodingField = new SelectField("site.encoding", "UTF-8", charsets);

			proxyHostField = new FormField("site.proxyHost", "", RequiredValidator.class);
			proxyPortField = new IntegerField("site.proxyPort", 0, 0, 65535, 1);
			proxyUserField = new FormField("site.proxyUser", "", RequiredValidator.class);
			proxyPassField = new FormField("site.proxyPass", "", RequiredValidator.class);

			isImplicitField = new BooleanField("site.isImplicit", false, "site.isImplicit.help");
			secretField = new SelectField("site.secret", "", "site.secret.help", new String[] { "", "SSL", "TLS" });
			trustmgrField = new SelectField("site.trustmgr", "all", "site.trustmgr.help", new String[] { "all", "valid", "none" });

			hiddenField = new BooleanField("site.hidden", false, "site.hidden.help");
			serverTypeField = new SelectField("site.serverType", "UNIX", "site.serverType.help", new String[] { "UNIX", "VMS", "WINDOWS" });
			saveUnparseableField = new BooleanField("site.saveUnparseable", false, "site.saveUnparseable.help");
			binaryTransferField = new BooleanField("site.binaryTransfer", false, "site.binaryTransfer.help");
			localActiveField = new BooleanField("site.localActive", false, "site.localActive.help");
			useEpsvWithIPv4Field = new BooleanField("site.useEpsvWithIPv4", false, "site.useEpsvWithIPv4.help");
			isMlsdField = new BooleanField("site.isMlsd", false, "site.isMlsd.help");

			transferModeField = new SelectField("site.transferMode", "", "site.transferMode.help", new String[] { "stream", "block", "compressed" });
			protField = new SelectField("site.prot", "", "site.prot.help", new String[] { "Clear", "Safe", "Confidential", "Private" });
			
			privateKeyField = new FormField("site.privateKey", "");
			passphraseField = new FormField("site.passphrase", "");

			JPanel panel = new JPanel();

			panel.setBorder(BorderFactory.createEmptyBorder());
			panel.setLayout(new VerticalFlowLayout(VerticalFlowLayout.TOP, 10, 10, true));

			JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scrollPane.getVerticalScrollBar().setUnitIncrement(17);

			tabbed.add(language.getString("site.advanced"), scrollPane);

			panel.add(encodingField);

			panel.add(proxyHostField);
			panel.add(proxyPortField);
			panel.add(proxyUserField);
			panel.add(proxyPassField);

			panel.add(privateKeyField);
			panel.add(passphraseField);

			panel.add(isImplicitField);
			panel.add(secretField);
			panel.add(trustmgrField);

			panel.add(hiddenField);
			panel.add(serverTypeField);
			panel.add(saveUnparseableField);
			panel.add(binaryTransferField);
			panel.add(localActiveField);
			panel.add(useEpsvWithIPv4Field);
			panel.add(isMlsdField);

			panel.add(transferModeField);
			panel.add(protField);

			for (int i = 0; i < panel.getComponentCount(); i++) {
				Component com = panel.getComponent(i);
				com.setPreferredSize(new Dimension(WIDTH, 40));
				if (com instanceof FormField) {
					((FormField) com).getLabel().setPreferredSize(new Dimension(120, HEIGHT));
				} else if (com instanceof IntegerField) {
					((IntegerField) com).getLabel().setPreferredSize(new Dimension(120, HEIGHT));
				} else if (com instanceof BooleanField) {
					((BooleanField) com).getLabel().setPreferredSize(new Dimension(120, HEIGHT));
				} else if (com instanceof SelectField) {
					((SelectField) com).getLabel().setPreferredSize(new Dimension(120, HEIGHT));
				}
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean isFTPS = "ftps".equals(protocolField.getValue());
			boolean isFTP = "ftp".equals(protocolField.getValue());
			boolean isFTPorFTPS = isFTP || isFTPS;
			boolean isSFTP = "sftp".equals(protocolField.getValue());
			
			proxyHostField.setVisible(!isFTPS);
			proxyPortField.setVisible(!isFTPS);
			proxyUserField.setVisible(!isFTPS);
			proxyPassField.setVisible(isFTP);

			// ftps
			isImplicitField.setVisible(isFTPS);
			secretField.setVisible(isFTPS);
			trustmgrField.setVisible(isFTPS);
			protField.setVisible(isFTPS);

			// ftp and ftps
			hiddenField.setVisible(isFTPorFTPS);
			serverTypeField.setVisible(isFTPorFTPS);
			saveUnparseableField.setVisible(isFTPorFTPS);
			binaryTransferField.setVisible(isFTPorFTPS);
			localActiveField.setVisible(isFTPorFTPS);
			useEpsvWithIPv4Field.setVisible(isFTPorFTPS);
			isMlsdField.setVisible(isFTPorFTPS);
			transferModeField.setVisible(isFTPorFTPS);
			
			// sftp
			privateKeyField.setVisible(isSFTP);
			passphraseField.setVisible(isSFTP);
		}
	}
}
