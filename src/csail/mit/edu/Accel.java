package csail.mit.edu;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class Accel extends Service{
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private SensorEventListener acclSensorListener;
	private double prevTime = 0;
	private Timer timer;
	private WakeLock wl;
	private PowerManager pm;

	
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Bundle extras = intent.getExtras();
		int samplingFreq = extras.getInt("Sampling Freq");
		int dutyCycleRate = extras.getInt("Duty Cycle");
		System.out.println("Accel service starts -- sampling Freq:" + samplingFreq + ", dutyCyclePrecentage:" + dutyCycleRate);
		
		
		pm = (PowerManager) Global.context.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
		
        timer = new Timer();
		acclSensorListener = new AcclListener();
		sensorManager = (SensorManager)Global.context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
		
        
        long triggerTime = (long)(dutyCycleRate * 0.01 * 60);
        timer.schedule(new AccelTask(), triggerTime * 1000); 
		sensorManager.registerListener(acclSensorListener, accelerometer, samplingFreq);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	class AccelTask extends TimerTask {
		@Override
		public void run() {
			sensorManager.unregisterListener(acclSensorListener);
			timer.cancel();
			wl.release();
			stopSelf();
			System.out.println("Accel service stopped.");
		}
		
	}
	 private class AcclListener implements SensorEventListener
	    {  
		  public void onSensorChanged(SensorEvent event)
	      {
			   // double curTime = (double)System.currentTimeMillis()/(1000.0);
			   // double freq = 1/ (curTime - prevTime);
			   //System.out.println("accel received, sampling freq: " + freq  );
			   // prevTime = curTime;
	      }

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {	
		}
	 } 
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
