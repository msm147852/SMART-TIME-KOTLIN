package com.smarttime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import com.smarttime.app.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 15, 30)
        window.navigationBarColor = android.graphics.Color.rgb(8, 15, 30)
        setContent { SmartTimeTheme { SmartTimeApp() } }
    }
}

private data class Entry(val id: String, val title: String, val detail: String = "", val amount: Double = 0.0, val done: Boolean = false)
private data class Section(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun SmartTimeApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("smart_time_native", android.content.Context.MODE_PRIVATE) }
    var route by remember { mutableStateOf("home") }
    var userName by remember { mutableStateOf(prefs.getString("user_name", "ضيف Smart Time") ?: "ضيف Smart Time") }
    var sessionToken by remember { mutableStateOf(prefs.getString("session_token", "") ?: "") }
    var notes by remember { mutableStateOf(readEntries(prefs, "notes")) }
    var tasks by remember { mutableStateOf(readEntries(prefs, "tasks")) }
    var expenses by remember { mutableStateOf(readEntries(prefs, "expenses")) }
    var notice by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sections = listOf(
        Section("home", "الرئيسية", Icons.Filled.Home),
        Section("notes", "ملاحظات", Icons.Filled.Description),
        Section("tasks", "الأعمال اليومية", Icons.Filled.CheckCircle),
        Section("expenses", "المصروفات", Icons.Filled.AccountBalanceWallet),
        Section("calendar", "التقويم", Icons.Filled.CalendarMonth),
        Section("calculator", "الحاسبة والتحويلات", Icons.Filled.Calculate),
        Section("trips", "الرحلات", Icons.Filled.Map),
        Section("vehicles", "السيارات", Icons.Filled.DirectionsCar),
        Section("education", "التعليم", Icons.Filled.School),
        Section("religious", "المحتوى الديني", Icons.Filled.AutoStories),
        Section("vault", "الخزنة الآمنة", Icons.Filled.Lock),
        Section("ai", "الذكاء الاصطناعي", Icons.Filled.Psychology),
        Section("chat", "الدردشة", Icons.Filled.Chat),
        Section("media", "مركز الوسائط", Icons.Filled.PhotoLibrary),
        Section("sports", "الرياضة", Icons.Filled.SportsSoccer),
        Section("wallet", "المحفظة", Icons.Filled.Wallet),
        Section("food", "الطعام", Icons.Filled.Restaurant),
        Section("hotels", "الفنادق", Icons.Filled.Hotel),
        Section("settings", "الإعدادات", Icons.Filled.Settings)
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = Navy,
            topBar = {
                Row(Modifier.fillMaxWidth().background(NavySurface).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(NavyRaised), contentAlignment = Alignment.Center) {
                            Text("ST", color = Gold, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("Smart Time", color = Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("وقتك من ذهب", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = { route = "home" }) { Icon(Icons.Filled.Home, "الرئيسية", tint = Gold) }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = NavySurface, contentColor = Gold) {
                    listOf(sections[0], sections[3], sections[12], sections.last()).forEach { section ->
                        NavigationBarItem(selected = route == section.route, onClick = { route = section.route },
                            icon = { Icon(section.icon, contentDescription = section.title) },
                            label = { Text(section.title, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Gold, selectedTextColor = Gold,
                                indicatorColor = NavyRaised, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary))
                    }
                }
            },
            snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 14.dp)) {
                if (notice.isNotBlank()) {
                    Surface(color = NavyRaised, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(notice, color = TextPrimary, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            IconButton(onClick = { notice = "" }) { Icon(Icons.Filled.Close, "إغلاق", tint = Gold) }
                        }
                    }
                }
                when (route) {
                    "home" -> HomeScreen(userName, sections, notes.size, tasks.count { it.done }, expenses.sumOf { it.amount }, { route = it })
                    "notes" -> NotesScreen(notes, { notes = it; saveEntries(prefs, "notes", it) })
                    "tasks" -> TasksScreen(tasks, { tasks = it; saveEntries(prefs, "tasks", it) })
                    "expenses" -> ExpensesScreen(expenses, { expenses = it; saveEntries(prefs, "expenses", it) })
                    "calculator" -> CalculatorScreen()
                    "settings" -> SettingsScreen(userName, sessionToken.isNotBlank(), { userName = it; prefs.edit().putString("user_name", it).apply() }, {
                        sessionToken = ""; prefs.edit().remove("session_token").apply(); userName = "ضيف Smart Time"; notice = "تم تسجيل الخروج من الجلسة المحلية."
                    }, { route = "home" })
                    "chat" -> GenericScreen("الدردشة", "واجهة محادثات Smart Time الأصلية تحتاج إلى ربط خدمات الرسائل ومصادقة الخادم.")
                    "ai" -> GenericScreen("مركز الذكاء الاصطناعي", "سيتم توصيل المساعد بخدمة الخادم الحالية دون تضمين مفاتيح سرية داخل التطبيق.")
                    else -> {
                        val title = sections.firstOrNull { it.route == route }?.title ?: "Smart Time"
                        GenericScreen(title, "تم إنشاء نقطة الدخول لهذا القسم. التكامل مع بيانات وخدمات النسخة الأصلية يحتاج مطابقة API الخاص به قبل اعتباره مكتملًا.")
                    }
                }
            }
        }
    }
    // Guest entry is deliberately tolerant of network failures, but reports the real result.
    if (route == "login") LoginScreen(busy, { identifier, password ->
        busy = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) { requestSession("/api/auth/login", JSONObject().put("identifier", identifier).put("password", password).put("role", "").put("turnstileToken", "").toString()) }
                sessionToken = result.optString("token")
                userName = result.optJSONObject("user")?.optString("name", "مستخدم Smart Time") ?: "مستخدم Smart Time"
                prefs.edit().putString("session_token", sessionToken).putString("user_name", userName).apply()
                route = "home"; notice = "تم تسجيل الدخول بنجاح."
            } catch (e: Exception) { notice = e.message ?: "تعذر تسجيل الدخول" }
            finally { busy = false }
        }
    }, {
        busy = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) { requestSession("/api/trial/session", null) }
                sessionToken = result.optString("token")
                userName = result.optJSONObject("user")?.optString("name", "زائر Smart Time") ?: "زائر Smart Time"
                prefs.edit().putString("session_token", sessionToken).putString("user_name", userName).apply()
                route = "home"; notice = "تم الدخول كزائر بنجاح."
            } catch (e: Exception) {
                // Allow a usable local guest workspace even if the server is unavailable.
                sessionToken = "local-guest"
                userName = "زائر Smart Time"
                prefs.edit().putString("session_token", sessionToken).putString("user_name", userName).apply()
                route = "home"; notice = "دخلت كزائر محليًا؛ تعذّر الاتصال بالخادم، وستظل بيانات هذا الجهاز متاحة."
            } finally { busy = false }
        }
    }, { route = "home" })
}

