package com.github.insanusmokrassar.AndroidFSMConfigConverter.utils.extensions

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser

private val parser = Parser.builder().build()
private val renderer = HtmlRenderer.builder().build()

fun String.asMarkdownToHTML(): String {
    return renderer.render(
            parser.parse(this)
    )
}
