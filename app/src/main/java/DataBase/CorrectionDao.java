package DataBase;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface CorrectionDao {
    @Query("SELECT * FROM correction")
    List<Correction> getAll();

    @Query("SELECT * FROM correction")
    LiveData<List<Correction>> getAllAsync();

    @Query("SELECT * FROM correction WHERE id = :id")
    Correction getById(long id);

    @Update
    void update(Correction correction);

    @Insert
    long insert(Correction correction);

    @Insert
    void insertAll(Correction... corrections);

    @Query("SELECT COUNT(*) FROM correction")
    int count();

    @Delete
    void delete(Correction correction);

    @Query("DELETE FROM correction WHERE id = :id")
    void deleteById(long id);
}
