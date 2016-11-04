package pl.droidcon.app.ui.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.requery.query.Result;
import me.relex.circleindicator.CircleIndicator;
import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.helper.DateTimePrinter;
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
//    private static final String SPEAKERS_EXTRA = "speakers";

    public static void start(Context context, Session session) {
        Intent intent = getSessionIntent(context, session);
        context.startActivity(intent);
    }

    public static Intent getSessionIntent(Context context, Session session) {
        Intent intent = new Intent(context, SessionActivity.class);
        intent.putExtra(SESSION_EXTRA, session.getId());
//        intent.putParcelableArrayListExtra(SPEAKERS_EXTRA, new ArrayList<Parcelable>(session.getSpeakers()));
        return intent;
    }

    private SessionEntity session;
    List<Speaker> speakersList;

    @Bind(R.id.speaker_photos)
    ViewPager speakerPhotos;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.session_description)
    TextView description;
    @Bind(R.id.session_title)
    TextView title;
    @Bind(R.id.session_date)
    TextView date;
    @Bind(R.id.session_room)
    TextView sessionRoom;
    @Bind(R.id.indicator)
    CircleIndicator indicator;
    @Bind(R.id.speakers)
    SpeakerList speakerListView;
    @Bind(R.id.favourite_button)
    FloatingActionButton favouriteButton;
    @Bind(R.id.root_view)
    CoordinatorLayout rootView;

    @Inject
    DatabaseManager databaseManager;
    @Inject
    SnackbarWrapper snackbarWrapper;
    @Inject
    SessionReminder sessionReminder;

    private CompositeSubscription compositeSubscription;
    private FavouriteClickListener favouriteClickListener = new FavouriteClickListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_activity);
        DroidconInjector.get().inject(this);
        ButterKnife.bind(this);
        setupToolbarBack(toolbar);
        int sessionId = getIntent().getExtras().getInt(SESSION_EXTRA);
        session = DroidconInjector.get().getDatabase().select(SessionEntity.class).where(SessionEntity.ID.eq(sessionId)).get().first();
        speakersList = session.getSpeakers();
        compositeSubscription = new CompositeSubscription();
        fillDetails();
        checkIsFavourite();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.clear();
    }

    private void fillDetails() {
        setToolbarTitle(null);
        title.setText(session.getTitle());
        speakerPhotos.setAdapter(new SpeakerPhotosAdapter(this, speakersList));
        description.setText(Html.fromHtml(session.getDescription()));
        date.setText(DateTimePrinter.toPrintableStringWithDay(new DateTime(session.getDate())));
        indicator.setViewPager(speakerPhotos);
        if (speakersList.size() == 1) {
            indicator.setVisibility(View.INVISIBLE);
        }
        speakerListView.setSpeakers(speakersList, this);
        favouriteButton.setOnClickListener(favouriteClickListener);
        int stringRes = Room.valueOfRoomId(session.getRoomId()).getStringRes();
        sessionRoom.setText(stringRes);
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
                .subscribeOn(Schedulers.io())
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
        IconDrawable iconDrawable;
        if (isFavourite) {
            iconDrawable = new IconDrawable(this, FontAwesomeIcons.fa_heart);
        } else {
            iconDrawable = new IconDrawable(this, FontAwesomeIcons.fa_heart_o);
        }
        favouriteButton.setImageDrawable(iconDrawable.colorRes(R.color.primaryColor).sizeDp(24));
        favouriteClickListener.alreadyFavourite = isFavourite;
    }


    private void checkAndAddToFavourite() {
        Subscription subscription = databaseManager.canSessionBeSchedule(session)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<ScheduleEntity, Observable<ScheduleCollision>>() {
                    @Override
                    public Observable<ScheduleCollision> call(ScheduleEntity scheduleEntity) {
                        if (scheduleEntity == null) {
                            return Observable.just(new ScheduleCollision(null, false));
                        } else {
                            return Observable.just(new ScheduleCollision(scheduleEntity, scheduleEntity.getSession().getId() != session.getId()));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ScheduleCollision>() {
                    @Override
                    public void onCompleted() {
                        addToFavourites();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ScheduleCollision scheduleCollision) {
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
                .subscribeOn(Schedulers.io())
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
                .subscribeOn(Schedulers.io())
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
                            snackbarWrapper.showSnackbar(rootView, R.string.fav_added);
                        }
                    }
                });
        compositeSubscription.add(subscription);
    }

    private void removeFromFavourites() {
        Subscription subscription = databaseManager.removeFromFavourite(session)
                .subscribeOn(Schedulers.io())
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
                        snackbarWrapper.showSnackbar(rootView, R.string.fav_removed);
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
        Subscription subscription = databaseManager.removeFromFavourite(oldSession)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
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

        public ViewPagerImageClickListener(String url) {
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
            imageView.setOnClickListener(new ViewPagerImageClickListener(url));
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ViewGroup) object);
        }
    }
}
