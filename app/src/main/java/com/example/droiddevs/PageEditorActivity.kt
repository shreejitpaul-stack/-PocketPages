package com.example.droiddevs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class PageEditorActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var etPageTitle: EditText
    private lateinit var recyclerViewBlocks: RecyclerView
    private lateinit var fabAddBlock: FloatingActionButton
    private lateinit var blockAdapter: BlockAdapter
    private lateinit var viewModel: PageEditorViewModel
    private var isNewPage: Boolean = false

    private var undoMenuItem: MenuItem? = null
    private var redoMenuItem: MenuItem? = null

    private var isApplyingStateChange = false
    private var lastFocusedBlockPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_editor)

        val pageId = intent.getStringExtra("page_id")
        isNewPage = intent.getBooleanExtra("create_new", false)

        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, factory)[PageEditorViewModel::class.java]

        initializeViews()
        setupRecyclerView()
        setupObservers()

        if (isNewPage) {
            if (viewModel.pageState.value == null) {
                viewModel.createNewPage()
            }
        } else {
            pageId?.let {
                if (viewModel.pageState.value == null) {
                    viewModel.loadPage(it)
                }
            } ?: run {
                Toast.makeText(this, "Error: Page ID not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        etPageTitle = findViewById(R.id.etPageTitle)
        recyclerViewBlocks = findViewById(R.id.recyclerViewBlocks)
        fabAddBlock = findViewById(R.id.fabAddBlock)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        etPageTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (etPageTitle.hasFocus() && !isApplyingStateChange) {
                    viewModel.updateTitle(s.toString())
                }
            }
        })

        // Add focus change listener to title to track cursor position
        etPageTitle.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                updateCursorStateInViewModel()
            }
        }

        fabAddBlock.setOnClickListener {
            showAddBlockMenu(lastFocusedBlockPosition)
        }
    }

    private fun updateCursorStateInViewModel() {
        val titleCursorPos = if (etPageTitle.hasFocus()) etPageTitle.selectionStart else -1
        val focusedBlockId = getCurrentFocusedBlockId()
        val blockCursorPos = getCurrentFocusedBlockCursorPosition()

        viewModel.updateCursorState(titleCursorPos, focusedBlockId, blockCursorPos)
    }

    private fun getCurrentFocusedBlockId(): String? {
        if (lastFocusedBlockPosition >= 0) {
            val page = viewModel.pageState.value
            return if (page != null && lastFocusedBlockPosition < page.blocks.size) {
                page.blocks[lastFocusedBlockPosition].id
            } else null
        }
        return null
    }

    private fun getCurrentFocusedBlockCursorPosition(): Int {
        if (lastFocusedBlockPosition >= 0) {
            val vh = recyclerViewBlocks.findViewHolderForAdapterPosition(lastFocusedBlockPosition)
            val et = vh?.itemView?.findViewById<EditText>(R.id.etBlockContent)
            return if (et?.hasFocus() == true) et.selectionStart else -1
        }
        return -1
    }

    private fun setupRecyclerView() {
        blockAdapter = BlockAdapter(
            onBlockContentChanged = { pos, content ->
                if (!isApplyingStateChange) {
                    viewModel.updateBlockContent(pos, content)
                }
            },
            onBlockDeleted = { pos ->
                if (!isApplyingStateChange) {
                    viewModel.deleteBlock(pos)
                }
            },
            onCheckboxChanged = { pos, isChecked ->
                if (!isApplyingStateChange) {
                    viewModel.updateTodoCheckbox(pos, isChecked)
                }
            },
            onRequestFocus = { pos, cursorPos ->
                recyclerViewBlocks.post {
                    requestFocusAtPosition(pos, cursorPos)
                }
            },
            isProgrammaticUpdate = { isApplyingStateChange },
            onBlockFocused = { position ->
                lastFocusedBlockPosition = position
                updateCursorStateInViewModel()
            }
        )
        recyclerViewBlocks.adapter = blockAdapter
        recyclerViewBlocks.layoutManager = LinearLayoutManager(this)
    }

    private fun requestFocusAtPosition(position: Int, cursorPosition: Int) {
        val vh = recyclerViewBlocks.findViewHolderForAdapterPosition(position)
        val et = vh?.itemView?.findViewById<EditText>(R.id.etBlockContent)
        et?.requestFocus()
        val positionToSet = if (cursorPosition == -1) et?.text?.length ?: 0 else cursorPosition
        et?.setSelection(minOf(positionToSet, et.text?.length ?: 0))
        lastFocusedBlockPosition = position
    }

    private fun setupObservers() {
        viewModel.pageState.observe(this) { page ->
            if (page == null) return@observe
            isApplyingStateChange = true

            if (etPageTitle.text.toString() != page.title) {
                etPageTitle.setText(page.title)
            }

            blockAdapter.submitList(page.blocks.toMutableList()) {
                recyclerViewBlocks.post {
                    isApplyingStateChange = false
                }
            }
        }

        // New observer for cursor position restoration
        viewModel.restoreCursorPosition.observe(this) { cursorInfo ->
            cursorInfo?.let { (focusedBlockId, cursorPosition) ->
                restoreCursorPosition(focusedBlockId, cursorPosition)
                viewModel.clearCursorRestoreSignal()
            }
        }

        viewModel.canUndo.observe(this) { canUndo -> updateUndoState(canUndo) }
        viewModel.canRedo.observe(this) { canRedo -> updateRedoState(canRedo) }
    }

    private fun restoreCursorPosition(focusedBlockId: String?, cursorPosition: Int) {
        recyclerViewBlocks.post {
            if (focusedBlockId != null) {
                // Find the position of the block with the given ID
                val page = viewModel.pageState.value
                val blockPosition = page?.blocks?.indexOfFirst { it.id == focusedBlockId } ?: -1

                if (blockPosition >= 0) {
                    // Scroll to the position if needed
                    recyclerViewBlocks.scrollToPosition(blockPosition)

                    // Request focus and set cursor position
                    recyclerViewBlocks.post {
                        requestFocusAtPosition(blockPosition, cursorPosition)
                    }
                }
            } else {
                // If no specific block was focused, check if title should be focused
                val page = viewModel.pageState.value
                if (page?.titleCursorPosition != -1) {
                    etPageTitle.requestFocus()
                    val titleCursorPos = page?.titleCursorPosition ?: 0
                    etPageTitle.setSelection(minOf(titleCursorPos, etPageTitle.text?.length ?: 0))
                }
            }
        }
    }

    private fun showAddBlockMenu(position: Int) {
        val blockTypes = arrayOf("Text", "Heading 1", "Heading 2", "To-do", "Quote", "Bullet List")
        AlertDialog.Builder(this)
            .setTitle("Add Block")
            .setItems(blockTypes) { _, which ->
                val blockType = when(which) {
                    0 -> BlockType.TEXT
                    1 -> BlockType.HEADING_1
                    2 -> BlockType.HEADING_2
                    3 -> BlockType.TODO
                    4 -> BlockType.QUOTE
                    5 -> BlockType.BULLET_LIST
                    else -> BlockType.TEXT
                }
                viewModel.addBlock(position, blockType)
            }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.page_editor_menu, menu)
        undoMenuItem = menu.findItem(R.id.action_undo)
        redoMenuItem = menu.findItem(R.id.action_redo)

        viewModel.canUndo.value?.let { updateUndoState(it) }
        viewModel.canRedo.value?.let { updateRedoState(it) }
        return true
    }

    private fun updateUndoState(isEnabled: Boolean) {
        undoMenuItem?.isEnabled = isEnabled
        undoMenuItem?.icon?.alpha = if (isEnabled) 255 else 130
    }

    private fun updateRedoState(isEnabled: Boolean) {
        redoMenuItem?.isEnabled = isEnabled
        redoMenuItem?.icon?.alpha = if (isEnabled) 255 else 130
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_undo -> {
                if (item.isEnabled) {
                    updateCursorStateInViewModel() // Capture current cursor state before undo
                    viewModel.undo()
                }
                true
            }
            R.id.action_redo -> {
                if (item.isEnabled) {
                    updateCursorStateInViewModel() // Capture current cursor state before redo
                    viewModel.redo()
                }
                true
            }
            R.id.action_delete -> { deletePageWithConfirmation(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun deletePageWithConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Move to Bin")
            .setMessage("Are you sure you want to move this page to the bin?")
            .setPositiveButton("Move to Bin") { _, _ ->
                isApplyingStateChange = true
                lifecycleScope.launch {
                    try {
                        viewModel.softDeletePage()?.join()
                        Toast.makeText(this@PageEditorActivity, "Page moved to bin", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        isApplyingStateChange = false
                        Toast.makeText(this@PageEditorActivity, "Error deleting page", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        if (!isApplyingStateChange) {
            updateCursorStateInViewModel() // Save cursor state before pausing
            viewModel.flushAndSave()
        }
    }
}