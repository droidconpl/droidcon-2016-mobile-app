package pl.droidcon.app.ui.activity;

import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import javax.inject.Inject;

import pl.droidcon.app.R;
import pl.droidcon.app.dagger.DroidconInjector;
import pl.droidcon.app.databinding.ActivityMainBinding;
import pl.droidcon.app.rx.DataSubscription;
import pl.droidcon.app.ui.fragment.BaseFragment;
import pl.droidcon.app.ui.fragment.factory.DrawerFragmentFactory;

public class MainActivity extends BaseActivity {

    @Inject
    DrawerFragmentFactory drawerFragmentFactory;

    @Inject
    DataSubscription dataSubscription;

    private ActionBarDrawerToggle actionBarDrawerToggle;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        DroidconInjector.get().inject(this);
        drawerFragmentFactory.restoreState(savedInstanceState, getSupportFragmentManager());
        setupToolbar(binding.mainToolbar.toolbar);
        setupNavigationView();
        dataSubscription.fetchData();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        drawerFragmentFactory.saveInstanceState(outState, getSupportFragmentManager());
    }

    private void setupNavigationView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                binding.drawerLayout,
                binding.mainToolbar.toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);

        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                binding.drawerLayout.closeDrawers();
                boolean checked = openDrawerMenu(menuItem.getItemId());
                checkRightDrawerMenuItem(menuItem, checked);
                return true;
            }
        });

        openDrawerMenu(drawerFragmentFactory.getLastFragmentOrDefault());
    }


    /**
     * open drawer menu item and return flag indicating should element be checked
     *
     * @param menuItemId id of drawer menu item
     * @return true if element should be checked
     */
    private boolean openDrawerMenu(@IdRes int menuItemId) {
        if (R.id.drawer_settings == menuItemId) {
            SettingsActivity.start(this);
            return false;
        } else {
            openFragment(menuItemId);
            return true;
        }
    }

    private void checkRightDrawerMenuItem(MenuItem menuItem, boolean checked) {
        if (checked) {
            menuItem.setChecked(true);
            return;
        }
        int lastFragmentOrDefault = drawerFragmentFactory.getLastFragmentOrDefault();
        binding.navigationView.getMenu().findItem(lastFragmentOrDefault).setChecked(true);
    }

    private void openFragment(@IdRes int menuItemId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        BaseFragment fragment = drawerFragmentFactory.getFragmentByMenuItemId(menuItemId);

        drawerFragmentFactory.setCurrentFragmentMenuId(menuItemId);
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
        setToolbarTitle(fragment.getTitle());
    }

}
