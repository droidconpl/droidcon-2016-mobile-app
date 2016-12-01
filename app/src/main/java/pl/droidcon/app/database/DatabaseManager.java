package pl.droidcon.app.database;


import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Date;

import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.rx.SingleEntityStore;
import pl.droidcon.app.model.common.SessionDay;
import pl.droidcon.app.model.db.NotificationEntity;
import pl.droidcon.app.model.db.ScheduleEntity;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SpeakerEntity;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

public class DatabaseManager {


    SingleEntityStore<Persistable> store;

    public DatabaseManager(SingleEntityStore<Persistable> store) {
        this.store = store;
    }

    public Observable<Result<SessionEntity>> sessions(final SessionDay sessionDay) {
        Date beginDate = sessionDay.when.toDate();
        Date endOfDate = sessionDay.when.plusHours(23).toDate();

        return store
                .select(SessionEntity.class)
                .where(SessionEntity.DATE.between(beginDate, endOfDate))
                .orderBy(SessionEntity.LEFT)
                .get().toSelfObservable();
    }

    public Observable<Result<SpeakerEntity>> speakers() {
        return store.select(SpeakerEntity.class).get().toSelfObservable();
    }

    public Observable<Result<SessionEntity>> sessions(final Collection<Integer> sessionIds) {
        return store
                .select(SessionEntity.class)
                .where(SessionEntity.ID.in(sessionIds))
                .get().toSelfObservable();
    }

    public Observable<SessionEntity> sessions(final DateTime when) {
        return store
                .select(SessionEntity.class)
                .where(SessionEntity.DATE.eq(when.toDate()))
                .get().toObservable();
    }

    public Observable<SessionEntity> session(final int sessionId) {
        return store.
                select(SessionEntity.class)
                .where(SessionEntity.ID.eq(sessionId))
                .get().toObservable();
    }

    public Observable<Result<ScheduleEntity>> schedules(final SessionDay sessionDay) {
        return store
                .select(ScheduleEntity.class)
                .where(
                        ScheduleEntity.SCHEDULE_DATE
                                .between(
                                        sessionDay.when.toDate(),
                                        sessionDay.when.plusHours(23).toDate()))
                .get().toSelfObservable();
    }

    public Observable<Result<ScheduleEntity>> isFavourite(final Session session) {
        return store
                .select(ScheduleEntity.class)
                .where(ScheduleEntity.SESSION.eq(session))
                .get().toSelfObservable();
    }

    public Observable<ScheduleEntity> addToFavourite(final Session session) {

        ScheduleEntity scheduleEntity = new ScheduleEntity();
        scheduleEntity.setScheduleDate(session.getDate());
        scheduleEntity.setSession(session);

        return store
                .insert(scheduleEntity)
                .toObservable();
    }

    public Single<Boolean> removeFromFavourite(final Session session) {
        return store.delete(ScheduleEntity.class)
                .where(ScheduleEntity.SESSION_ID.eq(session.getId()))
                .get()
                .toSingle()
                .flatMap(new Func1<Integer, Single<Boolean>>() {
                    @Override
                    public Single<Boolean> call(Integer integer) {
                        return Single.just(integer > 0);
                    }
                });
    }

    public Result<ScheduleEntity> canSessionBeSchedule(final Session session) {
        return store
                .select(ScheduleEntity.class)
                .where(ScheduleEntity.SCHEDULE_DATE.eq(session.getDate()))
                .get();
    }

    public Observable<NotificationEntity> addToNotification(final Session session) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setSession(session);

        return store
                .insert(notificationEntity)
                .toObservable();

    }

    public Observable<Boolean> removeFromNotification(final Session session) {
        return store
                .delete(NotificationEntity.class)
                .where(NotificationEntity.SESSION.eq(session))
                .get()
                .toSingle()
                .toObservable()
                .flatMap(new Func1<Integer, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Integer integer) {
                        return Observable.just(integer > 0);
                    }
                });
    }

    public Observable<NotificationEntity> notifications() {
        return store
                .select(NotificationEntity.class)
                .get().toObservable();
    }


}
