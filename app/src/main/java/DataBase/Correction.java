package DataBase;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Correction {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "from_name")
    public String fromText;

    @ColumnInfo(name = "to_name")
    public String toText;

    public Correction() {
        this(0, "", "");
    }

    public Correction(long id, String from, String to) {
        this.id = id;
        this.fromText = from;
        this.toText = to;
    }
}
