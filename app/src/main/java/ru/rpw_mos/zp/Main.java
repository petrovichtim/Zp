package ru.rpw_mos.zp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main extends AppCompatActivity implements
        AdapterView.OnItemClickListener {

    private static final String PAGE = "page";
    public static final boolean mDBisUnzipped = false;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private static int page = 0;
    private Fragment pendingFragment = My_accounts
            .getInstance(R.string.My_accounts);
    static DbAdapter mDb;
    static ArrayList<HashMap<String, String>> accountsList;
    public boolean mDraverCliked;
    static Context ctx;

    public static long mAccount_id;
    public static long mExpenses_id = -1;
    public static long mEx_id = -1;
    public static String Name_item;
    public static String Desc_item;
    public static String Sum_item;

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.main);
        ctx = this;
        accountsList = new ArrayList<HashMap<String, String>>();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
                .getColor(R.color.header_bg)));
        actionBar.setIcon(R.drawable.menu);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setOnItemClickListener(this);
        drawerList.setAdapter(new MenuAdapter(this, R.layout.drawer_list_item,
                R.array.menu_items, R.array.menu_icons));
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.ns_menu_open, R.string.ns_menu_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mDraverCliked)
                    loadFragment();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mDraverCliked = false;
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        loadSettings();
        if (mDb == null)
            mDb = new DbAdapter(this).open();
        loadFragment();
    }

    private void loadSettings() {
        // prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        // mDBisDownloaded = prefs.getBoolean(LoadFullBaseTask.DB_IS_DOWNLOADED,
        // false);
        // mDBisUnzipped = prefs.getBoolean(UnzipTask.DB_IS_UNZIPPED, false);
        // mKey = prefs.getString(LoadFullBaseTask.UPGRADE_KEY, null);
    }

    private void loadFragment() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (page == 40)// добавляем фрагмент поверх предыдущего
                {
                    FragmentTransaction ft = getSupportFragmentManager()
                            .beginTransaction();
                    ft.replace(R.id.content, pendingFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                } else { // добавляем фрагмент в корень стека
                    if (!pendingFragment.isAdded()) {
                        getSupportFragmentManager().popBackStack(null,
                                FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        FragmentTransaction ft = getSupportFragmentManager()
                                .beginTransaction();
                        ft.replace(R.id.content, pendingFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                }
            }
        }).start();

    }

    public static void CreateListDialog() {

        // array list

        ArrayList<String> checkAcListName = new ArrayList<>();
        final ArrayList<String> checkAcListID = new ArrayList<>();

        for (HashMap<String, String> hash : accountsList) {
            checkAcListName.add(hash.get("name"));
            checkAcListID.add(hash.get("id"));

        }

        final String[] checkAcName = checkAcListName
                .toArray(new String[checkAcListName.size()]);
        final boolean[] mCheckedItems = new boolean[checkAcName.length];
        Arrays.fill(mCheckedItems, false);// заполнили массив пустыми значениями

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Выберите расчет для загрузки")
                .setCancelable(true)
                .setPositiveButton("Скачать",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                int i = 0;
                                ArrayList<String> ListID = new ArrayList<String>();
                                for (boolean ChechId : mCheckedItems) {
                                    if (ChechId)
                                        ListID.add(checkAcListID.get(i)
                                        );
                                    i++;
                                }
                                if (ListID.size() > 0) {
                                    GetAcTask gat = new GetAcTask();
                                    gat.ctx = ctx;
                                    gat.execute(ListID.toString());// toArray(new
                                    // String[ListID
                                    // .size()]));
                                } else
                                    Toast.makeText(ctx,
                                            "Не выбран ни один расчёт",
                                            Toast.LENGTH_LONG).show();
                                Log.d("main", ListID.toString());
                                dialog.cancel();
                            }
                        })
                .setNeutralButton("Все", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        GetAcTask gat = new GetAcTask();
                        gat.ctx = ctx;
                        gat.execute(checkAcListID.toString());
                        // .toArray(new String[checkAcListID.size()]));
                        Log.d("main", checkAcListID.toString());
                        dialog.cancel();
                    }
                })
                .setMultiChoiceItems(checkAcName, mCheckedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {
                                mCheckedItems[which] = isChecked;
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        builder.create();
        builder.show();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        page = position;
        mDraverCliked = true;
        Tools.hideSoftKeyboard(this);
        switch (position) {
            case 0:
                pendingFragment = My_accounts.getInstance(R.string.My_accounts);
                break;
            case 1:
                pendingFragment = My_accounts
                        .getInstance(R.string.Comrads_accounts);
                break;
            case 2:
                pendingFragment = Info.getInstance(R.string.theory);
                break;
            case 3:
                pendingFragment = Info.getInstance(R.string.practice);
                break;
            case 4:
                pendingFragment = Settings.getInstance();
                break;
        }
        drawerLayout.closeDrawers();
    }

    @Override
    protected void onDestroy() {
        if (mDb != null)
            mDb.close();
        mDb = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStackImmediate();
            // removeCurrentFragment();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Вы действительно хотите покинуть программу?")
                    .setCancelable(true)
                    .setPositiveButton("Да",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    finish();
                                }
                            }).setNegativeButton("Нет", null).show();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration cfg) {
        super.onConfigurationChanged(cfg);
        drawerToggle.onConfigurationChanged(cfg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE, page);
    }

}
