package com.example.hiddenboss;

import android.os.Parcel;
import android.os.Parcelable;

//Games are java representations of JSON objects from the database. They also implement parcelable to be sent between activities
public class Games implements Parcelable {
    private String id;
    private String img;
    private String title;

    private Games(Parcel in) {
        id = in.readString();
        img = in.readString();
        title = in.readString();
    }

    public static final Creator<Games> CREATOR = new Creator<Games>() {
        @Override
        public Games createFromParcel(Parcel in) {
            return new Games(in);
        }

        @Override
        public Games[] newArray(int size) {
            return new Games[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Games{" +
                "id='" + id + '\'' +
                ", img='" + img + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(img);
        dest.writeString(title);
    }
}
