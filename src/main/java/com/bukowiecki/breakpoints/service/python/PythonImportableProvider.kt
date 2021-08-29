/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service.python

import com.bukowiecki.breakpoints.model.ImportableBreakpoint
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.model.python.PythonLineImportableBreakpointImpl
import com.bukowiecki.breakpoints.service.ImportableProvider
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.intellij.xdebugger.impl.breakpoints.LineBreakpointState
import com.jetbrains.python.debugger.PyLineBreakpointType

/**
 * @author Marcin Bukowiecki
 */
class PythonImportableProvider : ImportableProvider {

    private val supportedTypes = setOf<String>(
        PyLineBreakpointType::class.java.canonicalName
    )

    override fun isSupported(typeName: String): Boolean {
        return supportedTypes.contains(typeName)
    }

    override fun create(
        typeName: String,
        project: ProjectWrapper,
        typeID: String,
        state: Any,
        properties: Any?,
        breakpointMetadata: BreakpointMetadata
    ): ImportableBreakpoint {
        if (typeName == PyLineBreakpointType::class.java.canonicalName) {
            return PythonLineImportableBreakpointImpl(
                project,
                typeID,
                state as LineBreakpointState<*>,
                breakpointMetadata
            )
        } else {
            throw UnsupportedOperationException("Unsupported typeName: $typeName")
        }
    }
}