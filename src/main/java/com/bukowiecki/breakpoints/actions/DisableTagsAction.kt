/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.actions

import com.bukowiecki.breakpoints.listeners.BreakpointManagerListener
import com.bukowiecki.breakpoints.tagging.TagsProperties
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * @author Marcin Bukowiecki
 */
class DisableTagsAction : BreakpointManagerBaseAction() {

    override fun update(e: AnActionEvent) {
        val project = e.project ?: kotlin.run {
            e.presentation.isVisible = false
            return
        }

        if (!TagsProperties.getInstance(project).state.tagsEnabled) {
            e.presentation.isVisible = false
            return
        }

        e.presentation.isVisible = true
        super.update(e)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        TagsProperties.getInstance(project).state.tagsEnabled = false

        project
            .messageBus
            .syncPublisher(BreakpointManagerListener.topic)
            .tagsDisabled()
    }
}