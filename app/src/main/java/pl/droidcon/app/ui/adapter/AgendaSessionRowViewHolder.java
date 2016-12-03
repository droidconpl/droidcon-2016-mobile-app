package pl.droidcon.app.ui.adapter;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import pl.droidcon.app.R;
import pl.droidcon.app.databinding.AgendaElementNewBinding;
import pl.droidcon.app.databinding.AgendaElementSingleSessionBinding;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionRowEntity;
import pl.droidcon.app.model.db.Speaker;

public class AgendaSessionRowViewHolder extends BaseSessionViewHolder {

    AgendaElementNewBinding binding;

    private Session session;
    private SessionRowEntity sessionRowEnity;

    public AgendaSessionRowViewHolder(AgendaElementNewBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void attachSession(SessionRowEntity sessionRowEntity) {
        this.sessionRowEnity = sessionRowEntity;
        setRoom(sessionRowEntity.room1(), binding.agendaElementRoom1);
        setRoom(sessionRowEntity.room2(), binding.agendaElementRoom2);
        setRoom(sessionRowEntity.room3(), binding.agendaElementRoom3);
    }

    void setRoom(Session session, AgendaElementSingleSessionBinding agendaElementBinding) {
        if (session == null) {
            agendaElementBinding.sessionTitle.setText(sessionRowEnity.rowTitle());
            agendaElementBinding.sessionPicture.setImageResource(R.drawable.droidcon_krakow_logo);
            return;
        }
        agendaElementBinding.sessionTitle.setText(session.getTitle());

        setPicture(session.getSpeakers(), agendaElementBinding.sessionPicture);
    }

    private void setPicture(List<Speaker> realSpeakerList, RoundedImageView sessionPicture) {
        if (realSpeakerList.isEmpty()) {
            sessionPicture.setImageResource(R.drawable.droidcon_krakow_logo);
        } else {
            String url = UrlHelper.url(realSpeakerList.get(0).getImageUrl());
            Picasso.with(sessionPicture.getContext())
                    .load(url)
                    .into(sessionPicture);
        }
    }
}
