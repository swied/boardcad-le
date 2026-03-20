package cadcore;

import board.BezierBoard;

import java.awt.geom.Point2D;

import org.jogamp.vecmath.*;

import cadcore.MathUtils.Function;

class BezierBoardControlPointInterpolationSurfaceModel extends
		AbstractBezierBoardSurfaceModel {

	public Point3d getDeckAt(final BezierBoard brd, final double x,
			final double y) {
		Function func = new Function() {
			public double f(double s) {
				return getPointAt(brd, x, s, -90.0, 90.0, true).y;
			};
		};
		double s = MathUtils.RootFinder.getRoot(func, y);

		Point3d point = getPointAt(brd, x, s, -90.0, 90.0, true);

		return point;
	}

	public Point3d getBottomAt(final BezierBoard brd, final double x,
			final double y) {

		Function func = new Function() {
			public double f(double s) {
				return getPointAt(brd, x, s, 90.0, 270.0, true).y;
			};
		};
		double s = MathUtils.RootFinder.getRoot(func, y);

		return getPointAt(brd, x, s, 90.0, 270.0, true);
	}

	public Point3d getPointAt(final BezierBoard brd, double x, double s,
			double minAngle, double maxAngle,
			boolean useMinimumAngleOnSharpCorners) {

		if (x < 0.1)
			x = 0.1;

		if (x > brd.getLength() - 0.1)
			x = brd.getLength() - 0.1;

		BezierBoardCrossSection crossSection = brd.getInterpolatedCrossSection(x);
		if (crossSection == null) return new Point3d(0.0, 0.0, 0.0);

		double minS = BezierSpline.ONE;
		double maxS = BezierSpline.ZERO;

		if (minAngle > 0.0) {
			minS = crossSection.getBezierSpline().getSByNormalReverse(
					minAngle * MathUtils.DEG_TO_RAD,
					useMinimumAngleOnSharpCorners);
		}

		if (maxAngle < 270.0) {
			maxS = crossSection.getBezierSpline().getSByNormalReverse(
					maxAngle * MathUtils.DEG_TO_RAD,
					useMinimumAngleOnSharpCorners);
		}

		if (minS > BezierSpline.ONE)
			minS = BezierSpline.ONE;
		if (maxS < BezierSpline.ZERO)
			maxS = BezierSpline.ZERO;

		double currentS = ((maxS - minS) * s) + minS;
		Point2D.Double point2D = crossSection.getPointAtS(currentS);

		Point3d point = new Point3d(x, point2D.x, point2D.y);
		point.z += brd.getRockerAtPos(x);

		if(brd.getTailType() == 1 && x < brd.getSwallowTailDepth())
		{
			double swallowY = (brd.getSwallowTailWidth()/2.0) * (1.0 - x/brd.getSwallowTailDepth());
			if(Math.abs(point.y) < swallowY)
			{
				point.y = swallowY * (point.y < 0 ? -1 : 1);
			}
		}

		return point;
	}

	public Vector3f getNormalAt(final BezierBoard brd, double x, double s,
			double minAngle, double maxAngle,
			boolean useMinimumAngleOnSharpCorners) {
		if (x < 0.1)
			x = 0.1;

		if (x > brd.getLength() - 0.1)
			x = brd.getLength() - 0.1;

		final double X_OFFSET = 0.1;
		final double S_OFFSET = 0.01;

		boolean flipNormal = false;

		if (x < 1.0) {
			x = 1.0;
		}
		if (x > brd.getLength() - 1.0) {
			x = brd.getLength() - 1.0;
		}

		BezierBoardCrossSection crossSection = brd .getInterpolatedCrossSection(x);
		if (crossSection == null)
			return new Vector3f(0.0f, 0.0f, 0.0f);

		crossSection = (BezierBoardCrossSection)crossSection.clone();

		double minS = BezierSpline.ONE;
		double maxS = BezierSpline.ZERO;

		if (minAngle > 0.0)
		{
			minS = crossSection.getBezierSpline().getSByNormalReverse(
					minAngle * MathUtils.DEG_TO_RAD,
					useMinimumAngleOnSharpCorners);
		}

		if (maxAngle < 270.0)
		{
			maxS = crossSection.getBezierSpline().getSByNormalReverse(
					maxAngle * MathUtils.DEG_TO_RAD,
					useMinimumAngleOnSharpCorners);
		}

		if (minS > BezierSpline.ONE)
		{
			minS = BezierSpline.ONE;
		}

		if (maxS < BezierSpline.ZERO)
		{
			maxS = BezierSpline.ZERO;
		}

		double currentS = ((maxS - minS) * s) + minS;

		double so = currentS + S_OFFSET;
		if (so > 1.0) {
			so = currentS - S_OFFSET;
			flipNormal = true;
		}

		double xo = x + X_OFFSET;
		BezierBoardCrossSection crossSectionXO = (BezierBoardCrossSection) (brd.getInterpolatedCrossSection(xo).clone());
		if (crossSectionXO == null)return new Vector3f(0.0f, 0.0f, 0.0f);

		float rockerX = (float)brd.getBottom().getValueAt(x);
		float rockerXO = (float)brd.getBottom().getValueAt(xo);

		Point2D.Double p = crossSection.getPointAtS(currentS);
		Point2D.Double pso = crossSection.getPointAtS(so);
		Point2D.Double pxo = crossSectionXO.getPointAtS(currentS);

		Vector3d vc = new Vector3d(0, p.x - pso.x, p.y - pso.y); // Vector																// across
		//vc.normalize();

		Vector3d vl = new Vector3d(xo - x, pxo.x - p.x, pxo.y - p.y + rockerXO - rockerX); // Vector lengthwise
		//vl.normalize();

		vc.cross(vl, vc);
		Vector3f normalVec = new Vector3f(vc);
		normalVec.normalize();
		if (flipNormal == true) {
			normalVec.scale(-1.0f);
		}
		return normalVec;
	}

	public double getCrosssectionAreaAt(final BezierBoard brd, final double x,
			int splits) {
		final BezierBoardCrossSection crossSection = brd
				.getInterpolatedCrossSection(x);
		if (crossSection == null)
			return 0.0;

		double ttAtRail = crossSection.getBezierSpline().getTTByNormal(
				90.0 * MathUtils.DEG_TO_RAD);

		MathUtils.FunctionXY func = new MathUtils.FunctionXY() {
			public Point2D.Double f(double tt) {
				return crossSection.getBezierSpline().getPointByTT(tt);
			}
		};

		double deckIntegral = MathUtils.Integral.getIntegral(func, ttAtRail,
				1.0, splits);

		double bottomIntegral = MathUtils.Integral.getIntegral(func, 0.0,
				ttAtRail, splits);

		double area = deckIntegral - bottomIntegral;
		area *= 2.0;

		if (area < 0)
			area = 0.0;

		return area;
	}
}
