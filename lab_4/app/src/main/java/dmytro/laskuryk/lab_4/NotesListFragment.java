package dmytro.laskuryk.lab_4;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;

import java.util.List;
import java.util.function.Predicate;

import dmytro.laskuryk.lab_4.Adapters.NoteAdapter;
import dmytro.laskuryk.lab_4.Interfaces.INoteRepository;
import dmytro.laskuryk.lab_4.Models.Importance;
import dmytro.laskuryk.lab_4.Models.Note;
import dmytro.laskuryk.lab_4.Repositories.ImageRepository;
import dmytro.laskuryk.lab_4.Repositories.RepositoryFactory;

@RequiresApi(api = Build.VERSION_CODES.R)
public class NotesListFragment extends Fragment {
    private INoteRepository repository;
    private ImageRepository imageRepository;
    private ListView notesListView;

    // Filters
    private String currentFilterName = null;
    private @Nullable Integer currentImportancePosition = null;
    private Spinner importanceSpinner;
    private EditText nameFilterEditText;
    private TextWatcher watcher;
    private Predicate<Note> importanceFilter;
    private Predicate<Note> nameFilter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("nameFilter")) {
                currentFilterName = savedInstanceState.getString("nameFilter");
            }

            if (savedInstanceState.containsKey("importanceFilter")) {
                currentImportancePosition = savedInstanceState.getInt("importanceFilter");
            }
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.notes_list_fragment, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        nameFilterEditText.removeTextChangedListener(watcher);
        nameFilterEditText.setText("");
        nameFilterEditText.setVisibility(View.INVISIBLE);

        importanceSpinner.setSelection(0);
        importanceSpinner.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (nameFilter != null) {
            outState.putString("nameFilter", nameFilterEditText.getText().toString());
        }

        if (importanceFilter != null) {
            outState.putInt("importanceFilter", importanceSpinner.getSelectedItemPosition());
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = RepositoryFactory.getRepository(getContext());
        imageRepository = RepositoryFactory.getImageRepository(getContext());
        notesListView = view.findViewById(R.id.notesListView);
        nameFilterEditText = ((MainActivity)getActivity()).getFilterEditText();
        importanceSpinner = ((MainActivity)getActivity()).getImportanceSpinner();

        setUpView();
    }

    private void setUpView() {
        nameFilterEditText.setVisibility(View.VISIBLE);
        importanceSpinner.setVisibility(View.VISIBLE);
        ArrayAdapter<String> importanceAdapter = new ArrayAdapter<>(
                getContext(),
                R.layout.spinner_appbar_item,
                new String[] {
                        getString(R.string.all),
                        getString(Importance.Low.getResourceString()),
                        getString(Importance.Medium.getResourceString()),
                        getString(Importance.High.getResourceString())
                });
        importanceSpinner.setAdapter(importanceAdapter);

        importanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                NoteAdapter adapter = (NoteAdapter) notesListView.getAdapter();
                String name = nameFilterEditText.getText().toString();

                if (position != 0) {
                    Importance selectedImportance = Importance.parse(position - 1);
                    List<Note> filtered = repository.getByFilter(
                            name,
                            selectedImportance);
                    adapter.clear();
                    adapter.addAll(filtered);
                } else {
                    List<Note> all = repository.getByFilter(name, null);
                    adapter.clear();
                    adapter.addAll(all);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        nameFilterEditText.addTextChangedListener(watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NoteAdapter adapter = (NoteAdapter) notesListView.getAdapter();

                Importance importance = null;
                if (importanceSpinner.getSelectedItemPosition() != 0) {
                    importance = Importance.parse(importanceSpinner.getSelectedItemPosition() - 1);
                }

                if (!s.toString().isEmpty()) {
                    List<Note> filtered = repository.getByFilter(s.toString(), importance);
                    adapter.clear();
                    adapter.addAll(filtered);
                } else {
                    List<Note> all = repository.getByFilter(null, importance);
                    adapter.clear();
                    adapter.addAll(all);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        nameFilterEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    nameFilterEditText.clearFocus();
                }
                return false;
            }
        });

        notesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showNoteInfo(parent, position);
            }
        });

        notesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return showActionMenu(parent, position);
            }
        });

        List<Note> allNotes = repository.getAll();

        NoteAdapter noteAdapter = new NoteAdapter(getContext(), R.layout.note_item, allNotes);
        notesListView.setAdapter(noteAdapter);

        if (currentFilterName != null) {
            nameFilterEditText.setText(currentFilterName);
            currentFilterName = null;
        }

        if (currentImportancePosition != null) {
            importanceSpinner.setSelection(currentImportancePosition);
            currentImportancePosition = null;
        }
    }

    private void showNoteInfo(AdapterView<?> parent, int position) {
        NoteAdapter adapter = (NoteAdapter)parent.getAdapter();
        Note note = adapter.getItem(position);
        LayoutInflater inflater = getLayoutInflater();
        View itemInfoView = inflater.inflate(R.layout.note_info, null);

        ((TextView)itemInfoView.findViewById(R.id.nameValueTextView)).setText(note.Name);
        ((TextView)itemInfoView.findViewById(R.id.descriptionValueTextView)).setText(note.Description);
        ((TextView)itemInfoView.findViewById(R.id.importanceValueTextView)).setText(getString(note.Importance.getResourceString()));
        ((ImageView)itemInfoView.findViewById(R.id.icon_image)).setImageResource(note.Importance.getResource());
        ((TextView)itemInfoView.findViewById(R.id.creationDateValueTextView)).setText(note.CreationDate);
        ((TextView)itemInfoView.findViewById(R.id.appointmentDateValueTextView)).setText(note.AppointmentDate);

        if (note.hasImage()) {
            Bitmap img = imageRepository.get(note.ImagePath);
            ((ImageView)itemInfoView.findViewById(R.id.imgValueImageView)).setImageBitmap(img);
        } else {
            ((ImageView)itemInfoView.findViewById(R.id.imgValueImageView)).setImageResource(R.drawable.empty_img);
        }

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.note_info))
                .setView(itemInfoView)
                .setPositiveButton("Ok", null)
                .create()
                .show();
    }

    private boolean showActionMenu(AdapterView<?> parent, int position) {
        NoteAdapter adapter = (NoteAdapter) parent.getAdapter();
        Note note = adapter.getItem(position);

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.note_action_title))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getString(R.string.note_action).replace("$", note.Name))
                .setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        Bundle bundle = new Bundle();
                        bundle.putString("note", new Gson().toJson(note));

                        NavHostFragment.findNavController(NotesListFragment.this)
                                .navigate(R.id.UpdateNoteFragment, bundle);
                    }
                })
                .setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        new AlertDialog.Builder(getContext())
                                .setTitle(getString(R.string.delete))
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setMessage(getString(R.string.confirm_delete).replace("$", note.Name))
                                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        if (note.hasImage()) {
                                            imageRepository.remove(note.ImagePath);
                                        }

                                        repository.delete(note.Id);
                                        adapter.remove(note);
                                    }
                                })
                                .setNegativeButton(getString(R.string.no), null)
                                .create()
                                .show();
                    }
                })
                .setNeutralButton(getString(R.string.back), null)
                .create()
                .show();

        return true;
    }
}