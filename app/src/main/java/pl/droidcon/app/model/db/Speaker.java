package pl.droidcon.app.model.db;

import java.util.List;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;

@Entity
abstract class Speaker {

    @Key
    @Generated
    int id;
    String firstName;
    String lastName;
    String bio;
    String imageUrl;
    String websiteTitle;
    String websiteLink;
    String facebookLink;
    String twitterHandler;
    String githubLink;
    String linkedIn;
    String googlePlus;

    @JunctionTable
    @ManyToMany
    List<Session> session;
}
