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
import com.intellij.xdebugger.impl.breakpoints.ui.tree.BreakpointItemNode
import com.intellij.xdebugger.impl.breakpoints.ui.tree.BreakpointItemsTreeController

/**
 * @author Marcin Bukowiecki
 */
abstract class BaseBreakpointTreeController(val project: Project,
                                   groupingRules: List<XBreakpointGroupingRule<Any, XBreakpointGroup>>?) : BreakpointItemsTreeController(groupingRules), BreakpointsTreeController {

    fun deselectAll() {
        val root = root as CheckedTreeNode
        for (child in root.children()) {
            for (b in child.children()) {
                if (b is BreakpointItemNode) {
                    b.isChecked = false
                }
            }
        }
        refresh()
    }

    fun refresh() {
        for (child in root.children()) {
            for (b in child.children()) {
                if (b is BreakpointItemNode) {
                    nodeStateDidChangeImpl(b)
                }
            }
        }
    }
}