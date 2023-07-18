package com.gsoc.vedantsingh.locatedvoicecms;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class DriveServiceHelper {
    private static final String APPLICATION_NAME = "Located Voice CMS";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private Drive drive;

    public DriveServiceHelper() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets JSON file or obtain credentials in any other supported way
        // ...

        // Build the OAuth2 credentials using the appropriate scopes
        Credential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId("vedant@located-voice-cms.iam.gserviceaccount.com")
                .setServiceAccountPrivateKeyFromP12File(new File("C:\\Users\\vedan\\Downloads\\located-voice-cms-70c96675fce8.p12"))
                .setServiceAccountScopes(Collections.singletonList(DriveScopes.DRIVE))
//                .setServiceAccountUser("USER_EMAIL_TO_IMPERSONATE")
                .build();

        return credential;
    }

    // Other methods for working with the Google Drive API...
    public List<File> listFilesInFolder(String folderId) throws IOException {
        String query = "'" + folderId + "' in parents";
        List<File> files = drive.files().list().setQ(query).execute().getFiles();
        return files;
    }
}