private fun readEntries(prefs: android.content.SharedPreferences, key: String): List<Entry> = try {
    val arr = JSONArray(prefs.getString(key, "[]"))
    (0 until arr.length()).mapNotNull { i ->
        val o = arr.optJSONObject(i) ?: return@mapNotNull null
        Entry(o.optString("id"), o.optString("title"), o.optString("detail"), o.optDouble("amount"), o.optBoolean("done"))
    }
} catch (_: Exception) { emptyList() }

private fun saveEntries(prefs: android.content.SharedPreferences, key: String, entries: List<Entry>) {
    val arr = JSONArray()
    entries.forEach { arr.put(JSONObject().put("id", it.id).put("title", it.title).put("detail", it.detail).put("amount", it.amount).put("done", it.done)) }
    prefs.edit().putString(key, arr.toString()).apply()
}

private fun requestSession(path: String, body: String?): JSONObject {
    val connection = (URL("https://smart-time-production-661f.up.railway.app" + path).openConnection() as HttpURLConnection)
    connection.requestMethod = if (body == null) "GET" else "POST"
    connection.connectTimeout = 12000
    connection.readTimeout = 18000
    if (body != null) {
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
    }
    val code = connection.responseCode
    val stream = if (code in 200..299) connection.inputStream else connection.errorStream
    val raw = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    if (code !in 200..299) {
        val message = try { JSONObject(raw).optString("message").ifBlank { JSONObject(raw).optString("error") } } catch (_: Exception) { "" }
        throw IllegalStateException(message.ifBlank { "تعذر الاتصال بالخادم (" + code + ")" })
    }
    val result = JSONObject(raw)
    if (result.optString("token").isBlank()) throw IllegalStateException("الخادم لم يُرجع رمز جلسة.")
    return result
}

