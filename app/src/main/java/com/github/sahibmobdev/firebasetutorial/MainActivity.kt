package com.github.sahibmobdev.firebasetutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.github.sahibmobdev.firebasetutorial.databinding.ActivityMainBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
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
                val person = oldPersonData()
                savePerson(person)
            }
        }

        binding.btnRetrieveData.setOnClickListener {
            retrievePersons()
        }

        binding.btnUpdatePerson.setOnClickListener {
            val oldPerson = oldPersonData()
            val newPerson = newPersonData()
            setNewPersonData(oldPerson, newPerson)
        }

        binding.btnDeletePerson.setOnClickListener {
            val person = oldPersonData()
            deletePersonData(person)
        }

        binding.btnBatchWrite.setOnClickListener {
            changeName("ATBBGdNdJFhSatM7RB5z", "Mirtiz", "Mirtizov")
        }

    }

    private fun changeName(
        personId: String,
        newFirstName: String,
        newLastName: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runBatch {batch ->
                val personRef = personCollectionRef.document(personId)
                batch.update(personRef, "firstName", newFirstName)
                batch.update(personRef, "lastName", newLastName)
            }.await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deletePersonData(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val personQuery = personCollectionRef
                .whereEqualTo("firstName" , person.firstName)
                .whereEqualTo("lastName", person.lastName)
                .whereEqualTo("age", person.age)
                .get()
                .await()
            if (personQuery.documents.isNotEmpty()) {
                for (document in personQuery)  {
                    personCollectionRef.document(document.id).delete().await()
/*                    personCollectionRef.document(document.id).update(
                        mapOf( "firstName" to FieldValue.delete())
                    )*/
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "No person matched the query", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun oldPersonData(): Person {
        binding.apply {
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val age = etAge.text.toString().toInt()
            return Person(firstName, lastName, age)

        }
    }

    private fun newPersonData(): Map<String, Any> {
        binding.apply {
            val newFirstName = etNewFirstName.text.toString()
            val newLastName = etNewLastName.text.toString()
            val newAge = etNewAge.text.toString()
            val map = mutableMapOf<String, Any>()
            if (newFirstName.isNotEmpty()) {
                map["firstName"] = newFirstName
            }
            if (newLastName.isNotEmpty()) {
                map["lastName"] = newLastName
            }
            if (newAge.isNotEmpty()) {
                map["age"] = newAge
            }
            return map
        }
    }

    private fun setNewPersonData(person: Person, newPerson: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personCollectionRef
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    personCollectionRef.document(document.id).set(
                        newPerson,
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "No person matched the query", Toast.LENGTH_SHORT).show()
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

    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        val fromAge = binding.etFrom.text.toString().toInt()
        val toAge = binding.etTo.text.toString().toInt()
        try {
            val querySnapshot = personCollectionRef
                .whereGreaterThan("age", fromAge)
                .whereLessThan("age", toAge)
                .orderBy("age")
                .get()
                .await()
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