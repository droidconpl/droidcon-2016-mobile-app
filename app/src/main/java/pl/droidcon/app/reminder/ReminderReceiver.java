package pl.droidcon.app.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.ui.activity.SessionActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = ReminderReceiver.class.getSimpleName();

    private static final int DEFAULT_ID = -1;

    private static final String SESSION_ID_KEY = "session_id";

    @NonNull
    public static Intent createReceiverIntent(Context context, Session session) {
        return new Intent(context, ReminderReceiver.class)
                .putExtra(SESSION_ID_KEY, session.getId());
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "received");

        final int sessionId = intent.getIntExtra(SESSION_ID_KEY, DEFAULT_ID);

        if (DEFAULT_ID == sessionId) {
            return;
        }

        DroidconInjector.get()
                .databaseManager()
                .session(sessionId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SessionEntity>() {
                    @Override
                    public void call(SessionEntity sessionEntity) {
                        removeNotification(context, sessionEntity);
                    }
                });
    }

    private void removeNotification(@NonNull final Context context,
                                    @NonNull final SessionEntity sessionEntity) {
        DroidconInjector.get()
                .databaseManager()
                .removeFromNotification(sessionEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean removed) {
                        if (removed) {
                            showNotification(context, sessionEntity);
                        }
                    }
                });
    }

    private void showNotification(@NonNull Context context, @NonNull SessionEntity session) {
        final Intent sessionIntent = SessionActivity.getSessionIntent(context, session);
        final PendingIntent pendingIntent = createNotificationPendingIntent(context, session, sessionIntent);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        final Notification notification = builder
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.received_session_notification, session.getTitle()))
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(context, R.color.primaryColorDark))
                .build();

        final NotificationManager notificationManager = getNotificationManager(context);
        notificationManager.notify(session.getId(), notification);
    }

    private NotificationManager getNotificationManager(@NonNull Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @DrawableRes
    private int getNotificationIcon() {
        final boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_droidcon_silhouette : R.mipmap.ic_launcher;
    }

    @NonNull
    private PendingIntent createNotificationPendingIntent(@NonNull Context context,
                                                          @NonNull SessionEntity session,
                                                          @NonNull Intent sessionIntent) {
        return PendingIntent.getActivity(
                context,
                session.getId(),
                sessionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
    }
}
