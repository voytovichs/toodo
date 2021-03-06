package com.voytovichs.todomanager.activity.mainactivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.voytovichs.todomanager.R;
import com.voytovichs.todomanager.activity.addtask.AddTaskItemActivity;
import com.voytovichs.todomanager.activity.mainactivity.adapters.ListViewAdapter;
import com.voytovichs.todomanager.dao.TaskDAO;
import com.voytovichs.todomanager.dao.TaskHelperFactory;
import com.voytovichs.todomanager.activity.mainactivity.layouts.FloatingActionButton;

import java.sql.SQLException;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ListViewAdapter.editableElements {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int BUTTON_ICON_SIZE = 70;
    private static final int ADD_TODO_ITEM_REQUEST = Byte.MAX_VALUE;
    private TaskDAO taskDAO;


    private ListViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        TaskHelperFactory.setHelper(getApplicationContext());
        ListView list = (ListView) findViewById(R.id.listView);
        mAdapter = new ListViewAdapter(this);
        Log.e(TAG, "Attack adapter to list view");
        list.setAdapter(mAdapter);
        LinearLayout.LayoutParams mParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        list.setLayoutParams(mParam);
        setFloatingButton();

        try {
            taskDAO = TaskHelperFactory.getHelper().getTaskDAO();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setFloatingButton() {

        Log.e(TAG, "Creating floating button");

        Drawable dr = getDrawable(R.drawable.add_button);
        assert dr != null;
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable buttonIcon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, BUTTON_ICON_SIZE, BUTTON_ICON_SIZE, true));

        FloatingActionButton mButton = new FloatingActionButton.Builder(this)
                .withDrawable(buttonIcon)
                .withButtonColor(getResources().getColor(R.color.primaryColorDark))
                .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                .withMargins(0, 0, 16, 16)
                .create();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Floating button onCLick");
                addNewElement(ADD_TODO_ITEM_REQUEST, null);
            }
        });
    }

    private void addNewElement(int finalPosition, @Nullable TaskItem toDoItem) {
        Log.e(TAG, "Start adding new element to list");
        Intent toDoIntent = new Intent(MainActivity.this, AddTaskItemActivity.class);
        if (toDoItem != null) {
            TaskItem.packageIntent(toDoIntent, toDoItem);
        }
        //Bad decision: editing of first element
        startActivityForResult(toDoIntent, finalPosition);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e(TAG, "Getting result from the activityForResult");

        if (requestCode == ADD_TODO_ITEM_REQUEST && resultCode == RESULT_OK) {
            TaskItem item = new TaskItem(data);
            mAdapter.add(item);
        } else if (resultCode == RESULT_OK) {
            deleteElement(requestCode);
            mAdapter.delete(requestCode);
            TaskItem item = new TaskItem(data);
            mAdapter.add(requestCode, item);
        }
    }

    @Override
    public void onResume() {
        Log.e(TAG, "OnResume");
        super.onResume();
        if (mAdapter.getCount() == 0) {
            loadItems();
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "OnPause");
        super.onPause();
        saveItems();
    }

    private void saveItems() {

        Log.e(TAG, "Saving items to database");
        try {
            taskDAO.delete(mAdapter.getCollection());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int i = mAdapter.getCount() - 1; i >= 0; i--) {
            try {
                Log.d(TAG, "Adding task " + mAdapter.getItem(i));
                taskDAO.createOrUpdate(mAdapter.getItem(i));
            } catch (SQLException e) {
                Log.e(TAG, "Couldn't save task to database: " + e);
            }
        }
    }

    private void loadItems() {
        Log.e(TAG, "Load items from database");
        mAdapter.clear();
        try {
            for (TaskItem taskItem : taskDAO.queryForAll()) {
                Log.d(TAG, "Loading task " + taskItem);
                mAdapter.add(taskItem);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Couldn't load tasks: " + e);
        }

        if (mAdapter.getCount() == 0) {
            loadDefaultTutorialTasks(mAdapter);
        }
    }

    private void loadDefaultTutorialTasks(ListViewAdapter adapter) {
        List<Intent> tutorialIntents = DefaultTutorialIntents.getTutorialIntents();
        for (Intent intent : tutorialIntents) {
            adapter.add(new TaskItem(intent));
        }
    }

    @Override
    public void editElement(int position) {
        Log.e(TAG, "Start editing element");
        TaskItem toEditItem = mAdapter.getItem(position);
        addNewElement(position, toEditItem);
        Log.e(TAG, "Finish editing element");
    }

    @Override
    public void deleteElement(int position) {
        Log.e(TAG, "Start deleting element");
        TaskItem toEditItem = mAdapter.getItem(position);
        try {
            taskDAO.delete(toEditItem);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Finish editing element");
    }
}
