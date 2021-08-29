/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
interface ImportableBreakpoint {
    val yourAbsolutePath: String
    val breakpoint: XLineBreakpointImpl<*>

    fun getRelativePath(): String
    fun getPresentableName(): String = getRelativePath()
    fun getIcon(): Icon = AllIcons.Debugger.Db_disabled_breakpoint
    fun getLine(): Int
    fun getType(): XBreakpointType<*, *>?
    fun updateUI()
    fun getVirtualFile(): VirtualFile? {
        return VfsUtil.findFileByURL(VfsUtil.convertToURL(yourAbsolutePath) ?: return null)
    }
    override fun toString(): String

    //lazy
    fun createBreakpoint(): XLineBreakpointImpl<*>
}