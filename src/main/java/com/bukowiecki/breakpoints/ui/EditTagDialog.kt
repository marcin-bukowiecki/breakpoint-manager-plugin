/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.ui

import com.bukowiecki.breakpoints.controller.TagValidatorProvider
import com.bukowiecki.breakpoints.utils.BreakpointManagerBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.JBUI
import java.awt.Component
import javax.swing.JComponent

/**
 * @author Marcin Bukowiecki
 */
class EditTagDialog(val project: Project, parent: Component): DialogWrapper(project, parent, false, IdeModalityType.IDE) {

    init {
        setOKButtonText(BreakpointManagerBundle.message("breakpoints.tags.manage.save"))
        title = BreakpointManagerBundle.message("breakpoints.manager.tag.edit")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JBUI.Panels.simplePanel()

        val innerPanel = TagRowPanel(3, object : TagValidatorProvider {

            override fun validate(): Boolean {
                return true
            }
        }, showRemoveButton = false)

        mainPanel.add(innerPanel)

        return mainPanel
    }
}