package pl.droidcon.app.model.db;

import java.util.Date;

import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.OneToOne;

@Entity
public class Schedule {

    Date scheduleDate;

    @ForeignKey
    Session session;
}
