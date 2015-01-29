package br.com.mono.squest;

<<<<<<< HEAD
=======
import static org.bytedeco.javacpp.helper.opencv_core.*;
>>>>>>> FETCH_HEAD
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_video.*;

<<<<<<< HEAD
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_video.DenseOpticalFlow;

=======
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avformat.av_format_control_message;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

>>>>>>> FETCH_HEAD
public class OpenCvView extends View implements Camera.PreviewCallback {
	private static final String LOG_NAME = "OPENCV";
	public static final int SUBSAMPLING_FACTOR = 4;

<<<<<<< HEAD
	private IplImage grayImage;
=======
>>>>>>> FETCH_HEAD
	private Bitmap bitmap;

	private int width = 0;
	private int height = 0;

	private IplImage pImage;
	private IplImage cImage;
<<<<<<< HEAD
	
	private Camera camera;

	public OpenCvView(Context context) throws IOException {
		super(context);
=======

	private Paint mPaint;
	private FFmpegFrameRecorder recorder;

	private static final int MAX_CORNERS = 500;

	public OpenCvView(Context context) throws IOException {
		super(context);
		File video = new File(Environment.getExternalStorageDirectory(),
				"openMP4.mp4");
		recorder = new FFmpegFrameRecorder(video, 600, 400, 1);
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
        recorder.setFormat("mp4");
        //recorder.setPixelFormat(avutil.PIX_FMT_YUV420P16);
        recorder.setFrameRate(30);
		try {
			Log.e(LOG_NAME, "CODEC RECORD: " + recorder.getVideoCodec());
			Log.e(LOG_NAME, "INITIALIZE RECORD");
			recorder.start();
		} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
			Log.e(LOG_NAME, e.getMessage());
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			try {
				Log.e(LOG_NAME, "STOP RECORD");
				recorder.stop();
				recorder.release();
			} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
				Log.e(LOG_NAME, e.getMessage());
			}

			return true;
		}

		return super.onKeyDown(keyCode, event);
>>>>>>> FETCH_HEAD
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
<<<<<<< HEAD
		this.camera = camera;
		
		Log.e(LOG_NAME, "ON CALLBACK GETPARAMETER" + camera.getParameters().getPreviewFormat());
		
