package pl.droidcon.app.ui.fragment.agenda;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.List;

import javax.inject.Inject;

import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.databinding.AgendaFragmentBinding;
import pl.droidcon.app.model.common.SessionDay;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SessionRowEntity;
import pl.droidcon.app.model.ui.SwipeRefreshColorSchema;
import pl.droidcon.app.rx.DataSubscription;
import pl.droidcon.app.ui.adapter.AgendaAdapterNew;
import pl.droidcon.app.ui.view.RecyclerItemClickListener;
import pl.droidcon.app.wrapper.SnackbarWrapper;


public class AgendaFragment extends RxFragment implements RecyclerItemClickListener.OnItemClickListener {

    private static final String TAG = AgendaFragment.class.getSimpleName();
    private static final String SESSION_DAY_KEY = "sessionDay";

    @Inject
    SnackbarWrapper snackbarWrapper;
    @Inject
    SwipeRefreshColorSchema swipeRefreshColorSchema;
    @Inject
    DatabaseManager databaseManager;
    @Inject
    DataSubscription dataSubscription;
    private SessionDay sessionDay;

    private AgendaAdapterNew agendaAdapter = new AgendaAdapterNew();
    AgendaFragmentBinding binding;

    public static AgendaFragment newInstance(SessionDay sessionDay) {
        Bundle args = new Bundle();
        args.putSerializable(SESSION_DAY_KEY, sessionDay);
        AgendaFragment fragment = new AgendaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        sessionDay = (SessionDay) arguments.getSerializable(SESSION_DAY_KEY);
        DroidconInjector.get().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AgendaFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.agendaFragmentSwipeRefreshLayout.setColorSchemeColors(swipeRefreshColorSchema.getColors());
        binding.agendaFragmentSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataSubscription.fetchData();
                binding.agendaFragmentSwipeRefreshLayout.setRefreshing(false);
            }
        });
        binding.agendaView.setHasFixedSize(true);
//        GridLayoutManager mLayoutManager = new GridLayoutManager(view.getContext(), 3);
//        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
        //return 2 for single item as single item occupies all width
//                return agendaAdapter.getSessionByPosition(position).isSingleItem() ? 2 : 1;
//            }
//        });
        binding.agendaView.setLayoutManager(new LinearLayoutManager(getContext()));
//        binding.agendaView.addItemDecoration(new SpacesItemDecoration(view.getContext().getResources().getDimension(R.dimen.list_element_margin)));
        binding.agendaView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), this));
        binding.agendaView.setAdapter(agendaAdapter);
        getSessions();
    }

    public void showErrorSnackBar() {
        if (getView() != null) {
            binding.agendaFragmentSwipeRefreshLayout.setRefreshing(false);
            snackbarWrapper.showSnackbar(getView(), R.string.loading_error);
        }
    }

    private void getSessions() {
//        Observable
//                .combineLatest(databaseManager.sessionRows(0), databaseManager.speakers(), new Func2<Result<SessionRowEntity>, Result<SpeakerEntity>, Result<SessionRowEntity>>() {
//                    @Override
//                    public Result<SessionRowEntity> call(Result<SessionRowEntity> sessionEntities, Result<SpeakerEntity> speakerEntities) {
//                        return sessionEntities;
//                    }
//                })
//        databaseManager.
//                sessionRows(0)
//                .flatMap(new Func1<Result<SessionRowEntity>, Observable<SessionRowEntity>>() {
//                    @Override
//                    public Observable<SessionRowEntity> call(Result<SessionRowEntity> sessionRowEntities) {
//                        return sessionRowEntities.toObservable();
//                    }
//                })
//                .compose(this.<SessionRowEntity>bindToLifecycle())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<SessionRowEntity>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        showErrorSnackBar();
//                    }
//
//                    @Override
//                    public void onNext(SessionRowEntity sessionEntity) {
//                        agendaAdapter.add(sessionEntity);
//                        binding.agendaFragmentSwipeRefreshLayout.setRefreshing(false);
//                    }
//                });
//

        List<SessionRowEntity> sessionRowEntities =
                DroidconInjector.get().getDatabase()
                        .select(SessionRowEntity.class)
                        .where(SessionEntity.DAY_ID.eq(sessionDay.ordinal() + 1))
                        .orderBy(SessionRowEntity.SLOT_ID)
                        .get()
                        .toList();


        for (SessionRowEntity sessionRowEntity : sessionRowEntities) {
            agendaAdapter.add(sessionRowEntity);
        }

    }

    @Override
    public void onItemClick(View view, int position) {
        SessionRowEntity session = agendaAdapter.getSessionByPosition(position);
        if (session.room1().getSpeakers().isEmpty()) {
            return;
        }
//        SessionActivity.start(getContext(), session);
    }
}
