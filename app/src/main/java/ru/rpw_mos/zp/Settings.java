package ru.rpw_mos.zp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Settings extends Fragment {
    int mClickCount = 0;
    private ImageView mIv;

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
                new String[]{"rpw.mos@gmail.com"});
        sendMail.putExtra(Intent.EXTRA_SUBJECT, "zp");
        startActivity(Intent.createChooser(sendMail,
                getResources().getString(R.string.Choose_email_client)));
    }

    public void rotateImage() {
        Animation sampleFadeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
        sampleFadeAnimation.setRepeatCount(1);
        mIv.startAnimation(sampleFadeAnimation);
    }

    boolean canShareText(boolean allowSmsMms, Intent intent) {
        PackageManager manager = getActivity().getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(intent, 0);

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

    public void shareButtonClick() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.URL_to_vote));
        share.setType("text/plain");
        if (canShareText(true, share)) {
            startActivityForResult(Intent.createChooser(share, getString(R.string.Share_via)), 0);
        } else {
            Toast.makeText(getActivity(), R.string.Share_error, Toast.LENGTH_LONG)
                    .show();
        }

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
        mIv = (ImageView) rootView.findViewById(R.id.imageView1);
        mIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickCount++;
                if (mClickCount % 3 == 0)
                    rotateImage();
            }
        });
        Button shareButton = (Button) rootView.findViewById(R.id.Share_button_ac);
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                shareButtonClick();
            }
        });
        return rootView;
    }

}
