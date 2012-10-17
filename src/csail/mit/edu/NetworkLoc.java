package csail.mit.edu;

import java.util.Timer;
import java.util.TimerTask;

import csail.mit.edu.GPS.GPSTask;
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

public class NetworkLoc extends Service{
	
	private WakeLock wl;
	private PowerManager pm;
	private LocationManager locationManager;
    private LocationListener locationListener;
    private Timer timer;
    
 public int onStartCommand(Intent intent, int flags, int startId) {
    	 
    	 System.out.println("NWLocGPS OnStartCommand()");
    	 Bundle extras = intent.getExtras();
 		 int opTime = extras.getInt("Operate Time");
 		 
 		 pm = (PowerManager) Global.context.getSystemService(Context.POWER_SERVICE);
		 wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();
         
         this.locationManager = (LocationManager)Global.context.getSystemService(Context.LOCATION_SERVICE);
 		 this.locationListener = new GPSLocationListener();	
 		 
 		
 		 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
 		  
 		 timer = new Timer();
 		 timer.schedule(new NWGPSTask(), opTime * 1000); 
    	return super.onStartCommand(intent, flags, startId);
    }
 
 	class NWGPSTask extends TimerTask {
		@Override
		public void run() {
			 System.out.println("NWLocGPS Stopped");
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
     	System.out.println("NWLocGPS received: " + loc.getLatitude() + "," + loc.getLongitude());
     }
        
     public void onProviderDisabled(String provider) 
     {}

     public void onProviderEnabled(String provider) 
     {}

     public void onStatusChanged(String provider, int status, Bundle extras) 
     {}
 }

@Override
public IBinder onBind(Intent intent) {
	// TODO Auto-generated method stub
	return null;
}
}
