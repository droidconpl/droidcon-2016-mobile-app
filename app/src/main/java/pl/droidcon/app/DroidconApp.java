package pl.droidcon.app;

import android.app.Application;

import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import net.danlew.android.joda.JodaTimeAndroid;

import pl.droidcon.app.dagger.DroidconInjector;

public class DroidconApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        DroidconInjector.init(this);

        Iconify
                .with(new FontAwesomeModule());
    }
}
