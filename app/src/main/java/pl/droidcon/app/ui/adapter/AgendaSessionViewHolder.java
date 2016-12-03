package pl.droidcon.app.ui.adapter;

import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.util.List;

import pl.droidcon.app.R;
import pl.droidcon.app.databinding.AgendaElementBinding;
import pl.droidcon.app.helper.DateTimePrinter;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.common.Room;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SessionRowEntity;
import pl.droidcon.app.model.db.Speaker;

public class AgendaSessionViewHolder extends BaseSessionViewHolder {

    AgendaElementBinding binding;

    private Session session;

    public AgendaSessionViewHolder(AgendaElementBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void attachSession(SessionEntity session) {
        this.session = session;

        Room room = Room.valueOfRoomId(session.getRoomId());
        binding.sessionRoom.setText(room.getStringRes());
        binding.sessionTitle.setText(session.getTitle());

        binding.sessionDate.setText(DateTimePrinter.toPrintableString(new DateTime(session.getDate())));

        List<Speaker> realSpeakerList = session.getSpeakers();
        if (realSpeakerList.isEmpty()) {
            binding.sessionPicture.setImageResource(R.drawable.droidcon_krakow_logo);
        } else {
            String url = UrlHelper.url(realSpeakerList.get(0).getImageUrl());
            Picasso.with(binding.sessionPicture.getContext())
                    .load(url)
                    .into(binding.sessionPicture);
        }
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void attachSession(SessionRowEntity sessionRowEntity) {
        Session session = sessionRowEntity.room1();
        binding.sessionTitle.setText(session.getTitle());

//        binding.sessionDate.setText(DateTimePrinter.toPrintableString(new DateTime(session.getDate())));

        List<Speaker> realSpeakerList = session.getSpeakers();
        if (realSpeakerList.isEmpty()) {
            binding.sessionPicture.setImageResource(R.drawable.droidcon_krakow_logo);
        } else {
            String url = UrlHelper.url(realSpeakerList.get(0).getImageUrl());
            Picasso.with(binding.sessionPicture.getContext())
                    .load(url)
                    .into(binding.sessionPicture);
        }
    }
}
