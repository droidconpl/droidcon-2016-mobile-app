package pl.droidcon.app.rx;


import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.http.RestService;
import pl.droidcon.app.model.api.AgendaRow;
import pl.droidcon.app.model.api.AgendaRowDetails;
import pl.droidcon.app.model.api.SessionResponse;
import pl.droidcon.app.model.common.Room;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SessionRowEntity;
import pl.droidcon.app.model.db.SpeakerEntity;
import pl.droidcon.app.model.db.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DataSubscription {

    private static final String TAG = DataSubscription.class.getSimpleName();

    @Inject
    RestService restService;

    @Inject
    SingleEntityStore<Persistable> database;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public DataSubscription() {
        DroidconInjector.get().inject(this);

        database = DroidconInjector.get().getDatabase();
    }


    public void fetchData() {
        restService.getSpeakers()
                .flatMap(new Func1<List<SpeakerEntity>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(List<SpeakerEntity> speakerEntities) {

                        List<SpeakerEntity> entities = new ArrayList<>(speakerEntities.size());

                        for (SpeakerEntity speakerEntity : speakerEntities) {
                            entities.add(Utils.fromSpeaker(speakerEntity));
                        }
                        database.upsert(entities).toBlocking().value();

                        return Observable.just(speakerEntities.size());
                    }
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() called - speakers");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer speakersSize) {
                        Log.d(TAG, "Inserted " + speakersSize + " speakers");
                    }
                });


        restService.getSessions()
                .flatMap(new Func1<List<SessionResponse>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(List<SessionResponse> sessionResponses) {

                        List<SessionEntity> entities = new ArrayList<>(sessionResponses.size());

                        for (SessionResponse sessionResponse : sessionResponses) {
                            SessionEntity sessionEntity = new SessionEntity();
                            sessionEntity.setId(sessionResponse.sessionId);
                            sessionEntity.setTitle(sessionResponse.sessionTitle);
                            sessionEntity.setDescription(sessionResponse.sessionDescription);


                            for (Integer integer : sessionResponse.speakerId) {
                                SpeakerEntity speakerEntity = new SpeakerEntity();
                                speakerEntity.setId(integer);
                                sessionEntity.getSpeakers().add(speakerEntity);
                            }

                            entities.add(sessionEntity);
                        }

                        database.upsert(entities).toBlocking().value();

                        return Observable.just(sessionResponses.size());
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() called - sessions");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer slotsSize) {
                        Log.d(TAG, "Inserted " + slotsSize + " slots");
                    }
                });

        restService.getAgenda()
                .flatMap(new Func1<List<AgendaRow>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(List<AgendaRow> agendaRows) {

                        List<SessionRowEntity> entities = new ArrayList<>(agendaRows.size());

                        for (AgendaRow agendaRow : agendaRows) {
                            entities.add(parseAndStoreAgendaRow(agendaRow));
                        }

                        DroidconInjector.get().getDatabase().upsert(entities).toBlocking().value();

                        return Observable.just(agendaRows.size());
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() called - agenda");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer sessionsSize) {
                        Log.d(TAG, "Inserted " + sessionsSize + " sessions");
                    }
                });

    }

    private SessionRowEntity parseAndStoreAgendaRow(AgendaRow agendaRow) {
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

        return sessionRowEntity;
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
