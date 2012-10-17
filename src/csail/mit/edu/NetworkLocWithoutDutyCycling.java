package csail.mit.edu;

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

public class NetworkLocWithoutDutyCycling  extends Service{
	private WakeLock wl;
	private PowerManager pm;
	private LocationManager locationManager;
    private LocationListener locationListener;
    @Override
	public void onStart(Intent intent, int startId) {
		 
		 System.out.println("NWLoc OnStart()");
   	 	 Bundle extras = intent.getExtras();
   	 	
		 int samplingInterval = extras.getInt("Sampling Interval");
		 pm = (PowerManager) Global.context.getSystemService(Context.POWER_SERVICE);
		 wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();
        
         this.locationManager = (LocationManager)Global.context.getSystemService(Context.LOCATION_SERVICE);
		 this.locationListener = new NWLocationListener();	
		 try{locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}
		 catch(Exception ex){ System.out.println("network provider is not enabled.");}

		 locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, samplingInterval * 1000, 0, locationListener);

	}
    private class NWLocationListener implements LocationListener 
    {
        public void onLocationChanged(Location loc) 
        {		
        	System.out.println("NWLoc received: " + loc.getLatitude() + "," + loc.getLongitude());
        }
           
        public void onProviderDisabled(String provider) 
        {}

        public void onProviderEnabled(String provider) 
        {}

        public void onStatusChanged(String provider, int status, Bundle extras) 
        {
        	System.out.println("System status:"+ status );
        }
    }
	@Override
	public void onDestroy(){
		 System.out.println("GPS Stopped");
		 locationManager.removeUpdates(locationListener);	
		 wl.release();
		super.onDestroy();  
	}
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
