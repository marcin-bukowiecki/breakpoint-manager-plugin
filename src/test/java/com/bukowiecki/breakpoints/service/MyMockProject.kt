/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.service

import com.intellij.mock.MockProject
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile

val myDisposable = Disposer.newDisposable()

/**
 * @author Marcin Bukowiecki
 */
class MyMockProject : MockProject(null, myDisposable) {

    fun getProjectDir(): VirtualFile {
        return MyLightVirtualFile("foo")
    }
}

class MyLightVirtualFile(name: String): LightVirtualFile(name) {

    override fun getParent(): VirtualFile {
        return LightVirtualFile("C:")
    }
}