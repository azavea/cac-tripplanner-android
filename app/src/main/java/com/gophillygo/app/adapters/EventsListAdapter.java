package com.gophillygo.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gophillygo.app.R;
import com.gophillygo.app.data.models.Event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsListAdapter extends RecyclerView.Adapter {

    public interface EventListItemClickListener {
        void clickedEvent(int position);
    }

    private static final String LOG_LABEL = "EventListAdapter";

    private static final DateFormat isoDateFormat, monthFormat, dayOfMonthFormat, dayOfWeekFormat;

    static {
        isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        monthFormat = new SimpleDateFormat("MMM", Locale.US);
        dayOfMonthFormat = new SimpleDateFormat("dd", Locale.US);
        dayOfWeekFormat = new SimpleDateFormat("EEE", Locale.US);
    }

    private final Context context;
    private final LayoutInflater inflater;
    private EventListItemClickListener clickListener;

    private List<Event> eventList;

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameView;
        TextView destinationNameView;
        ImageButton eventOptionsButton;

        TextView monthView;
        TextView dayOfMonthView;
        TextView dayOfWeekView;
        TextView timesView;

        private ViewHolder(View parentView, final EventListItemClickListener listener) {
            super(parentView);

            parentView.setOnClickListener(v -> {
                listener.clickedEvent(getAdapterPosition());
            });
        }
    }

    public EventsListAdapter(Context context, List<Event> events, EventListItemClickListener listener) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.eventList = events;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View parentView = inflater.inflate(R.layout.event_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(parentView, this.clickListener);
        
        viewHolder.eventNameView = parentView.findViewById(R.id.event_list_item_name_label);
        viewHolder.destinationNameView = parentView.findViewById(R.id.event_list_destination_name);
        viewHolder.eventOptionsButton = parentView.findViewById(R.id.event_list_item_options_button);

        viewHolder.monthView = parentView.findViewById(R.id.event_month_label);
        viewHolder.dayOfMonthView = parentView.findViewById(R.id.event_day_of_month_label);
        viewHolder.dayOfWeekView = parentView.findViewById(R.id.event_day_of_week_label);

        parentView.setTag(viewHolder);
        return viewHolder;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Event event = eventList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.eventNameView.setText(event.getName());

        String destinationName = event.getDestinationName();
        if (destinationName != null && !destinationName.isEmpty()) {
            viewHolder.destinationNameView.setText(destinationName);
            viewHolder.destinationNameView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.destinationNameView.setVisibility(View.INVISIBLE);
        }

        String start = event.getStartDate();
        String end = event.getEndDate();
        Date startDate, endDate;

        try {
            startDate = isoDateFormat.parse(start);
            endDate = isoDateFormat.parse(end);

            viewHolder.monthView.setText(monthFormat.format(startDate));
            viewHolder.dayOfMonthView.setText(dayOfMonthFormat.format(startDate));
            viewHolder.dayOfWeekView.setText(dayOfWeekFormat.format(startDate));

            // check if event ends on same day as it starts
            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);

            // TODO: set times or date range with times
            if (startCalendar.get(Calendar.YEAR) == endCalendar.get(Calendar.YEAR) &&
                    startCalendar.get(Calendar.DAY_OF_YEAR) == endCalendar.get(Calendar.DAY_OF_YEAR)) {


            } else {
                // multi-day event

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        viewHolder.eventOptionsButton.setOnClickListener(v -> {
            Log.d(LOG_LABEL, "Clicked event options button for event #" + event.getId());
            PopupMenu menu = new PopupMenu(context, viewHolder.eventOptionsButton);
            menu.getMenuInflater().inflate(R.menu.place_options_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                Log.d(LOG_LABEL, "Clicked " + item.toString());
                return true;
            });

            // Force icons to show in the popup menu via the support library API
            // https://stackoverflow.com/questions/6805756/is-it-possible-to-display-icons-in-a-popupmenu
            MenuPopupHelper popupHelper = new MenuPopupHelper(context,
                    (MenuBuilder)menu.getMenu(),
                    viewHolder.eventOptionsButton);
            popupHelper.setForceShowIcon(true);
            popupHelper.show();
        });
    }

    @Override
    public long getItemId(int position) {
        Event event = eventList.get(position);
        if (event != null) {
            return event.getId();
        } else {
            Log.w(LOG_LABEL, "Could not find event at offset " + String.valueOf(position));
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}

