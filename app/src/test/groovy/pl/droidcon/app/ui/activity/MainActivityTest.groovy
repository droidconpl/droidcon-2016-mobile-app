package pl.droidcon.app.ui.activity

import pl.droidcon.app.model.db.SessionEntity
import spock.lang.Specification
class MainActivityTest extends Specification {

    def "first try"(){
        given:
        def activity = new MainActivity()

        and:
        def entitiy = new SessionEntity()
        entitiy.setDayId(2)

        expect:
        activity

        and:
        entitiy.getDayId() == 2
    }
}
