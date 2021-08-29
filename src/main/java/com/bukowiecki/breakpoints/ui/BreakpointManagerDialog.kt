/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.ui

import com.bukowiecki.breakpoints.controller.BreakpointsManagerTreeController
import com.bukowiecki.breakpoints.controller.ExportController
import com.bukowiecki.breakpoints.listeners.BreakpointManagerListener
import com.bukowiecki.breakpoints.model.TagWrapper
import com.bukowiecki.breakpoints.model.TagsTableModel
import com.bukowiecki.breakpoints.tagging.BreakpointTag
import com.bukowiecki.breakpoints.tagging.TagsProperties
import com.bukowiecki.breakpoints.utils.BreakpointManagerBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.*
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.impl.breakpoints.ui.grouping.XBreakpointGroupingByTypeRule
import com.intellij.xdebugger.impl.breakpoints.ui.tree.BreakpointItemNode
import com.intellij.xdebugger.impl.breakpoints.ui.tree.BreakpointsCheckboxTree
import com.intellij.xdebugger.impl.breakpoints.ui.tree.BreakpointsGroupNode
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * @author Marcin Bukowiecki
 */
class BreakpointManagerDialog(private val myProject: Project) : BaseBreakpointsDialog(myProject), ExportSupport {

    private lateinit var myToolbarGroup: DefaultActionGroup
    private lateinit var myTreeController: BreakpointsManagerTreeController
    private lateinit var myToolbar: ActionToolbar
    private lateinit var myTree: BreakpointsCheckboxTree
    private lateinit var myTable: JBTable
    private lateinit var myStatusLabel: JLabel

    private val myExportController = ExportController(this)
    private val myConnection = myProject.messageBus.connect()

    init {
        isModal = false
        collectItems()
        title = BreakpointManagerBundle.message("breakpoints.manager.title")
        init()
        addActions()

        myConnection.subscribe(BreakpointManagerListener.topic, object : BreakpointManagerListener {

            override fun tagRemoved(tag: BreakpointTag) {
                refreshTable()
            }

            override fun tagsSaved(psiFile: PsiFile, editors: List<Editor>, breakpoint: XLineBreakpoint<*>) {
                tagSelectionChanged()
            }

            override fun breakpointRemoved(breakpoint: XLineBreakpoint<*>) {
                refreshTree()
                refreshTable()
                refreshStatusLabel()
            }

            override fun breakpointStateChanged() {
                refreshTree()
            }

            override fun tagSelectionChanged() {
                val checkedTags = (myTable.model as TagsTableModel).getSelectedTags()
                val tagsProperties = TagsProperties.getInstance(myProject)
                val root = myTree.model.root as CheckedTreeNode

                for (child in root.children()) {
                    breakpointsLoop@
                    for (b in child.children()) {
                        if (b is BreakpointItemNode) {
                            val breakpoint = b.breakpointItem.breakpoint as? XLineBreakpoint<*> ?: continue
                            val tags = tagsProperties.findState(breakpoint.sourcePosition ?: continue).tagsAsSet()
                            for (checkedTag in checkedTags) {
                                b.isChecked = tags.contains(checkedTag.getBreakpointTag())
                                if (b.isChecked) {
                                    break@breakpointsLoop
                                }
                            }
                            b.isChecked = false
                        }
                    }
                }

                myTreeController.refresh()
                repaintTreeAndTable()
                refreshStatusLabel()
            }
        })
    }

    fun currentSelectedBreakpoint() = myTreeController.currentSelectedBreakpoint

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        val splitPane = JBSplitter(0.3f)
        splitPane.splitterProportionKey = "$dimensionServiceKey.splitter"

        splitPane.firstComponent = createMasterView()
        splitPane.secondComponent = createDetailView()

        mainPanel.add(splitPane, BorderLayout.CENTER)

        val exportPanel = createExportPanel()
        mainPanel.add(exportPanel, BorderLayout.SOUTH)

