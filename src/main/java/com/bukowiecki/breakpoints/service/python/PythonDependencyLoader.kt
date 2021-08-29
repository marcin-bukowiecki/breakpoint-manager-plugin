/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service.python

import com.bukowiecki.breakpoints.service.BreakpointManagerService
import com.bukowiecki.breakpoints.service.ImportableBreakpointFactory
import com.bukowiecki.breakpoints.service.python.PythonImportableProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.jetbrains.python.PythonLanguage

/**
 * @author Marcin Bukowiecki
 */
class PythonDependencyLoader : StartupActivity {

    private val log = Logger.getInstance(PythonDependencyLoader::class.java)

    override fun runActivity(project: Project) {
        val breakpointManagerService = BreakpointManagerService.getInstance(project)
        try {
            breakpointManagerService.addSupportedLanguage(PythonLanguage.INSTANCE)
        } catch (e: NoClassDefFoundError) {
            log.warn("Error while loading python dependency: ", e)
            return
        }

        val importableBreakpointFactory = ImportableBreakpointFactory.getInstance(project)
        importableBreakpointFactory.addImportableProvider(PythonImportableProvider())
    }
}