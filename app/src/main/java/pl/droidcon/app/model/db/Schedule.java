package pl.droidcon.app.model.db;

import java.util.Date;

import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.OneToOne;

@Entity
public interface Schedule {

    @Key
    @Generated
    int getId();

    Date getScheduleDate();

    @ForeignKey
    @OneToOne
    Session getSession();
}
