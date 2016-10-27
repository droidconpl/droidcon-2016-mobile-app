package pl.droidcon.app.model.api;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import pl.droidcon.app.model.db.SpeakerEntity;

public class SpeakerResponse {

    @SerializedName("people")
    public List<SpeakerEntity> speakers = new ArrayList<>();

    @Override
    public String toString() {
        return "SpeakerResponse{" +
                "speakersIds=" + speakers +
                '}';
    }
}
