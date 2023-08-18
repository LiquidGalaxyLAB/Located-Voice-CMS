package com.gsoc.vedantsingh.locatedvoicecms;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.gsoc.vedantsingh.locatedvoicecms.beans.Category;
import com.gsoc.vedantsingh.locatedvoicecms.beans.POI;
import com.gsoc.vedantsingh.locatedvoicecms.beans.PlaceInfo;
import com.gsoc.vedantsingh.locatedvoicecms.data.POIsContract;
import com.gsoc.vedantsingh.locatedvoicecms.utils.LGUtils;
import com.gsoc.vedantsingh.locatedvoicecms.utils.PoisGridViewAdapter;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class SearchFragment extends Fragment implements PoisGridViewAdapter.SignInListener {

    private String Audio_Path = "";
    private static boolean isPlaying = false;
    private static MediaPlayer mediaPlayer = new MediaPlayer();
    View rootView;
    GridView poisGridView;
    Session session;
    private String currentPlanet = "EARTH";
    private ListView categoriesListView;
    private Button nearbyplaces, sound_btn;
    private CategoriesAdapter adapter;
    private TextView categorySelectorTitle, sshConnText, aiConnText;
    private ImageView backIcon, backStartIcon, sshConnDot, aiConnDot;
    SharedPreferences sharedPreferences;
    private static ArrayList<String> backIDs = new ArrayList<>();

    private static final String TAG = "SearchFragment";

    private static final int REQUEST_CODE_SIGN_IN = 123;
    public static DriveServiceHelper mDriveServiceHelper = null;
    public static String recentPOI;
    String DRIVE_FOLDER_ID = "1IqFDdaIRWhqv580G2uknYaFgGQmEVQ8P";
    List<PlaceInfo> nearbyPlaces = new ArrayList<>();


    public SearchFragment() {
        // Required empty public constructor
    }

    public interface WikipediaObtainCoordinates {
        @GET("w/api.php")
        Call<WikipediaCoordinatesResponse> getCoordinates(
                @Query("action") String action,
                @Query("format") String format,
                @Query("titles") String titles,
                @Query("prop") String prop
        );
    }

    public interface WikipediaGeoSearchApiService {
        @GET("w/api.php")
        Call<WikipediaGeoSearchResponse> getPlaces(
                @Query("action") String action,
                @Query("format") String format,
                @Query("ggsradius") int ggsradius,
                @Query("ggslimit") int ggslimit,
                @Query("ggscoord") String ggscoord,
                @Query("generator") String generator,
                @Query("prop") String prop
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.newsearch_fragment, container, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sshConnDot = rootView.findViewById(R.id.ssh_conn_dot);
        sshConnText = rootView.findViewById(R.id.ssh_conn_text);
        aiConnText = rootView.findViewById(R.id.ai_conn_text);
        aiConnDot = rootView.findViewById(R.id.ai_conn_dot);
        categoriesListView = (ListView) rootView.findViewById(R.id.categories_listview);
        nearbyplaces = rootView.findViewById(R.id.nearbyplaces);
        sound_btn = rootView.findViewById(R.id.sound_btn);
        backIcon = (ImageView) rootView.findViewById(R.id.back_icon);
        backStartIcon = (ImageView) rootView.findViewById(R.id.back_start_icon);//comes back to the initial category
        categorySelectorTitle = (TextView) rootView.findViewById(R.id.current_category);
        poisGridView = (GridView) rootView.findViewById(R.id.POISgridview);

        if (getArguments() != null) {
            String currentplanet = getArguments().getString("currentplanet");
            if (Objects.equals(currentplanet, "EARTH")) {
                Earth();
            } else if (Objects.equals(currentplanet, "MOON")) {
                Moon();
            } else if (Objects.equals(currentplanet, "MARS")) {
                Mars();
            }
        }

        if (LGPC.LG_CONNECTION) {
            sshConnDot.setColorFilter(getResources().getColor(R.color.green));
            sshConnText.setTextColor(getResources().getColor(R.color.green));
            sshConnText.setText("LG Connected");
        } else {
            sshConnDot.setColorFilter(getResources().getColor(R.color.red));
            sshConnText.setTextColor(getResources().getColor(R.color.red));
            sshConnText.setText("LG Disconnected");
        }

        if (LGPC.AI_SERVER_CONNECTION) {
            aiConnDot.setColorFilter(getResources().getColor(R.color.green));
            aiConnText.setTextColor(getResources().getColor(R.color.green));
            aiConnText.setText("AI Server Connected");
        } else {
            aiConnDot.setColorFilter(getResources().getColor(R.color.red));
            aiConnText.setTextColor(getResources().getColor(R.color.red));
            aiConnText.setText("AI Server Disconnected");
        }

        backStartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backIDs.clear();
                Category category = getCategoryByName(currentPlanet);
                backIDs.add(String.valueOf(category.getId()));
                if (isPlaying) {
                    mediaPlayer.stop();
                    sound_btn.setText("Play Category Sound  ");
                    sound_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_volume_up_24, 0);
                }
                Audio_Path = "";
                showPoisByCategory();
            }
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backIDs.size() > 1) {
                    backIDs.remove(0);
                    if (isPlaying) {
                        mediaPlayer.stop();
                        sound_btn.setText("Play Category Sound  ");
                        sound_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_volume_up_24, 0);
                    }
                    Audio_Path = "";
                }
                showPoisByCategory();
            }
        });

        nearbyplaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                obtainCoordinatesfromWikiAndDisplayNearbyPlaces(recentPOI);
            }
        });

        if (isPermissionGranted(sharedPreferences)) {
            if (!isAudioSaved(sharedPreferences)) {
                POIsContract.CategoryEntry.saveAudioToDevice(getContext());

                // Store the updated value of audioSaved in shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("audioSaved", true);
                editor.apply();
            }
        } else {
            Toast toast = new Toast(getContext());
            View toast_view = LayoutInflater.from(getContext()).inflate(R.layout.toast_text, null);
            TextView toasttext = toast_view.findViewById(R.id.toasttext);
            toasttext.setText("Storage permissions are not granted, please restart the app and grant the permissions");
            toast.setView(toast_view);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 100);
            toast.show();
        }

        sound_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Audio_Path.isEmpty()) {
                    if (isPlaying) {
                        // If audio is already playing, stop it
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        sound_btn.setText("Play Category Sound  ");
                        sound_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_volume_up_24, 0);
                        isPlaying = false;
                    } else {
                        // Start playing the audio from the beginning
                        try {
                            mediaPlayer.setDataSource(Audio_Path);
                            mediaPlayer.prepare();
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    sound_btn.setText("Play Category Sound  ");
                                    sound_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_volume_up_24, 0);
                                }
                            });
                            mediaPlayer.start();
                            sound_btn.setText("Stop Category Sound  ");
                            sound_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_stop_24, 0);
                            isPlaying = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast toast = new Toast(getContext());
                    View toast_view = LayoutInflater.from(getContext()).inflate(R.layout.toast_text, null);
                    TextView toasttext = toast_view.findViewById(R.id.toasttext);
                    toasttext.setText("Audio not set");
                    toast.setView(toast_view);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 100);
                    toast.show();
                }
            }
        });

        return rootView;
    }

    public void searchNearbyPlaces(double[] coordinates) {

//        List<PlaceInfo> nearbyPlaces = new ArrayList<>();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WikipediaGeoSearchApiService wikipediaGeoSearchApiService = retrofit.create(WikipediaGeoSearchApiService.class);

        wikipediaGeoSearchApiService.getPlaces("query", "json", 1000, 10, String.valueOf(coordinates[0]) + "|" + String.valueOf(coordinates[1]), "geosearch", "coordinates|pageimages|description").enqueue(new Callback<WikipediaGeoSearchResponse>() {
            @Override
            public void onResponse(Call<WikipediaGeoSearchResponse> call, Response<WikipediaGeoSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WikipediaGeoSearchResponse.QueryResult queryResult = response.body().getQueryResult();
                    if (queryResult != null && queryResult.getWikiPages() != null) {
                        nearbyPlaces = new ArrayList<>();
                        for (WikipediaGeoSearchResponse.WikiPage wikiPage : queryResult.getWikiPages().values()) {
                            if (wikiPage != null) {
                                String title = wikiPage.getTitle();
                                String description = "";
                                if (wikiPage.getDescription() != null) {
                                    description = wikiPage.getDescription();
                                }

                                String imageLink = "";
                                if (wikiPage.getThumbnail() != null && wikiPage.getThumbnail().getSource() != null) {
                                    imageLink = wikiPage.getThumbnail().getSource();
                                }
                                PlaceInfo placeInfo = new PlaceInfo(title, description, imageLink);
                                nearbyPlaces.add(placeInfo);
                            }
                        }
                    }
                    Intent intent = new Intent(getActivity(), NearbyPlacesActivity.class);
                    intent.putParcelableArrayListExtra("nearbyPlacesList", (ArrayList<? extends Parcelable>) nearbyPlaces);
                    startActivity(intent);
                    if (LGPC.LG_CONNECTION) {
//                Displaying the Balloon on the rightmost part of the LG
                        String machinesString = sharedPreferences.getString("Machines", "3");
                        int machines = Integer.parseInt(machinesString);
                        int slave_num = Math.floorDiv(machines, 2) + 1;
                        String slave_name = "slave_" + slave_num;
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        SearchFragment.NearbyPlacesTask nearbyPlacesTask = new SearchFragment.NearbyPlacesTask(slave_name, session, getContext(), nearbyPlaces);
                        Future<Void> future = executorService.submit(nearbyPlacesTask);
                        try {
                            future.get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        executorService.shutdown();
                    }


                }
            }

            @Override
            public void onFailure(Call<WikipediaGeoSearchResponse> call, Throwable t) {

            }
        });
    }

    public void obtainCoordinatesfromWikiAndDisplayNearbyPlaces(String POIName) {

        final double[] coordinates = new double[2];
//       Get the coordinates of the Recent POI
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create the service using the Retrofit instance
        WikipediaObtainCoordinates wikipediaObtainCoordinates = retrofit.create(WikipediaObtainCoordinates.class);

        // Execute the call and handle the response
        wikipediaObtainCoordinates.getCoordinates("query", "json", POIName, "coordinates").enqueue(new Callback<WikipediaCoordinatesResponse>() {
            @Override
            public void onResponse(Call<WikipediaCoordinatesResponse> call, Response<WikipediaCoordinatesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WikipediaCoordinatesResponse.QueryResult queryResult = response.body().getQueryResult();
                    if (queryResult != null && queryResult.getWikiPages() != null) {
                        WikipediaCoordinatesResponse.WikiPage wikiPage = queryResult.getWikiPages().values().iterator().next();
                        if (wikiPage != null && wikiPage.getCoordinates() != null && wikiPage.getCoordinates().length > 0) {
                            coordinates[0] = wikiPage.getCoordinates()[0].getLatitude();
                            coordinates[1] = wikiPage.getCoordinates()[0].getLongitude();

                            searchNearbyPlaces(coordinates);

                            Log.d("Coordinates Obtained", String.valueOf(coordinates[0]) + " , " + String.valueOf(coordinates[1]));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<WikipediaCoordinatesResponse> call, Throwable t) {

            }
        });

    }

    @Override
    public void onSignInRequested(String poiName) {
        requestSignIn(poiName);
    }

    private void requestSignIn(String poiName) {
        Log.d(TAG, "Requesting sign-in");
        recentPOI = poiName;

        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (lastSignedInAccount != null) {
            // User is already signed in, handle the signed-in account
            handleSignInResult(lastSignedInAccount);
        } else {
            // User is not signed in, initiate the sign-in process
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(new Scope(DriveScopes.DRIVE_READONLY))
                            .build();
            GoogleSignInClient client = GoogleSignIn.getClient(getContext(), signInOptions);

            // The result of the sign-in Intent is handled in onActivityResult.
            Log.d(TAG, "Requesting sign-in 2");
            startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
        }
    }

    private void handleSignInResult(GoogleSignInAccount googleAccount) {
        if (googleAccount != null) {
            Log.d(TAG, "Signed in as " + googleAccount.getEmail());

            // Use the authenticated account to sign in to the Drive service.
            GoogleAccountCredential credential =
                    GoogleAccountCredential.usingOAuth2(
                            getContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
            credential.setSelectedAccount(googleAccount.getAccount());
            Drive googleDriveService =
                    new Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName("Located Voice CMS")
                            .build();

            // The DriveServiceHelper encapsulates all REST API and SAF functionality.
            // Its instantiation is required before handling any onClick actions.
            mDriveServiceHelper = new DriveServiceHelper(googleDriveService);

            Toast toast = new Toast(getContext());
            View toast_view = LayoutInflater.from(getContext()).inflate(R.layout.toast_text, null);
            TextView toasttext = toast_view.findViewById(R.id.toasttext);
            toasttext.setText("Fetching Audio...");
            toast.setView(toast_view);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 100);
            toast.show();

            if (recentPOI != null) {
                mDriveServiceHelper.playAudioFileInFolder(DRIVE_FOLDER_ID, POIsContract.CategoryEntry.getNameById(getContext(), Integer.parseInt(backIDs.get(0))), recentPOI + ".mp3");
            } else {
                toasttext.setText("Please select a POI to listen to its description");
                toast.setView(toast_view);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 100);
                toast.show();
            }
            Log.d("Sign In", "Completed");
        } else {
            Log.e(TAG, "Unable to sign in.");
        }
    }

    public static boolean isAudioSaved(SharedPreferences sharedPrefs) {
        return sharedPrefs.getBoolean("audioSaved", false);
    }

    public static boolean isPermissionGranted(SharedPreferences sharedPrefs) {
        return sharedPrefs.getBoolean("permissionGranted", false);
    }

    public static void audioPlayerStop() {
        if (isPlaying) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            isPlaying = false;
        }
    }

    private void setAudioFile() {
        String audioFilePath = POIsContract.CategoryEntry.getAudioPathByID(getActivity(), Integer.parseInt(backIDs.get(0)));
        if (audioFilePath != null) {
            Audio_Path = audioFilePath;
        }
    }

    private void showPoisByCategory() {

        Cursor queryCursor = getCategoriesCursor();
        showCategoriesOnScreen(queryCursor);

        String currentCategoryName = POIsContract.CategoryEntry.getNameById(getActivity(), Integer.parseInt(backIDs.get(0)));

        categorySelectorTitle.setText(currentCategoryName);

        final List<POI> poisList = getPoisList(Integer.parseInt(backIDs.get(0)));
        if (poisList != null) {
            poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity(), this));
        }
    }

    private Cursor getCategoriesCursor() {
        //we get only the categories that the admin user wants to be shown on the app screen and have father category ID the once of the parameters.
        return POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), backIDs.get(0));
    }

    private void showCategoriesOnScreen(Cursor queryCursor) {
        adapter = new CategoriesAdapter(getActivity(), queryCursor, 0);

        if (queryCursor.getCount() > 0) {
            categoriesListView.setAdapter(adapter);

            categoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);//gets the category selected
                    if (cursor != null) {
                        int itemSelectedID = cursor.getInt(0);
                        backIDs.add(0, String.valueOf(itemSelectedID));
                        //this method is call to see AGAIN the categories list. However, the view will
                        //correspond to the categories inside the current category just clicked.
                        showPoisByCategory();
                        setAudioFile();
                    }
                }
            });
        } else {
            categoriesListView.setAdapter(null);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Log.d(TAG, "Requesting sign-in 3");
                    GoogleSignIn.getSignedInAccountFromIntent(data)
                            .addOnSuccessListener(this::handleSignInResult);
                }
                break;
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            backIDs = savedInstanceState.getStringArrayList("backIds");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("backIds", backIDs);
    }

    @Override
    public void onResume() {
        super.onResume();
        showPoisByCategory();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (currentPlanet.equals("EARTH")) {
            Category category = getCategoryByName(currentPlanet);

            final List<POI> poisList = getPoisList(category.getId());
            if (poisList != null) {
                poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity(), this));
            }
        }

        Category category = getCategoryByName(currentPlanet);
        categorySelectorTitle.setText(category.getName());

        backIDs.add(String.valueOf(category.getId()));
        Cursor queryCursor = POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), String.valueOf(category.getId()));
        showCategoriesOnScreen(queryCursor);

        GetSessionTask getSessionTask = new GetSessionTask();
        getSessionTask.execute();
    }

    private List<POI> getPoisList(int categoryId) {

        List<POI> lPois = new ArrayList<>();

        try (Cursor allPoisByCategoryCursor = POIsContract.POIEntry.getPOIsByCategory(getActivity(), String.valueOf(categoryId))) {

            while (allPoisByCategoryCursor.moveToNext()) {

                int poiId = allPoisByCategoryCursor.getInt(0);

                POI poiEntry = getPoiData(poiId);
                lPois.add(poiEntry);
            }
        }
        return lPois;
    }

    private POI getPoiData(int poiId) {
        POI poiEntry = new POI();
        Cursor poiCursor = POIsContract.POIEntry.getPoiByID(poiId);

        if (poiCursor.moveToNext()) {

            poiEntry.setId(poiCursor.getLong(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_ID)));
            poiEntry.setName(poiCursor.getString(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_COMPLETE_NAME)));
            poiEntry.setAltitude(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_ALTITUDE)));
            poiEntry.setAltitudeMode(poiCursor.getString(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE)));
            poiEntry.setCategoryId(poiCursor.getInt(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_CATEGORY_ID)));
            poiEntry.setHeading(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_HEADING)));
            poiEntry.setLatitude(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_LATITUDE)));
            poiEntry.setLongitude(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_LONGITUDE)));
            poiEntry.setHidden(poiCursor.getInt(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_HIDE)) == 1);
            poiEntry.setRange(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_RANGE)));
            poiEntry.setTilt(poiCursor.getDouble(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_TILT)));
            poiEntry.setVisited_place(poiCursor.getString(poiCursor.getColumnIndex(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME)));
        }
        poiCursor.close();
        return poiEntry;
    }

    private Category getCategoryByName(String categoryName) {
        Category category = new Category();
        try (Cursor categoryCursor = POIsContract.CategoryEntry.getCategoriesByName(getActivity(), categoryName)) {

            if (categoryCursor.moveToNext()) {
                category.setId(categoryCursor.getInt(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_ID)));
                category.setFatherID(categoryCursor.getInt(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_FATHER_ID)));
                category.setName(categoryCursor.getString(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_NAME)));
                category.setShownName(categoryCursor.getString(categoryCursor.getColumnIndex(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME)));
            }
        }
        return category;
    }

    private void Earth() {
//        String command = "echo 'planet=earth' > /tmp/query.txt";

        if (Objects.equals(getArguments().getString("currentplanet"), "EARTH")) {
//            SearchTask searchTask = new SearchTask(command, true);
//            searchTask.execute();
            currentPlanet = "EARTH";
        }

        Category category = getCategoryByName(currentPlanet);
        categorySelectorTitle.setText(category.getName());

        backIDs = new ArrayList<>();
        backIDs.add(String.valueOf(category.getId()));

        Cursor queryCursor = POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), String.valueOf(category.getId()));
        showCategoriesOnScreen(queryCursor);

        final List<POI> poisList = getPoisList(category.getId());
        if (poisList != null) {
            poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity(), this));
        }
    }

    void Moon() {

//        String command = "echo 'planet=moon' > /tmp/query.txt";
        if (!currentPlanet.equals("MOON")) {
            //setConnectionWithLiquidGalaxy(command);
//            SearchTask searchTask = new SearchTask(command, true);
//            searchTask.execute();
            currentPlanet = "MOON";
            Category category = getCategoryByName(currentPlanet);
            categorySelectorTitle.setText(category.getName());

            backIDs = new ArrayList<>();
            backIDs.add(String.valueOf(category.getId()));

            Cursor queryCursor = POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), String.valueOf(category.getId()));
            showCategoriesOnScreen(queryCursor);

            final List<POI> poisList = getPoisList(category.getId());
            poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity(), this));
        }
    }

    private void Mars() {

//        String command = "echo 'planet=mars' > /tmp/query.txt";
        if (!currentPlanet.equals("MARS")) {
//            SearchTask searchTask = new SearchTask(command, true);
//            searchTask.execute();
            currentPlanet = "MARS";
            Category category = getCategoryByName(currentPlanet);
            categorySelectorTitle.setText(category.getName());

            backIDs = new ArrayList<>();
            backIDs.add(String.valueOf(category.getId()));

            Cursor queryCursor = POIsContract.CategoryEntry.getNotHiddenCategoriesByFatherID(getActivity(), String.valueOf(category.getId()));
            showCategoriesOnScreen(queryCursor);

            final List<POI> poisList = getPoisList(category.getId());
            poisGridView.setAdapter(new PoisGridViewAdapter(poisList, getActivity(), getActivity(), this));
        }
    }

    public class NearbyPlacesTask implements Callable<Void> {
        private String slaveName;
        private Session session;
        private Context context;
        private List<PlaceInfo> nearbyPlaces;

        public NearbyPlacesTask(String slaveName, Session session, Context context, List<PlaceInfo> nearbyPlaces) {
            this.slaveName = slaveName;
            this.session = session;
            this.context = context;
            this.nearbyPlaces = nearbyPlaces;
        }

        @Override
        public Void call() throws Exception {
            try {
                String sentence = "chmod 777 /var/www/html/kml/" + slaveName + ".kml; echo '" +
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
                        "  <Document>\n" +
                        "    <name>historic.kml</name>\n" +
                        "    <ScreenOverlay>\n" +
                        "      <name><![CDATA[<div style=\"text-align: center; font-size: 20px; font-weight: bold; vertical-align: middle;\">Nearby Places</div>]]></name>\n" +
                        "      <description><![CDATA[\n" +
                        "        <html>\n" +
                        "          <body>\n" +
                        "            <table width=\"400\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\" style=\"font-size: 14px;\" border=1 frame=void rules=rows>\n";

                Iterator<PlaceInfo> iterator = nearbyPlaces.iterator();
                int iterationCount = 0; // Counter variable to keep track of iterations
                while (iterator.hasNext() && iterationCount < 10) {
                    PlaceInfo placeInfo = iterator.next();
                    sentence += "              <tr>\n" +
                            "                <td colspan=\"2\" align=\"center\">\n" +
                            "                <img src=\"" + placeInfo.getImageLink() + "\" alt=\"picture\" height=\"100\" style=\"float: left; margin-right: 10px;\" />\n" +
                            "                  <p><b>" + placeInfo.getTitle() + "</b> " + placeInfo.getDescription() + "</p>\n" +
                            "                </td>\n" +
                            "              </tr>\n";
                    iterationCount++; // Increment the counter variable
                }

                sentence += "            </table>\n" +
                        "          </body>\n" +
                        "        </html>\n" +
                        "      ]]></description>\n" +
                        "      <overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                        "      <screenXY x=\"1\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                        "      <rotationXY x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                        "      <size x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>\n" +
                        "      <gx:balloonVisibility>1</gx:balloonVisibility>\n" +
                        "    </ScreenOverlay>\n" +
                        "  </Document>\n" +
                        "</kml>\n' > /var/www/html/kml/" + slaveName + ".kml";

                LGUtils.setConnectionWithLiquidGalaxy(session, sentence, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class GetSessionTask extends AsyncTask<Void, Void, Void> {

        public GetSessionTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (getActivity() != null) {
                session = LGUtils.getSession(getActivity());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void success) {
            super.onPostExecute(success);
        }
    }
}