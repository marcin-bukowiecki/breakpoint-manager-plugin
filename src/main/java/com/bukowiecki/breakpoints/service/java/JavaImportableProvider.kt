/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service.java

import com.bukowiecki.breakpoints.model.ImportableBreakpoint
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.model.java.JavaFieldImportableBreakpointImpl
import com.bukowiecki.breakpoints.model.java.JavaLineImportableBreakpointImpl
import com.bukowiecki.breakpoints.model.java.JavaMethodImportableBreakpointImpl
import com.bukowiecki.breakpoints.service.ImportableProvider
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.intellij.debugger.ui.breakpoints.JavaFieldBreakpointType
import com.intellij.debugger.ui.breakpoints.JavaLineBreakpointType
import com.intellij.debugger.ui.breakpoints.JavaMethodBreakpointType
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.impl.breakpoints.LineBreakpointState
import org.jetbrains.java.debugger.breakpoints.properties.JavaBreakpointProperties
import org.jetbrains.java.debugger.breakpoints.properties.JavaFieldBreakpointProperties
import org.jetbrains.java.debugger.breakpoints.properties.JavaMethodBreakpointProperties

/**
 * @author Marcin Bukowiecki
 */
class JavaImportableProvider : ImportableProvider {

    private val supportedTypes = setOf<String>(
        JavaLineBreakpointType::class.java.canonicalName,
        JavaMethodBreakpointType::class.java.canonicalName,
        JavaFieldBreakpointType::class.java.canonicalName,
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
        return when (typeName) {
            JavaLineBreakpointType::class.java.canonicalName -> {
                JavaLineImportableBreakpointImpl(
                    project,
                    typeID,
                    state as LineBreakpointState<*>,
                    properties as JavaBreakpointProperties<*>,
                    breakpointMetadata
                )
            }
            JavaMethodBreakpointType::class.java.canonicalName -> {
                JavaMethodImportableBreakpointImpl(
                    project,
                    typeID,
                    state as LineBreakpointState<*>,
                    properties as JavaMethodBreakpointProperties,
                    breakpointMetadata
                )
            }
            JavaFieldBreakpointType::class.java.canonicalName -> {
                JavaFieldImportableBreakpointImpl(
                    project,
                    typeID,
                    state as LineBreakpointState<*>,
                    properties as JavaFieldBreakpointProperties,
                    breakpointMetadata
                )
            }
            else -> {
                throw UnsupportedOperationException("Unsupported typeName: $typeName")
            }
        }
    }
}