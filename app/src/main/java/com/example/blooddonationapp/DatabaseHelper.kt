package com.example.blooddonationapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "BloodDonationApp2.db"
        const val DATABASE_VERSION = 3

        const val TABLE_USERS = "Users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_BLOOD_TYPE = "blood_type"
        const val COLUMN_LOCATION = "location"
        const val COLUMN_PHONE = "phone"
        const val COLUMN_IMAGE = "image"

        const val TABLE_REQUESTS = "requests"
        const val COLUMN_REQUEST_ID = "request_id"
        const val COLUMN_REQUESTER_ID = "requester_id"
        const val COLUMN_REQUESTER_NAME = "requester_name"
        const val COLUMN_REQUEST_BLOOD_TYPE = "blood_type"
        const val COLUMN_REQUEST_LOCATION = "location"
        const val COLUMN_MESSAGE = "message"
        const val COLUMN_MOBILE = "mobile"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_STATUS = "status"
        const val COLUMN_REQUEST_IMAGE = "request_image"
        const val COLUMN_DONOR_ID = "donor_id" // Add the donorId column
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_BLOOD_TYPE TEXT,
                $COLUMN_LOCATION TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_IMAGE BLOB
            )
        """

        val createTableRequest = """
            CREATE TABLE $TABLE_REQUESTS (
                $COLUMN_REQUEST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_REQUESTER_ID INTEGER NOT NULL,
                $COLUMN_REQUESTER_NAME TEXT DEFAULT 'Unknown', 
                $COLUMN_REQUEST_BLOOD_TYPE TEXT DEFAULT 'N/A',
                $COLUMN_REQUEST_LOCATION TEXT DEFAULT 'Unknown Location',
                $COLUMN_MESSAGE TEXT DEFAULT '',
                $COLUMN_MOBILE TEXT DEFAULT 'N/A',
                $COLUMN_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP,
                $COLUMN_STATUS TEXT DEFAULT 'Pending',
                $COLUMN_REQUEST_IMAGE BLOB,
                $COLUMN_DONOR_ID INTEGER DEFAULT NULL
            )
        """

        db?.execSQL(createUsersTable)
        db?.execSQL(createTableRequest)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db?.execSQL("ALTER TABLE $TABLE_REQUESTS ADD COLUMN $COLUMN_DONOR_ID INTEGER DEFAULT NULL")
        }
    }
}
