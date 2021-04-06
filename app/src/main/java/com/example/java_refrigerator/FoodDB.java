package com.example.java_refrigerator;


public class FoodDB {
    //create db form likes "foodname", "exceeded date", "where", "imagePath", "memo"

        public FoodDB(String _foodName, String _limitDate, String _UpDown, String _mPath, String _memo){
            this.foodName = _foodName;
            this.limitDate = _limitDate;
            this.UpDown = _UpDown;
            this.mPath = _mPath;
            this.memo = _memo;
        }

        String foodName;
        String limitDate;
        String UpDown;
        String mPath;
        String memo;
}
