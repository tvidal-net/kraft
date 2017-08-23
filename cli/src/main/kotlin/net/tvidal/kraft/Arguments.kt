package net.tvidal.kraft

import joptsimple.ArgumentAcceptingOptionSpec
import joptsimple.OptionParser

private const val SEPARATOR = ','

fun OptionParser.option(description: String, vararg options: String) =
  acceptsAll(options.toList(), description)!!

private inline fun <reified T> OptionParser.typeArgument(description: String, vararg options: String) =
  option(description, *options)
    .withRequiredArg()
    .ofType(T::class.java)!!

fun OptionParser.stringArgument(description: String, vararg options: String) =
  typeArgument<String>(description, *options)

fun OptionParser.longArgument(description: String, vararg options: String) =
  typeArgument<Long>(description, *options)

fun OptionParser.intArgument(description: String, vararg options: String) =
  typeArgument<Int>(description, *options)

fun ArgumentAcceptingOptionSpec<*>.acceptsMultipleValues() =
  withValuesSeparatedBy(SEPARATOR)
