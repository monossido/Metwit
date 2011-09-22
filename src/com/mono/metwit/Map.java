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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.ByteArrayBuffer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import de.android1.overlaymanager.ManagedOverlay;
import de.android1.overlaymanager.ManagedOverlayGestureDetector;
import de.android1.overlaymanager.ManagedOverlayItem;
import de.android1.overlaymanager.OverlayManager;
import de.android1.overlaymanager.ZoomEvent;
import de.android1.overlaymanager.lazyload.LazyLoadCallback;
import de.android1.overlaymanager.lazyload.LazyLoadException;

import static com.mono.metwit.API.*;

public class Map extends MapActivity {

	ProgressDialog myProgressDialog = null;
	Context context;
	OverlayManager overlayManager;
	MapController mc;
	ManagedOverlay managedOverlay;
	boolean right;
	Cursor c;
	GeoPoint[][] Lista;
	
    @Override
	public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        context = this;

		CreaArray();//Array punti
        
        SharedPreferences Login = getSharedPreferences("Login", 0);

        if(Login.getString("User", "").length()==0)
        {
        	ImageButton send = (ImageButton) findViewById(R.id.Send);
        	send.setVisibility(8);
        }
        
        Calendar now = Calendar.getInstance();
        long data = now.getTimeInMillis();
        Long dataAggiornamento = Login.getLong("DataAggiornamento", 0);
        if(data-dataAggiornamento>86400000)
        {
        	int duration = Toast.LENGTH_SHORT;

        	Toast toast = Toast.makeText(context, "I dati non vengono aggiornati da più di 24ore, si consiglia di aggiornare.", duration);
        	toast.show();
        }
        
        MapView mapView = (MapView) findViewById(R.id.mapview);
		overlayManager = new OverlayManager(getApplicationContext(), mapView);

        mapView.setBuiltInZoomControls(true);
        
        mc = mapView.getController();
        GeoPoint p = new GeoPoint(
                (int) (41.900*1E6), 
                (int) (12.500*1E6));
        mc.animateTo(p);
        mc.setZoom(7);
       
