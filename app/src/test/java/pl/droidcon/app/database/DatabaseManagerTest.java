package pl.droidcon.app.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.sql.Configuration;
import io.requery.sql.EntityDataStore;
import io.requery.sql.TableCreationMode;
import pl.droidcon.app.BuildConfig;
import pl.droidcon.app.model.db.Models;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SpeakerEntity;

/**
 * Created by rudy on 10/31/16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 17)
public class DatabaseManagerTest {


    private EntityDataStore<Persistable> dataStore;

    @Before
    public void setup() {
        DatabaseSource source = new DatabaseSource(RuntimeEnvironment.application, Models.DEFAULT, 1);
        source.setTableCreationMode(TableCreationMode.DROP_CREATE);
        Configuration configuration = source.getConfiguration();
        dataStore = new EntityDataStore<>(configuration);

        dataStore.transaction().begin();
    }

    @After
    public void tearDown() {
        dataStore.transaction().rollback();
        ;
    }

    @Test
    public void abc() {

        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setId(1);
        sessionEntity.setTitle("Session title #1");

        SpeakerEntity speakerEntity = new SpeakerEntity();
        speakerEntity.setId(12);
        speakerEntity.setFirstName("John");
        speakerEntity.setLastName("Doe");

        SpeakerEntity speakerEntity1 = new SpeakerEntity();
        speakerEntity1.setId(13);
        speakerEntity1.setFirstName("Marcy");
        speakerEntity1.setLastName("Eod");


        sessionEntity.getSpeakers().add(speakerEntity);
        sessionEntity.getSpeakers().add(speakerEntity1);

        dataStore.insert(speakerEntity);
        dataStore.insert(speakerEntity1);

        SessionEntity insert = dataStore.insert(sessionEntity);

        SessionEntity result = dataStore.select(SessionEntity.class).get().first();

        assert result.getSpeakers().size() == 2;
    }
}