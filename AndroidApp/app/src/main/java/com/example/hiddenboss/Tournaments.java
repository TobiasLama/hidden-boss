package com.example.hiddenboss;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class Tournaments implements Parcelable {
    //Tournament class is the representation of tournament objects from the database, that's why is does not follow the naming scheme for its variables
    //It also implements parcelable to be sent between activities
    private String host_id;
    private String id;
    private String game_id;
    private String title;
    private String description;
    private String location;
    private List<String> comments;
    private String start_time, end_time;
    private int start_day, start_month, start_year, end_day, end_month, end_year;
    private List<String> liked_by;
    private List<String> participants;
    private Games game;

    public Tournaments(String host_id, String id, String game_id, String title, String description, String location, List<String> comments, String start_time, String end_time, int start_day, int start_month, int start_year, int end_day, int end_month, int end_year, List<String> liked_by, List<String> participants, Games game) {
        this.host_id = host_id;
        this.id = id;
        this.game_id = game_id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.comments = comments;
        this.start_time = start_time;
        this.end_time = end_time;
        this.start_day = start_day;
        this.start_month = start_month;
        this.start_year = start_year;
        this.end_day = end_day;
        this.end_month = end_month;
        this.end_year = end_year;
        this.liked_by = liked_by;
        this.participants = participants;
        this.game = game;
    }

    protected Tournaments(Parcel in) {
        host_id = in.readString();
        id = in.readString();
        game_id = in.readString();
        title = in.readString();
        description = in.readString();
        location = in.readString();
        comments = in.createStringArrayList();
        start_time = in.readString();
        end_time = in.readString();
        start_day = in.readInt();
        start_month = in.readInt();
        start_year = in.readInt();
        end_day = in.readInt();
        end_month = in.readInt();
        end_year = in.readInt();
        liked_by = in.createStringArrayList();
        participants = in.createStringArrayList();
        game = in.readParcelable(Games.class.getClassLoader());
    }

    public static final Creator<Tournaments> CREATOR = new Creator<Tournaments>() {
        @Override
        public Tournaments createFromParcel(Parcel in) {
            return new Tournaments(in);
        }

        @Override
        public Tournaments[] newArray(int size) {
            return new Tournaments[size];
        }
    };

    public String getHost_id() {
        return host_id;
    }

    public String getId() {
        return id;
    }

    public String getGame_id() {
        return game_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getComments() {
        return comments;
    }

    public String getStart_time() {
        return start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public int getStart_day() {
        return start_day;
    }

    public int getStart_month() {
        return start_month;
    }

    public int getStart_year() {
        return start_year;
    }

    public int getEnd_day() {
        return end_day;
    }

    public int getEnd_month() {
        return end_month;
    }

    public int getEnd_year() {
        return end_year;
    }

    public List<String> getLiked_by() {
        return liked_by;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public Games getGame() {
        return game;
    }

    public String getStartToEnd(){
        Converters converters = new Converters();
        return converters.DateToString(getStart_year(),getStart_month(),getStart_day()) + " - " + converters.DateToString(getEnd_year(),getEnd_month(),getEnd_day());
    }

    @Override
    public String toString() {
        return "Tournaments{" +
                "host_id='" + host_id + '\'' +
                ", id='" + id + '\'' +
                ", game_id='" + game_id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", comments=" + comments +
                ", start_time='" + start_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", start_day=" + start_day +
                ", start_month=" + start_month +
                ", start_year=" + start_year +
                ", end_day=" + end_day +
                ", end_month=" + end_month +
                ", end_year=" + end_year +
                ", liked_by=" + liked_by +
                ", participants=" + participants +
                ", game=" + game +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(host_id);
        dest.writeString(id);
        dest.writeString(game_id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(location);
        dest.writeStringList(comments);
        dest.writeString(start_time);
        dest.writeString(end_time);
        dest.writeInt(start_day);
        dest.writeInt(start_month);
        dest.writeInt(start_year);
        dest.writeInt(end_day);
        dest.writeInt(end_month);
        dest.writeInt(end_year);
        dest.writeStringList(liked_by);
        dest.writeStringList(participants);
        dest.writeParcelable(game, flags);
    }

}
