package pl.droidcon.app.model.db;

import java.util.Date;

import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;

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

    @ForeignKey
     Speaker speaker;
}
