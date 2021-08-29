/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.controller

import com.bukowiecki.breakpoints.ui.BreakpointsTreeController
import com.intellij.openapi.project.Project
import com.intellij.ui.CheckedTreeNode
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroup
import com.intellij.xdebugger.breakpoints.ui.XBreakpointGroupingRule
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointItem
import com.intellij.xdebugger.impl.breakpoints.ui.tree.BreakpointItemNode

/**
 * @author Marcin Bukowiecki
 */
open class ExportBreakpointTreeController(project: Project, groupingRules: List<XBreakpointGroupingRule<Any, XBreakpointGroup>>?) :
    BaseBreakpointTreeController(project, groupingRules), BreakpointsTreeController {

    private lateinit var myCheckedBreakpoints: MutableSet<BreakpointItem>

    fun initStartingItems(myBreakpointItems: ArrayList<BreakpointItem>) {
        myCheckedBreakpoints = myBreakpointItems.toMutableSet()
        deselectAll()
    }

    override fun nodeStateDidChangeImpl(node: CheckedTreeNode) {
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
}