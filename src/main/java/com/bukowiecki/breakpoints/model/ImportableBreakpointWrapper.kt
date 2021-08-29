/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

/**
 * @author Marcin Bukowiecki
 */
class ImportableBreakpointWrapper(private val myImportableBreakpoint: ImportableBreakpoint) {

    var myChecked: Boolean = true

    var myOverride: Boolean = false

    var overrideDisabled = true

    fun getColumn(index: Int): Any {
        return when (index) {
            0 -> {
                myChecked
            }
            1 -> {
                myImportableBreakpoint.getIcon()
            }
            2 -> {
                myOverride
            }
            3 -> {
                myImportableBreakpoint.getPresentableName()
            }
            else -> {
                throw UnsupportedOperationException()
            }
        }
    }

    fun getImportableBreakpoint(): ImportableBreakpoint = myImportableBreakpoint
}