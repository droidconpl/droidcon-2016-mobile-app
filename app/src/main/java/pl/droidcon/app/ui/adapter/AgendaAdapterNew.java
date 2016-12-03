package pl.droidcon.app.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.droidcon.app.databinding.AgendaElementBinding;
import pl.droidcon.app.databinding.AgendaElementNewBinding;
import pl.droidcon.app.databinding.AgendaNonSessionLargeElementBinding;
import pl.droidcon.app.model.db.SessionRowEntity;

public class AgendaAdapterNew extends RecyclerView.Adapter<BaseSessionViewHolder> {

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


    private SortedList<SessionRowEntity> sessions = new SortedList<>(SessionRowEntity.class, new SortedListAdapterCallback<SessionRowEntity>(this) {
        @Override
        public int compare(SessionRowEntity o1, SessionRowEntity o2) {
            return o1.slotId() - o2.slotId();
        }

        @Override
        public boolean areContentsTheSame(SessionRowEntity oldItem, SessionRowEntity newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(SessionRowEntity item1, SessionRowEntity item2) {
            return item1.getId() == item2.getId();
        }
    });

    public AgendaAdapterNew() {
    }

    public void add(SessionRowEntity sessionEntity) {
        sessions.add(sessionEntity);
    }

    @Override
    public BaseSessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewType type = ViewType.of(viewType);
        View v;
        switch (type) {
            case NORMAL_SESSION:
                return new AgendaSessionRowViewHolder(AgendaElementNewBinding.inflate(inflater, parent, false));
            case LARGE_NON_SESSION:
                return new AgendaNonSessionLargeViewHolder(AgendaNonSessionLargeElementBinding.inflate(inflater, parent, false));
        }

        throw new IllegalStateException("Not valid viewType");
    }

    @Override
    public int getItemViewType(int position) {
        SessionRowEntity sessionByPosition = getSessionByPosition(position);

        if(sessionByPosition.room1() == null)
            return ViewType.LARGE_NON_SESSION.getViewType();
        else
            return ViewType.NORMAL_SESSION.getViewType();

//        if (TextUtils.isEmpty(sessionByPosition.rowTitle()))
//            return ViewType.NORMAL_SESSION.getViewType();
//        else
//            return ViewType.LARGE_NON_SESSION.getViewType();

//        if (!sessionByPosition.isSingleItem()) {
//            return ViewType.NORMAL_SESSION.getViewType();
//        }
//        if (sessionByPosition.getSpeakers().isEmpty()) {
//            return ViewType.LARGE_NON_SESSION.getViewType();
//        } else {
//            return ViewType.LARGE_SESSION.getViewType();
//        }
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
    public SessionRowEntity getSessionByPosition(int position) {
        return sessions.get(position);
    }
}
