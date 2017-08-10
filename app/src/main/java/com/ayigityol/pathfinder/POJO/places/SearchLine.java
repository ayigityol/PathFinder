package com.ayigityol.pathfinder.POJO.places;



public class SearchLine {


    private Info from;
    private Info to;

    public SearchLine() {

    }

    public Info getFrom() {
        return from;
    }

    public void setFrom(Info from) {
        this.from = from;
    }

    public Info getTo() {
        return to;
    }

    public void setTo(Info to) {
        this.to = to;
    }

    public SearchLine(Info from, Info to) {
        this.from = from;
        this.to = to;
    }
}
