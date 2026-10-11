package com.signalzero.app.data.repository

import com.signalzero.app.data.local.dao.ContactDao
import com.signalzero.app.data.local.entity.ContactEntity
import com.signalzero.app.data.model.TrustedContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: Flow<List<TrustedContact>> = contactDao.getAllContacts().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getContact(id: String): TrustedContact? = contactDao.getContactById(id)?.toDomain()

    suspend fun getPrimaryContact(): TrustedContact? = contactDao.getPrimaryContact()?.toDomain()

    suspend fun saveContact(contact: TrustedContact) {
        if (contact.isPrimary) {
            contactDao.clearPrimaryFlags()
        }
        contactDao.insertContact(ContactEntity.fromDomain(contact))
    }

    suspend fun deleteContact(contact: TrustedContact) {
        contactDao.deleteContact(ContactEntity.fromDomain(contact))
    }

    suspend fun setPrimary(contactId: String) {
        contactDao.clearPrimaryFlags()
        contactDao.setPrimaryContact(contactId)
    }

    suspend fun insertContact(contact: TrustedContact) = saveContact(contact)
    suspend fun setPrimaryContact(contactId: String) = setPrimary(contactId)
}
