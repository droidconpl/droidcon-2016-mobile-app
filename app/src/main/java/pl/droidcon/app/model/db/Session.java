package pl.droidcon.app.model.db;

import android.os.Parcelable;

import java.util.Date;
import java.util.List;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.OneToOne;

@Entity
public interface Session extends Parcelable {

    @Key
    int getId();

    @Column(name = "start_time")
    Date getDate();

    String getTitle();

    String getDescription();

    int getRoomId();

    String getDisplayHour();

    int getDayId();

    boolean isSingleItem();

    @Column(name = "left_column")
    boolean isLeft();

    @ManyToMany
    List<Speaker> getSpeakers();

    @OneToOne
    Schedule getSchedule();

    @OneToOne
    Notification getNotification();

    @OneToOne
    SessionRow sessionRow();
}
