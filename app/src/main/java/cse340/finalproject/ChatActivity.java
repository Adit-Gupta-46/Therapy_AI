package cse340.finalproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.*;
import android.speech.*;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.*;
import androidx.recyclerview.widget.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import okhttp3.*;
import org.json.*;

/**
 The ChatActivity class represents the activity for the "Chat" section of the app, extending
 the AppCompatActivity class
 */
public class ChatActivity extends AppCompatActivity {

    // UI elements
    private EditText messageBox;
    private RecyclerView recyclerView;
    private MessagesAdapter messageAdapter;
    private List<Message> messageList;
    private SpeechRecognizer mRecognizer = null;
    private View microphoneListening;

    // Constants for GPT-3 API call
    private static final double TEMPERATURE = 0.7;
    private static final int NUM_OF_TOKENS = 1024;
    private static final String GPT_MODEL = "text-davinci-003";
    private static final String GATEWAY_URL = "https://api.openai.com/v1/completions";

    // okhttp client to make API calls
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    /**
     * Launcher for requesting the RECORD_AUDIO permission.
     */
    private ActivityResultLauncher<String> mPermissionRequester = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startSpeechRecognition();
                } else {
                    Toast.makeText(this, R.string.microphone_justification,
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set content view to chat_layout.xml
        setContentView(R.layout.chat_layout);

        // Initialize UI elements
        messageList = new ArrayList<>();
        recyclerView =  findViewById(R.id.messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessagesAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        ImageView send_btn = findViewById(R.id.send_btn);
        ImageView mic_btn = findViewById(R.id.microphone_btn);
        messageBox = findViewById(R.id.message_box);

        // Create a new microphone listening View
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        microphoneListening = inflater.inflate(R.layout.microphone_being_shown, null);
        microphoneListening.setScaleX(0.5f);
        microphoneListening.setScaleY(0.5f);
        microphoneListening.setVisibility(View.INVISIBLE);

        // Set the layout parameters of the image
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        microphoneListening.setLayoutParams(params);
        addContentView(microphoneListening, params);

        // Send message button listener
        send_btn.setOnClickListener(view -> {
            String question = messageBox.getText().toString().trim();
            messageBox.setText("");
            if (question.equals("")) {
                Toast.makeText(this, R.string.message_required, Toast.LENGTH_LONG).show();
            } else {
                addMessage(question,Message.SEND_BY_ME);
                messageList.add(new Message(getString(R.string.typing_message),
                        Message.SEND_BY_BOT));
                callGPTAPI(getGptPrompt(question));
            }
        });

        // Microphone button listener
        mic_btn.setOnClickListener(view -> getPermissionsAndRecognizeSpeech());

        // Get bottom navigation view and set OnNavigationItemSelectedListener
        TabView nav = findViewById(R.id.bottom_nav); // Tabs at the bottom of screen.
        nav.setOnNavigationItemSelectedListener(item -> {
            Intent switchActivityIntent;
            // Switch to the selected activity based on the selected item
            switch (item.getItemId()) {
                case R.id.tab_profile:
                    switchActivityIntent = new Intent(this, ProfileActivity.class);
                    startActivity(switchActivityIntent);
                    finish();
                    return true;
                case R.id.tab_chat:
                    return true;
                case R.id.tab_about:
                    switchActivityIntent = new Intent(this, AboutActivity.class);
                    startActivity(switchActivityIntent);
                    finish();
                    return true;
                default:
                    // Log error if tab item is not recognized
                    Log.e(getString(R.string.error_tag),
                            getString(R.string.unrecognized_nav_error) + item.getTitle());
            }
            return false;
        });

        // send first Message
        addMessage(getResources().getString(R.string.first_message), Message.SEND_BY_BOT);

    }

    @Override
    public void onPause() {
        super.onPause();
        // Used to skip animation for transition
        // learned from https://stackoverflow.com/questions/6972295/how-to-switch-activity-
        // without-animation-in-android
        overridePendingTransition(0, 0);
    }

    /**
     * Adds a new message to the message list with the given message and sender, and updates the
     * adapter and recycler view. Runs on the UI thread
     *
     * @param message the message to add
     * @param sendBy the sender of the message
     */
    private void addMessage(String message, int sendBy){
        runOnUiThread(() -> {
            messageList.add(new Message(message, sendBy));
            messageAdapter.notifyDataSetChanged();
            // Scroll to the last message
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            if (messageAdapter.currentMessage != null) {
                messageAdapter.currentMessage.sendAccessibilityEvent(
                        AccessibilityEvent.TYPE_VIEW_FOCUSED);
                messageAdapter.currentMessage = null;
            }
        });
    }

    /**
     * Removes the last message ("typing") from the message list and adds a response message
     * with the given response text and sender
     *
     * @param response the response to add
     */
    private void addResponse(String response){
        // Remove "typing" message
        messageList.remove(messageList.size()-1);
        addMessage(response, Message.SEND_BY_BOT);
    }

    /**
     * Returns the prompt to use for the GPT API given the user input. The prompt includes the
     * user's name and a list of their struggles
     *
     * @param prompt the user's input
     * @return the prompt to use for the GPT API
     */
    private String getGptPrompt(String prompt) {
        // Get preferences for name and struggles context
        SharedPreferences pref = getSharedPreferences(ProfileActivity.PREFERENCES_KEY,MODE_PRIVATE);
        Set<String> struggles = pref.getStringSet(ProfileActivity.STRUGGLES_KEY, new HashSet<>());
        StringBuilder strugglesString = new StringBuilder();
        for (String s : struggles) {
            strugglesString.append(s).append(", ");
        }
        String name = pref.getString(ProfileActivity.NAME_KEY, "anonymous");

        return String.format(getString(R.string.gpt_prompt), name, name, strugglesString,
                name, prompt);
    }

    /**
     * Calls the GPT API with the given question. Then adds the response to the list when
     * it arrives.
     *
     * @param question the question to ask the API
     */
    private void callGPTAPI(String question){
        // Heavily referencing code from https://github.com/TheoKanning/openai-java and
        // https://github.com/AtikulSoftware/ChatGPT

        // Create a JSON object with the necessary parameters for the GPT API request
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model",GPT_MODEL);
            jsonBody.put("prompt", question);
            jsonBody.put("max_tokens",NUM_OF_TOKENS);
            jsonBody.put("temperature",TEMPERATURE);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Create a request with the JSON object
        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url(GATEWAY_URL)
                .header("Authorization","Bearer "+ getString(R.string.gpt_api_key))
                .post(requestBody)
                .build();

        // Enqueue the HTTP request asynchronously
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle the case when the request fails by adding an error message
                // to the list of responses
                addResponse(String.format(getString(R.string.failed_response), e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws
                    IOException {
                // Parse the response JSON and add the result to the list of responses
                if (response.isSuccessful()){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        addResponse(result.trim());

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Handle the case when the request fails by adding an error message
                    // to the list of responses
                    addResponse(String.format(getString(R.string.failed_response),
                            response.body().toString()));
                }
            }
        });
    }

    /**
     * Checks for RECORD_AUDIO permission and starts speech recognition if the permission
     * is granted. Otherwise, requests the permission or displays a message explaining why the
     * permission is needed.
     */
    private void getPermissionsAndRecognizeSpeech() {
        if (getBaseContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, start speech recognition
            startSpeechRecognition();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            // If the user has previously denied the permission, show a message explaining why
            // the permission is needed and request the permission again
            Toast.makeText(this,R.string.microphone_justification,
                    Toast.LENGTH_LONG).show();
            mPermissionRequester.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            // Request the permission
            mPermissionRequester.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    /**
     * Starts the speech recognition process using the device's microphone.
     */
    private void startSpeechRecognition() {

        // Create a new speech recognizer if it hasn't been created before and set its listeners
        if (mRecognizer == null){
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
            mRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                    messageBox.setText(R.string.listening);
                    microphoneListening.setVisibility(View.VISIBLE);
                }

                @Override
                public void onBeginningOfSpeech() {}

                @Override
                public void onRmsChanged(float v) {}

                @Override
                public void onBufferReceived(byte[] bytes) {}

                @Override
                public void onEndOfSpeech() {}

                @Override
                public void onError(int i) {
                    // reset state
                    messageBox.setText("");
                    microphoneListening.setVisibility(View.INVISIBLE);
                    String errorString = getErrorString(i);
                    Toast.makeText(getBaseContext(), errorString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResults(Bundle bundle) {
                    // Get the speech results and update the message box
                    ArrayList<String> texts = bundle.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION);
                    if (texts.size() == 0) {
                        messageBox.setText("");
                        return;
                    }
                    String text = texts.get(0);
                    messageBox.setText(text);
                    // Hide microphone icon
                    microphoneListening.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onPartialResults(Bundle bundle) {
                    // Get the partial speech recognition results and update the message box
                    ArrayList<String> texts = bundle.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION);
                    if (texts.size() == 0) {
                        messageBox.setText("");
                        return;
                    }
                    String text = texts.get(0);
                    messageBox.setText(text);
                }

                @Override
                public void onEvent(int i, Bundle bundle) {

                }
            });

        }

        // Create an intent for speech recognition and set its parameters
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);
        mRecognizer.startListening(recognizerIntent);
    }

    /**
     * Returns a string describing the error code.
     *
     * @param errorCode the error code to describe
     * @return a string describing the error code
     */
    private String getErrorString(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return getString(R.string.speech_error_1);
            case SpeechRecognizer.ERROR_NETWORK:
                return getString(R.string.speech_error_2);
            case SpeechRecognizer.ERROR_AUDIO:
                return getString(R.string.speech_error_3);
            case SpeechRecognizer.ERROR_SERVER:
                return getString(R.string.speech_error_4);
            case SpeechRecognizer.ERROR_CLIENT:
                return getString(R.string.speech_error_5);
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return getString(R.string.speech_error_6);
            case SpeechRecognizer.ERROR_NO_MATCH:
                return getString(R.string.speech_error_7);
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return getString(R.string.speech_error_8);
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return getString(R.string.speech_error_9);
            case SpeechRecognizer.ERROR_TOO_MANY_REQUESTS:
                return getString(R.string.speech_error_10);
            case SpeechRecognizer.ERROR_SERVER_DISCONNECTED:
                return getString(R.string.speech_error_11);
            case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:
                return getString(R.string.speech_error_12);
            case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE:
                return getString(R.string.speech_error_13);
            default:
                return getString(R.string.speech_error_unknown);
        }
    }
}
