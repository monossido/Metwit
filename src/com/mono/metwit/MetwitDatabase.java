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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MetwitDatabase extends SQLiteOpenHelper {

    static final int DATABASE_VERSION = 1;
    private static final String DICTIONARY_TABLE_NAME = "Segnalazioni";
    private static final String DICTIONARY_TABLE_CREATE =
                "CREATE TABLE " + DICTIONARY_TABLE_NAME + " (Id INTEGER, Immagine INTEGER, Utente VARCHAR, idUtente INTEGER, Lat FLOAT, Lng FLOAT, Data VARCHAR, Commento VARCHAR, Agent INTEGER);";

    MetwitDatabase(Context context, String name, int ver) {
        super(context, name, null, ver);
    }
    
    @Override
    public void onOpen(SQLiteDatabase db)
    {
		db.execSQL("DROP TABLE IF EXISTS " + DICTIONARY_TABLE_NAME);
		onCreate(db);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
        db.execSQL(DICTIONARY_TABLE_CREATE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
}
