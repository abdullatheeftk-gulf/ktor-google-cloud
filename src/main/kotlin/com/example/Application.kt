package com.example

import com.example.data.DatabaseFactory
import com.example.plugins.*
import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.partialcontent.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(PartialContent)
    install(AutoHeadResponse)
    configureSerialization()
    //configureDatabases()
    DatabaseFactory.init()
    configureTemplating()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureRouting()
}
