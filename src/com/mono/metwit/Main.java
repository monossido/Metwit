/*  Copyright 2011
 *	Lorenzo Braghetto monossido@lorenzobraghetto.com
 *      This file is part of Metwit <https://github.com/monossido/Metwit>
 *      
 *      Metwit is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *      
 *      Metwit is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with Metwit  If not, see <http://www.gnu.org/licenses/>.
 *      
 */
package com.mono.metwit;

import java.io.File;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.mono.metwit.API.*;


public class Main extends Activity {
	
	SharedPreferences.Editor ledit;
    double loclat;
    double loclng;
    NodeList notes2;
	static Context context;
	static DefaultHttpClient client=null;
	boolean right=true;
	List<Address> addresses;
    String immagine;
    String immagine2;
    String dataOra;
    String dataOra2;
    String localita;
    String localita2;
	List<Address> addresses2;
	String utente;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);  
        context = this;
    	client = new DefaultHttpClient();
    	addresses = null;
	    
        SharedPreferences Login = getSharedPreferences("Login", 0);
        Calendar now = Calendar.getInstance();
        long data = now.getTimeInMillis();
        Long dataAggiornamento = Login.getLong("DataAggiornamento", 0);
        if(data-dataAggiornamento>86400000)
        {
        	int duration = Toast.LENGTH_SHORT;

        	Toast toast = Toast.makeText(context, "I dati non vengono aggiornati da più di 24ore, si consiglia di aggiornare.", duration);
        	toast.show();
        }
        
        SharedPreferences posizione = getSharedPreferences("Posizione", Context.MODE_PRIVATE);
        String segnvicina = posizione.getString("SegnalazioneVicina", "");
    	TextView nsegnalazioni = (TextView)findViewById(R.id.NSegnalazioni);        	
    	nsegnalazioni.setText(posizione.getInt("SegnalazioniTotali", 0)+"");
    	
        if(segnvicina.length()!=0)
        {
        		utente=posizione.getString("SegnalazioneVicinaUtente", "");
        		dataOra = posizione.getString("SegnalazioneVicinaData", "");
        		float coo[] = new float[2];
        		coo[0] = Float.valueOf(posizione.getString("SegnalazioneVicinaLat", ""));
        		coo[1] = Float.valueOf(posizione.getString("SegnalazioneVicinaLng", ""));
        		immagine=posizione.getString("SegnalazioneVicinaIcona", "");
            	ImageView segnvicini = (ImageView)findViewById(R.id.SegnV);
                segnvicini.setImageDrawable(getResources().getDrawable(Map.ConvertiNomiG(immagine)));
                new Addresses().execute(coo);
        }else
        {
            	//nsegnalazioni.setText("Nessuna segnalazione per oggi, che aspetti a segnalare?");
        }
		   	SharedPreferences segn = context.getSharedPreferences("Segnalazione", 0);
		   	if(segn.getString("Immagine", "").length()!=0)
		   	{
        		dataOra = segn.getString("Data", "");
        	    localita = segn.getString("Localita", "");
        	    immagine2 = segn.getString("Immagine", "");
		        ImageView TuasegnImg = (ImageView)findViewById(R.id.TuaSegnImg);
		        TuasegnImg.setImageDrawable(getResources().getDrawable(Map.ConvertiNomiG(immagine2)));
		    	TextView TuasegnText = (TextView)findViewById(R.id.TuaSegn);
		    	TuasegnText.setText("Località: "+localita+"\nData: "+dataOra);
		   	}else
		   	{
		       new SegnalazioneTua().execute();
		   	}
    }
        
	
    @Override
    public void onResume()
    {
    	super.onResume();
    }
        
    public void reload() {
    	onStop();
        onCreate(getIntent().getExtras());
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	 super.onCreateOptionsMenu(menu);
	 MenuInflater inflater = getMenuInflater();
	 inflater.inflate(R.menu.menu2, menu);
	 return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.Home:
	    	onHomeClick(null);
	        break;
	    }
	    return false;
	}
	
	public void onRefreshClick(View v)
	{
    	new Aggiorna().execute();
	}
	
	
	private class Aggiorna extends AsyncTask<Object, Object, Boolean> {  
          
        @Override 
        protected void onPreExecute() {
        	findViewById(R.id.btn_title_refresh).setVisibility(8);
        	findViewById(R.id.title_refresh_progress).setVisibility(0);
        }  
  
        @Override 
		protected Boolean doInBackground(Object... params) {
			try {
				right=Splash.getData(context);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return right;
		}
		
        @Override 
		protected void onPostExecute(Boolean result)
		{   
        	findViewById(R.id.title_refresh_progress).setVisibility(8);
        	findViewById(R.id.btn_title_refresh).setVisibility(0);
			if(result)
			{
				reload();
		        }else
			{
		        	Context context = getApplicationContext();
		        	CharSequence text = "Errore con la connessione. Riprovare più tardi!";
		        	int duration = Toast.LENGTH_SHORT;

		        	Toast toast = Toast.makeText(context, text, duration);
		        	toast.show();
			}
		}
          
    }
	
	
	private class SegnalazioneTua extends AsyncTask<Object, Object, Boolean> {  
          
        @Override 
        protected void onPreExecute() {
        	findViewById(R.id.TuaSegnProgress2).setVisibility(0);		        	
        	findViewById(R.id.TuaSegnImg).setVisibility(8);
        	findViewById(R.id.TuaSegn).setVisibility(8);
        }  
  
        @Override 
		protected Boolean doInBackground(Object... params) {

    	   	SharedPreferences Login = context.getSharedPreferences("Login", 0);

    	   	int id = Login.getInt("Id", 0);
        	HttpPost httpPost = new HttpPost("http://metwit.net/api/infou/"+id+"/xml");
        	HttpResponse response = Main.simpleHttp(httpPost, context);
            Element root = Main.simpleParser(response);
            NodeList errore = root.getElementsByTagName("segnalazioni");
            
            if(Integer.parseInt(errore.item(0).getFirstChild().getNodeValue())!=0)
            {
            
        	    HttpPost httpPost1 = new HttpPost(API_INFOU+id+"/segnalazioni/xml");
        		HttpResponse response1 = Main.simpleHttp(httpPost1, context);
        	    Element root2 = Main.simpleParser(response1);
        	
        	    NodeList imgs2 = root2.getElementsByTagName("idIcona");
        	    NodeList datas2 = root2.getElementsByTagName("dataOra");
        	    NodeList locs2 = root2.getElementsByTagName("idLocalita");

        		dataOra2 = datas2.item(0).getFirstChild().getNodeValue();
        	    localita2 = locs2.item(0).getFirstChild().getNodeValue();
        	    immagine2 = imgs2.item(0).getFirstChild().getNodeValue();
        	    
                HttpPost httpPost2 = new HttpPost(API_GETL+localita2+"/XML");
            	HttpResponse response2 = Main.simpleHttp(httpPost2, context);
                Element root3 = Main.simpleParser(response2);
                NodeList localitas = root3.getElementsByTagName("latLng");
                String localitaz = localitas.item(0).getFirstChild().getNodeValue();
                
            	Geocoder gc = new Geocoder(context, Locale.getDefault());	    
                
        		String[] coo2 = localitaz.split(",",0);
        		float lat2 = (Float.valueOf(coo2[0]));
        		float lng2 = (Float.valueOf(coo2[1]));
        		try {
        			addresses2 = gc.getFromLocation(lat2, lng2, 1);
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		return true;
            }else
            {
            return false;
            }
		}
		
        @Override 
		protected void onPostExecute(Boolean result)
		{   
        	findViewById(R.id.TuaSegnProgress2).setVisibility(8);		        	
        	findViewById(R.id.TuaSegnImg).setVisibility(0);
        	findViewById(R.id.TuaSegn).setVisibility(0);
			if(result)
			{				
		        ImageView TuasegnImg = (ImageView)findViewById(R.id.TuaSegnImg);
		        TuasegnImg.setImageDrawable(getResources().getDrawable(Map.ConvertiNomiG(immagine2)));
		    	TextView TuasegnText = (TextView)findViewById(R.id.TuaSegn);
		    	String address;
	        	if(addresses2.size()==0)
	        		address = "";
	        	else
	        		address = addresses2.get(0).getLocality();
		    	TuasegnText.setText("Località: "+address+"\nData: "+dataOra2);
			   	SharedPreferences segn = context.getSharedPreferences("Segnalazione", 0);
			   	Editor segnedit = segn.edit();
			   	segnedit.putString("Immagine", immagine2);
			   	segnedit.putString("Localita", address);
			   	segnedit.putString("Data", dataOra2);
			   	segnedit.commit();
			}
		}
          
    }
	
	private class Addresses extends AsyncTask<float[], float[], Boolean> { //Segnalazione vicina 
        
        @Override 
        protected void onPreExecute() {
        	findViewById(R.id.TuaSegnProgress1).setVisibility(0);		        	
        	findViewById(R.id.TuaSegnT).setVisibility(8);
        }  
  
        @Override 
		protected Boolean doInBackground(float[]... params) {
        	Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());	
            try {          	
            	addresses = gc.getFromLocation(params[0][0], params[0][1], 1);
            
            } catch (IOException e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
            	return false;
            }
            return true;
		}
		
        @Override 
		protected void onPostExecute(Boolean result)
		{   
        	findViewById(R.id.TuaSegnProgress1).setVisibility(8);		        	
        	findViewById(R.id.TuaSegnT).setVisibility(0);
        	TextView segnviciniText = (TextView)findViewById(R.id.SegnVTx);
        	String address;
        	if(addresses.size()==0)
        		address = "";
        	else
        		address = addresses.get(0).getLocality();
        	segnviciniText.setText("Località: "+address+"\nUtente: "+utente+"\nData: "+dataOra);		            
        
		}
    }
	
	public static Element simpleParser(HttpResponse response)
	{
        Document docs=null;
		try {
			docs = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.getEntity().getContent());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        Element root=docs.getDocumentElement();	 
		
		return root;		
	}
	
	public static Element simpleParser(String file)
	{
        Document docs=null;
		try {
			docs = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(file));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        Element root=docs.getDocumentElement();	                    
		
		return root;		
	}
	
	public static HttpResponse simpleHttp(HttpPost httpPost, Context contexts)
	{
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("api_key", API_KEY));
        HttpResponse response=null;
        if(client==null)
        {
            client = new DefaultHttpClient();
            //client.getParams().setParameter("http.protocol.content-charset", "UTF-8"); //veze
        }
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            //httpPost.setHeader("Content-Type", "text/xml; charset=utf-8");

            // Execute HTTP Post Request
            response = client.execute(httpPost);

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
		return response;		
	}
	
	public static HttpResponse simpleHttp2(HttpPost httpPost, Context contexts)
	{
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("api_key", API_KEY));
        HttpResponse response=null;

        client = getClient();

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            //httpPost.setHeader("Content-Type", "text/xml; charset=utf-8");

            // Execute HTTP Post Request
            response = client.execute(httpPost);

        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }
		return response;		
	}
	
    
	/**
	 * Creates a HttpClient with http and http parameters
	 */
	public static DefaultHttpClient getClient() {
        DefaultHttpClient ret = null;
        // Parameters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, "utf-8");
        params.setBooleanParameter("http.protocol.expect-continue", false);
        // Schemes for http
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        // Schemes for http and https
        final SSLSocketFactory sslSocketFactory = SSLSocketFactory.getSocketFactory();
        sslSocketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        registry.register(new Scheme("https", sslSocketFactory, 443));
        // Client generation
        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
        ret = new DefaultHttpClient(manager, params);
        return ret;
    }

	public void onHomeClick(View v)
	{
		finish();
		Intent send = new Intent(getApplicationContext(), Home.class);
		send.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		startActivity(send);
	}
	
	public void onSendClick(View v)
	{
		Intent send = new Intent(getApplicationContext(), Meteo.class);
		startActivity(send);
	}
	
}
