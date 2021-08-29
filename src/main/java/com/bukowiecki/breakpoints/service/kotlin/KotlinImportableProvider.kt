/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service.kotlin

import com.bukowiecki.breakpoints.model.ImportableBreakpoint
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.model.java.JavaMethodImportableBreakpointImpl
import com.bukowiecki.breakpoints.model.kotlin.KotlinFieldImportableBreakpointImpl
import com.bukowiecki.breakpoints.model.kotlin.KotlinLineImportableBreakpointImpl
import com.bukowiecki.breakpoints.service.ImportableProvider
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.intellij.xdebugger.impl.breakpoints.LineBreakpointState
import org.jetbrains.java.debugger.breakpoints.properties.JavaBreakpointProperties
import org.jetbrains.java.debugger.breakpoints.properties.JavaFieldBreakpointProperties
import org.jetbrains.java.debugger.breakpoints.properties.JavaMethodBreakpointProperties
import org.jetbrains.kotlin.idea.debugger.breakpoints.KotlinFieldBreakpointType
import org.jetbrains.kotlin.idea.debugger.breakpoints.KotlinFunctionBreakpointType
import org.jetbrains.kotlin.idea.debugger.breakpoints.KotlinLineBreakpointType

/**
 * @author Marcin Bukowiecki
 */
class KotlinImportableProvider : ImportableProvider {

    private val supportedTypes = setOf<String>(
        KotlinLineBreakpointType::class.java.canonicalName,
        KotlinFunctionBreakpointType::class.java.canonicalName,
        KotlinFieldBreakpointType::class.java.canonicalName,
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
            KotlinLineBreakpointType::class.java.canonicalName -> {
                KotlinLineImportableBreakpointImpl(
                    project,
                    typeID,
                    state as LineBreakpointState<*>,
                    properties as JavaBreakpointProperties<*>,
                    breakpointMetadata
                )
            }
            KotlinFunctionBreakpointType::class.java.canonicalName -> {
                JavaMethodImportableBreakpointImpl(
                    project,
                    typeID,
                    state as LineBreakpointState<*>,
                    properties as JavaMethodBreakpointProperties,
                    breakpointMetadata
                )
            }
            KotlinFieldBreakpointType::class.java.canonicalName -> {
                KotlinFieldImportableBreakpointImpl(
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
