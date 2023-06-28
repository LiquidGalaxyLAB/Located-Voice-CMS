package com.gsoc.vedantsingh.locatedvoicecms;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;

import android.speech.RecognizerIntent;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gsoc.vedantsingh.locatedvoicecms.utils.LGUtils;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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


//    private ArrayList<String> backIDs = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private boolean logo_switch=true;
    Button SuggPOIButton, changeplanet, tourbutton;
    FloatingActionButton menufab, btnSpeak, buttonSearch;
    ImageView planetimg;
    EditText editSearch;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    TextView planetname;
    int numBack = 0;
    Bundle bundle = new Bundle();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.new_home);
//        changed layout from activity_lg to new_home
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SuggPOIButton=findViewById(R.id.suggpoibutton);
        changeplanet=findViewById(R.id.changeplanet);
        menufab=findViewById(R.id.menufab);
        tourbutton=findViewById(R.id.tourbutton);
        btnSpeak=findViewById(R.id.btnSpeak);
        buttonSearch=findViewById(R.id.searchButton);
        editSearch = findViewById(R.id.search_edittext);
        planetimg=findViewById(R.id.planetimg);
        planetname=findViewById(R.id.planetname);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenDensity = displayMetrics.densityDpi;

        if (screenDensity <= DisplayMetrics.DENSITY_MEDIUM) {
            // Small screen (low or medium density)
            menufab.setSize(FloatingActionButton.SIZE_MINI);
            btnSpeak.setSize(FloatingActionButton.SIZE_MINI);
            buttonSearch.setSize(FloatingActionButton.SIZE_MINI);
        } else {
            // Large screen (high density or above)
            menufab.setSize(FloatingActionButton.SIZE_NORMAL);
            btnSpeak.setSize(FloatingActionButton.SIZE_NORMAL);
            buttonSearch.setSize(FloatingActionButton.SIZE_NORMAL);
        }


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
        bundle.putString("currentplanet", "EARTH");
        SuggPOIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (bottomSheetDialog == null) {
//                    bottomSheetDialog = new BottomSheetDialog(LGPC.this, R.style.AppBottomSheetDialogTheme);
//                    View view = LayoutInflater.from(LGPC.this).inflate(R.layout.bottomsheetlayout, findViewById(R.id.bottomsheetll));
//                    bottomSheetDialog.setContentView(view);
//                }
//                bottomSheetDialog.show();
                SuggPOIBottomSheetFragment bottomSheetFragment = new SuggPOIBottomSheetFragment();
                bottomSheetFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppBottomSheetDialogTheme);
                bottomSheetFragment.setArguments(bundle);
                bottomSheetFragment.show(getSupportFragmentManager(), "bottom_sheet_tag");
            }
        });

        menufab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        changeplanet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlanetMenu();
            }
        });

        tourbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TourBottomSheetFragment bottomSheetFragment = new TourBottomSheetFragment();
                bottomSheetFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.TourAppBottomSheetDialogTheme);
                bottomSheetFragment.show(getSupportFragmentManager(), "bottom_sheet_tag2");
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

    public void showPlanetMenu() {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(LGPC.this, R.style.planetpopupBGStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, changeplanet); // Pass the context and the view that triggers the menu
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_planets, popupMenu.getMenu()); // Inflate your menu resource


        // Set a listener for menu item clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.earthid) {
                    bundle.putString("currentplanet", "EARTH");
                    SearchFragment fragment = new SearchFragment();
                    fragment.setArguments(bundle);
                    if(!planetimg.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.newearthimg).getConstantState())){
                        planetimg.setImageDrawable(getResources().getDrawable(R.drawable.newearthimg));
                        planetname.setText("Earth");
                    }
                    return true;
                } else if (itemId == R.id.moonid) {
                    bundle.putString("currentplanet", "MOON");
                    SearchFragment fragment = new SearchFragment();
                    fragment.setArguments(bundle);
                    if(!planetimg.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.newmoon).getConstantState())){
                        planetimg.setImageDrawable(getResources().getDrawable(R.drawable.newmoon));
                        planetname.setText("Moon");
                    }
                    return true;
                } else if (itemId == R.id.marsid) {
                    bundle.putString("currentplanet", "MARS");
                    SearchFragment fragment = new SearchFragment();
                    fragment.setArguments(bundle);
                    if(!planetimg.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.newmars).getConstantState())){
                        planetimg.setImageDrawable(getResources().getDrawable(R.drawable.newmars));
                        planetname.setText("Mars");
                    }
                    return true;
                } else {
                    return false;
                }
            }

        });

        // Show the popup menu
        popupMenu.show();
    }


    private void  showMenu() {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(LGPC.this, R.style.menupopupBGStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, findViewById(R.id.menufab));
        popupMenu.getMenuInflater().inflate(R.menu.menu_lgpc, popupMenu.getMenu());

        String machinesString = sharedPreferences.getString("Machines", "3");
        int machines = Integer.parseInt(machinesString);
        int slave_num = Math.floorDiv(machines, 2) + 2;
        String slave_name = "slave_" + slave_num;
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_information_help) {
                    Intent intent = new Intent(LGPC.this, InfoActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.action_showhidelogo){
                    if(logo_switch==true){
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.submit(new SetLogosTask(slave_name, session, LGPC.this));
                        executor.shutdown();
                        logo_switch=false;
                    }else{
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        CleanLogosTask cleanLogosTask = new CleanLogosTask(slave_name, session, LGPC.this);
                        Future<Void> future = executorService.submit(cleanLogosTask);
                        try {
                            future.get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        executorService.shutdown();
                        logo_switch=true;
                    }
                    return true;
                }else if (id == R.id.action_admin) {
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
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(LGPC.this,R.style.BlackTextAlertDialog);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // set the message to display
        alertbox.setMessage("Please, enter the password:");
        final EditText input = new EditText(LGPC.this);
        input.setHint("Password");
        input.setTextColor(Color.BLACK);
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
//                    if (ContextCompat.checkSelfPermission(LGPC.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                            && ContextCompat.checkSelfPermission(LGPC.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        // Permission is not granted, request it
//                        ActivityCompat.requestPermissions(LGPC.this,
//                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
//                                LOCATION_PERMISSION_REQUEST_CODE);
//                    } else {
//                        // Permission is already granted, proceed with your logic
//                        // ...
                        Intent intent = new Intent(LGPC.this, LGPCAdminActivity.class);
                        startActivity(intent);
//                    }

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

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                // Location permissions granted, proceed with your logic
//                // ...
//                Intent intent = new Intent(LGPC.this, LGPCAdminActivity.class);
//                startActivity(intent);
//            } else {
//                // Location permissions denied, handle accordingly (e.g., show an error message)
//                // ...
//            }
//        }
//    }

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

    public class SetLogosTask implements Runnable {
        private String slaveName;
        private Session session;
        private Context context;

        public SetLogosTask(String slaveName, Session session, Context context) {
            this.slaveName = slaveName;
            this.session = session;
            this.context = context;
        }

        @Override
        public void run() {
            try {
                Log.d("Set Logos", "SetLogosTask: Background task started");
                String sentence = "chmod 777 /var/www/html/kml/" + slaveName + ".kml; echo '" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\"\n" +
                        "xmlns:atom=\"http://www.w3.org/2005/Atom\" \n" +
                        " xmlns:gx=\"http://www.google.com/kml/ext/2.2\"> \n" +
                        " <Document>\n " +
                        " <Folder> \n" +
                        "<name>Logos</name> \n" +
                        "<ScreenOverlay>\n" +
                        "<name>Logo</name> \n" +
                        " <Icon> \n" +
                        "<href>https://raw.githubusercontent.com/vedantkingh/Located-Voice-CMS/master/app/src/main/res/drawable/logos.png</href> \n" +
                        " </Icon> \n" +
                        " <overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/> \n" +
                        " <screenXY x=\"0.02\" y=\"0.95\" xunits=\"fraction\" yunits=\"fraction\"/> \n" +
                        " <rotationXY x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/> \n" +
                        " <size x=\"0.6\" y=\"0.8\" xunits=\"fraction\" yunits=\"fraction\"/> \n" +
                        "</ScreenOverlay> \n" +
                        " </Folder> \n" +
                        " </Document> \n" +
                        " </kml>\n' > /var/www/html/kml/" + slaveName + ".kml";

                LGUtils.setConnectionWithLiquidGalaxy(session, sentence, context);
                Log.d("Set Logos", "Logos sent");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public class CleanLogosTask implements Callable<Void> {
        private String slaveName;
        private Session session;
        private Context context;

        public CleanLogosTask(String slaveName, Session session, Context context) {
            this.slaveName = slaveName;
            this.session = session;
            this.context = context;
        }

        @Override
        public Void call() throws Exception {
            try {
                Log.d("Clean Logos", "CleanLogosTask: Background task started");
                String sentence = "chmod 777 /var/www/html/kml/" + slaveName + ".kml; " +
                        "echo '' > /var/www/html/kml/"+ slaveName +".kml";

                LGUtils.setConnectionWithLiquidGalaxy(session, sentence, context);
                Log.d("Clean Logos","Logos cleaned");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
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