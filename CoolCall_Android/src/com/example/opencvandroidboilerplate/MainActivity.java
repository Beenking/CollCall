package com.example.opencvandroidboilerplate;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import com.example.opencvandroidboilerplate.R;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

public class MainActivity extends Activity implements CvCameraViewListener2,SensorEventListener {
	// camera manager
	private Mat input;
	private Mat displayedMat; // output Mat
	final String TAG = "CoolCall";
	private CameraBridgeViewBase mOpenCvCameraView;
	// sensor manager
	private boolean sw = false;
	private SensorManager sManager;//�������������
	private Sensor sensor;
	// sound manager
	private SoundPool soundPool;
	private int soundId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    Log.d(TAG, "called onCreate");
	    super.onCreate(savedInstanceState);
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    setContentView(R.layout.activity_main);
	    // camera manager
	    mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.CameraView	);
	    mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
	    mOpenCvCameraView.setCvCameraViewListener(this);
	    // sensor manager
	    sManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		sensor = sManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);	    
		// sound manager
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundId = soundPool.load(this, R.raw.sound, 1);
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	    @Override
	    public void onManagerConnected(int status) {
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {
	                Log.i(TAG, "OpenCV loaded successfully");
	                System.loadLibrary("process_frame");
	                
	                mOpenCvCameraView.enableView();
	            } break;
	            default:
	            {
	                super.onManagerConnected(status);
	            } break;
	        }
	    }
	};

	@Override
	public void onResume()
	{
		Log.d(TAG, "called onResume");
	    super.onResume();
	    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	    sManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onPause()
	{
		Log.d(TAG, "called onPause");
	    super.onPause();
	    if (mOpenCvCameraView != null)
	        mOpenCvCameraView.disableView();
	}
	
	public void onStop(){
		super.onStop();
		Log.d(TAG,"called onstop");
		if(sManager != null){
			//localWakeLock.release();//�ͷŵ�Դ����������ͷ�finish���acitivity����Ȼ�����Զ�������Ч�������ſ�����һ��
			sManager.unregisterListener(this);//ע������������
		}
	}

	public void onDestroy() {
		Log.d(TAG, "called onDestroy");
	    super.onDestroy();
	    if (mOpenCvCameraView != null)
	        mOpenCvCameraView.disableView();
	    soundPool.release();
		soundPool = null;
	}

	public void onCameraViewStarted(int width, int height) {
		Log.d(TAG, "called onCameraViewStarted");
		displayedMat = new Mat(height, width, CvType.CV_8UC3);
	}

	public void onCameraViewStopped() {
		Log.d(TAG, "called onCameraViewStopped");
		input.release();
		displayedMat.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		int[] phoneNumber = new int[11];
		input = inputFrame.rgba();
		if(sw==false){
			return input;
		}else{
			int i = processFrame(input.getNativeObjAddr(), displayedMat.getNativeObjAddr(), phoneNumber);
			String strNumber = "";
			for(int j=0;j<=10;j++){
				strNumber += phoneNumber[j];
			}
			if(0==i){
				Uri uri = Uri.parse("tel:"+strNumber);  
			    Intent it = new Intent(Intent.ACTION_DIAL, uri);  
			    startActivity(it);
				//soundPool.play(soundId, 1.0f, 0.5f, 1, 1, 1.0f);
				;
			}
			//Toast.makeText(this, strNumber, 10).show();
			Log.d(TAG, "return nubmer = "+ i);
			Log.d(TAG, "phoneNumber---->"+ strNumber);
			return displayedMat;
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		float[] its = event.values;
		if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			//�������ԣ��������������Ӧ����ʱ��its[0]����ֵΪ0.0�������뿪ʱ����1.0
			if (its[0] == 0.0) {// �����ֻ�
				Log.d(TAG,"hands up in calling activity");
				sw = true;
			} else {// Զ���ֻ�
				Log.d(TAG,"hands moved in calling activity");
				sw = false;
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	public native int processFrame(long matAddrInRGBA, long matAddrOutInRGBA, int[] phoneNum);
}
