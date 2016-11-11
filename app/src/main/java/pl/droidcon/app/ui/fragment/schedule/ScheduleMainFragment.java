package pl.droidcon.app.ui.fragment.schedule;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;

import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.databinding.FavouritesFragmentBinding;
import pl.droidcon.app.helper.DateTimePrinter;
import pl.droidcon.app.model.common.SessionDay;
import pl.droidcon.app.ui.fragment.BaseFragment;

public class ScheduleMainFragment extends BaseFragment {

    public static final String TAG = ScheduleMainFragment.class.getSimpleName();
    private FavouritesFragmentBinding binding;

    public static ScheduleMainFragment newInstance() {
        Bundle args = new Bundle();
        ScheduleMainFragment fragment = new ScheduleMainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DroidconInjector.get().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FavouritesFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.viewpager.setAdapter(new MyScheduleFragmentAdapter(getChildFragmentManager()));
        binding.tabs.setupWithViewPager(binding.viewpager);
    }

    @Override
    public int getTitle() {
        return R.string.favourites;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    private class MyScheduleFragmentAdapter extends FragmentPagerAdapter {

        public MyScheduleFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ScheduleFragment.newInstance(SessionDay.values()[position]);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            DateTime when = SessionDay.values()[position].when;
            return DateTimePrinter.toPrintableDay(when);
        }
    }

}

