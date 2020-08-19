package com.rinchinova.studentsapp;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.rinchinova.studentsapp.R;

public class startActivity extends AppCompatActivity implements View.OnClickListener
{
    private Button startButton;
    private SQLiteDatabase database;
    private static  final String DB_NAME = "students_app";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) //Вызывается при создании Окна
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(this); //Установка слушателя нажатия на кнопку

        database = getBaseContext().openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null); //Открытие/инициализация базы данных
        database.execSQL("CREATE TABLE IF NOT EXISTS students (first_name TEXT, last_name TEXT, patronymic TEXT, born_date TEXT, groupe TEXT)"); //Создание если нету и открытие если есть базы данных students
        database.execSQL("CREATE TABLE IF NOT EXISTS groups (number TEXT, fac_name TEXT)"); //Создание если нету и открытие если есть базы данных groups
    }

    @Override
    public void onClick(View v) //Метод обработки нажатий
    {
        Intent toMainActivity = new Intent(this, MainActivity.class);
        startActivity(toMainActivity); //Старт нового окна
    }
}
