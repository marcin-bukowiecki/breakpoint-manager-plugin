/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.utils

import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import com.intellij.xdebugger.impl.breakpoints.ui.BreakpointItem
import java.io.File

/**
 * @author Marcin Bukowiecki
 */
object BreakpointManagerUtils {

    const val defaultFileName = "Breakpoints"

    fun createAbsolutePath(project: ProjectWrapper,
                           relativePath: String): String {

        return project.guessProjectDir()?.path?.let { projectPath ->
            "file://" + FileUtil.normalize(projectPath + File.separator + relativePath)
        } ?: ""
    }

    fun canBeExported(project: Project, breakpointItem: BreakpointItem): Boolean {
        val breakpoint = breakpointItem.breakpoint as? XLineBreakpointImpl<*> ?: return false
        val fileUrl = breakpoint.fileUrl
        val breakpointFile = VfsUtil.findFileByURL(VfsUtil.convertToURL(fileUrl) ?: return false) ?: return false
        if (!breakpointFile.isInLocalFileSystem) {
            return false
        }

        val projectDir = project.guessProjectDir() ?: return false
        if (!projectDir.isInLocalFileSystem) {
            return false
        }

        return VfsUtilCore.isUnder(breakpointFile, setOf(projectDir))
    }
}