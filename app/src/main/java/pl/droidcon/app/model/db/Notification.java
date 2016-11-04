package pl.droidcon.app.model.db;

import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.OneToOne;

@Entity
public interface Notification {

    @Key
    @Generated
    int getId();

    @ForeignKey
    @OneToOne
    Session getSession();
}
