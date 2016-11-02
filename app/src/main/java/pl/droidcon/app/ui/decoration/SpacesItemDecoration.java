package pl.droidcon.app.ui.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import pl.droidcon.app.model.db.Session;
import pl.droidcon.app.ui.adapter.BaseSessionViewHolder;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(float space) {
        this.space = (int) space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        BaseSessionViewHolder sessionViewHolder = (BaseSessionViewHolder) parent.getChildViewHolder(view);
        Session session = sessionViewHolder.getSession();
        if (session.isSingleItem()) {
            // single element on list
            outRect.right = space;
            outRect.left = space;
        } else {
            if (session.isLeft()) {
                // left element
                outRect.right = (int) (space * 0.5f);
                outRect.left = space;
            } else {
                // right element
                outRect.right = space;
                outRect.left = (int) (space * 0.5f);
            }
        }

        outRect.top = (int) (space * 0.5f);
        outRect.bottom = (int) (space * 0.5f);
    }
}
