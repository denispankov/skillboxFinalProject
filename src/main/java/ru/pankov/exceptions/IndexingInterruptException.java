package ru.pankov.exceptions;

public class IndexingInterruptException extends Exception{
    public IndexingInterruptException(String errText){
        super(errText);
    }
}