		try {
			init(camera);
			//saveImage(data);
			//processImage(data);
			saveImageNative(data);
=======

		try {
			init(camera);
			saveImage(data);
>>>>>>> FETCH_HEAD
			camera.addCallbackBuffer(data);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

	}

<<<<<<< HEAD
	private void processImage(byte[] data) {

		Log.i(LOG_NAME, "Save IPLIMAGE GRAY");
		// Criando uma sub-amostragem e convertendo em escala de cinza.
		int f = SUBSAMPLING_FACTOR;
		if (grayImage == null || grayImage.width() != width / f
				|| grayImage.height() != height / f) {
			grayImage = IplImage.create(width / f, height / f, IPL_DEPTH_8U, 1);
		}
		int imageWidth = grayImage.width();
		int imageHeight = grayImage.height();
		int dataStride = f * width;
		int imageStride = grayImage.widthStep();

		ByteBuffer imageBuffer = grayImage.getByteBuffer();

		for (int y = 0; y < imageHeight; y++) {
			int dataLine = y * dataStride;
			int imageLine = y * imageStride;
			for (int x = 0; x < imageWidth; x++) {
				imageBuffer.put(imageLine + x, data[dataLine + f * x]);
			}
		}

		File file = new File(Environment.getExternalStorageDirectory(),
				"imageOpenGRAYCv.png");
		cvSaveImage(file.getAbsolutePath(), grayImage);

		postInvalidate();
		this.refreshDrawableState();

=======
	private void fillBitmap(IplImage image) {
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(image.getByteBuffer());
>>>>>>> FETCH_HEAD
	}

	private void saveImage(byte[] data) {

<<<<<<< HEAD
		Log.i(LOG_NAME, "Save IPLIMAGE");
		Camera.Parameters paramCam = camera.getParameters();
		Log.d(LOG_NAME, width+ "w");
		Log.d(LOG_NAME, height+ "h");
		
		Log.d(LOG_NAME, paramCam.getPreviewFormat()+ "p");
		
		Log.d(LOG_NAME, ImageFormat.getBitsPerPixel(paramCam.getPreviewFormat())+ "h");
		
		Log.d(LOG_NAME, data.length+ "d");
		IplImage image = getIplImageFromData(data);

		cImage = image;
		
		
		
		
=======
		IplImage image = getIplImageFromData(data);

		cImage = image;
>>>>>>> FETCH_HEAD

		if (pImage == null) {
			pImage = image;
		}
<<<<<<< HEAD

		// TestOF(pImage, cImage);

		File file = new File(Environment.getExternalStorageDirectory(),
				"imageOpenCv.jpg");

		File file1 = new File(Environment.getExternalStorageDirectory(),
				"imageOpen2SaveCv.jpg");

		// IplImage yuvimage = IplImage.create(width, height, IPL_DEPTH_8U, 2);
		// yuvimage.getByteBuffer().put(data);
		//
		// IplImage bgrimage = IplImage.create(width, height, IPL_DEPTH_8U, 3);
		// cvCvtColor(image, bgrimage, CV_YUV2BGR_NV21);

		FileOutputStream out;

		try {
			cvSaveImage(file1.getAbsolutePath(), image);
			out = new FileOutputStream(file);
			IplToBitmap(image, bitmap);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			Log.e(LOG_NAME, e.getMessage());
		}
		pImage = image;

	}

	private void saveImageNative(byte[] data) {

		Log.i(LOG_NAME, "Save Native");
		FileOutputStream out;

		Camera.Parameters paramCam = camera.getParameters();
		Log.d(LOG_NAME, width+ "w");
		Log.d(LOG_NAME, height+ "h");
		Log.d(LOG_NAME, ImageFormat.getBitsPerPixel(paramCam.getPreviewFormat())+ "h");
		
		Log.d(LOG_NAME, data.length+ "d");
		
		Rect rect = new Rect(0, 0, width, height);

		File file = new File(Environment.getExternalStorageDirectory(),
				"imageNative.jpg");

		try {
			// cvSaveImage(file.getAbsolutePath(), bgrimage);
			out = new FileOutputStream(file);
			YuvImage yuvimage = new YuvImage(data, paramCam.getPreviewFormat(), width,
					height, null);
			yuvimage.compressToJpeg(rect, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			Log.e(LOG_NAME, e.getMessage());
		}
=======
		IplImage result = null;
		Log.i(LOG_NAME, "INI Convert");
		try {
			result = TestLK(cImage, pImage);
			
			IplImage bgrimage = IplImage.create(cvGetSize(result), IPL_DEPTH_8U, 4);
			//cvCvtColor(result, bgrimage, CV_YUV420sp2BGR);
			recorder.record(result);
			fillBitmap(result);
		} catch (Exception e) {
			Log.e(LOG_NAME, e.getMessage());
		}

		File file1 = new File(Environment.getExternalStorageDirectory(),
				"imageOpen2SaveCv.jpg");

		try {
			cvSaveImage(file1.getAbsolutePath(), result);
		} catch (Exception e) {
			Log.e(LOG_NAME, e.getMessage());
		}
		pImage = cImage;

		// Força a tela desenhar
		postInvalidate();
		this.refreshDrawableState();
>>>>>>> FETCH_HEAD

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

<<<<<<< HEAD
	public void TestOF(IplImage pFrame, IplImage cFrame) {

		Log.i(LOG_NAME, "Do optical flow");

		IplImage pGray = cvCreateImage(cvGetSize(pFrame), IPL_DEPTH_8U, 1);
		Mat mat1 = new Mat(pGray.asCvMat());
		IplImage cGray = cvCreateImage(cvGetSize(cFrame), IPL_DEPTH_8U, 1);
		Mat mat2 = new Mat(cGray.asCvMat());
		cvConvertImage(pFrame, pGray, IPL_DEPTH_32F);
		cvConvertImage(cFrame, cGray, IPL_DEPTH_32F);
		IplImage Optical_Flow = cvCreateImage(cvGetSize(pGray), IPL_DEPTH_32F,
				2);
		Mat mat3 = new Mat(Optical_Flow.asCvMat());

		DenseOpticalFlow tvl1 = createOptFlow_DualTVL1();
		tvl1.calc(mat1, mat2, mat3);
		Optical_Flow = mat3.asIplImage();
		FloatBuffer buffer = Optical_Flow.getFloatBuffer();
		CvMat OF = cvCreateMat(pGray.height(), pGray.width(), CV_32FC1);
		int pixelVelocity = 0;
		int xVelocity = 0;
		int yVelocity = 0;
		int bufferIndex = 0;
		for (int y = 0; y < pGray.height(); y++) {
			for (int x = 0; x < pGray.width(); x++) {
				xVelocity = (int) buffer.get(bufferIndex);
				yVelocity = (int) buffer.get(bufferIndex + 1);
				pixelVelocity = (int) Math
						.sqrt((double) (xVelocity * xVelocity + yVelocity
								* yVelocity));
				OF.put(y, x, pixelVelocity);
			}
		}
		IplImage temp = OF.asIplImage();

		File file = new File(Environment.getExternalStorageDirectory(),
				"opticalFlowResult.jpg");

		cvSaveImage(file.getAbsolutePath(), temp);
=======
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

		if (posPoints.size() > negPoints.size())
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
			e.printStackTrace();
		}

		if (bitmap != null)
			canvas.drawBitmap(bitmap, 0, 0, getPaint());
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
>>>>>>> FETCH_HEAD
	}

}
