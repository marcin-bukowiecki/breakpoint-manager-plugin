/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.controller

import com.bukowiecki.breakpoints.model.BreakpointsToImportTableModel
import com.bukowiecki.breakpoints.model.ImportableBreakpoint
import com.bukowiecki.breakpoints.model.ImportableBreakpointWrapper
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.service.BreakpointManagerService
import com.bukowiecki.breakpoints.service.ImportableBreakpointFactory
import com.bukowiecki.breakpoints.service.XmlVisitor
import com.bukowiecki.breakpoints.ui.ImportBreakpointsDialog
import com.bukowiecki.breakpoints.utils.BreakpointManagerBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.util.ReflectionUtil
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase
import com.intellij.xdebugger.impl.breakpoints.XBreakpointManagerImpl
import java.io.FileInputStream
import java.io.FileNotFoundException
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory

/**
 * @author Marcin Bukowiecki
 */
class ImportController(private val myDialog: ImportBreakpointsDialog) {

    private val log = Logger.getInstance(ImportController::class.java)

    @Volatile
    private var toOverride = mutableMapOf<ImportableBreakpoint, XLineBreakpoint<*>>()

    fun set() {
        val table = myDialog.getTable()
        val breakpointManager = XDebuggerManager.getInstance(myDialog.getProject()).breakpointManager as? XBreakpointManagerImpl ?: return

        (table.model as? BreakpointsToImportTableModel)?.let { model ->
            for (selectedBreakpoint in model.getSelectedBreakpoints()) {
                val importableBreakpoint = selectedBreakpoint.getImportableBreakpoint()
                if (toOverride.containsKey(importableBreakpoint)) {
                    if (selectedBreakpoint.myOverride) {
                        ApplicationManager.getApplication().runWriteAction {
                            breakpointManager.removeBreakpoint(toOverride[importableBreakpoint]!!)
                        }
                    } else {
                        continue
                    }
                }

                val createdBreakpoint = selectedBreakpoint.getImportableBreakpoint().breakpoint

                WriteAction.computeAndWait(ThrowableComputable<Any, RuntimeException> {
                    val method = ReflectionUtil.getDeclaredMethod(
                        XBreakpointManagerImpl::class.java,
                        "addBreakpoint",
                        XBreakpointBase::class.java,
                        Boolean::class.java,
                        Boolean::class.java
                    )!!
                    method.isAccessible = true
                    method.invoke(breakpointManager, createdBreakpoint, false, true)
                    selectedBreakpoint.getImportableBreakpoint().updateUI()
                })
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun import() {
        val inputFilePath = myDialog.getInputFilePath()
        val xmlInputFactory: XMLInputFactory = XMLInputFactory.newInstance()
        val reader: XMLEventReader = try {
            xmlInputFactory.createXMLEventReader(FileInputStream(inputFilePath))
        } catch (e: FileNotFoundException) {
            Messages.showErrorDialog(
                BreakpointManagerBundle.message("breakpoints.import.file.notFound"),
                BreakpointManagerBundle.message("breakpoints.import.error")
            )
            return
        }
        val breakpointManager = XDebuggerManager.getInstance(myDialog.getProject()).breakpointManager as? XBreakpointManagerImpl ?: return
        val factory = ImportableBreakpointFactory.getInstance(myDialog.getProject())
        val visitor = XmlVisitor(factory, reader, ProjectWrapper(myDialog.getProject()))

        try {
            visitor.visit()
        } catch (e: Exception) {
            log.info("Exception while parsing imported breakpoints file", e)
            Messages.showErrorDialog(
                BreakpointManagerBundle.message("breakpoints.import.file.parseException"),
                BreakpointManagerBundle.message("breakpoints.import.error")
            )
            return
        }

        val importableBreakpoints = visitor.importableBreakpoints
        val tableRows = mutableListOf<ImportableBreakpointWrapper>()

        toOverride = mutableMapOf()

        label@
        for (importableBreakpoint in importableBreakpoints) {
            importableBreakpoint.createBreakpoint()

            val type = importableBreakpoint.getType()
            val importableBreakpointWrapper = ImportableBreakpointWrapper(importableBreakpoint)
            tableRows.add(importableBreakpointWrapper)

            log.info("Got breakpoint to import: $importableBreakpoint")

            if (type != null && type is XLineBreakpointType<*>) {
                val actual = try {
                    importableBreakpoint.getVirtualFile()?.let { vf ->
                        breakpointManager
                            .findBreakpointAtLine(
                                type,
                                vf,
                                importableBreakpoint.getLine()
                            )
                    }
                } catch (e: Exception) {
                    log.info("Breakpoint not found. Cause: ${e.message}")
                    null
                }
                if (actual != null) {
                    importableBreakpointWrapper.overrideDisabled = false
                    importableBreakpointWrapper.myOverride = true
                    toOverride[importableBreakpoint] = actual
                }
            }
        }

        tryShowMessage(visitor)
        showLabels(visitor)

        val table = myDialog.getTable()
        val tableModel = BreakpointsToImportTableModel(tableRows.toList())
        table.model = tableModel
        table.revalidate()
        myDialog.infoLabel.repaint()
    }

    private fun tryShowMessage(visitor: XmlVisitor) {
        val breakpointManagerService = BreakpointManagerService.getInstance(myDialog.getProject())
        if (visitor.commitID.isNotEmpty() && breakpointManagerService.getCurrentRevision().isEmpty()) {

            Messages.showWarningDialog(
                BreakpointManagerBundle.message("breakpoints.import.commitID.notAGitRepository", visitor.branchName, visitor.commitID),
                BreakpointManagerBundle.message("breakpoints.import.warning")
            )
        } else if (visitor.commitID.isNotEmpty() &&
            breakpointManagerService.getCurrentRevision().isNotEmpty() &&
            visitor.commitID != breakpointManagerService.getCurrentRevision()) {

            Messages.showWarningDialog(
                BreakpointManagerBundle.message("breakpoints.import.commitID.different", visitor.branchName, visitor.commitID),
                BreakpointManagerBundle.message("breakpoints.import.warning")
            )
        }
    }

    private fun showLabels(visitor: XmlVisitor) {
        if (toOverride.isNotEmpty()) {
            myDialog.infoLabel.isVisible = true
            myDialog.infoLabel.icon = AllIcons.Actions.IntentionBulb
            if (toOverride.size == 1) {
                myDialog.infoLabel.text = BreakpointManagerBundle.message("breakpoints.import.breakpoint.override")
            } else {
                myDialog.infoLabel.text = BreakpointManagerBundle.message("breakpoints.import.breakpoints.override", toOverride.size)
            }
        } else {
            myDialog.infoLabel.isVisible = false
        }

        if (visitor.commitID.isNotEmpty()) {
            myDialog.commitIDLabel.isVisible = true
            myDialog.commitIDLabel.icon = AllIcons.Actions.IntentionBulb
            myDialog.commitIDLabel.text = BreakpointManagerBundle.message("breakpoints.import.commitID", visitor.commitID)
        } else {
            myDialog.commitIDLabel.isVisible = false
        }

        if (visitor.branchName.isNotEmpty()) {
            myDialog.branchNameLabel.isVisible = true
            myDialog.branchNameLabel.icon = AllIcons.Actions.IntentionBulb
            myDialog.branchNameLabel.text = BreakpointManagerBundle.message("breakpoints.import.branchName", visitor.branchName)
        } else {
            myDialog.branchNameLabel.isVisible = false
        }
    }
}