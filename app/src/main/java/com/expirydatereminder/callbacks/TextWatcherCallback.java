package com.expirydatereminder.callbacks;

public interface TextWatcherCallback {
    void onTextChanged(CharSequence charSequence, int start, int before, int count);

}
