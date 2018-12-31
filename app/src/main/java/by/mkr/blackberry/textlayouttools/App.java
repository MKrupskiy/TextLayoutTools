package by.mkr.blackberry.textlayouttools;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.concurrent.Executors;

import DataBase.AppDatabase;
import DataBase.Correction;

public class App extends Application/* implements LifecycleOwner*/ {
    /*private LifecycleRegistry _reg;*/

    //public static App instance;

    private static AppDatabase database;

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
        database = Room
                .databaseBuilder(this, AppDatabase.class, "text_tools_db")
                .allowMainThreadQueries()
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                database.correctionDao().insertAll(new Correction[]{
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

    public static AppDatabase getDatabase() {
        return database;
    }
}
