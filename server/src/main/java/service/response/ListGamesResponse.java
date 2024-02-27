package service.response;

import model.GameData;

import java.util.Collection;
import java.util.List;

public record ListGamesResponse(Collection<GameData> games) {
}
