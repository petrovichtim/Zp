package ru.rpw_mos.zp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DbAdapter {
    private static final String DB_NAME = "zp.db";
    private static final String DB_TABLE_NAME = "accounts";

    private static final int DB_VERSION = 1;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_REGION = "region";
    public static final String AC_ID = "account_id";
    public static final String EXP_ID = "expenses_id";

    private static final String DB_VERSION_TAG = "DB_VERSION";
    private static final String TAG = DbAdapter.class.getSimpleName();

    // public static final String COLUMN_TEXT = "text";

    /*
     * private static final String DB_CREATE = "create table " + DB_TABLE_NAME +
     * " (" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_NOTE +
     * " text not null);";
     */
    private static String DB_PATH = null;
    private static SQLiteDatabase mDb = null;
    private final Context mContext;
    private DbHelper mDbHelper = null;

    static void copyDBifNeeded(Context c) throws IOException {
        boolean unpackDB = false;
        File dbFile = new File(getDBPath(c));
        SharedPreferences pref = c.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        if (!dbFile.exists()) {
            Log.d(TAG, "DB doesn't exist");
            dbFile.getParentFile().mkdirs();
            unpackDB = true;
        } else if (pref.getInt(DB_VERSION_TAG, 0) < DB_VERSION) {
            Log.d(TAG, "Forcing updating DB");
            unpackDB = true;
        }

        if (unpackDB) {
            unpackDB(c);
        }

    }

    static void unpackDB(Context c) throws IOException {
        SharedPreferences pref = c.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        Log.d(TAG, "Unpacking DB from assets");// to " +
        // dbFile.getAbsolutePath());

        InputStream is = c.getAssets().open(DB_NAME);
        int size;
        byte[] buffer = new byte[2048];

        // c.getApplicationInfo().dataDir.toString() + "/"
        // + DB_NAME;

        FileOutputStream fout = new FileOutputStream(getDBPath(c), false);
        BufferedOutputStream bufferOut = new BufferedOutputStream(fout,
                buffer.length);
        while ((size = is.read(buffer, 0, buffer.length)) != -1) {
            bufferOut.write(buffer, 0, size);
        }
        bufferOut.flush();
        bufferOut.close();
        fout.close();
        is.close();
        if (mDb == null)
            mDb = SQLiteDatabase.openDatabase(getDBPath(c), null,
                    SQLiteDatabase.OPEN_READWRITE);// OPEN_READONLY

        Log.d(TAG, "Upgrading complete!");

        pref.edit().putInt(DB_VERSION_TAG, DB_VERSION).apply();
        // Mark that everything has been done correctly

    }

    ;

    public static String getDBPath(Context c) {
        String path;
        File DBnoSD = c.getDatabasePath(DB_NAME);
        try {
            path = c.getExternalFilesDir(null).getPath() + "/";
        } catch (NoSuchMethodError e) { // Android 2.1 clause
            path = Environment.getExternalStorageDirectory() + "/Android/data/"
                    + c.getPackageName() + "/db/";
        } catch (NullPointerException e2) { // If no SD card is present - store
            // DB on main partition
            return DBnoSD.getAbsolutePath();
        }
        if (DBnoSD.exists())
            DBnoSD.delete();
        return path + DB_NAME;
    }

    static String getDataPath(Context c) {
        String path;
        File DBnoSD = c.getDatabasePath(DB_NAME);
        try {
            path = c.getExternalFilesDir(null).getAbsolutePath() + "/";
        } catch (NoSuchMethodError e) { // Android 2.1 clause
            path = Environment.getExternalStorageDirectory() + "/Android/data/"
                    + c.getPackageName() + "/";
        } catch (NullPointerException e2) { // If no SD card is present - store
            // DB on main partition
            return DBnoSD.getAbsolutePath();
        }
        if (DBnoSD.exists())
            DBnoSD.delete();
        return path;
    }

    static String getImagesPath(Context c) {
        String path;
        File DBnoSD = c.getDatabasePath(DB_NAME);
        try {
            path = c.getExternalFilesDir(null).getAbsolutePath() + "/images/";
        } catch (NoSuchMethodError e) { // Android 2.1 clause
            path = Environment.getExternalStorageDirectory() + "/Android/data/"
                    + c.getPackageName() + "/images/";
        } catch (NullPointerException e2) { // If no SD card is present - store
            // DB on main partition
            return DBnoSD.getAbsolutePath();
        }
        if (DBnoSD.exists())
            DBnoSD.delete();
        File imagesDirectory = new File(path);
        // have the object build the directory structure, if needed.
        imagesDirectory.mkdirs();
        return path;
    }

    static String getMyImagesPath(Context c) {
        String path;
        File DBnoSD = c.getDatabasePath(DB_NAME);
        try {
            path = c.getExternalFilesDir(null).getAbsolutePath()
                    + "/my_images/";
        } catch (NoSuchMethodError e) { // Android 2.1 clause
            path = Environment.getExternalStorageDirectory() + "/Android/data/"
                    + c.getPackageName() + "/my_images/";
        } catch (NullPointerException e2) { // If no SD card is present - store
            // DB on main partition
            return DBnoSD.getAbsolutePath();
        }
        if (DBnoSD.exists())
            DBnoSD.delete();
        File imagesDirectory = new File(path);
        // have the object build the directory structure, if needed.
        imagesDirectory.mkdirs();
        return path;
    }

    public DbAdapter(Context context) {
        mContext = context;
        DB_PATH = getDBPath(context);// context.getDatabasePath(DB_NAME).getPath();
        if (mDbHelper == null)
            mDbHelper = new DbHelper(mContext, DB_NAME, null, DB_VERSION);

    }

    public DbAdapter open() throws SQLException {
        if (mDb == null || !mDb.isOpen()) {
            try {
                copyDBifNeeded(mContext);
                Log.d(TAG, "Opening DB");
                mDb = SQLiteDatabase.openDatabase(getDBPath(mContext), null,
                        SQLiteDatabase.OPEN_READWRITE);// OPEN_READONLY
            } catch (IOException e) {
                Log.e(TAG, "bad DB", e);
                mDb = null;
            }
        }

        return this;
    }

    public void close() {
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    public Cursor getAccounts(int AcType) {
        if (mDb == null) {
            return null;
        }
        if (AcType == R.string.My_accounts)
            return mDb.query(DB_TABLE_NAME, null, "own_ac=1", null, null, null,
                    "date");
        if (AcType == R.string.Comrads_accounts)
            return mDb.query(DB_TABLE_NAME, null, "own_ac=0", null, null, null,
                    "date");
        if (AcType == R.string.ac_list)
            return getExpenses(Main.mAccount_id);

        return null;
    }

    public Cursor getAccount(long id) {
        if (mDb == null) {
            return null;
        }
        String selection = "_id=?";
        String[] selectionArgs = {"" + id};
        return mDb.query("accounts", null, selection, selectionArgs, null,
                null, null);
    }

    public Cursor getListAccount(long mAccount_id) {
        if (mDb == null) {
            return null;
        } else {
            String[] selectionArgs = {String.valueOf(mAccount_id)};
            return mDb
                    .rawQuery(
                            "select  ac.name name, ac.region region, ac.date date,ac.total total,ac.zp zp, ac.sys_id sys_id, ex.expenses_id exid,"
                                    + " ex.sum sum from accounts ac join expenses ex on ac._id=ex.account_id"
                                    + " where ac._id=? order by ex.expenses_id",
                            selectionArgs);

        }

    }

    public void updSys_id(String sys_id, String _id) {
        if (mDb == null) {
            return;
        } else {
            String[] selectionArgs = {sys_id, _id};
            Cursor c = mDb.rawQuery("update accounts set sys_id=? where _id=?",
                    selectionArgs);
            c.moveToFirst();
            c.close();

        }

    }

    public long insertExp(String account_id, String expenses_id, String sum) {
        if (mDb == null) {
            return -1;
        } else {
            ContentValues newValues = new ContentValues();
            newValues.put("account_id", account_id);
            newValues.put("expenses_id", expenses_id);
            newValues.put("sum", sum);
            Log.d("insert", "прошел инсерт insertExp");
            return mDb.insert("expenses", null, newValues);

        }
    }

    public int updateExp(String sum, String id) {
        ContentValues newValues = new ContentValues();
        newValues.put("sum", sum);
        return mDb.update("expenses", newValues, COLUMN_ID + " = ? ",
                new String[]{id});
    }

    public String getSysAcList() {

        StringBuilder s = new StringBuilder();
        Cursor c = mDb.rawQuery(
                "Select sys_id from accounts  where sys_id is not null ", null);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            if (!c.isNull(c.getColumnIndex("sys_id"))) // ага нул тут заменился
            // пробелом
            {
                String tmp = c.getString(c.getColumnIndex("sys_id"));
                if (tmp != null && tmp.length() != 0)
                    s.append(tmp + ',');
            }
        }
        c.close();
        Log.d("updateExp", "getSysAcList String=" + s.toString());
        if (s.length() > 1)
            s.deleteCharAt(s.length() - 1);
        return s.toString();
    }

    public int updateImportExp(String sum, String exp_id, String ac_id) {
        ContentValues newValues = new ContentValues();
        newValues.put("sum", sum);
        return mDb.update("expenses", newValues, AC_ID + " = ?  and " + EXP_ID
                + "=?", new String[]{ac_id, exp_id});
    }

    public long insertAccount(String name, String region, String date,
                              String sys_id, int own_ac) {
        if (mDb == null) {
            return -1;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            try {
                Date d = sdf.parse(date);
                date = dateFormat.format(d);

            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Log.d("insertAccount", "date =" + date);
            ContentValues newValues = new ContentValues();
            newValues.put("name", name);
            newValues.put("region", region);
            newValues.put("date", date);
            newValues.put("sys_id", sys_id);
            newValues.put("own_ac", own_ac);
            return mDb.insert("accounts", null, newValues);
        }
    }

    public int updateAccount(String name, String region, String date, String id) {
        ContentValues newValues = new ContentValues();
        newValues.put("name", name);
        newValues.put("region", region);
        newValues.put("date", date);
        return mDb.update("accounts", newValues, COLUMN_ID + " = ? ",
                new String[]{id});
    }

    public void deleteAccount(String id) {
        if (mDb != null)
            mDb.delete("accounts", COLUMN_ID + " = ? ", new String[]{id});

    }

    public Cursor getExpenses(long account_id) {
        if (mDb == null) {
            return null;
        } else {
            return mDb
                    .rawQuery(
                            "Select le._id _id,le.name name,le.description description, ex._id ex_id, ex.account_id,"
                                    + "ex.expenses_id, ex.sum sum from list_of_expenses  le"
                                    + " join expenses ex on ex.expenses_id=le._id"
                                    + " where ex.account_id="
                                    + account_id
                                    + " order by le._id", null);

        }

    }

    private static class DbHelper extends SQLiteOpenHelper {
        private SQLiteDatabase myDataBase;
        private final Context mContext;

        public DbHelper(Context context, String name, CursorFactory factory,
                        int version) {

            super(context, name, factory, version);
            this.mContext = context;
        }

        public void createDataBase() throws IOException {
            boolean dbExist = checkDataBase();

            if (dbExist) {
                // ничего не делать - база уже есть
            } else {
                // вызывая этот метод создаем пустую базу, позже она будет
                // перезаписана
                this.getReadableDatabase();

                try {
                    copyDataBase();
                } catch (IOException e) {
                    throw new Error("Error copying database");
                }
            }
        }

        /**
         * Проверяет, существует ли уже эта база, чтобы не копировать каждый раз
         * при запуске приложения
         *
         * @return true если существует, false если не существует
         */
        private boolean checkDataBase() {

            File dbFile = new File(DB_PATH);
            return dbFile.exists();
            /*
             * была такая проверка но она не работает в 2.2 и ниже
			 * SQLiteDatabase checkDB = null;
			 * 
			 * try { String myPath = DB_PATH;// + DB_NAME; checkDB =
			 * SQLiteDatabase.openDatabase(myPath, null,
			 * SQLiteDatabase.OPEN_READONLY); } catch (SQLiteException e) { //
			 * база еще не существует } if (checkDB != null) { checkDB.close();
			 * } return checkDB != null ? true : false;
			 */
        }

        /**
         * Копирует базу из папки assets заместо созданной локальной БД
         * Выполняется путем копирования потока байтов.
         */
        private void copyDataBase() throws IOException {
            // Открываем локальную БД как входящий поток
            InputStream myInput = mContext.getAssets().open(DB_NAME);

            // Путь ко вновь созданной БД
            String outFileName = DB_PATH;// + DB_NAME;

            // Открываем пустую базу данных как исходящий поток
            OutputStream myOutput = new FileOutputStream(outFileName);

            // перемещаем байты из входящего файла в исходящий
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            // закрываем потоки
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }

        // public void openDataBase() throws SQLException {
        // // открываем БД
        // String myPath = DB_PATH;// + DB_NAME;
        // myDataBase = SQLiteDatabase.openDatabase(myPath, null,
        // SQLiteDatabase.OPEN_READONLY);
        // }

        @Override
        public synchronized void close() {
            if (myDataBase != null)
                myDataBase.close();
            super.close();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Toast.makeText(mContext, "Словарь обновляется",
            // Toast.LENGTH_LONG)
            // .show();
            try {
                copyDataBase();

            } catch (IOException e) {
                //
                throw new Error("Error upgrading database");
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion,
                                int newVersion) {
            // super.onDowngrade(db, oldVersion, newVersion);
        }

    }

}
