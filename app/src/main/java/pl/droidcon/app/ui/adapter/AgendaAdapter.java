package pl.droidcon.app.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import pl.droidcon.app.R;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.Speaker;
import pl.droidcon.app.model.db.SpeakerEntity;
import pl.droidcon.app.model.db.Utils;

public class AgendaAdapter extends RecyclerView.Adapter<BaseSessionViewHolder> {

    private enum ViewType {
        NORMAL_SESSION(0),
        LARGE_NON_SESSION(1),
        LARGE_SESSION(2);
        private int viewType;

        ViewType(int viewType) {
            this.viewType = viewType;
        }

        public int getViewType() {
            return viewType;
        }

        public static ViewType of(int viewType) {
            for (ViewType value : values()) {
                if (viewType == value.getViewType()) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Not supported view type");
        }
    }


    private SortedList<SessionEntity> sessions = new SortedList<>(SessionEntity.class, new SortedListAdapterCallback<SessionEntity>(this) {
        @Override
        public int compare(SessionEntity o1, SessionEntity o2) {
            return (int) (o1.getDate().getTime() - o2.getDate().getTime());
        }

        @Override
        public boolean areContentsTheSame(SessionEntity oldItem, SessionEntity newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(SessionEntity item1, SessionEntity item2) {
            return item1.getId() == item2.getId();
        }
    });

    public AgendaAdapter() {
    }

    public void add(SessionEntity sessionEntity) {
//        Session session = new Session();
//        session.id = sessionEntity.getId();
//        session.date = new DateTime(sessionEntity.getDate());
//        session.dayId = sessionEntity.getDayId();
//        session.title = sessionEntity.getTitle();
//        session.description = sessionEntity.getDescription();
//        session.left = sessionEntity.isLeft();
//        session.sessionDisplayHour = sessionEntity.getDisplayHour();
//        session.roomId = sessionEntity.getRoomId();
//        session.singleItem = sessionEntity.isSingleItem();
////        session.setSpeakersList(sessionEntity.getSpeaker());
//
//        List<Speaker> speakers = sessionEntity.getSpeaker();
//        ArrayList<pl.droidcon.app.model.api.Speaker> sessionSpeakers = new ArrayList<>();
//
//        for (Speaker speaker : speakers) {
//            SpeakerEntity speakerEntity = Utils.fromSpeaker(speaker);
//            pl.droidcon.app.model.api.Speaker speakerSession = new pl.droidcon.app.model.api.Speaker();
//            speakerSession.id = speakerEntity.getId();
//            speakerSession.firstName = speakerEntity.getFirstName();
//            speakerSession.lastName = speakerEntity.getLastName();
//            speakerSession.websiteTitle = speakerEntity.getWebsiteTitle();
//            speakerSession.bio = speakerEntity.getBio();
//            speakerSession.websiteLink = speakerEntity.getWebsiteLink();
//            speakerSession.facebookLink = speakerEntity.getFacebookLink();
//            speakerSession.twitterHandler = speakerEntity.getTwitterHandler();
//            speakerSession.githubLink = speakerEntity.getGithubLink();
//            speakerSession.linkedIn = speakerEntity.getLinkedIn();
//            speakerSession.googlePlus = speakerEntity.getGooglePlus();
//            speakerSession.imageUrl = speakerEntity.getImageUrl();
//
//            sessionSpeakers.add(speakerSession);
//        }
//
//
//        session.setSpeakersList(sessionSpeakers);

        sessions.add(sessionEntity);
    }

    @Override
    public BaseSessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewType type = ViewType.of(viewType);
        View v;
        switch (type) {
            case NORMAL_SESSION:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.agenda_element, parent, false);
                return new AgendaSessionViewHolder(v);
            case LARGE_NON_SESSION:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.agenda_non_session_large_element, parent, false);
                return new AgendaNonSessionLargeViewHolder(v);
            case LARGE_SESSION:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.agenda_session_large_element, parent, false);
                return new AgendaSessionViewHolder(v);
        }

        throw new IllegalStateException("Not valid viewType");
    }

    @Override
    public int getItemViewType(int position) {
        SessionEntity sessionByPosition = getSessionByPosition(position);
        if (!sessionByPosition.singleItem) {
            return ViewType.NORMAL_SESSION.getViewType();
        }
        if (sessionByPosition.getSpeaker().isEmpty()) {
            return ViewType.LARGE_NON_SESSION.getViewType();
        } else {
            return ViewType.LARGE_SESSION.getViewType();
        }
    }

    @Override
    public void onBindViewHolder(BaseSessionViewHolder holder, int position) {
        holder.attachSession(sessions.get(position));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    @NonNull
    public SessionEntity getSessionByPosition(int position) {
        return sessions.get(position);
    }
}
