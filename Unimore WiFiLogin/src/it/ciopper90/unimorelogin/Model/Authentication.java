package it.ciopper90.unimorelogin.Model;

import it.ciopper90.unimorelogin.Exceptions.LoginException;
import it.ciopper90.unimorelogin.Exceptions.LogoutException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class Authentication {
	/**
	 * @author Copelli Alberto
	 */

	private static final String TAG = "UnimoreWiFi:Authentication";
	
	public static void Authenticate(Data data, String ip, String mac)
			throws LoginException {
		int ok=1;
		String returnString=sendGet("http://ciopper90.altervista.org/unimore.html",0);
		if(returnString.contains("UnimoreWifiLogin")){
			//login gia effettuato
			Log.d("Autentication", "2");
			ok=3;
		}else{
			//bisogna effettuare il login	
			ok=1;
		}
		if(ok!=3){
			try {
				//String uri=returnString.substring(0,returnString.indexOf('?'));
				URL url = new URL("https://c0.wifi.unimo.it/cgi-bin/login");
				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				if (currentapiVersion <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1){
				    trustAllHosts();
				    //conn.setHostnameVerifier(DO_NOT_VERIFY);
				}
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setReadTimeout(10000);
				conn.setConnectTimeout(20000);
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("_FORM_SUBMIT", "1"));
				params.add(new BasicNameValuePair("which_form", "regform"));
				params.add(new BasicNameValuePair("destination", ""));
				params.add(new BasicNameValuePair("cmd", "login"));
				params.add(new BasicNameValuePair("mac", mac));
				params.add(new BasicNameValuePair("ip", ip));
				params.add(new BasicNameValuePair("essid","unimore"));
				params.add(new BasicNameValuePair("url", "about:blank"));
				params.add(new BasicNameValuePair("user", data.getUsername()));
				params.add(new BasicNameValuePair("password", data.getPassword()));

				OutputStream os = conn.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(os, "UTF-8"));
				writer.write(getQuery(params));
				writer.flush();
				writer.close();
				os.close();

				//Get Response	
				InputStream is = conn.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;
				StringBuffer response = new StringBuffer(); 
				while((line = rd.readLine()) != null) {
					response.append(line);
					response.append('\r');
				}
				rd.close();
				//Log.d(TAG,""+response.toString());
				returnString=response.toString();

				//conn.connect();
				if(returnString.contains("Authentication failed")){
					Log.d("Autentication", "2");
					ok=2;}
				else{
					ok=0;	
				}
			} catch (MalformedURLException e) {
				Log.e(TAG, "MalformedURLException: " + e.getMessage());
			} catch (ProtocolException e) {
				Log.e(TAG, "ProtocolException: " + e.getMessage());
			} catch (IOException e) {
				e.printStackTrace();
				//Log.e(TAG, "IOException: " + e.getMessage());
			}
		}
		if(ok==0||ok==2)
			return;
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


	private static String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params)
		{
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	public static void logout() throws LogoutException {
		String returnString=sendGet("https://c0.wifi.unimo.it/cgi-bin/login?cmd=logout",1);
		if(returnString.contains("User not logged in")){
			throw new LogoutException("Not Logged");
		}else{
			if(!returnString.contains("Logout Successful")){
				throw new LogoutException("Authentication ");
			}
		}

	}

	/**
	 * 
	 * @param address Url da invocare
	 * @param type 0 -> http, 1-> https
	 */
	public static String sendGet(String address,int type){
		if(type==0){
			return sendGetHttp(address);
		}else{
			return sendGetHttps(address);
		}
		
	}
	
	public static String sendGetHttps(String address){
		try {
			URL url = new URL(address);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			int responseCode = conn.getResponseCode();

			if(responseCode == 200){
				BufferedReader in = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				return response.toString();
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException: " + e.getMessage());
		} catch (ProtocolException e) {
			Log.e(TAG, "ProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		return "";
	}
	public static String sendGetHttp(String address){
		try {
			URL url = new URL(address);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			int responseCode = conn.getResponseCode();

			if(responseCode == 200){
				BufferedReader in = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				return response.toString();
			}
			Log.d(TAG, responseCode +"");
			/*boolean redirect = false;
			int status = conn.getResponseCode();
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
			if(redirect){
				String newUrl = conn.getHeaderField("Location");
				Log.d(TAG, newUrl+"");
				return newUrl;
			}*/
				
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException: " + e.getMessage());
		} catch (ProtocolException e) {
			Log.e(TAG, "ProtocolException: " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		}
		return "";
	}

	
	/**
	 * Trust every server - dont check for any certificate
	 */
	private static void trustAllHosts() {
	        // Create a trust manager that does not validate certificate chains
	        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
	                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                        return new java.security.cert.X509Certificate[] {};
	                }

	                public void checkClientTrusted(X509Certificate[] chain,
	                                String authType) throws CertificateException {
	                }

	                public void checkServerTrusted(X509Certificate[] chain,
	                                String authType) throws CertificateException {
	                }
	        } };

	        // Install the all-trusting trust manager
	        try {
	                SSLContext sc = SSLContext.getInstance("TLS");
	                sc.init(null, trustAllCerts, new java.security.SecureRandom());
	                HttpsURLConnection
	                                .setDefaultSSLSocketFactory(sc.getSocketFactory());
	        } catch (Exception e) {
	                e.printStackTrace();
	        }
	}
	

final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
    public boolean verify(String hostname, SSLSession session) {
            return true;
    }
};

}
