package com.company;

import java.time.LocalDateTime;

public class Stats {

    private int filesCounter;
    private int recordsCounter;



    public int getFilesCounter() {
        return filesCounter;
    }

    public void setFilesCounter(int filesCounter) {
        this.filesCounter = filesCounter;
    }

    public int getRecordsCounter() {
        return recordsCounter;
    }

    public void setRecordsCounter(int recordsCounter) {
        this.recordsCounter = recordsCounter;
    }

    public void produceStats(){
        System.out.println(LocalDateTime.now() + ": Number of Files processed=" +
                " "+this.filesCounter+" Number of records processed="+this.recordsCounter);
    }

    public void incrementFileCounter(){

        this.filesCounter++;
    }

    public void incrementRecordCounter(){
        this.recordsCounter++;
    }
}
