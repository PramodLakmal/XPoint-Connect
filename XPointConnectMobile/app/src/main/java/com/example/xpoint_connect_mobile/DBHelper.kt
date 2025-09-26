package com.example.xpoint_connect_mobile

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "ev_app.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        // EV Owner table
        db.execSQL(
            "CREATE TABLE ev_owner(" +
                    "nic TEXT PRIMARY KEY," +
                    "name TEXT," +
                    "email TEXT," +
                    "password TEXT," +
                    "status INTEGER)"
        )

        // Reservation table
        db.execSQL(
            "CREATE TABLE reservation(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nic TEXT," +
                    "date TEXT," +
                    "time TEXT," +
                    "status TEXT," +
                    "FOREIGN KEY(nic) REFERENCES ev_owner(nic))"
        )

        db.execSQL(
            "CREATE TABLE reservations(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_email TEXT, " +
                    "date TEXT, " +
                    "time TEXT, " +
                    "station TEXT)"
        )

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ev_owner")
        db.execSQL("DROP TABLE IF EXISTS reservation")
        onCreate(db)
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE email=? AND password=?",
            arrayOf(email, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun addReservation(userEmail: String, date: String, time: String, station: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("user_email", userEmail)
            put("date", date)
            put("time", time)
            put("station", station)
        }
        val result = db.insert("reservations", null, values)
        return result != -1L
    }


}
