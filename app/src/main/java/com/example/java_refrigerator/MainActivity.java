package com.example.java_refrigerator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private String dbName = "Refrigerator.db";
    private int dbVersion =1;

    private DBHelper dbHelper;

    private ArrayList<FoodDB> Food;      //freezefood == Food
    private MyListAdapter MyAdapter;

    private Button add, all, up, down, paste;
    private TextView comment;
    private ListView myFoodList;    //foodList == myFoodList

    //field의 상태를 알아보기 위해
    private boolean isLong = false;

    private int deaded_food =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        dbHelper = new DBHelper(this, dbName, null, dbVersion);
        Food = new ArrayList<FoodDB>();

        //
        paste = (Button) findViewById(R.id.paste);
        comment = (TextView)findViewById(R.id.comment);
        myFoodList = (ListView) findViewById(R.id.foodList);
        add = (Button) findViewById(R.id.add);
        all = (Button) findViewById(R.id.all);
        up = (Button) findViewById(R.id.up);
        down = (Button) findViewById(R.id.down);

        MyAdapter = new MyListAdapter(this, R.layout.icontext_activity, Food);
        myFoodList.setAdapter(MyAdapter);


    }

    private class DBHelper {
        public DBHelper(MainActivity mainActivity, String dbName, Object o, int dbVersion) {
        }
    }

    //manage about ListView //구성을 잘 모르겠다 푸하하하항
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
}