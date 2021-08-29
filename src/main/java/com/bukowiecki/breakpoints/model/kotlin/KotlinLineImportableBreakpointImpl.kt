/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model.kotlin

import com.bukowiecki.breakpoints.model.ImportableBreakpointImpl
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.intellij.xdebugger.impl.breakpoints.LineBreakpointState
import org.jetbrains.java.debugger.breakpoints.properties.JavaBreakpointProperties

/**
 * @author Marcin Bukowiecki
 */
open class KotlinLineImportableBreakpointImpl(
    project: ProjectWrapper,
    typeID: String,
    state: LineBreakpointState<*>,
    properties: JavaBreakpointProperties<*>,
    breakpointMetadata: BreakpointMetadata
) : ImportableBreakpointImpl<LineBreakpointState<*>, JavaBreakpointProperties<*>>(project, typeID, state, properties, breakpointMetadata)