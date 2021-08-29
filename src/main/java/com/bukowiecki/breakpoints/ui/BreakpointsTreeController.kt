/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.ui

import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointItem

/**
 * @author Marcin Bukowiecki
 */
interface BreakpointsTreeController {
    fun getCheckedBreakpoints(): Set<BreakpointItem>
}