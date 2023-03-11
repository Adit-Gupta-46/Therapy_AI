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

public class ChatActivity extends AppCompatActivity {

    private EditText messageBox;
    private RecyclerView recyclerView;
    private MessagesAdapter messageAdapter;
    private List<Message> messageList;
    private SpeechRecognizer mRecognizer = null;
    private View microphoneListening;

    private static final double TEMPERATURE = 0.7;
    private static final int NUM_OF_TOKENS = 1024;
    private static final String GPT_MODEL = "text-davinci-003";
    private static final String GATEWAY_URL = "https://api.openai.com/v1/completions";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        messageList = new ArrayList<>();

        recyclerView =  findViewById(R.id.messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessagesAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);

        ImageView send_btn = findViewById(R.id.send_btn);
        ImageView mic_btn = findViewById(R.id.microphone_btn);
        messageBox = findViewById(R.id.message_box);

        send_btn.setOnClickListener(view -> {
            String question = messageBox.getText().toString().trim();
            messageBox.setText("");
            addMessage(question,Message.SEND_BY_ME);
            callGPTAPI(getGptPrompt(question));
        });

        mic_btn.setOnClickListener(view -> getPermissionsAndRecognizeSpeech());

        // Create a new microphone listening View
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        microphoneListening = inflater.inflate(R.layout.microphone_being_shown, null);
        microphoneListening.setScaleX(0.5f);
        microphoneListening.setScaleY(0.5f);
        microphoneListening.setVisibility(View.INVISIBLE);

        // Set the layout parameters of the image
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        microphoneListening.setLayoutParams(params);
        addContentView(microphoneListening, params);

        // Creates the tabs at the bottom of screen.
        TabView nav = findViewById(R.id.bottom_nav);
        nav.setOnNavigationItemSelectedListener(item -> {

            // Set tab contents based on selected item.
            switch (item.getItemId()) {
                case R.id.tab_profile:
                    Intent switchActivityIntent = new Intent(this, ProfileActivity.class);
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
                    Log.e(getString(R.string.error_tag), getString(R.string.unrecognized_nav_error) + item.getTitle());
            }
            return false;
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        // Used to skip animation for transition
        // learned from https://stackoverflow.com/questions/6972295/how-to-switch-activity-without-animation-in-android
        overridePendingTransition(0, 0);
    }

    private void addMessage(String message, int sendBy){
        runOnUiThread(() -> {
            messageList.add(new Message(message, sendBy));
            messageAdapter.notifyDataSetChanged();
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
        });
    }

    private void addResponse(String response){
        messageList.remove(messageList.size()-1);
        addMessage(response, Message.SEND_BY_BOT);
    }

    private String getGptPrompt(String input) {
        SharedPreferences pref = getSharedPreferences(ProfileActivity.PREFERENCES_KEY,MODE_PRIVATE);
        Set<String> struggles = pref.getStringSet(ProfileActivity.STRUGGLES_KEY, new HashSet<>());
        StringBuilder strugglesString = new StringBuilder();
        for (String s : struggles) {
            strugglesString.append(s).append(", ");
        }
        String name = pref.getString(ProfileActivity.NAME_KEY, "anonymous");

        return String.format(getString(R.string.gpt_prompt), name, name, strugglesString, name, input);
    }

    // Heavily referencing code from https://github.com/TheoKanning/openai-java and https://github.com/AtikulSoftware/ChatGPT
    private void callGPTAPI(String question){
        // okhttp
        messageList.add(new Message(getString(R.string.typing_message), Message.SEND_BY_BOT));

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model",GPT_MODEL);
            jsonBody.put("prompt", question);
            jsonBody.put("max_tokens",NUM_OF_TOKENS);
            jsonBody.put("temperature",TEMPERATURE);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url(GATEWAY_URL)
                .header("Authorization","Bearer "+ getString(R.string.gpt_api_key))
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse(String.format(getString(R.string.failed_response), e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
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
                    addResponse(String.format(getString(R.string.failed_response), response.body().toString()));
                }
            }
        });
    }

    private ActivityResultLauncher<String> mPermissionRequester = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startSpeechRecognition();
                } else {
                    Toast.makeText(this, R.string.microphone_justification, Toast.LENGTH_LONG).show();
                }
            });

    private void getPermissionsAndRecognizeSpeech() {
        if (getBaseContext().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this,R.string.microphone_justification, Toast.LENGTH_LONG).show();
            mPermissionRequester.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            // ask for permission
            mPermissionRequester.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startSpeechRecognition() {
        final TextView outputView = findViewById(R.id.message_box);

        if (mRecognizer == null){
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
            mRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle bundle) {
                    outputView.setText(R.string.listening);
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
                    outputView.setText("");
                    microphoneListening.setVisibility(View.INVISIBLE);
                    String errorString = getErrorString(i);
                    Toast.makeText(getBaseContext(), errorString, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResults(Bundle bundle) {
                    ArrayList<String> texts = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (texts.size() == 0) {
                        outputView.setText("");
                        return;
                    }
                    String text = texts.get(0);
                    outputView.setText(text);
                    microphoneListening.setVisibility(View.INVISIBLE);

                }

                @Override
                public void onPartialResults(Bundle bundle) {
                    ArrayList<String> texts = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (texts.size() == 0) {
                        outputView.setText("");
                        return;
                    }
                    String text = texts.get(0);
                    outputView.setText(text);
                }

                @Override
                public void onEvent(int i, Bundle bundle) {

                }
            });

        }

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 2);
        mRecognizer.startListening(recognizerIntent);
    }

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
