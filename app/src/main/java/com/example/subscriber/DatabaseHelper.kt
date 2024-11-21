import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.subscriber.MainActivity

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "locationData.db"
        const val DATABASE_VERSION = 2

        const val TABLE_NAME = "LocationData"
        const val COLUMN_ID = "id"
        const val COLUMN_STUDENT_ID = "studentID"
        const val COLUMN_TIME = "time"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_SPEED = "speed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_STUDENT_ID TEXT NOT NULL,
                $COLUMN_TIME INTEGER NOT NULL,
                $COLUMN_LATITUDE REAL NOT NULL,
                $COLUMN_LONGITUDE REAL NOT NULL,
                $COLUMN_SPEED REAL NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val addSpeedColumnQuery = """
            ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_SPEED REAL NOT NULL DEFAULT 0
        """
            db.execSQL(addSpeedColumnQuery)
        }
    }

    fun insertLocationData(studentID: String, time: Long, latitude: Double, longitude: Double, speed: Double): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_STUDENT_ID, studentID)
            put(COLUMN_TIME, time)
            put(COLUMN_LATITUDE, latitude)
            put(COLUMN_LONGITUDE, longitude)
            put(COLUMN_SPEED, speed)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }
    fun getMinMaxSpeed(studentID: String): Pair<Double, Double>? {
        val db = readableDatabase
        val query = """
        SELECT MIN($COLUMN_SPEED) AS minSpeed, MAX($COLUMN_SPEED) AS maxSpeed
        FROM $TABLE_NAME
        WHERE $COLUMN_STUDENT_ID = ?
    """
        val cursor = db.rawQuery(query, arrayOf(studentID))

        return if (cursor.moveToFirst()) {
            val minSpeed = cursor.getDouble(cursor.getColumnIndexOrThrow("minSpeed"))
            val maxSpeed = cursor.getDouble(cursor.getColumnIndexOrThrow("maxSpeed"))
            cursor.close()
            Pair(minSpeed, maxSpeed)
        } else {
            cursor.close()
            null
        }
    }

    fun getAllLocationData(): List<MainActivity.LocationData> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val locationDataList = mutableListOf<MainActivity.LocationData>()

        while (cursor.moveToNext()) {
            val studentID = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID))
            val time = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME))
            val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE))
            val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))

            locationDataList.add(MainActivity.LocationData(studentID, time, latitude, longitude))
        }
        cursor.close()
        return locationDataList
    }
}
