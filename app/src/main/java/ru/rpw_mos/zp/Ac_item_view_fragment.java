package ru.rpw_mos.zp;

import java.text.NumberFormat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Ac_item_view_fragment extends Fragment {

	public static Ac_item_view_fragment getInstance() {
		Ac_item_view_fragment f = new Ac_item_view_fragment();
		Bundle args = new Bundle();
		f.setArguments(args);
		f.setHasOptionsMenu(true);
		return f;
	}

	private View rootView;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.ac_exp_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.save_exp:
			SaveExp();
			return true;
		}

		return false;
	}

	private void SaveExp() {
		EditText es = (EditText) rootView.findViewById(R.id.editSum);
		TextView tvM = (TextView) rootView.findViewById(R.id.tv_Month);
		float sum = 0;
		try {
			sum = Float.valueOf(es.getText().toString());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		if (Main.mEx_id == -1) {
			Main.mDb.insertExp("" + Main.mAccount_id, "" + Main.mExpenses_id,
					es.getText().toString());
		} else {
			Main.mDb.updateExp(es.getText().toString(), "" + Main.mEx_id);
		}
		sum = sum / 300;
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMaximumFractionDigits(2);
		String s = formatter.format(sum);
		tvM.setText(s);
		Toast.makeText(getActivity(), R.string.ac_save_text, Toast.LENGTH_SHORT)
				.show();
		Tools.hideSoftKeyboard(getActivity());
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setTitle(Main.Name_item, null);
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
		rootView = inflater.inflate(R.layout.exp_item_view_fragment, container,
				false);
		TextView tv = (TextView) rootView.findViewById(R.id.tv_Description);
		tv.setText(Main.Desc_item);
		EditText es = (EditText) rootView.findViewById(R.id.editSum);
		es.setText(Main.Sum_item);
		TextView tvM = (TextView) rootView.findViewById(R.id.tv_Month);
		float sum = 0;
		try {
			sum = Float.valueOf(Main.Sum_item);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		sum = sum / 300;
		NumberFormat formatter = NumberFormat.getNumberInstance();
		formatter.setMaximumFractionDigits(2);
		String s = formatter.format(sum);
		tvM.setText(s);
		return rootView;
	}

}
