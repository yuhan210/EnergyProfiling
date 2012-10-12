package csail.mit.edu;


import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class WiFi extends Service {
	private WifiManager wifiManager;
	private WifiLock wifiLock;
	private WifiReceiver wifiReceiver = new WifiReceiver();
	private WakeLock wl;
	private PowerManager pm;

	 
	 public int onStartCommand(Intent intent, int flags, int startId) 
	 {
		 System.out.println("WiFi OnStartCommand()");	
		 
		 /** Acquire power lock **/
		 pm = (PowerManager) Global.context.getSystemService(Context.POWER_SERVICE);
         wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();
         
         
         wifiManager = (WifiManager)Global.context.getSystemService(Context.WIFI_SERVICE);
         wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "WiFiSilentScanLock");
         wifiLock.acquire();
         Global.context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 		 
         System.out.println("call startScan");
    	 wifiManager.startScan(); 
    	 
	  	 return super.onStartCommand(intent, flags, startId);
		
        
     }
	
	class WifiReceiver extends BroadcastReceiver {
	  	 public void onReceive(Context c, Intent intent) 
		    {
	  		
	  		 
		  	/**List<ScanResult> scanResults = wifiManager.getScanResults();
	  		System.out.println("wifi received + " + scanResults.size());
	  		for (int i = 0; i < scanResults.size(); i++)
	    	{
	    		System.out.println(scanResults.get(i).SSID + "," + scanResults.get(i).BSSID + "," + scanResults.get(i).level);
	    	}**/
	  		
	  		 Global.context.unregisterReceiver(wifiReceiver);
	  		 wifiLock.release();
	  		 wl.release();
	  		 stopSelf();
	    	 
	  }

	}
   
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}