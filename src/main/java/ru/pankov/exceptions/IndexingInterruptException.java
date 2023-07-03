package ru.pankov.exceptions;

public class IndexingInterruptException extends InterruptedException{
    public IndexingInterruptException(String errText){
        super(errText);
    }
}
