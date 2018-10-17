package com.papco.sundar.papcortgs;

public interface TableWorkCallback {

    public void onCreateComplete(long result);
    public void onReadComplete(Object result);
    public void onReadAllComplete(Object result);
    public void onUpdateComplete(int result);
    public void onDeleteComplete(int result);
}
