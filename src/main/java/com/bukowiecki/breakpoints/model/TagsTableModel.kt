/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

import com.bukowiecki.breakpoints.listeners.BreakpointManagerListener
import com.intellij.openapi.project.Project
import javax.swing.table.AbstractTableModel

/**
 * @author Marcin Bukowiecki
 */
class TagsTableModel(private val myProject: Project,
                     private val myTags: List<TagWrapper>) : AbstractTableModel() {

    override fun getRowCount(): Int {
        return myTags.size
    }

    override fun getColumnCount(): Int {
        return 3
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        if (columnIndex == 0) return true
        return false
    }

    override fun getColumnName(column: Int): String {
        if (column == 0) {
            return ""
        }
        if (column == 1) {
            return "Key"
        }
        if (column == 2) {
            return "Value"
        }
        return super.getColumnName(column)
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return myTags[rowIndex].getColumn(columnIndex)
    }

    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
        if (columnIndex == 0) {
            myTags[rowIndex].myChecked = aValue as? Boolean ?: true
            myProject.messageBus.syncPublisher(BreakpointManagerListener.topic).tagSelectionChanged()
        }
        super.setValueAt(aValue, rowIndex, columnIndex)
    }

    override fun getColumnClass(columnIndex: Int): Class<*> {
        if (columnIndex == 0) return java.lang.Boolean::class.java
        return String::class.java
    }

    fun getSelectedTags(): List<TagWrapper> {
        return myTags.filter { it.myChecked }
    }
}