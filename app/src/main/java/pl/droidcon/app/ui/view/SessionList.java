package pl.droidcon.app.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;


public class SessionList extends LinearLayout {

    public interface SessionClickListener {
        void onSessionClicked(Session session);
    }

    public SessionList(Context context) {
        super(context);
        init();
    }

    public SessionList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SessionList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SessionList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(VERTICAL);
        if (isInEditMode()) {
            showStubData();
        }
    }

    private void showStubData() {
        SessionEntity session = new SessionEntity();
        session.setTitle("stub");
        session.setDescription("stub.description");
        addSession(session);
        show(null);
    }

    List<SessionEntity> sessionEntities = new ArrayList<>();

    public void addSession(SessionEntity session) {
        sessionEntities.add(session);
    }

    public void show(@Nullable SessionClickListener sessionClickListener){
        removeAllViews();
        for (SessionEntity session : sessionEntities) {
            SessionListItem sessionListItem = new SessionListItem(getContext());
            sessionListItem.setSession(session);
            sessionListItem.setSessionClickListener(sessionClickListener);
            addView(sessionListItem);
        }
    }
}