        return mainPanel
    }

    override fun getDimensionServiceKey(): String? {
        return javaClass.name
    }

    fun selectedTags(): List<TagWrapper> {
        val tagsTableModel = myTable.model as TagsTableModel
        return tagsTableModel.getSelectedTags()
    }

    private fun repaintTreeAndTable() {
        myTree.validate()
        myTree.repaint()
        myTable.validate()
        myTable.repaint()
    }

    override fun createExportPanel(): JPanel {
        val exportPanel = super.createExportPanel()
        val exportButton = JButton(BreakpointManagerBundle.message("breakpoints.export"))
        exportButton.addActionListener {
            if (myExportController.validate()) {
                ProgressManager.getInstance().run(object : Task.Backgroundable(myProject, BreakpointManagerBundle.message("breakpoints.export.task.title"), false) {

                    @Suppress("UnstableApiUsage")
                    override fun run(indicator: ProgressIndicator) {
                        val exported = myExportController.export(indicator)
                        ApplicationManager.getApplication().invokeLaterOnWriteThread {
                            Messages.showInfoMessage(
                                BreakpointManagerBundle.message("breakpoints.export.success.message", exported),
                                BreakpointManagerBundle.message("breakpoints.export.title")
                            )
                        }
                    }
                })
            }
        }
        val buttonPanel = JPanel()
        buttonPanel.add(exportButton, BorderLayout.WEST)
        exportPanel.add(buttonPanel, BorderLayout.SOUTH)

        return exportPanel
    }

    private fun createMasterView(): JComponent {
        myTreeController = object : BreakpointsManagerTreeController(myProject, emptyList()) {

        }
        myTree = object : BreakpointsCheckboxTree(myProject, myTreeController) {
            override fun onDoubleClick(node: CheckedTreeNode) {
                if (node is BreakpointsGroupNode<*>) {
                    val path = TreeUtil.getPathFromRoot(node)
                    if (isExpanded(path)) {
                        collapsePath(path)
                    } else {
                        expandPath(path)
                    }
                }
            }

            override fun onNodeStateChanged(node: CheckedTreeNode?) {
                super.onNodeStateChanged(node)
                refreshStatusLabel()
            }
        }
        myTree.isHorizontalAutoScrollingEnabled = false
        val decorator = ToolbarDecorator.createDecorator(myTree)
            .setToolbarPosition(ActionToolbarPosition.TOP)
            .setPanelBorder(JBUI.Borders.empty())

        val panel = decorator.createPanel()
        myTreeController.treeView = myTree
        myTreeController.setGroupingRules(listOf(XBreakpointGroupingByTypeRule<XBreakpoint<XBreakpointProperties<*>>>()))
        myTreeController.rebuildTree(myBreakpointItems)
        myTreeController.uncheckAll()
        expandGroups(myTree, myTree.rowCount)
        panel.add(myTree)

        return panel
    }

    private fun createDetailView(): JComponent {
        val mainPanel = JBUI.Panels.simplePanel()

        this.myToolbarGroup = DefaultActionGroup()
        this.myToolbarGroup.add(MuteBreakpointsAction(myTreeController))
        this.myToolbarGroup.add(UnMuteBreakpointsAction(myTreeController))
        this.myToolbarGroup.add(RemoveBreakpointsAction(myTreeController))
        this.myToolbarGroup.add(RemoveTagsAction(this))
        this.myToolbarGroup.add(RefreshAction(this))

        this.myToolbar = ActionManager
            .getInstance()
            .createActionToolbar("BreakpointManager", myToolbarGroup, true)
        this.myToolbar.setTargetComponent(mainPanel)
        val toolbarPanel = JBUI.Panels.simplePanel()
        toolbarPanel.add(JLabel(BreakpointManagerBundle.message("breakpoints.manager.actions.label")), BorderLayout.NORTH)
        toolbarPanel.add(this.myToolbar.component, BorderLayout.CENTER)
        this.myStatusLabel = JLabel(BreakpointManagerBundle.message("breakpoints.manager.status", 0))
        toolbarPanel.add(this.myStatusLabel, BorderLayout.SOUTH)
        mainPanel.add(toolbarPanel, BorderLayout.NORTH)

        val tags = collectAllTags().map { TagWrapper(it) }
        val tableModel = TagsTableModel(myProject, tags)

        this.myTable = TagsTable(tableModel)

        val scrollPane = ScrollPaneFactory.createScrollPane(this.myTable)
        scrollPane.minimumSize = Dimension(-1, 100)
        mainPanel.add(scrollPane, BorderLayout.CENTER)

        val footerPanel = JPanel(BorderLayout())

        val tagsCheckBox = JBCheckBox(BreakpointManagerBundle.message("breakpoints.tags.selectAll"))
        tagsCheckBox.addActionListener {
            if (tagsCheckBox.isSelected) {
                this.myTable.model = TagsTableModel(myProject, collectAllTags().map { TagWrapper(it, true) })
            } else {
                this.myTable.model = TagsTableModel(myProject, collectAllTags().map { TagWrapper(it, false) })
            }
            repaintTreeAndTable()
            myProject.messageBus.syncPublisher(BreakpointManagerListener.topic).tagSelectionChanged()
        }
        footerPanel.add(tagsCheckBox, BorderLayout.NORTH)

        val breakpointsCheckBox = JBCheckBox(BreakpointManagerBundle.message("breakpoints.breakpoints.selectAll"))
        breakpointsCheckBox.addActionListener {
            if (breakpointsCheckBox.isSelected) {
                myTreeController.selectAll()
            } else {
                myTreeController.deselectAll()
            }
            repaintTreeAndTable()
            refreshTable()
            refreshStatusLabel()
        }
        footerPanel.add(breakpointsCheckBox, BorderLayout.CENTER)

        mainPanel.add(footerPanel, BorderLayout.SOUTH)

        refreshStatusLabel()

        return mainPanel
    }

    fun refreshStatusLabel() {
        val checkedBreakpoints = myTreeController.getCheckedBreakpoints()
        this.myStatusLabel.text = BreakpointManagerBundle.message("breakpoints.manager.status", checkedBreakpoints.size)
    }

    fun refreshTree() {
        collectItems()
        myTreeController.rebuildTree(myBreakpointItems)
    }

    fun refreshTable() {
        val tags = collectAllTags().map { TagWrapper(it) }
        val tableModel = TagsTableModel(myProject, tags)
        this.myTable.model = tableModel
    }

    fun getPlace() = "BreakpointManager"

    private fun collectAllTags(): List<BreakpointTag> {
        val tagsProperties = TagsProperties.getInstance(myProject)

        return XDebuggerManager.getInstance(myProject).breakpointManager.allBreakpoints.mapNotNull {
            it as? XLineBreakpoint<*>
        }.mapNotNull {
            it.sourcePosition
        }.map {
            tagsProperties.findState(it)
        }.flatMap { it.tags }
    }

    private fun addActions() {
        val actionManager = ActionManager.getInstance()
        val tagTableGroup = actionManager.getAction("Bukowiecki.BreakpointManager.TagsTable") as? DefaultActionGroup ?: return
        val treeGroup = actionManager.getAction("Bukowiecki.BreakpointManager.Tree") as? DefaultActionGroup ?: return

        val editTagAction = EditTagAction(this)
        tagTableGroup.add(editTagAction)

        myTable.addMouseListener(
            object : PopupHandler() {

                override fun invokePopup(comp: Component?, x: Int, y: Int) {
                    actionManager.createActionPopupMenu(getPlace(), tagTableGroup).component.show(comp, x, y)
                }
            }
        )

        val manageBreakpointTags = ManageBreakpointTagsAction(this)
        treeGroup.add(manageBreakpointTags)

        myTree.addMouseListener(
            object : PopupHandler() {

                override fun invokePopup(comp: Component?, x: Int, y: Int) {
                    actionManager.createActionPopupMenu(getPlace(), treeGroup).component.show(comp, x, y)
                }
            }
        )
    }

    override fun dispose() {
        val actionManager = ActionManager.getInstance()
        (actionManager.getAction("Bukowiecki.BreakpointManager.TagsTable") as? DefaultActionGroup)?.removeAll()
        (actionManager.getAction("Bukowiecki.BreakpointManager.Tree") as? DefaultActionGroup)?.removeAll()
        super.dispose()
    }

    override fun getOutputFilePath(): String {
        return myOutputFilePathTextField.text
    }

    override fun getOutputFileName(): String {
        return myOutputFileNameTextField.text
    }

    override fun getTreeController(): BreakpointsTreeController {
        return myTreeController
    }
}

