package pl.droidcon.app.database;


import android.content.Context;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.query.WhereAndOr;
import io.requery.rx.SingleEntityStore;
import io.requery.sql.EntityDataStore;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.helper.ScheduleMapper;
import pl.droidcon.app.helper.SessionMapper;
import pl.droidcon.app.helper.SessionNotificationMapper;
import pl.droidcon.app.helper.SpeakerMapper;
import pl.droidcon.app.model.api.AgendaResponse;
import pl.droidcon.app.model.api.Session;
import pl.droidcon.app.model.api.SessionRow;
import pl.droidcon.app.model.api.Speaker;
import pl.droidcon.app.model.api.SpeakerResponse;
import pl.droidcon.app.model.common.Schedule;
import pl.droidcon.app.model.common.ScheduleCollision;
import pl.droidcon.app.model.common.SessionDay;
import pl.droidcon.app.model.common.SessionNotification;
import pl.droidcon.app.model.db.RealmSchedule;
import pl.droidcon.app.model.db.RealmSession;
import pl.droidcon.app.model.db.RealmSessionNotification;
import pl.droidcon.app.model.db.RealmSpeaker;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SpeakerEntity;
import pl.droidcon.app.model.db.SpeakerEntity_SessionEntity;
import pl.droidcon.app.rx.RealmObservable;
import rx.Observable;
import rx.functions.Func1;

public class DatabaseManager {

    private static final String TAG = DatabaseManager.class.getSimpleName();


    @Inject
    Context context;
    @Inject
    SpeakerMapper speakerMapper;
    @Inject
    SessionMapper sessionMapper;
    @Inject
    ScheduleMapper scheduleMapper;
    @Inject
    SessionNotificationMapper sessionNotificationMapper;

    private Map<Class, List<DataObserver>> dataObserverMap;

    public DatabaseManager() {
        DroidconInjector.get().inject(this);
        dataObserverMap = new HashMap<>();
    }

    public void registerDataObserver(DataObserver dataObserver) {
        Log.d(TAG, "register data observer for class=" + dataObserver.getType());
        List<DataObserver> dataObservers = dataObserverMap.get(dataObserver.getType());
        if (dataObservers == null) {
            dataObservers = new ArrayList<>();
        }
        if (dataObservers.contains(dataObserver)) {
            throw new IllegalStateException("Observer already registered");
        }
        dataObservers.add(dataObserver);
        dataObserverMap.put(dataObserver.getType(), dataObservers);
    }

    public void unregisterDataObserver(DataObserver dataObserver) {
        Log.d(TAG, "unregister data observer for class=" + dataObserver.getType());
        List<DataObserver> dataObservers = dataObserverMap.get(dataObserver.getType());
        if (dataObservers == null) {
            throw new IllegalStateException("Not found any registered observers for given type " + dataObserver.getType());
        }
        if (!dataObservers.contains(dataObserver)) {
            throw new IllegalStateException("Observer not registered");
        }
        dataObservers.remove(dataObserver);
    }

