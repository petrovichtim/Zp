package com.rusdelphi.zp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class Ac_list_fragment extends Fragment implements
		LoaderCallbacks<Cursor> {
	
	private ListView mListView;
	static Loader<Cursor> loadermanager;
	MySimpleCursorAdapter sca;
	private static final String[] FROM = new String[] { "name", };
	private static final int[] TO = new int[] { R.id.name };

	public static Ac_list_fragment getInstance() {
		Ac_list_fragment f = new Ac_list_fragment();
		Bundle args = new Bundle();
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadermanager = getActivity().getSupportLoaderManager().restartLoader(
				0, null, this);
	}

	public static void UpdateAc() {
		loadermanager.forceLoad();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setTitle(R.string.acl_text, null);
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
		View rootView = inflater.inflate(R.layout.accounts_list_fragment,
				container, false);
		mListView = (ListView) rootView.findViewById(R.id.counts_list);
		sca = new MySimpleCursorAdapter(getActivity(),
				R.layout.exp_list_item_fragment, null, FROM, TO, 0);

		mListView.setAdapter(sca);
		// mListView.setOnItemClickListener(new
		// AdapterView.OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> adapterView, View view,
		// int i, long l) {
		// Main.mAccount_id = l;
		// ViewAccountFragment();
		// }
		//
		// });
		UpdateAc();
		return rootView;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new MyCursorLoader(getActivity(), Main.mDb, R.string.ac_list);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		sca.swapCursor(cursor);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}
}
