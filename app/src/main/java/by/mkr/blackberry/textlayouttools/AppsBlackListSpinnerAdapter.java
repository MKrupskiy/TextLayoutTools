package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;


public class AppsBlackListSpinnerAdapter extends ArrayAdapter<String>{

    private final LayoutInflater _mInflater;
    private final Context _mContext;
    private final List<AppsBlackListItem> _items;
    private final int _mResource;

    public AppsBlackListSpinnerAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
        super(context, resource, 0, objects);

        _mContext = context;
        _mInflater = LayoutInflater.from(context);
        _mResource = resource;
        _items = objects;
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        final View view = _mInflater.inflate(_mResource, parent, false);

        TextView packageName = (TextView) view.findViewById(R.id.textViewPackageName);
        TextView label = (TextView) view.findViewById(R.id.textViewLabel);
        ImageView icon = (ImageView) view.findViewById(R.id.imageViewIcon);

        AppsBlackListItem packageItem = _items.get(position);

        packageName.setText(packageItem.packageName);
        label.setText(packageItem.label);
        icon.setImageDrawable(packageItem.icon);

        return view;
    }
}
