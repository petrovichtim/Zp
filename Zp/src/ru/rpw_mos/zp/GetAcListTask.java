package ru.rpw_mos.zp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GetAcListTask extends AsyncTask<String, Integer, String> {
	ProgressDialog progressDialog;
	public Context ctx;
	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_PRODUCTS = "products";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_DATE = "date";
	private static final String TAG_REGION = "region";

	// products JSONArray
	JSONArray products = null;

	@Override
	protected String doInBackground(String... args) {
		// тут нужно собрать все sys_id уже имеющиеся в системе и отправить их
		// на сервер

		// Building Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ids", args[0]));
		// getting JSON string from URL
		JSONObject json = jParser.makeHttpRequest(
				ctx.getString(R.string.host_to_get_list), "POST", params);

		// Check your log cat for JSON reponse
		Log.d("All Products: ", json.toString());

		try {
			// Checking for SUCCESS TAG
			int success = json.getInt(TAG_SUCCESS);

			if (success == 1) {
				// products found
				// Getting Array of Products
				products = json.getJSONArray(TAG_PRODUCTS);

				// looping through All Products
				for (int i = 0; i < products.length(); i++) {
					JSONObject c = products.getJSONObject(i);

					// Storing each json item in variable
					String id = c.getString(TAG_ID);
					String name = c.getString(TAG_NAME);
					String region = c.getString(TAG_REGION);
					String date = c.getString(TAG_DATE);

					// creating new HashMap
					HashMap<String, String> map = new HashMap<String, String>();

					// adding each child node to HashMap key => value
					map.put(TAG_ID, id);
					map.put(TAG_NAME, name);
					map.put(TAG_REGION, region);
					map.put(TAG_DATE, date);

					// adding HashList to ArrayList
					Main.accountsList.add(map);
				}
				return "Колличество доступных расчетов: " + products.length();
			} else {
				return "Нет доступных расчетов";
				// no ac found
				// Launch Add New product Activity
				// Intent i = new Intent(ctx, NewProductActivity.class);
				// Closing all previous activities
				// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				// startActivity(i);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(ctx);
		progressDialog.setMessage("Получение данных ...");
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(true);
		progressDialog.show();
		Main.accountsList.clear();
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {// это ответ
			progressDialog.hide();
			progressDialog.dismiss();
			Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
			if (!result.equals("Нет доступных расчетов"))
				Main.CreateListDialog();
			super.onPostExecute(result);
			return;
		} else
			Toast.makeText(ctx, R.string.download_error, Toast.LENGTH_LONG)
					.show();
		progressDialog.hide();
		progressDialog.dismiss();
		super.onPostExecute(result);

	}

}