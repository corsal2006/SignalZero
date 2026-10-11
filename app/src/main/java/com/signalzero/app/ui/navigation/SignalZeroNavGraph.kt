package com.signalzero.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.signalzero.app.challenge.GrevixResilienceSimulator
import com.signalzero.app.core.AppLanguage
import com.signalzero.app.data.model.EmergencyCategory
import com.signalzero.app.data.model.LocationData
import com.signalzero.app.data.preferences.ThemeMode
import com.signalzero.app.data.preferences.UserPreferences
import com.signalzero.app.data.repository.*
import com.signalzero.app.mesh.*
import com.signalzero.app.service.*
import com.signalzero.app.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun SignalZeroNavGraph(
    navController: NavHostController,
    orchestrator: EmergencyOrchestrator,
    authRepository: AuthRepository,
    incidentRepository: IncidentRepository,
    contactRepository: ContactRepository,
    chatRepository: ChatRepository,
    peerRegistry: PeerRegistry,
    bleScanner: BleScannerManager,
    bleAdvertiser: BleAdvertiserManager,
    meshRouter: MeshRouter,
    locationService: LocationService,
    contactNotificationService: ContactNotificationService,
    firebaseSyncService: FirebaseSyncService,
    userPreferences: UserPreferences,
    simulator: GrevixResilienceSimulator,
    onPickContactClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val language by userPreferences.languageFlow.collectAsState(initial = AppLanguage.ENGLISH)
    val theme by userPreferences.themeFlow.collectAsState(initial = ThemeMode.DARK)
    val isShake by userPreferences.shakeEnabledFlow.collectAsState(initial = true)
    val isVoice by userPreferences.voiceEnabledFlow.collectAsState(initial = false)
    val nodePseudonym by userPreferences.nodePseudonymFlow.collectAsState(initial = "Rescuer")

    val activeIncident by orchestrator.activeIncident.collectAsState(initial = null)
    val recentIncidents by incidentRepository.recentIncidents.collectAsState(initial = emptyList())
    val peers by peerRegistry.peersState.collectAsState(initial = emptyList())
    val contacts by contactRepository.allContacts.collectAsState(initial = emptyList())

    val simState by simulator.simState.collectAsState()
    val simProgress by simulator.progress.collectAsState()
    val simElapsed by simulator.elapsedSeconds.collectAsState()
    val simReport by simulator.report.collectAsState()
    val simNodes by simulator.nodes.collectAsState()

    var lastLocation by remember { mutableStateOf<LocationData?>(null) }
    LaunchedEffect(Unit) {
        try {
            lastLocation = locationService.getCurrentLocation()
        } catch (_: Exception) {}
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevelRoute = currentRoute in listOf(
        NavRoutes.Home.route,
        NavRoutes.Map.route,
        NavRoutes.Network.route,
        NavRoutes.Contacts.route,
        NavRoutes.More.route
    )

    Scaffold(
        bottomBar = {
            if (isTopLevelRoute) {
                BottomNavBar(
                    navController = navController,
                    language = language
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (authRepository.currentUser != null) NavRoutes.Home.route else NavRoutes.Welcome.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Authentication & Onboarding
            composable(NavRoutes.Welcome.route) {
                WelcomeScreen(
                    language = language,
                    onNavigateToLogin = { navController.navigate(NavRoutes.Login.route) },
                    onNavigateToRegister = { navController.navigate(NavRoutes.Register.route) },
                    onContinueOffline = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Login.route) {
                LoginScreen(
                    authRepository = authRepository,
                    language = language,
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(NavRoutes.Register.route) },
                    onNavigateBack = { navController.popBackStack() },
                    onContinueOffline = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Register.route) {
                RegisterScreen(
                    authRepository = authRepository,
                    language = language,
                    onRegisterSuccess = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Welcome.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate(NavRoutes.Login.route) },
                    onNavigateBack = { navController.popBackStack() },
                    onContinueOffline = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Profile.route) {
                ProfileScreen(
                    authRepository = authRepository,
                    incidentRepository = incidentRepository,
                    language = language,
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) },
                    onNavigateToPermissions = { navController.navigate(NavRoutes.Permissions.route) },
                    onNavigateToHelp = { navController.navigate(NavRoutes.Help.route) },
                    onLogout = {
                        navController.navigate(NavRoutes.Welcome.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Permissions.route) {
                PermissionsScreen(
                    language = language,
                    onNavigateBack = { navController.popBackStack() },
                    onRequestPermissions = { }
                )
            }

            // Main Application Top-Level Tabs
            composable(NavRoutes.Home.route) {
                HomeScreen(
                    navController = navController,
                    language = language,
                    isBluetoothEnabled = bleAdvertiser.isAdvertising || bleScanner.isScanning,
                    isOnline = firebaseSyncService.isOnline(),
                    peerCount = peers.size,
                    activeIncident = activeIncident,
                    recentIncidents = recentIncidents,
                    onTriggerSos = { category ->
                        navController.navigate(NavRoutes.SosConfirmation.createRoute(category.name))
                    }
                )
            }

            composable(
                route = NavRoutes.SosConfirmation.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val catName = backStackEntry.arguments?.getString("category") ?: EmergencyCategory.MEDICAL.name
                val category = try {
                    EmergencyCategory.valueOf(catName)
                } catch (_: Exception) {
                    EmergencyCategory.MEDICAL
                }
                SosSendConfirmationScreen(
                    category = category,
                    orchestrator = orchestrator,
                    contactRepository = contactRepository,
                    locationData = lastLocation,
                    language = language,
                    onNavigateBack = { navController.popBackStack() },
                    onSosDispatched = { incidentId ->
                        navController.navigate(NavRoutes.SosStatus.createRoute(incidentId)) {
                            popUpTo(NavRoutes.Home.route)
                        }
                    },
                    onPickNativeContact = onPickContactClick
                )
            }

            composable(NavRoutes.Map.route) {
                MapScreen(
                    currentLat = lastLocation?.latitude,
                    currentLng = lastLocation?.longitude,
                    incidents = recentIncidents,
                    language = language,
                    navController = navController
                )
            }

            composable(NavRoutes.Network.route) {
                NetworkScreen(
                    peers = peers,
                    isScanning = bleScanner.isScanning,
                    isAdvertising = bleAdvertiser.isAdvertising,
                    myNodePseudonym = nodePseudonym,
                    language = language,
                    navController = navController,
                    onToggleScan = {
                        if (bleScanner.isScanning) {
                            bleScanner.stopScanning()
                        } else {
                            bleScanner.startScanning()
                        }
                    }
                )
            }

            composable(NavRoutes.Contacts.route) {
                ContactsScreen(
                    contacts = contacts,
                    language = language,
                    onAddContact = { contact ->
                        coroutineScope.launch { contactRepository.insertContact(contact) }
                    },
                    onDeleteContact = { contact ->
                        coroutineScope.launch { contactRepository.deleteContact(contact) }
                    },
                    onSetPrimary = { contactId ->
                        coroutineScope.launch { contactRepository.setPrimaryContact(contactId) }
                    },
                    onCallNumber = { number ->
                        contactNotificationService.callEmergencyNumber(number)
                    }
                )
            }

            composable(NavRoutes.More.route) {
                MoreScreen(
                    selectedLang = if (language == AppLanguage.HINDI) "hi" else "en",
                    onNavigateToChallenge = { navController.navigate(NavRoutes.Challenge.route) },
                    onNavigateToChat = {
                        val target = peers.firstOrNull()?.address ?: "broadcast"
                        navController.navigate(NavRoutes.Chat.createRoute(target))
                    },
                    onNavigateToHistory = { navController.navigate(NavRoutes.History.route) },
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) },
                    onNavigateToHelp = { navController.navigate(NavRoutes.Help.route) }
                )
            }

            composable(NavRoutes.SosTrigger.route) {
                SosTriggerScreen(
                    orchestrator = orchestrator,
                    selectedLang = if (language == AppLanguage.HINDI) "hi" else "en",
                    onNavigateBack = { navController.popBackStack() },
                    onSosDispatched = { incidentId ->
                        navController.navigate(NavRoutes.SosStatus.createRoute(incidentId)) {
                            popUpTo(NavRoutes.Home.route)
                        }
                    }
                )
            }

            composable(
                route = NavRoutes.SosStatus.route,
                arguments = listOf(navArgument("incidentId") { type = NavType.StringType })
            ) { backStackEntry ->
                val incidentId = backStackEntry.arguments?.getString("incidentId") ?: ""
                val targetIncident = recentIncidents.find { it.id == incidentId } ?: activeIncident

                SosStatusScreen(
                    incident = targetIncident,
                    language = language,
                    navController = navController,
                    onCancelIncident = { orchestrator.cancelEmergency(it) },
                    onResolveIncident = { orchestrator.resolveEmergency(it) },
                    onShareIncident = {
                        targetIncident?.let { inc -> contactNotificationService.shareIncident(inc) }
                    },
                    onCallEmergency = { contactNotificationService.callEmergencyNumber("112") }
                )
            }

            composable(NavRoutes.Challenge.route) {
                ChallengeScreen(
                    simState = simState,
                    progress = simProgress,
                    elapsedSeconds = simElapsed,
                    report = simReport,
                    nodes = simNodes,
                    language = language,
                    onRunTest = { simulator.runResilienceTest() }
                )
            }

            composable(
                route = NavRoutes.Chat.route,
                arguments = listOf(navArgument("peerAddress") { type = NavType.StringType })
            ) { backStackEntry ->
                val peerAddress = backStackEntry.arguments?.getString("peerAddress") ?: "broadcast"
                val conversationMessages by chatRepository.getConversation(peerAddress).collectAsState(initial = emptyList())

                ChatScreen(
                    peerAddress = peerAddress,
                    messages = conversationMessages,
                    language = language,
                    navController = navController,
                    onSendMessage = { text ->
                        coroutineScope.launch {
                            meshRouter.sendChatMessage(peerAddress, text, nodePseudonym)
                        }
                    }
                )
            }

            composable(NavRoutes.History.route) {
                HistoryScreen(
                    incidents = recentIncidents,
                    language = language,
                    navController = navController
                )
            }

            composable(NavRoutes.Settings.route) {
                SettingsScreen(
                    currentLanguage = language,
                    currentTheme = theme,
                    isShakeEnabled = isShake,
                    isVoiceEnabled = isVoice,
                    nodePseudonym = nodePseudonym,
                    onLanguageChange = { newLang ->
                        coroutineScope.launch { userPreferences.setLanguage(newLang) }
                    },
                    onThemeChange = { newTheme ->
                        coroutineScope.launch { userPreferences.setTheme(newTheme) }
                    },
                    onShakeToggle = { enabled ->
                        coroutineScope.launch { userPreferences.setShakeEnabled(enabled) }
                    },
                    onVoiceToggle = { enabled ->
                        coroutineScope.launch { userPreferences.setVoiceEnabled(enabled) }
                    }
                )
            }

            composable(NavRoutes.Help.route) {
                HelpScreen(
                    selectedLang = if (language == AppLanguage.HINDI) "hi" else "en",
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
