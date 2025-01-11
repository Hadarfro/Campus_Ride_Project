package com.example.projectcampusride;

import android.app.Notification;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private List<NotificationModel> notifications;
    private OnApproveListener onApproveListener;
    private OnRefuseListener onRefuseListener;

    public interface OnApproveListener {
        void onApprove(NotificationModel notification);
    }

    public interface OnRefuseListener {
        void onRefuse(NotificationModel notification);
    }

    public NotificationAdapter(Context context, List<NotificationModel> notifications) {
        this.context = context;
        this.notifications = notifications;
    }

    public void setOnApproveListener(OnApproveListener listener) {
        this.onApproveListener = listener;
    }

    public void setOnRefuseListener(OnRefuseListener listener) {
        this.onRefuseListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
//        LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);
        holder.messageTextView.setText(notification.getMessage());
        holder.approveButton.setOnClickListener(v -> {
            if (onApproveListener != null) {
                onApproveListener.onApprove(notification);
            }
        });
        holder.refuseButton.setOnClickListener(v -> {
            if (onRefuseListener != null) {
                onRefuseListener.onRefuse(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        Button approveButton;
        Button refuseButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            messageTextView = itemView.findViewById(R.id.notificationMessage);
//            approveButton = itemView.findViewById(R.id.approveButton);
//            refuseButton = itemView.findViewById(R.id.refuseButton);
        }
    }
}
