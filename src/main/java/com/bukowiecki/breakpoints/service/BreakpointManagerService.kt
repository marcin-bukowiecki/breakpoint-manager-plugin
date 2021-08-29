/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service

import com.bukowiecki.breakpoints.inlay.BreakpointManagerInlayUtil
import com.bukowiecki.breakpoints.listeners.BreakpointManagerListener
import com.bukowiecki.breakpoints.tagging.BreakpointTag
import com.bukowiecki.breakpoints.tagging.TagsProperties
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.util.messages.MessageBusConnection
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import git4idea.branch.GitBranchUtil

/**
 * @author Marcin Bukowiecki
 */
class BreakpointManagerService(private val myProject: Project) {

    private lateinit var myConnection: MessageBusConnection

    @Volatile
    private var supportedLanguages = setOf<Language>()

    fun addSupportedLanguage(language: Language) {
        supportedLanguages = supportedLanguages + setOf(language)
    }

    fun getCurrentRevision(): String {
        return GitBranchUtil.getCurrentRepository(myProject)?.currentRevision ?: ""
    }

    fun initListener() {
        myConnection = myProject.messageBus.connect()
        myConnection.subscribe(BreakpointManagerListener.topic, object : BreakpointManagerListener {

            override fun tagsSaved(psiFile: PsiFile, editors: List<Editor>, breakpoint: XLineBreakpoint<*>) {
                val tagsProperties = TagsProperties.getInstance(myProject)
                if (tagsProperties.state.tagsEnabled) {
                    BreakpointManagerInlayUtil.removeLineInlay(myProject, breakpoint)
                    val document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile.containingFile) ?: return
                    val line = breakpoint.line
                    val offset = try {
                        document.getLineEndOffset(line)
                    } catch (e: IndexOutOfBoundsException) {
                        return
                    }

                    breakpoint.sourcePosition?.let { sourcePosition ->
                        tagsProperties.findState(sourcePosition).let { state ->
                            for (tag in state.tags) {
                                editors.forEach { editor ->
                                    BreakpointManagerInlayUtil.createLineInlay(
                                        offset,
                                        editor,
                                        tag.createPrettyText(),
                                        0,
                                        breakpoint
                                    )
                                }
                            }
                        }
                    }
                }
            }

            override fun breakpointRemoved(breakpoint: XLineBreakpoint<*>) {
                BreakpointManagerInlayUtil.removeLineInlay(myProject, breakpoint)

            }

            override fun tagsDisabled() {
                BreakpointManagerInlayUtil.removeLineInlays(myProject)
            }

            override fun tagsEnabled() {
                val fileEditorManager = FileEditorManager.getInstance(myProject)
                for (openFile in FileEditorManager.getInstance(myProject).openFiles) {
                    editorOpened(fileEditorManager, openFile)
                }
            }

            override fun tagRemoved(tag: BreakpointTag) {
                tagsDisabled()
                tagsEnabled()
            }

            override fun editorOpened(source: FileEditorManager, file: VirtualFile) {
                val tagsProperties = TagsProperties.getInstance(myProject)
                if (tagsProperties.state.tagsEnabled) {
                    val psiFile = PsiManager.getInstance(myProject).findFile(file) ?: return
                    for (breakpointRef in tagsProperties.findStateForFile(file)) {
                        tagsSaved(psiFile, listOf(source.selectedTextEditor ?: continue), findBreakpointAtLine(file, breakpointRef.line) ?: continue)
                    }
                }
            }

            override fun editorChanged(event: FileEditorManagerEvent) {
                val source = event.manager
                val newFile = event.newFile ?: return
                editorOpened(source, newFile)
            }
        })
    }

    fun findBreakpointAtLine(virtualFile: VirtualFile, line: Int): XLineBreakpoint<*>? {
        val breakpointManager = XDebuggerManager.getInstance(myProject).breakpointManager
        for (lineBreakpointType in XDebuggerUtil.getInstance().lineBreakpointTypes) {
            breakpointManager.findBreakpointAtLine(lineBreakpointType, virtualFile, line)?.let {
                return it
            }
        }
        return null
    }

    fun isLanguageSupported(language: Language): Boolean {
        return supportedLanguages.contains(language)
    }

    fun getCurrentBranchName(): String? {
        return GitBranchUtil.getCurrentRepository(myProject)?.currentBranchName
    }

    fun tryOpenFileForBreakpoint(breakpoint: XBreakpoint<*>) {
        val sourcePosition = breakpoint.sourcePosition ?: return
        val file = sourcePosition.file
        val fileEditorManager = FileEditorManager.getInstance(myProject)
        val fileEditors = fileEditorManager.openFile(file, true)
        for (fileEditor in fileEditors) {
            if (fileEditor is PsiAwareTextEditorImpl) {
                val scrollingModel = fileEditor.editor.scrollingModel
                val logicalPosition = LogicalPosition(sourcePosition.line, 0)
                scrollingModel.scrollTo(logicalPosition, ScrollType.MAKE_VISIBLE)
            }
        }
    }

    companion object {

        fun getInstance(project: Project): BreakpointManagerService {
            return project.getService(BreakpointManagerService::class.java)
        }
    }
}