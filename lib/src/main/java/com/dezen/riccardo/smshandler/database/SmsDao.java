package com.dezen.riccardo.smshandler.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface SmsDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(SmsEntity... entities);
    @Update
    public void updateSms(SmsEntity... entities);
    @Delete
    public void deleteSms(SmsEntity... entities);
    @Query("SELECT * FROM smsentity")
    public SmsEntity[] loadAllSms();
    @Query("SELECT COUNT(id) FROM smsentity")
    public int getCount();
}
