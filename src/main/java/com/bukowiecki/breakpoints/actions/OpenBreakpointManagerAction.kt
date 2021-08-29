/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.actions

import com.bukowiecki.breakpoints.ui.BreakpointManagerDialog
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * @author Marcin Bukowiecki
 */
class OpenBreakpointManagerAction : BreakpointManagerBaseAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        BreakpointManagerDialog(project).show()
    }
}