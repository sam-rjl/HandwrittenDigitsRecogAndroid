
package jp.narr.tensorflowmnist;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

	private CameraBridgeViewBase mOpenCvCameraView;
	private boolean mIsFrontCamera = false;
	private MenuItem mItemSwitchCamera = null;
	static int REQUEST_CAMERA = 0;
	static boolean read_external_storage_granted = false;
	private Bitmap finaldigit = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888);
	private DigitDetector mDetector = new DigitDetector();
	private Mat mRgba;
	//private final Size size = new Size(10,10);

	private BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status){
				case BaseLoaderCallback.SUCCESS:
					Log.i("opencv", "加载成功");
					mOpenCvCameraView.enableView();
					break;
				default:
					super.onManagerConnected(status);
					break;
			}
		}
	};
	public static class VibrateHelp {
		private static Vibrator vibrator;
		/**
		 * @ClassName:VibrateHelp - 简单的震动
		 * @author:CaoJiaHao
		 * @Param:context 调用震动类的 context
		 * @param:millisecond 震动的时间
		 */
		@SuppressWarnings("static-access")
		public static void vSimple(Context context, int millisecode) {
			vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
			vibrator.vibrate(millisecode);
		}
		/**
		 * @param : pattern 震动的形式
		 * @param : repeate 震动循环的次数
		 * @ClassName:VibrateHelp - 复杂的震动
		 * @author:CaoJiaHao
		 * @Param: context 调用复杂震动的context
		 **/
		@SuppressWarnings("static-access")
		public static void vComplicated(Context context, long[] pattern, int repeate) {
			vibrator = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
			vibrator.vibrate(pattern, repeate);
		}
		/**
		 *@ClassName:VibrateHelp - 停止震动
		 *@author:CaoJiaHao
		 **/
		public static void stop() {
			if (vibrator != null)
				vibrator.cancel();
		}
	}
	public class ViewClickVibrate implements View.OnClickListener {
		private final int VIBRATE_TIME = 60;
		@Override
		public void onClick(View v) {
			VibrateHelp.vSimple(v.getContext(), VIBRATE_TIME);
		}
	}
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		boolean ret = mDetector.setup(this);
		if( !ret ) {
			Log.i("MainActivity", "Detector setup failed");
			return;
		}
		if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED){
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
		}else {
			read_external_storage_granted = true;
		}

		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallBack);
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		Button btn_call = (Button) findViewById(R.id.button);
		//给button按钮设置一个点击事件
		btn_call.setOnClickListener(new ViewClickVibrate() {
			@Override
			public void onClick(View v) {
				super.onClick(v);
				Catch(v);
			}
		});

	}
	private void Catch(View v)
	{
		// 获取屏幕
		View dView = getWindow().getDecorView();
//		View dView = v.getRootView();
		dView.setDrawingCacheEnabled(true);
		dView.buildDrawingCache();
		Bitmap bmp = dView.getDrawingCache();


		if (bmp != null)
		{
			try {
				// 获取内置SD卡路径
				String sdCardPath = Environment.getExternalStorageDirectory().getPath();
				// 图片文件路径
				Random random = new Random();
				int rannum = (int)(random.nextDouble() * (99999 - 10000 + 1)) + 10000;
				String filePath = sdCardPath + File.separator + "screenshot"+rannum+".png";

				File file = new File(filePath);
				FileOutputStream os = new FileOutputStream(file);
				bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
				os.flush();
				os.close();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		//mRgba = new Mat(height, width, CvType.CV_8UC4);
		mRgba = new Mat( width,height, CvType.CV_8UC4);

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		mItemSwitchCamera = menu.add("切换前置/后置摄像头");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String toastMessage;
		if (item == mItemSwitchCamera){
			mOpenCvCameraView.setVisibility(SurfaceView.GONE);
			mIsFrontCamera = !mIsFrontCamera;
			if (mIsFrontCamera){
				mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
				mOpenCvCameraView.setCameraIndex(1);
				toastMessage = "Front Camera";
			}else {
				mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
				mOpenCvCameraView.setCameraIndex(-1);//-1
				toastMessage = "Back Camera";
			}
			mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
			mOpenCvCameraView.setCvCameraViewListener(this);
			mOpenCvCameraView.enableView();
			Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
			toast.show();
		}
		return true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CAMERA){
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
				read_external_storage_granted = true;
			}else {
				Log.i("permission", "CAMERA denied");
			}
		}
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
	}

	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
		/***************************
		 mRgba = inputFrame.rgba();
		 Mat mRgba_gray = new Mat(mRgba.height(), mRgba.width(), CvType.CV_8UC1);
		 Imgproc.cvtColor(mRgba, mRgba_gray, Imgproc.COLOR_BGR2GRAY);
		 Imgproc.threshold(mRgba_gray, mRgba_gray, 100, 255, Imgproc.THRESH_BINARY);
		 Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
		 Imgproc.dilate(mRgba_gray, mRgba_gray, kernelDilate);
		 Imgproc.erode(mRgba_gray, mRgba_gray, kernelDilate);
		 Imgproc.cvtColor(mRgba_gray, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
		 //mRgba = cut_ROI(mRgba);
		 return mRgba;
		 ****************************/

		mRgba = inputFrame.rgba();

