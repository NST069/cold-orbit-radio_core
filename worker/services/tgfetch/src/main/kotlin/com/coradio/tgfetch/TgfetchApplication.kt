package com.coradio.tgfetch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TgfetchApplication

fun main(args: Array<String>) {
    runApplication<TgfetchApplication>(*args)
}
