/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.controller

import com.bukowiecki.breakpoints.model.DefaultBreakpoint
import com.bukowiecki.breakpoints.model.XmlBreakpointsRoot
import com.bukowiecki.breakpoints.service.BreakpointManagerService
import com.bukowiecki.breakpoints.service.ImportableBreakpointFactory
import com.bukowiecki.breakpoints.tagging.TagsProperties
import com.bukowiecki.breakpoints.ui.ExportSupport
import com.bukowiecki.breakpoints.utils.BreakpointManagerBundle
import com.bukowiecki.breakpoints.utils.BreakpointManagerUtils
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.io.exists
import com.intellij.util.xmlb.XmlSerializer
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl
import org.jdom.Document
import org.jdom.output.Format
import org.jdom.output.XMLOutputter
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths

/**
 * @author Marcin Bukowiecki
 */
class ExportController(private val myExportSupport: ExportSupport) {

    private val log = Logger.getInstance(ExportController::class.java)

    fun validate(): Boolean {
        val filePath = myExportSupport.getOutputFilePath().ifEmpty {
            FileUtil.normalize(myExportSupport.getProject().basePath + File.separator)
        }
        if (!Paths.get(filePath).exists()) {
            Messages.showErrorDialog(
                BreakpointManagerBundle.message("breakpoints.export.error.outputPath"),
                BreakpointManagerBundle.message("breakpoints.export.error.outputPath.title")
            )
            return false
        }

        return true
    }

    @Suppress("UnstableApiUsage")
    fun export(indicator: ProgressIndicator): Int {
        indicator.isIndeterminate = false
        indicator.text = BreakpointManagerBundle.message("breakpoints.export.progress")

        val project = myExportSupport.getProject()
        val checkedBreakpoints = myExportSupport.getTreeController().getCheckedBreakpoints()
        val tagsProperties = TagsProperties.getInstance(project)
        val doc = Document()
        val model = XmlBreakpointsRoot()
        val importableBreakpointFactory = ImportableBreakpointFactory.getInstance(project)
        val currentRevision = BreakpointManagerService.getInstance(project).getCurrentRevision()
        val currentBranchName = BreakpointManagerService.getInstance(project).getCurrentBranchName() ?: ""

        var exported = 0
        forLabel@
        for ((index, selectedItem) in checkedBreakpoints.withIndex()) {
            indicator.fraction = (index/checkedBreakpoints.size).toDouble()

            if (!BreakpointManagerUtils.canBeExported(project, selectedItem)) {
                log.info("Breakpoint ${selectedItem.displayText} of type ${selectedItem.breakpoint.javaClass.canonicalName} can't be exported")
                continue
            }

            val breakpoint = selectedItem.breakpoint
            val lineBreakpoint = breakpoint as? XLineBreakpointImpl<*> ?: continue@forLabel

            val wrapper = DefaultBreakpoint()
            wrapper.breakpointCanonicalName = breakpoint.javaClass.canonicalName
            wrapper.typeCanonicalName = lineBreakpoint.type.javaClass.canonicalName
            wrapper.typeID = lineBreakpoint.type.id

            val properties = lineBreakpoint.properties
            if (properties != null) {
                wrapper.propertiesCanonicalName = properties.javaClass.canonicalName
                wrapper.properties = XmlSerializer.serialize(properties)
            }
            wrapper.state = XmlSerializer.serialize(lineBreakpoint.state)
            wrapper.stateCanonicalName = lineBreakpoint.state.javaClass.canonicalName

            val metadata = lineBreakpoint.sourcePosition?.let { sourcePosition ->
                XmlSerializer.serialize(tagsProperties.findState(sourcePosition))
            } ?: continue@forLabel

            wrapper.metadata = metadata

            if (!importableBreakpointFactory.isSupported(wrapper.typeCanonicalName)) {
                continue@forLabel
            }

            model.commitID = currentRevision
            model.branchName = currentBranchName
            model.breakpoints.add(wrapper)
            exported++
        }

        doc.rootElement = XmlSerializer.serialize(model)

        val fileName = myExportSupport.getOutputFileName().ifEmpty {
            BreakpointManagerUtils.defaultFileName
        }

        var filePath = myExportSupport.getOutputFilePath().ifEmpty {
            project.basePath + File.separator
        }
        if (!filePath.endsWith(File.separator)) {
            filePath += File.separator
        }

        var absolutePath = "$filePath$fileName.xml"
        if (Paths.get(absolutePath).exists()) {
            absolutePath = createXMLAbsolutePath(filePath, fileName)
        }
        absolutePath = FileUtil.normalize(absolutePath)

        val out = XMLOutputter(Format.getPrettyFormat())
        out.output(doc, FileOutputStream(absolutePath))

        indicator.fraction = 1.0
        indicator.stop()

        ApplicationManager.getApplication().invokeLaterOnWriteThread {
            LocalFileSystem.getInstance().refreshAndFindFileByPath(absolutePath)
        }

        return exported
    }

    private fun createXMLAbsolutePath(filePath: String, fileName: String, index: Int = 1): String {
        val absolutePath = "$filePath$fileName($index).xml"
        if (Paths.get(absolutePath).exists()) {
            return createXMLAbsolutePath(filePath, fileName, index+1)
        }
        return absolutePath
    }
}