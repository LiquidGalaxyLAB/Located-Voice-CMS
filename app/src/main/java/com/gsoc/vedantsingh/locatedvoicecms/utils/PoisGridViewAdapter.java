package com.gsoc.vedantsingh.locatedvoicecms.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.gsoc.vedantsingh.locatedvoicecms.R;
import com.gsoc.vedantsingh.locatedvoicecms.SearchFragment;
import com.gsoc.vedantsingh.locatedvoicecms.WikipediaPageResponse;
import com.gsoc.vedantsingh.locatedvoicecms.beans.POI;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


/**
 * Created by Ivan Josa on 7/07/16.
 */
public class PoisGridViewAdapter extends BaseAdapter {

    private List<POI> poiList;
    private Context context;
    private Activity activity;
    private Session session;
    private SignInListener signInListener;

    interface WikiPediaPageService{
        @GET("/w/rest.php/v1/search/title")
        Call<WikipediaPageResponse> getQuery(@Query("q") String query, @Query("limit") int limit);
    }


    public PoisGridViewAdapter(List<POI> poiList, Context context, Activity activity, SignInListener listener) {
        this.poiList = poiList;
        this.context = context;
        this.activity = activity;
        this.signInListener = listener;

        GetSessionTask getSessionTask = new GetSessionTask();
        getSessionTask.execute();
    }

    @Override
    public int getCount() {
        return this.poiList.size();
    }

    @Override
    public Object getItem(int i) {
        return this.poiList.get(i);

    }

    @Override
    public long getItemId(int i) {
        return this.poiList.get(i).getId();
    }

    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        final POI currentPoi = this.poiList.get(i);

        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);

        RelativeLayout layout = new RelativeLayout(context);
        layout.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.button_rounded_grey, null));
        layout.setLayoutParams(params);

        //Rotation Button
        RelativeLayout.LayoutParams paramsRotate = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageButton rotatePoiButton = new ImageButton(context);
        rotatePoiButton.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.button_rounded_grey, null));
        paramsRotate.addRule(RelativeLayout.CENTER_VERTICAL);
        paramsRotate.addRule(RelativeLayout.ALIGN_PARENT_END);

        rotatePoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = buildCommand(currentPoi);
                Log.d("Rotate","button");
                SearchFragment.recentPOI = currentPoi.getName();
                VisitPoiTask visitPoiTask = new VisitPoiTask(command, currentPoi, true);
                visitPoiTask.execute();
            }
        });

        rotatePoiButton.setEnabled(false);
        rotatePoiButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_autorenew_white_36dp, null));

