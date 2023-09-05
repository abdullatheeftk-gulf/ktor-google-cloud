package com.example.data

import org.jetbrains.exposed.sql.Table

object Notes : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title",100)
    val description = varchar("description", length = 1000)


    override val primaryKey = PrimaryKey(id)
}

