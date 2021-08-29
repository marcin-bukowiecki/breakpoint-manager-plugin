/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.ui

import com.bukowiecki.breakpoints.controller.TagValidatorProvider
import com.bukowiecki.breakpoints.utils.BreakpointManagerBundle
import com.intellij.ui.components.JBTextField
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import java.awt.Color
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * @author Marcin Bukowiecki
 */
class TagRowPanel(private val rowCount: Int,
                  private val tagValidatorProvider: TagValidatorProvider,
                  private val showRemoveButton: Boolean = true) : JPanel(GridLayoutManager(rowCount, 3)) {

    private val keyLabel = JLabel("Key")
    private val valueLabel = JLabel("Value")

    private val keyErrorLabel = JLabel()
    private val valueErrorLabel = JLabel()

    private val keyTextField = JBTextField(15)
    private val valueTextField = JBTextField(15)

    private val removeButton = JButton(BreakpointManagerBundle.message("breakpoints.tags.manage.remove"))

    init {
        keyErrorLabel.foreground = Color.RED
        valueErrorLabel.foreground = Color.RED

        initComponents()

        keyTextField.document.addDocumentListener(object : DocumentListener {

            override fun insertUpdate(e: DocumentEvent?) {
                onChange()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                onChange()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                onChange()
            }

            private fun onChange() {
                tagValidatorProvider.validate()
            }
        })

        valueTextField.document.addDocumentListener(object : DocumentListener {

            override fun insertUpdate(e: DocumentEvent?) {
                onChange()
            }

            override fun removeUpdate(e: DocumentEvent?) {
                onChange()
            }

            override fun changedUpdate(e: DocumentEvent?) {
                onChange()
            }

            private fun onChange() {
                tagValidatorProvider.validate()
            }
        })
    }

    fun getKeyText(): String = keyTextField.text

    fun getValueText(): String = valueTextField.text

    fun setKey(text: String) {
        this.keyTextField.text = text
    }

    fun setValue(value: String) {
        this.valueTextField.text = value
    }

    fun getRemoveButton() = removeButton

    private fun initComponents() {
        var gridConstraints: GridConstraints
        var nextRow = 2

        if (rowCount > 2) {
            gridConstraints = GridConstraints()
            gridConstraints.row = 0
            gridConstraints.column = 0
            gridConstraints.anchor = GridConstraints.ANCHOR_WEST

            add(
                keyLabel,
                gridConstraints,
                -1
            )

            gridConstraints = GridConstraints()
            gridConstraints.row = 0
            gridConstraints.column = 1
            gridConstraints.anchor = GridConstraints.ANCHOR_WEST

            add(
                valueLabel,
                gridConstraints,
                -1
            )

            gridConstraints = GridConstraints()
            gridConstraints.row = 1
            gridConstraints.column = 0
            gridConstraints.anchor = GridConstraints.ANCHOR_WEST

            add(
                keyErrorLabel,
                gridConstraints,
                -1
            )

            gridConstraints = GridConstraints()
            gridConstraints.row = 1
            gridConstraints.column = 1
            gridConstraints.anchor = GridConstraints.ANCHOR_WEST

            add(
                valueErrorLabel,
                gridConstraints,
                -1
            )
        } else {
            gridConstraints = GridConstraints()
            gridConstraints.row = 0
            gridConstraints.column = 0
            gridConstraints.anchor = GridConstraints.ANCHOR_WEST

            add(
                keyErrorLabel,
                gridConstraints,
                -1
            )

            gridConstraints = GridConstraints()
            gridConstraints.row = 0
            gridConstraints.column = 1
            gridConstraints.anchor = GridConstraints.ANCHOR_WEST

            add(
                valueErrorLabel,
                gridConstraints,
                -1
            )

            nextRow = 1
        }

        gridConstraints = GridConstraints()
        gridConstraints.row = nextRow
        gridConstraints.column = 0
        gridConstraints.anchor = GridConstraints.ANCHOR_WEST

        add(
            keyTextField,
            gridConstraints,
            -1
        )

        gridConstraints = GridConstraints()
        gridConstraints.row = nextRow
        gridConstraints.column = 1
        gridConstraints.anchor = GridConstraints.ANCHOR_CENTER

        add(
            valueTextField,
            gridConstraints,
            -1
        )

        if (showRemoveButton) {
            gridConstraints = GridConstraints()
            gridConstraints.row = nextRow
            gridConstraints.column = 2
            gridConstraints.anchor = GridConstraints.ANCHOR_EAST

            add(
                removeButton,
                gridConstraints,
                -1
            )
        }
    }

    fun reportKeyError(text: String) {
        keyErrorLabel.text = text
    }

    fun clearErrors() {
        keyErrorLabel.text = ""
        valueErrorLabel.text = ""
    }
}