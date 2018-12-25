package uk.tvidal.kraft

interface ChainNode<T : ChainNode<T>> {
    var next: T?
    var prev: T?
}

fun <T : ChainNode<T>> createLinks(items: Iterable<T>) {
    var prev: T? = null
    for (item in items) {
        if (prev != null) prev.next = item
        item.prev = prev
        prev = item
    }
}