/***
		Mat rotateMat = Imgproc.getRotationMatrix2D(new Point(mRgba.rows() / 2, mRgba.cols() / 2), -90, 1);
		Imgproc.warpAffine(mRgba, mRgba, rotateMat, mRgba.size());
		if (mIsFrontCamera){
			Core.flip(mRgba, mRgba, 0);
		}
		***/
		//Mat k = new Mat();
		Core.rotate(mRgba,mRgba,Core.ROTATE_90_CLOCKWISE);
		if (mIsFrontCamera) Core.rotate(mRgba,mRgba,Core.ROTATE_90_COUNTERCLOCKWISE);
		mRgba = cut_ROI(mRgba);

		return mRgba;

	}

	Mat cut_ROI(Mat src){
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Mat dst = new Mat();
		Mat tem = new Mat();
		Mat pred = new Mat();
		StringBuffer num = new StringBuffer();

//		int[] array = new int[784];
//		Arrays.fill(array, 125);
		//Bitmap finaldigit = Bitmap.createBitmap(28, 28, Bitmap.Config.RGB_565);
		src.copyTo(dst);
		Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2GRAY);
		Imgproc.blur(dst, dst, new Size(3, 3));//dst模糊
		Imgproc.threshold(dst, tem, 120, 255, Imgproc.THRESH_BINARY_INV);//tem 灰度图
		Imgproc.findContours(tem, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE); //hierarchy

		for (MatOfPoint point : contours){
			Rect rects = Imgproc.boundingRect(point);//每个边框选中的数字
			if (rects.width > 15 && rects.height > 60 && rects.width < 500 && rects.height < 500){
				//Rect rect1 = new Rect((int)(rects.tl().x) - 2, (int)(rects.tl().y) - 2, rects.width + 4, rects.height + 4);
//				rects.width += 10;
//				rects.height += 10;
				Imgproc.threshold(tem.submat(rects), pred, 80, 255, CvType.CV_8UC1);
				Core.copyMakeBorder(pred, pred, 10, 10, 10, 10, Core.BORDER_CONSTANT, new Scalar(0));
				Imgproc.resize(pred, pred, new Size(28,28));
				//Core.copyMakeBorder(pred, pred, 8, 8, 8, 8, Core.BORDER_CONSTANT, new Scalar(0));
				pred.convertTo(pred, CvType.CV_32S);

				Mat pred1 = pred.reshape(0,1);
				int[] temp = new int[(int) (pred1.total()*pred1.channels())];
				pred.get(0,0,temp);
//				final int[] retPixels = new int[temp.length];
//				for (int i = 0; i < temp.length; ++i){
//					//Set 0 for white and 255 for black pixel
//					int pix = temp[i];
//					int b = pix & 0xff;
//					retPixels[i] = 0xff - b;
//				}
				//System.out.println(retPixels.length + ": " + retPixels[retPixels.length-1]);

				//Utils.matToBitmap(pred, finaldigit);
				//int[] piex = new int[finaldigit.getWidth()*finaldigit.getHeight()];
				//finaldigit.getPixels(piex, 0, finaldigit.getWidth(), 0, 0, finaldigit.getWidth(), finaldigit.getHeight());
				//System.out.println(piex[1]);
				//int pixels[] = getPixelData0(temp);
				//System.out.println(pixels[783]);
				int predict = mDetector.detectDigit(temp);
				System.out.println(predict);
				num.append(predict);
				Imgproc.rectangle(src, rects.tl(), rects.br(), new Scalar(0,255,0), 2);
				Imgproc.putText(src, Integer.toString(predict), rects.tl(), Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(255, 0, 0), 2);
				//break;
			}
		}
		//String num1="";
		//for (int j=0;j<i;j++) num1=num1+num[j];
		final TextView helloTextView = (TextView) findViewById(R.id.textView);
		helloTextView.setText(num.toString());
		
		return src;
	}

	public int[] getPixelData0(int[] temp){
		if (temp == null){
			return null;
		}
		//Get 28*28 pixel data from bitmap
		int[] pixels = new int[temp.length];
		int[] retPixels = new int[pixels.length];
		for (int i = 0; i < pixels.length; ++i){
			//Set 0 for white and 255 for black pixel
			int pix = pixels[i];
			int b = pix & 0xff;
			retPixels[i] = 0xff - b;
		}
		return retPixels;
	}

	public int[] getPixelData(Bitmap bitmap){
		if (bitmap == null){
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		//Get 28*28 pixel data from bitmap
		int[] pixels = new int[width*height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
		int[] retPixels = new int[pixels.length];
		for (int i = 0; i < pixels.length; ++i){
			//Set 0 for white and 255 for black pixel
			int pix = pixels[i];
			int b = pix & 0xff;
			retPixels[i] = 0xff - b;
		}
		return retPixels;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(mOpenCvCameraView != null){
			mOpenCvCameraView.disableView();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		//int[] array = new int[784];
		//Arrays.fill(array, 125);
		//predict = mDetector.detectDigit(array);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null){
			mOpenCvCameraView.disableView();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

}

