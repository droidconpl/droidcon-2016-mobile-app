package pl.droidcon.app.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;

import javax.inject.Inject;

import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.databinding.SessionChooserDialogBinding;
import pl.droidcon.app.helper.DateTimePrinter;
import pl.droidcon.app.model.db.ScheduleEntity;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.reminder.SessionReminder;
import pl.droidcon.app.ui.view.SessionList;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SessionChooserDialog extends AppCompatDialogFragment implements SessionList.SessionClickListener {

    private static final String TAG = SessionChooserDialog.class.getSimpleName();

    private static final String SESSION_DATE_KEY = "session_date";
    private SessionChooserDialogBinding binding;

    public static SessionChooserDialog newInstance(DateTime sessionDate) {
        Bundle args = new Bundle();
        args.putSerializable(SESSION_DATE_KEY, sessionDate);
        SessionChooserDialog fragment = new SessionChooserDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private DateTime sessionDate;
    private CompositeSubscription compositeSubscription;

    @Inject
    DatabaseManager databaseManager;
    @Inject
    SessionReminder sessionReminder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, getTheme());
        DroidconInjector.get().inject(this);
        sessionDate = (DateTime) getArguments().getSerializable(SESSION_DATE_KEY);
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SessionChooserDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.sessionDate.setText(DateTimePrinter.toPrintableStringWithDay(sessionDate));
        getSessions();
    }

    private void getSessions() {
        Subscription subscribe = databaseManager.sessions(sessionDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SessionEntity>() {
                    @Override
                    public void onCompleted() {
                        binding.sessions.show(SessionChooserDialog.this);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SessionEntity session) {
                        binding.sessions.addSession(session);
                    }
                });
        compositeSubscription.add(subscribe);
    }

    @Override
    public void onSessionClicked(final Session session) {
        Subscription subscription = databaseManager.addToFavourite(session)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ScheduleEntity>() {
                    @Override
                    public void call(ScheduleEntity scheduleEntity) {
                        sessionReminder.addSessionToReminding(session);
                        dismiss();
                    }
                });
        compositeSubscription.add(subscription);
    }
}
