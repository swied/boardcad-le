package boardcad.gui.jdk;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import board.BezierBoard;
import boardcad.i18n.LanguageResource;
import cadcore.UnitUtils;

public class TailDesignerDialog extends JDialog {
	static final long serialVersionUID=1L;

	private JPanel jContentPane = null;
	private JLabel mTailTypeLabel = null;
	private JComboBox<String> mTailTypeComboBox = null;
	private JLabel mSwallowDepthLabel = null;
	private JTextField mSwallowDepthTextField = null;
	private JLabel mSwallowWidthLabel = null;
	private JTextField mSwallowWidthTextField = null;
	private JButton mOkButton = null;
	private JButton mCancelButton = null;
	private BezierBoard mBrd;

	public TailDesignerDialog(BezierBoard brd) {
		super();
		mBrd = brd;
		initialize();
	}

	private void initialize() {
		this.setSize(new Dimension(350, 250));
		this.setContentPane(getJContentPane());
		this.setTitle(LanguageResource.getString("TAILDESIGNERTITLE_STR"));
		this.setLocationRelativeTo(null);
		
		mTailTypeComboBox.setSelectedIndex(mBrd.getTailType());
		mSwallowDepthTextField.setText(UnitUtils.convertLengthToCurrentUnit(mBrd.getSwallowTailDepth(), false));
		mSwallowWidthTextField.setText(UnitUtils.convertLengthToCurrentUnit(mBrd.getSwallowTailWidth(), false));
		
		updateEnabledState();
	}

	private JPanel getJContentPane() {
		if (jContentPane == null) {
			mTailTypeLabel = new JLabel();
			mTailTypeLabel.setBounds(new Rectangle(20, 20, 120, 20));
			mTailTypeLabel.setText(LanguageResource.getString("TAILTYPE_STR"));
			mTailTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

			mSwallowDepthLabel = new JLabel();
			mSwallowDepthLabel.setBounds(new Rectangle(20, 60, 120, 20));
			mSwallowDepthLabel.setText(LanguageResource.getString("SWALLOWTAILDEPTH_STR"));
			mSwallowDepthLabel.setHorizontalAlignment(SwingConstants.RIGHT);

			mSwallowWidthLabel = new JLabel();
			mSwallowWidthLabel.setBounds(new Rectangle(20, 100, 120, 20));
			mSwallowWidthLabel.setText(LanguageResource.getString("SWALLOWTAILWIDTH_STR"));
			mSwallowWidthLabel.setHorizontalAlignment(SwingConstants.RIGHT);

			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(mTailTypeLabel, null);
			jContentPane.add(getTailTypeComboBox(), null);
			jContentPane.add(mSwallowDepthLabel, null);
			jContentPane.add(getSwallowDepthTextField(), null);
			jContentPane.add(mSwallowWidthLabel, null);
			jContentPane.add(getSwallowWidthTextField(), null);
			jContentPane.add(getOkButton(), null);
			jContentPane.add(getCancelButton(), null);
		}
		return jContentPane;
	}

	private JComboBox<String> getTailTypeComboBox() {
		if (mTailTypeComboBox == null) {
			String[] tailTypes = {
				LanguageResource.getString("STANDARDTAIL_STR"),
				LanguageResource.getString("SWALLOWTAIL_STR")
			};
			mTailTypeComboBox = new JComboBox<>(tailTypes);
			mTailTypeComboBox.setBounds(new Rectangle(150, 20, 150, 20));
			mTailTypeComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateEnabledState();
				}
			});
		}
		return mTailTypeComboBox;
	}

	private JTextField getSwallowDepthTextField() {
		if (mSwallowDepthTextField == null) {
			mSwallowDepthTextField = new JTextField();
			mSwallowDepthTextField.setBounds(new Rectangle(150, 60, 100, 20));
		}
		return mSwallowDepthTextField;
	}

	private JTextField getSwallowWidthTextField() {
		if (mSwallowWidthTextField == null) {
			mSwallowWidthTextField = new JTextField();
			mSwallowWidthTextField.setBounds(new Rectangle(150, 100, 100, 20));
		}
		return mSwallowWidthTextField;
	}

	private void updateEnabledState() {
		boolean isSwallow = mTailTypeComboBox.getSelectedIndex() == 1;
		mSwallowDepthTextField.setEnabled(isSwallow);
		mSwallowWidthTextField.setEnabled(isSwallow);
	}

	private JButton getOkButton() {
		if (mOkButton == null) {
			mOkButton = new JButton();
			mOkButton.setBounds(new Rectangle(80, 160, 80, 26));
			mOkButton.setText(LanguageResource.getString("OKBUTTON_STR"));
			mOkButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						int type = mTailTypeComboBox.getSelectedIndex();
						double depth = UnitUtils.convertInputStringToInternalLengthUnit(mSwallowDepthTextField.getText());
						double width = UnitUtils.convertInputStringToInternalLengthUnit(mSwallowWidthTextField.getText());
						
						if(type == 1) {
							double maxDepth = mBrd.getMaxWidthPos();
							if(depth > maxDepth) depth = maxDepth;
							if(depth < 0) depth = 0;
						}
						
						mBrd.setTailType(type);
						mBrd.setSwallowTailDepth(depth);
						mBrd.setSwallowTailWidth(width);
						mBrd.onOutlineChanged();
					} catch (Exception ex) {
						// Ignore or show error
					}
					setVisible(false);
				}
			});
		}
		return mOkButton;
	}

	private JButton getCancelButton() {
		if (mCancelButton == null) {
			mCancelButton = new JButton();
			mCancelButton.setBounds(new Rectangle(180, 160, 80, 26));
			mCancelButton.setText(LanguageResource.getString("CANCELBUTTON_STR"));
			mCancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return mCancelButton;
	}
}
