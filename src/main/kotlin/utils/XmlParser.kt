package org.example.utils

import org.example.model.CallTree
import org.example.model.CallTreeNode
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class XmlParser(
    private val neo4jService: Neo4jService
) {
    fun constructCallTreeFromPath(filePath: String): CallTree {
        val rootNode = parseFromFilePath(filePath)
        val callTree = CallTree(rootNode)
        println("Call Tree Construction ready🥳")
        return callTree
    }

    private fun parseFromFilePath(xmlContent: String): CallTreeNode? {
        try {
            val xmlFile = File(xmlContent)
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(xmlFile)

            val root = doc.documentElement
            if (root.nodeName == "tree") {
                return parseNode(root, null, null)
            }
            return null
        }catch (e : Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun parseNode(nodeElement: Element, parentNode: CallTreeNode?, parentNodeId: Long?): CallTreeNode? {
        try {
            val nodeType = nodeElement.nodeName
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
                nodeType, leaf, className, methodName, methodSignature, time, count, selfTime, lineNumber, percent, parentNode
            )

            val currentNodeId = neo4jService.addNode(callTreeNode)!!
            callTreeNode.upgradeNodeId(currentNodeId)

            if (parentNodeId != null) {
                neo4jService.connectNode(parentNodeId, currentNodeId)
            }

            val nodeList = nodeElement.childNodes
            for (i in 0 until nodeList.length) {
                val tempNode = nodeList.item(i)
                if (tempNode.nodeType == Node.ELEMENT_NODE) {
                    parseNode(tempNode as Element, callTreeNode, currentNodeId)?.let {
                        callTreeNode.addChild(it)
                    }
                }
            }
            return callTreeNode
        }catch (e:Exception){
            e.printStackTrace()
            return null
        }
    }

}