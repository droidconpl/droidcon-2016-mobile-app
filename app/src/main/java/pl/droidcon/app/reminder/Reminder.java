package pl.droidcon.app.reminder;


import android.support.annotation.NonNull;

import pl.droidcon.app.model.db.Session;

public interface Reminder {

    void setRemind(@NonNull Session session);

    void removeRemind(@NonNull Session session);
}
