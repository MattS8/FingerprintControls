package com.android.ms8.fingerprintcontrols.data

import android.net.Uri
import android.view.View
import com.android.ms8.fingerprintcontrols.util.ApkInfoFactory
import com.android.ms8.fingerprintcontrols.views.AppActionsDialog
import java.lang.ref.WeakReference

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
        AppActionsDialog(view.context, this).show()
    }

    /**
     * Resets app info to default settings.
     * NOTE: This function calls a background task to write updated ModifiedApps list to file.
     **/
    fun onAppResetClicked(view: View) {
        swipeUpAction = ACTION_SAME_AS_DEFAULT
        swipeDownAction = ACTION_SAME_AS_DEFAULT
        swipeLeftAction = ACTION_SAME_AS_DEFAULT
        swipeRightAction = ACTION_SAME_AS_DEFAULT

        ApkInfoFactory.replaceAppInfo(this, WeakReference(view.context))
    }

    /** Returns the number of custom actions bound to this application **/
    private fun getNumOfCustomActions() : Int {
        var numActions = 0
        numActions += if (swipeUpAction != ACTION_SAME_AS_DEFAULT) 1 else 0
        numActions += if (swipeDownAction != ACTION_SAME_AS_DEFAULT) 1 else 0
        numActions += if (swipeLeftAction != ACTION_SAME_AS_DEFAULT) 1 else 0
        numActions += if (swipeRightAction != ACTION_SAME_AS_DEFAULT) 1 else 0

        return numActions
    }

    constructor()

    constructor(name: String, pName: String, pIconUri: Uri) {
        appName = name
        packageName = pName
        iconUri = pIconUri
    }

    override fun toString(): String {
        return "AppInfo name: $appName\nAppInfo packageName: $packageName" +
                "\n\tNumberOfCustomActions: $numberOfCustomActions" +
                "\n\tSwipeUpAction (int id): $swipeUpAction" +
                "\n\tSwipeDownAction (int id): $swipeDownAction" +
                "\n\tSwipeLeftAction (int id): $swipeLeftAction" +
                "\n\tSwipeRightAction (int id): $swipeRightAction"
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is AppInfo && other.packageName == packageName
    }

    override fun hashCode(): Int {
        var result = appName.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + (iconUri?.hashCode() ?: 0)
        result = 31 * result + numberOfCustomActions
        result = 31 * result + swipeUpAction
        result = 31 * result + swipeDownAction
        result = 31 * result + swipeLeftAction
        result = 31 * result + swipeRightAction
        return result
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