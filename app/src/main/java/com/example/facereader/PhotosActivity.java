package com.example.facereader;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

//klasa opisująca licencję zdjęć FER-2013
public class PhotosActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //ustawienie rozmieszczenia elementów
        setContentView(R.layout.activity_photos);
        //ustawienie toolbaru
        Toolbar toolbar = (Toolbar)findViewById(R.id.licence_bar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        //ustawienie ikony na toolbarze (Home button)
        ab.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
    }

    //wyjscie z tego activity (powrot do okna glównego po naciśnięciu Home Button)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

