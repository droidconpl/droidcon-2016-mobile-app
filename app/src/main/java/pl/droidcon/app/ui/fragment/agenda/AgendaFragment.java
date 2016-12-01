package pl.droidcon.app.ui.fragment.agenda;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle.components.support.RxFragment;

import javax.inject.Inject;

import io.requery.query.Result;
import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.databinding.AgendaFragmentBinding;
import pl.droidcon.app.model.common.SessionDay;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SpeakerEntity;
import pl.droidcon.app.model.ui.SwipeRefreshColorSchema;
import pl.droidcon.app.rx.DataSubscription;
import pl.droidcon.app.ui.activity.SessionActivity;
import pl.droidcon.app.ui.adapter.AgendaAdapter;
import pl.droidcon.app.ui.decoration.SpacesItemDecoration;
import pl.droidcon.app.ui.view.RecyclerItemClickListener;
import pl.droidcon.app.wrapper.SnackbarWrapper;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;


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

    private AgendaAdapter agendaAdapter = new AgendaAdapter();
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
        GridLayoutManager mLayoutManager = new GridLayoutManager(view.getContext(), 2);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //return 2 for single item as single item occupies all width
                return agendaAdapter.getSessionByPosition(position).isSingleItem() ? 2 : 1;
            }
        });
        binding.agendaView.setLayoutManager(mLayoutManager);
        binding.agendaView.addItemDecoration(new SpacesItemDecoration(view.getContext().getResources().getDimension(R.dimen.list_element_margin)));
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
        Observable
                .combineLatest(databaseManager.sessions(sessionDay), databaseManager.speakers(), new Func2<Result<SessionEntity>, Result<SpeakerEntity>, Result<SessionEntity>>() {
                    @Override
                    public Result<SessionEntity> call(Result<SessionEntity> sessionEntities, Result<SpeakerEntity> speakerEntities) {
                        return sessionEntities;
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<Result<SessionEntity>>bindToLifecycle())
                .flatMap(new Func1<Result<SessionEntity>, Observable<SessionEntity>>() {
                    @Override
                    public Observable<SessionEntity> call(Result<SessionEntity> sessionEntities) {
                        return sessionEntities.toObservable();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SessionEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        showErrorSnackBar();
                    }

                    @Override
                    public void onNext(SessionEntity sessionEntity) {
                        agendaAdapter.add(sessionEntity);
                        binding.agendaFragmentSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onItemClick(View view, int position) {
        SessionEntity session = agendaAdapter.getSessionByPosition(position);
        if (session.getSpeakers().isEmpty()) {
            return;
        }
        SessionActivity.start(getContext(), session);
    }
}
