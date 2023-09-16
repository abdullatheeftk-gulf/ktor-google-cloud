package com.example.data

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(){
        val driverClassName = "com.mysql.cj.jdbc.Driver"

            //"org.postgresql.Driver"
        val jdbcURL = "jdbc:mysql:///latheef-db?cloudSqlInstance=latheef-node-project:us-central1:mysql-instance&socketFactory=com.google.cloud.sql.mysql.SocketFactory"


            //"jdbc:postgresql:///sample-db?cloudSqlInstance=latheef-node-project:us-central1:ktor-google-instance&socketFactory=com.google.cloud.sql.postgres.SocketFactory&user=latheef&password=palotil"
        //val database = Database.connect(jdbcURL,driverClassName)

        val database = Database.connect(jdbcURL,driverClassName,"latheef-user","palotil")

        transaction (database){
            SchemaUtils.create(Notes)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}