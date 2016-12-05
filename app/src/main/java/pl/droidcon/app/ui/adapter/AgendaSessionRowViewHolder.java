package pl.droidcon.app.ui.adapter;

import android.view.View;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.requery.query.Result;
import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.database.DatabaseManager;
import pl.droidcon.app.databinding.AgendaElementNewBinding;
import pl.droidcon.app.databinding.AgendaElementSingleSessionBinding;
import pl.droidcon.app.helper.HtmlCompat;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SessionRowEntity;
import pl.droidcon.app.model.db.Speaker;
import pl.droidcon.app.model.db.SpeakerEntity;
import pl.droidcon.app.ui.activity.SessionActivity;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AgendaSessionRowViewHolder extends BaseSessionViewHolder {

    private final DatabaseManager databaseManager;
    AgendaElementNewBinding binding;

    private Session session;
    private SessionRowEntity sessionRowEnity;
    private Subscription room1Subsciption;
    private Subscription speakerSubscription;
    CompositeSubscription compositeSubscription = new CompositeSubscription();

    public AgendaSessionRowViewHolder(AgendaElementNewBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        databaseManager = DroidconInjector.get().databaseManager();
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void attachSession(SessionRowEntity sessionRowEntity) {
        this.sessionRowEnity = sessionRowEntity;
        binding.agendaElementStartTime.setText(sessionRowEntity.slotStart());
        binding.agendaElementEndTime.setText(sessionRowEntity.slotEnd());


        setRoom(sessionRowEntity.room1(), binding.agendaElementRoom1);
        setRoom(sessionRowEntity.room2(), binding.agendaElementRoom2);
        setRoom(sessionRowEntity.room3(), binding.agendaElementRoom3);
    }

    void setRoom(final Session session, final AgendaElementSingleSessionBinding agendaElementBinding) {
        if (session == null) {
            agendaElementBinding.sessionTitle.setText(HtmlCompat.fromHtml(sessionRowEnity.rowTitle()));
            agendaElementBinding.sessionPicture.setImageResource(R.drawable.droidcon_place_holder);
            return;
        }


        Subscription subscribe = databaseManager.observableSession(session.getId())
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Result<SessionEntity>, Observable<SessionEntity>>() {
                    @Override
                    public Observable<SessionEntity> call(Result<SessionEntity> sessionEntities) {
                        return sessionEntities.toObservable();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SessionEntity>() {
                    @Override
                    public void call(SessionEntity sessionEntity) {
                        foobar(session, agendaElementBinding);
                    }
                });

        compositeSubscription.add(subscribe);
    }

    private void foobar(final Session session, AgendaElementSingleSessionBinding agendaElementBinding) {
        if(session.getTitle() == null) return;

        if (session.getTitle().hashCode() == session.getId()) {
            agendaElementBinding.sessionTitle.setText(HtmlCompat.fromHtml(sessionRowEnity.rowTitle()));
            agendaElementBinding.sessionPicture.setImageResource(R.drawable.droidcon_place_holder);
        } else {
            agendaElementBinding.sessionTitle.setText(HtmlCompat.fromHtml(session.getTitle()));

            setPicture(session.getSpeakers(), agendaElementBinding.sessionPicture);

            agendaElementBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SessionActivity.start(view.getContext(), session);
                }
            });
        }
    }

    private void setPicture(List<Speaker> realSpeakerList, final RoundedImageView sessionPicture) {
        if (realSpeakerList.isEmpty()) {
            sessionPicture.setImageResource(R.drawable.droidcon_place_holder);
        } else {

            speakerSubscription = databaseManager
                    .observableSpeaker(realSpeakerList.get(0).getId())
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Func1<Result<SpeakerEntity>, Observable<SpeakerEntity>>() {
                        @Override
                        public Observable<SpeakerEntity> call(Result<SpeakerEntity> speakerEntities) {
                            return speakerEntities.toObservable();
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<SpeakerEntity>() {
                        @Override
                        public void call(SpeakerEntity speakerEntity) {

                            String url = UrlHelper.url(speakerEntity.getImageUrl());
                            Picasso.with(sessionPicture.getContext())
                                    .load(url)
                                    .placeholder(R.drawable.droidcon_place_holder)
                                    .into(sessionPicture);
                        }
                    });

            compositeSubscription.add(speakerSubscription);
        }
    }

    @Override
    public void unSubscribe() {
        super.unSubscribe();

        compositeSubscription.unsubscribe();
    }
}
