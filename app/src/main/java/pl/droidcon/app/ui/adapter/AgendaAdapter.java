package pl.droidcon.app.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.droidcon.app.databinding.AgendaElementBinding;
import pl.droidcon.app.databinding.AgendaNonSessionLargeElementBinding;
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


    private SortedList<SessionEntity> sessions = new SortedList<>(SessionEntity.class, new SortedListAdapterCallback<SessionEntity>(this) {
        @Override
        public int compare(SessionEntity o1, SessionEntity o2) {
            return (int) (o1.getDate().getTime() - o2.getDate().getTime());
        }

        @Override
        public boolean areContentsTheSame(SessionEntity oldItem, SessionEntity newItem) {
            return oldItem.equals(newItem) && oldItem.getSpeakers().size() == newItem.getSpeakers().size();
        }

        @Override
        public boolean areItemsTheSame(SessionEntity item1, SessionEntity item2) {
            return item1.getId() == item2.getId() && item1.getSpeakers().size() == item2.getSpeakers().size();
        }
    });

    public AgendaAdapter() {
    }

    public void add(SessionEntity sessionEntity) {
        sessions.add(sessionEntity);
    }

    @Override
    public BaseSessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewType type = ViewType.of(viewType);
        View v;
        switch (type) {
            case NORMAL_SESSION:
                return new AgendaSessionViewHolder(AgendaElementBinding.inflate(inflater, parent, false));
            case LARGE_NON_SESSION:
                return new AgendaNonSessionLargeViewHolder(AgendaNonSessionLargeElementBinding.inflate(inflater, parent, false));
            case LARGE_SESSION:
                return new AgendaSessionViewHolder(AgendaElementBinding.inflate(inflater, parent, false));
        }

        throw new IllegalStateException("Not valid viewType");
    }

    @Override
    public int getItemViewType(int position) {
        SessionEntity sessionByPosition = getSessionByPosition(position);
        if (!sessionByPosition.isSingleItem()) {
            return ViewType.NORMAL_SESSION.getViewType();
        }
        if (sessionByPosition.getSpeakers().isEmpty()) {
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
