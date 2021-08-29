/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.ui

import com.bukowiecki.breakpoints.controller.ImportController
import com.bukowiecki.breakpoints.model.BreakpointsToImportTableModel
import com.bukowiecki.breakpoints.model.ImportTable
import com.bukowiecki.breakpoints.utils.BreakpointManagerBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.JBSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * @author Marcin Bukowiecki
 */
class ImportBreakpointsDialog(private val myProject: Project) : DialogWrapper(myProject) {

    private val myController = ImportController(this)

    private lateinit var myTable: JBTable

    private lateinit var myInputFileTextField: JTextField

    val infoLabel = JBLabel()
    val commitIDLabel = JBLabel()
    val branchNameLabel = JBLabel()

    init {
        super.init()
        setOKButtonText(BreakpointManagerBundle.message("breakpoints.set"))
        title = BreakpointManagerBundle.message("breakpoints.toImport.title")
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        val splitPane = JBSplitter(true, 0.1f)
        splitPane.splitterProportionKey = "$dimensionServiceKey.splitter"

        val inputFilePathLabel = JLabel(BreakpointManagerBundle.message("breakpoints.inputFile"))

        this.myInputFileTextField = JTextField()
        val chooserButton = TextFieldWithBrowseButton(this.myInputFileTextField)
        val pathPanel = JBUI.Panels.simplePanel()
            .withMaximumHeight(50)
            .addToTop(inputFilePathLabel)
            .addToCenter(chooserButton)
        val folderDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("xml")
        chooserButton.addBrowseFolderListener(object : TextBrowseFolderListener(folderDescriptor) {

            override fun actionPerformed(e: ActionEvent?) {
                super.actionPerformed(e)
                if (getInputFilePath().isNotEmpty()) {
                    myController.import()
                }
            }
        })

        val tableModel = BreakpointsToImportTableModel(emptyList())
        myTable = ImportTable(tableModel)

        val scrollPane = ScrollPaneFactory.createScrollPane(myTable)

        splitPane.firstComponent = pathPanel
        splitPane.secondComponent = scrollPane

        mainPanel.add(splitPane, BorderLayout.CENTER)

        val labelsPanel = JPanel(BorderLayout())
        labelsPanel.add(infoLabel, BorderLayout.NORTH)
        labelsPanel.add(commitIDLabel, BorderLayout.CENTER)
        labelsPanel.add(branchNameLabel, BorderLayout.SOUTH)
        mainPanel.add(labelsPanel, BorderLayout.SOUTH)

        mainPanel.minimumSize = Dimension(300, 200)

        return mainPanel
    }

    override fun getDimensionServiceKey(): String? {
        return javaClass.name
    }

    override fun doOKAction() {
        super.doOKAction()
        myController.set()
    }

    override fun getInitialSize(): Dimension {
        return Dimension(400, 200)
    }

    fun getTable() = myTable

    fun getProject() = myProject

    fun getInputFilePath(): String = myInputFileTextField.text
}