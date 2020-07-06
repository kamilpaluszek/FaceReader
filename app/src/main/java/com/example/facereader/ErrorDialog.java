package com.example.facereader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialogFragment;

//klasa okna błędu
public class ErrorDialog extends AppCompatDialogFragment {
    public static ErrorDialog newInstance(String message) {
        ErrorDialog frag = new ErrorDialog();
        Bundle args = new Bundle();
        //ustawienie argumentu o kluczu "message"
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //odczyt klucza "message"
        String message = getArguments().getString("message");
        //zbudowanie przy pomocy AlertDialog opcji naszego okna
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Error!")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        return builder.create();
    }
}