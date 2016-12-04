package pl.droidcon.app.rx;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.http.RestService;
import pl.droidcon.app.model.api.AgendaRow;
import pl.droidcon.app.model.api.AgendaRowDetails;
import pl.droidcon.app.model.api.SessionResponse;
import pl.droidcon.app.model.common.Room;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SessionRowEntity;
import pl.droidcon.app.model.db.Speaker;
import pl.droidcon.app.model.db.SpeakerEntity;
import pl.droidcon.app.model.db.Utils;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DataSubscription {

    private static final String TAG = DataSubscription.class.getSimpleName();

    @Inject
    RestService restService;
    @Inject
    DatabaseManager databaseManager;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public DataSubscription() {
        DroidconInjector.get().inject(this);
    }


    public void fetchData() {
        restService.getSpeakers()
                .flatMap(new Func1<List<SpeakerEntity>, Observable<SpeakerEntity>>() {
                    @Override
                    public Observable<SpeakerEntity> call(List<SpeakerEntity> speakerEntities) {
                        return Observable.from(speakerEntities);
                    }
                })
                .flatMap(new Func1<Speaker, Observable<SpeakerEntity>>() {
                    @Override
                    public Observable<SpeakerEntity> call(Speaker speaker) {
                        return DroidconInjector.get().getDatabase().upsert(Utils.fromSpeaker(speaker)).toObservable();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<SpeakerEntity>() {
                    @Override
                    public void onCompleted() {
//                        Log.d(TAG, "onCompleted() called");
                    }

                    @Override
                    public void onError(Throwable e) {
//                        Log.d(TAG, "onError() called with: e = [" + e + "]");
                    }

                    @Override
                    public void onNext(SpeakerEntity speakerEntity) {
//                        Log.d(TAG, "onNext() called with: speakerEntity = [" + speakerEntity.getId() + "]");
                    }
                });


//        restService.getAgenda()
//                .flatMap(new Func1<AgendaResponse, Observable<SessionRow>>() {
//                    @Override
//                    public Observable<SessionRow> call(AgendaResponse agendaResponse) {
//                        return Observable.from(agendaResponse.sessions);
//                    }
//                })
//                .flatMap(new Func1<SessionRow, Observable<SessionEntity>>() {
//                    @Override
//                    public Observable<SessionEntity> call(SessionRow sessionRow) {
//                        return Observable.from(Utils.toSessions(sessionRow));
//                    }
//                })
//                .flatMap(new Func1<SessionEntity, Observable<SessionEntity>>() {
//                    @Override
//                    public Observable<SessionEntity> call(SessionEntity sessionEntity) {
//                        return DroidconInjector.get().getDatabase().upsert(sessionEntity).toObservable();
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .subscribe(new Subscriber<SessionEntity>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "onCompleted() called");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "onError() called with: e = [" + e + "]");
//                    }
//
//                    @Override
//                    public void onNext(SessionEntity sessionEntity) {
//                        Log.d(TAG, "onNext() called with: sessionEntity = [" + sessionEntity + "]");
//                    }
//                });


        restService.getSessions()
                .flatMap(new Func1<List<SessionResponse>, Observable<SessionResponse>>() {
                    @Override
                    public Observable<SessionResponse> call(List<SessionResponse> sessions) {
                        return Observable.from(sessions);
                    }
                })
                .flatMap(new Func1<SessionResponse, Observable<SessionEntity>>() {
                    @Override
                    public Observable<SessionEntity> call(SessionResponse sessionResponse) {
                        SessionEntity sessionEntity = new SessionEntity();
                        sessionEntity.setId(sessionResponse.sessionId);
                        sessionEntity.setTitle(sessionResponse.sessionTitle);
                        sessionEntity.setDescription(sessionResponse.sessionDescription);


                        for (Integer integer : sessionResponse.speakerId) {
                            SpeakerEntity speakerEntity = new SpeakerEntity();
                            speakerEntity.setId(integer);
                            sessionEntity.getSpeakers().add(speakerEntity);
                        }


                        return DroidconInjector.get().getDatabase().upsert(sessionEntity).toObservable();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<SessionEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SessionEntity session) {

                    }
                });

        restService.getData()
                .flatMap(new Func1<List<AgendaRow>, Observable<AgendaRow>>() {
                    @Override
                    public Observable<AgendaRow> call(List<AgendaRow> agendaRows) {
                        return Observable.from(agendaRows);
                    }
                })
                .flatMap(new Func1<AgendaRow, Observable<SessionRowEntity>>() {
                    @Override
                    public Observable<SessionRowEntity> call(AgendaRow agendaRow) {
                        return parseAndStoreAgendaRow(agendaRow).toObservable();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<SessionRowEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SessionRowEntity agendaRow) {

                    }
                });

    }

    private Single<SessionRowEntity> parseAndStoreAgendaRow(AgendaRow agendaRow) {
        SessionRowEntity sessionRowEntity = new SessionRowEntity();


        sessionRowEntity.dayId(agendaRow.dayId);
        sessionRowEntity.slotId(agendaRow.slotId);
        sessionRowEntity.setId(agendaRow.dayId * 100 + agendaRow.slotId);

        sessionRowEntity.slotStart(agendaRow.slotStart);
        sessionRowEntity.slotEnd(agendaRow.slotEnd);

        AgendaRowDetails room1 = agendaRow.slotArray.get(0);

        if (room1.slotSession.length() > 0) {
            SessionEntity sessionEntity = buildSessionEntity(agendaRow, room1);
            sessionEntity.setRoomId(Room.ROOM_1.id);
            sessionRowEntity.room1(sessionEntity);

        } else {
            SessionEntity room1Entity = new SessionEntity();

            room1Entity.setId(room1.slotTitle.hashCode());
            room1Entity.setTitle(room1.slotTitle);

            sessionRowEntity.room1(room1Entity);
        }

        AgendaRowDetails room2 = agendaRow.slotArray.get(1);

        if (room2.slotSession.length() > 0) {
            SessionEntity sessionEntity = buildSessionEntity(agendaRow, room2);
            sessionEntity.setRoomId(Room.ROOM_2.id);
            sessionRowEntity.room2(sessionEntity);
        } else {
            SessionEntity room2Entity = new SessionEntity();

            room2Entity.setId(room2.slotTitle.hashCode());
            room2Entity.setTitle(room2.slotTitle);

            sessionRowEntity.room2(room2Entity);
        }

        AgendaRowDetails room3 = agendaRow.slotArray.get(2);

        if (room3.slotSession.length() > 0) {
            SessionEntity sessionEntity = buildSessionEntity(agendaRow, room3);
            sessionEntity.setRoomId(Room.ROOM_3.id);
            sessionRowEntity.room3(sessionEntity);
        } else {
            SessionEntity room3Entity = new SessionEntity();

            room3Entity.setId(room3.slotTitle.hashCode());
            room3Entity.setTitle(room3.slotTitle);

            sessionRowEntity.room3(room3Entity);
        }

        sessionRowEntity.rowTitle(firstNotEmpty(room1.slotTitle, room2.slotTitle, room3.slotTitle));
        sessionRowEntity.rowPicture(firstNotEmpty(room1.slotPicture, room2.slotPicture, room3.slotPicture));

        sessionRowEntity.sessionType(agendaRow.sessionType);

        return DroidconInjector.get().getDatabase().upsert(sessionRowEntity);
    }

    @NonNull
    private SessionEntity buildSessionEntity(AgendaRow agendaRow, AgendaRowDetails room1) {
        SessionEntity room1Entity = new SessionEntity();
        room1Entity.setId(Integer.parseInt(room1.slotSession));


        try {
            String fullDate = (agendaRow.dayId == 1 ? "2016-12-08 " : "2016-12-09 ") + agendaRow.slotStart;
            Date date = simpleDateFormat.parse(fullDate);
            room1Entity.setDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return room1Entity;
    }

    String firstNotEmpty(String... texts) {

        for (String text : texts) {
            if (!TextUtils.isEmpty(text))
                return text;
        }

        return "";
    }
}
