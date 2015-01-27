package ru.rpw_mos.zp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Info extends Fragment {
	static final String INFO_TYPE = "INFO_TYPE";
	private int mInfoType;

	public static Info getInstance(int InfoType) {
		Info f = new Info();
		Bundle args = new Bundle();
		args.putInt(INFO_TYPE, InfoType);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setTitle(mInfoType, null);
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInfoType = getArguments().getInt(INFO_TYPE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = null;
		if (mInfoType == R.string.theory)
			rootView = inflater.inflate(R.layout.theory_fragment, container,
					false);
		if (mInfoType == R.string.practice)
			rootView = inflater.inflate(R.layout.practice_fragment, container,
					false);
		return rootView;
	}

}
