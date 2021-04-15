package com.example.java_refrigerator;

import android.annotation.SuppressLint;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class AlarmReceiver extends BroadcastReceiver {
    private static final int NAPNOTI = 1;
    private NotificationManager mNotimanager;

    private String dbName="Refrigerator.db";
    private int dbVersion = 1;

    private DBHelper dbHelper;

    private int deaded =0;
    private int state =0;

    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"alarm",Toast.LENGTH_LONG).show();

        String action = intent.getAction();

        // when phone reboot
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)){
            PendingIntent sender = PendingIntent.getBroadcast(context,0,intent,0);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

            Calendar compareDate = Calendar.getInstance();

            //9시에 알람을 울릴 것이다
            compareDate.set(Calendar.HOUR_OF_DAY,9);
            compareDate.set(Calendar.MINUTE,0);
            compareDate.set(Calendar.SECOND,0);
            compareDate.set(Calendar.MILLISECOND,0);

            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, compareDate.getTimeInMillis(),24 * 3600 * 1000 , sender);

        }
        dbHelper = new DBHelper(context,dbName,null,dbVersion);
        SQLiteDatabase db;
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Refrigerator", null);

        // exceeded food check
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                if(over_limit_date(cursor.getString(1)))
                    deaded++;
            }
        }
        cursor.close();
        dbHelper.close();

        if(deaded>0){
            //notification
            mNotimanager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification noti = new Notification.Builder(context).setTicker(deaded + "개의 음식이 유통기한이 지났습니다.")
                    .setSmallIcon(R.drawable.icon).setContentTitle("유통기한 알리미")
                    .setContentText("유통기한 지난 음식의 수 : " + deaded).setSubText("유통기한 지난 음식을 폐기해주세요!")
                    .build();
            mNotimanager.notify(NAPNOTI,noti);
            deaded = 0;


        }
    }

    private boolean over_limit_date(String f_date) {
        try{
            SimpleDateFormat d_format = new SimpleDateFormat("yyyy/MM/dd");
            Calendar cal = Calendar.getInstance();
            String today = d_format.format(cal.getTime());

            Date date1 = d_format.parse(f_date);
            Date date2 = d_format.parse(today);

            if(date1.before(date2) || date1.equals(date2)){
                return true;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE Refrigerator (_foodName TEXT, _limitedDate TEXT, _up_down TEXT, mpath TEXT primary key, memo TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS Refrigerator");
            onCreate(db);
        }
    }

}
