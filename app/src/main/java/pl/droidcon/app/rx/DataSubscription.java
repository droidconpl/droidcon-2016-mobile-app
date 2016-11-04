package pl.droidcon.app.rx;


import android.util.Log;

import javax.inject.Inject;

import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.http.RestService;
import pl.droidcon.app.model.api.AgendaResponse;
import pl.droidcon.app.model.api.SessionRow;
import pl.droidcon.app.model.api.SpeakerResponse;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.Speaker;
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
    DatabaseManager databaseManager;

    public DataSubscription() {
        DroidconInjector.get().inject(this);
    }


    public void fetchData() {
        restService.getSpeakers()
                .flatMap(new Func1<SpeakerResponse, Observable<SpeakerEntity>>() {
                    @Override
                    public Observable<SpeakerEntity> call(SpeakerResponse speakerResponse) {
                        return Observable.from(speakerResponse.speakers);
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
                        Log.d(TAG, "onCompleted() called");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError() called with: e = [" + e + "]");
                    }

                    @Override
                    public void onNext(SpeakerEntity speakerEntity) {
                        Log.d(TAG, "onNext() called with: speakerEntity = [" + speakerEntity.getId() + "]");
                    }
                });


        restService.getAgenda()
                .flatMap(new Func1<AgendaResponse, Observable<SessionRow>>() {
                    @Override
                    public Observable<SessionRow> call(AgendaResponse agendaResponse) {
                        return Observable.from(agendaResponse.sessions);
                    }
                })
                .flatMap(new Func1<SessionRow, Observable<SessionEntity>>() {
                    @Override
                    public Observable<SessionEntity> call(SessionRow sessionRow) {
                        return Observable.from(Utils.toSessions(sessionRow));
                    }
                })
                .flatMap(new Func1<SessionEntity, Observable<SessionEntity>>() {
                    @Override
                    public Observable<SessionEntity> call(SessionEntity sessionEntity) {
                        return DroidconInjector.get().getDatabase().upsert(sessionEntity).toObservable();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<SessionEntity>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() called");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError() called with: e = [" + e + "]");
                    }

                    @Override
                    public void onNext(SessionEntity sessionEntity) {
                        Log.d(TAG, "onNext() called with: sessionEntity = [" + sessionEntity + "]");
                    }
                });
    }
}