//        AI Context and AI Voice Button
        RelativeLayout.LayoutParams paramsView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsView.addRule(RelativeLayout.CENTER_VERTICAL);
        paramsView.addRule(RelativeLayout.ALIGN_START);

        ImageButton AIVoiceButton = new ImageButton(context);
        int iconSize = (int) context.getResources().getDimension(R.dimen.icon_size);
        AIVoiceButton.setLayoutParams(new RelativeLayout.LayoutParams(iconSize, iconSize));
        AIVoiceButton.setId(View.generateViewId());
        AIVoiceButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ai_voice, null));
        AIVoiceButton.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.button_rounded_grey, null));
        AIVoiceButton.setLayoutParams(paramsView);
        AIVoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestSignIn(currentPoi.getName());
            }
        });

        RelativeLayout.LayoutParams paramsView2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsView2.addRule(RelativeLayout.CENTER_VERTICAL);
        paramsView2.addRule(RelativeLayout.END_OF, AIVoiceButton.getId());

        ImageButton AIContextButton = new ImageButton(context);
        AIContextButton.setLayoutParams(new RelativeLayout.LayoutParams(iconSize, iconSize));
        AIContextButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ai_context, null));
        AIContextButton.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.button_rounded_grey, null));
        AIContextButton.setLayoutParams(paramsView2);
        AIContextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://en.wikipedia.org")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                WikiPediaPageService wikiPediaPageService = retrofit.create(WikiPediaPageService.class);
                wikiPediaPageService.getQuery(currentPoi.getName(), 1).enqueue(new Callback<WikipediaPageResponse>() {
                    @Override
                    public void onResponse(Call<WikipediaPageResponse> call, Response<WikipediaPageResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String fullSentence = makeSentence(currentPoi.getName(), response.body().getPages().get(0).getDescription());
                            Log.d("Wikipedia Response", "Succeeded: " + fullSentence);
                            playBarkAudioFromText(context, fullSentence);
                        }
                    }
                    @Override
                    public void onFailure(Call<WikipediaPageResponse> call, Throwable t) {
                        Log.d("Wikipedia Response", "Failure");
                    }
                });
            }
        });

        layout.addView(AIVoiceButton);
        layout.addView(AIContextButton);

        int maxLengthPoiName = getMaxLength();

        //Poi Name
        RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        TextView poiName = new TextView(context);

        if (currentPoi.getName().length() > maxLengthPoiName) {
            String name = currentPoi.getName().substring(0, maxLengthPoiName) + "...";
            poiName.setText(name);
        } else {
            poiName.setText(currentPoi.getName());
        }

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //Portrait Orientation
            poiName.setTextSize(15);
        } else {
            poiName.setTextSize(20);
        }


        poiName.setMaxLines(2);
        poiName.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.white, null));
        paramsText.addRule(RelativeLayout.CENTER_IN_PARENT);
        poiName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String command = buildCommand(currentPoi);
                Log.d("POINAME","button");
                SearchFragment.recentPOI = currentPoi.getName();
                VisitPoiTask visitPoiTask = new VisitPoiTask(command, currentPoi, false);
                visitPoiTask.execute();
                playBarkAudioFromText(context, "You are looking at " + currentPoi.getName());

                disableOtherRotateButtons(viewGroup);


                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                rotatePoiButton.setEnabled(true);
                rotatePoiButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.baseline_autorenew_24_for_orbit, null));
            }
        });

        layout.addView(poiName);
        poiName.setLayoutParams(paramsText);

        layout.addView(rotatePoiButton);
        rotatePoiButton.setLayoutParams(paramsRotate);

        return layout;
    }

    private void showVoiceMenu(View view, String POIName){
        List<PowerMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new PowerMenuItem("Artificial Intelligence Voice"));
        menuItems.add(new PowerMenuItem("Artificial Intelligence Context"));

        SearchFragment.recentPOI = POIName;

        PowerMenu powerMenu = new PowerMenu.Builder(context)
                .addItemList(menuItems)
                .setAnimation(MenuAnimation.SHOWUP_BOTTOM_LEFT)
                .setMenuRadius(20f)
                .setTextSize(15)
                .setWidth(500)
                .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT)
                .setAutoDismiss(true)
                .setTextColor(ContextCompat.getColor(context, R.color.offwhite))
                .setMenuColor(ContextCompat.getColor(context, R.color.suggpoi_blue))
                .setOnMenuItemClickListener(new OnMenuItemClickListener<PowerMenuItem>() {
                    @Override
                    public void onItemClick(int position, PowerMenuItem item) {
                        switch (position) {
                            case 0:
                                requestSignIn(POIName);
                                break;
                            case 1:
                                Retrofit retrofit = new Retrofit.Builder()
                                        .baseUrl("https://en.wikipedia.org")
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .build();

                                WikiPediaPageService wikiPediaPageService = retrofit.create(WikiPediaPageService.class);
                                wikiPediaPageService.getQuery(POIName, 1).enqueue(new Callback<WikipediaPageResponse>() {
                                    @Override
                                    public void onResponse(Call<WikipediaPageResponse> call, Response<WikipediaPageResponse> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            String fullSentence = makeSentence(POIName, response.body().getPages().get(0).getDescription());
                                            Log.d("Wikipedia Response", "Succeeded: " + fullSentence);
                                            playBarkAudioFromText(context, fullSentence);
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<WikipediaPageResponse> call, Throwable t) {
                                        Log.d("Wikipedia Response", "Failure");
                                    }
                                });
                                break;
                        }
                    }
                })
                .build();

        view.setOnClickListener(powerMenu::showAsAnchorLeftTop);
    }

//  Play the Audio received by Bark from the AI server
    public static void playBarkAudioFromText(final Context context, final String text) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Session session = LGUtils.getSession(context);

                Log.d("BARK", "playBarkAudioFromText");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String aiServerIp = prefs.getString("AIServerIP", "172.28.26.84");
                String aiServerPort = prefs.getString("AIServerPort", "5000");

                String command = "curl -X POST -H \"Content-Type: application/json\" -d '{\"text\":\"" + text + "\"}' " + "http://" + aiServerIp + ":" + aiServerPort + "/synthesize";
                byte[] response = LGUtils.executeAudioCommandWithResponse(session, command, context);
                File audioFile = saveAudioFile(context, response);
                if (audioFile != null && audioFile.exists() && audioFile.length() > 0) {
                    playAudio(context, audioFile);
                } else {
                    Log.d("BARK", "Audio file is not ready yet.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

//    Save Audio to Temp file
private static File saveAudioFile(Context context, byte[] audioData) throws IOException {
    File tempFile = File.createTempFile("lg_audio_", ".wav", context.getExternalCacheDir());
    FileOutputStream fos = new FileOutputStream(tempFile);

    ByteArrayInputStream bais = new ByteArrayInputStream(audioData);

    byte[] buffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = bais.read(buffer)) != -1) {
        fos.write(buffer, 0, bytesRead);
    }

    fos.close();
    return tempFile;
}

