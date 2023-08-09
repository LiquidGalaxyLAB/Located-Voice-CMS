package com.gsoc.vedantsingh.locatedvoicecms.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Ivan Josa on 7/07/16.
 */
public class LGUtils {

    private static Session session = null;

    public static Session getSession(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String user = prefs.getString("User", "lg");
        String password = prefs.getString("Password", "lg");
        String hostname = prefs.getString("HostName", "172.26.17.21");
        int port = Integer.parseInt(prefs.getString("Port", "22"));

        JSch jsch = new JSch();

        try {
            if (session == null || !session.isConnected()) {
                session = jsch.getSession(user, hostname, port);
                session.setPassword(password);

                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);
                session.connect(Integer.MAX_VALUE);
            } else {
                session.sendKeepAliveMsg();
                return session;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return session;
    }

    public static String setConnectionWithLiquidGalaxy(Session session, String command, Context context) throws JSchException, IOException {

        if (session == null || !session.isConnected()) {
            session = getSession(context);
        }

        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        channelssh.setOutputStream(baos);

        channelssh.setCommand(command);
        Log.d("Command built", command);
        channelssh.connect();
        Log.d("SSH","Command sent to LG");
        channelssh.disconnect();

        return baos.toString();
    }

    public static Session checkConnectionStatus(Session session, Context context){
        if (session == null || !session.isConnected()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Session> future = executor.submit(() -> {
                return getSession(context);
            });

            try {
                // Wait for 5 seconds to get the result from getSession
                session = future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                // Timeout occurred, handle the timeout case (skip it, for example)
                e.printStackTrace();
                session = null;
            }catch (Exception e) {
                // Handle any exceptions that may occur during getSession
                e.printStackTrace();
            } finally {
                // Shutdown the executor
                executor.shutdown();
            }
        }

        return session;
    }

    public static boolean checkAIServerConnection(Session session, Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String aiServerIp = prefs.getString("AIServerIP", "172.28.26.84");
            String aiServerPort = prefs.getString("AIServerPort", "5000");

            // Create the API URL for health check on server Y
            String apiURL = "http://" + aiServerIp + ":" + aiServerPort + "/health";

            // Create the SSH command to execute on server X
            String sshCommand = "curl -I " + apiURL;

            // Run the SSH command on server X
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(sshCommand);
            channel.connect();

            // Read the response from the SSH channel
            InputStream inputStream = channel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder responseBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line).append("\n");
            }
            inputStream.close();

            // Get the exit status of the SSH command
            int exitStatus = channel.getExitStatus();

            // Disconnect the SSH channel and session
            channel.disconnect();
            session.disconnect();

            // Return true if the exit status indicates a successful connection (0)
            return exitStatus == 0;
        } catch (JSchException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static byte[] executeAudioCommandWithResponse(Session session, String command, Context context) throws JSchException, IOException {
        if (session == null || !session.isConnected()) {
            session = LGUtils.getSession(context);
        }

        ChannelExec channelssh = (ChannelExec) session.openChannel("exec");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        channelssh.setOutputStream(baos);

        channelssh.setCommand(command);
        Log.d("Audio Command built", command);
        channelssh.connect();
        Log.d("SSH","Command sent to LG");

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // This code will run on the main thread
                Toast.makeText(context, "Audio Generation initiated, please wait...", Toast.LENGTH_SHORT).show();
            }
        });

        // Wait for the command to complete
        while (!channelssh.isClosed()) {
            try {
                Thread.sleep(100); // Add some delay between checks
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        channelssh.disconnect();

        return baos.toByteArray();
    }
}
