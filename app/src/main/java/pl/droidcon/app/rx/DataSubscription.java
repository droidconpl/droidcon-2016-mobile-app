package pl.droidcon.app.rx;


import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.SerializedSubject;

public class DataSubscription {

    private static final String TAG = DataSubscription.class.getSimpleName();

    @Inject
    RestService restService;

    @Inject
    DatabaseManager databaseManager;

    private SerializedSubject<DataReady, DataReady> readyBridge = BehaviorSubject
            .<DataReady>create()
            .toSerialized();

    public static class DataReady {

        private final boolean success;

        private DataReady(boolean success) {
            this.success = success;
        }

        @NonNull
        private static DataReady success() {
            return new DataReady(true);
        }

        @NonNull
        private static DataReady failure() {
            return new DataReady(false);
        }

        public boolean isSuccess() {
            return success;
        }
    }

    public DataSubscription() {
        DroidconInjector.get().inject(this);
    }

    @NonNull
    public Observable<DataReady> observeReadyData() {
        return readyBridge.asObservable();
    }

    public void fetchData() {
        readyBridge = BehaviorSubject.<DataReady>create().toSerialized();

        final Observable<Iterable<SpeakerEntity>> speakers = getSpeakerEntityObservable();

        final Observable<Iterable<SessionEntity>> sessions = getSessionEntityObservable();


        Observable
                .zip(speakers, sessions,
                        new Func2<Iterable<SpeakerEntity>, Iterable<SessionEntity>, DataReady>() {
                            @Override
                            public DataReady call(Iterable<SpeakerEntity> speakerEntities,
                                                  Iterable<SessionEntity> sessionEntities) {
                                return DataReady.success();
                            }
                        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DataReady>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted() called");
                    }

                    @Override
                    public void onError(Throwable e) {
                        readyBridge.onNext(DataReady.failure());
                    }

                    @Override
                    public void onNext(DataReady dataReady) {
                        readyBridge.onNext(dataReady);
                    }
                });
    }

    @NonNull
    private Observable<Iterable<SessionEntity>> getSessionEntityObservable() {
        return restService.getAgenda()
                .map(new Func1<AgendaResponse, Iterable<SessionRow>>() {
                    @Override
                    public List<SessionRow> call(AgendaResponse agendaResponse) {
                        return agendaResponse.sessions;
                    }
                })
                .flatMap(new Func1<Iterable<SessionRow>, Observable<Iterable<SessionEntity>>>() {
                    @Override
                    public Observable<Iterable<SessionEntity>> call(Iterable<SessionRow> sessionRows) {
                        return Observable.just(
                                Utils.toSessions(sessionRows)
                        );
                    }
                })
                .map(new Func1<Iterable<SessionEntity>, Iterable<SessionEntity>>() {
                    @Override
                    public Iterable<SessionEntity> call(Iterable<SessionEntity> sessionEntities) {
                        return DroidconInjector
                                .get()
                                .getDatabase()
                                .upsert(sessionEntities)
                                .toBlocking()
                                .value();
                    }
                });
    }

    @NonNull
    private Observable<Iterable<SpeakerEntity>> getSpeakerEntityObservable() {
        return restService.getSpeakers()
                .flatMap(new Func1<SpeakerResponse, Observable<SpeakerEntity>>() {
                    @Override
                    public Observable<SpeakerEntity> call(SpeakerResponse speakerResponse) {
                        return Observable.from(speakerResponse.speakers);
                    }
                })
                .flatMap(new Func1<Speaker, Observable<SpeakerEntity>>() {
                    @Override
                    public Observable<SpeakerEntity> call(Speaker speaker) {
                        return Observable
                                .just(Utils.fromSpeaker(speaker));
                    }
                })
                .toList()
                .map(new Func1<Iterable<SpeakerEntity>, Iterable<SpeakerEntity>>() {
                    @Override
                    public Iterable<SpeakerEntity> call(Iterable<SpeakerEntity> speakerEntities) {
                        return DroidconInjector
                                .get()
                                .getDatabase()
                                .upsert(speakerEntities)
                                .toBlocking()
                                .value();
                    }
                });
    }
}
