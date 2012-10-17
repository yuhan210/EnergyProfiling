package csail.mit.edu;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;


public class BatteryAnalysisActivity extends Activity{

	//private Messenger mService = null;
	//private boolean mIsBound;
	//private final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    private RadioGroup radioSensorGroup;
    private RadioGroup accelSampleFreq;
    private EditText accelDutyCycleEditText;
    private EditText radioSamplingIntervalEditText;
    private EditText gpsOperationText;
    private EditText gpsSamplingIntervalText;
    private ImageButton launchButton;
    
    private boolean[] sensorEnable = new boolean[7]; // wifi, gsm, gps, accel, GPS w/o duty cycling, NWLoc, NWLoc w/o duty cycling
    private int accelDutyCycle;
    private boolean[] accelSamplingFreq = new boolean[4];
    private int radioSamplingInterval;
    private int GPSOpTime;
    private int GPSSamplingInterval;
    

    
	/**private ServiceConnection mConnection = new ServiceConnection() 
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
			Toast.makeText(BatteryAnalysisActivity.this, "Service connected", Toast.LENGTH_SHORT).show();			
		}
		public void onServiceDisconnected(ComponentName className) 
		{
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			Toast.makeText(BatteryAnalysisActivity.this, "Service disconnected", Toast.LENGTH_SHORT).show();
		}
	};**/
	
	public void setDefaultSettings(){		
	    for(int i = 0; i < sensorEnable.length; ++i){
        	sensorEnable[i] = false;
	    }
	    for(int i = 0; i < accelSamplingFreq.length; ++i){
	    	accelSamplingFreq[i] = false;
	    }
	    sensorEnable[0] = true; // wifi
	    accelSamplingFreq[0] = true; // fastest
 	    accelDutyCycle = 100; // %
	    radioSamplingInterval = 10;
	    GPSOpTime = 10;
	    GPSSamplingInterval = 50;
	    
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Global.setContext(this);
        setDefaultSettings();
        
        radioSensorGroup = (RadioGroup) findViewById(R.id.sensorgroup);
        radioSensorGroup.setOnCheckedChangeListener(sensorRadioGroup);
        accelDutyCycleEditText=  (EditText)findViewById(R.id.dutyCycleEditText);
        accelDutyCycleEditText.setText("100");
        accelSampleFreq = (RadioGroup) findViewById(R.id.accelFreqGroup);
        accelSampleFreq.setOnCheckedChangeListener(accelFreqGroup);
        radioSamplingIntervalEditText = (EditText)findViewById(R.id.radioSamplingInterval);
        radioSamplingIntervalEditText.setText("10");
        gpsOperationText = (EditText)findViewById(R.id.gpsOperateTime);
        gpsOperationText.setText("30");
        gpsSamplingIntervalText  = (EditText)findViewById(R.id.gpsSamplingInterval);
        gpsSamplingIntervalText.setText("50");
        launchButton = (ImageButton) findViewById(R.id.buttonOK);
        launchButton.setOnClickListener(launchListener);
        
        //doStartService();       
        //doBindService();
       
    }
	
	public void startSettings(){
		
   		 CancelAlarm();
		 for(int i = 0; i < sensorEnable.length; ++i){
	        	
			    if(sensorEnable[i]){
	        		switch(i){
	        			case 0://wifi
	        				 radioSamplingInterval = Integer.parseInt(radioSamplingIntervalEditText.getText().toString());
	        				 Toast.makeText(Global.context, "WiFi started, sampling interval:" + radioSamplingInterval + "sec(s)", Toast.LENGTH_SHORT).show();	
	            			 Intent WiFiIntent = new Intent(this, WiFi.class);
	            	         PendingIntent WiFiPI = PendingIntent.getService(this, 0, WiFiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	            	         AlarmManager WiFiAM = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE); 
	            	         WiFiAM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), radioSamplingInterval * 1000, WiFiPI);
	            	    
	        				break;
	        			case 1://gsm
	        				 radioSamplingInterval = Integer.parseInt(radioSamplingIntervalEditText.getText().toString());
	        				 Toast.makeText(Global.context, "GSM started, sampling interval:" + radioSamplingInterval + "sec(s)", Toast.LENGTH_SHORT).show();	
	            			 Intent GSMIntent = new Intent(this, GSM.class);
	            	         PendingIntent GSMPI = PendingIntent.getService(this, 0, GSMIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	            	         AlarmManager GSMAM = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE); 
	            	         GSMAM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), radioSamplingInterval * 1000, GSMPI);
	            	    
	        				break;
	        			case 2:// gps
	        				 // starts GPS every GPSSamplingInterval secs, and works for GPSOpTime secs with sampling rate 1Hz.
	        				 GPSOpTime = Integer.parseInt(gpsOperationText.getText().toString());
	        				 GPSSamplingInterval = Integer.parseInt(gpsSamplingIntervalText.getText().toString());
	        				 Toast.makeText(Global.context, "GPS started, sampling interval:" + GPSSamplingInterval + "sec(s) Operation Time:" + GPSOpTime +"sec(s)" , Toast.LENGTH_SHORT).show();	
		            			
