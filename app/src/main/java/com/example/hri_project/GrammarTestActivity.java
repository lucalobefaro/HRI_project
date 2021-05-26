package com.example.hri_project;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.locale.Language;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.object.locale.Region;

public class GrammarTestActivity extends RobotActivity implements RobotLifecycleCallbacks {

    // TODOs
    // - make sure fade out listener does not interfere with fadein
    // - add exercise number over total view
    // - add a retry button in the score view?
    // - make buttons fade in shortly after sentence

    private boolean test;
    private QiContext qiContext = null;
    private final Locale itLocale = new Locale(Language.ITALIAN, Region.ITALY);
    private int progress = -1;
    private int score = -1;
    private Button[] choiceButtons = null;
    private TextView sentenceView = null;
    private int fadeDuration;
    private final String[][][] sentences = new String[][][] {
            {
                    { "", "Italia." },
                    { "", " caffè" },
                    { "", " spaghetti" },
                    { "", " studentessa" },
                    { "", "amico" }
            },
            {
                    { "Ogni giorno a colazione ", " un toast con la marmellata" },
                    { "La prossima settimana Sara e Francesca ", " per Parigi" },
                    { "Stasera io e Marco ", " sulla collina per guardare il tramonto" },
                    { "Oggi Susanna lavora molto e ", " a casa tardi" },
                    { "Il gatto ", " le fusa" }
            },
            {
                    { "Noi "," volentieri alla cena se non dovessimo lavorare" },
                    { "Se compraste una casa in campagna, non "," così stressati." },
                    { "Se Ivan ", " a Susanna di sposarlo, lei gli direbbe sicuramente di sì!" },
                    { "Se conoscessi mia sorella, ", " che è molto simpatica" },
                    { "Non ", " problemi economici, se spendeste meno soldi." }
            }
    };
    private final String[][][] choices = new String[][][] {
            {
                    { "La", "L'", "Le", "Il" },
                    { "Il", "I", "Lo", "La" },
                    { "I", "Gli", "Lo", "Li" },
                    { "La", "Il", "Lo", "Gli" },
                    { "Il", "L'", "I", "Lo" }
            },
            {
                    { "mangio", "mangia", "mangiano", "ho mangiato" },
                    { "partiremo", "parto", "partiamo", "partono" },
                    { "salgo", "sale", "salite", "saliamo" },
                    { "torno", "torna", "torniamo", "torni" },
                    { "fa", "fanno", "fate", "faccio" }
            },
            {
                    { "veniamo", "verremmo", "venissimo", "verremo" },
                    { "sareste", "sarei", "foste", "saremo" },
                    { "chiederebbe", "chiese", "chiede", "chiedesse" },
                    { "penseresti", "pensassi", "pensi", "penserebbi" },
                    { "aveste", "avete", "avreste", "avrete" }
            }
    };
    private final int[][] correctAnswers = new int[][] {
            { 1, 0, 1, 0, 1 },
            { 0, 3, 3, 1, 0 },
            { 1, 0, 3, 0, 2 }
    };
    private final int buttonBackgroundColor = Color.GRAY;
    private int level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_grammar);

        Intent intent = getIntent();
        String levelStr = intent.getStringExtra("level");
        if (levelStr.equals("EASY")) level = 0;
        else if (levelStr.equals("MEDIUM")) level = 1;
        else level = 2;
        test = intent.getBooleanExtra("test", false);

        fadeDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        choiceButtons = new Button[]{
                findViewById(R.id.choice1),
                findViewById(R.id.choice2),
                findViewById(R.id.choice3),
                findViewById(R.id.choice4)
        };
        sentenceView = findViewById(R.id.sentenceView);

        for (int i = 0; i < choiceButtons.length; i++) {
            int finalI = i;
            choiceButtons[i].setOnClickListener(v -> onUserChoice(finalI));
            choiceButtons[i].setVisibility(View.GONE);
        }
        sentenceView.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext newQiContext) {
        qiContext = newQiContext;
        Log.i("TAG", "focus gained, starting first exercise");
        say(qiContext, "Let's test your grammar");
        progress = 0;
        score = 0;
        runOnUiThread(this::showNextExerciseUi);
    }

    @Override
    public void onRobotFocusLost() {
        //TODO what goes here?
        qiContext = null;
        Log.i("TAG", "focus lost");
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }


    // Helper funcs -------------

    private void showNextExerciseUi() {
        sentenceView.setText(sentences[level][progress][0] + "___" + sentences[level][progress][1]);
        fadeIn(sentenceView, fadeDuration);
        //TODO wait a little
        for (int i = 0; i < choiceButtons.length; i++) {
            choiceButtons[i].setText(choices[level][progress][i]);
            choiceButtons[i].setBackgroundColor(buttonBackgroundColor);
            fadeIn(choiceButtons[i], fadeDuration);
        }
    }

    private void hideExerciseUi() {
        for (Button btn: choiceButtons) {
            fadeOut(btn, fadeDuration);
        }
        fadeOut(sentenceView, fadeDuration);
    }

    private void showScoreUi() {
        fadeOut(sentenceView, fadeDuration);
        for (Button btn: choiceButtons) {
            fadeOut(btn, fadeDuration);
        }
        if (score >= 3) {
            sentenceView.setText("Score: " + score + "/" + sentences[level].length + ". You passed! Tap here to continue.");
            sentenceView.setOnClickListener(v -> startConversationIntent());
        } else {
            sentenceView.setText("Score: " + score + "/" + sentences[level].length + ". Sorry, you failed. Tap here to go back.");
            sentenceView.setOnClickListener(v -> startChooseLessonIntent());
        }

        fadeIn(sentenceView, fadeDuration);
    }

    private void restartExercises() {
        hideExerciseUi();
        progress = 0;
        score = 0;
        showNextExerciseUi();
    }

    private void giveFeedback(int choice, Runnable afterFeedback) {
        Log.i("TAG", "giving feedback");
        String feedback = "That's wrong";
        if (choice == correctAnswers[level][progress]) {
            choiceButtons[choice].setBackgroundColor(Color.GREEN);
            feedback = "That's right, well done";
            score++;
        }
        else choiceButtons[choice].setBackgroundColor(Color.RED);

        String correctSentence = sentences[level][progress][0] + choices[level][progress][correctAnswers[level][progress]] + sentences[level][progress][1];
        Spannable spannable = new SpannableString(correctSentence);
        final int start = sentences[level][progress][0].length();
        final int end = start + choices[level][progress][correctAnswers[level][progress]].length();
        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sentenceView.setText(spannable, TextView.BufferType.SPANNABLE);

        //TODO how to stress the correct word?
        sayAsync(qiContext, feedback + ". The correct answer is", null,
                () -> sayAsync(qiContext, correctSentence, itLocale, afterFeedback));

    }

    private void onUserChoice(int choice) {
        Log.i("TAG", "user chose" + choice);
        giveFeedback(choice, () -> runOnUiThread(() -> {
            hideExerciseUi();
            progress++;
            if (progress < sentences[level].length) showNextExerciseUi();
            else showScoreUi();
        }));
    }

    private void startConversationIntent() {
        String levelStr;
        if (level == 0) levelStr = "EASY";
        else if (level == 1) levelStr = "MEDIUM";
        else levelStr = "HARD";
        Intent chooseLessonIntent = new Intent(this, ConversationActivity.class);
        chooseLessonIntent.putExtra("level", levelStr);
        chooseLessonIntent.putExtra("test", test);
        startActivity(chooseLessonIntent);
    }

    private void startChooseLessonIntent() {
        String levelStr;
        if (level == 0) levelStr = "EASY";
        else if (level == 1) levelStr = "MEDIUM";
        else levelStr = "HARD";
        Intent chooseLessonIntent = new Intent(this, ChooseLessonActivity.class);
        chooseLessonIntent.putExtra("level", levelStr);
        startActivity(chooseLessonIntent);
    }


    // Static helper funcs -----------------

    private static void say(QiContext qiContext, String text) {
        Phrase phrase = new Phrase(text);
        Say say = SayBuilder.with(qiContext)
                .withPhrase(phrase)
                .build();
        say.run();
    }

    private static void sayAsync(QiContext qiContext, String text, Locale locale, Runnable afterSay) {
        Future<Say> sayActionFuture;
        if (locale != null) {
            sayActionFuture = SayBuilder.with(qiContext) // Create a builder with the QiContext.
                    .withText(text) // Specify the action parameters.
                    .withLocale(locale)
                    .buildAsync();
        } else {
            sayActionFuture = SayBuilder.with(qiContext) // Create a builder with the QiContext.
                    .withText(text) // Specify the action parameters.
                    .buildAsync();
        }

        sayActionFuture.andThenConsume(say -> {
            say.run();
            if (afterSay != null) {
                afterSay.run();
            }
        });
    }

    // from https://developer.android.com/training/animation/reveal-or-hide-view

    // Assumes view is initially GONE; it will be VISIBLE after this
    private static void fadeIn(View view, int duration) {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null);
    }

    // Assumes view is initially VISIBLE; it will be GONE after this
    private static void fadeOut(View view, int duration) {
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

}