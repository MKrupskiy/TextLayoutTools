package by.mkr.blackberry.textlayouttools;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import java.io.File;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("ReplacerLog", "! App started");
    }

    public static File createAppFolder() {
        try {
            File f = new File(Environment.getExternalStorageDirectory(), "TextLayoutTools");
            if (!f.exists()) {
                f.mkdirs();
            }
            return f;
        } catch (Exception ex) {
            Log.d(LOG_TAG, "Ex createAppFolder: " + ex.getMessage());
        }
        return null;
    }
}
