package com.example.android.gloryinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gloryinventory.data.ProductContract;

public class DetailEditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Boolean flag that keeps track of whether the pet has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;
    /**
     * Identifier for the pet data loader
     */
    private static final int EXISTING_PRODUCT_LOADER = 0;
    /**
     * Content URI for the existing pet (null if it's a new pet)
     */
    private Uri mCurrentProductUri;
    private EditText mProductName;
    private EditText mProductPrice;
    private TextView mProductQuantity;
    private EditText mSupplierName;
    private ImageView mProductImg;
    Button increaseQuantity;
    Button decreaseQuantity;
    Button orderButton;
    Button deleteButton;
    Button saveButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_edit_layout);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        mSupplierName = (EditText) findViewById(R.id.edit_text_supplier);
        mProductName = (EditText) findViewById(R.id.edit_text_name);
        mProductPrice = (EditText) findViewById(R.id.edit_text_price);
        mProductQuantity = (TextView) findViewById(R.id.text_view_quantity);
        mProductImg = (ImageView) findViewById(R.id.image);

        increaseQuantity = (Button) findViewById(R.id.btn_quantity_increase);
        decreaseQuantity = (Button) findViewById(R.id.btn_quantity_decrease);
        deleteButton = (Button) findViewById(R.id.btn_delete);
        saveButton = (Button) findViewById(R.id.btn_save);
        orderButton = (Button) findViewById(R.id.btn_order);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSave();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOrder();
            }
        });

        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer quantity = Integer.parseInt(mProductQuantity.getText().toString().trim());
                if (quantity > 0) {
                    mProductQuantity.setText(Integer.toString(quantity-1));
                }
            }
        });
        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer quantity = Integer.parseInt(mProductQuantity.getText().toString().trim());

                    mProductQuantity.setText(Integer.toString(quantity+1));

            }
        });

    }


    public void onOrder() {
        String supplierName = mSupplierName.getText().toString().trim();
        String name = mProductName.getText().toString().trim();
        String message = createOrderSummary(name);
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { supplierName });
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject) + " " + name);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public String createOrderSummary(String name) {
        String message = "";
        message += getString(R.string.greetings) + "\n";
        message += getString(R.string.message) + "\n";
        message += getString(R.string.product_name_title) + " " + name + "\n";
        message += getString(R.string.thank);
        return message;
    }
    @Override
    public void onBackPressed() {


        //otherwise Protect user from loosing info
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel_deleting, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentProductUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {


            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int supplierColumnIndex = cursor.getColumnIndexOrThrow(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String supplierName = cursor.getString(supplierColumnIndex);
            Integer quantity = cursor.getInt(quantityColumnIndex);
            Float price = cursor.getFloat(priceColumnIndex);
            String imageURI = cursor.getString(imageColumnIndex);

            mProductName.setText(name);
            mSupplierName.setText(supplierName);
            mProductQuantity.setText(Integer.toString(quantity));
            mProductPrice.setText(Float.toString(price));
            if (imageURI != null) {
                mProductImg.setImageURI(Uri.parse(imageURI));
            }
        }

    }
    public void onSave() {
        saveProduct();
        finish();
    }

    private void saveProduct() {
        ContentValues values = new ContentValues();
        String name = mProductName.getText().toString().trim();
        String supplierName = mSupplierName.getText().toString().trim();
        Integer quantity = Integer.parseInt(mProductQuantity.getText().toString().trim());
        Float price = 0.0f;
        if(!"".equals(mProductPrice.getText().toString().trim()))
            price = Float.parseFloat(mProductPrice.getText().toString().trim());

        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, name);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierName);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, price);

        int rowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);

        if ( rowsUpdated  == 0) {
            Toast.makeText(this, getString(R.string.update_product_failed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.update_product_successful), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mSupplierName.setText("");
        mProductQuantity.setText(Integer.toString(0));
        mProductPrice.setText(Float.toString(0));
        mProductImg.setImageDrawable(null);
    }


}
