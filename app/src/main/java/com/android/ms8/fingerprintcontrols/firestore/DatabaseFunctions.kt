package com.android.ms8.fingerprintcontrols.firestore

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

object DatabaseFunctions {
    fun SendSuggestion(suggestion: String, type: SuggestionType) =
        FirebaseDatabase.getInstance().getReference("suggestions")
            .child(getSuggestionTypeEndpoint(type))
            .push().setValue(suggestion).also { Log.e("TEST", "SENDING") }


    private fun getSuggestionTypeEndpoint(type: SuggestionType) : String =
        when (type) {
            DatabaseFunctions.SuggestionType.FEATURE_REQUEST -> "feature requests"
            DatabaseFunctions.SuggestionType.USABILITY_IMPROVEMENT -> "usability improvements"
            DatabaseFunctions.SuggestionType.LANGUAGE_TRANSLATION -> "language translations"
            DatabaseFunctions.SuggestionType.OTHER -> "others"

    }

    enum class SuggestionType {FEATURE_REQUEST, USABILITY_IMPROVEMENT, LANGUAGE_TRANSLATION, OTHER}
    enum class ErrorType {CRITICAL, MAJOR, LOW}
}