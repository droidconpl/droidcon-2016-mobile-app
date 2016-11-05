package pl.droidcon.app.ui.fragment;


import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.trello.rxlifecycle.components.support.RxFragment;

public abstract class BaseFragment extends RxFragment {

    @StringRes
    public abstract int getTitle();

    public abstract String getFragmentTag();
}
