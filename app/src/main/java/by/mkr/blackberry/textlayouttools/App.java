package by.mkr.blackberry.textlayouttools;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.List;
import DataBase.AppDatabase;
import DataBase.Correction;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;

public class App extends Application/* implements LifecycleOwner*/ {
    /*private LifecycleRegistry _reg;*/

    //public static App instance;

    //private static AppDatabase _database;

    private static List<Correction> _corrections;

    /*
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return _reg;
    }
    */


    @Override
    public void onCreate() {
        super.onCreate();
        //instance = this;
        /*
        _database = Room
                .databaseBuilder(this, AppDatabase.class, "text_tools_db")
                .allowMainThreadQueries()
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                _database.correctionDao().insertAll(new Correction[]{
                                        new Correction(0, "ожон", "оддон"),
                                        new Correction(0, "ожан", "оддан"),
                                        new Correction(0, "ожерж", "оддерж"),
                                        new Correction(0, "ожуб", "оддуб"),
                                        new Correction(0, "уюот", "уббот")
                                });
                            }
                        });
                    }
                })
                .build();
        */

        // Initial corrections setup
        updateCorrections(AppDatabase.getInstance(this).correctionDao().getAll());

        Log.d("ReplacerLog", "! App started");

        /*
        _reg = new LifecycleRegistry(this);
        _reg.markState(Lifecycle.State.CREATED);
        _reg.markState(Lifecycle.State.STARTED);
        LayoutConverter.setObserve(this);
        */
    }

    /*public static App getInstance() {
        return instance;
    }*/

    public static List<Correction> getCorrections() {
        return _corrections;
    }

    public static void updateCorrections(List<Correction> corrections) {
        _corrections = corrections;
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
