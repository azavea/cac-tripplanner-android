package com.gophillygo.app.data;

import android.arch.lifecycle.ViewModel;

import com.gophillygo.app.data.models.AttractionFlag;

public class AttractionViewModel extends ViewModel {
    protected final DestinationRepository destinationRepository;

    AttractionViewModel(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    public void updateAttractionFlag(AttractionFlag flag) {
        destinationRepository.updateAttractionFlag(flag);
    }
}
