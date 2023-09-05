package com.example.data

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id:Int,
    val title:String,
    val description:String
)
