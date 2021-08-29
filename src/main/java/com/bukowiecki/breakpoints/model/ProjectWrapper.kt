/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.model

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile

/**
 * @author Marcin Bukowiecki
 */
open class ProjectWrapper(val project: Project) {

    open fun guessProjectDir(): VirtualFile? {
        return project.guessProjectDir()
    }
}