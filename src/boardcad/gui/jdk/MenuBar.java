package boardcad.gui.jdk;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;
import board.AbstractBoard;
import board.BezierBoard;
import board.writers.BrdWriter;
import boardcad.FileTools;
import boardcad.commands.BrdImportCrossSectionCommand;
import boardcad.commands.BrdImportOutlineCommand;
import boardcad.commands.BrdImportProfileCommand;
import boardcad.commands.BrdScaleCommand;
import boardcad.export.DxfExport;
import boardcad.export.GCodeDraw;
import boardcad.export.StlExport;
import boardcad.gui.jdk.actions.AddCrossSectionAction;
import boardcad.gui.jdk.actions.BoardLoadAction;
import boardcad.gui.jdk.actions.BoardSaveAction;
import boardcad.gui.jdk.actions.BoardSaveAndRefreshAction;
import boardcad.gui.jdk.actions.BoardSaveAsAction;
import boardcad.gui.jdk.actions.CopyCrossSectionAction;
import boardcad.gui.jdk.actions.DeleteCrossSectionAction;
import boardcad.gui.jdk.actions.GuidePointsAction;
import boardcad.gui.jdk.actions.LanguageSelectionAction;
import boardcad.gui.jdk.actions.MoveCrossSectionAction;
import boardcad.gui.jdk.actions.BoardNewAction;
import boardcad.gui.jdk.actions.NextCrossSectionAction;
import boardcad.gui.jdk.actions.PasteCrossSectionAction;
import boardcad.gui.jdk.actions.PreviousCrossSectionAction;
import boardcad.gui.jdk.actions.PrintSpecSheetAction;
import boardcad.gui.jdk.actions.PrintSpecSheetToFileAction;
import boardcad.gui.jdk.actions.RedoAction;
import boardcad.gui.jdk.actions.UndoAction;
import boardcad.i18n.LanguageResource;
import boardcad.print.PrintBrd;
import boardcad.print.PrintChamberedWoodTemplate;
import boardcad.print.PrintHollowWoodTemplates;
import boardcad.print.PrintSandwichTemplates;
import boardcad.settings.CategorizedSettings;
import boardcad.settings.Settings;
import boardcam.MachineConfig;
import boardcam.cutters.AbstractCutter;
import boardcam.holdingsystems.SupportsBlankHoldingSystem;
import boardcam.toolpathgenerators.AbstractToolpathGenerator;
import boardcam.toolpathgenerators.AtuaCoresToolpathGenerator;
import boardcam.toolpathgenerators.HotwireToolpathGenerator2;
import boardcam.toolpathgenerators.WidthSplitsToolpathGenerator;
import boardcam.toolpathgenerators.ext.SandwichCompensation;
import boardcam.writers.GCodeWriter;
import cadcore.AbstractBezierBoardSurfaceModel;
import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.UnitUtils;

public class MenuBar extends JMenuBar implements ActionListener {
	private PrintSandwichTemplates mPrintSandwichTemplates = new PrintSandwichTemplates();
	private PrintChamberedWoodTemplate mPrintChamberedWoodTemplate = new PrintChamberedWoodTemplate();
	private PrintHollowWoodTemplates mPrintHollowWoodTemplates  = new PrintHollowWoodTemplates();

	private final JMenu mRecentBrdFilesMenu = new JMenu();
	
	private JCheckBoxMenuItem mIsPaintingGridMenuItem;
	private JCheckBoxMenuItem mIsPaintingOriginalBrdMenuItem;
	private JCheckBoxMenuItem mIsPaintingGhostBrdMenuItem;
	private JCheckBoxMenuItem mIsPaintingControlPointsMenuItem;
	private JCheckBoxMenuItem mIsPaintingNonActiveCrossSectionsMenuItem;
	private JCheckBoxMenuItem mIsPaintingGuidePointsMenuItem;
	private JCheckBoxMenuItem mIsPaintingCurvatureMenuItem;
	private JCheckBoxMenuItem mIsPaintingVolumeDistributionMenuItem;
	private JCheckBoxMenuItem mIsPaintingCenterOfMassMenuItem;
	private JCheckBoxMenuItem mIsPaintingSlidingInfoMenuItem;
	private JCheckBoxMenuItem mIsPaintingSlidingCrossSectionMenuItem;
	private JCheckBoxMenuItem mIsPaintingFinsMenuItem;
	private JCheckBoxMenuItem mIsPaintingBackgroundImageMenuItem;
	private JCheckBoxMenuItem mIsPaintingBaseLineMenuItem;
	private JCheckBoxMenuItem mIsPaintingCenterLineMenuItem;
	private JCheckBoxMenuItem mIsPaintingOverCurveMesurementsMenuItem;
	private JCheckBoxMenuItem mIsPaintingMomentOfInertiaMenuItem;
	private JCheckBoxMenuItem mIsPaintingCrossectionsPositionsMenuItem;
	private JCheckBoxMenuItem mIsPaintingFlowlinesMenuItem;
	private JCheckBoxMenuItem mIsPaintingApexlineMenuItem;
	private JCheckBoxMenuItem mIsPaintingTuckUnderLineMenuItem;
	private JCheckBoxMenuItem mIsPaintingFootMarksMenuItem;
	private JCheckBoxMenuItem mIsAntialiasingMenuItem;
	private JCheckBoxMenuItem mUseFillMenuItem;
	
	private JCheckBoxMenuItem mShowRenderInwireframe;
	private JCheckBoxMenuItem mShowBezier3DModelMenuItem;
	private JCheckBoxMenuItem mAutoUpdate3DModelMenuItem;
	
	private JRadioButtonMenuItem mControlPointInterpolationButton;
	private JRadioButtonMenuItem mSBlendInterpolationButton;

	public MenuBar() {
		BoardCAD boardCAD = BoardCAD .getInstance();
		
		final JMenu fileMenu = new JMenu(LanguageResource.getString("FILEMENU_STR"));
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		final AbstractAction newBrd = new BoardNewAction();
		fileMenu.add(newBrd);

		final BoardLoadAction loadBrd = new BoardLoadAction(boardCAD.getCurrentBrd(), boardCAD.getOriginalBrd());
		fileMenu.add(loadBrd);

		mRecentBrdFilesMenu.setText(LanguageResource.getString("RECENTFILES_STR"));
		fileMenu.add(mRecentBrdFilesMenu);
		fileMenu.addSeparator();

		final AbstractAction saveBrd = new BoardSaveAction();
		fileMenu.add(saveBrd);

		BoardSaveAsAction saveBrdAs = new BoardSaveAsAction();
		fileMenu.add(saveBrdAs);

		final AbstractAction saveBrdRefresh = new BoardSaveAndRefreshAction();
		fileMenu.add(saveBrdRefresh);
		
		fileMenu.addSeparator();

		final BoardLoadAction loadGhost = new BoardLoadAction(boardCAD.getGhostBrd()) {
			@Override
			public void actionPerformed(ActionEvent event) {
				super.actionPerformed(event);
				mIsPaintingGhostBrdMenuItem.setSelected(true);
				boardCAD.getSelectedEdit().repaint();
			}
		};
		loadGhost.putValue(Action.NAME, LanguageResource.getString("OPENGHOSTBOARD_STR"));
		loadGhost.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
		fileMenu.add(loadGhost);

		final AbstractAction loadImage = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("LOADBACKGROUNDIMAGE_STR"));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				final JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showOpenDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;
				
				System.out.printf("Loading image");
				
				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				System.out.printf("Loading image %s", filename);
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				BoardEdit edit = boardCAD.getSelectedEdit();
				if (edit == null)
					return;

