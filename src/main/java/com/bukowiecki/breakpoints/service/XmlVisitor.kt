/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service

import com.bukowiecki.breakpoints.model.ImportableBreakpoint
import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.bukowiecki.breakpoints.tagging.BreakpointMetadata
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.XmlSerializer
import org.jdom.input.SAXBuilder
import java.io.StringWriter
import javax.xml.namespace.QName
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.StartElement
import javax.xml.stream.events.XMLEvent

/**
 * @author Marcin Bukowiecki
 */
class XmlVisitor(private val factory: ImportableBreakpointFactory,
                 private val myReader: XMLEventReader,
                 private val projectWrapper: ProjectWrapper) {

    private val log = Logger.getInstance(XmlVisitor::class.java)

    private var propObj: Any? = null
    private var stateObj: Any? = null
    private var metadataObj: BreakpointMetadata? = null

    var commitID = ""
    var branchName = ""

    val importableBreakpoints = mutableListOf<ImportableBreakpoint>()

    fun visit() {
        while (myReader.hasNext()) {
            val nextEvent: XMLEvent = myReader.nextEvent()
            if (nextEvent.isStartElement) {
                val name = nextEvent.asStartElement().name.localPart
                if (name == "exported-breakpoints") {
                    visitExportedBreakpoints()
                }
            }
        }
    }

    private fun visitExportedBreakpoints() {
        while (myReader.hasNext()) {
            val nextEvent = myReader.nextEvent()
            if (nextEvent.isStartElement) {
                if (nextEvent.asStartElement().name.localPart == "commitID") {
                    visitCommitID()
                } else if (nextEvent.asStartElement().name.localPart == "breakpoints") {
                    visitBreakpoints()
                } else if (nextEvent.asStartElement().name.localPart == "branchName") {
                    visitBranchName()
                }
            }
        }
    }

    private fun visitState(evt: XMLEvent, stateName: String) {
        var acc = evt.eventToString()
        while (myReader.hasNext()) {
            val nextEvent = myReader.nextEvent()
            acc += nextEvent.eventToString()
            if (nextEvent.isEndElement && nextEvent.asEndElement().name.localPart == "state") {
                break
            }
        }
        acc = acc.replace("&", "&amp;")
        val builder = SAXBuilder()
        val doc = builder.build(acc.byteInputStream())

        this.stateObj = XmlSerializer.deserialize(doc, Class.forName(stateName))
    }

    private fun visitMetadata(evt: XMLEvent) {
        var acc = evt.eventToString()
        while (myReader.hasNext()) {
            val nextEvent = myReader.nextEvent()
            acc += nextEvent.eventToString()
            if (nextEvent.isEndElement && nextEvent.asEndElement().name.localPart == "metadata") {
                break
            }
        }
        acc = acc.trim().replace("&", "&amp;")
        val builder = SAXBuilder()
        val doc = builder.build(acc.byteInputStream())

        this.metadataObj = XmlSerializer.deserialize(doc, BreakpointMetadata::class.java)
    }

    private fun visitProperties(evt: XMLEvent, propName: String) {
        var acc = evt.eventToString()
        while (myReader.hasNext()) {
            val nextEvent = myReader.nextEvent()
            acc += nextEvent.eventToString()
            if (nextEvent.isEndElement && nextEvent.asEndElement().name.localPart == "properties") {
                break
            }
        }
        val builder = SAXBuilder()
        val doc = builder.build(acc.byteInputStream())

        this.propObj = XmlSerializer.deserialize(doc, Class.forName(propName))
    }

    @Suppress("UNCHECKED_CAST")
    private fun visitExportedDefaultBreakpoint(se: StartElement) {
        val propName = se.getAttributeByName(QName("properties-canonical-name")).value
        val stateName = se.getAttributeByName(QName("state-canonical-name")).value
        val typeName = se.getAttributeByName(QName("type-canonical-name")).value
        if (!factory.isSupported(typeName)) {
            log.info("Unsupported breakpoint type: $typeName")
            while (myReader.hasNext()) {
                val nextEvent = myReader.nextEvent()
                if (nextEvent.isEndElement && nextEvent.asEndElement().name.localPart == "exported-default-breakpoint") {
                    break
                }
            }
            return
        }

        val typeID = se.getAttributeByName(QName("type-id")).value

        while (myReader.hasNext()) {
            val nextEvent = myReader.nextEvent()
            if (nextEvent.isStartElement && nextEvent.asStartElement().name.localPart == "properties") {
                visitProperties(nextEvent, propName)
            } else if (nextEvent.isStartElement && nextEvent.asStartElement().name.localPart == "state") {
                visitState(nextEvent, stateName)
            } else if (nextEvent.isStartElement && nextEvent.asStartElement().name.localPart == "metadata") {
                visitMetadata(nextEvent)
            } else if (nextEvent.isEndElement && nextEvent.asEndElement().name.localPart == "exported-default-breakpoint") {
                break
            }
        }

        if (metadataObj == null) {
            log.info("Could not create breakpoint: $typeName (metadata is empty)")
            return
        }

        val importableBreakpointImpl = try {
            factory.create(typeName, typeID, stateObj!!, propObj, metadataObj!!, projectWrapper) ?: return
        } catch (e: Exception) {
            log.error("Could not create breakpoint: $typeName", e)
            return
        }
        importableBreakpoints.add(importableBreakpointImpl)
    }

    private fun visitList() {
        while (myReader.hasNext()) {
            val nextEvent = myReader.nextEvent()
            if (nextEvent.isStartElement) {
                val asStartElement = nextEvent.asStartElement()
                if (asStartElement.name.localPart == "exported-default-breakpoint") {
                    visitExportedDefaultBreakpoint(asStartElement)
                }
            }
        }
    }

    private fun visitBranchName() {
        val nextEvent = myReader.nextEvent()
        if (nextEvent.isEndElement) return
        this.branchName = nextEvent.eventToString()
    }

    private fun visitCommitID() {
        val nextEvent = myReader.nextEvent()
        if (nextEvent.isEndElement) return
        this.commitID = nextEvent.eventToString()
    }

    private fun visitBreakpoints() {
        while (myReader.hasNext()) {
            val nextEvent = myReader.nextEvent()
            if (nextEvent.isStartElement) {
                if (nextEvent.asStartElement().name.localPart == "list") {
                    visitList()
                }
            }
        }
    }

    private fun XMLEvent.eventToString(): String {
        val stringWriter = StringWriter()
        this.writeAsEncodedUnicode(stringWriter)
        return stringWriter.toString()
    }
}