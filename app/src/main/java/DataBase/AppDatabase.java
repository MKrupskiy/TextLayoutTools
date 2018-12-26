package DataBase;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Correction.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CorrectionDao correctionDao();
}
