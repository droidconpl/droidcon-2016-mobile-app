package pl.droidcon.app.ui.adapter;


import com.squareup.picasso.Picasso;

import pl.droidcon.app.R;
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
        binding.sessionTime.setText(String.format("%s - %s", sessionRowEntity.slotStart(), sessionRowEntity.slotEnd()));

//        String url = UrlHelper.assetUrl(sessionRowEntity.rowPicture());
//        Picasso.with(binding.agendaLargeIcon.getContext())
//                .load(url)
//                .into(binding.agendaLargeIcon);


        String lowerCaseTitle = title.toLowerCase();
        if (lowerCaseTitle.contains("registration") || lowerCaseTitle.contains("opening") ||
                lowerCaseTitle.contains("closing") || lowerCaseTitle.contains("barcamp")) {
            binding.agendaLargeIcon.setImageResource(R.drawable.ic_icon_droid_large);
            return;
        }

        if (lowerCaseTitle.contains("coffe")) {
            binding.agendaLargeIcon.setImageResource(R.drawable.ic_icon_coffee_large);
            return;
        }

        if (lowerCaseTitle.contains("lunch")) {
            binding.agendaLargeIcon.setImageResource(R.drawable.ic_icon_fork_large);
            return;
        }
        if (lowerCaseTitle.contains("afterparty")) {
            binding.agendaLargeIcon.setImageResource(R.drawable.ic_icon_party_large);
        }
    }

}
