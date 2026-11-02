package tk.horiuchi.tatekaemaru.data

interface AppSettings {
    var initialSampleCreated: Boolean
    var myName: String
}

class EventSetup(private val repository: EventRepository, private val settings: AppSettings) {
    suspend fun initialize() {
        if (!settings.initialSampleCreated) {
            if (!repository.hasEvents()) repository.saveEvent(SampleRepository.sampleEvent)
            settings.initialSampleCreated = true
        }
    }

    fun createEvent(): Event = Event(name = "", members = listOf(Member(name = settings.myName)))
}
