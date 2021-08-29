/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service

import com.bukowiecki.breakpoints.model.ProjectWrapper
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile

/**
 * @author Marcin Bukowiecki
 */
class MyProjectWrapper(private val mockProject: MyMockProject) : ProjectWrapper(mockProject) {

    override fun guessProjectDir(): VirtualFile {
        return mockProject.getProjectDir()
    }
}