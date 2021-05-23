package com.example.hri_project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ChooseExerciseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar ab;

    private Button vocabularyButton;
    private Button conversationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_exercise);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setTitle(R.string.chooseActivity);
        // To display the arrow that goes back
        ab.setDisplayHomeAsUpEnabled(true);

        initializeLayout();
    }

    public void initializeLayout() {

        this.vocabularyButton = findViewById(R.id.vocabulary);
        this.conversationButton = findViewById(R.id.conversation);

        this.vocabularyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ChooseExerciseActivity", "event: vocabulary chosen");
                Intent intent = new Intent(ChooseExerciseActivity.this, LearnVocabularyActivity.class);
                startActivity(intent);
            }
        });

        this.conversationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ChooseExerciseActivity", "event: conversation chosen");
                Intent intent = new Intent(ChooseExerciseActivity.this, ConversationActivity.class);
                startActivity(intent);
            }
        });
    }
}