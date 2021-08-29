/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

import com.intellij.util.xmlb.annotations.Tag

/**
 * @author Marcin Bukowiecki
 */
@Tag("exported-breakpoints")
class XmlBreakpointsRoot {

    @Tag("commitID")
    var commitID = ""

    @Tag("branchName")
    var branchName = ""

    @Tag("breakpoints")
    var breakpoints: MutableList<DefaultBreakpoint> = mutableListOf()

}