package com.gsoc.vedantsingh.locatedvoicecms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.gsoc.vedantsingh.locatedvoicecms.data.POIsContract;
import com.gsoc.vedantsingh.locatedvoicecms.utils.LGUtils;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* This class makes reference to the functionalities to apply to Liquid Galaxy. This class is the once
*  which is able to reboot the LG, relaunch it or shut it down. It also is able to import files containing
*  a list of POIs and save it persistently in the application data base.*/
public class LGTools extends Fragment {

    Session session;
    private Button showLogos, hideLogos, relaunch, reboot, shutDown, cleanKML;
    private SharedPreferences sharedPreferences;
    public LGTools() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lgtools, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        showLogos = (Button) view.findViewById(R.id.show_logos);
        hideLogos = (Button) view.findViewById(R.id.hide_logos);
        relaunch = (Button) view.findViewById(R.id.relaunch);
        reboot = (Button) view.findViewById(R.id.reboot);
        shutDown = (Button) view.findViewById(R.id.shutdown);
        cleanKML = (Button) view.findViewById(R.id.cleanKmls);
//        setImportPOIsButtonBehaviour();
        setShowLogosButtonBehaviour();
        setHideLogosButtonBehaviour();
        setRelaunchButtonBehaviour();
        setRebootButtonBehaviour();
        setShutDownButtonBehaviour();
        setCleanKMLButtonBehaviour();
        return view;
    }

    private void setShowLogosButtonBehaviour() {
        showLogos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String machinesString = sharedPreferences.getString("Machines", "3");
                    int machines = Integer.parseInt(machinesString);
                    int slave_num = Math.floorDiv(machines, 2) + 2;
                    String slaveName = "slave_" + slave_num;
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
                    showAlertAndExecution(sentence, "show logos");
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();
                    Log.d("Exception Occured", e.getMessage());
                }
            }
        });
    }

    private void setHideLogosButtonBehaviour() {
        hideLogos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String machinesString = sharedPreferences.getString("Machines", "3");
                    int machines = Integer.parseInt(machinesString);
                    int slave_num = Math.floorDiv(machines, 2) + 2;
                    String slaveName = "slave_" + slave_num;
                    String sentence = "chmod 777 /var/www/html/kml/" + slaveName + ".kml; " +
                            "echo '' > /var/www/html/kml/"+ slaveName +".kml";
                    showAlertAndExecution(sentence, "hide logos");
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();
                    Log.d("Exception Occured", e.getMessage());
                }
            }
        });
    }

    private void setCleanKMLButtonBehaviour() {
        cleanKML.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //String sentence = "rm -f /var/www/html/kmls.txt; touch /var/www/html/kmls.txt > /home/lg/log.txt";
//                    String sentence = "chmod 777 /var/www/html/kmls.txt; echo '' > /var/www/html/kmls.txt";
                    String machinesString = sharedPreferences.getString("Machines", "3");
                    String sentence = null;
                    for (int i = 2; i <= Integer.parseInt(machinesString); i++){
                        if( i == 2 ){
                            sentence = "chmod 777 /var/www/html/kml/slave_" + i + ".kml; " +
                                    "echo '' > /var/www/html/kml/slave_"+ i +".kml";
                        }else{
                            sentence += "; chmod 777 /var/www/html/kml/slave_" + i + ".kml; " +
                                    "echo '' > /var/www/html/kml/slave_"+ i +".kml";
                        }
                    }
                    showAlertAndExecution(sentence, "clean kml files");
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();
                    Log.d("Exception Occured", e.getMessage());
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GetSessionTask getSessionTask = new GetSessionTask();
        getSessionTask.execute();
    }

    /*SHUT DOWN*/
    //When shut down Liquid Galaxy button is clicked, the sentence to achieve it is send to the LG.
    private void setShutDownButtonBehaviour() {
        shutDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String password = sharedPreferences.getString("Password", "lg");
                    String machinesString = sharedPreferences.getString("Machines", "3");
                    String sentence = null;
                    for (int i = Integer.parseInt(machinesString); i >= 1; i--){
                        if( i == Integer.parseInt(machinesString) ){
                            sentence = "sshpass -p " + password + " ssh -t lg" + i + " \"echo " + password + " | sudo -S poweroff\"";
                        }else{
                            sentence += "; sshpass -p " + password + " ssh -t lg" + i + " \"echo " + password + " | sudo -S poweroff\"";
                        }
                    }
                    showAlertAndExecution(sentence, "shut down");
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();
                    Log.d("Exception Occured", e.getMessage());
                }
            }
        });
    }

    /*REBOOT*/
    //When reboot Liquid Galaxy button is clicked, the sentence to achieve it is send to the LG.
    private void setRebootButtonBehaviour() {
        reboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String password = sharedPreferences.getString("Password", "lg");
                    String machinesString = sharedPreferences.getString("Machines", "3");
                    String sentence = null;
                    for (int i = Integer.parseInt(machinesString); i >= 1; i--){
                        if( i == Integer.parseInt(machinesString) ){
                            sentence = "sshpass -p " + password + " ssh -t lg" + i + " \"echo " + password + " | sudo -S reboot\"";
                        }else{
                            sentence += "; sshpass -p " + password + " ssh -t lg" + i + " \"echo " + password + " | sudo -S reboot\"";
                        }
                    }
                    showAlertAndExecution(sentence, "reboot");
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();
                    Log.d("Exception Occured", e.getMessage());
                }
            }
        });
    }

    /*RELAUNCH*/
    //When relaunch Liquid Galaxy button is clicked, the sentence to achieve it is send to the LG.
    private void setRelaunchButtonBehaviour() {
        relaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String user = sharedPreferences.getString("User", "lg");
                    String password = sharedPreferences.getString("Password", "lg");
                    String sentence = "/home/" + user + "/bin/lg-relaunch > /home/" + user + "/log.txt;\n " +
                                    "RELAUNCH_CMD=\"\\\n" +
                                    "if [ -f /etc/init/lxdm.conf ]; then\n" +
                                    "  export SERVICE=lxdm\n" +
                                    "elif [ -f /etc/init/lightdm.conf ]; then\n" +
                                    "  export SERVICE=lightdm\n" +
                                    "else\n" +
                                    "  exit 1\n" +
                                    "fi\n" +
                                    "if  [[ \\$(service \\$SERVICE status) =~ 'stop' ]]; then\n" +
                                    "  echo " + password + " | sudo -S service \\${SERVICE} start\n" +
                                    "else\n" +
                                    "  echo " + password + " | sudo -S service \\${SERVICE} restart\n" +
                                    "fi\n" +
                                    "\" && sshpass -p " + password + " ssh -x -t lg@lg1 \"$RELAUNCH_CMD\"";
                    showAlertAndExecution(sentence, "relaunch");
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_galaxy), Toast.LENGTH_LONG).show();
                    Log.d("Exception Occured", e.getMessage());
                }
            }
        });
    }

    /*SHUT DOWN, RELAUNCH and REBOOT*/
    private void showAlertAndExecution(String sentence, String action) {
        // prepare the alert box
        AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity(), R.style.BlackTextAlertDialog);

        // set the message to display
        alertbox.setMessage("Are you sure to " + action + " Liquid Galaxy?");

        // set a positive/yes button and create a listener
        alertbox.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    LGTools.SendLGCommands sendLGCommands = new LGTools.SendLGCommands(sentence, session, getActivity());
                    Future<Void> future = executorService.submit(sendLGCommands);
            }
        });

        // set a negative/no button and create a listener
        alertbox.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            // When button is clicked
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        // display box
        alertbox.show();
    }

    public class SendLGCommands implements Callable<Void> {
        private String sentence;
        private Session session;
        private Context context;

        public SendLGCommands(String sentence, Session session, Context context) {
            this.sentence = sentence;
            this.session = session;
            this.context = context;
        }

        @Override
        public Void call() throws Exception {
            try {
                LGUtils.setConnectionWithLiquidGalaxy(session, sentence, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /*IMPORT POIS*/
//    private void setImportPOIsButtonBehaviour() {
//        importPois.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                final AlertDialog chooseDialog = new AlertDialog.Builder(getActivity()).create();
//                chooseDialog.setTitle(getResources().getString(R.string.import_pois_dialog_title));
//                chooseDialog.setMessage(getResources().getString(R.string.import_pois_dialog_msg));
//                chooseDialog.setButton(Dialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        chooseDialog.dismiss();
//                    }
//                });
//
//                chooseDialog.setButton(Dialog.BUTTON_NEUTRAL, getResources().getString(R.string.import_pois_dialog_fromFile), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        //Complete procedure to get the file with the POIs to import
//                        selectFileToImport();
//                    }
//                });
//
//                chooseDialog.setButton(Dialog.BUTTON_POSITIVE, getResources().getString(R.string.import_pois_dialog_fromPW), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        openBluetoothImport();
//                    }
//                });
//                chooseDialog.show();
//            }
//        });
//    }

    private void openBluetoothImport() {
        ((LGPCAdminActivity) requireActivity()).mViewPager.setCurrentItem(AdminCollectionPagerAdapter.PAGE_BEACONS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //When user select one file
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    String path = uri.toString();
                    //We get the file path by executing one of the following methods, depending the explorer the user uses.
                    String filePath;
                    if (path.toLowerCase().startsWith("file://")) {
                        filePath = (new File(URI.create(path))).getAbsolutePath();
                    } else {
                        filePath = pathTreatment(data.getData().getPath(), Environment.getExternalStorageDirectory().getAbsolutePath());
                    }

                    //We read the file and create the POIs described inside it.
                    ReadImportFileTask readFileImportTask = new ReadImportFileTask(filePath);
                    readFileImportTask.execute();
                }
            }
        }
    }


    private void selectFileToImport() {
        //We use Intent.GATA_GET_CONTENT to let the user select the file to import
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        }
    }


    private String getPOIAttribute(String attribute, String line) {

        int attributeSize = attribute.length();
        int start = line.indexOf("<" + attribute + ">") + 1 + attributeSize + 1;
        int end = line.lastIndexOf("</" + attribute + ">");
        return line.substring(start, end);

    }

    private String getAltitudeMode(String line) {

        int attributeSize = "gx:altitudeMode>".length();
        int start = line.indexOf("<gx:altitudeMode>") + attributeSize + 1;
        int end = line.indexOf("</gx:altitudeMode>");
        return line.substring(start, end);
    }

    private int createCategoryTree(String[] categoryTreeName) {
        int categoryID = 0;

        int fatherCategoryId = 0;
        for (int i = 0; i < categoryTreeName.length - 1; i++) {

            try (Cursor categories = POIsContract.CategoryEntry.getCategoriesByName(getActivity(), categoryTreeName[i].toUpperCase())) {
                if (categories != null && categories.moveToFirst()) {
                    //FatherCategory Exists, we fetch it
                    fatherCategoryId = POIsContract.CategoryEntry.getIdByName(getActivity(), categoryTreeName[i].toUpperCase());
                } else {
                    fatherCategoryId = createNestedCategory(categoryTreeName[i].toUpperCase(), fatherCategoryId);
                }

                String fatherShownName = POIsContract.CategoryEntry.getShownNameByID(getActivity(), fatherCategoryId);

                ContentValues category = new ContentValues();
                category.put(POIsContract.CategoryEntry.COLUMN_NAME, categoryTreeName[i + 1].toUpperCase());
                category.put(POIsContract.CategoryEntry.COLUMN_FATHER_ID, fatherCategoryId);
                category.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, fatherShownName.endsWith("/") ? fatherShownName + categoryTreeName[i + 1].toUpperCase() : fatherShownName + "/" + categoryTreeName[i + 1].toUpperCase());
                category.put(POIsContract.CategoryEntry.COLUMN_HIDE, 0);

                try {
                    Uri insertedUri = POIsContract.CategoryEntry.createNewCategory(getActivity(), category);
                    categoryID = POIsContract.CategoryEntry.getIdByUri(insertedUri);
                } catch (android.database.SQLException e) {
                    Toast.makeText(getActivity(), "Already exists one category with the same name. Please, change it.", Toast.LENGTH_SHORT).show();
                    categoryID = POIsContract.CategoryEntry.getIdByShownName(getActivity(), categoryTreeName[i] + "/");
                    return categoryID;
                }
            }
        }

        return categoryID;
    }

    private int createNestedCategory(String categoryName, int fatherId) {

        int categoryID;
        ContentValues category = new ContentValues();
        category.put(POIsContract.CategoryEntry.COLUMN_NAME, categoryName);
        category.put(POIsContract.CategoryEntry.COLUMN_FATHER_ID, fatherId);
        category.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, categoryName + "/");
        category.put(POIsContract.CategoryEntry.COLUMN_HIDE, 0);

        try {
            Uri insertedUri = POIsContract.CategoryEntry.createNewCategory(getActivity(), category);
            categoryID = POIsContract.CategoryEntry.getIdByUri(insertedUri);
            return categoryID;
        } catch (android.database.SQLException e) {
            Toast.makeText(getActivity(), "Already exists one category with the same name. Please, change it.", Toast.LENGTH_SHORT).show();
            categoryID = POIsContract.CategoryEntry.getIdByShownName(getActivity(), categoryName + "/");
            return categoryID;
        }
    }

    private int createCategory(String categoryName) {

        int categoryID;
        ContentValues category = new ContentValues();
        category.put(POIsContract.CategoryEntry.COLUMN_NAME, categoryName);
        category.put(POIsContract.CategoryEntry.COLUMN_FATHER_ID, 0);
        category.put(POIsContract.CategoryEntry.COLUMN_SHOWN_NAME, categoryName + "/");
        category.put(POIsContract.CategoryEntry.COLUMN_HIDE, 0);

        try {
            Uri insertedUri = POIsContract.CategoryEntry.createNewCategory(getActivity(), category);
            categoryID = POIsContract.CategoryEntry.getIdByUri(insertedUri);
            return categoryID;
        } catch (android.database.SQLException e) {
            Toast.makeText(getActivity(), "Already exists one category with the same name. Please, change it.", Toast.LENGTH_SHORT).show();
            categoryID = POIsContract.CategoryEntry.getIdByShownName(getActivity(), categoryName + "/");
            return categoryID;
        }
    }

    private String pathTreatment(String path, String absolutePath) {

        String firstPathFolder;
        if (path.contains(":")) {
            int start = path.indexOf(":") + 1;
            path = path.substring(start);
        }

        if (path.startsWith("/")) {
            firstPathFolder = path.split("/")[1];
        } else {
            firstPathFolder = path.split("/")[0];
        }
        String[] absoluteFolders = absolutePath.split("/");
        String lastAbsoluteFolder = absoluteFolders[absoluteFolders.length - 1];

        if (firstPathFolder.equals(lastAbsoluteFolder)) {
            if (path.startsWith("/")) {
                path = path.substring(firstPathFolder.length() + 2);
            } else {
                path = path.substring(firstPathFolder.length() + 1);
            }
        }

        return absolutePath + path;
    }

    private class ReadImportFileTask extends AsyncTask<Void, Integer, Void> {

        private ProgressDialog importingDialog;
        private String path;


        ReadImportFileTask(String filePath) {
            this.path = filePath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (importingDialog == null) {
                importingDialog = new ProgressDialog(getActivity());
                importingDialog.setMessage(getResources().getString(R.string.readingFileAndImportingPois));
                importingDialog.setIndeterminate(false);
                importingDialog.setMax(100);
                importingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                importingDialog.setCancelable(true);
                importingDialog.setCanceledOnTouchOutside(false);
                importingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel(true);
                    }
                });
                importingDialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                readFile();
                return null;
            } catch (Exception e) {
                cancel(true);
                return null;
            }
        }

        private List<ContentValues> readFile() {
            List<ContentValues> poisList = new ArrayList<>();
            File file = new File(this.path);
            if (file.exists()) {
                long total = 0;
                try {
                    FileInputStream inputStream = new FileInputStream(file);
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String line;
                    int numLines = countLines(this.path);
                    while ((line = br.readLine()) != null) {
                        //for each POI described inside the file we read and introduce it inside the database.
                        if (!line.equals("")) {
                            total++;
                            readAndImportPOI(line);
                            publishProgress((int) ((total * 100) / numLines));
                        }
                    }
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getActivity(), "File couldn't be opened. Try to open it with a different file explorer, for example 'Root Explorer' once.", Toast.LENGTH_LONG).show();
                                                }
                                            }
                );
            }
            return poisList;
        }

        int countLines(String filename) throws IOException {

            LineNumberReader lnr = new LineNumberReader(new FileReader(new File(filename)));

            lnr.skip(Long.MAX_VALUE);

            return lnr.getLineNumber() + 1;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            importingDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);
            if (importingDialog != null) {
                importingDialog.dismiss();
            }
        }

        private void readAndImportPOI(String line) {
            ContentValues poi = new ContentValues();
            try {
                /**
                 * Added by Ivan Josa
                 */
                int categoryId = getOrCreatePoiCategoryByName(line);

                /**************/
                String name = getPOIName(line);
                String longitude = getPOIAttribute("longitude", line);
                String latitude = getPOIAttribute("latitude", line);
                String altitude = getPOIAttribute("altitude", line);
                String heading = getPOIAttribute("heading", line);
                String tilt = getPOIAttribute("tilt", line);
                String range = getPOIAttribute("range", line);
                String altitudeMode = getAltitudeMode(line);

                poi.put(POIsContract.POIEntry.COLUMN_COMPLETE_NAME, name);
                poi.put(POIsContract.POIEntry.COLUMN_VISITED_PLACE_NAME, name);
                poi.put(POIsContract.POIEntry.COLUMN_LONGITUDE, longitude);
                poi.put(POIsContract.POIEntry.COLUMN_LATITUDE, latitude);
                poi.put(POIsContract.POIEntry.COLUMN_ALTITUDE, altitude);
                poi.put(POIsContract.POIEntry.COLUMN_HEADING, heading);
                poi.put(POIsContract.POIEntry.COLUMN_TILT, tilt);
                poi.put(POIsContract.POIEntry.COLUMN_RANGE, range);
                poi.put(POIsContract.POIEntry.COLUMN_ALTITUDE_MODE, altitudeMode);
                poi.put(POIsContract.POIEntry.COLUMN_HIDE, 0);
                poi.put(POIsContract.POIEntry.COLUMN_CATEGORY_ID, categoryId);

                POIsContract.POIEntry.createNewPOI(getActivity(), poi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String getPOIName(String line) {
            int start = line.indexOf("@") + 1;
            int end = line.lastIndexOf("@");
            return line.substring(start, end);
        }

        private int getOrCreatePoiCategoryByName(String line) {
            int firstArrova = line.indexOf("@");
            int categoryId;
            String categoryName = line.substring(0, firstArrova);


            try (Cursor categories = POIsContract.CategoryEntry.getCategoriesByName(getActivity(), categoryName.toUpperCase())) {
                if (categories != null && categories.moveToFirst()) {
                    //Category Exists, we fetch it
                    categoryId = POIsContract.CategoryEntry.getIdByName(getActivity(), categoryName.toUpperCase());
                } else {
                    //Category not exist, we need to create it
                    String[] categoryTreeNames = categoryName.split("/");
                    if (categoryTreeNames.length > 1) {
                        categoryId = createCategoryTree(categoryTreeNames);
                    } else {
                        categoryId = createCategory(categoryName);
                    }
                }
            }

            return categoryId;
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
            session = LGUtils.getSession(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Void success) {
            super.onPostExecute(success);
        }
    }

}