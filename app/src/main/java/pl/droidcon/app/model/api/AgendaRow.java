package pl.droidcon.app.model.api;


import java.util.List;

public class AgendaRow {

    public static final int ID_DELTA = 1000;

    public int dayId;
    public int slotId;
    public String slotStart;
    public String slotEnd;
    public List<AgendaRowDetails> slotArray;


}
