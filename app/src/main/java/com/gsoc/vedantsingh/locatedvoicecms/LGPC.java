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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gsoc.vedantsingh.locatedvoicecms.utils.LGUtils;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

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
    ViewPager mViewPager;
    Session session = null;

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private static final int REQUEST_PERMISSION_CODE = 123;
    private SharedPreferences sharedPreferences;
    private boolean logo_switch=true;
    Button SuggPOIButton, changeplanet, tourbutton;
    FloatingActionButton menufab, btnSpeak, buttonSearch;
    ImageView planetimg, sshConnDot, aiConnDot;
    EditText editSearch;
    TextView planetname, sshConnText, aiConnText;
    public static boolean LG_CONNECTION = false;
    public static boolean AI_SERVER_CONNECTION = false;
    int numBack = 0;
    Bundle bundle = new Bundle();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.new_home);
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

        sshConnDot = findViewById(R.id.ssh_conn_dot);
        sshConnText = findViewById(R.id.ssh_conn_text);
        aiConnDot = findViewById(R.id.ai_conn_dot);
        aiConnText = findViewById(R.id.ai_conn_text);

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
                SuggPOIBottomSheetFragment bottomSheetFragment = new SuggPOIBottomSheetFragment();
                bottomSheetFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.AppBottomSheetDialogTheme);
                bottomSheetFragment.setArguments(bundle);
                bottomSheetFragment.show(getSupportFragmentManager(), "bottom_sheet_tag");
            }
        });

        showMenu();
        showPlanetMenu();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                CheckConnectionStatus checkConnectionStatus = new CheckConnectionStatus(session, LGPC.this);
                Future<Void> future = executorService.submit(checkConnectionStatus);
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                executorService.shutdown();
            }
        }, 1000);

        tourbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TourBottomSheetFragment bottomSheetFragment = new TourBottomSheetFragment();
                bottomSheetFragment.setStyle(DialogFragment.STYLE_NORMAL,R.style.TourAppBottomSheetDialogTheme);
                bottomSheetFragment.show(getSupportFragmentManager(), "bottom_sheet_tag2");
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android version is 10 or higher
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("permissionGranted", true);
            editor.apply();
        } else {
            // Check if the permission is already granted
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_PERMISSION_CODE);
                Log.d("Permission Storage", "part 1");
            } else {
                // Permission is already granted, proceed with your operations
                Log.d("Permission Storage", "part 2");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with your operations
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("permissionGranted", true);
                editor.apply();
            } else {
                // Permission is denied, handle accordingly (e.g., show an error message or disable features that require the permission)
                sharedPreferences.getBoolean("permissionGranted", false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CheckConnectionStatus checkConnectionStatus = new CheckConnectionStatus(session, LGPC.this);
        Future<Void> future = executorService.submit(checkConnectionStatus);
        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    public class CheckConnectionStatus implements Callable<Void> {
        private Session session1;
        private Context context;

        public CheckConnectionStatus(Session session1, Context context) {
            this.session1 = session1;
            this.context = context;
        }

        @Override
        public Void call() throws Exception {
            try {
                session1 = LGUtils.checkConnectionStatus(session1, context);
                if(session1 == null || !session1.isConnected()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sshConnDot.setColorFilter(getResources().getColor(R.color.red));
                            sshConnText.setTextColor(getResources().getColor(R.color.red));
                            sshConnText.setText("LG Disconnected");
                            LG_CONNECTION = false;
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sshConnDot.setColorFilter(getResources().getColor(R.color.green));
                            sshConnText.setTextColor(getResources().getColor(R.color.green));
                            sshConnText.setText("LG Connected");
                            LG_CONNECTION = true;
                        }
                    });
                    AI_SERVER_CONNECTION = LGUtils.checkAIServerConnection(session1, context);
                    if(AI_SERVER_CONNECTION){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                aiConnDot.setColorFilter(getResources().getColor(R.color.green));
                                aiConnText.setTextColor(getResources().getColor(R.color.green));
                                aiConnText.setText("AI Server Connected");
                                AI_SERVER_CONNECTION = true;
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                aiConnDot.setColorFilter(getResources().getColor(R.color.red));
                                aiConnText.setTextColor(getResources().getColor(R.color.red));
                                aiConnText.setText("AI Server Disconnected");
                                AI_SERVER_CONNECTION = false;
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void showPlanetMenu(){
        List<PowerMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new PowerMenuItem("EARTH"));
        menuItems.add(new PowerMenuItem("MOON"));
        menuItems.add(new PowerMenuItem("MARS"));

        PowerMenu powerMenu = new PowerMenu.Builder(this)
                .addItemList(menuItems)
                .setAnimation(MenuAnimation.SHOWUP_BOTTOM_LEFT)
                .setMenuRadius(20f)
                .setTextSize(14)
                .setAutoDismiss(true)
                .setTextColor(ContextCompat.getColor(this, R.color.offwhite))
                .setMenuColor(ContextCompat.getColor(this, R.color.lg_black))
                .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int position, PowerMenuItem item) {
                        switch (position) {
                            case 0:
                                bundle.putString("currentplanet", "EARTH");
                                SearchFragment earthFragment = new SearchFragment();
                                earthFragment.setArguments(bundle);
                                if(!planetimg.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.newearthimg).getConstantState())){
                                    planetimg.setImageDrawable(getResources().getDrawable(R.drawable.newearthimg));
                                    planetname.setText("Earth");
                                    if(LG_CONNECTION) {
                                        String command = "echo 'planet=earth' > /tmp/query.txt";
                                        SearchTask searchTask = new SearchTask(LGPC.this, command, true);
                                        searchTask.execute();
                                    }
                                }
                                break;
                            case 1:
                                bundle.putString("currentplanet", "MOON");
                                SearchFragment moonFragment = new SearchFragment();
                                moonFragment.setArguments(bundle);
                                if(!planetimg.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.newmoon).getConstantState())){
                                    planetimg.setImageDrawable(getResources().getDrawable(R.drawable.newmoon));
                                    planetname.setText("Moon");
                                    if(LG_CONNECTION){
                                        String command = "echo 'planet=moon' > /tmp/query.txt";
                                        SearchTask searchTask = new SearchTask(LGPC.this, command, true);
                                        searchTask.execute();
                                    }
                                }
                                break;
                            case 2:
                                bundle.putString("currentplanet", "MARS");
                                SearchFragment marsFragment = new SearchFragment();
                                marsFragment.setArguments(bundle);
                                if(!planetimg.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.newmars).getConstantState())){
                                    planetimg.setImageDrawable(getResources().getDrawable(R.drawable.newmars));
                                    planetname.setText("Mars");
                                    if(LG_CONNECTION) {
                                        String command = "echo 'planet=mars' > /tmp/query.txt";
                                        SearchTask searchTask = new SearchTask(LGPC.this, command, true);
                                        searchTask.execute();
                                    }
                                }
                                break;
                        }
                    }
                })
                .build();

        findViewById(R.id.changeplanet).setOnClickListener(powerMenu::showAsAnchorLeftTop);
    }

    private void showMenu() {
        List<PowerMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new PowerMenuItem("Administration Tools"));
        menuItems.add(new PowerMenuItem("Show/Hide Logo"));
        menuItems.add(new PowerMenuItem("Help"));
        menuItems.add(new PowerMenuItem("About"));

        PowerMenu powerMenu = new PowerMenu.Builder(this)
                .addItemList(menuItems)
                .setMenuRadius(20f)
                .setTextSize(13)
                .setWidth(375)
                .setAutoDismiss(true)
                .setTextColor(ContextCompat.getColor(this, R.color.offwhite))
                .setMenuColor(ContextCompat.getColor(this, R.color.lg_black))
                .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int position, PowerMenuItem item) {
                        String machinesString = sharedPreferences.getString("Machines", "3");
                        int machines = Integer.parseInt(machinesString);
                        int slave_num = Math.floorDiv(machines, 2) + 2;
                        String slave_name = "slave_" + slave_num;
                        switch (position) {
                            case 0:
                                if (!POISFragment.getTourState()) {
                                    showPasswordAlert();
                                } else {
                                    showAlert();
                                }
                                break;
                            case 1:
                                if (logo_switch) {
                                    ExecutorService executor = Executors.newSingleThreadExecutor();
                                    executor.submit(new SetLogosTask(slave_name, session, LGPC.this));
                                    executor.shutdown();
                                    logo_switch = false;
                                } else {
                                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                                    CleanLogosTask cleanLogosTask = new CleanLogosTask(slave_name, session, LGPC.this);
                                    Future<Void> future = executorService.submit(cleanLogosTask);
                                    try {
                                        future.get();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    executorService.shutdown();
                                    logo_switch = true;
                                }
                                break;
                            case 2:
                                Intent intent = new Intent(LGPC.this, InfoActivity.class);
                                startActivity(intent);
                                break;
                            case 3:
                                showAboutDialog();
                                break;
                        }
                    }
                })
                .build();

        findViewById(R.id.menufab).setOnClickListener(powerMenu::showAsDropDown);
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
        LayoutInflater inflater = LayoutInflater.from(LGPC.this);
        View customView = inflater.inflate(R.layout.password_alert, null);
        final EditText input = customView.findViewById(R.id.passwordInput);
        final ImageButton togglePasswordVisibilityButton = customView.findViewById(R.id.togglePasswordVisibility);

        // Set the transformation method for the password EditText
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // Set the click listener for the eye button to toggle password visibility
        togglePasswordVisibilityButton.setOnClickListener(new View.OnClickListener() {
            private boolean passwordVisible = false;

            @Override
            public void onClick(View v) {
                passwordVisible = !passwordVisible;
                if (passwordVisible) {
                    // Show password
                    input.setTransformationMethod(null);
                    togglePasswordVisibilityButton.setImageResource(R.drawable.eye_hide_pw);
                } else {
                    // Hide password
                    input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    togglePasswordVisibilityButton.setImageResource(R.drawable.eye_show_pw);
                }
                // Move the cursor to the end of the text
                input.setSelection(input.getText().length());
            }
        });

        // Build the alert dialog
        final AlertDialog.Builder alertbox = new AlertDialog.Builder(LGPC.this, R.style.BlackTextAlertDialog);
        alertbox.setMessage("Please, enter the password:");
        alertbox.setView(customView);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        alertbox.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
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

        alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                // Your existing code for cancel button
                // ...
            }
        });

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
                dialog = new ProgressDialog(context, R.style.CustomProgressDialog);
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
                int factor = 400 * (6190/6054);
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
                        " <size x=\"400\" y=\"" + factor + "\" xunits=\"pixels\" yunits=\"pixels\"/> \n" +
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