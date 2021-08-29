package com.bukowiecki.breakpoints.ui

import com.bukowiecki.breakpoints.model.TagsTableModel
import com.intellij.ui.table.JBTable
import javax.swing.table.TableModel

/**
 * @author Marcin Bukowiecki
 */
class TagsTable(tableModel: TagsTableModel) : JBTable(tableModel) {

    init {
        setupWidths()
    }

    override fun setModel(model: TableModel) {
        super.setModel(model)
        setupWidths()
    }

    private fun setupWidths() {
        val firstColumn = columnModel.getColumn(0)
        firstColumn.maxWidth = 50
    }
}