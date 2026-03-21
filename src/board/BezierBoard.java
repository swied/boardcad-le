package board;
/*

 * Created on Sep 18, 2005

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

import cadcore.AbstractBezierBoardSurfaceModel;
import cadcore.BezierBoardCrossSection;
import boardcad.i18n.LanguageResource;
import boardcad.settings.BoardCADSettings;
import cadcore.UnitUtils;
import cadcore.BezierKnot;
import cadcore.BezierSpline;
import cadcore.MathUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import org.jogamp.vecmath.*;

public class BezierBoard extends AbstractBoard implements Cloneable {

	Vector3d last;	//DEBUG

	static int VOLUME_X_SPLITS = 10;
	static int VOLUME_Y_SPLITS = 30;
	static int MASS_X_SPLITS = 10;
	static int MASS_Y_SPLIS = 10;
	public static int AREA_SPLITS = 10;

	protected String mVersion = new String();
	protected String mName = new String();
	protected String mAuthor = new String();
	protected String mDesigner = new String();
	protected String mBlankFile = new String();
	protected int mTopCuts = 0;
	protected int mBottomCuts = 0;
	protected int mRailCuts = 0;
	protected double mCutterDiam = 0;
	protected double mBlankPivot = 0;
	protected double mBoardPivot = 0;
	protected double mMaxAngle = 0;
	protected double mNoseMargin = 0;
	protected double mTailMargin = 0;
	protected double mNoseLength = 0;
	protected double mTailLength = 0;
	protected double mDeltaXNose = 0;
	protected double mDeltaXTail = 0;
	protected double mDeltaXMiddle = 0;
	protected int mToTailSpeed = 0;
	protected int mStringerSpeed = 0;
	protected int mRegularSpeed = 0;
	protected double[] mStrut1 = new double[3];
	protected double[] mStrut2 = new double[3];
	protected double[] mCutterStartPos = new double[3];
	protected double[] mBlankTailPos = new double[3];
	protected double[] mBoardStartPos = new double[3];
	protected int mCurrentUnits = 0;
	protected double mNoseRockerOneFoot = 0;
	protected double mTailRockerOneFoot = 0;
	protected boolean mShowOriginalBoard = true;
	protected int mStringerSpeedBottom = 0;
	protected String mMachineFolder = new String();
	protected double mTopShoulderAngle = 0;
	protected int mTopShoulderCuts = 0;
	protected int mBottomRailCuts = 0;
	protected String mSurfer = new String();
	protected String mComments = new String();
	protected double[] mFins = new double[9];	//x, y for back of fin, x,y for front of fin, back of center, front of center, depth of center, depth of side fin, splay angle
	protected String mFinType = new String();
	protected String  mDescription = new String();
	protected int mSecurityLevel = 0;
	protected String  mModel = new String();
	protected String  mAux1 = new String();
	protected String  mAux2 = new String();
	protected String  mAux3 = new String();

	protected int mTailType = 0; // 0 for standard, 1 for swallow/fish
	protected double mSwallowTailDepth = 0;
	protected double mSwallowTailWidth = 0;

	protected BezierSpline mOutlineSpline = new BezierSpline();

	protected BezierSpline mDeckSpline = new BezierSpline();

	protected BezierSpline mBottomSpline = new BezierSpline();

	protected ArrayList<Point2D.Double> mOutlineGuidePoints = new ArrayList<Point2D.Double>();

	protected ArrayList<Point2D.Double> mDeckGuidePoints = new ArrayList<Point2D.Double>();

	protected ArrayList<Point2D.Double> mBottomGuidePoints = new ArrayList<Point2D.Double>();

	protected ArrayList<BezierBoardCrossSection> mCrossSections = new ArrayList<BezierBoardCrossSection>();

	private int mCurrentCrossSection = 1;

	private String mFilename = new String();

	private double mCenterOfMass = 0;

	private AbstractBezierBoardSurfaceModel.ModelType mInterpolationType = AbstractBezierBoardSurfaceModel.ModelType.ControlPointInterpolation;
	
	private boolean mProtected = false;

	public BezierBoard()
	{
		reset();
	}

	public boolean isEmpty()
	{
		return (mOutlineSpline.getNrOfControlPoints() == 0);
	}


	public void set(BezierBoard brd)
	{
		mOutlineSpline = (BezierSpline)brd.mOutlineSpline.clone();

		mDeckSpline = (BezierSpline)brd.mDeckSpline.clone();

		mBottomSpline = (BezierSpline)brd.mBottomSpline.clone();

		mOutlineGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < brd.mOutlineGuidePoints.size(); i++)
		{
			mOutlineGuidePoints.add((Point2D.Double)brd.mOutlineGuidePoints.get(i).clone());
		}

		mDeckGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < brd.mDeckGuidePoints.size(); i++)
		{
			mDeckGuidePoints.add((Point2D.Double)brd.mDeckGuidePoints.get(i).clone());
		}

		mBottomGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < brd.mBottomGuidePoints.size(); i++)
		{
			mBottomGuidePoints.add((Point2D.Double)brd.mBottomGuidePoints.get(i).clone());
		}

		mCrossSections = new ArrayList<BezierBoardCrossSection>();
		for(int i = 0; i < brd.mCrossSections.size(); i++)
		{
			mCrossSections.add((BezierBoardCrossSection)brd.mCrossSections.get(i).clone());
		}

		mTailType = brd.mTailType;
		mSwallowTailDepth = brd.mSwallowTailDepth;
		mSwallowTailWidth = brd.mSwallowTailWidth;
	}

	public void reset()
	{
		mOutlineSpline.clear();

		mDeckSpline.clear();

		mBottomSpline.clear();

		mOutlineGuidePoints.clear();

		mDeckGuidePoints.clear();

		mBottomGuidePoints.clear();

		mCrossSections.clear();

		mCurrentCrossSection = 1;

		mFilename = new String();

		mVersion = new String("V4.4");
		mName = new String();
		mAuthor = new String();
		mDesigner = new String();
		mBlankFile = new String();
		mTopCuts = 0;
		mBottomCuts = 0;
		mRailCuts = 0;
		mCutterDiam = 0;
		mBlankPivot = 0;
		mBoardPivot = 0;
		mMaxAngle = 0;
		mNoseMargin = 0;
		mTailMargin = 0;
		mNoseLength = 0;
		mTailLength = 0;
		mDeltaXNose = 0;
		mDeltaXTail = 0;
		mDeltaXMiddle = 0;
		mToTailSpeed = 0;
		mStringerSpeed = 0;
		mRegularSpeed = 0;
		mStrut1 = new double[3];
		mStrut2 = new double[3];
		mCutterStartPos = new double[3];
		mBlankTailPos = new double[3];
		mBoardStartPos = new double[3];
		mCurrentUnits = 0;
		mNoseRockerOneFoot = 0;
		mTailRockerOneFoot = 0;
		mShowOriginalBoard = true;
		mStringerSpeedBottom = 0;
		mMachineFolder = new String("c:\\machine");
		mTopShoulderAngle = 0;
		mTopShoulderCuts = 0;
		mBottomRailCuts = 0;
		mSurfer = new String();
		mComments = new String();
		mFins = new double[9];	//x, y for back of fin, x,y for front of fin, bac of center, front of center, depth of center, depth of sidefin, splay angle
		mFinType = new String();
		mDescription = new String();
		mSecurityLevel = 0;
		mModel = new String();
		mAux1 = new String();
		mAux2 = new String();
		mAux3 = new String();

		mTailType = 0;
		mSwallowTailDepth = 0;
		mSwallowTailWidth = 0;

		mCenterOfMass = 0;
		
		mProtected = false;
	}

	public int getTailType() {
		return mTailType;
	}

	public void setTailType(int tailType) {
		mTailType = tailType;
	}

	public double getSwallowTailDepth() {
		return mSwallowTailDepth;
	}

	public void setSwallowTailDepth(double swallowTailDepth) {
		mSwallowTailDepth = swallowTailDepth;
	}

	public double getSwallowTailWidth() {
		return mSwallowTailWidth;
	}

	public void setSwallowTailWidth(double swallowTailWidth) {
		mSwallowTailWidth = swallowTailWidth;
	}



	public int getRailCuts() {
		return mRailCuts;
	}

	public void setRailCuts(int mRailCuts) {
		this.mRailCuts = mRailCuts;
	}

	public double getMaxAngle() {
		return mMaxAngle;
	}

	public void setMaxAngle(double mMaxAngle) {
		this.mMaxAngle = mMaxAngle;
	}

	public double getNoseMargin() {
		return mNoseMargin;
	}

	public void setNoseMargin(double mNoseMargin) {
		this.mNoseMargin = mNoseMargin;
	}

	public double getTailMargin() {
		return mTailMargin;
	}

	public void setTailMargin(double mTailMargin) {
		this.mTailMargin = mTailMargin;
	}

	public double getNoseLength() {
		return mNoseLength;
	}

	public void setNoseLength(double mNoseLength) {
		this.mNoseLength = mNoseLength;
	}

	public double getTailLength() {
		return mTailLength;
	}

	public void setTailLength(double mTailLength) {
		this.mTailLength = mTailLength;
	}

	public int getStringerSpeed() {
		return mStringerSpeed;
	}

	public void setStringerSpeed(int mStringerSpeed) {
		this.mStringerSpeed = mStringerSpeed;
	}

	public int getRegularSpeed() {
		return mRegularSpeed;
	}

	public void setRegularSpeed(int mRegularSpeed) {
		this.mRegularSpeed = mRegularSpeed;
	}

	public int getCurrentUnits() {
		return mCurrentUnits;
	}

	public void setCurrentUnits(int mCurrentUnits) {
		this.mCurrentUnits = mCurrentUnits;
	}

	public boolean getShowOriginalBoard() {
		return mShowOriginalBoard;
	}

	public void setShowOriginalBoard(boolean mShowOriginalBoard) {
		this.mShowOriginalBoard = mShowOriginalBoard;
	}

	public int getStringerSpeedBottom() {
		return mStringerSpeedBottom;
	}

	public void setStringerSpeedBottom(int mStringerSpeedBottom) {
		this.mStringerSpeedBottom = mStringerSpeedBottom;
	}

	public int getSecurityLevel() {
		return mSecurityLevel;
	}

	public void setSecurityLevel(int mSecurityLevel) {
		this.mSecurityLevel = mSecurityLevel;
	}

	public double getNoseRockerOneFoot() {
		return mNoseRockerOneFoot;
	}

	public void setNoseRockerOneFoot(double mNoseRockerOneFoot) {
		this.mNoseRockerOneFoot = mNoseRockerOneFoot;
	}

	public double getTailRockerOneFoot() {
		return mTailRockerOneFoot;
	}

	public void setTailRockerOneFoot(double mTailRockerOneFoot) {
		this.mTailRockerOneFoot = mTailRockerOneFoot;
	}

	public double[] getFins() {
		return mFins;
	}

	public void setFins(double[] mFins) {
		this.mFins = mFins;
	}

	public String getAuthor() {
		return mAuthor;
	}

	public void setSurfer(String surfer) {
		this.mSurfer = surfer;
	}

	public void setAuthor(String author) {
		this.mAuthor = author;
	}

	public void setInterpolationType(AbstractBezierBoardSurfaceModel.ModelType type)
	{
		mInterpolationType = type;
	}

	public AbstractBezierBoardSurfaceModel.ModelType getInterpolationType()
	{
		return mInterpolationType;
	}

	public String getVersion() {
		return mVersion;
	}

	public void setVersion(String mVersion) {
		this.mVersion = mVersion;
	}

	public String getDesigner() {
		return mDesigner;
	}

	public void setDesigner(String mDesigner) {
		this.mDesigner = mDesigner;
	}

	public String getBlankFile() {
		return mBlankFile;
	}

	public void setBlankFile(String blankFile) {
		this.mBlankFile = blankFile;
	}

	public int getTopCuts() {
		return mTopCuts;
	}

	public void setTopCuts(int mTopCuts) {
		this.mTopCuts = mTopCuts;
	}

	public int getBottomCuts() {
		return mBottomCuts;
	}

	public void setBottomCuts(int bottomCuts) {
		this.mBottomCuts = bottomCuts;
	}

	public double getCutterDiam() {
		return mCutterDiam;
	}

	public void setCutterDiam(double cutterDiam) {
		this.mCutterDiam = cutterDiam;
	}

	public double getBlankPivot() {
		return mBlankPivot;
	}

	public void setBlankPivot(double blankPivot) {
		this.mBlankPivot = blankPivot;
	}

	public double getBoardPivot() {
		return mBoardPivot;
	}

	public void setBoardPivot(double boardPivot) {
		this.mBoardPivot = boardPivot;
	}

	public double getDeltaXNose() {
		return mDeltaXNose;
	}

	public void setDeltaXNose(double deltaXNose) {
		this.mDeltaXNose = deltaXNose;
	}

	public double getDeltaXTail() {
		return mDeltaXTail;
	}

	public void setDeltaXTail(double deltaXTail) {
		this.mDeltaXTail = deltaXTail;
	}

	public double getDeltaXMiddle() {
		return mDeltaXMiddle;
	}

	public void setDeltaXMiddle(double deltaXMiddle) {
		this.mDeltaXMiddle = deltaXMiddle;
	}

	public int getToTailSpeed() {
		return mToTailSpeed;
	}

	public void setToTailSpeed(int toTailSpeed) {
		this.mToTailSpeed = toTailSpeed;
	}

	public double[] getStrut1() {
		return mStrut1;
	}

	public void setStrut1(double[] strut1) {
		this.mStrut1 = strut1;
	}

	public double[] getStrut2() {
		return mStrut2;
	}

	public void setStrut2(double[] strut2) {
		this.mStrut2 = strut2;
	}

	public double[] getCutterStartPos() {
		return mCutterStartPos;
	}

	public void setCutterStartPos(double[] cutterStartPos) {
		this.mCutterStartPos = cutterStartPos;
	}

	public double[] getBlankTailPos() {
		return mBlankTailPos;
	}

	public void setBlankTailPos(double[] blankTailPos) {
		this.mBlankTailPos = blankTailPos;
	}

	public double[] getBoardStartPos() {
		return mBoardStartPos;
	}

	public void setBoardStartPos(double[] boardStartPos) {
		this.mBoardStartPos = boardStartPos;
	}

	public String getMachineFolder() {
		return mMachineFolder;
	}

	public void setMachineFolder(String machineFolder) {
		this.mMachineFolder = machineFolder;
	}

	public double getTopShoulderAngle() {
		return mTopShoulderAngle;
	}

	public void setTopShoulderAngle(double topShoulderAngle) {
		this.mTopShoulderAngle = topShoulderAngle;
	}

	public int getTopShoulderCuts() {
		return mTopShoulderCuts;
	}

	public void setTopShoulderCuts(int topShoulderCuts) {
		this.mTopShoulderCuts = topShoulderCuts;
	}

	public int getBottomRailCuts() {
		return mBottomRailCuts;
	}

	public void setBottomRailCuts(int bottomRailCuts) {
		this.mBottomRailCuts = bottomRailCuts;
	}

	public String getComments() {
		return mComments;
	}

	public void setComments(String comments) {
		this.mComments = comments;
	}

	public String getFinType() {
		return mFinType;
	}

	public void setFinType(String finType) {
		this.mFinType = finType;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	public String getAux1() {
		return mAux1;
	}

	public void setAux1(String aux1) {
		this.mAux1 = aux1;
	}

	public String getAux2() {
		return mAux2;
	}

	public void setAux2(String aux2) {
		this.mAux2 = aux2;
	}

	public String getAux3() {
		return mAux3;
	}

	public void setAux3(String aux3) {
		this.mAux3 = aux3;
	}

	public void setDeckGuidePoints(ArrayList<Point2D.Double> deckGuidePoints) {
		this.mDeckGuidePoints = deckGuidePoints;
	}


	public void setCenterOfMass(double centerOfMass) {
		this.mCenterOfMass = centerOfMass;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public void setProtected(boolean protect) {
		this.mProtected = protect;
	}

	public boolean isProtected() {
		return mProtected;
	}


	public BezierSpline getOutline()
	{
		return mOutlineSpline;
	}

	public void setOutline(BezierSpline outline)
	{
		mOutlineSpline = outline;
	}

	public BezierSpline getDeck()
	{
		return mDeckSpline;
	}

	public void setDeck(BezierSpline deck)
	{
		mDeckSpline = deck;
	}

	public BezierSpline getBottom()
	{
		return mBottomSpline;
	}

	public void setBottom(BezierSpline bottom)
	{
		mBottomSpline = bottom;
	}

	public ArrayList<Point2D.Double> getOutlineGuidePoints()
	{
		return mOutlineGuidePoints;
	}

	public ArrayList<Point2D.Double> getDeckGuidePoints()
	{
		return mDeckGuidePoints;
	}

	public ArrayList<Point2D.Double> getBottomGuidePoints()
	{
		return mBottomGuidePoints;
	}

	public ArrayList<BezierBoardCrossSection> getCrossSections()
	{
		return mCrossSections;
	}

	public void addCrossSection(BezierBoardCrossSection crossSection)
	{
		mCrossSections.add(crossSection);
		sortCrossSections();
	}

	public void removeCrossSection(BezierBoardCrossSection crossSection)
	{
		mCrossSections.remove(crossSection);
		sortCrossSections();
		while(mCurrentCrossSection >= mCrossSections.size()-1 && mCurrentCrossSection > 1)
		{
			mCurrentCrossSection--;
		}
	}

	public void sortCrossSections()
	{
		Collections.sort(mCrossSections);
	}

	public BezierBoardCrossSection getCurrentCrossSection()
	{
		if(mCurrentCrossSection < 1 || mCurrentCrossSection > mCrossSections.size() -2)
			return null;

		return mCrossSections.get(mCurrentCrossSection);
	}

	public int getCurrentCrossSectionIndex()
	{
		return mCurrentCrossSection;
	}

	public void setCurrentCrossSection(int crossSectionNr)
	{
		mCurrentCrossSection = crossSectionNr;
	}

	public void nextCrossSection()
	{
		if(++mCurrentCrossSection >= mCrossSections.size()-1)
			mCurrentCrossSection = 1;
	}

	public void previousCrossSection()
	{

		if(--mCurrentCrossSection <= 0)
			mCurrentCrossSection = mCrossSections.size()-2;
	}

	public BezierBoardCrossSection getNearestCrossSection(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);
		if(index == -1)
		{
			return null;
		}
		return mCrossSections.get(index);
	}

	public BezierBoardCrossSection getPreviousCrossSection(double pos)
	{
		return mCrossSections.get(getPreviousCrossSectionIndex(pos));
	}

	public BezierBoardCrossSection getNextCrossSection(double pos)
	{
		return mCrossSections.get(getNextCrossSectionIndex(pos));
	}

	public double getPreviousCrossSectionPos(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);

		if(getCrossSections().get(index).getPosition() >= pos)
		{
			index -= 1;
		}

		return mCrossSections.get(index).getPosition();
	}

	public double getNextCrossSectionPos(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);

		if(getCrossSections().get(index).getPosition() < pos)
		{
			index += 1;
		}

		return mCrossSections.get(index).getPosition();
	}

	public int getNearestCrossSectionIndex(double pos)
	{
		int nearest = -1;
		double nearestPos = -300000.0;

		for(int i = 1; i < mCrossSections.size()-1; i++)
		{
			BezierBoardCrossSection current = mCrossSections.get(i);

			if(nearest == -1 || Math.abs(nearestPos - pos) > Math.abs(current.getPosition() - pos))
			{
				nearest = i;
				nearestPos = current.getPosition();
			}

		}

		return nearest;

	}

	public int getPreviousCrossSectionIndex(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);

		if(getCrossSections().get(index).getPosition() >= pos)
		{
			index -= 1;
		}


		//Get crosssections but use the first and last real crosssections if we're at the dummy crosssections at nose and tail
		if(index==0)
			index = 1;

		if(index > getCrossSections().size() - 2)
			index = getCrossSections().size();

		return index;
	}

	public int getNextCrossSectionIndex(double pos)
	{
		int index = getNearestCrossSectionIndex(pos);

		if(getCrossSections().get(index).getPosition() < pos)
		{
			index += 1;
		}


		//Get crosssections but use the first and last real crosssections if we're at the dummy crosssections at nose and tail
		if(index==0)
			index = 1;

		if(index > getCrossSections().size() - 2)
			index = getCrossSections().size() - 2;

		return index;
	}

	public double getLength() {

		double length = 0;

		for(int i = 0; i < mOutlineSpline.getNrOfControlPoints(); i++) {

			double x = mOutlineSpline.getControlPoint(i).getEndPoint().x;

			if(x > length) {

				length = x;

			}

		}

		return length;

	}

	public double getLengthOverCurve() {

		return mBottomSpline.getLength();

	}


	public double getCenterWidth() {

		return getWidthAtPos(getLength()/2);

	}

	public double getMaxWidth() {

		return mOutlineSpline.getMaxY()*2.0;

	}
	public double getMaxWidthPos() {

		return mOutlineSpline.getXForMaxY();

	}

	public double getThickness()
	{
		return getThicknessAtPos(getLength()/2.0);
	}

	public double getMaxThickness()
	{
		double max = -100000;
		for(int i = 0; i < Math.floor(getLength()*10); i++)  //in order to have it computed every millimeter
		{
			double posi = (double)i/10;
			double current = getThicknessAtPos(posi);
			if(current > max){
				max = current;
			}
		}
		return max;
	}

	public double getMaxThicknessPos()
	{
		double max = -100000;
		double maxPos = -100000;
		for(int i = 0; i < Math.floor(getLength()*10); i++)  //in order to have it computed every millimeter
		{
			double posi = (double)i/10;
			double current = getThicknessAtPos(posi);
			if(current > max){
				maxPos = posi;
				max = current;
			}
		}
		return maxPos;
	}

	public double getMaxRocker()
	{
		return mBottomSpline.getMaxY();
	}

	public Point2d getMaxDeckAtTailPos()
	{
		BezierSpline deck = getDeck();

		double x = deck.getXForMaxYInRange(0.0, UnitUtils.FOOT);
		double y = deck.getMaxYInRange(0.0, UnitUtils.FOOT);

		return new Point2d(x,y);
	}

	public Point2d getMaxDeckAtNosePos()
	{
		BezierSpline deck = getDeck();

		double x = deck.getXForMaxYInRange(getLength() - UnitUtils.FOOT, getLength());
		double y = deck.getMaxYInRange(getLength() - UnitUtils.FOOT, getLength());

		return new Point2d(x,y);
	}

	public double getWidthAtPos(double pos)
	{
		return mOutlineSpline.getValueAt(pos)*2;
	}

	public double getRockerAtPos(double pos)
	{
		return mBottomSpline.getValueAt(pos);
	}

	public double getDeckAtPos(double pos)
	{
		return mDeckSpline.getValueAt(pos);
	}

	public double getThicknessAtPos(double pos)
	{
		return getDeckAtPos(pos) - getRockerAtPos(pos);
	}

	public double getThicknessAtPos(double x, double y)
	{
		return getDeckAt(x,y) - getBottomAt(x,y);
	}


	public double getFromTailOverBottomCurveAtPos(double pos)
	{
		return mBottomSpline.getLengthByX(pos);
	}

	public double getFromNoseOverBottomCurveAtPos(double pos)
	{
		return mBottomSpline.getLength() - mBottomSpline.getLengthByX(pos);
	}

	public double getXFromTailByOverBottomCurveLength(double length)
	{
		return mBottomSpline.getPointByCurveLength(length).x;
	}

	public double getDeckAtPos(double x, double y)
	{
		return AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getDeckAt(this,x, y).getZ();
	}

	public double getBottomAtPos(double x, double y)
	{
		return AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getBottomAt(this,x, y).getZ();

	}

	//Point on crosssection without rocker added
	public Point2D.Double getPointAtPos(double x, double s)
	{
		Point2D.Double pos = getSurfacePointAtPos(x,s);

		return new Point2D.Double(pos.x,pos.y-getRockerAtPos(x));
	}

	public Point2D.Double getSurfacePointAtPos(double x, double s)
	{
		Point3d pos = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getPointAt(this, x, s, -90.0, 360.0, true);

		return new Point2D.Double(pos.y,pos.z);

	}


	public Vector3f getDeckNormalAt(double x, double y)
	{
		return AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getDeckNormalAt(this,x, y);
	}

	public Vector3f getBottomNormalAt(double x, double y)
	{
		return AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getBottomNormalAt(this,x, y);

	}

	public Vector3d getNormalAtPos(double x, double s)
	{
		final double OFFSET = 0.001;
		final double S_OFFSET = 0.001;
		Point2D.Double o = getSurfacePointAtPos(x,s);

		Point2D.Double cp = getSurfacePointAtPos(x,s-S_OFFSET);
		Vector3d cv = new Vector3d(x, cp.x-o.x, cp.y-o.y);

		Point2D.Double lp = getSurfacePointAtPos(x-OFFSET,s);
		Vector3d lv = new Vector3d(x, lp.x-o.x, lp.y-o.y);

		Vector3d normalVec = new Vector3d();
		normalVec.cross(cv,lv);
		normalVec.normalize();

		return normalVec;
	}

	public Vector3d getSurfaceNormalAtPos(double x, double s)
	{
		final double OFFSET = 0.001;
		final double S_OFFSET = 0.001;
		Point2D.Double o = getSurfacePointAtPos(x,s);

		Point2D.Double cp = getSurfacePointAtPos(x,s-S_OFFSET);
		Vector3d cv = new Vector3d(x, cp.x-o.x, cp.y-o.y);

		Point2D.Double lp = getSurfacePointAtPos(x-OFFSET,s);
		Vector3d lv = new Vector3d(x, lp.x-o.x, lp.y-o.y);

		Vector3d normalVec = new Vector3d();
		normalVec.cross(cv,lv);
		normalVec.normalize();

		return normalVec;
	}

	public Point3d getSurfacePoint(double x, double minAngle, double maxAngle, int currentSplit, int totalSplits)
	{
		return getSurfacePoint(x, minAngle, maxAngle, currentSplit, totalSplits, true);
	}

	public Point3d getSurfacePoint(double x, double minAngle, double maxAngle, int currentSplit, int totalSplits, boolean useMinimumAngleOnSharpCorners)
	{
		Point3d point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getPointAt(this,x, (double)currentSplit/(double)totalSplits, minAngle, maxAngle, useMinimumAngleOnSharpCorners);
		return point;
	}

	public Point3d getSurfacePoint(double x, double s)
	{
		Point3d point = null;

		point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getPointAt(this,x, s, -360.0, 360.0, true);
		return point;

	}

	public Vector3f getSurfaceNormal(double x, double minAngle, double maxAngle, int currentSplit, int totalSplits)
	{
		return 	getSurfaceNormal(x, minAngle, maxAngle, currentSplit, totalSplits, true);

	}

	public Vector3f getSurfaceNormal(double x, double minAngle, double maxAngle, int currentSplit, int totalSplits, boolean useMinimumAngleOnSharpCorners )
	{

		Vector3f normal = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getNormalAt(this,x, (double)currentSplit/(double)totalSplits, minAngle, maxAngle, useMinimumAngleOnSharpCorners);
		return normal;

	}

	public Vector3f getSurfaceNormal(double x, double s)
	{
		Vector3f normal = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getNormalAt(this,x, s, -360.0, 360.0, true);
		return normal;
	}


	public Vector3d getTangentAtPos(double x, double sa, double sb)
	{
		//Get the position first since we cheat with the crosssections at tip and tail
		double pos1 = getPreviousCrossSectionPos(x);
		double pos2 = getNextCrossSectionPos(x);

		//Get crosssections but use the first and last real crosssections position if we're at the dummy crosssections at nose and tail
		BezierBoardCrossSection c1 = getPreviousCrossSection(x);
		BezierBoardCrossSection c2 = getNextCrossSection(x);


		double a1 = c1.getBezierSpline().getTangentByS(sa);
		double a2 = c2.getBezierSpline().getTangentByS(sb);

		//Get blended point
		double p = (x - pos1)/(pos2 - pos1);

		double a  = ((1-p)*a1) + (p*a2);

		Vector3d ret = new Vector3d(0,Math.sin(a),Math.cos(a));

		return ret;
	}

	public BezierBoardCrossSection getInterpolatedCrossSection(double x)
	{
		if(getCrossSections().size() == 0)
			return null;

		if(x < 0)
			return null;

		if(x > getLength())
			return null;

		int index = getNearestCrossSectionIndex(x);
		if(getCrossSections().get(index).getPosition() > x)
		{
			index -= 1;
		}
		int nextIndex = index+1;

		double firstCrossSectionPos = getCrossSections().get(index).getPosition();
		double secondCrossSectionPos = getCrossSections().get(nextIndex).getPosition();

		//Calculate t
		double t = (x - firstCrossSectionPos) / (secondCrossSectionPos - firstCrossSectionPos);
		if(Double.isInfinite(t) || Double.isNaN(t))
		{
			t = 0.0;
		}

		if(index < 1)
		{
			index = 1;
		}
		if(nextIndex > mCrossSections.size()-2)
		{
			index = mCrossSections.size()-2;
			nextIndex = index;
		}

		BezierBoardCrossSection c1 = getCrossSections().get(index);
		BezierBoardCrossSection c2 = getCrossSections().get(nextIndex);

		BezierBoardCrossSection i = c1.interpolate(c2, t);
		if(i != null)
		{
			//Calculate scale
			double thickness = getThicknessAtPos(x);
			if(thickness < 0.5)
				thickness = 0.5;

			double width = getWidthAtPos(x);
			if(width < 0.5)
				width = 0.5;

			i.scale(thickness, width);

			i.setPosition(x);
		}

		return i;
	}

	public double getArea()
	{
	    final MathUtils.Function f = new MathUtils.Function(){public double f(double x){return getWidthAtPos(x);}};

		double newInt =  MathUtils.Integral.getIntegral(f, BezierSpline.ZERO, getLength()-BezierSpline.ZERO, AREA_SPLITS);

		return newInt;
	}

	public double getVolume()
	{
		if(getCrossSections().size() < 3)
			return 0;

		double a = 0.01;

		double b = getLength() - 0.01;

		MathUtils.Function crsSecAreaFunc = new MathUtils.Function(){public double f(double x){return getCrossSectionAreaAt(x,VOLUME_X_SPLITS);}};

		double volume =  MathUtils.Integral.getIntegral(crsSecAreaFunc, a, b, VOLUME_Y_SPLITS);

		return volume;
	}

	public double getMomentOfInertia(double x, double y)
	{
		if(getCrossSections().size() < 3)
			return 0;

		double a = 0.01;

		double b = getLength() - 0.01;

		MathUtils.Function crsSecAreaFunc = new MathUtils.Function(){public double f(double x){return getCrossSectionAreaAt(x,VOLUME_X_SPLITS);}};

		double pos = a;
		double pos_step = (b-a)/VOLUME_Y_SPLITS;
		double density = 3.0/30;	//3kg board of 30 liters is a good estimate for a modern performance board

		double momentOfInertia = 0;
		for(int i = 0; i < VOLUME_Y_SPLITS; i++)
		{
			double volume =  MathUtils.Integral.getIntegral(crsSecAreaFunc, pos, pos+pos_step, 1);
			volume /= UnitUtils.CUBICCENTIMETER_PR_LITRE;

			double dx = pos+(pos_step/2) - x;
			double dy = y;
			double r = Math.sqrt((dx*dx) + (dy*dy));
			r/=UnitUtils.CENTIMETER_PR_METER;	//To get the unit right kgm2

			momentOfInertia += volume*density*r*r;

			pos += pos_step;
		}

		return momentOfInertia;
	}


	public double getCenterOfMass()
	{
		if(mCenterOfMass != 0)
			return mCenterOfMass;

		double momentSum = 0.0;
		double weightSum = 0.0;

		if(getCrossSections().size() < 3)
			return 0.0;


		double a = 0.01;

		double b = getLength() - 0.01;

		double step = (b-a)/MASS_Y_SPLIS;

		double an = a;

		double x0 = getCrossSectionAreaAt(an, MASS_X_SPLITS);

		for(int i = 0; i < MASS_Y_SPLIS; i++)
		{

			double x1 = getCrossSectionAreaAt(an+(step/2), MASS_X_SPLITS);
			double x2 = getCrossSectionAreaAt(an+step, MASS_X_SPLITS);

			if(Double.isNaN(x0))
			{
				x0 = 0;
			}
			if(Double.isNaN(x1))
			{
				x1 = 0;
			}
			if(Double.isNaN(x2))
			{
				x2 = 0;
			}

			double integral = (step/6)*(x0 + (4*x1) + x2);

			momentSum += (an+(step/2))*integral;
			weightSum += integral;

			an += step;

			x0 = x2;
		}
		mCenterOfMass = momentSum/weightSum;
		return mCenterOfMass;

	}

	public double getCrossSectionAreaAt(final double pos, final int splits)
	{
		double area = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getCrosssectionAreaAt(this,pos, splits);

		return area;
	}

	public double getWidthAt(double x)
	{
		return getWidthAtPos(x);
	}

	public double getDeckAt(double x, double y)
	{
		if(getWidthAt(x)/2.0 < y)
			return 0.0;

		if(y == 0.0)
		{
			double z = getDeck().getValueAt(x);
			return z;
		}

		Point3d point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getDeckAt(this,x, y);
		return point.z;

	}

	public double getBottomAt(double x, double y)
	{
		if(getWidthAt(x)/2.0 < y)
			return 0.0;

		if(y == 0.0)
		{
			double z = getBottom().getValueAt(x);
			return z;
		}

		Point3d point = AbstractBezierBoardSurfaceModel.getBezierBoardSurfaceModel(getInterpolationType()).getBottomAt(this,x, y);
		return point.z;

	}

	public void onRockerChanged()
	{
		mCenterOfMass = 0;

		if(BoardCADSettings.getInstance().isUsingRockerStickAdjustment())
		{
			adjustRockerToCenterTangent();
		}

		adjustRockerToZero();

		adjustCrosssectionsToThicknessAndWidth();
	}

	public void onOutlineChanged()
	{
		mCenterOfMass = 0;
		adjustCrosssectionsToThicknessAndWidth();
	}

	public void onCrossSectionChanged()
	{
		mCenterOfMass = 0;
		adjustCrosssectionsToThicknessAndWidth();
	}

	void adjustCrosssectionsToThicknessAndWidth()
	{
		for(int i = 1; i < mCrossSections.size()-1; i++)
		{
			BezierBoardCrossSection current = mCrossSections.get(i);
			double currentPos = current.getPosition();
			current.scale(getThicknessAtPos(currentPos), getWidthAtPos(currentPos));
		}
	}

	void adjustRockerToCenterTangent()
	{
		double tangentAngle = mBottomSpline.getTangentByS(.5);	//Get tangent at center

		double sin = Math.sin(Math.PI - tangentAngle);
		double cos = Math.cos(Math.PI - tangentAngle);

		double x=0;
		double y=0;
		BezierKnot current = null;

		for(int i = 0; i < mBottomSpline.getNrOfControlPoints(); i++)
		{
			current = mBottomSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				x = points[j].x;
				y = points[j].y;

				points[j].x = x*sin + y*cos;
				points[j].y = x*cos + y*sin;
			}
		}

		for(int i = 0; i < mDeckSpline.getNrOfControlPoints(); i++)
		{
			current = mDeckSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				x = points[j].x;
				y = points[j].y;

				points[j].x = x*sin + y*cos;
				points[j].y = x*cos + y*sin;
			}
		}

		//Tail may be a bit off the end so move the rocker
		double fromZero = mBottomSpline.getControlPoint(0).getEndPoint().x;

		for(int i = 0; i < mBottomSpline.getNrOfControlPoints(); i++)
		{
			current = mBottomSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				points[j].x = points[j].x-fromZero;
			}
		}

		for(int i = 0; i < mDeckSpline.getNrOfControlPoints(); i++)
		{
			current = mDeckSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				points[j].x = points[j].x-fromZero;
			}
		}

		//Scale so length of rocker matches outline
		double rockerLength = mBottomSpline.getMaxX();
		double brdLength = getLength();

		mBottomSpline.scale(1.0, brdLength/rockerLength);
		mDeckSpline.scale(1.0, brdLength/rockerLength);

	}

	void adjustRockerToZero()
	{
		double min = mBottomSpline.getMinY();

		for(int i = 0; i < mBottomSpline.getNrOfControlPoints(); i++)
		{
			BezierKnot current = mBottomSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				points[j].y -= min;
			}
		}

		for(int i = 0; i < mDeckSpline.getNrOfControlPoints(); i++)
		{
			BezierKnot current = mDeckSpline.getControlPoint(i);
			Point2D.Double[] points = current.getPoints();
			for(int j = 0; j < 3; j++)
			{
				points[j].y -= min;
			}
		}
	}

	public void scale(double newLength, double newWidth, double newThickness)
	{
		double lengthScale = newLength/getLength();
		double widthScale = newWidth/getMaxWidth();
		double thicknessScale = newThickness/getMaxThickness();

		mOutlineSpline.scale(widthScale, lengthScale);
		mDeckSpline.scale(thicknessScale, lengthScale);
		//problem...from here the value of BoardCAD.getInstance().getCurrentBrd().getMaxThickness() is not what the user asked

		mBottomSpline.scale(thicknessScale, lengthScale);

		for(int i = 1; i < mCrossSections.size()-1; i++)
		{
			BezierBoardCrossSection cs = mCrossSections.get(i);
			cs.setPosition(cs.getPosition()*lengthScale);
		}
		mCrossSections.get(mCrossSections.size()-1).setPosition(newLength);

		adjustCrosssectionsToThicknessAndWidth();
	}

	public void scaleAccordingly(double newLength, double newWidth, double newThickness)
	{
		double lengthScale = newLength/getLength();
		double widthScale = newWidth/getMaxWidth();
		double thicknessScale = newThickness/getMaxThickness();
		double thicknessDiff = newThickness - getMaxThickness();

		double maxThicknessPos = getMaxThicknessPos();

		ArrayList<Double> thicknesses = new ArrayList<Double>();
		for(int i = 1; i < mDeckSpline.getNrOfControlPoints()-1; i++)
		{
			BezierKnot point = mDeckSpline.getControlPoint(i);

			double x = point.getEndPoint().x;
			if(x < BezierSpline.ZERO)
			{
				x = BezierSpline.ZERO;
			}
			if(x > getLength() - BezierSpline.ZERO)
			{
				x = getLength() - BezierSpline.ZERO;
			}
			double thickness = point.getEndPoint().y - mBottomSpline.getValueAt(x);

			thicknesses.add(thickness);
		}

		mOutlineSpline.scale(widthScale, lengthScale);
		mDeckSpline.scale(1.0, lengthScale);

		mBottomSpline.scale(lengthScale, lengthScale);

		double angle = Math.atan2(thicknessDiff, maxThicknessPos);

		for(int i = 1; i < mDeckSpline.getNrOfControlPoints()-1; i++)
		{
			BezierKnot point = mDeckSpline.getControlPoint(i);

			double x = point.getEndPoint().x;
			if(x < BezierSpline.ZERO)
			{
				x = BezierSpline.ZERO;
			}
			if(x > getLength() - BezierSpline.ZERO)
			{
				x = getLength() - BezierSpline.ZERO;
			}

			double thickness = thicknesses.get(i-1);

			double targetThickness = thickness*thicknessScale;

			double actualThickness = point.getEndPoint().y - mBottomSpline.getValueAt(x);

			double dy = targetThickness - actualThickness;

			point.setControlPointLocation(point.getEndPoint().x, point.getEndPoint().y + dy);


			double usedAngle = angle*((maxThicknessPos-x)/maxThicknessPos);

			point.setTangentToNextAngle(point.getTangentToNextAngle() + usedAngle);
			point.setTangentToPrevAngle(point.getTangentToPrevAngle() + usedAngle);
		}

		for(int i = 1; i < mCrossSections.size()-1; i++)
		{
			BezierBoardCrossSection cs = mCrossSections.get(i);
			cs.setPosition(cs.getPosition()*lengthScale);
		}
		mCrossSections.get(mCrossSections.size()-1).setPosition(newLength);

		adjustCrosssectionsToThicknessAndWidth();
	}

	public void finScaling(double straightXRatio, double YRatio)
	{
		mFins[0]=mFins[0]*straightXRatio;
		mFins[2]=mFins[2]*straightXRatio;
		mFins[4]=mFins[4]*straightXRatio;
		mFins[5]=mFins[5]*straightXRatio;

		mFins[1]=mFins[1]*YRatio;
		mFins[3]=mFins[3]*YRatio;
	}


	public void setLocks()
	{
		if(mOutlineSpline.getNrOfControlPoints() < 2)
			return;

//		Set masks
		mOutlineSpline.getControlPoint(0).setMask(0,0);
		mOutlineSpline.getControlPoint(mOutlineSpline.getNrOfControlPoints()-1).setMask(0,0);

		mDeckSpline.getControlPoint(0).setMask(0,1.0f);
		mDeckSpline.getControlPoint(mDeckSpline.getNrOfControlPoints()-1).setMask(0,1.0f);

		mBottomSpline.getControlPoint(0).setMask(0,0);

		mBottomSpline.getControlPoint(0).setMask(0,1.0f);
		mBottomSpline.getControlPoint(mBottomSpline.getNrOfControlPoints()-1).setMask(0,1.0f);

		boolean ajustCrossSectionThickNess = BoardCADSettings.getInstance().getAdjustCrossectionThickness();
		for(int i = 0; i < mCrossSections.size(); i++)
		{
			mCrossSections.get(i).getBezierSpline().getControlPoint(0).setMask(0, ajustCrossSectionThickNess ? 1 : 0);
			mCrossSections.get(i).getBezierSpline().getControlPoint(mCrossSections.get(i).getBezierSpline().getNrOfControlPoints()-1).setMask(0,ajustCrossSectionThickNess ? 1 : 0);
		}

//		Set slaves
		mDeckSpline.getControlPoint(0).setSlave(mBottomSpline.getControlPoint(0));
		mDeckSpline.getControlPoint(mDeckSpline.getNrOfControlPoints()-1).setSlave(mBottomSpline.getControlPoint(mBottomSpline.getNrOfControlPoints()-1));

		mBottomSpline.getControlPoint(0).setSlave(mDeckSpline.getControlPoint(0));
		mBottomSpline.getControlPoint(mBottomSpline.getNrOfControlPoints()-1).setSlave(mDeckSpline.getControlPoint(mDeckSpline.getNrOfControlPoints()-1));

//		Set locks
		for(int i = 0; i < mOutlineSpline.getNrOfControlPoints(); i++)
		{
			mOutlineSpline.getControlPoint(i).setTangentToPrevLocks(BezierKnot.LOCK_X_LESS);
			mOutlineSpline.getControlPoint(i).setTangentToNextLocks(BezierKnot.LOCK_X_MORE);
		}
		mOutlineSpline.getControlPoint(0).addTangentToNextLocks(BezierKnot.LOCK_Y_MORE);
		mOutlineSpline.getControlPoint(mOutlineSpline.getNrOfControlPoints()-1).addTangentToPrevLocks(BezierKnot.LOCK_Y_MORE);

		for(int i = 0; i < mDeckSpline.getNrOfControlPoints(); i++)
		{
			mDeckSpline.getControlPoint(i).setTangentToPrevLocks(BezierKnot.LOCK_X_LESS);
			mDeckSpline.getControlPoint(i).setTangentToNextLocks(BezierKnot.LOCK_X_MORE);
		}

		for(int i = 0; i < mBottomSpline.getNrOfControlPoints(); i++)
		{
			mBottomSpline.getControlPoint(i).setTangentToPrevLocks(BezierKnot.LOCK_X_LESS);
			mBottomSpline.getControlPoint(i).setTangentToNextLocks(BezierKnot.LOCK_X_MORE);
		}

		for(int i = 0; i < mCrossSections.size(); i++)
		{
			mCrossSections.get(i).getBezierSpline().getControlPoint(0).setTangentToNextLocks(BezierKnot.LOCK_X_MORE);
			mCrossSections.get(i).getBezierSpline().getControlPoint(mCrossSections.get(i).getBezierSpline().getNrOfControlPoints()-1).setTangentToPrevLocks(BezierKnot.LOCK_X_MORE);
		}
	}

	public void checkAndFixContinousy(boolean fixShouldBeCont, boolean fixShouldNotBeCont)
	{
		checkAndFixContinousy(getOutline(), fixShouldBeCont, fixShouldNotBeCont);
		checkAndFixContinousy(getBottom(), fixShouldBeCont, fixShouldNotBeCont);
		checkAndFixContinousy(getDeck(), fixShouldBeCont, fixShouldNotBeCont);

		for(int i = 0; i < getCrossSections().size(); i++)
		{
			checkAndFixContinousy(getCrossSections().get(i).getBezierSpline(), fixShouldBeCont, fixShouldNotBeCont);
		}

	}

	public void checkAndFixContinousy(BezierSpline patch, boolean fixShouldBeCont, boolean fixShouldNotBeCont)
	{
		for(int i = 0; i < patch.getNrOfControlPoints(); i++)
		{
			BezierKnot ControlPoint = patch.getControlPoint(i);

			double pta = ControlPoint.getTangentToPrevAngle();
			double nta = ControlPoint.getTangentToNextAngle();

			boolean cont = (Math.abs(Math.abs(Math.PI -pta)-nta) < 0.02)?true:false;	//0.02 is roughly one degree

			if(cont && fixShouldBeCont)
			{
				ControlPoint.setContinous(cont);
			}

			if(!cont && fixShouldNotBeCont)
			{
				ControlPoint.setContinous(cont);
			}
		}
	}

	public String getFilename()
	{
		return mFilename;
	}

	public void setModel(String model)
	{
		 mModel = model;
	}

	public String getModel()
	{
		return mModel;
	}

	public String getSurfer()
	{
		return mSurfer;
	}

	public void setFilename(String filename)
	{
		mFilename = filename;
	}

	boolean deckCollisionTest(Point3d aabbCenter, double width, double depth, double height)
	{
		if(aabbCenter.z - (height/2.0) > getMaxRocker())
			return false;

		if(aabbCenter.y - (width/2.0) > getMaxWidth())
				return false;

		if(aabbCenter.x - (depth/2.0) > getLength())
			return false;

		int depthSplits = 3;	//Numbers of coordinates, 3 mean corners point plus one at each center
		int widthSplits = 3;	//9 checks total

		double x = aabbCenter.x - (depth/2.0);

		for(int i = 0; i <= depthSplits; i++)
		{
			double y = aabbCenter.y - (width/2.0);

			for(int j = 0; j <= widthSplits; j++)
			{
				double z = getDeckAt(x, y);
				z += getRockerAtPos(x);

				if(z < aabbCenter.y - (width/2.0))
					return true;

				y += width / (double)(widthSplits-1);
			}
			x += depth / (double)(depthSplits-1);
		}

		return false;
	}

	public Object clone()
	{
		BezierBoard brd = null;
		try {
			brd = (BezierBoard)super.clone();
		} catch(CloneNotSupportedException e) {
			System.out.println("Exception in BezierBoard::clone(): " + e.toString());
			throw new Error("CloneNotSupportedException in Brd");
		}

		brd.mOutlineSpline = (BezierSpline)mOutlineSpline.clone();

		brd.mDeckSpline = (BezierSpline)mDeckSpline.clone();

		brd.mBottomSpline = (BezierSpline)mBottomSpline.clone();

		brd.mOutlineGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < this.mOutlineGuidePoints.size(); i++)
		{
			brd.mOutlineGuidePoints.add((Point2D.Double)this.mOutlineGuidePoints.get(i).clone());
		}

		brd.mDeckGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < this.mDeckGuidePoints.size(); i++)
		{
			brd.mDeckGuidePoints.add((Point2D.Double)this.mDeckGuidePoints.get(i).clone());
		}

		brd.mBottomGuidePoints = new ArrayList<Point2D.Double>();
		for(int i = 0; i < this.mBottomGuidePoints.size(); i++)
		{
			brd.mBottomGuidePoints.add((Point2D.Double)this.mBottomGuidePoints.get(i).clone());
		}

		brd.mCrossSections = new ArrayList<BezierBoardCrossSection>();
		for(int i = 0; i < this.mCrossSections.size(); i++)
		{
			brd.mCrossSections.add((BezierBoardCrossSection)this.mCrossSections.get(i).clone());
		}

		brd.mTailType = this.mTailType;
		brd.mSwallowTailDepth = this.mSwallowTailDepth;
		brd.mSwallowTailWidth = this.mSwallowTailWidth;

		return brd;
	}


	public String toString()
	{
		String str = new String();
		str.concat(getFilename());
		str.concat(" ");
		String measurementsString = LanguageResource.getString("MEASUREMENTS_STR") + UnitUtils.convertLengthToCurrentUnit(getLength(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(getCenterWidth(), true) + LanguageResource.getString("BY_STR") + UnitUtils.convertLengthToCurrentUnit(getThickness(), true);
		str.concat(measurementsString);
		if(getModel() != "")
		{
			String modelString = LanguageResource.getString("MODEL_STR") + getModel();
			str.concat(" ");
			str.concat(modelString);
		}
		if(getDesigner() != "")
		{
			String designerString = LanguageResource.getString("DESIGNER_STR") + getDesigner();
			str.concat(" ");
			str.concat(designerString);
		}
		if(getSurfer() != "")
		{
			String surferString = LanguageResource.getString("SURFER_STR") + getSurfer();
			str.concat(" ");
			str.concat(surferString);
		}


		return str;
	}

}
