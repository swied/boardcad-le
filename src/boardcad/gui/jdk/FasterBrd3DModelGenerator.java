package boardcad.gui.jdk;

import java.util.Vector;

import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.IndexedQuadArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.QuadArray;
import org.jogamp.vecmath.*;

import board.BezierBoard;



public class FasterBrd3DModelGenerator {
	boolean mCancelExecuting = false;
	Vector<Thread> mThreads = new Vector<Thread>();
	boolean mInitialModelRun = true;
	boolean mErrorOccured = false;

	public synchronized void update3DModel(BezierBoard brd, Shape3D model, int numTasks, boolean forceRefresh) {
		mCancelExecuting = true;
		//System.out.println("BezierBoard.update3DModel() cancel execution, waiting for threads");
		for (Thread thread : mThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.out.println("BezierBoard.update3DModel() InterruptedException");
			}
		}
		mThreads.clear();

		//System.out.println("BezierBoard.update3DModel() Done waiting ");

		if(forceRefresh){
			model.removeAllGeometries();
			mInitialModelRun = true;
		}

		if (brd.isEmpty())
			return;

		if (model.numGeometries() != numTasks) {
			//System.out.printf("BezierBoard.update3DModel() Need initial run geom: %d tasks: %d\n", model.numGeometries(), numTasks);

			model.removeAllGeometries();
			mInitialModelRun = true;
		} else {
			mInitialModelRun = false;
		}

		mCancelExecuting = false;
		mErrorOccured = false;
		
