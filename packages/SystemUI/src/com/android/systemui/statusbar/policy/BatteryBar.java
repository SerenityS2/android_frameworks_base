/*
<<<<<<< HEAD
 * Copyright (C) 2013 The CyanogenMod Project
=======
 * Copyright (C) 2013 The ChameleonOS Project
>>>>>>> b839fd4... Add battery bar. [1/2]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
<<<<<<< HEAD
import android.database.ContentObserver;
import android.graphics.drawable.Animatable;
=======
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
>>>>>>> b839fd4... Add battery bar. [1/2]
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
<<<<<<< HEAD
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class BatteryBar extends RelativeLayout implements Animatable {

    private static final String TAG = BatteryBar.class.getSimpleName();

    // Total animation duration
    private static final int ANIM_DURATION = 1000; // 5 seconds

    private boolean mAttached = false;
    private int mBatteryLevel = 0;
    private int mChargingLevel = -1;
    private boolean mBatteryCharging = false;
    private boolean shouldAnimateCharging = true;
    private boolean isAnimating = false;

    private Handler mHandler = new Handler();

    LinearLayout mBatteryBarLayout;
    View mBatteryBar;

    LinearLayout mChargerLayout;
    View mCharger;

    public static final int STYLE_REGULAR = 0;
    public static final int STYLE_SYMMETRIC = 1;

    boolean vertical = false;

    class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        void observer() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.STATUSBAR_BATTERY_BAR), false, this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.STATUSBAR_BATTERY_BAR_COLOR), false,
                    this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE),
                    false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    public BatteryBar(Context context) {
        this(context, null);
    }

    public BatteryBar(Context context, boolean isCharging, int currentCharge) {
        this(context, null);
        mBatteryLevel = currentCharge;
        mBatteryCharging = isCharging;
    }

    public BatteryBar(Context context, boolean isCharging, int currentCharge, boolean isVertical) {
        this(context, null);
        mBatteryLevel = currentCharge;
        mBatteryCharging = isCharging;
        vertical = isVertical;
=======
import android.view.View;
import android.R;

public class BatteryBar extends View {
    private static final long ANIMATION_FRAME_DELAY = 20;
    private static final long ANIMATION_RESTART_DELAY = 2000;

    private Handler mHandler;
    private Context mContext;
    private BatteryReceiver mBatteryReceiver = null;

    // state variables
    private boolean mAttached;      // whether or not attached to a window
    private boolean mActivated;     // whether or not activated due to system settings
    private boolean mBatteryPlugged;// whether or not battery is currently plugged
    private int     mBatteryStatus; // current battery status
    private int     mLevel;         // current battery level
    private int     mAnimOffset;    // current level of charging animation
    private boolean mIsAnimating;   // stores charge-animation status to reliably remove callbacks

    private int     mLowColor;
    private int     mHighColor;
    private Paint   mPaint;

    private int     mHeight;

    GradientDrawable mBarGradient;
    int[] mGradientColors;

    // runnable to invalidate view via mHandler.postDelayed() call
    private final Runnable mInvalidate = new Runnable() {
        public void run() {
            if(mActivated && mAttached) {
                invalidate();
            }
        }
    };

    // observes changes in system battery settings and enables/disables view accordingly
    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        public void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_BATTERY), false, this);
            onChange(true);
        }

        @Override
        public void onChange(boolean selfChange) {
            int batteryStyle = (Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.STATUS_BAR_BATTERY, 0));

            mActivated = (batteryStyle == BatteryController.BATTERY_STYLE_BAR);

            setVisibility(mActivated && isBatteryPresent() ? View.VISIBLE : View.GONE);
            if (mBatteryReceiver != null) {
                mBatteryReceiver.updateRegistration();
            }

            if (mActivated && mAttached) {
                invalidate();
            }
        }
    }

    // keeps track of current battery level and charger-plugged-state
    class BatteryReceiver extends BroadcastReceiver {
        private boolean mIsRegistered = false;

        public BatteryReceiver(Context context) {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                onBatteryStatusChange(intent);

                int visibility = View.VISIBLE;//mActivated && isBatteryPresent() ? View.VISIBLE : View.GONE;
                if (getVisibility() != visibility) {
                    setVisibility(visibility);
                }

                if (mActivated && mAttached) {
                    invalidate();
                }
            }
        }

        private void registerSelf() {
            if (!mIsRegistered) {
                mIsRegistered = true;

                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_BATTERY_CHANGED);
                mContext.registerReceiver(mBatteryReceiver, filter);
            }
        }

        private void unregisterSelf() {
            if (mIsRegistered) {
                mIsRegistered = false;
                mContext.unregisterReceiver(this);
            }
        }

        private void updateRegistration() {
            if (mActivated && mAttached) {
                registerSelf();
            } else {
                unregisterSelf();
            }
        }
    }

    /***
     * Start of CircleBattery implementation
     */
    public BatteryBar(Context context) {
        this(context, null);
>>>>>>> b839fd4... Add battery bar. [1/2]
    }

    public BatteryBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
