package com.example.droiddevs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Stack

class PageEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NotionRepository(application)

    private val _pageState = MutableLiveData<Page?>()
    val pageState: LiveData<Page?> = _pageState

    private val undoStack = Stack<Page>()
    private val redoStack = Stack<Page>()
    private val _canUndo = MutableLiveData<Boolean>(false)
    val canUndo: LiveData<Boolean> = _canUndo
    private val _canRedo = MutableLiveData<Boolean>(false)
    val canRedo: LiveData<Boolean> = _canRedo

    private var lastUndoableState: Page? = null
    private var isUndoingOrRedoing = false

    private var autoSaveJob: Job? = null
    private var historySnapshotJob: Job? = null
    private val debounceDelayMs = 2000L

    // Add cursor position restoration callback
    private val _restoreCursorPosition = MutableLiveData<Pair<String?, Int>?>()
    val restoreCursorPosition: LiveData<Pair<String?, Int>?> = _restoreCursorPosition

    fun loadPage(pageId: String) {
        viewModelScope.launch {
            try {
                _pageState.value = repository.getPage(pageId)
                clearHistory()
            } catch (e: Exception) {
                _pageState.value = null
            }
        }
    }

    fun createNewPage() {
        viewModelScope.launch {
            try {
                val newPage = Page(title = "", blocks = mutableListOf(Block(type = BlockType.TEXT, content = "")))
                repository.savePage(newPage)
                _pageState.value = newPage
                clearHistory()
            } catch (e: Exception) {
                _pageState.value = null
            }
        }
    }

    private fun captureCurrentCursorState(page: Page): Page {
        // This will be called from the Activity to update cursor positions before state changes
        return page.copy()
    }

    fun updateCursorState(titleCursorPos: Int, focusedBlockId: String?, blockCursorPos: Int) {
        _pageState.value?.let { page ->
            _pageState.value = page.copy(
                titleCursorPosition = titleCursorPos,
                focusedBlockId = focusedBlockId,
                focusedBlockCursorPosition = blockCursorPos
            )
        }
    }

    private fun onStateChanged(newState: Page) {
        if (isUndoingOrRedoing) return

        if (lastUndoableState == null) {
            lastUndoableState = _pageState.value?.copy(
                blocks = _pageState.value!!.blocks.map { it.copy() }.toMutableList()
            )
            updateUndoRedoButtonsState()
        }

        _pageState.value = newState

        historySnapshotJob?.cancel()
        autoSaveJob?.cancel()

        historySnapshotJob = viewModelScope.launch {
            delay(debounceDelayMs)
            commitInProgressChangeToHistory()
        }
        autoSaveJob = viewModelScope.launch {
            delay(debounceDelayMs)
            savePageToDatabase()
        }
    }

    private fun commitInProgressChangeToHistory() {
        lastUndoableState?.let { state ->
            undoStack.push(state)
            redoStack.clear()
            updateUndoRedoButtonsState()
        }
        lastUndoableState = null
        historySnapshotJob = null
    }

    fun updateTitle(newTitle: String) {
        _pageState.value?.let { page ->
            if (page.title != newTitle) {
                onStateChanged(page.copy(title = newTitle))
            }
        }
    }

    fun updateBlockContent(position: Int, newContent: String) {
        _pageState.value?.let { page ->
            if (position in page.blocks.indices && page.blocks[position].content != newContent) {
                val updatedBlocks = page.blocks.map { it.copy() }.toMutableList()
                updatedBlocks[position] = updatedBlocks[position].copy(content = newContent)
                onStateChanged(page.copy(blocks = updatedBlocks))
            }
        }
    }

    fun updateTodoCheckbox(position: Int, isChecked: Boolean) {
        _pageState.value?.let { page ->
            if (position in page.blocks.indices) {
                val updatedBlocks = page.blocks.map { it.copy() }.toMutableList()
                val oldBlock = updatedBlocks[position]
                val currentCompleted = oldBlock.properties["completed"] as? Boolean ?: false

                if (currentCompleted != isChecked) {
                    val newProperties = oldBlock.properties.toMutableMap().apply { this["completed"] = isChecked }
                    updatedBlocks[position] = oldBlock.copy(properties = newProperties)
                    onStateChanged(page.copy(blocks = updatedBlocks))
                }
            }
        }
    }

    fun addBlock(position: Int, blockType: BlockType) {
        _pageState.value?.let { page ->
            val updatedBlocks = page.blocks.map { it.copy() }.toMutableList()
            val insertPosition = if (position < 0) page.blocks.size else position + 1
            updatedBlocks.add(insertPosition, Block(type = blockType))
            onStateChanged(page.copy(blocks = updatedBlocks))
        }
    }

    fun deleteBlock(position: Int) {
        _pageState.value?.let { page ->
            if (position in page.blocks.indices && page.blocks.size > 1) {
                val updatedBlocks = page.blocks.map { it.copy() }.toMutableList()
                updatedBlocks.removeAt(position)
                onStateChanged(page.copy(blocks = updatedBlocks))
            }
        }
    }

    fun undo() {
        if (!canUndo.value!!) return
        isUndoingOrRedoing = true
        try {
            val targetState = if (lastUndoableState != null) {
                _pageState.value?.let { current ->
                    redoStack.push(current.copy(blocks = current.blocks.map { b -> b.copy() }.toMutableList()))
                }
                val state = lastUndoableState?.copy(blocks = lastUndoableState!!.blocks.map { b -> b.copy() }.toMutableList())
                lastUndoableState = null
                state
            } else if (undoStack.isNotEmpty()) {
                _pageState.value?.let { current ->
                    redoStack.push(current.copy(blocks = current.blocks.map { b -> b.copy() }.toMutableList()))
                }
                undoStack.pop()
            } else null

            targetState?.let { state ->
                _pageState.value = state
                // Trigger cursor restoration
                _restoreCursorPosition.value = Pair(state.focusedBlockId, state.focusedBlockCursorPosition)
            }

            finalizeHistoryAction()
        } finally {
            isUndoingOrRedoing = false
        }
    }

    fun redo() {
        if (!canRedo.value!! || redoStack.isEmpty()) return
        isUndoingOrRedoing = true
        try {
            _pageState.value?.let { current ->
                undoStack.push(current.copy(blocks = current.blocks.map { b -> b.copy() }.toMutableList()))
            }
            val targetState = redoStack.pop()
            _pageState.value = targetState

            // Trigger cursor restoration
            _restoreCursorPosition.value = Pair(targetState.focusedBlockId, targetState.focusedBlockCursorPosition)

            finalizeHistoryAction()
        } finally {
            isUndoingOrRedoing = false
        }
    }

    fun clearCursorRestoreSignal() {
        _restoreCursorPosition.value = null
    }

    private fun finalizeHistoryAction() {
        historySnapshotJob?.cancel()
        autoSaveJob?.cancel()
        lastUndoableState = null
        updateUndoRedoButtonsState()
        savePageToDatabase()
    }

    fun flushAndSave() {
        historySnapshotJob?.cancel()
        autoSaveJob?.cancel()
        commitInProgressChangeToHistory()
        savePageToDatabase()
    }

    private fun savePageToDatabase() {
        _pageState.value?.let { page -> viewModelScope.launch { repository.savePage(page.copy(updatedAt = Date())) } }
    }

    fun softDeletePage(): Job? {
        flushAndSave()
        return _pageState.value?.let { page -> viewModelScope.launch { repository.softDeletePage(page.id) } }
    }

    private fun updateUndoRedoButtonsState() {
        _canUndo.value = undoStack.isNotEmpty() || lastUndoableState != null
        _canRedo.value = redoStack.isNotEmpty()
    }

    private fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
        lastUndoableState = null
        updateUndoRedoButtonsState()
    }

    override fun onCleared() {
        super.onCleared()
        historySnapshotJob?.cancel()
        autoSaveJob?.cancel()
    }
}