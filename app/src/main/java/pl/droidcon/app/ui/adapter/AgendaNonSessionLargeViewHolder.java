package pl.droidcon.app.ui.adapter;


import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import pl.droidcon.app.R;
import pl.droidcon.app.databinding.AgendaNonSessionLargeElementBinding;
import pl.droidcon.app.helper.DateTimePrinter;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionEntity;
import pl.droidcon.app.model.db.SessionRowEntity;

public class AgendaNonSessionLargeViewHolder extends BaseSessionViewHolder {

    AgendaNonSessionLargeElementBinding binding;
    private Session session;

    public AgendaNonSessionLargeViewHolder(AgendaNonSessionLargeElementBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void attachSession(SessionEntity session) {
        this.session = session;

        String title = session.getTitle();

        binding.sessionTitle.setText(title);
        binding.sessionDate.setText(DateTimePrinter.toPrintableStringWithDay(new DateTime(session.getDate())));
        String lowerCaseTitle = session.getTitle().toLowerCase();
        if (lowerCaseTitle.startsWith("registration") || lowerCaseTitle.startsWith("opening") ||
                lowerCaseTitle.startsWith("closing") || lowerCaseTitle.startsWith("barcamp")) {
            binding.agendaLargeIcon.setImageResource(R.drawable.ic_icon_droid_large);
            return;
        }

        if (lowerCaseTitle.startsWith("coffe")) {
            binding.agendaLargeIcon.setImageResource(R.drawable.ic_icon_coffee_large);
            return;
        }

        if (lowerCaseTitle.startsWith("lunch")) {
            binding.agendaLargeIcon.setImageResource(R.drawable.ic_icon_fork_large);
            return;
        }
        if (lowerCaseTitle.startsWith("afterparty")) {
            binding.agendaLargeIcon.setImageResource(R.drawable.ic_icon_party_large);
        }
    }


    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void attachSession(SessionRowEntity sessionRowEntity) {
        String title = sessionRowEntity.rowTitle();

        binding.sessionTitle.setText(title);
//        binding.sessionDate.setText(DateTimePrinter.toPrintableStringWithDay(new DateTime(session.getDate())));
        String lowerCaseTitle = title.toLowerCase();

        String url = UrlHelper.assetUrl(sessionRowEntity.rowPicture());
        Picasso.with(binding.agendaLargeIcon.getContext())
                .load(url)
                .into(binding.agendaLargeIcon);

    }

}
