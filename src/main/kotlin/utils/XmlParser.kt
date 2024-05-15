package org.example.utils

import org.example.model.CallTree
import org.example.model.CallTreeNode
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class XmlParser() {
    fun constructCallTreeFromPath(filePath: String): CallTree {
        val rootNode = parseFromFilePath(filePath)
        val callTree = CallTree(rootNode)
        return callTree
    }
    fun parseFromFilePath(xmlContent: String): CallTreeNode? {
        val xmlFile = File(xmlContent)
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(xmlFile)

        val root = doc.documentElement
        if (root.nodeName == "tree") {
            return parseNode(root)
        }
        return null
    }
    private fun parseNode(nodeElement: Element): CallTreeNode {
        val leaf = nodeElement.getAttribute("leaf").toBoolean()
        val className = nodeElement.getAttribute("class")
        val methodName = nodeElement.getAttribute("methodName")
        val methodSignature = nodeElement.getAttribute("methodSignature")
        val time = nodeElement.getAttribute("time").toIntOrNull() ?: 0
        val count = nodeElement.getAttribute("count").toIntOrNull() ?: 0
        val selfTime = nodeElement.getAttribute("selfTime").toIntOrNull() ?: 0
        val lineNumber = nodeElement.getAttribute("lineNumber").toIntOrNull() ?: -1
        val percent = nodeElement.getAttribute("percent").toDoubleOrNull() ?: 0.0

        val callTreeNode = CallTreeNode(
            leaf, className, methodName, methodSignature, time, count, selfTime, lineNumber, percent
        )

        val nodeList = nodeElement.childNodes
        for (i in 0 until nodeList.length) {
            val tempNode = nodeList.item(i)
            if (tempNode.nodeType == Node.ELEMENT_NODE) {
                callTreeNode.children.add(parseNode(tempNode as Element))
            }
        }
        return callTreeNode
    }

}