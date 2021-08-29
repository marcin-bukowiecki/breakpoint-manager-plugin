/*
 * Copyright 2021 Marcin Bukowiecki.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

package com.bukowiecki.breakpoints.inlay

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.FontInfo
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.SimpleColoredText
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.paint.EffectPainter
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.UIUtil
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.impl.inline.InlineDebugRenderer
import com.intellij.xdebugger.ui.DebuggerColors
import java.awt.*
import java.lang.ref.WeakReference

/**
 * @author Marcin Bukowiecki
 */
class BreakpointManagerElementRenderer(
    private val text: String,
    private val margin: Int,
    val breakpointReference: WeakReference<XLineBreakpoint<*>>,
    val inlayType: BreakpointManagerInlayType
) : EditorCustomElementRenderer {

    private val backgroundAlpha = 0.55f
    private val myPresentation: SimpleColoredText
    private var isHovered = false
    private var myTextStartXCoordinate = 0

    init {
        myPresentation = getPresentation()
    }

    private fun getPresentation(): SimpleColoredText {
        val presentation = SimpleColoredText()
        presentation.insert(0, text, SimpleTextAttributes(Font.ITALIC, Color(102, 109, 117)))
        return presentation
    }

    @Suppress("unused")
    fun onMouseExit(inlay: Inlay<*>, event: EditorMouseEvent) {
        setHovered(false, inlay, event.editor as EditorEx)
    }

    @Suppress("unused")
    fun onMouseMove(inlay: Inlay<*>, event: EditorMouseEvent) {
        val editorEx = event.editor as EditorEx
        if (event.mouseEvent.x >= myTextStartXCoordinate) {
            setHovered(true, inlay, editorEx)
        } else {
            setHovered(false, inlay, editorEx)
        }
    }

    private fun setHovered(active: Boolean, inlay: Inlay<*>, editorEx: EditorEx) {
        val oldState: Boolean = isHovered
        isHovered = active
        val cursor = if (active) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else null
        editorEx.setCustomCursor(InlineDebugRenderer::class.java, cursor)
        if (oldState != active) {
            inlay.update()
        }
    }

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        return getInlayTextWidth(inlay)
    }

    private fun getInlayTextWidth(inlay: Inlay<*>): Int {
        val font = getFont(inlay.editor)
        val text: String = myPresentation.toString()
        return getFontMetrics(font, inlay.editor).stringWidth(text + InlineDebugRenderer.INDENT)
    }

    override fun paint(inlay: Inlay<*>, g: Graphics, r: Rectangle, textAttributes: TextAttributes) {
        val editor = inlay.editor as EditorImpl
        val inlineAttributes = getAttributes(editor)
        if (inlineAttributes == null || inlineAttributes.foregroundColor == null) return
        val font = getFont(editor)
        g.font = font
        val metrics = getFontMetrics(font, editor)
        val gap = 1 //(r.height < fontMetrics.lineHeight + 2) ? 1 : 2;
        val backgroundColor = inlineAttributes.backgroundColor
        var curX = r.x + metrics.charWidth(' ')
        if (backgroundColor != null) {
            val alpha = backgroundAlpha
            val config = GraphicsUtil.setupAAPainting(g)
            GraphicsUtil.paintWithAlpha(g, alpha)
            g.color = backgroundColor
            g.fillRoundRect(
                curX + margin,
                r.y + gap,
                r.width - 2 * margin - metrics.charWidth(' '),
                r.height - gap * 2,
                6,
                6
            )
            config.restore()
        }
        curX += 2 * margin
        myTextStartXCoordinate = curX
        for (i in myPresentation.texts.indices) {
            var curText: String = myPresentation.texts[i]
            if (i == 0) {
                curText += " "
            }
            val attr: SimpleTextAttributes = myPresentation.attributes[i]
            val fgColor = attr.fgColor
            g.color = fgColor
            g.drawString(curText, curX, r.y + inlay.editor.ascent)
            curX += metrics.stringWidth(curText)
        }
        paintEffects(g, r, editor, inlineAttributes, font, metrics)
    }

    private fun getFontMetrics(font: Font, editor: Editor): FontMetrics {
        return FontInfo.getFontMetrics(font, FontInfo.getFontRenderContext(editor.contentComponent))
    }

    private fun getFont(editor: Editor): Font {
        val colorsScheme = editor.colorsScheme
        val attributes = editor.colorsScheme.getAttributes(DebuggerColors.INLINED_VALUES_EXECUTION_LINE)
        val fontStyle = attributes?.fontType ?: Font.PLAIN
        return UIUtil.getFontWithFallback(colorsScheme.getFont(EditorFontType.forJavaStyle(fontStyle)))
    }

    private fun getAttributes(editor: Editor): TextAttributes? {
        val key =
            if (isInExecutionPointHighlight()) DebuggerColors.INLINED_VALUES_EXECUTION_LINE else DebuggerColors.INLINED_VALUES
        val scheme = editor.colorsScheme
        return scheme.getAttributes(key)
    }

    private fun paintEffects(
        g: Graphics,
        r: Rectangle,
        editor: EditorImpl,
        inlineAttributes: TextAttributes,
        font: Font,
        metrics: FontMetrics
    ) {
        val effectColor = inlineAttributes.effectColor
        val effectType = inlineAttributes.effectType
        if (effectColor != null) {
            g.color = effectColor
            val g2d = g as Graphics2D
            val xStart = r.x
            val xEnd = r.x + r.width
            val y = r.y + metrics.ascent
            when (effectType) {
                EffectType.LINE_UNDERSCORE -> {
                    EffectPainter.LINE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.descent, font)
                }
                EffectType.BOLD_LINE_UNDERSCORE -> {
                    EffectPainter.BOLD_LINE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.descent, font)
                }
                EffectType.STRIKEOUT -> {
                    EffectPainter.STRIKE_THROUGH.paint(g2d, xStart, y, xEnd - xStart, editor.charHeight, font)
                }
                EffectType.WAVE_UNDERSCORE -> {
                    EffectPainter.WAVE_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.descent, font)
                }
                EffectType.BOLD_DOTTED_LINE -> {
                    EffectPainter.BOLD_DOTTED_UNDERSCORE.paint(g2d, xStart, y, xEnd - xStart, metrics.descent, font)
                }
            }
        }
    }

    private fun isInExecutionPointHighlight(): Boolean {
        return false
    }
}