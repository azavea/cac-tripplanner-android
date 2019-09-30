package org.gophillygo.app.data;

import androidx.lifecycle.ViewModel;

import org.gophillygo.app.data.models.AttractionFlag;

public class AttractionViewModel extends ViewModel {
    protected final DestinationRepository destinationRepository;

    AttractionViewModel(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    public void updateAttractionFlag(AttractionFlag flag, String userUuid, String apiKey, boolean postToServer) {
        destinationRepository.updateAttractionFlag(flag, userUuid, apiKey, postToServer);
    }
}
