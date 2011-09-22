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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import biz.source_code.base64Coder.Base64Coder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import static com.mono.metwit.API.*;

public class Meteo extends Activity {

	LocationManager manager;
    MyLocationListener2 listener;
	Context context;
	SharedPreferences.Editor sedit;
	SharedPreferences Login;
	String lat;
	String lng;
    int fonte;
    int condizione;
    String commento;
	List<Address> addresses;
	Geocoder gc;
	String value;
    static final int IMAGE_PICK = 0;
    static final int TAKE_IMAGE = 1;
    String filePath;
    private Uri imageUri = null;
    private Uri selectedImage = null;
    Bitmap bm;
    FileInputStream fis;
    private Invia mTask; 
    private boolean mShownDialog; 
	protected static final int LOADING_DIALOG = 0;	
       
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Login = getSharedPreferences("Login", 0);
    	final SharedPreferences SegnalazioneT = getSharedPreferences("SegnalazioneTemp", 0);
    	sedit = SegnalazioneT.edit();
    	manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener2();
        context = this;
        condizione=-1;
        fonte=-1;
        lat="";
        lng="";
        addresses  = null;
        gc = new Geocoder(getApplicationContext(), Locale.getDefault()); 
        
        setContentView(R.layout.meteo);      
        
        if(getIntent().getAction()!=null)//Prende l'immagine quando la si vuole condividere dalla galleria
        {
        	if(getIntent().getAction().equals( Intent.ACTION_SEND ))
        	{
                if(Login.getString("User", "")=="")
                {
                	startActivity(new Intent(context, Home.class));
                }
        		selectedImage = (Uri)getIntent().getExtras().getParcelable(Intent.EXTRA_STREAM);
        		String[] filePathColumn = {MediaStore.Images.Media.DATA};
        		
        		Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        		cursor.moveToFirst();
        		
        		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        		filePath = cursor.getString(columnIndex);
        	
        		ImageView imageViewer = (ImageView)findViewById(R.id.Image);
        		imageViewer.setVisibility(0);
        		imageViewer.setImageURI(selectedImage);
        	}
        }else
        {

        }

            final EditText commentoEditText = (EditText) findViewById(R.id.EditText01);
            commentoEditText.setSelected(false);
	        
