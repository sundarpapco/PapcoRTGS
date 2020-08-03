package com.papco.sundar.papcortgs.common;

public class Event<T> {

    private boolean isAlreadyHandled=false;
    private T data=null;

    public Event(T data){
        this.data=data;
    }

    public boolean isAlreadyHandled(){
        return isAlreadyHandled;
    }

    public T handleEvent(){

        if(isAlreadyHandled)
            return null;
        else{
            isAlreadyHandled=true;
            return data;
        }

    }

}
