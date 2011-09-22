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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import static com.mono.metwit.API.*;

public class MyParser {
    
public boolean parseXml(String xmlUrl, Context context) throws InterruptedException{

    
	SQLiteDatabase db = (new MetwitDatabase(context, "Metwit.db", MetwitDatabase.DATABASE_VERSION)).getWritableDatabase();
	//db.delete("Segnalazioni", null, null);

	SharedPreferences Posizione = context.getSharedPreferences("Posizione", 0);
	SharedPreferences login = context.getSharedPreferences("Login", 0);
	Document doc=null;
    SharedPreferences.Editor pedit = Posizione.edit();
            try {

	                HttpPost httpPost2 = new HttpPost(API_LAST);
	    			HttpResponse response2 = Main.simpleHttp(httpPost2, context);        					
	
	    			if(response2!=null)
	    			{
	    	            //Costruiamo il nostro documento a partire dallo stream dati fornito dall'URL
	    	            Element root2=Main.simpleParser(response2);
	    	            //Elemento(nodo) radice del documento
	    	   
	    	            NodeList segnalazioni=root2.getElementsByTagName("segnalazioni");
	    	            pedit.putInt("SegnalazioniTotali", Integer.parseInt(segnalazioni.item(0).getFirstChild().getNodeValue()));
	    	
	    			   	SharedPreferences segn = context.getSharedPreferences("Segnalazione", 0);
	    			   	Editor segnedit = segn.edit();
	    			   	segnedit.clear();
	    			   	segnedit.commit();
	    			}
	    		   	
	                SharedPreferences settings = context.getSharedPreferences("Settings", 0);
	                boolean foto = settings.getBoolean("Foto", true);
	                if(foto)
	                {
	    				HttpPost httpPost = new HttpPost(API_LAST);
	    	
	    	            HttpResponse respimg = Main.simpleHttp(httpPost, context);
	    	            CreateLast(context, respimg, "ultimefoto.xml");
	                }
	                
	                Calendar now = Calendar.getInstance();
	
	                Editor loginedit = login.edit();
	                loginedit.putLong("DataAggiornamento", now.getTimeInMillis());
	                loginedit.commit();
	                
	                
            		
	                HttpPost httpPost = new HttpPost(xmlUrl);
					HttpResponse response = Main.simpleHttp(httpPost, context);
					if(response==null)
					{
						return false;
					}
					//CreateLast(context, response, "last.xml");
					
	                
                    doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.getEntity().getContent());


                    //Costruiamo il nostro documento a partire dallo stream dati fornito dall'URL
                    Element root=doc.getDocumentElement();
                    //Elemento(nodo) radice del documento
           
                    NodeList notes=root.getElementsByTagName("latLng"); //potremmo direttamente prendere gli elementi note
                    NodeList imgs = root.getElementsByTagName("idIcona");
                    NodeList ids = root.getElementsByTagName("id");
                    NodeList users = root.getElementsByTagName("username");
                    NodeList datas = root.getElementsByTagName("dataOra");
    	        	NodeList commenti = root.getElementsByTagName("commento");
    	        	NodeList froms = root.getElementsByTagName("idFrom");
    	        	NodeList idUsers = root.getElementsByTagName("idUsername");
    	        	if(ids.getLength()==0)	
    	        		{
    	        			if (db != null)
    	        				db.close();
    	        			return false;
    	        		}
    	        	
    	            //NodeList notes=((Node) root).getChildNodes(); 
                    //ma prediamo tutti i "figli" diretti di root. Utile se non avessimo solo "note" come figli di root
    	            float distanzaMin=2000000000;//2000000km

                    for(int i=0;i<notes.getLength();i++){//per ogni
                            Node c= notes.item(i);//nodo
                            Node img = imgs.item(i);
                            Node id = ids.item(i);
                            Node user = users.item(i);
                            Node commento = commenti.item(i);
                            Node data = datas.item(i);
                            Node from = froms.item(i);
                            Node idUser = idUsers.item(i);

                    		String n = c.getFirstChild().getNodeValue();
                    		String[] coo = n.split(",",0);

                    		Location blocation = new Location("asd");
                    		blocation.setLatitude(Double.parseDouble(coo[0]));
                    		blocation.setLongitude(Double.parseDouble(coo[1]));
                    		Location alocation = new Location("aasd");
                    		alocation.setLatitude(Double.parseDouble(Posizione.getString("lat","")));
                    		alocation.setLongitude(Double.parseDouble(Posizione.getString("lng","")));               		
                    		
                    		float distanza1 = alocation.distanceTo(blocation);
            	            if(distanza1<=distanzaMin && !user.getFirstChild().getNodeValue().toString().equalsIgnoreCase(login.getString("User", "")))
            	            {
            	            	distanzaMin=distanza1;
                	            pedit.putString("SegnalazioneVicina", id.getFirstChild().getNodeValue()+"");
                                pedit.putString("SegnalazioneVicinaLat",coo[0]);
                                pedit.putString("SegnalazioneVicinaLng",coo[1]);
                                pedit.putString("SegnalazioneVicinaIcona", img.getFirstChild().getNodeValue());
                                pedit.putString("SegnalazioneVicinaUtente",user.getFirstChild().getNodeValue());
                                pedit.putString("SegnalazioneVicinaData",data.getFirstChild().getNodeValue());
            	            }
            	            
            	            String comm;
            	            String immagine = null;
            	           if(commento.hasChildNodes())
            	           {
            	        	   comm = commento.getFirstChild().getNodeValue().replace('"', '\"');
            	           }else
            	           {
            	        	   comm ="";
            	           }
            	           if(img.getFirstChild().getNodeValue()=="999")
            	           {
            	        	   immagine = "13";
            	           }else
            	           {
            	        	   immagine=img.getFirstChild().getNodeValue();
            	           }
            	             /* Insert data to a Table*/
            	             db.execSQL("INSERT INTO "
            	               + "Segnalazioni"
            	               + " (Id, Immagine, Utente, idUtente, Lat, Lng, Data, Commento, Agent)"
            	               + " VALUES (\""+id.getFirstChild().getNodeValue()+"\", \""+immagine+"\", \""+user.getFirstChild().getNodeValue()+"\", \""+idUser.getFirstChild().getNodeValue()+"\", \""+coo[0]+"\", \""+coo[1]+"\", \""+data.getFirstChild().getNodeValue()+"\", \""+comm+"\", \""+from.getFirstChild().getNodeValue()+"\");");
                    }
     	         pedit.commit();
   	             if (db != null)
   	              db.close();
            //gestione eccezioni
            } catch (SAXException e) {
            } catch (IOException e) {
            } catch (ParserConfigurationException e) {
            } catch (FactoryConfigurationError e) {
            }
            
			return true; 
            
    }

public static void CreateLast(Context context, HttpResponse httpResponse, String nomefile)
{
    FileOutputStream fOut = null; 
    OutputStreamWriter osw = null; 
    try{ 
        fOut = context.openFileOutput(nomefile, 1);  
           osw = new OutputStreamWriter(fOut);
           BufferedReader r = new BufferedReader(
                   new InputStreamReader(httpResponse.getEntity().getContent(), "utf-8"));
           String buffer;
           StringBuffer result = new StringBuffer();
           while ((buffer = r.readLine()) != null) {
        	   result = result.append(buffer);
           }
           osw.write(result.toString());
           osw.flush();
           } 
           catch (Exception e) {       
           e.printStackTrace(); 
           } 
           finally { 
              try { 
                     osw.close(); 
                     fOut.close(); 
                     } catch (IOException e) { 
                     e.printStackTrace(); 
                     } 
           } 
}

}


