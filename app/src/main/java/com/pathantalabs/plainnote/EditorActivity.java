package com.pathantalabs.plainnote;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditorActivity extends AppCompatActivity {

    private String action;
    private EditText editor;
    private String noteFilter;
    private String oldText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editor = (EditText) findViewById(R.id.editText);
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);
        if(uri == null){
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.title_newNote));
        }else {
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" +uri.getLastPathSegment();
            setTitle("Edit Note");

            Cursor cursor = getContentResolver().query(uri,DBOpenHelper.ALL_COLUMNS,noteFilter,null,null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            editor.setText(oldText);
            editor.requestFocus();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(action.equals(Intent.ACTION_EDIT)){
            getMenuInflater().inflate(R.menu.menu_editor,menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent shareIntent = null, chooser = null;
       switch(item.getItemId()){
           case android.R.id.home:
               finishEditing();
               break;
           case R.id.action_delete:
               deleteNote();
               break;
           case R.id.action_share:
               shareIntent = new Intent(Intent.ACTION_SEND);
               shareIntent.putExtra(Intent.EXTRA_TEXT,editor.getText().toString().trim());
               shareIntent.setType("text/plain");
               chooser = Intent.createChooser(shareIntent,"Share NoteText");
               startActivity(chooser);
               break;
       }
        return true;
    }

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI,noteFilter,null);
        Toast.makeText(this, R.string.note_deleted,Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }


    public void finishEditing(){
        String newText = editor.getText().toString().trim();
        switch(action){
            case Intent.ACTION_INSERT:
                if(newText.length() == 0){
                    setResult(RESULT_CANCELED);
                } else{
                    insertNote(newText);
                }
                break;
            case Intent.ACTION_EDIT:
                if(newText.length() == 0){
                    deleteNote();
                }else if(oldText.equals(newText)){
                    setResult(RESULT_CANCELED);
                }else{
                    updataNote(newText);
                }
        }
        finish();
    }

    private void updataNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        getContentResolver().update(NotesProvider.CONTENT_URI,values,noteFilter,null);
        Toast.makeText(this,getString(R.string.update_note),Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        getContentResolver().insert(NotesProvider.CONTENT_URI,values);
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