    public Observable<Session> sessions(final SessionDay sessionDay) {

        Date beginDate = sessionDay.when.toDate();
        Date endOfDate = sessionDay.when.plusHours(23).toDate();

        List<Session> sessionList = new ArrayList<>();

        final SingleEntityStore<Persistable> store = DroidconInjector.get().getDatabase();
        WhereAndOr<Result<SessionEntity>> query = store.select(SessionEntity.class).where(SessionEntity.DATE.between(beginDate, endOfDate));
        Result<SessionEntity> sessionEntities = query.get();


        Observable<SessionEntity> entityObservable = sessionEntities.toObservable();

        return entityObservable.map(new Func1<SessionEntity, Session>() {
            @Override
            public Session call(SessionEntity sessionEntity) {
                Session session = new Session();
                session.id = sessionEntity.getId();
                session.date = new DateTime(sessionEntity.getDate());
                session.dayId = sessionEntity.getDayId();
                session.title = sessionEntity.getTitle();
                session.description = sessionEntity.getDescription();
                session.left = sessionEntity.isLeft();
                session.sessionDisplayHour = sessionEntity.getDisplayHour();
                session.roomId = sessionEntity.getRoomId();
                session.singleItem = sessionEntity.isSingleItem();

//                Result<SpeakerEntity> query = store
//                        .select(SpeakerEntity.class)
//                        .join(SpeakerEntity_SessionEntity.class)
//                        .on(SpeakerEntity.ID.eq(SpeakerEntity_SessionEntity.SPEAKER_ID))
//                        .where(SpeakerEntity_SessionEntity.SESSION_ID.eq(session.id))
//                        .get();
//
                List<SpeakerEntity> speakerEntities = new ArrayList<SpeakerEntity>();
//
//                try {
//                    SpeakerEntity entity = query.toObservable().toBlocking().first();
//                    speakerEntities.add(entity);
//                } catch (Throwable e) {
//                    e.toString();
//                }


                List<SpeakerEntity_SessionEntity> entities = store.select(SpeakerEntity_SessionEntity.class).where(SpeakerEntity_SessionEntity.SESSION_ID.eq(session.id)).get().toList();

                List<Integer> ids = new ArrayList<Integer>();

                for (SpeakerEntity_SessionEntity entity : entities) {
                    ids.add(entity.getSpeakerId());
                }

                List<SpeakerEntity> speakers = store.select(SpeakerEntity.class).where(SpeakerEntity.ID.in(ids)).get().toList();

                for (SpeakerEntity speakerEntity : speakers) {
                    Speaker speaker = new Speaker();
                    speaker.id = speakerEntity.getId();
                    speaker.firstName = speakerEntity.getFirstName();
                    speaker.lastName = speakerEntity.getLastName();
                    speaker.imageUrl = speakerEntity.getImageUrl();
                    session.getSpeakersList().add(speaker);
                }

                return session;
            }
        });
    }

    public Observable<List<Session>> sessions(final Collection<Integer> sessionIds) {
        return RealmObservable.results(context, new Func1<Realm, RealmResults<RealmSession>>() {
            @Override
            public RealmResults<RealmSession> call(Realm realm) {
                return realm.where(RealmSession.class)
                        .findAll();
            }
        }).map(new Func1<RealmResults<RealmSession>, List<Session>>() {
            @Override
            public List<Session> call(RealmResults<RealmSession> realmSessions) {
                List<RealmSession> rightSessions = new ArrayList<>();
                for (RealmSession realmSession : realmSessions) {
                    if (sessionIds.contains(realmSession.getId())) {
                        rightSessions.add(realmSession);
                    }
                }
                return sessionMapper.fromDBList(rightSessions);
            }
        });
    }

    public Observable<List<Session>> sessions(final DateTime when) {
        return RealmObservable.results(context, new Func1<Realm, RealmResults<RealmSession>>() {
            @Override
            public RealmResults<RealmSession> call(Realm realm) {
                return realm.where(RealmSession.class)
                        .equalTo("date", when.toDate())
                        .findAll();
            }
        }).map(new Func1<RealmResults<RealmSession>, List<Session>>() {
            @Override
            public List<Session> call(RealmResults<RealmSession> realmSessions) {
                return sessionMapper.fromDBList(realmSessions);
            }
        });
    }

    public Observable<Session> session(final int sessionId) {
        return RealmObservable.object(context, new Func1<Realm, RealmSession>() {
            @Override
            public RealmSession call(Realm realm) {
                return realm.where(RealmSession.class)
                        .equalTo("id", sessionId)
                        .findFirst();
            }
        }).map(new Func1<RealmSession, Session>() {
            @Override
            public Session call(RealmSession realmSession) {
                return sessionMapper.fromDB(realmSession);
            }
        });
    }

