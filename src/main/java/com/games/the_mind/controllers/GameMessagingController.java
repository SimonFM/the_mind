package com.games.the_mind.controllers;

import com.games.the_mind.api.*;
import com.games.the_mind.api.error.ApiError;
import com.games.the_mind.model.Player;
import com.games.the_mind.model.TheMindGame;
import com.games.the_mind.services.GameManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class GameMessagingController {

    @Autowired
    GameManagerService gameManagerService;

    @MessageMapping("/connect")
    @SendToUser("/topic/game/connected")
    public Object connect(Principal principal) {
        Player player = gameManagerService.connect(principal);
        if (player != null) {
            return player;
        }
        return new ApiError(HttpStatus.NOT_FOUND, "Unable to connect the game");
    }

    @MessageMapping("/disconnect")
    @SendToUser("/topic/game/disconnected")
    public Object disconnect(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return new ApiError(HttpStatus.BAD_REQUEST, "Invalid params provided");
        }
        gameManagerService.disconnect(principal.getName());
        return new APIResponse(HttpStatus.OK, "Removed: " + principal.getName());
    }

    @MessageMapping("/get")
    @SendToUser("/topic/game/messages")
    public Object getGame(InviteMessage inviteMessage) throws Exception {
        ApiError error = null;
        TheMindGame game = null;
        String invite = inviteMessage == null ? null : inviteMessage.getInvite();

        if (invite == null || invite.isBlank()) {
            error = new ApiError(HttpStatus.BAD_REQUEST, "No invite code provided");
        } else {
            game = gameManagerService.getTheMindGameForByName(invite);
        }
        if (game == null) {
            error = new ApiError(HttpStatus.NOT_FOUND);
        }
        return error != null ? error : game;
    }

    @MessageMapping("/create")
    @SendToUser("/topic/game/create")
    public Object create(CreateMessage createMessage, Principal principal) {
        ApiError error = null;
        String name = null;
        if (createMessage != null && createMessage.getName() != null && !createMessage.getName().isBlank()) {
            name = createMessage.getName();
        }
        TheMindGame game = gameManagerService.create(name, principal.getName());

        if (game == null) {
            error = new ApiError(HttpStatus.NOT_FOUND);
        }
        return error != null ? error : game;
    }


    @MessageMapping("/join")
    @SendToUser("/topic/game/join")
    public Object join(JoinMessage joinMessage, Principal principal) {
        String invite = joinMessage == null ? null : joinMessage.getInvite();
        if (invite == null || invite.isBlank()) {
            return new ApiError(HttpStatus.BAD_REQUEST, "Invalid params provided");
        }
        TheMindGame game = gameManagerService.join(invite, principal.getName());
        if (game != null) {
            return game;
        }
        return new ApiError(HttpStatus.NOT_FOUND, "Unable to find the game");
    }


    @MessageMapping("/start")
    @SendToUser("/topic/game/messages")
    public Object start(RoomMessage roomMessage) {
        String roomId = roomMessage == null ? null : roomMessage.getRoomId();
        if (roomId == null || roomId.isBlank()) {
            return new ApiError(HttpStatus.BAD_REQUEST, "Invalid params provided");
        }
        gameManagerService.startGame(roomId);
        return new APIResponse(HttpStatus.OK, "Start Message received");
    }

    @MessageMapping("/card")
    @SendToUser("/topic/game/messages")
    public Object cardPlayed(RoomMessage roomMessage, Principal principal) {
        String roomId = roomMessage == null ? "" : roomMessage.getRoomName();
        String playerId = principal == null ? "" : principal.getName();
        int card = roomMessage == null ? -1 : (int) roomMessage.getData();
        if (roomId == null || roomId.isBlank() || playerId == null || playerId.isBlank() || card <= 0) {
            return new ApiError(HttpStatus.BAD_REQUEST, "Invalid params provided");
        }
        gameManagerService.playCard(roomId, playerId, card);
        return new APIResponse(HttpStatus.OK, "Card Message received");
    }
}
