/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.ui

import com.bukowiecki.breakpoints.controller.ExportBreakpointTreeController
import com.bukowiecki.breakpoints.controller.ExportController
import com.bukowiecki.breakpoints.utils.BreakpointManagerBundle
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.popup.util.DetailController
import com.intellij.ui.popup.util.DetailViewImpl
import com.intellij.ui.popup.util.ItemWrapper
import com.intellij.ui.popup.util.MasterController
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import com.intellij.xdebugger.XDebuggerBundle
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointItem
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointPanelProvider
import com.intellij.xdebugger.impl.breakpoints.ui.grouping.XBreakpointGroupingByTypeRule
import com.intellij.xdebugger.impl.breakpoints.ui.tree.BreakpointsCheckboxTree
import com.intellij.xdebugger.impl.breakpoints.ui.tree.BreakpointsGroupNode
import java.awt.BorderLayout
import java.util.function.Consumer
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
class ExportBreakpointsDialog(private val myProject: Project,
                              private val myBreakpointsPanelProviders: List<BreakpointPanelProvider<*>>) : BaseBreakpointsDialog(myProject), ExportSupport {

    private lateinit var myTreeController: ExportBreakpointTreeController

    private val myListenerDisposable = Disposer.newDisposable()
    private val myExportController = ExportController(this)
    private val temp = JLabel()
    private val myMasterController: MasterController = object : MasterController {
        override fun getSelectedItems(): Array<ItemWrapper> {
            val res = myTreeController.getSelectedBreakpoints(false)
            return res.toTypedArray()
        }

        override fun getPathLabel(): JLabel {
            return temp
        }
    }
    private val myDetailController = DetailController(myMasterController)

    init {
        collectItems()
        title = BreakpointManagerBundle.message("breakpoints.export.title")
        setOKButtonText(BreakpointManagerBundle.message("breakpoints.export"))
        init()
    }

    private fun updateBreakpoints() {
        collectItems()
        myTreeController.rebuildTree(myBreakpointItems)
        myDetailController.doUpdateDetailView(true)
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        val splitPane = JBSplitter(0.3f)
        splitPane.splitterProportionKey = "$dimensionServiceKey.splitter"

        splitPane.firstComponent = createMasterView()
        splitPane.secondComponent = createDetailView()

        val exportPanel = createExportPanel()

        mainPanel.add(splitPane, BorderLayout.CENTER)
        mainPanel.add(exportPanel, BorderLayout.SOUTH)

        return mainPanel
    }

    private fun createDetailView(): JComponent {
        val detailView = DetailViewImpl(myProject)
        detailView.setEmptyLabel(XDebuggerBundle.message("xbreakpoint.label.empty"))
        myDetailController.setDetailView(detailView)
        return detailView
    }

    private fun createMasterView(): JComponent {
        myTreeController = object : ExportBreakpointTreeController(myProject, emptyList()) {

            override fun selectionChangedImpl() {
                super.selectionChangedImpl()
                //saveCurrentItem()
                myDetailController.updateDetailView()
            }
        }
        val tree: BreakpointsCheckboxTree = object : BreakpointsCheckboxTree(myProject, myTreeController) {
            override fun onDoubleClick(node: CheckedTreeNode) {
                if (node is BreakpointsGroupNode<*>) {
                    val path = TreeUtil.getPathFromRoot(node)
                    if (isExpanded(path)) {
                        collapsePath(path)
                    } else {
                        expandPath(path)
                    }
                } else {
                    navigate(false)
                }
            }
        }
        tree.isHorizontalAutoScrollingEnabled = false

        val decorator = ToolbarDecorator.createDecorator(tree)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())

        val panel = decorator.createPanel()
        myTreeController.treeView = tree
        myTreeController.setGroupingRules(listOf(XBreakpointGroupingByTypeRule<XBreakpoint<XBreakpointProperties<*>>>()))
        myTreeController.rebuildTree(myBreakpointItems)
        myTreeController.initStartingItems(myBreakpointItems)
        panel.add(tree)
        expandGroups(tree, tree.rowCount)

        myBreakpointsPanelProviders.forEach(Consumer { provider: BreakpointPanelProvider<*> ->
            provider.addListener(
                { updateBreakpoints() }, myProject, myListenerDisposable
            )
        })

        return panel
    }

    private fun navigate(requestFocus: Boolean) {
        myTreeController.getSelectedBreakpoints(false).stream().findFirst().ifPresent { b: BreakpointItem ->
            b.navigate(
                requestFocus
            )
        }
    }

    override fun getDimensionServiceKey(): String? {
        return javaClass.name
    }

    override fun doOKAction() {
        if (myExportController.validate()) {
            ProgressManager.getInstance().run(object : Task.Backgroundable(myProject, BreakpointManagerBundle.message("breakpoints.export.task.title"), false) {

                override fun run(indicator: ProgressIndicator) {
                    myExportController.export(indicator)
                }
            })
            super.doOKAction()
        }
    }

    override fun getTreeController() = myTreeController

    override fun getOutputFileName(): String = myOutputFileNameTextField.text

    override fun getOutputFilePath(): String = myOutputFilePathTextField.text
}