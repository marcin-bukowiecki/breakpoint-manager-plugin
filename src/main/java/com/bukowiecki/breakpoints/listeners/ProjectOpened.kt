/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.listeners

import com.bukowiecki.breakpoints.service.BreakpointManagerService
import com.bukowiecki.breakpoints.tagging.TagsProperties
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl

/**
 * @author Marcin Bukowiecki
 */
class ProjectOpened : StartupActivity {

    @Suppress("UnstableApiUsage")
    override fun runActivity(project: Project) {
        val simpleConnection = project.messageBus.simpleConnect()
        val publisher = project.messageBus.syncPublisher(BreakpointManagerListener.topic)
        val breakpointManagerService = BreakpointManagerService.getInstance(project)
        breakpointManagerService.initListener()

        simpleConnection.subscribe(
            XBreakpointListener.TOPIC, object : XBreakpointListener<XBreakpoint<*>> {

                override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
                    if (breakpoint is XLineBreakpointImpl<*>) {
                        TagsProperties.getInstance(project).removeState(breakpoint)
                        publisher.breakpointRemoved(breakpoint)
                    }
                }

                override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
                    if (breakpoint is XLineBreakpointImpl<*>) {
                        breakpoint.sourcePosition?.let { TagsProperties.getInstance(project).saveState(it, emptyList()) }
                        publisher.breakpointAdded(breakpoint)
                    }
                }
            }
        )

        publisher.tagsEnabled() //add on load

        simpleConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object :
            FileEditorManagerListener {

            override fun fileClosed(source: FileEditorManager, file: VirtualFile) {
                publisher.editorClosed(source, file)
            }

            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                publisher.editorOpened(source, file)
            }

            override fun selectionChanged(event: FileEditorManagerEvent) {
                publisher.editorChanged(event)
            }
        })
    }
}