    public Observable<List<Schedule>> schedules(final SessionDay sessionDay) {
        return RealmObservable.results(context, new Func1<Realm, RealmResults<RealmSchedule>>() {
            @Override
            public RealmResults<RealmSchedule> call(Realm realm) {
                return realm.where(RealmSchedule.class)
                        .between("scheduleDate", sessionDay.when.toDate(), sessionDay.when.plusHours(23).toDate())
                        .findAll();
            }
        }).map(new Func1<RealmResults<RealmSchedule>, List<Schedule>>() {
            @Override
            public List<Schedule> call(RealmResults<RealmSchedule> realmSchedules) {
                return scheduleMapper.fromDBList(realmSchedules);
            }
        });
    }

    public Observable<Boolean> isFavourite(final Session session) {
        return RealmObservable.object(context, new Func1<Realm, RealmSchedule>() {
            @Override
            public RealmSchedule call(Realm realm) {
                return realm.where(RealmSchedule.class)
                        .equalTo("realmSessionId", session.id)
                        .findFirst();
            }
        }).map(new Func1<RealmSchedule, Boolean>() {
            @Override
            public Boolean call(RealmSchedule realmSchedule) {
                return realmSchedule != null;
            }
        });
    }

    public Observable<RealmSchedule> addToFavourite(final Session session) {
        return RealmObservable.object(context, new Func1<Realm, RealmSchedule>() {
            @Override
            public RealmSchedule call(Realm realm) {
                RealmSchedule realmSchedule = new RealmSchedule(session.id, session.date.toDate());
                Schedule schedule = scheduleMapper.fromDB(realmSchedule);
                callScheduleInserted(schedule);
                return realm.copyToRealm(realmSchedule);
            }
        });
    }

    public Observable<Boolean> removeFromFavourite(final Session session) {
        return RealmObservable.object(context, new Func1<Realm, RealmSchedule>() {
            @Override
            public RealmSchedule call(Realm realm) {
                RealmSchedule realmSchedule = realm.where(RealmSchedule.class)
                        .equalTo("realmSessionId", session.id)
                        .findFirst();
                if (realmSchedule != null) {
                    Schedule schedule = scheduleMapper.fromDB(realmSchedule);
                    callScheduleDeleted(schedule);
                    realmSchedule.removeFromRealm();
                }

                return realmSchedule;
            }
        }).map(new Func1<RealmSchedule, Boolean>() {
            @Override
            public Boolean call(RealmSchedule realmSchedule) {
                return realmSchedule != null;
            }
        });
    }

    public Observable<ScheduleCollision> canSessionBeSchedule(final Session session) {
        return RealmObservable.object(context, new Func1<Realm, RealmSchedule>() {
            @Override
            public RealmSchedule call(Realm realm) {
                return realm.where(RealmSchedule.class)
                        .equalTo("scheduleDate", session.date.toDate())
                        .findFirst();
            }
        }).map(new Func1<RealmSchedule, ScheduleCollision>() {
            @Override
            public ScheduleCollision call(RealmSchedule realmSchedule) {
                if (realmSchedule == null) {
                    return new ScheduleCollision(null, false);
                } else {
                    return new ScheduleCollision(scheduleMapper.fromDB(realmSchedule), true);
                }
            }
        });
    }

    public Observable<RealmSessionNotification> addToNotification(final SessionNotification sessionNotification) {
        return RealmObservable.object(context, new Func1<Realm, RealmSessionNotification>() {
            @Override
            public RealmSessionNotification call(Realm realm) {
                RealmSessionNotification realmSessionNotification = sessionNotificationMapper.map(sessionNotification);
                return realm.copyToRealm(realmSessionNotification);
            }
        });
    }

    public Observable<Boolean> removeFromNotification(final SessionNotification sessionNotification) {
        return RealmObservable.object(context, new Func1<Realm, RealmSessionNotification>() {
            @Override
            public RealmSessionNotification call(Realm realm) {
                RealmSessionNotification notification = realm.where(RealmSessionNotification.class)
                        .equalTo("sessionId", sessionNotification.getSessionId())
                        .findFirst();
                if (notification != null) {
                    notification.removeFromRealm();
                }
                return notification;
            }
        }).map(new Func1<RealmSessionNotification, Boolean>() {
            @Override
            public Boolean call(RealmSessionNotification sessionNotification) {
                return sessionNotification != null;
            }
        });
    }

