package com.septmb.septmb.septmb.classes;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.septmb.septmb.septmb.R;
import com.septmb.septmb.septmb.commons.Commons;
import com.septmb.septmb.septmb.main.CoinbaseActivity;

/**
 * Created by Alex Michenko on 12.04.2017.
 */

public class TransactionDialog extends BottomSheetDialogFragment {

    private EditText etEmail;
    private EditText etAmount;
    private EditText etNote;
    private Button btnAction;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = View.inflate(getContext(), R.layout.dialog_transaction, null);

        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etEmail.setText(Commons.user.getEmail());
        etAmount = (EditText) view.findViewById(R.id.etAmount);
        etAmount.setText(Commons.productInfo.getPrice().replace("$","").replace("€","").replace("£","").replace("¥",""));
        etNote = (EditText) view.findViewById(R.id.etNote);
        btnAction = (Button) view.findViewById(R.id.btnAction);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String amount = etAmount.getText().toString();

                boolean hasEmail = isValidEmail(email);
                boolean hasAmount = isValidAmount(amount);

                if (hasEmail && hasAmount) {
                    ((CoinbaseActivity) getActivity()).setDialogResult(email, Integer.valueOf(amount), etNote.getText().toString());
                    dismiss();
                }
            }
        });

        dialog.setContentView(view);
    }

    private boolean isValidEmail(CharSequence email) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Field is require");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email is invalid");
            return false;
        }
        etEmail.setError(null);
        return true;
    }

    private boolean isValidAmount(String amount) {
        if (TextUtils.isEmpty(amount)) {
            etAmount.setError("Field is require");
            return false;
        }
        if (!(Integer.valueOf(amount) > 0)) {
            etAmount.setError("Need positive value");
            return false;
        }
        etAmount.setError(null);
        return true;
    }
}
