package com.papco.sundar.papcortgs.database.common;

public interface TableWorkCallback {

    void onCreateComplete(long result);
    void onReadComplete(Object result);
    void onReadAllComplete(Object result);
    void onUpdateComplete(int result);
    void onDeleteComplete(int result);
}
