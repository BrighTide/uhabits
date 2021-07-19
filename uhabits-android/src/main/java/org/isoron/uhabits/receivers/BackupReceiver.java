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

package org.isoron.uhabits.receivers;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import org.isoron.uhabits.HabitsApplication;
import org.isoron.uhabits.core.models.Habit;
import org.isoron.uhabits.core.models.HabitList;
import org.isoron.uhabits.core.tasks.ExportCSVTask;
import org.isoron.uhabits.core.ui.widgets.WidgetBehavior;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import dagger.Component;

/**
 * The Android BroadcastReceiver for Loop Habit Tracker.
 * <p>
 * All broadcast messages are received and processed by this class.
 */
public class BackupReceiver extends BroadcastReceiver
{

    public static final String BACKUP =
            "org.isoron.uhabits.ACTION_BACKUP";

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        HabitsApplication app =
                (HabitsApplication) context.getApplicationContext();
        System.out.println("BACK UP TRIGGERED1!");

        try
        {
            switch (intent.getAction())
            {
                case BACKUP:

                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "sync/projects/habits/exports");

                    HabitList habitList = app.getComponent().getHabitList();

                    List<Habit> selected = new LinkedList<>();
                    for (Habit h : habitList) selected.add(h);

                    app.getComponent().getTaskRunner().execute(
                            new ExportCSVTask(habitList, selected, dir, filename ->
                            {
                            })
                    );

                    break;

            }
        }
        catch (RuntimeException e)
        {
            Log.e("WidgetReceiver", "could not process intent", e);
        }
    }

}
