package com.gsoc.vedantsingh.locatedvoicecms;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBar.Tab;
import androidx.appcompat.app.ActionBar.TabListener;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.gsoc.vedantsingh.locatedvoicecms.data.POIsDbHelper;
import com.gsoc.vedantsingh.locatedvoicecms.data.POIsProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;

public class LGPCAdminActivity extends AppCompatActivity implements TabListener {
    AdminCollectionPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    private BottomNavigationView bottomNavigationView;

    PendingIntent intent;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lgpcadmin);

        mSectionsPagerAdapter = new AdminCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.pager_admin);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int position = 0;
            if (item.getItemId() == R.id.menu_treeview) {
                position = AdminCollectionPagerAdapter.PAGE_TREEEVIEW;
            } else if (item.getItemId() == R.id.menu_tours) {
                position = AdminCollectionPagerAdapter.PAGE_TOURS;
            } else if (item.getItemId() == R.id.menu_tools) {
                position = AdminCollectionPagerAdapter.PAGE_TOOLS;
//            } else if (item.getItemId() == R.id.menu_tasks) {
//                position = AdminCollectionPagerAdapter.PAGE_TASKS;
//            } else if (item.getItemId() == R.id.menu_beacons) {
//                position = AdminCollectionPagerAdapter.PAGE_BEACONS;
            }

            mViewPager.setCurrentItem(position);
            return true;
        });


        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });

        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("comeFrom");
            if (value != null && value.equalsIgnoreCase("tours")) {
                mViewPager.setCurrentItem(AdminCollectionPagerAdapter.PAGE_TOURS);
            } else if (value != null && value.equalsIgnoreCase("treeView")) {
                mViewPager.setCurrentItem(AdminCollectionPagerAdapter.PAGE_TREEEVIEW);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lgpcadmin, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, LGPC.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.reset_db) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getResources().getString(R.string.are_you_sure_delete_database));

            alert.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    resetDatabase();
                }
            });

            alert.setNegativeButton(getResources().getString(R.string.no),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
            alert.show();


            return true;
        } else if (id == R.id.export_db) {
            exportDatabase();
            return true;
        } else if (id == R.id.action_information_help) {
            startActivity(new Intent(this, Help.class));
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id != R.id.log_out) {
            return super.onOptionsItemSelected(item);
        } else {
            startActivity(new Intent(this, LGPC.class));
            return true;
        }
    }

    private void showAboutDialog() {
        final Dialog dialog = new Dialog(this, R.style.BlackTextAlertDialog);
        dialog.setContentView(R.layout.about_dialog);
        dialog.setTitle(getResources().getString(R.string.about_Controller_message));

        Button dialogButton = (Button) dialog.findViewById(R.id.aboutDialogButtonOK);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void exportDatabase() {
        Log.i("INFO", "EXPORTING DATABASE");
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();
            if (sd.canWrite()) {
                Calendar c = Calendar.getInstance();
                String dayAndMonth = c.get(Calendar.DAY_OF_MONTH) + "_" + (c.get(Calendar.MONTH) + 1) + "_" + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE);

                String currentDBPath = "/data/" + this.getPackageName() + "/databases/" + POIsDbHelper.DATABASE_NAME;
                String backupDBPath = "DB_" + dayAndMonth + ".sqlite";


                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    Log.i("INFO", backupDB.getAbsolutePath());
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
            Log.i("INFO", "DATABASE EXPORTED");
        } catch (Exception e) {
            Log.e("ERROR", "EXPORTING DATABASE ERROR" + e.getCause());

        }
    }

    public void resetDatabase() {
        ContentResolver resolver = getApplicationContext().getContentResolver();
        ContentProviderClient client = resolver.acquireContentProviderClient(this.getPackageName());
        POIsProvider provider = (POIsProvider) client.getLocalContentProvider();
        provider.resetDatabase();
        client.release();
        resetApp();
    }

    public void resetApp() {

        AlarmManager alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alm.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE));
        System.exit(0);
    }

    public void onTabSelected(Tab tab, FragmentTransaction fragmentTransaction) {
        this.mViewPager.setCurrentItem(tab.getPosition());
    }

    public void onTabUnselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public void onTabReselected(Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /* renamed from: com.gsoc.ijosa.liquidgalaxycontroller.LGPCAdminActivity.1 */
    class C02741 extends SimpleOnPageChangeListener {
        final /* synthetic */ ActionBar val$actionBar;

        C02741(ActionBar actionBar) {
            this.val$actionBar = actionBar;
        }

        public void onPageSelected(int position) {
            this.val$actionBar.setSelectedNavigationItem(position);
        }
    }
}
