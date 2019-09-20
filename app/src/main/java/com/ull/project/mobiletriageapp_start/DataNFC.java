package com.ull.project.mobiletriageapp_start;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Clase que contiene la estructura de los distintos tipos de datos que se guardarán
 * en una etiqueta NFC mediante el uso del algoritmo del sistema de triaje Start
 */

class DataNFC implements Parcelable {

    private String tagId_;
    private String color_;
    private double lat_;
    private double long_;
    private String signature_;

    public static Parcelable.Creator<DataNFC> CREATOR = new Parcelable.Creator<DataNFC>(){

        @Override
        public DataNFC createFromParcel(Parcel in){
            return new DataNFC(in);
        }

        @Override
        public DataNFC[] newArray (int size){

            return new DataNFC[size];
        }

    };
    private DataNFC(Parcel in){
        readFromParcel(in);
    }

    public DataNFC(String color){
        this.color_ = color;

    }

    public String getTagId() {
        return this.tagId_;
    }

    public void setTagId(String tagId_) {
        this.tagId_ = tagId_;
    }

    public double getLatitude() {
        return lat_;
    }

    public void setLatitude(double latitude) {
        this.lat_ = latitude;
    }

    public double getLongitude(){
        return this.long_;
    }
    public void setLongitude(double longitude) {
        this.long_ = longitude;
    }

    public void setColor (String color) {
        if (color != null)
        {
            color_ = color;
        }

    }
    public String getColor(){

        return this.color_;
    }
    public String getSignature() {
        return this.signature_;
    }
    public void setSignature(String sign){
        if(sign == null){
            this.signature_ = "";
        }
        this.signature_ = sign;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,int flags){
        dest.writeString(this.getTagId());
        dest.writeString(this.getColor());
        dest.writeDouble(this.getLatitude());
        dest.writeDouble(this.getLongitude());
        dest.writeString(this.getSignature());

    }
    private void readFromParcel(Parcel in){
        this.setTagId(in.readString());
        this.setColor(in.readString());
        this.setLatitude(in.readDouble());
        this.setLongitude(in.readDouble());
        this.setSignature(in.readString());

    }
}