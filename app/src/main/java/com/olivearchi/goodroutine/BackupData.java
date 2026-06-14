package com.olivearchi.goodroutine;

import java.io.Serializable;
import java.util.List;

public class BackupData implements Serializable {
    private static final long serialVersionUID = 1L;

    public List<TodoItem> todos;
    public List<ReadingNoteItem> readingNotes;
    public List<TodayTaskItem> todayTasks;
    public List<MemorizationItem> memorizations;
    public List<PerformHistoryItem> performHistory;
    public List<MemoItem> memos;
    public List<SecretNoteItem> secretNotes;

    public BackupData(List<TodoItem> todos, List<ReadingNoteItem> readingNotes, List<TodayTaskItem> todayTasks, List<MemorizationItem> memorizations, List<PerformHistoryItem> performHistory, List<MemoItem> memos, List<SecretNoteItem> secretNotes) {
        this.todos = todos;
        this.readingNotes = readingNotes;
        this.todayTasks = todayTasks;
        this.memorizations = memorizations;
        this.performHistory = performHistory;
        this.memos = memos;
        this.secretNotes = secretNotes;
    }
}
