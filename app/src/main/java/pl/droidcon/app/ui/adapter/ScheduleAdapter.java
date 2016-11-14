package pl.droidcon.app.ui.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import pl.droidcon.app.databinding.ScheduleElementBinding;
import pl.droidcon.app.model.common.Slot;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleViewHolder> {

    private List<Slot> slots;

    private ScheduleViewHolder.ScheduleClickListener scheduleClickListener;

    public ScheduleAdapter(List<Slot> slots, ScheduleViewHolder.ScheduleClickListener scheduleClickListener) {
        this.slots = slots;
        this.scheduleClickListener = scheduleClickListener;
    }

    @Override
    public ScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ScheduleViewHolder(
                ScheduleElementBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                ), scheduleClickListener);
    }

    @Override
    public void onBindViewHolder(ScheduleViewHolder holder, int position) {
        holder.attachSlot(getSlot(position));
    }

    public void attachSessionSlots(Slot slotToAdd) {
        for (Slot slot : slots) {
            if (slot.equals(slotToAdd)) {
                slots.set(slots.indexOf(slot), slotToAdd);
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public Slot getSlot(int position) {
        return slots.get(position);
    }
}
