/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

import javax.swing.Icon
import javax.swing.table.AbstractTableModel

/**
 * @author Marcin Bukowiecki
 */
class BreakpointsToImportTableModel(private val myBreakpoints: List<ImportableBreakpointWrapper>) : AbstractTableModel() {

    override fun getRowCount(): Int {
        return myBreakpoints.size
    }

    override fun getColumnCount(): Int {
        return 4
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        if (columnIndex == 0) return true
        if (columnIndex == 2) {
            val row = myBreakpoints[rowIndex]
            return !row.overrideDisabled
        }
        return false
    }

    override fun getColumnName(column: Int): String {
        if (column == 0) {
            return ""
        }
        if (column == 1) {
            return ""
        }
        if (column == 2) {
            return "Override"
        }
        if (column == 3) {
            return "Place"
        }
        return super.getColumnName(column)
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return myBreakpoints[rowIndex].getColumn(columnIndex)
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex == 0) {
            myBreakpoints[rowIndex].myChecked = aValue as? Boolean ?: true
        }
        if (columnIndex == 2) {
            myBreakpoints[rowIndex].myOverride = aValue as? Boolean ?: true
        }
        super.setValueAt(aValue, rowIndex, columnIndex)
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        return when (columnIndex) {
            0 -> {
                java.lang.Boolean::class.java
            }
            1 -> {
                Icon::class.java
            }
            2 -> {
                java.lang.Boolean::class.java
            }
            else -> {
                String::class.java
            }
        }
    }

    fun getSelectedBreakpoints(): List<ImportableBreakpointWrapper> {
        return myBreakpoints.filter { it.myChecked }
    }
}