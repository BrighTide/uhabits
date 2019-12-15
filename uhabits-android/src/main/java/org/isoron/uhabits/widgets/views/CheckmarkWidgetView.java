/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.widgets.views;

import android.content.*;
import android.support.annotation.*;
import android.util.*;
import android.widget.*;

import org.isoron.androidbase.utils.*;
import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.activities.common.views.*;
import org.isoron.uhabits.utils.*;

import static org.isoron.androidbase.utils.InterfaceUtils.getDimension;

public class CheckmarkWidgetView extends HabitWidgetView
{
    private int activeColor;

    private float percentage;

    @Nullable
    private String name;

    private RingView ring;

    private TextView label;

    private int checkmarkValue;

    private int freeDays;

    public CheckmarkWidgetView(Context context)
    {
        super(context);
        init();
    }

    public CheckmarkWidgetView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public void refresh()
    {
        if (backgroundPaint == null || frame == null || ring == null) return;

        StyledResources res = new StyledResources(getContext());

        String text;
        int fgColor;
        int transparent = android.graphics.Color.argb(0, 0, 0, 0);
        int white = android.graphics.Color.argb(175, 255, 255, 255);

        int highContrast = res.getColor(R.attr.highContrastReverseTextColor);
        int lowContrast = R.attr.cardBackgroundColor;
        setShadowAlpha(0x00);

        rebuildBackground();

        backgroundPaint.setColor(transparent);
        frame.setBackgroundDrawable(background);

        switch (checkmarkValue)
        {
            case Checkmark.CHECKED_EXPLICITLY:
                ring.setColor(activeColor);
                ring.setText(getResources().getString(R.string.fa_check));

                label.setTextColor(highContrast);

                break;

            case Checkmark.CHECKED_IMPLICITLY:
                ring.setColor(white);
                if (freeDays <= 0) {
                    ring.setText(getResources().getString(R.string.fa_check));
                }
                else {
                    ring.setText(String.valueOf(freeDays));
                }

                label.setTextColor(highContrast);

                break;

            case Checkmark.UNCHECKED:
            default:
                ring.setColor(white);
                ring.setText(String.valueOf(freeDays));

                label.setTextColor(highContrast);

                break;
        }

        ring.setPercentage(percentage);
        ring.setBackgroundColor(transparent); // I think this needs to be transparent

        label.setText(name);

        requestLayout();
        postInvalidate();
    }

    public void setActiveColor(int activeColor)
    {
        this.activeColor = activeColor;
    }

    public void setCheckmarkValue(int checkmarkValue)
    {
        this.checkmarkValue = checkmarkValue;
    }

    public void setFreeDaysLeft(int freeDays) {
        this.freeDays = freeDays;
    }

    public void setName(@NonNull String name)
    {
        this.name = name;
    }

    public void setPercentage(float percentage)
    {
        this.percentage = percentage;
    }

    @Override
    @NonNull
    protected Integer getInnerLayoutId()
    {
        return R.layout.widget_checkmark;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        float w = width;
        float h = width * 1.25f;
        float scale = Math.min(width / w, height / h);

        w *= scale;
        h *= scale;

        if (h < getDimension(getContext(), R.dimen.checkmarkWidget_heightBreakpoint))
            ring.setVisibility(GONE);
        else
            ring.setVisibility(VISIBLE);

        widthMeasureSpec =
                MeasureSpec.makeMeasureSpec((int) w, MeasureSpec.EXACTLY);
        heightMeasureSpec =
                MeasureSpec.makeMeasureSpec((int) h, MeasureSpec.EXACTLY);

        float textSize = 0.15f * h;
        float maxTextSize = getDimension(getContext(), R.dimen.smallerTextSize);
        textSize = Math.min(textSize, maxTextSize);

        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        ring.setTextSize(textSize);
        ring.setThickness(0.15f * textSize);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void init()
    {
        ring = (RingView) findViewById(R.id.scoreRing);
        label = (TextView) findViewById(R.id.label);

        if (ring != null) ring.setIsTransparencyEnabled(true);

        if (isInEditMode())
        {
            percentage = 0.75f;
            name = "Wake up early";
            activeColor = PaletteUtils.getAndroidTestColor(6);
            checkmarkValue = Checkmark.CHECKED_EXPLICITLY;
            refresh();
        }
    }
}
