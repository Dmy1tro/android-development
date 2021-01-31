package dmytro.laskuryk.lab_4;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {
    private static Locale enLocale;
    private static Locale ukLocale;
    private static Locale appliedLocale;
    private TextView appTitle;
    private EditText filterEditText;
    private Spinner importanceSpinner;
    private Menu menu;

    public EditText getFilterEditText() { return  filterEditText;}
    public Spinner getImportanceSpinner() { return importanceSpinner; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyLocale(appliedLocale);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        appTitle = findViewById(R.id.appNameTextView);
        filterEditText = findViewById(R.id.filterNameEditText);
        importanceSpinner = findViewById(R.id.importanceSpinner);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.create_note_action:
                Fragment fr = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                NavHostFragment.findNavController(fr)
                        .navigate(R.id.CreateNoteFragment);
                break;

            case R.id.change_locale_action:
                changeLocale();

                // Lagaet
                //recreate();

                // Works ok
                appTitle.setText(getString(R.string.app_name));
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_main, menu);
                FragmentManager manager = getSupportFragmentManager().getFragments().get(0).getChildFragmentManager();

                Fragment currentFragment = manager.getFragments().get(0);

                manager.beginTransaction()
                        .detach(currentFragment)
                        .attach(currentFragment)
                        .commit();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void changeLocale() {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        Locale currentLocale = config.getLocales().get(0);

        if (enLocale == null || ukLocale == null) {
            enLocale = new Locale("en", "US");
            ukLocale = new Locale("uk", "UA");
        }

        if (currentLocale.getLanguage().equals("uk")) {
            appliedLocale = enLocale;
        } else {
            appliedLocale = ukLocale;
        }

        applyLocale(appliedLocale);
    }

    private void applyLocale(Locale locale) {
        if (locale == null) {
            return;
        }

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        Locale.setDefault(locale);
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}