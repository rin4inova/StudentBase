package com.rinchinova.studentsapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class groupsActivity extends AppCompatActivity
{
    final int DELETE = 0;
    final int EDIT = 1;
    private List<String> detailfor = new ArrayList<>();
    private List<String> listGroups = new ArrayList<>();
    private List<String> listGDelete = new ArrayList<>();
    private ListView listGroupsNumber;
    private ArrayAdapter<String> groups_adapter;
    private SQLiteDatabase database;
    private static  final String DB_NAME = "students_app";

    @Override
    protected void onCreate(Bundle savedInstanceState)  //Вызывается при создании Окна
    {

        //Инициализация элементов окна
        super.onCreate(savedInstanceState);
        setContentView(R.layout.groups_activity);

        listGroupsNumber = findViewById(R.id.listGroupsNumber);
        registerForContextMenu(listGroupsNumber);
        // -------------------------------------

        database = getBaseContext().openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null); //Открытие/инициализация базы данных
        new LoadDataTask().execute(); //Запуск потока выгрузки информации из БД
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) //Вызывается для создания контекстного меню
    {
        menu.add(0, DELETE, 0, "Удалить");
        menu.add(0, EDIT, 0, "Редактировать");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) //Вызывается при выборе элемента в меню
    {
        AdapterView.AdapterContextMenuInfo ctxMenuInfo;
        ctxMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int selectedPosition = ctxMenuInfo.position;

        String get = detailfor.get(selectedPosition).trim();

        Log.d("SQLA", get);

        switch (item.getItemId())
        {
            case DELETE:

                String selection = "groupe = ?";
                String[] selectionArgs = {get};
                Cursor groupInfo = database.query("students", null, selection, selectionArgs, null, null, null);
                if (groupInfo.moveToFirst())
                {
                    do {
                        String group = groupInfo.getString(0).trim();
                        listGDelete.add(group);
                    }while (groupInfo.moveToNext());
                }

                if (listGDelete.isEmpty())
                {
                    SQLiteStatement stmtDEL = database.compileStatement("DELETE FROM groups WHERE number = (?)");
                    stmtDEL.bindString(1, get);
                    stmtDEL.executeUpdateDelete();
                    groups_adapter.clear();
                    groups_adapter.notifyDataSetChanged();

                    listGDelete.clear();

                    loadDataFromDB();
                } else
                {
                    Toast.makeText(getApplicationContext(), "Группа не пуста", Toast.LENGTH_SHORT).show();
                    listGDelete.clear();
                }

                break;

            case EDIT:

                AdapterView.AdapterContextMenuInfo upd_ctxMenuInfo;
                upd_ctxMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                int upd_selectedPosition = upd_ctxMenuInfo.position;

                String upd_get = detailfor.get(upd_selectedPosition).trim();

                LayoutInflater upd_inflater = LayoutInflater.from(this);
                View upd_group_dialog = upd_inflater.inflate(R.layout.update_group_dialog, null);
                EditText upd_facult = upd_group_dialog.findViewById(R.id.upd_facult_text);
                EditText upd_numder = upd_group_dialog.findViewById(R.id.upd_group_number_text);

                ContentValues gUPDargs = new ContentValues();
                ContentValues sUPDargs = new ContentValues();

                String where = "number = ?";
                String swhere = "groupe = ?";
                String[] whereArgs = {upd_get};

                upd_numder.setText(upd_get);

                AlertDialog.Builder builder = new AlertDialog.Builder(groupsActivity.this);
                builder.setTitle("Обновить группу")
                        .setView(upd_group_dialog)
                        .setPositiveButton("Готово", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String updFName = upd_facult.getText().toString().trim();
                                String updGNumber = upd_numder.getText().toString().trim();

                                if(updFName.length() > 2 && updGNumber.length() > 1)
                                {
                                    gUPDargs.put("number", updGNumber);
                                    gUPDargs.put("fac_name", updFName);
                                    sUPDargs.put("groupe", updGNumber);

                                    database.update("groups", gUPDargs, where, whereArgs);
                                    database.update("students", sUPDargs, swhere, whereArgs);

                                    groups_adapter.clear();
                                    groups_adapter.notifyDataSetChanged();

                                    loadDataFromDB();
                                }else  Toast.makeText(getApplicationContext(), "Данные введены не верно", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                            }
                        })
                        .create();
                builder.show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onDestroy() //Метод вызывается при закрытии приложении
    {
        super.onDestroy();
        database.close();
    }

    private void loadDataFromDB() //Метод для выгрузки данных из БД в список
    {
        Cursor cursor_groups = database.query("groups", null,null,
                null, null, null, null); //TODO додумать систему получения данных(?) изменить на rawQuery
        if (cursor_groups.moveToFirst())
        {
            do {
                String groupe_number = cursor_groups.getString(0).trim();
                String facult_name = cursor_groups.getString(1).trim();

                String formatGruop = "Группа: " + "\n" + groupe_number + "\n" + "Факультет: " + "\n" + facult_name;
                listGroups.add(formatGruop);
                detailfor.add(groupe_number);

            } while (cursor_groups.moveToNext());
        }
        updateMainUI();
        cursor_groups.close();
    }

    private void updateMainUI() //Обновление данных в списках
    {
        groups_adapter = new ArrayAdapter<String>(this, R.layout.style_listview, listGroups);
        listGroupsNumber.setAdapter(groups_adapter);
    }

    class LoadDataTask extends AsyncTask<Void, Void, Void>  //Класс нового потока для выгрузки информации из БД
    {
        @Override
        protected Void doInBackground(Void... voids)
        {
            loadDataFromDB();
            return null;
        }

    }
}
