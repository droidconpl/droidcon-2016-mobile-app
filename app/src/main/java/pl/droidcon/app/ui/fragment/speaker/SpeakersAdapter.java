package pl.droidcon.app.ui.fragment.speaker;


import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import pl.droidcon.app.R;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.db.SpeakerEntity;

public class SpeakersAdapter extends RecyclerView.Adapter<SpeakersAdapter.SpeakerHolder> {


    private SortedList<SpeakerEntity> speakers = new SortedList<>(SpeakerEntity.class, new SortedListAdapterCallback<SpeakerEntity>(this) {
        @Override
        public int compare(SpeakerEntity o1, SpeakerEntity o2) {
            return o1.getId() - o2.getId();
        }

        @Override
        public boolean areContentsTheSame(SpeakerEntity oldItem, SpeakerEntity newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(SpeakerEntity item1, SpeakerEntity item2) {
            return item1.getId() == item2.getId();
        }
    });


    public void add(SpeakerEntity speakerEntity) {
        speakers.add(speakerEntity);
    }

    @Override
    public SpeakersAdapter.SpeakerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.speakers_element, parent, false);

        return new SpeakerHolder(v);
    }

    @Override
    public void onBindViewHolder(SpeakersAdapter.SpeakerHolder holder, int position) {
        holder.bind(speakers.get(position));
    }

    @Override
    public int getItemCount() {
        return speakers.size();
    }

    @NonNull
    public SpeakerEntity getSpeakerByPosition(int position) {
        return speakers.get(position);
    }

    static class SpeakerHolder extends RecyclerView.ViewHolder {

        private final Picasso picasso;
        ImageView speakerPhoto;
        TextView speakerName;


        public SpeakerHolder(View itemView) {
            super(itemView);
            speakerPhoto = (ImageView) itemView.findViewById(R.id.speaker_picture);
            speakerName = (TextView) itemView.findViewById(R.id.speaker_name);
            picasso = Picasso.with(itemView.getContext());
        }

        public void bind(SpeakerEntity speakerEntity) {

            String url = UrlHelper.url(speakerEntity.getImageUrl());

            picasso.load(url).into(speakerPhoto);

            speakerName.setText(String.format("%s %s", speakerEntity.getFirstName(), speakerEntity.getLastName()));


        }
    }
}
