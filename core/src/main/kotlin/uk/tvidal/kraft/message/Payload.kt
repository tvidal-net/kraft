package uk.tvidal.kraft.message

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

@Retention(RUNTIME)
@Target(PROPERTY, VALUE_PARAMETER)
annotation class Payload
