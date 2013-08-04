package com.android.systemui.statusbar.halo;

import android.os.Handler;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.animation.ValueAnimator;
import android.animation.Animator;
import android.view.View;

public class CustomObjectAnimator {

    private View rootView;
    private Handler handler = new Handler();
    private ObjectAnimator animator;

    public CustomObjectAnimator(View root) {
        rootView = root;
    }

    public void animate(ObjectAnimator newInstance, TimeInterpolator interpolator, AnimatorUpdateListener update) {
        runAnimation(newInstance, interpolator, update, null);
    }

    public void animate(final ObjectAnimator newInstance, final TimeInterpolator interpolator,
            final AnimatorUpdateListener update, long startDelay, final Runnable executeAfter) {

        handler.postDelayed(new Runnable() {
            public void run() {
                runAnimation(newInstance, interpolator, update, executeAfter);
            }}, startDelay);
    }

    private void runAnimation(ObjectAnimator newInstance, TimeInterpolator interpolator,
            AnimatorUpdateListener update, final Runnable executeAfter) {

        // Terminate old instance, if present
        cancel(false);
        animator = newInstance;

        // Invalidate
        if (update == null) {
            animator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    rootView.invalidate();
                }});
        } else {
            animator.addUpdateListener(update);
        }
        
        animator.setInterpolator(interpolator);

        if (executeAfter != null) {
            animator.addListener(new Animator.AnimatorListener() {
                boolean canceled = false;
                @Override public void onAnimationRepeat(Animator animation) {}
                @Override public void onAnimationStart(Animator animation) {}
                @Override public void onAnimationCancel(Animator animation) {
                    canceled = true;
                }
                @Override public void onAnimationEnd(Animator animation) {
                    if (!canceled) executeAfter.run();
                }});
        }
        
        animator.start();
    }

    public void cancel(boolean unschedule) {
        if (unschedule) handler.removeCallbacksAndMessages(null);
        if (animator != null) animator.cancel();
    }
}
