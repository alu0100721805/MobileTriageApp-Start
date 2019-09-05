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
    private double latitude_;
    private double longitude_;
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

    public String getTagId_() {
        return tagId_;
    }

    public void setTagId_(String tagId_) {
        this.tagId_ = tagId_;
    }

    public double getLatitude() {
        return latitude_;
    }

    public void setLatitude(double latitude) {
        this.latitude_ = latitude;
    }

    public double getLongitude_(){
        return longitude_;
    }
    public void setLongitude(double longitude) {
        this.longitude_ = longitude;
    }

    public void setColor (String color) {
        if (color != null)
        {
            color_ = color;
        }

    }
    public String getColor_(){
        return color_;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,int flags){
        dest.writeString(tagId_);
        dest.writeString(color_);
        dest.writeDouble(latitude_);
        dest.writeDouble(longitude_);

    }
    private void readFromParcel(Parcel in){
        this.setTagId_(in.readString());
       this.setColor(in.readString());
        this.setLatitude(in.readDouble());
        this.setLongitude(in.readDouble());

    }
}