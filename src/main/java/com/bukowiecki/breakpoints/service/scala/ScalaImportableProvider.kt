/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service.scala

import com.bukowiecki.breakpoints.model.ImportableBreakpoint
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.model.scala.ScalaLineImportableBreakpointImpl
import com.bukowiecki.breakpoints.service.ImportableProvider
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.intellij.xdebugger.impl.breakpoints.LineBreakpointState
import org.jetbrains.java.debugger.breakpoints.properties.JavaLineBreakpointProperties
import org.jetbrains.plugins.scala.debugger.breakpoints.ScalaLineBreakpointType

/**
 * @author Marcin Bukowiecki
 */
class ScalaImportableProvider : ImportableProvider {

    private val supportedTypes = setOf<String>(
        ScalaLineBreakpointType::class.java.canonicalName
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
        if (typeName == ScalaLineBreakpointType::class.java.canonicalName) {
            return ScalaLineImportableBreakpointImpl(
                project,
                typeID,
                state as LineBreakpointState<*>,
                properties as? JavaLineBreakpointProperties,
                breakpointMetadata
            )
        } else {
            throw UnsupportedOperationException("Unsupported typeName: $typeName")
        }
    }
}