package com.example.santeindexproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.santeindexproject.ui.theme.SanteIndexProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SanteIndexProjectTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: SanteViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsState()
    if (currentUser == null) {
        AuthScreen(viewModel)
    } else {
        MainScreen(viewModel)
    }
}

@Composable
fun AuthScreen(viewModel: SanteViewModel) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Vendor Login" else "Vendor Sign Up",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        if (!isLogin) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Vendor Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("Shop Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Market Location") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (isLogin) {
                    viewModel.login(email, password) { success, error ->
                        if (!success) Toast.makeText(context, error ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (name.isBlank() || shopName.isBlank() || location.isBlank()) {
                        Toast.makeText(context, "Please fill profile details", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.signUp(email, password, name, shopName, location) { success, error ->
                        if (!success) Toast.makeText(context, error ?: "Sign up failed", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLogin) "Login" else "Register")
        }

        TextButton(onClick = { isLogin = !isLogin }) {
            Text(if (isLogin) "New here? Sign up" else "Already have an account? Login")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SanteViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val commodities by viewModel.commodities.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.AutoMirrored.Filled.List, "Watch") }, label = { Text("Watch") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Calculate, "Calc") }, label = { Text("Profit") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.Monitor, "Board") }, label = { Text("Board") })
                NavigationBarItem(selected = selectedTab == 3, onClick = { selectedTab = 3 }, icon = { Icon(Icons.Default.Person, "Profile") }, label = { Text("Profile") })
                if (userProfile?.isAdmin == true) {
                    NavigationBarItem(selected = selectedTab == 4, onClick = { selectedTab = 4 }, icon = { Icon(Icons.Default.AdminPanelSettings, "Admin") }, label = { Text("Admin") })
                }
            }
        }
    ) { p ->
        Box(Modifier.padding(p).fillMaxSize()) {
            when (selectedTab) {
                0 -> PriceWatchScreen(commodities)
                1 -> ProfitCalcScreen(viewModel, commodities)
                2 -> PriceBoardScreen(viewModel, commodities)
                3 -> ProfileScreen(viewModel, userProfile)
                4 -> AdminScreen(viewModel)
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: SanteViewModel, profile: UserProfile?) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AccountCircle, null, Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Vendor Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        ProfileItem("Vendor Name", profile?.name ?: "Loading...")
        ProfileItem("Shop Name", profile?.shopName ?: "Loading...")
        ProfileItem("Market Location", profile?.location ?: "Loading...")

        Spacer(Modifier.weight(1f))

        Button(onClick = { viewModel.logout() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: SanteViewModel) {
    var vegName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedTrend by remember { mutableStateOf(Trend.STABLE) }
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Admin Panel", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Update Live Market Prices", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = vegName, onValueChange = { vegName = it }, label = { Text("Vegetable Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Mandi Price (₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        Text("Trend:", Modifier.padding(top = 16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Trend.entries.forEach { trend ->
                FilterChip(selected = selectedTrend == trend, onClick = { selectedTrend = trend }, label = { Text(trend.name) })
            }
        }

        Button(onClick = {
            val p = price.toDoubleOrNull() ?: 0.0
            if (vegName.isNotBlank() && p > 0) {
                viewModel.addOrUpdateCommodity(vegName, p, selectedTrend)
                vegName = ""; price = ""
                Toast.makeText(context, "Market Updated!", Toast.LENGTH_SHORT).show()
            }
        }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text("Update Market Data")
        }
    }
}

@Composable
fun PriceWatchScreen(commodities: List<Commodity>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Live Mandi Prices", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (commodities.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Connecting to market...")
            }
        } else {
            LazyColumn {
                items(commodities) { commodity ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(commodity.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                val color = when(commodity.trend) {
                                    Trend.UP -> Color.Red
                                    Trend.DOWN -> Color.Green
                                    Trend.STABLE -> Color.Gray
                                }
                                Text("Trend: ${commodity.trend}", color = color)
                            }
                            Text("₹${commodity.mandiPrice}/${commodity.unit}", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfitCalcScreen(viewModel: SanteViewModel, commodities: List<Commodity>) {
    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Profit Calculator", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = viewModel.transportCost.value, onValueChange = { viewModel.transportCost.value = it }, label = { Text("Total Transport Cost (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = viewModel.wastePercent.value, onValueChange = { viewModel.wastePercent.value = it }, label = { Text("Waste/Loss %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(24.dp))
        Text("Suggested Selling Prices (RRP):", fontWeight = FontWeight.Bold)

        commodities.forEach { commodity ->
            val rrp = viewModel.calculateRRP(commodity.mandiPrice)
            ListItem(headlineContent = { Text(commodity.name) }, trailingContent = { Text("₹${rrp.toInt()}/kg", fontSize = 20.sp, fontWeight = FontWeight.Bold) })
        }
    }
}

@Composable
fun PriceBoardScreen(viewModel: SanteViewModel, commodities: List<Commodity>) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Todays Price", color = Color.Yellow, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
        HorizontalDivider(color = Color.Yellow, thickness = 3.dp, modifier = Modifier.padding(vertical = 20.dp))
        commodities.forEach { commodity ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(commodity.name.uppercase(), color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                val price = viewModel.calculateRRP(commodity.mandiPrice)
                Text("₹${price.toInt()}", color = Color.Yellow, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}