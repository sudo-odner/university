package com.example.lab2.presentation.viewmodel;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.lab2.data.datasource.SharedPreferencesDraftDataSource;
import com.example.lab2.data.repository.DraftRepositoryImpl;
import com.example.lab2.domain.repository.DraftRepository;
import com.example.lab2.domain.usecase.CalculatePriceUseCase;
import com.example.lab2.domain.usecase.ClearDraftUseCase;
import com.example.lab2.domain.usecase.GetDraftUseCase;
import com.example.lab2.domain.usecase.SaveDraftUseCase;
import com.example.lab2.domain.usecase.ValidateComplaintsUseCase;
import com.example.lab2.domain.usecase.ValidateDateUseCase;
import com.example.lab2.domain.usecase.ValidateDobUseCase;
import com.example.lab2.domain.usecase.ValidateFioUseCase;
import com.example.lab2.domain.usecase.ValidateOmsUseCase;
import com.example.lab2.domain.usecase.ValidatePhoneUseCase;
import com.example.lab2.domain.usecase.ValidateSpecialtyUseCase;
import com.example.lab2.domain.usecase.ValidateTimeUseCase;

public class MainViewModelFactory implements ViewModelProvider.Factory {

    private final Context appContext;

    public MainViewModelFactory(Context appContext) {
        this.appContext = appContext.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            DraftRepository draftRepository = new DraftRepositoryImpl(
                    new SharedPreferencesDraftDataSource(appContext)
            );

            return (T) new MainViewModel(
                    new ValidateFioUseCase(),
                    new ValidateDobUseCase(),
                    new ValidateOmsUseCase(),
                    new ValidatePhoneUseCase(),
                    new ValidateSpecialtyUseCase(),
                    new ValidateDateUseCase(),
                    new ValidateTimeUseCase(),
                    new ValidateComplaintsUseCase(),
                    new CalculatePriceUseCase(),
                    new SaveDraftUseCase(draftRepository),
                    new GetDraftUseCase(draftRepository),
                    new ClearDraftUseCase(draftRepository)
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
