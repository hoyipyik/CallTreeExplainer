package org.example.utils

import org.example.model.explaintree.CallTreeNode
import org.neo4j.driver.*

class Neo4jService(url: String, username: String, password: String, dbName: String) {
    private val driver: Driver = GraphDatabase.driver(url, AuthTokens.basic(username, password))
    private val databaseName = dbName

    fun close() {
        driver.close()
    }

    fun createDatabase(databaseName: String = this.databaseName) {
        try {
            driver.session().use { session ->
                session.executeWrite { tx ->
                    val result = tx.run("CREATE DATABASE $databaseName")
                    result.consume() // Ensure you consume the result if it's needed
                    "Database $databaseName created successfully"
                }.also { println(it) }
            }
        } catch (e: Exception) {
            println(e.message)
            println("database create failure")
        }
    }

    fun deleteAll() {
        try {
            driver.session().use { session ->
                session.executeWrite { tx ->
                    val result = tx.run("MATCH (n:CallTreeNode) DETACH DELETE n")
                    result.consume()
                }
            }
            println("Neo4j has deleted all old nodes")
        }catch (e: Exception){
            println(e.message)
            println("Delete all failed")
        }
    }

    fun dropDatabaseByName(databaseName: String = this.databaseName) {
            execute(databaseName) { tx ->
                try {
                    tx.run("DROP DATABASE $databaseName")
                    println("Database $databaseName have been dropped")
                }catch (e: Exception){
                    println(e.message)
                    println("Drop database by name failed")
                }
            }
    }

    // save the whole call tree from empty
    fun saveCallTree(root: CallTreeNode, databaseName: String = this.databaseName) {
        execute(databaseName) { tx ->
            val rootNodeId = createNode(tx, root)
            addChildren(tx, rootNodeId, root)
        }
    }

    fun addNode(node: CallTreeNode, databaseName: String = this.databaseName): Long? {
        try {
            return if(databaseName.isEmpty()){
                driver.session().use { session ->
                    session.executeWrite { tx ->
                        createNode(tx, node)
                    }
                }
            }else{
                driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
                    session.executeWrite { tx ->
                        createNode(tx, node)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    fun connectNode(parentId: Long, childId: Long, databaseName: String = this.databaseName) {
        execute(databaseName) { tx ->
            tx.run(
                "MATCH (p), (c) WHERE id(p) = ${'$'}parentId AND id(c) = ${'$'}childId CREATE (p)-[:CALL]->(c)",
                mapOf("parentId" to parentId, "childId" to childId)
            )
        }
    }

    fun upgradeNodeExplanation(nodeId: Long, newExplanation: String, databaseName: String = this.databaseName) {
        println("Upgrade explanation for $nodeId, $newExplanation")
        execute(databaseName) { tx ->
            tx.run(
                "MATCH (n) WHERE id(n) = \$nodeId SET n.explanation = \$newExplanation RETURN n",
                mapOf("nodeId" to nodeId, "newExplanation" to newExplanation)
            )
        }
    }

    fun upgradeNodePrompt(nodeId: Long, newPrompt: String, databaseName: String = this.databaseName) {
        execute(databaseName) { tx ->
            tx.run(
                "MATCH (n) WHERE id(n) = \$nodeId SET n.prompt = \$newPrompt RETURN n",
                mapOf("nodeId" to nodeId, "newPrompt" to newPrompt)
            )
        }
    }

    fun upgradeNodeSourceCode(nodeId: Long, newSourceCode: String, databaseName: String = this.databaseName) {
        execute(databaseName) { tx ->
            tx.run(
                "MATCH (n) WHERE id(n) = \$nodeId SET n.sourceCode = \$newSourceCode RETURN n",
                mapOf("nodeId" to nodeId, "newSourceCode" to newSourceCode)
            )
        }
    }

    fun upgradeChildNodes(nodeId: Long, childNodes: List<String>, databaseName: String = this.databaseName) {
        execute(databaseName) { tx ->
            tx.run(
                "MATCH (n) WHERE id(n) = \$nodeId SET n.childNodes = \$childNodes RETURN n",
                mapOf("nodeId" to nodeId, "childNodes" to childNodes)
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
                childIndex: ${'$'}childIndex,
                prompt: ${'$'}prompt,
                explanation: ${'$'}explanation,
                sourceCode: ${'$'}sourceCode,
                parentNode: ${'$'}parentNode,
                layer: ${'$'}layer,
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
            "childIndex" to node.childIndex,
            "prompt" to node.prompt,
            "explanation" to node.explanation,
            "sourceCode" to node.sourceCode,
            "parentNode" to (node.parent?.className?.takeIf { it.isNotEmpty() } ?: "root"),
            "layer" to node.layer,
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

    private fun execute(databaseName:String = this.databaseName, operation: (tx: TransactionContext) -> Unit) {
        try {
            if(databaseName.isEmpty()) {
                driver.session().use { session ->
                    session.executeWrite { tx ->
                        operation(tx)
                    }
                }
            }else {
                driver.session(SessionConfig.forDatabase(databaseName)).use { session ->
                    session.executeWrite { tx ->
                        operation(tx)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}