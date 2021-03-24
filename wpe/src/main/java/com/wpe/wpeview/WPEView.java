package com.wpe.wpeview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.wpe.wpe.Browser;
import com.wpe.wpe.gfx.View;
import com.wpe.wpe.gfx.ViewObserver;

/**
 * WPEView wraps WPE WebKit browser engine in a reusable Android library.
 * WPEView serves a similar purpose to Android's built-in WebView and tries to mimick
 * its API aiming to be an easy to use drop-in replacement with extended functionality.
 *
 * The WPEView class is the main API entry point.
 */
@UiThread
public class WPEView extends FrameLayout implements ViewObserver {
    private static final String LOGTAG = "WPEView";
    private final Context m_context;

    public WPEView(final Context context) {
        super(context);
        m_context = context;
    }

    public WPEView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        m_context = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        WPEView self = this;
        // Queue the creation of the Page until view's measure, layout, etc.
        // so we have a known width and height to create the associated WebKitWebView
        // before having the Surface texture.
        post(new Runnable() {
            @Override
            public void run() {
                Browser.getInstance().createPage(self, m_context);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Browser.getInstance().destroyPage(this);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        Browser.getInstance().onVisibilityChanged(this, visibility);
    }

    /**
     * Loads the given URL.
     * @param url The URL of the resource to be loaded.
     */
    public void loadUrl(@NonNull String url) {
        Browser.getInstance().loadUrl(this, m_context, url);
    }

    @Override @WorkerThread
    public void onViewCreated(View view) {
        Log.v(LOGTAG, "View created " + view + " number of views " + getChildCount());
        post(new Runnable() {
            public void run() {
                // Run on the main thread
                try {
                    addView(view);
                } catch(Exception e) {
                    Log.e(LOGTAG, "Error setting view " + e.toString());
                }
            }
        });
    }

    @Override @WorkerThread
    public void onViewReady(View view) {
        Log.v(LOGTAG, "View ready " + getChildCount());
        // FIXME: Once PSON is enabled we may want to do something smarter here and not
        //        display the view until this point.
    }
}