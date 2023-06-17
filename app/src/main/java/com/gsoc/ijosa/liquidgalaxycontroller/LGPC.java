package com.gsoc.ijosa.liquidgalaxycontroller;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;

import android.speech.RecognizerIntent;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gsoc.ijosa.liquidgalaxycontroller.utils.LGUtils;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/*This is the MAIN Activity, the first that appears when the application is opened. On the
* bar there are some Tabs corresponding on some different contents.*/
public class LGPC extends AppCompatActivity implements ActionBar.TabListener {

    //Required for kioskMode
    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    CollectionPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Session session;

    private final int REQ_CODE_SPEECH_INPUT = 100;


    private ArrayList<String> backIDs = new ArrayList<>();
    Button SuggPOIButton;
    FloatingActionButton menufab,btnSpeak,buttonSearch;
    EditText editSearch;
    int numBack = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.new_home);
//        changed layout from activity_lg to new_home
        SuggPOIButton=findViewById(R.id.suggpoibutton);
        menufab=findViewById(R.id.menufab);
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        btnSpeak=findViewById(R.id.btnSpeak);
        buttonSearch=findViewById(R.id.searchButton);
        editSearch = findViewById(R.id.search_edittext);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LGPC.GetSessionTask getSessionTask = new GetSessionTask(LGPC.this);
                getSessionTask.execute();

                String placeToSearch = editSearch.getText().toString();
                if (!placeToSearch.equals("") && placeToSearch != null) {

                    String command = "echo 'search=" + placeToSearch + "' > /tmp/query.txt";
                    SearchTask searchTask = new SearchTask(LGPC.this, command, false);
                    searchTask.execute();

                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_enter_search), Toast.LENGTH_LONG).show();
                }
            }
        });

        SuggPOIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }
        });

        menufab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });



        //////////////////////////////////////////////////////////////////////////////////
//        // Set up the action bar.
//        final ActionBar actionBar = getSupportActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//
//        if (Build.VERSION.SDK_INT >= 23) {
//            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_action_bar));
//        }
//
//
//
//        // Create the adapter that will return a fragment for each of the three
//        // primary sections of the activity.
//        mSectionsPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
//
//        // Set up the ViewPager with the sections adapter.
//        mViewPager = (ViewPager) findViewById(R.id.pager);
//        mViewPager.setAdapter(mSectionsPagerAdapter);
//
//        // When swiping between different sections, select the corresponding
//        // tab. We can also use ActionBar.Tab#select() to do this if we have
//        // a reference to the Tab.
//        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
//            }
//        });
//
//        // For each of the sections in the app, add a tab to the action bar.
//        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//            // Create a tab with text corresponding to the page title defined by
//            // the adapter. Also specify this Activity object, which implements
//            // the TabListener interface, as the callback (listener) for when
//            // this tab is selected.
//            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
//                            .setTabListener(this));
//        }
//
//        showLogo();
    }


    private void showLogo() {

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.lg_logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.menufab));
        popupMenu.getMenuInflater().inflate(R.menu.menu_lgpc, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_information_help) {
                    Intent intent = new Intent(LGPC.this, InfoActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.action_admin) {
                    if (!POISFragment.getTourState()) {
                        showPasswordAlert();
                    } else {
                        showAlert();
                    }
                    return true;
                } else if (id == R.id.action_about) {
                    showAboutDialog();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lgpc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_information_help){
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_admin) {
            //When the user decides to enter to the Administration section, first appears one
            //popup asking for a password.
            if(!POISFragment.getTourState()) {
                showPasswordAlert();
            }else{
                showAlert();
            }
        } else if (id == R.id.action_about) {
            showAboutDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        final Dialog dialog = new Dialog(this);
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

    private void showAlert(){
        // prepare the alert box
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(LGPC.this);

        // set the message to display
        alertbox.setMessage("Please, first stop the Tour.");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });
        // display box
        alertbox.show();
    }

    private void showPasswordAlert(){
        // prepare the alert box
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(LGPC.this);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // set the message to display
        alertbox.setMessage("Please, enter the password:");
        final EditText input = new EditText(LGPC.this);
        input.setHint("Password");
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertbox.setView(input);

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {

                String pass = input.getText().toString();
                String correct_pass = prefs.getString("AdminPassword", "lg");
                if(pass.equals(correct_pass)){
                    Intent intent = new Intent(LGPC.this, LGPCAdminActivity.class);
                    startActivity(intent);
                }else{
                    incorrectPasswordAlertMessage();
                }
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        // display box
        alertbox.show();
    }

    private void incorrectPasswordAlertMessage() {
        // prepare the alert box
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(LGPC.this);

        // set the message to display
        alertbox.setTitle("Error");
        alertbox.setMessage("Incorrect password. Please, try it again or cancel the operation.");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton("Retry", new DialogInterface.OnClickListener() {

            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
                showPasswordAlert();
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        // display box
        alertbox.show();
    }

    private void promptSpeechInput() {

        Locale spanish = new Locale("es", "ES");
        Locale catalan = new Locale("ca", "ES");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, catalan);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, catalan);
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, catalan);
        intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, spanish);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, spanish);
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, spanish);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class GetSessionTask extends AsyncTask<Void, Void, Void> {

        private final Activity activity;

        public GetSessionTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (getApplicationContext() != null) {
                session = LGUtils.getSession(activity);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void success) {
            super.onPostExecute(success);
        }
    }

    private class SearchTask extends AsyncTask<Void, Void, String> {

        String command;
        boolean isChangingPlanet;
        private ProgressDialog dialog;
        private final Context context;

        public SearchTask(Context context, String command, boolean isChangingPlanet) {
            this.context= context;
            this.command = command;
            this.isChangingPlanet = isChangingPlanet;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(context);
                if (isChangingPlanet) {
                    dialog.setMessage(getResources().getString(R.string.changingPlanet));
                } else {
                    dialog.setMessage(getResources().getString(R.string.searching));
                }
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
                dialog.show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return LGUtils.setConnectionWithLiquidGalaxy(session, command, context);
            } catch (JSchException e) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String success) {
            super.onPostExecute(success);
            if (success != null) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            } else {
                Toast.makeText(context, getResources().getString(R.string.connection_failure), Toast.LENGTH_LONG).show();
            }
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onBackPressed() {
        //Required for kioskMode
        numBack++;
        if (numBack == 4) {
            finish();
            System.exit(0);
        }
    }

    //Required for kioskMode
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (!hasFocus) {
//            // Close every kind of system dialog
//            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//            sendBroadcast(closeDialog);
//        }
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return blockedKeys.contains(event.getKeyCode()) || super.dispatchKeyEvent(event);
    }

}