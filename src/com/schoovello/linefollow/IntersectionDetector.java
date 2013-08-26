package com.schoovello.linefollow;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

public class IntersectionDetector {

	public static final int LINE_LEFT   = 0;
	public static final int LINE_RIGHT  = 1;
	public static final int LINE_TOP    = 2;
	public static final int LINE_BOTTOM = 3;

	private static final int PAD_PX = 10;
	private static final int MIN_LINE_WIDTH_PX = 1;

	public static Scalar lineColor = new Scalar(128, 0, 0);

	private final Line[] mLines;
	private final boolean[] mDetectedLines;
	private final Line mHLine;
	private final Line mVLine;

	public IntersectionDetector() {
		mLines = new Line[4];
		for (int i = 0; i < mLines.length; i++) {
			mLines[i] = new Line();
		}
		mHLine = new Line();
		mVLine = new Line();
		mDetectedLines = new boolean[4];
	}

	public boolean[] detectLines(Mat gray, Mat color) {
		Size size = gray.size();

		for (int i = 0; i < 4; i++) {
			boolean detected = false;
			Line line = mLines[i];

			switch (i) {
			case LINE_LEFT:
				detected = detectLineVertical(gray, line, PAD_PX, (int) (size.height));
				mHLine.a.x = (line.a.x + line.b.x) / 2.0;
				mHLine.a.y = (line.a.y + line.b.y) / 2.0;
				break;
			case LINE_TOP:
				detected = detectLineHorizontal(gray, line, PAD_PX, (int) (size.width));
				mVLine.a.x = (line.a.x + line.b.x) / 2.0;
				mVLine.a.y = (line.a.y + line.b.y) / 2.0;
				break;
			case LINE_RIGHT:
				detected = detectLineVertical(gray, line, (int) (size.width - PAD_PX), (int) (size.height));
				mHLine.b.x = (line.a.x + line.b.x) / 2.0;
				mHLine.b.y = (line.a.y + line.b.y) / 2.0;
				break;
			case LINE_BOTTOM:
				detected = detectLineHorizontal(gray, line, (int) (size.height - PAD_PX), (int) (size.width));
				mVLine.b.x = (line.a.x + line.b.x) / 2.0;
				mVLine.b.y = (line.a.y + line.b.y) / 2.0;
				break;
			default:
			}

			if (detected) {
				line.draw(gray, lineColor);
			}

			mDetectedLines[i] = detected;
		}

		if (mDetectedLines[LINE_LEFT] && mDetectedLines[LINE_RIGHT]) {
			mHLine.draw(gray, lineColor);
		}
		if (mDetectedLines[LINE_TOP] && mDetectedLines[LINE_BOTTOM]) {
			mVLine.draw(gray, lineColor);
		}

		return mDetectedLines;
	}

	private static boolean detectLineVertical(Mat mat, Line line, int col, int height) {
		int start = PAD_PX;
		int stop = height - PAD_PX;

		int top = 0;
		int bottom = 0;

		double value;
		for (int r = start; r < stop; r++) {
			value = mat.get(r, col)[0];
			if (value > 0) {
				top = r;
				break;
			}
		}
		for (int r = top + 1; r < stop; r++) {
			value = mat.get(r, col)[0];
			if (value < 1) {
				bottom = r;
				break;
			}
		}

		line.a.x = col;
		line.a.y = top;
		line.b.x = col;
		line.b.y = bottom;

		return bottom - top > MIN_LINE_WIDTH_PX;
	}

	private static boolean detectLineHorizontal(Mat mat, Line line, int row, int width) {
		int start = PAD_PX;
		int stop = width - PAD_PX;

		int left = 0;
		int right = 0;

		double value;
		for (int c = start; c < stop; c++) {
			value = mat.get(row, c)[0];
			if (value > 0) {
				left = c;
				break;
			}
		}
		for (int c = left + 1; c < stop; c++) {
			value = mat.get(row, c)[0];
			if (value < 1) {
				right = c;
				break;
			}
		}

		line.a.x = left;
		line.a.y = row;
		line.b.x = right;
		line.b.y = row;

		return right - left > MIN_LINE_WIDTH_PX;
	}

	private static class Line {
		public Point a;
		public Point b;
		public int width;

		public Line() {
			a = new Point();
			b = new Point();
			width = 2;
		}

		public void draw(Mat mat, Scalar color) {
			Core.line(mat, a, b, color, width, Core.LINE_AA, 0);
		}
	}

}
