/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service

import com.bukowiecki.breakpoints.service.python.PythonImportableProvider
import org.junit.Assert
import org.junit.Test
import java.io.FileInputStream
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory

/**
 * @author Marcin Bukowiecki
 */
class XmlBreakpointsParserTest {

    @Test
    fun testParse1() {
        val project = MyMockProject()
        val factory = ImportableBreakpointFactory(project)
        factory.addImportableProvider(PythonImportableProvider())
        val xmlInputFactory: XMLInputFactory = XMLInputFactory.newInstance()
        val reader: XMLEventReader = xmlInputFactory.createXMLEventReader(FileInputStream("./src/test/resources/data/d005a435-1b76-4bd0-9f8a-b7ad51efe8cb.xml"))
        val visitor = XmlVisitor(factory, reader, MyProjectWrapper(project))
        visitor.visit()
        val importableBreakpoints = visitor.importableBreakpoints
        Assert.assertEquals(1, importableBreakpoints.size)
        Assert.assertEquals("foo", visitor.branchName)
        Assert.assertEquals("1234", visitor.commitID)
    }

    @Test
    fun testParse2() {
        val project = MyMockProject()
        val factory = ImportableBreakpointFactory(project)
        factory.addImportableProvider(PythonImportableProvider())
        val xmlInputFactory: XMLInputFactory = XMLInputFactory.newInstance()
        val reader: XMLEventReader = xmlInputFactory.createXMLEventReader(FileInputStream("./src/test/resources/data/Breakpoints.xml"))
        val visitor = XmlVisitor(factory, reader, MyProjectWrapper(project))
        visitor.visit()
        val importableBreakpoints = visitor.importableBreakpoints
        Assert.assertEquals(1, importableBreakpoints.size)
        Assert.assertEquals("", visitor.branchName)
        Assert.assertEquals("", visitor.commitID)
    }

    @Test
    fun testParse3() {
        val project = MyMockProject()
        val factory = ImportableBreakpointFactory(project)
        factory.addImportableProvider(PythonImportableProvider())
        val xmlInputFactory: XMLInputFactory = XMLInputFactory.newInstance()
        val reader: XMLEventReader = xmlInputFactory.createXMLEventReader(FileInputStream("./src/test/resources/data/Breakpoints2.xml"))
        val visitor = XmlVisitor(factory, reader, MyProjectWrapper(project))
        visitor.visit()
        val importableBreakpoints = visitor.importableBreakpoints
        Assert.assertEquals(1, importableBreakpoints.size)
        Assert.assertEquals("", visitor.branchName)
        Assert.assertEquals("", visitor.commitID)
    }

    @Test
    fun testParse4() {
        val project = MyMockProject()
        val factory = ImportableBreakpointFactory(project)
        factory.addImportableProvider(PythonImportableProvider())
        val xmlInputFactory: XMLInputFactory = XMLInputFactory.newInstance()
        val reader: XMLEventReader = xmlInputFactory.createXMLEventReader(FileInputStream("./src/test/resources/data/Breakpoints3.xml"))
        val visitor = XmlVisitor(factory, reader, MyProjectWrapper(project))
        visitor.visit()
        val importableBreakpoints = visitor.importableBreakpoints
        Assert.assertEquals(1, importableBreakpoints.size)
        Assert.assertEquals("master", visitor.branchName)
        Assert.assertEquals("adb32d8b80ef5b24c7e286db39c151cbffb168c2", visitor.commitID)
    }

    @Test
    fun testParse5() {
        val project = MyMockProject()
        val factory = ImportableBreakpointFactory(project)
        factory.addImportableProvider(PythonImportableProvider())
        val xmlInputFactory: XMLInputFactory = XMLInputFactory.newInstance()
        val reader: XMLEventReader = xmlInputFactory.createXMLEventReader(FileInputStream("./src/test/resources/data/Breakpoints4.xml"))
        val visitor = XmlVisitor(factory, reader, MyProjectWrapper(project))
        visitor.visit()
        val importableBreakpoints = visitor.importableBreakpoints
        Assert.assertEquals(2, importableBreakpoints.size)
        Assert.assertEquals("master", visitor.branchName)
        Assert.assertEquals("40910804189e3654caac1aa91dfcb1ccf2d4baf6", visitor.commitID)
    }
}