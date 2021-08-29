/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.actions

import com.bukowiecki.breakpoints.ui.ExportBreakpointsDialog
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil

/**
 * @author Marcin Bukowiecki
 */
class ExportBreakpointsAction : BreakpointManagerBaseAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ExportBreakpointsDialog(project, XBreakpointUtil.collectPanelProviders()).show()
    }
}