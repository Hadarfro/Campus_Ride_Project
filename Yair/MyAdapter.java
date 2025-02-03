package com.example.campusride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusride.models.Ride;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Ride> rideList; // רשימת הנתונים

    // בנאי שמקבל את הרשימה
    public MyAdapter(List<Ride> rideList) {
        this.rideList = rideList;
    }

    // יצירת ה-ViewHolder (תצוגת פריט ברשימה)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יצירת פריט חדש מתוך קובץ XML
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new ViewHolder(view);
    }

    // קישור הנתונים לתצוגה
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ride ride = rideList.get(position); // קבלת הנתון המתאים
        holder.textViewRide.setText((CharSequence) ride);    // קישור הנתון לתצוגה
    }

    // החזרת מספר הפריטים ברשימה
    @Override
    public int getItemCount() {
        return rideList.size();
    }

    // מחלקת ViewHolder – מנהלת את התצוגות של כל פריט
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewRide;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRide = itemView.findViewById(R.id.text_view_ride); // קישור לתצוגה ב-XML
        }
    }
}

