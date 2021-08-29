/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.ui

import com.bukowiecki.breakpoints.utils.BreakpointManagerBundle
import com.bukowiecki.breakpoints.utils.BreakpointManagerUtils
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ReflectionUtil
import com.intellij.util.ui.JBUI
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointItem
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointPanelProvider
import java.awt.BorderLayout
import java.util.function.Consumer
import javax.swing.*

/**
 * @author Marcin Bukowiecki
 */
abstract class BaseBreakpointsDialog(private val myProject: Project) : DialogWrapper(myProject) {

    private val log = Logger.getInstance(BaseBreakpointsDialog::class.java)

    protected lateinit var myOutputFilePathTextField: JTextField
    protected lateinit var myOutputFileNameTextField: JTextField

    private lateinit var myChooserButton: TextFieldWithBrowseButton

    private val myBreakpointsPanelProviders: List<BreakpointPanelProvider<Any>> = XBreakpointUtil.collectPanelProviders()

    protected val myBreakpointItems: ArrayList<BreakpointItem> = ArrayList()

    override fun dispose() {
        disposeItems()
        super.dispose()
    }

    open fun createExportPanel(): JPanel {
        val exportPanel = JBUI.Panels.simplePanel()
        exportPanel.border = BorderFactory.createEmptyBorder(50, 0, 0, 0)

        val outputFileNameLabel = JLabel(BreakpointManagerBundle.message("breakpoints.export.label.outputFile"))
        this.myOutputFileNameTextField = JTextField()
        val namePanel = JBUI.Panels.simplePanel()
            .addToTop(outputFileNameLabel)
            .addToCenter(this.myOutputFileNameTextField)
        exportPanel.add(namePanel, BorderLayout.NORTH)

        val outputFilePathLabel = JLabel(BreakpointManagerBundle.message("breakpoints.export.label.outputPath"))
        this.myOutputFilePathTextField = JTextField()
        this.myChooserButton = TextFieldWithBrowseButton(this.myOutputFilePathTextField)
        val pathPanel = JBUI.Panels.simplePanel()
            .addToTop(outputFilePathLabel)
            .addToCenter(this.myChooserButton)
        val folderDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        this.myChooserButton.addBrowseFolderListener(object : TextBrowseFolderListener(folderDescriptor) {

        })
        exportPanel.add(pathPanel, BorderLayout.CENTER)

        myOutputFileNameTextField.text = BreakpointManagerUtils.defaultFileName
        myChooserButton.text = myProject.basePath ?: ""

        return exportPanel
    }

    fun getProject(): Project = myProject

    protected fun collectItems() {
        if (myBreakpointsPanelProviders.isNotEmpty()) {
            disposeItems()
            myBreakpointItems.clear()
            for (panelProvider in myBreakpointsPanelProviders) {
                panelProvider.provideBreakpointItems(myProject, myBreakpointItems)
            }
        }
    }

    protected fun expandGroups(tree: JTree, rowCount: Int) {
        for (i in 0 until rowCount) {
            tree.expandRow(i)
        }
    }

    private fun disposeItems() {
        myBreakpointItems.forEach(Consumer {
                obj: BreakpointItem ->
            try {
                ReflectionUtil.getDeclaredMethod(BreakpointItem::class.java, "dispose")?.let { declaredMethod ->
                    declaredMethod.isAccessible = true
                    declaredMethod.invoke(obj)
                }
            } catch (e: Exception) {
                log.info("Couldn't dispose breakpoint item: " + e.message)
            }
        })
    }
}