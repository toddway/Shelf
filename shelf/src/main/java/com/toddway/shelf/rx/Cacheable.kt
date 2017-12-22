package com.toddway.shelf.rx

internal interface Cacheable<T> {

    var cache: T?
    val isCacheValid: Boolean
}