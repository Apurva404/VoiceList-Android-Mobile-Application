package com.apurva.voiceList.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.speech.RecognizerIntent;
import android.content.ActivityNotFoundException;

import com.apurva.voiceList.constant.Constants;
import com.apurva.voiceList.database.DataController;
import com.apurva.voiceList.model.ListItemModel;


public class EditListActivity extends Activity {
    private Button mListNameButton;
    private Button mSaveButton;
    private Button mBackButton;
    private ImageButton mMicButton;
    private LinearLayout mListItemLinearLayout;

    private boolean mListHasUnsavedChanges;
    private String mListName;
    private String mListId;
    private DataController mDC;

    // The item stored as value in this map can have
    // 1. Valid id and valid listId (if the item exists in DB), or
    // 2. Null id and Null listId (if the item belongs to a new list, which is yet to be saved even once), or
    // 3. Null id and Valid listId (if the item is a new item added to a previously saved list).
    // since new items will not have ids, we will use pseudo ids to bind model items to the UI delete buttons.
    private Map<String, ListItemModel> mPseudoIdListItemMap;
    // contains all items that are new and needs to inserted into DB on next save
    private ArrayList<ListItemModel> mItemsToInsertIntoDB;
    // contains all items that exists in DB but have been deleted post last save, and needs
    // to be removed from DB on next save
    private ArrayList<ListItemModel> mItemsToDeleteFromDB;
    private int mPseudoId;
    private int mCountExpectedListItemInsertHandlerCalls;

