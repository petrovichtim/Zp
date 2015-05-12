package ru.rpw_mos.zp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SendDataTask extends AsyncTask<String, Integer, String> {
    ProgressDialog progressDialog;
    public Context ctx;

    // private Exception m_error = null;

    @Override
    protected String doInBackground(String... key) {

        // Tools.SendJava();
        // Create a new HttpClient and Post Header

        try {
            URL url1 = new URL(ctx.getString(R.string.host_to_send));
            HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            // вставляем параметры для поста
            Map<String, String> params = new HashMap<String, String>();
            params.put("name", key[0]);
            params.put("region", key[1]);
            params.put("date", key[2]);
            params.put("total", key[3]);
            params.put("zp", key[4]);
            for (int i = 5; i < 40; i++)
                params.put("sum" + (i - 3),
                        key[i]);

            writer.write(JSONParser.createQueryStringForParameters(params));
            writer.flush();
            writer.close();
            os.close();
            try {
                InputStream is = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                // is, "UTF-8"), 8);
                //is,"iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;// = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                String resp = sb.toString();
                Log.d("пост", "пост прошел и ответ" + resp.toString());
                return resp;
            } finally {
                urlConnection.disconnect();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(ctx);
        progressDialog.setMessage("Отправка данных ...");
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progressDialog
                .setProgress((int) ((values[0] / (float) values[1]) * 100));
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {// это ответ
            progressDialog.hide();
            progressDialog.dismiss();
            Log.d("SendDataTask", "Данные получены" + result);
            Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
            super.onPostExecute(result);
            return;
        }
        progressDialog.hide();
        progressDialog.dismiss();
        super.onPostExecute(result);
    }

}