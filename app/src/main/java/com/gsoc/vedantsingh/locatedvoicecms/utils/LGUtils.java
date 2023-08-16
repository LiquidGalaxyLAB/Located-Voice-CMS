package com.gsoc.vedantsingh.locatedvoicecms.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gsoc.vedantsingh.locatedvoicecms.R;
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
                session = future.get(3, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                e.printStackTrace();
                session = null;
            }catch (Exception e) {
                e.printStackTrace();
            } finally {
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

            String apiURL = "http://" + aiServerIp + ":" + aiServerPort + "/health";

            String sshCommand = "curl -I " + apiURL;

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(() -> {
                try {
                    ChannelExec channel = (ChannelExec) session.openChannel("exec");
                    channel.setCommand(sshCommand);
                    channel.connect();

                    InputStream inputStream = channel.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Read and discard output
                    }
                    inputStream.close();

                    int exitStatus = channel.getExitStatus();

                    channel.disconnect();
                    session.disconnect();

                    // Return true if the exit status indicates a successful connection (0)
                    return exitStatus == 0;
                } catch (JSchException | IOException e) {
                    e.printStackTrace();
                    return false;
                }
            });

            boolean isServerRunning;
            try {
                isServerRunning = future.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                isServerRunning = false;
                future.cancel(true); // Cancel the task
            }

            executor.shutdown();

            return isServerRunning;
        } catch (Exception e) {
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
                Toast toast = new Toast(context);
                View toast_view = LayoutInflater.from(context).inflate(R.layout.toast_text, null);
                TextView toasttext = toast_view.findViewById(R.id.toasttext);
                toasttext.setText("Audio Generation initiated, please wait...");
                toast.setView(toast_view);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 100);
                toast.show();
            }
        });

        while (!channelssh.isClosed()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        channelssh.disconnect();

        return baos.toByteArray();
    }
}
