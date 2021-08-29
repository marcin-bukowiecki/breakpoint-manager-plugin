/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.actions

import com.bukowiecki.breakpoints.ui.ManageTagsDialog
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil

/**
 * @author Marcin Bukowiecki
 */
class ManageTagsAction : BreakpointManagerBaseAction() {

    override fun update(e: AnActionEvent) {
        super.update(e)
        val presentation = e.presentation
        val project = e.project ?: run {
            presentation.isVisible = false
            return
        }

        val editor = e.getData(CommonDataKeys.EDITOR) ?: run {
            presentation.isVisible = false
            return
        }

        val vf = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: run {
            presentation.isVisible = false
            return
        }

        findBreakpoint(project, editor, vf)?.let {
            presentation.isVisible = true
            return
        }

        presentation.isVisible = false
        super.update(e)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run {
            return
        }

        val editor = e.getData(CommonDataKeys.EDITOR) ?: run {
            return
        }

        val vf = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: run {
            return
        }

        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: run {
            return
        }

        findBreakpoint(project, editor, vf)?.let { b ->
            ManageTagsDialog(b, editor, psiFile, project).show()
        }
    }

    private fun findBreakpoint(project: Project, editor: Editor, virtualFile: VirtualFile): XLineBreakpoint<*>? {
        val breakpointManager = XDebuggerManager.getInstance(project).breakpointManager
        val breakpointTypes = XBreakpointUtil.breakpointTypes()
        val caretModel = editor.caretModel
        val visualLineStart = caretModel.visualLineStart
        val lineNumber = editor.document.getLineNumber(visualLineStart)

        for (breakpointType in breakpointTypes) {
            if (breakpointType is XLineBreakpointType<*>) {
                breakpointManager.findBreakpointAtLine(breakpointType, virtualFile, lineNumber)?.let { b ->
                    return b
                }
            }
        }

        return null
    }
}