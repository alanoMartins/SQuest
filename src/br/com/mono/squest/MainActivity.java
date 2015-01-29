package br.com.mono.squest;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MainActivity extends Activity{
	
	private Preview mPreview;
	private FrameLayout layout;
	private OpenCvView openCvView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
<<<<<<< HEAD
		
=======
>>>>>>> FETCH_HEAD
		try {
			layout = new FrameLayout(this);
			openCvView = new OpenCvView(this);
			mPreview = new Preview(this, openCvView);
			layout.addView(mPreview);
			layout.addView(openCvView);
			setContentView(layout);
		} catch (IOException e) {
			e.printStackTrace();
			new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
		}
		
	}
	
	
}