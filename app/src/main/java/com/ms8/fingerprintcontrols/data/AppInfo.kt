package com.ms8.fingerprintcontrols.data

import android.net.Uri
import android.view.View
import com.ms8.fingerprintcontrols.data.Configuration.Companion.APP_ACTION_DEFAULT
import com.ms8.fingerprintcontrols.util.ApkInfoFactory
import com.ms8.fingerprintcontrols.views.AppActionsDialog
import java.lang.ref.WeakReference

class AppInfo {
    var appName = ""
    var packageName = ""
    var iconUri : Uri? = null
    var numberOfCustomActions = 0

    var swipeUpAction = APP_ACTION_DEFAULT
        set(value) {
            field = value
            numberOfCustomActions = getNumOfCustomActions()
        }
    var swipeDownAction = APP_ACTION_DEFAULT
        set(value) {
            field = value
            numberOfCustomActions = getNumOfCustomActions()
        }
    var swipeLeftAction = APP_ACTION_DEFAULT
        set(value) {
            field = value
            numberOfCustomActions = getNumOfCustomActions()
        }
    var swipeRightAction = APP_ACTION_DEFAULT
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
        swipeUpAction = APP_ACTION_DEFAULT
        swipeDownAction = APP_ACTION_DEFAULT
        swipeLeftAction = APP_ACTION_DEFAULT
        swipeRightAction = APP_ACTION_DEFAULT

        ApkInfoFactory.replaceAppInfo(this, WeakReference(view.context))
    }

    /** Returns the number of custom actions bound to this application **/
    private fun getNumOfCustomActions() : Int {
        var numActions = 0
        numActions += if (swipeUpAction != APP_ACTION_DEFAULT) 1 else 0
        numActions += if (swipeDownAction != APP_ACTION_DEFAULT) 1 else 0
        numActions += if (swipeLeftAction != APP_ACTION_DEFAULT) 1 else 0
        numActions += if (swipeRightAction != APP_ACTION_DEFAULT) 1 else 0

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

}