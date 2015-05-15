package ru.rpw_mos.zp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
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

//import android.util.Log;

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

    private static SQLiteDatabase mDb = null;
    private final Context mContext;
    private DBHelper mDbHelper = null;

    static void copyDBifNeeded(Context c) throws IOException {
        boolean unpackDB = false;
        File dbFile = new File(getDBPath(c));
        SharedPreferences pref = c.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        if (!dbFile.exists()) {
            //Log.d(TAG, "DB doesn't exist");
            dbFile.getParentFile().mkdirs();
            unpackDB = true;
        } else if (pref.getInt(DB_VERSION_TAG, 0) < DB_VERSION) {
            // Log.d(TAG, "Forcing updating DB");
            unpackDB = true;
        }

        if (unpackDB) {
            unpackDB(c);
        }

    }

    static void unpackDB(Context c) throws IOException {
        SharedPreferences pref = c.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        // Log.d(TAG, "Unpacking DB from assets");// to " +
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

        // Log.d(TAG, "Upgrading complete!");

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


    public DbAdapter(Context context) {
        mContext = context;
        if (mDbHelper == null)
            mDbHelper = new DBHelper(mContext);

    }

    public DbAdapter open() throws SQLException {
        if (mDb == null || !mDb.isOpen()) {
            try {
                copyDBifNeeded(mContext);
                //Log.d(TAG, "Opening DB");
                if (mDb == null || !mDb.isOpen())
                    mDb = SQLiteDatabase.openDatabase(getDBPath(mContext), null,
                            SQLiteDatabase.OPEN_READWRITE);// OPEN_READONLY
            } catch (IOException e) {
                //Log.e(TAG, "bad DB", e);
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
            //Log.d("insert", "прошел инсерт insertExp");
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
        //Log.d("updateExp", "getSysAcList String=" + s.toString());
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
                                    + " where ex.account_id=?  order by le._id", new String[]{String.valueOf(account_id)});

        }

    }

    private class DBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = DB_NAME;
        private static final int DATABASE_VERSION = DB_VERSION;
        private static final String SP_KEY_DB_VER = "db_ver";
        private final Context mContext;

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            initialize();
        }

        /**
         * Initializes database. Creates database if doesn't exist.
         */
        private void initialize() {
            if (databaseExists()) {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(mContext);
                int dbVersion = prefs.getInt(SP_KEY_DB_VER, 1);
                if (DATABASE_VERSION != dbVersion) {
                    File dbFile = mContext.getDatabasePath(DATABASE_NAME);
                    if (!dbFile.delete()) {
                        Log.w(TAG, "Unable to update database");
                    }
                }
            }
            if (!databaseExists()) {
                createDatabase();
            }
        }

        /**
         * Returns true if database file exists, false otherwise.
         */
        private boolean databaseExists() {
            File dbFile = mContext.getDatabasePath(DATABASE_NAME);
            return dbFile.exists();
        }

        /**
         * Creates database by copying it from assets directory.
         */
        private void createDatabase() {
            String parentPath = mContext.getDatabasePath(DATABASE_NAME).getParent();
            String path = mContext.getDatabasePath(DATABASE_NAME).getPath();

            File file = new File(parentPath);
            if (!file.exists()) {
                if (!file.mkdir()) {
                    Log.w(TAG, "Unable to create database directory");
                    return;
                }
            }

            InputStream is = null;
            OutputStream os = null;
            try {
                is = mContext.getAssets().open(DATABASE_NAME);
                os = new FileOutputStream(path);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(SP_KEY_DB_VER, DATABASE_VERSION);
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
