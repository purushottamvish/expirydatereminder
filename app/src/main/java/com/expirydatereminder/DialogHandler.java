package com.expirydatereminder;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.time.Year;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class DialogHandler extends AppCompatDialogFragment implements AdapterView.OnItemSelectedListener {
    private EditText name, editTextDate, month, year, date;
    private ExampleDialogListener listener;
    private Spinner spinner;
    private List<String> CATEGORIES;
    SettingsDatabaseHandler settingsDatabaseHandler;
    Calendar cal = Calendar.getInstance(TimeZone.getDefault()); // Get current date

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.item_activity_layout, null);
  //      LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog.
        // Pass null as the parent view because it's going in the dialog layout.
       // builder.setView(inflater.inflate(R.layout.dialog_signin, null))

        builder.setView(view)

                //.setIcon(R.drawable.shopping_cart_24)
              //  .setTitle("Add Item")


                .setNegativeButton("ᴄᴀɴᴄᴇʟ", (dialogInterface, i) -> {
                })
                .setPositiveButton("ᴏᴋ", (dialogInterface, i) -> {
                    if (!name.getText().toString().trim().isEmpty()) {


                        if (!month.getText().toString().isEmpty()) {
                            String itemName = name.getText().toString().trim();
                            int expiryMonth = Integer.parseInt(month.getText().toString());
                            int expiryYear = 0;
                            if (!year.getText().toString().isEmpty()) {
                                expiryYear = Integer.parseInt(year.getText().toString());
                            } else expiryYear = Year.now().getValue();

                            int d = 0;
                            String item_category = spinner.getSelectedItem().toString();

                            if (expiryMonth < 13 && expiryMonth > 0) {
                                if (date.getText().toString().isEmpty()) {
                                    d = YearMonth.of(expiryYear, expiryMonth).lengthOfMonth();
                                } else {
                                    d = Integer.parseInt(date.getText().toString());
                                    if (d < 1 || d > YearMonth.of(expiryYear, expiryMonth).lengthOfMonth()) {
                                        Toast.makeText(getContext(), "Incorrect date!", Toast.LENGTH_SHORT).show();
                                        date.setText("");
                                        return;
                                    }
                                }
                                if (year.getText().toString().isEmpty()) {
                                    listener.addItemAsNeeded(itemName, d, expiryMonth, Year.now().getValue(), item_category);
                                } else {
                                    System.out.println("d=\t\t" + d);
                                    if (expiryYear > 999 && expiryYear < 10000)
                                        listener.addItemAsNeeded(itemName, d, expiryMonth, expiryYear, item_category);
                                    else {
                                        Toast.makeText(getContext(), "Invalid Year, it should be in YYYY format", Toast.LENGTH_SHORT).show();
                                        year.setText("");
                                    }
                                }
                            } else
                                Toast.makeText(getContext(), "Invalid month, it should be between 1 and 12", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getContext(), "Month cannot be empty", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                });

        name = view.findViewById(R.id.name_dialog_box_editText);
        month = view.findViewById(R.id.month_dialog_box_editText);
        year = view.findViewById(R.id.year_dialog_box_editText);
        date = view.findViewById(R.id.date_dialog_box_editText);

        //date piker data add

       // editTextDate = view.findViewById(R.id.editTextDate);
     //   editTextDate.setOnClickListener(view1 -> openDatePickerDialog(view));


        settingsDatabaseHandler = new SettingsDatabaseHandler(getContext());
        CATEGORIES = settingsDatabaseHandler.getCategories();

        spinner = view.findViewById(R.id.spinner_category_selector_add_item);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_dropdown_item, CATEGORIES);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);


        return builder.create();
    }


   /* public void openDatePickerDialog(final View v) {
        // Get Current Date
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, year, monthOfYear, dayOfMonth) -> {

                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;

                    v.findViewById(R.id.editTextDate);
                    editTextDate.setText(selectedDate);
                    switch (v.getId()) {
                        case R.id.editTextDate:
                            ((EditText) v).setText(selectedDate);
                            break;
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));


        datePickerDialog.getDatePicker().setMaxDate(cal.getTimeInMillis());
        datePickerDialog.show();
    }*/


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (ExampleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + "must implement ExampleDialogListener");
        }
    }

    public interface ExampleDialogListener {
        void addItemAsNeeded(String item_name, int date, int month, int year, String category_name);

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getContext(), CATEGORIES.get(i), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}