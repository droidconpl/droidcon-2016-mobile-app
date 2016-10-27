package pl.droidcon.app.model.db;

import java.util.Date;
import java.util.Set;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToMany;

@Entity
public class Session {

    @Key
    @Generated
    int id;
    Date date;
    String title;
    String description;
    int roomId;
    String displayHour;
    int dayId;
    boolean singleItem;
    boolean left;

    @ManyToMany
    Set<Speaker> speaker;
}
