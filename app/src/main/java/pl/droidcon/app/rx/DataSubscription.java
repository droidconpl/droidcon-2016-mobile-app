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
import pl.droidcon.app.model.event.NewDataEvent;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class DataSubscription {

    private static final String TAG = DataSubscription.class.getSimpleName();

    @Inject
    BinderUtil binderUtil;
    @Inject
    RestService restService;
    @Inject
    DatabaseManager databaseManager;

    private BehaviorSubject<NewDataEvent> newDataEventSubject = BehaviorSubject.create();

    public DataSubscription() {
        DroidconInjector.get().inject(this);
    }

    public void refresh() {
        newDataEventSubject = BehaviorSubject.create();
        fetchData();
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



//        Observable.zip(restService.getAgenda(), restService.getSpeakers(),
//                new Func2<AgendaResponse, SpeakerResponse, NewDataEvent>() {
//                    @Override
//                    public NewDataEvent call(AgendaResponse agendaResponse, SpeakerResponse speakerResponse) {
//                        Log.d(TAG, "Downloading and saving start.....");
//                        databaseManager.saveData(agendaResponse, speakerResponse);
//                        Log.d(TAG, "Downloading and saving end.....");
//                        return new NewDataEvent();
//                    }
//                })
//                .observeOn(Schedulers.io())
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .subscribe(newDataEventSubject);
    }


    public Subscription bindNewDataEvent(Action1<NewDataEvent> onNext, Action0 onError) {
        return binderUtil.bindProperty(newDataEventSubject, onNext, onError);
    }

    public void unbind(Subscription subscription) {
        if (subscription != null) {
            binderUtil.unbind(subscription);
        }
    }
}
