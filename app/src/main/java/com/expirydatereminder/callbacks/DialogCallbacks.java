package com.expirydatereminder.callbacks;



public interface DialogCallbacks {

    void onDialogPositiveButtonClicked(String forWhat);

    default void onDialogNegativeButtonClicked(String forWhat) {
    }

}