	        				 if(GPSOpTime > GPSSamplingInterval){
	        					 GPSOpTime = GPSSamplingInterval - 1;
	        				 }
	        				 Intent GPSIntent = new Intent(this, GPS.class);
	            			 GPSIntent.putExtra("Operate Time", GPSOpTime);
	            			 
	            	         PendingIntent GPSPI = PendingIntent.getService(this, 0, GPSIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	            	         AlarmManager GPSAM = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE); 
	            	         GPSAM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), GPSSamplingInterval  * 1000, GPSPI);
	 	            	     
	            	        
	        				break;
	        			case 3:// accel
	        				 // check sampling frequency
	        				 int samplingOption = 0;
	        				 for(samplingOption = 0; samplingOption < accelSamplingFreq.length; ++samplingOption){
	        				     if(accelSamplingFreq[samplingOption]){
	        					 	break;
	        					 }		
	        				 }   
	        				 accelDutyCycle = Integer.parseInt(accelDutyCycleEditText.getText().toString());
	        				 Toast.makeText(this, "Accel started, sampling Freq Option:" + samplingOption + " Duty cycle:" + accelDutyCycle, Toast.LENGTH_SHORT).show();	
	            			 Intent AccelIntent = new Intent(this, Accel.class);
	            			 AccelIntent.putExtra("Sampling Freq", samplingOption);
	            			 AccelIntent.putExtra("Duty Cycle", accelDutyCycle);
	            			 
	            	         PendingIntent AccelPI = PendingIntent.getService(this, 0, AccelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	            	         AlarmManager AccelAM = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE); 
	            	         AccelAM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60 * 1000, AccelPI);   
	            			
	        				
	        				break;
	        			case 4: // gps without duty cycling
	        				GPSSamplingInterval = Integer.parseInt(gpsSamplingIntervalText.getText().toString());
	        				Toast.makeText(Global.context, "GPS without duty cycling started, sampling interval:" + GPSSamplingInterval + "sec(s)", Toast.LENGTH_SHORT).show();	
		            		
	        				Intent GPSNoDutyCycleIntent = new Intent(this, GPSWithoutDutyCycling.class);
	        				GPSNoDutyCycleIntent.putExtra("Sampling Interval", GPSSamplingInterval);
	            			startService(GPSNoDutyCycleIntent);
	        				
	        				break;
	        			case 5: //NWLoc with duty cycling
	        				 GPSOpTime = Integer.parseInt(gpsOperationText.getText().toString());
	        				 GPSSamplingInterval = Integer.parseInt(gpsSamplingIntervalText.getText().toString());
	        				 Toast.makeText(Global.context, "NWLocGPS started, sampling interval:" + GPSSamplingInterval + "sec(s) Operation Time:" + GPSOpTime +"sec(s)" , Toast.LENGTH_SHORT).show();	
		            			
	        				 if(GPSOpTime > GPSSamplingInterval){
	        					 GPSOpTime = GPSSamplingInterval - 1;
	        				 }
	        				 Intent NWLocGPSIntent = new Intent(this, NetworkLoc.class);
	        				 NWLocGPSIntent.putExtra("Operate Time", GPSOpTime);
	            			 
	            	         PendingIntent NWLocGPSPI = PendingIntent.getService(this, 0, NWLocGPSIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	            	         AlarmManager NWLocGPSAM = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE); 
	            	         NWLocGPSAM.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), GPSSamplingInterval  * 1000, NWLocGPSPI);
	 	            	     break;
	        			case 6: // NWLoc without duty cycling
	        				
	        				GPSSamplingInterval = Integer.parseInt(gpsSamplingIntervalText.getText().toString());
	        				Toast.makeText(Global.context, "NWLoc without duty cycling started, sampling interval:" + GPSSamplingInterval + "sec(s)", Toast.LENGTH_SHORT).show();	
		            		
	        				Intent NWLocNoDutyCycleIntent = new Intent(this, NetworkLocWithoutDutyCycling.class);
	        				NWLocNoDutyCycleIntent.putExtra("Sampling Interval", GPSSamplingInterval);
	            			startService(NWLocNoDutyCycleIntent);
	        				
	        				
	        				break;
	            			
	        		}
	        		break;
	        	}
		  }
		 
		
	}
	 public void CancelAlarm()
	 {
	     
	     AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
	     alarmManager.cancel(PendingIntent.getService(this, 0, new Intent(this, WiFi.class), PendingIntent.FLAG_UPDATE_CURRENT));     
	     alarmManager.cancel(PendingIntent.getService(this, 0, new Intent(this, GSM.class), PendingIntent.FLAG_UPDATE_CURRENT));  
	     alarmManager.cancel(PendingIntent.getService(this, 0, new Intent(this, Accel.class), PendingIntent.FLAG_UPDATE_CURRENT));
	     alarmManager.cancel(PendingIntent.getService(this, 0, new Intent(this, GPS.class), PendingIntent.FLAG_UPDATE_CURRENT));
	     stopService(new Intent(this, GPSWithoutDutyCycling.class));
	     alarmManager.cancel(PendingIntent.getService(this, 0, new Intent(this, NetworkLoc.class), PendingIntent.FLAG_UPDATE_CURRENT));
	     stopService(new Intent(this, NetworkLocWithoutDutyCycling.class));
	     
	    	     
	 }
	private OnClickListener launchListener = new OnClickListener (){
		@Override
		public void onClick(View v) {
			startSettings();
					
		}
	};
	
	private OnCheckedChangeListener accelFreqGroup = new OnCheckedChangeListener()
	  { 
	   public void onCheckedChanged(RadioGroup group, int checkedId) {	   
		   for(int i = 0; i <accelSamplingFreq.length ; ++i){
			   accelSamplingFreq[i] = false;
		   }
		   switch(checkedId){
		   	case R.id.fast:
		   		accelSamplingFreq[0] = true;
		   		break;
		   	case R.id.game:
		   		accelSamplingFreq[1] = true;
		   		break;
		   	case R.id.ui:
		   		accelSamplingFreq[2] = true;
		   		break;
		   	case R.id.normal:
		   		accelSamplingFreq[3] = true;
		   		break;
		   }     	     
	    }
	 };
	
	  private OnCheckedChangeListener sensorRadioGroup = new OnCheckedChangeListener()
	  {
	   public void onCheckedChanged(RadioGroup group, int checkedId) {
		   for(int i = 0; i< sensorEnable.length; ++i){
				sensorEnable[i] = false;
			}
			switch(checkedId){
            case R.id.WiFiButton:
            	sensorEnable[0] = true; 
                  break;
            case R.id.GSMButton:
            	sensorEnable[1] = true; 
                break;
            case R.id.GPSButton:
            	sensorEnable[2] = true; 
                break;
            case R.id.AccelButton:
            	sensorEnable[3] = true; 
                break;
            case R.id.GPSNoDutyCycleButton:
            	sensorEnable[4] = true;
            	break;
            case R.id.NWLocGPSButton:
            	sensorEnable[5] = true;
            	break;
            case R.id.NWLocNoDutyCycleButton:
            	sensorEnable[6] = true;
            	break;
			}
	         	     
	    }
	 };
	

	  class IncomingHandler extends Handler {
	        @Override
	        public void handleMessage(Message msg) {
	        	
	       }
	    }
	  /**private void sendSettingsToService(){
		String settings= "";
		settings = generateSettingString();
		try {
			Message msg = Message.obtain(null, BatteryService.MSG_SETTINGS, settings);
			msg.replyTo = mMessenger;
			mService.send(msg);
		} catch (RemoteException e) {
			
		}
	}
	
	private String generateSettingString(){
		
		String settingStr = ""; //sensorID, accelDutyCyle, accelSamplingFreq, radioSamplingInterval, GPSOpTime, GpsSamplingInterval
		
		accelDutyCycle = Integer.parseInt(accelDutyCycleEditText.getText().toString());
		radioSamplingInterval = Integer.parseInt(radioSamplingIntervalEditText.getText().toString());
		GPSOpTime = Integer.parseInt(gpsOperationText.getText().toString());
		GPSSamplingInterval = Integer.parseInt(gpsSamplingIntervalText.getText().toString());
		
		 for(int i = 0; i < sensorEnable.length; ++i){
	        	if(sensorEnable[i]){
	        		settingStr+= i + ",";
	        		break;
	        	}
		  }
		 settingStr += accelDutyCycle + ",";
		 
		 for(int i = 0; i < accelSamplingFreq.length; ++i){
			    if(accelSamplingFreq[i]){
					settingStr += i + ",";
					break;
				}		
		}
		settingStr += radioSamplingInterval + "," +  GPSOpTime + "," + GPSSamplingInterval;
		 
		return settingStr;
	}**/ 
	/**  
   private void doStartService(){
		ComponentName n = startService(new Intent(BatteryAnalysisActivity.this, BatteryService.class));
		if (n != null)
			System.out.println("started: " + n);
		else
			System.out.println("null service returned");
		
	}	
	private void doBindService(){
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(BatteryAnalysisActivity.this, BatteryService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() 
	{
		if (mIsBound) 
		{
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {}

			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
			System.out.println("Unbinding");
		}
	}	
	private void doStopService()
	{
		boolean stopped = stopService(new Intent(BatteryAnalysisActivity.this, BatteryService.class));
		System.out.println("stopped service: " + stopped);
	}
	 **/
	public void onDestroy()
	{
		super.onDestroy();
		CancelAlarm();
		//doUnbindService();
		//doStopService();
		System.out.println("destroyed");
	}
	
}