/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service.scala

import com.bukowiecki.breakpoints.service.BreakpointManagerService
import com.bukowiecki.breakpoints.service.ImportableBreakpointFactory
import com.bukowiecki.breakpoints.service.python.PythonImportableProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.jetbrains.python.PythonLanguage
import org.jetbrains.plugins.scala.ScalaLanguage

/**
 * @author Marcin Bukowiecki
 */
class ScalaDependencyLoader : StartupActivity {

    override fun runActivity(project: Project) {
        val breakpointManagerService = BreakpointManagerService.getInstance(project)
        breakpointManagerService.addSupportedLanguage(ScalaLanguage.INSTANCE)

        val importableBreakpointFactory = ImportableBreakpointFactory.getInstance(project)
        importableBreakpointFactory.addImportableProvider(ScalaImportableProvider())
    }
}