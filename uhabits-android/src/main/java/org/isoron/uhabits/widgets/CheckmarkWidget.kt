/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
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

package org.isoron.uhabits.widgets

import android.content.*
import android.view.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.widgets.views.*
import java.util.Calendar;
import java.util.TimeZone

import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.utils.DateUtils
import org.isoron.uhabits.utils.toThemedAndroidColor
import org.isoron.uhabits.widgets.views.CheckmarkWidgetView

open class CheckmarkWidget(
    context: Context,
    widgetId: Int,
    protected val habit: Habit,
    stacked: Boolean = false,
) : BaseWidget(context, widgetId, stacked) {

    override val defaultHeight: Int = 100
    override val defaultWidth: Int = 100

    override fun getOnClickPendingIntent(context: Context): PendingIntent? {
        return if (habit.isNumerical) {
            pendingIntentFactory.setNumericalValue(context, habit, 10, null)
        } else {
            pendingIntentFactory.toggleCheckmark(habit, null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun refreshData(widgetView: View) {
        val cal = Calendar.getInstance()
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        // get start of the week
        cal.set(Calendar.DAY_OF_WEEK, 1)
        val startOfWeek = Timestamp(cal.getTimeInMillis())

        cal.add(Calendar.DAY_OF_WEEK, 7)
        val endOfWeek = Timestamp(cal.getTimeInMillis())

        val checks = habit.computedEntries.getByInterval(startOfWeek, endOfWeek)

        // Assumes target value is out of 7 days
        val done = checks.sumBy { checkmark -> if(checkmark.value === Entry.YES_MANUAL) 1 else 0 };

        val target = if(habit.frequency.numerator === 1 && habit.frequency.denominator === 1) 7 else habit.frequency.numerator

        val daysLeftInWhichYouCanHaveToDoSomething = target - done // *Chance a recruiter will see this VS bangin it out and going to read in the park on a Sunday afternoon like God intended*

        // (v as CheckmarkWidgetView).apply {
        //     setPercentage(habit.scores.todayValue.toFloat())
        //     setActiveColor(PaletteUtils.getColor(context, habit.color))
        //     setName(habit.name)
        //     setCheckmarkValue(habit.checkmarks.todayValue)
        //     setFreeDaysLeft(daysLeftInWhichYouCanHaveToDoSomething)
        //     refresh()
        // }

        (widgetView as CheckmarkWidgetView).apply {
            val today = DateUtils.getTodayWithOffset()
            setBackgroundAlpha(preferedBackgroundAlpha)
            activeColor = habit.color.toThemedAndroidColor(context)
            name = habit.name
            entryValue = habit.computedEntries.get(today).value
            freeDays = daysLeftInWhichYouCanHaveToDoSomething

            if (habit.isNumerical) {
                isNumerical = true
                entryState = getNumericalEntryState()
            } else {
                entryState = habit.computedEntries.get(today).value
            }
            percentage = habit.scores[today].value.toFloat()
            refresh()
        }

    }

    override fun buildView(): View {
        return CheckmarkWidgetView(context)
    }

    private fun getNumericalEntryState(): Int {
        return if (habit.isCompletedToday()) {
            Entry.YES_MANUAL
        } else {
            Entry.NO
        }
    }
}
