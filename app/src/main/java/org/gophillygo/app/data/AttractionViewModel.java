package org.gophillygo.app.data;

import android.arch.lifecycle.ViewModel;

import org.gophillygo.app.data.models.AttractionFlag;

public class AttractionViewModel extends ViewModel {
    protected final DestinationRepository destinationRepository;

    AttractionViewModel(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    public void updateAttractionFlag(AttractionFlag flag, String userUuid, String apiKey) {
        destinationRepository.updateAttractionFlag(flag, userUuid, apiKey);
    }
}
