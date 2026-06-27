package com.olivearchi.goodroutine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TodoDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "todos.db";
    public static final int DATABASE_VERSION = 26;

    private static final String TABLE_TODOS = "todos";
    // ... (previous columns)
    
    // English Words Table
    private static final String TABLE_ENGLISH = "english_words";
    private static final String COLUMN_E_ID = "id";
    private static final String COLUMN_E_WORD = "word";
    private static final String COLUMN_E_MEANING = "meaning";
    private static final String COLUMN_E_EXAMPLE = "example";
    private static final String COLUMN_E_PHONETIC = "phonetic";
    private static final String COLUMN_E_LEVEL = "level";
    private static final String COLUMN_E_FAVORITE = "is_favorite";
    private static final String COLUMN_E_LAST_ACCESSED = "last_accessed_at";

    // Japanese Words Table
    private static final String TABLE_JAPANESE = "japanese_words";
    private static final String COLUMN_J_ID = "id";
    private static final String COLUMN_J_WORD = "word";
    private static final String COLUMN_J_READING = "reading";
    private static final String COLUMN_J_MEANING = "meaning";
    private static final String COLUMN_J_EXAMPLE = "example";
    private static final String COLUMN_J_LEVEL = "level";
    private static final String COLUMN_J_FAVORITE = "is_favorite";
    private static final String COLUMN_J_LAST_ACCESSED = "last_accessed_at";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SUBJECT = "subject";
    private static final String COLUMN_DETAIL = "detail";
    private static final String COLUMN_START_DATETIME = "start_datetime";
    private static final String COLUMN_END_DATETIME = "end_datetime";
    private static final String COLUMN_IS_REPEATING = "is_repeating";
    private static final String COLUMN_REPEAT_TYPE = "repeat_type";
    private static final String COLUMN_IS_DONE = "is_done";
    private static final String COLUMN_COLOR = "color";
    private static final String COLUMN_PERFORM_COUNT = "perform_count";
    private static final String COLUMN_VERSION = "version";
    private static final String COLUMN_EMOTICON = "emoticon";

    private static final String TABLE_HISTORY = "perform_history";
    private static final String COLUMN_SEQ = "seq";
    private static final String COLUMN_HISTORY_TODO_ID = "todo_id";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_PERFORM_DATETIME = "perform_datetime";

    private static final String TABLE_READING_NOTES = "reading_notes";
    private static final String COLUMN_NOTE_ID = "id";
    private static final String COLUMN_BOOK_TITLE = "book_title";
    private static final String COLUMN_NOTE_CONTENT = "note_content";
    private static final String COLUMN_REMARKS = "remarks";
    private static final String COLUMN_MODIFIED_DATETIME = "modified_datetime";
    private static final String COLUMN_NOTE_LAST_ACCESSED = "last_accessed_at";
    private static final String COLUMN_NOTE_FAVORITE = "is_favorite";

    private static final String TABLE_TODAY_TASKS = "today_tasks";
    private static final String COLUMN_TASK_ID = "id";
    private static final String COLUMN_TASK_TITLE = "title";
    private static final String COLUMN_TASK_DESCRIPTION = "description";
    private static final String COLUMN_TASK_ESTIMATED_MINUTES = "estimated_minutes";
    private static final String COLUMN_TASK_LAST_ACCESSED = "last_accessed_at";

    private static final String TABLE_MEMORIZATION = "memorization";
    private static final String COLUMN_MEMO_ID = "id";
    private static final String COLUMN_MEMO_TITLE = "title";
    private static final String COLUMN_MEMO_CONTENT = "content";
    private static final String COLUMN_MEMO_KEYWORD = "keyword";
    private static final String COLUMN_MEMO_CREATED_AT = "created_at";
    private static final String COLUMN_MEMO_UPDATED_AT = "updated_at";
    private static final String COLUMN_MEMO_LAST_ACCESSED = "last_accessed_at";
    private static final String COLUMN_MEMO_FAVORITE = "is_favorite";

    // Memos Table
    private static final String TABLE_MEMOS = "memos";
    private static final String COLUMN_M_ID = "seq";
    private static final String COLUMN_M_TITLE = "title";
    private static final String COLUMN_M_CONTENT = "content";
    private static final String COLUMN_M_REMARKS = "remarks";
    private static final String COLUMN_M_CREATED_AT = "created_at";
    private static final String COLUMN_M_LAST_ACCESSED = "last_accessed_at";
    private static final String COLUMN_M_FAVORITE = "is_favorite";

    // Secret Notes Table
    private static final String TABLE_SECRET_NOTES = "secret_notes";
    private static final String COLUMN_S_ID = "id";
    private static final String COLUMN_S_TITLE = "title";
    private static final String COLUMN_S_CONTENT = "content";
    private static final String COLUMN_S_REMARKS = "remarks";
    private static final String COLUMN_S_CREATED_AT = "created_at";
    private static final String COLUMN_S_FAVORITE = "is_favorite";

    // Feature Structure Table
    public static final String TABLE_FEATURES = "feature_structure";
    public static final String COLUMN_FS_SEQ = "seq";
    public static final String COLUMN_FS_ID = "feature_id";
    public static final String COLUMN_FS_POS = "position";
    public static final String COLUMN_FS_TITLE = "title";
    public static final String COLUMN_FS_ICON = "icon";
    public static final String COLUMN_FS_COLOR = "color";

    private Context context;

    public TodoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TODOS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_SUBJECT + " TEXT,"
                + COLUMN_DETAIL + " TEXT,"
                + COLUMN_START_DATETIME + " TEXT,"
                + COLUMN_END_DATETIME + " TEXT,"
                + COLUMN_IS_REPEATING + " INTEGER,"
                + COLUMN_REPEAT_TYPE + " INTEGER,"
                + COLUMN_IS_DONE + " INTEGER,"
                + COLUMN_COLOR + " INTEGER,"
                + COLUMN_PERFORM_COUNT + " INTEGER,"
                + COLUMN_VERSION + " REAL,"
                + COLUMN_EMOTICON + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_SEQ + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_HISTORY_TODO_ID + " INTEGER,"
                + COLUMN_CATEGORY + " TEXT,"
                + COLUMN_PERFORM_DATETIME + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_READING_NOTES + "("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BOOK_TITLE + " TEXT,"
                + COLUMN_NOTE_CONTENT + " TEXT,"
                + COLUMN_REMARKS + " TEXT,"
                + COLUMN_MODIFIED_DATETIME + " TEXT,"
                + COLUMN_NOTE_LAST_ACCESSED + " TEXT,"
                + COLUMN_NOTE_FAVORITE + " INTEGER DEFAULT 0"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_TODAY_TASKS + "("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TASK_TITLE + " TEXT,"
                + COLUMN_TASK_DESCRIPTION + " TEXT,"
                + COLUMN_TASK_ESTIMATED_MINUTES + " INTEGER,"
                + COLUMN_TASK_LAST_ACCESSED + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_MEMORIZATION + "("
                + COLUMN_MEMO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MEMO_TITLE + " TEXT,"
                + COLUMN_MEMO_CONTENT + " TEXT,"
                + COLUMN_MEMO_KEYWORD + " TEXT,"
                + COLUMN_MEMO_CREATED_AT + " TEXT,"
                + COLUMN_MEMO_UPDATED_AT + " TEXT,"
                + COLUMN_MEMO_LAST_ACCESSED + " TEXT,"
                + COLUMN_MEMO_FAVORITE + " INTEGER DEFAULT 0"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_MEMOS + "("
                + COLUMN_M_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_M_TITLE + " TEXT,"
                + COLUMN_M_CONTENT + " TEXT,"
                + COLUMN_M_REMARKS + " TEXT,"
                + COLUMN_M_CREATED_AT + " TEXT,"
                + COLUMN_M_LAST_ACCESSED + " TEXT,"
                + COLUMN_M_FAVORITE + " INTEGER DEFAULT 0"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_ENGLISH + "("
                + COLUMN_E_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_E_WORD + " TEXT,"
                + COLUMN_E_MEANING + " TEXT,"
                + COLUMN_E_EXAMPLE + " TEXT,"
                + COLUMN_E_PHONETIC + " TEXT,"
                + COLUMN_E_LEVEL + " INTEGER,"
                + COLUMN_E_FAVORITE + " INTEGER,"
                + COLUMN_E_LAST_ACCESSED + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_JAPANESE + "("
                + COLUMN_J_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_J_WORD + " TEXT,"
                + COLUMN_J_READING + " TEXT,"
                + COLUMN_J_MEANING + " TEXT,"
                + COLUMN_J_EXAMPLE + " TEXT,"
                + COLUMN_J_LEVEL + " INTEGER,"
                + COLUMN_J_FAVORITE + " INTEGER,"
                + COLUMN_J_LAST_ACCESSED + " TEXT"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_FEATURES + "("
                + COLUMN_FS_SEQ + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FS_ID + " TEXT,"
                + COLUMN_FS_POS + " INTEGER,"
                + COLUMN_FS_TITLE + " TEXT,"
                + COLUMN_FS_ICON + " TEXT,"
                + COLUMN_FS_COLOR + " INTEGER"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_SECRET_NOTES + "("
                + COLUMN_S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_S_TITLE + " TEXT,"
                + COLUMN_S_CONTENT + " TEXT,"
                + COLUMN_S_REMARKS + " TEXT,"
                + COLUMN_S_CREATED_AT + " TEXT,"
                + COLUMN_S_FAVORITE + " INTEGER DEFAULT 0"
                + ")");
        
        seedEnglishWords(db);
        seedJapaneseWords(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE " + TABLE_TODOS + " ADD COLUMN " + COLUMN_VERSION + " REAL DEFAULT 0.1");
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + "(" + COLUMN_SEQ + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_HISTORY_TODO_ID + " INTEGER," + COLUMN_CATEGORY + " TEXT," + COLUMN_PERFORM_DATETIME + " TEXT" + ")");
            migrateHistoryData(db);
        }
        if (oldVersion < 4) db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_READING_NOTES + "(" + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_BOOK_TITLE + " TEXT," + COLUMN_NOTE_CONTENT + " TEXT," + COLUMN_MODIFIED_DATETIME + " TEXT" + ")");
        if (oldVersion < 5) db.execSQL("ALTER TABLE " + TABLE_READING_NOTES + " ADD COLUMN " + COLUMN_REMARKS + " TEXT");
        if (oldVersion < 6) db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TODAY_TASKS + "(" + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TASK_TITLE + " TEXT," + COLUMN_TASK_DESCRIPTION + " TEXT," + COLUMN_TASK_ESTIMATED_MINUTES + " INTEGER" + ")");
        if (oldVersion < 7) db.execSQL("ALTER TABLE " + TABLE_TODOS + " ADD COLUMN " + COLUMN_EMOTICON + " TEXT DEFAULT '😊'");
        if (oldVersion < 8) db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MEMORIZATION + "(" + COLUMN_MEMO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_MEMO_TITLE + " TEXT," + COLUMN_MEMO_CONTENT + " TEXT," + COLUMN_MEMO_KEYWORD + " TEXT," + COLUMN_MEMO_CREATED_AT + " TEXT," + COLUMN_MEMO_UPDATED_AT + " TEXT" + ")");
        if (oldVersion < 9) {
            db.execSQL("ALTER TABLE " + TABLE_READING_NOTES + " ADD COLUMN " + COLUMN_NOTE_LAST_ACCESSED + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_TODAY_TASKS + " ADD COLUMN " + COLUMN_TASK_LAST_ACCESSED + " TEXT");
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            db.execSQL("UPDATE " + TABLE_READING_NOTES + " SET " + COLUMN_NOTE_LAST_ACCESSED + " = '" + now + "'");
            db.execSQL("UPDATE " + TABLE_TODAY_TASKS + " SET " + COLUMN_TASK_LAST_ACCESSED + " = '" + now + "'");
        }
        if (oldVersion < 10) {
            db.execSQL("ALTER TABLE " + TABLE_MEMORIZATION + " ADD COLUMN " + COLUMN_MEMO_LAST_ACCESSED + " TEXT");
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            db.execSQL("UPDATE " + TABLE_MEMORIZATION + " SET " + COLUMN_MEMO_LAST_ACCESSED + " = '" + now + "'");
        }
        if (oldVersion < 11) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_MEMOS + "(" + COLUMN_M_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_M_TITLE + " TEXT," + COLUMN_M_CONTENT + " TEXT," + COLUMN_M_REMARKS + " TEXT," + COLUMN_M_CREATED_AT + " TEXT," + COLUMN_M_LAST_ACCESSED + " TEXT" + ")");
        }
        if (oldVersion < 12) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_ENGLISH + "(" + COLUMN_E_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_E_WORD + " TEXT," + COLUMN_E_MEANING + " TEXT," + COLUMN_E_EXAMPLE + " TEXT," + COLUMN_E_PHONETIC + " TEXT," + COLUMN_E_LEVEL + " INTEGER," + COLUMN_E_FAVORITE + " INTEGER," + COLUMN_E_LAST_ACCESSED + " TEXT" + ")");
        }
        if (oldVersion < 15) {
            try { db.execSQL("ALTER TABLE " + TABLE_READING_NOTES + " ADD COLUMN " + COLUMN_NOTE_FAVORITE + " INTEGER DEFAULT 0"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_MEMORIZATION + " ADD COLUMN " + COLUMN_MEMO_FAVORITE + " INTEGER DEFAULT 0"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_MEMOS + " ADD COLUMN " + COLUMN_M_FAVORITE + " INTEGER DEFAULT 0"); } catch (Exception ignored) {}
        }
        if (oldVersion < 16) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_JAPANESE + "(" + COLUMN_J_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_J_WORD + " TEXT," + COLUMN_J_READING + " TEXT," + COLUMN_J_MEANING + " TEXT," + COLUMN_J_EXAMPLE + " TEXT," + COLUMN_J_LEVEL + " INTEGER," + COLUMN_J_FAVORITE + " INTEGER," + COLUMN_J_LAST_ACCESSED + " TEXT" + ")");
        }
        if (oldVersion < 18) {
            db.execSQL("DELETE FROM " + TABLE_ENGLISH);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_JAPANESE + "(" + COLUMN_J_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_J_WORD + " TEXT," + COLUMN_J_READING + " TEXT," + COLUMN_J_MEANING + " TEXT," + COLUMN_J_EXAMPLE + " TEXT," + COLUMN_J_LEVEL + " INTEGER," + COLUMN_J_FAVORITE + " INTEGER," + COLUMN_J_LAST_ACCESSED + " TEXT" + ")");
            db.execSQL("DELETE FROM " + TABLE_JAPANESE);
            seedEnglishWords(db);
            seedJapaneseWords(db);
        }
        if (oldVersion < 19) {
            // Final stabilization for 3000 words + 1000 words
            db.execSQL("DELETE FROM " + TABLE_ENGLISH);
            db.execSQL("DELETE FROM " + TABLE_JAPANESE);
            seedEnglishWords(db);
            seedJapaneseWords(db);
        }
        if (oldVersion < 20) {
            // Force re-seed for the complete 1000 Japanese words list
            db.execSQL("DELETE FROM " + TABLE_JAPANESE);
            seedJapaneseWords(db);
        }
        if (oldVersion < 21) {
            // Re-seed all words to ensure data integrity with new 1000/3000 lists
            db.execSQL("DELETE FROM " + TABLE_ENGLISH);
            db.execSQL("DELETE FROM " + TABLE_JAPANESE);
            seedEnglishWords(db);
            seedJapaneseWords(db);
        }
        if (oldVersion < 22) {
            // Force re-seed for Travel Japanese focus
            db.execSQL("DELETE FROM " + TABLE_JAPANESE);
            seedJapaneseWords(db);
        }
        if (oldVersion < 23) {
            // Ensure 1000 words list is fully loaded
            db.execSQL("DELETE FROM " + TABLE_ENGLISH);
            db.execSQL("DELETE FROM " + TABLE_JAPANESE);
            seedEnglishWords(db);
            seedJapaneseWords(db);
        }
        if (oldVersion < 24) {
            // Deduplicated English words list re-seed
            db.execSQL("DELETE FROM " + TABLE_ENGLISH);
            seedEnglishWords(db);
        }
        if (oldVersion < 25) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FEATURES + "("
                    + COLUMN_FS_SEQ + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_FS_ID + " TEXT,"
                    + COLUMN_FS_POS + " INTEGER,"
                    + COLUMN_FS_TITLE + " TEXT,"
                    + COLUMN_FS_ICON + " TEXT,"
                    + COLUMN_FS_COLOR + " INTEGER"
                    + ")");
        }
        if (oldVersion < 26) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SECRET_NOTES + "("
                    + COLUMN_S_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_S_TITLE + " TEXT,"
                    + COLUMN_S_CONTENT + " TEXT,"
                    + COLUMN_S_REMARKS + " TEXT,"
                    + COLUMN_S_CREATED_AT + " TEXT,"
                    + COLUMN_S_FAVORITE + " INTEGER DEFAULT 0"
                    + ")");
        }
    }

    private void migrateHistoryData(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_TODOS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String detail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DETAIL));
                if (detail != null && detail.contains("[수행: ")) {
                    String[] lines = detail.split("\n");
                    StringBuilder newDetail = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith("[수행: ")) {
                            String timestamp = line.replace("[수행: ", "").replace("]", "").trim();
                            ContentValues historyValues = new ContentValues();
                            historyValues.put(COLUMN_HISTORY_TODO_ID, id);
                            historyValues.put(COLUMN_CATEGORY, "수행기록");
                            historyValues.put(COLUMN_PERFORM_DATETIME, timestamp);
                            db.insert(TABLE_HISTORY, null, historyValues);
                        } else {
                            if (newDetail.length() > 0) newDetail.append("\n");
                            newDetail.append(line);
                        }
                    }
                    ContentValues todoValues = new ContentValues();
                    todoValues.put(COLUMN_DETAIL, newDetail.toString());
                    db.update(TABLE_TODOS, todoValues, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    public void addTodo(TodoItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, item.getId());
        values.put(COLUMN_SUBJECT, item.getSubject());
        values.put(COLUMN_DETAIL, item.getDetail());
        values.put(COLUMN_START_DATETIME, item.getStartDateTime());
        values.put(COLUMN_END_DATETIME, item.getEndDateTime());
        values.put(COLUMN_IS_REPEATING, item.isRepeating() ? 1 : 0);
        values.put(COLUMN_REPEAT_TYPE, item.getRepeatType());
        values.put(COLUMN_IS_DONE, item.isDone() ? 1 : 0);
        values.put(COLUMN_COLOR, item.getColor());
        values.put(COLUMN_PERFORM_COUNT, item.getPerformCount());
        values.put(COLUMN_VERSION, item.getVersion());
        values.put(COLUMN_EMOTICON, item.getEmoticon());
        db.insert(TABLE_TODOS, null, values);
        db.close();
    }

    public void updateTodo(TodoItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUBJECT, item.getSubject());
        values.put(COLUMN_DETAIL, item.getDetail());
        values.put(COLUMN_START_DATETIME, item.getStartDateTime());
        values.put(COLUMN_END_DATETIME, item.getEndDateTime());
        values.put(COLUMN_IS_REPEATING, item.isRepeating() ? 1 : 0);
        values.put(COLUMN_REPEAT_TYPE, item.getRepeatType());
        values.put(COLUMN_IS_DONE, item.isDone() ? 1 : 0);
        values.put(COLUMN_COLOR, item.getColor());
        values.put(COLUMN_PERFORM_COUNT, item.getPerformCount());
        values.put(COLUMN_VERSION, item.getVersion());
        values.put(COLUMN_EMOTICON, item.getEmoticon());

        db.update(TABLE_TODOS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(item.getId())});
        db.close();
    }

    public void addPerformHistory(long todoId, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        int repeatType = 0;
        Cursor todoCursor = db.query(TABLE_TODOS, new String[]{COLUMN_REPEAT_TYPE}, COLUMN_ID + " = ?", new String[]{String.valueOf(todoId)}, null, null, null);
        if (todoCursor.moveToFirst()) { repeatType = todoCursor.getInt(0); }
        todoCursor.close();

        if (repeatType != TodoItem.REPEAT_NONE) {
            String targetIdStr = String.valueOf(todoId);
            Cursor historyCursor = db.query(TABLE_HISTORY, new String[]{COLUMN_SEQ, COLUMN_PERFORM_DATETIME}, 
                    COLUMN_HISTORY_TODO_ID + " = ?", new String[]{targetIdStr}, null, null, COLUMN_SEQ + " DESC");
            
            boolean found = false;
            if (historyCursor.moveToFirst()) {
                do {
                    String existingTime = historyCursor.getString(1);
                    if (isSamePeriod(existingTime, timestamp, repeatType)) {
                        int seq = historyCursor.getInt(0);
                        ContentValues values = new ContentValues();
                        values.put(COLUMN_PERFORM_DATETIME, timestamp);
                        db.update(TABLE_HISTORY, values, COLUMN_SEQ + " = ?", new String[]{String.valueOf(seq)});
                        found = true;
                        break;
                    }
                } while (historyCursor.moveToNext());
            }
            historyCursor.close();
            if (found) {
                db.close();
                return;
            }
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_HISTORY_TODO_ID, todoId);
        values.put(COLUMN_CATEGORY, "Record");
        values.put(COLUMN_PERFORM_DATETIME, timestamp);
        db.insert(TABLE_HISTORY, null, values);
        db.execSQL("UPDATE " + TABLE_TODOS + " SET " + COLUMN_PERFORM_COUNT + " = " + COLUMN_PERFORM_COUNT + " + 1 WHERE " + COLUMN_ID + " = ?", new Object[]{todoId});
        db.close();
    }

    private boolean isSamePeriod(String time1, String time2, int repeatType) {
        try {
            String t1 = time1.replace("[수행: ", "").replace("]", "").trim();
            String t2 = time2.replace("[수행: ", "").replace("]", "").trim();
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(sdf.parse(t1));
            cal2.setTime(sdf.parse(t2));

            if (repeatType == TodoItem.REPEAT_DAY) {
                return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                       cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
            } else if (repeatType == TodoItem.REPEAT_WEEK) {
                return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                       cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
            } else if (repeatType == TodoItem.REPEAT_MONTH) {
                return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                       cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
            } else if (repeatType == TodoItem.REPEAT_YEAR) {
                return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
            }
        } catch (Exception e) {
            Log.e("TodoDbHelper", "Error comparing dates", e);
        }
        return false;
    }

    public void addRawHistory(PerformHistoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HISTORY_TODO_ID, item.getTodoId());
        values.put(COLUMN_CATEGORY, item.getCategory());
        values.put(COLUMN_PERFORM_DATETIME, item.getPerformDateTime());
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public void deleteTodo(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.delete(TABLE_HISTORY, COLUMN_HISTORY_TODO_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void clearAllTodos() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODOS, null, null);
        db.delete(TABLE_HISTORY, null, null);
        db.close();
    }

    public List<String> getLast5History(long todoId) {
        List<String> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HISTORY, new String[]{COLUMN_PERFORM_DATETIME}, COLUMN_HISTORY_TODO_ID + "=?", new String[]{String.valueOf(todoId)}, null, null, COLUMN_SEQ + " DESC", "5");
        if (cursor.moveToFirst()) {
            do { history.add("[수행: " + cursor.getString(0) + "]"); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return history;
    }

    public List<String> getAllHistory(long todoId) {
        List<String> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HISTORY, new String[]{COLUMN_PERFORM_DATETIME}, COLUMN_HISTORY_TODO_ID + "=?", new String[]{String.valueOf(todoId)}, null, null, COLUMN_SEQ + " DESC");
        if (cursor.moveToFirst()) {
            do { history.add("[수행: " + cursor.getString(0) + "]"); } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return history;
    }

    public List<PerformHistoryItem> getFullHistoryForBackup() {
        List<PerformHistoryItem> history = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HISTORY, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                history.add(new PerformHistoryItem(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_HISTORY_TODO_ID)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PERFORM_DATETIME))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return history;
    }

    public List<TodoItem> getAllTodos() {
        List<TodoItem> todoList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TODOS, null);
        if (cursor.moveToFirst()) {
            do {
                TodoItem item = new TodoItem(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DETAIL)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_DATETIME)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_DATETIME)), cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_REPEATING)) == 1, cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REPEAT_TYPE)));
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                item.setDone(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DONE)) == 1);
                item.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLOR)));
                item.setPerformCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PERFORM_COUNT)));
                double version = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_VERSION));
                item.setVersion(version == 0.0 ? 0.1 : version);
                item.setEmoticon(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMOTICON)));
                todoList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return todoList;
    }

    public void addReadingNote(ReadingNoteItem note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (note.getId() != 0) values.put(COLUMN_NOTE_ID, note.getId());
        values.put(COLUMN_BOOK_TITLE, note.getBookTitle());
        values.put(COLUMN_NOTE_CONTENT, note.getContent());
        values.put(COLUMN_REMARKS, note.getRemarks());
        values.put(COLUMN_MODIFIED_DATETIME, note.getModifiedDateTime());
        values.put(COLUMN_NOTE_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COLUMN_NOTE_FAVORITE, note.isFavorite() ? 1 : 0);
        db.insert(TABLE_READING_NOTES, null, values);
        db.close();
    }

    public void updateReadingNote(ReadingNoteItem note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_TITLE, note.getBookTitle());
        values.put(COLUMN_NOTE_CONTENT, note.getContent());
        values.put(COLUMN_REMARKS, note.getRemarks());
        values.put(COLUMN_MODIFIED_DATETIME, note.getModifiedDateTime());
        values.put(COLUMN_NOTE_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COLUMN_NOTE_FAVORITE, note.isFavorite() ? 1 : 0);
        db.update(TABLE_READING_NOTES, values, COLUMN_NOTE_ID + " = ?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public void updateReadingNoteAccessTime(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(TABLE_READING_NOTES, values, COLUMN_NOTE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void toggleReadingNoteFavorite(long id, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_READING_NOTES, values, COLUMN_NOTE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteReadingNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_READING_NOTES, COLUMN_NOTE_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<ReadingNoteItem> getAllReadingNotes() {
        List<ReadingNoteItem> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_READING_NOTES + " ORDER BY " + COLUMN_MODIFIED_DATETIME + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                ReadingNoteItem note = new ReadingNoteItem(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_TITLE)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_CONTENT)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMARKS)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODIFIED_DATETIME)));
                note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)));
                note.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_FAVORITE)) == 1);
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notes;
    }

    public void clearAllReadingNotes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_READING_NOTES, null, null);
        db.close();
    }

    public void addTodayTask(TodayTaskItem task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (task.getId() != 0) values.put(COLUMN_TASK_ID, task.getId());
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(COLUMN_TASK_ESTIMATED_MINUTES, task.getEstimatedMinutes());
        values.put(COLUMN_TASK_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.insert(TABLE_TODAY_TASKS, null, values);
        db.close();
    }

    public void updateTodayTask(TodayTaskItem task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DESCRIPTION, task.getDescription());
        values.put(COLUMN_TASK_ESTIMATED_MINUTES, task.getEstimatedMinutes());
        values.put(COLUMN_TASK_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(TABLE_TODAY_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
    }

    public void updateTodayTaskAccessTime(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(TABLE_TODAY_TASKS, values, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteTodayTask(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODAY_TASKS, COLUMN_TASK_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<TodayTaskItem> getAllTodayTasks() {
        List<TodayTaskItem> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TODAY_TASKS + " ORDER BY " + COLUMN_TASK_LAST_ACCESSED + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                TodayTaskItem task = new TodayTaskItem(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)), cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ESTIMATED_MINUTES)));
                task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)));
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tasks;
    }

    public void clearAllTodayTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TODAY_TASKS, null, null);
        db.close();
    }

    public void addMemorization(MemorizationItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (item.getId() != 0) values.put(COLUMN_MEMO_ID, item.getId());
        values.put(COLUMN_MEMO_TITLE, item.getTitle());
        values.put(COLUMN_MEMO_CONTENT, item.getContent());
        values.put(COLUMN_MEMO_KEYWORD, item.getKeyword());
        values.put(COLUMN_MEMO_CREATED_AT, item.getCreatedAt());
        values.put(COLUMN_MEMO_UPDATED_AT, item.getUpdatedAt());
        values.put(COLUMN_MEMO_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COLUMN_MEMO_FAVORITE, item.isFavorite() ? 1 : 0);
        db.insert(TABLE_MEMORIZATION, null, values);
        db.close();
    }

    public void updateMemorization(MemorizationItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEMO_TITLE, item.getTitle());
        values.put(COLUMN_MEMO_CONTENT, item.getContent());
        values.put(COLUMN_MEMO_KEYWORD, item.getKeyword());
        values.put(COLUMN_MEMO_UPDATED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COLUMN_MEMO_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COLUMN_MEMO_FAVORITE, item.isFavorite() ? 1 : 0);
        db.update(TABLE_MEMORIZATION, values, COLUMN_MEMO_ID + " = ?", new String[]{String.valueOf(item.getId())});
        db.close();
    }

    public void updateMemorizationAccessTime(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEMO_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(TABLE_MEMORIZATION, values, COLUMN_MEMO_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void toggleMemorizationFavorite(long id, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEMO_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_MEMORIZATION, values, COLUMN_MEMO_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteMemorization(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEMORIZATION, COLUMN_MEMO_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<MemorizationItem> getAllMemorizations() {
        List<MemorizationItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MEMORIZATION + " ORDER BY " + COLUMN_MEMO_UPDATED_AT + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                MemorizationItem item = new MemorizationItem(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO_TITLE)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO_CONTENT)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO_KEYWORD)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO_CREATED_AT)), cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMO_UPDATED_AT)));
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MEMO_ID)));
                item.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MEMO_FAVORITE)) == 1);
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return items;
    }

    public void clearAllMemorizations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEMORIZATION, null, null);
        db.close();
    }

    public void clearAllMemos() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEMOS, null, null);
        db.close();
    }

    // Memos CRUD
    public void addMemo(MemoItem memo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (memo.getId() != 0) values.put(COLUMN_M_ID, memo.getId());
        values.put(COLUMN_M_TITLE, memo.getTitle());
        values.put(COLUMN_M_CONTENT, memo.getContent());
        values.put(COLUMN_M_REMARKS, memo.getRemarks());
        values.put(COLUMN_M_CREATED_AT, memo.getCreatedAt());
        values.put(COLUMN_M_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        values.put(COLUMN_M_FAVORITE, memo.isFavorite() ? 1 : 0);
        db.insert(TABLE_MEMOS, null, values);
        db.close();
    }

    public void updateMemo(MemoItem memo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_M_TITLE, memo.getTitle());
        values.put(COLUMN_M_CONTENT, memo.getContent());
        values.put(COLUMN_M_REMARKS, memo.getRemarks());
        // Update CREATED_AT to serve as 'last modified' for sorting as requested
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(COLUMN_M_CREATED_AT, now);
        values.put(COLUMN_M_LAST_ACCESSED, now);
        values.put(COLUMN_M_FAVORITE, memo.isFavorite() ? 1 : 0);
        db.update(TABLE_MEMOS, values, COLUMN_M_ID + " = ?", new String[]{String.valueOf(memo.getId())});
        db.close();
    }

    public void updateMemoAccessTime(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_M_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(TABLE_MEMOS, values, COLUMN_M_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void toggleMemoFavorite(long id, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_M_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_MEMOS, values, COLUMN_M_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteMemo(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEMOS, COLUMN_M_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<MemoItem> getAllMemos() {
        List<MemoItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MEMOS + " ORDER BY " + COLUMN_M_CREATED_AT + " DESC", null);
        if (cursor.moveToFirst()) {
            do {
                MemoItem item = new MemoItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_M_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_M_CONTENT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_M_REMARKS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_M_CREATED_AT))
                );
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_M_ID)));
                item.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_M_FAVORITE)) == 1);
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void addSecretNote(SecretNoteItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_S_TITLE, item.getTitle());
        v.put(COLUMN_S_CONTENT, item.getContent());
        v.put(COLUMN_S_REMARKS, item.getRemarks());
        v.put(COLUMN_S_CREATED_AT, item.getCreatedAt());
        v.put(COLUMN_S_FAVORITE, 0);
        db.insert(TABLE_SECRET_NOTES, null, v);
        db.close();
    }

    public List<SecretNoteItem> getAllSecretNotes() {
        List<SecretNoteItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SECRET_NOTES + " ORDER BY " + COLUMN_S_CREATED_AT + " DESC", null);
        if (c.moveToFirst()) {
            do {
                SecretNoteItem item = new SecretNoteItem(c.getString(c.getColumnIndexOrThrow(COLUMN_S_TITLE)), c.getString(c.getColumnIndexOrThrow(COLUMN_S_CONTENT)), c.getString(c.getColumnIndexOrThrow(COLUMN_S_REMARKS)), c.getString(c.getColumnIndexOrThrow(COLUMN_S_CREATED_AT)));
                item.setId(c.getLong(c.getColumnIndexOrThrow(COLUMN_S_ID)));
                item.setFavorite(c.getInt(c.getColumnIndexOrThrow(COLUMN_S_FAVORITE)) == 1);
                list.add(item);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public SecretNoteItem getSecretNoteById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_SECRET_NOTES, null, COLUMN_S_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);
        SecretNoteItem item = null;
        if (c.moveToFirst()) {
            item = new SecretNoteItem(c.getString(c.getColumnIndexOrThrow(COLUMN_S_TITLE)), c.getString(c.getColumnIndexOrThrow(COLUMN_S_CONTENT)), c.getString(c.getColumnIndexOrThrow(COLUMN_S_REMARKS)), c.getString(c.getColumnIndexOrThrow(COLUMN_S_CREATED_AT)));
            item.setId(c.getLong(c.getColumnIndexOrThrow(COLUMN_S_ID)));
            item.setFavorite(c.getInt(c.getColumnIndexOrThrow(COLUMN_S_FAVORITE)) == 1);
        }
        c.close();
        db.close();
        return item;
    }

    public void updateSecretNote(SecretNoteItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_S_TITLE, item.getTitle());
        v.put(COLUMN_S_CONTENT, item.getContent());
        v.put(COLUMN_S_REMARKS, item.getRemarks());
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        v.put(COLUMN_S_CREATED_AT, now);
        db.update(TABLE_SECRET_NOTES, v, COLUMN_S_ID + " = ?", new String[]{String.valueOf(item.getId())});
        db.close();
    }

    public void deleteSecretNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SECRET_NOTES, COLUMN_S_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void clearSecretNotes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SECRET_NOTES, null, null);
        db.close();
    }

    public void toggleSecretNoteFavorite(long id, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_S_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_SECRET_NOTES, v, COLUMN_S_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // English Words CRUD
    public List<EnglishWordItem> getAllEnglishWords() {
        List<EnglishWordItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ENGLISH, null, null, null, null, null, COLUMN_E_LAST_ACCESSED + " DESC, " + COLUMN_E_WORD + " ASC");
        if (cursor.moveToFirst()) {
            do {
                EnglishWordItem item = new EnglishWordItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_E_WORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_E_MEANING)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_E_EXAMPLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_E_PHONETIC)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_E_LEVEL))
                );
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_E_ID)));
                item.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_E_FAVORITE)) == 1);
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void updateEnglishWordAccessTime(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_E_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(TABLE_ENGLISH, values, COLUMN_E_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void addEnglishWord(EnglishWordItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_E_WORD, item.getWord());
        v.put(COLUMN_E_MEANING, item.getMeaning());
        v.put(COLUMN_E_EXAMPLE, item.getExample());
        v.put(COLUMN_E_PHONETIC, "");
        v.put(COLUMN_E_LEVEL, item.getLevel());
        v.put(COLUMN_E_FAVORITE, 0);
        v.put(COLUMN_E_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.insert(TABLE_ENGLISH, null, v);
        db.close();
    }

    public void deleteEnglishWord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ENGLISH, COLUMN_E_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Japanese Words CRUD
    public List<JapaneseWordItem> getAllJapaneseWords() {
        List<JapaneseWordItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_JAPANESE, null, null, null, null, null, COLUMN_J_LAST_ACCESSED + " DESC, " + COLUMN_J_WORD + " ASC");
        if (cursor.moveToFirst()) {
            do {
                JapaneseWordItem item = new JapaneseWordItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_J_WORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_J_READING)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_J_MEANING)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_J_EXAMPLE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_J_LEVEL))
                );
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_J_ID)));
                item.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_J_FAVORITE)) == 1);
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void updateJapaneseWordAccessTime(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_J_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(TABLE_JAPANESE, values, COLUMN_J_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void addJapaneseWord(JapaneseWordItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_J_WORD, item.getWord());
        v.put(COLUMN_J_READING, item.getReading());
        v.put(COLUMN_J_MEANING, item.getMeaning());
        v.put(COLUMN_J_EXAMPLE, item.getExample());
        v.put(COLUMN_J_LEVEL, item.getLevel());
        v.put(COLUMN_J_FAVORITE, 0);
        v.put(COLUMN_J_LAST_ACCESSED, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.insert(TABLE_JAPANESE, null, v);
        db.close();
    }

    public void deleteJapaneseWord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_JAPANESE, COLUMN_J_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void toggleJapaneseWordFavorite(long id, boolean isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_J_FAVORITE, isFavorite ? 1 : 0);
        db.update(TABLE_JAPANESE, values, COLUMN_J_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    private void seedJapaneseWords(SQLiteDatabase db) {
        try {
            java.io.InputStream is = context.getAssets().open("japanese_words.txt");
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
            String line;
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    try {
                        ContentValues v = new ContentValues();
                        v.put(COLUMN_J_WORD, parts[0].trim());
                        v.put(COLUMN_J_READING, parts[1].trim());
                        v.put(COLUMN_J_MEANING, parts[2].trim());
                        v.put(COLUMN_J_EXAMPLE, parts[3].trim());
                        v.put(COLUMN_J_LEVEL, Integer.parseInt(parts[4].trim()));
                        v.put(COLUMN_J_FAVORITE, 0);
                        v.put(COLUMN_J_LAST_ACCESSED, now);
                        db.insert(TABLE_JAPANESE, null, v);
                    } catch (NumberFormatException e) {
                        Log.e("DB", "Error parsing Japanese word level: " + line);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seedEnglishWords(SQLiteDatabase db) {
        try {
            java.io.InputStream is = context.getAssets().open("english_words.txt");
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
            String line;
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    try {
                        ContentValues v = new ContentValues();
                        v.put(COLUMN_E_WORD, parts[0].trim());
                        v.put(COLUMN_E_MEANING, parts[1].trim());
                        v.put(COLUMN_E_EXAMPLE, parts[2].trim());
                        v.put(COLUMN_E_PHONETIC, ""); // Phonetic removed
                        v.put(COLUMN_E_LEVEL, Integer.parseInt(parts[3].trim()));
                        v.put(COLUMN_E_FAVORITE, 0);
                        v.put(COLUMN_E_LAST_ACCESSED, now);
                        db.insert(TABLE_ENGLISH, null, v);
                    } catch (NumberFormatException e) {
                        Log.e("DB", "Error parsing English word level: " + line);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAllEnglishWords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ENGLISH, null, null);
        db.close();
    }

    public void clearAllJapaneseWords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_JAPANESE, null, null);
        db.close();
    }

    public List<FeatureItem> getAllFeatures() {
        List<FeatureItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FEATURES, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new FeatureItem(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FS_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FS_POS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FS_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FS_ICON)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FS_COLOR))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void updateFeaturePosition(String featureId, int pos) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_FS_POS, pos);
        db.update(TABLE_FEATURES, v, COLUMN_FS_ID + " = ?", new String[]{featureId});
        db.close();
    }

    public void updateFeatureColor(String featureId, int color) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_FS_COLOR, color);
        db.update(TABLE_FEATURES, v, COLUMN_FS_ID + " = ?", new String[]{featureId});
        db.close();
    }

    public void saveFeature(FeatureItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COLUMN_FS_ID, item.getFeatureId());
        v.put(COLUMN_FS_POS, item.getPosition());
        v.put(COLUMN_FS_TITLE, item.getTitle());
        v.put(COLUMN_FS_ICON, item.getIcon());
        v.put(COLUMN_FS_COLOR, item.getColor());
        
        int rows = db.update(TABLE_FEATURES, v, COLUMN_FS_ID + " = ?", new String[]{item.getFeatureId()});
        if (rows == 0) {
            db.insert(TABLE_FEATURES, null, v);
        }
        db.close();
    }

    public void resetFeaturePositions() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FEATURES, null, null);
        db.close();
    }

    public Map<String, Integer> getHistoryCountByDate() {
        Map<String, Integer> counts = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Clean up data format if needed: remove "[수행: " and "]" then take first 10 chars (YYYY-MM-DD)
        String query = "SELECT substr(REPLACE(REPLACE(" + COLUMN_PERFORM_DATETIME + ", '[수행: ', ''), ']', ''), 1, 10) as date, COUNT(*) as count " +
                "FROM " + TABLE_HISTORY + " GROUP BY date";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                counts.put(cursor.getString(0), cursor.getInt(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return counts;
    }

    public Map<String, Integer> getReadingNoteCountByMonth() {
        Map<String, Integer> counts = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Extract YYYY-MM from YYYY-MM-DD HH:mm
        String query = "SELECT substr(" + COLUMN_MODIFIED_DATETIME + ", 1, 7) as month, COUNT(*) as count " +
                "FROM " + TABLE_READING_NOTES + " GROUP BY month";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                counts.put(cursor.getString(0), cursor.getInt(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return counts;
    }

    public Map<String, Integer> getReadingNoteTotalStats() {
        Map<String, Integer> stats = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor1 = db.rawQuery("SELECT COUNT(DISTINCT " + COLUMN_BOOK_TITLE + ") FROM " + TABLE_READING_NOTES, null);
        if (cursor1.moveToFirst()) stats.put("total_books", cursor1.getInt(0));
        cursor1.close();

        Cursor cursor2 = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_READING_NOTES, null);
        if (cursor2.moveToFirst()) stats.put("total_passages", cursor2.getInt(0));
        cursor2.close();

        // New: Total character count for achievement feeling
        long totalChars = 0;
        Cursor c3 = db.rawQuery("SELECT SUM(length(" + COLUMN_NOTE_CONTENT + ")) FROM " + TABLE_READING_NOTES, null);
        if (c3.moveToFirst()) totalChars += c3.getLong(0);
        c3.close();
        Cursor c4 = db.rawQuery("SELECT SUM(length(" + COLUMN_M_CONTENT + ")) FROM " + TABLE_MEMOS, null);
        if (c4.moveToFirst()) totalChars += c4.getLong(0);
        c4.close();
        Cursor c5 = db.rawQuery("SELECT SUM(length(" + COLUMN_MEMO_CONTENT + ")) FROM " + TABLE_MEMORIZATION, null);
        if (c5.moveToFirst()) totalChars += c5.getLong(0);
        c5.close();
        stats.put("total_characters", (int)totalChars);
        
        db.close();
        return stats;
    }

    public List<String> getAllRawContents(String sinceDate) {
        List<String> allContents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String where = (sinceDate != null) ? " >= ?" : null;
        String[] args = (sinceDate != null) ? new String[]{sinceDate} : null;

        // From Reading Notes
        String rWhere = (sinceDate != null) ? COLUMN_MODIFIED_DATETIME + where : null;
        Cursor c1 = db.query(TABLE_READING_NOTES, new String[]{COLUMN_NOTE_CONTENT}, rWhere, args, null, null, null);
        if (c1.moveToFirst()) { do { allContents.add(c1.getString(0)); } while (c1.moveToNext()); }
        c1.close();

        // From Memos
        String mWhere = (sinceDate != null) ? COLUMN_M_CREATED_AT + where : null;
        Cursor c2 = db.query(TABLE_MEMOS, new String[]{COLUMN_M_CONTENT}, mWhere, args, null, null, null);
        if (c2.moveToFirst()) { do { allContents.add(c2.getString(0)); } while (c2.moveToNext()); }
        c2.close();

        // From Memorization
        String memWhere = (sinceDate != null) ? COLUMN_MEMO_CREATED_AT + where : null;
        Cursor c3 = db.query(TABLE_MEMORIZATION, new String[]{COLUMN_MEMO_CONTENT}, memWhere, args, null, null, null);
        if (c3.moveToFirst()) { do { allContents.add(c3.getString(0)); } while (c3.moveToNext()); }
        c3.close();

        db.close();
        return allContents;
    }

    public List<String> getAllRawContents() {
        return getAllRawContents(null);
    }

    public Map<String, Integer> getMemoCountByMonth() {
        Map<String, Integer> counts = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT substr(" + COLUMN_M_CREATED_AT + ", 1, 7) as month, COUNT(*) as count " +
                "FROM " + TABLE_MEMOS + " GROUP BY month";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                counts.put(cursor.getString(0), cursor.getInt(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return counts;
    }

    public Map<String, Integer> getMemorizationCountByMonth() {
        Map<String, Integer> counts = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT substr(" + COLUMN_MEMO_CREATED_AT + ", 1, 7) as month, COUNT(*) as count " +
                "FROM " + TABLE_MEMORIZATION + " GROUP BY month";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                counts.put(cursor.getString(0), cursor.getInt(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return counts;
    }

    public List<SearchResultItem> searchAll(String query) {
        List<SearchResultItem> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String wildQuery = "%" + query + "%";

        // Search Habits (Todos)
        Cursor c1 = db.query(TABLE_TODOS, null, COLUMN_SUBJECT + " LIKE ? OR " + COLUMN_DETAIL + " LIKE ?", new String[]{wildQuery, wildQuery}, null, null, null);
        if (c1.moveToFirst()) {
            do {
                TodoItem item = new TodoItem(c1.getString(c1.getColumnIndexOrThrow(COLUMN_SUBJECT)), c1.getString(c1.getColumnIndexOrThrow(COLUMN_DETAIL)), c1.getString(c1.getColumnIndexOrThrow(COLUMN_START_DATETIME)), c1.getString(c1.getColumnIndexOrThrow(COLUMN_END_DATETIME)), c1.getInt(c1.getColumnIndexOrThrow(COLUMN_IS_REPEATING)) == 1, c1.getInt(c1.getColumnIndexOrThrow(COLUMN_REPEAT_TYPE)));
                item.setId(c1.getLong(c1.getColumnIndexOrThrow(COLUMN_ID)));
                item.setEmoticon(c1.getString(c1.getColumnIndexOrThrow(COLUMN_EMOTICON)));
                item.setColor(c1.getInt(c1.getColumnIndexOrThrow(COLUMN_COLOR)));
                item.setDone(c1.getInt(c1.getColumnIndexOrThrow(COLUMN_IS_DONE)) == 1);
                item.setPerformCount(c1.getInt(c1.getColumnIndexOrThrow(COLUMN_PERFORM_COUNT)));
                results.add(new SearchResultItem(SearchResultItem.Type.HABIT, item, item.getSubject(), item.getDetail()));
            } while (c1.moveToNext());
        }
        c1.close();

        // Search Reading Notes
        Cursor c2 = db.query(TABLE_READING_NOTES, null, COLUMN_BOOK_TITLE + " LIKE ? OR " + COLUMN_NOTE_CONTENT + " LIKE ? OR " + COLUMN_REMARKS + " LIKE ?", new String[]{wildQuery, wildQuery, wildQuery}, null, null, null);
        if (c2.moveToFirst()) {
            do {
                ReadingNoteItem item = new ReadingNoteItem(c2.getString(c2.getColumnIndexOrThrow(COLUMN_BOOK_TITLE)), c2.getString(c2.getColumnIndexOrThrow(COLUMN_NOTE_CONTENT)), c2.getString(c2.getColumnIndexOrThrow(COLUMN_REMARKS)), c2.getString(c2.getColumnIndexOrThrow(COLUMN_MODIFIED_DATETIME)));
                item.setId(c2.getLong(c2.getColumnIndexOrThrow(COLUMN_NOTE_ID)));
                results.add(new SearchResultItem(SearchResultItem.Type.READING, item, item.getBookTitle(), item.getContent()));
            } while (c2.moveToNext());
        }
        c2.close();

        // Search Memorizations
        Cursor c3 = db.query(TABLE_MEMORIZATION, null, COLUMN_MEMO_TITLE + " LIKE ? OR " + COLUMN_MEMO_CONTENT + " LIKE ? OR " + COLUMN_MEMO_KEYWORD + " LIKE ?", new String[]{wildQuery, wildQuery, wildQuery}, null, null, null);
        if (c3.moveToFirst()) {
            do {
                MemorizationItem item = new MemorizationItem(c3.getString(c3.getColumnIndexOrThrow(COLUMN_MEMO_TITLE)), c3.getString(c3.getColumnIndexOrThrow(COLUMN_MEMO_CONTENT)), c3.getString(c3.getColumnIndexOrThrow(COLUMN_MEMO_KEYWORD)), c3.getString(c3.getColumnIndexOrThrow(COLUMN_MEMO_CREATED_AT)), c3.getString(c3.getColumnIndexOrThrow(COLUMN_MEMO_UPDATED_AT)));
                item.setId(c3.getLong(c3.getColumnIndexOrThrow(COLUMN_MEMO_ID)));
                results.add(new SearchResultItem(SearchResultItem.Type.MEMORIZATION, item, item.getTitle(), item.getContent()));
            } while (c3.moveToNext());
        }
        c3.close();

        // Search Tasks
        Cursor c4 = db.query(TABLE_TODAY_TASKS, null, COLUMN_TASK_TITLE + " LIKE ? OR " + COLUMN_TASK_DESCRIPTION + " LIKE ?", new String[]{wildQuery, wildQuery}, null, null, null);
        if (c4.moveToFirst()) {
            do {
                TodayTaskItem item = new TodayTaskItem(c4.getString(c4.getColumnIndexOrThrow(COLUMN_TASK_TITLE)), c4.getString(c4.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)), c4.getInt(c4.getColumnIndexOrThrow(COLUMN_TASK_ESTIMATED_MINUTES)));
                item.setId(c4.getLong(c4.getColumnIndexOrThrow(COLUMN_TASK_ID)));
                results.add(new SearchResultItem(SearchResultItem.Type.TASK, item, item.getTitle(), item.getDescription()));
            } while (c4.moveToNext());
        }
        c4.close();

        // Search Memos
        Cursor c5 = db.query(TABLE_MEMOS, null, COLUMN_M_TITLE + " LIKE ? OR " + COLUMN_M_CONTENT + " LIKE ? OR " + COLUMN_M_REMARKS + " LIKE ?", new String[]{wildQuery, wildQuery, wildQuery}, null, null, null);
        if (c5.moveToFirst()) {
            do {
                MemoItem item = new MemoItem(c5.getString(c5.getColumnIndexOrThrow(COLUMN_M_TITLE)), c5.getString(c5.getColumnIndexOrThrow(COLUMN_M_CONTENT)), c5.getString(c5.getColumnIndexOrThrow(COLUMN_M_REMARKS)), c5.getString(c5.getColumnIndexOrThrow(COLUMN_M_CREATED_AT)));
                item.setId(c5.getLong(c5.getColumnIndexOrThrow(COLUMN_M_ID)));
                results.add(new SearchResultItem(SearchResultItem.Type.MEMO, item, item.getTitle(), item.getContent()));
            } while (c5.moveToNext());
        }
        c5.close();

        // Search Japanese Words
        Cursor c6 = db.query(TABLE_JAPANESE, null, COLUMN_J_WORD + " LIKE ? OR " + COLUMN_J_READING + " LIKE ? OR " + COLUMN_J_MEANING + " LIKE ?", new String[]{wildQuery, wildQuery, wildQuery}, null, null, null);
        if (c6.moveToFirst()) {
            do {
                JapaneseWordItem item = new JapaneseWordItem(
                        c6.getString(c6.getColumnIndexOrThrow(COLUMN_J_WORD)),
                        c6.getString(c6.getColumnIndexOrThrow(COLUMN_J_READING)),
                        c6.getString(c6.getColumnIndexOrThrow(COLUMN_J_MEANING)),
                        c6.getString(c6.getColumnIndexOrThrow(COLUMN_J_EXAMPLE)),
                        c6.getInt(c6.getColumnIndexOrThrow(COLUMN_J_LEVEL))
                );
                item.setId(c6.getLong(c6.getColumnIndexOrThrow(COLUMN_J_ID)));
                results.add(new SearchResultItem(SearchResultItem.Type.JAPANESE, item, item.getWord(), item.getMeaning()));
            } while (c6.moveToNext());
        }
        c6.close();

        return results;
    }
}
