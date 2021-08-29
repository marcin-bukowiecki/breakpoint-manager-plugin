/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service.kotlin

import com.bukowiecki.breakpoints.service.BreakpointManagerService
import com.bukowiecki.breakpoints.service.ImportableBreakpointFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.jetbrains.kotlin.idea.KotlinLanguage

/**
 * @author Marcin Bukowiecki
 */
class KotlinDependencyLoader : StartupActivity {

    override fun runActivity(project: Project) {
        val breakpointManagerService = BreakpointManagerService.getInstance(project)
        breakpointManagerService.addSupportedLanguage(KotlinLanguage.INSTANCE)

        val importableBreakpointFactory = ImportableBreakpointFactory.getInstance(project)
        importableBreakpointFactory.addImportableProvider(KotlinImportableProvider())
    }
}