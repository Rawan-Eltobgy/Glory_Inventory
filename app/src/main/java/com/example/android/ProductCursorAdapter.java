package com.example.android.gloryinventory;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.gloryinventory.data.ProductContract;


/**
 * Created by user on 9/5/2017.
 */
public class ProductCursorAdapter extends CursorAdapter {
    public static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();
    int rowsUpdated;
    LayoutInflater mInflater = LayoutInflater.from(mContext);
    ContentResolver cr = mContext.getContentResolver();
    Cursor cursor = cr.query(ProductContract.ProductEntry.CONTENT_URI, null, null, null, null);

    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.cursor = cursor;
         //   cur.moveToFirst();
       // initializeChecked();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView  quantityView = (TextView) view.findViewById(R.id.quantity);
        TextView  priceView = (TextView) view.findViewById(R.id.price);
        ImageView imageView = (ImageView) view.findViewById(R.id.product_img);
       // ImageButton sellButton = (ImageButton) view.findViewById(R.id.sell_btn);

        // Find the columns of pet attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);


         String name= cursor.getString(nameColumnIndex);
         final String imagePath = cursor.getString(imageColumnIndex);
        String price = cursor.getString(priceColumnIndex);
        String quantity = cursor.getString(quantityColumnIndex);

      //Update the TextViews with the attributes for the current product
        float tempQuatity = Float.parseFloat(Float.toString(Float.parseFloat(quantity)));
        final int quantityInt = (int)Math.round(tempQuatity);
        nameView.setText(name);
        quantityView.setText(Integer.toString(quantityInt));
        priceView.setText(Float.toString(Float.parseFloat(price)));

        if (TextUtils.isEmpty(name)) {
            name = context.getString(R.string.unknown_price);
        }
        if (TextUtils.isEmpty(quantity)) {
            context.getString(R.string.unknown_quantity);
        }
        if (TextUtils.isEmpty(price)) {
            price = context.getString(R.string.unknown_product);
        }
        //
        if (imagePath != null) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageURI(null);
            imageView.setImageURI(Uri.parse(imagePath));
        } else {
            imageView.setVisibility(View.GONE);
        }

        final String finalName = name;

        final String finalPrice = price;



}}