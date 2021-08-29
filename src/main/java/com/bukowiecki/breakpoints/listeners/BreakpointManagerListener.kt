/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.listeners

import com.bukowiecki.breakpoints.tagging.BreakpointTag
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.util.messages.Topic
import com.intellij.xdebugger.breakpoints.XLineBreakpoint

/**
 * @author Marcin Bukowiecki
 */
interface BreakpointManagerListener {

    fun editorOpened(source: FileEditorManager, file: VirtualFile) {

    }

    fun editorClosed(source: FileEditorManager, file: VirtualFile) {

    }

    fun editorChanged(event: FileEditorManagerEvent) {

    }

    fun breakpointRemoved(breakpoint: XLineBreakpoint<*>) {

    }

    fun breakpointAdded(breakpoint: XLineBreakpoint<*>) {

    }

    fun breakpointStateChanged() {

    }

    fun tagSelectionChanged() {

    }

    fun tagsEnabled() {

    }

    fun tagsDisabled() {

    }

    fun tagsSaved(psiFile: PsiFile, editors: List<Editor>, breakpoint: XLineBreakpoint<*>) {

    }

    fun tagRemoved(tag: BreakpointTag) {

    }

    companion object {

        val topic = Topic(
            "BreakpointManager.Events",
            BreakpointManagerListener::class.java
        )
    }
}