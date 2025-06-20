package com.example.droiddevs

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var pageAdapter: PageAdapter
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var viewModel: NotionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[NotionViewModel::class.java]
        initializeViews()
        setupRecyclerView()
        setupBottomNavigation()
        setupObservers()
        // No need to call loadPages() anymore! The LiveData does it.
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerViewPages)
        bottomNav = findViewById(R.id.bottom_navigation)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        pageAdapter = PageAdapter(
            onPageClick = { page ->
                val intent = Intent(this, PageEditorActivity::class.java)
                intent.putExtra("page_id", page.id)
                startActivity(intent)
            },
            onPageLongClick = { /* Can add context menu later */ }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = pageAdapter
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_add_page -> { createNewPage(); true }
                R.id.nav_deleted -> { startActivity(Intent(this, DeletedActivity::class.java)); true }
                R.id.nav_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun setupObservers() {
        // This observer will now fire automatically whenever the pages table changes.
        viewModel.pages.observe(this) { pages ->
            pageAdapter.submitList(pages)
        }
    }

    private fun createNewPage() {
        val intent = Intent(this, PageEditorActivity::class.java)
        intent.putExtra("create_new", true)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                showProfileMenu(findViewById(R.id.action_profile))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showProfileMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add("Log Out")
            setOnMenuItemClickListener { logOut(); true }
            show()
        }
    }

    private fun logOut() {
        getSharedPreferences("notion_prefs", MODE_PRIVATE).edit().clear().apply()
        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Ensure the home icon is selected when returning
        bottomNav.menu.findItem(R.id.nav_home).isChecked = true
    }
}