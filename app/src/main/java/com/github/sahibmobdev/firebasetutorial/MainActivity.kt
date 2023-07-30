package com.github.sahibmobdev.firebasetutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.sahibmobdev.firebasetutorial.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
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
        subscribesToRealTimeUpdate()
/*        binding.btnRetrieveData.setOnClickListener {
            retrievePersons()
        }*/

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

    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = personCollectionRef.get().await()
            val sb = StringBuilder()
            for (document in querySnapshot) {
                val person = document.toObject<Person>()
                sb.append("$person\n")
            }
            withContext(Dispatchers.Main) {
                binding.tvPersons.text = sb.toString()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun subscribesToRealTimeUpdate() {
        personCollectionRef.addSnapshotListener { value, error ->
            error?.let {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            value?.let {
                val sb = StringBuilder()
                for (document in it) {
                    val person = document.toObject<Person>()
                    sb.append(person)
                }
                binding.tvPersons.text = sb.toString()
            }
        }
    }
}