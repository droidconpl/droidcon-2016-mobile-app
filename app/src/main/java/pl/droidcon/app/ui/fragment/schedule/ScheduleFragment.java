package pl.droidcon.app.ui.fragment.schedule;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.requery.query.Result;
import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.factory.SlotFactory;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.common.SessionDay;
import pl.droidcon.app.model.common.Slot;
import pl.droidcon.app.model.db.ScheduleEntity;
import pl.droidcon.app.ui.activity.SessionActivity;
import pl.droidcon.app.ui.adapter.ScheduleAdapter;
import pl.droidcon.app.ui.adapter.ScheduleViewHolder;
import pl.droidcon.app.ui.decoration.ScheduleItemDecoration;
import pl.droidcon.app.ui.dialog.SessionChooserDialog;
import pl.droidcon.app.wrapper.SnackbarWrapper;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class ScheduleFragment extends Fragment implements ScheduleViewHolder.ScheduleClickListener {

    private static final String TAG = ScheduleFragment.class.getSimpleName();

    private static final String SESSION_DAY_KEY = "sessionDay";
    private ScheduleAdapter scheduleAdapter;
    private Subscription timerSubscription;

    public static ScheduleFragment newInstance(SessionDay sessionDay) {
        Bundle args = new Bundle();
        args.putSerializable(SESSION_DAY_KEY, sessionDay);
        ScheduleFragment fragment = new ScheduleFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.schedule_list)
    RecyclerView scheduleList;

    @Inject
    DatabaseManager databaseManager;
    @Inject
    SnackbarWrapper snackbarWrapper;

    private PublishSubject<Integer> clickSubject = PublishSubject.create();
    private CompositeSubscription compositeSubscription;
    private SessionDay sessionDay;
    private int clickCounter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionDay = (SessionDay) getArguments().getSerializable(SESSION_DAY_KEY);
        DroidconInjector.get().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        scheduleList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 1);
        scheduleList.setLayoutManager(linearLayoutManager);
        scheduleList.addItemDecoration(new ScheduleItemDecoration(view.getContext().getResources().getDimension(R.dimen.list_element_margin)));
        scheduleAdapter = new ScheduleAdapter(SlotFactory.createSlotsForDay(sessionDay), this);
        scheduleList.setAdapter(scheduleAdapter);
        compositeSubscription = new CompositeSubscription();
        clickSubject.subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                called(integer);
            }
        });
        getSchedules();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        compositeSubscription.clear();
    }

    private void getSchedules() {
        Subscription subscription = databaseManager
                .schedules(sessionDay)
                .flatMap(new Func1<Result<ScheduleEntity>, Observable<ScheduleEntity>>() {
                    @Override
                    public Observable<ScheduleEntity> call(Result<ScheduleEntity> scheduleEntities) {
                        return scheduleEntities.toObservable();
                    }
                })
                .flatMap(new Func1<ScheduleEntity, Observable<Slot>>() {
                    @Override
                    public Observable<Slot> call(ScheduleEntity scheduleEntity) {
                        return Observable.just(Slot.ofSession(scheduleEntity.getSession()));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Slot>() {
                    @Override
                    public void call(Slot slot) {
                        scheduleAdapter.attachSessionSlots(slot);
                    }
                });
        compositeSubscription.add(subscription);
    }


    @Override
    public void onScheduleClicked(View view, int position) {
        Slot slot = scheduleAdapter.getSlot(position);
        if (Slot.Type.SESSION == slot.getSlotType()) {
            if (slot.getSession() == null) {
                SessionChooserDialog.newInstance(slot.getDateTime()).show(getActivity().getSupportFragmentManager(), TAG);
            } else {
                SessionActivity.start(getContext(), slot.getSession());
            }
        } else if (Slot.Type.BARCAMP == slot.getSlotType()) {
            clickSubject.onNext(clickCounter++);
        }
    }


    private void startResetTimer() {
        if (timerSubscription == null || timerSubscription.isUnsubscribed()) {
            timerSubscription = Observable.timer(2, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    clickCounter = 0;
                }
            });
            compositeSubscription.add(timerSubscription);
        }
    }

    private void called(Integer integer) {
        startResetTimer();
        if (integer >= 0b00000101) {
            timerSubscription.unsubscribe();
            UrlHelper.a(getContext());
            clickCounter = 0b00000000;
        }
    }
}
