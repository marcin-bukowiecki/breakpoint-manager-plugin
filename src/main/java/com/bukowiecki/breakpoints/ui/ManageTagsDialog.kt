/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.ui

import com.bukowiecki.breakpoints.controller.TagsController
import com.bukowiecki.breakpoints.tagging.TagsProperties
import com.bukowiecki.breakpoints.utils.BreakpointManagerBundle
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiFile
import com.intellij.ui.JBSplitter
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
class ManageTagsDialog(val breakpoint: XLineBreakpoint<*>,
                       val editor: Editor,
                       val psiFile: PsiFile,
                       val project: Project,
                       private val saveCallback: () -> Unit = {}) : DialogWrapper(project) {

    private val myTags = mutableListOf<TagRowPanel>()

    private val myController = TagsController(this)

    init {
        val instance = TagsProperties.getInstance(project)
        breakpoint.sourcePosition?.let { sourcePosition ->
            instance.findState(sourcePosition).let {
                it.tags.forEachIndexed { i, tag ->
                    val tagPanel = if (i == 0) {
                        TagRowPanel(3, myController)
                    } else {
                        TagRowPanel(2, myController)
                    }
                    tagPanel.setKey(tag.key)
                    tagPanel.setValue(tag.value)
                    myTags.add(tagPanel)
                }
            }
        }

        super.init()
        setOKButtonText(BreakpointManagerBundle.message("breakpoints.tags.manage.save"))
        title = BreakpointManagerBundle.message("breakpoints.tags.manage.title")

        myController.validate()
    }

    override fun doOKAction() {
        if (myController.validate()) {
            super.doOKAction()
            myController.save()
            saveCallback.invoke()
        }
    }

    fun getTags() = myTags

    override fun createCenterPanel(): JComponent {
        val mainPanel = JBUI.Panels.simplePanel()

        val splitPane = JBSplitter(true, 0.9f)

        val footerPanel = JPanel(GridLayoutManager(1, 1))
        val addTagButton = JButton("Add Tag")
        val gridConstraints = GridConstraints()
        gridConstraints.row = 0
        gridConstraints.column = 0
        gridConstraints.anchor = GridConstraints.ANCHOR_CENTER

        footerPanel.add(addTagButton, gridConstraints, -1)
        addTagButton.addActionListener {
            addRow(splitPane)
            buildTagsPanel(splitPane)
        }

        if (getTags().isEmpty()) {
            addRow(splitPane) //first row
        }

        buildTagsPanel(splitPane)
        splitPane.secondComponent = footerPanel

        mainPanel.add(splitPane, BorderLayout.CENTER)

        return mainPanel
    }

    private fun addRow(splitPane: JBSplitter) {
        val rowPanel = if (getTags().size == 0) {
            TagRowPanel(3, myController)
        } else {
            TagRowPanel(2, myController)
        }
        myTags.add(rowPanel)
        rowPanel.getRemoveButton().addActionListener {
            if (myTags.isNotEmpty()) {
                if (myTags.first() != rowPanel) {
                    myTags.remove(rowPanel)
                }
                buildTagsPanel(splitPane)
            }
        }
    }

    private fun buildTagsPanel(splitPane: JBSplitter) {
        val tagRowsPanel = JPanel(GridLayoutManager(myTags.size, 1))

        for ((i, tag) in myTags.withIndex()) {
            val gridConstraints = GridConstraints()
            gridConstraints.row = i
            gridConstraints.column = 0
            gridConstraints.anchor = GridConstraints.ANCHOR_NORTH

            tagRowsPanel.add(
                tag,
                gridConstraints,
                -1
            )
        }

        splitPane.firstComponent = tagRowsPanel
        splitPane.validate()
        splitPane.repaint()
    }

    override fun getDimensionServiceKey(): String? {
        return javaClass.name
    }

    fun enableSave() {
        okAction.isEnabled = true
    }

    fun disableSave() {
        okAction.isEnabled = false
    }
}