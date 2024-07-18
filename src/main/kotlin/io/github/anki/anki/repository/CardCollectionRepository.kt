package io.github.anki.anki.repository

import io.github.anki.anki.models.CardCollection


interface CardCollectionRepository {

//    fun createCollection()
    fun getCardCollections(): Collection<CardCollection>
//    fun getCollection()
//    fun updateCollection()
//    fun deleteCollection()
}
