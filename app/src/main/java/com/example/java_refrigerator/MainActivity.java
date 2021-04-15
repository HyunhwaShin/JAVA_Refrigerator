package com.example.java_refrigerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private String dbName = "Refrigerator.db";
    private int dbVersion =1;

    private DBHelper dbHelper;

    private ArrayList<FoodDB> Food;
    private MyListAdapter MyAdapter;

    private Button add, all, up, down, paste;
    private TextView comment;
    private ListView myFoodList;

    //field의 상태를 알아보기 위해
    private boolean isLong = false;

    private int deaded_food =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        dbHelper = new DBHelper(this, dbName, null, dbVersion);
        Food = new ArrayList<FoodDB>();

        paste = (Button) findViewById(R.id.paste);
        comment = (TextView) findViewById(R.id.comment);
        myFoodList = (ListView) findViewById(R.id.foodList);
        add = (Button) findViewById(R.id.add);
        all = (Button) findViewById(R.id.all);
        up = (Button) findViewById(R.id.up);
        down = (Button) findViewById(R.id.down);

        MyAdapter = new MyListAdapter(this, R.layout.icontext_activity, Food);
        myFoodList.setAdapter(MyAdapter);

        search_all_db();

        myFoodList.setOnItemClickListener(new OnItemClickListener() {

            //short click
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final LinearLayout linear = (LinearLayout)View.inflate(MainActivity.this, R.layout.info_activity, null);

                if(isLong == false){
                    String path = Food.get(position).mPath;

                    ImageView pic = (ImageView) linear.findViewById(R.id.Picture);
                    TextView memo = (TextView) linear.findViewById(R.id.memo);

                    InputStream in = null;       //입력을 받아오기 위해서
                    File file = new File(getExternalCacheDir(),path);

                    //Image 받아올 때 오류처리
                    try{
                        in = new FileInputStream(file);

                        Bitmap bitmap = BitmapFactory.decodeStream(in);
                        pic.setImageBitmap(bitmap);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try{
                        in.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    memo.setText(Food.get(position).memo);
                }

                //Dialog 로 Img, memo 보여주기
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("음식정보 ("+ Food.get(position).UpDown + ")").setView(linear)
                        .setPositiveButton("확인", null).show();
            }
        });

        //long click
        myFoodList.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String f_name = Food.get(position).foodName;

                isLong = true;

                //button 누르면 delete
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("삭제").setMessage(f_name + "삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String path = Food.get(position).mPath;
                                delete_food_db(path);
                                Food.remove(position);

                                MyAdapter.notifyDataSetChanged();
                                search_all_db();
                                isLong = false;
                            }
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        isLong = false;
                    }
                }).show();

                return false;
            }
        });









    }



    //manage DB
    public static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE Refrigerator(_foodName TEXT, _limitDate TEXT, _updown TEXT, mpath TEXT primary key, memo TEXT);");
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS Refrigerator");
            onCreate(db);
        }
    }


    //manage about ListView
    class MyListAdapter extends BaseAdapter {

        Context maincon;
        LayoutInflater Inflater;
        ArrayList<FoodDB> myFoodList;
        int layout;

        public MyListAdapter(Context _context, int _layout, ArrayList<FoodDB> Food) {
            maincon = _context;
            Inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myFoodList = Food;
            layout = _layout;

        }

        // items size 즉 number of items
        @Override
        public int getCount() {
            return myFoodList.size();
        }

        //item's name
        @Override
        public Object getItem(int position) {
            return myFoodList.get(position);
        }

        //position
        @Override
        public long getItemId(int position) {
            return position;
        }

        //ListView draw
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            //imageView 에 image 그리기
            if(view == null){
                view = Inflater.inflate(layout, parent,false);
            }
            ImageView img = (ImageView)view.findViewById(R.id.img);

            if(myFoodList.get(position).UpDown.equals("냉동")){
                img.setImageResource(R.drawable.ice);
            }else{
                img.setImageResource(R.drawable.water);
            }

            TextView foodName = (TextView) view.findViewById(R.id.f_name);
            foodName.setText(myFoodList.get(position).foodName);

            TextView limitData = (TextView) view.findViewById(R.id.l_date);
            foodName.setText(myFoodList.get(position).foodName);

            return view;
        }
    }
    //all data in db
    private void search_all_db() {
        SQLiteDatabase db;

        Food.clear();
        db= dbHelper.getReadableDatabase();

        deaded_food=0;

        Cursor cursor = db.rawQuery("SELECT * FROM Refrigerator", null);

        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                FoodDB foodDB = new FoodDB(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4));

                //insert
                Food.add(foodDB);

                //exceeded food check
                boolean state = over_limit_date(cursor.getString(1));
                if(state){
                    deaded_food++;
                }
            }
        }
        //버튼 위에 유통기한 지난 음식의 수 표시
        paste.setText(deaded_food+ "/" + Food.size());

        deaded_food =0;

        cursor.close();
        dbHelper.close();
    }

    private void delete_food_db(String path) {
        SQLiteDatabase db;
        db = dbHelper.getWritableDatabase();     //db를 open

        db.execSQL("DELETE FROM Refrigerator WHERE mpath='"+ path +"';");
    }

    private void search_paste_db(){
        SQLiteDatabase db;

        Food.clear();
        db= dbHelper.getReadableDatabase();

        deaded_food=0;

        Cursor cursor = db.rawQuery("SELECT * FROM Refrigerator", null);

        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                FoodDB foodDB = new FoodDB(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4));

                //insert
                Food.add(foodDB);

                //exceeded food check
                boolean state = over_limit_date(cursor.getString(1));
                if(state){
                    deaded_food++;
                }
            }
        }
        cursor.close();
        dbHelper.close();
    }
    private void search_up_db(){
        SQLiteDatabase db;

        Food.clear();
        db= dbHelper.getReadableDatabase();

        deaded_food=0;

        Cursor cursor = db.rawQuery("SELECT * FROM Refrigerator WHERE up_down = '냉동'", null);

        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                FoodDB foodDB = new FoodDB(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4));

                Food.add(foodDB);

                boolean state = over_limit_date(cursor.getString(1));
                if(state){
                    deaded_food++;
                }
            }
        }
        paste.setText(deaded_food+ "/" + Food.size());

        deaded_food =0;

        cursor.close();
        dbHelper.close();

    }

    private void search_down_db(){
        SQLiteDatabase db;

        Food.clear();
        db= dbHelper.getReadableDatabase();

        deaded_food=0;

        Cursor cursor = db.rawQuery("SELECT * FROM Refrigerator WHERE up_down = '냉장'", null);

        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                FoodDB foodDB = new FoodDB(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4));

                Food.add(foodDB);

                boolean state = over_limit_date(cursor.getString(1));
                if(state){
                    deaded_food++;
                }
            }
        }
        paste.setText(deaded_food+ "/" + Food.size());

        deaded_food =0;

        cursor.close();
        dbHelper.close();

    }

    //오늘의 날짜로 유통기한 지났는지 확인
    private boolean over_limit_date(String f_date) {
        try {
            // date format
            SimpleDateFormat d_format = new SimpleDateFormat("yyyy/MM/dd");

            // today date, time
            Calendar cal = Calendar.getInstance();

            String today = d_format.format(cal.getTime());

            // 비교하기위해 같이 포맷
            Date date1 = d_format.parse(f_date);			// database exceeded date
            Date date2 = d_format.parse(today);				// today date

            Log.e("date1", date1 + "");
            Log.e("date2", date2 + "");

            if (date1.before(date2) || date1.equals(date2)) {
                // true 이면 유통기한을 지났다는 뜻
                return true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        //false 이면 유통기한을 안지났다는 뜻
        return false;
    }

}