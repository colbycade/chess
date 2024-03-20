package model.request;

import chess.ChessGame.TeamColor;

public record JoinGameRequest(String authToken, TeamColor clientColor, Integer gameID) {
}
