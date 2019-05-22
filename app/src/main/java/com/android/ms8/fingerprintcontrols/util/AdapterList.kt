package com.android.ms8.fingerprintcontrols.util

import android.arch.lifecycle.MutableLiveData
import com.android.ms8.fingerprintcontrols.data.AppInfo

class AdapterList<T> : ArrayList<T>() {
    var posOld = -1
    var posNew = -1

    fun <T> MutableLiveData<AdapterList<T>>.add(item: T) {
        this.value?.let {
            it.posOld = -1
            it.add(item)
            it.posNew = it.size-1
        }

        // Notifies listeners that a value has changed
        this.value = this.value
    }
}

/**
 * Finds and replaces item matching newItem, placing it at the given index.
 *
 *@param newItem This item is used to match against a similar item in the list. Since the same object
 *     is used to both compare and replace, this function only works if the comparator doesn't strictly
 *     match.
 *@param newIndex The index to place the new item (index position is calculated *AFTER* item is removed
 *     from list). If null, the item is placed at the same location. If out of bounds, the item is
 *     placed at the end of the list
 *
 * @return true if the item was successfully replaced, false otherwise
 */
fun MutableLiveData<AdapterList<AppInfo>>.replace(newItem: AppInfo, newIndex: Int?) : Boolean {
    this.value?.let {
        // Find index of old value
        val pos = it.indexOf(newItem)

        // Return early if item isn't found
        if (pos == -1)
            return false

        // Remove item
        it.removeAt(pos)

        // Update posOld
        it.posOld = pos

        // Add item at newIndex
        when (newIndex) {
            null -> it.add(newItem).apply { it.posNew = pos }
            in 0 until it.size -> it.add(newIndex, newItem).apply { it.posNew = newIndex }
            else -> it.add(newItem).apply { it.posNew = it.size-1 }
        }
    }

    // Notifies listeners that a value has changed
    this.value = this.value

    return true
}

/**
 * Finds and replaces item matching newItem, placing it at the end of the list.
 *
 *@param newItem This item is used to match against a similar item in the list. Since the same object
 *     is used to both compare and replace, this function only works if the comparator doesn't strictly
 *     match.
 *
 * @return <tt>true</tt> if the item was successfully replaced, <tt>false</tt> otherwise
 */
fun MutableLiveData<AdapterList<AppInfo>>.replace(newItem: AppInfo) : Boolean {
    this.value?.let { return replace(newItem, null) }
    return false
}