package csail.mit.edu;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class GSM extends Service{
	private TelephonyManager telephonyManager;
	private WakeLock wl;
	private PowerManager pm;
	
	
	 public int onStartCommand(Intent intent, int flags, int startId) {
		 
		 System.out.println("GSM OnStartCommand()");
		 pm = (PowerManager) Global.context.getSystemService(Context.POWER_SERVICE);
		 wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
         wl.acquire();
         
         telephonyManager = (TelephonyManager)Global.context.getSystemService(Context.TELEPHONY_SERVICE);
		 telephonyManager.listen(phoneStateListner, PhoneStateListener.LISTEN_CELL_LOCATION);
		
		 
		 return super.onStartCommand(intent, flags, startId);
	 }
	 
	private PhoneStateListener phoneStateListner = new PhoneStateListener()
	{
		public void  onSignalStrengthChanged(int asu)
		{}
		
		public void  onCellLocationChanged  (CellLocation location)
		{
			System.out.println("GSM scan received");
			telephonyManager.listen(phoneStateListner, PhoneStateListener.LISTEN_NONE);
			wl.release();
			stopSelf();
		}
	};
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}

