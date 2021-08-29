package com.example.lazyparking;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import common.Reply;
import common.ReplyMessage;


public class ExpiredUsersReportRecyclerAdapter extends RecyclerView.Adapter<ExpiredUsersReportRecyclerAdapter.ViewHolder> {

    private List<UsernameExpirationDatePair> mData;
    private LayoutInflater mInflater;
    private static ExpiredUsersReportRecyclerAdapter activeExpiredUsersReportRecyclerAdapter;
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
    //private ItemClickListener mClickListener;

    // data is passed into the constructor
    ExpiredUsersReportRecyclerAdapter(Context context, List<UsernameExpirationDatePair> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        activeExpiredUsersReportRecyclerAdapter = this;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        UsernameExpirationDatePair pair = mData.get(position);
        holder.usernameTV.setText(pair.username);
        holder.expirationDateTV.setText(format.format(pair.expirationDate));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    private void removeFromView(String username) {
        int index = 0;
        for (UsernameExpirationDatePair pair : mData) {
            if (!pair.username.equals(username))
                index++;
            else
                break;
        }
        mData.remove(index);
        notifyItemRemoved(index);
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView usernameTV;
        TextView expirationDateTV;

        ViewHolder(View itemView) {
            super(itemView);
            usernameTV = itemView.findViewById(R.id.expired_username_TV);
            expirationDateTV = itemView.findViewById(R.id.expiration_date_TV);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();

            new AlertDialog.Builder(context)
                    .setTitle(R.string.choose_action_title)
                    .setNeutralButton(R.string.cancel, null)
                    .setNegativeButton(R.string.update_expiration_button_text, new DialogInterface.OnClickListener() {
                        private Date date;
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final CalendarView calendarView = new CalendarView(context);
                            date = new Date();
                            calendarView.setDate(date.getTime());
                            calendarView.setOnDateChangeListener((v, y, m, d)->{
                                Calendar c = Calendar.getInstance();
                                c.set(y, m, d);
                                date = new Date(c.getTimeInMillis());
                            });

                            new AlertDialog.Builder(context)
                                    .setView(calendarView)
                                    .setNegativeButton(R.string.cancel, null)
                                    .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new ChangeDriverExpirationDateFromExpiredUsersReportTask().execute(mData.get(getAdapterPosition()).username, date);
                                        }
                                    })
                                    .show();
                        }
                    })
                    .setPositiveButton(context.getString(R.string.remove_driver_button_text), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.remove_driver_menu)
                                    .setMessage(R.string.confirmation_message)
                                    .setNegativeButton(R.string.cancel, null)
                                    .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new RemoveDriverFromExpiredUsersReportTask().execute(mData.get(getAdapterPosition()).username);
                                        }
                                    })
                                    .show();
                        }
                    })
                    .show();
        }
    }

    private static class ChangeDriverExpirationDateFromExpiredUsersReportTask extends OperatorActivity.ChangeDriverExpirationDateTask {
        private String usernameToRemove;

        @Override
        protected ReplyMessage doInBackground(Object... objects) {
            usernameToRemove = (String)objects[0];
            return super.doInBackground(objects);
        }

        @Override
        protected void onPostExecute(ReplyMessage reply) {
           super.onPostExecute(reply);
           if (reply.reply == Reply.SUCCESS) {
               activeExpiredUsersReportRecyclerAdapter.removeFromView(usernameToRemove);
           }
        }
    }

    private static class RemoveDriverFromExpiredUsersReportTask extends OperatorActivity.RemoveDriverTask {
        private String usernameToRemove;

        @Override
        protected ReplyMessage doInBackground(String... strings) {
            usernameToRemove = strings[0];
            return super.doInBackground(strings);
        }

        @Override
        protected void onPostExecute(ReplyMessage reply) {
            super.onPostExecute(reply);
            if (reply.reply == Reply.SUCCESS) {
                activeExpiredUsersReportRecyclerAdapter.removeFromView(usernameToRemove);
            }
        }
    }

    static class UsernameExpirationDatePair {
        public String username;
        public Date expirationDate;
    }
}
