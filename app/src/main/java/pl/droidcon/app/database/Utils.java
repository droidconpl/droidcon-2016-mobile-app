package pl.droidcon.app.database;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import pl.droidcon.app.model.api.SessionRow;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SpeakerEntity;

public class Utils {

    public static final int ID_DELTA = 1000;

    public static List<SessionEntity> toSessions(@NonNull SessionRow sessionRow) {
        List<SessionEntity> sessions = new ArrayList<>();

        SessionEntity leftSession = new SessionEntity();
        leftSession.setDate(sessionRow.sessionDate.toDate());
        leftSession.setId(sessionRow.id);
        leftSession.setDayId(sessionRow.dayId);
        leftSession.setDisplayHour(sessionRow.sessionDisplayHour);

        leftSession.setTitle(sessionRow.sessionTitle.get(0));
        leftSession.setDescription(sessionRow.sessionDescription.get(0));

        List<Integer> speakerIdsLeft = sessionRow.speakerIds.get(0);
        for (Integer speakerId : speakerIdsLeft) {
            SpeakerEntity speakerEntity = new SpeakerEntity();
            speakerEntity.setId(speakerId);
            leftSession.getSpeaker().add(speakerEntity);
        }

        leftSession.setRoomId(sessionRow.roomId.get(0));
        leftSession.setLeft(true);

        SessionEntity rightSession = new SessionEntity();
        rightSession.setDate(sessionRow.sessionDate.toDate());
        rightSession.setId(sessionRow.id + ID_DELTA);
        rightSession.setDayId(sessionRow.dayId);
        rightSession.setDisplayHour(sessionRow.sessionDisplayHour);


        rightSession.setTitle(sessionRow.sessionTitle.get(1));
        rightSession.setDescription(sessionRow.sessionDescription.get(1));

        List<Integer> speakerIdsRight = sessionRow.speakerIds.get(1);
        for (Integer speakerId : speakerIdsRight) {
            SpeakerEntity speakerEntity = new SpeakerEntity();
            speakerEntity.setId(speakerId);
            rightSession.getSpeaker().add(speakerEntity);
        }

        rightSession.setRoomId(sessionRow.roomId.get(1));
        rightSession.setLeft(false);

        if (!TextUtils.isEmpty(rightSession.getTitle())) {
            sessions.add(rightSession);
        } else {
            leftSession.setSingleItem(true);
        }

        sessions.add(leftSession);
        return sessions;
    }
}
