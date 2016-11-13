package pl.droidcon.app.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import pl.droidcon.app.R;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.db.Speaker;


public class SpeakerListItem extends SimpleListItem {

    private static final String TAG = SpeakerListItem.class.getSimpleName();

    @Nullable
    private SpeakerList.SpeakerItemClickListener speakerItemClickListener;
    private Speaker speaker;

    public SpeakerListItem(Context context) {
        super(context);
    }

    public SpeakerListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeakerListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SpeakerListItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super.init(context, attributeSet, defStyleAttr, defStyleRes);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speakerItemClickListener != null) {
                    speakerItemClickListener.onSpeakerClicked(speaker);
                }
            }
        });
    }

    public void setSpeakerItemClickListener(@Nullable SpeakerList.SpeakerItemClickListener speakerItemClickListener) {
        this.speakerItemClickListener = speakerItemClickListener;
    }

    public void setSpeaker(Speaker speaker) {
        if (isInEditMode()) {
            return;
        }
        this.speaker = speaker;
        setTitle(getResources().getString(R.string.speaker_full_name_format, speaker.getFirstName(), speaker.getLastName()));
        setImage(UrlHelper.url(speaker.getImageUrl()));
        setDescription(speaker.getBio());
    }

}
