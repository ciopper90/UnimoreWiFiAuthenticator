package it.ciopper90.unimorelogin;


import it.ciopper90.unimorelogin.Exceptions.LoadDataException;
import it.ciopper90.unimorelogin.Exceptions.LoginException;
import it.ciopper90.unimorelogin.Farcade.FacadeController;
import it.ciopper90.unimorelogin.Model.Data;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

@SuppressLint({ "WorldWriteableFiles", "HandlerLeak" })
public class NetWatcher extends BroadcastReceiver {
	/**
	 * @author Copelli Alberto
	 */
	private String TAG="NetWatcher";
	private Context context;
	private Vibrator vibrator;
	private SharedPreferences sharedPref;
	private static boolean op;

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context=context;
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		sharedPref = context.getSharedPreferences("data",
				Activity.MODE_WORLD_WRITEABLE);
		if(sharedPref.getBoolean("intent", false)){

			//android.net.wifi.WIFI_STATE_CHANGED
			//android.net.wifi.STATE_CHANGE
			//android.net.wifi.supplicant.CONNECTION_CHANGE
			//condition to login
			//Log.d(TAG, "ricezione broadcast");
			if(wifiManager!=null){
				if(wifiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED){
					String ssid=wifiManager.getConnectionInfo().getSSID();
					if(ssid==null){
						ssid="";
					}
					//Log.d(TAG, ssid);
					if (ssid.contains(context.getString(R.string.bssid))&&op==false) {
						op=true;
						try {
							Thread.sleep(4000);
						} catch (InterruptedException e) {
						}
						//login
						//Log.d(TAG, "Login");
						test(context.getString(R.string.login_progress));
						login();
					}
				}
			}
		}
	}

	private void login() {
		final FacadeController fc = FacadeController.getInstance();

		new Thread(new Runnable() {
			private final static String TAG = "threadLoginButtonClick";

			public void run() {
				try {
					Data d=loadData();
					if(d.getUsername().equals("username")&&d.getPassword().equals("password")){
						loginHandler.sendEmptyMessage(2);
					}else{
						fc.login(d, getWifi("ip"), getWifi("mac"));
						loginHandler.sendEmptyMessage(1);
					}
				} catch (LoginException e) {
					if(e.getMessage().contains("Data Error")){
						Log.e(TAG, "LoginException: " + e.getMessage());
						loginHandler.sendEmptyMessage(2);
					}else{
						if(e.getMessage().contains("Login Error")){
							Log.e(TAG, "LoginException: " + e.getMessage());
							loginHandler.sendEmptyMessage(0);
						}else{							
							Log.e(TAG, "LoginException: " + e.getMessage());
							loginHandler.sendEmptyMessage(3);
						}
					}
				} catch (LoadDataException e) {
					Log.e(TAG, "LoadDataException: " + e.getMessage());
					loginHandler.sendEmptyMessage(2);
				}	
			}
		}).start();

	}

	private String getWifi(String im) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		String IM = null;
		if (im == "ip") {
			// Log.i(TAG,"richiesta ip in corso...");
			int ip = wifiManager.getConnectionInfo().getIpAddress();
			// Log.d(TAG, "ip ottenuto: "+ip);
			IM = (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
					+ ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
		}
		if (im == "mac") {
			// Log.i(TAG,"richiesta MAC in corso...");
			IM = wifiManager.getConnectionInfo().getMacAddress();
			// Log.d(TAG, "MAC ottenuto: "+IM);
		}
		return IM;
	}

	private Data loadData() throws LoadDataException {
		// Log.i(TAG,"loadData()");
		Data data = new Data();
		try {
			sharedPref = context.getSharedPreferences("data",
					Activity.MODE_WORLD_WRITEABLE);
			data = new Data(sharedPref.getString("username", "username"),
					sharedPref.getString("password", "password"));
			// Log.d(TAG,"DATA: " + data.toString());
		} catch (ClassCastException e) {
			Log.e(TAG, "ClassCastException: " + e.getMessage());
			throw new LoadDataException(e.getMessage());
		}
		return data;
	}

	private void vibra() {
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = pm.isScreenOn();
		if(isScreenOn)
			vibrator.vibrate(100);
		else{
			sharedPref = context.getSharedPreferences("data",Activity.MODE_WORLD_WRITEABLE);
			boolean a=sharedPref.getBoolean("vibraspento", false);
			if(a){
				vibrator.vibrate(100);
			}
		}
	}

	private Handler loginHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			op=false;
			if (msg.what == 0) {
				Log.d(TAG,"msg.what==0");
				vibra();
				Toast toast = Toast.makeText(context,
						R.string.login_successful_all, Toast.LENGTH_LONG);
				toast.show();
			}
			if (msg.what == 1) {
				Log.d(TAG,"msg.what==1");
				vibra();
				Toast toast = Toast.makeText(context,
						R.string.login_successful, Toast.LENGTH_LONG);
				toast.show();
			}
			if (msg.what == 2) {
				Log.d(TAG,"msg.what==2");
				vibra();
				Toast toast = Toast.makeText(context,
						R.string.missing_data, Toast.LENGTH_SHORT);
				toast.show();
			}
			if(msg.what==3){
				vibra();
				Toast toast = Toast.makeText(context, R.string.login_error,
						Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};

	private void test(String ciao){
		Toast toast = Toast.makeText(context,
				ciao, Toast.LENGTH_SHORT);
		toast.show();
	}


}