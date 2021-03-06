package ru.rpw_mos.zp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class My_accounts extends Fragment implements LoaderCallbacks<Cursor> {
    private ListView mListView;
    static SimpleCursorAdapter sca;
    static Loader<Cursor> loadermanager;
    // private boolean mSiteIsAvailable = false;
    int mAcType;
    static final String ACCOUNTS_TYPE = "ACCOUNTS_TYPE";
    // формируем столбцы сопоставления
    String[] from = new String[]{DbAdapter.COLUMN_NAME,
            DbAdapter.COLUMN_REGION, DbAdapter.COLUMN_DATE};
    int[] to = new int[]{R.id.name, R.id.region, R.id.date,};
    private static LoaderManager mngr;

    public static My_accounts getInstance(int AcType) {
        My_accounts f = new My_accounts();
        Bundle args = new Bundle();
        args.putInt(ACCOUNTS_TYPE, AcType);
        f.setArguments(args);
        f.setHasOptionsMenu(true);
        return f;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        if (mAcType == R.string.My_accounts)
            inflater.inflate(R.menu.my_ac_menu, menu);
        if (mAcType == R.string.Comrads_accounts)
            inflater.inflate(R.menu.comrads_ac_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_account:
                Main.mAccount_id = -1;
                ViewAccountFragment();
                return true;

            case R.id.download_ac:
                DownloadAccounts();
                return true;
        }
        return false;
    }

    private void DownloadAccounts() {
        if (!Tools.isOnline(getActivity())) {
            Toast.makeText(getActivity(), "Подключите интернет",
                    Toast.LENGTH_LONG).show();
            return;
        }

        GetAcListTask galt = new GetAcListTask();
        galt.ctx = getActivity();
        galt.execute(Main.mDb.getSysAcList());

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitle(mAcType, null);
    }

    public void setTitle(int titleId, CharSequence subtitle) {
        setTitle(getActivity().getString(titleId), subtitle);
    }

    public void setTitle(CharSequence title, CharSequence subtitle) {
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity())
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
        // CheckHost();
        mAcType = getArguments().getInt(ACCOUNTS_TYPE);
        // костыль!

        if (loadermanager != null && loadermanager.isReset()) {
            mngr = getLoaderManager();
            mngr.restartLoader(0, getArguments(), this);
        } else {
            mngr = getLoaderManager();
            mngr.initLoader(0, getArguments(), this);
        }

    }

    public static void UpdateAc() {
        mngr.getLoader(0).forceLoad();
    }

    private void ViewAccountFragment() {
        Fragment f = Ac_fragment.getInstance();
        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                .beginTransaction();
        ft.replace(R.id.content, f);
        ft.addToBackStack(null);
        ft.commit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.accounts_fragment, container,
                false);
        mListView = (ListView) rootView.findViewById(R.id.counts_list);
        mListView.setEmptyView(rootView.findViewById(R.id.empty));
        sca = new SimpleCursorAdapter(getActivity(), R.layout.ac_list_item,
                null, from, to, 0);
        mListView.setAdapter(sca);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int i, long l) {
                Main.mAccount_id = l;
                ViewAccountFragment();
            }
        });

        UpdateAc();
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new MyCursorLoader(getActivity(), Main.mDb, mAcType);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        sca.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }

}
