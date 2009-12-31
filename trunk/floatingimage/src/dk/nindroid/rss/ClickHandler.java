package dk.nindroid.rss;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Vibrator;
import android.view.MotionEvent;
import dk.nindroid.rss.gfx.Vec2f;
import dk.nindroid.rss.uiActivities.Toaster;

public class ClickHandler extends TimerTask {
	private static RiverRenderer renderer;
	private static Vec2f 	mTouchLastPos;
	private static Vec2f	mTouchStartPos;
	private static Timer	mClickTimer;
	private static final int LONGCLICKTIME = 1000;
	private static long 	mMoveTime;
	private static float[]	mLastSpeedX = new float[3];
	private static float[]	mLastSpeedY = new float[3];
	
	private static int 		mAction = 0; // 0: nothing, 1: move, 2: long click, 3: ??
	
	private final static int ACTION_NOTHING 	= 0;
	private final static int ACTION_MOVE    	= 1;
	private final static int ACTION_LONG_CLICK	= 2;
	
	public static void init(RiverRenderer renderer){
		ClickHandler.renderer = renderer;
		mTouchLastPos = new Vec2f();
		mTouchStartPos = new Vec2f();
	}
	
		
	@Override
	public void run() {
		
		mAction = ACTION_LONG_CLICK;
		((Vibrator)ShowStreams.current.getSystemService(Activity.VIBRATOR_SERVICE)).vibrate(100l);
		Intent intent = renderer.followSelected();
		if(intent != null){
			ShowStreams.current.startActivity(intent);
		}else{
			ShowStreams.current.runOnUiThread(new Toaster("No image selected..."));
		}
	}
	
	public static boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX(); float y = event.getY();
		float lastX = mTouchLastPos.getX();
		float lastY = mTouchLastPos.getY();
		if(action ==  MotionEvent.ACTION_DOWN){
			mMoveTime = -1;
			mAction = ACTION_NOTHING;
			mTouchStartPos.set(x, y);
			mTouchLastPos.set(mTouchStartPos);
			mClickTimer = new Timer();
			mClickTimer.schedule(new ClickHandler(), LONGCLICKTIME);
		}else if(action ==  MotionEvent.ACTION_UP && mAction == ACTION_NOTHING){
			// Click
			mClickTimer.cancel();
			renderer.onClick(x,y);
		}else if(action == MotionEvent.ACTION_MOVE){
			if(Math.abs(mTouchStartPos.getX() - x) > 40.0f || Math.abs(mTouchStartPos.getY() - y) > 40.0f){
				mClickTimer.cancel();
				mAction = ACTION_MOVE;
			}
			if(mAction == ACTION_MOVE){
				float diffX = x - lastX;
				float diffY = y - lastY;
				saveSpeed(diffX, diffY);
				renderer.xChanged(diffX);
				renderer.yChanged(diffY);
				mMoveTime = new Date().getTime();
			}
			mTouchLastPos.setX(x);
			mTouchLastPos.setY(y);
		}else if(action == MotionEvent.ACTION_UP && mAction == ACTION_MOVE){
			//mDragDecelerator.setSpeed((mLastSpeed[0] + mLastSpeed[1] + mLastSpeed[2]) / 3.0f);
			float speedX = (mLastSpeedX[0] + mLastSpeedX[1] + mLastSpeedX[2]) / 3.0f;
			float speedY = (mLastSpeedY[0] + mLastSpeedY[1] + mLastSpeedY[2]) / 3.0f;
			speedX = Math.min(speedX, 25.0f);
			speedX = Math.max(speedX, -25.0f);
			speedY = Math.min(speedY, 25.0f);
			speedY = Math.max(speedY, -25.0f);
			renderer.moveRelease(speedX, speedY, new Date().getTime());
		}
		return true;
	}
	
	private static void saveSpeed(float diffX, float diffY){
		long timeDiff = new Date().getTime() - mMoveTime;
		mLastSpeedX[2] = mLastSpeedX[1];
		mLastSpeedX[1] = mLastSpeedX[0];
		float speed = diffX / timeDiff * 10.0f;
		mLastSpeedX[0] = speed;
		
		mLastSpeedY[2] = mLastSpeedY[1];
		mLastSpeedY[1] = mLastSpeedY[0];
		speed = diffY / timeDiff * 10.0f;
		mLastSpeedY[0] = speed;
	}
}
