package dataAccess;

import model.UserData;

import java.util.ArrayList;

public class MemoryUserDAO implements UserDAO {
    private ArrayList<UserData> Users;

    public MemoryUserDAO() {
        Users = new ArrayList<>();
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        Users.add(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData u : Users) {
            if (u.username().equals(username)) {
                return u;
            }
        }
        return null;
    }
}
