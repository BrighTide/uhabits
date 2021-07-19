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

package org.isoron.uhabits.activities.habits.list

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.isoron.uhabits.BaseExceptionHandler
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.activities.habits.list.views.HabitCardListAdapter
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.ui.ThemeSwitcher.Companion.THEME_DARK
import org.isoron.uhabits.core.utils.MidnightTimer
import org.isoron.uhabits.database.AutoBackup
import org.isoron.uhabits.inject.ActivityContextModule
import org.isoron.uhabits.inject.DaggerHabitsActivityComponent
import org.isoron.uhabits.utils.restartWithFade

class ListHabitsActivity : AppCompatActivity() {

    var pureBlack: Boolean = false
    lateinit var taskRunner: TaskRunner
    lateinit var adapter: HabitCardListAdapter
    lateinit var rootView: ListHabitsRootView
    lateinit var screen: ListHabitsScreen
    lateinit var prefs: Preferences
    lateinit var midnightTimer: MidnightTimer
    private val scope = CoroutineScope(Dispatchers.Main)

    private lateinit var menu: ListHabitsMenu


    // Storage Permissions
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     * @param activity
     */
    fun verifyStoragePermissions(activity: Activity) {
        // Check if we have write permission
        val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appComponent = (applicationContext as HabitsApplication).component
        val component = DaggerHabitsActivityComponent
            .builder()
            .activityContextModule(ActivityContextModule(this))
            .habitsApplicationComponent(appComponent)
            .build()
        component.themeSwitcher.apply()

        prefs = appComponent.preferences
        pureBlack = prefs.isPureBlackEnabled
        midnightTimer = appComponent.midnightTimer
        rootView = component.listHabitsRootView
        screen = component.listHabitsScreen
        adapter = component.habitCardListAdapter
        taskRunner = appComponent.taskRunner
        menu = component.listHabitsMenu
        Thread.setDefaultUncaughtExceptionHandler(BaseExceptionHandler(this))
        component.listHabitsBehavior.onStartup()

        this.verifyStoragePermissions(this)
        setContentView(rootView)
    }

    override fun onPause() {
        midnightTimer.onPause()
        screen.onDettached()
        adapter.cancelRefresh()
        super.onPause()
    }

    override fun onResume() {
        adapter.refresh()
        screen.onAttached()
        rootView.postInvalidate()
        midnightTimer.onResume()
        taskRunner.run {
            AutoBackup(this@ListHabitsActivity).run()
        }
        if (prefs.theme == THEME_DARK && prefs.isPureBlackEnabled != pureBlack) {
            restartWithFade(ListHabitsActivity::class.java)
        }
        super.onResume()
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {
        menu.onCreate(menuInflater, m)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        invalidateOptionsMenu()
        return menu.onItemSelected(item)
    }

    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        super.onActivityResult(request, result, data)
        screen.onResult(request, result, data)
    }
}
