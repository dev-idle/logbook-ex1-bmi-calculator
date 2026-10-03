package com.comp1786.logbook.bmi.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.comp1786.logbook.bmi.R;
import com.google.android.material.color.MaterialColors;

import java.util.List;

/**
 * A bar of equal-width colored segments, labeled at the boundaries, with a pointer above it. The
 * result card uses it to show where a BMI falls among the categories.
 */
public final class BmiGaugeView extends View {

    private final Paint segmentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF segment = new RectF();
    private final Path pointer = new Path();

    private final float barHeight;
    private final float segmentGap;
    private final float pointerWidth;
    private final float pointerHeight;
    private final float spacing;

    private int[] colors = new int[0];
    private List<String> labels = List.of();
    private float position = Float.NaN;

    public BmiGaugeView(Context context) {
        this(context, null);
    }

    public BmiGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Resources resources = getResources();
        barHeight = resources.getDimension(R.dimen.gauge_bar_height);
        segmentGap = resources.getDimension(R.dimen.gauge_segment_gap);
        pointerWidth = resources.getDimension(R.dimen.gauge_pointer_width);
        pointerHeight = resources.getDimension(R.dimen.gauge_pointer_height);
        spacing = resources.getDimension(R.dimen.gauge_spacing);

        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(resources.getDimension(R.dimen.gauge_label_text_size));
        labelPaint.setColor(MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnSurfaceVariant));
        pointerPaint.setColor(MaterialColors.getColor(
                this, com.google.android.material.R.attr.colorOnSurface));
        pointerPaint.setPathEffect(new CornerPathEffect(pointerWidth / 6));
    }

    /** Shows one segment per color, with one label fewer at the boundaries between them. */
    void setSegments(@ColorInt int[] segmentColors, List<String> boundaryLabels) {
        if (boundaryLabels.size() != segmentColors.length - 1) {
            throw new IllegalArgumentException("Expected one label per boundary");
        }
        colors = segmentColors.clone();
        labels = List.copyOf(boundaryLabels);
        invalidate();
    }

    /** Points at {@code segments} along the bar: 1.5 is the middle of the second segment. */
    void setPointer(float segments) {
        position = segments;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float labelHeight = labelPaint.descent() - labelPaint.ascent();
        float height = getPaddingTop() + pointerHeight + spacing + barHeight + spacing
                + labelHeight + getPaddingBottom();
        setMeasuredDimension(
                getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                resolveSize((int) Math.ceil(height), heightMeasureSpec));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int count = colors.length;
        if (count == 0) {
            return;
        }
        float left = getPaddingLeft();
        float right = getWidth() - getPaddingRight();
        float segmentWidth = (right - left - segmentGap * (count - 1)) / count;
        float step = segmentWidth + segmentGap;
        float barTop = getPaddingTop() + pointerHeight + spacing;
        float radius = barHeight / 2;

        for (int i = 0; i < count; i++) {
            float start = mirror(left + i * step);
            float end = mirror(left + i * step + segmentWidth);
            segment.set(Math.min(start, end), barTop, Math.max(start, end), barTop + barHeight);
            segmentPaint.setColor(colors[i]);
            canvas.drawRoundRect(segment, radius, radius, segmentPaint);
        }

        float baseline = barTop + barHeight + spacing - labelPaint.ascent();
        for (int i = 0; i < labels.size(); i++) {
            float boundary = left + (i + 1) * step - segmentGap / 2;
            canvas.drawText(labels.get(i), mirror(boundary), baseline, labelPaint);
        }

        if (!Float.isNaN(position)) {
            int index = Math.min((int) position, count - 1);
            float x = left + index * step + (position - index) * segmentWidth;
            // Keep the whole pointer inside the view at either end of the bar.
            x = mirror(Math.max(left + pointerWidth / 2, Math.min(right - pointerWidth / 2, x)));
            float top = getPaddingTop();
            pointer.rewind();
            pointer.moveTo(x - pointerWidth / 2, top);
            pointer.lineTo(x + pointerWidth / 2, top);
            pointer.lineTo(x, top + pointerHeight);
            pointer.close();
            canvas.drawPath(pointer, pointerPaint);
        }
    }

    /** Flips {@code x} in right-to-left layouts, so the gauge starts on the right. */
    private float mirror(float x) {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL ? getWidth() - x : x;
    }
}
