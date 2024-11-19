package io.github.anki.anki.configuration


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class FileDefaultTemplate(val path: String)