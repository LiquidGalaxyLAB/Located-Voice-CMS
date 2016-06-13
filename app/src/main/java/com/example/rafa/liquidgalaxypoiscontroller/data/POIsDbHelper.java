package com.example.rafa.liquidgalaxypoiscontroller.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class POIsDbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "poi_controller.db";
    private static final int DATABASE_VERSION = 33;

    public POIsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createCategoryEntryTable());
        db.execSQL(createPOInEntryTable());
        db.execSQL(createTourEntryTable());
        db.execSQL(createTourPOIsEntryTable());
        createBaseCategories(db);
    }

    private void createBaseCategories(SQLiteDatabase db) {
        db.execSQL(Earth());
        db.execSQL(Moon());
        db.execSQL(Mars());
    }

    private String Earth() {
        String SQL_CREATE_EARTH_CATEGORY = "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('EARTH', 0, 'EARTH/', 0);";
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('EARTH', 0, 'EARTH/', 0);";
    }

    private String Moon() {
        String SQL_CREATE_MOON_CATEGORY = "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('MOON', 0, 'MOON/', 0);";
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('MOON', 0, 'MOON/', 0);";
    }

    private String Mars() {
        String SQL_CREATE_MARS_CATEGORY = "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('MARS', 0, 'MARS/', 0);";
        return "INSERT INTO category(Name, Father_ID, Shown_Name, Hide) VALUES ('MARS', 0, 'MARS/', 0);";
    }

    private String createPOInEntryTable() {
        String SQL_CREATE_POI_TABLE = "CREATE TABLE poi (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT UNIQUE NOT NULL, Visited_Place TEXT NOT NULL, Longitude REAL NOT NULL, Latitude REAL NOT NULL, Altitude REAL NOT NULL, Heading REAL NOT NULL, Tilt REAL NOT NULL, Range REAL NOT NULL, Altitude_Mode TEXT NOT NULL, Hide INTEGER NOT NULL, Category INTEGER DEFAULT 0, FOREIGN KEY (Category) REFERENCES category (_id)  );";
        return "CREATE TABLE poi (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT UNIQUE NOT NULL, Visited_Place TEXT NOT NULL, Longitude REAL NOT NULL, Latitude REAL NOT NULL, Altitude REAL NOT NULL, Heading REAL NOT NULL, Tilt REAL NOT NULL, Range REAL NOT NULL, Altitude_Mode TEXT NOT NULL, Hide INTEGER NOT NULL, Category INTEGER DEFAULT 0, FOREIGN KEY (Category) REFERENCES category (_id)  );";
    }

    private String createCategoryEntryTable() {
        String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE category (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Father_ID INTEGER NOT NULL, Shown_Name TEXT UNIQUE NOT NULL, Hide INTEGER NOT NULL  );";
        return "CREATE TABLE category (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Father_ID INTEGER NOT NULL, Shown_Name TEXT UNIQUE NOT NULL, Hide INTEGER NOT NULL  );";
    }

    private String createTourEntryTable() {
        String SQL_CREATE_TOUR_TABLE = "CREATE TABLE tour (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Category INTEGER NOT NULL, Hide INTEGER NOT NULL, Interval_of_time INTEGER NOT NULL, FOREIGN KEY (Category) REFERENCES category (_id)  );";
        return "CREATE TABLE tour (_id INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT NOT NULL, Category INTEGER NOT NULL, Hide INTEGER NOT NULL, Interval_of_time INTEGER NOT NULL, FOREIGN KEY (Category) REFERENCES category (_id)  );";
    }

    private String createTourPOIsEntryTable() {
        String SQL_CREATE_TOUR_TABLE = "CREATE TABLE Tour_POIs (_id INTEGER PRIMARY KEY AUTOINCREMENT,Tour INTEGER NOT NULL, POI INTEGER NOT NULL, POI_Order INTEGER NOT NULL, POI_Duration INTEGER DEFAULT 0,  FOREIGN KEY (Tour) REFERENCES tour (_id)  FOREIGN KEY (POI) REFERENCES poi (_id)  );";
        return "CREATE TABLE Tour_POIs (_id INTEGER PRIMARY KEY AUTOINCREMENT,Tour INTEGER NOT NULL, POI INTEGER NOT NULL, POI_Order INTEGER NOT NULL, POI_Duration INTEGER DEFAULT 0,  FOREIGN KEY (Tour) REFERENCES tour (_id)  FOREIGN KEY (POI) REFERENCES poi (_id)  );";
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS category");
        db.execSQL("DROP TABLE IF EXISTS poi");
        db.execSQL("DROP TABLE IF EXISTS tour");
        db.execSQL("DROP TABLE IF EXISTS Tour_POIs");
        onCreate(db);
    }
}
