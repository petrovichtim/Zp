package com.rusdelphi.zp;

import java.util.ArrayList;
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

public class GetAcTask extends AsyncTask<String, Integer, String> {
	ProgressDialog progressDialog;
	public Context ctx;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_ACCOUNTS = "accounts";
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_REGION = "region";
	private static final String TAG_DATE = "date";

	// products JSONArray
	JSONArray products = null;

	@Override
	protected String doInBackground(String... args) {

		// Building Parameters
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ids", args[0]));
		Log.d("main", "GetAcTask doInBackground args[0]="+args[0]);
		// getting JSON string from URL
		JSONObject json = jParser.makeHttpRequest(
				ctx.getString(R.string.host_to_get_ac), "POST", params);

		// Check your log cat for JSON reponse
		Log.d("All Products: ", json.toString());

		try {
			// Checking for SUCCESS TAG
			int success = json.getInt(TAG_SUCCESS);

			if (success == 1) {
				// products found
				// Getting Array of Products
				products = json.getJSONArray(TAG_ACCOUNTS);

				// looping through All Products
				for (int i = 0; i < products.length(); i++) {
					JSONObject c = products.getJSONObject(i);

					// Storing each json item in variable
					String sys_id = c.getString(TAG_ID); // тут sys_id дл€
															// локальной бд
					String name = c.getString(TAG_NAME);
					String region = c.getString(TAG_REGION);
					String date = c.getString(TAG_DATE);

					// тут надо инсерт в базу сделать
					long ac_id = Main.mDb.insertAccount(name,region, date,
							sys_id,0); // вставили
					// расчет
					// тут в цикле надо пробежать по затратам
					for (int j = 1; j < 36; j++) {
						String exp_id = String.valueOf(j);
						String sum = c.getString(exp_id);

						Main.mDb.updateImportExp(sum, exp_id,
								String.valueOf(ac_id));
						
					}
				}
				return " олличество сохраненных расчетов: " + products.length();
			} else {
				return "ќшибка сохранени€ расчетов";
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(ctx);
		progressDialog.setMessage("ѕолучение данных ...");
		progressDialog.setIndeterminate(false);
		progressDialog.setCancelable(true);
		progressDialog.show();
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {// это ответ
			progressDialog.hide();
			progressDialog.dismiss();
			Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
			My_accounts.UpdateAc();
			super.onPostExecute(result);
			return;
		}
		progressDialog.hide();
		progressDialog.dismiss();
		super.onPostExecute(result);

	}

}