	        Object retained = getLastNonConfigurationInstance(); 
	        if ( retained instanceof Invia ) { 
	                mTask = (Invia) retained; 
	                mTask.setActivity(this); 
	        } else { 
	                
	        }
	        
	        
	        final CharSequence[] items = {"Sereno", "Pioggia", "Temporale", "Neve", "Variabile", "Nuvoloso", "Grandine", "Mare mosso", "Mare calmo", "Nebbia", "Nevischio", "Vento"};

	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setTitle("Seleziona meteo");
	        builder.setItems(items, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int item) {
	            	
	            	ImageView img = (ImageView) findViewById(R.id.ImageView01);
	            	img.setImageDrawable(getResources().getDrawable(ConvertiNomi(item)));
                    sedit.putInt("Meteo", item+1);
                    condizione=item+1;
	            }
	        });
	        final AlertDialog alert = builder.create();
	        
	    	View aboutButton = findViewById(R.id.Button01);
	    	aboutButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View arg0) {

	            	alert.show();
	            } 
	         });
	    	
	    	  RadioButton radio1 = (RadioButton) findViewById(R.id.RadioButton01);
	    	  radio1.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View arg0) {
	            	sedit.putInt("Fonte",1);
	            	fonte=1;
	            }
	            
	    	  });
	    	  RadioButton radio2 = (RadioButton) findViewById(R.id.RadioButton02);
	    	  radio2.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View arg0) {
	            	sedit.putInt("Fonte",2);
	            	fonte=2;
	            }
	            
	    	  });
	    	  RadioButton radio3 = (RadioButton) findViewById(R.id.RadioButton03);
	    	  radio3.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View arg0) {
	            	sedit.putInt("Fonte",3);
	            	fonte=3;
	            }
	            
	    	  });
	    	
	    	final View Pos = findViewById(R.id.Button02);
	    	Pos.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View arg0) {
		        Criteria c = new Criteria();
		        c.setAccuracy(Criteria.ACCURACY_FINE);
	       		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	       		if(!settings.getBoolean("GPS", false))
	       		{
			        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000L, 10.0f, listener);
		        	findViewById(R.id.rilevamento).setVisibility(0);		        	
		        	Pos.setVisibility(8);
		        	findViewById(R.id.Button04).setEnabled(false);
			        int duration = Toast.LENGTH_SHORT;
			        Toast toast = Toast.makeText(getApplicationContext(), "Verrà rilevata la posizione usando la rete cellulare...", duration);
			        toast.show();
	       		}else
	       		{		       		
			        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000L, 10.0f, listener);
		        	findViewById(R.id.rilevamento).setVisibility(0);		        	
		        	Pos.setVisibility(8);
		        	findViewById(R.id.Button04).setEnabled(false);
			        int duration = Toast.LENGTH_SHORT;
			        Toast toast = Toast.makeText(getApplicationContext(), "Verrà rilevata la posizione usando il GPS...", duration);
			        toast.show();
	       		}
	            }
	        });
	    	

	    		
	    	View insertPos = findViewById(R.id.Button03);
        	final AlertDialog.Builder pos = new AlertDialog.Builder(this);  
      	  
        	pos.setTitle("Metwit");  
        	pos.setMessage("Seleziona la città");  
	    	
	    	insertPos.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View arg0) {
	            	
	            	  
	            	// Set an EditText view to get user input   
	            	final EditText input = new EditText(getApplicationContext());  
	            	pos.setView(input);  
	            	  
	            	pos.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
	            	public void onClick(DialogInterface dialog, int whichButton) {
	            	
            		value = input.getText().toString();
            		
            		new GetLoc().execute(value);
	              	  
	            }
	    	});
	              	pos.show();
	    	
	            }
	    	});
	    	
	    	
	        final CharSequence[] foto = {"Scatta una foto","Seleziona una foto dalla galleria"};

	        AlertDialog.Builder builderFoto = new AlertDialog.Builder(this);
	        builderFoto.setTitle("Scegli");
	        builderFoto.setItems(foto, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int item) {
	            	if(item==0)
	            	{

		                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		                GregorianCalendar gc = new GregorianCalendar();
		                
		                File dir = new File("/sdcard/metwit");
		                boolean esiste = dir.exists();
            	    	if(!esiste)	dir.mkdirs();
		                
		                File photo = new File(Environment.getExternalStorageDirectory()+"/metwit/",  gc.get(Calendar.YEAR)+""+(gc.get(Calendar.MONTH) + 1)+""+gc.get(Calendar.DATE)+""+gc.get(Calendar.HOUR)+""+gc.get(Calendar.MINUTE)+""+gc.get(Calendar.SECOND)+"metwit.jpg");
		                intent.putExtra(MediaStore.EXTRA_OUTPUT,
		                        Uri.fromFile(photo));
		                imageUri = Uri.fromFile(photo);
		                selectedImage = null;
						startActivityForResult(intent, TAKE_IMAGE);
	            	}else{
		                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("image/*");
						imageUri = null;
						startActivityForResult(intent, IMAGE_PICK);
	            	}

	            }
	        });
	        final AlertDialog alertFoto = builderFoto.create();
	    	
	    	
	    	Button image = (Button)findViewById(R.id.PickImage);
	    	image.setOnClickListener( new OnClickListener()
	        {
					@Override
	                public void onClick(View viewParam)			
	                {
						alertFoto.show();
	                }
	        });
	            	
	        Button send = (Button)findViewById(R.id.Button04);
	        send.setOnClickListener( new OnClickListener()
	        {
					@Override
	                public void onClick(View viewParam)
	                {
	                    commento = commentoEditText.getText().toString();
	                    

	                    if(lat.length()==0 || lng.length()==0 || condizione==-1 || fonte==-1 )
	                    {
	                    	AlertDialog.Builder alert = new AlertDialog.Builder(context);  
	                  	  
	                    	alert.setTitle("Errore");  
	                    	alert.setMessage("Uno dei dati richiesti è mancante"); 
	                    	alert.show();
	                    }
	                    else
	                    {

	    	                mTask = new Invia(Meteo.this); 
	    	                mTask.execute(); 
	    	                }
	                    	                    
	                }
	        });

    }
    
	protected Dialog onCreateDialog(int id) {
		if(id == LOADING_DIALOG){
	        ProgressDialog Dialog = new ProgressDialog(Meteo.this);  
        	Dialog.setTitle("Attendi");
            Dialog.setMessage("Sto inviando la segnalazione...");  
			return Dialog;
		} 
		return super.onCreateDialog(id);
	}
    
    @Override 
    public Object onRetainNonConfigurationInstance() { 
            //mTask.setActivity(null); 
            return mTask; 
    } 	
    
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
        case IMAGE_PICK:
            if(resultCode == RESULT_OK){ 
                selectedImage = imageReturnedIntent.getData();
 
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                
                ImageView imageViewer = (ImageView)findViewById(R.id.Image);
                imageViewer.setVisibility(0);
                imageViewer.setImageURI(selectedImage);
            }
            break;
        case TAKE_IMAGE:
        	if(resultCode == RESULT_OK){ 
                
                filePath = imageUri.getPath();
                bm = null;
                try {
                        fis = new FileInputStream(filePath);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        bm = BitmapFactory.decodeStream(bis);
                } catch (FileNotFoundException e) {

                }
                ImageView imageViewer = (ImageView)findViewById(R.id.Image);
                imageViewer.setVisibility(0);
                imageViewer.setImageBitmap(bm) ;
            }else{
            	imageUri = null;
            }
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
      savedInstanceState.putInt("condizione", condizione);
      savedInstanceState.putInt("fonte", fonte);
      savedInstanceState.putString("lat", lat);
      savedInstanceState.putString("lng", lng);
      savedInstanceState.putString("commento", commento);

      if(selectedImage!=null)      savedInstanceState.putString("selectedImage",selectedImage.toString());
      if(imageUri!=null)      savedInstanceState.putString("imageUri",imageUri.toString());
      savedInstanceState.putString("filePath", filePath);
      // etc.
      super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	    ImageView imageViewer = (ImageView)findViewById(R.id.Image);
		condizione = savedInstanceState.getInt("condizione");
		fonte = savedInstanceState.getInt("fonte");
		lat = savedInstanceState.getString("lat");
		lng = savedInstanceState.getString("lng");
		commento = savedInstanceState.getString("commento");
		String selectedImageString = savedInstanceState.getString("selectedImage");
		if(selectedImageString!=null) selectedImage = Uri.parse(selectedImageString);
		String imageUriString = savedInstanceState.getString("imageUri");
		if(imageUriString!=null) imageUri = Uri.parse(imageUriString);
		filePath = savedInstanceState.getString("filePath");

		ImageView img = (ImageView) findViewById(R.id.ImageView01);
		if(condizione!=-1)
		{
			img.setImageDrawable(getResources().getDrawable(ConvertiNomi(condizione-1)));
		}

		
		if(selectedImageString!=null)
		{
			imageViewer.setImageURI(selectedImage);
		}else if(imageUriString!=null && filePath!=null)
		{
            Bitmap bm = null;
            try {
                    FileInputStream fis = new FileInputStream(filePath);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bm = BitmapFactory.decodeStream(bis);
            } catch (FileNotFoundException e) {

            }
            imageViewer.setVisibility(0);
            imageViewer.setImageBitmap(bm) ;
		}

    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
        ImageView imageViewer = (ImageView)findViewById(R.id.Image);
    	if(bm!=null)
    	{
    		bm.recycle();
    	}
    	bm = null;
    	fis = null;
    	filePath = null;
    	imageUri = null;
    	selectedImage = null;
    	Drawable toRecycle= imageViewer.getDrawable();
    	if(toRecycle != null)
    	{
    		((BitmapDrawable)imageViewer.getDrawable()).getBitmap().recycle();
    	}
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) { 
            super.onPrepareDialog(id, dialog); 
            if ( id == LOADING_DIALOG ) { 
                    mShownDialog = true; 
            } 
    }
    
    
    private class Invia extends AsyncTask<Object, Object, HttpResponse> {  
        //private ProgressDialog Dialog = new ProgressDialog(Meteo.this);  
          
		 private Meteo activitys; 
         private boolean completed; 
                 
         private Invia(Meteo activity) { 
                 this.activitys = activity; 
         } 
        
        
        @Override 
        protected void onPreExecute() { 
			activitys.showDialog(LOADING_DIALOG);

        }  
  
        @Override 
		protected HttpResponse doInBackground(Object... params) {

            String commentoU =null ;
            if(commento.length()!=0)
            {
	            URI uri=null;
				try {
					uri = new URI("http", commento, null);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	            commentoU = uri.toASCIIString().replace("http:", "").replace("?", "%3F");
            }
            String urlSegnalazione = null;
            if(commentoU!=null)
            {
            	urlSegnalazione = API_SEND_SEGNALAZIONE + condizione+"/"+fonte+"/"+lat+","+lng+"/"+commentoU+"/XML";
            }else
            {
            	urlSegnalazione = API_SEND_SEGNALAZIONE + condizione+"/"+fonte+"/"+lat+","+lng+"/XML";
            }
            HttpPost httpPost2 = new HttpPost(urlSegnalazione);
            String inputString = Login.getString("User", "").replace(" ","")+":"+Login.getString("Password", "");
            String base64login;

            base64login = new String(Base64Coder.encodeString(inputString));

            httpPost2.setHeader("Authorization","Basic "+base64login);

            HttpResponse response2=null;
            
            if(filePath!=null)
            {
	            HttpClient client = getClient();
	            try {
	          	  MultipartEntity entity = new MultipartEntity();
	          	  
	          	  File image = new File(filePath);
	          	  entity.addPart("qqfile", new FileBody(image, "image/jpg"));
	          	  entity.addPart("api_key", new StringBody("39cf633a159f62883e2fecbac62f0d5f"));
	          	httpPost2.setEntity(entity);
	          	} catch (IOException e) {
	          	}
	            try {	
	                // Execute HTTP Post Request
	                response2 = client.execute(httpPost2);
	
	            } catch (ClientProtocolException e) {
	            } catch (IOException e) {

	            }
	            
            }else{
            	
	            response2 = Main.simpleHttp2(httpPost2, context);//Controllare responso? TODO
            }
            return response2;
		}
		
        @Override 
		protected void onPostExecute(HttpResponse response2)
		{     
            completed = true;
            if ( null != activitys ) { 
            if ( mShownDialog ) { 
            	activitys.dismissDialog(LOADING_DIALOG);
                mShownDialog = false;
            }
            }
            if(response2==null)//no connessione
            {
		    	AlertDialog.Builder errordialog = new AlertDialog.Builder(context);
		    	errordialog.setTitle("Errore");
		    	errordialog.setMessage("Errore con la connessione, riprova più tardi\n");
				errordialog.show();
            }else{//errore nella segnalazione?
            	Element root2 = Main.simpleParser(response2);
            	NodeList errors = root2.getElementsByTagName("error");
            	if(errors.getLength()==0)
            	{
              	  	AlertDialog.Builder errordialog = new AlertDialog.Builder(context);
              	  	errordialog.setTitle("Errore");
              	  	errordialog.setMessage("Si è verificato un errore, forse è stato usato qualche carattere speciale non permesso.");
              	  	errordialog.show();
              	  	return;
            	}
                String error = errors.item(0).getFirstChild().getNodeValue();
				if(Integer.parseInt(error)==1)//errore
				{
					NodeList messaggi=root2.getElementsByTagName("message");
              	  	AlertDialog.Builder errordialog = new AlertDialog.Builder(context);
              	  	errordialog.setTitle("Errore");
              	  	errordialog.setMessage(messaggi.item(0).getFirstChild().getNodeValue());
              	  	errordialog.show();
				}
				else
				{
              	  	AlertDialog.Builder okdialog = new AlertDialog.Builder(context);
	              	okdialog.setTitle("OK");
	              	okdialog.setMessage("Segnalazione inviata!");
	              	okdialog.setOnCancelListener(new OnCancelListener() {
	                    public void onCancel(DialogInterface dialog) {
	            	      sedit.commit();
	            	      onHomeClick(null);
	                    }});
	              	okdialog.show();
				}
            }
            
		}
        
        private void setActivity(Meteo activity) {
        	
            this.activitys = activity;
            if(completed)
            {
            if ( null != activity ) { 
                if ( mShownDialog ) { 
                	activity.dismissDialog(LOADING_DIALOG);
                    mShownDialog = false;
                }
                }
            }
    }
       
          
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
    
    public void reload() {
        onStop();
        onCreate(getIntent().getExtras());
    }

	public static int ConvertiNomi(int img)
	{
		img++;
		int id = 0;
		if(img==1)
		{
			id=R.drawable.soleg;
		}
		else if(img==2)
		{
			id=R.drawable.pioggiag;
		}
		else if(img==3)
		{
			id=R.drawable.temporaleg;
		}
		else if(img==4)
		{
			id=R.drawable.neveg;
		}
		else if(img==5)
		{
			id=R.drawable.variabileg;
		}
		else if(img==6)
		{
			id=R.drawable.nuvolosog;
		}
		else if(img==7)
		{
			id=R.drawable.grandineg;
		}
		else if(img==8)
		{
			id=R.drawable.maremossog;
		}
		else if(img==9)
		{
			id=R.drawable.marecalmog;
		}
		else if(img==10)
		{
			id=R.drawable.nebbiag;
		}
		else if(img==11)
		{
			id=R.drawable.nevischiog;
		}
		else if(img==12)
		{
			id=R.drawable.ventog;
		}
		return id;
	}
	
    private class MyLocationListener2 implements LocationListener {

        public void onLocationChanged(Location location) {
                try {
                        if (location != null) {
		                    sedit.putString("lat", Double.toString(location.getLatitude()));
		                    sedit.putString("lng", Double.toString(location.getLongitude()));
		                    lat=Double.toString(location.getLatitude());
		                    lng=Double.toString(location.getLongitude());
				        	findViewById(R.id.rilevamento).setVisibility(8);		        	
				        	findViewById(R.id.Button02).setVisibility(0);
				        	findViewById(R.id.Button04).setEnabled(true);
		    		        int duration = Toast.LENGTH_SHORT;
		    		        Toast toast = Toast.makeText(getApplicationContext(), "Fatto!", duration);
		    		        toast.show();
		    		        manager.removeUpdates(listener);
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
        	findViewById(R.id.rilevamento).setVisibility(8);		        	
        	findViewById(R.id.Button02).setVisibility(0);
        	findViewById(R.id.Button04).setEnabled(true);
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
    
    @Override
    public void onPause()
    {
    	super.onPause();
        manager.removeUpdates(listener);
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
	
    private class GetLoc extends AsyncTask<String,  List<Address>,  List<Address>> {  
        
        @Override 
        protected void onPreExecute() {
        	findViewById(R.id.conversione).setVisibility(0);		        	
        	findViewById(R.id.Button03).setVisibility(8);
        	findViewById(R.id.Button04).setEnabled(false);
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
                sedit.putString("lat", Double.toString(addresses.get(0).getLatitude()));
                sedit.putString("lng", Double.toString(addresses.get(0).getLongitude()));
                lat=Double.toString(addresses.get(0).getLatitude());
            	lng=Double.toString(addresses.get(0).getLongitude());
	        	findViewById(R.id.conversione).setVisibility(8);		        	
	        	findViewById(R.id.Button03).setVisibility(0);
	        	findViewById(R.id.Button04).setEnabled(true);
		        int duration = Toast.LENGTH_SHORT;
		        Toast toast = Toast.makeText(getApplicationContext(), "Coordinate trovate per la località "+value, duration);
		        toast.show();
			}else
			{
            	AlertDialog.Builder alert = new AlertDialog.Builder(context);  
              	  
            	alert.setTitle("Errore");  
            	alert.setMessage("La località non è stata trovata"); 
            	alert.show();
	        	findViewById(R.id.conversione).setVisibility(8);		        	
	        	findViewById(R.id.Button03).setVisibility(0);
	        	findViewById(R.id.Button04).setEnabled(true);
			}

		}
          
    }  
	
	public void onHomeClick(View v)
	{
		finish();
		Intent send = new Intent(getApplicationContext(), Home.class);
		send.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		startActivity(send);
	}
	
	public void onMappaClick(View v)
	{
		Intent send = new Intent(getApplicationContext(), Map.class);
		startActivity(send);
	}
}

