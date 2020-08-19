package com.rinchinova.studentsapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button goToStudentsButton, goToGroupsButton;
    private Button addStudentButton, addGroupButton;
    private ArrayAdapter<String> spinner_adapter;
    private List<String> groupInformation = new ArrayList<>();
    private List<String> groupCheck = new ArrayList<>();
    private SQLiteDatabase database;

    private static  final String DB_NAME = "students_app";
    @Override
    protected void onCreate(Bundle savedInstanceState) //Вызывается при создании Окна
    {
        //Инициализация элементов окна
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = getBaseContext().openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);
        goToGroupsButton = findViewById(R.id.goToGroupsBtn);
        goToGroupsButton.setOnClickListener(this);
        goToStudentsButton = findViewById(R.id.goToStudentsBtn);
        goToStudentsButton.setOnClickListener(this);
        addStudentButton = findViewById(R.id.addStudentButton);
        addStudentButton.setOnClickListener(this);
        addGroupButton = findViewById(R.id.addGroupButton);
        addGroupButton.setOnClickListener(this);
        // -------------------------------------


        groupCheck.clear();
        Cursor groupCheck_cursor = database.query("groups", null, null, null, null, null, null);
        if (groupCheck_cursor.moveToFirst())
        {
            do {
                String group = groupCheck_cursor.getString(0).trim();
                groupCheck.add(group);
            }while (groupCheck_cursor.moveToNext());
        }

        if(groupCheck.isEmpty())
        {
            addStudentButton.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Для добавления нового студента необходимо добавить группу", Toast.LENGTH_LONG).show();
        }
        else
            addStudentButton.setEnabled(true);

        groupCheck_cursor.close();


    }


    @Override
    public void onClick(View v) //Метод обработки нажатия на кнопки
    {
        switch (v.getId())
        {
            case R.id.goToStudentsBtn:
                Intent studentsIntent = new Intent(this, studentsActivity.class);
                startActivity(studentsIntent); //Старт нового окна Студентов
                break;
            case R.id.goToGroupsBtn:
                Intent groupIntent = new Intent(this, groupsActivity.class);
                startActivity(groupIntent); //Старт нового окна Групп
                break;
            case R.id.addStudentButton:
                add_Student(); //Вызов метода добавления студента
                break;
            case R.id.addGroupButton:
                add_Group(); //Вызов метода добавления группы
                break;
        }
    }

    @Override
    protected void onStart() //Метод вызывается при старте Окна
    {
        groupCheck.clear();
        Cursor groupCheck_cursor = database.query("groups", null, null, null, null, null, null);
        if (groupCheck_cursor.moveToFirst())
        {
            do {
                String group = groupCheck_cursor.getString(0).trim();
                groupCheck.add(group);
            }while (groupCheck_cursor.moveToNext());
        }

        if(groupCheck.isEmpty())
        {
            addStudentButton.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Для добавления нового студента необходимо добавить группу", Toast.LENGTH_LONG).show();
        }
        else
            addStudentButton.setEnabled(true);
        super.onStart();
    }

    @Override
    protected void onRestart() //Метод вызывается при рестарте Окна
    {
        groupCheck.clear();
        Cursor groupCheck_cursor = database.query("groups", null, null, null, null, null, null);
        if (groupCheck_cursor.moveToFirst())
        {
            do {
                String group = groupCheck_cursor.getString(0).trim();
                groupCheck.add(group);
            }while (groupCheck_cursor.moveToNext());
        }

        if(groupCheck.isEmpty())
        {
            addStudentButton.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Для добавления нового студента необходимо добавить группу", Toast.LENGTH_LONG).show();
        }
        else
            addStudentButton.setEnabled(true);

        groupCheck_cursor.close();
        super.onRestart();
    }

    @Override
    protected void onResume() //Метод вызывается при рестарте(из свернутого) Окна
    {
        groupCheck.clear();
        Cursor groupCheck_cursor = database.query("groups", null, null, null, null, null, null);
        if (groupCheck_cursor.moveToFirst())
        {
            do {
                String group = groupCheck_cursor.getString(0).trim();
                groupCheck.add(group);
            }while (groupCheck_cursor.moveToNext());
        }

        if(groupCheck.isEmpty())
        {
            addStudentButton.setEnabled(false);
            Toast.makeText(getApplicationContext(), "Для добавления нового студента необходимо добавить группу", Toast.LENGTH_LONG).show();
        }
        else
            addStudentButton.setEnabled(true);

        groupCheck_cursor.close();
        super.onResume();
    }

    private void add_Student() //Метод добавления нового студента
    {
        ContentValues add_contentValues = new ContentValues();
        LayoutInflater inflater = LayoutInflater.from(this);
        View add_student_dialog = inflater.inflate(R.layout.add_student_dialog, null);

        EditText first_Name = add_student_dialog.findViewById(R.id.firstname_text);
        EditText last_Name = add_student_dialog.findViewById(R.id.lastname_text);
        EditText patronymic_Name = add_student_dialog.findViewById(R.id.patronymic_text);
        EditText born_Date = add_student_dialog.findViewById(R.id.borndate_text);

        Spinner gSpinner = add_student_dialog.findViewById(R.id.group_spinner);

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

        gSpinner.setAdapter(spinner_adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Добавить студента")
                .setView(add_student_dialog)
                .setPositiveButton("Готово", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String fName = first_Name.getText().toString().trim();
                        String lName = last_Name.getText().toString().trim();
                        String pName = patronymic_Name.getText().toString().trim();
                        String bDate = born_Date.getText().toString().trim();
                        String nGroupe = gSpinner.getSelectedItem().toString().trim();

                        if (fName.length() > 2 && lName.length() > 2 && pName.length() > 2 && bDate.length() > 2 && nGroupe.length() > 2)
                        {
                            add_contentValues.put("first_name", fName);
                            add_contentValues.put("last_name", lName);
                            add_contentValues.put("patronymic", pName);
                            add_contentValues.put("born_date", bDate);
                            add_contentValues.put("groupe", nGroupe);
                            database.insert("students", null, add_contentValues);
                        }else Toast.makeText(getApplicationContext(), "Данные введены не верно", Toast.LENGTH_SHORT).show();

                        groupInformation.clear();
                        spinner_adapter.clear();
                        spinner_adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        groupInformation.clear();
                        spinner_adapter.clear();
                        spinner_adapter.notifyDataSetChanged();
                    }

                })
                .setOnCancelListener(new DialogInterface.OnCancelListener()
                {
                    @Override
                    public void onCancel(DialogInterface dialog)
                    {
                        groupInformation.clear();
                        spinner_adapter.clear();
                        spinner_adapter.notifyDataSetChanged();
                    }
                })
                .create();
        builder.show();
    }

    private void add_Group() //Метод добавления новой группы
    {
        ContentValues add_group_contentValues = new ContentValues();
        LayoutInflater inflater = LayoutInflater.from(this);
        View add_group_dialog = inflater.inflate(R.layout.add_group_dialog, null);

        EditText facult = add_group_dialog.findViewById(R.id.facult_text);
        EditText group = add_group_dialog.findViewById(R.id.group_number_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Добавить группу")
                .setView(add_group_dialog)
                .setPositiveButton("Готово", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String facult_Name = facult.getText().toString().trim();
                        String group_Number = group.getText().toString().trim();

                        if(facult_Name.length() > 2 && group_Number.length() > 1)
                        {
                            add_group_contentValues.put("number", group_Number);
                            add_group_contentValues.put("fac_name", facult_Name);
                            database.insert("groups", null, add_group_contentValues);
                        }else Toast.makeText(getApplicationContext(), "Данные введены не верно", Toast.LENGTH_SHORT).show();

                        Cursor groupCheck_cursor = database.query("groups", null, null, null, null, null, null);
                        if (groupCheck_cursor.moveToFirst())
                        {
                            do {
                                String group = groupCheck_cursor.getString(0).trim();
                                groupCheck.add(group);
                            }while (groupCheck_cursor.moveToNext());
                        }

                        if(groupCheck.isEmpty())
                            addStudentButton.setEnabled(false);
                        else
                            addStudentButton.setEnabled(true);

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
    }

}