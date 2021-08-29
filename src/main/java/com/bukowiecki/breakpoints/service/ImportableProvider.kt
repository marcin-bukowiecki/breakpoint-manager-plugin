/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service

import com.bukowiecki.breakpoints.model.ImportableBreakpoint
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata

/**
 * @author Marcin Bukowiecki
 */
interface ImportableProvider {

    fun isSupported(typeName: String): Boolean

    fun create(
        typeName: String,
        project: ProjectWrapper,
        typeID: String,
        state: Any,
        properties: Any?,
        breakpointMetadata: BreakpointMetadata
    ): ImportableBreakpoint
}