package com.example.contactpermission.databases

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.contactpermission.models.ContactType

class MyDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION),
    MyDbInterface {
    companion object {
        const val DB_NAME = "db_name"
        const val DB_VERSION = 1
        const val ID = "id"
        const val CONTACT_TABLE = "contact_table_3"
        const val CONTACT_NAME = "contact_name"
        const val CONTACT_SURNAME = "contact_surname"
        const val CONTACT_NUMBER = "contact_number"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
        val contactQuery =
            "create table $CONTACT_TABLE(ID integer not null primary key autoincrement unique, $CONTACT_NAME text not null, $CONTACT_SURNAME text not null, $CONTACT_NUMBER text not null)"
        p0?.execSQL(contactQuery)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}
    override fun addContact(contactType: ContactType) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(CONTACT_NAME, contactType.name)
        contentValues.put(CONTACT_SURNAME, contactType.surname)
        contentValues.put(CONTACT_NUMBER, contactType.number)
        database.insert(CONTACT_TABLE, null, contentValues)
        database.close()

    }

    override fun getAllContacts(): ArrayList<ContactType> {
        val database = this.writableDatabase
        val cursor = database.rawQuery("select * from $CONTACT_TABLE", null)
        val list = ArrayList<ContactType>()
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    ContactType(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    override fun deleteContact(contactType: ContactType) {
        val database = this.writableDatabase
        database.delete(CONTACT_TABLE, "id=?", arrayOf(contactType.id.toString()))
        database.close()
        database.close()
    }

    override fun editContact(contactType: ContactType): Int {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(CONTACT_NAME, contactType.name)
        contentValues.put(CONTACT_SURNAME, contactType.surname)
        contentValues.put(CONTACT_NUMBER, contactType.number)

        return database.update(
            CONTACT_TABLE,
            contentValues,
            "$ID=?",
            arrayOf(contactType.id.toString())
        )
    }
}


interface MyDbInterface {
    fun addContact(contactType: ContactType)
    fun getAllContacts(): ArrayList<ContactType>
    fun deleteContact(contactType: ContactType)
    fun editContact(contactType: ContactType): Int

}