package pl.droidcon.app.dagger.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.rx.RxSupport;
import io.requery.rx.SingleEntityStore;
import io.requery.sql.Configuration;
import io.requery.sql.EntityDataStore;
import io.requery.sql.TableCreationMode;
import pl.droidcon.app.BuildConfig;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.model.db.Models;
import pl.droidcon.app.reminder.Reminder;
import pl.droidcon.app.reminder.ReminderImpl;
import pl.droidcon.app.reminder.ReminderPersistence;
import pl.droidcon.app.reminder.ReminderPersistenceImpl;
import pl.droidcon.app.reminder.SessionReminder;
import pl.droidcon.app.reminder.SessionReminderImpl;
import pl.droidcon.app.rx.BinderUtil;
import pl.droidcon.app.rx.DataSubscription;

@Module
public class LogicModule {

    @Provides
    public BinderUtil provideBinderUtil() {
        return new BinderUtil();
    }

    @Provides
    @Singleton
    public DataSubscription provideDatabaseSubscription() {
        return new DataSubscription();
    }

    @Singleton
    @Provides
    public DatabaseManager provideDatabaseManager() {
        return new DatabaseManager();
    }

    @Provides
    @Singleton
    public SessionReminder provideSessionReminder() {
        return new SessionReminderImpl();
    }

    @Provides
    public ReminderPersistence provideReminderPersistence(Context context) {
        return new ReminderPersistenceImpl(context);
    }

    @Provides
    public Reminder provideReminder(Context context) {
        return new ReminderImpl(context);
    }

    @Singleton
    @Provides
    public SingleEntityStore<Persistable> provideDatabase(Context context) {
        DatabaseSource source = new DatabaseSource(context, Models.DEFAULT, 1);
        if (BuildConfig.DEBUG) {
            // use this in development mode to drop and recreate the tables on every upgrade
            source.setTableCreationMode(TableCreationMode.DROP_CREATE);
        }
        Configuration configuration = source.getConfiguration();
        return RxSupport.toReactiveStore(
                new EntityDataStore<Persistable>(configuration));
    }

}
