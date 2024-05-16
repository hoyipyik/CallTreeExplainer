package org.example.utils

import org.example.model.CallTreeNode
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.TransactionContext

class Neo4jService(url: String, username: String, password: String) {
    private val driver: Driver = GraphDatabase.driver(url, AuthTokens.basic(username, password))

    fun close() {
        driver.close()
    }

    fun deleteAll() {
        execute { tx ->
            tx.run("MATCH (n:CallTreeNode) DETACH DELETE n")
        }
        println("Neo4j has deleted all old nodes")
    }

    fun saveCallTree(root: CallTreeNode) {
        execute { tx ->
            val rootNodeId = createNode(tx, root)
            addChildren(tx, rootNodeId, root)
        }
    }

    fun addNode(node: CallTreeNode): Long? {
        try {
            return driver.session().use { session ->
                session.executeWrite { tx ->
                    createNode(tx, node)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    fun connectNode(parentId: Long, childId: Long) {
        execute { tx ->
            tx.run(
                "MATCH (p), (c) WHERE id(p) = ${'$'}parentId AND id(c) = ${'$'}childId CREATE (p)-[:CALL]->(c)",
                mapOf("parentId" to parentId, "childId" to childId)
            )
        }
    }

    fun upgradeNodeExplanation(nodeId: Long, newExplanation: String) {
        println("Upgrade explanation for $nodeId, $newExplanation")
        execute { tx ->
            tx.run(
                "MATCH (n) WHERE id(n) = \$nodeId SET n.explanation = \$newExplanation RETURN n",
                mapOf("nodeId" to nodeId, "newExplanation" to newExplanation)
            )
        }
    }


    private fun createNode(tx: TransactionContext, node: CallTreeNode): Long {
        val query = """
            CREATE (n:CallTreeNode {
                nodeType: ${'$'}nodeType, 
                leaf: ${'$'}leaf, 
                className: ${'$'}className, 
                methodName: ${'$'}methodName,
                methodSignature: ${'$'}methodSignature,
                time: ${'$'}time,
                count: ${'$'}count,
                selfTime: ${'$'}selfTime,
                lineNumber: ${'$'}lineNumber,
                percent: ${'$'}percent,
                explanation: ${'$'}explanation,
                parentNode: ${'$'}parentNode,
                childNodes: ${'$'}childNodes
            }) RETURN id(n)
        """.trimIndent()

        val params = mapOf("nodeType" to node.nodeType,
            "leaf" to node.leaf,
            "className" to node.className,
            "methodName" to node.methodName,
            "methodSignature" to node.methodSignature,
            "time" to node.time,
            "count" to node.count,
            "selfTime" to node.selfTime,
            "lineNumber" to node.lineNumber,
            "percent" to node.percent,
            "explanation" to node.explanation,
            "parentNode" to (node.parent?.className?.takeIf { it.isNotEmpty() } ?: "root"),
            "childNodes" to node.children.map { it.className })

        return tx.run(query, params).single()[0].asLong()
    }

    private fun addChildren(tx: TransactionContext, parentId: Long, parentNode: CallTreeNode) {
        parentNode.children.forEach { child ->
            val childId = createNode(tx, child)
            tx.run(
                "MATCH (p), (c) WHERE id(p) = ${'$'}parentId AND id(c) = ${'$'}childId CREATE (p)-[:CALL]->(c)",
                mapOf("parentId" to parentId, "childId" to childId)
            )
            addChildren(tx, childId, child)
        }
    }

    private fun execute(operation: (tx: TransactionContext) -> Unit) {
        try {
            driver.session().use { session ->
                session.executeWrite { tx ->
                    operation(tx)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}