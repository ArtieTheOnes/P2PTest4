package artie.urop.sqliteapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by artie on 7/10/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String  DATABASE_NAME = "test.db";
    public static final String  TABLE_NAME = "test_table";


    public static final String COL_1 = "MOMENT";
    public static final String COL_2 = "SENDER";
    public static final String COL_3 = "RECEIVER";
    public static final String COL_4 = "FILE";
    public static final String COL_5 = "FUNCTION";
    public static final String COL_6 = "DURATION";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (MOMENT DATETIME DEFAULT CURRENT_TIMESTAMP,SENDER TEXT,RECEIVER TEXT,FILE TEXT,FUNCTION TEXT,DURATION TIME)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData (String sender, String receiver, String filename, String function, String endtime)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        final String time = getDateTime();

        contentValues.put(COL_1, getDateTime());
        contentValues.put(COL_2, sender);
        contentValues.put(COL_3, receiver);
        contentValues.put(COL_4, filename);
        contentValues.put(COL_5, function);
        //contentValues.put();

        long result = db.insert(TABLE_NAME,null,contentValues);

        return !(result == -1);

    }


    public Cursor getAllData ()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    public boolean updateData(String id,String content)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put(COL_1,id);
        contentValues.put(COL_2,id);
        contentValues.put(COL_3, content);
        contentValues.put(COL_4,getDateTime());
        db.update(TABLE_NAME,contentValues,"ID = ?", new String[] {id});
        return true;

    }

    public Integer deleteData(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }




}
