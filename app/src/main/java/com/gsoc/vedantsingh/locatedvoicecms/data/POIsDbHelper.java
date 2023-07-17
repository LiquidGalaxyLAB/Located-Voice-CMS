package com.gsoc.vedantsingh.locatedvoicecms.data;



import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class POIsDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "poi_controller.db";
    private static final int DATABASE_VERSION = 33;
    private static final int REQUEST_PERMISSION_CODE = 1;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Context context;

    POIsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createCategoryEntryTable());
        db.execSQL(createPOInEntryTable());
        db.execSQL(createTourEntryTable());
        db.execSQL(createTourPOIsEntryTable());
        db.execSQL(createTasksEntryTable());
        createBaseCategories(db);
        createDefaultLgTasks(db);
        insertDefaultData(db);
        Log.d("Permission Storage", "Database initiated");

//        if (hasPermissions()) {
//            saveAudioToDevice(db);
//            Log.d("Permission Storage", "Database done");
//        } else {
//
//            ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, REQUEST_PERMISSION_CODE);
//            Log.d("Permission Storage", "Not done");
//        }
    }

//    private boolean hasPermissions() {
//        for (String permission : PERMISSIONS) {
//            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }

//   @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_PERMISSION_CODE) {
//            boolean permissionsGranted = true;
//            for (int grantResult : grantResults) {
//                if (grantResult != PackageManager.PERMISSION_GRANTED) {
//                    permissionsGranted = false;
//                    break;
//                }
//            }
//            if (permissionsGranted) {
//                saveAudioToDevice(getWritableDatabase());
//            } else {
//                // Handle permission denied case
//            }
//        }
//    }

    private void insertDefaultData(SQLiteDatabase db) {
        try {
            InputStream inputStream = context.getAssets().open("Inserts.sql");
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = r.readLine()) != null) {
                if (!line.equals("\n") && !line.equals("") && !line.contains("/*")) {
                    db.execSQL(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAudioToDevice(SQLiteDatabase db){
        ArrayList<Integer> columnCategories = new ArrayList<>();
        Log.d("Check 1", "audio");
        Cursor cursor = db.query("category", new String[]{"_id"}, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                int data = cursor.getInt(cursor.getColumnIndex("_id"));
                Log.d("Check 2",Integer.toString(data));
                columnCategories.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (int categoryId : columnCategories) {
            String audioFileName = "category" + categoryId + ".mp3"; // Generate the audio file name based on the category ID or any other logic
            String destinationFilePath = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) + "/" + audioFileName;
            Log.d("Check 3",destinationFilePath);

            try {
                int audioResourceId = context.getResources().getIdentifier("category_" + categoryId, "raw", context.getPackageName());
                if (audioResourceId != 0) {
                    InputStream inputStream = context.getResources().openRawResource(audioResourceId); // Replace with the appropriate raw resource ID or file name
                    OutputStream outputStream = new FileOutputStream(destinationFilePath);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    Log.d("Check 4", "Files Saved");

                    // Save the audio file path in the database
                    ContentValues values = new ContentValues();
                    values.put("AudioFilePath", destinationFilePath);
                    db.update("category", values, "_id=?", new String[]{String.valueOf(categoryId)});
                    Log.d("Check 5", "DB Updated");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createDefaultLgTasks(SQLiteDatabase db) {
        String sqlLG = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,isRunning) VALUES ('Liquid Galaxy','Launch Liquid Galaxy Task','/home/lg/bin/startup-script.sh','/home/lg/bin/lg-run \"killall run-earth-bin.sh googleearth googleearth-bin\"','$lgIp','lg','lqgalaxy',0)";
        db.execSQL(sqlLG);

        String stopLG = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,isRunning) VALUES ('Stop Liquid Galaxy','Stop Liquid Galaxy Task','/home/lg/bin/lg-run \"killall run-earth-bin.sh googleearth googleearth-bin\"','','$lgIp','lg','lqgalaxy',0)";
        db.execSQL(stopLG);

        String sqlPeruse = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('Peruse a Rue','Peruse a Rue','/home/lg/asherat666-peruse-a-rue/scripts/lg-peruse-a-rue $lgIp $serverIp 8086 lg','/home/lg/asherat666-peruse-a-rue/scripts/lg-peruse-a-rue-stop $lgIp lg','$serverIp','lg','lq','$serverIp:8086/touchscreen',0)";
        db.execSQL(sqlPeruse);

        String sqlPotree = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('PCVT','Point Cloud Visualization Tool','bash /home/lg/asherat666-peruse-a-rue/scripts/lg-potree $lgIp $serverIp 8086 lg','/home/lg/asherat666-peruse-a-rue/scripts/lg-potree-stop $lgIp lg','$serverIp','lg','lq','$serverIp:8086/lg-potree/library',0)";
        db.execSQL(sqlPotree);

        String sqlDLP = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('DLP','Drone Logistics Platform','export DISPLAY=:0 && bash /home/lg/Desktop/lglab/gsoc16/DLP/start-dlp $lgIp $serverIp:$serverPort','bash /home/lg/Desktop/lglab/gsoc16/DLP/exitdlp','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlDLP);

        String sqlPILT = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('PILT','Panoramic Interactive Live Tracker','/home/lg/Desktop/lglab/gsoc16/PILT/pilt-start $lgIp','/home/lg/Desktop/lglab/python-end $lgIp','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlPILT);

        String sqlFAED = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('FAED','Flying Automated External Defibrilator','export DISPLAY=:0 && bash /home/lg/Desktop/lglab/gsoc15/FAED/faed-start $lgIp','/home/lg/Desktop/lglab/gsoc15/FAED/faed-exit $lgIp','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlFAED);

        String sqlVYD = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('VYD','View Your Data','/home/lg/Desktop/lglab/gsoc15/VYD/vyd-start $lgIp','/home/lg/Desktop/lglab/python-end $lgIp','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlVYD);

        String sqlIBRI = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('IBRI','Interactive Beacon Rescue Interface','/home/lg/Desktop/lglab/gsoc16/IBRI/ibri-start $serverIp $serverPort','/home/lg/Desktop/lglab/python-end $lgIp','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlIBRI);

        String sqlFlOYBD = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('FlOYBD','Fly Over Your Big Data','/home/lg/Desktop/lglab/projectsRunner/mainscrip.sh floybd $lgIp ','/home/lg/Desktop/lglab/projectsRunner/end_django_proj.sh $lgIp','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlFlOYBD);

        String sqlWikimediaDataProject = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('WDLGV','WikiData Liquid Galaxy Visualization','/home/lg/Desktop/lglab/projectsRunner/mainscrip.sh WikimediaDataProject $lgIp ','/home/lg/Desktop/lglab/projectsRunner/end_django_proj.sh $lgIp','$serverIp','lg','lq','$serverIp:$serverPort',0)";
        db.execSQL(sqlWikimediaDataProject);

        String sqlmy_meteorological_station = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('MMS','My Meteorological Station','/home/lg/Desktop/lglab/projectsRunner/mainscrip.sh my_meteorological_station $lgIp ','/home/lg/Desktop/lglab/projectsRunner/end_node_proj.sh $lgIp','$serverIp','lg','lq','$serverIp:3000',0)";
        db.execSQL(sqlmy_meteorological_station);

        String sqlmemories = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('Geographical Memories','','/home/lg/Desktop/lglab/projectsRunner/mainscrip.sh memories $lgIp ','/home/lg/Desktop/lglab/projectsRunner/end_node_proj.sh $lgIp','$serverIp','lg','lq','geographical-memories.firebaseapp.com',0)";
        db.execSQL(sqlmemories);

        String sqlSmartAgroVisualizationTool = "INSERT INTO LG_TASK(Title, Description, Script,Shutdown_Script,IP,User,Password,URL,isRunning) VALUES ('SAVT','Smart Agro Visualization Tool','/home/lg/Desktop/lglab/projectsRunner/mainscrip.sh SmartAgroVisualizationTool $lgIp ','/home/lg/Desktop/lglab/projectsRunner/end_node_proj.sh $lgIp','$serverIp','lg','lq','$serverIp:3001',0)";
        db.execSQL(sqlSmartAgroVisualizationTool);

    }

    private void createBaseCategories(SQLiteDatabase db) {
        db.execSQL(Earth());
        db.execSQL(Moon());
        db.execSQL(Mars());
//        db.execSQL(ImportedFolder());
    }

    private String ImportedFolder() {
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('PW Beacon Imported',(SELECT _ID FROM CATEGORY WHERE NAME LIKE 'EARTH'), 'PW IMPORTED/', 0);";
    }

    private String Earth() {
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('EARTH', 0, 'EARTH/', 0);";
    }

    private String Moon() {
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('MOON', 0, 'MOON/', 0);";
    }

    private String Mars() {
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('MARS', 0, 'MARS/', 0);";
    }

    private String createPOInEntryTable() {
        return "CREATE TABLE poi (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Visited_Place TEXT NOT NULL, Longitude REAL NOT NULL, Latitude REAL NOT NULL, Altitude REAL NOT NULL, Heading REAL NOT NULL, Tilt REAL NOT NULL, Range REAL NOT NULL, Altitude_Mode TEXT NOT NULL, Hide INTEGER NOT NULL, Category INTEGER DEFAULT 0, FOREIGN KEY (Category) REFERENCES category (_id),UNIQUE(Name, Category) ON CONFLICT FAIL  );";
    }

    private String createCategoryEntryTable() {
        return "CREATE TABLE category (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Father_ID INTEGER NOT NULL, Shown_Name TEXT UNIQUE NOT NULL, Hide INTEGER NOT NULL, AudioFilePath TEXT  );";
    }

    private String createTourEntryTable() {
        return "CREATE TABLE tour (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Category INTEGER NOT NULL, Hide INTEGER NOT NULL, Interval_of_time INTEGER NOT NULL, FOREIGN KEY (Category) REFERENCES category (_id)  );";
    }

    private String createTourPOIsEntryTable() {
        return "CREATE TABLE Tour_POIs (_id INTEGER PRIMARY KEY AUTOINCREMENT,Tour INTEGER NOT NULL, POI INTEGER NOT NULL, POI_Order INTEGER NOT NULL, POI_Duration INTEGER DEFAULT 0,  FOREIGN KEY (Tour) REFERENCES tour (_id)  FOREIGN KEY (POI) REFERENCES poi (_id)  );";
    }

    private String createTasksEntryTable() {
        return "CREATE TABLE LG_TASK (_id INTEGER PRIMARY KEY AUTOINCREMENT,Title TEXT NOT NULL, Description TEXT, Script TEXT NOT NULL, Shutdown_Script TEXT NOT NULL, Image BLOB, IP TEXT, User TEXT, Password TEXT,URL TEXT, isRunning INTEGER);";
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS poi");
        db.execSQL("DROP TABLE IF EXISTS tour");
        db.execSQL("DROP TABLE IF EXISTS Tour_POIs");
        db.execSQL("DROP TABLE IF EXISTS LG_TASK");
        onCreate(db);
    }


    void resetDatabase(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS poi");
        db.execSQL("DROP TABLE IF EXISTS tour");
        db.execSQL("DROP TABLE IF EXISTS Tour_POIs");
        db.execSQL("DROP TABLE IF EXISTS LG_TASK");
        onCreate(db);
    }
}
