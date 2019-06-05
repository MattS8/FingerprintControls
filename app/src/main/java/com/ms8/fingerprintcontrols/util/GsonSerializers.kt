package com.ms8.fingerprintcontrols.util

import android.net.Uri
import com.google.gson.*
import java.lang.reflect.Type

class GsonSerializers {
    class UriSerializer : JsonSerializer<Uri?> {
        override fun serialize(src: Uri?, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src?.toString())
        }
    }

    class UriDeserializer : JsonDeserializer<Uri?> {
        @Throws(JsonParseException::class)
        override fun deserialize(src: JsonElement, srcType: Type, context: JsonDeserializationContext): Uri? {
            return if (src.toString() == "")
                null
            else
                Uri.parse(src.toString())
        }
    }
}
