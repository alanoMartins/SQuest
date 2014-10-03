package br.com.mono.squest;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_video.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_video.DenseOpticalFlow;

public class OpenCvView extends View implements Camera.PreviewCallback {
	private static final String LOG_NAME = "OPENCV";
	public static final int SUBSAMPLING_FACTOR = 4;

	private IplImage grayImage;
	private Bitmap bitmap;

	private int width = 0;
	private int height = 0;

	private IplImage pImage;
	private IplImage cImage;

	List<Point2d> pointsToDraw;

	private CvMat opticalFlow;
	private Mat cOpticalFlow;

	private Paint mPaint;

	private Camera camera;

	public OpenCvView(Context context) throws IOException {
		super(context);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		this.camera = camera;
		pointsToDraw = new ArrayList<Point2d>();

		Log.e(LOG_NAME, "ON CALLBACK GETPARAMETER"
				+ camera.getParameters().getPreviewFormat());

		try {
			init(camera);
			Log.i(LOG_NAME, "On Preview");
			fillBitmap(data);
			// fillOf(data);
			saveImage(data);
			//processImage(data);
			saveImageNative(data);
			camera.addCallbackBuffer(data);
			//this.invalidate();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

	}

	private void fillOf(byte[] data) {

		Log.i(LOG_NAME, "FILL OPT FLOW");
		IplImage image = getIplImageFromData(data);

		Log.i(LOG_NAME, "FILL OPT FLOW 1");

		cImage = image;
		if (pImage != null) {
			pImage = image;
		}

		Log.i(LOG_NAME, "FILL OPT FLOW 2");
		opticalFlow = cImage.asCvMat();

		pImage = cImage;

	}

	private void fillBitmap(byte[] data) {
		IplImage image = getIplImageFromData(data);
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(image.getByteBuffer());
	}

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

		// Deve ser igual ao grayImage.widthStep()
		int widthResult = imageWidth * 1;

		Log.i(LOG_NAME, "widthResult: " + widthResult);
		Log.i(LOG_NAME, "imageStride: " + imageStride);

		ByteBuffer imageBuffer = grayImage.getByteBuffer();

		for (int y = 0; y < imageHeight; y++) {
			int dataLine = y * dataStride;
			int imageLine = y * imageStride;
			for (int x = 0; x < imageWidth; x++) {
				imageBuffer.put(imageLine + x, data[dataLine + f * x]);
			}
		}

		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.copyPixelsFromBuffer(grayImage.getByteBuffer());

		File file = new File(Environment.getExternalStorageDirectory(),
				"imageOpenGRAYCv.png");
		cvSaveImage(file.getAbsolutePath(), grayImage);

		postInvalidate();
		this.refreshDrawableState();

	}

	private void saveImage(byte[] data) {

		// Log.i(LOG_NAME, "Save IPLIMAGE");
		Camera.Parameters paramCam = camera.getParameters();
		// Log.d(LOG_NAME, width + "w");
		// Log.d(LOG_NAME, height + "h");

		// Log.d(LOG_NAME, paramCam.getPreviewFormat() + "p");

		// Log.d(LOG_NAME,
		// ImageFormat.getBitsPerPixel(paramCam.getPreviewFormat()) + "h");

		// Log.d(LOG_NAME, data.length + "d");
		IplImage image = getIplImageFromData(data);

		cImage = image;

		if (pImage == null) {
			pImage = image;
		}

		pointsToDraw = calcPoints(new Mat(cImage));
		// cOpticalFlow = new Mat(cImage);
		IplImage opflw =  TestOF(pImage, cImage).asIplImage();

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
			cvSaveImage(file1.getAbsolutePath(), opflw);
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
		Log.d(LOG_NAME, width + "w");
		Log.d(LOG_NAME, height + "h");
		Log.d(LOG_NAME,
				ImageFormat.getBitsPerPixel(paramCam.getPreviewFormat()) + "h");

		Log.d(LOG_NAME, data.length + "d");

		Rect rect = new Rect(0, 0, width, height);

		File file = new File(Environment.getExternalStorageDirectory(),
				"imageNative.jpg");

		try {
			// cvSaveImage(file.getAbsolutePath(), bgrimage);
			out = new FileOutputStream(file);
			YuvImage yuvimage = new YuvImage(data, paramCam.getPreviewFormat(),
					width, height, null);
			yuvimage.compressToJpeg(rect, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			Log.e(LOG_NAME, e.getMessage());
		}

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

	public CvMat TestOF(IplImage pFrame, IplImage cFrame) {

		Log.i(LOG_NAME, "Do optical flow");

		IplImage pGray = pFrame;
		Mat mat1 = new Mat(pGray.asCvMat());
		IplImage cGray = cFrame;
		Mat mat2 = new Mat(cGray.asCvMat());
		cvConvertImage(pFrame, pGray, IPL_DEPTH_32F);
		cvConvertImage(cFrame, cGray, IPL_DEPTH_32F);
		IplImage Optical_Flow = IplImage
				.create(width, height, IPL_DEPTH_32F, 2);
		Mat mat3 = new Mat(Optical_Flow.asCvMat());

		DenseOpticalFlow tvl1 = createOptFlow_DualTVL1();
		tvl1.calc(mat1, mat2, mat3);
		Optical_Flow = mat3.asIplImage();
		FloatBuffer buffer = Optical_Flow.getFloatBuffer();
		CvMat OF = cvCreateMat(pGray.height(), pGray.width(), CV_32F);
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
		// IplImage temp = OF.asIplImage();

		return OF;

		// File file = new File(Environment.getExternalStorageDirectory(),
		// "opticalFlowResult.jpg");
		//
		// cvSaveImage(file.getAbsolutePath(), temp);
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

	private List<Point2d> calcPoints(Mat matResult) {

		List<Point2d> points = new ArrayList<Point2d>();

		for (int row = 0; row < matResult.rows(); row++) {

			for (int col = 0; col < matResult.cols(); col++) {

				Mat mat = matResult.row(row).col(col);
				points.add(new Point2d(mat.getFloatBuffer()));

			}
		}
		return points;
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
