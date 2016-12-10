package pl.droidcon.app.ui.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import pl.droidcon.app.R;
import pl.droidcon.app.databinding.SpeakerListItemBinding;
import pl.droidcon.app.helper.HtmlCompat;


public class SimpleListItem extends LinearLayout {

    private static final String TAG = SimpleListItem.class.getSimpleName();

    private int avatarSize;
    protected SpeakerListItemBinding binding;

    public SimpleListItem(Context context) {
        this(context, null);
    }

    public SimpleListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SimpleListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SimpleListItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }


    protected void init(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        binding = SpeakerListItemBinding.inflate(LayoutInflater.from(context), this, true);
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(VERTICAL);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
        setBackgroundResource(typedValue.resourceId);
        setClickable(true);
        setFocusable(true);

        avatarSize = (int) context.getResources().getDimension(R.dimen.speaker_photo_avatar_size);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                removeGlobalLayoutListener(this);
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) getLayoutParams();
                int marginPixelSize = getResources().getDimensionPixelSize(R.dimen.list_element_margin);
                marginLayoutParams.topMargin = marginPixelSize;
                marginLayoutParams.bottomMargin = marginPixelSize;
                setLayoutParams(marginLayoutParams);
                setPadding(marginPixelSize, marginPixelSize, marginPixelSize, marginPixelSize);
            }
        });
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void removeGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener) {
        if (Build.VERSION.SDK_INT < 16) {
            getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
        } else {
            getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        }
    }

    protected void setTitle(String text) {
        binding.speakerListItemFullName.setText(HtmlCompat.fromHtml(text));
    }

    protected void setImage(@NonNull String imageUrl) {
        Log.d(TAG, "setImage() called with: imageUrl = [" + imageUrl + "]");
        Picasso.with(getContext())
                .load(imageUrl)
                .resize(avatarSize, avatarSize)
                .transform(new RoundTransformation(getResources()))
                .into(binding.speakerListItemThumbnail);
    }

    protected void setDescription(String text) {
        binding.speakerListItemText.setText(HtmlCompat.fromHtml(text));
    }

}
