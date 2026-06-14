package com.olivearchi.goodroutine;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TodoViewModel extends AndroidViewModel {
    public static final int FILTER_ALL = 0;
    public static final int FILTER_IN_PROGRESS = 1;
    public static final int FILTER_DONE = 2;
    public static final double CURRENT_DATA_VERSION = 1.0;

    private static final String OLD_FILE_NAME = "todos.dat";
    private final MutableLiveData<List<TodoItem>> todoList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> filterType = new MutableLiveData<>(FILTER_ALL);
    private final TodoDbHelper dbHelper;

    public TodoViewModel(@NonNull Application application) {
        super(application);
        dbHelper = new TodoDbHelper(application);
        migrateOldData();
        loadTodos();
    }

    private void migrateOldData() {
        File file = getApplication().getFileStreamPath(OLD_FILE_NAME);
        if (file.exists()) {
            try (FileInputStream fis = getApplication().openFileInput(OLD_FILE_NAME);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                List<TodoItem> oldList = (List<TodoItem>) ois.readObject();
                if (oldList != null) {
                    for (TodoItem item : oldList) {
                        if (item.getVersion() == 0.0) {
                            item.setVersion(0.1);
                        }
                        if (item.getEmoticon() == null) {
                            item.setEmoticon("😊");
                        }
                        dbHelper.addTodo(item);
                    }
                }
                Log.d("TodoViewModel", "Migration successful.");
            } catch (Exception e) {
                Log.e("TodoViewModel", "Migration failed", e);
            } finally {
                file.delete();
            }
        }
    }

    public LiveData<List<TodoItem>> getTodoList() {
        return todoList;
    }

    public LiveData<Integer> getFilterType() {
        return filterType;
    }

    public void setFilterType(int type) {
        filterType.setValue(type);
    }

    public void addTodo(String subject, String detail, String startDateTime, String endDateTime, boolean isRepeating, int repeatType, boolean isDone, int color, String emoticon) {
        TodoItem newItem = new TodoItem(subject, detail, startDateTime, endDateTime, isRepeating, repeatType);
        newItem.setDone(isDone);
        newItem.setColor(color);
        newItem.setEmoticon(emoticon);
        dbHelper.addTodo(newItem);
        AlarmHelper.scheduleAlarm(getApplication(), newItem);
        loadTodos();
    }

    public void updateTodo(int position, String subject, String detail, String startDateTime, String endDateTime, boolean isRepeating, int repeatType, boolean isDone, int color, String emoticon) {
        List<TodoItem> currentList = todoList.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            TodoItem item = currentList.get(position);
            AlarmHelper.cancelAlarm(getApplication(), item);
            item.setSubject(subject);
            item.setDetail(detail);
            item.setStartDateTime(startDateTime);
            item.setEndDateTime(endDateTime);
            item.setRepeating(isRepeating);
            item.setRepeatType(repeatType);
            item.setDone(isDone);
            item.setColor(color);
            item.setEmoticon(emoticon);
            dbHelper.updateTodo(item);
            AlarmHelper.scheduleAlarm(getApplication(), item);
            loadTodos();
        }
    }
    
    public void toggleDone(TodoItem item, boolean isDone) {
        item.setDone(isDone);
        if (isDone) {
            AlarmHelper.cancelAlarm(getApplication(), item);
        } else {
            AlarmHelper.scheduleAlarm(getApplication(), item);
        }
        dbHelper.updateTodo(item);
        loadTodos();
    }

    public void addPerformHistory(TodoItem item, String timestamp) {
        dbHelper.addPerformHistory(item.getId(), timestamp);
        loadTodos();
    }

    public List<String> getLast5History(long todoId) {
        return dbHelper.getLast5History(todoId);
    }

    public List<String> getAllHistory(long todoId) {
        return dbHelper.getAllHistory(todoId);
    }

    public void notifyDataChanged() {
        loadTodos();
    }

    public void deleteTodo(int position) {
        List<TodoItem> currentList = todoList.getValue();
        if (currentList != null && position >= 0 && position < currentList.size()) {
            TodoItem item = currentList.get(position);
            AlarmHelper.cancelAlarm(getApplication(), item);
            dbHelper.deleteTodo(item.getId());
            loadTodos();
        }
    }

    public void loadTodos() {
        todoList.setValue(dbHelper.getAllTodos());
    }

    public void backupData() {
        File backupFile = new File(getApplication().getCacheDir(), "todos_backup.dat");
        try (FileOutputStream fos = new FileOutputStream(backupFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            BackupData data = new BackupData(
                    dbHelper.getAllTodos(),
                    dbHelper.getAllReadingNotes(),
                    dbHelper.getAllTodayTasks(),
                    dbHelper.getAllMemorizations(),
                    dbHelper.getFullHistoryForBackup(),
                    dbHelper.getAllMemos(),
                    dbHelper.getAllSecretNotes()
            );
            oos.writeObject(data);
        } catch (Exception e) {
            Log.e("TodoViewModel", "Backup failed", e);
        }
    }

    public void restoreData(Object rawData) {
        if (rawData instanceof BackupData) {
            BackupData data = (BackupData) rawData;
            dbHelper.clearAllTodos();
            dbHelper.clearAllReadingNotes();
            dbHelper.clearAllTodayTasks();
            dbHelper.clearAllMemorizations();
            dbHelper.clearAllMemos();
            dbHelper.clearSecretNotes(); // Need to add this to helper

            if (data.todos != null) for (TodoItem item : data.todos) dbHelper.addTodo(item);
            if (data.readingNotes != null) for (ReadingNoteItem item : data.readingNotes) dbHelper.addReadingNote(item);
            if (data.todayTasks != null) for (TodayTaskItem item : data.todayTasks) dbHelper.addTodayTask(item);
            if (data.memorizations != null) for (MemorizationItem item : data.memorizations) dbHelper.addMemorization(item);
            if (data.memos != null) for (MemoItem item : data.memos) dbHelper.addMemo(item);
            if (data.secretNotes != null) for (SecretNoteItem item : data.secretNotes) dbHelper.addSecretNote(item);
            if (data.performHistory != null) for (PerformHistoryItem item : data.performHistory) dbHelper.addRawHistory(item);
            
            loadTodos();
        } else if (rawData instanceof List) {
            // Backward compatibility for old backup format (List<TodoItem>)
            List<TodoItem> items = (List<TodoItem>) rawData;
            dbHelper.clearAllTodos();
            for (TodoItem item : items) dbHelper.addTodo(item);
            loadTodos();
        }
    }
}
