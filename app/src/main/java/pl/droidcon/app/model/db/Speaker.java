package pl.droidcon.app.model.db;

import android.os.Parcelable;

import java.util.List;

import io.requery.Entity;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;

@Entity
public interface Speaker extends Parcelable {

    @Key
    int getId();
    String getFirstName();
    String getLastName();
    String getBio();
    String getImageUrl();
    String getWebsiteTitle();
    String getWebsiteLink();
    String getFacebookLink();
    String getTwitterHandler();
    String getGithubLink();
    String getLinkedIn();
    String getGooglePlus();

    @JunctionTable
    @ManyToMany
    List<Session> getSessions();
}
