package net.tvidal.kraft

class RaftError(message: String? = null, cause: Throwable? = null) : Error(message, cause)
