package boardcad.gui.jdk;

/*

 * Created on Sep 17, 2005

 *

 * To change the template for this generated file go to

 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments

 */

/**

 * @author Håvard

 *

 * To change the template for this generated type comment go to

 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments

 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.Timer;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.*;
import org.jogamp.java3d.Appearance;

import cadcore.*;
import board.*;
import boardcad.settings.*;
import boardcad.gui.jdk.actions.BoardSaveAsAction;
import boardcad.gui.jdk.actions.DeleteControlPointAction;
import boardcad.gui.jdk.actions.NextCrossSectionAction;
import boardcad.gui.jdk.actions.PreviousCrossSectionAction;
import boardcad.gui.jdk.actions.ToggleDeckAndBottomAction;
import boardcad.gui.jdk.plugin.*;
import boardcad.FileTools;
import boardcad.commands.*;
import boardcad.i18n.LanguageResource;
import board.writers.*;

public class BoardCAD implements Runnable, KeyEventDispatcher {
	private static final String appname = "BoardCAD v3.2.1 Limited Edition";
	enum DeckOrBottom {
		DECK, BOTTOM, BOTH
	};

	public static String defaultDirectory = "";
	
	protected static BoardCAD mInstance = null;

	protected BezierBoard mCurrentBrd;
	private BezierBoard mOriginalBrd;
	private BezierBoard mGhostBrd;

	private BrdCommand mCurrentCommand = new BrdEditCommand();
	private BrdCommand mPreviousCommand;


	private QuadView mQuadView;

	public QuadView getQuadView() {
		return mQuadView;
	}

	public String getQuadViewActiveName() {
		return getQuadView().getActive().getName();
	}

	private BoardEdit mQuadViewOutlineEdit;
	private BoardEdit mQuadViewCrossSectionEdit;
	private BoardEdit mQuadViewRockerEdit;

	private BoardEdit mOutlineEdit;
	BoardEdit mCrossSectionEdit;
	private BoardEdit mCrossSectionOutlineEdit;
	private BoardEdit mBottomAndDeckEdit;
	private BoardEdit mOutlineEdit2;

	DeckOrBottom mEditDeckOrBottom = DeckOrBottom.DECK;

	private BoardSpec mBoardSpec;

	private JPanel mBoardSpecPanel;

	private ControlPointInfo mControlPointInfo;

	private JSplitPane mCrossSectionSplitPane;

	private BrdEditSplitPane mOutlineAndProfileSplitPane;

	JPanel mRenderedPanel;
	
	JFrame mFrame;

	private ToolBar mToolBar;
	
	public JSplitPane mSplitPane;
	public JTabbedPane mTabbedPane;
	public JTabbedPane mTabbedPane2;

	private boolean mBoardChanged = false;
	protected boolean mGhostMode = false;
	protected boolean mOrgFocus = false;

	public static double mPrintMarginLeft = 72 / 4;
	public static double mPrintMarginRight = 72 / 4;
	public static double mPrintMarginTop = 72 / 4;
	public static double mPrintMarginBottom = 72 / 4;

	protected ThreeDView mRendered3DView;
	protected ThreeDView mQuad3DView;

	public StatusPanel mStatusPanel;

	WeightCalculatorDialog mWeightCalculatorDialog;

	BoardGuidePointsDialog mGuidePointsDialog;

	private BoardCADSettings mSettings;

	private BezierBoardCrossSection mCrossSectionCopy;

	Timer mBezier3DModelUpdateTimer;
	
	private boolean mIsFlipped = false;

	private MenuBar mMenuBar;

	private PopupMenu mPopupMenu;
	
	public static void main(final String[] args) {
		BoardCAD.getInstance();
	}

	public static BoardCAD getInstance() {
		if (mInstance == null) {
			mInstance = new BoardCAD();
			mInstance.init();
		}
		return mInstance;
	}

	protected BoardCAD() {
		/*
		 * // Test gcodedraw GeneralPath squarePath = new GeneralPath();
		 * squarePath.moveTo(1.0, -1.0); squarePath.lineTo(1.0, 1.0);
		 * squarePath.lineTo(-1.0, 1.0); squarePath.lineTo(-1.0, -1.0);
		 * squarePath.closePath();
		 *
		 * GeneralPath linePath = new GeneralPath(); linePath.moveTo(-1.0, 0.0); int
		 * steps = 100; for (int i = 0; i < steps; i++) { linePath.moveTo(-1.0 + ((2.0 *
		 * i) / steps), 0.0); }
		 *
		 * GCodeDraw gdrawSquare = new GCodeDraw(
		 * "C:/Users/Haavard/Desktop/G-Code/SquareTest.nc", 0.05, -0.05, 0.05, 0.01,
		 * 0.2, 0.03); GCodeDraw gdrawSquareNoOffset = new GCodeDraw(
		 * "C:/Users/Haavard/Desktop/G-Code/SquareTestNoOffset.nc", 0.0, -0.05, 0.05,
		 * 0.01, 0.2, 0.03);
		 *
		 * gdrawSquare.draw(squarePath); gdrawSquareNoOffset.draw(squarePath);
		 */
	}

	protected void init() {
		LanguageResource.init(this);

		mCurrentBrd = new BezierBoard();
		mGhostBrd = new BezierBoard();
		mOriginalBrd = new BezierBoard();

		mSettings = BoardCADSettings.getInstance();

		// Run the application
		SwingUtilities.invokeLater(this);
	}

	public void getPreferences() {
		// Preference keys for this package

		final Preferences prefs = Preferences.userNodeForPackage(BoardCAD.class);

		defaultDirectory = prefs.get("defaultDirectory", "");
		
		mMenuBar.getPreferences();

		mSettings.getPreferences();
	}
	
	public void putPreferences() {
		// Preference keys for this package
		final Preferences prefs = Preferences.userNodeForPackage(BoardCAD.class);

		prefs.put("defaultDirectory", BoardCAD.defaultDirectory);

		mMenuBar.putPreferences();

		mSettings.putPreferences();

	}

	public void updateBezier3DModel() {
		updateBezier3DModel(false);
	}

	public void updateBezier3DModel(boolean forceUpdate) {
		if (mTabbedPane.getSelectedComponent() == mRenderedPanel) {
			mRendered3DView.updateBezier3DModel(forceUpdate);
		} else if (mTabbedPane.getSelectedComponent() == mQuadView) {
			mQuad3DView.updateBezier3DModel(forceUpdate);
		}
	}
	
	public void resetBezier3DModel() {
		if (mTabbedPane.getSelectedComponent() == mRenderedPanel) {
			mRendered3DView.hardReset();
			mRendered3DView.setBackgroundColor(mSettings.getRenderBackgroundColor());
		} else if (mTabbedPane.getSelectedComponent() == mQuadView) {
			mQuad3DView.hardReset();
			mQuad3DView.setBackgroundColor(mSettings.getRenderBackgroundColor());
		}
	}

	
	public void setCurrentCommand(final BrdCommand command) {
		mCurrentCommand = command;
	}

	public BrdCommand getCurrentCommand() {
		return mCurrentCommand;
	}

	public void setSelectedEdit(final Component edit) {
		if (edit == mCrossSectionEdit) {
			mTabbedPane.setSelectedComponent(mCrossSectionSplitPane);
		} else if (edit == mOutlineEdit2) {
			mTabbedPane.setSelectedComponent(mOutlineAndProfileSplitPane);
		} else if (edit == mBottomAndDeckEdit) {
			mTabbedPane.setSelectedComponent(mBottomAndDeckEdit);
		} else if (edit == mQuadViewOutlineEdit || edit == mQuadViewCrossSectionEdit || edit == mQuadViewRockerEdit) {
			mTabbedPane.setSelectedComponent(mQuadView);
		} else {
			mTabbedPane.setSelectedComponent(edit);
		}
	}

	public JTabbedPane getTabbedPane() {
		return mTabbedPane;
	}

	public BoardEdit getSelectedEdit() {
		try {
			final Component component = mTabbedPane.getSelectedComponent();

			if (component == mCrossSectionSplitPane) {
				return mCrossSectionEdit;
			} else if (component == mOutlineAndProfileSplitPane) {
				return mOutlineAndProfileSplitPane.getActive();
			} else if (component == mQuadView) {
				return mQuadView.getActive();
			} else if (component instanceof BoardEdit) {
				return (BoardEdit)component;
			} else {
				return null;
			}
		} catch (final Exception e) {
			System.out.println("BoardCAD.getSelectedEdit() Exception: " + e.toString());
			return null;
		}
	}
	
	public BoardEdit getCrossSectionEdit() {
		return mCrossSectionEdit;
	}
	
	public BoardEdit getOutlineEdit() {
		return mOutlineEdit;
	}

	public BoardEdit getBottomAndDeckEdit() {
		return mBottomAndDeckEdit;
	}

	
	
	public BoardGuidePointsDialog getGuidePointsDialog() {
		return mGuidePointsDialog;
	}

	public JFrame getFrame() {
		return mFrame;
	}

	public ToolBar getToolBar() {
		return mToolBar;
	}
	
	
	// public MachineView getMachineView() {
	// return mMachineView;
	// }

	public ControlPointInfo getControlPointInfo() {
		return mControlPointInfo;
	}

	public boolean isPaintingOriginalBrd() {
		return mMenuBar.isPaintingOriginalBrd();
	}

	public boolean isPaintingGhostBrd() {
		return mMenuBar.isPaintingGhostBrd();
	}

	public boolean isPaintingGrid() {
		return mMenuBar.isPaintingGrid();
	}

	public boolean isPaintingControlPoints() {
		return mMenuBar.isPaintingControlPoints();
	}

	public boolean isPaintingNonActiveCrossSections() {
		return mMenuBar.isPaintingNonActiveCrossSections();
	}

	public boolean isPaintingGuidePoints() {
		return mMenuBar.isPaintingGuidePoints();
	}

	public boolean isPaintingCurvature() {
		return mMenuBar.isPaintingCurvature();
	}

	public boolean isPaintingVolumeDistribution() {
		return mMenuBar.isPaintingVolumeDistribution();
	}

	public boolean isPaintingCenterOfMass() {
		return mMenuBar.isPaintingCenterOfMass();
	}

	public boolean isPaintingSlidingInfo() {
		return mMenuBar.isPaintingSlidingInfo();
	}

	public boolean isPaintingSlidingCrossSection() {
		return mMenuBar.isPaintingSlidingCrossSection();
	}

	public boolean isPaintingFins() {
		return mMenuBar.isPaintingFins();
	}

	public boolean isPaintingBackgroundImage() {
		return mMenuBar.isPaintingBackgroundImage();
	}

	public boolean isAntialiasing() {
		return mMenuBar.isAntialiasing();
	}

	public boolean isPaintingBaseLine() {
		return mMenuBar.isPaintingBaseLine();
	}

	public boolean isPaintingCenterLine() {
		return mMenuBar.isPaintingCenterLine();
	}

	public boolean isPaintingOverCurveMeasurements() {
		return mMenuBar.isPaintingOverCurveMeasurements();
	}

	public boolean isPaintingMomentOfInertia() {
		return mMenuBar.isPaintingMomentOfInertia();
	}

	public boolean isPaintingCrossectionsPositions() {
		return mMenuBar.isPaintingCrossectionsPositions();
	}

	public boolean isPaintingFlowlines() {
		return mMenuBar.isPaintingFlowlines();
	}

	public boolean isPaintingApexline() {
		return mMenuBar.isPaintingApexline();
	}

	public boolean isPaintingTuckUnderLine() {
		return mMenuBar.isPaintingTuckUnderLine();
	}

	public boolean isPaintingFootMarks() {
		return mMenuBar.isPaintingFootMarks();
	}

	public boolean useFill() {
		return mMenuBar.useFill();
	}

	public boolean isGhostMode() {
		return mGhostMode;
	}

	public boolean isOrgFocus() {
		return mOrgFocus;
	}

	public AbstractBezierBoardSurfaceModel.ModelType getCrossSectionInterpolationType() {
		return mMenuBar.getCrossSectionInterpolationType();
	}

	public int getCrossSectionInterpolationTypeAsInt() {
		return mMenuBar.getCrossSectionInterpolationTypeAsInt();
	}

	public void setCrossSectionInterpolationType(final AbstractBezierBoardSurfaceModel.ModelType type) {
		mMenuBar.setCrossSectionInterpolationType(type);
	}

	public void setCrossSectionInterpolationTypeFromInt(int type) {
		mMenuBar.setCrossSectionInterpolationTypeFromInt(type);
	}

	public BezierBoard getCurrentBrd() {
		return mCurrentBrd;
	}

	public BezierBoard getOriginalBrd() {
		return mOriginalBrd;
	}
	
	public void setOriginalBrd(BezierBoard board) {
		mOriginalBrd = board;
	}

	public BezierBoard getGhostBrd() {
		return mGhostBrd;
	}

	public void redraw() {
		mOutlineEdit.repaint();
		mBottomAndDeckEdit.repaint();
		mQuadViewOutlineEdit.repaint();
		mQuadViewCrossSectionEdit.repaint();
		mQuadViewRockerEdit.repaint();
	}

	BezierBoard getFocusedBoard() {
		if (isGhostMode()) {
			return BoardCAD.getInstance().getGhostBrd();
		} else if (mOrgFocus) {
			return BoardCAD.getInstance().getOriginalBrd();
		} else {
			return BoardCAD.getInstance().getCurrentBrd();
		}

	}
	
	public void showGuidePointsDialog() {
		mGuidePointsDialog.setVisible(true);
		mFrame.repaint();
	}

	public void showWeightCalculatorDialog() {
		mWeightCalculatorDialog.setDefaults();
		mWeightCalculatorDialog.updateAll();
		mWeightCalculatorDialog.setVisible(true);
	}

	public void fitAll() {
		mOutlineEdit.fitAll();
		mBottomAndDeckEdit.fitAll();
		mCrossSectionEdit.fitAll();

		mQuadViewOutlineEdit.fitAll();
		mQuadViewCrossSectionEdit.fitAll();
		mQuadViewRockerEdit.fitAll();
		
		mToolBar.setLifeSize(false);
	}

	public void onBrdChanged() {
		updateScreenValues();

		setBoardChanged(true);
		setBoardChangedFor3D();
		
		boolean selected = mMenuBar.isShowBesizer3DModelSelected();
		boolean autoUpdate = mMenuBar.isAutoUpdate3DModelSelected();
		
		if (!selected || !autoUpdate) return;
		
		if (mBezier3DModelUpdateTimer != null) {
			mBezier3DModelUpdateTimer.cancel();
			mBezier3DModelUpdateTimer.purge();
			mBezier3DModelUpdateTimer = null;
		}

		mBezier3DModelUpdateTimer = new Timer("Bezier3DModelUpdateTimer");
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				mBezier3DModelUpdateTimer = null;
				updateBezier3DModel();
			}
		};
		mBezier3DModelUpdateTimer.schedule(task, 300);
	}

	public void updateScreenValues() {
		if (getCurrentBrd().isEmpty()) {
			return;
		}

		final double length = getCurrentBrd().getLength();

		final double maxWidth = getCurrentBrd().getMaxWidth();

		mFrame.setTitle(appname + " - " + getCurrentBrd().getFilename() + "  "
				+ UnitUtils.convertLengthToCurrentUnit(length, true) + " x "
				+ UnitUtils.convertLengthToCurrentUnit(maxWidth, false));

		mBoardSpec.updateInfo();

		if (mWeightCalculatorDialog.isVisible())
			mWeightCalculatorDialog.updateAll();

		if (mGuidePointsDialog.isVisible())
			mGuidePointsDialog.update();

	}

	void setBoardChangedFor3D() {
		mRendered3DView.setBoardChangedFor3D();
		mQuad3DView.setBoardChangedFor3D();
	}

	protected void setCurrentUnit(int unitType) {
		UnitUtils.setCurrentUnit(unitType);
		if (mWeightCalculatorDialog != null)
			mWeightCalculatorDialog.updateAll();
		if (mGuidePointsDialog != null)
			mGuidePointsDialog.update();
		updateScreenValues();
		onControlPointChanged();
		redraw();
	}

	public void onControlPointChanged() {
		BrdCommand cmd = getCurrentCommand();
		if(cmd != null) {
			final String className = cmd.getClass().getSimpleName();
	
			if (className.compareTo("BrdEditCommand") == 0) {
				final BoardEdit edit = getSelectedEdit();
	
				if ((edit != null) && edit.getSelectedControlPoints().size() == 1) {
					final BrdEditCommand editCmd = (BrdEditCommand)cmd;
					mControlPointInfo.mCmd = editCmd;
					mControlPointInfo.setEnabled(true);
					final ArrayList<BezierKnot> controlPoints = edit.getSelectedControlPoints();
					final BezierKnot controlPoint = controlPoints.get(0);
					mControlPointInfo.setControlPoint(controlPoint);
					mControlPointInfo.setWhich(editCmd.getWhich());
				} else {
					mControlPointInfo.setEnabled(false);
				}
			}
		}
	}

	public void onSettingsChanged(Setting setting) {
		if (mControlPointInfo != null) {
			mControlPointInfo.setColors();
			mFrame.repaint();
		}

		if (setting.key() == BoardCADSettings.RENDERBACKGROUNDCOLOR) {
			if (mRendered3DView != null)
				mRendered3DView.setBackgroundColor(mSettings.getRenderBackgroundColor());
			if (mQuad3DView != null)
				mQuad3DView.setBackgroundColor(mSettings.getRenderBackgroundColor());
		}
		
		if(setting.key().startsWith("3d")) {
			if (mRendered3DView != null)
				mRendered3DView.updateAppearance();
			if (mQuad3DView != null)
				mQuad3DView.updateAppearance();			
		}
	}

	public void saveAs(String filename) {

		final String ext = FileTools.getExtension(filename);
		BrdWriter.saveFile(getCurrentBrd(), filename);

		mMenuBar.addRecentBoardFile(getCurrentBrd().getFilename());

		onBrdChanged();
		setBoardChanged(false);
	}

	public int saveChangedBoard() {
		if (isBoardChanged() == true) {
			final Object[] options = { LanguageResource.getString("YESBUTTON_STR"),
					LanguageResource.getString("NOBUTTON_STR"), LanguageResource.getString("CANCELBUTTON_STR") };
			final int n = JOptionPane.showOptionDialog(mFrame, LanguageResource.getString("SAVECURRENTBOARDMSG_STR"),
					LanguageResource.getString("SAVECURRENTBOARDTITLE_STR"), JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

			switch (n) {
			case 0:
				new BoardSaveAsAction().actionPerformed(null);
				return n;
			case -1:
				return n; // break out by close button
			case 1:
				return n;
			case 2:
				return n; // break out
			default:
				return n;

			}
		}
		return 0;
	}

	/**
	 *
	 * Creates and shows the GUI. This method should be
	 *
	 * invoked on the event-dispatching thread.
	 *
	 */

	@Override
	public void run() {
		createAndShowGUI();
	}

	/**
	 *
	 * Brings up a window that contains a ClickMe component.
	 *
	 * For thread safety, this method should be invoked from
	 *
	 * the event-dispatching thread.
	 *
	 */

	private void createAndShowGUI() {

		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		mFrame = new JFrame(" " + appname);
		mFrame.setMinimumSize(new Dimension(1000, 700));

		mFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		mFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				int r = saveChangedBoard();
				if (r == -1 || r == 2) // closed dialog or cancel button pressed
					return;
				putPreferences();
				System.exit(1);
			}
		});

		// Set up the layout manager.
		mFrame.getContentPane().setLayout(new BorderLayout());

		// Insert Icon on JFrame
		try {
			ImageIcon icon = new ImageIcon(getClass().getResource("/boardcad/icons/BoardCAD icon 32x32.png"));
			mFrame.setIconImage(icon.getImage());
		} catch (Exception e) {
			System.out.println("Jframe Icon error:\n" + e.getMessage());
		}

		mMenuBar = new MenuBar();
		mPopupMenu = new PopupMenu();
		
		mFrame.setJMenuBar(mMenuBar);

		mToolBar = new ToolBar();
		mFrame.getContentPane().add(mToolBar, BorderLayout.NORTH);
		

		mTabbedPane = new JTabbedPane();

		mQuadView = new QuadView();

		mQuadViewOutlineEdit = new BoardEdit() {
			static final long serialVersionUID = 1L;
			{
				setPreferredSize(new Dimension(300, 200));
				mDrawControl = BezierBoardDrawUtil.MirrorY;
			}

			@Override
			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				return new BezierSpline[] { brd.getOutline() };
			}

			@Override
			public ArrayList<Point2D.Double> getGuidePoints() {
				return BoardCAD.getInstance().getCurrentBrd().getOutlineGuidePoints();
			}

			@Override
			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke, final BezierBoard brd,
					boolean fill) {
				super.drawPart(g, color, stroke, brd, fill);
				if (isPaintingCenterLine()) {
					drawCenterLine(g, mSettings.getCenterLineColor(), stroke, brd.getLength() / 2.0,
							brd.getCenterWidth() * 1.1);
				}
				if (isPaintingCrossectionsPositions())
					drawOutlineCrossections(this, g, color, stroke, brd);
				if (isPaintingFlowlines())
					drawOutlineFlowlines(this, g, mSettings.getFlowLinesColor(), stroke, brd);
				if (isPaintingTuckUnderLine())
					drawOutlineTuckUnderLine(this, g, mSettings.getTuckUnderLineColor(), stroke, brd);
				if (isPaintingFootMarks() && (brd == getCurrentBrd() || (brd == getGhostBrd() && isGhostMode())
						|| (brd == getOriginalBrd() && isOrgFocus())))
					drawOutlineFootMarks(this, g, new BasicStroke(2.0f / (float) this.mScale), brd);
				drawStringer(g, mSettings.getStringerColor(), stroke, brd);
				if (isPaintingFins()) {
					drawFins(g, mSettings.getFinsColor(), stroke, brd);
				}
			}

			@Override
			public void drawSlidingInfo(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd) {
				drawOutlineSlidingInfo(this, g, color, stroke, brd);
			}

			@Override
			public void onBrdChanged() {
				getCurrentBrd().onOutlineChanged();

				super.onBrdChanged();
				mQuadViewCrossSectionEdit.repaint();
				mQuadViewRockerEdit.repaint();
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				super.mousePressed(e);

				if (mSelectedControlPoints.size() == 0) {
					final Point pos = e.getPoint();
					final Point2D.Double brdPos = screenCoordinateToBrdCoordinate(pos);
					final int index = getCurrentBrd().getNearestCrossSectionIndex(brdPos.x);
					double tolerance = 5.0;
					if (index != -1 && Math
							.abs(getCurrentBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance) {
						getCurrentBrd().setCurrentCrossSection(index);
					}
					if (getOriginalBrd() != null) {
						final int indexOriginal = getOriginalBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexOriginal != -1
								&& Math.abs(getOriginalBrd().getCrossSections().get(indexOriginal).getPosition()
										- brdPos.x) < tolerance) {
							getOriginalBrd().setCurrentCrossSection(indexOriginal);
						}
					}
					if (getGhostBrd() != null) {
						final int indexGhost = getGhostBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexGhost != -1 && Math.abs(getGhostBrd().getCrossSections().get(indexGhost).getPosition()
								- brdPos.x) < tolerance) {
							getGhostBrd().setCurrentCrossSection(indexGhost);
						}
					}
					mQuadViewCrossSectionEdit.repaint();
				}

			}

			@Override
			public void mouseMoved(final MouseEvent e) {

				super.mouseMoved(e);
				mQuadViewCrossSectionEdit.repaint();
			}

		};
		mQuadViewOutlineEdit.add(mPopupMenu);

		mQuadViewCrossSectionEdit = new BoardEdit() {

			static final long serialVersionUID = 1L;

			{
				mIsCrossSectionEdit = true;
				setPreferredSize(new Dimension(300, 200));
				mDrawControl = BezierBoardDrawUtil.MirrorX | BezierBoardDrawUtil.FlipY;
				mCurvatureScale = 25;
			}

			@Override
			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				final BezierBoardCrossSection currentCrossSection = brd.getCurrentCrossSection();
				if (currentCrossSection == null)
					return null;

				return new BezierSpline[] { brd.getCurrentCrossSection().getBezierSpline() };
			}

			@Override
			public ArrayList<Point2D.Double> getGuidePoints() {
				final BezierBoardCrossSection currentCrossSection = BoardCAD.getInstance().getCurrentBrd()
						.getCurrentCrossSection();
				if (currentCrossSection == null)
					return null;

				return currentCrossSection.getGuidePoints();
			}

			@Override
			protected boolean isPaintingVolumeDistribution() {
				return false;
			}

			@Override
			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke, final BezierBoard brd,
					boolean fill) {

				if (brd.isEmpty())
					return;

				if (isPaintingNonActiveCrossSections()) {
					final ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();

					final BasicStroke bs = (BasicStroke) stroke;

					final float[] dashPattern = new float[] { 0.8f, 0.2f };
					final BasicStroke stapled = new BasicStroke((float) (bs.getLineWidth() / 2.0), bs.getEndCap(),
							bs.getLineJoin(), bs.getMiterLimit(), dashPattern, 0f);
					final Color noneActiveColor = color.brighter();

					double currentCrossSectionRocker = brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition());

					JavaDraw d = new JavaDraw(g);
					for (int i = 0; i < crossSections.size(); i++) {
						if (crossSections.get(i) != brd.getCurrentCrossSection()) {

							double rockerOffset = 0;
							if (mSettings.isUsingOffsetInterpolation()) {
								rockerOffset = brd.getRockerAtPos(crossSections.get(i).getPosition())
										- currentCrossSectionRocker;
								rockerOffset *= this.mScale;
							}

							BezierBoardDrawUtil.paintBezierSpline(d, mOffsetX, mOffsetY - rockerOffset, mScale, 0.0,
									noneActiveColor, stapled, crossSections.get(i).getBezierSpline(), mDrawControl,
									fill);
						}

					}

				}

				if (isPaintingSlidingCrossSection()) {

					final Color col = (isGhostMode()) ? color : Color.GRAY;

					double pos = mQuadViewRockerEdit.hasMouse() ? mQuadViewRockerEdit.mBrdCoord.x
							: mQuadViewOutlineEdit.mBrdCoord.x;

					double rockerOffset = 0;
					if (mSettings.isUsingOffsetInterpolation()) {
						double currentCrossSectionRocker = brd
								.getRockerAtPos(brd.getCurrentCrossSection().getPosition());
						rockerOffset = brd.getRockerAtPos(pos) - currentCrossSectionRocker;
						rockerOffset *= this.mScale;
					}

					// DEBUG System.out.printf("rockerOffset: %f\n",
					// rockerOffset);

					BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g), mOffsetX, mOffsetY - rockerOffset,
							mScale, 0.0, col, stroke, (mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
							(mDrawControl & BezierBoardDrawUtil.FlipY) != 0, pos, brd);

					if (isGhostMode()) {
						if (mSettings.isUsingOffsetInterpolation()) {
							double currentCrossSectionRocker = getCurrentBrd()
									.getRockerAtPos(getCurrentBrd().getCurrentCrossSection().getPosition());
							rockerOffset = getCurrentBrd().getRockerAtPos(pos) - currentCrossSectionRocker;
							rockerOffset *= this.mScale;
						}
						BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g), mOffsetX, mOffsetY - rockerOffset,
								mScale, 0.0, mSettings.getGhostBrdColor(), stroke,
								(mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
								(mDrawControl & BezierBoardDrawUtil.FlipY) != 0, pos, getCurrentBrd());
					}

				}

				super.drawPart(g, color, stroke, brd, fill);

				if (isPaintingCenterLine())
					drawCrossSectionCenterline(this, g, mSettings.getCenterLineColor(), stroke, brd);
				if (isPaintingTuckUnderLine())
					drawCrossSectionTuckUnderLine(this, g, mSettings.getTuckUnderLineColor(), stroke, brd);
				if (isPaintingFlowlines())
					drawCrossSectionFlowlines(this, g, mSettings.getFlowLinesColor(), stroke, brd);
				if (isPaintingApexline())
					drawCrossSectionApexline(this, g, mSettings.getApexLineColor(), stroke, brd);

			}

			@Override
			public void drawBrdCoordinate(Graphics2D g) {
				super.drawBrdCoordinate(g);

				BezierBoard brd = getCurrentBrd();
				if (brd.isEmpty())
					return;

				BezierBoardCrossSection crs = brd.getCurrentCrossSection();
				if (crs == null)
					return;

				g.setColor(Color.BLACK);

				// get metrics from the graphics
				FontMetrics metrics = g.getFontMetrics(mBrdCoordFont);

				// get the height of a line of text in this font and render
				// context
				int hgt = metrics.getHeight();

				String posStr = LanguageResource.getString("CROSSECTIONPOS_STR") + UnitUtils.convertLengthToCurrentUnit(
						mBoardSpec.isOverCurveSelected() ? brd.getBottom().getLengthByX(crs.getPosition())
								: crs.getPosition(),
						false) + (mBoardSpec.isOverCurveSelected() ? " O.C" : "");

				g.drawString(posStr, 10, hgt * 3);

				// get the height of a line of text in this font and render
				// context

				String widthStr = LanguageResource.getString("CROSSECTIONWIDTH_STR")
						+ UnitUtils.convertLengthToCurrentUnit(crs.getWidth(), false);

				g.drawString(widthStr, 10, hgt * 4);

				final Dimension dim = getSize();

				String releaseAngleStr = LanguageResource.getString("RELEASEANGLE_STR")
						+ String.format("%1$.1f degrees", crs.getReleaseAngle() / MathUtils.DEG_TO_RAD);

				final int releaseAngleStrLength = metrics.stringWidth(releaseAngleStr);

				g.drawString(releaseAngleStr, dim.width - releaseAngleStrLength - 10, hgt * 1);

				String tuckUnderRadiusStr = LanguageResource.getString("TUCKRADIUS_STR")
						+ UnitUtils.convertLengthToCurrentUnit(crs.getTuckRadius(), false);

				final int tuckUnderRadiusStrLength = metrics.stringWidth(tuckUnderRadiusStr);

				g.drawString(tuckUnderRadiusStr, dim.width - tuckUnderRadiusStrLength - 10, hgt * 2);

			}

			@Override
			public void drawSlidingInfo(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd) {

				if (brd.getCrossSections().size() == 0)
					return;

				this.setName("QuadViewCrossSection");

				if (brd.getCurrentCrossSection() == null)
					return;

				BezierBoardCrossSection crs = brd.getCurrentCrossSection();
				final double thickness = crs.getThicknessAtPos(Math.abs(mBrdCoord.x));

				if (thickness <= 0)
					return;

				final double bottom = crs.getBottomAtPos(Math.abs(mBrdCoord.x));
				final double centerThickness = crs.getThicknessAtPos(BezierSpline.ZERO);

				final double mulX = (mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
				final double mulY = (mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;

				// get metrics from the graphics
				final FontMetrics metrics = g.getFontMetrics(mSlidingInfoFont);
				// get the height of a line of text in this font and render
				// context
				final int hgt = metrics.getHeight();

				final Dimension dim = getSize();

				String thicknessStr = LanguageResource.getString("CROSSECTIONSLIDINGINFOTHICKNESS_STR");
				mSlidingInfoString = thicknessStr + UnitUtils.convertLengthToCurrentUnit(thickness, false)
						+ String.format("(%02d%%)", (int) ((thickness * 100) / centerThickness));

				g.setColor(Color.BLUE);

				// get the advance of my text in this font and render context
				final int adv = metrics.stringWidth(mSlidingInfoString);

				// calculate the size of a box to hold the text with some
				// padding.
				final Dimension size = new Dimension(adv, hgt + 1);

				// get the advance of my text in this font and render context
				final int advOfThicknessStr = metrics.stringWidth(thicknessStr);

				// calculate the size of a box to hold the text with some
				// padding.
				final Dimension sizeOfThicknessStr = new Dimension(advOfThicknessStr, hgt + 1);

				int textX = mScreenCoord.x - (sizeOfThicknessStr.width);
				if (textX < 10)
					textX = 10;

				if (textX + size.width + 10 > dim.width)
					textX = dim.width - size.width - 10;

				g.setStroke(new BasicStroke((float) (1.0 / mScale)));
				g.drawString(mSlidingInfoString, textX, dim.height - (size.height * 2 + 5));

				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOBOTTOM_STR")
						+ UnitUtils.convertLengthToCurrentUnit(bottom, false);

				g.setColor(Color.RED);

				g.drawString(mSlidingInfoString, textX, dim.height - size.height);

				g.setColor(Color.BLACK);

				final double fromCenter = Math.abs(mBrdCoord.x);

				final double fromRail = crs.getWidth() / 2 - Math.abs(mBrdCoord.x);

				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOFROMRAIL_STR")
						+ UnitUtils.convertLengthToCurrentUnit(fromRail, false);

				g.drawString(mSlidingInfoString, textX, dim.height - (size.height + 2) * 4);

				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOFROMCENTER_STR")
						+ UnitUtils.convertLengthToCurrentUnit(fromCenter, false);

				g.drawString(mSlidingInfoString, textX, dim.height - (size.height + 2) * 3);

				// sets the color of the +ve sliding info (above Y base line)
				g.setColor(Color.BLUE);

				final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), mOffsetX,
						mOffsetY, mScale, 0.0);

				mSlidingInfoLine.setLine(mBrdCoord.x * mulX, bottom * mulY, mBrdCoord.x * mulX,
						(bottom + thickness) * mulY);
				g.draw(mSlidingInfoLine);

				// sets the color of the Bottom sliding info (-ve# when concaved
				// +ve# when Vee)
				g.setColor(Color.RED);

				mSlidingInfoLine.setLine(mBrdCoord.x * mulX, 0 * mulY, mBrdCoord.x * mulX, bottom * mulY);
				g.draw(mSlidingInfoLine);

				g.setTransform(savedTransform);

			}

			@Override
			public void fitBrd() {
				final BezierBoard brd = getCurrentBrd();
				final Dimension dim = getSize();

				double width = brd.getCenterWidth();

				mScale = (dim.width - ((BORDER * dim.width / 100) * 2)) / width;

				mOffsetX = dim.width * 1 / 2;
				mOffsetY = dim.height * 1 / 2 + (brd.getThicknessAtPos(brd.getLength() / 2.0f) * mScale);
				// mOffsetY=board_handler.get_edge_offset()/10*mScale+2*dim.height/3;

				mLastWidth = dim.width;
			}

			@Override
			public void onBrdChanged() {
				adjustFoilFromCrossSection(this);
				
				getCurrentBrd().onCrossSectionChanged();

				super.onBrdChanged();

				mQuadViewOutlineEdit.repaint();
				mQuadViewRockerEdit.repaint();
			}

			@Override
			Point2D.Double getTailPos() {
				final BezierBoard brd = getCurrentBrd();
				final Point2D.Double tail = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(0)
						.getEndPoint().clone();

				return tail;
			}

			@Override
			Point2D.Double getNosePos() {
				final BezierBoard brd = getCurrentBrd();
				final Point2D.Double tail = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(0)
						.getEndPoint().clone();
				final Point2D.Double nose = (Point2D.Double) getActiveBezierSplines(brd)[0]
						.getControlPoint(getActiveBezierSplines(brd)[0].getNrOfControlPoints() - 1).getEndPoint()
						.clone();
				nose.y = tail.y;
				nose.x = getActiveBezierSplines(brd)[0].getMaxX();
				return nose;
			}

			@Override
			public void repaint() {
				super.repaint();
				if (mCrossSectionOutlineEdit != null)
					mCrossSectionOutlineEdit.repaint();
			}

		};
		mQuadViewCrossSectionEdit.add(mPopupMenu);

		mQuadViewRockerEdit = new BoardEdit() {
			static final long serialVersionUID = 1L;

			{
				setPreferredSize(new Dimension(300, 200));
				mDrawControl = BezierBoardDrawUtil.FlipY;
				mCurvatureScale = 1000;
			}

			@Override
			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				switch (mEditDeckOrBottom) {
				case DECK:
					return new BezierSpline[] { brd.getDeck() };
				case BOTTOM:
					return new BezierSpline[] { brd.getBottom() };
				case BOTH:
				default:
					return new BezierSpline[] { brd.getDeck(), brd.getBottom() };
				}
			}

			@Override
			public ArrayList<Point2D.Double> getGuidePoints() {
				switch (mEditDeckOrBottom) {
				case DECK:
					return BoardCAD.getInstance().getCurrentBrd().getDeckGuidePoints();
				case BOTTOM:
					return BoardCAD.getInstance().getCurrentBrd().getBottomGuidePoints();
				case BOTH:
				default: {
					ArrayList<Point2D.Double> list = new ArrayList<Point2D.Double>();
					list.addAll(BoardCAD.getInstance().getCurrentBrd().getDeckGuidePoints());
					list.addAll(BoardCAD.getInstance().getCurrentBrd().getBottomGuidePoints());
					return list;
				}
				}

			}

			@Override
			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke, final BezierBoard brd,
					boolean fill) {
				if (isPaintingBaseLine()) {
					drawStringer(g, mSettings.getBaseLineColor(),
							new BasicStroke((float) (mSettings.getBaseLineThickness() / mScale)), brd);
				}
				if (isPaintingFootMarks() && (brd == getCurrentBrd() || (brd == getGhostBrd() && isGhostMode())
						|| (brd == getOriginalBrd() && isOrgFocus())))
					drawProfileFootMarks(this, g, new BasicStroke(2.0f / (float) this.mScale), brd);
				if (isPaintingBaseLine()) {
					drawStringer(g, mSettings.getBaseLineColor(),
							new BasicStroke((float) (mSettings.getBaseLineThickness() / mScale)), brd);
				}
				if (isPaintingCenterLine()) {
					drawCenterLine(g, mSettings.getCenterLineColor(), stroke, brd.getLength() / 2.0,
							brd.getThickness() * 2.2);
				}

				BezierBoardDrawUtil.paintBezierSplines(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0, color, stroke,
						new BezierSpline[] { brd.getBottom(), brd.getDeck() }, mDrawControl, fill);

				super.drawPart(g, color, stroke, brd, false);

				if (isPaintingFlowlines())
					drawProfileFlowlines(this, g, mSettings.getFlowLinesColor(), stroke, brd);
				if (isPaintingApexline())
					drawProfileApexline(this, g, mSettings.getApexLineColor(), stroke, brd);
				if (isPaintingTuckUnderLine())
					drawProfileTuckUnderLine(this, g, mSettings.getTuckUnderLineColor(), stroke, brd);
			}

			@Override
			public void drawSlidingInfo(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd) {
				drawProfileSlidingInfo(this, g, color, stroke, brd);
			}

			@Override
			public void onBrdChanged() {
				getCurrentBrd().onRockerChanged();

				super.onBrdChanged();
				mQuadViewCrossSectionEdit.repaint();
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				super.mousePressed(e);

				if (mSelectedControlPoints.size() == 0) {
					final Point pos = e.getPoint();
					final Point2D.Double brdPos = screenCoordinateToBrdCoordinate(pos);
					final int index = getCurrentBrd().getNearestCrossSectionIndex(brdPos.x);
					double tolerance = 5.0;
					if (index != -1 && Math
							.abs(getCurrentBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance) {
						getCurrentBrd().setCurrentCrossSection(index);
					}
					if (getOriginalBrd() != null) {
						final int indexOriginal = getOriginalBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexOriginal != -1 && Math.abs(
								getOriginalBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance) {
							getOriginalBrd().setCurrentCrossSection(indexOriginal);
						}
					}
					if (getGhostBrd() != null) {
						final int indexOriginal = getGhostBrd().getNearestCrossSectionIndex(brdPos.x);
						if (indexOriginal != -1 && Math.abs(
								getGhostBrd().getCrossSections().get(index).getPosition() - brdPos.x) < tolerance) {
							getGhostBrd().setCurrentCrossSection(indexOriginal);
						}
					}
					mQuadViewCrossSectionEdit.repaint();
				}

			}

			@Override
			public void mouseMoved(final MouseEvent e) {

				super.mouseMoved(e);
				mQuadViewCrossSectionEdit.repaint();
				mQuadViewOutlineEdit.repaint();
			}

		};
		mQuadViewRockerEdit.add(mPopupMenu);
		mQuadViewRockerEdit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "TOGGLE_DECKBOTTOM");
		ToggleDeckAndBottomAction toggleDeckAndBottom = new ToggleDeckAndBottomAction();
		mQuadViewRockerEdit.getActionMap().put("TOGGLE_DECKBOTTOM", toggleDeckAndBottom);

		mRendered3DView = new ThreeDView();
		mQuad3DView = new ThreeDView();
		mRendered3DView.setBackgroundColor(mSettings.getRenderBackgroundColor());
		mQuad3DView.setBackgroundColor(mSettings.getRenderBackgroundColor());

		mStatusPanel = new StatusPanel();

		mQuadViewOutlineEdit.setParentContainer(mQuadView);
		mQuadViewCrossSectionEdit.setParentContainer(mQuadView);
		mQuadViewRockerEdit.setParentContainer(mQuadView);
		
		mQuadView.add(mQuadViewOutlineEdit);
		mQuadView.add(mQuadViewCrossSectionEdit);
		mQuadView.add(mQuadViewRockerEdit);
		mQuadView.add(mQuad3DView);
		mTabbedPane.add(LanguageResource.getString("QUADVIEW_STR"), mQuadView);

		mOutlineEdit = new BoardEdit() {
			static final long serialVersionUID = 1L;
			{
				mDrawControl = BezierBoardDrawUtil.MirrorY;
			}

			@Override
			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				return new BezierSpline[] { brd.getOutline() };
			}

			@Override
			public ArrayList<Point2D.Double> getGuidePoints() {
				return BoardCAD.getInstance().getCurrentBrd().getOutlineGuidePoints();
			}

			@Override
			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke, final BezierBoard brd,
					boolean fill) {
				super.drawPart(g, color, stroke, brd, fill);
				if (isPaintingFlowlines())
					drawOutlineFlowlines(this, g, mSettings.getFlowLinesColor(), stroke, brd);
				if (isPaintingTuckUnderLine())
					drawOutlineTuckUnderLine(this, g, mSettings.getTuckUnderLineColor(), stroke, brd);
				if (isPaintingFootMarks() && (brd == getCurrentBrd() || (brd == getGhostBrd() && isGhostMode())
						|| (brd == getOriginalBrd() && isOrgFocus())))
					drawOutlineFootMarks(this, g, new BasicStroke(2.0f / (float) this.mScale), brd);
				if (isPaintingCenterLine()) {
					drawCenterLine(g, mSettings.getCenterLineColor(), stroke, brd.getLength() / 2.0,
							brd.getCenterWidth() * 1.1);
				}
				drawStringer(g, mSettings.getStringerColor(), stroke, brd);
				if (isPaintingFins()) {
					drawFins(g, mSettings.getFinsColor(), stroke, brd);
				}
			}

			@Override
			public void drawSlidingInfo(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd) {
				drawOutlineSlidingInfo(this, g, color, stroke, brd);
			}

			@Override
			public void onBrdChanged() {
				getCurrentBrd().onOutlineChanged();

				super.onBrdChanged();
			}

		};
		mOutlineEdit.add(mPopupMenu);
		mTabbedPane.add(LanguageResource.getString("OUTLINEEDIT_STR"), mOutlineEdit);

		mBottomAndDeckEdit = new BoardEdit() {
			static final long serialVersionUID = 1L;

			{
				setPreferredSize(new Dimension(400, 150));
				mDrawControl = BezierBoardDrawUtil.FlipY;
				mCurvatureScale = 1000;
			}

			@Override
			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				switch (mEditDeckOrBottom) {
				case DECK:
					return new BezierSpline[] { brd.getDeck() };
				case BOTTOM:
					return new BezierSpline[] { brd.getBottom() };
				case BOTH:
				default:
					return new BezierSpline[] { brd.getDeck(), brd.getBottom() };
				}
			}

			@Override
			public ArrayList<Point2D.Double> getGuidePoints() {
				switch (mEditDeckOrBottom) {
				case DECK:
					return BoardCAD.getInstance().getCurrentBrd().getDeckGuidePoints();
				case BOTTOM:
					return BoardCAD.getInstance().getCurrentBrd().getBottomGuidePoints();
				case BOTH:
				default: {
					ArrayList<Point2D.Double> list = new ArrayList<Point2D.Double>();
					list.addAll(BoardCAD.getInstance().getCurrentBrd().getDeckGuidePoints());
					list.addAll(BoardCAD.getInstance().getCurrentBrd().getBottomGuidePoints());
					return list;
				}
				}

			}

			@Override
			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke, final BezierBoard brd,
					boolean fill) {
				if (isPaintingFootMarks() && (brd == getCurrentBrd() || (brd == getGhostBrd() && isGhostMode())
						|| (brd == getOriginalBrd() && isOrgFocus())))
					drawProfileFootMarks(this, g, new BasicStroke(2.0f / (float) this.mScale), brd);
				if (isPaintingBaseLine()) {
					drawStringer(g, mSettings.getBaseLineColor(),
							new BasicStroke((float) (mSettings.getBaseLineThickness() / mScale)), brd);
				}
				if (isPaintingCenterLine()) {
					drawCenterLine(g, mSettings.getCenterLineColor(), stroke, brd.getLength() / 2.0,
							brd.getThickness() * 2.2);
				}

				BezierBoardDrawUtil.paintBezierSplines(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0, color, stroke,
						new BezierSpline[] { brd.getBottom(), brd.getDeck() }, mDrawControl, fill);

				super.drawPart(g, color, stroke, brd, false);

				if (isPaintingFlowlines())
					drawProfileFlowlines(this, g, mSettings.getFlowLinesColor(), stroke, brd);
				if (isPaintingApexline())
					drawProfileApexline(this, g, mSettings.getApexLineColor(), stroke, brd);
				if (isPaintingTuckUnderLine())
					drawProfileTuckUnderLine(this, g, mSettings.getTuckUnderLineColor(), stroke, brd);
			}

			@Override
			public void drawSlidingInfo(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd) {
				drawProfileSlidingInfo(this, g, color, stroke, brd);
			}

			@Override
			public void onBrdChanged() {
				getCurrentBrd().onRockerChanged();

				super.onBrdChanged();
			}

		};
		mBottomAndDeckEdit.add(mPopupMenu);
		mBottomAndDeckEdit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "TOGGLE_DECKBOTTOM");
		mBottomAndDeckEdit.getActionMap().put("TOGGLE_DECKBOTTOM", toggleDeckAndBottom);


		mTabbedPane.add(LanguageResource.getString("BOTTOMANDDECKEDIT_STR"), mBottomAndDeckEdit);

		/*
		 * mOutlineAndProfileSplitPane = new BrdEditSplitPane(
		 * JSplitPane.VERTICAL_SPLIT, mOutlineEdit2, mBottomAndDeckEdit);
		 * mOutlineAndProfileSplitPane.setOneTouchExpandable(true);
		 * mOutlineAndProfileSplitPane.setResizeWeight(0.7);
		 *
		 * mTabbedPane.add(LanguageResource.getString("OUTLINEPROFILEEDIT_STR"),
		 * mOutlineAndProfileSplitPane);
		 */
		
		mCrossSectionEdit = new BoardEdit() {
			static final long serialVersionUID = 1L;

			{
				mIsCrossSectionEdit = true;
				setPreferredSize(new Dimension(400, 200));
				mDrawControl = BezierBoardDrawUtil.MirrorX | BezierBoardDrawUtil.FlipY;
				mCurvatureScale = 25;
			}

			@Override
			public BezierSpline[] getActiveBezierSplines(final BezierBoard brd) {
				final BezierBoardCrossSection currentCrossSection = brd.getCurrentCrossSection();
				if (currentCrossSection == null)
					return null;

				return new BezierSpline[] { brd.getCurrentCrossSection().getBezierSpline() };
			}

			@Override
			public ArrayList<Point2D.Double> getGuidePoints() {
				final BezierBoardCrossSection currentCrossSection = BoardCAD.getInstance().getCurrentBrd()
						.getCurrentCrossSection();
				if (currentCrossSection == null)
					return null;

				return currentCrossSection.getGuidePoints();
			}

			@Override
			protected boolean isPaintingVolumeDistribution() {
				return false;
			}

			@Override
			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke, final BezierBoard brd,
					boolean fill) {
				if (brd.isEmpty())
					return;

				if (isPaintingNonActiveCrossSections()) {
					final ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();

					final BasicStroke bs = (BasicStroke) stroke;

					final float[] dashPattern = new float[] { 0.8f, 0.2f };
					final BasicStroke stapled = new BasicStroke((float) (bs.getLineWidth() / 2.0), bs.getEndCap(),
							bs.getLineJoin(), bs.getMiterLimit(), dashPattern, 0f);
					final Color noneActiveColor = color.brighter();

					double currentCrossSectionRocker = brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition());

					for (int i = 0; i < crossSections.size(); i++) {
						if (crossSections.get(i) != brd.getCurrentCrossSection()) {

							double rockerOffset = 0;
							if (mSettings.isUsingOffsetInterpolation()) {
								rockerOffset = brd.getRockerAtPos(crossSections.get(i).getPosition())
										- currentCrossSectionRocker;
								rockerOffset *= this.mScale;
							}

							BezierBoardDrawUtil.paintBezierSpline(new JavaDraw(g), mOffsetX, mOffsetY - rockerOffset,
									mScale, 0.0, noneActiveColor, stapled, crossSections.get(i).getBezierSpline(),
									mDrawControl, fill);
						}

					}
				}

				if (isPaintingSlidingCrossSection()) {

					final Color col = (isGhostMode()) ? color : Color.GRAY;

					double rockerOffset = 0;
					if (mSettings.isUsingOffsetInterpolation()) {
						double currentCrossSectionRocker = brd
								.getRockerAtPos(brd.getCurrentCrossSection().getPosition());
						rockerOffset = brd.getRockerAtPos(mCrossSectionOutlineEdit.mBrdCoord.x)
								- currentCrossSectionRocker;
						rockerOffset *= this.mScale;
					}

					BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g), mOffsetX, mOffsetY - rockerOffset,
							mScale, 0.0, col, stroke, (mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
							(mDrawControl & BezierBoardDrawUtil.FlipY) != 0, mCrossSectionOutlineEdit.mBrdCoord.x, brd);

					if (isGhostMode()) {
						if (mSettings.isUsingOffsetInterpolation()) {
							double currentCrossSectionRocker = getCurrentBrd()
									.getRockerAtPos(getCurrentBrd().getCurrentCrossSection().getPosition());
							rockerOffset = getCurrentBrd().getRockerAtPos(mCrossSectionOutlineEdit.mBrdCoord.x)
									- currentCrossSectionRocker;
							rockerOffset *= this.mScale;
						}
						BezierBoardDrawUtil.paintSlidingCrossSection(new JavaDraw(g), mOffsetX, mOffsetY - rockerOffset,
								0.0, mScale, mSettings.getGhostBrdColor(), stroke,
								(mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
								(mDrawControl & BezierBoardDrawUtil.FlipY) != 0, mCrossSectionOutlineEdit.mBrdCoord.x,
								getCurrentBrd());
					}

				}
				super.drawPart(g, color, stroke, brd, fill);

				if (isPaintingTuckUnderLine())
					drawCrossSectionTuckUnderLine(this, g, mSettings.getTuckUnderLineColor(), stroke, brd);
				if (isPaintingApexline())
					drawCrossSectionApexline(this, g, mSettings.getApexLineColor(), stroke, brd);
				if (isPaintingFlowlines())
					drawCrossSectionFlowlines(this, g, mSettings.getFlowLinesColor(), stroke, brd);
			}

			@Override
			public void drawBrdCoordinate(Graphics2D g) {
				super.drawBrdCoordinate(g);

				BezierBoard brd = getCurrentBrd();
				if (brd.isEmpty())
					return;

				BezierBoardCrossSection crs = brd.getCurrentCrossSection();
				if (crs == null)
					return;

				g.setColor(Color.BLACK);

				// get metrics from the graphics
				FontMetrics metrics = g.getFontMetrics(mBrdCoordFont);

				// get the height of a line of text in this font and render
				// context
				int hgt = metrics.getHeight();

				String posStr = LanguageResource.getString("CROSSECTIONPOS_STR") + UnitUtils.convertLengthToCurrentUnit(
						mBoardSpec.isOverCurveSelected() ? brd.getBottom().getLengthByX(crs.getPosition())
								: crs.getPosition(),
						false) + (mBoardSpec.isOverCurveSelected() ? " O.C" : "");

				g.drawString(posStr, 10, hgt * 3);

				// get the height of a line of text in this font and render
				// context

				String widthStr = LanguageResource.getString("CROSSECTIONWIDTH_STR")
						+ UnitUtils.convertLengthToCurrentUnit(crs.getWidth(), false);

				g.drawString(widthStr, 10, hgt * 4);

				final Dimension dim = getSize();

				String releaseAngleStr = LanguageResource.getString("RELEASEANGLE_STR")
						+ String.format("%1$.1f degrees", crs.getReleaseAngle() / MathUtils.DEG_TO_RAD);

				final int releaseAngleStrLength = metrics.stringWidth(releaseAngleStr);

				g.drawString(releaseAngleStr, dim.width - releaseAngleStrLength - 10, hgt * 1);

				String tuckUnderRadiusStr = LanguageResource.getString("TUCKRADIUS_STR")
						+ UnitUtils.convertLengthToCurrentUnit(crs.getTuckRadius(), false);

				final int tuckUnderRadiusStrLength = metrics.stringWidth(tuckUnderRadiusStr);

				g.drawString(tuckUnderRadiusStr, dim.width - tuckUnderRadiusStrLength - 10, hgt * 2);
			}

			@Override
			public void drawSlidingInfo(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd) {
				if (brd.getCrossSections().size() == 0)
					return;

				if (brd.getCurrentCrossSection() == null)
					return;

				BezierBoardCrossSection crs = brd.getCurrentCrossSection();
				final double thickness = crs.getThicknessAtPos(Math.abs(mBrdCoord.x));

				if (thickness <= 0)
					return;

				final double bottom = crs.getBottomAtPos(Math.abs(mBrdCoord.x));
				final double centerThickness = crs.getThicknessAtPos(BezierSpline.ZERO);

				final double mulX = (mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
				final double mulY = (mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;

				// get metrics from the graphics
				final FontMetrics metrics = g.getFontMetrics(mSlidingInfoFont);
				// get the height of a line of text in this font and render
				// context
				final int hgt = metrics.getHeight();

				final Dimension dim = getSize();

				String thicknessStr = LanguageResource.getString("CROSSECTIONSLIDINGINFOTHICKNESS_STR");
				mSlidingInfoString = thicknessStr + UnitUtils.convertLengthToCurrentUnit(thickness, false)
						+ String.format("(%02d%%)", (int) ((thickness * 100) / centerThickness));

				g.setColor(Color.BLUE);

				// get the advance of my text in this font and render context
				final int adv = metrics.stringWidth(mSlidingInfoString);

				// calculate the size of a box to hold the text with some
				// padding.
				final Dimension size = new Dimension(adv, hgt + 1);

				// get the advance of my text in this font and render context
				final int advOfThicknessStr = metrics.stringWidth(thicknessStr);

				// calculate the size of a box to hold the text with some
				// padding.
				final Dimension sizeOfThicknessStr = new Dimension(advOfThicknessStr, hgt + 1);

				int textX = mScreenCoord.x - (sizeOfThicknessStr.width);
				if (textX < 10)
					textX = 10;

				if (textX + size.width + 10 > dim.width)
					textX = dim.width - size.width - 10;

				g.setStroke(new BasicStroke((float) (1.0 / mScale)));
				g.drawString(mSlidingInfoString, textX, dim.height - (size.height * 2 + 5));

				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOBOTTOM_STR")
						+ UnitUtils.convertLengthToCurrentUnit(bottom, false);
				g.setColor(Color.RED);

				g.drawString(mSlidingInfoString, textX, dim.height - size.height);

				g.setColor(Color.BLACK);

				final double fromCenter = Math.abs(mBrdCoord.x);

				final double fromRail = crs.getWidth() / 2.0 - Math.abs(mBrdCoord.x);

				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOFROMRAIL_STR")
						+ UnitUtils.convertLengthToCurrentUnit(fromRail, false);

				g.drawString(mSlidingInfoString, textX, dim.height - (size.height + 2) * 4);

				mSlidingInfoString = LanguageResource.getString("CROSSECTIONSLIDINGINFOFROMCENTER_STR")
						+ UnitUtils.convertLengthToCurrentUnit(fromCenter, false);

				g.drawString(mSlidingInfoString, textX, dim.height - (size.height + 2) * 3);

				// sets the color of the +ve sliding info (above Y base line)
				g.setColor(Color.BLUE);

				final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), mOffsetX,
						mOffsetY, mScale, 0.0);

				mSlidingInfoLine.setLine(mBrdCoord.x * mulX, bottom * mulY, mBrdCoord.x * mulX,
						(bottom + thickness) * mulY);
				g.draw(mSlidingInfoLine);

				// sets the color of the Bottom sliding info (-ve# when concaved
				// +ve# when Vee)
				g.setColor(Color.RED);

				mSlidingInfoLine.setLine(mBrdCoord.x * mulX, 0 * mulY, mBrdCoord.x * mulX, bottom * mulY);
				g.draw(mSlidingInfoLine);

				g.setTransform(savedTransform);

			}

			@Override
			public void fitBrd() {
				final BezierBoard brd = getCurrentBrd();
				final Dimension dim = getSize();

				double width = brd.getCenterWidth();

				mScale = (dim.width - ((BORDER * dim.width / 100) * 2)) / width;

				mOffsetX = dim.width * 1 / 2;
				mOffsetY = dim.height * 1 / 2 + (brd.getThicknessAtPos(brd.getLength() / 2.0f) * mScale);
				// mOffsetY=board_handler.get_edge_offset()/10*mScale+2*dim.height/3;

				mLastWidth = dim.width;
			}

			@Override
			public void onBrdChanged() {
				adjustFoilFromCrossSection(this);
				
				getCurrentBrd().onCrossSectionChanged();

				super.onBrdChanged();
			}

			@Override
			Point2D.Double getTailPos() {
				final BezierBoard brd = getCurrentBrd();
				final Point2D.Double tail = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(0)
						.getEndPoint().clone();

				return tail;
			}

			@Override
			Point2D.Double getNosePos() {
				final BezierBoard brd = getCurrentBrd();
				final Point2D.Double tail = (Point2D.Double) getActiveBezierSplines(brd)[0].getControlPoint(0)
						.getEndPoint().clone();
				final Point2D.Double nose = (Point2D.Double) getActiveBezierSplines(brd)[0]
						.getControlPoint(getActiveBezierSplines(brd)[0].getNrOfControlPoints() - 1).getEndPoint()
						.clone();
				nose.y = tail.y;
				nose.x = getActiveBezierSplines(brd)[0].getMaxX();
				return nose;
			}

			@Override
			public void repaint() {
				super.repaint();
				if (mCrossSectionOutlineEdit != null)
					mCrossSectionOutlineEdit.repaint();
			}

		};
		mCrossSectionEdit.add(mPopupMenu);

		mCrossSectionOutlineEdit = new BoardEdit() {
			static final long serialVersionUID = 1L;

			static final double fixedHeightBorder = 0;
			{
				setPreferredSize(new Dimension(400, 100));
				mDrawControl = BezierBoardDrawUtil.MirrorY;
			};

			@Override
			public void paintComponent(final Graphics g) {
				fitBrd();
				super.paintComponent(g);
			}

			@Override
			public void fitBrd() {
				super.fitBrd();

				final Dimension dim = getSize();

				final BezierBoard brd = getCurrentBrd();
				final double width = brd.getCenterWidth() + brd.getMaxRocker() * 2;
				if (dim.height - (fixedHeightBorder * 2) < width * mScale) {
					mScale = (dim.height - (fixedHeightBorder * 2)) / width;

					if ((mDrawControl & BezierBoardDrawUtil.FlipX) == 0) {
						mOffsetX = (dim.width - (brd.getLength() * mScale)) / 2;
					} else {
						mOffsetX = (dim.width - (brd.getLength() * mScale)) / 2 + brd.getLength() * mScale;
					}
				}

				mOffsetY -= brd.getMaxRocker() / 2 * mScale;

			}

			@Override
			public void drawPart(final Graphics2D g, final Color color, final Stroke stroke, final BezierBoard brd,
					boolean fill) {

				Color brdColor = mSettings.getBrdColor();
				Color current = BoardCAD.getInstance().isGhostMode() ? mSettings.getGhostBrdColor() : color;
				current = BoardCAD.getInstance().isOrgFocus() ? mSettings.getOriginalBrdColor() : current;

				BezierBoardDrawUtil.paintBezierSpline(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0, current, stroke,
						brd.getOutline(), mDrawControl, fill);

				if (isPaintingFlowlines())
					BezierBoardDrawUtil.paintOutlineFlowLines(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0,
							mSettings.getFlowLinesColor(), stroke, brd, (mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
							true);

				if (isPaintingTuckUnderLine())
					BezierBoardDrawUtil.paintOutlineTuckUnderLine(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0,
							mSettings.getTuckUnderLineColor(), stroke, brd,
							(mDrawControl & BezierBoardDrawUtil.FlipX) != 0, true);

				BezierBoardDrawUtil.paintBezierSplines(new JavaDraw(g), mOffsetX,
						mOffsetY + ((brd.getCenterWidth() / 2.0 + brd.getMaxRocker()) * mScale), mScale, 0.0, current,
						stroke, new BezierSpline[] { brd.getDeck(), brd.getBottom() },
						(mDrawControl & BezierBoardDrawUtil.FlipX) | BezierBoardDrawUtil.FlipY, fill);

				if (isPaintingFlowlines())
					BezierBoardDrawUtil.paintProfileFlowLines(new JavaDraw(g), mOffsetX,
							mOffsetY + (((brd.getCenterWidth() / 2 + brd.getMaxRocker())) * mScale), mScale, 0.0,
							mSettings.getFlowLinesColor(), stroke, brd, (mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
							true);

				if (isPaintingApexline())
					BezierBoardDrawUtil.paintProfileApexline(new JavaDraw(g), mOffsetX,
							mOffsetY + (((brd.getCenterWidth() / 2 + brd.getMaxRocker())) * mScale), mScale, 0.0,
							mSettings.getApexLineColor(), stroke, brd, (mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
							true);

				if (isPaintingTuckUnderLine())
					BezierBoardDrawUtil.paintProfileTuckUnderLine(new JavaDraw(g), mOffsetX,
							mOffsetY + (((brd.getCenterWidth() / 2 + brd.getMaxRocker())) * mScale), mScale, 0.0,
							mSettings.getTuckUnderLineColor(), stroke, brd,
							(mDrawControl & BezierBoardDrawUtil.FlipX) != 0, true);

				BezierBoard ghost = BoardCAD.getInstance().getGhostBrd();
				if (BoardCAD.getInstance().isGhostMode() && ghost != null && !ghost.isEmpty()) {
					BezierBoardDrawUtil.paintBezierSpline(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0, brdColor,
							stroke, ghost.getOutline(), mDrawControl, fill);

					BezierBoardDrawUtil.paintBezierSplines(new JavaDraw(g), mOffsetX,
							mOffsetY + (((brd.getCenterWidth() / 2 + brd.getMaxRocker())) * mScale), mScale, 0.0,
							brdColor, stroke, new BezierSpline[] { ghost.getDeck(), ghost.getBottom() },
							(mDrawControl & BezierBoardDrawUtil.FlipX) | BezierBoardDrawUtil.FlipY, fill);

				}

				BezierBoard org = BoardCAD.getInstance().getOriginalBrd();
				if (BoardCAD.getInstance().isOrgFocus() && org != null && !org.isEmpty()) {
					BezierBoardDrawUtil.paintBezierSpline(new JavaDraw(g), mOffsetX, mOffsetY, mScale, 0.0, brdColor,
							stroke, org.getOutline(), mDrawControl, fill);

					BezierBoardDrawUtil.paintBezierSplines(new JavaDraw(g), mOffsetX,
							mOffsetY + (((brd.getCenterWidth() / 2 + brd.getMaxRocker())) * mScale), mScale, 0.0,
							brdColor, stroke, new BezierSpline[] { org.getDeck(), org.getBottom() },
							(mDrawControl & BezierBoardDrawUtil.FlipX) | BezierBoardDrawUtil.FlipY, fill);

				}

				final AffineTransform savedTransform = g.getTransform();

				g.setColor(color);

				g.setStroke(stroke);

				final AffineTransform at = new AffineTransform();

				at.setToTranslation(mOffsetX, mOffsetY);

				g.transform(at);

				at.setToScale(mScale, mScale);

				g.transform(at);

				final double mulX = ((mDrawControl & BezierBoardDrawUtil.FlipX) != 0) ? -1 : 1;
				final double mulY = ((mDrawControl & BezierBoardDrawUtil.FlipY) != 0) ? -1 : 1;

				final ArrayList<BezierBoardCrossSection> crossSections = brd.getCrossSections();
				final Line2D line = new Line2D.Double();
				for (int i = 1; i < crossSections.size() - 1; i++) {
					final double pos = crossSections.get(i).getPosition();
					double width = brd.getWidthAtPos(pos);

					if (crossSections.get(i) == brd.getCurrentCrossSection()) {
						g.setColor(Color.RED);
					} else {
						g.setColor(color);
					}
					line.setLine(pos * mulX, (-width / 2) * mulY, pos * mulX, (width / 2) * mulY);
					g.draw(line);
				}

				if (BoardCAD.getInstance().isGhostMode() && ghost != null && !ghost.isEmpty()) {
					final ArrayList<BezierBoardCrossSection> ghostCrossSections = ghost.getCrossSections();
					for (int i = 1; i < ghostCrossSections.size() - 1; i++) {
						final double pos = ghostCrossSections.get(i).getPosition();
						double width = ghost.getWidthAtPos(pos);

						if (ghostCrossSections.get(i) == ghost.getCurrentCrossSection()) {
							g.setColor(Color.RED);
						} else {
							g.setColor(color);
						}
						line.setLine(pos * mulX, (-width / 2) * mulY, pos * mulX, (width / 2) * mulY);
						g.draw(line);
					}

				}

				if (BoardCAD.getInstance().isOrgFocus() && org != null && !org.isEmpty()) {
					final ArrayList<BezierBoardCrossSection> orgCrossSections = org.getCrossSections();
					for (int i = 1; i < orgCrossSections.size() - 1; i++) {
						final double pos = orgCrossSections.get(i).getPosition();
						double width = org.getWidthAtPos(pos);

						if (orgCrossSections.get(i) == org.getCurrentCrossSection()) {
							g.setColor(Color.RED);
						} else {
							g.setColor(color);
						}
						line.setLine(pos * mulX, (-width / 2) * mulY, pos * mulX, (width / 2) * mulY);
						g.draw(line);
					}

				}

				g.setTransform(savedTransform);

			}

			@Override
			public void drawSlidingInfo(final Graphics2D g, final Color color, final Stroke stroke,
					final BezierBoard brd) {
				drawOutlineSlidingInfo(this, g, color, stroke, brd);
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				final Point pos = e.getPoint();
				final Point2D.Double brdPos = screenCoordinateToBrdCoordinate(pos);
				final int index = getCurrentBrd().getNearestCrossSectionIndex(brdPos.x);
				if (index != -1) {
					getCurrentBrd().setCurrentCrossSection(index);
				}
				if (getOriginalBrd() != null) {
					final int indexOriginal = getOriginalBrd().getNearestCrossSectionIndex(brdPos.x);
					if (indexOriginal != -1) {
						getOriginalBrd().setCurrentCrossSection(indexOriginal);
					}
				}
				if (getGhostBrd() != null) {
					final int indexOriginal = getGhostBrd().getNearestCrossSectionIndex(brdPos.x);
					if (indexOriginal != -1) {
						getGhostBrd().setCurrentCrossSection(indexOriginal);
					}
				}
				mCrossSectionSplitPane.repaint();

			}

			@Override
			public void mouseMoved(final MouseEvent e) {

				super.mouseMoved(e);
				mCrossSectionEdit.repaint();
			}

		};

		mCrossSectionSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mCrossSectionEdit, mCrossSectionOutlineEdit);
		mCrossSectionSplitPane.setOneTouchExpandable(true);
		mCrossSectionSplitPane.setResizeWeight(0.7);

		mTabbedPane.add(LanguageResource.getString("CROSSECTIONEDIT_STR"), mCrossSectionSplitPane);

		mRenderedPanel = new JPanel();
		mRenderedPanel.setLayout(new BorderLayout());

		mRenderedPanel.add(mRendered3DView, BorderLayout.CENTER);
		mRenderedPanel.add(mStatusPanel, BorderLayout.SOUTH);

		mTabbedPane.addTab(LanguageResource.getString("3DRENDEREDVIEW_STR"), mRenderedPanel);

		/*
		 * DEBUG! mTabbedPane.add("PrintBrd", mPrintBrd); // Only for debugging
		 * mTabbedPane.add("PrintSpecSheet", mPrintSpecSheet); // Only for // debugging
		 * mTabbedPane.add("PrintChamberedWood", mPrintChamberedWoodTemplate); // Only
		 * // for // debugging mTabbedPane.add("PrintSandwich",
		 * mPrintSandwichTemplates); // Only for // debugging
		 * mTabbedPane.add("PrintHWS", mPrintHollowWoodTemplates); // Only for //
		 * debugging
		 */
		mTabbedPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent e) {
				mGuidePointsDialog.update();

				if (mTabbedPane.getSelectedComponent() == mRenderedPanel
						|| mTabbedPane.getSelectedComponent() == mQuadView) {

					boolean selected = mMenuBar.isShowBesizer3DModelSelected();
					if (selected) {
						updateBezier3DModel();
					}

					mRendered3DView.redraw();
					mQuad3DView.redraw();
				}

			}
		});

		final JMenu pluginMenu = new JMenu(LanguageResource.getString("PLUGINSMENU_STR"));

		final AbstractPluginHandler pluginLoader = new AbstractPluginHandler() {
			static final long serialVersionUID = 1L;

			@Override
			public void onNewPluginMenu(JMenu menu) {
				pluginMenu.add(menu);
			}

			@Override
			public void onNewPluginComponent(JComponent component) {
				mTabbedPane.add(component);
			}
		};
		pluginLoader.loadPlugins("plugins");
		if (pluginMenu.getItemCount() > 0) {
			mMenuBar.add(pluginMenu);
		}

		mBoardSpecPanel = new JPanel();
		mBoardSpecPanel.setLayout(new BorderLayout());
		mBoardSpecPanel.add(mStatusPanel, BorderLayout.NORTH);

		mControlPointInfo = new ControlPointInfo();
		mBoardSpecPanel.add(mControlPointInfo, BorderLayout.EAST);

		mBoardSpec = new BoardSpec();
		mBoardSpecPanel.add(mBoardSpec, BorderLayout.WEST);

		mTabbedPane2 = new JTabbedPane(SwingConstants.BOTTOM){
			public Dimension getPreferredSize() {
                return new Dimension(600, 230);
            }
		};
		mTabbedPane2.addTab("Board specification", mBoardSpecPanel);

		mSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, mTabbedPane, mTabbedPane2);
		mSplitPane.setResizeWeight(1.0);
		mSplitPane.setOneTouchExpandable(true);
		mSplitPane.setDividerLocation(0.7);
		mFrame.getContentPane().add(mSplitPane, BorderLayout.CENTER);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = screenSize.width * 9 / 10;
		int height = screenSize.height * 9 / 10;
		mFrame.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);

		mFrame.setSize(width, height);

		mFrame.setVisible(true);

		//edit.actionPformed(null);

		mWeightCalculatorDialog = new WeightCalculatorDialog();
		mWeightCalculatorDialog.setModal(false);
		mWeightCalculatorDialog.setAlwaysOnTop(true);
		mWeightCalculatorDialog.setVisible(false);

		mGuidePointsDialog = new BoardGuidePointsDialog();
		mGuidePointsDialog.setModal(false);
		mGuidePointsDialog.setAlwaysOnTop(true);
		mGuidePointsDialog.setVisible(false);
		
		// Set current unit after all and everything is initialized
		mToolBar.setUnitSelection(1);
		
		//Use delete as hot-key for delete control points
		final DeleteControlPointAction deleteControlPoint = new DeleteControlPointAction();
		mQuadViewRockerEdit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_CONTROLPOINT");
		mQuadViewRockerEdit.getActionMap().put("DELETE_CONTROLPOINT", deleteControlPoint);
		mOutlineEdit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_CONTROLPOINT");
		mOutlineEdit.getActionMap().put("DELETE_CONTROLPOINT", deleteControlPoint);
		mCrossSectionEdit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_CONTROLPOINT");
		mCrossSectionEdit.getActionMap().put("DELETE_CONTROLPOINT", deleteControlPoint);
		mBottomAndDeckEdit.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_CONTROLPOINT");
		mBottomAndDeckEdit.getActionMap().put("DELETE_CONTROLPOINT", deleteControlPoint);

		getPreferences();
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent e) {
		if (mControlPointInfo != null && mControlPointInfo.isEditing())
			return false;

		if (mGuidePointsDialog != null && mGuidePointsDialog.isVisible() && mGuidePointsDialog.isFocused())
			return false;

		if (mWeightCalculatorDialog != null && mWeightCalculatorDialog.isVisible()
				&& mWeightCalculatorDialog.isFocused())
			return false;

		BoardEdit edit = getSelectedEdit();
		if (edit == null)
			return false;

		if (this.getFrame().getFocusOwner() == null)
			return false;

		// System.out.printf("dispatchKeyEvent() event %s\n",e.toString());

		switch (e.getKeyCode()) {
		case KeyEvent.VK_ADD:
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				new NextCrossSectionAction().actionPerformed(null);
			}
			break;

		case KeyEvent.VK_SUBTRACT:
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				new PreviousCrossSectionAction().actionPerformed(null);
			}
			break;

		case KeyEvent.VK_G:
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.isControlDown())
					break;

				if (mGhostMode == false) {
					mGhostMode = true;
					if (mPreviousCommand == null) {
						mPreviousCommand = getCurrentCommand();
						setCurrentCommand(new GhostCommand());
					}
					if (edit != null)
						edit.repaint();
					mBoardSpec.updateInfoInstantly();
				}
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				if (mGhostMode == true) {
					mGhostMode = false;
					if (mPreviousCommand != null) {
						setCurrentCommand(mPreviousCommand);
						mPreviousCommand = null;
					}
					if (edit != null)
						edit.repaint();
					mBoardSpec.updateInfoInstantly();
				}
			}
			return true;

		case KeyEvent.VK_O:

			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.isControlDown())
					break;

				if (mOrgFocus != true) {
					mOrgFocus = true;
					if (edit != null)
						edit.repaint();
					mBoardSpec.updateInfoInstantly();
				}
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				if (mOrgFocus != false) {
					mOrgFocus = false;
					if (edit != null)
						edit.repaint();
					mBoardSpec.updateInfoInstantly();
				}
			}
			return true;
		case KeyEvent.VK_ESCAPE:
			setCurrentCommand(new BrdEditCommand());
			if (edit != null)
				edit.repaint();
			break;
		}

		if (isGhostMode()) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					if (edit != null)
						edit.mGhostOffsetY -= (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_DOWN:
					if (edit != null)
						edit.mGhostOffsetY += (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_LEFT:
					if (edit != null)
						edit.mGhostOffsetX -= (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_RIGHT:
					if (edit != null)
						edit.mGhostOffsetX += (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_Q:
					if (edit != null)
						edit.mGhostRot -= (Math.PI / 180.0f) * (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_W:
					if (edit != null)
						edit.mGhostRot += (Math.PI / 180.0f) * (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				default:
					return false;
				}
			}

		}
		if (mOrgFocus) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {

				switch (e.getKeyCode()) {
				case KeyEvent.VK_UP:
					if (edit != null)
						edit.mOriginalOffsetY -= (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_DOWN:
					if (edit != null)
						edit.mOriginalOffsetY += (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_LEFT:
					if (edit != null)
						edit.mOriginalOffsetX -= (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				case KeyEvent.VK_RIGHT:
					if (edit != null)
						edit.mOriginalOffsetX += (e.isAltDown() ? .1f : 1f) / edit.getScale();
					mFrame.repaint();
					return true;
				default:
					return false;
				}
			}
		}

		final BrdInputCommand cmd = (BrdInputCommand) edit.getCurrentCommand();
		if (cmd != null) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_T:
				if (e.getID() == KeyEvent.KEY_PRESSED) {
					if (mPreviousCommand == null) {
						mPreviousCommand = getCurrentCommand();
						setCurrentCommand(new SetImageTailCommand());
					}
				} else if (e.getID() == KeyEvent.KEY_RELEASED) {
					if (mPreviousCommand != null) {
						setCurrentCommand(mPreviousCommand);
						mPreviousCommand = null;
					}
				}
				return true;

			case KeyEvent.VK_N:
				if (e.getID() == KeyEvent.KEY_PRESSED) {
					if (mPreviousCommand == null) {
						mPreviousCommand = getCurrentCommand();
						setCurrentCommand(new SetImageNoseCommand());
					}
				} else if (e.getID() == KeyEvent.KEY_RELEASED) {
					if (mPreviousCommand != null) {
						setCurrentCommand(mPreviousCommand);
						mPreviousCommand = null;
					}
				}
				return true;

			}

			return cmd.onKeyEvent(edit, e);
		} else {
			return false;
		}
	}

	public void toggleEditBottomOrDeck() {
		switch (mEditDeckOrBottom) {
		case DECK:
			setEditBottomOrDeck(DeckOrBottom.BOTTOM);
			break;
		case BOTTOM:
			setEditBottomOrDeck(DeckOrBottom.BOTH);
			break;
		case BOTH:
			setEditBottomOrDeck(DeckOrBottom.DECK);
			break;
		}
	}
	
	public void setEditBottomOrDeck(DeckOrBottom editDeckOrBottom) {
		mEditDeckOrBottom = editDeckOrBottom;

		mBottomAndDeckEdit.repaint();

		mBottomAndDeckEdit.mSelectedControlPoints.clear();

		redraw();
	}

	public void drawOutlineFootMarks(final BoardEdit source, final Graphics2D g, final Stroke stroke,
			final BezierBoard brd) {

		if (brd.isEmpty())
			return;

		g.setStroke(stroke);

		Point centerPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(brd.getLength() / 2.0, 0.0));
		Point widthPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(0, brd.getMaxWidth() / 2));

		// get metrics from the graphics
		final FontMetrics metrics = g.getFontMetrics(source.mSlidingInfoFont);
		// get the height of a line of text in this font and render context
		final int hgt = metrics.getHeight();

		for (int i = 0; i < 7; i++) {
			double pos = 0.0;
			String label;

			if (i < 3) {
				pos = (i == 0) ? UnitUtils.INCH : i * UnitUtils.INCH * UnitUtils.INCHES_PR_FOOT;
				label = UnitUtils.convertLengthToCurrentUnit(pos, false);
			} else if (i == 3) {
				pos = brd.getMaxWidthPos();
				label = "W.P:" + UnitUtils.convertLengthToCurrentUnit(mBoardSpec.isOverCurveSelected()
						? brd.getBottom().getPointByCurveLength(pos).x
								- brd.getBottom().getPointByCurveLength(brd.getLength() / 2.0).x
						: pos - brd.getLength() / 2.0, false);
			} else {
				pos = (mBoardSpec.isOverCurveSelected() ? brd.getBottom().getLength() : brd.getLength())
						- ((i == 6) ? UnitUtils.INCH : (6 - i) * UnitUtils.INCH * UnitUtils.INCHES_PR_FOOT);
				label = UnitUtils.convertLengthToCurrentUnit(
						-((i == 6) ? UnitUtils.INCH : (6 - i) * UnitUtils.INCH * UnitUtils.INCHES_PR_FOOT), false);
			}

			if (mBoardSpec.isOverCurveSelected()) {
				pos = brd.getBottom().getPointByCurveLength(pos).x;

				label = label.concat(" O.C");
			}

			double width = brd.getWidthAt(pos);

			String widthStr = UnitUtils.convertLengthToCurrentUnit(width, false);

			g.setColor(Color.BLUE);

			// get the advance of my text in this font and render context
			final int labelWidth = metrics.stringWidth(label);

			// get the advance of my text in this font and render context
			final int widthOfWidthString = metrics.stringWidth(widthStr);

			Point outlinePoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(pos, width / 2.0));
			Point upperOutlinePoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(pos, -width / 2.0));

			g.setColor(Color.BLACK);

			g.drawString(label, outlinePoint.x - (labelWidth / 2), centerPoint.y);

			g.setColor(Color.BLUE);

			g.drawLine(outlinePoint.x, upperOutlinePoint.y, outlinePoint.x, outlinePoint.y);

			g.drawString(widthStr, outlinePoint.x - (widthOfWidthString / 2), widthPoint.y + hgt);

			g.setColor(Color.DARK_GRAY);

			g.drawLine(outlinePoint.x, outlinePoint.y, outlinePoint.x, widthPoint.y);
		}

	}

	public void drawProfileFootMarks(final BoardEdit source, final Graphics2D g, final Stroke stroke,
			final BezierBoard brd) {

		g.setStroke(stroke);

		Point centerPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(brd.getLength() / 2.0, 0.0));
		Point maxThicknessPoint = source
				.brdCoordinateToScreenCoordinateTo(new Point2D.Double(0, brd.getMaxThickness()));
		Point maxRockerPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(0, brd.getMaxRocker()));
		Point bottomPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(0, 0));

		// get metrics from the graphics
		final FontMetrics metrics = g.getFontMetrics(source.mSlidingInfoFont);
		// get the height of a line of text in this font and render context
		final int hgt = metrics.getHeight();

		for (int i = 0; i < 7; i++) {
			double pos = 0.0;
			String label;

			if (i < 3) {
				pos = (i == 0) ? 0.001 : i * UnitUtils.INCH * UnitUtils.INCHES_PR_FOOT;
				label = (i == 0) ? "" : UnitUtils.convertLengthToCurrentUnit(pos, false);
			} else if (i == 3) {
				pos = brd.getLength() / 2.0;
				label = "Center: " + UnitUtils.convertLengthToCurrentUnit(
						mBoardSpec.isOverCurveSelected() ? brd.getBottom().getPointByCurveLength(pos).x : pos, false);
			} else {
				pos = (mBoardSpec.isOverCurveSelected() ? brd.getBottom().getLength() : brd.getLength())
						- ((i == 6) ? 0.005 : (6 - i) * UnitUtils.INCH * UnitUtils.INCHES_PR_FOOT);
				label = (i == 6) ? ""
						: UnitUtils.convertLengthToCurrentUnit(-(6 - i) * UnitUtils.INCH * UnitUtils.INCHES_PR_FOOT,
								false);
			}

			if (mBoardSpec.isOverCurveSelected()) {
				pos = brd.getBottom().getPointByCurveLength(pos).x;

				if (label != "")
					label = label.concat(" O.C");
			}

			double thickness = brd.getThicknessAtPos(pos);
			double rocker = brd.getRockerAtPos(pos);

			String thicknessStr = UnitUtils.convertLengthToCurrentUnit(thickness, false);
			String rockerStr = UnitUtils.convertLengthToCurrentUnit(rocker, false);

			g.setColor(Color.BLUE);

			// get the advance of my text in this font and render context
			final int labelWidth = metrics.stringWidth(label);

			// get the advance of my text in this font and render context
			final int widthOfThicknessString = metrics.stringWidth(thicknessStr);
			final int widthOfRockerString = metrics.stringWidth(rockerStr);

			Point deckPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(pos, rocker + thickness));
			Point rockerPoint = source.brdCoordinateToScreenCoordinateTo(new Point2D.Double(pos, rocker));

			g.setColor(Color.BLACK);

			g.drawString(label, deckPoint.x - (labelWidth / 2), (maxThicknessPoint.y + maxRockerPoint.y) / 2);

			g.setColor(Color.RED);

			g.drawString(thicknessStr, deckPoint.x - (widthOfThicknessString / 2), bottomPoint.y + hgt);

			g.drawLine(deckPoint.x, deckPoint.y, rockerPoint.x, rockerPoint.y);

			g.setColor(Color.BLUE);

			g.drawString(rockerStr, deckPoint.x - (widthOfRockerString / 2), bottomPoint.y + hgt * 2);

			g.drawLine(rockerPoint.x, rockerPoint.y, rockerPoint.x, bottomPoint.y);
		}

	}

	public void drawOutlineSlidingInfo(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {

		final double width = brd.getWidthAtPos(source.mBrdCoord.x);
		if (width <= 0.0)
			return;

		final double mulX = (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
		final double mulY = (source.mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;

		String widthStr = LanguageResource.getString("OUTLINESLIDINGINFOWIDTH_STR");
		source.mSlidingInfoString = widthStr + UnitUtils.convertLengthToCurrentUnit(width, false);

		g.setColor(Color.BLUE);

		// get metrics from the graphics
		final FontMetrics metrics = g.getFontMetrics(source.mSlidingInfoFont);
		// get the height of a line of text in this font and render context
		final int hgt = metrics.getHeight();

		// get the advance of my text in this font and render context
		final int adv = metrics.stringWidth(source.mSlidingInfoString);

		// calculate the size of a box to hold the text with some padding.
		final Dimension size = new Dimension(adv, hgt + 1);
		final Dimension dim = source.getSize();

		// get the advance of my text in this font and render context
		final int advOfWidthStr = metrics.stringWidth(widthStr);

		// calculate the size of a box to hold the text with some padding.
		final Dimension sizeOfWidthStr = new Dimension(advOfWidthStr, hgt + 1);

		int textX = source.mScreenCoord.x - (sizeOfWidthStr.width);
		if (textX < 10)
			textX = 10;

		if (textX + size.width + 10 > dim.width)
			textX = dim.width - size.width - 10;

		if (BoardCAD.getInstance().isPaintingOverCurveMeasurements()) {
			source.mSlidingInfoString = LanguageResource.getString("OUTLINESLIDINGINFOOVERCURVE_STR");

			g.setColor(Color.BLACK);

			g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 4);

			final double fromNose = brd.getFromNoseOverBottomCurveAtPos(source.mBrdCoord.x);

			final double fromTail = brd.getFromTailOverBottomCurveAtPos(source.mBrdCoord.x);

			source.mSlidingInfoString = LanguageResource.getString("OUTLINESLIDINGINFOFROMTAIL_STR")
					+ UnitUtils.convertLengthToCurrentUnit(fromTail, false);

			g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 3);

			source.mSlidingInfoString = LanguageResource.getString("OUTLINESLIDINGINFOFROMNOSE_STR") + (" ")
					+ UnitUtils.convertLengthToCurrentUnit(fromNose, false);

			g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 2);

		}

		if (BoardCAD.getInstance().isPaintingMomentOfInertia()) {
			final double momentOfInertia = brd.getMomentOfInertia(source.mBrdCoord.x, source.mBrdCoord.y);

			source.mSlidingInfoString = LanguageResource.getString("SLIDINGINFOMOMENTOFINERTIA_STR")
					+ UnitUtils.convertMomentOfInertiaToCurrentUnit(momentOfInertia);

			g.drawString(source.mSlidingInfoString, textX, dim.height
					- (size.height + 2) * (BoardCAD.getInstance().isPaintingOverCurveMeasurements() ? 5 : 2));
		}

		source.mSlidingInfoString = LanguageResource.getString("OUTLINESLIDINGINFOWIDTH_STR")
				+ UnitUtils.convertLengthToCurrentUnit(width, false);

		g.setColor(Color.BLUE);

		g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 1);

		final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), source.mOffsetX,
				source.mOffsetY, source.mScale, 0.0);

		source.mSlidingInfoLine.setLine(source.mBrdCoord.x * mulX, -(width / 2) * mulY, source.mBrdCoord.x * mulX,
				(width / 2) * mulY);

		g.draw(source.mSlidingInfoLine);

		g.setTransform(savedTransform);
	}

	public void drawOutlineCrossections(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {

		final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), source.mOffsetX,
				source.mOffsetY, source.mScale, 0.0, (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0,
				(source.mDrawControl & BezierBoardDrawUtil.FlipY) != 0);

		Line2D.Double crossSectionLine = new Line2D.Double(); // @jve:decl-index=0:

		BezierBoardCrossSection currentCrossSection = brd.getCurrentCrossSection();

		final float[] dashPattern = new float[] { 5.0f, 1.0f };
		final BasicStroke bs = (BasicStroke) stroke;
		final BasicStroke stapled = new BasicStroke((float) (bs.getLineWidth() / 2.0), bs.getEndCap(), bs.getLineJoin(),
				bs.getMiterLimit(), dashPattern, 0f);
		final Color noneActiveColor = color.brighter();

		for (int i = 0; i < brd.getCrossSections().size(); i++) {
			BezierBoardCrossSection tmp = brd.getCrossSections().get(i);

			if (tmp == currentCrossSection) {
				g.setColor(color);
				g.setStroke(stroke);
			} else {
				g.setColor(noneActiveColor);
				g.setStroke(stapled);
			}

			double pos = tmp.getPosition();

			final double width = brd.getWidthAtPos(pos);
			if (width <= 0.0)
				continue;

			crossSectionLine.setLine(pos, -(width / 2), pos, (width / 2));

			g.draw(crossSectionLine);
		}

		g.setTransform(savedTransform);
	}

	public void drawProfileSlidingInfo(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {

		final double thickness = brd.getThicknessAtPos(source.mBrdCoord.x);
		if (thickness <= 0.0)
			return;

		final double rocker = brd.getRockerAtPos(source.mBrdCoord.x);
		final double curvature = source.getActiveBezierSplines(brd)[0].getCurvatureAt(source.mBrdCoord.x);
		final double radius = 1 / curvature;

		final double mulX = (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
		final double mulY = (source.mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;

		// get metrics from the graphics
		final FontMetrics metrics = g.getFontMetrics(source.mSlidingInfoFont);
		// get the height of a line of text in this font and render context
		final int hgt = metrics.getHeight();

		final Dimension dim = source.getSize();

		String thicknessStr = LanguageResource.getString("PROFILESLIDINGINFOTHICKNESS_STR");
		source.mSlidingInfoString = thicknessStr + UnitUtils.convertLengthToCurrentUnit(thickness, false);

		// get the advance of my text in this font and render context
		final int adv = metrics.stringWidth(source.mSlidingInfoString);

		// calculate the size of a box to hold the text with some padding.
		final Dimension size = new Dimension(adv, hgt + 1);

		// get the advance of my text in this font and render context
		final int advOfThicknessStr = metrics.stringWidth(thicknessStr);

		// calculate the size of a box to hold the text with some padding.
		final Dimension sizeOfThicknessStr = new Dimension(advOfThicknessStr, hgt + 1);

		int textX = source.mScreenCoord.x - (sizeOfThicknessStr.width);
		if (textX < 10)
			textX = 10;

		if (textX + size.width + 10 > dim.width)
			textX = dim.width - size.width - 10;

		g.setColor(Color.BLUE);

		g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 3);

		g.setColor(Color.RED);

		source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFOROCKER_STR")
				+ UnitUtils.convertLengthToCurrentUnit(rocker, false);

		g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 2);

		source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFORADIUS_STR")
				+ UnitUtils.convertLengthToCurrentUnit(radius, true);

		g.setColor(new Color(102, 102, 102));

		g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 1);

		if (BoardCAD.getInstance().isPaintingOverCurveMeasurements()) {
			source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFOOVERCURVE_STR");

			g.setColor(Color.BLACK);

			g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 6);

			final double fromNose = brd.getFromNoseOverBottomCurveAtPos(source.mBrdCoord.x);

			final double fromTail = brd.getFromTailOverBottomCurveAtPos(source.mBrdCoord.x);

			source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFOFROMTAIL_STR")
					+ UnitUtils.convertLengthToCurrentUnit(fromTail, false);

			g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 5);

			source.mSlidingInfoString = LanguageResource.getString("PROFILESLIDINGINFOFROMNOSE_STR")
					+ UnitUtils.convertLengthToCurrentUnit(fromNose, false);

			g.drawString(source.mSlidingInfoString, textX, dim.height - (size.height + 2) * 4);

		}

		final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), source.mOffsetX,
				source.mOffsetY, source.mScale, 0.0);

		// sets the color of the thickness sliding info bar (inside board)
		g.setColor(Color.BLUE);

		source.mSlidingInfoLine.setLine(source.mBrdCoord.x * mulX, rocker * mulY, source.mBrdCoord.x * mulX,
				(rocker + thickness) * mulY);

		g.draw(source.mSlidingInfoLine);

		// sets the color of the rocker sliding info bar (outside board)
		g.setColor(Color.RED);

		source.mSlidingInfoLine.setLine(source.mBrdCoord.x * mulX, 0 * mulY, source.mBrdCoord.x * mulX, rocker * mulY);

		g.draw(source.mSlidingInfoLine);

		g.setTransform(savedTransform);

	}

	public void drawProfileCrossections(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {

		final double mulX = (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0 ? -1 : 1;
		final double mulY = (source.mDrawControl & BezierBoardDrawUtil.FlipY) != 0 ? -1 : 1;

		final AffineTransform savedTransform = BezierBoardDrawUtil.setTransform(new JavaDraw(g), source.mOffsetX,
				source.mOffsetY, source.mScale, 0.0);

		Line2D.Double crossSectionLine = new Line2D.Double(); // @jve:decl-index=0:

		BezierBoardCrossSection currentCrossSection = brd.getCurrentCrossSection();

		final float[] dashPattern = new float[] { 5.0f, 1.0f };
		final BasicStroke bs = (BasicStroke) stroke;
		final BasicStroke stapled = new BasicStroke((float) (bs.getLineWidth() / 2.0), bs.getEndCap(), bs.getLineJoin(),
				bs.getMiterLimit(), dashPattern, 0f);
		final Color noneActiveColor = color.brighter();

		for (int i = 0; i < brd.getCrossSections().size(); i++) {
			BezierBoardCrossSection tmp = brd.getCrossSections().get(i);

			if (tmp == currentCrossSection) {
				g.setColor(color);
				g.setStroke(stroke);
			} else {
				g.setColor(noneActiveColor);
				g.setStroke(stapled);
			}

			double pos = tmp.getPosition();
			final double deck = brd.getDeckAtPos(pos);
			final double rocker = brd.getRockerAtPos(pos);

			if (deck <= 0.0)
				continue;

			crossSectionLine.setLine(pos * mulX, deck * mulY, pos * mulX, rocker * mulY);

			g.draw(crossSectionLine);
		}

		g.setTransform(savedTransform);
	}

	public void drawOutlineFlowlines(final BoardEdit source, final Graphics2D g, final Color color, final Stroke stroke,
			final BezierBoard brd) {
		BezierBoardDrawUtil.paintOutlineFlowLines(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale, 0.0,
				color, stroke, brd, (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0, true);
	}

	public void drawOutlineTuckUnderLine(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {
		BezierBoardDrawUtil.paintOutlineTuckUnderLine(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale,
				0.0, color, stroke, brd, (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0, true);
	}

	public void drawProfileFlowlines(final BoardEdit source, final Graphics2D g, final Color color, final Stroke stroke,
			final BezierBoard brd) {
		BezierBoardDrawUtil.paintProfileFlowLines(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale, 0.0,
				color, stroke, brd, (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0, true);
	}

	public void drawProfileApexline(final BoardEdit source, final Graphics2D g, final Color color, final Stroke stroke,
			final BezierBoard brd) {
		BezierBoardDrawUtil.paintProfileApexline(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale, 0.0,
				color, stroke, brd, (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0, true);
	}

	public void drawProfileTuckUnderLine(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {
		BezierBoardDrawUtil.paintProfileTuckUnderLine(new JavaDraw(g), source.mOffsetX, source.mOffsetY, source.mScale,
				0.0, color, stroke, brd, (source.mDrawControl & BezierBoardDrawUtil.FlipX) != 0, true);
	}

	public void drawCrossSectionCenterline(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {
		boolean isCompensatedForRocker = mSettings.isUsingOffsetInterpolation();
		BezierBoardDrawUtil.paintCrossSectionCenterline(new JavaDraw(g), source.mOffsetX,
				source.mOffsetY + (isCompensatedForRocker
						? brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition()) * source.mScale
						: 0),
				source.mScale, 0.0, color, stroke, brd, true, !isCompensatedForRocker);
	}

	public void drawCrossSectionFlowlines(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {
		boolean isCompensatedForRocker = mSettings.isUsingOffsetInterpolation();
		BezierBoardDrawUtil.paintCrossSectionFlowLines(new JavaDraw(g), source.mOffsetX,
				source.mOffsetY + (isCompensatedForRocker
						? brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition()) * source.mScale
						: 0),
				source.mScale, 0.0, color, stroke, brd, true, !isCompensatedForRocker);
	}

	public void drawCrossSectionApexline(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {
		boolean isCompensatedForRocker = mSettings.isUsingOffsetInterpolation();
		BezierBoardDrawUtil.paintCrossSectionApexline(new JavaDraw(g), source.mOffsetX,
				source.mOffsetY + (isCompensatedForRocker
						? brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition()) * source.mScale
						: 0),
				source.mScale, 0.0, color, stroke, brd, true, !isCompensatedForRocker);
	}

	public void drawCrossSectionTuckUnderLine(final BoardEdit source, final Graphics2D g, final Color color,
			final Stroke stroke, final BezierBoard brd) {
		boolean isCompensatedForRocker = mSettings.isUsingOffsetInterpolation();
		BezierBoardDrawUtil.paintCrossSectionTuckUnderLine(new JavaDraw(g), source.mOffsetX,
				source.mOffsetY + (isCompensatedForRocker
						? brd.getRockerAtPos(brd.getCurrentCrossSection().getPosition()) * source.mScale
						: 0),
				source.mScale, 0.0, color, stroke, brd, true, !isCompensatedForRocker);
	}

	static public Frame findParentFrame(Container container) {
		while (container != null) {
			if (container instanceof Frame) {
				return (Frame) container;
			}

			container = container.getParent();
		}
		return null;
	}
	
	private void adjustFoilFromCrossSection(BoardEdit edit) {
		if(BoardCADSettings.getInstance().getAdjustCrossectionThickness()) {
			BezierBoardCrossSection crossSection = getCurrentBrd().getCurrentCrossSection();
			BezierSpline crossSectionSpline = crossSection.getBezierSpline();
			ArrayList<BezierKnot> selected = edit.getSelectedControlPoints();
			
			BezierKnot crsDeckPoint = crossSectionSpline.getControlPoint(crossSectionSpline.getNrOfControlPoints()-1);
			if(selected.contains(crsDeckPoint)) {
				BezierSpline deck = getCurrentBrd().getDeck();
				double pos = crossSection.getPosition();
				Point.Double coord = new Point.Double(pos, crsDeckPoint.getEndPoint().y);
				BezierKnot nearest = deck.findBestMatch(coord);
				double distance = Math.abs(nearest.getEndPoint().x - pos);

				BezierKnot deckKnot = null;
				if(distance < 1.0) {
					deckKnot = nearest;
				} else {
					BrdAddControlPointCommand cmd = new BrdAddControlPointCommand();
					this.setEditBottomOrDeck(DeckOrBottom.DECK);
					deckKnot = cmd.addControlPoint(mQuadViewRockerEdit, new Point.Double(pos, deck.getValueAt(pos)));
					deckKnot.setContinous(true);
				}
				BezierSpline bottom = getCurrentBrd().getBottom();
				double y = crsDeckPoint.getEndPoint().y  + bottom.getValueAt(pos);
				deckKnot.setControlPointLocation(pos, y);
			}

			BezierKnot crsBottomPoint = crossSectionSpline.getControlPoint(0);
			if(selected.contains(crsBottomPoint)) {
				BezierSpline bottom = getCurrentBrd().getBottom();
				double pos = crossSection.getPosition();
				Point.Double coord = new Point.Double(pos, crsBottomPoint.getEndPoint().y);
				BezierKnot nearest = bottom.findBestMatch(coord);
				double distance = Math.abs(nearest.getEndPoint().x - pos);
				
				BezierKnot bottomKnot = null;
				if(distance < 1.0) {
					bottomKnot = nearest;
				} else {
					BrdAddControlPointCommand cmd = new BrdAddControlPointCommand();
					this.setEditBottomOrDeck(DeckOrBottom.BOTTOM);
					bottomKnot = cmd.addControlPoint(mQuadViewRockerEdit, new Point.Double(pos, bottom.getValueAt(pos)));					
					bottomKnot.setContinous(true);
				}
				double y = crsBottomPoint.getEndPoint().y  + bottom.getValueAt(pos);
				bottomKnot.setControlPointLocation(pos, y);
				
				double offset = coord.y;
				crossSectionSpline.translate(0, -offset);
			}
			
		}
	}

	public boolean isBoardChanged() {
		return mBoardChanged;
	}

	public void setBoardChanged(boolean aBoardChanged) {
		this.mBoardChanged = aBoardChanged;
	}

	public BezierBoardCrossSection getCrossSectionCopy() {
		return mCrossSectionCopy;
	}

	public void setCrossSectionCopy(BezierBoardCrossSection mCrossSectionCopy) {
		this.mCrossSectionCopy = mCrossSectionCopy;
	}

	public boolean isFlipped() {
		return mIsFlipped;
	}

	public void setFlipped(boolean flipped) {
		this.mIsFlipped = flipped;

		mOutlineEdit.setFlipped(mIsFlipped);
		if (mOutlineEdit2 != null)
			mOutlineEdit2.setFlipped(mIsFlipped);
		mBottomAndDeckEdit.setFlipped(mIsFlipped);
		mCrossSectionOutlineEdit.setFlipped(mIsFlipped);

		mQuadViewOutlineEdit.setFlipped(mIsFlipped);
		mQuadViewRockerEdit.setFlipped(mIsFlipped);

		fitAll();

		mFrame.repaint();
	}

	public void set3DModelApperance(Appearance a) {
		if (mRendered3DView.getBezier3DModel() != null) {
			mRendered3DView.getBezier3DModel().setAppearance(a);
		}

		if (mQuad3DView.getBezier3DModel() != null) {
			mQuad3DView.getBezier3DModel().setAppearance(a);
		}
	}

	public void setShowBezierModel(boolean selected) {
		mRendered3DView.setShowBezierModel(selected);
		mQuad3DView.setShowBezierModel(selected);
		if (selected) {
			updateBezier3DModel();
		}
	}

	public MenuBar getMenuBar() {
		return mMenuBar;
	}

}
