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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;

import static com.mono.metwit.API.*;

public class Splash extends Activity {

	LocationManager manager;
    MyLocationListener listener;
	ProgressDialog myProgressDialog;
	Thread splashTread;
	 ProgressBar myProgressBar;
	 boolean right;
	 Context context;
    private splashGet mTask; 
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.splash);

	    
        Object retained = getLastNonConfigurationInstance(); 
        if ( retained instanceof splashGet ) { 
                mTask = (splashGet) retained; 
                //mTask.setActivity(this);
                return;
        } else { 
                
        }
	    
	    context = this;
	    right=true;
	    
		if(!isOnline())
		{
	    	AlertDialog.Builder errordialog = new AlertDialog.Builder(context);
	    	errordialog.setTitle("Errore");
	    	errordialog.setMessage("Errore con la connessione, riprova più tardi\n");
			errordialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                	finish();
                    }});
			errordialog.show();
			return;
		}
	    
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean aggiorna = settings.getBoolean("Aggiorna", true);
        
        if(aggiorna)
        {
	    	manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    	Location last = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    	if(last!=null)
	    	{
	    		SharedPreferences Posizione = getSharedPreferences("Posizione", 0);
	            SharedPreferences.Editor pedit = Posizione.edit();
	            pedit.putString("lat", Double.toString(last.getLatitude()));
	            pedit.putString("lng", Double.toString(last.getLongitude()));
	            pedit.putString("alt", Double.toString(last.getAltitude()));
	            pedit.commit();
	            mTask = new splashGet(); 
	            mTask.execute(); 
	    	}else
	    	{ 		
		       listener = new MyLocationListener();
		       if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
		       {
		    	   manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000L, 10.0f, listener);
		       }else
		       {
			    	AlertDialog.Builder errordialog = new AlertDialog.Builder(context);
			    	errordialog.setTitle("Errore");
			    	errordialog.setMessage("La localizzazione via network non è attiva, attivala dalle impostazioni.");
					errordialog.setOnCancelListener(new OnCancelListener() {
		                public void onCancel(DialogInterface dialog) {
		                	finish();
		                    }});
					errordialog.show();
					return;
		       }         
			}
        }else
        {
			finish();
			Intent main = new Intent(context, Home.class);
			startActivity(main);
        }
	}
    
    @Override 
    public Object onRetainNonConfigurationInstance() { 
            //mTask.setActivity(null); 
            return mTask; 
    } 	
	
	private class MyLocationListener implements LocationListener {

	    public void onLocationChanged(Location location) {
	            try {
	                    if (location != null) {
							SharedPreferences Posizione = getSharedPreferences("Posizione", 0);
		    	            SharedPreferences.Editor pedit = Posizione.edit();
		                    pedit.putString("lat", Double.toString(location.getLatitude()));
		                    pedit.putString("lng", Double.toString(location.getLongitude()));
		                    pedit.putString("alt", Double.toString(location.getAltitude()));
		                    pedit.commit();  
		    		        manager.removeUpdates(listener);
		    		    	try {
		    					getData(Splash.this);
		    				} catch (InterruptedException e1) {
		    					// TODO Auto-generated catch block
		    					e1.printStackTrace();
		    				}		    	

	    					finish();
	    					Intent tabmeteo = new Intent(context, Home.class);
	    					startActivity(tabmeteo);
		    				
	                    }
	            } catch (Exception e) {
	                    e.printStackTrace();
	            }
	    }

	    @Override
	    public void onProviderDisabled(String provider) {
	    	// TODO Auto-generated method stub
	    	
	    }

	    @Override
	    public void onProviderEnabled(String provider) {
	    	// TODO Auto-generated method stub
	    	
	    }

	    @Override
	    public void onStatusChanged(String provider, int status, Bundle extras) {
	    	
	    }
	}
	
	public boolean isOnline() {
		 ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
	}
	
	public static boolean getData(Context context) throws InterruptedException
    {
            
            MyParser parser=new MyParser(); //otteniamo un istanza del nostro parser 
            boolean right = parser.parseXml(API_LAST, context);//usiamo il parser
            if(!right)
            {
            	return false;
            }else
            {
                return true;
            }
    }
	
	private class splashGet extends AsyncTask<boolean[], boolean[],  boolean[]> {  
        
        
        @Override 
        protected void onPreExecute() {
			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }  
  
        @Override 
		protected boolean[]  doInBackground(boolean[]... bool) {
        	bool = new boolean[1][1];
	    	try {
				bool[0][0]=getData(context);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return bool[0];
		}
		
        @Override 
		protected void onPostExecute(boolean[] bool)
		{  
        	//completed = true;

			finish();
			Intent main = new Intent(context, Home.class);
			startActivity(main);
		}

          
    }  

}