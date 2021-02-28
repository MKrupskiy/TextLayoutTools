package by.mkr.blackberry.textlayouttools;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class CorrectionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppThemeHelper.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrections);

        final AppSettings appSettings = ReplacerService.getAppSettings();

        final List<CorrectionViewItem> values = mapSettingToViewItems(appSettings.corrections);

        RecyclerView correctionsView = findViewById(R.id.corrections_view);
        correctionsView.setHasFixedSize(true);
        correctionsView.setLayoutManager(new LinearLayoutManager(this));
        final CorrectionAdapter correctionAdapter = new CorrectionAdapter(values);
        correctionAdapter.setListener(new CorrectionAdapter.CorrectionsListener() {
            @Override
            public void onItemAdded(int itemIndex, String newFromText, String newToText) {
                appSettings.corrections.add(new CorrectionItem(newFromText, newToText));
                values.add(new CorrectionViewItem(itemIndex, newFromText, newToText));
                correctionAdapter.notifyDataSetChanged();
                // update the repo
                appSettings.updateCorrections(appSettings.corrections);
            }

            @Override
            public void onItemEdited(int itemIndex, String newFromText, String newToText) {
                values.get(itemIndex).fromText = newFromText;
                values.get(itemIndex).toText = newToText;
                correctionAdapter.notifyDataSetChanged();
                ReplacerService.log("edited: " + values.get(itemIndex).fromText + " => " + values.get(itemIndex).toText);
                // update the repo
                CorrectionItem corrItem = appSettings.corrections.get(itemIndex);
                corrItem.from = newFromText;
                corrItem.to = newToText;
                appSettings.updateCorrections(appSettings.corrections);
            }

            @Override
            public void onItemDeleted(int itemIndex) {
                ReplacerService.log("deleted: " + values.get(itemIndex).fromText + " => " + values.get(itemIndex).toText);
                long itemId = values.get(itemIndex).id;
                values.remove(itemIndex);
                correctionAdapter.notifyDataSetChanged();
                // update the repo
                appSettings.corrections.remove(itemIndex);
                appSettings.updateCorrections(appSettings.corrections);
            }
        });
        correctionsView.setAdapter(correctionAdapter);


        // Floating Action Button
        FloatingActionButton fab = findViewById(R.id.btnFloatAddCorrection);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correctionAdapter.addItem(view.getContext());
                ReplacerService.log("added new");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private List<CorrectionViewItem> mapSettingToViewItems(List<CorrectionItem> items) {
        List<CorrectionViewItem> mappedItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            mappedItems.add(new CorrectionViewItem(i, items.get(i).from, items.get(i).to));
        }
        return mappedItems;
    }
}
