package ru.rpw_mos.zp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class Settings extends Fragment {

	public static Settings getInstance() {
		Settings f = new Settings();
		Bundle args = new Bundle();
		f.setArguments(args);
		return f;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setTitle(R.string.settings, null);
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

	public void Blog_View() {
		Uri address = Uri.parse("http://rpw-mos.ru/apps");
		Intent openlink = new Intent(Intent.ACTION_VIEW, address);
		startActivity(openlink);
	}

	public void Send_Email() {
		Intent sendMail = new Intent(Intent.ACTION_SEND);
		sendMail.setType("plain/text");
		sendMail.putExtra(Intent.EXTRA_EMAIL,
				new String[] { "rpw.mos@gmail.com" });
		sendMail.putExtra(Intent.EXTRA_SUBJECT, "zp");
		startActivity(Intent.createChooser(sendMail,
				getResources().getString(R.string.Choose_email_client)));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.about_fragment, container,
				false);
		TextView tv_send_email = (TextView) rootView
				.findViewById(R.id.Send_Email);
		tv_send_email.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Send_Email();

			}
		});
		TextView tv_Blog_View = (TextView) rootView
				.findViewById(R.id.Blog_View);
		tv_Blog_View.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Blog_View();
			}
		});

		return rootView;
	}

}
