package com.example.java_refrigerator;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.lang.String.format;

public class AddActivity extends Activity {

    private String dbName = "Refrigerator.db";
    private int dbVersion = 1;

    private DBHelper dbHelper;

    private ImageView img;
    private Button save, limitDate;
    private EditText foodName, memo;
    private Switch sw;

    //image 의 경로와 이름
    private String mPath;
    private String saveIMG ="";

    private String showDate,deadDate;

    private static final int CALL_CAMERA = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        dbHelper = new DBHelper(this,dbName, null, dbVersion);

        foodName = (EditText)findViewById(R.id.food_name);
        limitDate = (Button) findViewById(R.id.limitDate);
        memo = (EditText) findViewById(R.id.memo);
        sw = (Switch) findViewById(R.id.up_down);
        img = (ImageView) findViewById(R.id.image);
        save = (Button) findViewById(R.id.add_save);

        String FreshFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Freshment";
        File f = new File(FreshFolder);
        if(!f.exists())
            f.mkdir();

        mPath = FreshFolder+"/tmpimage.png";

        final Calendar cal = Calendar.getInstance();

        showDate ="유통기한 : " + cal.get(Calendar.YEAR)+ "년" +((cal.get(Calendar.MONTH))+1) + "월" + cal.get(Calendar.DATE) +"일";
        deadDate =cal.get(Calendar.YEAR)+ "년" +((cal.get(Calendar.MONTH))+1) + "월" + cal.get(Calendar.DATE) +"일";

        limitDate.setText(showDate);

        limitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Today date
                DatePickerDialog dateDialog = new DatePickerDialog(AddActivity.this, listener, cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH)), cal.get(Calendar.DATE));
                dateDialog.show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db;

                db= dbHelper.getWritableDatabase();

                String name ="";
                String mo ="";
                String state ="";

                name = foodName.getText().toString();
                mo = memo.getText().toString();

                //switch
                if(sw.isChecked()){
                    state="냉장";
                }else{
                    state="냉동";
                }
                if (!name.equals("") && !saveIMG.equals("")){
                    //INSET INTO Refrigerator VALUES('foodName', 'exceeded date', 'up_down', 'fileName', 'memo')
                    String sql = String.format("INSERT INTO Refrigerator VALUES('%s', '%s', '%s', '%s', '%s')", name, deadDate, state, saveIMG, mo);
                    db.execSQL(sql);

                    Intent intent = new Intent(getApplication(),MainActivity.class);
                    startActivity(intent);
                    //background 에서 삭제
                    finish();
                }else {
                    Toast.makeText(getApplicationContext(),"이름과 사진은 필수입니다.",Toast.LENGTH_LONG).show();
                }
            }
            });
            }

    //유통기한
    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            showDate = "유통기한 : " + year + "년" + (monthOfYear + 1) + "월" + dayOfMonth + "일" ;
            deadDate = year + "년" + (monthOfYear + 1) + "월" + dayOfMonth + "일" ;

            limitDate.setText(showDate);
        };
    };

    //Camera
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {//make picture

            if (requestCode == CALL_CAMERA) {

                Bitmap bm = BitmapFactory.decodeFile(mPath);

                Bitmap saveImg = Bitmap.createScaledBitmap(bm, 250, 200, false);

                OutputStream out = null;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/mm/dd/hh:mm:ss");
                String date = dateFormat.format(new Date());
                saveIMG = date + ".png";

                File file = new File(getExternalCacheDir(), saveIMG);
                try {
                    // write image file
                    out = new FileOutputStream(file);

                    saveImg.compress(Bitmap.CompressFormat.JPEG, 100, out);

                    // draw image View
                    img.setImageBitmap(saveImg);

                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "사진 찍기 실패", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                try {
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class DBHelper extends SQLiteOpenHelper {

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
}
