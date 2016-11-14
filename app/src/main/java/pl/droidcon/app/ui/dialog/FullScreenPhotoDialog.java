package pl.droidcon.app.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import pl.droidcon.app.databinding.FullScreenSpeakerPhotoDialogBinding;

public class FullScreenPhotoDialog extends AppCompatDialogFragment {

    private static final String TAG = FullScreenPhotoDialog.class.getSimpleName();

    private static final String PHOTO_URL_KEY = "photo";
    private FullScreenSpeakerPhotoDialogBinding binding;

    public static FullScreenPhotoDialog newInstance(String photoUrl) {
        Bundle args = new Bundle();
        args.putString(PHOTO_URL_KEY, photoUrl);
        FullScreenPhotoDialog fragment = new FullScreenPhotoDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private String photoUrl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, getTheme());
        photoUrl = getArguments().getString(PHOTO_URL_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FullScreenSpeakerPhotoDialogBinding.inflate(inflater, container, false);
        binding.fullScreenPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Picasso.with(getContext())
                .load(photoUrl)
                .into(binding.fullScreenPhoto);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Picasso.with(getContext()).cancelRequest(binding.fullScreenPhoto);
    }

}
