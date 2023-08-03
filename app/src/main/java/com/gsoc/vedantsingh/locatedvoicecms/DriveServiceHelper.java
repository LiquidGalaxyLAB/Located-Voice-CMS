package com.gsoc.vedantsingh.locatedvoicecms;

import android.app.Activity;
import com.google.api.services.drive.model.File;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.model.FileList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/* class to demonstrate use of Drive files list API */
public class DriveServiceHelper extends Activity {
//    private static final String TAG = "drive-quickstart";
//    private static final int REQUEST_CODE_SIGN_IN = 0;
//
//    private Drive mDriveService;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        signIn();
//    }
//
//    /** Start sign-in activity. */
//    private void signIn() {
//        Log.i(TAG, "Start sign in");
//        GoogleSignInOptions signInOptions =
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestScopes(Drive.SCOPE_FILE)
//                        .build();
//
//        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API, signInOptions)
//                .build();
//
//        startActivityForResult(googleApiClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
//    }
//
//    @Override
//    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_SIGN_IN) {
//            Log.i(TAG, "Sign in request code");
//            GoogleSignInResult result = com.google.android.gms.auth.api.Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//            handleSignInResult(result);
//        }
//    }
//
//    /** Handle sign-in result. */
//    private void handleSignInResult(GoogleSignInResult result) {
//        if (result.isSuccess()) {
//            Log.i(TAG, "Signed in successfully.");
//            GoogleSignInAccount account = result.getSignInAccount();
//            // Use the access token to authenticate HTTP requests
//            assert account != null;
//            GoogleCredential credential = new GoogleCredential().setAccessToken(((GoogleSignInAccount) account).getIdToken());
//            // Create the Drive service
//            mDriveService = new Drive.Builder(
//                    AndroidHttp.newCompatibleTransport(),
//                    new AndroidJsonFactory(),
//                    credential)
//                    .setApplicationName("YourAppName")
//                    .build();
//
//            // Now you can make API requests using mDriveService
//            // For example, to list files:
//            try {
//                com.google.api.services.drive.model.FileList files = mDriveService.files().list().execute();
//                for (File file : files.getFiles()) {
//                    Log.d(TAG, "File Name: " + file.getName());
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Log.e(TAG, "Unable to sign in.");
//        }
//    }

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final com.google.api.services.drive.Drive mDriveService;
    static MediaPlayer mediaPlayer;


    public DriveServiceHelper(com.google.api.services.drive.Drive googleDriveService) {
        mDriveService = googleDriveService;
    }

    /**
     * Returns a list of files inside a specific folder identified by the given {@code folderId}.
     */
    public Task<List<File>> listFilesInFolder(String folderId) {
        return Tasks.call(mExecutor, () -> {
            String query = "'" + folderId + "' in parents and trashed=false";
            return mDriveService.files().list().setQ(query).setSpaces("drive").execute().getFiles();
        });
    }

    /**
     * Navigates into the specified folder structure and plays the specified audio file.
     */

    public void playAudioFileInFolder(String folderId, String folderInFolderName, String fileName) {
        Log.d("File", "Folder ID: " + folderId);
        Log.d("File", "Folder in Folder Name: " + folderInFolderName);
        Log.d("File", "Audio File Name: " + fileName);
        listFilesInFolder(folderId).onSuccessTask(mExecutor, task -> {
            List<File> files = task;
            String folderInFolderId = null;

            for (File file : files) {
                if (file.getName().equals(folderInFolderName) && file.getMimeType().equals("application/vnd.google-apps.folder")) {
                    folderInFolderId = file.getId();
                    break;
                }
            }

            if (folderInFolderId != null) {
                return listFilesInFolder(folderInFolderId).onSuccessTask(mExecutor, folderTask -> {
                    List<File> folderFiles = folderTask;
                    Log.d("FileCount", folderFiles.toString());
                    String fileId = null;

                    for (File file : folderFiles) {
                        if (file.getName().equals(fileName) && file.getMimeType().equals("audio/mpeg")) {
                            fileId = file.getId();
                            break;
                        }
                    }

                    if (fileId != null) {
                        return getFileUrl(fileId).onSuccessTask(mExecutor, fileUrl -> {
                            Log.d("File URL", fileUrl);
                            if (fileUrl != null) {
                                // Play the audio file using the URL
                                playAudioFileUsingUrl(fileUrl);
                            } else {
                                throw new IOException("URL for audio file not found.");
                            }
                            return null;
                        });
                    } else {
                        throw new IOException("Audio file not found in the specified folder.");
                    }
                });
            } else {
                throw new IOException("Folder not found in the main folder.");
            }
        }).addOnFailureListener(mExecutor, exception -> {
            // The code inside this block will be executed if the task fails
            Log.e("Error", "Error listing files in the folder: " + exception.getMessage());
        });
    }



    /**
     * Opens the file identified by {@code fileId} and returns its URL as a String.
     * Note: This assumes that the file is an mp3 audio file.
     */
    public Task<String> getFileUrl(String fileId) {
        return Tasks.call(mExecutor, () -> {
            String fileUrl = null;
            // Get the metadata of the file
            File file = mDriveService.files().get(fileId).execute();
            String mimeType = file.getMimeType();

            // Check if the file is an mp3 audio file
            if (mimeType != null && mimeType.equals("audio/mpeg")) {
                // Create a file content URL
                fileUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
            }

            return fileUrl;
        });
    }

    /**
     * Play an mp3 audio file directly using its URL.
     */
    public void playAudioFileUsingUrl(String fileUrl) {
        try {
            // Set audio attributes for the media player
            mediaPlayer = new MediaPlayer();
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);

            // Set the data source using the URL
            mediaPlayer.setDataSource(fileUrl);

            // Prepare and start playback
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();
//                changeListenDescButtonState(true);
            });
            mediaPlayer.start();
//            changeListenDescButtonState(false);

//            SearchFragment.listen_desc.setText("Stop Listening to description  ");
//            SearchFragment.listen_desc.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_stop_24, 0);
            // Release the media player after playback completes
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void changeListenDescButtonState(boolean state) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(state){
//                    SearchFragment.listenDescButtonResetState();
//                } else {
//                    SearchFragment.listenDescButtonPlayState();
//                }
//            }
//        });
//    }

    public void stopVoicePlayer(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}