<<<<<<< HEAD
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;

            mBatteryBarLayout = new LinearLayout(mContext);
            addView(mBatteryBarLayout, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

            mBatteryBar = new View(mContext);
            mBatteryBarLayout.addView(mBatteryBar, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
            float dp = 4f;
            int pixels = (int) (metrics.density * dp + 0.5f);

            // Charger
            mChargerLayout = new LinearLayout(mContext);

            if (vertical) {
                addView(mChargerLayout, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        pixels));
            } else {
                addView(mChargerLayout, new RelativeLayout.LayoutParams(pixels,
                        LayoutParams.MATCH_PARENT));
            }

            mCharger = new View(mContext);
            mChargerLayout.setVisibility(View.GONE);
            mChargerLayout.addView(mCharger, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());

            SettingsObserver observer = new SettingsObserver(mHandler);
            observer.observer();
            updateSettings();
=======

        mContext = context;
        mHandler = new Handler();

        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();
        mBatteryReceiver = new BatteryReceiver(mContext);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        Resources res = getResources();

        mHeight = res.getDimensionPixelSize(com.android.systemui.R.dimen.battery_bar_height);

        mLowColor = res.getColor(R.color.holo_red_light);
        mHighColor = res.getColor(R.color.holo_blue_light);
        mPaint.setColor(mHighColor);

        mGradientColors = new int[2];
        mGradientColors[0] = mLowColor;
        mGradientColors[1] = mHighColor;

        mBarGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, mGradientColors);
    }

    protected int getLevel() {
        return mLevel;
    }

    protected int getBatteryStatus() {
        return mBatteryStatus;
    }

    protected boolean isBatteryPlugged() {
        return mBatteryPlugged;
    }

    protected boolean isBatteryPresent() {
        return true;
    }

    private boolean isBatteryStatusUnknown() {
        return getBatteryStatus() == BatteryManager.BATTERY_STATUS_UNKNOWN;
    }

    private boolean isBatteryStatusCharging() {
        return getBatteryStatus() == BatteryManager.BATTERY_STATUS_CHARGING;
    }

    protected void onBatteryStatusChange(Intent intent) {
        mLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        mBatteryPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
        mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                                            BatteryManager.BATTERY_STATUS_UNKNOWN);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            mBatteryReceiver.updateRegistration();
            mHandler.postDelayed(mInvalidate, 250);
