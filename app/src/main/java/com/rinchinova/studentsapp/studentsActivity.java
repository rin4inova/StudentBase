package com.rinchinova.studentsapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.LinearGradient;
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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class studentsActivity extends AppCompatActivity
{
    final int DELETE = 0;
    final int EDIT = 1;
    private Button orderByLastName_btn, orderByGroup_btn;
    private List<String> listStudents = new ArrayList<>();
    private List<String> detailInformation = new ArrayList<>();;
    private List<String> detailfor = new ArrayList<>();
    private List<String> listGroups = new ArrayList<>();
    private List<String> groupInformation = new ArrayList<>();
    private List<String> sorted_GroupInformation = new ArrayList<>();
    private ListView studentsListView, groupsListView;
    private ArrayAdapter<String> students_adapter;
    private ArrayAdapter<String> detail_adapter;
    private ArrayAdapter<String> spinner_adapter;
    private ArrayAdapter<String> sorted_spinner_adapter;
    private SQLiteDatabase database;
    private static final String DB_NAME = "students_app";

    @Override
    public void onCreate(Bundle savedInstanceState) //Вызывается при создании Окна
    {

        //Инициализация элементов окна
        super.onCreate(savedInstanceState);
        setContentView(R.layout.students_activity);

        orderByLastName_btn = findViewById(R.id.orderByLastName_btn);
        orderByGroup_btn = findViewById(R.id.orderByGroup_btn);

        studentsListView = findViewById(R.id.studentsList);
       // -------------------------------------
        registerForContextMenu(studentsListView); //Регистрация контекстного меню для списка

        studentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()  //Установка слушателя нажатия в списке
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                seeDetailInformation(position);
            }
        });

        database = getBaseContext().openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null); //Открытие/инициализация базы данных
        new LoadDataTask().execute();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) //Вызывается для создания контекстного меню
    {
        menu.add(0, DELETE, 0, "Удалить"); //Добавление элемента меню
        menu.add(1, EDIT, 1, "Редактировать"); //Добавление элемента меню
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) //Вызывается при выборе элемента в меню
    {
        String[] FIOf;
        AdapterView.AdapterContextMenuInfo ctxMenuInfo;
        ctxMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int selectedPosition = ctxMenuInfo.position;
        String get = detailfor.get(selectedPosition).trim();
        FIOf = get.split("\\s"); //Фильтрация результата, отделение от пробелов

        switch (item.getItemId())
        {
            case DELETE:
                Log.d("Info", FIOf[0]);
                SQLiteStatement stmtDEL = database.compileStatement("DELETE FROM students WHERE first_name = (?) AND last_name = (?) AND patronymic = (?)");
                stmtDEL.bindString(1, FIOf[0]);
                stmtDEL.bindString(2, FIOf[1]);
                stmtDEL.bindString(3, FIOf[2]);
                stmtDEL.executeUpdateDelete();

                students_adapter.clear();
                students_adapter.notifyDataSetChanged();

                loadDataFromDB();
                break;

            case EDIT:

                String[] updFIO;
                AdapterView.AdapterContextMenuInfo upd_ctxMenuInfo;
                upd_ctxMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                int upd_selectedPosition = upd_ctxMenuInfo.position;
                String upd_get = detailfor.get(upd_selectedPosition).trim();
                updFIO = upd_get.split("\\s");

                LayoutInflater upd_inflater = LayoutInflater.from(this);
                View upd_dialog = upd_inflater.inflate(R.layout.update_student_dialog, null);

                EditText upd_first_Name = upd_dialog.findViewById(R.id.upd_firstname);
                EditText upd_last_Name = upd_dialog.findViewById(R.id.upd_lastname);
                EditText upd_patronymic_Name = upd_dialog.findViewById(R.id.upd_patronymic);
                EditText upd_born_Date = upd_dialog.findViewById(R.id.upd_borndate);
                Spinner upd_group_spinner = upd_dialog.findViewById(R.id.upd_group_spinner);

                Cursor groupInfo = database.query("groups", null, null, null, null, null, null);
                if (groupInfo.moveToFirst())
                {
                    do {
                        String group = groupInfo.getString(0).trim();
                        groupInformation.add(group);
                    }while (groupInfo.moveToNext());
                }

                spinner_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.style_spinner, groupInformation);
                spinner_adapter.setDropDownViewResource(R.layout.style_spinner);

                upd_group_spinner.setAdapter(spinner_adapter);

                ContentValues args = new ContentValues();
                String where = "first_name = ? AND last_name = ? AND patronymic = ?";
                String[] whereArgs = {updFIO[0], updFIO[1], updFIO[2]};

                upd_first_Name.setText(updFIO[0]);
                upd_last_Name.setText(updFIO[1]);
                upd_patronymic_Name.setText(updFIO[2]);

                AlertDialog.Builder builder = new AlertDialog.Builder(studentsActivity.this);
                builder.setTitle("Обновить студента")
                        .setView(upd_dialog)
                        .setPositiveButton("Готово", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String updFName = upd_first_Name.getText().toString().trim();
                                String updLName = upd_last_Name.getText().toString().trim();
                                String updPName = upd_patronymic_Name.getText().toString().trim();
                                String updDate = upd_born_Date.getText().toString().trim();
                                String updGroup = upd_group_spinner.getSelectedItem().toString().trim();

                                if (updFName.length() > 2 && updLName.length() > 2 && updPName.length() > 2 && updDate.length() > 2 && updGroup.length() > 2)
                                {
                                    args.put("first_name", updFName);
                                    args.put("last_name", updLName);
                                    args.put("patronymic", updPName);
                                    args.put("born_date", updDate);
                                    args.put("groupe", updGroup);
                                    database.update("students", args, where, whereArgs);

                                    students_adapter.clear();
                                    students_adapter.notifyDataSetChanged();

                                    loadDataFromDB();
                                }else Toast.makeText(getApplicationContext(), "Данные введены не верно", Toast.LENGTH_SHORT).show();
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
        database.close();
        super.onDestroy();
    }

    public void loadDataFromDB() //Метод для выгрузки данных из БД в список
    {
        Cursor cursor_students = database.query("students", null,null,
                null, null, null, null); //Создание запроса для выгрузки данных по определенным критериям
        if (cursor_students.moveToFirst())
        {
            do {
                String first_name = cursor_students.getString(0).trim();
                String last_name = cursor_students.getString(1).trim();
                String patronymic = cursor_students.getString(2).trim();
                String group = cursor_students.getString(4).trim();

                String FIO = "ФИО: " + "\n" + first_name + " " + last_name + " " + patronymic + "\n" + "Группа: " + "\n" + group; //Форматированная строка ФИО
                String FIOforDetail = first_name + " " + last_name + " " + patronymic; //Форматированная строка ФИО

                listStudents.add(FIO); //Добавления Фамилии, Имени, Отчества в список студентов
                detailfor.add(FIOforDetail);

                //listGroups.add(group); //Добавление группы в список групп


            } while (cursor_students.moveToNext());
        }
        updateMainUI(); //Вызов метода обновления списков
        cursor_students.close(); //Закрытие запроса
    }

   private void updateMainUI() //Обновление данных в списках
   {
        students_adapter = new ArrayAdapter<String>(this, R.layout.style_listview, listStudents); //Создание нового адаптера
        studentsListView.setAdapter(students_adapter); //Установка адаптера для списка
    }

    public void loadDataOrderedByLastName(View view) //Метод сортировки по Фамилии
    {

        LayoutInflater upd_inflater = LayoutInflater.from(this);
        View filter_dialog = upd_inflater.inflate(R.layout.filter_bylastname_dialog, null);

        EditText familySortedText = filter_dialog.findViewById(R.id.familySortedText);




        AlertDialog.Builder builder = new AlertDialog.Builder(studentsActivity.this);
        builder.setTitle("Введите фамилию")
                .setView(filter_dialog)
                .setPositiveButton("Готово", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String sortedText = familySortedText.getText().toString().trim();

                        listStudents.clear();
                        detailfor.clear();
                        students_adapter.clear();
                        students_adapter.notifyDataSetChanged();

                        String selection = "last_name = ?";
                        String[] selectionArgs = {sortedText};
                        Cursor cursor_ord_name = database.query("students", null, selection, selectionArgs, null,
                                null, "last_name");

                        if (cursor_ord_name.moveToFirst())
                        {
                            do {
                                String first_name = cursor_ord_name.getString(0).trim();
                                String last_name = cursor_ord_name.getString(1).trim();
                                String patronymic = cursor_ord_name.getString(2).trim();
                                String group = cursor_ord_name.getString(4).trim();

                                String FIO = "ФИО: " + "\n" + first_name + " " + last_name + " " + patronymic + "\n" + "Группа: " + "\n" + group; //Форматированная строка ФИО
                                String FIOforDetail = first_name + " " + last_name + " " + patronymic;

                                listStudents.add(FIO);
                                detailfor.add(FIOforDetail);

                            } while (cursor_ord_name.moveToNext());
                        }
                        updateMainUI();
                    }
                })
                .setNegativeButton("Сбросить фильтр", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        listStudents.clear();
                        detailfor.clear();
                        students_adapter.clear();
                        students_adapter.notifyDataSetChanged();

                        Cursor cursor_ord_name = database.query("students", null,null, null, null,
                                null, null);

                        if (cursor_ord_name.moveToFirst())
                        {
                            do {
                                String first_name = cursor_ord_name.getString(0).trim();
                                String last_name = cursor_ord_name.getString(1).trim();
                                String patronymic = cursor_ord_name.getString(2).trim();
                                String group = cursor_ord_name.getString(4).trim();

                                String FIO = "ФИО: " + "\n" + first_name + " " + last_name + " " + patronymic + "\n" + "Группа: " + "\n" + group; //Форматированная строка ФИО
                                String FIOforDetail = first_name + " " + last_name + " " + patronymic;

                                listStudents.add(FIO);
                                detailfor.add(FIOforDetail);

                            } while (cursor_ord_name.moveToNext());
                        }
                        updateMainUI();
                        dialog.dismiss();
                    }
                })
                .create();

        builder.show();



    }

    public void loadDataOrderedByGroup(View view) //Метод сортировки по Группе
    {

        LayoutInflater upd_inflater = LayoutInflater.from(this);
        View filter_dialog = upd_inflater.inflate(R.layout.filter_bygroup_dialog, null);

        Spinner sorted_spinner = filter_dialog.findViewById(R.id.sorted_spinner);

        Cursor groupInfo = database.query("groups", null, null, null, null, null, null);
        if (groupInfo.moveToFirst())
        {
            do {
                String group = groupInfo.getString(0).trim();
                sorted_GroupInformation.add(group);
            }while (groupInfo.moveToNext());
        }

        sorted_spinner_adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.style_spinner, sorted_GroupInformation);
        sorted_spinner_adapter.setDropDownViewResource(R.layout.style_spinner);
        sorted_spinner.setAdapter(sorted_spinner_adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(studentsActivity.this);
        builder.setTitle("Выберите группу")
                .setView(filter_dialog)
                .setPositiveButton("Готово", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String chooseGroup = sorted_spinner.getSelectedItem().toString().trim();

                        String selection = "groupe = ?";
                        String[] selectionArgs = {chooseGroup};

                        listStudents.clear();
                        detailfor.clear();
                        students_adapter.clear();
                        students_adapter.notifyDataSetChanged();

                        Cursor cursor_ord_name = database.query("students", null, selection, selectionArgs,
                                null, null, "groupe");

                        if (cursor_ord_name.moveToFirst())
                        {
                            do {
                                String first_name = cursor_ord_name.getString(0).trim();
                                String last_name = cursor_ord_name.getString(1).trim();
                                String patronymic = cursor_ord_name.getString(2).trim();
                                String group = cursor_ord_name.getString(4).trim();

                                String FIO = "ФИО: " + "\n" + first_name + " " + last_name + " " + patronymic + "\n" + "Группа: " + "\n" + group; //Форматированная строка ФИО
                                String FIOforDetail = first_name + " " + last_name + " " + patronymic;

                                listStudents.add(FIO);
                                detailfor.add(FIOforDetail);

                            } while (cursor_ord_name.moveToNext());
                        }
                        updateMainUI();

                        sorted_GroupInformation.clear();
                        sorted_spinner_adapter.clear();
                        sorted_spinner_adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Сбросить фильтр", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        listStudents.clear();
                        detailfor.clear();
                        students_adapter.clear();
                        students_adapter.notifyDataSetChanged();

                        Cursor cursor_ord_name = database.query("students", null,null, null, null,
                                null, null);

                        if (cursor_ord_name.moveToFirst())
                        {
                            do {
                                String first_name = cursor_ord_name.getString(0).trim();
                                String last_name = cursor_ord_name.getString(1).trim();
                                String patronymic = cursor_ord_name.getString(2).trim();
                                String group = cursor_ord_name.getString(4).trim();

                                String FIO = "ФИО: " + "\n" + first_name + " " + last_name + " " + patronymic + "\n" + "Группа: " + "\n" + group; //Форматированная строка ФИО
                                String FIOforDetail = first_name + " " + last_name + " " + patronymic;

                                listStudents.add(FIO);
                                detailfor.add(FIOforDetail);

                            } while (cursor_ord_name.moveToNext());
                        }
                        updateMainUI();
                        sorted_GroupInformation.clear();
                        sorted_spinner_adapter.clear();
                        sorted_spinner_adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        sorted_GroupInformation.clear();
                        sorted_spinner_adapter.clear();
                        sorted_spinner_adapter.notifyDataSetChanged();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        sorted_GroupInformation.clear();
                        sorted_spinner_adapter.clear();
                        sorted_spinner_adapter.notifyDataSetChanged();
                    }
                })
                .create();

        builder.show();
    }

    public void seeDetailInformation(int position) //Метод для отображения детальной информации
    {
        LayoutInflater detail_inflater = LayoutInflater.from(this);
        View detail_dialog = detail_inflater.inflate(R.layout.detail_dialog, null);

        ListView detailListView = detail_dialog.findViewById(R.id.detailListView);

        String[] detailFIO;
        String detail_get = detailfor.get(position).trim();

        detailFIO = detail_get.split("\\s");

        String dFName = detailFIO[0].trim();
        String dLName = detailFIO[1].trim();
        String dPName = detailFIO[2].trim();

        for(int i = 0; i<detailFIO.length; i++)
        Log.d("SQLS", i + detailFIO[i]);

        String selection = "first_name = ? AND last_name = ? AND patronymic = ?";
        String[] selectionArgs = {dFName, dLName, dPName};

        Cursor cursor_detail = database.query("students", null, selection, selectionArgs, null, null, null);

        if (cursor_detail.moveToFirst())
        {
            do {
                String first_name = cursor_detail.getString(0);
                String last_name = cursor_detail.getString(1);
                String patronymic = cursor_detail.getString(2);
                String born_date = cursor_detail.getString(3);
                String groupe = cursor_detail.getString(4);

                detailInformation.add("Имя: " + "\n" + first_name);
                detailInformation.add("Фамилия: " + "\n" +last_name);
                detailInformation.add("Отчество: " + "\n" +patronymic);
                detailInformation.add("Дата рождения: " + "\n" +born_date);
                detailInformation.add("Группа: " + "\n" +groupe);

            } while (cursor_detail.moveToNext());
            detail_adapter = new ArrayAdapter<String>(detail_dialog.getContext(), R.layout.style_listview, detailInformation);
            detailListView.setAdapter(detail_adapter);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(studentsActivity.this);
        builder.setTitle("Подробная информация")
                .setView(detail_dialog)
                .setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener()
                {
                    @Override
                    public void onDismiss(DialogInterface dialog)
                    {
                        detail_adapter.clear();
                        detail_adapter.notifyDataSetChanged();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        detailInformation.clear();
                        detail_adapter.clear();
                        detail_adapter.notifyDataSetChanged();
                    }
                })
                .create();
        builder.show();
    }

    class LoadDataTask extends AsyncTask<Void, Void, Void> //Класс нового потока для выгрузки информации из БД
    {
        @Override
        protected Void doInBackground(Void... voids)
        {
            loadDataFromDB();
            return null;
        }
    }
}