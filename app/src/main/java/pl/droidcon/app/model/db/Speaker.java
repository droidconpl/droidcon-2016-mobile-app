package pl.droidcon.app.model.db;

import android.os.Parcelable;

import java.util.List;

import io.requery.Entity;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;

@Entity
public abstract class Speaker implements Parcelable {

    @Key
    int id;
    public String firstName;
    public String lastName;
    public String bio;
    public String imageUrl;
    public String websiteTitle;
    public String websiteLink;
    public String facebookLink;
    public String twitterHandler;
    public String githubLink;
    public String linkedIn;
    public String googlePlus;

    @JunctionTable
    @ManyToMany
    List<Session> session;


}
