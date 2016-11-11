package pl.droidcon.app.ui.fragment.agenda;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.joda.time.DateTime;

import butterknife.ButterKnife;
import pl.droidcon.app.R;
import pl.droidcon.app.databinding.AgendaMainFragmentBinding;
import pl.droidcon.app.helper.DateTimePrinter;
import pl.droidcon.app.model.common.SessionDay;
import pl.droidcon.app.ui.fragment.BaseFragment;


public class AgendaMainFragment extends BaseFragment {

    public static final String TAG = AgendaMainFragment.class.getSimpleName();
    private AgendaMainFragmentBinding binding;

    public static AgendaMainFragment newInstance() {
        Bundle args = new Bundle();
        AgendaMainFragment fragment = new AgendaMainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = AgendaMainFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.bind(this, getView());
        binding.viewpager.setAdapter(new AgendaFragmentAdapter(getChildFragmentManager()));
        binding.tabs.setupWithViewPager(binding.viewpager);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getTitle() {
        return R.string.agenda;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    private class AgendaFragmentAdapter extends FragmentPagerAdapter {

        public AgendaFragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return AgendaFragment.newInstance(SessionDay.values()[position]);
        }

        @Override
        public int getCount() {
            return SessionDay.values().length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            DateTime when = SessionDay.values()[position].when;
            return DateTimePrinter.toPrintableDay(when);
        }
    }
}
