package ru.rpw_mos.zp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Tools {
    private static final String TAG = "Tools";

    public static boolean isConnected(String url_name) {

        try {
            URL url = new URL(url_name);
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();
            urlConnection.connect();
            urlConnection.disconnect();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Exception", e);
        }
        return false;
    }

    public static File getDownloadsStorageDir(String fileName)
            throws IOException {
        File folder = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/");
        folder.mkdirs();

        File file = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName);
        if (!file.createNewFile()) {
            Log.d("main", "File not created");
        }
        return file;
    }

    public static String SaveStringToFile(String inFile, Context ctx,
                                          String file_name) {
        Log.d("main", "SaveStringToFile path=" + ctx.getFilesDir());
        String s = null;
        try {
            File f = getDownloadsStorageDir(file_name);
            FileWriter out = new FileWriter(f);
            out.write(inFile);
            s = f.getAbsolutePath();
            Log.d("main", "SaveStringToFile s=" + s);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s;

    }

    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static File getExternalSDCardDirectory() {
        File innerDir = Environment.getExternalStorageDirectory();
        File rootDir = innerDir.getParentFile();
        File firstExtSdCard = innerDir;
        File[] files = rootDir.listFiles();
        for (File file : files) {
            if (file.compareTo(innerDir) != 0) {
                firstExtSdCard = file;
                break;
            }
        }
        // Log.i("2", firstExtSdCard.getAbsolutePath().toString());
        return firstExtSdCard;
    }

    public static String LoadData(String inFile, Context ctx) {
        String tContents = null;

        try {
            InputStream stream = ctx.getAssets().open(inFile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            tContents = new String(buffer);
        } catch (IOException e) {
            // Handle exceptions here
        }

        return tContents;

    }

    public static String entityToString(InputStream is)
            throws IllegalStateException, IOException {
        // InputStream is = entity.getContent();
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(is));
        StringBuilder str = new StringBuilder();

        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                str.append(line + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // tough luck...
            }
        }
        return str.toString();
    }

    static void SendJava() {

        URL url;
        try {
            url = new URL("www.r-p-w.ru/wage/write_account.php");
            URLConnection connection = null;
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(
                    connection.getOutputStream());
            osw.write("name=TEST_java_UU&date=16.10.2014");
            osw.flush();
            int responseCode;
            responseCode = ((HttpURLConnection) connection).getResponseCode();

            InputStream is;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = connection.getInputStream();
                Log.d("main", "Получилось!");
                String s = entityToString(is);
                //
                Log.d("main", "Ответ=" + s);

            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void SendTestPost() {

        String urlParameters = "name=TEST&date=16.10.2014";
        String request = "http://r-p-w.ru/wage/write_account.php";
        URL url;
        try {
            url = new URL(request);

            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.getBytes().length));
            connection.setUseCaches(false);

            String s = entityToString(connection.getInputStream());
            // DataOutputStream wr = new DataOutputStream(
            // connection.getOutputStream());
            // wr.writeBytes(urlParameters);
            //
            Log.d("main", "Ответ=" + s);
            // wr.flush();
            // wr.close();
            connection.disconnect();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static boolean isConnectedOk(String url) {
        try {
            HttpGet request = new HttpGet(url);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
                @Override
                public long getKeepAliveDuration(HttpResponse response,
                                                 HttpContext context) {
                    return 0;
                }
            });
            HttpResponse response = httpClient.execute(request);
            return response.getStatusLine().getStatusCode() == 200;

        } catch (IOException e) {
        }
        return false;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static void hideSoftKeyboard(Activity a) {
        if (a.getCurrentFocus() != null)
            ((InputMethodManager) a
                    .getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(a.getCurrentFocus()
                            .getWindowToken(), 0);
    }

    public static String upperFirstLetter(String s) {
        if (s == null)
            return null;
        else if (s.length() == 0)
            return s;
        else
            return s.substring(0, 1).toUpperCase()
                    + s.substring(1, s.length()).toLowerCase();
    }

    public static byte[] intArrayToBytes(int[] ints) {
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            bytes[i] = (byte) ints[i];
        }
        return bytes;
    }

    public static void unpackZip(InputStream is, OutputStream os)
            throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze;

        while ((ze = zis.getNextEntry()) != null) {
            byte[] buffer = new byte[1024];
            int count;

            while ((count = zis.read(buffer)) > -1) {
                os.write(buffer, 0, count);
            }

            os.close();
            zis.closeEntry();
        }

        zis.close();
        is.close();
    }

    public static void unzip(String zipFile, String location)
            throws IOException {
        int size;
        byte[] buffer = new byte[2048];
        try {
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(
                    new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        FileOutputStream fout = new FileOutputStream(path,
                                false);
                        BufferedOutputStream bufferOut = new BufferedOutputStream(
                                fout, buffer.length);
                        while ((size = zin.read(buffer, 0, buffer.length)) != -1) {
                            bufferOut.write(buffer, 0, size);
                        }
                        bufferOut.flush();
                        bufferOut.close();
                        fout.close();
                        zin.closeEntry();
                    }
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unzip exception", e);
        }
    }

    public static void loadImage(Context context, ImageView img, String file) {
        try {
            if (Main.mDBisUnzipped) {
                String path = context.getApplicationInfo().dataDir.toString()
                        + "/" + file;
                InputStream is = new BufferedInputStream(new FileInputStream(
                        path));
                img.setImageBitmap(BitmapFactory.decodeStream(is));
                // ? надо ли ? is.close();
            } else
                img.setImageBitmap(BitmapFactory.decodeStream(context
                        .getAssets().open(file)));
            img.setTag(file);
            img.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            Log.e(TAG, "can't find file: " + file);
            img.setVisibility(View.GONE);
        }
    }

    public static void hideSoftKeyboard(Context c, View view) {
        InputMethodManager imm = (InputMethodManager) c
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
