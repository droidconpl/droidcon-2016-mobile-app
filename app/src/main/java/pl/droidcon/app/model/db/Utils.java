package pl.droidcon.app.model.db;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import pl.droidcon.app.model.api.SessionRow;

public final class Utils {

    private static final int ID_DELTA = 1000;

    @NonNull
    private static List<SessionEntity> toSessions(@NonNull SessionRow sessionRow) {
        final List<SessionEntity> sessions = new ArrayList<>();

        final SessionEntity leftSession = new SessionEntity();
        leftSession.setDate(sessionRow.sessionDate.toDate());
        leftSession.setId(sessionRow.id);
        leftSession.setDayId(sessionRow.dayId);
        leftSession.setDisplayHour(sessionRow.sessionDisplayHour);

        leftSession.setTitle(sessionRow.sessionTitle.get(0));
        leftSession.setDescription(sessionRow.sessionDescription.get(0));

        final List<Integer> speakerIdsLeft = sessionRow.speakerIds.get(0);
        for (Integer speakerId : speakerIdsLeft) {
            SpeakerEntity speakerEntity = new SpeakerEntity();
            speakerEntity.setId(speakerId);
            leftSession.getSpeakers().add(speakerEntity);
        }

        leftSession.setRoomId(sessionRow.roomId.get(0));
        leftSession.setLeft(true);

        final SessionEntity rightSession = new SessionEntity();
        rightSession.setDate(sessionRow.sessionDate.toDate());
        rightSession.setId(sessionRow.id + ID_DELTA);
        rightSession.setDayId(sessionRow.dayId);
        rightSession.setDisplayHour(sessionRow.sessionDisplayHour);


        rightSession.setTitle(sessionRow.sessionTitle.get(1));
        rightSession.setDescription(sessionRow.sessionDescription.get(1));

        final List<Integer> speakerIdsRight = sessionRow.speakerIds.get(1);
        for (Integer speakerId : speakerIdsRight) {
            SpeakerEntity speakerEntity = new SpeakerEntity();
            speakerEntity.setId(speakerId);
            rightSession.getSpeakers().add(speakerEntity);
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

    @NonNull
    public static SpeakerEntity fromSpeaker(@NonNull Speaker speaker) {
        final SpeakerEntity speakerEntity = new SpeakerEntity();

        speakerEntity.setId(speaker.getId());
        speakerEntity.setFirstName(speaker.getFirstName());
        speakerEntity.setLastName(speaker.getLastName());
        speakerEntity.setBio(speaker.getBio());
        speakerEntity.setImageUrl(speaker.getImageUrl());
        speakerEntity.setWebsiteTitle(speaker.getWebsiteTitle());
        speakerEntity.setWebsiteLink(speaker.getWebsiteLink());
        speakerEntity.setFacebookLink(speaker.getFacebookLink());
        speakerEntity.setTwitterHandler(speaker.getTwitterHandler());
        speakerEntity.setGithubLink(speaker.getGithubLink());
        speakerEntity.setLinkedIn(speaker.getLinkedIn());
        speakerEntity.setGooglePlus(speaker.getGooglePlus());

        return speakerEntity;
    }

    @NonNull
    public static Iterable<SessionEntity> toSessions(@NonNull Iterable<SessionRow> sessionRows) {
        final List<SessionEntity> sessionEntities = new ArrayList<>();

        for (SessionRow sessionRow : sessionRows) {
            sessionEntities.addAll(
                    toSessions(sessionRow)
            );
        }

        return sessionEntities;
    }

    private Utils() {
    }
}
