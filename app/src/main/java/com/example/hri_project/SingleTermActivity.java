package com.example.hri_project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Bookmark;
import com.aldebaran.qi.sdk.object.conversation.BookmarkStatus;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SingleTermActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar ab;

    private ImageView imgView;
    private TextView txtView;
    private TextView subTxtView;
    private Button previousBtn;
    private Button nextBtn;

    private List<String> wordsList;
    private List<String> translationsList;
    private int imagesList[];
    private int foodList[] = {R.drawable.tomato, R.drawable.meat,
            R.drawable.strawberry, R.drawable.blueberry, R.drawable.icecream};
    private int animalsList[] = {R.drawable.dog, R.drawable.cat,
            R.drawable.ladybug, R.drawable.squirrel, R.drawable.elephant};
    private int objectsList[] = {R.drawable.bottle, R.drawable.lamp,
            R.drawable.bed, R.drawable.wardrobe, R.drawable.car};
    private int counter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_term);

        String category = getIntent().getStringExtra("category");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setTitle(category);
        // To display the arrow that goes back
        ab.setDisplayHomeAsUpEnabled(true);

        if (category.equals("FOOD")) {
            wordsList = Arrays.asList("POMODORO", "CARNE", "FRAGOLA", "MIRTILLO", "GELATO");
            translationsList = Arrays.asList("tomato", "meat", "strawberry", "blueberry", "ice cream");
            imagesList = foodList;
        } if (category.equals("ANIMALS")) {
            wordsList = Arrays.asList("CANE", "GATTO", "COCCINELLA", "SCOIATTOLO", "ELEFANTE");
            translationsList = Arrays.asList("dog", "cat", "ladybug", "squirrel", "elephant");
            imagesList = animalsList;
        } if (category.equals("OBJECTS")) {
            wordsList = Arrays.asList("BOTTIGLIA", "LAMPADA", "LETTO", "ARMADIO", "MACCHINA");
            translationsList = Arrays.asList("bottle", "lamp", "bed", "wardrobe", "car");
            imagesList = objectsList;
        }

        imgView = findViewById(R.id.card_img);
        txtView = findViewById(R.id.card_txt);
        subTxtView = findViewById(R.id.card_subtxt);

        for (final Object i : wordsList) {
            txtView.setText(i.toString());
            subTxtView.setText(translationsList.get(wordsList.indexOf(i)));
            imgView.setImageResource(imagesList[wordsList.indexOf(i)]);
        }

        previousBtn = findViewById(R.id.previous);;
        nextBtn = findViewById(R.id.next);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                counter += 1;
                if(counter == wordsList.size()) counter = 0;
                txtView.setText(wordsList.get(counter));
                subTxtView.setText(translationsList.get(counter));
                imgView.setImageResource(imagesList[counter]);
            }
        });

        previousBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                counter -= 1;
                if(counter < 0) counter = wordsList.size() - 1;
                txtView.setText(wordsList.get(counter));
                subTxtView.setText(translationsList.get(counter));
                imgView.setImageResource(imagesList[counter]);
            }
        });
    }
}