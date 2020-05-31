package DataBase;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;
import java.util.concurrent.Executors;


@Database(entities = {Correction.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract CorrectionDao correctionDao();


    public synchronized static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = buildDatabase(context);
        }
        return INSTANCE;
    }

    public synchronized static void releaseDB() {
        if (INSTANCE != null) {
            INSTANCE.close();
        }
    }

    private static AppDatabase buildDatabase(final Context context) {
        return Room
                .databaseBuilder(context, AppDatabase.class, "text_tools_db")
                .allowMainThreadQueries()
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                getInstance(context).correctionDao().insertAll(
                                        new Correction(0, "ожон", "оддон"),
                                        new Correction(0, "ожан", "оддан"),
                                        new Correction(0, "ожерж", "оддерж"),
                                        new Correction(0, "ожуб", "оддуб"),
                                        new Correction(0, "уюот", "уббот"),
                                        new Correction(0, "стрэ", "стрее"));
                            }
                        });
                    }
                })
                .build();
    }
}
