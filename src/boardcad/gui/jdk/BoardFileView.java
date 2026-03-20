package boardcad.gui.jdk;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

import board.BezierBoard;

import cadcore.BezierSpline;
import board.readers.*;
import boardcad.FileTools;

public class BoardFileView extends FileView {

	//private Font mSpecsFont = new Font("Dialog", Font.PLAIN, 10);
	private Font mTypeFont = new Font("Ariel", Font.PLAIN, 8);

	@Override
	public String getTypeDescription(File f) {
		String extension = FileTools.getExtension(f);
		String type = null;

		if (extension != null) {
			if (extension.equals("srf")) {
				type = "SurfCAD file";
			} else if (extension.equals("brd")) {
				type = "APS3000 file";
			} else if (extension.equals("s3d")) {
				type = "Shape3D file";
			} else if (extension.equals("s3dx")) {
				type = "Shape3D X file";
			}
		}
		return type;
	}

	// Returns an icon representing the file or its type. Here is
	// ImageFileView's implementation of this method:
	@Override
	public Icon getIcon(File f) {

		BufferedImage image = new BufferedImage(64, 64,
				BufferedImage.TYPE_INT_RGB);
		Icon icon = null;

		String extension = FileTools.getExtension(f);
		if (extension != null) {
			if (extension.equals("srf")) {
				BezierBoard brd = new BezierBoard();
				SrfReader.loadFile(brd, f.getAbsolutePath());

				icon = paintBoard(image, brd, "srf");
			} else if (extension.equals("brd")) {
				BezierBoard brd = new BezierBoard();
				BrdReader.loadFile(brd, f.getAbsolutePath());

				icon = paintBoard(image, brd, "brd");
			} else if (extension.equals("s3d")) {
				BezierBoard brd = new BezierBoard();
				S3dReader.loadFile(brd, f.getAbsolutePath());

				icon = paintBoard(image, brd, "s3d");
			} else if (extension.equals("s3dx")) {
				BezierBoard brd = new BezierBoard();
				S3dxReader.loadFile(brd, f.getAbsolutePath());

				icon = paintBoard(image, brd, "s3dx");
			}
		}

		return icon;
	}

	Icon paintBoard(BufferedImage image, BezierBoard brd, String type) {
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setBackground(new Color(1.0f, 1.0f, 1.0f, 1.0f));
		g.clearRect(0, 0, image.getHeight(), image.getWidth());

		double leftMargin = 2.0;
		// double topMargin = 3.0;
		// double bottomMargin = 3.0;

		double height = image.getHeight();
		double width = image.getWidth();

		JavaDraw jd = new JavaDraw(g);

		// Draw type text
		// FontMetrics sf = g.getFontMetrics(mSpecsFont);
		FontMetrics tf = g.getFontMetrics(mTypeFont);

		double typeStringLength = tf.stringWidth(type);

		g.setColor(new Color(0.6f, 0.6f, 0.6f));
		g.setFont(mTypeFont);
		g.drawString(type, (int) (width - typeStringLength - leftMargin),
				(tf.getHeight()));
		
		if(brd.isProtected()) {
			URL padlockUrl = getClass().getResource("/boardcad/icons/padlock.png");
			Image padlockImage = new ImageIcon(padlockUrl).getImage();
			int padLockWidth = (int)width / 2;
			int padLockHeight = (int)height / 2;
			int posX = ((int)width - padLockWidth) / 2;
			int posY = ((int)height - padLockHeight) / 2;
			g.drawImage(padlockImage, posX, posY, padLockWidth, padLockHeight, null);
		} else {
	
			// Horizontal
			double scale = (width - leftMargin * 2.0) / brd.getLength();
			BasicStroke stroke = new BasicStroke(1.0f / (float) scale);
			double outlinePos = height / 2 - (brd.getThickness() + 10.0) * scale
					/ 2;
			BezierBoardDrawUtil.paintBezierSplines(jd, leftMargin, outlinePos,
					scale, 0.0, Color.BLACK, stroke, brd,
					new BezierSpline[] { brd.getOutline() },
					BezierBoardDrawUtil.FlipY | BezierBoardDrawUtil.MirrorY, true);
	
			BezierBoardDrawUtil.paintBezierSplines(jd, leftMargin,
					outlinePos
							+ (brd.getMaxWidth() / 2.0 + brd.getThickness() + 10.0)
							* scale, scale, 0.0, Color.BLACK, stroke, brd,
					new BezierSpline[] { brd.getDeck(), brd.getBottom() },
					BezierBoardDrawUtil.FlipY, true);
		}

		return new ImageIcon(image);
	}

	@Override
	public String getName(File f) {
		return null;
	}

	@Override
	public String getDescription(File f) {
		return null;
	}

	@Override
	public Boolean isTraversable(File f) {
		return null;
	}

}
