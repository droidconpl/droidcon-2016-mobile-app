package pl.droidcon.app.ui.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SessionRowEntity;

public abstract class BaseSessionViewHolder extends RecyclerView.ViewHolder {

    public BaseSessionViewHolder(View itemView) {
        super(itemView);
    }

    abstract public Session getSession();

    public abstract void attachSession(SessionRowEntity sessionRowEntity);
}
