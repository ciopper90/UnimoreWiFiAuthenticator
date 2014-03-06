package it.ciopper90.unimorelogin.Model;

import it.ciopper90.unimorelogin.Exceptions.LoginException;
import it.ciopper90.unimorelogin.Exceptions.LogoutException;
import it.ciopper90.unimorelogin.Utils.SimpleSSLSocketFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;



import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class Authentication {
	/**
	 * @author Copelli Alberto
	 */

	private static final String TAG = "UnimoreWiFi:Authentication";

	public static void Authenticate(Data data, String ip, String mac)
			throws LoginException {
		HttpClient httpclient = getClient(5000);
		int ok=1;
		HttpGet httpget=new HttpGet("http://ciopper90.altervista.org/unimore.html");


		try{
			HttpResponse response = httpclient.execute(httpget);
			String returnString=EntityUtils.toString(response.getEntity());
			if(returnString.contains("UnimoreWifiLogin")){
				//login gia effettuato
				Log.d("Autentication", "2");
				ok=3;
			}else{
				//bisogna effettuare il login	
				ok=1;
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Errore nel client protocol: " + e.getMessage());
		} catch (UnknownHostException e) {
			Log.e(TAG, "UnknownHostException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		if(ok!=3){
			httpclient = getClient(20000);
			String url="https://securelogin.arubanetworks.com/cgi-bin/login";
			HttpPost httppost = new HttpPost(url);
			for(int i=0;i<2;i++){
				try {
					// Add your data
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("_FORM_SUBMIT", "1"));
					nameValuePairs.add(new BasicNameValuePair("which_form", "regform"));
					nameValuePairs.add(new BasicNameValuePair("destination", ""));
					nameValuePairs.add(new BasicNameValuePair("cmd", "login"));
					nameValuePairs.add(new BasicNameValuePair("mac", mac));
					nameValuePairs.add(new BasicNameValuePair("ip", ip));
					nameValuePairs.add(new BasicNameValuePair("essid","unimore"));
					nameValuePairs.add(new BasicNameValuePair("url", "about:blank"));
					nameValuePairs.add(new BasicNameValuePair("user", data.getUsername()));
					nameValuePairs.add(new BasicNameValuePair("password", data.getPassword()));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					// Execute HTTP Post Request
					HttpResponse response = httpclient.execute(httppost);
					String returnString=EntityUtils.toString(response.getEntity());
					if(returnString.contains("Authentication failed")){
						Log.d("Autentication", "2");
						ok=2;}
					else{
						ok=0;	
					}
				} catch (ClientProtocolException e) {
					Log.e(TAG, "Errore nel client protocol: " + e.getMessage());
				} catch (UnknownHostException e) {
					Log.e(TAG, "UnknownHostException: " + e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, "IOException: " + e.getMessage());
				}
				if(ok==0||ok==2)
					break;
			}
		}
		if(ok==1){
			HttpPost httppost = new HttpPost(
					"https://aruba-slave.unimore.it/cgi-bin/login");

			for(int i=0;i<2;i++){
				try {
					// Add your data
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("_FORM_SUBMIT", "1"));
					nameValuePairs.add(new BasicNameValuePair("which_form", "regform"));
					nameValuePairs.add(new BasicNameValuePair("destination", ""));
					nameValuePairs.add(new BasicNameValuePair("cmd", "login"));
					nameValuePairs.add(new BasicNameValuePair("mac", mac));
					nameValuePairs.add(new BasicNameValuePair("ip", ip));
					nameValuePairs.add(new BasicNameValuePair("essid","unimore"));
					nameValuePairs.add(new BasicNameValuePair("url", "about:blank"));
					nameValuePairs.add(new BasicNameValuePair("user", data.getUsername()));
					nameValuePairs.add(new BasicNameValuePair("password", data.getPassword()));
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					// Execute HTTP Post Request
					HttpResponse response = httpclient.execute(httppost);
					String returnString=EntityUtils.toString(response.getEntity());
					if(returnString.contains("Authentication failed")){
						Log.d("Autentication", "2");
						ok=2;}
					else{
						ok=0;	
					}
				} catch (ClientProtocolException e) {
					Log.e(TAG, "Errore nel client protocol: " + e.getMessage());
				} catch (UnknownHostException e) {
					Log.e(TAG, "UnknownHostException: " + e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, "IOException: " + e.getMessage());
				}
				if(ok==0||ok==2)
					break;
			}
		}
		if(ok==1){
			throw new LoginException("Authentication ");
		}
		if(ok==2){
			throw new LoginException("Data Error");
		}
		if(ok==3){
			throw new LoginException("Login Error");
		}


	}

	public static DefaultHttpClient getClient(int timeout) {
		DefaultHttpClient ret = null;

		//versione 1.1

		try {
			SSLSocketFactory sslFactory = new SimpleSSLSocketFactory(null);
			sslFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			// Enable HTTP parameters
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			//gestione timeout
			HttpConnectionParams.setConnectionTimeout(params, timeout);
			HttpConnectionParams.setSoTimeout(params, timeout);
			
			
			// Register the HTTP and HTTPS Protocols. For HTTPS, register our custom SSL Factory object.
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sslFactory, 443));

			// Create a new connection manager using the newly created registry and then create a new HTTP client
			// using this connection manager
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			ret = new DefaultHttpClient(ccm, params);

		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static void logout() throws LogoutException {
		HttpClient client = getClient(20000);
		int ok1=1,ok2=1;
		HttpGet get = new HttpGet("https://aruba-slave.unimore.it/cgi-bin/login?cmd=logout");
		try {
			client.execute(get);
			ok1=0;
		} catch (UnknownHostException e) {
			Log.e(TAG, "UnknownHostException: " + e.getMessage());
			ok1=1;
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Errore nel client protocol: " + e.getMessage());
			ok1=2;
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
			ok1=3;
		}
		if(ok1!=0){
			get = new HttpGet("https://securelogin.arubanetworks.com/cgi-bin/login?cmd=logout");
			try {
				client.execute(get);
				ok2=0;
			} catch (UnknownHostException e) {
				Log.e(TAG, "UnknownHostException: " + e.getMessage());
				ok2=1;
			} catch (ClientProtocolException e) {
				Log.e(TAG, "Errore nel client protocol: " + e.getMessage());
				ok2=2;
			} catch (IOException e) {
				Log.e(TAG, "IOException: " + e.getMessage());
				ok2=3;
			}
		}

		if(ok1!=0&&ok2!=0){
			throw new LogoutException("Authentication ");
		}


	}
}
