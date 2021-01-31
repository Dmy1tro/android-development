package dmytro.laskuryk.lab_4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import dmytro.laskuryk.lab_4.Interfaces.INoteRepository;
import dmytro.laskuryk.lab_4.Models.Importance;
import dmytro.laskuryk.lab_4.Models.Note;
import dmytro.laskuryk.lab_4.Repositories.ImageRepository;
import dmytro.laskuryk.lab_4.Repositories.RepositoryFactory;

@RequiresApi(api = Build.VERSION_CODES.R)
public class UpdateNoteFragment extends Fragment {
    private static final Integer PICK_IMAGE_CODE = 0;
    // Core
    private INoteRepository repository;
    private ImageRepository imageRepository;
    private Note note;
    private Boolean isImageSelected;
    private Boolean isImageCleared;
    // UI elements
    private EditText nameInput;
    private EditText descriptionInput;
    private Spinner importanceSpinner;
    private EditText appointmentInput;
    private Button pickImageButton;
    private Button clearImageButton;
    private Button saveButton;
    private Button resetButton;
    private Button backButton;
    private ImageView selectedImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.update_note_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = RepositoryFactory.getRepository(getContext());
        imageRepository = RepositoryFactory.getImageRepository(getContext());
        note = new Gson().fromJson(getArguments().getString("note"), Note.class);
        nameInput = view.findViewById(R.id.nameInputEditText);
        descriptionInput = view.findViewById(R.id.descriptionInputEditText);
        importanceSpinner = view.findViewById(R.id.importanceSpinner);
        appointmentInput = view.findViewById(R.id.appointmentInputEditText);
        pickImageButton = view.findViewById(R.id.pickImageButton);
        clearImageButton = view.findViewById(R.id.clearImageButton);
        saveButton = view.findViewById(R.id.saveButton);
        resetButton = view.findViewById(R.id.resetButton);
        backButton = view.findViewById(R.id.backButton);
        isImageSelected = false;
        isImageCleared = false;
        selectedImageView = view.findViewById(R.id.selectedImage);

        setUpView();
    }

    private void setUpView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                new String[]{
                        getString(Importance.Low.getResourceString()),
                        getString(Importance.Medium.getResourceString()),
                        getString(Importance.High.getResourceString()) });

        importanceSpinner.setAdapter(adapter);
        selectedImageView.setImageResource(R.drawable.empty_img);

        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshForm();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavHostFragment.findNavController(UpdateNoteFragment.this)
                        .navigate(R.id.NotesListFragment);
            }
        });

        clearImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isImageSelected = false;
                isImageCleared = true;
                selectedImageView.setImageResource(R.drawable.empty_img);
            }
        });

        refreshForm();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Select image"),
                PICK_IMAGE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != PICK_IMAGE_CODE || data == null) {
            return;
        }

        isImageSelected = true;
        selectedImageView.setImageURI(data.getData());
    }

    private void saveNote() {
        if (!isFormValid()) {
            makeToastMessage(getString(R.string.form_not_valid));
            return;
        }

        String name = nameInput.getText().toString();
        String description = descriptionInput.getText().toString();
        Importance importance = Importance.parse(importanceSpinner.getSelectedItemPosition());

        LocalDateTime appointmentDate = LocalDateTime.parse(
                appointmentInput.getText().toString(),
                Note.getRequiredDateTimeFormatter());

        String selectedImage = note.ImagePath;

        if (isImageCleared && !isImageSelected) {
            selectedImage = null;

            if (note.hasImage()) {
                imageRepository.remove(note.ImagePath);
            }
        }

        if (isImageSelected) {
            Bitmap bitmapImage = ((BitmapDrawable) selectedImageView.getDrawable()).getBitmap();
            selectedImage = imageRepository.save(bitmapImage);

            if (note.hasImage()) {
                imageRepository.remove(note.ImagePath);
            }
        }

        note.setData(name, description, importance, appointmentDate, selectedImage);

        repository.update(note);

        makeToastMessage(getString(R.string.saved_successfully));
        NavHostFragment.findNavController(this)
                .navigate(R.id.NotesListFragment);
    }

    private void refreshForm() {
        isImageSelected = false;
        isImageCleared = false;
        nameInput.setText(note.Name);
        descriptionInput.setText(note.Description);
        importanceSpinner.setSelection(note.Importance.getValue());
        appointmentInput.setText(note.AppointmentDate);
        if (note.hasImage()) {
            Bitmap img = imageRepository.get(note.ImagePath);
            selectedImageView.setImageBitmap(img);
        } else {
            selectedImageView.setImageResource(R.drawable.empty_img);
        }
    }

    private Boolean isFormValid() {
        Pattern pattern = Note.getDateTimePattern();
        boolean findMatches = pattern.matcher(appointmentInput.getText().toString()).find();
        boolean dateParsedOk = false;

        if (findMatches) {
            try {
                LocalDateTime.parse(appointmentInput.getText().toString(), Note.getRequiredDateTimeFormatter());
                dateParsedOk = true;
            } catch (Exception ignored) {
            }
        }

        return !nameInput.getText().toString().isEmpty() &&
                !descriptionInput.getText().toString().isEmpty() &&
                !importanceSpinner.isSelected() &&
                findMatches &&
                dateParsedOk;
    }

    private void makeToastMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}