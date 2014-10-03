package br.com.mono.squest;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String LOG_NAME = "OPENCV";
	
	SurfaceHolder surfaceHolder;
	Camera camera;
	Camera.PreviewCallback previewCallback;
	Context context;

	@SuppressWarnings("deprecation")
	Preview(Context context, Camera.PreviewCallback previewCallback) {
		super(context);
		this.previewCallback = previewCallback;
		this.context = context;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open(0);
		setWillNotDraw(false);
		try {
			camera.setPreviewDisplay(holder);
			setCameraDisplayOrientation((Activity) context,
					CameraInfo.CAMERA_FACING_BACK, camera);
		} catch (IOException exception) {
			camera.release();
			camera = null;
			// TODO: add more exception handling logic here
		}

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int widht, int height) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) widht / height;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = height;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = camera.getParameters();
		parameters.getSupportedPreviewFormats();
		
		Log.i("OPENCV", "CAMERA PREVIEW FORMAT:" + String.valueOf(parameters.getPreviewFormat()));

		List<Size> sizes = parameters.getSupportedPreviewSizes();
		Size optimalSize = getOptimalPreviewSize(sizes, width, height);
		parameters.setPreviewSize(width, height);
		
		//camera.setParameters(parameters);
		if (previewCallback != null) {
			camera.setPreviewCallbackWithBuffer(previewCallback);
			Camera.Size size = parameters.getPreviewSize();
			byte[] data = new byte[size.width
					* size.height
					* ImageFormat
							.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
			camera.addCallbackBuffer(data);
		}
		camera.startPreview();
	}

	// Camera inicialmente fica rotacionada =/
	public static void setCameraDisplayOrientation(Activity activity,
			int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {

	    Log.w(LOG_NAME, "On Draw Called SURF");
	}
}