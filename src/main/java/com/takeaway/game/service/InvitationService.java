package com.takeaway.game.service;

import com.takeaway.game.model.Invitation;
import com.takeaway.game.repository.InvitationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;

    public List<Invitation> getInvitations() {
        return invitationRepository.findAll().stream()
                .filter(invitation -> !invitation.getPlayerId().equals(getSessionID()))
                .collect(Collectors.toList());
    }

    private String getSessionID() {
        return RequestContextHolder.currentRequestAttributes().getSessionId();
    }
}
