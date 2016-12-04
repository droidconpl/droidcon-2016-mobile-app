package pl.droidcon.app.factory;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import pl.droidcon.app.model.common.SessionDay;
import pl.droidcon.app.model.common.Slot;

import static pl.droidcon.app.model.common.SessionDay.DAY_ONE;
import static pl.droidcon.app.model.common.SessionDay.DAY_TWO;
import static pl.droidcon.app.model.common.Slot.Type;

public final class SlotFactory {

    @NonNull
    public static List<Slot> createSlotsForDay(SessionDay sessionDay) {
        switch (sessionDay) {
            case DAY_ONE:
                return dayOne();
            case DAY_TWO:
                return dayTwo();
        }
        throw new IllegalArgumentException("Not supported day");
    }

    @NonNull
    private static List<Slot> dayOne() {
        List<Slot> slots = new ArrayList<>();

        slots.add(Slot.ofType(Type.REGISTRATION, DAY_ONE, 8, 0));
        slots.add(Slot.ofType(Type.OPENING_1_DAY, DAY_ONE, 9, 0));
        slots.add(Slot.ofType(Type.SESSION, DAY_ONE, 9, 15));
        slots.add(Slot.ofType(Type.SESSION, DAY_ONE, 10, 10));
        slots.add(Slot.ofType(Type.COFFEE_BREAK, DAY_ONE, 10, 55));
        slots.add(Slot.ofType(Type.SESSION, DAY_ONE, 11, 15));
        slots.add(Slot.ofType(Type.SESSION, DAY_ONE, 12, 10));
        slots.add(Slot.ofType(Type.LUNCH_BREAK, DAY_ONE, 12, 40));
        slots.add(Slot.ofType(Type.SESSION, DAY_ONE, 14, 0));
        slots.add(Slot.ofType(Type.SESSION, DAY_ONE, 14, 55));
        slots.add(Slot.ofType(Type.COFFEE_BREAK, DAY_ONE, 15, 40));
        slots.add(Slot.ofType(Type.SESSION, DAY_ONE, 16, 0));
        slots.add(Slot.ofType(Type.CLOSING_1_DAY, DAY_ONE, 16, 45));
        slots.add(Slot.ofType(Type.AFTER_PARTY, DAY_ONE, 18, 0));

        return slots;
    }

    @NonNull
    private static List<Slot> dayTwo() {
        List<Slot> slots = new ArrayList<>();

        slots.add(Slot.ofType(Type.OPENING_2_DAY, DAY_TWO, 8, 45));
        slots.add(Slot.ofType(Type.SESSION, DAY_TWO, 9, 15));
        slots.add(Slot.ofType(Type.SESSION, DAY_TWO, 10, 5));
        slots.add(Slot.ofType(Type.COFFEE_BREAK, DAY_TWO, 10, 35));
        slots.add(Slot.ofType(Type.SESSION, DAY_TWO, 11, 0));
        slots.add(Slot.ofType(Type.SESSION, DAY_TWO, 11, 50));
        slots.add(Slot.ofType(Type.LUNCH_BREAK, DAY_TWO, 12, 35));
        slots.add(Slot.ofType(Type.BARCAMP, DAY_TWO, 14, 0));
        slots.add(Slot.ofType(Type.SESSION, DAY_TWO, 15, 0));
        slots.add(Slot.ofType(Type.COFFEE_BREAK, DAY_TWO, 15, 45));
        slots.add(Slot.ofType(Type.SESSION, DAY_TWO, 16, 0));
        slots.add(Slot.ofType(Type.CLOSING_2_DAY, DAY_TWO, 16, 45));

        return slots;
    }

}
