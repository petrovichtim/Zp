package com.rusdelphi.zp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class SendDataTask extends AsyncTask<String, Integer, String> {
	ProgressDialog progressDialog;
	public Context ctx;

	// private Exception m_error = null;

	@Override
	protected String doInBackground(String... key) {

		// Tools.SendJava();
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(ctx.getString(R.string.host_to_send));
		try {
			// Добавляем свои данные
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("name", key[0]));
			nameValuePairs.add(new BasicNameValuePair("region", key[1]));
			nameValuePairs.add(new BasicNameValuePair("date", key[2]));
			nameValuePairs.add(new BasicNameValuePair("total", key[3]));
			nameValuePairs.add(new BasicNameValuePair("zp", key[4]));
			for (int i = 5; i < 40; i++)
				nameValuePairs.add(new BasicNameValuePair("sum" + (i - 3),
						key[i]));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
					HTTP.UTF_8));
			// Выполняем HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			String resp = entityToString(response.getEntity());
			Log.d("пост", "пост прошел и ответ" + response.toString()
					+ " тело:" + resp);
			return resp;

		} catch (ClientProtocolException e) {
			Log.d("пост", "ошибка1" + e.toString());
			// m_error = e;
		} catch (IOException e) {
			Log.d("пост", "ошибка2" + e.toString());
			// m_error = e;
		}
		return null;
	}

	public static String entityToString(HttpEntity entity)
			throws IllegalStateException, IOException {
		InputStream is = entity.getContent();
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
			Log.d("SendDataTask", "Данные получены" + result);
			Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
			super.onPostExecute(result);
			return;
		}
		progressDialog.hide();
		super.onPostExecute(result);
	}

}