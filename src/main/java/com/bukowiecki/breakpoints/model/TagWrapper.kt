/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

import com.bukowiecki.breakpoints.tagging.BreakpointTag

/**
 * @author Marcin Bukowiecki
 */
class TagWrapper(private val myBreakpointTag: BreakpointTag) {

    var myChecked: Boolean = false

    constructor(breakpointTag: BreakpointTag, checked: Boolean): this(breakpointTag) {
        this.myChecked = checked
    }

    fun getColumn(index: Int): Any {
        return when (index) {
            0 -> {
                myChecked
            }
            1 -> {
                myBreakpointTag.key
            }
            2 -> {
                myBreakpointTag.value
            }
            else -> {
                throw UnsupportedOperationException()
            }
        }
    }

    fun getBreakpointTag(): BreakpointTag = myBreakpointTag
}