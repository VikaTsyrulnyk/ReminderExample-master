package com.example.viktoria.reminderexample;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by viktoria on 27.01.15.
 */
public class ReminderListFragment extends ListFragment {
    private ArrayList<Reminder> reminderItems;
    private ReminderListListener mCallback;
    private ActionBar actionBar;
    private MyListAdapter adapter;
    private MyMultiChoiceModeListener multiChoiceModeListener;

    // interface to communicate with other fragment through activity, fragment shouldn't know about parent activity
    public interface ReminderListListener {
        public void onItemClick(int position);

        public void onReminderBatchDelete(ArrayList<Reminder> reminders);
    }

    @Override
    public void onResume() {
        super.onResume();
        actionBar.setDisplayHomeAsUpEnabled(false); //this is to disable up navigation
        actionBar.setTitle(getActivity().getString(R.string.list_fragment_title));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
menu.findItem(R.id.action_delete).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // this makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ReminderListListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnItemClickListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        actionBar = (getActivity()).getActionBar();
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        multiChoiceModeListener = new MyMultiChoiceModeListener();
        getListView().setMultiChoiceModeListener(multiChoiceModeListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        reminderItems = getArguments().getParcelableArrayList(getActivity().getString(R.string.reminderListIntent));
        adapter = new MyListAdapter(getActivity(), R.layout.list_item, reminderItems);
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mCallback.onItemClick(position);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getResources().getString(R.string.no_items)); //this is to set message if reminderItems.size==0
    }

    public void setReminderItems(ArrayList<Reminder> reminderItems) {
        this.reminderItems = reminderItems;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().getFragmentManager().popBackStack();
                return true;
            case R.id.action_add:
                getActivity().getFragmentManager().beginTransaction().replace(R.id.content_frame, new ReminderFragment(),
                        "reminder_fr").addToBackStack(
                        "reminder_fr").commit();
                return true;
        }
        return false;
    }


    class MyMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked) {

            // Set the CAB title according to total checked items
            mode.setTitle(getActivity().getString(R.string.selected) + " " + getListView().getCheckedItemCount());
            // Calls toggleSelection method from ListViewAdapter Class
            adapter.toggleSelection(position);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    SparseBooleanArray selected = adapter
                            .getSelectedIds();
                    ArrayList<Reminder> items_to_delete = new ArrayList<Reminder>();
                    for (int i = (selected.size() - 1); i >= 0; i--) {
                        if (selected.valueAt(i)) {
                            items_to_delete.add(adapter
                                    .getItem(selected.keyAt(i)));
                        }
                    }
                    mCallback.onReminderBatchDelete(items_to_delete);
                    // Close CAB
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_action_mode, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.removeSelection();
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(getActivity().getString(R.string.selected) + " " + getListView().getCheckedItemCount());
            return false;
        }
    }
}