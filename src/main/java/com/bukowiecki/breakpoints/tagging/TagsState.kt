/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.tagging

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl

/**
 * @author Marcin Bukowiecki
 */
@State(
    name = "BreakpointManagerTagsProperties",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)]
)
class TagsProperties(@Suppress("unused") private val project: Project) : PersistentStateComponent<StateData> {

    private val state = StateData()

    override fun getState(): StateData {
        return this.state
    }

    override fun loadState(state: StateData) {
        this.state.breakpoints = state.breakpoints
    }

    fun findStatesByTag(breakpointTag: BreakpointTag): List<BreakpointMetadata> {
        return state.breakpoints.filter { it.tags.toSet().contains(breakpointTag) }
    }

    fun findStateForFile(file: VirtualFile): List<BreakpointMetadata> {
        return state.breakpoints.filter { it.url == file.url }
    }

    @Deprecated("unused")
    fun findStateForEditor(editor: Editor): List<BreakpointMetadata> {
        val fileDocumentManager = FileDocumentManager.getInstance()
        val result = mutableListOf<BreakpointMetadata>()

        for (breakpoint in state.breakpoints) {
            val document = editor.document
            val file = fileDocumentManager.getFile(document) ?: continue

            if (breakpoint.url == file.url) {
                result.add(breakpoint)
            }
        }

        return result
    }

    fun findState(sourcePosition: XSourcePosition): BreakpointMetadata {
        val file = sourcePosition.file
        val url = file.url
        val line = sourcePosition.line

        val state = findState(url, line)
        return state ?: saveState(sourcePosition, emptyList())
    }

    private fun findState(url: String, line: Int): BreakpointMetadata? {
        return state.breakpoints.find { it.url == url && it.line == line }
    }

    fun saveState(sourcePosition: XSourcePosition,
                  tags: List<BreakpointTag>): BreakpointMetadata {

        val ref = BreakpointMetadata()
        ref.url = sourcePosition.file.url
        ref.relativeUrl = project.guessProjectDir()?.let { VfsUtilCore.getRelativeLocation(sourcePosition.file, it) } ?: ""
        ref.line = sourcePosition.line
        ref.tags = tags

        state.breakpoints.add(ref)

        return ref
    }

    fun removeState(xBreakpoint: XLineBreakpointImpl<*>) {
        val sourcePosition = xBreakpoint.sourcePosition ?: return
        val file = sourcePosition.file
        val url = file.url
        val line = sourcePosition.line

        val iterator = state.breakpoints.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.line == line && next.url == url) {
                iterator.remove()
            }
        }
    }

    fun removeState(metadata: BreakpointMetadata) {
        val iterator = this.state.breakpoints.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next == metadata) {
                iterator.remove()
                break
            }
        }
    }

    companion object {

        fun getInstance(project: Project): TagsProperties {
            return project.getService(TagsProperties::class.java)
        }
    }
}

/**
 * @author Marcin Bukowiecki
 */
class StateData {
    var tagsEnabled = true
    var breakpoints: MutableList<BreakpointMetadata> = mutableListOf()
}

/**
 * @author Marcin Bukowiecki
 */
class BreakpointTag() {
    var key: String = ""
    var value: String = ""

    constructor(key: String, value: String): this() {
        this.key = key
        this.value = value
    }

    override fun toString(): String {
        return "$key:$value"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BreakpointTag

        if (key != other.key) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    fun createPrettyText(): String {
        var acc = ""

        if (key.isNotEmpty()) {
            acc += "Tag: $key"
        }

        if (value.isNotEmpty()) {
            acc += " : $value"
        }

        return acc
    }
}

/**
 * @author Marcin Bukowiecki
 */
class BreakpointMetadata {
    var url: String = ""
    var relativeUrl: String = ""
    var line: Int = -1
    var tags: List<BreakpointTag> = emptyList()

    fun tagsAsSet() = tags.toSet()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BreakpointMetadata

        if (url != other.url) return false
        if (line != other.line) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + line
        return result
    }

    override fun toString(): String {
        return "BreakpointRef(url='$url', line=$line, tags=$tags)"
    }
}