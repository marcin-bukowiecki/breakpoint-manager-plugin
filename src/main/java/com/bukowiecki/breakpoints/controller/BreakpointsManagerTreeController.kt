/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.controller

import com.bukowiecki.breakpoints.service.BreakpointManagerService
import com.bukowiecki.breakpoints.service.ImportableBreakpointFactory
import com.bukowiecki.breakpoints.ui.BreakpointsTreeController
import com.intellij.openapi.project.Project
import com.intellij.ui.CheckedTreeNode
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroup
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroupingRule
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointItem
import com.intellij.xdebugger.impl.breakpoints.ui.tree.BreakpointItemNode
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JTree

/**
 * @author Marcin Bukowiecki
 */
open class BreakpointsManagerTreeController(project: Project,
                                            groupingRules: List<XBreakpointGroupingRule<Any, XBreakpointGroup>>?) : BaseBreakpointTreeController(project, groupingRules), BreakpointsTreeController {

    @Volatile
    private var myCheckedBreakpoints: MutableSet<BreakpointItem> = emptySet<BreakpointItem>().toMutableSet()

    var currentSelectedBreakpoint: BreakpointItemNode? = null

    fun selectAll() {
        val root = root as CheckedTreeNode
        for (child in root.children()) {
            for (b in child.children()) {
                if (b is BreakpointItemNode) {
                    val breakpoint = b.breakpointItem.breakpoint as? XBreakpoint<*> ?: continue
                    if (ImportableBreakpointFactory.getInstance(project).isSupported(breakpoint.type.javaClass.canonicalName)) {
                        b.isChecked = true
                    }
                }
            }
        }
        refresh()
    }

    override fun rebuildTree(items: MutableCollection<out BreakpointItem>) {
        super.rebuildTree(items)
        val old = myCheckedBreakpoints
        setCheckedBreakpoints(items.toMutableSet())
        val root = root as CheckedTreeNode
        for (child in root.children()) {
            breakpointsLoop@
            for (b in child.children()) {
                if (b is BreakpointItemNode) {
                    b.isChecked = old.contains(b.breakpointItem ?: continue)
                    if (!b.isChecked) {
                        myCheckedBreakpoints.remove(b.breakpointItem)
                    }
                }
            }
        }
    }

    fun uncheckAll() {
        val root = root as CheckedTreeNode
        for (child in root.children()) {
            breakpointsLoop@
            for (b in child.children()) {
                if (b is BreakpointItemNode) {
                    b.isChecked = false
                }
            }
        }
        setCheckedBreakpoints(emptySet<BreakpointItem>().toMutableSet())
    }

    public override fun nodeStateDidChangeImpl(node: CheckedTreeNode) {
        if (node is BreakpointItemNode) {
            val breakpointItem = node.breakpointItem
            if (node.isChecked) {
                myCheckedBreakpoints.add(breakpointItem)
            } else {
                myCheckedBreakpoints.remove(breakpointItem)
            }
        }
    }

    override fun getCheckedBreakpoints(): Set<BreakpointItem> {
        return myCheckedBreakpoints.toSet()
    }

    override fun setTreeView(treeView: JTree) {
        super.setTreeView(treeView)
        treeView.selectionModel.addTreeSelectionListener { event ->
            currentSelectedBreakpoint = event.path.lastPathComponent as? BreakpointItemNode
        }
        treeView.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val path = treeView.lastSelectedPathComponent
                    if (path is BreakpointItemNode) {
                        (path.breakpointItem.breakpoint as? XBreakpoint<*>)?.let {
                            BreakpointManagerService.getInstance(project).tryOpenFileForBreakpoint(it)
                        }
                    }
                }
            }

            override fun mousePressed(e: MouseEvent?) { }

            override fun mouseReleased(e: MouseEvent?) { }

            override fun mouseEntered(e: MouseEvent?) { }

            override fun mouseExited(e: MouseEvent?) { }
        })
    }

    private fun setCheckedBreakpoints(checkedBreakpoints: MutableSet<BreakpointItem>) {
        this.myCheckedBreakpoints = checkedBreakpoints
    }
}