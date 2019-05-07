package com.android.ms8.fingerprintcontrols.views

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.RequiresApi
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ScrollView
import com.android.ms8.fingerprintcontrols.R
import com.android.ms8.fingerprintcontrols.util.Dp
import com.android.ms8.fingerprintcontrols.util.Px
import com.android.ms8.fingerprintcontrols.util.density

/**
 * Created by Hugo Castelani
 * Date: 19/09/17
 * Time: 19:30
 */

/**
 * Modifications by Matthew Steinhardt
 * Date: 06/05/19
 * Time: 13:24
 */

class WaterfallToolbar : CardView {
    init {
        // set density to be able to use DimensionUnits
        // this code must run before all the signings using DimensionUnits
        if (density == null) density = resources.displayMetrics.density
    }

    /**
     * The recycler view whose scroll is going to be listened
     */
    var recyclerView: RecyclerView? = null
        set(value) {
            if (value == null) {
                // Unbinding recyclerView
                if (field != null)
                {
                    // Save recyclerView height position
                    lastRecyclerViewPos = realPosition.value
                    removeRecyclerViewScrollListener()
                    field = value
                }
            } else {
                field = value
                addRecyclerViewScrollListener()
                ValueAnimator.ofInt(realPosition.value, lastRecyclerViewPos)
                    .apply {
                        duration = 600
                        interpolator = DecelerateInterpolator()
                        addUpdateListener {
                            realPosition.value = animatedValue as Int
                            mutualScrollListenerAction()
                        }
                        start()
                    }
            }
        }

    /**
     * Reference to RecyclerView listener that is used to adjusting toolbar height
     */
    var recyclerViewListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            // real position must always get updated

