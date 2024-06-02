package org.example.utils
import org.example.model.analysis.AnalysisData
import java.sql.Connection
import java.sql.DriverManager

class PostgreSQLService(private val dbName: String, private val dbUser: String, private val dbPassword: String) {
    private fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/$dbName", dbUser, dbPassword)
    }
    init {
        createSchema()
        println("schema created")
    }
    private fun createSchema(){
        val sql = """
            CREATE TABLE IF NOT EXISTS analysis_data (
                id VARCHAR(255) PRIMARY KEY,
                simple_name VARCHAR(255),
                qualified_name VARCHAR(255),
                kind VARCHAR(255),
                meta_src VARCHAR(255),
                labels TEXT[],  -- assuming labels are stored as an array of text
                visibility VARCHAR(255),
                source_text TEXT,
                doc_comment TEXT,
                layer VARCHAR(255)
            );
        """.trimIndent()
        getConnection().use{ conn ->
            val preparedStatement = conn.prepareStatement(sql)
            preparedStatement.executeUpdate()
        }
    }

    private fun insertOne(data: AnalysisData) {
        getConnection().use { conn ->
            val preparedStatement = conn.prepareStatement(
                "INSERT INTO analysis_data (id, simple_name, qualified_name, kind, meta_src, labels) VALUES (?, ?, ?, ?, ?, ?)"
            )
            preparedStatement.setString(1, data.id)
            preparedStatement.setString(2, data.simpleName)
            preparedStatement.setString(3, data.qualifiedName)
            preparedStatement.setString(4, data.kind)
            preparedStatement.setString(5, data.metaSrc)
            preparedStatement.setArray(6, conn.createArrayOf("text", data.labels.toTypedArray()))
            preparedStatement.executeUpdate()
        }
    }

    fun addDataList(dataList: List<AnalysisData>) {
        for (data in dataList) {
            insertOne(data)
        }
    }

    fun getDataById(id: String): AnalysisData? {
        getConnection().use { conn ->
            val preparedStatement = conn.prepareStatement(
                "SELECT * FROM analysis_data WHERE id = ?"
            )
            preparedStatement.setString(1, id)
            val resultSet = preparedStatement.executeQuery()

            if (resultSet.next()) {
                return AnalysisData(
                    query = "",
                    id = resultSet.getString("id"),
                    visibility = resultSet.getString("visibility"),
                    simpleName = resultSet.getString("simple_name"),
                    qualifiedName = resultSet.getString("qualified_name"),
                    kind = resultSet.getString("kind"),
                    sourceText = resultSet.getString("source_text"),
                    docComment = resultSet.getString("doc_comment"),
                    metaSrc = resultSet.getString("meta_src"),
                    layer = resultSet.getString("layer"),
                    labels = (resultSet.getArray("labels").array as Array<String>).toList()
                )
            }
            return null
        }
    }

    fun updateData(data: AnalysisData) {
        getConnection().use { conn ->
            val preparedStatement = conn.prepareStatement(
                "UPDATE analysis_data SET simple_name = ?, qualified_name = ?, kind = ?, meta_src = ?, labels = ? WHERE id = ?"
            )
            preparedStatement.setString(1, data.simpleName)
            preparedStatement.setString(2, data.qualifiedName)
            preparedStatement.setString(3, data.kind)
            preparedStatement.setString(4, data.metaSrc)
            preparedStatement.setArray(5, conn.createArrayOf("text", data.labels.toTypedArray()))
            preparedStatement.setString(6, data.id)
            preparedStatement.executeUpdate()
        }
    }

    fun deleteData(id: String) {
        getConnection().use { conn ->
            val preparedStatement = conn.prepareStatement(
                "DELETE FROM analysis_data WHERE id = ?"
            )
            preparedStatement.setString(1, id)
            preparedStatement.executeUpdate()
        }
    }
}

fun main() {
    try {
        val dbService = PostgreSQLService("analysis_db", "postuser", "testpassword")
        // Use dbService to interact with the database
    } catch (e: Exception) {
        println("Failed to connect or operate on the database: ${e.message}")
    }
}