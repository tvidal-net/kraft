package uk.tvidal.kraft.message

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.PROPERTY

@Retention(RUNTIME)
@Target(PROPERTY)
annotation class Payload
