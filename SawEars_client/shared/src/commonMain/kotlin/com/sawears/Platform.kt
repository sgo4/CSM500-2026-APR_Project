package com.sawears

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform