package com.SIMATS.smartcity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiChatActivity extends AppCompatActivity implements ChatMessageAdapter.OnChatButtonClickListener {

    private enum ConversationState {
        IDLE, AWAITING_PROBLEM_CONFIRMATION, AWAITING_DATETIME, AWAITING_IMAGE_CHOICE, READY_TO_PUBLISH
    }
    private ConversationState currentState = ConversationState.IDLE;

    private ImageButton btnClose, btnUploadImage, btnSendMessage;
    private EditText etChatInput;
    private RecyclerView recyclerViewChat;
    private LottieAnimationView loadingIndicator;
    private LinearLayout actionButtonsLayout;
    private Button btnActionPositive, btnActionNegative;
    private List<ChatMessage> messageList;
    private ChatMessageAdapter chatMessageAdapter;
    private GenerativeModelFutures generativeModel;
    private List<Content> chatHistory;
    private Executor backgroundExecutor;
    private String confirmedIssueType = "", confirmedIssueDetails = "", confirmedDateTime = "";
    private Uri confirmedImageUri = null;
    private Bitmap confirmedBitmap = null;
    private Calendar calendar;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.civicbuddy);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);

        initializeViews();
        setupChatRecyclerView();
        initializeGemini();
        setupImagePickerLauncher();
        setupButtons();

        addMessage(new ChatMessage("Hello! I'm Civic Buddy. Please describe the civic issue you'd like to report.", ChatMessage.TYPE_AI));
        updateUiForState();
    }

    private void initializeGemini() {
        // Initialize model
        // Use the model name that is confirmed to be working for your key
        GenerativeModel gm = new GenerativeModel("gemini-2.0-flash", BuildConfig.GEMINI_API_KEY);
        generativeModel = GenerativeModelFutures.from(gm);
        backgroundExecutor = Executors.newSingleThreadExecutor();
        chatHistory = new ArrayList<>();

        // --- SYSTEM INSTRUCTION (strong, explicit behavior rules) ---
        String systemInstructionText =
                "You are Civic Buddy, the official AI assistant of the SmartCity – PWD Neighborhood App. " +
                        "Personality: friendly, patient, clear, and helpful. Speak in simple sentences. " +
                        "Primary Objective: Help users report and track civic issues, explain app features, provide guidance for using the manual complaint form, and provide PWD emergency contact details when asked. " +
                        "Scope: civic issues only (e.g., potholes, street light failures, drainage, garbage, waterlogging, road markings, speed breakers, other public works). If asked outside scope, politely refuse and redirect to civic issue reporting. " +

                        "RESPONSE FORMATTING RULES (follow precisely): " +
                        "1) When you have successfully parsed a user-provided civic issue (text or image), reply ONLY with JSON using EXACT format: " +
                        "{\"status\":\"success\",\"issue_type\":\"<one of: Potholes, Waterlogging, Garbage on Road, Street Light Not Working, Cracks on Road, Damaged Speed Breaker, Blocked Drainage, Faded Road Markings, Other>\",\"issue_details\":\"<short human-readable summary>\"}" +
                        "2) When user's description is ambiguous or incomplete, reply ONLY with JSON using EXACT format: " +
                        "{\"status\":\"clarification_needed\",\"question\":\"<one short clarifying question to obtain missing detail>\"}" +
                        "3) For greetings or off-topic small talk, respond in plain text (natural language), not JSON. Example: 'Hello! I can help you report civic issues. What problem would you like to report?' " +
                        "4) For feature questions (e.g., 'How do I report?', 'What features?'), respond in plain text and provide step-by-step actions or short, numbered instructions. " +
                        "5) For emergency contact queries, provide PWD emergency phone and email in plain text and briefly explain when to use them. Use placeholders if the real contact is not available. " +
                        "6) Do not invent personal data or claim authority; if asked for official timelines/commitments, refer the user to PWD or contractor and suggest they use the tracking feature. " +
                        "7) If the user uploads an image, acknowledge receipt and either classify (if clear) or ask for clarification. If classification is possible, respond with the JSON success format above. " +

                        "WORKFLOW RULES: " +
                        "A. Step-by-step complaint filing: 1) Identify issue type & details. 2) Confirm summary with the user. 3) Ask for approximate date/time. 4) Ask if user wants to attach an image (optional). 5) Create the complaint and show a confirmation / complaint ID. " +
                        "B. Tracking: If asked 'track my complaint' or 'status', ask for complaint ID (if not provided). " +
                        "C. Admin / contractor actions: If the user identifies as a contractor or admin, provide guidance for forwarding complaints, requesting after-completion images from contractors, and marking status as completed. " +
                        "D. After-Completion Proof: The system supports 'before' and 'after' images. Explain to users that contractors must upload an after image and that the user will be notified when it is available. " +

                        "YOUR KNOWLEDGE BASE: " +
                        // NEW: Added specific emergency numbers
                        "* Emergency Contacts: If asked for 'emergency numbers' or 'helpline', provide the relevant number and suggest opening the Emergency page. Example Response: 'The PWD Helpline is 1800-xxxx-001. For a full list of contacts like Electricity (1912) and Water Supply (1916), I recommend opening the Emergency Contacts page for direct calling.' " +

                        "EXAMPLES & FEW-SHOT RULES: The assistant will follow the examples below as exact templates for JSON responses and plain-language replies. Do NOT deviate from the JSON keys or required wording when producing the JSON outputs.";

        // Add system instruction to history
        chatHistory.add(new Content.Builder().addText(systemInstructionText).build());

        // --- FEW-SHOT EXAMPLES: user utterance followed by assistant response (either JSON or text) ---
        // 1: Greeting (plain text)
        chatHistory.add(new Content.Builder().addText("User: hi").build());
        chatHistory.add(new Content.Builder().addText("Assistant: Hello! I'm Civic Buddy. I can help you report civic issues like potholes, streetlights, drainage, or garbage. What would you like to report?").build());

        // 2: Clear issue -> success JSON
        chatHistory.add(new Content.Builder().addText("User: There is a huge pothole on Main Street near the bakery").build());
        chatHistory.add(new Content.Builder().addText("{\"status\":\"success\",\"issue_type\":\"Potholes\",\"issue_details\":\"Large pothole reported near the bakery on Main Street.\"}").build());

        // 3: Vague issue -> clarification JSON
        chatHistory.add(new Content.Builder().addText("User: There is a problem with the road").build());
        chatHistory.add(new Content.Builder().addText("{\"status\":\"clarification_needed\",\"question\":\"Could you please specify what kind of road problem it is — a pothole, a crack, waterlogging, or something else?\"}").build());

        // 4: User asks how to report (plain text step-by-step)
        chatHistory.add(new Content.Builder().addText("User: How do I report a problem?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: To report an issue: 1) Tap 'File a Complaint'. 2) Choose the issue type. 3) Add a short description. 4) Attach an optional photo. 5) Confirm and submit. You can also use the chat assistant to guide you step-by-step.").build());

        // 5: User asks about features (plain text)
        chatHistory.add(new Content.Builder().addText("User: What features does this app have?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: The app supports: image-based reporting, GPS location tagging, complaint tracking, complaint history, profile management, and an in-app admin workflow where complaints are exported to PWD/contractors. A chatbot helps file complaints by asking questions.").build());

        // 6: Image provided, clearly showing garbage -> success JSON
        chatHistory.add(new Content.Builder().addText("User: (uploads an image showing a pile of garbage on the street)").build());
        chatHistory.add(new Content.Builder().addText("{\"status\":\"success\",\"issue_type\":\"Garbage on Road\",\"issue_details\":\"Pile of garbage blocking the street as shown in the image.\"}").build());

        // 7: Image ambiguous -> clarification JSON
        chatHistory.add(new Content.Builder().addText("User: (uploads a blurry photo of a road patch)").build());
        chatHistory.add(new Content.Builder().addText("{\"status\":\"clarification_needed\",\"question\":\"The image is a bit unclear. Is this a pothole, a repair patch, or waterlogging? Please describe what you see or upload a clearer photo.\"}").build());

        // 8: User asks for tracking status -> plain text guiding question
        chatHistory.add(new Content.Builder().addText("User: How can I check my complaint status?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: You can check status in 'My Complaints' on the dashboard. Please provide your complaint ID here and I'll look up the status for you.").build());

        // 9: User asks who receives the complaint -> plain text
        chatHistory.add(new Content.Builder().addText("User: Who sees my complaint?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: Complaints are forwarded to the Public Works Department (PWD) and, when necessary, to assigned contractors. Personal contact details are kept private and shared only with authorized PWD staff.").build());

        // 10: Admin/contractor guidance (plain text)
        chatHistory.add(new Content.Builder().addText("User: I'm a contractor — how do I upload the after image when work is done?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: Contractors can use the admin interface or the designated endpoint to upload completion images. After uploading, mark the complaint as 'Completed' and the user will receive the after image notification. Ensure the image clearly shows the fixed area.").build());

        // 11: After-completion image explanation (plain text)
        chatHistory.add(new Content.Builder().addText("User: How will I know the work is completed?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: When the contractor uploads the 'after' photo and the PWD updates the status to 'Completed', you will receive a notification and be able to view the after image in the complaint details.").build());

        // 12: Emergency contact question (plain text)
        chatHistory.add(new Content.Builder().addText("User: What is the emergency contact for PWD?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: For urgent road emergencies, contact the PWD helpline at +91-1800-000-0000 or email pwd.help@municipality.gov. Use this for hazards that require immediate attention (major sinkholes, collapsed roads, severe flooding). Replace the placeholders with your local PWD contacts if available.").build());

        // 13: Asking for help using the manual complaint form (plain text + example)
        chatHistory.add(new Content.Builder().addText("User: Can you open the manual complaint form?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: Sure — I can open the manual complaint form for you. Or, I can guide you step-by-step here in chat and then create the complaint. Which do you prefer?").build());

        // 14: Example—user chooses to use AI: assistant leads to JSON success
        chatHistory.add(new Content.Builder().addText("User: There's a broken street light near Park Avenue").build());
        chatHistory.add(new Content.Builder().addText("{\"status\":\"success\",\"issue_type\":\"Street Light Not Working\",\"issue_details\":\"Street light broken near Park Avenue causing darkness at night.\"}").build());

        // 15: Example—user gives vague "water problem" -> clarification
        chatHistory.add(new Content.Builder().addText("User: water issue on road").build());
        chatHistory.add(new Content.Builder().addText("{\"status\":\"clarification_needed\",\"question\":\"Is the problem waterlogging, a leaking pipe, or poor drainage? Please specify the exact issue.\"}").build());

        // 16: Example—user asks about privacy (plain text)
        chatHistory.add(new Content.Builder().addText("User: Is my phone number visible to others?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: No. Your personal contact details are kept private and shared only with authorized PWD staff where necessary for follow-up.").build());

        // 17: Example—user asks for next steps after confirmation (plain text)
        chatHistory.add(new Content.Builder().addText("User: I confirmed the issue, what next?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: After confirmation, the system will create a complaint record and assign an ID. Complaints may be exported to PWD or forwarded to a contractor. You will receive status updates and a completion proof image when work is done.").build());

        // 18: Example—user asks to edit a submitted complaint (plain text)
        chatHistory.add(new Content.Builder().addText("User: I want to edit my complaint").build());
        chatHistory.add(new Content.Builder().addText("Assistant: You cannot edit a complaint once it has been submitted. If the status is still 'Pending', you can delete the existing complaint from your Track page and then file a new one with the correct information.").build());

        // 19: Example—user asks about offline mode (plain text)
        chatHistory.add(new Content.Builder().addText("User: Can I submit a complaint when I have no internet?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: Yes. The app supports offline complaint creation which will sync automatically when your device reconnects to the internet. Attachments upload will also retry when you are back online.").build());

        // 20: Example—user asks for a sample JSON summary re-affirmation (success)
        chatHistory.add(new Content.Builder().addText("User: Summarize this: 'big pothole outside school on 3rd cross'").build());
        chatHistory.add(new Content.Builder().addText("{\"status\":\"success\",\"issue_type\":\"Potholes\",\"issue_details\":\"Large pothole outside the school on 3rd Cross Road.\"}").build());

        // 21: Example—user asks for instructions how to attach photo (plain text)
        chatHistory.add(new Content.Builder().addText("User: How do I attach a photo?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: Tap the camera icon, choose 'Take Photo' or 'Choose from Gallery'. If taking a photo, ensure the problem area is centered and well-lit. Then confirm upload.").build());

        // 22: Example—user asks "who will fix it" (plain text)
        chatHistory.add(new Content.Builder().addText("User: Who will fix my complaint?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: Complaints are handled by PWD and, where required, assigned contractors. The system forwards the problem to the responsible team and they will update the status and upload an after-completion image.").build());

        // 23: Example—user asks "can I cancel complaint" (plain text)
        chatHistory.add(new Content.Builder().addText("User: Can I cancel a complaint?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: You can cancel a complaint while it is still in 'Pending' status via 'My Complaints'. If already assigned or in progress, please contact PWD support via support page to request cancellation.").build());

        // 24: Example—user asks for multilingual support (plain text)
        chatHistory.add(new Content.Builder().addText("User: Can I use the app in my native language?").build());
        chatHistory.add(new Content.Builder().addText("Assistant: The app supports multiple languages in planned updates. Currently, English is fully supported; regional language support will be added soon.").build());

        // 25: Example—error handling / API failure (plain text)
        chatHistory.add(new Content.Builder().addText("User: The app shows an error when submitting").build());
        chatHistory.add(new Content.Builder().addText("Assistant: I'm sorry. Please try again in a moment. If the problem persists, capture the error message and contact PWD support with the details. You can also try reconnecting to the internet and resubmitting.").build());

        // The above 25 examples provide a broad range of cases (greetings, explicit JSON outputs, clarifications, features, admin flows, emergency contacts, upload guidance, offline mode, cancel/edit requests, error handling).
        // They will bias the model to return JSON in "success" and "clarification_needed" cases and plain text otherwise.

        // Add a final short instruction content for response constraints
        String finalNote = "When replying with JSON, be strict about JSON format (no added text). When replying with plain text, be concise (1-3 sentences for short answers and 3-6 sentences for step-by-step instructions).";
        chatHistory.add(new Content.Builder().addText(finalNote).build());
    }


    private void initializeViews() {
        btnClose = findViewById(R.id.btn_close);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnSendMessage = findViewById(R.id.btn_send_message);
        etChatInput = findViewById(R.id.et_chat_input);
        recyclerViewChat = findViewById(R.id.recycler_view_chat);
        loadingIndicator = findViewById(R.id.loading_indicator);
        actionButtonsLayout = findViewById(R.id.action_buttons_layout);
        btnActionPositive = findViewById(R.id.btn_action_positive);
        btnActionNegative = findViewById(R.id.btn_action_negative);
    }

    private void setupChatRecyclerView() {
        messageList = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(messageList, this);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatMessageAdapter);
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                                if(currentState == ConversationState.AWAITING_IMAGE_CHOICE || currentState == ConversationState.IDLE) {
                                    confirmedImageUri = imageUri;
                                    confirmedBitmap = bitmap;
                                    addMessage(new ChatMessage("Image uploaded, analyzing...", confirmedBitmap));
                                    Content userInput = new Content.Builder().addImage(confirmedBitmap).build();
                                    sendContentToGemini(userInput);
                                }
                            } catch (IOException e) {
                                Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void setupButtons() {
        btnClose.setOnClickListener(v -> finish());
        btnUploadImage.setOnClickListener(v -> imagePickerLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)));

        btnSendMessage.setOnClickListener(v -> {
            String message = etChatInput.getText().toString().trim();
            if (!message.isEmpty()) {
                etChatInput.setText("");
                addMessage(new ChatMessage(message, ChatMessage.TYPE_USER));

                if (currentState == ConversationState.AWAITING_DATETIME) {
                    confirmedDateTime = message;
                    currentState = ConversationState.AWAITING_IMAGE_CHOICE;
                    addMessage(new ChatMessage("Thank you. Would you like to add a photo? This is optional but very helpful.", ChatMessage.TYPE_AI));
                } else {
                    resetComplaintData();
                    sendContentToGemini(new Content.Builder().addText(message).build());
                }
                updateUiForState();
            }
        });

        btnActionPositive.setOnClickListener(v -> handlePositiveAction());
        btnActionNegative.setOnClickListener(v -> handleNegativeAction());
    }

    @Override
    public void onChatButtonClick(ChatMessage message) {
        if ("Pick Date & Time".equals(message.getButtonText())) {
            showDatePicker();
        } else if ("Create Complaint Form".equals(message.getButtonText())) {
            createComplaintForm();
        }
    }

    private void sendContentToGemini(Content userInput) {
        showLoading(true);
        addMessage(new ChatMessage("...", ChatMessage.TYPE_AI));
        chatHistory.add(userInput);

        Futures.addCallback(generativeModel.generateContent(chatHistory.toArray(new Content[0])), new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                if (result.getCandidates() != null && !result.getCandidates().isEmpty()) {
                    chatHistory.add(result.getCandidates().get(0).getContent());
                }
                String responseText = result.getText();
                processGeminiResponse(responseText);
            }
            @Override
            public void onFailure(Throwable t) {
                if (chatHistory.size() > 1) {
                    chatHistory.remove(chatHistory.size() - 1);
                }
                handleGeminiFailure(t);
            }
        }, backgroundExecutor);
    }

    private void processGeminiResponse(String responseText) {
        runOnUiThread(() -> {
            showLoading(false);
            if (!messageList.isEmpty()) {
                messageList.remove(messageList.size() - 1);
            }
            try {
                String cleanedJson = responseText.replace("```json", "").replace("```", "").trim();
                JSONObject jsonObject = new JSONObject(cleanedJson);
                String status = jsonObject.getString("status");

                switch (status) {
                    case "success":
                        confirmedIssueType = jsonObject.getString("issue_type");
                        confirmedIssueDetails = jsonObject.getString("issue_details");
                        addMessage(new ChatMessage("Okay, I understand. I've summarized the issue as:\n\n**Type:** " + confirmedIssueType + "\n**Details:** " + confirmedIssueDetails + "\n\nIs this correct?", ChatMessage.TYPE_AI));
                        currentState = ConversationState.AWAITING_PROBLEM_CONFIRMATION;
                        break;
                    case "clarification_needed":
                        String question = jsonObject.getString("question");
                        addMessage(new ChatMessage(question, ChatMessage.TYPE_AI));
                        currentState = ConversationState.IDLE;
                        break;
                }
            } catch (JSONException e) {
                addMessage(new ChatMessage(responseText, ChatMessage.TYPE_AI));
                currentState = ConversationState.IDLE;
            }
            chatMessageAdapter.notifyDataSetChanged();
            recyclerViewChat.scrollToPosition(messageList.size() - 1);
            updateUiForState();
        });
    }

    private void handleGeminiFailure(Throwable t) {
        Log.e("GeminiFailure", "AI request failed: ", t);
        runOnUiThread(() -> {
            showLoading(false);
            if (!messageList.isEmpty() && messageList.get(messageList.size() - 1).getText().equals("...")) {
                messageList.remove(messageList.size() - 1);
            }
            addMessage(new ChatMessage("I'm having a little trouble connecting right now. Please try again in a moment.", ChatMessage.TYPE_AI));
            chatMessageAdapter.notifyDataSetChanged();
            recyclerViewChat.scrollToPosition(messageList.size() - 1);
            currentState = ConversationState.IDLE;
            updateUiForState();
        });
    }

    private void handlePositiveAction() {
        switch (currentState) {
            case AWAITING_PROBLEM_CONFIRMATION:
                addMessage(new ChatMessage("Yes, that's correct.", ChatMessage.TYPE_USER));
                ChatMessage datePickerMessage = new ChatMessage(
                        "Great! To help us, please provide the approximate date and time this occurred.",
                        "Pick Date & Time",
                        null, null, null, null
                );
                addMessage(datePickerMessage);
                currentState = ConversationState.IDLE;
                break;
            case AWAITING_IMAGE_CHOICE:
                addMessage(new ChatMessage("Add a photo.", ChatMessage.TYPE_USER));
                imagePickerLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                break;
            case READY_TO_PUBLISH:
                createComplaintForm();
                break;
        }
        updateUiForState();
    }

    private void handleNegativeAction() {
        switch (currentState) {
            case AWAITING_PROBLEM_CONFIRMATION:
                addMessage(new ChatMessage("No, that's not right.", ChatMessage.TYPE_USER));
                addMessage(new ChatMessage("My apologies. Please describe the issue again in more detail.", ChatMessage.TYPE_AI));
                resetComplaintData();
                currentState = ConversationState.IDLE;
                break;
            case AWAITING_IMAGE_CHOICE:
                addMessage(new ChatMessage("No, thanks.", ChatMessage.TYPE_USER));
                currentState = ConversationState.READY_TO_PUBLISH;
                ChatMessage finalConfirmation = new ChatMessage(
                        "Okay, no problem. I have all the information needed.",
                        "Create Complaint Form",
                        confirmedIssueType,
                        confirmedIssueDetails,
                        confirmedDateTime,
                        confirmedImageUri
                );
                addMessage(finalConfirmation);
                break;
        }
        updateUiForState();
    }

    private void showDatePicker() {
        calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    confirmedDateTime = sdf.format(calendar.getTime());

                    addMessage(new ChatMessage("Set to: " + confirmedDateTime, ChatMessage.TYPE_USER));
                    currentState = ConversationState.AWAITING_IMAGE_CHOICE;
                    addMessage(new ChatMessage("Thank you. Would you like to add a photo? This is optional but very helpful.", ChatMessage.TYPE_AI));
                    updateUiForState();
                },
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        );
        timePickerDialog.show();
    }

    private void updateUiForState() {
        runOnUiThread(() -> {
            actionButtonsLayout.setVisibility(View.GONE);
            btnActionNegative.setVisibility(View.VISIBLE);
            etChatInput.setVisibility(View.VISIBLE);
            btnSendMessage.setVisibility(View.VISIBLE);
            btnUploadImage.setVisibility(View.VISIBLE);
            etChatInput.setHint("Type a message...");

            switch (currentState) {
                case AWAITING_PROBLEM_CONFIRMATION:
                    actionButtonsLayout.setVisibility(View.VISIBLE);
                    btnActionPositive.setText("Yes, correct");
                    btnActionNegative.setText("No, rephrase");
                    etChatInput.setVisibility(View.GONE);
                    btnSendMessage.setVisibility(View.GONE);
                    btnUploadImage.setVisibility(View.GONE);
                    break;
                case AWAITING_IMAGE_CHOICE:
                    actionButtonsLayout.setVisibility(View.VISIBLE);
                    btnActionPositive.setText("Add Photo");
                    btnActionNegative.setText("No, thanks");
                    etChatInput.setVisibility(View.GONE);
                    btnSendMessage.setVisibility(View.GONE);
                    btnUploadImage.setVisibility(View.GONE);
                    break;
                case READY_TO_PUBLISH:
                    // This state is now handled by the button within the chat message itself,
                    // so we don't need a separate UI state for the action buttons.
                    etChatInput.setVisibility(View.GONE);
                    btnSendMessage.setVisibility(View.GONE);
                    btnUploadImage.setVisibility(View.GONE);
                    break;
                default:
                    actionButtonsLayout.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void addMessage(ChatMessage message) {
        runOnUiThread(() -> {
            messageList.add(message);
            chatMessageAdapter.notifyDataSetChanged();
            if (!messageList.isEmpty()) {
                recyclerViewChat.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void resetComplaintData() {
        confirmedIssueType = "";
        confirmedIssueDetails = "";
        confirmedDateTime = "";
        confirmedImageUri = null;
        confirmedBitmap = null;
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE));
    }

    public void createComplaintForm() {
        Intent intent = new Intent(GeminiChatActivity.this, actmannualcomplaint.class);
        intent.putExtra("PREFILL_ISSUE_TYPE", confirmedIssueType);
        intent.putExtra("PREFILL_ISSUE_DETAILS", confirmedIssueDetails);
        intent.putExtra("PREFILL_DATE_TIME", confirmedDateTime);
        if (confirmedImageUri != null) {
            intent.putExtra("PREFILL_IMAGE_URI", confirmedImageUri.toString());
        }
        startActivity(intent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}