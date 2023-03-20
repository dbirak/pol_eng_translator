package com.example.pol_eng_translator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {

    public static final String WORDS_LIST = "WORDS_LIST";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_WORD_PL = "WORD_PL";
    public static final String COLUMN_WORD_EN = "WORD_EN";

    public Database(@Nullable Context context) {
        super(context, "words.db", null, 1);
    }

    //metoda wywoływana pierwszy raz przy tworzeniu bazy danych
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + WORDS_LIST + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_WORD_PL + " TEXT, " + COLUMN_WORD_EN + " TEXT )";

        db.execSQL(createTable);

        String seed = "INSERT INTO '" + WORDS_LIST +"' ('" + COLUMN_WORD_PL + "', '" + COLUMN_WORD_EN + "') VALUES" +
                "  ('jabłko', 'apple')," +
                "  ('samochód', 'car')," +
                "  ('dom', 'house')," +
                "  ('niebieski', 'blue')";

        db.execSQL(seed);
    }

    //metoda wywoływana podczas aktualizacji naszej bazy danych do kolejnych wersji
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean addWord(String wordpl, String worden)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_WORD_PL, wordpl);
        cv.put(COLUMN_WORD_EN, worden);

        long insert = db.insert(WORDS_LIST, null, cv);
        if(insert == -1) return false;
        else return true;
    }

    public List<Word_Model> getRandomWord()
    {
        List<Word_Model> RandomWord = new ArrayList<>();
        String query = "SELECT * FROM " + WORDS_LIST + " ORDER BY RANDOM() LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                Word_Model word = new Word_Model(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                RandomWord.add(word);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return RandomWord;

    }

    public List<Word_Model> getAllWords()
    {
        List<Word_Model> AllWords = new ArrayList<>();
        String query = "SELECT * FROM " + WORDS_LIST;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()) {
            do {
                Word_Model word = new Word_Model(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                AllWords.add(word);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return AllWords;

    }

    public boolean removeWord(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        long delete = db.delete(WORDS_LIST, COLUMN_ID + "=" + id, null);

        if(delete == 0) return false;
        else return true;
    }

    public long countWord()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, WORDS_LIST);
        db.close();
        return count;
    }

    public boolean checkExistWord(String wordpl, String worden)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(" + COLUMN_ID + ") FROM " + WORDS_LIST + " WHERE " + COLUMN_WORD_PL + "='" + wordpl + "' OR " + COLUMN_WORD_EN + "='" + worden + "'";
        int counter = 0;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) counter = cursor.getInt(0);

        db.close();
        cursor.close();

        if(counter == 0) return false;
        else return true;
    }
}
