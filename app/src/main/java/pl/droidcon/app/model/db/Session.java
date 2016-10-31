package pl.droidcon.app.model.db;

import java.util.Date;
import java.util.List;
import java.util.Set;

import io.requery.Entity;
import io.requery.Key;
import io.requery.ManyToMany;

@Entity
public class Session {

    @Key
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
    List<Speaker> speaker;
}
