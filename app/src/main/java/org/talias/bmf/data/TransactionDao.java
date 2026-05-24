package org.talias.bmf.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getById(long id);

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    List<Transaction> getAllNewestFirst();

    @Query("SELECT * FROM transactions WHERE dateMillis >= :start AND dateMillis < :end ORDER BY dateMillis DESC")
    List<Transaction> getBetween(long start, long end);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND dateMillis >= :start AND dateMillis < :end")
    double sumIncomeBetween(long start, long end);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND dateMillis >= :start AND dateMillis < :end")
    double sumExpenseBetween(long start, long end);

    @Delete
    void delete(Transaction transaction);
}
