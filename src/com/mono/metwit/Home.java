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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import biz.source_code.base64Coder.Base64Coder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.mono.metwit.API.*;

public class Home extends Activity {
	
	SharedPreferences Login;
	SharedPreferences.Editor ledit;
    Button login;
    Context context;
    double loclat;
    double loclng;
    NodeList notes2;
	List<Address> addresses;
	LocationManager manager;
    MyLocationListener2 listener;
	Geocoder gc;
	String value;
	Dialog dialog;
	String urlauth;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        Login = getSharedPreferences("Login", 0);
        ledit = Login.edit();
        context = this;
    	manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener2();
        gc = new Geocoder(getApplicationContext(), Locale.getDefault());
        urlauth = API_URL_AUTH;

        if(Login.getString("User", "").length()==0)
        {
	        setContentView(R.layout.login);        
	        login = (Button)findViewById(R.id.login);
	        login.setOnClickListener( new OnClickListener()
	        {
	        		//Cliccato pulsante Login
					@Override
	                public void onClick(View viewParam)
	                {
	                    EditText usernameEditText = (EditText) findViewById(R.id.user);
	                    EditText passwordEditText = (EditText) findViewById(R.id.pswd);
	                   
	                    String sUserName = usernameEditText.getText().toString().replace(" ","");
	                    String sPassword = passwordEditText.getText().toString();
	                    	                    
	                    HttpPost httpPost1 = new HttpPost(urlauth);
	                    String inputString = sUserName+":"+sPassword;
	                    String base64login=null;

	                    base64login = new String(Base64Coder.encodeString(inputString));

	                    httpPost1.setHeader("Authorization","Basic "+base64login);
	                    HttpResponse response1 = Main.simpleHttp(httpPost1, context);
	                    if(response1==null)
	                    {
	                    	return;
	                    }
	                    Element root1=Main.simpleParser(response1);	                    
	                    NodeList notes1=root1.getElementsByTagName("error");
	                    NodeList notes2=root1.getElementsByTagName("idUsername");

	                    if(Integer.parseInt(notes1.item(0).getFirstChild().getNodeValue())==0)
	                    {
		                    ledit.putString("User", sUserName);
		                    ledit.putString("Password", sPassword);
		                    ledit.putInt("Id", Integer.parseInt(notes2.item(0).getFirstChild().getNodeValue()));
		                    ledit.commit(); 
		                	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		                	imm.hideSoftInputFromWindow(login.getWindowToken(), 0);
		                    reload();
	                    }else
	                    {
		              	  	AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		              	  	dialog.setTitle("Errore");
		              	  	dialog.setMessage("User o Password errati!");
		              	  	dialog.show();
	                    }
	                }
	        }); 
	        
	        Button register = (Button)findViewById(R.id.register);
	        register.setOnClickListener( new OnClickListener()
	        {
	        	//Cliccato pulsante registra faccio vedere un Dialogo
				@Override
				public void onClick(View arg0) {
					
	            	dialog = new Dialog(Home.this); 
	            	dialog.setContentView(R.layout.register);
	            	dialog.setTitle("Registrati");
	            	dialog.show();
	            	
	    	        Button loc = (Button) dialog.findViewById(R.id.LocalitaR);
	    	        final AlertDialog.Builder pos = new AlertDialog.Builder(Home.this);  
	    	      	  
	            	pos.setTitle("Metwit");  
	            	pos.setMessage("Seleziona la città");  
	    	    	
	    	    	loc.setOnClickListener(new OnClickListener() {
	    	            public void onClick(View arg0) {
	    	            	
	    	            	final EditText input = new EditText(context);  
	    	            	pos.setView(input);  
	    	            	  
	    	            	pos.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
	    	            	public void onClick(DialogInterface dialog, int whichButton) {  
	    		            	
	    	            	value = input.getText().toString();
	    	            	new GetLoc().execute(value);

	    	            	}
	    	            	});
	    	            	pos.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
	    	              	  public void onClick(DialogInterface dialog, int whichButton) {  
	    	              	    // Canceled.  
	    	              	  }  
	    	              	});  
	    	              	  
	    	              	pos.show();
	    	            }
	    	    	});

