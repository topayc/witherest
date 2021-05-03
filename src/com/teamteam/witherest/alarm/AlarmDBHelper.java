package com.teamteam.witherest.alarm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDBHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "roomalarm.db";
	public static final String TABLE_NAME = "alarm_table";
	
	public static final String ID = "_id";
	public static final String USER_ID ="user_id";
	public static final String ROOM_ID = "room_id";
	public static final String ROOM_NAME = "room_name";
	public static final String ROOM_PURPOSE = "room_purpose";
	public static final String ALARM_TIME = "alarm_time";
	public static final String ALARM_CODE = "alarm_code";
	public static final String ALARM_ENABLED = "alarm_enabled";
	public static final String USER_ROOMTIME_OPTION = "user_roomtime_option";
	

	public AlarmDBHelper(Context context) {
		super(context, DATABASE_NAME, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE "
				+ TABLE_NAME 
					+ " (" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"user_id TEXT," +
						"room_id INTEGER," +
						"room_name TEXT," +
						"room_purpose TEXT," +
						"alarm_time TEXT," +
						"alarm_code INTEGER," +
						"alarm_enabled INTEGER,"+
						"user_roomtime_option INTEGER" +
				    	");"
					);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	}
}
