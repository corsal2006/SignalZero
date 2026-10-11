package com.signalzero.app.ui.navigation

sealed class NavRoutes(val route: String) {
    object Welcome : NavRoutes("welcome")
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object Profile : NavRoutes("profile")
    object Permissions : NavRoutes("permissions")

    object Home : NavRoutes("home")
    object Map : NavRoutes("map")
    object Network : NavRoutes("network")
    object Contacts : NavRoutes("contacts")
    object More : NavRoutes("more")
    object SosTrigger : NavRoutes("sos_trigger")
    object SosConfirmation : NavRoutes("sos_confirmation/{category}") {
        fun createRoute(category: String) = "sos_confirmation/$category"
    }
    object SosStatus : NavRoutes("sos_status/{incidentId}") {
        fun createRoute(incidentId: String) = "sos_status/$incidentId"
    }
    object Chat : NavRoutes("chat/{peerAddress}") {
        fun createRoute(peerAddress: String) = "chat/$peerAddress"
    }
    object Challenge : NavRoutes("challenge")
    object History : NavRoutes("history")
    object Settings : NavRoutes("settings")
    object Help : NavRoutes("help")
    object Auth : NavRoutes("auth")
}
