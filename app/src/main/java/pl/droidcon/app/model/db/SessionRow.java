package pl.droidcon.app.model.db;


import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Key;
import io.requery.OneToOne;

@Entity
public interface SessionRow {

    @Key
    int getId();

    int dayId();

    int slotId();

    String slotStart();

    String slotEnd();

    String rowTitle();

    String rowPicture();

    @ForeignKey
    @OneToOne
    Session room1();

    @ForeignKey
    @OneToOne
    Session room2();

    @ForeignKey
    @OneToOne
    Session room3();


    // Either Talk or break
//    String type();

//    String title();
//    String picture();
//
//    @ManyToMany
//    List<Speaker> speakers();
//
//    @ForeignKey
//    @OneToOne
//    Session session();
}
