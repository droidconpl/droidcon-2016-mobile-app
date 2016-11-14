package pl.droidcon.app.ui.adapter;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.databinding.ScheduleElementBinding;
import pl.droidcon.app.helper.DateTimePrinter;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.common.Room;
import pl.droidcon.app.model.common.Slot;
import pl.droidcon.app.model.db.Session;


public class ScheduleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public interface ScheduleClickListener {
        void onScheduleClicked(View view, int position);
    }

    @Inject
    Resources resources;

    ScheduleElementBinding binding;
    private ScheduleClickListener scheduleClickListener;

    public ScheduleViewHolder(ScheduleElementBinding binding, ScheduleClickListener scheduleClickListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.scheduleClickListener = scheduleClickListener;

        DroidconInjector.get().inject(this);
        binding.slotViewClickable.setOnClickListener(this);
    }

    public void attachSlot(Slot slot) {
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        binding.slotViewHour.setText(DateTimePrinter.toPrintableString(slot.getDateTime()));
        binding.slotViewTitle.setText(slot.getDisplayTitle());
        binding.slotViewSessionTitle.setText(slot.getDisplayTitle());
        // be sure to reset title widget
        // show & hide
        binding.slotViewTitle.setVisibility(View.VISIBLE);
        binding.slotViewSessionTitle.setVisibility(View.GONE);
        setRoomName(slot.getSession());

        int resId = -1;
        int height = resources.getDimensionPixelSize(R.dimen.list_item_height);
        switch (slot.getSlotType()) {
            case REGISTRATION:
            case OPENING_1_DAY:
            case CLOSING_1_DAY:
            case OPENING_2_DAY:
            case CLOSING_2_DAY:
            case BARCAMP:
                resId = R.drawable.ic_icon_droid;
                resetPhoto();
                break;
            case SESSION:
                height = getSessionSlotHeight(slot.getSession(), height);
                setSessionPhoto(slot.getSession());
                break;
            case LUNCH_BREAK:
                resId = R.drawable.ic_icon_fork;
                resetPhoto();
                break;
            case COFFEE_BREAK:
                resId = R.drawable.ic_icon_coffee;
                resetPhoto();
                break;
            case AFTER_PARTY:
                resId = R.drawable.ic_icon_party;
                resetPhoto();
                break;
        }
        if (resId != -1) {
            binding.scheduleIcon.setVisibility(View.VISIBLE);
            binding.scheduleIcon.setImageResource(resId);
            binding.slotViewTitle.setSingleLine(true);
            binding.slotViewTitle.setEllipsize(TextUtils.TruncateAt.END);
            binding.slotViewTitle.setMaxLines(1);
        } else {
            binding.slotViewTitle.setSingleLine(false);
            binding.slotViewTitle.setEllipsize(null);
            binding.slotViewTitle.setMaxLines(2);
            binding.scheduleIcon.setVisibility(View.GONE);
            binding.scheduleIcon.setImageDrawable(null);
        }
        layoutParams.height = height;
        itemView.setLayoutParams(layoutParams);
    }

    @Override
    public void onClick(View v) {
        scheduleClickListener.onScheduleClicked(v, getAdapterPosition());
    }

    private void resetPhoto() {
        binding.slotViewSpeakerImage.setImageDrawable(null);
    }

    private void setRoomName(@Nullable Session session) {
        if (session == null) {
            binding.slotRoomName.setText(null);
            return;
        }

        int stringRes = Room.valueOfRoomId(session.getRoomId()).getStringRes();
        String room = resources.getString(stringRes);
        binding.slotRoomName.setText(room);
    }

    private int getSessionSlotHeight(@Nullable Session session, int defaultValue) {
        return session == null ? defaultValue : resources.getDimensionPixelSize(R.dimen.list_item_expanded_height);
    }

    private void setSessionPhoto(@Nullable Session session) {
        if (session != null && !session.getSpeakers().isEmpty()) {
            String url = UrlHelper.url(session.getSpeakers().get(0).getImageUrl());
            Picasso.with(itemView.getContext())
                    .load(url)
                    .resize(512, 512)
                    .centerCrop()
                    .into(binding.slotViewSpeakerImage, avatarCallback);
        } else {
            resetPhoto();
        }
    }

    Callback avatarCallback = new Callback() {
        @Override
        public void onSuccess() {
            binding.slotViewTitle.setVisibility(View.GONE);
            binding.slotViewSessionTitle.setVisibility(View.VISIBLE);
            binding.slotViewSessionTitle.setSingleLine(false);
            binding.slotViewSessionTitle.setEllipsize(null);
            binding.slotViewSessionTitle.setMaxLines(2);
        }

        @Override
        public void onError() {

        }
    };

}
