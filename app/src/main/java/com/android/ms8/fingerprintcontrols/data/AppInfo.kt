package com.android.ms8.fingerprintcontrols.data

import android.app.Dialog
import android.databinding.DataBindingUtil
import android.net.Uri
import android.util.Log
import android.view.View
import com.android.ms8.fingerprintcontrols.AppActionsDialog

class AppInfo {
    var appName = ""
    var packageName = ""
    var iconUri : Uri? = null
    var numberOfCustomActions = 0

    var swipeUpAction = ACTION_SAME_AS_DEFAULT
        set(value) {
            field = value
            numberOfCustomActions = getNumOfCustomActions()
        }
    var swipeDownAction = ACTION_SAME_AS_DEFAULT
        set(value) {
            field = value
            numberOfCustomActions = getNumOfCustomActions()
        }
    var swipeLeftAction = ACTION_SAME_AS_DEFAULT
        set(value) {
            field = value
            numberOfCustomActions = getNumOfCustomActions()
        }
    var swipeRightAction = ACTION_SAME_AS_DEFAULT
        set(value) {
        field = value
        numberOfCustomActions = getNumOfCustomActions()
    }

    /** Shows a dialog allowing the user to change gesture actions for application **/
    fun onAppClicked(view: View) {
        Log.d("test####", "onAppClicked: $appName")
        AppActionsDialog(view.context, this).show()
    }

    /** Returns the number of custom actions bound to this application **/
    private fun getNumOfCustomActions() : Int {
        var numActions = if (swipeUpAction == ACTION_SAME_AS_DEFAULT) 1 else 0
        numActions += if (swipeDownAction == ACTION_SAME_AS_DEFAULT) 1 else 0
        numActions += if (swipeLeftAction == ACTION_SAME_AS_DEFAULT) 1 else 0
        numActions += if (swipeRightAction == ACTION_SAME_AS_DEFAULT) 1 else 0

        return numActions
    }

    constructor()

    constructor(name: String, pName: String, pIconUri: Uri) {
        appName = name
        packageName = pName
        iconUri = pIconUri
    }

    companion object {
        const val ACTION_NONE = 0
        const val ACTION_BACK = 1
        const val ACTION_HOME = 2
        const val ACTION_RECENTS = 3
        const val ACTION_NOTIFICATIONS = 4
        const val ACTION_POWER_MENU = 5
        const val ACTION_QUICK_SETTINGS = 6
        const val ACTION_SCROLL_LEFT = 7
        const val ACTION_SCROLL_RIGHT = 8
        const val ACTION_SCROLL_UP = 9
        const val ACTION_SCROLL_DOWN = 10
        const val ACTION_SAME_AS_DEFAULT = 11
    }
}