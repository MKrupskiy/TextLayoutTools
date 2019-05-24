package by.mkr.blackberry.textlayouttools;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

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
        Log.d("ReplacerLog", "! onCreate");

        final AppDatabase db = AppDatabase.getInstance(this);
        final CorrectionDao correctionDao = db.correctionDao();

        final List<CorrectionItem> values = mapDbToViewItems(correctionDao.getAll());

        RecyclerView correctionsView = (RecyclerView)findViewById(R.id.corrections_view);
        correctionsView.setHasFixedSize(true);
        correctionsView.setLayoutManager(new LinearLayoutManager(this));
        final CorrectionAdapter correctionAdapter = new CorrectionAdapter(values);
        correctionAdapter.setListener(new CorrectionAdapter.CorrectionsListener() {
            @Override
            public void onItemAdded(int itemIndex, String newFromText, String newToText) {
                Correction corr = new Correction(0, newFromText, newToText);
                // update the repo and get actual id
                long actualId = correctionDao.insert(corr);
                values.add(new CorrectionItem(actualId, newFromText, newToText));
                correctionAdapter.notifyDataSetChanged();
                App.updateCorrections(correctionDao.getAll());
            }

            @Override
            public void onItemEdited(int itemIndex, String newFromText, String newToText) {
                values.get(itemIndex).fromText = newFromText;
                values.get(itemIndex).toText = newToText;
                correctionAdapter.notifyDataSetChanged();
                Log.d("ReplacerLog", "edited: " + values.get(itemIndex).fromText + " => " + values.get(itemIndex).toText);
                // update the repo
                Correction corr = new Correction(values.get(itemIndex).id, newFromText, newToText);
                correctionDao.update(corr);
                App.updateCorrections(correctionDao.getAll());
            }

            @Override
            public void onItemDeleted(int itemIndex) {
                Log.d("ReplacerLog", "deleted: " + values.get(itemIndex).fromText + " => " + values.get(itemIndex).toText);
                long itemId = values.get(itemIndex).id;
                values.remove(itemIndex);
                correctionAdapter.notifyDataSetChanged();
                // update the repo
                correctionDao.deleteById(itemId);
                App.updateCorrections(correctionDao.getAll());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ReplacerLog", "! onDestroy");
        AppDatabase.releaseDB();
    }

    private List<CorrectionItem> mapDbToViewItems(List<DataBase.Correction> items) {
        List<CorrectionItem> mappedItems = new ArrayList<>();
        for (DataBase.Correction item : items) {
            mappedItems.add(new CorrectionItem(item.id, item.fromText, item.toText));
        }
        return mappedItems;
    }
}
