/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.actions

import com.bukowiecki.breakpoints.service.BreakpointManagerService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

/**
 * @author Marcin Bukowiecki
 */
abstract class BreakpointManagerBaseAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project ?: kotlin.run {
            e.presentation.isVisible = false
            return
        }

        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: kotlin.run {
            e.presentation.isVisible = false
            return
        }

        val language = psiFile.language
        val breakpointManagerService = BreakpointManagerService.getInstance(project)
        if (!breakpointManagerService.isLanguageSupported(language)) {
            e.presentation.isVisible = false
            return
        }
        super.update(e)
    }
}