				edit.loadBackgroundImage(filename);
				mIsPaintingBackgroundImageMenuItem.setSelected(true);
				edit.repaint();
			}

		};
		fileMenu.add(loadImage);
		fileMenu.addSeparator();

		final JMenu printMenu = new JMenu(LanguageResource.getString("PRINTMENU_STR"));
		final JMenuItem printOutline = new JMenuItem(LanguageResource.getString("PRINTOUTLINE_STR"), KeyEvent.VK_O);
		printOutline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		printOutline.addActionListener(this);
		printMenu.add(printOutline);

		final JMenuItem printSpinTemplate = new JMenuItem(LanguageResource.getString("PRINTSPINTEMPLATE_STR"),
				KeyEvent.VK_T);
		// printOutline.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
		// ActionEvent.ALT_MASK));
		printSpinTemplate.addActionListener(this);
		printMenu.add(printSpinTemplate);

		final JMenuItem printProfile = new JMenuItem(LanguageResource.getString("PRINTPROFILE_STR"), KeyEvent.VK_P);
		printProfile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
		printProfile.addActionListener(this);
		printMenu.add(printProfile);

		final JMenuItem printSlices = new JMenuItem(LanguageResource.getString("PRINTCROSSECTION_STR"), KeyEvent.VK_S);
		printSlices.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, ActionEvent.ALT_MASK));
		printSlices.addActionListener(this);
		printMenu.add(printSlices);

		printMenu.addSeparator();

		final JMenu printSandwichMenu = new JMenu(LanguageResource.getString("PRINTSANDWICHTEMPLATESMENU_STR"));

		final AbstractAction printProfileTemplate = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSANDWICHPROFILETEMPLATE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("SANDWICHPARAMETERSCATEGORY_STR");
				Settings sandwichSettings = settings.addCategory(categoryName);
				sandwichSettings.addMeasurement("SkinThickness", 0.3,
						LanguageResource.getString("SANDWICHSKINTHICKNESS_STR"));
				sandwichSettings.addBoolean("Flatten", false, LanguageResource.getString("SANDWICHFLATTEN_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTSANDWICHPROFILETEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}

				mPrintSandwichTemplates.printProfileTemplate(sandwichSettings.getMeasurement("SkinThickness"),
						sandwichSettings.getBoolean("Flatten"), 0.0);
				settingsDialog.dispose();
			}

		};
		printSandwichMenu.add(printProfileTemplate);

		final AbstractAction printRailTemplate = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSANDWICHRAILTEMPLATE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("SANDWICHPARAMETERSCATEGORY_STR");
				Settings sandwichSettings = settings.addCategory(categoryName);
				sandwichSettings.addMeasurement("SkinThickness", 0.3,
						LanguageResource.getString("SANDWICHSKINTHICKNESS_STR"));
				sandwichSettings.addMeasurement("ToRail", 2.54 / 2,
						LanguageResource.getString("SANDWICHDISTANCETORAIL_STR"));
				sandwichSettings.addMeasurement("TailOffset", 2.0, LanguageResource.getString("SANDWICHTAILOFFSET"));
				sandwichSettings.addMeasurement("NoseOffset", 6.0, LanguageResource.getString("SANDWICHNOSEOFFSET"));
				sandwichSettings.addBoolean("Flatten", false, LanguageResource.getString("SANDWICHFLATTEN_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTSANDWICHRAILTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}

				mPrintSandwichTemplates.printRailTemplate(sandwichSettings.getMeasurement("ToRail"),
						sandwichSettings.getMeasurement("SkinThickness"), sandwichSettings.getMeasurement("TailOffset"),
						sandwichSettings.getMeasurement("NoseOffset"), sandwichSettings.getBoolean("Flatten"));
				settingsDialog.dispose();
			}

		};
		printSandwichMenu.add(printRailTemplate);

		final AbstractAction printDeckSkin = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSANDWICHDECKSKINTEMPLATE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("SANDWICHPARAMETERSCATEGORY_STR");
				Settings sandwichSettings = settings.addCategory(categoryName);
				sandwichSettings.addMeasurement("ToRail", 2.54 / 2,
						LanguageResource.getString("SANDWICHDISTANCETORAIL_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTSANDWICHDECKSKINTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}

				mPrintSandwichTemplates.printDeckSkinTemplate(sandwichSettings.getMeasurement("ToRail"));
				settingsDialog.dispose();
			}

		};
		printSandwichMenu.add(printDeckSkin);

		final AbstractAction printBottomSkin = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTSANDWICHBOTTOMSKINTEMPLATE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("SANDWICHPARAMETERSCATEGORY_STR");
				Settings sandwichSettings = settings.addCategory(categoryName);
				sandwichSettings.addMeasurement("ToRail", 2.54 / 2,
						LanguageResource.getString("SANDWICHDISTANCETORAIL_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTSANDWICHBOTTOMSKINTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}

				mPrintSandwichTemplates.printDeckSkinTemplate(sandwichSettings.getMeasurement("ToRail"));
				settingsDialog.dispose();
			}

		};
		printSandwichMenu.add(printBottomSkin);

		printMenu.add(printSandwichMenu);

		printMenu.addSeparator();

		final JMenu printHWSMenu = new JMenu(LanguageResource.getString("PRINTHWSMENU_STR"));

		final AbstractAction printHWSSTringer = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSSTRINGER_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSSTRINGERTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printStringerTemplate(HWSSettings.getMeasurement("SkinThickness"),
						HWSSettings.getMeasurement("FrameThickness"), HWSSettings.getMeasurement("Webbing"),
						HWSSettings.getMeasurement("TailOffset"), HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSSTringer);

		final AbstractAction printHWSRibs = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSRIBS_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSRIBSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printCrosssectionTemplates(HWSSettings.getMeasurement("DistanceFromRail"),
						HWSSettings.getMeasurement("SkinThickness"), HWSSettings.getMeasurement("FrameThickness"),
						HWSSettings.getMeasurement("Webbing"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSRibs);

		final AbstractAction printHWSRail = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSRAIL_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSRAILTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printRailTemplate(HWSSettings.getMeasurement("DistanceFromRail"),
						HWSSettings.getMeasurement("SkinThickness"), HWSSettings.getMeasurement("FrameThickness"),
						HWSSettings.getMeasurement("Webbing"), HWSSettings.getMeasurement("TailOffset"),
						HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSRail);

		final AbstractAction printHWSNosePiece = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSNOSEPIECE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSTAILPIECETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printNoseTemplate(HWSSettings.getMeasurement("DistanceFromRail"),
						HWSSettings.getMeasurement("SkinThickness"), HWSSettings.getMeasurement("FrameThickness"),
						HWSSettings.getMeasurement("Webbing"), HWSSettings.getMeasurement("TailOffset"),
						HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSNosePiece);

		final AbstractAction printHWSTailPiece = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSTAILPIECE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSNOSEPIECETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printTailTemplate(HWSSettings.getMeasurement("DistanceFromRail"),
						HWSSettings.getMeasurement("SkinThickness"), HWSSettings.getMeasurement("FrameThickness"),
						HWSSettings.getMeasurement("Webbing"), HWSSettings.getMeasurement("TailOffset"),
						HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSTailPiece);

		final AbstractAction printHWSDeckTemplate = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSDECKTEMPLATE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSDECKTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printDeckSkinTemplate(HWSSettings.getMeasurement("DistanceFromRail"),
						HWSSettings.getMeasurement("TailOffset"), HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSDeckTemplate);

		final AbstractAction printHWSBottomTemplate = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTHWSBOTTOMTEMPLATE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				settings.getPreferences();
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTHWSBOTTOMTEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				mPrintHollowWoodTemplates.printBottomSkinTemplate(HWSSettings.getMeasurement("DistanceFromRail"),
						HWSSettings.getMeasurement("TailOffset"), HWSSettings.getMeasurement("NoseOffset"));
				settingsDialog.dispose();
			}

		};
		printHWSMenu.add(printHWSBottomTemplate);

		printMenu.add(printHWSMenu);

		final JMenu printChamberedWoodMenu = new JMenu(
				LanguageResource.getString("PRINTCHAMBEREDWOODTEMPLATESMENU_STR"));

		final AbstractAction printChamberedWoodTemplate = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("PRINTCHAMBEREDWOODPROFILETEMPLATE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				BezierBoard currentBrd = boardCAD.getCurrentBrd();

				final CategorizedSettings settings = new CategorizedSettings();
				final String categoryName = LanguageResource.getString("CHAMBEREDWOODPARAMETERSCATEGORY_STR");
				final Settings chamberedWoodSettings = settings.addCategory(categoryName);
				chamberedWoodSettings.addBoolean("Draw grid", true, LanguageResource.getString("DRAWGRID_STR"));
				chamberedWoodSettings.addMeasurement("Start Offset from center", 0.0,
						LanguageResource.getString("CHAMBEREDWOODOFFSETFROMCENTER_STR"));
				chamberedWoodSettings.addMeasurement("End Offset from center", currentBrd.getMaxWidth() / 2.0,
						LanguageResource.getString("CHAMBEREDWOODENDOFFSET_STR"));
				chamberedWoodSettings.addMeasurement("Plank thickness", 2.54,
						LanguageResource.getString("CHAMBEREDWOODPLANKTHICKNESS_STR"));
				chamberedWoodSettings.addMeasurement("Deck/Bottom thickness", 0.8,
						LanguageResource.getString("CHAMBEREDWOODDECKANDBOTTOMTHICKNESS_STR"));
				chamberedWoodSettings.addBoolean("Draw chambering", true,
						LanguageResource.getString("CHAMBEREDDRAWCHAMBERING_STR"));
				chamberedWoodSettings.addBoolean("Draw alignment marks", true,
						LanguageResource.getString("CHAMBEREDDRAWALIGNEMNETMARKS_STR"));

				chamberedWoodSettings.addBoolean("Print multiple", false,
						LanguageResource.getString("CHAMBEREDPRINTMULTIPLETEMPLATES_STR"));

				settings.getPreferences();

				if (chamberedWoodSettings.getMeasurement("End Offset from center") > currentBrd.getMaxWidth() / 2.0) {
					chamberedWoodSettings.setMeasurement("End Offset from center", currentBrd.getMaxWidth() / 2.0);
				}

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("PRINTCHAMBEREDWOODPROFILETEMPLATETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}

				double start = chamberedWoodSettings.getMeasurement("Start Offset from center");
				double end = chamberedWoodSettings.getMeasurement("End Offset from center");
				double plankThickness = chamberedWoodSettings.getMeasurement("Plank thickness");

				boolean printMultiple = chamberedWoodSettings.getBoolean("Print multiple");
				if (printMultiple) {

					int numberOfTemplates = (int) ((end - start) / plankThickness);

					int selection = JOptionPane.showConfirmDialog(BoardCAD.getInstance().getFrame(),
							String.valueOf(numberOfTemplates) + " "
									+ LanguageResource.getString("PRINTCHAMBEREDWOODMULTIPLETEMPLATESMSG_STR"),
							LanguageResource.getString("PRINTCHAMBEREDWOODMULTIPLETEMPLATESTITLE_STR"),
							JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
					if (selection != JOptionPane.YES_OPTION) {

						return;

					}
				}

				mPrintChamberedWoodTemplate.printTemplate(chamberedWoodSettings.getBoolean("Draw grid"), start, end,
						plankThickness, chamberedWoodSettings.getMeasurement("Deck/Bottom thickness"),
						chamberedWoodSettings.getBoolean("Draw chambering"),
						chamberedWoodSettings.getBoolean("Draw alignment marks"), printMultiple);

				settingsDialog.dispose();

				settings.putPreferences();
			}

		};
		printChamberedWoodMenu.add(printChamberedWoodTemplate);

		printMenu.add(printChamberedWoodMenu);

		printMenu.addSeparator();

		/*
		 * final JMenuItem printSpecSheet = new JMenuItem(LanguageResource.getString
		 * ("PRINTSPECSHEET_STR"),KeyEvent.VK_H);
		 * printSpecSheet.setAccelerator(KeyStroke
		 * .getKeyStroke(KeyEvent.VK_5,ActionEvent.ALT_MASK));
		 * printSpecSheet.addActionListener(this); printMenu.add(printSpecSheet);
		 */

		final AbstractAction printSpecSheet = new PrintSpecSheetAction();
		printMenu.add(printSpecSheet);


		final AbstractAction printSpecSheetToFile = new PrintSpecSheetToFileAction();
		printMenu.add(printSpecSheetToFile);

		fileMenu.add(printMenu);

		final JMenu importMenu = new JMenu(LanguageResource.getString("IMPORTMENU_STR"));

		final JMenu importBezierMenu = new JMenu(LanguageResource.getString("IMPORTBEZIERMENU_STR"));
		importMenu.add(importBezierMenu);
		final AbstractAction importOutlineAction = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("IMPORTBEZIEROUTLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				BrdImportOutlineCommand cmd = new BrdImportOutlineCommand(boardCAD.getOutlineEdit());
				cmd.execute();
			}

		};
		importBezierMenu.add(importOutlineAction);
		final AbstractAction importProfileAction = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("IMPORTBEZIERPROFILE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				BrdImportProfileCommand cmd = new BrdImportProfileCommand(boardCAD.getBottomAndDeckEdit());
				cmd.execute();
			}

		};
		importBezierMenu.add(importProfileAction);
		final AbstractAction importCrossSectionAction = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("IMPORTBEZIERCROSSSECTION_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				BrdImportCrossSectionCommand cmd = new BrdImportCrossSectionCommand(boardCAD.getCrossSectionEdit());
				cmd.execute();
			}

		};
		importBezierMenu.add(importCrossSectionAction);

		fileMenu.add(importMenu);

		final JMenu exportMenu = new JMenu(LanguageResource.getString("EXPORTMENU_STR"));

		final AbstractAction exportBezierStl = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERTOSTL_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				try {
					StlExport.exportBezierBoard(filename, BoardCAD.getInstance().getCurrentBrd(), 50, 50, 200);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERTOSTLFAILEDTITLE_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIERTOSTLFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);
				}
			}

		};

		exportMenu.add(exportBezierStl);
		exportMenu.addSeparator();

		JMenu beziersExportMenu = new JMenu(LanguageResource.getString("EXPORTBEZIERS_STR"));
		final AbstractAction exportBezierOutline = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIEROUTLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));
				fc.setFileFilter(new FileFilter() {

					// Accept all directories and brd and s3d files.
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						String extension = FileTools.getExtension(f);
						if (extension != null && extension.equals("otl")) {
							return true;
						}

						return false;
					}

					// The description of this filter
					@Override
					public String getDescription() {
						return LanguageResource.getString("OUTLINEFILES_STR");
					}
				});

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				try {
					if (BrdWriter.exportOutline(boardCAD.getCurrentBrd(), filename) == false) {
						throw new Exception();
					}
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIEROUTLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIEROUTLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}

				BoardCAD.defaultDirectory = file.getPath();

			}

		};
		beziersExportMenu.add(exportBezierOutline);

		final AbstractAction exportBezierProfile = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERPROFILE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));
				fc.setFileFilter(new FileFilter() {

					// Accept all directories and pfl files.
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						String extension = FileTools.getExtension(f);
						if (extension != null && extension.equals("pfl")) {
							return true;
						}

						return false;
					}

					// The description of this filter
					@Override
					public String getDescription() {
						return LanguageResource.getString("PROFILEFILES_STR");
					}
				});

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				try {
					if (BrdWriter.exportProfile(boardCAD.getCurrentBrd(), filename) == false) {
						throw new Exception();
					}
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIERFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

				}

				BoardCAD.defaultDirectory = file.getPath();

			}

		};
		beziersExportMenu.add(exportBezierProfile);

		final AbstractAction exportBezierCrossection = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERCROSSECTION_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));
				fc.setFileFilter(new FileFilter() {

					// Accept all directories and brd and s3d files.
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						String extension = FileTools.getExtension(f);
						if (extension != null && extension.equals("crs")) {
							return true;
						}

						return false;
					}

					// The description of this filter
					@Override
					public String getDescription() {
						return LanguageResource.getString("CROSSECTIONFILES_STR");
					}
				});

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				try {
					if (BrdWriter.exportCrossection(boardCAD.getCurrentBrd(), boardCAD.getCurrentBrd().getCurrentCrossSectionIndex(),
							filename) == false) {
						throw new Exception();
					}
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIERFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

				}

				BoardCAD.defaultDirectory = file.getPath();

			}

		};
		beziersExportMenu.add(exportBezierCrossection);

		exportMenu.add(beziersExportMenu);
		exportMenu.addSeparator();

		final AbstractAction exportProfileAsDxfSpline = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERPROFILEASDXFSPLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd().getBottom();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd().getDeck();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint((org.getNrOfControlPoints() - 1) - i)
								.clone();
						controlPoint.switch_tangents();
						patches[1].append(controlPoint);
					}

					DxfExport.exportBezierSplines(filename, patches);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERPROFILEASDXFSPLINEFAILEDMSG_STR")
							+ e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIERPROFILEASDXFSPLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportProfileAsDxfSpline);

		final AbstractAction exportOutlineAsDxfSpline = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFSPLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd().getOutline();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd().getOutline();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint((org.getNrOfControlPoints() - 1) - i)
								.clone();
						controlPoint.switch_tangents();
						controlPoint.getEndPoint().y = -controlPoint.getEndPoint().y;
						controlPoint.getTangentToPrev().y = -controlPoint.getTangentToPrev().y;
						controlPoint.getTangentToNext().y = -controlPoint.getTangentToNext().y;
						patches[1].append(controlPoint);
					}
					DxfExport.exportBezierSplines(filename, patches);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFSPLINEFAILEDMSG_STR")
							+ e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFSPLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportOutlineAsDxfSpline);

		final AbstractAction exportCurrentCrossSectionAsDxfSpline = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFSPLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd().getCurrentCrossSection().getBezierSpline();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd().getCurrentCrossSection()
							.getBezierSpline();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint((org.getNrOfControlPoints() - 1) - i)
								.clone();
						controlPoint.switch_tangents();
						controlPoint.getEndPoint().x = -controlPoint.getEndPoint().x;
						controlPoint.getTangentToPrev().x = -controlPoint.getTangentToPrev().x;
						controlPoint.getTangentToNext().x = -controlPoint.getTangentToNext().x;
						patches[1].append(controlPoint);
					}
					DxfExport.exportBezierSplines(filename, patches);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFSPLINEFAILEDMSG_STR")
							+ e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFSPLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportCurrentCrossSectionAsDxfSpline);

		exportMenu.addSeparator();

		final AbstractAction exportProfileAsDxfPolyline = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERPROFILEASDXFPOLYLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd().getBottom();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd().getDeck();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint((org.getNrOfControlPoints() - 1) - i)
								.clone();
						controlPoint.switch_tangents();
						patches[1].append(controlPoint);
					}

					DxfExport.exportPolylineFromSplines(filename, patches, 100);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERPROFILEASDXFPOLYLINEFAILEDMSG_STR")
							+ e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIERPROFILEASDXFPOLYLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportProfileAsDxfPolyline);

		final AbstractAction exportOutlineAsDxfPolyline = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFPOLYLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd().getOutline();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd().getOutline();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint((org.getNrOfControlPoints() - 1) - i)
								.clone();
						controlPoint.switch_tangents();
						controlPoint.getEndPoint().y = -controlPoint.getEndPoint().y;
						controlPoint.getTangentToPrev().y = -controlPoint.getTangentToPrev().y;
						controlPoint.getTangentToNext().y = -controlPoint.getTangentToNext().y;
						patches[1].append(controlPoint);
					}
					DxfExport.exportPolylineFromSplines(filename, patches, 100);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFPOLYLINEFAILEDMSG_STR")
							+ e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIEROUTLINEASDXFPOLYLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportOutlineAsDxfPolyline);

		final AbstractAction exportCrossSectionAsDxfPolyline = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFPOLYLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				try {
					BezierSpline[] patches = new BezierSpline[2];
					patches[0] = BoardCAD.getInstance().getCurrentBrd().getCurrentCrossSection().getBezierSpline();
					patches[1] = new BezierSpline();
					BezierSpline org = BoardCAD.getInstance().getCurrentBrd().getCurrentCrossSection()
							.getBezierSpline();

					for (int i = 0; i < org.getNrOfControlPoints(); i++) {
						BezierKnot controlPoint = (BezierKnot) org.getControlPoint((org.getNrOfControlPoints() - 1) - i)
								.clone();
						controlPoint.switch_tangents();
						controlPoint.getEndPoint().x = -controlPoint.getEndPoint().x;
						controlPoint.getTangentToPrev().x = -controlPoint.getTangentToPrev().x;
						controlPoint.getTangentToNext().x = -controlPoint.getTangentToNext().x;
						patches[1].append(controlPoint);
					}
					DxfExport.exportPolylineFromSplines(filename, patches, 100);
				} catch (Exception e) {
					String str = LanguageResource.getString("EXPORTBEZIERCROSSSECTIONASDXFPOLYLINEFAILEDMSG_STR")
							+ e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("EXPORTBEZIERCROSSECTIONASDXFPOLYLINEFAILEDTITLE_STR"),
							JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		exportMenu.add(exportCrossSectionAsDxfPolyline);

		fileMenu.add(exportMenu);

		final JMenu gcodeMenu = new JMenu(LanguageResource.getString("GCODEMENU_STR"));

		final AbstractAction gcodeBezier = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEBEZIER_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				BezierBoard brd = boardCAD.getCurrentBrd();
				MachineConfig machineConfig = new MachineConfig();
				machineConfig.setBoard((BezierBoard) brd.clone());
				MachineDialog dialog = new MachineDialog(machineConfig);
				// dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
				// dialog.setModal(false);

				machineConfig.setMachineView(dialog.getMachineView());
				machineConfig.initialize();
				machineConfig.getPreferences();

				// Turn of sandwich compensation so we don't use sandwich
				// compensation by accident (lesson learned the hard way)
				Settings sandwichCompensationSettings = machineConfig
						.getCategory(LanguageResource.getString("SANDWICHCOMPENSATIONCATEGORY_STR"));
				sandwichCompensationSettings.setBoolean(SandwichCompensation.SANDWICH_DECK_COMPENSATION_ON, false);
				sandwichCompensationSettings.setBoolean(SandwichCompensation.SANDWICH_BOTTOM_COMPENSATION_ON, false);
				sandwichCompensationSettings.setBoolean(SandwichCompensation.SANDWICH_OUTLINE_COMPENSATION_ON, false);

				Settings generalSettings = machineConfig.getCategory(LanguageResource.getString("GENERALCATEGORY_STR"));

				if (generalSettings.getBoolean(MachineConfig.USE_BRD_SETTINGS) == true) {
					System.out.printf("Using board settings");
					if (generalSettings.getEnumeration(MachineConfig.BLANKHOLDINGSYSTEM_TYPE) == 0) {
						// generalSettings.getDouble(MachineConfig.TAILSTOP_POS,
						// );

						Settings supportsSettings = machineConfig
								.getCategory(LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR"));

						supportsSettings.setObject(SupportsBlankHoldingSystem.SUPPORT_1_POS,
								new Double(brd.getStrut1()[0]));
						supportsSettings.setObject(SupportsBlankHoldingSystem.SUPPORT_2_POS,
								new Double(brd.getStrut2()[0]));

						supportsSettings.setObject(SupportsBlankHoldingSystem.SUPPORT_1_HEIGHT,
								new Double(brd.getStrut1()[1]));
						supportsSettings.setObject(SupportsBlankHoldingSystem.SUPPORT_2_HEIGHT,
								new Double(brd.getStrut2()[1]));
					}

					generalSettings.setObject(MachineConfig.BLANK, generalSettings.new FileName(brd.getBlankFile()));

					Settings cutsSettings = machineConfig.getCategory(LanguageResource.getString("CUTSCATEGORY_STR"));

					cutsSettings.setObject(MachineConfig.DECK_CUTS, new Integer(brd.getTopCuts()));
					cutsSettings.setObject(MachineConfig.DECK_RAIL_CUTS, new Integer(brd.getTopShoulderCuts()));
					cutsSettings.setObject(MachineConfig.BOTTOM_CUTS, new Integer(brd.getBottomCuts()));
					cutsSettings.setObject(MachineConfig.BOTTOM_RAIL_CUTS, new Integer(brd.getBottomRailCuts()));

					cutsSettings.setObject(MachineConfig.DECK_ANGLE, new Double(brd.getTopShoulderAngle()));
					cutsSettings.setObject(MachineConfig.DECK_RAIL_ANGLE, new Double(brd.getMaxAngle()));

					Settings speedSettings = machineConfig.getCategory(LanguageResource.getString("SPEEDCATEGORY_STR"));
					speedSettings.setObject(MachineConfig.CUTTING_SPEED, new Double(brd.getRegularSpeed()));
					speedSettings.setObject(MachineConfig.CUTTING_SPEED_STRINGER, new Double(brd.getStringerSpeed()));
					speedSettings.setObject(MachineConfig.CUTTING_SPEED_RAIL, new Double(brd.getRegularSpeed()));
					speedSettings.setObject(MachineConfig.CUTTING_SPEED_OUTLINE, new Double(brd.getRegularSpeed()));

				}

				dialog.setVisible(true);

				if (generalSettings.getBoolean(MachineConfig.USE_BRD_SETTINGS)) {
					if (generalSettings.getEnumeration(MachineConfig.BLANKHOLDINGSYSTEM_TYPE) == 0) {
						// generalSettings.getDouble(MachineConfig.TAILSTOP_POS,
						// );

						Settings supportsSettings = machineConfig
								.addCategory(LanguageResource.getString("BLANKHOLDINGSYSTEMCATEGORY_STR"));

						// generalSettings.getDouble(MachineConfig.TAILSTOP_POS,
						// );
						brd.getStrut1()[0] = supportsSettings.getDouble(SupportsBlankHoldingSystem.SUPPORT_1_POS);
						brd.getStrut2()[0] = supportsSettings.getDouble(SupportsBlankHoldingSystem.SUPPORT_2_POS);

						brd.getStrut1()[1] = supportsSettings.getDouble(SupportsBlankHoldingSystem.SUPPORT_1_HEIGHT);
						brd.getStrut2()[1] = supportsSettings.getDouble(SupportsBlankHoldingSystem.SUPPORT_2_HEIGHT);
					}

					brd.setBlankFile(generalSettings.getFileName(MachineConfig.BLANK));

					Settings cutsSettings = machineConfig.getCategory(LanguageResource.getString("CUTSCATEGORY_STR"));

					brd.setTopCuts(cutsSettings.getInt(MachineConfig.DECK_CUTS));
					brd.setTopShoulderCuts(cutsSettings.getInt(MachineConfig.DECK_RAIL_CUTS));
					brd.setBottomCuts(cutsSettings.getInt(MachineConfig.BOTTOM_CUTS));
					brd.setBottomRailCuts(cutsSettings.getInt(MachineConfig.BOTTOM_RAIL_CUTS));

					brd.setTopShoulderAngle(cutsSettings.getDouble(MachineConfig.DECK_ANGLE));
					brd.setMaxAngle(cutsSettings.getDouble(MachineConfig.DECK_RAIL_ANGLE));
					// cutsSettings.getDouble(MachineConfig.BOTTOM_ANGLE, new
					// Double(90));
					// cutsSettings.getDouble(MachineConfig.BOTTOM_RAIL_ANGLE,
					// new Double(90));

					Settings speedSettings = machineConfig.getCategory(LanguageResource.getString("SPEEDCATEGORY_STR"));
					brd.setRegularSpeed((int) speedSettings.getDouble(MachineConfig.CUTTING_SPEED));
					brd.setStringerSpeed((int) speedSettings.getDouble(MachineConfig.CUTTING_SPEED_STRINGER));
					brd.setRegularSpeed((int) speedSettings.getDouble(MachineConfig.CUTTING_SPEED_RAIL));
					// speedSettings.getDouble(MachineConfig.CUTTING_SPEED_NOSE_REDUCTION,
					// new Double(0.5));
					// speedSettings.getDouble(MachineConfig.CUTTING_SPEED_TAIL_REDUCTION,
					// new Double(0.5));
					// brd.setNoseLength(speedSettings.getDouble(MachineConfig.CUTTING_SPEED_NOSE_REDUCTION_DIST));
					// brd.setTailLength(speedSettings.getDouble(MachineConfig.CUTTING_SPEED_TAIL_REDUCTION_DIST));
				}
			}
		};

		gcodeMenu.add(gcodeBezier);

		gcodeMenu.addSeparator();

		final AbstractAction gcodeOutline = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEOUTLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HOTWIRECATEGORY_STR");
				Settings hotwireSettings = settings.addCategory(categoryName);
				hotwireSettings.addMeasurement("CuttingSpeed", 50.0,
						LanguageResource.getString("HOTWIRECUTTINGSPEED_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("HOTWIREPARAMETERSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				settingsDialog.dispose();
				if (settingsDialog.wasCancelled()) {
					return;
				}

				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				filename = FileTools.setExtension(filename, "nc");

				BoardCAD.defaultDirectory = file.getPath();

				HotwireToolpathGenerator2 toolpathGenerator = new HotwireToolpathGenerator2(new AbstractCutter() {
					public double[] calcOffset(Point3d point, Vector3d normal, AbstractBoard board) {
						return new double[] { point.x, point.y, point.z };
					}

				}, new GCodeWriter(),
						hotwireSettings.getMeasurement("CuttingSpeed") * UnitUtils.MILLIMETER_PR_CENTIMETER);

				try {
					toolpathGenerator.writeOutline(filename, boardCAD.getCurrentBrd());
				} catch (Exception e) {
					String str = LanguageResource.getString("GCODEOUTLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("GCODEOUTLINEFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		gcodeMenu.add(gcodeOutline);

		final AbstractAction gcodeProfile = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEPROFILE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HOTWIRECATEGORY_STR");
				final Settings hotwireSettings = settings.addCategory(categoryName);
				hotwireSettings.addMeasurement("CuttingSpeed", 50.0,
						LanguageResource.getString("HOTWIRECUTTINGSPEED_STR"));
				hotwireSettings.addMeasurement("AdditionalThickness", 0.0,
						LanguageResource.getString("ADDITIONALTHICKNESS_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("HOTWIREPARAMETERSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				settingsDialog.dispose();
				if (settingsDialog.wasCancelled()) {
					return;
				}

				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				filename = FileTools.setExtension(filename, "nc");

				BoardCAD.defaultDirectory = file.getPath();

				HotwireToolpathGenerator2 toolpathGenerator = new HotwireToolpathGenerator2(new AbstractCutter() {
					public double[] calcOffset(Point3d point, Vector3d normal, AbstractBoard board) {

						// double additionalThickness =
						// hotwireSettings.getMeasurement("AdditionalThickness")*UnitUtils.MILLIMETER_PR_CENTIMETER;

						Point3d offsetPoint = new Point3d(point);
						// Vector3d normalScaled = new Vector3d(normal);
						// normalScaled.scale(additionalThickness);
						// offsetPoint.add(normalScaled);

						return new double[] { offsetPoint.x, offsetPoint.y, offsetPoint.z };
					}

				}, new GCodeWriter(),
						hotwireSettings.getMeasurement("CuttingSpeed") * UnitUtils.MILLIMETER_PR_CENTIMETER,
						hotwireSettings.getMeasurement("AdditionalThickness") * UnitUtils.MILLIMETER_PR_CENTIMETER);

				try {
					toolpathGenerator.writeProfile(filename, boardCAD.getCurrentBrd());
				} catch (Exception e) {
					String str = LanguageResource.getString("GCODEPROFILEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("GCODEPROFILEFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		gcodeMenu.add(gcodeProfile);

		final AbstractAction gcodeDeck = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEDECK_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				MachineConfig config = new MachineConfig();
				config.getPreferences();

				AbstractToolpathGenerator toolpathGenerator = new WidthSplitsToolpathGenerator(new AbstractCutter() {
					public double[] calcOffset(Point3d point, Vector3d normal, AbstractBoard board) {
						return new double[] { point.x, point.y, point.z };
					}

					public double calcSpeed(Point3d point, Vector3d normal, AbstractBoard board,
							boolean isCuttingStringer) {
						return 10;
					}

				}, null, new GCodeWriter(), config);

				try {
					toolpathGenerator.writeToolpath(filename, boardCAD.getCurrentBrd(), null);
				} catch (Exception e) {
					String str = LanguageResource.getString("GCODEDECKFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("GCODEDECKFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		gcodeMenu.add(gcodeDeck);

		final AbstractAction gcodeBottom = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEBOTTOM_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				BoardCAD.defaultDirectory = file.getPath();

				MachineConfig config = new MachineConfig();
				config.getPreferences();

				try {
					AbstractToolpathGenerator toolpathGenerator = new WidthSplitsToolpathGenerator(new AbstractCutter() {
						public double[] calcOffset(Point3d point, Vector3d normal, AbstractBoard board) {
							return new double[] { point.x, point.y, point.z };
						}
	
					}, null, new GCodeWriter(), config) {
						public double calcSpeed(Point3d point, Vector3d normal, AbstractBoard board, boolean isCuttingStringer) {
							return 10;
						}
					};
	
					toolpathGenerator.writeToolpath(filename, boardCAD.getCurrentBrd(), null);
				} catch (Exception e) {
					String str = LanguageResource.getString("GCODEBOTTOMFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("GCODEBOTTOMFAILEDMSG_STR"), JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		gcodeMenu.add(gcodeBottom);

		final JMenu gcodeHWSMenu = new JMenu(LanguageResource.getString("GCODEHWSMENU_STR"));

		final AbstractAction gcodeHWSSTringer = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEHWSSTRINGER_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("CutterDiam", 5.0, LanguageResource.getString("CUTTERDIAMETER_STR"));
				HWSSettings.addMeasurement("JogHeight", 5.0, LanguageResource.getString("JOGHEIGHT_STR"));
				HWSSettings.addMeasurement("JogSpeed", 20.0, LanguageResource.getString("JOGSPEED_STR"));
				HWSSettings.addMeasurement("SinkSpeed", 2.0, LanguageResource.getString("SINKSPEED_STR"));
				HWSSettings.addMeasurement("CutDepth", 0.0, LanguageResource.getString("CUTDEPTH_STR"));
				HWSSettings.addMeasurement("CutSpeed", 2.0, LanguageResource.getString("CUTSPEED_STR"));
				settings.getPreferences();
				String filename = FileTools.removeExtension(boardCAD.getCurrentBrd().getFilename());
				filename.concat("HWSStringer.nc");
				HWSSettings.addFileName("StringerFilename", filename, LanguageResource.getString("FILENAME_STR"));

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("HWSSTRINGERTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				GCodeDraw gdraw = new GCodeDraw(HWSSettings.getFileName("StringerFilename"),
						HWSSettings.getMeasurement("CutterDiam"), HWSSettings.getMeasurement("CutDepth"),
						HWSSettings.getMeasurement("CutSpeed"), HWSSettings.getMeasurement("JogHeight"),
						HWSSettings.getMeasurement("JogSpeed"), HWSSettings.getMeasurement("SinkSpeed"));

				double skinThickness = HWSSettings.getMeasurement("SkinThickness");
				double frameThickness = HWSSettings.getMeasurement("FrameThickness");
				double webbing = HWSSettings.getMeasurement("Webbing");
				double tailOffset = HWSSettings.getMeasurement("TailOffset");
				double noseOffset = HWSSettings.getMeasurement("NoseOffset");
				double distanceToRail = HWSSettings.getMeasurement("DistanceFromRail");

				gdraw.setFlipNormal(true);

				BezierBoardDrawUtil.printProfile(gdraw, 0.0, 0.0, 1.0, 0.0, false,
						BoardCAD.getInstance().getCurrentBrd(), 0.0, skinThickness, false, tailOffset, noseOffset);

				gdraw.setFlipNormal(false);

				PrintHollowWoodTemplates.printStringerWebbing(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), skinThickness, frameThickness, webbing);

				PrintHollowWoodTemplates.printStringerTailPieceCutOut(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);

				PrintHollowWoodTemplates.printStringerNosePieceCutOut(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);

				settingsDialog.dispose();
				gdraw.close();
			}

		};
		gcodeHWSMenu.add(gcodeHWSSTringer);

		final AbstractAction gcodeHWSRibs = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEHWSRIBS_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				LanguageResource.getString("HWSSKINTHICKNESS_STR");
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("CutterDiam", 5.0, LanguageResource.getString("CUTTERDIAMETER_STR"));
				HWSSettings.addMeasurement("JogHeight", 5.0, LanguageResource.getString("JOGHEIGHT_STR"));
				HWSSettings.addMeasurement("JogSpeed", 20.0, LanguageResource.getString("JOGSPEED_STR"));
				HWSSettings.addMeasurement("SinkSpeed", 2.0, LanguageResource.getString("SINKSPEED_STR"));
				HWSSettings.addMeasurement("CutDepth", 0.0, LanguageResource.getString("CUTDEPTH_STR"));
				HWSSettings.addMeasurement("CutSpeed", 2.0, LanguageResource.getString("CUTSPEED_STR"));
				settings.getPreferences();
				String filename = FileTools.removeExtension(boardCAD.getCurrentBrd().getFilename());
				filename.concat("HWSRibs.nc");
				HWSSettings.addFileName("Filename", filename, LanguageResource.getString("FILENAME_STR"));

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("GCODEHWSRAILSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				GCodeDraw gdraw = new GCodeDraw(HWSSettings.getFileName("Filename"),
						HWSSettings.getMeasurement("CutterDiam"), HWSSettings.getMeasurement("CutDepth"),
						HWSSettings.getMeasurement("CutSpeed"), HWSSettings.getMeasurement("JogHeight"),
						HWSSettings.getMeasurement("JogSpeed"), HWSSettings.getMeasurement("SinkSpeed"));

				double skinThickness = HWSSettings.getMeasurement("SkinThickness");
				double frameThickness = HWSSettings.getMeasurement("FrameThickness");
				double webbing = HWSSettings.getMeasurement("Webbing");
				double tailOffset = HWSSettings.getMeasurement("TailOffset");
				double noseOffset = HWSSettings.getMeasurement("NoseOffset");
				double distanceToRail = HWSSettings.getMeasurement("DistanceFromRail");
				double cutterDiam = HWSSettings.getMeasurement("CutterDiam");

				//
				int nrOfCrossSections = (int) ((BoardCAD.getInstance().getCurrentBrd().getLength()
						- 9.0 * UnitUtils.INCH) / UnitUtils.FOOT);
				double crosssectionPos = 0.0;
				double verticalPos = 0.0;
				for (int i = 0; i < nrOfCrossSections; i++) {
					crosssectionPos = (i + 1) * UnitUtils.FOOT;

					PrintHollowWoodTemplates.printCrossSection(gdraw, 0.0, verticalPos, 1.0, 0.0,
							BoardCAD.getInstance().getCurrentBrd(), crosssectionPos, distanceToRail, skinThickness,
							frameThickness, webbing, false);

					PrintHollowWoodTemplates.printCrossSectionWebbing(gdraw, verticalPos, 0.0, 1.0, 0.0,
							BoardCAD.getInstance().getCurrentBrd(), crosssectionPos, distanceToRail, skinThickness,
							frameThickness, webbing, false);

					double verticalStep = BoardCAD.getInstance().getCurrentBrd().getThicknessAtPos(crosssectionPos)
							- skinThickness + (cutterDiam * 2.0);

					verticalPos += verticalStep;
				}

				settingsDialog.dispose();
			}

		};
		gcodeHWSMenu.add(gcodeHWSRibs);

		final AbstractAction gcodeHWSRail = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEHWSRAIL_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				LanguageResource.getString("HWSSKINTHICKNESS_STR");
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("CutterDiam", 5.0, LanguageResource.getString("CUTTERDIAMETER_STR"));
				HWSSettings.addMeasurement("JogHeight", 5.0, LanguageResource.getString("JOGHEIGHT_STR"));
				HWSSettings.addMeasurement("JogSpeed", 20.0, LanguageResource.getString("JOGSPEED_STR"));
				HWSSettings.addMeasurement("SinkSpeed", 2.0, LanguageResource.getString("SINKSPEED_STR"));
				HWSSettings.addMeasurement("CutDepth", 0.0, LanguageResource.getString("CUTDEPTH_STR"));
				HWSSettings.addMeasurement("CutSpeed", 2.0, LanguageResource.getString("CUTSPEED_STR"));
				settings.getPreferences();
				String filename = FileTools.removeExtension(boardCAD.getCurrentBrd().getFilename());
				filename.concat("HWSRibs.nc");
				HWSSettings.addFileName("StringerFilename", filename, LanguageResource.getString("FILENAME_STR"));

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("GCODEHWSRAILSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				GCodeDraw gdraw = new GCodeDraw(HWSSettings.getFileName("StringerFilename"),
						HWSSettings.getMeasurement("CutterDiam"), HWSSettings.getMeasurement("CutDepth"),
						HWSSettings.getMeasurement("CutSpeed"), HWSSettings.getMeasurement("JogHeight"),
						HWSSettings.getMeasurement("JogSpeed"), HWSSettings.getMeasurement("SinkSpeed"));

				double skinThickness = HWSSettings.getMeasurement("SkinThickness");
				double frameThickness = HWSSettings.getMeasurement("FrameThickness");
				double webbing = HWSSettings.getMeasurement("Webbing");
				double tailOffset = HWSSettings.getMeasurement("TailOffset");
				double noseOffset = HWSSettings.getMeasurement("NoseOffset");
				double distanceToRail = HWSSettings.getMeasurement("DistanceFromRail");

				PrintHollowWoodTemplates.printRailWebbing(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);

				PrintHollowWoodTemplates.printRailNotching(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);

				PrintHollowWoodTemplates.printRailNosePieceNotches(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);

				PrintHollowWoodTemplates.printRailTailPieceNotches(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);

				settingsDialog.dispose();
			}

		};
		gcodeHWSMenu.add(gcodeHWSRail);

		final AbstractAction gcodeHWSNosePiece = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEHWSTAILPIECE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("CutterDiam", 5.0, LanguageResource.getString("CUTTERDIAMETER_STR"));
				HWSSettings.addMeasurement("JogHeight", 5.0, LanguageResource.getString("JOGHEIGHT_STR"));
				HWSSettings.addMeasurement("JogSpeed", 20.0, LanguageResource.getString("JOGSPEED_STR"));
				HWSSettings.addMeasurement("SinkSpeed", 2.0, LanguageResource.getString("SINKSPEED_STR"));
				HWSSettings.addMeasurement("CutDepth", 0.0, LanguageResource.getString("CUTDEPTH_STR"));
				HWSSettings.addMeasurement("CutSpeed", 2.0, LanguageResource.getString("CUTSPEED_STR"));
				settings.getPreferences();
				String filename = FileTools.removeExtension(boardCAD.getCurrentBrd().getFilename());
				filename.concat("HWSTail.nc");
				HWSSettings.addFileName("StringerFilename", filename, LanguageResource.getString("FILENAME_STR"));

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("GCODEHWSTAILPIECETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				GCodeDraw gdraw = new GCodeDraw(HWSSettings.getFileName("StringerFilename"),
						HWSSettings.getMeasurement("CutterDiam"), HWSSettings.getMeasurement("CutDepth"),
						HWSSettings.getMeasurement("CutSpeed"), HWSSettings.getMeasurement("JogHeight"),
						HWSSettings.getMeasurement("JogSpeed"), HWSSettings.getMeasurement("SinkSpeed"));

				double skinThickness = HWSSettings.getMeasurement("SkinThickness");
				double frameThickness = HWSSettings.getMeasurement("FrameThickness");
				double webbing = HWSSettings.getMeasurement("Webbing");
				double tailOffset = HWSSettings.getMeasurement("TailOffset");
				double noseOffset = HWSSettings.getMeasurement("NoseOffset");
				double distanceToRail = HWSSettings.getMeasurement("DistanceFromRail");

				PrintHollowWoodTemplates.printNosePiece(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset, false);

				PrintHollowWoodTemplates.printNosePiece(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset, true);

				PrintHollowWoodTemplates.printNosePieceWebbing(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset, false);

				PrintHollowWoodTemplates.printNosePieceWebbing(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset, true);

				settingsDialog.dispose();
			}

		};
		gcodeHWSMenu.add(gcodeHWSNosePiece);

		final AbstractAction gcodeHWSTailPiece = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEHWSNOSEPIECE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("CutterDiam", 5.0, LanguageResource.getString("CUTTERDIAMETER_STR"));
				HWSSettings.addMeasurement("JogHeight", 5.0, LanguageResource.getString("JOGHEIGHT_STR"));
				HWSSettings.addMeasurement("JogSpeed", 20.0, LanguageResource.getString("JOGSPEED_STR"));
				HWSSettings.addMeasurement("SinkSpeed", 2.0, LanguageResource.getString("SINKSPEED_STR"));
				HWSSettings.addMeasurement("CutDepth", 0.0, LanguageResource.getString("CUTDEPTH_STR"));
				HWSSettings.addMeasurement("CutSpeed", 2.0, LanguageResource.getString("CUTSPEED_STR"));
				settings.getPreferences();
				String filename = FileTools.removeExtension(boardCAD.getCurrentBrd().getFilename());
				filename.concat("HWSNose.nc");
				HWSSettings.addFileName("StringerFilename", filename, LanguageResource.getString("FILENAME_STR"));

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("GCODEHWSNOSEPIECETITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				GCodeDraw gdraw = new GCodeDraw(HWSSettings.getFileName("StringerFilename"),
						HWSSettings.getMeasurement("CutterDiam"), HWSSettings.getMeasurement("CutDepth"),
						HWSSettings.getMeasurement("CutSpeed"), HWSSettings.getMeasurement("JogHeight"),
						HWSSettings.getMeasurement("JogSpeed"), HWSSettings.getMeasurement("SinkSpeed"));

				double skinThickness = HWSSettings.getMeasurement("SkinThickness");
				double frameThickness = HWSSettings.getMeasurement("FrameThickness");
				double webbing = HWSSettings.getMeasurement("Webbing");
				double tailOffset = HWSSettings.getMeasurement("TailOffset");
				double noseOffset = HWSSettings.getMeasurement("NoseOffset");
				double distanceToRail = HWSSettings.getMeasurement("DistanceFromRail");

				PrintHollowWoodTemplates.printTailPiece(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset, false);

				PrintHollowWoodTemplates.printTailPiece(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset, true);

				PrintHollowWoodTemplates.printTailPieceWebbing(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset, false);

				PrintHollowWoodTemplates.printTailPieceWebbing(gdraw, 0.0, 0.0, 1.0, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset, true);

				settingsDialog.dispose();
			}

		};
		gcodeHWSMenu.add(gcodeHWSTailPiece);

		final AbstractAction gcodeHWSDeckTemplate = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEHWSDECKTEMPLATE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("CutterDiam", 5.0, LanguageResource.getString("CUTTERDIAMETER_STR"));
				HWSSettings.addMeasurement("JogHeight", 5.0, LanguageResource.getString("JOGHEIGHT_STR"));
				HWSSettings.addMeasurement("JogSpeed", 20.0, LanguageResource.getString("JOGSPEED_STR"));
				HWSSettings.addMeasurement("SinkSpeed", 2.0, LanguageResource.getString("SINKSPEED_STR"));
				HWSSettings.addMeasurement("CutDepth", 0.0, LanguageResource.getString("CUTDEPTH_STR"));
				HWSSettings.addMeasurement("CutSpeed", 2.0, LanguageResource.getString("CUTSPEED_STR"));
				settings.getPreferences();
				String filename = FileTools.removeExtension(boardCAD.getCurrentBrd().getFilename());
				filename.concat("HWSRibs.nc");
				HWSSettings.addFileName("StringerFilename", filename, LanguageResource.getString("FILENAME_STR"));

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("HWSSTRINGERTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				GCodeDraw gdraw = new GCodeDraw(HWSSettings.getFileName("StringerFilename"),
						HWSSettings.getMeasurement("CutterDiam"), HWSSettings.getMeasurement("CutDepth"),
						HWSSettings.getMeasurement("CutSpeed"), HWSSettings.getMeasurement("JogHeight"),
						HWSSettings.getMeasurement("JogSpeed"), HWSSettings.getMeasurement("SinkSpeed"));

				double skinThickness = HWSSettings.getMeasurement("SkinThickness");
				double frameThickness = HWSSettings.getMeasurement("FrameThickness");
				double webbing = HWSSettings.getMeasurement("Webbing");
				double tailOffset = HWSSettings.getMeasurement("TailOffset");
				double noseOffset = HWSSettings.getMeasurement("NoseOffset");
				double distanceToRail = HWSSettings.getMeasurement("DistanceFromRail");

				BezierBoardDrawUtil.printDeckSkinTemplate(gdraw, 0.0, 0.0, 1.0, 0.0, true,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail);

				settingsDialog.dispose();
			}

		};
		gcodeHWSMenu.add(gcodeHWSDeckTemplate);

		final AbstractAction gcodeHWSBottomTemplate = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEHWSBOTTOMTEMPLATE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("CutterDiam", 5.0, LanguageResource.getString("CUTTERDIAMETER_STR"));
				HWSSettings.addMeasurement("JogHeight", 5.0, LanguageResource.getString("JOGHEIGHT_STR"));
				HWSSettings.addMeasurement("JogSpeed", 20.0, LanguageResource.getString("JOGSPEED_STR"));
				HWSSettings.addMeasurement("SinkSpeed", 2.0, LanguageResource.getString("SINKSPEED_STR"));
				HWSSettings.addMeasurement("CutDepth", 0.0, LanguageResource.getString("CUTDEPTH_STR"));
				HWSSettings.addMeasurement("CutSpeed", 2.0, LanguageResource.getString("CUTSPEED_STR"));
				settings.getPreferences();
				String filename = FileTools.removeExtension(boardCAD.getCurrentBrd().getFilename());
				filename.concat("HWSRibs.nc");
				HWSSettings.addFileName("StringerFilename", filename, LanguageResource.getString("FILENAME_STR"));

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("HWSSTRINGERTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				GCodeDraw gdraw = new GCodeDraw(HWSSettings.getFileName("StringerFilename"),
						HWSSettings.getMeasurement("CutterDiam"), HWSSettings.getMeasurement("CutDepth"),
						HWSSettings.getMeasurement("CutSpeed"), HWSSettings.getMeasurement("JogHeight"),
						HWSSettings.getMeasurement("JogSpeed"), HWSSettings.getMeasurement("SinkSpeed"));

				double skinThickness = HWSSettings.getMeasurement("SkinThickness");
				double frameThickness = HWSSettings.getMeasurement("FrameThickness");
				double webbing = HWSSettings.getMeasurement("Webbing");
				double tailOffset = HWSSettings.getMeasurement("TailOffset");
				double noseOffset = HWSSettings.getMeasurement("NoseOffset");
				double distanceToRail = HWSSettings.getMeasurement("DistanceFromRail");

				BezierBoardDrawUtil.printDeckSkinTemplate(gdraw, 0.0, 0.0, 1.0, 0.0, true,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail);

				settingsDialog.dispose();
			}

		};

		gcodeHWSMenu.add(gcodeHWSBottomTemplate);

		final AbstractAction gcodeHWSAllInternalTemplates = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("GCODEHWSALLINTERNALTEMPLATES_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("HWSPARAMETERSCATEGORY_STR");
				Settings HWSSettings = settings.addCategory(categoryName);
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("SkinThickness", 0.4, LanguageResource.getString("HWSSKINTHICKNESS_STR"));
				HWSSettings.addMeasurement("FrameThickness", 0.5, LanguageResource.getString("HWSFRAMETHICKNESS_STR"));
				HWSSettings.addMeasurement("Webbing", 1.5, LanguageResource.getString("HWSWEBBING_STR"));
				HWSSettings.addMeasurement("NoseOffset", 3.5, LanguageResource.getString("HWSNOSEOFFSET_STR"));
				HWSSettings.addMeasurement("TailOffset", 3.5, LanguageResource.getString("HWSTAILOFFSET_STR"));
				HWSSettings.addMeasurement("DistanceFromRail", 3.0,
						LanguageResource.getString("HWSDISTANCEFROMRAIL_STR"));
				HWSSettings.addMeasurement("CutterDiam", 5.0, LanguageResource.getString("CUTTERDIAMETER_STR"));
				HWSSettings.addMeasurement("JogHeight", 5.0, LanguageResource.getString("JOGHEIGHT_STR"));
				HWSSettings.addMeasurement("JogSpeed", 20.0, LanguageResource.getString("JOGSPEED_STR"));
				HWSSettings.addMeasurement("SinkSpeed", 2.0, LanguageResource.getString("SINKSPEED_STR"));
				HWSSettings.addMeasurement("CutDepth", 0.0, LanguageResource.getString("CUTDEPTH_STR"));
				HWSSettings.addMeasurement("CutSpeed", 2.0, LanguageResource.getString("CUTSPEED_STR"));
				settings.getPreferences();
				String filename = FileTools.removeExtension(boardCAD.getCurrentBrd().getFilename());
				filename = filename.concat(" HWSFrame.nc");
				HWSSettings.addFileName("Filename", filename, LanguageResource.getString("FILENAME_STR"));
				HWSSettings.addMeasurement("OffsetX", 0.0, LanguageResource.getString("OFFSET_X_STR"));
				HWSSettings.addMeasurement("OffsetY", 0.0, LanguageResource.getString("OFFSET_Y_STR"));

				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("GCODEHWSALLINTERNALTEMPLATESTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				if (settingsDialog.wasCancelled()) {
					settingsDialog.dispose();
					return;
				}
				settings.putPreferences();

				GCodeDraw gdraw = new GCodeDraw(HWSSettings.getFileName("Filename"),
						HWSSettings.getMeasurement("CutterDiam"), HWSSettings.getMeasurement("CutDepth"),
						HWSSettings.getMeasurement("CutSpeed"), HWSSettings.getMeasurement("JogHeight"),
						HWSSettings.getMeasurement("JogSpeed"), HWSSettings.getMeasurement("SinkSpeed"));

				gdraw.writeComment("HWS Frame");
				gdraw.writeComment(BoardCAD.getInstance().getCurrentBrd().getName() + " - "
						+ BoardCAD.getInstance().getCurrentBrd().getAuthor());

				double skinThickness = HWSSettings.getMeasurement("SkinThickness");
				double frameThickness = HWSSettings.getMeasurement("FrameThickness");
				double webbing = HWSSettings.getMeasurement("Webbing");
				double tailOffset = HWSSettings.getMeasurement("TailOffset");
				double noseOffset = HWSSettings.getMeasurement("NoseOffset");
				double distanceToRail = HWSSettings.getMeasurement("DistanceFromRail");
				double cutterDiam = HWSSettings.getMeasurement("CutterDiam");
				double offsetX = HWSSettings.getMeasurement("OffsetX");
				double offsetY = HWSSettings.getMeasurement("OffsetY");

				// Cut Stringer
				gdraw.writeComment("Stringer");
				gdraw.setFlipNormal(true);

				double x = offsetX;
				double y = offsetY;
				double scale = 1.0;

				/*
				 * PrintHollowWoodTemplates.printStringerWebbing(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), skinThickness, frameThickness,
				 * webbing);
				 *
				 * gdraw.setFlipNormal(false);
				 *
				 *
				 * BezierBoardDrawUtil.printProfile(gdraw, x, y, scale, false,
				 * BoardCAD.getInstance().getCurrentBrd(), 0.0, skinThickness, false,
				 * tailOffset, noseOffset);
				 *
				 * //Debug without offset gdraw.setCutterDiameter(0.0);
				 * BezierBoardDrawUtil.printProfile(gdraw, x, y, scale, false,
				 * BoardCAD.getInstance().getCurrentBrd(), 0.0, skinThickness, false,
				 * tailOffset, noseOffset);
				 *
				 *
				 * PrintHollowWoodTemplates.printStringerTailPieceCutOut(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset);
				 *
				 * PrintHollowWoodTemplates.printStringerNosePieceCutOut(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset);
				 *
				 * y+= BoardCAD.getInstance().getCurrentBrd().getThickness()*10.0; y+=
				 * cutterDiam*2.0;
				 */
				// Print rails twice
				gdraw.writeComment("Rails");

				BezierBoardDrawUtil.printRailTemplate(gdraw, x, y, scale, 0.0, false,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, tailOffset, noseOffset,
						false);

				PrintHollowWoodTemplates.printRailWebbing(gdraw, x, y, scale, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);

				PrintHollowWoodTemplates.printRailNotching(gdraw, x, y, scale, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);

				PrintHollowWoodTemplates.printRailNosePieceNotches(gdraw, x, y, scale, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);

				PrintHollowWoodTemplates.printRailTailPieceNotches(gdraw, x, y, scale, 0.0,
						BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness, frameThickness, webbing,
						tailOffset, noseOffset);
				settingsDialog.dispose();
				gdraw.close();
				return;
				/*
				 * x+= BoardCAD.getInstance().getCurrentBrd().getThickness(); x+=
				 * cutterDiam*2.0;
				 *
				 * BezierBoardDrawUtil.printRailTemplate(gdraw, x, y, scale, false,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * tailOffset, noseOffset, false);
				 *
				 * PrintHollowWoodTemplates.printRailWebbing(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset);
				 *
				 * PrintHollowWoodTemplates.printRailNotching(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset);
				 *
				 * PrintHollowWoodTemplates.printRailNosePieceNotches(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset);
				 *
				 * PrintHollowWoodTemplates.printRailTailPieceNotches(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset);
				 *
				 * //Print cross sections gdraw.writeComment("Ribs"); x+=
				 * BoardCAD.getInstance().getCurrentBrd().getThickness(); x+= cutterDiam*2.0;
				 * y=offsetY;
				 *
				 * int nrOfCrossSections =
				 * (int)((BoardCAD.getInstance().getCurrentBrd().getLength() -
				 * 9.0*UnitUtils.INCH)/UnitUtils.FOOT); double crosssectionPos = 0.0; for(int i
				 * = 0; i < nrOfCrossSections; i++) { crosssectionPos = (i+1)* UnitUtils.FOOT;
				 *
				 * gdraw.writeComment("Rib at " +
				 * UnitUtils.convertLengthToCurrentUnit(crosssectionPos, true));
				 *
				 * PrintHollowWoodTemplates.printCrossSection(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), crosssectionPos, distanceToRail,
				 * skinThickness, frameThickness, webbing, false);
				 *
				 * PrintHollowWoodTemplates.printCrossSectionWebbing(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), crosssectionPos, distanceToRail,
				 * skinThickness, frameThickness, webbing, false);
				 *
				 * double verticalStep =
				 * BoardCAD.getInstance().getCurrentBrd().getThicknessAtPos (crosssectionPos) -
				 * skinThickness + (cutterDiam*2.0);
				 *
				 * y += verticalStep; }
				 *
				 *
				 * //Nose piece gdraw.writeComment("Nose");
				 * PrintHollowWoodTemplates.printNosePiece(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset, false);
				 *
				 * PrintHollowWoodTemplates.printNosePiece(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset, true);
				 *
				 * PrintHollowWoodTemplates.printNosePieceWebbing(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset, false);
				 *
				 * PrintHollowWoodTemplates.printNosePieceWebbing(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset, true);
				 *
				 * //Tail piece gdraw.writeComment("Tail"); y += UnitUtils.FOOT;
				 * PrintHollowWoodTemplates.printTailPiece(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset, false);
				 *
				 * PrintHollowWoodTemplates.printTailPiece(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset, true);
				 *
				 * PrintHollowWoodTemplates.printTailPieceWebbing(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset, false);
				 *
				 * PrintHollowWoodTemplates.printTailPieceWebbing(gdraw, x, y, scale,
				 * BoardCAD.getInstance().getCurrentBrd(), distanceToRail, skinThickness,
				 * frameThickness, webbing, tailOffset, noseOffset, true);
				 *
				 *
				 * settingsDialog.dispose();
				 *
				 * gdraw.close();
				 */
			}

		};

		gcodeHWSMenu.add(gcodeHWSAllInternalTemplates);

		gcodeMenu.add(gcodeHWSMenu);
		

		fileMenu.add(gcodeMenu);

		final JMenu extensionsMenu = new JMenu(LanguageResource.getString("EXTENSIONSMENU_STR"));
		final JMenu atuaCoresMenu = new JMenu(LanguageResource.getString("ATUACORESMENU_STR"));
		final AbstractAction atuaCoresProfile = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ATUACORESPROFILE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				CategorizedSettings settings = new CategorizedSettings();
				String categoryName = LanguageResource.getString("ATUAPARAMETERSCATEGORY_STR");
				Settings atuaSettings = settings.addCategory(categoryName);
				atuaSettings.addBoolean("NoRotation", false, LanguageResource.getString("ATUANOROTATION_STR"));
				SettingDialog settingsDialog = new SettingDialog(settings);
				settingsDialog.setTitle(LanguageResource.getString("ATUAPARAMETERSTITLE_STR"));
				settingsDialog.setModal(true);
				settingsDialog.setVisible(true);
				settingsDialog.dispose();
				if (settingsDialog.wasCancelled()) {
					return;
				}

				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				filename = FileTools.setExtension(filename, "atua");

				BoardCAD.defaultDirectory = file.getPath();

				AtuaCoresToolpathGenerator toolpathGenerator = new AtuaCoresToolpathGenerator();
				try {
					toolpathGenerator.writeProfile(filename, boardCAD.getCurrentBrd(), atuaSettings.getBoolean("NoRotation"));
				} catch (Exception e) {
					String str = LanguageResource.getString("ATUACORESPROFILEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("ATUACORESPROFILEFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		atuaCoresMenu.add(atuaCoresProfile);

		final AbstractAction atuaCoresOutline = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ATUACORESOUTLINE_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser fc = new JFileChooser();

				fc.setCurrentDirectory(new File(BoardCAD.defaultDirectory));

				int returnVal = fc.showSaveDialog(BoardCAD.getInstance().getFrame());
				if (returnVal != JFileChooser.APPROVE_OPTION)
					return;

				File file = fc.getSelectedFile();

				String filename = file.getPath(); // Load and display
				// selection
				if (filename == null)
					return;

				filename = FileTools.setExtension(filename, "atua");

				BoardCAD.defaultDirectory = file.getPath();

				AtuaCoresToolpathGenerator toolpathGenerator = new AtuaCoresToolpathGenerator();
				try {
					toolpathGenerator.writeOutline(filename, boardCAD.getCurrentBrd());
				} catch (Exception e) {
					String str = LanguageResource.getString("ATUACORESOUTLINEFAILEDMSG_STR") + e.toString();
					JOptionPane.showMessageDialog(BoardCAD.getInstance().getFrame(), str,
							LanguageResource.getString("ATUACORESOUTLINEFAILEDTITLE_STR"), JOptionPane.ERROR_MESSAGE);

				}
			}

		};
		atuaCoresMenu.add(atuaCoresOutline);

		extensionsMenu.add(atuaCoresMenu);

		fileMenu.add(extensionsMenu);

		fileMenu.addSeparator();

		final AbstractAction test = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, "Test");
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				/*
				 * CategorizedSettings settings = new CategorizedSettings();
				 * settings.addCategory("test");
				 * settings.getSettings("test").addBoolean("Test1", true, "Test 1");
				 *
				 * SettingDialog dialog = new SettingDialog(settings); dialog.setModal(true);
				 * dialog.setVisible(true); if(!dialog.wasCancelled()) { boolean test1 =
				 * settings.getSettings("test").getBoolean("Test1");
				 * System.out.printf("Test1: %s", test1?"true":"false"); }
				 *
				 * BezierSpline b = boardCAD.getCurrentBrd().getNearestCrossSection(getCurrentBrd
				 * ().getLength()/2.0f).getBezierSpline(); double startAngle =
				 * b.getNormalByS(BezierSpline.ZERO); double endAngle =
				 * b.getNormalByS(BezierSpline.ONE);
				 *
				 * System.out.printf("startAngle: %f endAngle: %f\n",
				 * startAngle/BezierBoard.DEG_TO_RAD, endAngle/BezierBoard.DEG_TO_RAD);
				 *
				 * int steps = 20;
				 *
				 * System.out.printf( "----------------------------------------\n" );
				 * System.out.printf( "----------------------------------------\n" );
				 * System.out. printf("---------------TEST BEGIN---------------\n" );
				 * System.out.printf( "----------------------------------------\n" );
				 *
				 * for(int i = 0; i < steps; i++) { double currentAngle =
				 * b.getNormalByS((double)i/(double)steps); System.out.printf("Angle:%f\n",
				 * currentAngle/BezierBoard.DEG_TO_RAD); }
				 *
				 * System.out.printf( "----------------------------------------\n" );
				 * System.out.printf( "----------------------------------------\n" );
				 *
				 * double angleStep = (endAngle-startAngle) / steps;
				 *
				 * for(int i = 0; i < steps; i++) { System.out.printf(
				 * "----------------------------------------\n" ); double currentAngle =
				 * startAngle + (angleStep*i); double s = b.getSByNormalReverse(currentAngle);
				 * double checkAngle = b.getNormalByS(s); System.out.
				 * printf("Target Angle:%f Result s:%f Angle for s:%f\n" ,
				 * currentAngle/BezierBoard.DEG_TO_RAD, s, checkAngle/BezierBoard.DEG_TO_RAD); }
				 */
				/*
				 * System.out.printf("__________________________________\n"); // Test
				 * SimpleBullnoseCutter SimpleBullnoseCutter cutter = new
				 * SimpleBullnoseCutter(50, 10, 100);
				 * System.out.printf("TEST!!! Cutter diam: 50 corner: 10 height: 100\n");
				 * 
				 * Point3d point = new Point3d(0.0, 0.0, 0.0);
				 * 
				 * Vector<Vector3d> testVectors = new Vector<Vector3d>();
				 * 
				 * testVectors.add(new Vector3d(1.0, 1.0, 1.0)); // testVectors.add(new
				 * Vector3d(1.0,0.0,1.0)); // testVectors.add(new Vector3d(-1.0,0.0,1.0)); //
				 * testVectors.add(new Vector3d(0.0,1.0,1.0)); // testVectors.add(new
				 * Vector3d(0.0,-1.0,1.0)); // testVectors.add(new Vector3d(0.0,-1.0,0.0)); //
				 * testVectors.add(new Vector3d(1.0,0.0,1.0));
				 * 
				 * System.out.printf("\n__________________________________\n"); for (int i = 0;
				 * i < testVectors.size(); i++) { Vector3d vector = testVectors.elementAt(i);
				 * vector.normalize(); System.out.printf("\nTEST!!! Vector%d: %f,%f,%f\n", i,
				 * vector.x, vector.y, vector.z); double[] result = cutter.calcOffset(point,
				 * vector, null); System.out.printf("Result: %f, %f, %f\n", result[0],
				 * result[1], result[2]); }
				 * System.out.printf("\n__________________________________\n");
				 */
			}
		};
		fileMenu.add(test);

		final AbstractAction exit = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("EXIT_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boardCAD.getFrame().dispose();
				// mFrame.setVisible(false);
			}

		};
		fileMenu.add(exit);

		this.add(fileMenu);

		final JMenu editMenu = new JMenu(LanguageResource.getString("EDITMENU_STR"));
		editMenu.setMnemonic(KeyEvent.VK_E);
		final AbstractAction undo = new UndoAction();
		editMenu.add(undo);

		final AbstractAction redo = new RedoAction();
		editMenu.add(redo);

		this.add(editMenu);

		final JMenu viewMenu = new JMenu(LanguageResource.getString("VIEWMENU_STR"));
		viewMenu.setMnemonic(KeyEvent.VK_V);
		
		ItemListener itemListener = new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				boardCAD.getFrame().repaint();
			}
		};

		mIsPaintingGridMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWGRID_STR"));
		mIsPaintingGridMenuItem.setMnemonic(KeyEvent.VK_R);
		mIsPaintingGridMenuItem.setSelected(false);
		mIsPaintingGridMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingGridMenuItem);

		mIsPaintingGhostBrdMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWGHOSTBOARD_STR"));
		mIsPaintingGhostBrdMenuItem.setMnemonic(KeyEvent.VK_G);
		mIsPaintingGhostBrdMenuItem.setSelected(false);
		mIsPaintingGhostBrdMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingGhostBrdMenuItem);

		mIsPaintingOriginalBrdMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWORIGINALBOARD_STR"));
		mIsPaintingOriginalBrdMenuItem.setMnemonic(KeyEvent.VK_O);
		mIsPaintingOriginalBrdMenuItem.setSelected(false);
		mIsPaintingOriginalBrdMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingOriginalBrdMenuItem);

		mIsPaintingControlPointsMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWCONTROLPOINTS_STR"));
		mIsPaintingControlPointsMenuItem.setMnemonic(KeyEvent.VK_C);
		mIsPaintingControlPointsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0));// ,
																									// KeyEvent.CTRL_DOWN_MASK));
		mIsPaintingControlPointsMenuItem.setSelected(true);
		mIsPaintingControlPointsMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingControlPointsMenuItem);

		mIsPaintingNonActiveCrossSectionsMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWNONEACTIVECROSSECTIONS_STR"));
		mIsPaintingNonActiveCrossSectionsMenuItem.setMnemonic(KeyEvent.VK_N);
		mIsPaintingNonActiveCrossSectionsMenuItem.setSelected(true);
		mIsPaintingNonActiveCrossSectionsMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingNonActiveCrossSectionsMenuItem);

		mIsPaintingGuidePointsMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWGUIDEPOINTS_STR"));
		mIsPaintingGuidePointsMenuItem.setMnemonic(KeyEvent.VK_P);
		mIsPaintingGuidePointsMenuItem.setSelected(true);
		mIsPaintingGuidePointsMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingGuidePointsMenuItem);

		mIsPaintingCurvatureMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWCURVATURE_STR"));
		mIsPaintingCurvatureMenuItem.setMnemonic(KeyEvent.VK_V);
		mIsPaintingCurvatureMenuItem.setSelected(true);
		mIsPaintingCurvatureMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingCurvatureMenuItem);

		mIsPaintingVolumeDistributionMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWVOLUMEDISTRIBUTION_STR"));
		mIsPaintingVolumeDistributionMenuItem.setMnemonic(KeyEvent.VK_V);
		mIsPaintingVolumeDistributionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));
		mIsPaintingVolumeDistributionMenuItem.setSelected(false);
		mIsPaintingVolumeDistributionMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingVolumeDistributionMenuItem);

		mIsPaintingCenterOfMassMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWCENTEROFMASS_STR"));
		mIsPaintingCenterOfMassMenuItem.setMnemonic(KeyEvent.VK_M);
		mIsPaintingCenterOfMassMenuItem.setSelected(false);
		mIsPaintingCenterOfMassMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingCenterOfMassMenuItem);

		mIsPaintingSlidingInfoMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWSLIDINGINFO_STR"));
		mIsPaintingSlidingInfoMenuItem.setMnemonic(KeyEvent.VK_S);
		mIsPaintingSlidingInfoMenuItem.setSelected(true);
		mIsPaintingSlidingInfoMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingSlidingInfoMenuItem);

		mIsPaintingSlidingCrossSectionMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWSLIDINGCROSSECTION_STR"));
		mIsPaintingSlidingCrossSectionMenuItem.setMnemonic(KeyEvent.VK_X);
		mIsPaintingSlidingCrossSectionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
		mIsPaintingSlidingCrossSectionMenuItem.setSelected(true);
		mIsPaintingSlidingCrossSectionMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingSlidingCrossSectionMenuItem);

		mIsPaintingFinsMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWFINS_STR"));
		mIsPaintingFinsMenuItem.setMnemonic(KeyEvent.VK_F);
		mIsPaintingFinsMenuItem.setSelected(true);
		mIsPaintingFinsMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingFinsMenuItem);

		mIsPaintingBackgroundImageMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWBACKGROUNDIMAGE_STR"));
		mIsPaintingBackgroundImageMenuItem.setMnemonic(KeyEvent.VK_B);
		mIsPaintingBackgroundImageMenuItem.setSelected(false);
		mIsPaintingBackgroundImageMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingBackgroundImageMenuItem);

		mIsAntialiasingMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("USEANTIALIASING_STR"));
		mIsAntialiasingMenuItem.setMnemonic(KeyEvent.VK_A);
		mIsAntialiasingMenuItem.setSelected(true);
		mIsAntialiasingMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsAntialiasingMenuItem);

		mIsPaintingBaseLineMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWBASELINE_STR"));
		mIsPaintingBaseLineMenuItem.setMnemonic(KeyEvent.VK_L);
		mIsPaintingBaseLineMenuItem.setSelected(false);
		mIsPaintingBaseLineMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingBaseLineMenuItem);

		mIsPaintingCenterLineMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWCENTERLINE_STR"));
		mIsPaintingCenterLineMenuItem.setMnemonic(KeyEvent.VK_J);
		mIsPaintingCenterLineMenuItem.setSelected(true);
		mIsPaintingCenterLineMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingCenterLineMenuItem);

		mIsPaintingOverCurveMesurementsMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWOVERBOTTOMCURVEMEASUREMENTS_STR"));
		mIsPaintingOverCurveMesurementsMenuItem.setMnemonic(KeyEvent.VK_D);
		mIsPaintingOverCurveMesurementsMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift V"));
		mIsPaintingOverCurveMesurementsMenuItem.setSelected(true);
		mIsPaintingOverCurveMesurementsMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingOverCurveMesurementsMenuItem);

		mIsPaintingMomentOfInertiaMenuItem = new JCheckBoxMenuItem(
				LanguageResource.getString("SHOWMOMENTOFINERTIA_STR"));
		mIsPaintingMomentOfInertiaMenuItem.setMnemonic(KeyEvent.VK_D);
		// mIsPaintingMomentOfInertiaMenuItem.setAccelerator(
		// KeyStroke.getKeyStroke("shift V") );
		mIsPaintingMomentOfInertiaMenuItem.setSelected(false);
		mIsPaintingMomentOfInertiaMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingMomentOfInertiaMenuItem);

		mIsPaintingCrossectionsPositionsMenuItem = new JCheckBoxMenuItem(
				LanguageResource.getString("SHOWCROSSECTIONSPOSITIONS_STR"));
		mIsPaintingCrossectionsPositionsMenuItem.setMnemonic(KeyEvent.VK_D);
		// mIsPaintingCrossectionsPositionsMenuItem.setAccelerator(
		// KeyStroke.getKeyStroke("shift V") );
		mIsPaintingCrossectionsPositionsMenuItem.setSelected(false);
		mIsPaintingCrossectionsPositionsMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingCrossectionsPositionsMenuItem);

		mIsPaintingFlowlinesMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWFLOWLINES_STR"));
		mIsPaintingFlowlinesMenuItem.setMnemonic(KeyEvent.VK_D);
		// mIsPaintingFlowlinesMenuItem.setAccelerator(
		// KeyStroke.getKeyStroke("shift V") );
		mIsPaintingFlowlinesMenuItem.setSelected(false);
		mIsPaintingFlowlinesMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingFlowlinesMenuItem);

		mIsPaintingApexlineMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWAPEXLINE_STR"));
		mIsPaintingApexlineMenuItem.setMnemonic(KeyEvent.VK_D);
		// mIsPaintingApexlineMenuItem.setAccelerator(
		// KeyStroke.getKeyStroke("shift V") );
		mIsPaintingApexlineMenuItem.setSelected(false);
		mIsPaintingApexlineMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingApexlineMenuItem);

		mIsPaintingTuckUnderLineMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWTUCKUNDERLINE_STR"));
		mIsPaintingTuckUnderLineMenuItem.setMnemonic(KeyEvent.VK_D);
		// mIsPaintingTuckUnderLineMenuItem.setAccelerator(
		// KeyStroke.getKeyStroke("shift V") );
		mIsPaintingTuckUnderLineMenuItem.setSelected(false);
		mIsPaintingTuckUnderLineMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingTuckUnderLineMenuItem);

		mIsPaintingFootMarksMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWFOOTMARKS_STR"));
		mIsPaintingFootMarksMenuItem.setMnemonic(KeyEvent.VK_D);
		mIsPaintingFootMarksMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift F"));
		mIsPaintingFootMarksMenuItem.setSelected(false);
		mIsPaintingFootMarksMenuItem.addItemListener(itemListener);
		viewMenu.add(mIsPaintingFootMarksMenuItem);

		mUseFillMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("USEFILL_STR"));
		// mUseFillMenuItem.setMnemonic(KeyEvent.VK_D);
		// mUseFillMenuItem.setAccelerator(KeyStroke.getKeyStroke("shift F") );
		mUseFillMenuItem.setSelected(true);
		mUseFillMenuItem.addItemListener(itemListener);
		viewMenu.add(mUseFillMenuItem);

		this.add(viewMenu);

		final JMenu crossSectionsMenu = new JMenu(LanguageResource.getString("CROSSECTIONSMENU_STR"));
		crossSectionsMenu.setMnemonic(KeyEvent.VK_C);

		NextCrossSectionAction nextCrossSection = new NextCrossSectionAction();
		crossSectionsMenu.add(nextCrossSection);

		PreviousCrossSectionAction previousCrossSection = new PreviousCrossSectionAction();
		crossSectionsMenu.add(previousCrossSection);
		
		crossSectionsMenu.addSeparator();

		final AbstractAction addCrossSection = new AddCrossSectionAction();
		crossSectionsMenu.add(addCrossSection);

		final AbstractAction moveCrossSection = new MoveCrossSectionAction();
		crossSectionsMenu.add(moveCrossSection);

		final AbstractAction deleteCrossSection = new DeleteCrossSectionAction();
		crossSectionsMenu.add(deleteCrossSection);
		crossSectionsMenu.addSeparator();

		final AbstractAction copyCrossSection = new CopyCrossSectionAction();
		crossSectionsMenu.add(copyCrossSection);

		final AbstractAction pasteCrossSection = new PasteCrossSectionAction();
		crossSectionsMenu.add(pasteCrossSection);

		this.add(crossSectionsMenu);

		final JMenu boardMenu = new JMenu(LanguageResource.getString("BOARDMENU_STR"));
		boardMenu.setMnemonic(KeyEvent.VK_B);

		final AbstractAction scale = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("SCALECURRENT_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				BrdScaleCommand cmd = new BrdScaleCommand(boardCAD.getSelectedEdit());
				cmd.execute();

				boardCAD.getFrame().repaint();
			}

		};
		boardMenu.add(scale);

		final AbstractAction scaleGhost = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("SCALEGHOST_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boardCAD.getGhostBrd().scale(boardCAD.getCurrentBrd().getLength(), boardCAD.getCurrentBrd().getCenterWidth(),
						boardCAD.getCurrentBrd().getThickness());

				boardCAD.getFrame().repaint();
			}

		};
		boardMenu.add(scaleGhost);

		final AbstractAction info = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("INFO_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				BoardInfo dialog = new BoardInfo(boardCAD.getCurrentBrd());
				dialog.setModal(true);
				dialog.setResizable(false);
				// dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.setVisible(true);
				dialog.dispose();
				boardCAD.getFrame().repaint();
			}

		};
		boardMenu.addSeparator();
		boardMenu.add(info);

		final AbstractAction fins = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("FINS_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {

				BoardFinsDialog dialog = new BoardFinsDialog(boardCAD.getCurrentBrd());
				dialog.setModal(true);
				// dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.setVisible(true);
				dialog.dispose();
				boardCAD.getFrame().repaint();
			}

		};
		boardMenu.add(fins);

		boardMenu.addSeparator();

		final GuidePointsAction guidePoints = new GuidePointsAction();
		boardMenu.add(guidePoints);

		boardMenu.addSeparator();

		final AbstractAction weightCalc = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("WEIGHTCALC_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boardCAD.showWeightCalculatorDialog();
			}

		};

		boardMenu.add(weightCalc);

		final AbstractAction tailDesigner = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("TAILDESIGNER_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				TailDesignerDialog dialog = new TailDesignerDialog(boardCAD.getCurrentBrd());
				dialog.setModal(true);
				dialog.setResizable(false);
				dialog.setVisible(true);
				dialog.dispose();
				boardCAD.getFrame().repaint();
			}

		};

		boardMenu.add(tailDesigner);

		final AbstractAction flip = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("FLIP_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boardCAD.setFlipped(!boardCAD.isFlipped());
			}

		};
		boardMenu.addSeparator();
		boardMenu.add(flip);

		this.add(boardMenu);

		final JMenu miscMenu = new JMenu(LanguageResource.getString("MISCMENU_STR"));
		miscMenu.setMnemonic(KeyEvent.VK_M);

		final AbstractAction settings = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("SETTINGS_STR"));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				BoardCADSettingsDialog dlg = new BoardCADSettingsDialog();
				dlg.setModal(true);
				dlg.setVisible(true);

				BoardCAD.getInstance().getFrame().repaint();
			}

		};

		miscMenu.add(settings);

		final AbstractAction language = new LanguageSelectionAction();
		miscMenu.add(language);

		miscMenu.addSeparator();

		final JMenu crossSectionInterpolationMenu = new JMenu(
				LanguageResource.getString("CROSSECTIONINTERPOLATIONMENU_STR"));
		mControlPointInterpolationButton = new JRadioButtonMenuItem(
				LanguageResource.getString("CROSSECTIONINTERPOLATIONTYPECONTROLPOINT_STR"));
		mSBlendInterpolationButton = new JRadioButtonMenuItem(
				LanguageResource.getString("CROSSECTIONINTERPOLATIONTYPESBLEND_STR"));

		ActionListener interpolationTypeListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boardCAD.setBoardChangedFor3D();
				if (isShowBesizer3DModelSelected()) {
					boardCAD.updateBezier3DModel();
				}
				BezierBoard current = boardCAD.getCurrentBrd();
				if (current != null) {
					current.setInterpolationType(boardCAD.getCrossSectionInterpolationType());
				}
			}
		};

		mControlPointInterpolationButton.addActionListener(interpolationTypeListener);
		mSBlendInterpolationButton.addActionListener(interpolationTypeListener);

		final ButtonGroup interpolationButtonGroup = new ButtonGroup();
		interpolationButtonGroup.add(mControlPointInterpolationButton);
		interpolationButtonGroup.add(mSBlendInterpolationButton);

		crossSectionInterpolationMenu.add(mControlPointInterpolationButton);
		crossSectionInterpolationMenu.add(mSBlendInterpolationButton);

		miscMenu.add(crossSectionInterpolationMenu);

		this.add(miscMenu);


		final JMenu menuRender = new JMenu(LanguageResource.getString("RENDERMENU_STR"));
		menuRender.setMnemonic(KeyEvent.VK_R);

		mShowRenderInwireframe = new JCheckBoxMenuItem(LanguageResource.getString("SHOWWIREFRAME_STR"));
		mShowRenderInwireframe.setMnemonic(KeyEvent.VK_S);
		mShowRenderInwireframe.setSelected(false);
		mShowRenderInwireframe.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				Appearance a = new Appearance();
				PolygonAttributes pa = new PolygonAttributes();
				if (mShowRenderInwireframe.isSelected()) {
					Color3f ambient = new Color3f(0.1f, 0.5f, 0.1f);
					Color3f emissive = new Color3f(0.0f, 0.0f, 0.0f);
					Color3f diffuse = new Color3f(0.1f, 1.0f, 0.1f);
					Color3f specular = new Color3f(0.9f, 1.0f, 0.9f);

					// Set up the material properties
					a.setMaterial(new Material(ambient, emissive, diffuse, specular, 115.0f));

					pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
					pa.setCullFace(PolygonAttributes.CULL_BACK); // experiment
																	// with it
					a.setPolygonAttributes(pa);
				} else {
					Color3f ambient = new Color3f(0.4f, 0.4f, 0.45f);
					Color3f emissive = new Color3f(0.0f, 0.0f, 0.0f);
					Color3f diffuse = new Color3f(0.8f, 0.8f, 0.8f);
					Color3f specular = new Color3f(1.0f, 1.0f, 1.0f);

					// Set up the material properties
					a.setMaterial(new Material(ambient, emissive, diffuse, specular, 115.0f));

					pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
					pa.setCullFace(PolygonAttributes.CULL_BACK); // experiment
																	// with it
					a.setPolygonAttributes(pa);
				}

				boardCAD.set3DModelApperance(a);

			}

		});
		menuRender.add(mShowRenderInwireframe);

		mShowBezier3DModelMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("SHOWBEZIER3DMODEL_STR"));
		mShowBezier3DModelMenuItem.setMnemonic(KeyEvent.VK_B);
		mShowBezier3DModelMenuItem.setSelected(true);
		ActionListener showBezier3DListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				boolean selected = mShowBezier3DModelMenuItem.getModel().isSelected();
				boardCAD.setShowBezierModel(selected);

			}
		};
		mShowBezier3DModelMenuItem.addActionListener(showBezier3DListener);
		menuRender.add(mShowBezier3DModelMenuItem);
		
		mAutoUpdate3DModelMenuItem = new JCheckBoxMenuItem(LanguageResource.getString("AUTOUPDATE3DMODEL_STR"));
		mAutoUpdate3DModelMenuItem.setMnemonic(KeyEvent.VK_A);
		mAutoUpdate3DModelMenuItem.setSelected(true);
		menuRender.add(mAutoUpdate3DModelMenuItem);

		final AbstractAction manualRefresh3DModel = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("MANUALUPDATE3DMODEL_STR"));
				this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boardCAD.updateBezier3DModel(true);
			}

		};
		menuRender.add(manualRefresh3DModel);
	
		final AbstractAction reset3DModel = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("RESET3DMODEL_STR"));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				boardCAD.resetBezier3DModel();
			}

		};
		menuRender.add(reset3DModel);

		
		
		this.add(menuRender);

		final JMenu helpMenu = new JMenu(LanguageResource.getString("HELPMENU_STR"));
		helpMenu.setMnemonic(KeyEvent.VK_H);

		final AbstractAction onlineHelp = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ONLINEHELP_STR"));
				this.putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("/boardcad/icons/Help16.gif")));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				BrowserControl.displayURL("https://havardnj.github.io/boardcad-le/");
			}

		};
		helpMenu.add(onlineHelp);

		final AbstractAction about = new AbstractAction() {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, LanguageResource.getString("ABOUT_STR"));
				this.putValue(Action.SMALL_ICON,
						new ImageIcon(getClass().getResource("/boardcad/icons/Information16.gif")));
				// this.putValue(Action.ACCELERATOR_KEY,
				// KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0));
			};

			@Override
			public void actionPerformed(ActionEvent arg0) {
				AboutBox box = new AboutBox();
				box.setModal(true);
				box.setVisible(true);
				box.dispose();
			}

		};
		helpMenu.add(about);

		this.add(helpMenu);
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {

		final String cmdStr = e.getActionCommand();
		if (cmdStr == LanguageResource.getString("PRINTOUTLINE_STR")) {
			CategorizedSettings settings = new CategorizedSettings();
			String categoryName = LanguageResource.getString("PRINTOUTLINEPARAMETERSCATEGORY_STR");
			Settings printOutlineSettings = settings.addCategory(categoryName);
			printOutlineSettings.addBoolean("PrintGrid", true, LanguageResource.getString("PRINTGRID_STR"));
			printOutlineSettings.addBoolean("OverCurve", false, LanguageResource.getString("PRINTOVERCURVE_STR"));
			SettingDialog settingsDialog = new SettingDialog(settings);
			settingsDialog.setTitle(LanguageResource.getString("PRINTOUTLINEPARAMETERSTITLE_STR"));
			settingsDialog.setModal(true);
			settingsDialog.setVisible(true);
			settingsDialog.dispose();
			if (settingsDialog.wasCancelled()) {
				return;
			}

			PrintBrd printBrd = new PrintBrd();
			printBrd.printOutline(printOutlineSettings.getBoolean("PrintGrid"),
					printOutlineSettings.getBoolean("OverCurve"));

		} else if (cmdStr == LanguageResource.getString("PRINTSPINTEMPLATE_STR")) {
			CategorizedSettings settings = new CategorizedSettings();
			String categoryName = LanguageResource.getString("PRINTSPINTEMPLATEPARAMETERSCATEGORY_STR");
			Settings printOutlineSettings = settings.addCategory(categoryName);
			printOutlineSettings.addBoolean("PrintGrid", true, LanguageResource.getString("PRINTGRID_STR"));
			printOutlineSettings.addBoolean("OverCurve", false, LanguageResource.getString("OVERCURVE_STR"));
			SettingDialog settingsDialog = new SettingDialog(settings);
			settingsDialog.setTitle(LanguageResource.getString("PRINTSPINTEMPLATEPARAMETERSTITLE_STR"));
			settingsDialog.setModal(true);
			settingsDialog.setVisible(true);
			settingsDialog.dispose();
			if (settingsDialog.wasCancelled()) {
				return;
			}

			PrintBrd printBrd = new PrintBrd();
			printBrd.printSpinTemplate(printOutlineSettings.getBoolean("PrintGrid"),
					printOutlineSettings.getBoolean("OverCurve"));
		} else if (cmdStr == LanguageResource.getString("PRINTPROFILE_STR")) {
			PrintBrd printBrd = new PrintBrd();
			printBrd.printProfile();
		} else if (cmdStr == LanguageResource.getString("PRINTCROSSECTION_STR")) {
			PrintBrd printBrd = new PrintBrd();
			printBrd.printSlices();
			// } else if (cmdStr ==
			// LanguageResource.getString("PRINTSPECSHEET_STR")) {
			// mPrintBrd.printSpecSheet();
			// } else if (cmdStr == LanguageResource.getString("VIEW3D_STR")) {
			// design_panel.view_3d();
			// } else if (cmdStr == LanguageResource.getString("EDITNURBS_STR"))
			// {
			// design_panel.view_all();
			// design_panel.fit_all();
		}

	}
	
	public boolean isShowBesizer3DModelSelected() {
		return mShowBezier3DModelMenuItem.getModel().isSelected();
	}
	

	public void getPreferences() {
		// Preference keys for this package

		final Preferences prefs = Preferences.userNodeForPackage(BoardCAD.class);

		mIsPaintingGridMenuItem.setSelected(prefs.getBoolean("mIsPaintingGridMenuItem", false));
		mIsPaintingOriginalBrdMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingOriginalBrdMenuItem", mIsPaintingOriginalBrdMenuItem.isSelected()));
		mIsPaintingGhostBrdMenuItem
				.setSelected(prefs.getBoolean("mIsPaintingGhostBrdMenuItem", mIsPaintingGhostBrdMenuItem.isSelected()));
		mIsPaintingControlPointsMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingControlPointsMenuItem", mIsPaintingControlPointsMenuItem.isSelected()));
		mIsPaintingNonActiveCrossSectionsMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingNonActiveCrossSectionsMenuItem", mIsPaintingNonActiveCrossSectionsMenuItem.isSelected()));
		mIsPaintingGuidePointsMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingGuidePointsMenuItem", mIsPaintingGuidePointsMenuItem.isSelected()));
		mIsPaintingCurvatureMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingCurvatureMenuItem", mIsPaintingCurvatureMenuItem.isSelected()));
		mIsPaintingVolumeDistributionMenuItem.setSelected(prefs.getBoolean("mIsPaintingVolumeDistributionMenuItem",
				mIsPaintingVolumeDistributionMenuItem.isSelected()));
		mIsPaintingCenterOfMassMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingCenterOfMassMenuItem", mIsPaintingCenterOfMassMenuItem.isSelected()));
		mIsPaintingSlidingInfoMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingSlidingInfoMenuItem", mIsPaintingSlidingInfoMenuItem.isSelected()));
		mIsPaintingSlidingCrossSectionMenuItem.setSelected(prefs.getBoolean("mIsPaintingSlidingCrossSectionMenuItem",
				mIsPaintingSlidingCrossSectionMenuItem.isSelected()));
		mIsPaintingFinsMenuItem
				.setSelected(prefs.getBoolean("mIsPaintingFinsMenuItem", mIsPaintingFinsMenuItem.isSelected()));
		mIsPaintingBackgroundImageMenuItem.setSelected(prefs.getBoolean("mIsPaintingBackgroundImageMenuItem",
				mIsPaintingBackgroundImageMenuItem.isSelected()));
		mIsAntialiasingMenuItem
				.setSelected(prefs.getBoolean("mIsAntialiasingMenuItem", mIsAntialiasingMenuItem.isSelected()));
		mUseFillMenuItem.setSelected(prefs.getBoolean("mUseFillMenuItem", mUseFillMenuItem.isSelected()));
		mIsPaintingBaseLineMenuItem
				.setSelected(prefs.getBoolean("mIsPaintingBaseLineMenuItem", mIsPaintingBaseLineMenuItem.isSelected()));
		mIsPaintingCenterLineMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingCenterLineMenuItem", mIsPaintingCenterLineMenuItem.isSelected()));
		mIsPaintingOverCurveMesurementsMenuItem.setSelected(prefs.getBoolean("mIsPaintingoverCurveMesurementsMenuItem",
				mIsPaintingOverCurveMesurementsMenuItem.isSelected()));
		mIsPaintingMomentOfInertiaMenuItem.setSelected(prefs.getBoolean("mIsPaintingMomentOfInertiaMenuItem",
				mIsPaintingMomentOfInertiaMenuItem.isSelected()));

		mIsPaintingCrossectionsPositionsMenuItem.setSelected(prefs.getBoolean(
				"mIsPaintingCrossectionsPositionsMenuItem", mIsPaintingCrossectionsPositionsMenuItem.isSelected()));

		mIsPaintingFlowlinesMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingFlowlinesMenuItem", mIsPaintingFlowlinesMenuItem.isSelected()));

		mIsPaintingApexlineMenuItem
				.setSelected(prefs.getBoolean("mIsPaintingApexlineMenuItem", mIsPaintingApexlineMenuItem.isSelected()));

		mIsPaintingTuckUnderLineMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingTuckUnderLineMenuItem", mIsPaintingTuckUnderLineMenuItem.isSelected()));
		mIsPaintingFootMarksMenuItem.setSelected(
				prefs.getBoolean("mIsPaintingFootMarksMenuItem", mIsPaintingFootMarksMenuItem.isSelected()));

		BoardCAD.mPrintMarginLeft = prefs.getDouble("mPrintMarginLeft", BoardCAD.mPrintMarginLeft);
		BoardCAD.mPrintMarginRight = prefs.getDouble("mPrintMarginRight", BoardCAD.mPrintMarginRight);
		BoardCAD.mPrintMarginTop = prefs.getDouble("mPrintMarginTop", BoardCAD.mPrintMarginTop);
		BoardCAD.mPrintMarginBottom = prefs.getDouble("mPrintMarginBottom", BoardCAD.mPrintMarginBottom);

		BoardCAD boardCAD = BoardCAD.getInstance();
		final int type = prefs.getInt("CrossSectionInterpolationType", boardCAD.getCrossSectionInterpolationTypeAsInt());
		boardCAD.setCrossSectionInterpolationTypeFromInt(type);

		for (int i = 8; i >= 0; i--) {
			String id = "mRecentBrdFiles" + i;
			String string = prefs.get(id, "");
			if (string == null || string.compareTo("") == 0)
				continue;

			addRecentBoardFile(string);
		}

	}
	

	public void putPreferences() {
		// Preference keys for this package
		final Preferences prefs = Preferences.userNodeForPackage(BoardCAD.class);

		prefs.put("defaultDirectory", BoardCAD.defaultDirectory);
		prefs.putBoolean("mIsPaintingGridMenuItem", mIsPaintingGridMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingOriginalBrdMenuItem", mIsPaintingOriginalBrdMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingGhostBrdMenuItem", mIsPaintingGhostBrdMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingControlPointsMenuItem", mIsPaintingControlPointsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingNonActiveCrossSectionsMenuItem",
				mIsPaintingNonActiveCrossSectionsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingGuidePointsMenuItem", mIsPaintingGuidePointsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingCurvatureMenuItem", mIsPaintingCurvatureMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingVolumeDistributionMenuItem", mIsPaintingVolumeDistributionMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingCenterOfMassMenuItem", mIsPaintingCenterOfMassMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingSlidingInfoMenuItem", mIsPaintingSlidingInfoMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingSlidingCrossSectionMenuItem", mIsPaintingSlidingCrossSectionMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingFinsMenuItem", mIsPaintingFinsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingBackgroundImageMenuItem", mIsPaintingCrossectionsPositionsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingBaseLineMenuItem", mIsPaintingBaseLineMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingCenterLineMenuItem", mIsPaintingCenterLineMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingOverCurveMesurementsMenuItem",
				mIsPaintingOverCurveMesurementsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingMomentOfInertiaMenuItem", mIsPaintingMomentOfInertiaMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingCrossectionsPositionsMenuItem",
				mIsPaintingCrossectionsPositionsMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingFlowlinesMenuItem", mIsPaintingFlowlinesMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingApexlineMenuItem", mIsPaintingApexlineMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingTuckUnderLineMenuItem", mIsPaintingTuckUnderLineMenuItem.isSelected());
		prefs.putBoolean("mIsPaintingFootMarksMenuItem", mIsPaintingFootMarksMenuItem.isSelected());
		prefs.putBoolean("mIsAntialiasingMenuItem", mIsAntialiasingMenuItem.isSelected());
		prefs.putBoolean("mUseFillMenuItem", mUseFillMenuItem.isSelected());
		prefs.putInt("CrossSectionInterpolationType", BoardCAD.getInstance().getCrossSectionInterpolationTypeAsInt());

		prefs.putDouble("mPrintMarginLeft", BoardCAD.mPrintMarginLeft);
		prefs.putDouble("mPrintMarginRight", BoardCAD.mPrintMarginRight);
		prefs.putDouble("mPrintMarginTop", BoardCAD.mPrintMarginTop);
		prefs.putDouble("mPrintMarginBottom", BoardCAD.mPrintMarginBottom);

		for (int i = 0; i < mRecentBrdFilesMenu.getMenuComponentCount(); i++) {
			String str = ((JMenuItem) mRecentBrdFilesMenu.getMenuComponent(i)).getText();
			String id = "mRecentBrdFiles" + i;
			prefs.put(id, str);
		}
	}

	public void addRecentBoardFile(final String filename) {
		BoardCAD boardCAD = BoardCAD.getInstance();
		
		// Remove item if already exists
		for (int i = 0; i < mRecentBrdFilesMenu.getMenuComponentCount(); i++) {
			JMenuItem menuItem = (JMenuItem) mRecentBrdFilesMenu.getMenuComponent(i);
			String str = menuItem.getText();

			if (str.compareTo(filename) == 0) {
				mRecentBrdFilesMenu.remove(menuItem);
				break;
			}
		}

		final BoardLoadAction loadRecentBrd = new BoardLoadAction(boardCAD.getCurrentBrd(), boardCAD.getOriginalBrd()) {
			static final long serialVersionUID = 1L;
			{
				this.putValue(Action.NAME, filename);
			};

			@Override
			public void actionPerformed(ActionEvent event) {
				int r = boardCAD.saveChangedBoard();
				if (r == -1 || r == 2) // closed dialog or cancel button pressed
					return;

				String filename = (String) this.getValue(Action.NAME);

				super.load(filename);

				addRecentBoardFile(filename);

				boardCAD.fitAll();
				boardCAD.onBrdChanged();
				boardCAD.onControlPointChanged();
				boardCAD.setBoardChanged(false);
				boolean selected = mShowBezier3DModelMenuItem.getModel().isSelected();
				if (selected) {
					boardCAD.updateBezier3DModel();
				}
				boardCAD.redraw();
			}
		};

		mRecentBrdFilesMenu.add(new JMenuItem(loadRecentBrd), 0);

		while (mRecentBrdFilesMenu.getMenuComponentCount() > 8) {
			mRecentBrdFilesMenu.remove(mRecentBrdFilesMenu.getMenuComponentCount() - 1);
		}
	}

	public boolean isAutoUpdate3DModelSelected() {
		return mAutoUpdate3DModelMenuItem.getModel().isSelected();
	}
	

	public boolean isPaintingOriginalBrd() {
		return mIsPaintingOriginalBrdMenuItem.isSelected();
	}

	public boolean isPaintingGhostBrd() {
		return mIsPaintingGhostBrdMenuItem.isSelected();
	}

	public boolean isPaintingGrid() {
		return mIsPaintingGridMenuItem.isSelected();
	}

	public boolean isPaintingControlPoints() {
		return mIsPaintingControlPointsMenuItem.isSelected();
	}

	public boolean isPaintingNonActiveCrossSections() {
		return mIsPaintingNonActiveCrossSectionsMenuItem.isSelected();
	}

	public boolean isPaintingGuidePoints() {
		return mIsPaintingGuidePointsMenuItem.isSelected();
	}

	public boolean isPaintingCurvature() {
		return mIsPaintingCurvatureMenuItem.isSelected();
	}

	public boolean isPaintingVolumeDistribution() {
		return mIsPaintingVolumeDistributionMenuItem.isSelected();
	}

	public boolean isPaintingCenterOfMass() {
		return mIsPaintingCenterOfMassMenuItem.isSelected();
	}

	public boolean isPaintingSlidingInfo() {
		return mIsPaintingSlidingInfoMenuItem.isSelected();
	}

	public boolean isPaintingSlidingCrossSection() {
		return mIsPaintingSlidingCrossSectionMenuItem.isSelected();
	}

	public boolean isPaintingFins() {
		return mIsPaintingFinsMenuItem.isSelected();
	}

	public boolean isPaintingBackgroundImage() {
		return mIsPaintingBackgroundImageMenuItem.isSelected();
	}

	public boolean isAntialiasing() {
		return mIsAntialiasingMenuItem.isSelected();
	}

	public boolean isPaintingBaseLine() {
		return mIsPaintingBaseLineMenuItem.isSelected();
	}

	public boolean isPaintingCenterLine() {
		return mIsPaintingCenterLineMenuItem.isSelected();
	}

	public boolean isPaintingOverCurveMeasurements() {
		return mIsPaintingOverCurveMesurementsMenuItem.isSelected();
	}

	public boolean isPaintingMomentOfInertia() {
		return mIsPaintingMomentOfInertiaMenuItem.isSelected();
	}

	public boolean isPaintingCrossectionsPositions() {
		return mIsPaintingCrossectionsPositionsMenuItem.isSelected();
	}

	public boolean isPaintingFlowlines() {
		return mIsPaintingFlowlinesMenuItem.isSelected();
	}

	public boolean isPaintingApexline() {
		return mIsPaintingApexlineMenuItem.isSelected();
	}

	public boolean isPaintingTuckUnderLine() {
		return mIsPaintingTuckUnderLineMenuItem.isSelected();
	}

	public boolean isPaintingFootMarks() {
		return mIsPaintingFootMarksMenuItem.isSelected();
	}

	public boolean useFill() {
		return mUseFillMenuItem.isSelected();
	}

	public AbstractBezierBoardSurfaceModel.ModelType getCrossSectionInterpolationType() {
		if (mControlPointInterpolationButton == null)
			return AbstractBezierBoardSurfaceModel.ModelType.SLinearInterpolation;

		if (mSBlendInterpolationButton.isSelected())
			return AbstractBezierBoardSurfaceModel.ModelType.SLinearInterpolation;
		else
			return AbstractBezierBoardSurfaceModel.ModelType.ControlPointInterpolation;
	}

	public int getCrossSectionInterpolationTypeAsInt() {
		AbstractBezierBoardSurfaceModel.ModelType type = getCrossSectionInterpolationType();
		switch (type) {
		default:
		case ControlPointInterpolation:
			return 2;
		case SLinearInterpolation:
			return 3;
		}
	}

	public void setCrossSectionInterpolationType(final AbstractBezierBoardSurfaceModel.ModelType type) {
		switch (type) {
		default:
		case ControlPointInterpolation:
			mControlPointInterpolationButton.doClick();
			break;
		case SLinearInterpolation:
			mSBlendInterpolationButton.doClick();
			break;
		}
	}

	public void setCrossSectionInterpolationTypeFromInt(int type) {
		switch (type) {
		default:
		case 2:
			mControlPointInterpolationButton.doClick();
			break;
		case 3:
			mSBlendInterpolationButton.doClick();
			break;
		}
	}

	
}
