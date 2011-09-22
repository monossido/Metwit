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

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.maps.GeoPoint;

import java.util.List;
import java.util.LinkedList;

public class GeoHelper {

    public static List<GeoPoint> findMarker(GeoPoint topleft, GeoPoint bottomright, int zoomlevel, String tipo, Context context, GeoPoint[][] Lista) {
    	
        SharedPreferences posizione = context.getSharedPreferences("Posizione", Context.MODE_PRIVATE);
        int totali = posizione.getInt("SegnalazioniTotali", 0);
        GeoPoint[] geopoint= new GeoPoint[totali];
        int z;
    	for(z=0;Lista[Integer.parseInt(tipo)][z]!=null;z++)
    	{
			geopoint[z]=Lista[Integer.parseInt(tipo)][z];
    	}
        List<GeoPoint> marker = new LinkedList<GeoPoint>();
            if (topleft.getLongitudeE6() > bottomright.getLongitudeE6()) {
                for (int i = 0; i < z; i++) {
                    GeoPoint p = geopoint[i];
                    if ((p.getLongitudeE6() > topleft.getLongitudeE6() || p.getLongitudeE6() < bottomright.getLongitudeE6())
                            && p.getLatitudeE6() < topleft.getLatitudeE6() && p.getLatitudeE6() > bottomright.getLatitudeE6()) {
                        marker.add(p);
                    }
                }
            } else {
                for (int i = 0; i < z; i++) {
                    GeoPoint p = geopoint[i];
                    if (p.getLongitudeE6() > topleft.getLongitudeE6() && p.getLatitudeE6() < topleft.getLatitudeE6()
                            && p.getLongitudeE6() < bottomright.getLongitudeE6() && p.getLatitudeE6() > bottomright.getLatitudeE6()) {
                        marker.add(p);
                    }
                }
            }
        return marker;
    }

    
}