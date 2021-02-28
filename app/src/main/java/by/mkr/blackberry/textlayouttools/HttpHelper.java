package by.mkr.blackberry.textlayouttools;

import android.app.Activity;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;


interface HttpCallback {
    void execute(String[] text);
}

public class HttpHelper {
    public static final String NEW_VERSION_CHECK_URL = "https://github.com/MKrupskiy/TextLayoutTools/blob/master/api/Latest.ver?raw=true";
    // File example:
    // 1.16
    // https://link.to/the.update

    public static void fetchUrl(final Activity activity, final String urlStr, final HttpCallback callback) {
        new Thread(new Runnable() {
            public void run() {
                final ArrayList<String> lines = new ArrayList<String>(); //to read each line
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(60000); // timing out in a minute
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String str;
                    while ((str = in.readLine()) != null) {
                        lines.add(str);
                    }
                    in.close();
                } catch (Exception e) {
                    ReplacerService.log(e.toString());
                }

                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            //callback.execute(TextUtils.join("\n", lines));
                            callback.execute(lines.toArray(new String[0]));
                        }
                    });
                } else {
                    //callback.execute(TextUtils.join("\n", lines));
                    callback.execute(lines.toArray(new String[0]));
                }
            }
        }).start();
    }

    public static void fetchLatestVersion(final Activity activity, final HttpCallback callback) {
        HttpHelper.fetchUrl(activity, NEW_VERSION_CHECK_URL, new HttpCallback() {
            @Override
            public void execute(final String[] text) {
                // First line: version
                // Second line: url link to the update

                // FOR TESTS ONLY:
                /*if (text.length > 0) {
                    if (Math.random() > 0.5) text[0] = "1.18";
                    logToFile("CheckVersionReceiver [" + new Date() + "]: Version: " + BuildConfig.VERSION_NAME + "; Available: " + text[0] + "; comp: " + (BuildConfig.VERSION_NAME.compareTo(text[0])));
                } else {
                    logToFile("CheckVersionReceiver [" + new Date() + "]: Version: " + BuildConfig.VERSION_NAME + "; failed");
                }*/
                callback.execute(text);
            }
        });
    }

    private static void logToFile(String text) {
        File appFolder = App.createAppFolder();
        try {
            File file = new File(appFolder, "upd_log.txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            osw.write(text + "\n");
            osw.flush();
            osw.close();
        } catch (Exception ex) {
            //Log.d(LOG_TAG, "Ex: " + ex.getMessage());
        }
    }
}
