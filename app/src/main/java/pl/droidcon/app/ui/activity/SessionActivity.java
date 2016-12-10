package pl.droidcon.app.ui.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.List;

import javax.inject.Inject;

import io.requery.query.Result;
import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.databinding.SessionActivityBinding;
import pl.droidcon.app.helper.DateTimePrinter;
import pl.droidcon.app.helper.HtmlCompat;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.common.Room;
import pl.droidcon.app.model.common.ScheduleCollision;
import pl.droidcon.app.model.db.Schedule;
import pl.droidcon.app.model.db.ScheduleEntity;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.Speaker;
import pl.droidcon.app.reminder.SessionReminder;
import pl.droidcon.app.ui.dialog.FullScreenPhotoDialog;
import pl.droidcon.app.ui.dialog.ScheduleOverlapDialog;
import pl.droidcon.app.ui.dialog.SpeakerDialog;
import pl.droidcon.app.ui.view.SpeakerList;
import pl.droidcon.app.wrapper.SnackbarWrapper;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SessionActivity extends BaseActivity implements SpeakerList.SpeakerItemClickListener {

    private static final String TAG = SessionActivity.class.getSimpleName();

    private static final String SESSION_EXTRA = "session";

    public static void start(Context context, @NonNull Session session) {
        Intent intent = getSessionIntent(context, session);
        context.startActivity(intent);
    }

    @NonNull
    public static Intent getSessionIntent(Context context, Session session) {
        return new Intent(context, SessionActivity.class)
                .putExtra(SESSION_EXTRA, session.getId());
    }

    SessionEntity session;
    SessionActivityBinding binding;

    @Inject
    DatabaseManager databaseManager;

    @Inject
    SnackbarWrapper snackbarWrapper;

    @Inject
    SessionReminder sessionReminder;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();
    private FavouriteClickListener favouriteClickListener = new FavouriteClickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.session_activity);
        DroidconInjector.get().inject(this);
        setupToolbarBack(binding.toolbar);
        int sessionId = getIntent().getExtras().getInt(SESSION_EXTRA);
        session = DroidconInjector.get().getDatabase()
                .select(SessionEntity.class)
                .where(SessionEntity.ID.eq(sessionId))
                .get()
                .first();

        fillDetails();
        checkIsFavourite();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    private void fillDetails() {
        final List<Speaker> speakersList = session.getSpeakers();

        setToolbarTitle(null);

        binding.sessionTitle.setText(HtmlCompat.fromHtml(session.getTitle()));

        binding.speakerPhotos.setAdapter(new SpeakerPhotosAdapter(this, speakersList));
        binding.sessionDescription.setText(HtmlCompat.fromHtml(session.getDescription()));
        binding.sessionDate.setText(DateTimePrinter.toPrintableStringWithDay(new DateTime(session.getDate())));
        binding.indicator.setViewPager(binding.speakerPhotos);

        if (speakersList.size() == 1) {
            binding.indicator.setAlpha(0f);
        }

        binding.speakers.setSpeakers(speakersList, this);
        binding.favouriteButton.setOnClickListener(favouriteClickListener);

        int stringRes = Room.valueOfRoomId(session.getRoomId()).getStringRes();
        binding.sessionRoom.setText(stringRes);
    }

    @Override
    public void onSpeakerClicked(@NonNull Speaker speaker) {
        SpeakerDialog.newInstance(speaker).show(getSupportFragmentManager(), TAG);
    }

    private void checkIsFavourite() {
        final Subscription subscription = databaseManager.isFavourite(session)
                .map(new Func1<Result<ScheduleEntity>, Boolean>() {
                    @Override
                    public Boolean call(Result<ScheduleEntity> scheduleEntities) {
                        return !(scheduleEntities == null || scheduleEntities.toList().isEmpty());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "favourite check exception", e);
                    }

                    @Override
                    public void onNext(Boolean isFavourite) {
                        setRightFloatingActionButtonAction(isFavourite);
                    }
                });

        compositeSubscription.add(subscription);
    }

    private void setRightFloatingActionButtonAction(boolean isFavourite) {
        int favoriteDrawable;
        if (isFavourite) {
            favoriteDrawable = R.drawable.ic_favorite;
        } else {
            favoriteDrawable = R.drawable.ic_favorite_border;
        }
        binding.favouriteButton.setImageResource(favoriteDrawable);
        favouriteClickListener.alreadyFavourite = isFavourite;
    }


    private void checkAndAddToFavourite() {
        final Subscription subscription = Observable
                .just(databaseManager.canSessionBeSchedule(session))
                .map(new Func1<Result<ScheduleEntity>, ScheduleCollision>() {
                    @Override
                    public ScheduleCollision call(Result<ScheduleEntity> scheduleEntities) {
                        if (scheduleEntities == null || scheduleEntities.toList().isEmpty()) {
                            return new ScheduleCollision(null, false);
                        }
                        final ScheduleEntity scheduleEntity = scheduleEntities.first();

                        return new ScheduleCollision(scheduleEntity, true);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ScheduleCollision>() {
                    @Override
                    public void call(ScheduleCollision scheduleCollision) {
                        if (scheduleCollision.isCollision()) {
                            getCollisionSessionAndShowOverlapDialog(scheduleCollision.getSchedule());
                        } else {
                            addToFavourites();
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void getCollisionSessionAndShowOverlapDialog(Schedule schedule) {
        Subscription subscription = databaseManager.session(schedule.getSession().getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Session>() {
                    @Override
                    public void call(Session session) {
                        showOverlapDialog(session);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void addToFavourites() {
        Subscription subscription = databaseManager.addToFavourite(session)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ScheduleEntity>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "on completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error adding to favourites", e);
                    }

                    @Override
                    public void onNext(ScheduleEntity realmSchedule) {
                        if (realmSchedule != null) {
                            sessionReminder.addSessionToReminding(session);
                            setRightFloatingActionButtonAction(true);
                            snackbarWrapper.showSnackbar(binding.rootView, R.string.fav_added);
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void removeFromFavourites() {
        final Subscription subscription = databaseManager.removeFromFavourite(session)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error removing from favourites", e);
                    }

                    @Override
                    public void onNext(Boolean removeResult) {
                        sessionReminder.removeSessionFromReminding(session);
                        setRightFloatingActionButtonAction(!removeResult);
                        snackbarWrapper.showSnackbar(binding.rootView, R.string.fav_removed);
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void showOverlapDialog(final Session collisionSession) {
        ScheduleOverlapDialog
                .newInstance(collisionSession, session, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        replaceSchedule(collisionSession);
                    }
                })
                .show(getSupportFragmentManager(), TAG);
    }

    private void replaceSchedule(final Session oldSession) {
        final Subscription subscription = databaseManager.removeFromFavourite(oldSession)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean removed) {
                        sessionReminder.removeSessionFromReminding(oldSession);
                        if (removed) {
                            addToFavourites();
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    private class FavouriteClickListener implements View.OnClickListener {

        private boolean alreadyFavourite;

        @Override
        public void onClick(View v) {
            if (!alreadyFavourite) {
                checkAndAddToFavourite();
            } else {
                removeFromFavourites();
            }
        }
    }

    private class ViewPagerImageClickListener implements View.OnClickListener {
        private String url;

        private ViewPagerImageClickListener(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View v) {
            FullScreenPhotoDialog.newInstance(url).show(getSupportFragmentManager(), TAG);
        }
    }

    private class SpeakerPhotosAdapter extends PagerAdapter {

        private Context context;
        private List<Speaker> speakers;

        private SpeakerPhotosAdapter(Context context, List<Speaker> speakers) {
            this.context = context;
            this.speakers = speakers;
        }

        @Override
        public int getCount() {
            return speakers.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.speaker_pager_item, container, false);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.speaker_photo);
            String url = UrlHelper.url(speakers.get(position).getImageUrl());
            Picasso.with(context)
                    .load(url)
                    .into(imageView);
            itemView.setOnClickListener(new ViewPagerImageClickListener(url));
            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ViewGroup) object);
        }
    }
}