    	overlayManager = new OverlayManager(getApplication(), mapView);
    	createOverlayWithLazyLoading("1");
    	createOverlayWithLazyLoading("2");
    	createOverlayWithLazyLoading("3");
    	createOverlayWithLazyLoading("4");
    	createOverlayWithLazyLoading("5");
    	createOverlayWithLazyLoading("6");
    	createOverlayWithLazyLoading("7");
    	createOverlayWithLazyLoading("8");
    	createOverlayWithLazyLoading("9");
    	createOverlayWithLazyLoading("10");
    	createOverlayWithLazyLoading("11");
    	createOverlayWithLazyLoading("12");
    	createOverlayWithLazyLoading("13");
    	createOverlayWithListener();
        
    }
	
	public void onRefreshClick(View v) {
    	new Aggiorna().execute();
	}
	
	private void CreaArray()
	{
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.mono.metwit/databases/Metwit.db", null, 1);
        c = db.query("Segnalazioni",new String[] {"Immagine", "Lat", "Lng"},null, null, null, null, null);

    	
        SharedPreferences posizione = getSharedPreferences("Posizione", Context.MODE_PRIVATE);
        int totali = posizione.getInt("SegnalazioniTotali", 0);
        totali++;
        Lista= new GeoPoint[14][totali];//Array [condizioniMeteoPossibili][totaleSegnalazioni] abbstanza grande TODO da sistemare
        for(int i =0;i<14;i++)
        {
        	for(int z=0;z<totali;z++)
        	{
        		Lista[i][z]=null;
        	}
        }
		if(c.moveToFirst())
    	for(;!c.isAfterLast();c.moveToNext())
    	{
    		boolean esci=false;
    		for(int z=0;z<totali && !esci;z++)
    		{
    			int immagine = c.getInt(c.getColumnIndex("Immagine"));
    			if(immagine==999)	immagine=13;
    			if(Lista[immagine][z]==null)
    			{
    	    		Lista[immagine][z] = new GeoPoint((int)(c.getFloat(c.getColumnIndex("Lat"))*1E6), (int)(c.getFloat(c.getColumnIndex("Lng"))*1E6));
    	    		esci=true;
    			}
    		}
    	}
        
        c.close();
        db.close();
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
				CreaArray();
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
    
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public static int ConvertiNomi(String img)
	{
		int id = 0;
		if(Integer.parseInt(img)==1)
		{
			id=R.drawable.sole;
		}
		else if(Integer.parseInt(img)==2)
		{
			id=R.drawable.pioggia;
		}
		else if(Integer.parseInt(img)==3)
		{
			id=R.drawable.temporale;
		}
		else if(Integer.parseInt(img)==4)
		{
			id=R.drawable.neve;
		}
		else if(Integer.parseInt(img)==5)
		{
			id=R.drawable.variabile;
		}
		else if(Integer.parseInt(img)==6)
		{
			id=R.drawable.nuvoloso;
		}
		else if(Integer.parseInt(img)==7)
		{
			id=R.drawable.grandine;
		}
		else if(Integer.parseInt(img)==8)
		{
			id=R.drawable.maremosso;
		}
		else if(Integer.parseInt(img)==9)
		{
			id=R.drawable.marecalmo;
		}
		else if(Integer.parseInt(img)==10)
		{
			id=R.drawable.nebbia;
		}
		else if(Integer.parseInt(img)==11)
		{
			id=R.drawable.nevischio;
		}
		else if(Integer.parseInt(img)==12)
		{
			id=R.drawable.vento;
		}
		else if(Integer.parseInt(img)==13)
		{
			id=R.drawable.tweet;
		}
		return id;
	}

	public void createOverlayWithLazyLoading(final String tipo) {
        //This time we use our own marker
		ImageView loaderanim = (ImageView) findViewById(R.id.loader);
		managedOverlay = overlayManager.createOverlay("lazyloadOverlay", getResources().getDrawable(ConvertiNomi(tipo)));
		
		managedOverlay.enableLazyLoadAnimation(loaderanim);
		
		managedOverlay.setLazyLoadCallback(new LazyLoadCallback() {
			@Override
			public List<ManagedOverlayItem> lazyload(GeoPoint topLeft, GeoPoint bottomRight, ManagedOverlay overlay) throws LazyLoadException {
				List<ManagedOverlayItem> items = new LinkedList<ManagedOverlayItem>();
				try {
					List<GeoPoint> marker = GeoHelper.findMarker(topLeft, bottomRight, overlay.getZoomlevel(), tipo, context, Lista);
					for (int i = 0; i < marker.size(); i++) {
						GeoPoint point = marker.get(i);
						ManagedOverlayItem item = new ManagedOverlayItem(point, tipo, null);
						items.add(item);
					}
					// lets simulate a latency
					//TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					throw new LazyLoadException(e.getMessage());
				}
				return items;
			}
		});
		managedOverlay.setOnOverlayGestureListener(new ManagedOverlayGestureDetector.OnOverlayGestureListener() {

			@Override
			public boolean onDoubleTap(MotionEvent arg0, ManagedOverlay arg1,
					GeoPoint arg2, ManagedOverlayItem arg3) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onLongPress(MotionEvent arg0, ManagedOverlay arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onLongPressFinished(MotionEvent arg0,
					ManagedOverlay arg1, GeoPoint arg2, ManagedOverlayItem arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean onScrolled(MotionEvent arg0, MotionEvent arg1,
					float arg2, float arg3, ManagedOverlay arg4) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onSingleTap(MotionEvent arg0, ManagedOverlay arg1,
					GeoPoint arg2, ManagedOverlayItem arg3) {
				if(arg3!=null)
				{
					String id=getId(arg3.getPoint());
					SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.mono.metwit/databases/Metwit.db", null, 1);
			        c = db.query("Segnalazioni",null,"Id="+id, null, null, null, null);
			        if(c.moveToFirst());
					String commento = c.getString(c.getColumnIndex("Commento"));
					String utente = c.getString(c.getColumnIndex("Utente"));;
					String dataOra = c.getString(c.getColumnIndex("Data"));;
					Float lat = c.getFloat(c.getColumnIndex("Lat"));
					Float lng = c.getFloat(c.getColumnIndex("Lng"));;
					int from = c.getInt(c.getColumnIndex("Agent"));
					int idUtente = c.getInt(c.getColumnIndex("idUtente"));
					c.close();
					db.close();

					  final View image = findViewById(R.id.image);
					  image.setVisibility(0);
					  image.setOnClickListener(new View.OnClickListener() {
				            public void onClick(View arg0) {
				            	image.setVisibility(8);
				            }
					  });
					  ImageView meteo = (ImageView) findViewById(R.id.meteo);
					  meteo.setImageDrawable(context.getResources().getDrawable(ConvertiNomiG(arg3.getTitle())));
					  float coo[] = new float[2];
					  coo[0]=lat;
					  coo[1]=lng;
					  new Addresses().execute(coo);
					  TextView userT = (TextView) findViewById(R.id.User);
					  userT.setText(utente);
					  TextView commentoT = (TextView) findViewById(R.id.commento);
					  if(commento.length()!=0)
					  {
						  commentoT.setVisibility(0);
						  commentoT.setText("Commento: "+commento);
					  }else{
						  commentoT.setVisibility(8);
					  }					 
					  TextView dataT = (TextView) findViewById(R.id.data);
					  dataT.setText("Data: "+dataOra);
					  ImageView fromI = (ImageView) findViewById(R.id.from);
					  if(from==2)
					  {
						  fromI.setImageDrawable(context.getResources().getDrawable(R.drawable.byfacebook));
					  }else if(from==3)
					  {
						  fromI.setImageDrawable(context.getResources().getDrawable(R.drawable.byandroid));
					  }else if(from==4)
					  {
						  fromI.setImageDrawable(context.getResources().getDrawable(R.drawable.byiphone));
					  }else if(from==5)
					  {
						  fromI.setImageDrawable(context.getResources().getDrawable(R.drawable.bytwitter));
					  }else
					  {
						  fromI.setImageDrawable(null);
					  }
					  
			          SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			          boolean avatarB = settings.getBoolean("AvatarM", true);
			            
			          if(avatarB)
			          {
						  HttpPost httpPost = new HttpPost(API_INFOU+idUtente+"/xml");
						  HttpResponse imgReponse = Main.simpleHttp(httpPost, context);
						  Element root = Main.simpleParser(imgReponse);
						  NodeList avatars = root.getElementsByTagName("avatar");
					  
						  ImageView avatarImg = (ImageView) findViewById(R.id.avatar);
			              avatarImg.setVisibility(8);
						  String nomeimg = null;
						  if(avatars.item(0).hasChildNodes())
						  {
							  String avatar = avatars.item(0).getFirstChild().getNodeValue();
							  						  
							  if(avatar.length()==0)
							  {
								  return true;
							  }else
							  {
								  nomeimg = new File(avatar).getName();
								  File f = new File("/Android/data/com.mono.metwit/cache/"+nomeimg);
								  if(!f.exists())
								  {
									  DownloadFromUrl(avatar, nomeimg);
								  }
							  }
							  Bitmap bm = null;
				                try {
				                        FileInputStream fis = new FileInputStream("/sdcard/Android/data/com.mono.metwit/cache/"+nomeimg);
				                        BufferedInputStream bis = new BufferedInputStream(fis);
				                        bm = BitmapFactory.decodeStream(bis);
				                } catch (FileNotFoundException e) {
	
				                }
				                avatarImg.setVisibility(0);
								avatarImg.setImageBitmap(bm);
						  }
			          }
					  return true;	
				}
				return false;

			  
			}

			@Override
			public boolean onZoom(ZoomEvent arg0, ManagedOverlay arg1) {
				// TODO Auto-generated method stub
				return false;
			}

		});
		overlayManager.populate();
		managedOverlay.invokeLazyLoad(1000);
	}
	
	private class Addresses extends AsyncTask<float[], float[], String> { //Segnalazione vicina 
        
        @Override 
        protected void onPreExecute() {
        	findViewById(R.id.LocalitaProgress1).setVisibility(0);		        	
        	findViewById(R.id.localita).setVisibility(4);
        }  
  
        @Override 
		protected String doInBackground(float[]... params) {
        	Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
        	List<Address> addresses;
            try {          	
            	addresses = gc.getFromLocation(params[0][0], params[0][1], 1);
            } catch (IOException e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
            	return "";
            }
            if(addresses.size()==0)
            	return "";
            else
            	return addresses.get(0).getLocality();
		}
		
        @Override 
		protected void onPostExecute(String result)
		{   
        	
        	findViewById(R.id.LocalitaProgress1).setVisibility(8);		        	
        	findViewById(R.id.localita).setVisibility(0);
        	TextView localitaT = (TextView) findViewById(R.id.localita);
        	localitaT.setText(result);		                    
		}
    }

	
	public void createOverlayWithListener() {
		ManagedOverlay managedOverlay = overlayManager.createOverlay("listenerOverlay");
		managedOverlay.setOnOverlayGestureListener(new ManagedOverlayGestureDetector.OnOverlayGestureListener() {
			@Override
			public boolean onZoom(ZoomEvent zoom, ManagedOverlay overlay) {
				return false;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
				mc.animateTo(point);
				mc.zoomIn();
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e, ManagedOverlay overlay) {
			}

			@Override
			public void onLongPressFinished(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {
				
			}

			@Override
			public boolean onScrolled(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY, ManagedOverlay overlay) {
				return false;
			}

			@Override
			public boolean onSingleTap(MotionEvent e, ManagedOverlay overlay, GeoPoint point, ManagedOverlayItem item) {

				return false;

			}
		});
		overlayManager.populate();
	}
	
	public String getId(GeoPoint p)
	{
		String id="";
		
        SQLiteDatabase db = SQLiteDatabase.openDatabase("/data/data/com.mono.metwit/databases/Metwit.db", null, 1);
        c = db.query("Segnalazioni",new String[] {"Id", "Lat", "Lng"},null, null, null, null, null);
        
        if(c.moveToFirst());
        for(;!c.isAfterLast();c.moveToNext())
        {
        	if(Arrotonda(p.getLatitudeE6()/1E6, 5)==Arrotonda(c.getFloat(c.getColumnIndex("Lat")), 5) && Arrotonda(p.getLongitudeE6()/1E6, 5)==Arrotonda(c.getFloat(c.getColumnIndex("Lng")), 5))
        	{
        		id=c.getInt(c.getColumnIndex("Id"))+"";
        	}
        }
		c.close();
		db.close();
		return id;
	}
	
	public static double Arrotonda (double number, int decimals)
	{
		double factor = Math.pow (10, decimals);
		double risultato = (Math.floor (number * factor + 0.5) / factor);
		if(number<0)
				risultato = -risultato;
	    return risultato;
	}
	
	public static int ConvertiNomiG(String img)//TODO case/switch
	{
		int id = 0;
		if(Integer.parseInt(img)==1)
		{
			id=R.drawable.soleg;
		}
		else if(Integer.parseInt(img)==2)
		{
			id=R.drawable.pioggiag;
		}
		else if(Integer.parseInt(img)==3)
		{
			id=R.drawable.temporaleg;
		}
		else if(Integer.parseInt(img)==4)
		{
			id=R.drawable.neveg;
		}
		else if(Integer.parseInt(img)==5)
		{
			id=R.drawable.variabileg;
		}
		else if(Integer.parseInt(img)==6)
		{
			id=R.drawable.nuvolosog;
		}
		else if(Integer.parseInt(img)==7)
		{
			id=R.drawable.grandineg;
		}
		else if(Integer.parseInt(img)==8)
		{
			id=R.drawable.maremossog;
		}
		else if(Integer.parseInt(img)==9)
		{
			id=R.drawable.marecalmog;
		}
		else if(Integer.parseInt(img)==10)
		{
			id=R.drawable.nebbiag;
		}
		else if(Integer.parseInt(img)==11)
		{
			id=R.drawable.nevischiog;
		}
		else if(Integer.parseInt(img)==12)
		{
			id=R.drawable.ventog;
		}else if(Integer.parseInt(img)==13)
		{
			id=R.drawable.tweetg;
		}else if(Integer.parseInt(img)==999)
		{
			id=R.drawable.tweetg;
		}
		return id;
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
	
	public static void DownloadFromUrl(String imageURL, String fileName) {  //this is the downloader method
                URL url = null;
				try {
					url = new URL(imageURL);
				} catch (MalformedURLException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				} //you can write here any link

                URLConnection ucon;
				BufferedInputStream bis = null;
				try {
					ucon = url.openConnection();

                InputStream is = ucon.getInputStream();
                bis = new BufferedInputStream(is);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

                /*
                 * Read bytes to the Buffer until there is nothing more to read(-1).
                 */
                ByteArrayBuffer baf = new ByteArrayBuffer(50);
                int current = 0;
                try {
					while ((current = bis.read()) != -1) {
					        baf.append((byte) current);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                try {
                    File root = Environment.getExternalStorageDirectory();
                    if (root.canWrite()){
                        File file = null;
                        File dir=null;

                    	dir = new File(root+"/Android/data/com.mono.metwit/cache/");
                    	file = new File(root+"/Android/data/com.mono.metwit/cache/"+fileName);
            	    	boolean esiste = dir.exists();
            	    	if(!esiste)	dir.mkdirs();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(baf.toByteArray());
                        fos.close();
                    }

        } catch (IOException e) {
        }

        
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
