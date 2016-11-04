package pl.droidcon.app.model.api;


import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.List;

public class SessionRow {

    public static final int ID_DELTA = 1000;

    @SerializedName("Id")
    public int id;

    public int dayId;

    @SerializedName("speakerIDs")
    public List<List<Integer>> speakerIds;

    public DateTime sessionDate;

    public String sessionDisplayHour;

    public List<String> sessionTitle;

    public List<String> sessionDescription;

    public List<Integer> roomId;


}