		double length = brd.getLength();
		for (int i = 0; i < numTasks; i++) {
			final double sx = (length / numTasks) * i;
			final double ex = (length / numTasks) * (i + 1);
			final int index = i;
			Runnable task = () -> {
				update3DModel((BezierBoard) brd.clone(), model, sx, ex, index);
			};

			Thread thread = new Thread(task);
			mThreads.add(thread);
			thread.start();
		}
		for (Thread thread : mThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.out.println("BezierBoard.update3DModel() InterruptedException");
			}
		}
		mThreads.clear();
		
		if(mErrorOccured) {
			//update3DModel(brd, model, numTasks, true);
		}
	}
	

	public synchronized void update3DModel(BezierBoard brd, Group parent, int numTasks, boolean forceRefresh) {
		mCancelExecuting = true;
		//System.out.println("BezierBoard.update3DModel() cancel execution, waiting for threads");
		for (Thread thread : mThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.out.println("BezierBoard.update3DModel() InterruptedException");
			}
		}
		mThreads.clear();

		//System.out.println("BezierBoard.update3DModel() Done waiting ");

		if (brd.isEmpty())
			return;
		
		mCancelExecuting = false;
		mInitialModelRun = true;
		
		Shape3D new3DModel = new Shape3D();
		new3DModel.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		new3DModel.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

		// Create an Appearance.
		Appearance a = new Appearance();
		Color3f ambient = new Color3f(0.4f, 0.4f, 0.4f);
		Color3f emissive = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f diffuse = new Color3f(0.8f, 0.8f, 0.8f);
		Color3f specular = new Color3f(1.0f, 1.0f, 1.0f);

		// Set up the material properties
		a.setMaterial(new Material(ambient, emissive, diffuse, specular, 115.0f));
		new3DModel.setAppearance(a);
		
		BranchGroup newBezier3DModelGroup = new BranchGroup();
		newBezier3DModelGroup.setCapability(BranchGroup.ALLOW_DETACH);
		newBezier3DModelGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		newBezier3DModelGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		newBezier3DModelGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
		newBezier3DModelGroup.addChild(new3DModel);

		double length = brd.getLength();
		for (int i = 0; i < numTasks; i++) {
			final double sx = (length / numTasks) * i;
			final double ex = (length / numTasks) * (i + 1);
			final int index = i;
			Runnable task = () -> {
				update3DModel((BezierBoard) brd.clone(), new3DModel, sx, ex, index);
			};

			Thread thread = new Thread(task);
			mThreads.add(thread);
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.out.println("BezierBoard.update3DModel() InterruptedException");
			}
		}	
		mThreads.clear();
		
		((BranchGroup)parent.getChild(0)).detach();
		parent.addChild(newBezier3DModelGroup);
	}


	public void update3DModel(BezierBoard brd, Shape3D model, double startX, double endX, int index) {
		double lengthAccuracy = 1.0;
		double widthMinAccuracy = 1.0;

		double spanLength = endX - startX;
		double width = brd.getCenterWidth();

		boolean isTail = startX <= 0.0;
		boolean isNose = endX >= brd.getLength();

		int lengthSteps = (int) (spanLength / lengthAccuracy) + 1;
		int widthSteps = (int) ((width / 2.0) / widthMinAccuracy) + 1;

		double lengthStep = spanLength / lengthSteps;

		int nrOfCoords = lengthSteps * (widthSteps * 2) * 4 * 2;
		if(isTail || isNose){
			nrOfCoords += (widthSteps * 2) * 4 * 2;
		}
		if(brd.getTailType() == 1){
			nrOfCoords += lengthSteps * 4 * 2;	// Extra for swallow tail walls
		}


		//Generate deck coordinates
		double xPos = 0.0;
		double minAngle = -45.0;
		double maxAngle = 150.0;

		Point3d[][] deckVertices = new Point3d[widthSteps+1][lengthSteps+1];
		Vector3f[][] deckNormals = new Vector3f[widthSteps+1][lengthSteps+1];

		for (int i = 0; i <= widthSteps; i++) {
			if (mCancelExecuting)
				return;

			xPos = startX;
			for (int j = 0; j <= lengthSteps; j++) {

				deckVertices[i][j] = brd.getSurfacePoint(xPos, minAngle, maxAngle, i, widthSteps);
				deckNormals[i][j] = brd.getSurfaceNormal(xPos, minAngle, maxAngle,i, widthSteps);
				if(i == 0){
					if(brd.getTailType() != 1 || xPos >= brd.getSwallowTailDepth())
					{
						deckVertices[i][j].setY(0.0);
					}
				}
				xPos += lengthStep;
			}
		}

		//Generate quads
		int nrOfQuads = 0;
		QuadArray quads = new QuadArray(nrOfCoords, IndexedQuadArray.COORDINATES | IndexedQuadArray.NORMALS);
		Point3d[] quadCoords = new Point3d[lengthSteps*4];
		Vector3f[] quadNormals = new Vector3f[lengthSteps*4];
		for (int i = 0; i < widthSteps; i++) {
			if(mCancelExecuting)return;

			int q = 0;
			for (int j = 0; j < lengthSteps; j++) {
				quadCoords[q] = deckVertices[i][j];
				quadNormals[q] = deckNormals[i][j];
				++q;
				quadCoords[q] = deckVertices[i][j+1];
				quadNormals[q] = deckNormals[i][j+1];
				++q;
				quadCoords[q] = deckVertices[i+1][j+1];
				quadNormals[q] = deckNormals[i+1][j+1];
				++q;
				quadCoords[q] = deckVertices[i+1][j];
				quadNormals[q] = deckNormals[i+1][j];
				++q;
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += lengthSteps;
		}

		//Mirror deck coordinates
		for (int i = 0; i <= widthSteps; i++) {
			if(mCancelExecuting)return;

			for (int j = 0; j <= lengthSteps; j++) {
				deckVertices[i][j].setY(-deckVertices[i][j].getY());
				deckNormals[i][j].setY(-deckNormals[i][j].getY());
			}
		}

		//Generate mirrored quads
		for (int i = 0; i < widthSteps; i++) {
			if(mCancelExecuting)return;

			int q = 0;
			for (int j = 0; j < lengthSteps; j++) {
				quadCoords[q] = deckVertices[i+1][j];
				quadNormals[q] = deckNormals[i+1][j];
				++q;
				quadCoords[q] = deckVertices[i+1][j+1];
				quadNormals[q] = deckNormals[i+1][j+1];
				++q;
				quadCoords[q] = deckVertices[i][j+1];
				quadNormals[q] = deckNormals[i][j+1];
				++q;
				quadCoords[q] = deckVertices[i][j];
				quadNormals[q] = deckNormals[i][j];
				++q;
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += lengthSteps;
		}

		//Generate bottom
		minAngle = maxAngle;
		maxAngle = 360.0;

		Point3d[][] bottomVertices = new Point3d[widthSteps+1][lengthSteps+1];
		Vector3f[][] bottomNormals = new Vector3f[widthSteps+1][lengthSteps+1];

		for (int i = 0; i <= widthSteps; i++) {
			if(mCancelExecuting)return;

			xPos = startX;

			for (int j = 0; j <= lengthSteps; j++) {
				bottomVertices[i][j] = brd.getSurfacePoint(xPos, minAngle, maxAngle, i, widthSteps);
				bottomNormals[i][j] = brd.getSurfaceNormal(xPos, minAngle, maxAngle,i, widthSteps);
				if(i == widthSteps){
					if(brd.getTailType() != 1 || xPos >= brd.getSwallowTailDepth())
					{
						bottomVertices[i][j].setY(0.0);
					}
				}
				xPos += lengthStep;
			}
		}

		//Generate quads
		for (int i = 0; i < widthSteps; i++) {
			if (mCancelExecuting)
				return;

			int q = 0;
			for (int j = 0; j < lengthSteps; j++) {
				quadCoords[q] = bottomVertices[i][j];
				quadNormals[q] = bottomNormals[i][j];
				++q;
				quadCoords[q] = bottomVertices[i][j+1];
				quadNormals[q] = bottomNormals[i][j+1];
				++q;
				quadCoords[q] = bottomVertices[i+1][j+1];
				quadNormals[q] = bottomNormals[i+1][j+1];
				++q;
				quadCoords[q] = bottomVertices[i+1][j];
				quadNormals[q] = bottomNormals[i+1][j];
				++q;
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += lengthSteps;
		}

		//Mirror bottom coordinates
		for (int i = 0; i <= widthSteps; i++) {
			if (mCancelExecuting)return;

			for (int j = 0; j <= lengthSteps; j++) {
				bottomVertices[i][j].setY(-bottomVertices[i][j].getY());
				bottomNormals[i][j].setY(-bottomNormals[i][j].getY());
			}
		}

		//Generate mirrored quads
		for (int i = 0; i < widthSteps; i++) {
			if (mCancelExecuting)return;

			int q = 0;
			for (int j = 0; j < lengthSteps; j++) {
				quadCoords[q] = bottomVertices[i+1][j];
				quadNormals[q] = bottomNormals[i+1][j];
				++q;
				quadCoords[q] = bottomVertices[i+1][j+1];
				quadNormals[q] = bottomNormals[i+1][j+1];
				++q;
				quadCoords[q] = bottomVertices[i][j+1];
				quadNormals[q] = bottomNormals[i][j+1];
				++q;
				quadCoords[q] = bottomVertices[i][j];
				quadNormals[q] = bottomNormals[i][j];
				++q;
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += lengthSteps;
		}

		//Create tail patch
		if(isTail){
			if(mCancelExecuting)return;

			int q = 0;
			quadCoords = new Point3d[widthSteps*4];
			quadNormals = new Vector3f[widthSteps*4];

			for (int i = 0; i < widthSteps; i++) {
				quadCoords[q] = bottomVertices[i+1][0];
				quadNormals[q] = bottomNormals[i+1][0];
				++q;
				quadCoords[q] = bottomVertices[i][0];
				quadNormals[q] = bottomNormals[i][0];
				++q;
				quadCoords[q] = deckVertices[(widthSteps - i)][0];
				quadNormals[q] = deckNormals[(widthSteps - i)][0];
				++q;
				quadCoords[q] = deckVertices[(widthSteps - i)-1][0];
				quadNormals[q] = deckNormals[(widthSteps - i)-1][0];
				++q;
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += q/4;

			//Mirror
			for (int i = 0; i <= widthSteps; i++) {
				bottomVertices[i][0].setY(-bottomVertices[i][0].getY());
				deckVertices[i][0].setY(-deckVertices[i][0].getY());
			}
			q = 0;
			for (int i = 0; i < widthSteps; i++) {
				quadCoords[q] = bottomVertices[i][0];
				quadNormals[q] = bottomNormals[i][0];
				++q;
				quadCoords[q] = bottomVertices[i+1][0];
				quadNormals[q] = bottomNormals[i+1][0];
				++q;
				quadCoords[q] = deckVertices[(widthSteps - i)-1][0];
				quadNormals[q] = deckNormals[(widthSteps - i)-1][0];
				++q;
				quadCoords[q] = deckVertices[(widthSteps - i)][0];
				quadNormals[q] = deckNormals[(widthSteps - i)][0];
				++q;
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += q/4;
		}

		//Create nose patch
		if(isNose){
			if (mCancelExecuting)return;

			int q = 0;
			int steps = widthSteps;
			quadCoords = new Point3d[steps*4];
			quadNormals = new Vector3f[steps*4];

			for (int i = 0; i < steps; i++) {
				quadCoords[q] = bottomVertices[i][lengthSteps];
				quadNormals[q] = bottomNormals[i][lengthSteps];
				++q;
				quadCoords[q] = bottomVertices[i+1][lengthSteps];
				quadNormals[q] = bottomNormals[i+1][lengthSteps];
				++q;
				quadCoords[q] = deckVertices[(widthSteps - i)-1][lengthSteps];
				quadNormals[q] = deckNormals[(widthSteps - i)-1][lengthSteps];
				++q;
				quadCoords[q] = deckVertices[(widthSteps - i)][lengthSteps];
				quadNormals[q] = deckNormals[(widthSteps - i)][lengthSteps];
				++q;
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += q/4;

			//Mirror
			for (int i = 0; i <= widthSteps; i++) {
				bottomVertices[i][lengthSteps].setY(-bottomVertices[i][lengthSteps].getY());
				deckVertices[i][lengthSteps].setY(-deckVertices[i][lengthSteps].getY());
			}
			q = 0;
			for (int i = 0; i < steps; i++) {
				quadCoords[q] = bottomVertices[i+1][lengthSteps];
				quadNormals[q] = bottomNormals[i+1][lengthSteps];
				++q;
				quadCoords[q] = bottomVertices[i][lengthSteps];
				quadNormals[q] = bottomNormals[i][lengthSteps];
				++q;
				quadCoords[q] = deckVertices[(widthSteps - i)][lengthSteps];
				quadNormals[q] = deckNormals[(widthSteps - i)][lengthSteps];
				++q;
				quadCoords[q] = deckVertices[(widthSteps - i)-1][lengthSteps];
				quadNormals[q] = deckNormals[(widthSteps - i)-1][lengthSteps];
				++q;
			}
			quads.setCoordinates(nrOfQuads * 4, quadCoords);
			quads.setNormals(nrOfQuads * 4, quadNormals);
			nrOfQuads += q/4;
		}

		//Create swallow tail walls
		if(brd.getTailType() == 1)
		{
			if(mCancelExecuting)return;

			int q = 0;
			quadCoords = new Point3d[lengthSteps*4];
			quadNormals = new Vector3f[lengthSteps*4];

			for (int j = 0; j < lengthSteps; j++) {
				double x = startX + j*lengthStep;
				if(x < brd.getSwallowTailDepth())
				{
					quadCoords[q] = bottomVertices[widthSteps][j];
					quadNormals[q] = new Vector3f(0, -1, 0);
					++q;
					quadCoords[q] = bottomVertices[widthSteps][j+1];
					quadNormals[q] = new Vector3f(0, -1, 0);
					++q;
					quadCoords[q] = deckVertices[0][j+1];
					quadNormals[q] = new Vector3f(0, -1, 0);
					++q;
					quadCoords[q] = deckVertices[0][j];
					quadNormals[q] = new Vector3f(0, -1, 0);
					++q;
				}
			}
			if(q > 0) {
				quads.setCoordinates(nrOfQuads * 4, quadCoords);
				quads.setNormals(nrOfQuads * 4, quadNormals);
				nrOfQuads += q/4;
			}

			q = 0;
			for (int j = 0; j < lengthSteps; j++) {
				double x = startX + j*lengthStep;
				if(x < brd.getSwallowTailDepth())
				{
					// Mirror by creating new points with inverted Y
					quadCoords[q] = new Point3d(bottomVertices[widthSteps][j+1].x, -bottomVertices[widthSteps][j+1].y, bottomVertices[widthSteps][j+1].z);
					quadNormals[q] = new Vector3f(0, 1, 0);
					++q;
					quadCoords[q] = new Point3d(bottomVertices[widthSteps][j].x, -bottomVertices[widthSteps][j].y, bottomVertices[widthSteps][j].z);
					quadNormals[q] = new Vector3f(0, 1, 0);
					++q;
					quadCoords[q] = new Point3d(deckVertices[0][j].x, -deckVertices[0][j].y, deckVertices[0][j].z);
					quadNormals[q] = new Vector3f(0, 1, 0);
					++q;
					quadCoords[q] = new Point3d(deckVertices[0][j+1].x, -deckVertices[0][j+1].y, deckVertices[0][j+1].z);
					quadNormals[q] = new Vector3f(0, 1, 0);
					++q;
				}
			}
			if(q > 0) {
				quads.setCoordinates(nrOfQuads * 4, quadCoords);
				quads.setNormals(nrOfQuads * 4, quadNormals);
				nrOfQuads += q/4;
			}
		}
		
		if (mCancelExecuting)return;

		try {
			if (mInitialModelRun) {
				model.addGeometry(quads);
			} else {
				model.setGeometry(quads, index);
			}
		} catch (Exception e) {
			System.out.printf("BezierBoard.update3DModel() model.setGeometry() failed, index: %d numGeometries: %d\n", index, model.numGeometries());
			e.printStackTrace(System.out);
			mErrorOccured = true;
		}

	}
}
