/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.bukowiecki.breakpoints.utils.BreakpointManagerUtils
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XBreakpointType
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import com.intellij.xdebugger.impl.XDebuggerManagerImpl
import com.intellij.xdebugger.impl.breakpoints.LineBreakpointState
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl
import com.intellij.xdebugger.impl.breakpoints.XBreakpointUtil
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import javax.swing.Icon

/**
 * @author Marcin Bukowiecki
 */
abstract class ImportableBreakpointImpl<STATE : LineBreakpointState<*>, PROP : XBreakpointProperties<*>>(
    private val myProjectWrapper: ProjectWrapper,
    private val typeID: String,
    protected val state: STATE,
    protected val properties: PROP?,
    protected val breakpointMetadata: BreakpointMetadata
) : ImportableBreakpoint {

    override val yourAbsolutePath: String = BreakpointManagerUtils.createAbsolutePath(myProjectWrapper, breakpointMetadata.relativeUrl)

    override lateinit var breakpoint: XLineBreakpointImpl<PROP>

    override fun getIcon(): Icon {
        return breakpoint.icon ?: super.getIcon()
    }

    override fun updateUI() {
        breakpoint.updateUI()
    }

    override fun getLine(): Int = state.line

    @Suppress("UNCHECKED_CAST")
    override fun getType(): XBreakpointType<XBreakpoint<PROP>, PROP>? {
        return XBreakpointUtil.findType(typeID) as? XBreakpointType<XBreakpoint<PROP>, PROP>?
    }

    override fun getRelativePath(): String {
        return breakpointMetadata.relativeUrl
    }

    override fun toString(): String {
        return "Breakpoint at: ${breakpointMetadata.line} in ${breakpointMetadata.url} (relative: ${breakpointMetadata.relativeUrl})"
    }

    //lazy initialize
    @Suppress("UNCHECKED_CAST")
    override fun createBreakpoint(): XLineBreakpointImpl<PROP> {
        val breakpointManager = XDebuggerManagerImpl.getInstance(myProjectWrapper.project).breakpointManager as XBreakpointManagerImpl
        assert(yourAbsolutePath.isNotEmpty())
        state.fileUrl = yourAbsolutePath

        val created = XLineBreakpointImpl(
            getType() as XLineBreakpointType<PROP>,
            breakpointManager,
            properties,
            state as LineBreakpointState<PROP>
        )

        breakpoint = created

        return created
    }
}