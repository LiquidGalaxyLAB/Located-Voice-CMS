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
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final com.google.api.services.drive.Drive mDriveService;
    static MediaPlayer mediaPlayer;
//    public static boolean mediaPlayerRunning = false;


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
            });
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopVoicePlayer(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}