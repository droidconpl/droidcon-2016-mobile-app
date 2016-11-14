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

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import pl.droidcon.app.R;
import pl.droidcon.app.databinding.SpeakerDialogBinding;
import pl.droidcon.app.helper.UrlHelper;
import pl.droidcon.app.model.db.Speaker;

public class SpeakerDialog extends AppCompatDialogFragment {

    private static final String SPEAKER_EXTRA = "speaker";
    private SpeakerDialogBinding binding;

    public static SpeakerDialog newInstance(Speaker speaker) {
        Bundle args = new Bundle();
        args.putParcelable(SPEAKER_EXTRA, speaker);
        SpeakerDialog fragment = new SpeakerDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private Speaker speaker;

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
        binding = SpeakerDialogBinding.inflate(LayoutInflater.from(getContext()), null);
        int size = (int) getResources().getDimension(R.dimen.speaker_dialog_avatar_size);
        Picasso.with(getContext()).load(UrlHelper.url(speaker.getImageUrl())).resize(size, size).into(avatarTarget);
        binding.speakerFullName.setText(getString(R.string.speaker_full_name_format, speaker.getFirstName(), speaker.getLastName()));
        binding.speakerBio.setText(Html.fromHtml(speaker.getBio()));
        binding.speakerTitle.setText(speaker.getWebsiteLink());
        setLinks();
        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.hide, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    private void setLinks() {
        if (TextUtils.isEmpty(speaker.getWebsiteLink())) {
            binding.websiteLink.setVisibility(View.GONE);
        } else {
            binding.websiteLink.setOnClickListener(new LinkClickListener(speaker.getWebsiteLink(), LinkClickListener.Type.WEBSITE));
        }
        if (TextUtils.isEmpty(speaker.getFacebookLink())) {
            binding.facebookLink.setVisibility(View.GONE);
        } else {
            binding.facebookLink.setOnClickListener(new LinkClickListener(speaker.getFacebookLink(), LinkClickListener.Type.FACEBOOK));
        }
        if (TextUtils.isEmpty(speaker.getTwitterHandler())) {
            binding.twitterLink.setVisibility(View.GONE);
        } else {
            binding.twitterLink.setOnClickListener(new LinkClickListener(speaker.getTwitterHandler(), LinkClickListener.Type.TWITTER));
        }
        if (TextUtils.isEmpty(speaker.getGithubLink())) {
            binding.githubLink.setVisibility(View.GONE);
        } else {
            binding.githubLink.setOnClickListener(new LinkClickListener(speaker.getGithubLink(), LinkClickListener.Type.GITHUB));
        }
        if (TextUtils.isEmpty(speaker.getLinkedIn())) {
            binding.linkedinLink.setVisibility(View.GONE);
        } else {
            binding.linkedinLink.setOnClickListener(new LinkClickListener(speaker.getLinkedIn(), LinkClickListener.Type.LINKEDIN));
        }
        if (TextUtils.isEmpty(speaker.getGooglePlus())) {
            binding.googleLink.setVisibility(View.GONE);
        } else {
            binding.googleLink.setOnClickListener(new LinkClickListener(speaker.getGooglePlus(), LinkClickListener.Type.GOOGLE));
        }
    }

    private Target avatarTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);
            binding.speakerPhoto.setImageDrawable(roundedBitmapDrawable);
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