>>>>>>> b839fd4... Add battery bar. [1/2]
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            mAttached = false;
<<<<<<< HEAD
            getContext().unregisterReceiver(mIntentReceiver);
        }
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                mBatteryCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0) == BatteryManager.BATTERY_STATUS_CHARGING;
                if (mBatteryCharging && mBatteryLevel < 100) {
                    start();
                } else {
                    stop();
                }
                setProgress(mBatteryLevel);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                stop();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                if (mBatteryCharging && mBatteryLevel < 100) {
                    start();
                }
            }
        }
    };

    private void updateSettings() {
        ContentResolver resolver = getContext().getContentResolver();

        int color = Settings.System.getInt(resolver, Settings.System.STATUSBAR_BATTERY_BAR_COLOR,
                0xFF33B5E5);

        shouldAnimateCharging = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1;

        if (mBatteryCharging && mBatteryLevel < 100 && shouldAnimateCharging) {
            start();
        } else {
            stop();
        }
        setProgress(mBatteryLevel);
        mBatteryBar.setBackgroundColor(color);
        mCharger.setBackgroundColor(color);
    }

    private void setProgress(int n) {
        if (vertical) {
            int w = (int) (((getHeight() / 100.0) * n) + 0.5);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBatteryBarLayout
                    .getLayoutParams();
            params.height = w;
            mBatteryBarLayout.setLayoutParams(params);
        } else {
            int w = (int) (((getWidth() / 100.0) * n) + 0.5);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBatteryBarLayout
                    .getLayoutParams();
            params.width = w;
            mBatteryBarLayout.setLayoutParams(params);
        }
    }

    @Override
    public void start() {
        if (!shouldAnimateCharging) {
            return;
        }

        if (vertical) {
            TranslateAnimation a = new TranslateAnimation(getX(), getX(), getHeight(),
                    mBatteryBarLayout.getHeight());
            a.setInterpolator(new AccelerateInterpolator());
            a.setDuration(ANIM_DURATION);
            a.setRepeatCount(Animation.INFINITE);
            mChargerLayout.startAnimation(a);
            mChargerLayout.setVisibility(View.VISIBLE);
        } else {
            TranslateAnimation a = new TranslateAnimation(getWidth(), mBatteryBarLayout.getWidth(),
                    getTop(), getTop());
            a.setInterpolator(new AccelerateInterpolator());
            a.setDuration(ANIM_DURATION);
            a.setRepeatCount(Animation.INFINITE);
            mChargerLayout.startAnimation(a);
            mChargerLayout.setVisibility(View.VISIBLE);
        }
        isAnimating = true;
    }

    @Override
    public void stop() {
        mChargerLayout.clearAnimation();
        mChargerLayout.setVisibility(View.GONE);
        isAnimating = false;
    }

    @Override
    public boolean isRunning() {
        return isAnimating;
=======
            mBatteryReceiver.updateRegistration();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        updateChargeAnim();
        float width = (float)getWidth();
        float size = (mIsAnimating ? mAnimOffset : mLevel) / 100f;

        mGradientColors[1] = mixColors(mHighColor, mLowColor, size);
        mBarGradient.setBounds(0, 0, (int)(width*size), mHeight);
        mBarGradient.setColors(mGradientColors);
        mBarGradient.draw(canvas);
    }

    private int mixColors(int color1, int color2, float mix) {
        int[] rgb1 = colorToRgb(color1);
        int[] rgb2 = colorToRgb(color2);

        rgb1[0] = mixedValue(rgb1[0], rgb2[0], mix);
        rgb1[1] = mixedValue(rgb1[1], rgb2[1], mix);
        rgb1[2] = mixedValue(rgb1[2], rgb2[2], mix);
        rgb1[3] = mixedValue(rgb1[3], rgb2[3], mix);

        return rgbToColor(rgb1);
    }

    private int[] colorToRgb(int color) {
        int[] rgb = {(color & 0xFF000000) >> 24, (color & 0xFF0000) >> 16, (color & 0xFF00) >> 8, (color & 0xFF)};
        return rgb;
    }

    private int rgbToColor(int[] rgb) {
        return (rgb[0] << 24) + (rgb[1] << 16) + (rgb[2] << 8) + rgb[3];
    }

    private int mixedValue(int val1, int val2, float mix) {
        return (int)Math.min((mix * val1 + (1f - mix) * val2), 255f);
    }

    /***
     * updates the animation counter
     * cares for timed callbacks to continue animation cycles
     * uses mInvalidate for delayed invalidate() callbacks
     */
    private void updateChargeAnim() {
        if (!isBatteryStatusCharging()) {
            if (mIsAnimating) {
                mIsAnimating = false;
                mAnimOffset = 0;
                mHandler.removeCallbacks(mInvalidate);
            }
            return;
        }

        mIsAnimating = true;
        long delay = ANIMATION_FRAME_DELAY;

        if (mAnimOffset >= mLevel) {
            mAnimOffset = 0;
        } else {
            mAnimOffset += 1;
            if (mAnimOffset >= mLevel) {
                mAnimOffset = mLevel;
                delay = ANIMATION_RESTART_DELAY;
            }
        }

        mHandler.removeCallbacks(mInvalidate);
        mHandler.postDelayed(mInvalidate, delay);
>>>>>>> b839fd4... Add battery bar. [1/2]
    }
}
