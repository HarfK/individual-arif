package com.example.individual_arif;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteOpenHelper that manages the local electricity-bill database.
 *
 * Table: bills
 *   _id          INTEGER  PRIMARY KEY AUTOINCREMENT
 *   month        TEXT     (e.g. "January")
 *   units        REAL     kWh consumed
 *   rebate       REAL     percentage 0–5
 *   total_charges REAL    computed total before rebate
 *   final_cost   REAL     total after rebate deduction
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "electricity_bills.db";
    private static final int DATABASE_VERSION = 1;

    // Table & column names
    public static final String TABLE_BILLS       = "bills";
    public static final String COL_ID            = "_id";
    public static final String COL_MONTH         = "month";
    public static final String COL_UNITS         = "units";
    public static final String COL_REBATE        = "rebate";
    public static final String COL_TOTAL_CHARGES = "total_charges";
    public static final String COL_FINAL_COST    = "final_cost";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_BILLS + " (" +
            COL_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_MONTH         + " TEXT NOT NULL, " +
            COL_UNITS         + " REAL NOT NULL, " +
            COL_REBATE        + " REAL NOT NULL, " +
            COL_TOTAL_CHARGES + " REAL NOT NULL, " +
            COL_FINAL_COST    + " REAL NOT NULL" +
            ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    // ───────────────────────── CRUD ─────────────────────────

    /** Insert a new bill record. Returns the row ID, or -1 on error. */
    public long insertBill(String month, double units, double rebate,
                           double totalCharges, double finalCost) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MONTH,         month);
        cv.put(COL_UNITS,         units);
        cv.put(COL_REBATE,        rebate);
        cv.put(COL_TOTAL_CHARGES, totalCharges);
        cv.put(COL_FINAL_COST,    finalCost);
        long id = db.insert(TABLE_BILLS, null, cv);
        db.close();
        return id;
    }

    /** Return all bills ordered by insertion (newest first). */
    public List<BillRecord> getAllBills() {
        List<BillRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_BILLS, null, null, null, null, null,
                COL_ID + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToRecord(cursor));
            }
            cursor.close();
        }
        db.close();
        return list;
    }

    /** Return a single bill by ID, or null if not found. */
    public BillRecord getBillById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_BILLS, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        BillRecord record = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) record = cursorToRecord(cursor);
            cursor.close();
        }
        db.close();
        return record;
    }

    /** Update an existing bill record. Returns rows affected. */
    public int updateBill(long id, String month, double units, double rebate,
                          double totalCharges, double finalCost) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MONTH,         month);
        cv.put(COL_UNITS,         units);
        cv.put(COL_REBATE,        rebate);
        cv.put(COL_TOTAL_CHARGES, totalCharges);
        cv.put(COL_FINAL_COST,    finalCost);
        int rows = db.update(TABLE_BILLS, cv, COL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    /** Delete a bill by ID. Returns rows affected. */
    public int deleteBill(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_BILLS, COL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // ───────────────────────── helpers ─────────────────────────

    private BillRecord cursorToRecord(Cursor c) {
        return new BillRecord(
                c.getLong(c.getColumnIndexOrThrow(COL_ID)),
                c.getString(c.getColumnIndexOrThrow(COL_MONTH)),
                c.getDouble(c.getColumnIndexOrThrow(COL_UNITS)),
                c.getDouble(c.getColumnIndexOrThrow(COL_REBATE)),
                c.getDouble(c.getColumnIndexOrThrow(COL_TOTAL_CHARGES)),
                c.getDouble(c.getColumnIndexOrThrow(COL_FINAL_COST))
        );
    }
}
