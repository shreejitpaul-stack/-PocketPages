package com.example.droiddevs

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class DeletedActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var pageAdapter: PageAdapter
    private lateinit var viewModel: DeletedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deleted)
        viewModel = ViewModelProvider(this)[DeletedViewModel::class.java]
        initializeViews()
        setupRecyclerView()
        setupObservers()
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Bin"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById(R.id.recyclerViewDeletedPages)
    }

    private fun setupRecyclerView() {
        pageAdapter = PageAdapter(
            onPageClick = { page -> showDeletedPageOptions(page) },
            onPageLongClick = { page -> showDeletedPageOptions(page) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = pageAdapter
    }

    private fun setupObservers() {
        viewModel.deletedPages.observe(this) { deletedPages ->
            pageAdapter.submitList(deletedPages)
        }
    }

    private fun showDeletedPageOptions(page: Page) {
        AlertDialog.Builder(this)
            .setTitle(page.title.ifEmpty { "Untitled" })
            .setItems(arrayOf("Restore", "Delete Permanently")) { dialog, which ->
                when (which) {
                    0 -> {
                        viewModel.restorePage(page.id)
                        Toast.makeText(this, "'${page.title}' restored.", Toast.LENGTH_SHORT).show()
                    }
                    1 -> showPermanentDeleteConfirmation(page)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermanentDeleteConfirmation(page: Page) {
        AlertDialog.Builder(this)
            .setTitle("Delete Permanently?")
            .setMessage("This action cannot be undone.")
            .setPositiveButton("Delete Forever") { _, _ ->
                viewModel.deletePagePermanently(page.id)
                Toast.makeText(this, "Page permanently deleted.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}