/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.controller

/**
 * @author Marcin Bukowiecki
 */
interface TagValidatorProvider {

    fun validate(): Boolean
}