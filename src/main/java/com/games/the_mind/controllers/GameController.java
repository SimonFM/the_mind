package com.games.the_mind.controllers;

import com.games.the_mind.api.error.ApiError;
import com.games.the_mind.model.TheMindGame;
import com.games.the_mind.services.GameManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/game")
public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    @Autowired
    GameManagerService gameManagerService;

    @PostMapping(value = "/create")
    @ResponseBody
    public HttpEntity<Object> create(@RequestBody TheMindGame gameConfig) {
        ApiError error = null;
        String name = null;
        if (gameConfig != null && gameConfig.getName() != null && !gameConfig.getName().isBlank()) {
            name = gameConfig.getName();
        }
        TheMindGame game = gameManagerService.create(name, "");

        if (game == null) {
            error = new ApiError(HttpStatus.NOT_FOUND);
        }
        return new HttpEntity<Object>(error != null ? error : game);

    }

    @GetMapping(value = "/{invite}")
    @ResponseBody
    public HttpEntity<Object> getGame(@PathVariable("invite") final String invite) throws Exception {
        ApiError error = null;
        TheMindGame game = null;
        if (invite == null || invite.isBlank()) {
            error = new ApiError(HttpStatus.BAD_REQUEST, "No invite code provided");
        } else {
            game = gameManagerService.getTheMindGameForByName(invite);
        }
        if (game == null) {
            error = new ApiError(HttpStatus.NOT_FOUND);
        }
        return new HttpEntity<Object>(error != null ? error : game);
    }

    @PutMapping(value = "join/{invite}/{user}")
    @ResponseBody
    public ResponseEntity join(@PathVariable("invite") final String invite, @PathVariable("user") final String userId) throws Exception {
        ApiError error = null;
        if (invite == null || invite.isBlank() || userId == null || userId.isBlank()) {
            return ResponseEntity.ok(new ApiError(HttpStatus.BAD_REQUEST, "Invalid params provided"));
        }
        TheMindGame game = gameManagerService.join(invite, userId);
        if (game != null) {
            return ResponseEntity.ok(game);
        }
        return ResponseEntity.notFound().build();
    }
}
