package pl.droidcon.app.helper;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;


public final class HtmlCompat {

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(@NonNull String source) {
        String result = handleDelTag(source);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(result, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(result);
        }
    }

    private static String handleDelTag(String source) {
        return source.replaceAll("&ltdel", "&ltstrike");
    }

    private HtmlCompat() {
    }
}
