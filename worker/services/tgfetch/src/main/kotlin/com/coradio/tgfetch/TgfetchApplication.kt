package com.coradio.tgfetch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TgfetchApplication

fun main(args: Array<String>) {
    runApplication<TgfetchApplication>(*args)
}
