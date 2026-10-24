package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdManager
import com.example.data.DeveloperAuthManager
import com.example.data.DeveloperContentManager
import com.example.service.NotificationHelper
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.IslamicBorder
import com.example.ui.theme.IslamicCard
import com.example.ui.theme.IslamicCardHover
import com.example.ui.theme.IslamicDarkBg
import com.example.ui.theme.IslamicEmerald
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicEmeraldLight
import com.example.ui.theme.IslamicGold
import com.example.ui.theme.IslamicGoldLight
import com.example.ui.theme.IslamicRed
import com.example.ui.theme.IslamicSurface
import com.example.ui.theme.IslamicSurfaceVariant
import com.example.ui.theme.IslamicTextMuted
import com.example.ui.theme.IslamicTextPrimary
import com.example.ui.theme.IslamicTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperDashboardDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isLoggedIn by DeveloperAuthManager.isLoggedIn.collectAsState()
    val isAccountSetup by DeveloperAuthManager.isAccountSetup.collectAsState()
    val devEmail by DeveloperAuthManager.developerEmail.collectAsState()

    var inputEmail by remember { mutableStateOf(devEmail) }
    var inputPassword by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("القراء والتلاوات", "الإشعارات", "الإعلانات", "النسخ الاحتياطي")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = IslamicDarkBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(IslamicBorder)
            )
        },
        modifier = modifier.testTag("developer_dashboard_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(IslamicEmeraldDark)
                            .border(1.dp, IslamicGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = IslamicGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "لوحة تحكم المطوّر",
                            fontFamily = AmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = IslamicGoldLight
                        )
                        Text(
                            text = if (isLoggedIn) "حساب مصرح: $devEmail" else "إدارة محتوى وإعدادات التطبيق",
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = IslamicTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoggedIn) {
                        IconButton(
                            onClick = {
                                DeveloperAuthManager.logout()
                                authError = null
                            },
                            modifier = Modifier.testTag("dev_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "تسجيل خروج",
                                tint = IslamicRed
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dev_dashboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = IslamicTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isLoggedIn) {
                // Authentication View (Sign in or Initial Setup)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = IslamicCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IslamicBorder)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (!isAccountSetup) "إنشاء حساب المطوّر الخاص بك" else "دخول المطوّر المصرح له",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = IslamicGoldLight
                            )
                            Text(
                                text = if (!isAccountSetup)
                                    "قم بتعيين البريد الإلكتروني وكلمة المرور الخاصة بك. سيتم حفظ البيانات محلياً بشكل مشفّر (SHA-256) دون إرسالها أو كشفها داخل الكود."
                                else
                                    "أدخل بيانات حسابك للوصول إلى لوحة إدارة القراء، الإشعارات، الإعلانات، والنسخ الاحتياطي.",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = IslamicTextSecondary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        label = { Text("البريد الإلكتروني للمطوّر", fontFamily = CairoFontFamily) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = IslamicCard,
                            unfocusedContainerColor = IslamicCard,
                            focusedBorderColor = IslamicGold,
                            unfocusedBorderColor = IslamicBorder,
                            focusedTextColor = IslamicTextPrimary,
                            unfocusedTextColor = IslamicTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("dev_email_input")
                    )

                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = { inputPassword = it },
                        label = { Text("كلمة المرور", fontFamily = CairoFontFamily) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = IslamicCard,
                            unfocusedContainerColor = IslamicCard,
                            focusedBorderColor = IslamicGold,
                            unfocusedBorderColor = IslamicBorder,
                            focusedTextColor = IslamicTextPrimary,
                            unfocusedTextColor = IslamicTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("dev_password_input")
                    )

                    authError?.let { err ->
                        Text(
                            text = err,
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            color = IslamicRed
                        )
                    }

                    Button(
                        onClick = {
                            if (!isAccountSetup) {
                                if (DeveloperAuthManager.setupDeveloperAccount(inputEmail, inputPassword)) {
                                    authError = null
                                } else {
                                    authError = "يرجى كتابة بريد صالح وكلمة مرور من 6 خانات على الأقل."
                                }
                            } else {
                                if (DeveloperAuthManager.login(inputEmail, inputPassword)) {
                                    authError = null
                                } else {
                                    authError = "بيانات الدخول غير صحيحة. يرجى التأكد من البريد وكلمة المرور."
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("dev_submit_auth_button")
                    ) {
                        Text(
                            text = if (!isAccountSetup) "حفظ وتفعيل حساب المطوّر" else "تسجيل الدخول إلى اللوحة",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1400),
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                // Logged In Dashboard Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = IslamicDarkBg,
                    contentColor = IslamicGold,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTabIndex) {
                    0 -> RecitersManagementTab(context)
                    1 -> NotificationsTab(context)
                    2 -> AdsManagementTab(context)
                    3 -> BackupAndRestoreTab(context)
                }
            }
        }
    }
}

@Composable
private fun RecitersManagementTab(context: Context) {
    val reciters by DeveloperContentManager.recitersList.collectAsState()
    val hiddenIds by DeveloperContentManager.hiddenReciterIds.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newRiwayah by remember { mutableStateOf("حفص عن عاصم") }
    var newServerUrl by remember { mutableStateOf("https://server10.mp3quran.net/") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "قائمة القراء (${reciters.size})",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = IslamicGoldLight
            )

            Button(
                onClick = { showAddDialog = !showAddDialog },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_reciter_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة قارئ جديد", fontFamily = CairoFontFamily, fontSize = 12.sp)
            }
        }

        if (showAddDialog) {
            Card(
                colors = CardDefaults.cardColors(containerColor = IslamicCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("إضافة قارئ وتلاوة جديدة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IslamicGoldLight)
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("اسم القارئ بالكامل", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRiwayah,
                        onValueChange = { newRiwayah = it },
                        label = { Text("الرواية (مثال: حفص عن عاصم)", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newServerUrl,
                        onValueChange = { newServerUrl = it },
                        label = { Text("رابط الخادم (مثال: https://server10.mp3quran.net/minsh/)", fontFamily = CairoFontFamily, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            if (newName.isNotBlank() && newServerUrl.isNotBlank()) {
                                DeveloperContentManager.addCustomReciter(newName, newRiwayah, newServerUrl)
                                newName = ""
                                showAddDialog = false
                                Toast.makeText(context, "تمت إضافة القارئ بنجاح!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ وإتاحة القارئ", fontFamily = CairoFontFamily, color = Color(0xFF1B1400), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(reciters, key = { it.id }) { reciter ->
                val isHidden = hiddenIds.contains(reciter.id)
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isHidden) IslamicSurface else IslamicCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IslamicBorder)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reciter.nameArabic,
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isHidden) IslamicTextMuted else IslamicTextPrimary
                            )
                            Text(
                                text = "${reciter.riwayah} • ${if (isHidden) "مخفي حالياً" else "متاح في التطبيق"}",
                                fontFamily = CairoFontFamily,
                                fontSize = 11.sp,
                                color = if (isHidden) IslamicRed else IslamicEmeraldLight
                            )
                            Text(
                                text = reciter.serverBaseUrl,
                                fontFamily = CairoFontFamily,
                                fontSize = 10.sp,
                                color = IslamicTextMuted,
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = { DeveloperContentManager.toggleHideReciter(reciter.id) }
                        ) {
                            Icon(
                                imageVector = if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isHidden) "إظهار" else "إخفاء",
                                tint = if (isHidden) IslamicTextMuted else IslamicGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsTab(context: Context) {
    var notifTitle by remember { mutableStateOf("تذكير بتلاوة القرآن الكريم") }
    var notifMessage by remember { mutableStateOf("قال رسول الله ﷺ: «اقرؤوا القرآن فإنه يأتي يوم القيامة شفيعاً لأصحابه».") }
    val fridayEnabled by DeveloperContentManager.isFridayReminderEnabled.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = IslamicCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(IslamicBorder)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("إرسال إشعار فوري لجميع المستخدمين", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IslamicGoldLight)
                OutlinedTextField(
                    value = notifTitle,
                    onValueChange = { notifTitle = it },
                    label = { Text("عنوان الإشعار", fontFamily = CairoFontFamily) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notifMessage,
                    onValueChange = { notifMessage = it },
                    label = { Text("نص الإشعار", fontFamily = CairoFontFamily) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val sent = NotificationHelper.sendDeveloperNotification(context, notifTitle, notifMessage)
                        if (sent) {
                            Toast.makeText(context, "تم إرسال الإشعار بنجاح!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "يرجى منح إذن الإشعارات من إعدادات الهاتف.", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGold),
                    modifier = Modifier.fillMaxWidth().testTag("send_notification_button")
                ) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF1B1400), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إرسال الإشعار الآن", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1B1400))
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = IslamicCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("تذكير سورة الكهف يوم الجمعة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IslamicTextPrimary)
                    Text("إرسال تذكير تلقائي كل جمعة لتلاوة سورة الكهف والاستماع إليها.", fontFamily = CairoFontFamily, fontSize = 12.sp, color = IslamicTextSecondary)
                }

                Switch(
                    checked = fridayEnabled,
                    onCheckedChange = { DeveloperContentManager.setFridayReminderEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = IslamicGold, checkedTrackColor = IslamicEmerald)
                )
            }
        }
    }
}

@Composable
private fun AdsManagementTab(context: Context) {
    val isBannerEnabled by DeveloperContentManager.isBannerEnabled.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = IslamicCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("شريط الإعلانات في أسفل الواجهة (Banner)", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IslamicTextPrimary)
                    Text("إظهار أو إخفاء مساحة الإعلان الهادئة أسفل قائمة السور دون التأثير على التلاوة.", fontFamily = CairoFontFamily, fontSize = 12.sp, color = IslamicTextSecondary)
                }

                Switch(
                    checked = isBannerEnabled,
                    onCheckedChange = { DeveloperContentManager.setBannerEnabled(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = IslamicGold, checkedTrackColor = IslamicEmerald)
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = IslamicCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("إعدادات حساب AdMob للمطوّر", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IslamicGoldLight)
                Text(
                    text = "• التطبيق يستخدم معرّفات اختبار AdMob الرسمية خلال مرحلة التطوير.\n• لا يتم تشغيل أي إعلان صوتي أو مقاطعة التلاوة في الخلفية مطلقاً وفقاً لشروطك.\n• عند جهوزية حسابك، يمكنك وضع معرّف التطبيق الحقيقي في ملف الإعدادات دون إعادة كتابة الكود.",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = IslamicTextSecondary
                )
            }
        }
    }
}

@Composable
private fun BackupAndRestoreTab(context: Context) {
    val backupJson = remember { DeveloperContentManager.exportBackupJson() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = IslamicCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = IslamicGold)
                    Text("نسخة احتياطية من إعدادات وتلاوات التطبيق", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IslamicGoldLight)
                }

                Text(
                    text = "يمكنك نسخ هذا الملف النصي (JSON) والاحتفاظ به على هاتفك لحفظ كافة بيانات القراء الجدد، الروابط، وإعدادات التطبيق دون الاعتماد على خادم خارجي.",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = IslamicTextSecondary
                )

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("QuranAudio_Backup", backupJson)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ النسخة الاحتياطية إلى الحافظة بنجاح!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGold),
                    modifier = Modifier.fillMaxWidth().testTag("copy_backup_json_button")
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF1B1400), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نسخ النسخة الاحتياطية (JSON) للحافظة", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold, color = Color(0xFF1B1400))
                }
            }
        }
    }
}