            realPosition.value = realPosition.value + dy
            mutualScrollListenerAction()
        }
    }

    /**
     * The scroll view whose scroll is going to be listened
     */
    var scrollView: ScrollView? = null
        set(value) {
            if (value == null) {
                // Unbinding ScrollView
                if (field != null)
                {
                    // Save ScrollView height position
                    lastScrollViewPos = realPosition.value
                    Log.d("test%%%", "savedScrollViewPos = $lastScrollViewPos")
                    removeScrollViewScrollListener()
                    field = value
                }
            } else {
                field = value
                addScrollViewScrollListener()
                ValueAnimator.ofInt(realPosition.value, lastScrollViewPos)
                    .apply {
                        duration = 600
                        interpolator = DecelerateInterpolator()
                        addUpdateListener {
                            realPosition.value = animatedValue as Int
                            mutualScrollListenerAction()
                        }
                        start()
                    }
            }
        }

    /**
     * Reference to ScrollView listener that is used to adjust toolbar height
     */
    var scrollViewListener: () -> Unit = {
        // real position must always get updated
        // Safety check on nullable ScrollView
        scrollView?.let {
            realPosition.value = it.scrollY
            mutualScrollListenerAction() }
    }

    /**
     * The three variables ahead are null safe, since they are always set
     * at least once in init() and a null value can't be assigned to them
     * after that. So all the "!!" involving them below are fully harmless.
     */

    /**
     * The elevation with which the toolbar starts
     */
    var initialElevation: Px? = null
        set(value) {
            if (value != null) {
                field = value

                // got to update elevation in case this value have
                // been set in a running and visible activity
                if (isSetup) adjustCardElevation()

            } else throw NullPointerException("This field cannot be null.")
        }

    /**
     * The elevation the toolbar gets when it reaches final scroll elevation
     */
    var finalElevation: Px? = null
        set(value) {
            if (value != null) {
                field = value

                // got to update elevation in case this value have
                // been set in a running and visible activity
                if (isSetup) adjustCardElevation()

            } else throw NullPointerException("This field cannot be null.")
        }

    /**
     * The percentage of the screen's height that is
     * going to be scrolled to reach the final elevation
     */
    var scrollFinalPosition: Int? = null
        set(value) {
            if (value != null) {
                val screenHeight = resources.displayMetrics.heightPixels
                field = Math.round(screenHeight * (value / 100.0f))

                // got to update elevation in case this value have
                // been set in a running and visible activity
                if (isSetup) adjustCardElevation()

            } else throw NullPointerException("This field cannot be null.")
        }

    /**
     * Dimension units (dp and pixel) auxiliary
     */


    /**
     * Values related to Waterfall Toolbar behavior in their default forms
     */
    val defaultInitialElevation = Dp(0f).toPx()
    val defaultFinalElevation = Dp(4f).toPx()
    val defaultScrollFinalElevation = 12
    val defaultResetHeightOnUnbind = true

    var lastScrollViewPos = defaultInitialElevation.value
    var lastRecyclerViewPos = defaultInitialElevation.value

    /**
     * Determines whether toolbar height is reset to
     * initial position when ScrollView or RecyclerView
     * is unbound (i.e. set to null)
     */
    //private var isResetHeightOnUnbind: Boolean = defaultResetHeightOnUnbind

    /**
     * Auxiliary that indicates if the view is already setup
     */
    private var isSetup: Boolean = false

    /**
     * Position in which toolbar must be to reach expected shadow
     */
    private var orthodoxPosition = Px(0)

    /**
     * Recycler/scroll view real position
     */
    private var realPosition = Px(0)

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int?)
            : super(context, attrs, defStyleAttr!!) {
        init(context, attrs)
    }

    private fun init(context: Context?, attrs: AttributeSet?) {
        // leave card corners square
        radius = 0f

        if (context != null && attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaterfallToolbar)

            val rawInitialElevation = typedArray.getDimensionPixelSize(
                R.styleable.WaterfallToolbar_initial_elevation, defaultInitialElevation.value)

            val rawFinalElevation = typedArray.getDimensionPixelSize(
                R.styleable.WaterfallToolbar_final_elevation, defaultFinalElevation.value)

            scrollFinalPosition = typedArray.getInteger(
                R.styleable.WaterfallToolbar_scroll_final_elevation, defaultScrollFinalElevation)

            this.initialElevation = Px(rawInitialElevation)
            this.finalElevation = Px(rawFinalElevation)
            //this.isResetHeightOnUnbind = typedArray.getBoolean(
            //    R.styleable.WaterfallToolbar_reset_height_on_unbind, defaultResetHeightOnUnbind)

            typedArray.recycle()

        } else {

            initialElevation = defaultInitialElevation
            finalElevation = defaultFinalElevation
            scrollFinalPosition = defaultScrollFinalElevation
            //isResetHeightOnUnbind = defaultResetHeightOnUnbind
        }

        adjustCardElevation()    // just to make sure card elevation is set

        isSetup = true
    }

    private fun addRecyclerViewScrollListener() {
        recyclerView?.addOnScrollListener(recyclerViewListener)
    }

    private fun removeRecyclerViewScrollListener() {
        recyclerView?.removeOnScrollListener(recyclerViewListener)
    }

    private fun addScrollViewScrollListener() {
        scrollView?.viewTreeObserver?.addOnScrollChangedListener(scrollViewListener)
    }

    private fun removeScrollViewScrollListener() {
        viewTreeObserver?.removeOnScrollChangedListener(scrollViewListener)
    }

    /**
     * These lines are common in both scroll listeners, so they are better joined
     */
    private fun mutualScrollListenerAction() {
        // orthodoxPosition can't be higher than scrollFinalPosition because
        // the last one holds the position in which shadow reaches ideal size

        if (realPosition.value <= scrollFinalPosition!!) {
            orthodoxPosition.value = realPosition.value
        } else {
            orthodoxPosition.value = scrollFinalPosition!!
        }

        adjustCardElevation()
    }

    /**
     * Speed up the card elevation setting
     */
    private fun adjustCardElevation() {
        cardElevation = calculateElevation().value.toFloat()
    }

    /**
     * Calculates the elevation based on given attributes and scroll
     * @return New calculated elevation
     */
    private fun calculateElevation(): Px {
        // getting back to rule of three:
        // finalElevation = scrollFinalPosition
        // newElevation   = orthodoxPosition
        var newElevation: Int = finalElevation!!.value * orthodoxPosition.value / scrollFinalPosition!!

        // avoid values under minimum value
        if (newElevation < initialElevation!!.value) newElevation = initialElevation!!.value

        return Px(newElevation)
    }

    /**
     * Saves the view's current dynamic state in a parcelable object
     * @return A parcelable with the saved data
     */
    override fun onSaveInstanceState(): Parcelable? {
        super.onSaveInstanceState()?.let {
            val savedState = SavedState(it)

            savedState.elevation = cardElevation.toInt()
            savedState.orthodoxPosition = orthodoxPosition
            savedState.realPosition = realPosition

            return savedState
        }

        return null
    }

    /**
     * Restore the view's dynamic state
     * @param state The frozen state that had previously been returned by onSaveInstanceState()
     */
    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)

            // setting card elevation doesn't work until view is created
            post {
                // it's safe to use "!!" here, since savedState will
                // always store values properly set in onSaveInstanceState()
                cardElevation = state.elevation!!.toFloat()
                orthodoxPosition = state.orthodoxPosition!!
                realPosition = state.realPosition!!
            }

        } else {

            super.onRestoreInstanceState(state)
        }
    }

    /**
     * Custom parcelable to store this view's dynamic state
     */
    private class SavedState : View.BaseSavedState {
        var elevation: Int? = null
        var orthodoxPosition: Px? = null
        var realPosition: Px? = null

        internal constructor(source: Parcel) : super(source)

        @RequiresApi(api = Build.VERSION_CODES.N)
        internal constructor(source: Parcel, loader: ClassLoader) : super(source, loader)

        internal constructor(superState: Parcelable) : super(superState)


        companion object {
            @JvmField
            internal val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}