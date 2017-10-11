package com.apurva.voiceList.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.apurva.voiceList.constant.Constants;
import com.apurva.voiceList.database.SortByEntry;
import com.apurva.voiceList.database.DataController;
import com.apurva.voiceList.model.ListModel;


public class MainActivity extends Activity implements OnItemSelectedListener  {
    private Spinner spinner;
    private static final SortByEntry[] sortCriteria = {SortByEntry.SORT_BY_NAME,
            SortByEntry.SORT_BY_CREATED_ON_DATE,
            SortByEntry.SORT_BY_UPDATED_ON_DATE};
    private EditText mFilterEditText;
    private LinearLayout mListUIContainer;
    private DataController mDC;


    private BroadcastReceiver getFilteredListsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] serializedObjects = intent.getStringArrayExtra(DataController.SERIALIZED_LIST_MODEL_ARRAY_KEY);
            for(String serializedObject : serializedObjects) {
                ListModel lm = ListModel.deserialize(serializedObject);
                addNewRowInListUI(lm.getName(), lm.getId());
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFilterEditText = (EditText)findViewById(R.id.filter_edit_text);
        mListUIContainer = (LinearLayout)findViewById(R.id.list_of_lists_linear_layout);
        spinner = (Spinner)findViewById(R.id.sort_spinner);

        ArrayAdapter<SortByEntry>adapter = new ArrayAdapter<>(MainActivity.this,
                R.layout.spiner_item, sortCriteria);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        mDC = new DataController(this.getApplicationContext());
        mDC.open();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(getFilteredListsReceiver, new IntentFilter(DataController.GET_FILTERED_LIST_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(getFilteredListsReceiver);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        hideKeyboardHack();
        updateUI();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) { }

    public void createList(View v){
        Intent intent = new Intent(MainActivity.this, EditListActivity.class);
        startActivityForResult(intent, Constants.REQ_CODE_CREATE_NEW_LIST);
    }

    private void editList(View v) {
        Button b = (Button) v;
        String buttonName = b.getText().toString();
        String buttonId = (String)b.getTag();

        //if button name or id is invalid or null throw exception
        // don't let user edit this
        Intent intent = new Intent(MainActivity.this, EditListActivity.class);
        intent.putExtra(Constants.LIST_NAME_KEY, buttonName);
        if(buttonId != null)
            intent.putExtra(Constants.LIST_DB_ID_KEY, buttonId);
        startActivityForResult(intent, Constants.REQ_CODE_EDIT_LIST);
    }

    private void deleteList(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete list permanently?");

        // Set up the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ImageButton b = (ImageButton)v;
                String listId = (String) b.getTag();
                mListUIContainer.removeView((View)v.getParent());
                mDC.deleteListAsync(listId);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void filterList(View v) {
        hideKeyboardHack();
        updateUI();
    }

    public void clearFilter(View v) {
        mFilterEditText.setText("");
        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQ_CODE_CREATE_NEW_LIST:
            case Constants.REQ_CODE_EDIT_LIST:
                if (resultCode == RESULT_OK) {
                    updateUI();
                }
                break;

        }
    }

    private int dpToPixel(int dim) {
        float dpMultiplier = getResources().getDisplayMetrics().density;
        return (int)(dim * dpMultiplier);
    }

    private void addNewRowInListUI(String listName, String listId) {
        // Create a RelativeLayout element
        final RelativeLayout relativelayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(dpToPixel(10), dpToPixel(0), dpToPixel(10), dpToPixel(0));
        relativelayout.setLayoutParams(params1);
        // Create Button (edit)
        final Button itemButton = new Button(this);
        final ImageButton deleteButton = new ImageButton(this);
        relativelayout.addView(itemButton);
        relativelayout.addView(deleteButton);

        itemButton.setMaxHeight(dpToPixel(25));
        itemButton.setMinimumHeight(0);
        itemButton.setMinHeight(0);
        itemButton.setWidth(dpToPixel(290));

        itemButton.setGravity(Gravity.CENTER_VERTICAL);
        itemButton.setPadding(dpToPixel(5), 0, 0, 0);
        itemButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.beige));
        itemButton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        itemButton.setText(listName);
        itemButton.setTextSize(15);
        itemButton.setAllCaps(false);
        if (listId != null) {
            itemButton.setTag(listId);
        }
        itemButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                editList(v);
            }
        });

        // Create delete button
        deleteButton.setImageResource(R.mipmap.delete);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params2.width = dpToPixel(25);
        params2.height = dpToPixel(25);
        deleteButton.setLayoutParams(params2); //causes layout update
        if (listId != null) {
            deleteButton.setTag(listId);
        }
        deleteButton.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View v) {
                deleteList(v);
            }
        });

        // Add the RelativeLayout element to the List layout inside ScrollView
        mListUIContainer.addView(relativelayout);
    }

    private void hideKeyboardHack() {
        mFilterEditText.setEnabled(false);
        mFilterEditText.setEnabled(true);
    }

    private void updateUI() {
        mListUIContainer.removeAllViews();
        String inputFilter = mFilterEditText.getText().toString();
        String orderByCriteria = ((SortByEntry)spinner.getSelectedItem()).getDbColumnName();
        mDC.getFilteredListsAsync(inputFilter, orderByCriteria);
    }

    public void closeApp(View V) {
        mDC.close();
        this.finish();
    }
}
