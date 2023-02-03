package com.example.contactpermission

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.contactpermission.adapters.RvAdapter
import com.example.contactpermission.databases.MyDbHelper
import com.example.contactpermission.databinding.ActivityMainBinding
import com.example.contactpermission.models.ContactType
import com.github.florent37.runtimepermission.kotlin.askPermission


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        window.statusBarColor = Color.parseColor("#FB8C00")

    }



}
