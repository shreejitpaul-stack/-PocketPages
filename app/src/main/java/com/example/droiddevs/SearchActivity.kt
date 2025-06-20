package com.example.droiddevs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.droiddevs.R

class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Search"
    }
}