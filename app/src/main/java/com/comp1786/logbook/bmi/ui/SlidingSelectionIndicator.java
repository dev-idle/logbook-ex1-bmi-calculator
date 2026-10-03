package com.comp1786.logbook.bmi.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.Button;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.motion.MotionUtils;

/**
 * The background of a single-selection toggle group: a rounded track with a pill that slides to
 * the checked button. Labels change color as the pill passes under them.
 */
final class SlidingSelectionIndicator extends Drawable {

    private static final int FALLBACK_DURATION_MILLIS = 300;

    private final MaterialButtonToggleGroup group;
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF track = new RectF();
    private final RectF pill = new RectF();
    private final RectF slideStart = new RectF();
    private final ValueAnimator slide = ValueAnimator.ofFloat(0f, 1f);
    @ColorInt
    private final int labelColor;
    @ColorInt
    private final int checkedLabelColor;

    private SlidingSelectionIndicator(MaterialButtonToggleGroup group) {
        this.group = group;
        trackPaint.setColor(MaterialColors.getColor(
                group, com.google.android.material.R.attr.colorSurfaceContainerHigh));
        pillPaint.setColor(MaterialColors.getColor(group, androidx.appcompat.R.attr.colorPrimary));
        labelColor = MaterialColors.getColor(
                group, com.google.android.material.R.attr.colorOnSurfaceVariant);
        checkedLabelColor = MaterialColors.getColor(
                group, com.google.android.material.R.attr.colorOnPrimary);

        // Material 3 motion tokens, so the slide matches other component transitions.
        Context context = group.getContext();
        slide.setDuration(MotionUtils.resolveThemeDuration(context,
                com.google.android.material.R.attr.motionDurationMedium2,
                FALLBACK_DURATION_MILLIS));
        // The fallback is the Material 3 standard easing curve, cubic-bezier(0.2, 0, 0, 1).
        slide.setInterpolator(MotionUtils.resolveThemeInterpolator(context,
                com.google.android.material.R.attr.motionEasingEmphasizedInterpolator,
                new PathInterpolator(0.2f, 0f, 0f, 1f)));
        slide.addUpdateListener(animation -> {
            View target = checkedButton();
            if (target != null) {
                float fraction = (float) animation.getAnimatedValue();
                pill.set(lerp(slideStart.left, target.getLeft(), fraction),
                        lerp(slideStart.top, target.getTop(), fraction),
                        lerp(slideStart.right, target.getRight(), fraction),
                        lerp(slideStart.bottom, target.getBottom(), fraction));
                onPillMoved();
            }
        });

        group.addOnButtonCheckedListener((toggleGroup, checkedId, isChecked) -> {
            if (isChecked) {
                slideToCheckedButton();
            }
        });
        group.addOnLayoutChangeListener((view, left, top, right, bottom,
                                         oldLeft, oldTop, oldRight, oldBottom) -> {
            if (!slide.isRunning()) {
                snapToCheckedButton();
            }
        });
        group.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View view) {
                // Nothing to restore: the layout listener places the pill.
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View view) {
                slide.cancel();
            }
        });
    }

    /** Makes a sliding selection indicator the background of {@code group}. */
    static void attach(MaterialButtonToggleGroup group) {
        group.setBackground(new SlidingSelectionIndicator(group));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float trackRadius = track.height() / 2;
        canvas.drawRoundRect(track, trackRadius, trackRadius, trackPaint);
        if (!pill.isEmpty()) {
            float pillRadius = pill.height() / 2;
            canvas.drawRoundRect(pill, pillRadius, pillRadius, pillPaint);
        }
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        track.set(bounds);
    }

    @Override
    public void setAlpha(int alpha) {
        trackPaint.setAlpha(alpha);
        pillPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        trackPaint.setColorFilter(colorFilter);
        pillPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    @SuppressWarnings("deprecation") // Still abstract in Drawable, so it must be implemented.
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private void slideToCheckedButton() {
        // Before the first layout there is nothing to slide from; the layout listener places it.
        if (!group.isLaidOut() || pill.isEmpty()) {
            snapToCheckedButton();
            return;
        }
        slide.cancel();
        slideStart.set(pill);
        slide.start();
    }

    private void snapToCheckedButton() {
        View checked = checkedButton();
        if (checked == null) {
            pill.setEmpty();
        } else {
            pill.set(checked.getLeft(), checked.getTop(), checked.getRight(), checked.getBottom());
        }
        onPillMoved();
    }

    /** Colors each label by how much of its button the pill covers. */
    private void onPillMoved() {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Button button && button.getWidth() > 0) {
                float covered = Math.min(pill.right, button.getRight())
                        - Math.max(pill.left, button.getLeft());
                float ratio = Math.max(0f, covered) / button.getWidth();
                button.setTextColor(ColorUtils.blendARGB(labelColor, checkedLabelColor, ratio));
            }
        }
        invalidateSelf();
    }

    @Nullable
    private View checkedButton() {
        int id = group.getCheckedButtonId();
        return id == View.NO_ID ? null : group.findViewById(id);
    }

    private static float lerp(float from, float to, float fraction) {
        return from + (to - from) * fraction;
    }
}