    public Observable<List<SessionNotification>> notifications() {
        return RealmObservable.results(context, new Func1<Realm, RealmResults<RealmSessionNotification>>() {
            @Override
            public RealmResults<RealmSessionNotification> call(Realm realm) {
                return realm.where(RealmSessionNotification.class)
                        .findAll();
            }
        }).map(new Func1<RealmResults<RealmSessionNotification>, List<SessionNotification>>() {
            @Override
            public List<SessionNotification> call(RealmResults<RealmSessionNotification> realmSessionNotifications) {
                return sessionNotificationMapper.fromDBList(realmSessionNotifications);
            }
        });
    }

    public void saveData(AgendaResponse agendaResponse, SpeakerResponse speakerResponse) {

        storeRequery(speakerResponse, agendaResponse);

        List<SessionRow> sessionRows = agendaResponse.sessions;
        List<Session> sessions = new ArrayList<>();
        for (SessionRow sessionRow : sessionRows) {
            sessions.addAll(SessionRow.toSessions(sessionRow));
        }

        List<RealmSession> sessionDBs = sessionMapper.mapList(sessions);
//        List<RealmSpeaker> speakerDBs = speakerMapper.mapList(speakerResponse.speakers);
        List<RealmSpeaker> speakerDBs = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        realm.copyToRealmOrUpdate(sessionDBs);
        List<RealmSpeaker> realmSpeakers = realm.copyToRealmOrUpdate(speakerDBs);

        for (Session session : sessions) {
            List<RealmSpeaker> sessionsSpeaker = speakerMapper.matchFromApi(realmSpeakers, session.speakersIds);
            RealmSession sessionDB = realm
                    .where(RealmSession.class)
                    .equalTo("id", session.id)
                    .findFirst();
            sessionDB.getSpeakers().addAll(sessionsSpeaker);
            realm.copyToRealmOrUpdate(sessionDB);
        }
        realm.commitTransaction();
        realm.close();
    }

    private void storeRequery(SpeakerResponse speakerResponse, AgendaResponse agendaResponse) {
        EntityDataStore<Persistable> database = DroidconInjector.get().getNonRxDatabase();

        for (SpeakerEntity speakerEntity : speakerResponse.speakers) {
            try {
                database.insert(speakerEntity);
            } catch (io.requery.PersistenceException e) {
                database.update(speakerEntity);
                // nop
            }
        }

//        database.select(SpeakerEntity.class).get().toList();

//        List<SpeakerEntity> speakerEntities = database.select(SpeakerEntity.class).get().toList();
//        speakerEntities.toString();

        List<SessionEntity> sessionEntities = new ArrayList<>(agendaResponse.sessions.size());
        for (SessionRow session : agendaResponse.sessions) {
            sessionEntities.addAll(Utils.toSessions(session));
        }


        for (SessionEntity sessionEntity : sessionEntities) {
            try {
                database.insert(sessionEntity);
            } catch (io.requery.PersistenceException e) {
                database.update(sessionEntity);
            }
        }


        database.select(SessionEntity.class).get().toList();

//        database.upsert(sessionEntities).toBlocking().value();
    }


    @SuppressWarnings("unchecked")
    private void callScheduleInserted(Schedule schedule) {
        List<DataObserver> dataObservers = dataObserverMap.get(Schedule.class);
        if (dataObservers != null) {
            for (DataObserver observer : dataObservers) {
                observer.onInsert(schedule);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void callScheduleDeleted(Schedule schedule) {
        List<DataObserver> dataObservers = dataObserverMap.get(Schedule.class);
        if (dataObservers != null) {
            for (DataObserver observer : dataObservers) {
                observer.onDelete(schedule);
            }
        }
    }
}
