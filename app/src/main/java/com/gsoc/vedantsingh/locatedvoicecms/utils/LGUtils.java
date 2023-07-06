package com.gsoc.vedantsingh.locatedvoicecms.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Ivan Josa on 7/07/16.
 */
public class LGUtils {

    private static Session session = null;

    public static Session getSession(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String user = prefs.getString("User", "lg");
        String password = prefs.getString("Password", "lq");
        String hostname = prefs.getString("HostName", "192.168.56.102");
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


}
