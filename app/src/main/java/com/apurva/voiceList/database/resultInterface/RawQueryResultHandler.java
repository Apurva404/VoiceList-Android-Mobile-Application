package com.apurva.voiceList.database.resultInterface;

import android.database.Cursor;

public interface RawQueryResultHandler {
    void handleRawQueryResult(String queryId, Cursor cursor);
}
