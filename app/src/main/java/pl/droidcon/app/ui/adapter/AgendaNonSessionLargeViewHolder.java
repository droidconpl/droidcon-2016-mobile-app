package pl.droidcon.app.ui.adapter;


import com.squareup.picasso.Picasso;

import pl.droidcon.app.databinding.AgendaNonSessionLargeElementBinding;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.model.db.SessionRowEntity;

public class AgendaNonSessionLargeViewHolder extends BaseSessionViewHolder {

    AgendaNonSessionLargeElementBinding binding;
    private Session session;

    public AgendaNonSessionLargeViewHolder(AgendaNonSessionLargeElementBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
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
