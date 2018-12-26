package by.mkr.blackberry.textlayouttools;

import android.app.AlertDialog;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import DataBase.AppDatabase;
import DataBase.Correction;
import DataBase.CorrectionDao;

public class CorrectionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrections);

        final AppDatabase db = App.getInstance().getDatabase();
        final CorrectionDao correctionDao = db.correctionDao();

        final List<CorrectionItem> values = mapDbToViewItems(correctionDao.getAll());

        RecyclerView correctionsView = (RecyclerView)findViewById(R.id.corrections_view);
        correctionsView.setHasFixedSize(true);
        correctionsView.setLayoutManager(new LinearLayoutManager(this));
        final CorrectionAdapter correctionAdapter = new CorrectionAdapter(values);
        correctionAdapter.setListener(new CorrectionAdapter.CorrectionsListener() {
            @Override
            public void onItemAdded(int itemIndex, String newFromText, String newToText) {
                // itemIndex always eq -1
                Correction corr = new Correction(0, newFromText, newToText);
                long actualId = correctionDao.insert(corr);
                // TODO update the repo and get actual id
                values.add(new CorrectionItem(actualId, newFromText, newToText));
                correctionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onItemEdited(int itemIndex, String newFromText, String newToText) {
                values.get(itemIndex).fromText = newFromText;
                values.get(itemIndex).toText = newToText;
                correctionAdapter.notifyDataSetChanged();
                Log.d("ReplacerLog", "edited: " + values.get(itemIndex).fromText + " => " + values.get(itemIndex).toText);
                // TODO update the repo
                Correction corr = new Correction(values.get(itemIndex).id, newFromText, newToText);
                correctionDao.update(corr);
            }

            @Override
            public void onItemDeleted(int itemIndex) {
                Log.d("ReplacerLog", "deleted: " + values.get(itemIndex).fromText + " => " + values.get(itemIndex).toText);
                long itemId = values.get(itemIndex).id;
                values.remove(itemIndex);
                correctionAdapter.notifyDataSetChanged();
                // TODO update the repo
                correctionDao.deleteById(itemId);
            }
        });
        correctionsView.setAdapter(correctionAdapter);


        // Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.btnFloatAddCorrection);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correctionAdapter.addItem(view.getContext());
                Log.d("ReplacerLog", "added new");
            }
        });
    }

    private List<CorrectionItem> mapDbToViewItems(List<DataBase.Correction> items) {
        List<CorrectionItem> mappedItems = new ArrayList<>();
        for (DataBase.Correction item : items) {
            mappedItems.add(new CorrectionItem(item.id, item.fromText, item.toText));
        }
        return mappedItems;
    }
}