//    Play the Audio file via MediaPlayer
private static void playAudio(Context context, File audioFilePath) {
    MediaPlayer mediaPlayer = new MediaPlayer();

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);
        } else {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mediaPlayer.setDataSource(audioFilePath.getAbsolutePath());
        mediaPlayer.prepare();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
        try {
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    } catch (IOException e) {
        e.printStackTrace();
        mediaPlayer.release();
    }
}


//   Making proper sentence for description returned by the wikipedia API
    private String makeSentence(String POIName, String description){
        String fullSentence;
        char firstChar = description.charAt(0);
        if (Character.isLetter(firstChar)) {
            if (isVowel(firstChar)) {
                // If the description starts with a vowel, use "an" before the description
                fullSentence = String.format("You have clicked on " + POIName + ". " + POIName + " is an " + Character.toLowerCase(firstChar) + description.substring(1));
            } else {
                // If the description starts with a consonant, use "a" before the description
                fullSentence = String.format("You have clicked on " + POIName + ". " + POIName + " is a " + Character.toLowerCase(firstChar) + description.substring(1));
            }
        } else {
            // If the first character is not a letter, use "the" before the description
            fullSentence = String.format("You have clicked on " + POIName + ". " + POIName + " is the " + Character.toLowerCase(firstChar) + description.substring(1));
        }
        return fullSentence;
    }

    private boolean isVowel(char ch) {
        // Check if the character is a vowel (ignoring case)
        return "AEIOUaeiou".indexOf(ch) != -1;
    }

    public interface SignInListener {
        void onSignInRequested(String poiName);
    }

    private void requestSignIn(String poiName) {
        if (signInListener != null) {
            signInListener.onSignInRequested(poiName);
        }
    }

    private void disableOtherRotateButtons(ViewGroup viewGroup) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            RelativeLayout poiItem = (RelativeLayout) viewGroup.getChildAt(i);
            ImageButton rotateButton = (ImageButton) poiItem.getChildAt(3);

            rotateButton.setEnabled(false);
            rotateButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_autorenew_white_36dp, null));
        }
    }

    private int getMaxLength() {

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;

        //The size of the diagonal in inches is equal to the square root of the height in inches squared plus the width in inches squared.
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;

        float smallestWidth = Math.min(widthDp, heightDp);

        if (smallestWidth > 800) {
            return 35;
        } else if (smallestWidth <= 800 && smallestWidth >= 600) {
            return 13;
        } else {
            return 10;
        }
    }

    private String buildCommand(POI poi) {
        return "echo 'flytoview=<gx:duration>3</gx:duration><gx:flyToMode>smooth</gx:flyToMode><LookAt><longitude>" + poi.getLongitude() + "</longitude>" +
                "<latitude>" + poi.getLatitude() + "</latitude>" +
                "<altitude>" + poi.getAltitude() + "</altitude>" +
                "<heading>" + poi.getHeading() + "</heading>" +
                "<tilt>" + poi.getTilt() + "</tilt>" +
                "<range>" + poi.getRange() + "</range>" +
                "<gx:altitudeMode>" + poi.getAltitudeMode() + "</gx:altitudeMode>" +
                "</LookAt>' > /tmp/query.txt";
    }

    private class VisitPoiTask extends AsyncTask<Void, Void, String> {

        String command;
        POI currentPoi;
        boolean rotate;
        int rotationAngle = 10;
        int rotationFactor = 1;
        boolean changeVelocity = false;
        private ProgressDialog dialog;

        VisitPoiTask(String command, POI currentPoi, boolean rotate) {
            this.command = command;
            this.currentPoi = currentPoi;
            this.rotate = rotate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dialog == null) {
                dialog = new ProgressDialog(context, R.style.BlackTextAlertDialog);
                String message = context.getResources().getString(R.string.viewing) + " " + this.currentPoi.getName() + " " + context.getResources().getString(R.string.inLG);
                dialog.setMessage(message);
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCancelable(false);


                //Buton positive => more speed
                //Button neutral => less speed
                if (this.rotate) {
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(R.string.speedx2), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing, we after define the onclick
                        }
                    });

                    dialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getResources().getString(R.string.speeddiv2), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing, we after define the onclick
                        }
                    });
                }


                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        cancel(true);
                    }
                });
                dialog.setCanceledOnTouchOutside(false);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });


                dialog.show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_fast_forward_24, 0, 0);
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_fast_rewind_24, 0, 0);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeVelocity = true;
                        rotationFactor = rotationFactor * 2;

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(context.getResources().getString(R.string.speedx4));
                        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setText(context.getResources().getString(R.string.speeddiv2));
                        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(true);
                        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_fast_rewind_24, 0, 0);

                        if (rotationFactor == 4) {
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                        }
                    }
                });
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeVelocity = true;
                        rotationFactor = rotationFactor / 2;

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(context.getResources().getString(R.string.speedx2));
                        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setText(context.getResources().getString(R.string.speeddiv4));
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_fast_forward_24, 0, 0);

                        if (rotationFactor == 1) {
                            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(false);
                        }
                    }
                });
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                session = LGUtils.getSession(activity);

                //We fly to the point
                LGUtils.setConnectionWithLiquidGalaxy(session, command, activity);

                //If rotation button is pressed, we start the rotation
                if (this.rotate) {

                    boolean isFirst = true;

                    while (!isCancelled()) {
                        session.sendKeepAliveMsg();

                        for (int i = 0; i <= (360 - this.currentPoi.getHeading()); i += (this.rotationAngle * this.rotationFactor)) {

                            String commandRotate = "echo 'flytoview=<gx:duration>3</gx:duration><gx:flyToMode>smooth</gx:flyToMode><LookAt>" +
                                    "<longitude>" + this.currentPoi.getLongitude() + "</longitude>" +
                                    "<latitude>" + this.currentPoi.getLatitude() + "</latitude>" +
                                    "<altitude>" + this.currentPoi.getAltitude() + "</altitude>" +
                                    "<heading>" + (this.currentPoi.getHeading() + i) + "</heading>" +
                                    "<tilt>" + this.currentPoi.getTilt() + "</tilt>" +
                                    "<range>" + this.currentPoi.getRange() + "</range>" +
                                    "<gx:altitudeMode>" + this.currentPoi.getAltitudeMode() + "</gx:altitudeMode>" +
                                    "</LookAt>' > /tmp/query.txt";


                            LGUtils.setConnectionWithLiquidGalaxy(session, commandRotate, activity);
                            session.sendKeepAliveMsg();

                            if (isFirst) {
                                isFirst = false;
                                Thread.sleep(7000);
                            } else {
                                Thread.sleep(4000);
                            }
                        }
                    }
                }

                return "";

            } catch (JSchException e) {
                this.cancel(true);
                if (dialog != null) {
                    dialog.dismiss();
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
//                        Toast.makeText(context, context.getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();
                        Toast toast = new Toast(context);
                        View toast_view = LayoutInflater.from(context).inflate(R.layout.toast_text, null);
                        TextView toasttext = toast_view.findViewById(R.id.toasttext);
                        toasttext.setText(context.getResources().getString(R.string.error_galaxy));
                        toast.setView(toast_view);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 100);
                        toast.show();
                    }
                });

                return null;
            } catch (InterruptedException e) {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
//                        Toast.makeText(context, context.getResources().getString(R.string.visualizationCanceled), Toast.LENGTH_LONG).show();
                        Toast toast = new Toast(context);
                        View toast_view = LayoutInflater.from(context).inflate(R.layout.toast_text, null);
                        TextView toasttext = toast_view.findViewById(R.id.toasttext);
                        toasttext.setText(context.getResources().getString(R.string.visualizationCanceled));
                        toast.setView(toast_view);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 100);
                        toast.show();
                    }
                });
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String success) {
            super.onPostExecute(success);
            if (success != null) {
                if (dialog != null) {
                    dialog.hide();
                    dialog.dismiss();
                }
            }
        }
    }

    private class GetSessionTask extends AsyncTask<Void, Void, Void> {

        GetSessionTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            session = LGUtils.getSession(activity);
            return null;
        }

        @Override
        protected void onPostExecute(Void success) {
            super.onPostExecute(success);
        }
    }

}
