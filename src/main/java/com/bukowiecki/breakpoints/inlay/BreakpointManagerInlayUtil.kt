/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.inlay

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import java.lang.ref.WeakReference

/**
 * @author Marcin Bukowiecki
 */
object BreakpointManagerInlayUtil {

    fun createLineInlay(
        offset: Int,
        editor: Editor,
        text: String,
        margin: Int,
        breakpoint: XLineBreakpoint<*>
    ) {

        ApplicationManager.getApplication().invokeLater {
            editor
                .inlayModel
                .addAfterLineEndElement(
                    offset,
                    true,
                    BreakpointManagerElementRenderer(
                        text,
                        margin,
                        WeakReference(breakpoint),
                        BreakpointManagerInlayType
                    )
                )
        }
    }

    fun removeLineInlay(project: Project, breakpoint: XLineBreakpoint<*>) {
        ApplicationManager.getApplication().invokeLater({
            val allEditors = FileEditorManager.getInstance(project).allEditors
            allEditors.filterIsInstance<TextEditor>().map { it }.forEach {
                val editor = it.editor
                val document = editor.document

                editor.inlayModel.getAfterLineEndElementsInRange(
                    0,
                    document.textLength,
                    BreakpointManagerElementRenderer::class.java
                ).filter { inlay -> inlay.renderer.inlayType.javaClass == BreakpointManagerInlayType.javaClass }
                .filter { inlay ->
                    val renderer = inlay.renderer
                    val ref = renderer.breakpointReference.get()
                    ref == breakpoint
                }.forEach { inlay -> Disposer.dispose(inlay) }
            }
        }, project.disposed)
    }

    fun removeLineInlays(project: Project) {
        ApplicationManager.getApplication().invokeLater({
            val allEditors = FileEditorManager.getInstance(project).allEditors
            allEditors.filterIsInstance<TextEditor>().map { it }.forEach {
                val editor = it.editor
                val document = editor.document

                editor.inlayModel.getAfterLineEndElementsInRange(
                    0,
                    document.textLength,
                    BreakpointManagerElementRenderer::class.java
                ).filter { inlay ->
                    inlay.renderer.inlayType.javaClass == BreakpointManagerInlayType.javaClass
                }.forEach { inlay -> Disposer.dispose(inlay) }
            }
        }, project.disposed)
    }
}