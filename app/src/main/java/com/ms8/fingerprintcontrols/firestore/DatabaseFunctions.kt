package com.ms8.fingerprintcontrols.firestore

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import com.ms8.fingerprintcontrols.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap

object DatabaseFunctions {
    fun getSuggestionTypeFromDropdown(context: Context?, selectedItem: String) : String {
        val suggestionTypes = context?.resources?.getStringArray(R.array.suggestion_types)
            ?: arrayOf("Feature Request", "Usability Improvement", "Language Translation", "Other")
        return when (selectedItem) {
            suggestionTypes[0] -> "Suggestions (Feature Request)"
            suggestionTypes[1] -> "Suggestions (Usability Improvement)"
            suggestionTypes[2] -> "Suggestions (Language Translation)"
            suggestionTypes[3] -> "Suggestions (Other)"
            else -> {
                Log.e("DatabaseFunctions", "unknown suggestion type $selectedItem")
                "UNKNOWN"
            }
        }
    }

    fun sendSuggestion(description: String, type: String): Task<DocumentReference> {
        return FirebaseFirestore.getInstance().collection(type).add(
            HashMap<String, Any>().apply {
                put("Description", description)
                put("Firebase User", FirebaseAuth.getInstance().currentUser!!.uid)
                put("Date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
            }
        )
    }


    fun getErrorTypeFromDropdown(context: Context?, selectedItem: String) : String {
        val bugTypes = context?.resources?.getStringArray(R.array.bug_severity)
            ?: arrayOf("Critical (App crashes)", "Major (App Malfunctions/ Unusable)", "Minor (Graphical / Minor hiccups)")
        return when (selectedItem) {
            bugTypes[0] -> "Errors (Critical)"
            bugTypes[1] -> "Errors (Major)"
            bugTypes[2] -> "Errors (Minor)"
            else -> {
                Log.e("DatabaseFunctions", "unknown bug type $selectedItem")
                "UNKNOWN"
            }
        }
    }

    fun sendBugReport(description: String, type: String): Task<DocumentReference> {
        val bugReport = HashMap<String, Any>().apply {
            put("Date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()))
            put("Firebase User", FirebaseAuth.getInstance().currentUser!!.uid)
            put("Description", description)
            put("Serial", Build.USER)
            put("model", Build.MODEL)
            put("Id", Build.ID)
            put("Manufacture", Build.MANUFACTURER)
            put("Brand", Build.BRAND)
            put("Type", Build.TYPE)
            put("User", Build.USER)
            put("Base", Build.VERSION_CODES.BASE)
            put("Incremental", Build.VERSION.INCREMENTAL)
            put("SDK", Build.VERSION.SDK_INT)
            put("Board", Build.BOARD)
            put("Brand", Build.BRAND)
            put("Host", Build.HOST)
            put("Fingerprint", Build.FINGERPRINT)
            put("Version Code", Build.VERSION.RELEASE)
        }

        return FirebaseFirestore.getInstance().collection(type).add(bugReport)
    }

    private fun getSuggestionTypeEndpoint(type: SuggestionType) : String =
        when (type) {
            DatabaseFunctions.SuggestionType.FEATURE_REQUEST -> "feature requests"
            DatabaseFunctions.SuggestionType.USABILITY_IMPROVEMENT -> "usability improvements"
            DatabaseFunctions.SuggestionType.LANGUAGE_TRANSLATION -> "language translations"
            DatabaseFunctions.SuggestionType.OTHER -> "others"

    }
    enum class SuggestionType {FEATURE_REQUEST, USABILITY_IMPROVEMENT, LANGUAGE_TRANSLATION, OTHER}

    const val TYPE_ERR_CRITICAL = "Errors (Critical)"
    const val TYPE_ERR_MAJOR = "Errors (Major)"
    const val TYPE_ERR_MINOR = "Errors (Minor)"

    const val TYPE_SUGG_FEATURE_REQ = "Suggestions (Feature Request)"
    const val TYPE_SUGG_USABILITY = "Suggestions (Usability Improvement)"
    const val TYPE_SUGG_LANG = "Suggestions (Language Translation)"
    const val TYPE_SUGG_OTHER = "Suggestions (Other)"
}