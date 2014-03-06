package it.ciopper90.unimorelogin;


import it.ciopper90.unimorelogin.R;
import it.ciopper90.unimorelogin.Exceptions.SaveDataException;
import it.ciopper90.unimorelogin.Model.Data;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SetData extends Activity {
	/**
	 * @author Copelli Alberto
	 */
	private static final int MILLISECONDS = 100;
	private static final String savePath = "data";
	private EditText username;
	private EditText password;
	private Vibrator vibrator;
	private ProgressDialog myPd;
	private SharedPreferences sharedPref;
	private SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_data);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		myPd = new ProgressDialog(SetData.this);

		// verifica se esistono già valori impostati e scrittura dei rispettivi
		// valori nelle EditText
		sharedPref = this.getSharedPreferences(SetData.savePath,
				Activity.MODE_WORLD_WRITEABLE);

		if (sharedPref.getString("username", "username") != "username"
				&& sharedPref.getString("password", "password") != "password") {
			username.setText(sharedPref.getString("username", "username"));
			password.setText(sharedPref.getString("password", "password"));

		}
		//vibraspento
		boolean stat = sharedPref.getBoolean("vibraspento",false);
		final CheckBox c=(CheckBox)findViewById(R.id.vibraspento);
		c.setChecked(stat);

		c.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View arg0) {
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("vibraspento", c.isChecked());
				editor.commit();	
			}
		});
		
		
		// Dichiarazione ed assegnazione dei listener del pulsante
		View save = findViewById(R.id.save);
		save.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				saveButtonClick();
			}
		});
	}

	public void saveButtonClick() {
		myPd.setMessage(getResources().getString(R.string.saving_data));
		myPd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					if (username.getText().length() > 0
							&& password.getText().length() > 0) {
						SetData.this.saveData(new Data(username.getText()
								.toString(), password.getText().toString()));
						saveDataHandler.sendEmptyMessage(1);
					} else
						saveDataHandler.sendEmptyMessage(0);
				} catch (SaveDataException e) {
					saveDataHandler.sendEmptyMessage(0);
				} finally {
					SetData.this.finish();
				}
			}
		}).start();
	}

	private Handler saveDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			myPd.dismiss();
			if (msg.what == 1) {
				vibra();
				Toast toast = Toast.makeText(SetData.this,
						R.string.save_data_successful, Toast.LENGTH_SHORT);
				toast.show();
			} else {
				vibra();
				Toast toast = Toast.makeText(SetData.this,
						R.string.save_data_error, Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};

	private void saveData(Data data) throws SaveDataException {
		sharedPref = this.getSharedPreferences(SetData.savePath,
				Activity.MODE_WORLD_WRITEABLE);
		editor = sharedPref.edit();
		editor.putString("username", data.getUsername());
		editor.putString("password", data.getPassword());
		if (!editor.commit()) {
			throw new SaveDataException("Data not saved");
		}
	}

	private void vibra() {
		vibrator.vibrate(SetData.MILLISECONDS);
	}
}
