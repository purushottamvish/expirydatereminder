package com.expirydatereminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

public class HelpDialogHandler extends AppCompatDialogFragment {
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.help_layout,null);

        builder.setView(v)
                .setPositiveButton("ᴏᴋ", (dialogInterface, i) -> {});
        return builder.create();
    }
}
