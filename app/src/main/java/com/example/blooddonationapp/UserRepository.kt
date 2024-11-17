package com.example.blooddonationapp

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class UserRepository(private val dbHelper: DatabaseHelper) {

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val resizedBitmap = resizeBitmap(bitmap, 500, 500)
        val stream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 80, stream)
        return stream.toByteArray()
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        var newWidth = maxWidth
        var newHeight = maxHeight

        if (width > height) {
            newHeight = (newWidth / ratioBitmap).toInt()
        } else {
            newWidth = (newHeight * ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun insertUser(
        name: String, email: String, password: String, bloodType: String,
        location: String, phone: String, image: Bitmap
    ): Long {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NAME, name)
            put(DatabaseHelper.COLUMN_EMAIL, email)
            put(DatabaseHelper.COLUMN_PASSWORD, password)
            put(DatabaseHelper.COLUMN_BLOOD_TYPE, bloodType)
            put(DatabaseHelper.COLUMN_LOCATION, location)
            put(DatabaseHelper.COLUMN_PHONE, phone)
            put(DatabaseHelper.COLUMN_IMAGE, bitmapToByteArray(image))
        }
        return db.insert(DatabaseHelper.TABLE_USERS, null, contentValues)
    }
    fun getUserImageById(userId: Long): Bitmap? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        var userImage: Bitmap? = null

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_IMAGE),
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE))
            userImage = byteArrayToBitmap(imageByteArray)
        }

        cursor.close()
        return userImage
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun getAllUsers(): List<User> {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val userList = mutableListOf<User>()

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_EMAIL,
                DatabaseHelper.COLUMN_PASSWORD,
                DatabaseHelper.COLUMN_BLOOD_TYPE,
                DatabaseHelper.COLUMN_LOCATION,
                DatabaseHelper.COLUMN_PHONE,
                DatabaseHelper.COLUMN_IMAGE
            ),
            null, null, null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL))
                val password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD))
                val bloodType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BLOOD_TYPE))
                val location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE))
                val imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE))
                val bitmap = byteArrayToBitmap(imageByteArray)

                userList.add(User(id, name, email, password, bloodType, location, phone, bitmap))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return userList
    }

    fun getUserByEmailAndPassword(email: String, password: String): User? {
        val db: SQLiteDatabase = dbHelper.readableDatabase
        var user: User? = null

        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(
                DatabaseHelper.COLUMN_USER_ID,
                DatabaseHelper.COLUMN_NAME,
                DatabaseHelper.COLUMN_EMAIL,
                DatabaseHelper.COLUMN_PASSWORD,
                DatabaseHelper.COLUMN_BLOOD_TYPE,
                DatabaseHelper.COLUMN_LOCATION,
                DatabaseHelper.COLUMN_PHONE,
                DatabaseHelper.COLUMN_IMAGE
            ),
            "${DatabaseHelper.COLUMN_EMAIL} = ? AND ${DatabaseHelper.COLUMN_PASSWORD} = ?",
            arrayOf(email, password),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME))
            val emailDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL))
            val passwordDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD))
            val bloodType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BLOOD_TYPE))
            val location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE))
            val imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE))
            val bitmap = byteArrayToBitmap(imageByteArray)

            user = User(id, name, emailDb, passwordDb, bloodType, location, phone, bitmap)
        }

        cursor.close()
        return user
    }
}