@Composable
private fun HomeScreen(name: String, sections: List<Section>, noteCount: Int, completedTasks: Int, expenseTotal: Double, navigate: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item {
            Surface(color = NavySurface, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Gold.copy(alpha = .45f))) {
                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("مرحبًا، " + name, color = TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("وقتك من ذهب", color = Gold, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("نظّم يومك، تابع مصروفاتك، واحتفظ بكل مهامك في مكان واحد.", color = TextSecondary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard("الملاحظات", noteCount.toString(), Modifier.weight(1f))
                        MetricCard("المهام المكتملة", completedTasks.toString(), Modifier.weight(1f))
                    }
                    MetricCard("المصروفات المسجلة", String.format(java.util.Locale.US, "%.2f ج.م", expenseTotal), Modifier.fillMaxWidth())
                }
            }
        }
        item { Text("الأقسام الرئيسية", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        items(sections.filter { it.route != "home" }.chunked(2)) { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { s ->
                    Surface(modifier = Modifier.weight(1f).clickable { navigate(s.route) }, color = NavySurface, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Gold.copy(alpha = .18f))) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Icon(s.icon, null, tint = Gold, modifier = Modifier.size(24.dp))
                            Text(s.title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item {
            Surface(color = NavySurface, shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().clickable { navigate("tasks") }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsActive, null, tint = Gold)
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text("تذكير", color = Gold, fontWeight = FontWeight.Bold)
                        Text("افتح الأعمال اليومية لمتابعة مهامك.", color = TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.ChevronLeft, null, tint = Gold)
                }
            }
        }
    }
}

@Composable private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = NavyRaised, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = TextSecondary, fontSize = 11.sp)
            Text(value, color = Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun NotesScreen(notes: List<Entry>, onChange: (List<Entry>) -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("ملاحظات", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(title, { title = it }, label = { Text("عنوان الملاحظة") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = smartFieldColors())
        OutlinedTextField(body, { body = it }, label = { Text("اكتب ملاحظتك هنا…") }, modifier = Modifier.fillMaxWidth(), minLines = 3, colors = smartFieldColors())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { if (title.isNotBlank() || body.isNotBlank()) { val item = Entry(editing ?: UUID.randomUUID().toString(), title.ifBlank { "ملاحظة جديدة" }, body); onChange(if (editing == null) listOf(item) + notes else notes.map { if (it.id == editing) item else it }); title = ""; body = ""; editing = null } }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Icon(Icons.Filled.Save, null); Text("حفظ") }
            OutlinedButton(onClick = { title = ""; body = ""; editing = null }) { Text("جديد", color = Gold) }
        }
        Text("المحفوظات", color = Gold, fontWeight = FontWeight.SemiBold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notes, key = { it.id }) { item ->
                Surface(color = NavySurface, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f).clickable { editing = item.id; title = item.title; body = item.detail }) {
                            Text(item.title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(item.detail, color = TextSecondary, maxLines = 2, fontSize = 12.sp)
                        }
                        IconButton(onClick = { onChange(notes.filterNot { it.id == item.id }) }) { Icon(Icons.Filled.Delete, "حذف", tint = Gold) }
                    }
                }
            }
        }
    }
}

@Composable private fun TasksScreen(tasks: List<Entry>, onChange: (List<Entry>) -> Unit) {
    var title by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("الأعمال اليومية — ذكرني", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("مهمة جديدة") }, modifier = Modifier.weight(1f), singleLine = true, colors = smartFieldColors())
            IconButton(onClick = { if (title.isNotBlank()) { onChange(listOf(Entry(UUID.randomUUID().toString(), title.trim())) + tasks); title = "" } }) { Icon(Icons.Filled.Add, "إضافة", tint = Gold) }
        }
        Text(tasks.count { it.done }.toString() + " من " + tasks.size + " مهام مكتملة", color = TextSecondary)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tasks, key = { it.id }) { task ->
                Surface(color = NavySurface, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(task.done, { checked -> onChange(tasks.map { if (it.id == task.id) it.copy(done = checked) else it }) }, colors = CheckboxDefaults.colors(checkedColor = Gold))
                        Text(task.title, color = if (task.done) TextSecondary else TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onChange(tasks.filterNot { it.id == task.id }) }) { Icon(Icons.Filled.Delete, "حذف", tint = Gold) }
                    }
                }
            }
        }
    }
}

