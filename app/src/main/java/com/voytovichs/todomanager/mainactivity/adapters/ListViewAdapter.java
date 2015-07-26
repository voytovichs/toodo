package com.voytovichs.todomanager.mainactivity.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.voytovichs.todomanager.R;
import com.voytovichs.todomanager.mainactivity.TaskItem;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by voytovichs on 12.07.15.
 */
public class ListViewAdapter extends BaseAdapter {

    public interface editableElements {
        void editElement(int position);

        void deleteElement(int position);
    }

    private final Context mContext;
    private final List<TaskItem> mData;
    private final editableElements activity;
    private boolean isSwipe = false;

    public ListViewAdapter(Context context) {
        mContext = context;
        mData = new LinkedList<>();
        try {
            this.activity = (editableElements) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement editableElements");
        }
    }

    public void clear() {
        mData.clear();
    }

    public void add(TaskItem item) {
        mData.add(0, item);
        notifyDataSetChanged();
    }

    public void add(int position, TaskItem item) {
        mData.add(position, item);
        notifyDataSetChanged();
    }

    public void delete(int position) {
        //Don't touch the order!
        activity.deleteElement(position);
        mData.remove(position);

        notifyDataSetChanged();
    }

    public Collection<TaskItem> getCollection() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public TaskItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final TaskItem taskItem = mData.get(position);

        LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final SwipeLayout listItem = (SwipeLayout) mInflater.inflate(R.layout.list_item, null);
        listItem.setShowMode(SwipeLayout.ShowMode.LayDown);
        listItem.addDrag(SwipeLayout.DragEdge.Top, listItem.findViewById(R.id.bottom_wrapper));
        listItem.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                isSwipe = true;
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
                delete(position);
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                //do nothing
            }

            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
                //do nothing
            }

            @Override
            public void onHandRelease(SwipeLayout swipeLayout, float v, float v1) {
                //do nothing
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
                //do nothing
            }

        });

        final TextView titleView = (TextView) listItem.findViewById(R.id.listTitle);
        titleView.setText(taskItem.getTitle());

        final CheckBox statusView = (CheckBox) listItem.findViewById(R.id.statusCheckBox);
        statusView.setChecked(taskItem.getStatus().equals(TaskItem.Status.COMPLETED));
        statusView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (taskItem.getStatus().equals(TaskItem.Status.COMPLETED)) {
                    taskItem.setStatus(TaskItem.Status.INCOMPLETED);
                } else {
                    taskItem.setStatus(TaskItem.Status.COMPLETED);
                }
            }
        });
        final TextView dateView = (TextView) listItem.findViewById(R.id.dateTextView);
        dateView.setText(taskItem.getDate());

        final TextView timeView = (TextView) listItem.findViewById(R.id.timeTextView);
        timeView.setText(taskItem.getTime());

        final TextView commentView = (TextView) listItem.findViewById(R.id.listDescription);
        commentView.setText(taskItem.getComment());
        commentView.setVisibility(View.GONE);

        listItem.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //description should not opens when MotionAction is swipe
                                            if (isSwipe) {
                                                isSwipe = false;
                                                return;
                                            }
                                            if (commentView.getText().equals("")) {
                                                return;
                                            }
                                            if (commentView.isShown()) {
                                                commentView.setVisibility(View.GONE);
                                            } else {
                                                commentView.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
        );


        listItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.editElement(position);
                return true;
            }
        });
        return listItem;
    }
}
