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

public class GrammarActivity extends RobotActivity implements RobotLifecycleCallbacks {

    // TODOs
    // - make sure fade out listener does not interfere with fadein
    // - add exercise number over total view
    // - add a retry button in the score view?
    // - make buttons fade in shortly after sentence

    private QiContext qiContext = null;
    private final Locale itLocale = new Locale(Language.ITALIAN, Region.ITALY);
    private int progress = -1;
    private int score = -1;
    private Button[] choiceButtons = null;
    private TextView sentenceView = null;
    private int fadeDuration;
    private final String[][] sentences = new String[][] {
            { "Io ", " pepper." },
            { "Sono programmato ", "." }
    };
    private final String[][] choices = new String[][] {
            { "è", "era", "sono", "sei" },
            { "benissimo", "benino", "insomma", "male" }
    };
    private final int[] correctAnswers = new int[] { 2, 0 };
    private final int buttonBackgroundColor = Color.BLUE;
    private String level;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_grammar);

        Intent intent = getIntent();
        level = intent.getStringExtra("level");

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
        sentenceView.setText(sentences[progress][0] + "..." + sentences[progress][1]);
        fadeIn(sentenceView, fadeDuration);
        //TODO wait a little
        for (int i = 0; i < choiceButtons.length; i++) {
            choiceButtons[i].setText(choices[progress][i]);
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
        sentenceView.setText("Score: " + score + "/" + sentences.length + ". Tap here to restart the exercises.");
        sentenceView.setOnClickListener(v -> restartExercises());
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
        String feedback = "That's wrong, you fool";
        if (choice == correctAnswers[progress]) {
            choiceButtons[choice].setBackgroundColor(Color.GREEN);
            feedback = "That's right, well done";
            score++;
        }
        else choiceButtons[choice].setBackgroundColor(Color.RED);

        String correctSentence = sentences[progress][0] + choices[progress][correctAnswers[progress]] + sentences[progress][1];
        Spannable spannable = new SpannableString(correctSentence);
        final int start = sentences[progress][0].length();
        final int end = start + choices[progress][correctAnswers[progress]].length();
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
            if (progress < sentences.length) showNextExerciseUi();
            else showScoreUi();
        }));
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