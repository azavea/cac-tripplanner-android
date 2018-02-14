package com.gophillygo.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gophillygo.app.R;


public class PlaceCategoryGridAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;

    private static class ViewHolder {
        ImageView imageView;
        TextView categoryNameView;
    }

    private static final int CATEGORIES_COUNT = 6;

    private static final String[] placeCategoryNames = {
            "Upcoming events",
            "Want to go",
            "Nature",
            "Places you like",
            "Exercise",
            "Educational"
    };

    private static final String[] placeCategoryImages = {
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/4c5f9e802b89495da7e485a4449df220.jpg",
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/60a86b43597a4b8f8e129f9d6435960a.jpg",
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/e6aa6bc0891247c4a4d651f22c028fe6.jpg",
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/874f2bd93b5f4bc692cf39d1aaba5ead.jpg",
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/4c5f9e802b89495da7e485a4449df220.jpg",
            "https://cleanair-images-prod.s3.amazonaws.com/destinations/ad72d3d20dfb4197b76c7b4d211a8eef.jpg"
    };

    public PlaceCategoryGridAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return CATEGORIES_COUNT;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.place_category_grid_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = convertView.findViewById(R.id.place_category_grid_item_image);
            viewHolder.categoryNameView = convertView.findViewById(R.id.place_category_grid_item_name);

            // size the image to fill its square grid box
            ViewGroup.LayoutParams params = viewHolder.imageView.getLayoutParams();
            int columnSize = ((GridView) parent).getColumnWidth();
            params.width = columnSize;
            params.height = columnSize;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Glide.with(context)
                .load(placeCategoryImages[position])
                .into(viewHolder.imageView);

        viewHolder.imageView.setContentDescription(placeCategoryNames[position]);
        viewHolder.categoryNameView.setText(placeCategoryNames[position]);

        return convertView;
    }
}
