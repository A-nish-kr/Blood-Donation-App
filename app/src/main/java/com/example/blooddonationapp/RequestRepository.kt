package com.example.blooddonationapp

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class RequestRepository(private val dbHelper: DatabaseHelper) {

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
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

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun insertRequest(
        requesterId: Long,
        requesterName: String,
        donorId: Long?,
        bloodType: String,
        location: String,
        message: String,
        mobile: String,
        image: Bitmap
    ): Long {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_REQUESTER_ID, requesterId)
            put(DatabaseHelper.COLUMN_REQUESTER_NAME, requesterName)
            put(DatabaseHelper.COLUMN_REQUEST_BLOOD_TYPE, bloodType)
            put(DatabaseHelper.COLUMN_REQUEST_LOCATION, location)
            put(DatabaseHelper.COLUMN_MESSAGE, message)
            put(DatabaseHelper.COLUMN_MOBILE, mobile)
            put(DatabaseHelper.COLUMN_REQUEST_IMAGE, bitmapToByteArray(image))
            donorId?.let { put(DatabaseHelper.COLUMN_DONOR_ID, it) }
        }
        return db.insert(DatabaseHelper.TABLE_REQUESTS, null, contentValues).also { db.close() }
    }

    fun getAllRequests(): List<Request> {
        val db = dbHelper.readableDatabase
        val requestList = mutableListOf<Request>()
        val query = """
            SELECT r.${DatabaseHelper.COLUMN_REQUEST_ID},
                   r.${DatabaseHelper.COLUMN_REQUESTER_ID},
                   r.${DatabaseHelper.COLUMN_REQUESTER_NAME},
                   r.${DatabaseHelper.COLUMN_REQUEST_BLOOD_TYPE},
                   r.${DatabaseHelper.COLUMN_REQUEST_LOCATION},
                   r.${DatabaseHelper.COLUMN_MESSAGE},
                   r.${DatabaseHelper.COLUMN_MOBILE},
                   r.${DatabaseHelper.COLUMN_CREATED_AT},
                   r.${DatabaseHelper.COLUMN_STATUS},
                   r.${DatabaseHelper.COLUMN_REQUEST_IMAGE},
                   r.${DatabaseHelper.COLUMN_DONOR_ID}
            FROM ${DatabaseHelper.TABLE_REQUESTS} r
            JOIN ${DatabaseHelper.TABLE_USERS} u
            ON r.${DatabaseHelper.COLUMN_REQUESTER_ID} = u.${DatabaseHelper.COLUMN_USER_ID}
        """
        val cursor = db.rawQuery(query, null)

        cursor.use {
            if (cursor.moveToFirst()) {
                do {
                    val requestId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_ID))
                    val requesterId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUESTER_ID))
                    val requesterName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUESTER_NAME))
                    val bloodType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_BLOOD_TYPE))
                    val location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_LOCATION))
                    val message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MESSAGE))
                    val mobile = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MOBILE))
                    val createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
                    val status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS))
                    val imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_IMAGE))
                    val imageBitmap = byteArrayToBitmap(imageByteArray)
                    val donorId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONOR_ID)).takeIf { it != 0L }

                    requestList.add(
                        Request(
                            requestId, requesterId, requesterName, donorId, bloodType,
                            location, message, createdAt, mobile, status, imageBitmap
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        db.close()
        return requestList
    }

    fun updateRequest(
        requestId: Long,
        bloodType: String,
        location: String,
        message: String,
        status: String,
        mobile: String,
        image: Bitmap
    ): Int {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_REQUEST_BLOOD_TYPE, bloodType)
            put(DatabaseHelper.COLUMN_REQUEST_LOCATION, location)
            put(DatabaseHelper.COLUMN_MESSAGE, message)
            put(DatabaseHelper.COLUMN_STATUS, status)
            put(DatabaseHelper.COLUMN_MOBILE, mobile)
            put(DatabaseHelper.COLUMN_REQUEST_IMAGE, bitmapToByteArray(image))
        }
        val rowsUpdated = db.update(
            DatabaseHelper.TABLE_REQUESTS,
            contentValues,
            "${DatabaseHelper.COLUMN_REQUEST_ID} = ?",
            arrayOf(requestId.toString())
        )
        db.close()
        return rowsUpdated
    }

    fun deleteRequest(requestId: Long): Int {
        val db = dbHelper.writableDatabase
        val rowsDeleted = db.delete(
            DatabaseHelper.TABLE_REQUESTS,
            "${DatabaseHelper.COLUMN_REQUEST_ID} = ?",
            arrayOf(requestId.toString())
        )
        db.close()
        return rowsDeleted
    }

    fun getRequestById(requestId: Long): Request? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_REQUESTS,
            arrayOf(
                DatabaseHelper.COLUMN_REQUEST_ID,
                DatabaseHelper.COLUMN_REQUESTER_ID,
                DatabaseHelper.COLUMN_REQUESTER_NAME,
                DatabaseHelper.COLUMN_REQUEST_BLOOD_TYPE,
                DatabaseHelper.COLUMN_REQUEST_LOCATION,
                DatabaseHelper.COLUMN_MESSAGE,
                DatabaseHelper.COLUMN_CREATED_AT,
                DatabaseHelper.COLUMN_STATUS,
                DatabaseHelper.COLUMN_REQUEST_IMAGE,
                DatabaseHelper.COLUMN_DONOR_ID,
                DatabaseHelper.COLUMN_MOBILE
            ),
            "${DatabaseHelper.COLUMN_REQUEST_ID} = ?",
            arrayOf(requestId.toString()),
            null, null, null
        )

        cursor.use {
            if (cursor.moveToFirst()) {
                val requesterId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUESTER_ID))
                val requesterName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUESTER_NAME))
                val bloodType = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_BLOOD_TYPE))
                val location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_LOCATION))
                val message = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MESSAGE))
                val createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CREATED_AT))
                val status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS))
                val mobile = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MOBILE))
                val imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REQUEST_IMAGE))
                val imageBitmap = byteArrayToBitmap(imageByteArray)
                val donorId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DONOR_ID)).takeIf { it != 0L }

                db.close()
                return Request(requestId, requesterId, requesterName, donorId, bloodType, location, message, createdAt, mobile, status, imageBitmap)
            }
        }
        db.close()
        return null
    }
    fun updateDonorIdAndStatus(requestId: Long, donorId: Long): Int {
        val db = dbHelper.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseHelper.COLUMN_DONOR_ID, donorId)
            put(DatabaseHelper.COLUMN_STATUS, "fulfilled")
        }

        val rowsUpdated = db.update(
            DatabaseHelper.TABLE_REQUESTS,
            contentValues,
            "${DatabaseHelper.COLUMN_REQUEST_ID} = ?",
            arrayOf(requestId.toString())
        )
        db.close()
        return rowsUpdated
    }

}
