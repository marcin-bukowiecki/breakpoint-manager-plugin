/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Tag
import org.jdom.Element

/**
 * @author Marcin Bukowiecki
 */
@Tag("exported-default-breakpoint")
class DefaultBreakpoint {

    @Attribute("properties-canonical-name")
    var propertiesCanonicalName: String? = ""

    @Attribute("type-canonical-name")
    var typeCanonicalName: String? = ""

    @Attribute("state-canonical-name")
    var stateCanonicalName: String? = ""

    @Attribute("type-id")
    var typeID: String? = ""

    @Attribute("breakpoint-canonical-name")
    var breakpointCanonicalName: String? = ""

    var state: Element? = null

    var properties: Element? = null

    var metadata: Element? = null
}