@Composable private fun ExpensesScreen(expenses: List<Entry>, onChange: (List<Entry>) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عام") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("المصروفات والميزانية", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        MetricCard("إجمالي المصروفات", String.format(java.util.Locale.US, "%.2f ج.م", expenses.sumOf { it.amount }), Modifier.fillMaxWidth())
        OutlinedTextField(title, { title = it }, label = { Text("بند المصروف") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = smartFieldColors())
        OutlinedTextField(amount, { amount = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("المبلغ بالجنيه") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true, colors = smartFieldColors())
        OutlinedTextField(category, { category = it }, label = { Text("التصنيف") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = smartFieldColors())
        Button(onClick = { val value = amount.toDoubleOrNull(); if (title.isNotBlank() && value != null && value >= 0) { onChange(listOf(Entry(UUID.randomUUID().toString(), title.trim(), category, value)) + expenses); title = ""; amount = "" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("تسجيل المصروف") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses, key = { it.id }) { item ->
                Surface(color = NavySurface, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(item.title, color = TextPrimary); Text(item.detail, color = TextSecondary, fontSize = 12.sp) }
                        Text(String.format(java.util.Locale.US, "%.2f ج.م", item.amount), color = Gold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable private fun CalculatorScreen() {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("الحاسبة الذكية", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(expression, { expression = it }, modifier = Modifier.fillMaxWidth(), label = { Text("العملية الحسابية") }, colors = smartFieldColors())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("(", ")", "7", "8", "9", "÷").forEach { key -> OutlinedButton(onClick = { expression += key }, modifier = Modifier.weight(1f)) { Text(key, color = Gold) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("4", "5", "6", "×", "1", "2").forEach { key -> OutlinedButton(onClick = { expression += key }, modifier = Modifier.weight(1f)) { Text(key, color = Gold) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("3", "0", ".", "-", "+", "⌫").forEach { key -> OutlinedButton(onClick = { expression = if (key == "⌫") expression.dropLast(1) else expression + key }, modifier = Modifier.weight(1f)) { Text(key, color = Gold) } }
        }
        Button(onClick = { result = "استخدم عملية بسيطة بأرقام وعلامة واحدة في الوقت الحالي." }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("احسب") }
        Text(result, color = TextPrimary)
    }
}

@Composable private fun SettingsScreen(name: String, signedIn: Boolean, onName: (String) -> Unit, onLogout: () -> Unit, onLogin: () -> Unit) {
    var editedName by remember(name) { mutableStateOf(name) }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("الإعدادات", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("الملف الشخصي", color = Gold, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(editedName, { editedName = it }, label = { Text("الاسم الظاهر") }, modifier = Modifier.fillMaxWidth(), colors = smartFieldColors())
        Button(onClick = { onName(editedName) }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("حفظ الاسم") }
        Text(if (signedIn) "حالة الجلسة: متصل أو زائر" else "لم يتم تسجيل الدخول", color = TextSecondary)
        if (signedIn) OutlinedButton(onClick = onLogout) { Text("تسجيل الخروج", color = Gold) }
        else Button(onClick = onLogin, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text("تسجيل الدخول / دخول كزائر") }
        Text("المظهر المعتمد: كحلي وأسود مع لمسات ذهبية.", color = TextSecondary)
        Text("الدعم الفني: ENG.MAMDOUH2009@GMAIL.COM", color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable private fun LoginScreen(busy: Boolean, onLogin: (String, String) -> Unit, onGuest: () -> Unit, onCancel: () -> Unit) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("تسجيل الدخول", color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(identifier, { identifier = it }, label = { Text("البريد الإلكتروني أو اسم المستخدم") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = smartFieldColors())
        OutlinedTextField(password, { password = it }, label = { Text("كلمة المرور") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = smartFieldColors())
        Button(onClick = { if (identifier.isNotBlank() && password.isNotBlank()) onLogin(identifier.trim(), password) }, enabled = !busy, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)) { Text(if (busy) "جارٍ الاتصال…" else "دخول") }
        OutlinedButton(onClick = onGuest, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("دخول كزائر", color = Gold) }
        TextButton(onClick = onCancel) { Text("رجوع للرئيسية", color = TextSecondary) }
    }
}

@Composable private fun GenericScreen(title: String, description: String) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(title, color = Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Surface(color = NavySurface, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, Gold.copy(alpha = .2f))) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Smart Time", color = Gold, fontWeight = FontWeight.Bold)
                Text(description, color = TextPrimary)
            }
        }
    }
}

@Composable private fun smartFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Gold, unfocusedBorderColor = NavyRaised, focusedLabelColor = Gold,
    unfocusedLabelColor = TextSecondary, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Gold
)
