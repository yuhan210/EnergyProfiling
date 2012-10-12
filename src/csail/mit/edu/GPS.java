package csail.mit.edu;

import java.util.Timer;
import java.util.TimerTask;

import csail.mit.edu.Accel.AccelTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.Toast;

public class GPS extends Service{
	private WakeLock wl;
	private PowerManager pm;
	private LocationManager locationManager;
    private LocationListener locationListener;
    private Timer timer;
   
   /** public GPS(){
    	this.pm = (PowerManager) Global.context.getSystemService(Context.POWER_SERVICE);
        this.wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
		this.locationManager = (LocationManager)Global.context.getSystemService(Context.LOCATION_SERVICE);
		this.locationListener = new GPSLocationListener();	
    }
    
    public void run(int samplingIntervalInMs){
    	if(!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)){
			Toast.makeText(Global.context, "Turn ON GPS", Toast.LENGTH_SHORT).show();
    	}
    	 wl.acquire();
    	 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, samplingIntervalInMs, 0, locationListener);
 		 
    }**/
	
    public int onStartCommand(Intent intent, int flags, int startId) {
    	 
    	 System.out.println("GPS OnStartCommand()");
    	 Bundle extras = intent.getExtras();
    	 	 
 		 int opTime = extras.getInt("Operate Time");
		 pm = (PowerManager) Global.context.getSystemService(Context.POWER_SERVICE);
		 wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();
         
         this.locationManager = (LocationManager)Global.context.getSystemService(Context.LOCATION_SERVICE);
 		 this.locationListener = new GPSLocationListener();	 
 		 locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1 * 1000, 0, locationListener);
 		  
 		 timer = new Timer();
 		 timer.schedule(new GPSTask(), opTime * 1000); 
    	return super.onStartCommand(intent, flags, startId);
    }
    
    class GPSTask extends TimerTask {
		@Override
		public void run() {
			 System.out.println("GPS Stopped");
			 locationManager.removeUpdates(locationListener);
			 timer.cancel();
			 wl.release();
			 stopSelf();
		}
    }
	
	private class GPSLocationListener implements LocationListener 
    {
        public void onLocationChanged(Location loc) 
        {		
        	System.out.println("GPS received: " + loc.getLatitude() + "," + loc.getLongitude());
        }
           
        public void onProviderDisabled(String provider) 
        {}

        public void onProviderEnabled(String provider) 
        {}

        public void onStatusChanged(String provider, int status, Bundle extras) 
        {}
    }
/**	public void stop(){
		
			 locationManager.removeUpdates(locationListener);
			 if(wl.isHeld()) wl.release();
	}**/


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
			
	
}
	