	    	        Button rilloc = (Button) dialog.findViewById(R.id.Rileva);
	    	        rilloc.setOnClickListener(new View.OnClickListener() {
	    	            public void onClick(View arg0) {
	    	            dialog.findViewById(R.id.localitaR).setVisibility(0);		        	
	    	            dialog.findViewById(R.id.Rileva).setVisibility(8);
	    	            dialog.findViewById(R.id.InviaR).setEnabled(false);
	    		        Criteria c = new Criteria();
	    		        c.setAccuracy(Criteria.ACCURACY_FINE);
	    	       		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    	       		if(!settings.getBoolean("GPS", false))
	    	       		{
	    			        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000L, 10.0f, listener);
	    			        int duration = Toast.LENGTH_SHORT;
	    			        Toast toast = Toast.makeText(getApplicationContext(), "Verrà rilevata la posizione usando la rete cellulare...", duration);
	    			        toast.show();
	    	       		}else
	    	       		{
	    			        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 10.0f, listener);
	    			        int duration = Toast.LENGTH_SHORT;
	    			        Toast toast = Toast.makeText(getApplicationContext(), "Verrà rilevata la posizione usando il GPS...", duration);
	    			        toast.show();
	    	       		}
	    	            }
	    	        });
	    	    	
	    	    	Button registrati = (Button) dialog.findViewById(R.id.InviaR);
	    	        registrati.setOnClickListener( new OnClickListener()
	    	        {
	    	        	//Cliccato pulsante registrati
						@Override
						public void onClick(View v) {
 
		                    EditText UserEditText = (EditText) dialog.findViewById(R.id.UserR);	
		                    EditText PassEditText = (EditText) dialog.findViewById(R.id.PasswordR);	                   
		                    EditText MailEditText = (EditText) dialog.findViewById(R.id.MailR);	                   
		                    final String user = UserEditText.getText().toString();
		                    final String pass = PassEditText.getText().toString();
		                    final String mail = MailEditText.getText().toString();
		                    
		                    if(user==null || pass==null || mail==null || notes2==null)
		                    {
			              	  	AlertDialog.Builder errordialog = new AlertDialog.Builder(context);
			              	  	errordialog.setTitle("Errore");
			              	  	errordialog.setMessage("Compilare tutti i form");
			              	  	errordialog.show();
			              	  	return;
		                    }
		                    
		                    String userT = trasformaUrl(user);
		                    String passT = trasformaUrl(pass);
		                    String mailT = trasformaUrl(mail.replace(" ",""));

		                    String urlRegistrazione = API_URL_REG +userT+"/"+passT+"/"+mailT+"/"+notes2.item(0).getFirstChild().getNodeValue()+"/XML";
		                    
		                    HttpPost httpPost3 = new HttpPost(urlRegistrazione);
		                    HttpResponse response3 = Main.simpleHttp(httpPost3, context);
		                   		                    
		                    Element root3 = Main.simpleParser(response3);
		                    String error = root3.getElementsByTagName("error").item(0).getFirstChild().getNodeValue();
							if(Integer.parseInt(error)==1)
							{
								NodeList messaggi=root3.getElementsByTagName("message");
			              	  	AlertDialog.Builder errordialog = new AlertDialog.Builder(context);
			              	  	errordialog.setTitle("Errore");
			              	  	errordialog.setMessage(messaggi.item(0).getFirstChild().getNodeValue());
			              	  	errordialog.show();
							}
							else
							{
								dialog.cancel();
											                    
			                    HttpPost httpPost = new HttpPost(urlauth);
			                    String inputString = user+":"+pass;
			                    String base64login=null;

			                    base64login = new String(Base64Coder.encodeString(inputString));

			                    httpPost.setHeader("Authorization","Basic "+base64login);
			                    HttpResponse response = Main.simpleHttp(httpPost, context);
			                    if(response==null)
			                    {
			                    	return;
			                    }
			                    Element root=Main.simpleParser(response);	                    
			                    NodeList notes1=root.getElementsByTagName("error");
			                    NodeList notes2=root.getElementsByTagName("idUsername");

			                    if(Integer.parseInt(notes1.item(0).getFirstChild().getNodeValue())==0)
			                    {
				                    ledit.putString("User", user);
				                    ledit.putString("Password", pass);
				                    ledit.putInt("Id", Integer.parseInt(notes2.item(0).getFirstChild().getNodeValue()));
				                    ledit.commit(); 
			                    }
								
			              	  AlertDialog.Builder okdialog = new AlertDialog.Builder(context);
				              	okdialog.setTitle("Registrato!");
				              	okdialog.setMessage("Il tuo account è già attivo. Segnala subito!");
				              	okdialog.setOnCancelListener(new OnCancelListener() {
				                    public void onCancel(DialogInterface dialog) {
				                    	reload();
				            	      Intent segnala = new Intent(context, Meteo.class);
				            	      startActivity(segnala);
				                    }});
				              	okdialog.show();
							}
						}
	    	        	
	    	        });
				}
	        });
	        
        }else//Già loggato
        {
    	    setContentView(R.layout.home);
            View sView = findViewById(R.id.ScrollImg);
            sView.setHorizontalScrollBarEnabled(false);
            sView.setVerticalScrollBarEnabled(false);
                        
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            boolean foto = settings.getBoolean("Foto", true);
            
            File fotoF = new File("/data/data/com.mono.metwit/files/ultimefoto.xml");
            
            if(foto && fotoF.exists())
            {
            	Element root=Main.simpleParser("/data/data/com.mono.metwit/files/ultimefoto.xml");	                    
	            String photo0 = root.getElementsByTagName("photos").item(0).getFirstChild().getNodeValue().split(",")[0];
	            String photo1 = root.getElementsByTagName("photos").item(1).getFirstChild().getNodeValue().split(",")[0];
	            String photo2 = root.getElementsByTagName("photos").item(2).getFirstChild().getNodeValue().split(",")[0];
	            String photo3 = root.getElementsByTagName("photos").item(3).getFirstChild().getNodeValue().split(",")[0];
	            String photo4 = root.getElementsByTagName("photos").item(4).getFirstChild().getNodeValue().split(",")[0];
	            String user0 = root.getElementsByTagName("username").item(0).getFirstChild().getNodeValue();
	            String user1 = root.getElementsByTagName("username").item(1).getFirstChild().getNodeValue();
	            String user2 = root.getElementsByTagName("username").item(2).getFirstChild().getNodeValue();
	            String user3 = root.getElementsByTagName("username").item(3).getFirstChild().getNodeValue();
	            String user4 = root.getElementsByTagName("username").item(4).getFirstChild().getNodeValue();
	            final String id0 = root.getElementsByTagName("idSegnalazione").item(0).getFirstChild().getNodeValue();
	            final String id1 = root.getElementsByTagName("idSegnalazione").item(1).getFirstChild().getNodeValue();
	            final String id2 = root.getElementsByTagName("idSegnalazione").item(2).getFirstChild().getNodeValue();
	            final String id3 = root.getElementsByTagName("idSegnalazione").item(3).getFirstChild().getNodeValue();
	            final String id4 = root.getElementsByTagName("idSegnalazione").item(4).getFirstChild().getNodeValue();
	            
	            String[] params0 = {user0,photo0};
	            new GetImg1().execute(params0);
	            String[] params1 = {user1,photo1};
	            new GetImg2().execute(params1);
	            String[] params2 = {user2,photo2};
	            new GetImg3().execute(params2);
	            String[] params3 = {user3,photo3};
	            new GetImg4().execute(params3);
	            String[] params4 = {user4,photo4};
	            new GetImg5().execute(params4);
            
	    	    ImageView img1 = (ImageView) findViewById(R.id.Img1);
	    	    img1.setOnClickListener(new OnClickListener() {
		            public void onClick(View arg0) {
		             
		            	HttpPost httpPost = new HttpPost(API_GET_URL_SEGN+id0+"/xml");
	
		                HttpResponse respimg = Main.simpleHttp(httpPost, context);
		                Element root=Main.simpleParser(respimg);	                    
		                String url = root.getElementsByTagName("response").item(0).getFirstChild().getNodeValue();
		                Intent i = new Intent(Intent.ACTION_VIEW);
		                i.setData(Uri.parse(url));
		            	startActivity(i);
		            }
	    	    });
	    	    ImageView img2 = (ImageView) findViewById(R.id.Img2);
	    	    img2.setOnClickListener(new OnClickListener() {
		            public void onClick(View arg0) {
		               
		            	HttpPost httpPost = new HttpPost(API_GET_URL_SEGN+id1+"/xml");
	
		                HttpResponse respimg = Main.simpleHttp(httpPost, context);
		                Element root=Main.simpleParser(respimg);	                    
		                String url = root.getElementsByTagName("response").item(0).getFirstChild().getNodeValue();
		                Intent i = new Intent(Intent.ACTION_VIEW);
		                i.setData(Uri.parse(url));
		            	startActivity(i);
		            }
	    	    });
	    	    ImageView img3 = (ImageView) findViewById(R.id.Img3);
	    	    img3.setOnClickListener(new OnClickListener() {
		            public void onClick(View arg0) {
		               
		            	HttpPost httpPost = new HttpPost(API_GET_URL_SEGN+id2+"/xml");
	
		                HttpResponse respimg = Main.simpleHttp(httpPost, context);
		                Element root=Main.simpleParser(respimg);	                    
		                String url = root.getElementsByTagName("response").item(0).getFirstChild().getNodeValue();
		                Intent i = new Intent(Intent.ACTION_VIEW);
		                i.setData(Uri.parse(url));
		            	startActivity(i);
		            }
	    	    });
	    	    ImageView img4 = (ImageView) findViewById(R.id.Img4);
	    	    img4.setOnClickListener(new OnClickListener() {
		            public void onClick(View arg0) {
		                
		            	HttpPost httpPost = new HttpPost(API_GET_URL_SEGN+id3+"/xml");
	
		                HttpResponse respimg = Main.simpleHttp(httpPost, context);
		                Element root=Main.simpleParser(respimg);	                    
		                String url = root.getElementsByTagName("response").item(0).getFirstChild().getNodeValue();
		                Intent i = new Intent(Intent.ACTION_VIEW);
		                i.setData(Uri.parse(url));
		            	startActivity(i);
		            }
	    	    });
	    	    ImageView img5 = (ImageView) findViewById(R.id.Img5);
	    	    img5.setOnClickListener(new OnClickListener() {
		            public void onClick(View arg0) {
		               
		            	HttpPost httpPost = new HttpPost(API_GET_URL_SEGN+id4+"/xml");
	
		                HttpResponse respimg = Main.simpleHttp(httpPost, context);
		                Element root=Main.simpleParser(respimg);	                    
		                String url = root.getElementsByTagName("response").item(0).getFirstChild().getNodeValue();
		                Intent i = new Intent(Intent.ACTION_VIEW);
		                i.setData(Uri.parse(url));
		            	startActivity(i);
		            }
	    	    });
            }else
            {
            	HorizontalScrollView scroll = (HorizontalScrollView) findViewById(R.id.ScrollImg);
            	scroll.setVisibility(4);
            }
        }
	}
	
	
	 private class GetImg1 extends AsyncTask<String, Bitmap, Bitmap> {  
         
	        @Override 
	        protected void onPreExecute() {
	        	findViewById(R.id.Img1).setVisibility(8);		        	
	        	findViewById(R.id.Progress1).setVisibility(0);
	        }  
	  
	        @Override 
			protected Bitmap doInBackground(String...params) {
	            Map.DownloadFromUrl(URL_AVATAR+params[0].toLowerCase()+"-metwit/thumb-"+params[1], params[1]);
	            Bitmap bm = null;
	            try {
	                    FileInputStream fis = new FileInputStream("/sdcard/Android/data/com.mono.metwit/cache/"+params[1]);
	                    BufferedInputStream bis = new BufferedInputStream(fis);
	                    bm = BitmapFactory.decodeStream(bis);
	            } catch (FileNotFoundException e) {

	            }
	        	return bm;
			}
			
	        @Override 
			protected void onPostExecute(Bitmap bm)
			{  
	    	    ImageView img1 = (ImageView) findViewById(R.id.Img1);
	    	    img1.setVisibility(0);		        	
	        	findViewById(R.id.Progress1).setVisibility(8);
	    	    img1.setImageBitmap(bm);
			}
	          
	    }
	 
	 private class GetImg2 extends AsyncTask<String, Bitmap, Bitmap> {  
         
	        @Override 
	        protected void onPreExecute() {
	        	findViewById(R.id.Img2).setVisibility(8);		        	
	        	findViewById(R.id.Progress2).setVisibility(0);
	        }  
	  
	        @Override 
			protected Bitmap doInBackground(String...params) {
	            Map.DownloadFromUrl(URL_AVATAR+params[0].toLowerCase()+"-metwit/thumb-"+params[1], params[1]);
	            Bitmap bm = null;
	            try {
	                    FileInputStream fis = new FileInputStream("/sdcard/Android/data/com.mono.metwit/cache/"+params[1]);
	                    BufferedInputStream bis = new BufferedInputStream(fis);
	                    bm = BitmapFactory.decodeStream(bis);
	            } catch (FileNotFoundException e) {

	            }
	        	return bm;
			}
			
	        @Override 
			protected void onPostExecute(Bitmap bm)
			{  
	    	    ImageView img1 = (ImageView) findViewById(R.id.Img2);
	    	    img1.setVisibility(0);		        	
	        	findViewById(R.id.Progress2).setVisibility(8);
	    	    img1.setImageBitmap(bm);
			}
	          
	    } 
	 
	 private class GetImg3 extends AsyncTask<String, Bitmap, Bitmap> {  
         
	        @Override 
	        protected void onPreExecute() {
	        	findViewById(R.id.Img3).setVisibility(8);		        	
	        	findViewById(R.id.Progress3).setVisibility(0);
	        }  
	  
	        @Override 
			protected Bitmap doInBackground(String...params) {
	            Map.DownloadFromUrl(URL_AVATAR+params[0].toLowerCase()+"-metwit/thumb-"+params[1], params[1]);
	            Bitmap bm = null;
	            try {
	                    FileInputStream fis = new FileInputStream("/sdcard/Android/data/com.mono.metwit/cache/"+params[1]);
	                    BufferedInputStream bis = new BufferedInputStream(fis);
	                    bm = BitmapFactory.decodeStream(bis);
	            } catch (FileNotFoundException e) {

	            }
	        	return bm;
			}
			
	        @Override 
			protected void onPostExecute(Bitmap bm)
			{  
	    	    ImageView img1 = (ImageView) findViewById(R.id.Img3);
	    	    img1.setVisibility(0);		        	
	        	findViewById(R.id.Progress3).setVisibility(8);
	    	    img1.setImageBitmap(bm);
			}
	          
	    } 
	 
	 private class GetImg4 extends AsyncTask<String, Bitmap, Bitmap> {  
         
	        @Override 
	        protected void onPreExecute() {
	        	findViewById(R.id.Img4).setVisibility(8);		        	
	        	findViewById(R.id.Progress4).setVisibility(0);
	        }  
	  
	        @Override 
			protected Bitmap doInBackground(String...params) {
	            Map.DownloadFromUrl(URL_AVATAR+params[0].toLowerCase()+"-metwit/thumb-"+params[1], params[1]);
	            Bitmap bm = null;
	            try {
	                    FileInputStream fis = new FileInputStream("/sdcard/Android/data/com.mono.metwit/cache/"+params[1]);
	                    BufferedInputStream bis = new BufferedInputStream(fis);
	                    bm = BitmapFactory.decodeStream(bis);
	            } catch (FileNotFoundException e) {

	            }
	        	return bm;
			}
			
	        @Override 
			protected void onPostExecute(Bitmap bm)
			{  
	    	    ImageView img1 = (ImageView) findViewById(R.id.Img4);
	    	    img1.setVisibility(0);		        	
	        	findViewById(R.id.Progress4).setVisibility(8);
	    	    img1.setImageBitmap(bm);
			}
	          
	    } 
	 
	 private class GetImg5 extends AsyncTask<String, Bitmap, Bitmap> {  
         
	        @Override 
	        protected void onPreExecute() {
	        	findViewById(R.id.Img5).setVisibility(8);		        	
	        	findViewById(R.id.Progress5).setVisibility(0);
	        }  
	  
	        @Override 
			protected Bitmap doInBackground(String...params) {
	            Map.DownloadFromUrl(URL_AVATAR+params[0].toLowerCase()+"-metwit/thumb-"+params[1], params[1]);
	            Bitmap bm = null;
	            try {
	                    FileInputStream fis = new FileInputStream("/sdcard/Android/data/com.mono.metwit/cache/"+params[1]);
	                    BufferedInputStream bis = new BufferedInputStream(fis);
	                    bm = BitmapFactory.decodeStream(bis);
	            } catch (FileNotFoundException e) {

	            }
	        	return bm;
			}
			
	        @Override 
			protected void onPostExecute(Bitmap bm)
			{  
	    	    ImageView img1 = (ImageView) findViewById(R.id.Img5);
	    	    img1.setVisibility(0);		        	
	        	findViewById(R.id.Progress5).setVisibility(8);
	    	    img1.setImageBitmap(bm);
			}
	          
	    } 
	
    
    public void reload() {
    	onStop();
        onCreate(getIntent().getExtras());
    }
	
	public void onMainClick(View v)
	{
		Intent home = new Intent(getApplicationContext(),Main.class);
		startActivity(home);
	}

	public void onInviaClick(View v)
	{
		Intent meteo = new Intent(getApplicationContext(),Meteo.class);
		startActivity(meteo);
	}
	
	public void onMappaClick(View v)
	{
		Intent Map = new Intent(getApplicationContext(),Map.class);
		startActivity(Map);
	}
	
	public void onSettingsClick(View v)
	{
		Intent settings = new Intent(getApplicationContext(),Settings.class);
		startActivity(settings);
	}
	
	public void onSendClick(View v)
	{
		Intent send = new Intent(getApplicationContext(), Meteo.class);
		startActivity(send);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	 super.onCreateOptionsMenu(menu);
	 MenuInflater inflater = getMenuInflater();
	 inflater.inflate(R.menu.menu, menu);
	 return true;
	}
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	 super.onPrepareOptionsMenu(menu);
	 if((Login.getString("User","")).length()==0)
	 {
		 menu.findItem(R.id.LogOut).setEnabled(false);
	 }else
	 {
		 menu.findItem(R.id.LogOut).setEnabled(true);
	 }	 
	 return true;		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.LogOut:
	    	ledit.clear();
	    	ledit.commit();
	    	reload();
	        break;
	    case R.id.Info:
	    	final Dialog infodialog = new Dialog(Home.this);
	    	infodialog.setContentView(R.layout.infodialog);
	    	infodialog.setTitle("Metwit info");
	    	infodialog.setCancelable(true);
	    	
	    	Linkify.addLinks((TextView) infodialog.findViewById(R.id.Sitii), Linkify.ALL);
	    	Linkify.addLinks((TextView) infodialog.findViewById(R.id.mail), Linkify.ALL);


            Button button = (Button) infodialog.findViewById(R.id.Cancel);
            button.setOnClickListener(new OnClickListener() {
            @Override
                public void onClick(View v) {
                    infodialog.cancel();
                }
            });
              
            infodialog.show();
	    	break;
	    }
	    return false;
	}
	
	public void onHomeClick(View v)
	{
		finish();
		Intent send = new Intent(getApplicationContext(), Home.class);
		send.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		startActivity(send);
	}
	
	private class MyLocationListener2 implements LocationListener {

        public void onLocationChanged(Location location) {
                try {
                        if (location != null) {
		                    
    						HttpPost httpPost2 = new HttpPost(API_CHECK_LATLNG+location.getLatitude()+","+location.getLongitude()+"/XML");
    	                    HttpResponse response2 = Main.simpleHttp(httpPost2, context);

    	                    Element root2=Main.simpleParser(response2);	                    
    	                    notes2=root2.getElementsByTagName("idLocalita");
		    		        int duration = Toast.LENGTH_SHORT;
		    		        Toast toast = Toast.makeText(getApplicationContext(), "Fatto!", duration);
		    		        toast.show();
	    	            	dialog.findViewById(R.id.localitaR).setVisibility(8);		        	
	    	            	dialog.findViewById(R.id.Rileva).setVisibility(0);
	    	            	dialog.findViewById(R.id.InviaR).setEnabled(true);
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

		@Override
		public void onProviderDisabled(String provider) {
   			AlertDialog.Builder error = new AlertDialog.Builder(context);  	            	  
        	error.setTitle("Errore");  
        	error.setMessage("La localizzazione selezionata è disattivata nelle impostazioni Android\nClicca ok per andare nelle impostazioni");
        	error.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int item) {
	            	dialog.dismiss();
	            	startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
	            }
        	});
        	error.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int item) {
	            	dialog.dismiss();
	            	}
        	}); 
        	error.show();
        	dialog.findViewById(R.id.localitaR).setVisibility(8);		        	
        	dialog.findViewById(R.id.Rileva).setVisibility(0);
        	dialog.findViewById(R.id.InviaR).setEnabled(true);
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
    }
	
    private class GetLoc extends AsyncTask<String,  List<Address>,  List<Address>> {  
          
        @Override 
        protected void onPreExecute() {
        	dialog.findViewById(R.id.trasformaR).setVisibility(0);		        	
        	dialog.findViewById(R.id.LocalitaR).setVisibility(8);
        	dialog.findViewById(R.id.InviaR).setEnabled(false);
        }  
  
        @Override 
		protected List<Address>  doInBackground(String... nome) {
        	try {
				addresses = gc.getFromLocationName(nome[0], 1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return addresses;
		}
		
        @Override 
		protected void onPostExecute( List<Address> adresses)
		{  
			if(addresses.size()>0)
			{				
		        int duration = Toast.LENGTH_SHORT;
		        Toast toast = Toast.makeText(getApplicationContext(), "Coordinate trovate per la località "+value, duration);
		        toast.show();
                loclat = addresses.get(0).getLatitude();
                loclng = addresses.get(0).getLongitude();
                
        		HttpPost httpPost2 = new HttpPost(API_CHECK_LATLNG+loclat+","+loclng+"/XML");
                HttpResponse response2 = Main.simpleHttp(httpPost2, context);

                Element root2=Main.simpleParser(response2);	                    
                notes2=root2.getElementsByTagName("idLocalita");
			}else
			{
            	AlertDialog.Builder alert = new AlertDialog.Builder(context);  
              	  
            	alert.setTitle("Errore");  
            	alert.setMessage("La località non è stata trovata"); 
            	alert.show();
			}
			dialog.findViewById(R.id.trasformaR).setVisibility(8);		        	
        	dialog.findViewById(R.id.LocalitaR).setVisibility(0);
        	dialog.findViewById(R.id.InviaR).setEnabled(true);
		}
          
    }  
    public String trasformaUrl (String stringa)
    {
        URI uri=null;
		try {
			uri = new URI("http", stringa, null);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        return uri.toASCIIString().replace("http:", "");
    }
}
