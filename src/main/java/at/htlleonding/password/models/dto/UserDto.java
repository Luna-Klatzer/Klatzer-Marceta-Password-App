package at.htlleonding.password.models.dto;

import at.htlleonding.password.models.User;

public class UserDto {
    public String username;
    public String phoneNumber;

    public UserDto(String username, String phoneNumber) {
        this.username = username;
        this.phoneNumber = phoneNumber;
    }

    public static UserDto fromUser(User user) {
        return new UserDto(user.getUsername(), user.getPhoneNumber());
    }
}
