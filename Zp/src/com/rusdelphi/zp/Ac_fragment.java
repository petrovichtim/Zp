package com.rusdelphi.zp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Ac_fragment extends Fragment {

	private View rootView;
	private EditText en;
	private EditText er;
	private EditText ed;
	private TextView tv_ac_itog;
	private TextView tv_ac_zp;
	private boolean mSiteIsAvailable = false;
	private static Cursor mCursor = null;
	static ArrayList<String> mDataList = new ArrayList<String>();

	public static Ac_fragment getInstance() {
		Ac_fragment f = new Ac_fragment();
		Bundle args = new Bundle();
		f.setHasOptionsMenu(true);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.ac_item_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete_account:
			DeleteAccount();
			return true;

		case R.id.share_account:
			try {
				ShareAccount();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;

		case R.id.send_account:
			SendAccount();
			return true;

		case R.id.print_account:
			PrintAccount();
			return true;

		case R.id.save_account:
			SaveAccount();
			return true;
		}

		return false;
	}

	private void SaveAccount() {
		EditText en = (EditText) rootView.findViewById(R.id.editName);
		EditText er = (EditText) rootView.findViewById(R.id.editRegion);
		EditText ed = (EditText) rootView.findViewById(R.id.editDate);
		if (en.getText().toString().isEmpty()
				|| er.getText().toString().isEmpty()
				|| ed.getText().toString().isEmpty())
			Toast.makeText(getActivity(), R.string.acl_save_error,
					Toast.LENGTH_SHORT).show();
		else {
			if (Main.mAccount_id == -1) {
				Main.mAccount_id = Main.mDb.insertAccount(en.getText()
						.toString(), er.getText().toString(), ed.getText()
						.toString(), null, 1);
			} else {
				Main.mDb.updateAccount(en.getText().toString(), er.getText()
						.toString(), ed.getText().toString(), String
						.valueOf(Main.mAccount_id));
			}
			Toast.makeText(getActivity(), R.string.ac_save_text,
					Toast.LENGTH_SHORT).show();
		}
	}

	public static String getHexString(String s) {
		byte[] buf = s.getBytes(Charset.forName("Cp1251"));
		StringBuffer sb = new StringBuffer();
		for (byte b : buf) {
			sb.append("\\'");
			sb.append(String.format("%x", b));

		}
		return sb.toString();
	}

	public String replaceString(String input, String field, String value) {
		String result = "\\lang1049\\f0  " + getHexString(value);
		Log.d("main", "result=" + result);
		input = input.replace(field, result);
		return input;
	}

	private void PrintAccount() {
		String file_rtf = Tools.LoadData("template.rtf", getActivity());
		// получить данные для отправки
		mCursor = Main.mDb.getListAccount(Main.mAccount_id);
		mCursor.moveToFirst();
		String file_name = mCursor.getString(mCursor.getColumnIndex("name"))
				.toString()
				+ "_"
				+ mCursor.getString(mCursor.getColumnIndex("date")).toString()
				+ ".rtf";
		file_rtf = replaceString(file_rtf, "name",
				mCursor.getString(mCursor.getColumnIndex("name")).toString());
		file_rtf = replaceString(file_rtf, "region",
				mCursor.getString(mCursor.getColumnIndex("region")).toString());
		file_rtf = replaceString(file_rtf, "date",
				mCursor.getString(mCursor.getColumnIndex("date")).toString());
		file_rtf = replaceString(file_rtf, "total",
				mCursor.getString(mCursor.getColumnIndex("total")).toString());
		file_rtf = replaceString(file_rtf, "zp",
				mCursor.getString(mCursor.getColumnIndex("zp")).toString());
		int i = 1;
		for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
				.moveToNext()) {
			file_rtf = replaceString(file_rtf, "sum" + i,
					mCursor.getString(mCursor.getColumnIndex("sum")).toString());
			i++;
		}

		Uri fileUri = Uri.parse("file:///"
				+ Tools.SaveStringToFile(file_rtf, getActivity(), file_name));
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
		sendIntent.setType("*/*");
		startActivity(Intent.createChooser(sendIntent, "Поделиться файлом"));

	}

	private void CheckHost() {
		new Thread(new Runnable() {
			public void run() {
				mSiteIsAvailable = Tools
						.isConnected(getString(R.string.host_to_send));
			}
		}).start();

	}

	private void SendAccount() {
		if (!Tools.isOnline(getActivity())) {
			Toast.makeText(getActivity(), "Подключите интернет",
					Toast.LENGTH_LONG).show();
			return;
		}
		//ConnectivityManager cm = (ConnectivityManager) getActivity()
		//		.getSystemService(Context.CONNECTIVITY_SERVICE);
		//NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

		// проверяем хост по активной сети
//		boolean isHostAlive = cm.requestRouteToHost(activeNetwork.getType(),
//				R.string.host_to_send);
//		if (!mSiteIsAvailable) {
//			Toast.makeText(getActivity(), "Сервер не доступен",
//					Toast.LENGTH_LONG).show();
//			return;
//		}

		// получить данные для отправки
		mCursor = Main.mDb.getListAccount(Main.mAccount_id);
		mCursor.moveToFirst();
		// проверяем sys_id на NULL
		String tmp = mCursor.getString(mCursor.getColumnIndex("sys_id"));

		if (tmp != null && tmp.length() != 0) {
			Toast.makeText(getActivity(), "Расчет уже отправлен на сервер",
					Toast.LENGTH_LONG).show();
			return;
		}
		mDataList.clear();
		mDataList.add(mCursor.getString(mCursor.getColumnIndex("name")));
		mDataList.add(mCursor.getString(mCursor.getColumnIndex("region")));
		mDataList.add(mCursor.getString(mCursor.getColumnIndex("date")));
		mDataList.add(mCursor.getString(mCursor.getColumnIndex("total")));
		mDataList.add(mCursor.getString(mCursor.getColumnIndex("zp")));
		for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
				.moveToNext()) {
			mDataList.add(mCursor.getString(mCursor.getColumnIndex("sum")));
		}
		SendDataTask sdt = new SendDataTask();
		sdt.ctx = getActivity();
		sdt.execute(mDataList.toArray(new String[mDataList.size()]));
		try {
			String sys_id = sdt.get();
			// sys_id.trim();
			sys_id = sys_id.replaceAll("\\n", "");
			Main.mDb.updSys_id(sys_id, String.valueOf(Main.mAccount_id));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private String save_View_ToSDCard(View share_view, String finalName)
			throws FileNotFoundException {
		File sdCard = Environment.getExternalStorageDirectory();
		File file = new File(sdCard, finalName);
		FileOutputStream fos = new FileOutputStream(file);
		share_view.setDrawingCacheEnabled(true);
		share_view.setLayoutParams(new RelativeLayout.LayoutParams(1000, 600));
		share_view.measure(
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		share_view.layout(0, 0, share_view.getMeasuredWidth(),
				share_view.getMeasuredHeight());
		Log.d("main",
				"save_View_ToSDCard height=" + share_view.getMeasuredHeight());
		Log.d("main",
				"save_View_ToSDCard width=" + share_view.getMeasuredWidth());

		share_view.buildDrawingCache(true);

		Bitmap b = Tools.getBitmapFromView(share_view);
		b.compress(CompressFormat.JPEG, 100, fos);
		share_view.setDrawingCacheEnabled(false); // clear drawing cache
		return file.toString();
	}

	boolean canShareText(boolean allowSmsMms, Intent intent) {
		List<ResolveInfo> list = getActivity().getPackageManager()
				.queryIntentActivities(intent, 0);
		if (list != null && list.size() > 0) {
			if (allowSmsMms)
				return true;
			int handlersCount = 0;
			for (ResolveInfo li : list) {
				if (li != null && li.activityInfo != null
						&& li.activityInfo.packageName != null
						&& li.activityInfo.packageName == "com.android.mms") {
				} else
					++handlersCount;
			}
			if (handlersCount > 0)
				return true;
		}
		return false;
	}

	private void ShareAccount() throws FileNotFoundException {
		LayoutInflater Inflater = getLayoutInflater(null);
		View share_view = Inflater.inflate(R.layout.ac_share_fragment, null,
				false);
		TextView tv_name = (TextView) share_view.findViewById(R.id.tv_name);
		tv_name.setText("Название расчета : " + en.getText());
		TextView tv_region = (TextView) share_view.findViewById(R.id.tv_region);
		tv_region.setText("Регион расчета : " + er.getText());
		TextView tv_date = (TextView) share_view.findViewById(R.id.tv_date);
		tv_date.setText("Дата расчета : " + ed.getText());
		TextView tv_total = (TextView) share_view.findViewById(R.id.tv_total);
		tv_total.setText("Общая сумма всех затрат : " + tv_ac_itog.getText());
		TextView tv_zp = (TextView) share_view.findViewById(R.id.tv_zp);
		tv_zp.setText("В пересчете на месяц : " + tv_ac_zp.getText());

		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		// Uri uri = Uri.fromFile(new File(save_View_ToSDCard(share_view,
		// tv_name
		// .getText().toString() + ".jpg")));
		Uri uri = Uri.parse("file:///"
				+ save_View_ToSDCard(share_view, tv_name.getText().toString()
						+ ".jpg"));
		share.putExtra(Intent.EXTRA_STREAM, uri);
		if (canShareText(true, share)) {
			startActivityForResult(
					Intent.createChooser(share, "Поделиться с помощью"), 0);
		} else {
			Toast.makeText(getActivity(),
					"Установите социальные приложения и повторите попытку",
					Toast.LENGTH_LONG).show();
		}

	}

	private void DeleteAccount() {
		Main.mDb.deleteAccount(String.valueOf(Main.mAccount_id));
		Toast.makeText(getActivity(), R.string.ac_delete_text,
				Toast.LENGTH_SHORT).show();
		getActivity().getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setTitle(R.string.title_activity_account, null);
	}

	public void setTitle(int titleId, CharSequence subtitle) {
		setTitle(getActivity().getString(titleId), subtitle);
	}

	public void setTitle(CharSequence title, CharSequence subtitle) {
		if (getActivity() instanceof ActionBarActivity) {
			ActionBar actionBar = ((ActionBarActivity) getActivity())
					.getSupportActionBar();
			actionBar.setTitle(title);
			actionBar.setSubtitle(subtitle);
		} else {
			getActivity().setTitle(title);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		CheckHost();
		rootView = inflater.inflate(R.layout.ac_fragment, container, false);
		en = (EditText) rootView.findViewById(R.id.editName);
		er = (EditText) rootView.findViewById(R.id.editRegion);
		ed = (EditText) rootView.findViewById(R.id.editDate);
		tv_ac_itog = (TextView) rootView.findViewById(R.id.tv_ac_itog);
		tv_ac_zp = (TextView) rootView.findViewById(R.id.tv_ac_zp);
		Button btn = (Button) rootView.findViewById(R.id.ac_list_button);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Main.mAccount_id == -1)
					Toast.makeText(getActivity(), R.string.acl_error,
							Toast.LENGTH_SHORT).show();
				else {
					Fragment f = Ac_list_fragment.getInstance();
					FragmentTransaction ft = getActivity()
							.getSupportFragmentManager().beginTransaction();
					ft.replace(R.id.content, f);
					ft.addToBackStack(null);
					ft.commit();
				}
			}
		});
		if (Main.mAccount_id == -1) {
			en.setText("");
			er.setText("");
			ed.setText("");
		} else {
			mCursor = Main.mDb.getAccount(Main.mAccount_id);
			mCursor.moveToFirst();
			String name = mCursor.getString(mCursor.getColumnIndex("name"));
			en.setText(name.toString());
			String region = mCursor.getString(mCursor.getColumnIndex("region"));
			er.setText(region.toString());
			String date = mCursor.getString(mCursor.getColumnIndex("date"));
			if (date != null) {
				ed.setText(date.toString());
			}
			Float ac_itog = mCursor.getFloat(mCursor.getColumnIndex("total"));
			Float ac_zp = mCursor.getFloat(mCursor.getColumnIndex("zp"));
			String s = "" + ac_itog.longValue();
			tv_ac_itog.setText(s);
			s = ac_zp.toString();
			tv_ac_zp.setText(s);
		}
		return rootView;
	}
}
