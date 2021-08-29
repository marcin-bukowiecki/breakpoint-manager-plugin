/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

import com.intellij.ui.table.JBTable
import javax.swing.table.TableModel

/**
 * @author Marcin Bukowiecki
 */
class ImportTable(tableModel: BreakpointsToImportTableModel) : JBTable(tableModel) {

    init {
        setupWidths()
    }

    override fun setModel(model: TableModel) {
        super.setModel(model)
        setupWidths()
    }

    private fun setupWidths() {
        columnModel.getColumn(0).maxWidth = 50
        columnModel.getColumn(1).maxWidth = 50
        columnModel.getColumn(2).maxWidth = 70
    }
}