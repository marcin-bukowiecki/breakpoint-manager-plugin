/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service

import com.bukowiecki.breakpoints.model.ImportableBreakpoint
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

/**
 * @author Marcin Bukowiecki
 */
class ImportableBreakpointFactory(private val project: Project) {

    private val log = Logger.getInstance(ImportableBreakpointFactory::class.java)

    @Volatile
    private var importableProviders = listOf<ImportableProvider>()

    fun addImportableProvider(importableProvider: ImportableProvider) {
        importableProviders = importableProviders + listOf(importableProvider)
    }

    fun isSupported(typeName: String?): Boolean {
        if (typeName == null) return false
        return importableProviders.any { it.isSupported(typeName) }
    }

    fun create(typeName: String,
               typeID: String,
               state: Any,
               properties: Any?,
               breakpointMetadata: BreakpointMetadata,
               projectWrapper: ProjectWrapper): ImportableBreakpoint? {

        for (importableProvider in importableProviders) {
            if (importableProvider.isSupported(typeName)) {
                return importableProvider.create(typeName, projectWrapper, typeID, state, properties, breakpointMetadata)
            }
        }

        log.info("Unsupported breakpoint type: $typeName")
        return null
    }

    companion object {
        fun getInstance(project: Project): ImportableBreakpointFactory {
            return project.getService(ImportableBreakpointFactory::class.java)
        }
    }
}