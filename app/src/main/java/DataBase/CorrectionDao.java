package DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