/**
 * @author Marcin Bukowiecki
 */
class MuteBreakpointsAction(private val controller: BreakpointsManagerTreeController) : AnAction() {

    init {
        templatePresentation.icon = AllIcons.Debugger.MuteBreakpoints
        templatePresentation.text = BreakpointManagerBundle.message("breakpoints.manager.breakpoint.mute.action")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val checkedBreakpoints = controller.getCheckedBreakpoints()
        for (checkedBreakpoint in checkedBreakpoints) {
            (checkedBreakpoint.breakpoint as? XBreakpoint<*>)?.let {
                it.isEnabled = false
            }
        }
        controller.project.messageBus.syncPublisher(BreakpointManagerListener.topic).breakpointStateChanged()
    }
}

/**
 * @author Marcin Bukowiecki
 */
class UnMuteBreakpointsAction(private val controller: BreakpointsManagerTreeController) : AnAction() {

    init {
        templatePresentation.icon = AllIcons.Debugger.Db_set_breakpoint
        templatePresentation.text = BreakpointManagerBundle.message("breakpoints.manager.breakpoint.unmute.action")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val checkedBreakpoints = controller.getCheckedBreakpoints()
        for (checkedBreakpoint in checkedBreakpoints) {
            (checkedBreakpoint.breakpoint as? XBreakpoint<*>)?.let {
                it.isEnabled = true
            }
        }
        controller.project.messageBus.syncPublisher(BreakpointManagerListener.topic).breakpointStateChanged()
    }
}

