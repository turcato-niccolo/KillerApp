package com.dezen.riccardo.smshandler.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SmsEntity.class}, version = 1)
public abstract class SmsDatabase extends RoomDatabase {
    public abstract SmsDao access();
}