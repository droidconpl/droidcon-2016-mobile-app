package pl.droidcon.app.ui.fragment.speaker;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.requery.query.Result;
import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.model.db.SpeakerEntity;
import pl.droidcon.app.ui.dialog.SpeakerDialog;
import pl.droidcon.app.ui.fragment.BaseFragment;
import pl.droidcon.app.ui.view.RecyclerItemClickListener;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SpeakersFragment extends BaseFragment implements RecyclerItemClickListener.OnItemClickListener {

    public static final String TAG = SpeakersFragment.class.getSimpleName();
    private SpeakersAdapter speakersAdapter;

    public static SpeakersFragment newInstance() {
        return new SpeakersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.speakers_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.speakers_list);

        speakersAdapter = new SpeakersAdapter();

        GridLayoutManager mLayoutManager = new GridLayoutManager(view.getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.addItemDecoration(new SpacesItemDecoration((int) getResources().getDimension(R.dimen.list_element_margin)));

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), this));
        recyclerView.setAdapter(speakersAdapter);

        DatabaseManager store = DroidconInjector.get().databaseManager();

        store
                .speakers()
                .flatMap(new Func1<Result<SpeakerEntity>, Observable<SpeakerEntity>>() {
                    @Override
                    public Observable<SpeakerEntity> call(Result<SpeakerEntity> speakerEntities) {

                        return speakerEntities.toObservable();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<SpeakerEntity>bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SpeakerEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SpeakerEntity speakerEntity) {
                        speakersAdapter.add(speakerEntity);
                    }
                });

    }

    @Override
    public int getTitle() {
        return R.string.speakers;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onItemClick(View view, int position) {
        SpeakerEntity speakerEntity = speakersAdapter.getSpeakerByPosition(position);
        SpeakerDialog.newInstance(speakerEntity).show(getFragmentManager(), TAG);
        ;
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
//            if(parent.getChildPosition(view) == 0)
            outRect.top = space;
        }
    }
}
