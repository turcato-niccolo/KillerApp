package com.dezen.riccardo.smshandler;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.util.Log;

import androidx.room.Room;

import com.dezen.riccardo.smshandler.database.SmsDatabase;
import com.dezen.riccardo.smshandler.database.SmsEntity;

import java.util.ArrayList;
import java.util.List;

public class SmsUtils {
    private static String[] inboxProjection = {
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.SUBJECT,
            Telephony.Sms.BODY
    };
    public static List<String> getInbox(Context context){
        List<String> list = new ArrayList<>();
        String[] selectionArgs = null;
        String selectionClause = null;
        Cursor mCursor = context.getContentResolver().query(
                Telephony.Sms.CONTENT_URI,
                inboxProjection, selectionClause, selectionArgs,
                Telephony.Sms._ID);
        if(mCursor != null && mCursor.getCount() >= 1){
            mCursor.moveToFirst();
            while(!mCursor.isLast()){
                StringBuilder sb = new StringBuilder()
                        .append(mCursor.getString(0)).append(" ")
                        .append(mCursor.getString(1)).append(" ")
                        .append(mCursor.getString(2)).append(" ")
                        .append(mCursor.getString(3));
                list.add(sb.toString());
                Log.d("SMSUtils", sb.toString());
                mCursor.moveToNext();
            }
            mCursor.close();
        }
        return list;
    }


    public static void logUnreadMessages(Context context){
        new LogTask(context).execute();
    }

    private static class LogTask extends AsyncTask<String, Integer, Void>{
        private Context context;
        public LogTask(Context context){
            this.context = context;
        }
        @Override
        protected Void doInBackground(String... strings) {
            SmsDatabase db = Room.databaseBuilder(context, SmsDatabase.class, SmsHandler.SMS_HANDLER_LOCAL_DATABASE)
                    .enableMultiInstanceInvalidation()
                    .build();
            SmsEntity[] messages = db.access().loadAllSms();
            for(SmsEntity sms : messages){
                db.access().deleteSms(sms);
                Log.e("Unread Message", sms.address+" "+sms.body);
            }
            return null;
        }
    }
}
