/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model.scala

import com.bukowiecki.breakpoints.model.ImportableBreakpointImpl
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.impl.breakpoints.LineBreakpointState

/**
 * @author Marcin Bukowiecki
 */
open class ScalaLineImportableBreakpointImpl(
    project: ProjectWrapper,
    typeID: String,
    state: LineBreakpointState<*>,
    properties: XBreakpointProperties<*>?,
    breakpointMetadata: BreakpointMetadata
) : ImportableBreakpointImpl<LineBreakpointState<*>, XBreakpointProperties<*>>(
    project,
    typeID,
    state,
    properties,
    breakpointMetadata
)