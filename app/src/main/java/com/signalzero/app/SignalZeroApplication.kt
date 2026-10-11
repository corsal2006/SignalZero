package com.signalzero.app

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.preference.PreferenceManager
import com.signalzero.app.challenge.GrevixResilienceSimulator
import com.signalzero.app.data.local.SignalZeroDatabase
import com.signalzero.app.data.preferences.UserPreferences
import com.signalzero.app.data.repository.AuthRepository
import com.signalzero.app.data.repository.ChatRepository
import com.signalzero.app.data.repository.ContactRepository
import com.signalzero.app.data.repository.IncidentRepository
import com.signalzero.app.data.repository.PeerRepository
import com.signalzero.app.mesh.*
import com.signalzero.app.service.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.osmdroid.config.Configuration

class SignalZeroApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: SignalZeroDatabase
        private set

    lateinit var userPreferences: UserPreferences
        private set

    lateinit var incidentRepository: IncidentRepository
        private set

    lateinit var contactRepository: ContactRepository
        private set

    lateinit var chatRepository: ChatRepository
        private set

    lateinit var peerRepository: PeerRepository
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var locationService: LocationService
        private set

    lateinit var contactNotificationService: ContactNotificationService
        private set

    lateinit var peerRegistry: PeerRegistry
        private set

    lateinit var gattServerManager: GattServerManager
        private set

    lateinit var gattClientManager: GattClientManager
        private set

    lateinit var bleAdvertiser: BleAdvertiserManager
        private set

    lateinit var bleScanner: BleScannerManager
        private set

    lateinit var meshRouter: MeshRouter
        private set

    lateinit var aiTriageEngine: AiTriageEngine
        private set

    lateinit var firebaseSyncService: FirebaseSyncService
        private set

    lateinit var emergencyOrchestrator: EmergencyOrchestrator
        private set

    lateinit var grevixSimulator: GrevixResilienceSimulator
        private set

    lateinit var shakeDetector: ShakeDetector
        private set

    lateinit var voiceSosDetector: VoiceSosDetector
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Initialize Osmdroid Configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName

        // 2. Initialize Database & Repositories
        database = SignalZeroDatabase.getInstance(this)
        userPreferences = UserPreferences(this)
        incidentRepository = IncidentRepository(database.incidentDao())
        contactRepository = ContactRepository(database.contactDao())
        chatRepository = ChatRepository(database.chatMessageDao())
        peerRepository = PeerRepository(database.peerDao())
        authRepository = com.signalzero.app.data.repository.AuthRepository()

        // 3. Initialize Location & Communications Services
        locationService = LocationService(this)
        contactNotificationService = ContactNotificationService(this)

        // 4. Initialize Bluetooth & BLE Mesh Stack
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

        peerRegistry = PeerRegistry(peerRepository, applicationScope)

        // MeshRouter needs gattClientManager, and gattClientManager/gattServerManager need callbacks
        // MeshRouter implements both PacketTransferCallback and PacketReceivedListener
        lateinit var routerHolder: MeshRouter

        val transferCallback = object : PacketTransferCallback {
            override fun onTransferSuccess(deviceAddress: String, messageId: String) {
                if (::meshRouter.isInitialized) meshRouter.onTransferSuccess(deviceAddress, messageId)
            }
            override fun onTransferFailure(deviceAddress: String, messageId: String, reason: String) {
                if (::meshRouter.isInitialized) meshRouter.onTransferFailure(deviceAddress, messageId, reason)
            }
        }

        val receivedListener = object : PacketReceivedListener {
            override fun onRawPacketReceived(packetJson: String, originDeviceAddress: String) {
                if (::meshRouter.isInitialized) meshRouter.onRawPacketReceived(packetJson, originDeviceAddress)
            }
        }

        gattClientManager = GattClientManager(this, bluetoothAdapter, transferCallback)
        if (bluetoothManager != null) {
            gattServerManager = GattServerManager(this, bluetoothManager, receivedListener)
        }

        bleAdvertiser = BleAdvertiserManager(bluetoothAdapter)
        bleScanner = BleScannerManager(bluetoothAdapter, peerRegistry)

        meshRouter = MeshRouter(
            packetDao = database.meshPacketDao(),
            incidentRepository = incidentRepository,
            chatRepository = chatRepository,
            peerRepository = peerRepository,
            peerRegistry = peerRegistry,
            gattClientManager = gattClientManager,
            scope = applicationScope
        )

        // 5. Initialize AI Triage, Cloud Sync & Simulator
        aiTriageEngine = AiTriageEngine()
        firebaseSyncService = FirebaseSyncService(this, incidentRepository)
        grevixSimulator = GrevixResilienceSimulator(applicationScope, meshRouter)

        // 6. Central Emergency Orchestrator
        emergencyOrchestrator = EmergencyOrchestrator(
            context = this,
            incidentRepository = incidentRepository,
            contactRepository = contactRepository,
            locationService = locationService,
            contactNotificationService = contactNotificationService,
            meshRouter = meshRouter,
            firebaseSyncService = firebaseSyncService,
            userPreferences = userPreferences,
            aiTriageEngine = aiTriageEngine,
            scope = applicationScope
        )

        // 7. Motion & Voice Emergency Triggers
        shakeDetector = ShakeDetector(this)
        voiceSosDetector = VoiceSosDetector(this)
    }

    companion object {
        lateinit var instance: SignalZeroApplication
            private set
    }
}
