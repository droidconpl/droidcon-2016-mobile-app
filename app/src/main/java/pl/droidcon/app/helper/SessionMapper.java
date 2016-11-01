package pl.droidcon.app.helper;


import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.Speaker;
import pl.droidcon.app.model.db.RealmSession;
import pl.droidcon.app.model.db.RealmSpeaker;

public class SessionMapper implements Mapper<SessionEntity, RealmSession> {

    private SpeakerMapper speakerMapper;

    public SessionMapper(SpeakerMapper speakerMapper) {
        this.speakerMapper = speakerMapper;
    }

    @Override
    public RealmSession map(SessionEntity session) {
        RealmSession realmSession = new RealmSession();
        realmSession.setId(session.getId());
        realmSession.setDate(session.getDate());
        realmSession.setTitle(session.getTitle());
        realmSession.setDescription(session.getDescription());
        realmSession.setRoomId(session.getRoomId());
        realmSession.setDisplayHour(session.getDisplayHour());
        realmSession.setDayId(session.getDayId());
        realmSession.setSingleItem(session.isSingleItem());
        realmSession.setLeft(session.isLeft());
        return realmSession;
    }

    @Override
    public RealmList<RealmSession> mapList(List<SessionEntity> sessions) {
        RealmList<RealmSession> realmSessions = new RealmList<>();
        for (SessionEntity session : sessions) {
            realmSessions.add(map(session));
        }
        return realmSessions;
    }

    @Override
    public RealmList<RealmSession> matchFromApi(List<RealmSession> databaseObjects, List<Integer> ids) {
        RealmList<RealmSession> dbs = new RealmList<>();
        for (Integer id : ids) {
            for (RealmSession orig : databaseObjects) {
                if (id.equals(orig.getId())) {
                    dbs.add(orig);
                }
            }
        }
        return dbs;
    }

    @Override
    public List<SessionEntity> fromDBList(List<RealmSession> realmSessions) {
        List<SessionEntity> sessions = new ArrayList<>();
        for (RealmSession realmSession : realmSessions) {
            sessions.add(fromDB(realmSession));
        }
        return sessions;
    }

    @Override
    public SessionEntity fromDB(RealmSession realmSession) {
        SessionEntity session = new SessionEntity();
        session.setId(realmSession.getId());
        session.setDate(realmSession.getDate());
        session.setTitle(realmSession.getTitle());
        session.setDescription(realmSession.getDescription());
        session.setRoomId(realmSession.getRoomId());
        session.setDisplayHour(realmSession.getDisplayHour());
        session.setDayId(realmSession.getDayId());
        session.setSingleItem(realmSession.isSingleItem());
        session.setLeft(realmSession.isLeft());
//        RealmList<RealmSpeaker> speakers = realmSession.getSpeakers();
//        session.speaker = speakerMapper.fromDBList(speakers);
        return session;
    }
}
