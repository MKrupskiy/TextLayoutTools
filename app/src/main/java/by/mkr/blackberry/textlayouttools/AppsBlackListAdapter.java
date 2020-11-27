package by.mkr.blackberry.textlayouttools;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import by.mkr.controls.ToggleState;
import by.mkr.controls.ThreeStateToggleView;


class AppsBlackListItem {
    public String packageName;
    public String label;
    public Drawable icon;
    public BlacklistItemBlockState state;

    public AppsBlackListItem() {
        this("", "", null, BlacklistItemBlockState.None);
    }

    public AppsBlackListItem(String packageName, String label, Drawable icon, BlacklistItemBlockState state) {
        this.packageName = packageName;
        this.label = label;
        this.icon = icon;
        this.state = state;
    }
}


enum BlacklistItemBlockState {
    None,
    Autocorrect,
    All;

    public static BlacklistItemBlockState fromString(String x) {
        switch (x) {
            case "None":
                return None;
            case "Autocorrect":
                return Autocorrect;
            case "All":
                return All;
            default:
                return null;
        }
    }

    public BlacklistItemBlockState getNext() {
        switch (this) {
            case None:
                return All;
            case Autocorrect:
                return None;
            case All:
                return Autocorrect;
            default:
                return null;
        }
    }

    /// Animations:
    /// https://medium.com/@burakcanekici/android-animation-example-with-animated-vector-drawable-and-svg-file-3e511b77cb0c
    public int getResource() {
        switch (this) {
            case All:
                return R.drawable.vector_toggle_left;
            case Autocorrect:
                return R.drawable.vector_toggle_middle;
            case None:
                return R.drawable.vector_toggle_right;
            default:
                return R.drawable.vector_toggle_left;
        }
    }

    public ToggleState getToggleState() {
        switch (this) {
            case All:
                return ToggleState.Left;
            case Autocorrect:
                return ToggleState.Middle;
            case None:
                return ToggleState.Right;
            default:
                return ToggleState.Left;
        }
    }

    public String getDescription() {
        switch (this) {
            case All:
                return "";
            case Autocorrect:
                return "Ctrl+Q";
            case None:
                return "Ctrl+Q\nAuto";
            default:
                return "";
        }
    }
}


public class AppsBlackListAdapter extends RecyclerView.Adapter<AppsBlackListAdapter.MyViewHolder> {


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView packageName;
        public TextView label;
        public ImageView icon;
        public ThreeStateToggleView toggle;
        public TextView textViewState;
        public int itemIndex;

        public LinearLayout layoutView;


        public MyViewHolder(final LinearLayout layoutView, final AppsBlackListListener listener) {
            super(layoutView);
            packageName = layoutView.findViewById(R.id.textViewPackageName);
            label = layoutView.findViewById(R.id.textViewLabel);
            icon = layoutView.findViewById(R.id.imageViewIcon);
            toggle = layoutView.findViewById(R.id.toggle);
            textViewState = layoutView.findViewById(R.id.textViewState);
            this.layoutView = layoutView;

            toggle.addOnToggleListener(new ThreeStateToggleView.ThreeStateToggleListener() {
                @Override
                public void onToggle(ToggleState state) {
                    BlacklistItemBlockState newState = BlacklistItemBlockState.All;
                    switch (state) {
                        case Left:
                            newState = BlacklistItemBlockState.All;
                            break;
                        case Middle:
                            newState = BlacklistItemBlockState.Autocorrect;
                            break;
                        case Right:
                            newState = BlacklistItemBlockState.None;
                            break;
                        default:
                            break;
                    }
                    listener.onItemStateChanged(itemIndex, newState);
                }
            });
        }

        public void bind(AppsBlackListItem value) {
            packageName.setText(value.packageName);
            label.setText(value.label);
            icon.setImageDrawable(value.icon);
        }

    }



    private final List<AppsBlackListItem> _values;
    private AppsBlackListListener _listener;

    public AppsBlackListAdapter(List<AppsBlackListItem> values) {
        _values = values;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LinearLayout layoutView = (LinearLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_appblacklist_item, viewGroup, false);
        MyViewHolder vh = new MyViewHolder(layoutView, _listener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        AppsBlackListItem item = _values.get(i);
        myViewHolder.itemIndex = i;
        myViewHolder.packageName.setText(item.packageName);
        myViewHolder.label.setText(item.label);
        myViewHolder.icon.setImageDrawable(item.icon);
        myViewHolder.toggle.setToggle(item.state.getToggleState());
        myViewHolder.textViewState.setText(item.state.getDescription());
    }



    @Override
    public int getItemCount() {
        return _values.size();
    }

    public void setListener(final AppsBlackListListener listener) {
        this._listener = listener;
    }

    public interface AppsBlackListListener {
        void onItemStateChanged(int itemIndex, BlacklistItemBlockState state);
    }
}
