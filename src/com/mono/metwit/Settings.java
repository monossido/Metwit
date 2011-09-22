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
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import biz.source_code.base64Coder.Base64Coder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import static com.mono.metwit.API.*;


public class Settings extends PreferenceActivity {
	
	Uri imageUri;
	Uri selectedImage;
	String filePath;
	Bitmap bm;
    FileInputStream fis;
    static final int IMAGE_PICK = 0;
    static final int TAKE_IMAGE = 1;
	protected static final int LOADING_DIALOG = 0;
    private Invia mTask; 
    private boolean mShownDialog; 
    int idUtente;
    Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	   super.onCreate(savedInstanceState);
	   context = this;
	   addPreferencesFromResource(R.xml.preference);
	   
       Object retained = getLastNonConfigurationInstance(); 
       if ( retained instanceof Invia ) { 
               mTask = (Invia) retained; 
               mTask.setActivity(this); 
       } else { 
               
       }
   	
       CheckBoxPreference checkPref = (CheckBoxPreference) findPreference("GPS");
	   
	   checkPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
           public boolean onPreferenceChange(Preference preference, Object newValue) {

               return true;
           }
       });
	   
       CheckBoxPreference checkPrefa = (CheckBoxPreference) findPreference("Aggiorna");
	   
	   checkPrefa.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
           public boolean onPreferenceChange(Preference preference, Object newValue) {

               return true;
           }
       });
	   
       CheckBoxPreference checkPreff = (CheckBoxPreference) findPreference("Foto");
	   
	   checkPreff.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
           public boolean onPreferenceChange(Preference preference, Object newValue) {

               return true;
           }
       });
	   
       CheckBoxPreference checkPrefam = (CheckBoxPreference) findPreference("AvatarM");
	   
	   checkPrefam.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
           public boolean onPreferenceChange(Preference preference, Object newValue) {
 
               return true;
           }
       });
       
       final CharSequence[] foto = {"Scatta una foto","Seleziona una foto dalla galleria"};

       AlertDialog.Builder builderFoto = new AlertDialog.Builder(context);
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
	   
	   Preference img = findPreference("avatar");
	   img.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference preference) {
     	   Dialog imgdialog = new Dialog(context);
    	   imgdialog.setTitle("Avatar");
    	   imgdialog.setContentView(R.layout.avatar);
    	   imgdialog.show();
    	      
   	   	SharedPreferences Login = context.getSharedPreferences("Login", 0);

	   	idUtente = Login.getInt("Id", 0);
			  HttpPost httpPost = new HttpPost(API_INFOU+idUtente+"/xml");
			  HttpResponse imgReponse = Main.simpleHttp(httpPost, context);
			  Element root = Main.simpleParser(imgReponse);
			  NodeList avatars = root.getElementsByTagName("avatar");
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
						  Map.DownloadFromUrl(avatar, nomeimg);
					  }
				  }
				  Bitmap bm = null;
	                try {
	                        FileInputStream fis = new FileInputStream("/sdcard/Android/data/com.mono.metwit/cache/"+nomeimg);
	                        BufferedInputStream bis = new BufferedInputStream(fis);
	                        bm = BitmapFactory.decodeStream(bis);
	                } catch (FileNotFoundException e) {
	                }
	                //savatarImg.setVisibility(0);
	  			    ImageView avatarImg = (ImageView) imgdialog.findViewById(R.id.AvatarImgs);
	  			    avatarImg.setImageBitmap(bm);
			  }
			  
			  Button change = (Button) imgdialog.findViewById(R.id.ChangeAvatar);
			  change.setOnClickListener(new OnClickListener()
			  {
				@Override
				public void onClick(View v) {
					alertFoto.show();
				}
			  });
		return false;  
		}
	   });
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

          
        switch(requestCode) {
        case IMAGE_PICK:
            if(resultCode == RESULT_OK){ 
                selectedImage = imageReturnedIntent.getData();
 
                String[] filePathColumn = {MediaColumns.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                
                mTask = new Invia(Settings.this); 
                mTask.execute();

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
                mTask = new Invia(Settings.this); 
                mTask.execute(); 
            }else{
            	imageUri = null;
            }
        }
    }
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == LOADING_DIALOG){
	        ProgressDialog Dialog = new ProgressDialog(Settings.this);  
        	Dialog.setTitle("Attendi");
            Dialog.setMessage("Sto inviando l'immagine...");  
			return Dialog;
		} 
		return super.onCreateDialog(id);
	}
	
    @Override 
    public Object onRetainNonConfigurationInstance() { 
            //mTask.setActivity(null); 
            return mTask; 
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
          
		 private Settings activitys; 
         private boolean completed; 
                 
         private Invia(Settings activity) { 
                 this.activitys = activity; 
         } 
        
        
        @Override 
        protected void onPreExecute() {
			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        	//Dialog.setTitle("Attendi");
            //Dialog.setMessage("Sto inviando la segnalazione...");  
            //Dialog.show();  
			activitys.showDialog(LOADING_DIALOG);

        }  
  
        @Override 
		protected HttpResponse doInBackground(Object... params) {

            String urlSegnalazione = API_AVATAR+idUtente+"/xml";

            HttpPost httpPost = new HttpPost(urlSegnalazione);
       	   	SharedPreferences Login = context.getSharedPreferences("Login", 0);
            String inputString = Login.getString("User", "").replace(" ","")+":"+Login.getString("Password", "");
            String base64login;

            base64login = new String(Base64Coder.encodeString(inputString));

            httpPost.setHeader("Authorization","Basic "+base64login);

            HttpResponse response2=null;
            
            if(filePath!=null)
            {
	            HttpClient client = Meteo.getClient();
	            try {
	          	  MultipartEntity entity = new MultipartEntity();
	          	  
	          	  File image = new File(filePath);
	          	  entity.addPart("qqfile", new FileBody(image, "image/jpg"));
	          	  entity.addPart("api_key", new StringBody("39cf633a159f62883e2fecbac62f0d5f"));
	          	httpPost.setEntity(entity);
	          	} catch (IOException e) {
	          	}
	            try {

	                response2 = client.execute(httpPost);
	
	            } catch (ClientProtocolException e) {
	            } catch (IOException e) {

	            }
	            
            }else{
            	
	            response2 = Main.simpleHttp2(httpPost, context);//Controllare responso? TODO
            }
            return response2;
		}
		
        @Override 
		protected void onPostExecute(HttpResponse response2)
		{     
        	//MyParser.CreateLast(context, response2, "prova");
            completed = true;
            if ( null != activitys ) { 
            if ( mShownDialog ) { 
            	activitys.dismissDialog(LOADING_DIALOG);
                mShownDialog = false;
            }
            }
        	//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
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
	              	okdialog.show();
				}
            }
            
		}
        
 private void setActivity(Settings activity) {
        	
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
	
}
