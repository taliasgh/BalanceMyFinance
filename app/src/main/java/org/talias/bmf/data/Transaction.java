package org.talias.bmf.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {

    public static final String TYPE_INCOME = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

    @PrimaryKey(autoGenerate = true)
    public long id;

    public double amount;
    public String type;
    public String category;
    public long dateMillis;
    public String note;

    /** Required empty constructor so Room can create objects from database rows. */
    public Transaction() {
    }

    /** For new rows before insert; id is assigned by Room. */
    @Ignore
    public Transaction(double amount, String type, String category, long dateMillis, String note) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.dateMillis = dateMillis;
        this.note = note == null ? "" : note;
    }
}
