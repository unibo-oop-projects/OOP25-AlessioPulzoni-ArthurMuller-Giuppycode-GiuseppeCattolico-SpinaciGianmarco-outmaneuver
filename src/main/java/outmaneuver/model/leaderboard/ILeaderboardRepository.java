package outmaneuver.model.leaderboard;

import java.util.List;

import outmaneuver.model.session.ScoreEntry;

public interface ILeaderboardRepository {

    List<ScoreEntry> load();

    void persist(List<ScoreEntry> entries);
}
