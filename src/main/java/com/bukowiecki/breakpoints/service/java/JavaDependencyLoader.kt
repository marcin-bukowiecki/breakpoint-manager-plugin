/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service.java

import com.bukowiecki.breakpoints.service.BreakpointManagerService
import com.bukowiecki.breakpoints.service.ImportableBreakpointFactory
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

/**
 * @author Marcin Bukowiecki
 */
class JavaDependencyLoader : StartupActivity {

    override fun runActivity(project: Project) {
        val breakpointManagerService = BreakpointManagerService.getInstance(project)
        breakpointManagerService.addSupportedLanguage(JavaLanguage.INSTANCE)

        val importableBreakpointFactory = ImportableBreakpointFactory.getInstance(project)
        importableBreakpointFactory.addImportableProvider(JavaImportableProvider())
    }
}