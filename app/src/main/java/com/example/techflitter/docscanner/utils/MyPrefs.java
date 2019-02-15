package com.example.techflitter.docscanner.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class MyPrefs {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor spEditor;

    private final String DATABASE_NAME="DocScannerPrefs";
    private final String DOC_COUNT="docCount";

    int docCount;

    public MyPrefs(Context context){
        sharedPreferences=context.getSharedPreferences(DATABASE_NAME, Context.MODE_PRIVATE);
    }

    public int getDocCount() {
        return sharedPreferences.getInt(DOC_COUNT,1);
    }

    public void setDocCount(int docCount) {
        spEditor=sharedPreferences.edit();
        spEditor.putInt(DOC_COUNT,docCount);
        spEditor.commit();
    }
}
