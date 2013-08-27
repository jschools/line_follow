package com.schoovello.linefollow;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements CvCameraViewListener2 {

	private CameraBridgeViewBase mCameraView;
	private TextView mThreshView;
	private ImageView mDetectedLinesView;
	private double mThreshold = 75;
	private int mDetectedLines = 0;
	private final IntersectionDetector mIntersectionDetector = new IntersectionDetector();

	private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				mCameraView.enableView();
			}
			break;
			default: {
				super.onManagerConnected(status);
			}
			break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);

		mCameraView = (CameraBridgeViewBase) findViewById(R.id.native_surface);
		mCameraView.setMaxFrameSize(400, 400);
		mCameraView.setCvCameraViewListener(this);

		mThreshView = (TextView) findViewById(R.id.threshold);
		findViewById(R.id.btn_thresh_up).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setThreshold(mThreshold + 5);
			}
		});
		findViewById(R.id.btn_thresh_down).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setThreshold(mThreshold - 5);
			}
		});
		setThreshold(mThreshold);

		mDetectedLinesView = (ImageView) findViewById(R.id.detected_lines);
		mDetectedLinesView.setImageLevel(0);
	}

	private void setThreshold(double threshold) {
		mThreshold = threshold;
		mThreshView.setText(String.format("%.0f", mThreshold));
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mCameraView != null) {
			mCameraView.disableView();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mCameraView != null) {
			mCameraView.disableView();
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
	}

	@Override
	public void onCameraViewStopped() {
	}

	private final Runnable mUpdateDetectedLinesViewRunnable = new Runnable() {
		@Override
		public void run() {
			mDetectedLinesView.setImageLevel(mDetectedLines);
		}
	};

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		Mat gray = inputFrame.gray();
		Mat color = inputFrame.rgba();

		Imgproc.threshold(gray, gray, mThreshold, 255, Imgproc.THRESH_BINARY_INV);
		Imgproc.medianBlur(gray, gray, 15);

		boolean[] detectedLines = mIntersectionDetector.detectLines(gray, color);
		mDetectedLines = 0;
		for (int i = 0; i < detectedLines.length; i++) {
			if (detectedLines[i]) {
				mDetectedLines |= 1 << i;
			}
		}
		mDetectedLinesView.post(mUpdateDetectedLinesViewRunnable);

		return gray;
	}
}
