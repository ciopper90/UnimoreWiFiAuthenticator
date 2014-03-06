package it.ciopper90.unimorelogin;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;
import it.ciopper90.unimorelogin.R;

public class About extends Activity {
	/**
	 * @author Copelli Alberto
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Log.i(TAG,"onCreate()");
		setContentView(R.layout.about);
		TextView aboutText = (TextView) findViewById(R.id.about_content);
		Linkify.addLinks(aboutText, Linkify.EMAIL_ADDRESSES);
	}

}
