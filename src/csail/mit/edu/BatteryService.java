package csail.mit.edu;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class BatteryService extends Service{
	
	static public final int MSG_SETTINGS = 1;
	private final Messenger mMessenger = new Messenger(new IncomingHandler()); 


    private GPS gpssensor;
  
    
    private boolean accelDutyCycleEnable;
    private int wifiSampleRateMs = 10*1000;
    private final int accelSampleRateMs = 0;
    private final int gpsSampleRateMs = 1000;
    private final int gsmSampleRateMs = 1000;
    private final int gyroSampleRateMs = 0;
    private final int accelDCSampleRateMs = 0;
	private PowerManager.WakeLock wakelock;
	private File root;
    private BatteryReceiver batteryReceiver;
    private String filename;
    private FileWriter filewriter;
    private PowerManager power;
	public enum Sensors {ACCEL,WIFI,GPS,GSM,Gyro};

	
	
	public void onCreate(){

		gpssensor = new GPS();
    	
    	/** Write battery level into SD card... will use this later
    	 * root = Environment.getExternalStorageDirectory();
    	constructFileName();
		try{
		filewriter = new FileWriter(new File(root, filename),true);
		}catch (IOException e) { throw new RuntimeException(e); }	
	
		batteryReceiver = new BatteryReceiver();
		registerReceiver(batteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED)); 		
    	**/
		
    	
	}
	public void onDestroy() 
    {
		CancelAlarm();
		//gpssensor.stop();
        Toast.makeText(this, "Battery service stopped", Toast.LENGTH_SHORT).show();
    }

    private void startNewSettings(String setting){
    	
    	String settingSeg[] = setting.split(",");
    	//Format: sensorID, accelDutyCyle, accelSamplingFreq, radioSamplingInterval, GPSOpTime, GpsSamplingInterval
    	
    	CancelAlarm();
    	switch(Integer.parseInt(settingSeg[0])){
    		case 0: // wifi
    			
    			 Toast.makeText(Global.context, "WiFi started, sampling interval:" + Integer.parseInt(settingSeg[3]) + "sec(s)", Toast.LENGTH_SHORT).show();	
    			 Intent WiFiIntent = new Intent(Global.context, WiFi.class);
    	         PendingIntent WiFiPI = PendingIntent.getService(Global.context, 0, WiFiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    	         AlarmManager WiFiAM = (AlarmManager) Global.context.getSystemService(Context.ALARM_SERVICE); 
    	         WiFiAM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Integer.parseInt(settingSeg[3]) * 1000, WiFiPI);
    	    
    			
    			
    			break;
    		case 1:  // gsm
    			
    			 Toast.makeText(Global.context, "GSM started, sampling interval:" + Integer.parseInt(settingSeg[3]) + "sec(s)", Toast.LENGTH_SHORT).show();	
    			 Intent GSMIntent = new Intent(Global.context, GSM.class);
    	         PendingIntent GSMPI = PendingIntent.getService(Global.context, 0, GSMIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    	         AlarmManager GSMAM = (AlarmManager) Global.context.getSystemService(Context.ALARM_SERVICE); 
    	         GSMAM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Integer.parseInt(settingSeg[3]) * 1000, GSMPI);
    	    
    			
    			
    			break;
    		case 2:  // gps
    			//Toast.makeText(Global.context, "GPS started, sampling interval:" + Integer.parseInt(settingSeg[5]) + " Operate Time:" + Integer.parseInt(settingSeg[4]), Toast.LENGTH_SHORT).show();	
    			Toast.makeText(Global.context, "GPS started, sampling interval:" + Integer.parseInt(settingSeg[5]), Toast.LENGTH_SHORT).show();	
    			
    			//gpssensor.run(Integer.parseInt(settingSeg[5]) * 1000);
    			/**
    			  Intent GPSIntent = new Intent(Global.context, Accel.class);
    			 GPSIntent.putExtra("Sampling Interval", Integer.parseInt(settingSeg[5]));
    			 GPSIntent.putExtra("Operate Time", Integer.parseInt(settingSeg[4]));
    			 
    	         PendingIntent GPSPI = PendingIntent.getService(Global.context, 0, GPSIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    	         AlarmManager GPSAM = (AlarmManager) Global.context.getSystemService(Context.ALARM_SERVICE); 
    	         GPSAM.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), GPSPI);
    	         
    	         //GPSAM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, GPSPI);   
    			**/
    			
    			
    			break; 
    		case 3:  //accel
    			
    			 Toast.makeText(Global.context, "Accel started, sampling Freq Option:" + Integer.parseInt(settingSeg[2]) + " Duty cycle:" + Integer.parseInt(settingSeg[1]), Toast.LENGTH_SHORT).show();	
    			 Intent AccelIntent = new Intent(Global.context, Accel.class);
    			 AccelIntent.putExtra("Sampling Freq", Integer.parseInt(settingSeg[2]));
    			 AccelIntent.putExtra("Duty Cycle", Integer.parseInt(settingSeg[1]));
    			 
    	         PendingIntent AccelPI = PendingIntent.getService(Global.context, 0, AccelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    	         AlarmManager AccelAM = (AlarmManager) Global.context.getSystemService(Context.ALARM_SERVICE); 
    	         AccelAM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, AccelPI);   
    			
    			break;	
    	}	
    }
	@Override
	public IBinder onBind(Intent intent) {
		 return mMessenger.getBinder();
	}
	public boolean onUnbind(Intent intent){
		System.out.println("Service onbound...");
		return false;
	}
	class IncomingHandler extends Handler{
		 public void handleMessage(Message msg) {
	           switch(msg.what){
	           
	               case MSG_SETTINGS :
	            	 //sensorID, accelDutyCyle, accelSamplingFreq, radioSamplingInterval, GPSOpTime, GpsSamplingInterval
	            	   String settings = (String) msg.obj;
	            	   System.out.println("setting:" +  settings);
	            	   startNewSettings(settings);
	            	   
	               break;	
	            	   	
	           	    default: 
	            	    super.handleMessage(msg);       	   
	           }
			  
		 }
		
	}
    
	class BatteryReceiver extends BroadcastReceiver{
	    public void onReceive(Context arg0, Intent intent) {  
	    	String action = intent.getAction();
	    	 
	    	if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
	                int level = intent.getIntExtra("level", 0);
	                //int scale = intent.getIntExtra("scale", 0);
	                //int voltage = intent.getIntExtra("voltage", 0);
	    	    	 try
	    	  		{
	    	    		 String buffer = System.currentTimeMillis()+ ","+ level + "\n";
	    	    		 filewriter.write(buffer);	
	 	                 filewriter.flush();
	    	    		 /**if(level == 0){
	    	    			filewriter.close();    			       
	    	    			unregisterReceiver(batteryReceiver);
	    	    		 }**/
	    	  			 
	    	  		}
	    	  		catch (IOException e) { throw new RuntimeException(e); }	
	    }  
	 }  
	}
	
	 public void CancelAlarm()
	 {
	     Intent intent = new Intent(Global.context, WiFi.class);
	     PendingIntent sender = PendingIntent.getService(Global.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	     AlarmManager alarmManager = (AlarmManager) Global.context.getSystemService(Context.ALARM_SERVICE);
	     alarmManager.cancel(sender);
	     
	     intent = new Intent(Global.context, GSM.class);
	     sender = PendingIntent.getService(Global.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	     alarmManager.cancel(sender);
	     
	     intent = new Intent(Global.context, Accel.class);
	     sender = PendingIntent.getService(Global.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	     alarmManager.cancel(sender);
	     
	     //gpssensor.stop();
	    /**
	     intent = new Intent(Global.context, GPS.class);
	     sender = PendingIntent.getService(Global.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	     alarmManager.cancel(sender);
	     **/
	    	     
	 }
	/**
	public void constructFileName(){
    	filename = "battery";

		for(Sensors s: Sensors.values()){
			switch (s){
			case ACCEL:
				if(accelEnable)
					filename = filename + "_" + s.name()+ accelSampleRateMs;
				break;
			case WIFI:
				if(wifiEnable)
					filename = filename + "_" + s.name()+ wifiSampleRateMs;
				break;
			case GPS:
				if(gpsEnable)
					filename = filename + "_" + s.name()+ gpsSampleRateMs;
				break;
			case GSM:
				if(gsmEnable)
					filename = filename + "_" + s.name()+ gsmSampleRateMs;
				break;
			case Gyro:
				if(gyroEnable)
					filename = filename + "_" + s.name()+ gyroSampleRateMs;
				break;
			default:
				break;
			}
			
		}
	}**/
}
