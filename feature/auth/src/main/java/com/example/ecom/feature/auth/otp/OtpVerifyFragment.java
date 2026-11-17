package com.example.ecom.feature.auth.otp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecom.core.api.AuthApiService;
import com.example.ecom.core.api.RetrofitClient;
import com.example.ecom.core.data.ApiResponse;
import com.example.ecom.core.data.AuthDtos;
import com.example.ecom.core.navigation.AppPages;
import com.example.ecom.core.navigation.AppRouter;
import com.example.ecom.core.ui.base.BaseFragment;
import com.example.ecom.feature.auth.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpVerifyFragment extends BaseFragment {

    private String email;
    private TextView tvOtpEmail;
    private EditText inputOtp;
    private TextView tvOtpError;
    private TextView btnResendOtp;
    private CountDownTimer countDownTimer;

    public OtpVerifyFragment() {
        super(R.layout.fragment_otp_verify);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        email = args != null ? args.getString(AppPages.ARG_EMAIL, "") : "";

        View btnBack = view.findViewById(R.id.headerBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> AppRouter.back(this));
        }

        tvOtpEmail = view.findViewById(R.id.tvOtpEmail);
        inputOtp = view.findViewById(R.id.inputOtp);
        tvOtpError = view.findViewById(R.id.tvOtpError);
        Button btnConfirmOtp = view.findViewById(R.id.btnConfirmOtp);
        btnResendOtp = view.findViewById(R.id.btnResendOtp);

        if (tvOtpEmail != null) {
            tvOtpEmail.setText(email);
        }

        if (btnConfirmOtp != null) {
            btnConfirmOtp.setOnClickListener(v -> verifyOtp());
        }

        if (btnResendOtp != null) {
            btnResendOtp.setOnClickListener(v -> resendOtp());
        }
    }

    private void verifyOtp() {
        String otp = inputOtp.getText().toString().trim();
        if (otp.length() < 6) {
            showError("Vui lòng nhập đủ 6 chữ số OTP!");
            return;
        }

        showError(null);
        showLoading(true);

        AuthApiService api = RetrofitClient.getAuthApiService(requireContext());
        api.verifyOtp(new AuthDtos.VerifyOtpRequest(email, otp)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                showLoading(false);
                String msg = getResponseMessage(response);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    toast(msg.isEmpty() ? "Xác thực OTP thành công!" : msg);
                    AppRouter.toLogin(OtpVerifyFragment.this);
                } else {
                    showError(msg.isEmpty() ? "Mã OTP không chính xác hoặc đã hết hạn!" : msg);
                    toast(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Không thể kết nối tới máy chủ");
                toast("Không thể kết nối tới máy chủ");
            }
        });
    }

    private void resendOtp() {
        showError(null);
        startResendTimer();

        AuthApiService api = RetrofitClient.getAuthApiService(requireContext());
        api.resendOtp(new AuthDtos.ResendOtpRequest(email)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                String msg = getResponseMessage(response);
                toast(msg.isEmpty() ? "Đã gửi lại mã OTP tới email!" : msg);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                toast("Không thể gửi lại mã OTP. Vui lòng thử lại!");
            }
        });
    }

    private void startResendTimer() {
        if (btnResendOtp == null) return;
        btnResendOtp.setEnabled(false);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (btnResendOtp != null) {
                    btnResendOtp.setText("Gửi lại mã (" + (millisUntilFinished / 1000) + "s)");
                }
            }

            @Override
            public void onFinish() {
                if (btnResendOtp != null) {
                    btnResendOtp.setEnabled(true);
                    btnResendOtp.setText("Gửi lại mã OTP");
                }
            }
        }.start();
    }

    private void showError(String msg) {
        if (tvOtpError != null) {
            if (msg == null || msg.isEmpty()) {
                tvOtpError.setVisibility(View.GONE);
            } else {
                tvOtpError.setVisibility(View.VISIBLE);
                tvOtpError.setText(msg);
            }
        }
    }

    private <T> String getResponseMessage(Response<ApiResponse<T>> response) {
        if (response.body() != null && response.body().message != null && !response.body().message.isEmpty()) {
            return response.body().message;
        }
        if (response.errorBody() != null) {
            try {
                String errStr = response.errorBody().string();
                com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(errStr).getAsJsonObject();
                if (json.has("message")) return json.get("message").getAsString();
            } catch (Exception ignored) {}
        }
        return "";
    }

    @Override
    public void onDestroyView() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroyView();
    }
}