/**
 * @author Marcin Bukowiecki
 */
class RemoveBreakpointsAction(private val controller: BreakpointsManagerTreeController) : AnAction() {

    init {
        templatePresentation.icon = AllIcons.Actions.DeleteTag
        templatePresentation.text = BreakpointManagerBundle.message("breakpoints.manager.breakpoint.remove.action")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val checkedBreakpoints = controller.getCheckedBreakpoints()
        val breakpointManager = XDebuggerManager.getInstance(controller.project).breakpointManager
        for (checkedBreakpoint in checkedBreakpoints) {
            (checkedBreakpoint.breakpoint as? XBreakpoint<*>)?.let {
                ApplicationManager.getApplication().runWriteAction {
                    breakpointManager.removeBreakpoint(it)
                }
            }
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
class RemoveTagsAction(private val dialog: BreakpointManagerDialog) : AnAction() {

    init {
        templatePresentation.icon = AllIcons.Diff.Remove
        templatePresentation.text = BreakpointManagerBundle.message("breakpoints.manager.tags.remove.action")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val selectedTags = dialog.selectedTags()
        val prop = TagsProperties.getInstance(dialog.getProject())
        selectedTags.forEach {
            val breakpointTag = it.getBreakpointTag()
            for (breakpointRef in prop.findStatesByTag(breakpointTag)) {
                breakpointRef.tags = breakpointRef.tags.filter { it != breakpointTag }
                if (breakpointRef.tags.isEmpty()) {
                    prop.removeState(breakpointRef)
                }
            }
            dialog.getProject().messageBus.syncPublisher(BreakpointManagerListener.topic).tagRemoved(breakpointTag)
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
class EditTagAction(private val myDialog: BreakpointManagerDialog) : AnAction() {

    init {
        templatePresentation.text = BreakpointManagerBundle.message("breakpoints.manager.tags.edit.action")
    }

    override fun update(e: AnActionEvent) {
        val selectedTags = myDialog.selectedTags()
        e.presentation.isVisible = selectedTags.size == 1
    }

    override fun actionPerformed(e: AnActionEvent) {
        EditTagDialog(myDialog.getProject(), myDialog.window).show()
    }
}

/**
 * @author Marcin Bukowiecki
 */
class RefreshAction(private val myDialog: BreakpointManagerDialog) : AnAction() {

    init {
        templatePresentation.text = BreakpointManagerBundle.message("breakpoints.refresh")
        templatePresentation.icon = AllIcons.Actions.ForceRefresh
    }

    override fun actionPerformed(e: AnActionEvent) {
        myDialog.refreshTable()
        myDialog.refreshTree()
        myDialog.refreshStatusLabel()
        myDialog.validate()
        myDialog.repaint()
    }
}

/**
 * @author Marcin Bukowiecki
 */
class ManageBreakpointTagsAction(private val myDialog: BreakpointManagerDialog): AnAction() {

    init {
        templatePresentation.text = BreakpointManagerBundle.message("breakpoints.tags.manage.title")
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = myDialog.currentSelectedBreakpoint() != null
    }

    @Suppress("UnstableApiUsage")
    override fun actionPerformed(e: AnActionEvent) {
        val selectedBreakpoint = myDialog.currentSelectedBreakpoint()
        if (selectedBreakpoint != null) {
            (selectedBreakpoint.breakpointItem.breakpoint as? XLineBreakpoint<*>)?.let { breakpoint ->
                val fileUrl = breakpoint.fileUrl
                val virtualFile = VfsUtil.findFileByURL(VfsUtil.convertToURL(fileUrl) ?: return) ?: return
                val psiFile = PsiManager.getInstance(myDialog.getProject()).findFile(virtualFile) ?: return
                val document = PsiDocumentManager.getInstance(myDialog.getProject()).getDocument(psiFile)
                val editors = FileEditorManagerEx.getInstance(myDialog.getProject()).selectedTextEditorWithRemotes

                if (editors.isNotEmpty()) {
                    for (editor in editors) {
                        if (editor.document == document) {
                            ManageTagsDialog(
                                breakpoint,
                                editor,
                                psiFile,
                                myDialog.getProject()
                            ) {
                                myDialog.refreshTable()
                            }.show()
                            break
                        }
                    }
                }
            }
        }
    }
}

