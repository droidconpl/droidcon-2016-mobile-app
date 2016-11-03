package pl.droidcon.app.reminder;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import javax.inject.Inject;

import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.model.db.NotificationEntity;
import pl.droidcon.app.model.db.Session;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ReminderPersistenceImpl implements ReminderPersistence {

    private static final String TAG = ReminderPersistenceImpl.class.getSimpleName();

    private static final String SHARED_PREFERENCES_NAME = "reminder";
    private static final String REMINDING_KEY = "reminding";

    private SharedPreferences sharedPreferences;

    @Inject
    DatabaseManager databaseManager;

    public ReminderPersistenceImpl(Context context) {
        DroidconInjector.get().inject(this);
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean isReminding() {
        return sharedPreferences.getBoolean(REMINDING_KEY, true);
    }

    @Override
    public void setReminding(boolean reminding) {
        sharedPreferences.edit()
                .putBoolean(REMINDING_KEY, reminding)
                .apply();
    }

    @Override
    public void addSessionToReminding(@NonNull final Session session) {
        databaseManager.addToNotification(session)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<NotificationEntity>() {
                    @Override
                    public void call(NotificationEntity notificationEntity) {
                        Log.d(TAG, "Added session " + session.getTitle() + " to notifications");
                    }
                });
    }

    @Override
    public void removeSessionFromReminding(@NonNull final Session session) {
        databaseManager.removeFromNotification(session)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        Log.d(TAG, "Removed notification for session " + session.getTitle());
                    }
                });
    }

    @Override
    public void sessionsToRemind(final Subscriber<? super Session> topSubscriber) {
        databaseManager.notifications()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())

                .subscribe(new Subscriber<NotificationEntity>() {
                    @Override
                    public void onCompleted() {
                        topSubscriber.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(NotificationEntity notificationEntity) {
                        topSubscriber.onNext(notificationEntity.getSession());
//                        databaseManager.session(notificationEntity.getSession().getId())
//                                .subscribe(new Action1<Session>() {
//                                    @Override
//                                    public void call(Session session) {
//                                        topSubscriber.onNext(session);
//                                    }
//                                });
                    }
                });

//                .subscribe(new Action1<List<NotificationEntity>>() {
//                    @Override
//                    public void call(List<NotificationEntity> sessionNotifications) {
//
//
//                    }
//                });
    }
}
