package com.james.status.views;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.icon.IconData;
import com.james.status.utils.ColorAnimator;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class StatusView extends FrameLayout {

    private LinearLayout status, leftLayout, rightLayout, centerLayout;

    @ColorInt
    private Integer color, iconColor = Color.WHITE;
    private boolean isSystemShowing, isFullscreen, isAnimations, isIconAnimations, isTintedIcons, isContrastIcons;

    private List<IconData> icons;

    public StatusView(Context context) {
        super(context);
    }

    public StatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public StatusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setUp() {
        if (status != null && status.getParent() != null) removeView(status);

        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_status, null);
        status = (LinearLayout) v.findViewById(R.id.status);
        status.getLayoutParams().height = StaticUtils.getStatusBarHeight(getContext());

        leftLayout = (LinearLayout) v.findViewById(R.id.notificationIcons);
        rightLayout = (LinearLayout) v.findViewById(R.id.statusIcons);
        centerLayout = (LinearLayout) v.findViewById(R.id.statusCenterIcons);

        Boolean isAnimations = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_BACKGROUND_ANIMATIONS);
        this.isAnimations = isAnimations != null ? isAnimations : true;

        Boolean isIconAnimations = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_ICON_ANIMATIONS);
        this.isIconAnimations = isIconAnimations != null ? isIconAnimations : true;

        if (this.isIconAnimations) {
            leftLayout.setLayoutTransition(new LayoutTransition());
            rightLayout.setLayoutTransition(new LayoutTransition());
            centerLayout.setLayoutTransition(new LayoutTransition());
        } else {
            leftLayout.setLayoutTransition(null);
            rightLayout.setLayoutTransition(null);
            centerLayout.setLayoutTransition(null);
        }

        Boolean isTintedIcons = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_TINTED_ICONS);
        this.isTintedIcons = isTintedIcons != null ? isTintedIcons : false;

        Boolean isContrastIcons = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);
        this.isContrastIcons = isContrastIcons != null ? isContrastIcons : true;

        addView(v);

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isStatusColorAuto != null && !isStatusColorAuto) {
            Integer statusBarColor = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
            if (statusBarColor != null) setColor(statusBarColor);
        } else if (color != null) setColor(color);
        else setColor(Color.BLACK);
    }

    public void setIcons(List<IconData> icons) {
        for (int i = (leftLayout.getChildCount() - 1); i >= 0; i--) {
            View child = leftLayout.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag instanceof IconData) {
                ((IconData) tag).unregister();
                leftLayout.removeViewAt(i);
            }
        }

        for (int i = (centerLayout.getChildCount() - 1); i >= 0; i--) {
            View child = centerLayout.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag instanceof IconData) {
                ((IconData) tag).unregister();
                centerLayout.removeViewAt(i);
            }
        }

        for (int i = (rightLayout.getChildCount() - 1); i >= 0; i--) {
            View child = rightLayout.getChildAt(i);
            Object tag = child.getTag();

            if (tag != null && tag instanceof IconData) {
                ((IconData) tag).unregister();
                rightLayout.removeViewAt(i);
            }
        }

        this.icons = icons;

        for (final IconData iconData : icons) {
            if (!iconData.isVisible()) continue;

            final View item = iconData.getIconView();

            iconData.setDrawableListener(new IconData.DrawableListener() {
                @Override
                public void onUpdate(@Nullable Drawable drawable) {
                    CustomImageView iconView = (CustomImageView) item.findViewById(R.id.icon);

                    if (drawable != null && iconView != null)
                        ImageUtils.tintDrawable(iconView, drawable, iconColor);
                    else if (iconView == null || !iconView.getParent().equals(item))
                        setIconTint(item, iconColor);
                }
            });

            switch (iconData.getGravity()) {
                case IconData.LEFT_GRAVITY:
                    leftLayout.addView(item, 0);
                    break;
                case IconData.CENTER_GRAVITY:
                    centerLayout.addView(item, 0);
                    break;
                case IconData.RIGHT_GRAVITY:
                    rightLayout.addView(item, 0);
                    break;
            }
        }
    }

    public List<IconData> getIcons() {
        if (icons == null) icons = new ArrayList<>();
        return icons;
    }

    public void register() {
        if (icons != null) {
            for (IconData icon : icons) {
                icon.register();
            }
        }
    }

    public void unregister() {
        if (icons != null) {
            for (IconData icon : icons) {
                icon.unregister();
            }
        }
    }

    public void setSystemShowing(boolean isSystemShowing) {
        if ((this.isFullscreen != isSystemShowing || this.isSystemShowing != isSystemShowing) && isSystemShowing)
            setStatusBarVisibility(false);
        this.isSystemShowing = isSystemShowing;
    }

    public boolean isSystemShowing() {
        return isSystemShowing;
    }

    public void setFullscreen(boolean isFullscreen) {
        if (((getVisibility() == View.GONE) != isFullscreen) && !isSystemShowing) {
            setStatusBarVisibility(!isFullscreen);
        }

        this.isFullscreen = isFullscreen;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    private void setStatusBarVisibility(final boolean visible) {
        if (isAnimations) {
            ValueAnimator animator = ValueAnimator.ofFloat(getY(), visible ? 0 : -StaticUtils.getStatusBarHeight(getContext()));
            animator.setDuration(150);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float y = (float) valueAnimator.getAnimatedValue();
                    setY(y);
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    if (visible) setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (!visible) setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });
            animator.start();
        } else {
            if (visible) setVisibility(View.VISIBLE);
            else setVisibility(View.GONE);
        }
    }

    public void setColor(@ColorInt int color) {
        if (this.color == null) this.color = Color.BLACK;
        color = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));

        if (!isTintedIcons) {
            if (isAnimations) {
                new ColorAnimator(this.color, color).setDuration(150).setColorUpdateListener(new ColorAnimator.ColorUpdateListener() {
                    @Override
                    public void onColorUpdate(ColorAnimator animator, @ColorInt int color) {
                        if (status != null) {
                            status.setBackgroundColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                        }
                    }
                }).start();
            } else
                status.setBackgroundColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));

            setDarkMode(!ColorUtils.isColorDark(color));
        } else if (status != null) {
            int backgroundColor = getDefaultColor();
            if (color == backgroundColor) {
                if (color == Color.BLACK) color = Color.WHITE;
                else if (color == Color.WHITE) color = Color.BLACK;
            }

            status.setBackgroundColor(backgroundColor);

            if (isContrastIcons)
                color = ColorUtils.isColorDark(backgroundColor) ? ColorUtils.lightColor(color) : ColorUtils.darkColor(color);

            if (isIconAnimations) {
                new ColorAnimator(this.color, color).setDuration(150).setColorUpdateListener(new ColorAnimator.ColorUpdateListener() {
                    @Override
                    public void onColorUpdate(ColorAnimator animator, @ColorInt int color) {
                        if (status != null) {
                            setIconTint(status, Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                        }
                    }
                }).start();
            } else
                setIconTint(status, Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));

            iconColor = color;
        }

        this.color = color;
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    @ColorInt
    private int getDefaultColor() {
        Integer color = PreferenceUtils.getIntegerPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
        if (color == null) color = Color.BLACK;
        return color;
    }

    public void setHomeScreen() {
        if (status != null) {
            Bitmap background = ImageUtils.cropBitmapToBar(getContext(), ImageUtils.drawableToBitmap(WallpaperManager.getInstance(getContext()).getDrawable()));

            if (background != null) {
                int color = ColorUtils.getAverageColor(background);

                Boolean transparent = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_HOME_TRANSPARENT);
                if (transparent == null || transparent) {
                    status.setBackground(new BitmapDrawable(getResources(), background));
                    setDarkMode(!ColorUtils.isColorDark(color));
                    StatusView.this.color = color;
                } else setColor(color);
            } else setColor(Color.BLACK);
        }
    }

    public void setDarkMode(boolean isDarkMode) {
        if (isContrastIcons) {
            int color = isDarkMode ? Color.BLACK : Color.WHITE;

            if (isIconAnimations) {
                new ColorAnimator(iconColor, color).setDuration(150).setColorUpdateListener(new ColorAnimator.ColorUpdateListener() {
                    @Override
                    public void onColorUpdate(ColorAnimator animator, @ColorInt int color) {
                        if (status != null) {
                            setIconTint(status, Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                        }
                    }
                }).start();
            } else
                setIconTint(status, Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));

            iconColor = color;
        }
    }

    private void setIconTint(View view, int color) {
        if (view instanceof LinearLayout) {
            for (int i = 0; i < ((LinearLayout) view).getChildCount(); i++) {
                setIconTint(((LinearLayout) view).getChildAt(i), color);
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        } else if (view instanceof CustomImageView) {
            CustomImageView imageView = (CustomImageView) view;
            if (imageView.getDrawable() != null)
                ImageUtils.tintDrawable(imageView, imageView.getDrawable(), color);
        }
    }

    public void setLockscreen(boolean lockscreen) {
        Boolean expand = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_LOCKSCREEN_EXPAND);
        if (expand != null && expand)
            status.getLayoutParams().height = StaticUtils.getStatusBarHeight(getContext()) * (lockscreen ? 3 : 1);

        if (lockscreen) {
            Palette.from(ImageUtils.drawableToBitmap(WallpaperManager.getInstance(getContext()).getFastDrawable())).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    setColor(palette.getDarkVibrantColor(ColorUtils.darkColor(palette.getVibrantColor(Color.BLACK))));
                }
            });
        }
    }
}
