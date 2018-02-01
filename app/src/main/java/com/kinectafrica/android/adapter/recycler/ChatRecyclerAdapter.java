package com.kinectafrica.android.adapter.recycler;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinectafrica.android.R;
import com.kinectafrica.android.model.Message;
import com.kinectafrica.android.utility.Utils;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Made by acefalobi on 5/13/2017.
 */


public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private String userId;

    public ChatRecyclerAdapter(String userId, List<Message> messages) {
        this.messages = messages;
        this.userId = userId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatRecyclerAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        Message message = messages.get(position);

        Calendar today = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((Long) message.getTimeSent());

        if (position == messages.size() - 1) {
            itemHolder.cardView.setVisibility(View.VISIBLE);
            if ((today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR))
                    && (today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))) {
                itemHolder.textTimeStamp.setText(R.string.today);
            } else {
                itemHolder.textTimeStamp.setText(Utils.INSTANCE.covertMonthToString(calendar.get(Calendar.MONTH))
                        + " " + calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.YEAR));
            }
        } else {
            Calendar previous = Calendar.getInstance();
            previous.setTimeInMillis((Long) messages.get(position + 1).getTimeSent());
            if ((previous.get(Calendar.DAY_OF_YEAR) != calendar.get(Calendar.DAY_OF_YEAR))
                    || (previous.get(Calendar.YEAR) != calendar.get(Calendar.YEAR))) {
                itemHolder.cardView.setVisibility(View.VISIBLE);
                if ((today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR))
                        && (today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))) {
                    itemHolder.textTimeStamp.setText(R.string.today);
                } else {
                    itemHolder.textTimeStamp.setText(Utils.INSTANCE.covertMonthToString(calendar.get(Calendar.MONTH))
                            + " " + calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.YEAR));
                }
            }
        }

        DecimalFormat decimalFormat = new DecimalFormat("00");

        String time = decimalFormat.format(calendar.get(Calendar.HOUR))
                + ":" + decimalFormat.format(calendar.get(Calendar.MINUTE));

        if (message.getUserId().equals(userId)) {
            itemHolder.layoutMessageSender.setVisibility(View.VISIBLE);
            itemHolder.textMessageSender.setText(message.getMessage());
            itemHolder.textTimeStampSender.setVisibility(View.VISIBLE);
            itemHolder.textTimeStampSender.setText(time);
        } else {
            itemHolder.layoutMessageReceiver.setVisibility(View.VISIBLE);
            itemHolder.textMessageReceiver.setText(message.getMessage());
            itemHolder.textTimeStampReceiver.setVisibility(View.VISIBLE);
            itemHolder.textTimeStampReceiver.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutMessageSender, layoutMessageReceiver;

        TextView textMessageSender, textMessageReceiver, textTimeStampReceiver, textTimeStampSender, textTimeStamp;

        CardView cardView;

        ItemHolder(View view) {
            super(view);

            layoutMessageReceiver = (LinearLayout) view.findViewById(R.id.layout_message_receiver);
            layoutMessageSender = (LinearLayout) view.findViewById(R.id.layout_message_sender);

            textMessageSender = (TextView) view.findViewById(R.id.text_message_content_sender);
            textMessageReceiver = (TextView) view.findViewById(R.id.text_message_content_receiver);
            textTimeStampReceiver = (TextView) view.findViewById(R.id.text_message_time_receiver);
            textTimeStampSender = (TextView) view.findViewById(R.id.text_message_time_sender);
            textTimeStamp = (TextView) view.findViewById(R.id.text_timestamp);

            cardView = (CardView) view.findViewById(R.id.card_timestamp);
        }
    }
}
