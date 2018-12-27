package com.toddway.shelf.rx

import com.toddway.shelf.ShelfItem

/**
 * Created by tway on 4/24/16.
 * updated nschwermann 12/21/17
 */
internal class Shelfable<T>(internal var item: ShelfItem, internal var type: Class<T>) : Cacheable<T> {

    override var cache: T?
        get() = try{
            item.get(type)
        }catch(e : Exception){
            item.clear()
            null
        }
        set(t) {
            item.put(t)
        }

    override val isCacheValid: Boolean
        get() = !item.isOlderThanLifetime

    /**
     * @return The [ShelfItem] containing the cache
     */
    fun item(): ShelfItem {
        return item
    }

}