/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.utils

import com.bukowiecki.breakpoints.service.MyMockProject
import com.bukowiecki.breakpoints.service.MyProjectWrapper
import org.junit.Assert
import org.junit.Test

/**
 * @author Marcin Bukowiecki
 */
class BreakpointManagerUtilsTest {

    @Test
    fun testCreateAbsolutePath1() {
        val expected = "file:///C:/foo/src/Test.java"
        val given = BreakpointManagerUtils.createAbsolutePath(MyProjectWrapper(MyMockProject()), "src/Test.java")
        Assert.assertEquals(expected, given)
    }
}