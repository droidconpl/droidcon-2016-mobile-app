package pl.droidcon.app.model.api;


import java.util.List;


public class SessionResponse {

    public Integer sessionId;
    public List<Integer> speakerId;
    public String sessionType;
    public String sessionTitle;
    public String sessionDescription;

    public Float sessionLength;
    public Integer workshopCapacity;
}
