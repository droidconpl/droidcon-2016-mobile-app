package pl.droidcon.app.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.droidcon.app.R;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.db.Speaker;

public class SpeakerDialog extends AppCompatDialogFragment {

    private static final String SPEAKER_EXTRA = "speaker";

    public static SpeakerDialog newInstance(Speaker speaker) {
        Bundle args = new Bundle();
        args.putParcelable(SPEAKER_EXTRA, speaker);
        SpeakerDialog fragment = new SpeakerDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private Speaker speaker;

    @Bind(R.id.speaker_photo)
    ImageView speakerPhoto;
    @Bind(R.id.speaker_full_name)
    TextView speakerFullName;
    @Bind(R.id.speaker_title)
    TextView speakerTitle;
    @Bind(R.id.speaker_bio)
    TextView speakerBio;
    @Bind(R.id.website_link)
    ImageButton websiteButton;
    @Bind(R.id.facebook_link)
    ImageButton facebookButton;
    @Bind(R.id.twitter_link)
    ImageButton twitterButton;
    @Bind(R.id.github_link)
    ImageButton githubButton;
    @Bind(R.id.linkedin_link)
    ImageButton linkedInButton;
    @Bind(R.id.google_link)
    ImageButton googleLink;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, getTheme());
        speaker = getArguments().getParcelable(SPEAKER_EXTRA);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.speaker_dialog, null);
        ButterKnife.bind(this, view);
        int size = (int) getResources().getDimension(R.dimen.speaker_dialog_avatar_size);
        Picasso.with(getContext()).load(UrlHelper.url(speaker.getImageUrl())).resize(size, size).into(avatarTarget);
        speakerFullName.setText(getString(R.string.speaker_full_name_format, speaker.getFirstName(), speaker.getLastName()));
        speakerBio.setText(Html.fromHtml(speaker.getBio()));
        speakerTitle.setText(speaker.getWebsiteLink());
        setLinks();
        builder.setView(view)
                .setPositiveButton(R.string.hide, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        Glide.clear(target);
        ButterKnife.unbind(this);
    }

    private void setLinks() {
        if (TextUtils.isEmpty(speaker.getWebsiteLink())) {
            websiteButton.setVisibility(View.GONE);
        } else {
            websiteButton.setOnClickListener(new LinkClickListener(speaker.getWebsiteLink(), LinkClickListener.Type.WEBSITE));
        }
        if (TextUtils.isEmpty(speaker.getFacebookLink())) {
            facebookButton.setVisibility(View.GONE);
        } else {
            facebookButton.setOnClickListener(new LinkClickListener(speaker.getFacebookLink(), LinkClickListener.Type.FACEBOOK));
        }
        if (TextUtils.isEmpty(speaker.getTwitterHandler())) {
            twitterButton.setVisibility(View.GONE);
        } else {
            twitterButton.setOnClickListener(new LinkClickListener(speaker.getTwitterHandler(), LinkClickListener.Type.TWITTER));
        }
        if (TextUtils.isEmpty(speaker.getGithubLink())) {
            githubButton.setVisibility(View.GONE);
        } else {
            githubButton.setOnClickListener(new LinkClickListener(speaker.getGithubLink(), LinkClickListener.Type.GITHUB));
        }
        if (TextUtils.isEmpty(speaker.getLinkedIn())) {
            linkedInButton.setVisibility(View.GONE);
        } else {
            linkedInButton.setOnClickListener(new LinkClickListener(speaker.getLinkedIn(), LinkClickListener.Type.LINKEDIN));
        }
        if (TextUtils.isEmpty(speaker.getGooglePlus())) {
            googleLink.setVisibility(View.GONE);
        } else {
            googleLink.setOnClickListener(new LinkClickListener(speaker.getGooglePlus(), LinkClickListener.Type.GOOGLE));
        }
    }

    private Target avatarTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);
            speakerPhoto.setImageDrawable(roundedBitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private static class LinkClickListener implements View.OnClickListener {

        private enum Type {
            WEBSITE,
            FACEBOOK,
            TWITTER,
            GITHUB,
            LINKEDIN,
            GOOGLE
        }

        private final Type type;
        private final String value;

        public LinkClickListener(String value, Type type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public void onClick(View v) {
            Intent intent = getIntent(v.getContext());
            v.getContext().startActivity(intent);
        }

        private Intent getIntent(Context context) {
            switch (type) {
                case WEBSITE:
                    return getBrowserIntent();
                case FACEBOOK:
                    return getFBIntent(context);
                case TWITTER:
                    return getTwitterIntent();
                case GITHUB:
                    return getGithubIntent();
                case LINKEDIN:
                    return getBrowserIntent();
                case GOOGLE:
                    return getBrowserIntent();
            }
            throw new UnsupportedOperationException("Not supported intent");
        }

        private Intent getBrowserIntent() {
            return new Intent(Intent.ACTION_VIEW, Uri.parse(value));
        }

        private Intent getFBIntent(Context context) {
            try {
                context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                String facebookScheme = "fb://facewebmodal/f?href=" + value;
                return new Intent(Intent.ACTION_VIEW, Uri.parse(facebookScheme));
            } catch (Exception e) {
                return new Intent(Intent.ACTION_VIEW, Uri.parse(value));
            }
        }

        private Intent getTwitterIntent() {
            String url = "https://twitter.com/" + value;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            return intent;
        }

        private Intent getGithubIntent() {
            String url = "https://github.com/" + value;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            return intent;
        }
    }

}
