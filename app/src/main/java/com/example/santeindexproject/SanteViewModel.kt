package com.example.santeindexproject

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val name: String = "",
    val shopName: String = "",
    val location: String = "",
    val isAdmin: Boolean = false
)

class SanteViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.database.reference
    private val pricesRef = db.child("daily_prices")
    private val usersRef = db.child("users")

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUser

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _commodities = MutableStateFlow<List<Commodity>>(emptyList())
    val commodities: StateFlow<List<Commodity>> = _commodities

    var transportCost = mutableStateOf("100")
    var wastePercent = mutableStateOf("5")

    init {
        fetchPrices()
        auth.currentUser?.let { fetchUserProfile(it.uid) }
    }

    private fun fetchPrices() {
        pricesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Commodity>()
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    // Fallback to show items even if Firebase is empty
                    list.addAll(listOf(
                        Commodity("Onion", 20.0, trend = Trend.UP),
                        Commodity("Tomato", 45.0, trend = Trend.DOWN),
                        Commodity("Potato", 15.0, trend = Trend.STABLE),
                        Commodity("Chilli", 80.0, trend = Trend.UP)
                    ))
                } else {
                    for (data in snapshot.children) {
                        val name = data.child("name").getValue(String::class.java) ?: ""
                        val price = data.child("mandiPrice").getValue(Double::class.java) ?: 0.0
                        val trendStr = data.child("trend").getValue(String::class.java) ?: "STABLE"
                        val trend = try { Trend.valueOf(trendStr) } catch(e: Exception) { Trend.STABLE }
                        list.add(Commodity(name, price, trend = trend))
                    }
                }
                _commodities.value = list
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchUserProfile(uid: String) {
        usersRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserProfile::class.java)
                _userProfile.value = profile ?: UserProfile("Vendor", "Shop Name", "Market")
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun signUp(email: String, pass: String, name: String, shop: String, loc: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: ""
                val profile = UserProfile(name, shop, loc, isAdmin = false)
                usersRef.child(uid).setValue(profile).addOnCompleteListener {
                    _currentUser.value = auth.currentUser
                    fetchUserProfile(uid)
                    onResult(true, null)
                }
            } else onResult(false, task.exception?.message)
        }
    }

    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: ""
                _currentUser.value = auth.currentUser
                fetchUserProfile(uid)
                onResult(true, null)
            } else onResult(false, task.exception?.message)
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userProfile.value = null
    }

    fun addOrUpdateCommodity(name: String, price: Double, trend: Trend) {
        val updates = mapOf(
            "name" to name,
            "mandiPrice" to price,
            "trend" to trend.name
        )
        pricesRef.child(name).setValue(updates)
    }

    fun calculateRRP(mandiPrice: Double): Double {
        val transport = transportCost.value.toDoubleOrNull() ?: 0.0
        val waste = wastePercent.value.toDoubleOrNull() ?: 0.0
        val transportPerKg = transport / 50.0
        val costWithWaste = mandiPrice * (1 + (waste / 100))
        return (costWithWaste + transportPerKg) * 1.15
    }
}
