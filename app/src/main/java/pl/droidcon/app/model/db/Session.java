package pl.droidcon.app.model.db;

import android.os.Parcelable;

import java.util.Date;
import java.util.List;
import java.util.Set;

import io.requery.Entity;
import io.requery.Key;
import io.requery.ManyToMany;

@Entity
public abstract class Session implements Parcelable{

    @Key
    public
    int id;
    public Date date;
    public String title;
    public String description;
    public int roomId;
    public String displayHour;
    public int dayId;
    public boolean singleItem;
    public boolean left;

    @ManyToMany
    List<Speaker> speaker;
}
