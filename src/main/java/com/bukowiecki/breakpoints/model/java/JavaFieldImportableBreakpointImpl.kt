/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model.java

import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.intellij.xdebugger.impl.breakpoints.LineBreakpointState
import org.jetbrains.java.debugger.breakpoints.properties.JavaFieldBreakpointProperties

/**
 * @author Marcin Bukowiecki
 */
class JavaFieldImportableBreakpointImpl(
    myProject: ProjectWrapper,
    typeID: String,
    state: LineBreakpointState<*>,
    properties: JavaFieldBreakpointProperties,
    breakpointMetadata: BreakpointMetadata
) : JavaLineImportableBreakpointImpl(myProject, typeID, state, properties, breakpointMetadata)