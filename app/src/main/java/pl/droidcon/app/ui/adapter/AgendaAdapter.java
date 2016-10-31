package pl.droidcon.app.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;

import pl.droidcon.app.R;
import pl.droidcon.app.model.api.Session;
import pl.droidcon.app.model.db.SessionEntity;

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


    private SortedList<Session> sessions = new SortedList<>(Session.class, new SortedListAdapterCallback<Session>(this) {
        @Override
        public int compare(Session o1, Session o2) {
            return (int) (o1.date.toDate().getTime() - o2.date.toDate().getTime());
        }

        @Override
        public boolean areContentsTheSame(Session oldItem, Session newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Session item1, Session item2) {
            return item1.id == item2.id;
        }
    });

    public AgendaAdapter() {
    }

    public void add(SessionEntity sessionEntity) {
        Session session = new Session();
        session.id = sessionEntity.getId();
        session.date = new DateTime(sessionEntity.getDate());
        session.dayId = sessionEntity.getDayId();
        session.title = sessionEntity.getTitle();
        session.description = sessionEntity.getDescription();
        session.left = sessionEntity.isLeft();
        session.sessionDisplayHour = sessionEntity.getDisplayHour();
        session.roomId = sessionEntity.getRoomId();
        session.singleItem = sessionEntity.isSingleItem();
//        session.setSpeakersList(sessionEntity.getSpeaker());

        sessions.add(session);
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
        Session sessionByPosition = getSessionByPosition(position);
        if (!sessionByPosition.singleItem) {
            return ViewType.NORMAL_SESSION.getViewType();
        }
        if (sessionByPosition.getSpeakersList().isEmpty()) {
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
    public Session getSessionByPosition(int position) {
        return sessions.get(position);
    }
}
