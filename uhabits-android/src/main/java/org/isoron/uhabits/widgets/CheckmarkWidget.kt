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

package org.isoron.uhabits.widgets

import android.content.*
import android.view.*
import org.isoron.uhabits.core.models.*
import org.isoron.uhabits.utils.*
import org.isoron.uhabits.widgets.views.*
import java.util.Calendar;
import java.util.TimeZone


class CheckmarkWidget(
        context: Context,
        widgetId: Int,
        private val habit: Habit
) : BaseWidget(context, widgetId) {

    override fun getOnClickPendingIntent(context: Context) =
            pendingIntentFactory.toggleCheckmark(habit, null)

    override fun refreshData(v: View) {
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

        val checks = habit.checkmarks.getByInterval(startOfWeek, endOfWeek)

        // Assumes target value is out of 7 days
        val done = checks.sumBy { checkmark -> if(checkmark.value === Checkmark.CHECKED_EXPLICITLY) 1 else 0 };

        val target = if(habit.frequency.numerator === 1 && habit.frequency.denominator === 1) 7 else habit.frequency.numerator

        val daysLeftInWhichYouCanHaveToDoSomething = target - done // *Chance a recruiter will see this VS bangin it out and going to read in the park on a Sunday afternoon like God intended*
        println(daysLeftInWhichYouCanHaveToDoSomething)

        println(target)
        println(done)
        println(daysLeftInWhichYouCanHaveToDoSomething)


        (v as CheckmarkWidgetView).apply {
            setPercentage(habit.scores.todayValue.toFloat())
            setActiveColor(PaletteUtils.getColor(context, habit.color))
            setName(habit.name)
            setCheckmarkValue(habit.checkmarks.todayValue)
            setFreeDaysLeft(daysLeftInWhichYouCanHaveToDoSomething)
            refresh()
        }
    }

    override fun buildView() = CheckmarkWidgetView(context)
    override fun getDefaultHeight() = 125
    override fun getDefaultWidth() = 125
}
