package com.github.sahibmobdev.firebasetutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.sahibmobdev.firebasetutorial.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val personCollectionRef = Firebase.firestore.collection("person")
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUploadData.setOnClickListener {
            binding.apply {
                val firstName = etFirstName.text.toString()
                val lastName = etLastName.text.toString()
                val age = etAge.text.toString().toInt()
                val person = Person(firstName, lastName, age)
                savePerson(person)
            }
        }

    }

    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Successfully saved data.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
           withContext(Dispatchers.Main) {
               Toast.makeText(this@MainActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
           }
        }
    }
}