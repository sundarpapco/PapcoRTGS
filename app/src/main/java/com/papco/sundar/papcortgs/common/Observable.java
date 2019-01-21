package com.papco.sundar.papcortgs.common;

public class Observable<ListenerType> {

    private ListenerType callback;

    public void setCallback(ListenerType callback){
        this.callback=callback;
    }

    public ListenerType getCallback(){
        return callback;
    }

    public void removeCallback(){
        callback=null;
    }
}
