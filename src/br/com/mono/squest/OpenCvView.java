package br.com.mono.squest;

import static org.bytedeco.javacpp.helper.opencv_core.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_video.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.helper.opencv_core.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class OpenCvView extends View implements Camera.PreviewCallback {
	private static final String LOG_NAME = "OPENCV";
	public static final int SUBSAMPLING_FACTOR = 4;

	private Bitmap bitmap;

	private int width = 0;
	private int height = 0;

	private IplImage pImage;
	private IplImage cImage;

	List<Point2d> pointsToDraw;

	private Paint mPaint;


	private static final int MAX_CORNERS = 500;

	public OpenCvView(Context context) throws IOException {
		super(context);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		pointsToDraw = new ArrayList<Point2d>();

		Log.d(LOG_NAME, "ON CALLBACK GETPARAMETER"
				+ camera.getParameters().getPreviewFormat());

		try {
			init(camera);
			Log.i(LOG_NAME, "On Preview");
			// fillBitmap(data);
			saveImage(data);
			// processImage(data);
			camera.addCallbackBuffer(data);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

	}

	private void fillBitmap(IplImage image) {
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(image.getByteBuffer());
	}
 
	private void saveImage(byte[] data) {

		IplImage image = getIplImageFromData(data);

		cImage = image;

		if (pImage == null) {
			pImage = image;
		}
		IplImage result = null;
		Log.i(LOG_NAME, "INI Convert");
		try {
			result = TestLK(cImage, pImage);
			fillBitmap(result);
		} catch (Exception e) {
			Log.e(LOG_NAME, e.getMessage());
		}

		File file1 = new File(Environment.getExternalStorageDirectory(),
				"imageOpen2SaveCv.jpg");

		try {
			// Log.i(LOG_NAME, "INI SAVE");
			cvSaveImage(file1.getAbsolutePath(), result);
			// Log.i(LOG_NAME, "END SAVE");
		} catch (Exception e) {
			Log.e(LOG_NAME, e.getMessage());
		}
		pImage = cImage;
		
		postInvalidate();
		this.refreshDrawableState();

	}

	private void init(Camera camera) {
		if (width == 0 || height == 0) {
			width = camera.getParameters().getPictureSize().width;
			height = camera.getParameters().getPictureSize().height;
		}

		if (bitmap == null) {
			bitmap = Bitmap
					.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		}
	}

	private void IplToBitmap(IplImage src, Bitmap dst) {
		dst.copyPixelsFromBuffer(src.getIntBuffer());
	}

	private IplImage getIplImageFromData(byte[] data) {
		int[] temp = new int[width * height];
		IplImage image = IplImage.create(width, height, IPL_DEPTH_8U, 4);
		if (image != null) {
			decodeYUV420SP(temp, data, width, height); // convert to rgb
			image.getIntBuffer().put(temp);
		}
		return image;
	}

	private void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
			int height) {

		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	public IplImage TestLK(IplImage pImg, IplImage cImg) {

		IplImage imgA = cvCreateImage(cvGetSize(pImage), IPL_DEPTH_8U, 1);
		IplImage imgB = cvCreateImage(cvGetSize(pImage), IPL_DEPTH_8U, 1);
		IplImage imgC = pImg.clone();

		cvCvtColor(pImg, imgA, CV_BGR2GRAY);
		cvCvtColor(cImg, imgB, CV_BGR2GRAY);

		IplImage eig_image = cvCreateImage(cvGetSize(pImage), IPL_DEPTH_8U, 1);
		IplImage tmp_image = cvCreateImage(cvGetSize(pImage), IPL_DEPTH_8U, 1);

		IntPointer corner_count = new IntPointer(1).put(MAX_CORNERS);
		CvPoint2D32f cornersA = new CvPoint2D32f(MAX_CORNERS);
		double quality_level = 0.1; // OR 0.01
		double min_distance = 5;

		cvGoodFeaturesToTrack(imgA, eig_image, tmp_image, cornersA,
				corner_count, quality_level, min_distance);

		cvFindCornerSubPix(imgA, cornersA, MAX_CORNERS, cvSize(3, 3),
				cvSize(-1, -1),
				cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.1));

		// Call Lucas Kanade algorithm
		byte[] features_found = new byte[MAX_CORNERS];
		float[] feature_errors = new float[MAX_CORNERS];

		CvSize pyr_sz = cvSize(imgA.width() + 8, imgB.height() / 3);

		IplImage pyrA = cvCreateImage(pyr_sz, IPL_DEPTH_8U, 1);
		IplImage pyrB = cvCreateImage(pyr_sz, IPL_DEPTH_8U, 1);

		CvPoint2D32f cornersB = new CvPoint2D32f(MAX_CORNERS);

		BytePointer bPointer = new BytePointer(features_found);
		FloatPointer fPointer = new FloatPointer(feature_errors);

		cvCalcOpticalFlowPyrLK(imgA, imgB, pyrA, pyrB, cornersA, cornersB,
				MAX_CORNERS, cvSize(5, 5), 2, bPointer, fPointer,
				cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.3), 0);

		List<Float> posPoints = new ArrayList<Float>();
		List<Float> negPoints = new ArrayList<Float>();

		// Make an image of the results
		for (int i = 0; i < corner_count.sizeof(); i++) {
			// if (features_found[i] == 0 || feature_errors[i] > 550) {
			// System.out.println("Error is " + feature_errors[i] + "/n");
			// continue;
			// }
			System.out.println("Got it/n");
			cornersA.position(i);
			cornersB.position(i);

			float diff = cornersA.x() - cornersB.x();

			if (diff > 0)
				posPoints.add(cornersA.x() - cornersB.x());
			else
				negPoints.add(cornersA.x() - cornersB.x());

			Log.i(LOG_NAME, "Xa: " + cornersA.x() + " Ya: " + cornersA.y());
			Log.i(LOG_NAME, "Xb: " + cornersB.x() + " Yb: " + cornersB.y());

			CvPoint p0 = cvPoint(Math.round(cornersA.x()),
					Math.round(cornersA.y()));
			CvPoint p1 = cvPoint(Math.round(cornersB.x()),
					Math.round(cornersB.y()));
			cvLine(imgC, p0, p1, CV_RGB(255, 0, 0), 2, 8, 0);
		}
		
		if(posPoints.size() > negPoints.size())
			Log.i(LOG_NAME, "DESLOCOU PRA DIREITA");
		else
			Log.i(LOG_NAME, "DESLOCOU PRA ESQUERDA");

		File fileA = new File(Environment.getExternalStorageDirectory(),
				"imageA.jpg");
		File fileB = new File(Environment.getExternalStorageDirectory(),
				"imageB.jpg");
		File fileRes = new File(Environment.getExternalStorageDirectory(),
				"imageOFLK.jpg");

		// Log.i(LOG_NAME, "CORNER A: " + cornersA.sizeof());
		// Log.i(LOG_NAME, "Corner B: " + cornersB.sizeof());

		// Log.i(LOG_NAME, "Save Test INI");
		cvSaveImage(fileA.getAbsolutePath(), imgA);
		cvSaveImage(fileB.getAbsolutePath(), imgB);
		cvSaveImage(fileRes.getAbsolutePath(), imgC);
		// Log.i(LOG_NAME, "Save Test END");

		return imgC;
	}

	@Override
	public void onDraw(Canvas canvas) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(LOG_NAME, "ONDRAW INI");

		if (bitmap != null)
			canvas.drawBitmap(bitmap, 0, 0, getPaint());

		// if (pointsToDraw != null) {
		// Log.i(LOG_NAME, "pointsToDraw: " + pointsToDraw.size());
		//
		//
		// // CvMat flow = opticalFlow;
		//
		// if (pointsToDraw.size() > 0)
		//
		// for (int index = 0; index < pointsToDraw.size() / 1000; index++) {
		// Point2d point = pointsToDraw.get(index);
		//
		// Log.i(LOG_NAME, "XXXXXX: " + point.x());
		// Log.i(LOG_NAME, "YYYYYY: " + point.y());
		//
		// canvas.drawPoint((float) point.x(), (float) point.y(),
		// getPaint());
		// }
		// }
		Log.i(LOG_NAME, "ONDRAW END");
	}

	private Paint getPaint() {
		if (mPaint == null) {
			mPaint = new Paint();
			mPaint.setDither(true);
			mPaint.setColor(0xFFFFFF00);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(3);
		}

		return mPaint;
	}

}
