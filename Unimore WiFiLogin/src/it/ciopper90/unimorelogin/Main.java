package it.ciopper90.unimorelogin;

import it.ciopper90.unimorelogin.R;
import it.ciopper90.unimorelogin.Exceptions.LoadDataException;
import it.ciopper90.unimorelogin.Exceptions.LoginException;
import it.ciopper90.unimorelogin.Exceptions.LogoutException;
import it.ciopper90.unimorelogin.Farcade.FacadeController;
import it.ciopper90.unimorelogin.Model.Data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.util.Log;

@SuppressLint({ "WorldWriteableFiles", "HandlerLeak" })
public class Main extends Activity{
	/**
	 * @author Copelli Alberto
	 */
	private static final String TAG = "UnimoreWiFi:Main";
	private static final int MILLISECONDS = 100;
	private static final String savePath = "data";
	private FacadeController fc;
	private Vibrator vibrator;
	private static ProgressDialog myPd;
	private SharedPreferences sharedPref;
	private static String progressDialog;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		fc = FacadeController.getInstance();
		this.context=this.getApplicationContext();
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		myPd = new ProgressDialog(Main.this);
		sharedPref = this.getSharedPreferences(Main.savePath,
				Activity.MODE_WORLD_WRITEABLE);
		// Dichiarazione ed assegnazione dei listener dei pulsanti
		View loginButton = findViewById(R.id.login_button);
		View exitButton = findViewById(R.id.exit_button);
		View logoutButton = findViewById(R.id.logout_button);
		CheckBox cbox= (CheckBox)findViewById(R.id.checkBox1);
		cbox.setChecked(sharedPref.getBoolean("intent", false));
		cbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("intent", isChecked);
				editor.commit();
			}
		});

		exitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				exitButtonClick();
			}
		});

		loginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				loginButtonClick();
			}
		});
		logoutButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				logoutButtonClick();
			}
		});

		if(progressDialog==null){
			progressDialog="";
		}else{
			if(progressDialog.equals("")){

			}else{
				myPd.setMessage(progressDialog);
				myPd.setCancelable(false);
				myPd.show();
			}
		}
	}

	private void exitButtonClick() {
		finish();
	}

	private void loginButtonClick() {
		myPd.setMessage(getResources().getString(R.string.login_progress));

		// verifica se esistono già le credenziali di login impostate
		sharedPref = this.getSharedPreferences(Main.savePath,
				Activity.MODE_WORLD_WRITEABLE);

		if (sharedPref.getString("username", "username") == "username"
				&& sharedPref.getString("password", "password") == "password") {
			this.startActivity(new Intent(Main.this, SetData.class));
		} else {
			WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
			String ssid="";
			if(wifiManager!=null){
				if(wifiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED){
					ssid=wifiManager.getConnectionInfo().getSSID();
					if(ssid==null){
						ssid="";
					}
					if (!(ssid.contains(this.getString(R.string.bssid)))) {
						connectHandler.sendEmptyMessage(0);
					}else{
						myPd.setCancelable(false);
						myPd.show();
						progressDialog=getResources().getString(R.string.login_progress);
						new Thread(new Runnable() {
							private final static String TAG = "threadLoginButtonClick";

							public void run() {
								try {
									fc.login(loadData(), getWifi("ip"), getWifi("mac"));
									loginHandler.sendEmptyMessage(1);				
								} catch (LoginException e) {
									if(e.getMessage().contains("Data Error")){
										Log.e(TAG, "LoginException: " + e.getMessage());
										loginHandler.sendEmptyMessage(2);
									}else{
										if(e.getMessage().contains("Login Error")){
											Log.e(TAG, "LoginException: " + e.getMessage());
											loginHandler.sendEmptyMessage(3);
										}else{	
											Log.e(TAG, "LoginException: " + e.getMessage());
											loginHandler.sendEmptyMessage(0);
										}
									}
								} catch (LoadDataException e) {
									Log.e(TAG, "LoadDataException: " + e.getMessage());
									loginHandler.sendEmptyMessage(0);
								}
							}
						}).start();
					}
				}else{
					connectHandler.sendEmptyMessage(1);
				}
			}else{
				connectHandler.sendEmptyMessage(1);
			}
		}
	}

	private void logoutButtonClick() {
		final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

		if(wifiManager.getWifiState()==WifiManager.WIFI_STATE_ENABLED){
			myPd.setMessage(getResources().getString(R.string.logout_progress));
			myPd.show();
			progressDialog=getResources().getString(R.string.logout_progress);
			new Thread(new Runnable() {
				private final static String TAG = "threadLogoutButtonClick";

				public void run() {
					try {
						fc.logout();
						logoutHandler.sendEmptyMessage(1);
					} catch (LogoutException e) {
						Log.e(TAG, "LogoutException: " + e.getMessage());
						logoutHandler.sendEmptyMessage(0);
					}
				}
			}).start();
		}else{
			connectHandler.sendEmptyMessage(1);
		}
	}
	
	private Handler loginHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			myPd.dismiss();
			progressDialog="";
			if (msg.what == 1) {
				vibra();
				Toast toast = Toast.makeText(Main.this,
						R.string.login_successful, Toast.LENGTH_SHORT);
				toast.show();
			} else {
				if(msg.what==0){
					vibra();
					Toast toast = Toast.makeText(Main.this, R.string.login_error,
							Toast.LENGTH_SHORT);
					toast.show();
				}else{
					if(msg.what==3){
						vibra();
						Toast toast = Toast.makeText(Main.this, R.string.login_successful_all,
								Toast.LENGTH_SHORT);
						toast.show();
					}else{
						vibra();
						Toast toast = Toast.makeText(Main.this, R.string.login_data_error,
								Toast.LENGTH_SHORT);
						toast.show();
					}
				}
			}
		}
	};

	private Handler logoutHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			progressDialog="";
			myPd.dismiss();
			if (msg.what == 1) {
				vibra();
				Toast toast = Toast.makeText(Main.this,
						R.string.logout_successful, Toast.LENGTH_SHORT);
				toast.show();
			} else {
				vibra();
				Toast toast = Toast.makeText(Main.this, R.string.logout_error,
						Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};

	private Handler connectHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			progressDialog="";
			myPd.dismiss();
			if (msg.what == 1) {
				vibra();
				Toast toast = Toast.makeText(Main.this, R.string.wifi_on,
						Toast.LENGTH_SHORT);
				toast.show();
			}
			if (msg.what == 2) {
				vibra();
				Toast toast = Toast.makeText(Main.this, R.string.wifi_err,
						Toast.LENGTH_SHORT);
				toast.show();
			}
			if (msg.what == 3) {
				vibra();
				Toast toast = Toast.makeText(Main.this, R.string.no_ap,
						Toast.LENGTH_LONG);
				toast.show();
			}
			if (msg.what == 0) {
				vibra();
				Toast toast = Toast.makeText(Main.this, R.string.ssid_error,
						Toast.LENGTH_LONG);
				toast.show();
			}
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		int order = Menu.FIRST;
		int GROUPA = 0;
		int GROUPB = 1;
		menu.add(GROUPA, order, order++,
				getResources().getString(R.string.set_data_label))
				.setIntent(new Intent(Main.this, SetData.class))
				.setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(GROUPB, order, order++,
				getResources().getString(R.string.about_label))
				.setIntent(new Intent(Main.this, About.class))
				.setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	private String getWifi(String im) {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
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
		Data data = new Data();
		try {
			sharedPref = this.getSharedPreferences(Main.savePath,
					Activity.MODE_WORLD_WRITEABLE);
			data = new Data(sharedPref.getString("username", "username"),
					sharedPref.getString("password", "password"));
		} catch (ClassCastException e) {
			Log.e(TAG, "ClassCastException: " + e.getMessage());
			throw new LoadDataException(e.getMessage());
		}
		return data;
	}

	private void vibra() {
		vibrator.vibrate(Main.MILLISECONDS);
	}
}