    private BroadcastReceiver insertListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mListId = intent.getStringExtra(DataController.INSERTED_LIST_ID_KEY);
            saveDataToExistingList();
        }
    };

    private BroadcastReceiver insertListItemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // countdown mechanism to wait until all inserts are done
            if(mCountExpectedListItemInsertHandlerCalls > 1) {
                mCountExpectedListItemInsertHandlerCalls--;
            } else {
                mCountExpectedListItemInsertHandlerCalls = 0;
                finalizeSaveList();
            }
        }
    };

    private BroadcastReceiver getItemsInListReceiver = new BroadcastReceiver() {
        @Override
        // updates the UI too, so don't call refresh UI from here!!
        public void onReceive(Context context, Intent intent) {
            String[] serializedObjects = intent.getStringArrayExtra(DataController.SERIALIZED_LIST_ITEM_MODEL_ARRAY_KEY);
            for(String serializedObject : serializedObjects) {
                ListItemModel lim = ListItemModel.deserialize(serializedObject);
                addNewListItemToUI(lim);
            }

            enableAllButtons();
            if(!mListHasUnsavedChanges)
                disableSaveButton();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        mPseudoId = 1;
        mPseudoIdListItemMap = new HashMap<>();
        mItemsToDeleteFromDB = new ArrayList<>();
        mItemsToInsertIntoDB = new ArrayList<>();

        mListId = getIntent().getStringExtra(Constants.LIST_DB_ID_KEY);
        mListName = getIntent().getStringExtra(Constants.LIST_NAME_KEY);

        mDC = new DataController(this.getApplicationContext());
        mDC.open();

        mListItemLinearLayout = (LinearLayout) findViewById(R.id.listItemLinearLayout);
        mListNameButton = (Button) findViewById(R.id.listNameButton);
        mSaveButton = (Button) findViewById(R.id.saveListButton);
        mBackButton = (Button) findViewById(R.id.backButton);
        mMicButton = (ImageButton) findViewById(R.id.btnSpeak);

        mCountExpectedListItemInsertHandlerCalls = 0;
        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(getItemsInListReceiver, new IntentFilter(DataController.GET_ITEMS_IN_LIST_BROADCAST));
        registerReceiver(insertListReceiver, new IntentFilter(DataController.INSERT_LIST_BROADCAST));
        registerReceiver(insertListItemReceiver, new IntentFilter(DataController.INSERT_ITEM_IN_LIST_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(getItemsInListReceiver);
        unregisterReceiver(insertListReceiver);
        unregisterReceiver(insertListItemReceiver);
    }

    private void refreshUI() {
        disableAllButtons();
        mListItemLinearLayout.removeAllViews();
        mPseudoIdListItemMap.clear();

        if(mListName != null)
            mListNameButton.setText(mListName);
        else
            mListNameButton.setText(Constants.DEFAULT_FILENAME);

        if((mListId != null)) {
            mDC.getItemsInListAsync(mListId);
        } else {
            enableAllButtons();
            if(!mListHasUnsavedChanges)
                disableSaveButton();
        }
    }

    public void renameList(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New name of list");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String listNewName = input.getText().toString();
                if(!listNewName.isEmpty()) {
                    mListName = listNewName;
                    ((Button)v).setText(mListName);
                    mListHasUnsavedChanges = true;
                    enableSaveButton();
                }
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

    public void promptSpeechInput(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, Constants.REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if((result != null) && (result.size() >= 1)) {
                        String itemName = result.get(0);
                        ListItemModel item = new ListItemModel();
                        item.setName(itemName);
                        if (mListId != null) {
                            item.setListId(mListId);
                        }
                        mItemsToInsertIntoDB.add(item);
                        addNewListItemToUI(item);

                        mListHasUnsavedChanges = true;
                        enableSaveButton();
                    }
                }
                break;
            }
        }
    }

    public void saveList(View v) {
        if (mListName == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("New name of list");

            // Set up the input
            final EditText input = new EditText(this);
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String listNewName = input.getText().toString();
                    if(!listNewName.isEmpty()) {
                        mListName = listNewName;
                        saveDataToNamedList();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            saveDataToNamedList();
        }
    }

    private void saveDataToNamedList() {
        disableAllButtons();
        if(mListId == null) {
            mDC.insertListAsync(mListName);
        } else {
            mDC.updateListAsync(mListId, mListName);
            saveDataToExistingList();
        }
    }

    private void saveDataToExistingList() {
        for(ListItemModel lim : mItemsToDeleteFromDB) {
            mDC.deleteListItemAsync(lim.getId());
        }
        mItemsToDeleteFromDB.clear();

        mCountExpectedListItemInsertHandlerCalls = mItemsToInsertIntoDB.size();
        if(mCountExpectedListItemInsertHandlerCalls != 0) {
            for (ListItemModel lim : mItemsToInsertIntoDB) {
                mDC.insertListItemAsync(lim.getName(), Integer.valueOf(mListId));
            }
        } else {
            finalizeSaveList();
        }
    }

    private void finalizeSaveList() {
        mItemsToInsertIntoDB.clear();
        mListHasUnsavedChanges = false;
        refreshUI();
    }

    private void deleteListItem(final View v) {
        ImageButton b = (ImageButton) v;
        String pseudoId = (String)b.getTag();
        ListItemModel item = mPseudoIdListItemMap.get(pseudoId);

        if(item.getId() != null) {
            mItemsToDeleteFromDB.add(item);
        } else if(mItemsToInsertIntoDB.contains(item)) {
            mItemsToInsertIntoDB.remove(item);
        }

        removeListItemFromUI(v);

        mListHasUnsavedChanges = true;
        enableSaveButton();
    }

    public void back(View v) {
        if(mListHasUnsavedChanges) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Abandon unsaved changes?");

            // Set up the buttons
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishApp();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        } else {
            finishApp();
        }
    }

    private void finishApp() {
        setResult(Activity.RESULT_OK);
        mDC.close();
        finish();
    }

    private void addNewListItemToUI(ListItemModel item) {
        // Create a RelativeLayout element
        RelativeLayout relativelayout = new RelativeLayout(this);

        // Create TextView
        TextView textView = new TextView(this);
        textView.setText(item.getName());
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        textView.setTextSize(20);

        // Create delete button
        final ImageButton button = new ImageButton(this);
        button.setImageResource(R.mipmap.delete);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.width = 50;
        params.height = 50;
        button.setLayoutParams(params); //causes layout update

        String pseudoId = Integer.valueOf(mPseudoId).toString();
        mPseudoId++;
        button.setTag(pseudoId);
        mPseudoIdListItemMap.put(pseudoId, item);
        button.setOnClickListener(new ImageButton.OnClickListener(){

            @Override
            public void onClick(View v) {
                deleteListItem(v);
            }
        });

        relativelayout.addView(textView);
        relativelayout.addView(button);

        // Add the RelativeLayout element to the List layount inside ScrollView
        mListItemLinearLayout.addView(relativelayout);
    }

    private void removeListItemFromUI(final View v) {
        ImageButton b = (ImageButton) v;
        String pseudoId = (String)b.getTag();
        mListItemLinearLayout.removeView((View) v.getParent());
        mPseudoIdListItemMap.remove(pseudoId);
    }

    private void enableSaveButton() {
        mSaveButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        mSaveButton.setClickable(true);
    }

    private void disableSaveButton() {
        mSaveButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        mSaveButton.setClickable(false);
    }

    private void enableAllButtons() {
        int colorSaveButtonEnabled = ContextCompat.getColor(getApplicationContext(), R.color.red);
        int colorBackButtonEnabled = ContextCompat.getColor(getApplicationContext(), R.color.brown);
        int colorMicButtonEnabled = ContextCompat.getColor(getApplicationContext(), R.color.green);

        mListNameButton.setClickable(true);
        mSaveButton.setClickable(true);
        mBackButton.setClickable(true);
        mMicButton.setClickable(true);

        mSaveButton.setBackgroundColor(colorSaveButtonEnabled);
        mBackButton.setBackgroundColor(colorBackButtonEnabled);
        mMicButton.setBackgroundColor(colorMicButtonEnabled);
    }

    private void disableAllButtons() {
        int colorDisabled = ContextCompat.getColor(getApplicationContext(), R.color.gray);

        mListNameButton.setClickable(false);
        mSaveButton.setClickable(false);
        mBackButton.setClickable(false);
        mMicButton.setClickable(false);

        mSaveButton.setBackgroundColor(colorDisabled);
        mBackButton.setBackgroundColor(colorDisabled);
        mMicButton.setBackgroundColor(colorDisabled);
    }
}
