package com.monbat.planning.models.dto.user_session;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SessionInfo {
    private boolean loggedIn;
    private String username;
}
