package com.pathantalabs.plainnote;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EDITOR_REQUEST_CODE = 100;
    CursorAdapter cursorAdapter;
    String quote,week,months;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        quote = res.getString(R.string.quote);
        week = res.getString(R.string.days);
        months = res.getString(R.string.months);

        cursorAdapter = new NotesCursorAdapter(this, null,0);
        listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(cursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" +id);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE,uri);
                startActivityForResult(intent,EDITOR_REQUEST_CODE);
            }
        });

        if (listView!=null) {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
                    final PopupMenu popUp = new PopupMenu(MainActivity.this, view);
                    MenuInflater inflater = popUp.getMenuInflater();
                    inflater.inflate(R.menu.popup_menu, popUp.getMenu());
                    popUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int idN = item.getItemId();
                            switch (idN) {
                                case R.id.popup_edit:
                                    Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                                    Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                                    intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                                    startActivityForResult(intent, EDITOR_REQUEST_CODE);
                                    break;
                                case R.id.popup_delete:
                                    getContentResolver().delete(Uri.parse("content://com.pathantalabs.plainnote.notesprovider/notes" + id), null, null);
                                    Toast.makeText(MainActivity.this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
                                    cursorAdapter.notifyDataSetChanged();
                                    setResult(RESULT_OK);
                                    popUp.dismiss();
                                    break;
                            }

                            return true;
                        }
                    });
                    popUp.show();
                    return true;
                }
            });
        }

        getLoaderManager().initLoader(0,null,this);
    }

    private void insertNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        Uri noteUri = getContentResolver().insert(NotesProvider.CONTENT_URI,values);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,NotesProvider.CONTENT_URI,null,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        cursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.sample_data:
                insertSampleData();
                break;
            case R.id.delete_all:
                deleteAllNotes();
                break;
        }
       return super.onOptionsItemSelected(item);
    }

    private void deleteAllNotes() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getContentResolver().delete(NotesProvider.CONTENT_URI,null,null);
                            restartLoader();
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }

    private void insertSampleData() {
        insertNote(quote);
        insertNote(week);
        insertNote(months);
        restartLoader();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0,null,this);
    }

    public void openEditorForNewNote(View view) {
        Intent intent = new Intent(this,EditorActivity.class);
        startActivityForResult(intent,EDITOR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK){
            restartLoader();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
