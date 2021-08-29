/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.controller

import com.bukowiecki.breakpoints.listeners.BreakpointManagerListener
import com.bukowiecki.breakpoints.tagging.BreakpointTag
import com.bukowiecki.breakpoints.tagging.TagsProperties
import com.bukowiecki.breakpoints.ui.ManageTagsDialog

/**
 * @author Marcin Bukowiecki
 */
class TagsController(private val tagsDialog: ManageTagsDialog): TagValidatorProvider {

    private val myPublisher =  tagsDialog.project.messageBus.syncPublisher(BreakpointManagerListener.topic)

    override fun validate(): Boolean {
        var isValid = true

        val definedTags = mutableSetOf<String>()
        val tags = tagsDialog.getTags()
        tags.forEach { panel ->
            if (panel.getValueText().isNotEmpty() && panel.getKeyText().isEmpty()) {
                panel.reportKeyError("Expected key name")
                isValid = false
            } else if (panel.getKeyText() != "" && definedTags.contains(panel.getKeyText())) {
                panel.reportKeyError("Keys must be unique")
                isValid = false
            } else {
                panel.clearErrors()
            }

            definedTags.add(panel.getKeyText())
        }

        if (isValid) {
            tagsDialog.enableSave()
        } else {
            tagsDialog.disableSave()
        }

        return isValid
    }

    fun save() {
        val breakpoint = tagsDialog.breakpoint
        val sourcePosition = breakpoint.sourcePosition ?: return
        val tags = tagsDialog.getTags()
        val project = tagsDialog.project
        val instance = TagsProperties.getInstance(project)
        val currentState = instance.findState(sourcePosition)
        val toSave = tags
            .filter { it.getKeyText().isNotEmpty() }
            .map {
                BreakpointTag(it.getKeyText(), it.getValueText())
            }

        currentState.tags = toSave
        myPublisher.tagsSaved(tagsDialog.psiFile, listOf(tagsDialog.editor), breakpoint)
    }
}